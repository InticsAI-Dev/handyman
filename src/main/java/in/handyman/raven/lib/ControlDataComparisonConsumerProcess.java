package in.handyman.raven.lib;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.core.encryption.SecurityEngine;
import in.handyman.raven.core.encryption.impl.EncryptionRequestClass;
import in.handyman.raven.core.encryption.inticsgrity.InticsIntegrity;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.adapters.comparison.ComparisonAdapter;
import in.handyman.raven.lib.adapters.comparison.ComparisonAdapterFactory;
import in.handyman.raven.lib.model.controldatacomaprison.ControlDataComparisonQueryInputTable;
import in.handyman.raven.lib.ControlDataComparisonAction.ControlDataComparisonOutputTable;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.net.URL;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static in.handyman.raven.core.encryption.EncryptionConstants.ENCRYPT_ITEM_WISE_ENCRYPTION;

public class ControlDataComparisonConsumerProcess implements CoproProcessor.ConsumerProcess<ControlDataComparisonQueryInputTable, ControlDataComparisonOutputTable> {

    private final Logger log;
    private final Marker aMarker;
    private final ActionExecutionAudit action;
    private final String jdbiResourceName;
    private final String outputTable;
    private final ObjectMapper mapper = new ObjectMapper();

    public ControlDataComparisonConsumerProcess(Logger log, Marker aMarker, ActionExecutionAudit action, String jdbiResourceName, String outputTable) {
        this.log = log;
        this.aMarker = aMarker;
        this.action = action;
        this.jdbiResourceName = jdbiResourceName;
        this.outputTable = outputTable;
    }

    @Override
    public List<ControlDataComparisonOutputTable> process(URL endpoint, ControlDataComparisonQueryInputTable entity) throws Exception {
        List<ControlDataComparisonOutputTable> outputs = new ArrayList<>();
        try {
            String lowTouch = action.getContext().get("control.data.low.touch.threshold");
            String oneTouch = action.getContext().get("control.data.one.touch.threshold");
            String adapterKey = entity.getAllowedAdapter() != null ? entity.getAllowedAdapter() : "string";
            ComparisonAdapter adapter = ComparisonAdapterFactory.getAdapter(adapterKey);

            Long mismatchCount = adapter.validate(entity, action, log);
            String matchStatus = ControlDataComparisonAction.calculateValidationScores(mismatchCount, oneTouch, lowTouch);

            entity.setMatchStatus(matchStatus);
            entity.setMismatchCount(mismatchCount);

            InticsIntegrity encryptionHandler = SecurityEngine.getInticsIntegrityMethod(action, log);
            boolean itemWiseEncryption = Boolean.parseBoolean(action.getContext().getOrDefault(ENCRYPT_ITEM_WISE_ENCRYPTION, "false"));
            boolean actualEncryption = Boolean.parseBoolean(action.getContext().getOrDefault(ControlDataComparisonAction.ACTUAL_ENCRYPTION_VARIABLE, "false"));

            if (itemWiseEncryption) {
                List<EncryptionRequestClass> requests = buildEncryptionRequests(Collections.singletonList(entity), true);
                if (!requests.isEmpty()) {
                    List<EncryptionRequestClass> responses = encryptionHandler.encrypt(requests);
                    Map<String, List<EncryptionRequestClass>> grouped = groupResponsesByRecordId(responses);
                    applyEncryptedValues(Collections.singletonList(entity), grouped, true);
                }
            }

            if (actualEncryption) {
                List<EncryptionRequestClass> requests = buildEncryptionRequests(Collections.singletonList(entity), false);
                if (!requests.isEmpty()) {
                    List<EncryptionRequestClass> responses = encryptionHandler.encrypt(requests);
                    Map<String, List<EncryptionRequestClass>> grouped = groupResponsesByRecordId(responses);
                    applyEncryptedValues(Collections.singletonList(entity), grouped, false);
                }
            }

            ControlDataComparisonOutputTable out = new ControlDataComparisonOutputTable();
            out.setRootPipelineId(entity.getRootPipelineId());
            out.setCreatedOn(new Timestamp(Instant.now().toEpochMilli()));
            out.setGroupId(entity.getGroupId());
            out.setFileName(entity.getFileName());
            out.setOriginId(entity.getOriginId());
            out.setBatchId(entity.getBatchId());
            out.setPaperNo(entity.getPaperNo());
            out.setActualValue(entity.getActualValue());
            out.setExtractedValue(entity.getExtractedValue());
            out.setMatchStatus(entity.getMatchStatus());
            out.setMismatchCount(entity.getMismatchCount());
            out.setTenantId(entity.getTenantId());
            out.setClassification(determineClassification(entity.getActualValue(), entity.getExtractedValue(), entity.getMatchStatus()));
            out.setSorContainerId(entity.getSorContainerId());
            out.setSorItemName(entity.getSorItemName());
            out.setSorItemId(entity.getSorItemId());
            out.setStatus("COMPLETED");

            outputs.add(out);

            // 🔥 Insert into DB immediately
            insertExecutionInfo(Collections.singletonList(out));
        } catch (Exception e) {
            log.error(aMarker, "ControlDataComparison consumer processing failed for entity {} : {}", entity.getId(), e.getMessage(), e);
            throw e;
        }
        return outputs;
    }

    private void insertExecutionInfo(List<ControlDataComparisonOutputTable> records) {
        if (records.isEmpty()) return;
        Jdbi jdbi = in.handyman.raven.lambda.access.ResourceAccess.rdbmsJDBIConn(jdbiResourceName);
        jdbi.useHandle(handle -> {
            String sql = "INSERT INTO " + outputTable + " (" +
                    "root_pipeline_id, created_on, group_id, file_name, origin_id, batch_id, " +
                    "paper_no, actual_value, extracted_value, match_status, mismatch_count, " +
                    "tenant_id, classification, sor_container_id, sor_item_name, sor_item_id" +
                    ") VALUES (" +
                    ":rootPipelineId, :createdOn, :groupId, :fileName, :originId, :batchId, :paperNo, " +
                    ":actualValue, :extractedValue, :matchStatus, :mismatchCount, :tenantId, " +
                    ":classification, :sorContainerId, :sorItemName, :sorItemId" +
                    ")";
            PreparedBatch batch = handle.prepareBatch(sql);
            for (ControlDataComparisonOutputTable item : records) {
                batch.bind("rootPipelineId", item.getRowData().get(0))
                        .bind("createdOn", item.getRowData().get(1))
                        .bind("groupId", item.getRowData().get(2))
                        .bind("fileName", item.getRowData().get(3))
                        .bind("originId", item.getRowData().get(4))
                        .bind("batchId", item.getRowData().get(5))
                        .bind("paperNo", item.getRowData().get(6))
                        .bind("actualValue", item.getRowData().get(7))
                        .bind("extractedValue", item.getRowData().get(8))
                        .bind("matchStatus", item.getRowData().get(9))
                        .bind("mismatchCount", item.getRowData().get(10))
                        .bind("tenantId", item.getRowData().get(11))
                        .bind("classification", item.getRowData().get(12))
                        .bind("sorContainerId", item.getRowData().get(13))
                        .bind("sorItemName", item.getRowData().get(14))
                        .bind("sorItemId", item.getRowData().get(15))
                        .add();
            }
            batch.execute();
        });
    }

    // helper methods (encryption + classification) unchanged...
    private List<EncryptionRequestClass> buildEncryptionRequests(
            List<ControlDataComparisonQueryInputTable> records,
            boolean isExtracted
    ) {
        log.info("Building encryption requests (isExtracted={}): {}", isExtracted, records.size());
        List<EncryptionRequestClass> requests = new ArrayList<>();

        for (ControlDataComparisonQueryInputTable r : records) {
            if (!"t".equalsIgnoreCase(r.getIsEncrypted())) continue;

            String rawVal = isExtracted ? r.getExtractedValue() : r.getActualValue();
            if (rawVal == null || rawVal.isEmpty()) continue;

            if ("multi_value".equalsIgnoreCase(r.getLineItemType()) && rawVal.contains(",")) {
                addMultiValueRequests(requests, r, rawVal);
            } else {
                requests.add(newRequest(String.valueOf(r.getId()), rawVal, r.getEncryptionPolicy()));
            }
        }
        return requests;
    }
    private void addMultiValueRequests(List<EncryptionRequestClass> requests,
                                       ControlDataComparisonQueryInputTable record,
                                       String rawVal) {
        log.info("Adding multi-value requests for record ID: {}", record.getId());
        String[] parts = rawVal.split(",");
        for (int i = 0; i < parts.length; i++) {
            String trimmed = parts[i].trim();
            if (!trimmed.isEmpty()) {
                requests.add(newRequest(record.getId() + "_" + i, trimmed, record.getEncryptionPolicy()));
            }
        }
    }
    private EncryptionRequestClass newRequest(String key, String value, String policy) {
        log.info("Creating request - Key: {}, Policy: {}", key, policy);
        return EncryptionRequestClass.builder()
                .key(key)
                .value(value)
                .policy(policy)
                .build();
    }
    private void applyEncryptedValues(List<ControlDataComparisonQueryInputTable> records,
                                      Map<String, List<EncryptionRequestClass>> groupedResponses,
                                      boolean isExtracted) {
        log.info(aMarker, "Applying encrypted values to records (isExtracted={}): {}", isExtracted, records.size());
        for (ControlDataComparisonQueryInputTable r : records) {
            List<EncryptionRequestClass> respList = groupedResponses.get(String.valueOf(r.getId()));
            if (respList == null) continue;

            // Preserve order for multi-value
            respList.sort(Comparator.comparingInt(resp -> {
                if (!resp.getKey().contains("_")) return 0;
                return Integer.parseInt(resp.getKey().substring(resp.getKey().indexOf("_") + 1));
            }));

            String finalValue = respList.stream()
                    .map(EncryptionRequestClass::getValue)
                    .collect(Collectors.joining(","));

            if (isExtracted) {
                r.setExtractedValue(finalValue);
            } else {
                r.setActualValue(finalValue);
            }
        }
    }
    private Map<String, List<EncryptionRequestClass>> groupResponsesByRecordId(
            List<EncryptionRequestClass> responses
    ) {
        log.info("Grouping responses by record ID, total responses: {}", responses.size());
        return responses.stream()
                .collect(Collectors.groupingBy(resp -> {
                    String key = resp.getKey();
                    return key.contains("_") ? key.substring(0, key.indexOf("_")) : key;
                }));
    }

    private String determineClassification(String actualValue, String extractedValue, String matchStatus) {
        String normalizedActual = actualValue == null ? "" : actualValue.trim();
        String normalizedExtracted = extractedValue == null ? "" : extractedValue.trim();

        boolean actualEmpty = normalizedActual.isEmpty();
        boolean extractedEmpty = normalizedExtracted.isEmpty();

        if ("NO TOUCH".equals(matchStatus) && actualEmpty && extractedEmpty) {
            return "TN";
        }

        if ("NO TOUCH".equals(matchStatus) && !actualEmpty && !extractedEmpty) {
            return "TP";
        }

        if (actualEmpty && !extractedEmpty) {
            return "FN";
        }

        if (!actualEmpty && (extractedEmpty || !"NO TOUCH".equals(matchStatus))) {
            return "FP";
        }

        return "UNKNOWN";
    }

}
