package in.handyman.raven.lib;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import in.handyman.raven.lib.custom.outbound.dao.MetadataContext;
import in.handyman.raven.lib.custom.outbound.dao.PredictionDTO;
import in.handyman.raven.lib.custom.outbound.model.CustomResponseOutputTable;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CustomResponseGenerationConsumerProcessor implements CoproProcessor.ConsumerProcess<CustomResponseGenerationConsumerProcessor.CustomResponseGenerationInput, CustomResponseOutputTable> {
    private static final String CUSTOM_JSON_GENERATION_STRUCTURE = "custom.json.generation.structure";
    private final Logger log;
    private final Marker aMarker;
    private final ActionExecutionAudit action;
    private final ObjectMapper objectMapper;

    public CustomResponseGenerationConsumerProcessor(final Logger log, final Marker aMarker, final ActionExecutionAudit action) {
        this.log = log;
        this.aMarker = aMarker;
        this.action = action;
        this.objectMapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();
    }

    @Override
    public List<CustomResponseOutputTable> process(URL endpoint, CustomResponseGenerationInput entity) throws Exception {
        if (entity == null || entity.getPredictions() == null || entity.getPredictions().isEmpty()) {
            return Collections.emptyList();
        }

        List<PredictionDTO> predictionDTOList = entity.getPredictions();
        PredictionDTO first = predictionDTOList.get(0);
        String originId = entity.getOriginId();
        String metadata = first.getMetadataJson();
        Long groupId = first.getGroupId();
        Long tenantId = first.getTenantId();
        String batchId = first.getBatchId();
        String rootPipelineId = first.getRootPipelineId();
        Long parsedRootPipelineId = rootPipelineId != null ? Long.valueOf(rootPipelineId) : 0L;
        Integer parsedProcessId = rootPipelineId != null ? Integer.valueOf(rootPipelineId) : 0;

        String template = action.getContext().getOrDefault(CUSTOM_JSON_GENERATION_STRUCTURE, "{}");
        JsonNode generatedJson = generateCustomJson(template, predictionDTOList);
        String customResponseStr = objectMapper.writeValueAsString(generatedJson);
        String inboundTransactionId = extractInboundTransactionId(metadata);

        CustomResponseOutputTable out = CustomResponseOutputTable.builder()
                .processId(parsedProcessId)
                .groupId(groupId)
                .customResponse(customResponseStr)
                .originId(originId)
                .tenantId(tenantId)
                .rootPipelineId(parsedRootPipelineId)
                .status("SUCCESS")
                .stage("Custom Response Generation")
                .message("Custom Response generated successfully")
                .triggeredUrl("")
                .feature("Custom")
                .batchId(batchId)
                .inboundTransactionId(inboundTransactionId)
                .createdOn(LocalDateTime.now())
                .lastUpdatedOn(LocalDateTime.now())
                .build();

        log.debug(aMarker, "Custom response generated for originId {}", originId);
        return Collections.singletonList(out);
    }

    JsonNode generateCustomJson(String templateJson, List<PredictionDTO> predictions) throws Exception {
        JsonNode root = objectMapper.readTree(templateJson);
        Map<String, List<PredictionDTO>> bySorItem = predictions.stream()
                .filter(p -> p.getSorItemName() != null)
                .collect(Collectors.groupingBy(p -> p.getSorItemName().toLowerCase()));
        Map<String, List<PredictionDTO>> byNormalizedSorItem = predictions.stream()
                .filter(p -> p.getSorItemName() != null)
                .collect(Collectors.groupingBy(p -> normalizeKey(p.getSorItemName())));
        JsonNode filled = fillNode(root, bySorItem, byNormalizedSorItem);
        if (filled.isObject()) {
            ObjectNode target = (ObjectNode) filled;
            if (target.has("root") && target.get("root").isObject()) {
                target = (ObjectNode) target.get("root");
            }
            enrichRootEnvelope(target, predictions);
        }
        return filled;
    }

    private JsonNode fillNode(JsonNode node, Map<String, List<PredictionDTO>> bySorItem, Map<String, List<PredictionDTO>> byNormalizedSorItem) {
        if (node == null || node.isNull()) {
            return node;
        }
        if (node.isObject()) {
            ObjectNode obj = ((ObjectNode) node).deepCopy();

            if (obj.has("value")) {
                return obj;
            }

            List<String> fieldNames = new ArrayList<>();
            obj.fieldNames().forEachRemaining(fieldNames::add);
            for (String fieldName : fieldNames) {
                JsonNode child = obj.get(fieldName);
                if (isLeafTemplateNode(child)) {
                    PredictionDTO match = firstPredictionForLeaf((ObjectNode) child, fieldName, bySorItem, byNormalizedSorItem);
                    if (match != null) {
                        obj.set(fieldName, populateLeaf((ObjectNode) child.deepCopy(), match));
                    }
                } else {
                    obj.set(fieldName, fillNode(child, bySorItem, byNormalizedSorItem));
                }
            }
            return obj;
        }

        if (node.isArray()) {
            ArrayNode inputArray = (ArrayNode) node;
            ArrayNode resultArray = objectMapper.createArrayNode();
            if (inputArray.isEmpty()) {
                return resultArray;
            }

            // Template-driven additional properties:
            // each item should carry propName.value; propValue is filled from matching sorItemName.
            if (isTemplateDrivenPropertyArray(inputArray)) {
                for (JsonNode item : inputArray) {
                    ObjectNode itemCopy = (ObjectNode) item.deepCopy();
                    String propName = itemCopy.path("propName").path("value").asText("");
                    if (!propName.isEmpty()) {
                        PredictionDTO match = firstPrediction(propName, bySorItem, byNormalizedSorItem);
                        JsonNode propValueNode = itemCopy.get("propValue");
                        if (propValueNode != null && propValueNode.isObject() && ((ObjectNode) propValueNode).has("value")) {
                            ObjectNode populated = populateLeaf((ObjectNode) propValueNode.deepCopy(), match != null ? match : PredictionDTO.builder().build());
                            if (match == null) {
                                populated.put("value", "");
                            }
                            itemCopy.set("propValue", populated);
                        } else if (match != null) {
                            itemCopy.put("propValue", match.getPredictedValue() == null ? "" : match.getPredictedValue());
                            itemCopy.put("page", match.getPaperNo() == null ? 0 : match.getPaperNo());
                            int confidence = match.getPrecision() == null ? 0 : (int) Math.round(match.getPrecision() * 100);
                            itemCopy.put("confidence", confidence);
                            ObjectNode bbox = objectMapper.createObjectNode();
                            bbox.put("x", match.getLeftPos() == null ? 0 : match.getLeftPos());
                            bbox.put("width", match.getRightPos() == null ? 0 : match.getRightPos());
                            bbox.put("y", match.getUpperPos() == null ? 0 : match.getUpperPos());
                            bbox.put("height", match.getLowerPos() == null ? 0 : match.getLowerPos());
                            itemCopy.set("boundingBox", bbox);
                        }
                    }
                    resultArray.add(itemCopy);
                }
                return resultArray;
            }

            JsonNode templateItem = inputArray.get(0);
            if (!templateItem.isObject()) {
                inputArray.forEach(item -> resultArray.add(fillNode(item, bySorItem, byNormalizedSorItem)));
                return resultArray;
            }

            Map<String, List<PredictionDTO>> groupedByInstance = new HashMap<>();
            ObjectNode templateObject = (ObjectNode) templateItem;
            templateObject.fieldNames().forEachRemaining(field -> {
                List<PredictionDTO> matches = bySorItem.getOrDefault(field.toLowerCase(), Collections.emptyList());
                for (PredictionDTO p : matches) {
                    String instanceKey = p.getSorContainerInstance() == null || p.getSorContainerInstance().trim().isEmpty()
                            ? "0" : p.getSorContainerInstance();
                    groupedByInstance.computeIfAbsent(instanceKey, k -> new ArrayList<>()).add(p);
                }
            });

            if (groupedByInstance.isEmpty()) {
                resultArray.add(fillNode(templateItem, bySorItem, byNormalizedSorItem));
                return resultArray;
            }

            groupedByInstance.entrySet().stream()
                    .sorted(Comparator.comparing(Map.Entry::getKey))
                    .forEach(entry -> {
                        Map<String, List<PredictionDTO>> scopedBySor = entry.getValue().stream()
                                .filter(p -> p.getSorItemName() != null)
                                .collect(Collectors.groupingBy(p -> p.getSorItemName().toLowerCase()));
                        Map<String, List<PredictionDTO>> scopedByNormalizedSor = entry.getValue().stream()
                                .filter(p -> p.getSorItemName() != null)
                                .collect(Collectors.groupingBy(p -> normalizeKey(p.getSorItemName())));
                        resultArray.add(fillNode(templateItem.deepCopy(), scopedBySor, scopedByNormalizedSor));
                    });
            return resultArray;
        }
        return node;
    }

    private boolean isLeafTemplateNode(JsonNode node) {
        return node != null && node.isObject() && node.has("value");
    }

    private PredictionDTO firstPrediction(String fieldName, Map<String, List<PredictionDTO>> bySorItem, Map<String, List<PredictionDTO>> byNormalizedSorItem) {
        List<PredictionDTO> matches = bySorItem.get(fieldName.toLowerCase());
        if (matches != null && !matches.isEmpty()) {
            return matches.get(0);
        }
        List<PredictionDTO> normalizedMatches = byNormalizedSorItem.get(normalizeKey(fieldName));
        if (normalizedMatches == null || normalizedMatches.isEmpty()) {
            return null;
        }
        return normalizedMatches.get(0);
    }

    private PredictionDTO firstPredictionForLeaf(ObjectNode leafNode, String fieldName,
                                                 Map<String, List<PredictionDTO>> bySorItem,
                                                 Map<String, List<PredictionDTO>> byNormalizedSorItem) {
        String placeholderKey = extractPlaceholder(leafNode.path("value").asText(null));
        if (placeholderKey != null && !placeholderKey.isEmpty()) {
            PredictionDTO matchByPlaceholder = firstPrediction(placeholderKey, bySorItem, byNormalizedSorItem);
            if (matchByPlaceholder != null) {
                return matchByPlaceholder;
            }
        }
        return firstPrediction(fieldName, bySorItem, byNormalizedSorItem);
    }

    private ObjectNode populateLeaf(ObjectNode leaf, PredictionDTO p) {
        leaf.put("value", p.getPredictedValue() == null ? "" : p.getPredictedValue());
        leaf.put("page", p.getPaperNo() == null ? 0 : p.getPaperNo());
        int confidence = p.getPrecision() == null ? 0 : (int) Math.round(p.getPrecision() * 100);
        leaf.put("confidence", confidence);

        ObjectNode bbox = leaf.has("boundingBox") && leaf.get("boundingBox").isObject()
                ? (ObjectNode) leaf.get("boundingBox")
                : objectMapper.createObjectNode();
        bbox.put("x", p.getLeftPos() == null ? 0 : p.getLeftPos());
        bbox.put("width", p.getRightPos() == null ? 0 : p.getRightPos());
        bbox.put("y", p.getUpperPos() == null ? 0 : p.getUpperPos());
        bbox.put("height", p.getLowerPos() == null ? 0 : p.getLowerPos());
        leaf.set("boundingBox", bbox);
        return leaf;
    }

    private String extractInboundTransactionId(String metadataJson) {
        if (metadataJson == null || metadataJson.trim().isEmpty()) {
            return null;
        }
        try {
            MetadataContext metadataContext = objectMapper.readValue(metadataJson, MetadataContext.class);
            return metadataContext.getInboundTransactionId();
        } catch (Exception e) {
            log.warn(aMarker, "Failed to parse metadata for inboundTransactionId", e);
            return null;
        }
    }
    
    private boolean isTemplateDrivenPropertyArray(ArrayNode inputArray) {
        for (JsonNode item : inputArray) {
            if (!item.isObject()) {
                return false;
            }
            JsonNode propName = item.get("propName");
            if (propName == null || !propName.isObject() || !propName.has("value")) {
                return false;
            }
            JsonNode propValue = item.get("propValue");
            if (propValue == null) {
                return false;
            }
        }
        return inputArray.size() > 0;
    }

    private void enrichRootEnvelope(ObjectNode root, List<PredictionDTO> predictions) {
        if (predictions == null || predictions.isEmpty()) {
            return;
        }
        PredictionDTO first = predictions.get(0);
        MetadataContext metadataContext = parseMetadata(first.getMetadataJson());

        if (root.has("requestTxnId")) {
            String requestTxnId = first.getTransactionId();
            if ((requestTxnId == null || requestTxnId.isEmpty()) && metadataContext != null) {
                requestTxnId = metadataContext.getRequestTxnId();
            }
            if (requestTxnId != null) {
                root.put("requestTxnId", requestTxnId);
            }
        }
        if (root.has("status")) {
            root.put("status", "SUCCESS");
        }
        if (root.has("errorMessage")) {
            root.putNull("errorMessage");
        }
        if (root.has("errorMessageDetail")) {
            root.putNull("errorMessageDetail");
        }
        if (root.has("errorCd")) {
            root.putNull("errorCd");
        }
        if (root.has("documentId")) {
            String documentId = first.getOriginId();
            if ((documentId == null || documentId.isEmpty()) && metadataContext != null) {
                documentId = metadataContext.getDocumentId();
            }
            if (documentId != null) {
                root.put("documentId", documentId);
            }
        }
        if (root.has("inboundTransactionId")) {
            String inboundTransactionId = metadataContext != null ? metadataContext.getInboundTransactionId() : null;
            if ((inboundTransactionId == null || inboundTransactionId.isEmpty()) && first.getTransactionId() != null) {
                inboundTransactionId = first.getTransactionId();
            }
            if (inboundTransactionId != null) {
                root.put("inboundTransactionId", inboundTransactionId);
            }
        }

        if (root.has("metadata") && root.get("metadata").isObject()) {
            ObjectNode metadata = (ObjectNode) root.get("metadata");
            if (metadataContext != null) {
                if (metadata.has("documentType") && metadataContext.getDocumentType() != null) {
                    metadata.put("documentType", metadataContext.getDocumentType());
                }
                if (metadata.has("documentExtension") && metadataContext.getDocumentExtension() != null) {
                    metadata.put("documentExtension", metadataContext.getDocumentExtension());
                }
                if (metadata.has("transactionId") && metadataContext.getTransactionId() != null) {
                    metadata.put("transactionId", metadataContext.getTransactionId());
                } else if (metadata.has("transactionId") && first.getTransactionId() != null) {
                    metadata.put("transactionId", first.getTransactionId());
                }
                if (metadata.has("inboundDocumentName") && metadataContext.getInboundDocumentName() != null) {
                    metadata.put("inboundDocumentName", metadataContext.getInboundDocumentName());
                }
                if (metadata.has("processStartTime") && metadataContext.getProcessStartTime() != null) {
                    metadata.put("processStartTime", metadataContext.getProcessStartTime());
                }
                if (metadata.has("processEndTime") && metadataContext.getProcessEndTime() != null) {
                    metadata.put("processEndTime", metadataContext.getProcessEndTime());
                }
                if (metadata.has("processedAt") && metadataContext.getProcessedAt() != null) {
                    metadata.put("processedAt", metadataContext.getProcessedAt());
                }
                if (metadata.has("candidatePaper")) {
                    ArrayNode candidate = objectMapper.createArrayNode();
                    if (metadataContext.getCandidatePapers() != null) {
                        metadataContext.getCandidatePapers().forEach(candidate::add);
                    }
                    metadata.set("candidatePaper", candidate);
                }
                if (metadata.has("processingTimeMs") && metadataContext.getProcessStartTime() != null && metadataContext.getProcessEndTime() != null) {
                    try {
                        LocalDateTime s = LocalDateTime.parse(metadataContext.getProcessStartTime());
                        LocalDateTime e = LocalDateTime.parse(metadataContext.getProcessEndTime());
                        metadata.put("processingTimeMs", Duration.between(s, e).toMillis());
                    } catch (Exception ignored) {
                    }
                }
            }
            if (metadata.has("pageCount")) {
                int pageCount = predictions.stream()
                        .map(PredictionDTO::getPaperNo)
                        .filter(p -> p != null)
                        .max(Integer::compareTo)
                        .orElse(0);
                metadata.put("pageCount", pageCount);
            }
            if (metadata.has("overallConfidence")) {
                int overall = (int) Math.round(predictions.stream()
                        .map(PredictionDTO::getPrecision)
                        .filter(p -> p != null)
                        .mapToDouble(Double::doubleValue)
                        .average()
                        .orElse(0.0) * 100);
                metadata.put("overallConfidence", overall);
            }
        }
    }

    private MetadataContext parseMetadata(String metadataJson) {
        if (metadataJson == null || metadataJson.trim().isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(metadataJson, MetadataContext.class);
        } catch (Exception e) {
            return null;
        }
    }

    private String extractPlaceholder(String valueTemplate) {
        if (valueTemplate == null) {
            return null;
        }
        String trimmed = valueTemplate.trim();
        if (trimmed.startsWith("${") && trimmed.endsWith("}") && trimmed.length() > 3) {
            return trimmed.substring(2, trimmed.length() - 1).trim();
        }
        return null;
    }

    private String normalizeKey(String key) {
        if (key == null) {
            return "";
        }
        return key.replaceAll("[^A-Za-z0-9]", "").toLowerCase();
    }

    @Data
    @Builder
    @AllArgsConstructor
    public static class CustomResponseGenerationInput {
        private String originId;
        private List<PredictionDTO> predictions;
    }

}
