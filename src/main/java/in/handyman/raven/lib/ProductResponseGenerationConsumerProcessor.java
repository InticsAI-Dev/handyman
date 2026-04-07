package in.handyman.raven.lib;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import in.handyman.raven.lib.custom.outbound.dao.MetadataContext;
import in.handyman.raven.lib.custom.outbound.dao.PredictionDTO;
import in.handyman.raven.lib.custom.outbound.model.ProductResponseOutputTable;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class ProductResponseGenerationConsumerProcessor implements CoproProcessor.ConsumerProcess<PredictionDTO, ProductResponseOutputTable> {
    private final Logger log;
    private final Marker aMarker;
    private final ActionExecutionAudit action;

    public ProductResponseGenerationConsumerProcessor(final Logger log, final Marker aMarker, final ActionExecutionAudit action) {
        this.log = log;
        this.aMarker = aMarker;
        this.action = action;
    }

    @Override
    public List<ProductResponseOutputTable> process(URL endpoint, PredictionDTO entity) throws Exception {
        if (entity == null) {
            return Collections.emptyList();
        }

        final List<PredictionDTO> predictions = Collections.singletonList(entity);
        final String originId = entity.getOriginId();
        final String metadata = entity.getMetadataJson();
        final Long groupId = entity.getGroupId();
        final Long tenantId = entity.getTenantId();
        final String batchId = entity.getBatchId();
        final String rootPipelineId = entity.getRootPipelineId();
        final String transactionId = entity.getTransactionId();

        ObjectMapper objectMapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();

        JsonNode productResponseJson = buildProductResponseJson(
                predictions,
                originId,
                tenantId,
                groupId,
                batchId,
                rootPipelineId,
                transactionId,
                metadata,
                objectMapper
        );

        String productResponseStr = objectMapper.writeValueAsString(productResponseJson);
        String inboundTransactionId = extractInboundTransactionId(metadata, objectMapper);
        Long parsedRootPipelineId = rootPipelineId != null ? Long.valueOf(rootPipelineId) : 0L;
        Integer parsedProcessId = rootPipelineId != null ? Integer.valueOf(rootPipelineId) : 0;

        ProductResponseOutputTable out = ProductResponseOutputTable.builder()
                .processId(parsedProcessId)
                .groupId(groupId)
                .productResponse(productResponseStr)
                .originId(originId)
                .tenantId(tenantId)
                .rootPipelineId(parsedRootPipelineId)
                .status("COMPLETED")
                .stage("Product Response Generation")
                .message("Product Response generated successfully")
                .triggeredUrl("")
                .feature("Product")
                .batchId(batchId)
                .inboundTransactionId(inboundTransactionId)
                .createdOn(LocalDateTime.now())
                .lastUpdatedOn(LocalDateTime.now())
                .build();

        return Collections.singletonList(out);
    }

    private JsonNode buildProductResponseJson(
            List<PredictionDTO> predictions,
            String originId,
            Long tenantId,
            Long groupId,
            String batchId,
            String rootPipelineId,
            String transactionId,
            String metadataJson,
            ObjectMapper objectMapper) throws JsonProcessingException {

        log.info(aMarker, "Building product response JSON for originId: {}, predictions: {}", originId, predictions.size());

        ObjectNode documentInfo = objectMapper.createObjectNode();
        documentInfo.put("originId", originId);
        MetadataContext metadataContext = null;
        documentInfo.put("extension", "");
        documentInfo.put("totalProcessedDuration", 0);

        if (metadataJson != null && !metadataJson.trim().isEmpty()) {
            try {
                metadataContext = objectMapper.readValue(metadataJson, MetadataContext.class);
                if (metadataContext != null) {
                    if (metadataContext.getDocumentId() != null) {
                        documentInfo.put("documentId", metadataContext.getDocumentId());
                    }
                    if (metadataContext.getProcessStartTime() != null) {
                        documentInfo.put("processStartedOn", metadataContext.getProcessStartTime());
                    }
                    if (metadataContext.getProcessEndTime() != null) {
                        documentInfo.put("processCompletedOn", metadataContext.getProcessEndTime());
                    }
                    documentInfo.put("extension", metadataContext.getDocumentExtension() != null ?
                            metadataContext.getDocumentExtension() : "");
                    if (metadataContext.getProcessStartTime() != null && metadataContext.getProcessEndTime() != null) {
                        try {
                            LocalDateTime startTime = LocalDateTime.parse(metadataContext.getProcessStartTime());
                            LocalDateTime endTime = LocalDateTime.parse(metadataContext.getProcessEndTime());
                            long durationSeconds = Duration.between(startTime, endTime).getSeconds();
                            documentInfo.put("totalProcessedDuration", (int) durationSeconds);
                        } catch (Exception e) {
                            log.warn(aMarker, "Failed to calculate totalProcessedDuration", e);
                            documentInfo.put("totalProcessedDuration", 0);
                        }
                    }
                }
            } catch (Exception e) {
                log.warn(aMarker, "Failed to parse metadata JSON for originId: {}", originId, e);
            }
        }

        Map<String, String> context = action != null && action.getContext() != null ? action.getContext() : new HashMap<>();
        String sourceFileURI = constructSourceFileURI(originId, tenantId, metadataContext, context);
        String preprocessedFileURI = constructPreprocessedFileURI(originId, tenantId, metadataContext, context);
        documentInfo.put("sourceFileURI", sourceFileURI != null ? sourceFileURI : "");
        documentInfo.put("preprocessedFileURI", preprocessedFileURI != null ? preprocessedFileURI : "");

        Map<Integer, List<PredictionDTO>> predictionsByPage = predictions.stream()
                .filter(p -> p.getPaperNo() != null)
                .collect(Collectors.groupingBy(
                        PredictionDTO::getPaperNo,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        int totalPages = predictionsByPage.isEmpty() ? 0 :
                predictionsByPage.keySet().stream().mapToInt(Integer::intValue).max().orElse(0);
        documentInfo.put("totalPages", totalPages);

        ArrayNode paperInfoArray = objectMapper.createArrayNode();
        predictionsByPage.forEach((pageNo, pagePredictions) -> {
            ObjectNode paperInfo = buildPaperInfo(pageNo, pagePredictions, objectMapper);
            paperInfoArray.add(paperInfo);
        });

        documentInfo.set("paperInfo", paperInfoArray);
        buildSummarySections(predictions, documentInfo, objectMapper);
        log.info(aMarker, "Completed building product response JSON for originId: {}", originId);
        return documentInfo;
    }

    private ObjectNode buildPaperInfo(
            Integer pageNo,
            List<PredictionDTO> pagePredictions,
            ObjectMapper objectMapper) {

        ObjectNode paperInfo = objectMapper.createObjectNode();
        paperInfo.put("pageNo", pageNo);
        paperInfo.putNull("paperType");

        Map<String, List<PredictionDTO>> predictionsByFeature = pagePredictions.stream()
                .filter(p -> p.getFeature() != null)
                .collect(Collectors.groupingBy(PredictionDTO::getFeature));

        ObjectNode featureInfo = objectMapper.createObjectNode();
        featureInfo.putNull("kie");
        featureInfo.putNull("checkbox");
        featureInfo.putNull("table");
        featureInfo.putNull("currency");
        featureInfo.putNull("bulletin");
        featureInfo.putNull("paragraph");
        featureInfo.putNull("aggregatedTableResult");

        predictionsByFeature.forEach((feature, featurePredictions) -> {
            switch (feature.toUpperCase()) {
                case "KIE":
                    buildKieFeature(featureInfo, featurePredictions, objectMapper);
                    break;
                case "CHECKBOX_EXTRACTION":
                case "CHECKBOX_DETECT":
                    buildCheckboxFeature(featureInfo, featurePredictions, objectMapper);
                    break;
                case "TABLE_EXTRACT":
                case "TABLE_DETECT":
                    buildTableFeature(featureInfo, featurePredictions, objectMapper);
                    break;
                case "CURRENCY_DETECTION":
                    buildCurrencyFeature(featureInfo, featurePredictions, objectMapper);
                    break;
                case "BULLETIN_EXTRACTION":
                    buildBulletinFeature(featureInfo, featurePredictions, objectMapper);
                    break;
                case "PARAGRAPH_EXTRACTION":
                    buildParagraphFeature(featureInfo, featurePredictions, objectMapper);
                    break;
                case "TABLE_EXTRACT_AGGREGATE":
                    buildTableAggregateFeature(featureInfo, featurePredictions, objectMapper);
                    break;
                default:
                    break;
            }
        });

        paperInfo.set("featureInfo", featureInfo);
        return paperInfo;
    }

    private void buildKieFeature(ObjectNode featureInfo, List<PredictionDTO> predictions, ObjectMapper objectMapper) {
        if (predictions == null || predictions.isEmpty()) {
            return;
        }
        ObjectNode kie = objectMapper.createObjectNode();
        PredictionDTO firstPrediction = predictions.get(0);
        kie.put("originId", firstPrediction.getOriginId() != null ? firstPrediction.getOriginId() : "");
        kie.put("tenantId", firstPrediction.getTenantId() != null ? firstPrediction.getTenantId() : 0L);
        kie.put("pageNo", firstPrediction.getPaperNo() != null ? firstPrediction.getPaperNo() : 0);

        Map<String, List<PredictionDTO>> byContainer = predictions.stream()
                .filter(p -> p.getContainerName() != null)
                .collect(Collectors.groupingBy(PredictionDTO::getContainerName));

        ObjectNode entityDetails = objectMapper.createObjectNode();
        ObjectNode sorContainerDetails = objectMapper.createObjectNode();

        byContainer.forEach((containerName, containerPredictions) -> {
            ObjectNode sorContainer = objectMapper.createObjectNode();
            sorContainer.put("sorContainerName", containerName.replace(" ", "_"));
            sorContainer.put("naturalKey", containerName);
            sorContainer.put("originId", predictions.get(0).getOriginId());
            sorContainer.put("tenantId", predictions.get(0).getTenantId());
            sorContainer.put("pageNo", predictions.get(0).getPaperNo());

            ObjectNode sorItemsDetails = objectMapper.createObjectNode();
            containerPredictions.forEach(prediction -> {
                if (prediction.getSorItemName() != null) {
                    ObjectNode sorItem = objectMapper.createObjectNode();
                    sorItem.put("sorItemName", prediction.getSorItemName().replace(" ", "_"));
                    sorItem.put("naturalKey", prediction.getSorItemName());
                    sorItem.put("predictedValue", prediction.getPredictedValue() != null ? prediction.getPredictedValue() : "");
                    sorItem.put("confidenceScore", prediction.getPrecision() != null ?
                            (int) (prediction.getPrecision() * 100) : 0);
                    sorItem.put("originId", prediction.getOriginId());
                    sorItem.put("tenantId", prediction.getTenantId());
                    sorItem.put("pageNo", prediction.getPaperNo());
                    sorItem.putNull("paperType");

                    if (prediction.getLeftPos() != null && prediction.getRightPos() != null &&
                            prediction.getUpperPos() != null && prediction.getLowerPos() != null &&
                            (prediction.getLeftPos() != 0.0 || prediction.getRightPos() != 0.0 ||
                                    prediction.getUpperPos() != 0.0 || prediction.getLowerPos() != 0.0)) {
                        ObjectNode boundingBox = objectMapper.createObjectNode();
                        boundingBox.put("leftPosition", prediction.getLeftPos());
                        boundingBox.put("rightPosition", prediction.getRightPos());
                        boundingBox.put("upperPosition", prediction.getUpperPos());
                        boundingBox.put("lowerPosition", prediction.getLowerPos());
                        sorItem.set("boundingBox", boundingBox);
                    } else {
                        sorItem.putNull("boundingBox");
                    }
                    sorItemsDetails.set(prediction.getSorItemName().replace(" ", "_"), sorItem);
                }
            });

            if (sorItemsDetails.size() > 0) {
                sorContainer.set("sorItemsDetails", sorItemsDetails);
            } else {
                sorContainer.putNull("sorItemsDetails");
            }
            sorContainerDetails.set(containerName.replace(" ", "_"), sorContainer);
        });

        entityDetails.set("sorContainerDetails", sorContainerDetails);
        kie.set("entityDetails", entityDetails);
        featureInfo.set("kie", kie);
    }

    private void buildCheckboxFeature(ObjectNode featureInfo, List<PredictionDTO> predictions, ObjectMapper objectMapper) {
        ObjectNode checkbox = objectMapper.createObjectNode();
        checkbox.put("originId", predictions.get(0).getOriginId());
        checkbox.put("tenantId", predictions.get(0).getTenantId());
        checkbox.put("pageNo", predictions.get(0).getPaperNo());

        ArrayNode checkboxItems = objectMapper.createArrayNode();
        predictions.forEach(prediction -> {
            ObjectNode checkboxItem = objectMapper.createObjectNode();
            checkboxItem.put("originId", prediction.getOriginId());
            checkboxItem.put("tenantId", prediction.getTenantId());
            checkboxItem.put("predictedValue", prediction.getPredictedValue() != null ? prediction.getPredictedValue() : "");
            checkboxItem.put("confidenceScore", prediction.getPrecision() != null ?
                    (int) (prediction.getPrecision() * 100) : 0);
            checkboxItem.put("checkboxState", prediction.getState() != null ? prediction.getState() : "");
            checkboxItem.put("pageNo", prediction.getPaperNo());

            if (prediction.getLeftPos() != null && prediction.getRightPos() != null &&
                    prediction.getUpperPos() != null && prediction.getLowerPos() != null) {
                ObjectNode boundingBox = objectMapper.createObjectNode();
                boundingBox.put("leftPosition", prediction.getLeftPos());
                boundingBox.put("rightPosition", prediction.getRightPos());
                boundingBox.put("upperPosition", prediction.getUpperPos());
                boundingBox.put("lowerPosition", prediction.getLowerPos());
                checkboxItem.set("boundingBox", boundingBox);
            }

            checkboxItems.add(checkboxItem);
        });

        checkbox.set("checkboxItems", checkboxItems);
        featureInfo.set("checkbox", checkbox);
    }

    private void buildTableFeature(ObjectNode featureInfo, List<PredictionDTO> predictions, ObjectMapper objectMapper) {
        ObjectNode table = objectMapper.createObjectNode();
        table.put("originId", predictions.get(0).getOriginId());
        table.put("tenantId", predictions.get(0).getTenantId());
        table.put("pageNo", predictions.get(0).getPaperNo());

        ArrayNode tableDataArray = objectMapper.createArrayNode();

        predictions.forEach(prediction -> {
            if (prediction.getTableData() != null && !prediction.getTableData().trim().isEmpty()) {
                try {
                    ObjectNode tableData = objectMapper.createObjectNode();
                    tableData.put("originId", prediction.getOriginId());
                    tableData.put("tenantId", prediction.getTenantId());
                    tableData.put("pageNo", prediction.getPaperNo());
                    tableData.put("csvFilePath", prediction.getCsvFilePath() != null ? prediction.getCsvFilePath() : "");

                    JsonNode tableDataJson = objectMapper.readTree(prediction.getTableData());
                    if (tableDataJson.has("columnHeaders")) {
                        tableData.set("columnHeaders", tableDataJson.get("columnHeaders"));
                    }
                    if (tableDataJson.has("data")) {
                        tableData.set("rowData", tableDataJson.get("data"));
                    }

                    tableDataArray.add(tableData);
                } catch (Exception e) {
                    log.warn(aMarker, "Failed to parse table data for prediction: {}", prediction.getPredictionId(), e);
                }
            }
        });

        table.set("tableData", tableDataArray);
        featureInfo.set("table", table);
    }

    private void buildCurrencyFeature(ObjectNode featureInfo, List<PredictionDTO> predictions, ObjectMapper objectMapper) {
        ArrayNode currencyArray = objectMapper.createArrayNode();

        predictions.forEach(prediction -> {
            ObjectNode currencyNode = objectMapper.createObjectNode();
            currencyNode.put("currency", prediction.getCurrencyValue() != null ? prediction.getCurrencyValue() : "");
            currencyNode.put("asciiValue", prediction.getCurrencyAsciiValue() != null ? prediction.getCurrencyAsciiValue() : "");
            currencyNode.put("confidenceScore", prediction.getPrecision() != null ?
                    (int) (prediction.getPrecision() * 100) : 0);
            currencyArray.add(currencyNode);
        });

        featureInfo.set("currency", currencyArray);
    }

    private void buildBulletinFeature(ObjectNode featureInfo, List<PredictionDTO> predictions, ObjectMapper objectMapper) {
        ArrayNode bulletinArray = objectMapper.createArrayNode();

        predictions.forEach(prediction -> {
            ObjectNode bulletinNode = objectMapper.createObjectNode();
            bulletinNode.put("bulletinHeader", prediction.getBulletinSection() != null ? prediction.getBulletinSection() : "");
            bulletinNode.put("bulletinPoints", prediction.getBulletinPoints() != null ? prediction.getBulletinPoints() : "");
            bulletinArray.add(bulletinNode);
        });

        featureInfo.set("bulletin", bulletinArray);
    }

    private void buildParagraphFeature(ObjectNode featureInfo, List<PredictionDTO> predictions, ObjectMapper objectMapper) {
        ArrayNode paragraphArray = objectMapper.createArrayNode();

        predictions.forEach(prediction -> {
            if (prediction.getSorItemName() != null) {
                ObjectNode paragraphNode = objectMapper.createObjectNode();
                ObjectNode innerMap = objectMapper.createObjectNode();
                innerMap.put("paragraphHeader", prediction.getParagraphSection() != null ? prediction.getParagraphSection() : "");
                innerMap.put("paragraphPoints", prediction.getParagraphPoints() != null ? prediction.getParagraphPoints() : "");
                paragraphNode.set(prediction.getSorItemName(), innerMap);
                paragraphArray.add(paragraphNode);
            }
        });

        featureInfo.set("paragraph", paragraphArray);
    }

    private void buildTableAggregateFeature(ObjectNode featureInfo, List<PredictionDTO> predictions, ObjectMapper objectMapper) {
        ArrayNode aggregateArray = objectMapper.createArrayNode();

        predictions.forEach(prediction -> {
            if (prediction.getAggregatedJson() != null && !prediction.getAggregatedJson().trim().isEmpty()) {
                try {
                    ObjectNode aggregateData = objectMapper.createObjectNode();
                    aggregateData.put("originId", prediction.getOriginId());
                    aggregateData.put("tenantId", prediction.getTenantId());
                    aggregateData.put("sorItemName", prediction.getSorItemName() != null ? prediction.getSorItemName() : "");
                    aggregateData.put("pageNo", prediction.getPaperNo());
                    aggregateData.put("sorContainerName", prediction.getContainerName() != null ? prediction.getContainerName() : "");

                    JsonNode aggregatedJson = objectMapper.readTree(prediction.getAggregatedJson());
                    aggregateData.set("aggregatedjson", aggregatedJson);
                    aggregateArray.add(aggregateData);
                } catch (Exception e) {
                    log.warn(aMarker, "Failed to parse aggregated JSON for prediction: {}", prediction.getPredictionId(), e);
                }
            }
        });

        featureInfo.set("aggregatedTableResult", aggregateArray);
    }

    private void buildSummarySections(List<PredictionDTO> allPredictions, ObjectNode documentInfo, ObjectMapper objectMapper) {
        ObjectNode kvpSummary = objectMapper.createObjectNode();
        Map<String, List<PredictionDTO>> kvpPredictions = allPredictions.stream()
                .filter(p -> "KIE".equalsIgnoreCase(p.getFeature()) && p.getContainerName() != null)
                .collect(Collectors.groupingBy(PredictionDTO::getContainerName));

        kvpPredictions.forEach((containerName, predictions) -> {
            ObjectNode containerNode = objectMapper.createObjectNode();
            predictions.forEach(prediction -> {
                if (prediction.getSorItemName() != null) {
                    String value = prediction.getPredictedValue();
                    if (value != null && !value.trim().isEmpty()) {
                        containerNode.put(prediction.getSorItemName(), value);
                    } else {
                        containerNode.putNull(prediction.getSorItemName());
                    }
                }
            });
            kvpSummary.set(containerName, containerNode);
        });
        documentInfo.set("kvpSummary", kvpSummary);

        ObjectNode kvpCfScoreSummary = objectMapper.createObjectNode();
        kvpPredictions.forEach((containerName, predictions) -> {
            ObjectNode containerNode = objectMapper.createObjectNode();
            predictions.forEach(prediction -> {
                if (prediction.getSorItemName() != null) {
                    if (prediction.getPrecision() != null) {
                        containerNode.put(prediction.getSorItemName(), (int) (prediction.getPrecision() * 100));
                    } else {
                        containerNode.putNull(prediction.getSorItemName());
                    }
                }
            });
            kvpCfScoreSummary.set(containerName, containerNode);
        });
        documentInfo.set("kvpCfScoreSummary", kvpCfScoreSummary);

        documentInfo.set("tableDataSummary", objectMapper.createArrayNode());

        ArrayNode currencySummary = objectMapper.createArrayNode();
        allPredictions.stream()
                .filter(p -> "CURRENCY_DETECTION".equalsIgnoreCase(p.getFeature()))
                .forEach(prediction -> {
                    ObjectNode currencyNode = objectMapper.createObjectNode();
                    currencyNode.put("currency", prediction.getCurrencyValue() != null ? prediction.getCurrencyValue() : "");
                    currencyNode.put("asciiValue", prediction.getCurrencyAsciiValue() != null ? prediction.getCurrencyAsciiValue() : "");
                    currencyNode.put("confidenceScore", prediction.getPrecision() != null ?
                            (int) (prediction.getPrecision() * 100) : 0);
                    currencySummary.add(currencyNode);
                });
        documentInfo.set("currencySummary", currencySummary);

        ArrayNode bulletinSummary = objectMapper.createArrayNode();
        allPredictions.stream()
                .filter(p -> "BULLETIN_EXTRACTION".equalsIgnoreCase(p.getFeature()))
                .forEach(prediction -> {
                    ObjectNode bulletinNode = objectMapper.createObjectNode();
                    bulletinNode.put("bulletinHeader", prediction.getBulletinSection() != null ? prediction.getBulletinSection() : "");
                    bulletinNode.put("bulletinPoints", prediction.getBulletinPoints() != null ? prediction.getBulletinPoints() : "");
                    bulletinSummary.add(bulletinNode);
                });
        documentInfo.set("bulletinSummary", bulletinSummary);

        ArrayNode paragraphSummary = objectMapper.createArrayNode();
        allPredictions.stream()
                .filter(p -> "PARAGRAPH_EXTRACTION".equalsIgnoreCase(p.getFeature()))
                .forEach(prediction -> {
                    if (prediction.getSorItemName() != null) {
                        ObjectNode paragraphNode = objectMapper.createObjectNode();
                        ObjectNode innerMap = objectMapper.createObjectNode();
                        innerMap.put("paragraphHeader", prediction.getParagraphSection() != null ? prediction.getParagraphSection() : "");
                        innerMap.put("paragraphPoints", prediction.getParagraphPoints() != null ? prediction.getParagraphPoints() : "");
                        paragraphNode.set(prediction.getSorItemName(), innerMap);
                        paragraphSummary.add(paragraphNode);
                    }
                });
        documentInfo.set("paragraphSummary", paragraphSummary);
    }

    private String constructSourceFileURI(String originId, Long tenantId, MetadataContext metadataContext, Map<String, String> context) {
        if (context == null) {
            context = new HashMap<>();
        }
        String alchemyBaseURL = context.getOrDefault("alchemy.base.url", "");
        String outboundFileUriFormat = context.getOrDefault("outbound.file.uri.format.specifer", "api");

        if ("file".equals(outboundFileUriFormat) && metadataContext != null &&
                metadataContext.getInboundDocumentName() != null && metadataContext.getTransactionId() != null) {
            return alchemyBaseURL + "/alchemy/vulcan_data/data/output/" + tenantId + "/transaction/" +
                    metadataContext.getTransactionId() + "/" + metadataContext.getInboundDocumentName();
        } else {
            if (tenantId != null) {
                return alchemyBaseURL + "/response/file/source/" + originId + "?tenantId=" + tenantId;
            } else {
                return alchemyBaseURL + "/response/file/source/" + originId;
            }
        }
    }

    private String constructPreprocessedFileURI(String originId, Long tenantId, MetadataContext metadataContext, Map<String, String> context) {
        if (context == null) {
            context = new HashMap<>();
        }
        String alchemyBaseURL = context.getOrDefault("alchemy.base.url", "");
        String outboundFileUriFormat = context.getOrDefault("outbound.file.uri.format.specifer", "api");

        if ("file".equals(outboundFileUriFormat)) {
            return "";
        } else {
            if (tenantId != null) {
                return alchemyBaseURL + "/response/file/preprocessed/" + originId + "?tenantId=" + tenantId;
            } else {
                return alchemyBaseURL + "/response/file/preprocessed/" + originId;
            }
        }
    }

    private String extractInboundTransactionId(String metadataJson, ObjectMapper objectMapper) {
        if (metadataJson == null || metadataJson.trim().isEmpty()) {
            return null;
        }
        try {
            MetadataContext metadataContext = objectMapper.readValue(metadataJson, MetadataContext.class);
            return metadataContext.getInboundTransactionId();
        } catch (Exception e) {
            log.warn(aMarker, "Failed to extract inboundTransactionId from metadata", e);
            return null;
        }
    }

}
