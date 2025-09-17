package in.handyman.raven.lib;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.core.encryption.SecurityEngine;
import in.handyman.raven.core.encryption.impl.EncryptionRequestClass;
import in.handyman.raven.core.encryption.inticsgrity.InticsIntegrity;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.access.ResourceAccess;
import in.handyman.raven.lambda.action.ActionExecution;
import in.handyman.raven.lambda.action.IActionExecution;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.ControlDataComparison;
import in.handyman.raven.lib.model.controldatacomaprison.ControlDataComparisonQueryInputTable;
import in.handyman.raven.lib.utils.CustomBatchWithScaling;
import in.handyman.raven.util.CommonQueryUtil;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.Query;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.jetbrains.annotations.NotNull;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static in.handyman.raven.core.encryption.EncryptionConstants.ENCRYPT_ITEM_WISE_ENCRYPTION;

@ActionExecution(actionName = "ControlDataComparison")
public class ControlDataComparisonAction implements IActionExecution{
    private static final String DEFAULT_SOCKET_TIMEOUT = "100";
    private static final String THREAD_SLEEP_TIME_DEFAULT = "1000";
    private static final String INSERT_INTO = "INSERT INTO";
    public static final String COLUMN_LIST = "created_on, created_user_id, last_updated_on, last_updated_user_id, input_file_path," +
            " total_response_json, paper_no, origin_id, process_id, action_id, process, group_id, tenant_id, " +
            "root_pipeline_id, batch_id, model_registry, status, stage, message, category,request,response,endpoint,sor_container_id";
    public static final String VAL_STRING_LIST = "VALUES( ?,?,?,?,?," +
            "?,?,?,?,?" +
            ",?,?,?,?,?," +
            "?,?, ?, ?" +
            ",?,?,?,?, ?)";

    private final ActionExecutionAudit action;
    private final Logger log;
    private ControlDataComparison controlDataComparison = new ControlDataComparison();
    private final Marker aMarker;

    private final int threadSleepTime;
    private final int writeBatchSize;
    private int readBatchSize;
    private final int timeout;

    private final String targetTableName;
    private final String controlDataComparisonUrl;
    private final String insertQuery;
    public static final String ACTUAL_ENCRYPTION_VARIABLE = "actual.encryption.variable";

    public ControlDataComparisonAction(ActionExecutionAudit action, Logger log, Object controlDataComparison) {
        this.action = action;
        this.log = log;
        this.controlDataComparison = (ControlDataComparison) controlDataComparison;
        this.aMarker = MarkerFactory.getMarker("ControlDataComparison:" + this.controlDataComparison.getName());
        this.timeout = parseContextValue(action, "copro.client.socket.timeout", DEFAULT_SOCKET_TIMEOUT);
        this.threadSleepTime = parseContextValue(action, "copro.client.api.sleeptime", THREAD_SLEEP_TIME_DEFAULT);
        this.writeBatchSize = parseContextValue(action, "write.batch.size", "10");
        this.targetTableName = this.controlDataComparison.getOutputTable();
        this.controlDataComparisonUrl = this.controlDataComparison.getInputTable();
        this.insertQuery = INSERT_INTO + " " + targetTableName + "(" + COLUMN_LIST + ") " + " " + VAL_STRING_LIST;

    }

    @Override
    public void execute() throws Exception {
        try {
            Jdbi jdbi = ResourceAccess.rdbmsJDBIConn(this.controlDataComparison.getResourceConn());
            this.log.info(this.aMarker, "Control Data Comparison Action for {} has been started", this.controlDataComparison.getName());
            this.log.info(this.aMarker, "Decryption process completed");
            final List<URL> urls = Optional.ofNullable(controlDataComparisonUrl).map(s -> Arrays.stream(s.split(",")).map(urlItem -> {
                try {
                    return new URL(urlItem);
                } catch (MalformedURLException e) {
                    log.error("Error in processing the URL {}", urlItem, e);
                    return null;
                }
            }).collect(Collectors.toList())).orElse(Collections.emptyList());

            int consumerApiCount = 0;
            CustomBatchWithScaling customBatchWithScaling = new CustomBatchWithScaling(action, log);
            boolean isPodScalingCheckEnabled = customBatchWithScaling.isPodScalingCheckEnabled();
            if (isPodScalingCheckEnabled) {
                log.info(aMarker, "Pod Scaling Check is enabled, computing consumer API count using CustomBatchWithScaling");
                consumerApiCount = customBatchWithScaling.computeSorTransactionApiCount();
            }

            if (consumerApiCount <= 0) {
                log.info(aMarker, "No kvp consumer API count found using kube client, using existing context value");
                String key = "Radon.kvp.consumer.API.count";
                consumerApiCount = parseContextValue(action, key, "1");
            }
            log.info(aMarker, "Consumer API count for kvp action is {}", consumerApiCount);

            readBatchSize = parseContextValue(action, "read.batch.size", "10");
            if (consumerApiCount >= readBatchSize) {
                log.info(aMarker, "Consumer API count {} is greater than read batch size {}, setting read batch size to consumer API count", consumerApiCount, readBatchSize);
                readBatchSize = consumerApiCount;
            } else {
                log.info(aMarker, "Consumer API count {} is less than or equal to read batch size {}, keeping read batch size as is", consumerApiCount, readBatchSize);
            }

            final CoproProcessor<ControlDataComparisonQueryInputTable, ControlDataComparisonOutputTable> coproProcessor = getTableCoproProcessor(urls);
            Thread.sleep(threadSleepTime);

            final ControlDataComparisonConsumerProcess controlDataComparisonConsumerProcess = new ControlDataComparisonConsumerProcess(log, aMarker, action, controlDataComparison.getResourceConn(),controlDataComparison);
            coproProcessor.startConsumer(insertQuery, consumerApiCount, writeBatchSize, controlDataComparisonConsumerProcess);
            log.info(aMarker, " LLM kvp Action has been completed {}  ", controlDataComparison.getName());
        } catch (Exception e) {
            action.getContext().put(controlDataComparison.getName() + ".isSuccessful", "false");
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("Error in execute method for LLM kvp action", handymanException, action);

        }
    }

    private @NotNull CoproProcessor<ControlDataComparisonQueryInputTable, ControlDataComparisonOutputTable> getTableCoproProcessor(List<URL> urls) {
        ControlDataComparisonQueryInputTable neonQueryInputTable = new ControlDataComparisonQueryInputTable();
        final CoproProcessor<ControlDataComparisonQueryInputTable, ControlDataComparisonOutputTable> coproProcessor =
                new CoproProcessor<>(new LinkedBlockingQueue<>(),
                        ControlDataComparisonOutputTable.class,
                        ControlDataComparisonQueryInputTable.class,
                        controlDataComparison.getResourceConn(), log,
                        neonQueryInputTable, urls, action);

        coproProcessor.startProducer(controlDataComparison.getQuerySet(), readBatchSize);
        return coproProcessor;
    }
    private int parseContextValue(ActionExecutionAudit action, String key, String defaultValue) {
        String value = action.getContext().getOrDefault(key, defaultValue).trim();
        try {
            return Integer.parseInt(value.isEmpty() ? defaultValue : value);
        } catch (Exception e) {
            log.warn(aMarker, "Failed to parse context value for key {}. Using default {}", key, defaultValue);
            return Integer.parseInt(defaultValue);
        }
    }

    @NotNull
    public List<ControlDataComparisonQueryInputTable> getControlDataComparisonQueryInputTables(Jdbi jdbi, String querySet) {
        final List<ControlDataComparisonQueryInputTable> records = new ArrayList<>();
        jdbi.useTransaction(handle -> {
            final List<String> formattedQuery = CommonQueryUtil.getFormattedQuery(querySet);
            AtomicInteger i = new AtomicInteger(0);
            formattedQuery.forEach(sql -> {
                log.info(aMarker, "Executing query {} from index {}", sql, i.getAndIncrement());
                Query query = handle.createQuery(sql);
                records.addAll(query.mapToBean(ControlDataComparisonQueryInputTable.class).list());
            });
        });
        return records;
    }

    private void performDecryption(List<ControlDataComparisonQueryInputTable> records, InticsIntegrity encryptionHandler) {
        boolean itemWiseEncryption = Boolean.parseBoolean(action.getContext().getOrDefault(ENCRYPT_ITEM_WISE_ENCRYPTION, "false"));
        boolean actualEncryption = Boolean.parseBoolean(action.getContext().getOrDefault(ACTUAL_ENCRYPTION_VARIABLE, "false"));

        if (itemWiseEncryption) decryptAndUpdate(records, encryptionHandler, true);
        if (actualEncryption) decryptAndUpdate(records, encryptionHandler, false);
    }

    private void decryptAndUpdate(List<ControlDataComparisonQueryInputTable> records,
                                  InticsIntegrity encryptionHandler,
                                  boolean isExtracted) {
        List<EncryptionRequestClass> requests = buildDecryptionRequests(records, isExtracted);
        if (requests.isEmpty()) return;

        List<EncryptionRequestClass> responses = encryptionHandler.decrypt(requests);
        Map<String, List<EncryptionRequestClass>> grouped = groupResponsesByRecordId(responses);
        applyDecryptedValues(records, grouped, isExtracted);
    }

    private List<EncryptionRequestClass> buildDecryptionRequests(List<ControlDataComparisonQueryInputTable> records, boolean isExtracted) {
        List<EncryptionRequestClass> requests = new ArrayList<>();
        for (ControlDataComparisonQueryInputTable r : records) {
            if (!"t".equalsIgnoreCase(r.getIsEncrypted())) continue;
            String rawVal = isExtracted ? r.getExtractedValue() : r.getActualValue();
            if (rawVal == null || rawVal.isEmpty()) continue;

            if ("multi_value".equalsIgnoreCase(r.getLineItemType()) && rawVal.contains(",")) {
                String[] parts = rawVal.split(",");
                for (int i = 0; i < parts.length; i++) {
                    requests.add(new EncryptionRequestClass(r.getId() + "_" + i, parts[i].trim(), r.getEncryptionPolicy()));
                }
            } else {
                requests.add(new EncryptionRequestClass(String.valueOf(r.getId()), rawVal, r.getEncryptionPolicy()));
            }
        }
        return requests;
    }

    private Map<String, List<EncryptionRequestClass>> groupResponsesByRecordId(List<EncryptionRequestClass> responses) {
        return responses.stream().collect(Collectors.groupingBy(resp -> {
            String key = resp.getKey();
            return key.contains("_") ? key.substring(0, key.indexOf("_")) : key;
        }));
    }

    private void applyDecryptedValues(List<ControlDataComparisonQueryInputTable> records,
                                      Map<String, List<EncryptionRequestClass>> grouped,
                                      boolean isExtracted) {
        for (ControlDataComparisonQueryInputTable r : records) {
            List<EncryptionRequestClass> respList = grouped.get(String.valueOf(r.getId()));
            if (respList == null) continue;

            respList.sort(Comparator.comparingInt(resp -> {
                if (!resp.getKey().contains("_")) return 0;
                return Integer.parseInt(resp.getKey().substring(resp.getKey().indexOf("_") + 1));
            }));

            String finalValue = respList.stream().map(EncryptionRequestClass::getValue).collect(Collectors.joining(","));
            if (isExtracted) r.setExtractedValue(finalValue); else r.setActualValue(finalValue);
        }
    }

    public static String calculateValidationScores(Long mismatchCount, String oneTouch, String lowTouch) {
        if (mismatchCount == 0) return "NO TOUCH";
        if (mismatchCount <= Long.parseLong(oneTouch)) return "ONE TOUCH";
        if (mismatchCount <= Long.parseLong(lowTouch)) return "LOW TOUCH";
        return "HIGH TOUCH";
    }

    @Override
    public boolean executeIf() {
        return controlDataComparison.getCondition();
    }

    /**
     * Output entity used by CoproProcessor as the output target (matches DB columns used previously).
     * Implements CoproProcessor.Entity so default insertion logic in your pipeline can use getRowData()
     */
    public static class ControlDataComparisonOutputTable implements CoproProcessor.Entity {
        private Long rootPipelineId;
        private Timestamp createdOn;
        private Long groupId;
        private String fileName;
        private String originId;
        private String batchId;
        private Long paperNo;
        private String actualValue;
        private String extractedValue;
        private String matchStatus;
        private Long mismatchCount;
        private Long tenantId;
        private String classification;
        private Long sorContainerId;
        private String sorItemName;
        private Long sorItemId;
        private String status;

        public void setRootPipelineId(Long v) { this.rootPipelineId = v; }
        public void setCreatedOn(Timestamp v) { this.createdOn = v; }
        public void setGroupId(Long v) { this.groupId = v; }
        public void setFileName(String v) { this.fileName = v; }
        public void setOriginId(String v) { this.originId = v; }
        public void setBatchId(String v) { this.batchId = v; }
        public void setPaperNo(Long v) { this.paperNo = v; }
        public void setActualValue(String v) { this.actualValue = v; }
        public void setExtractedValue(String v) { this.extractedValue = v; }
        public void setMatchStatus(String v) { this.matchStatus = v; }
        public void setMismatchCount(Long v) { this.mismatchCount = v; }
        public void setTenantId(Long v) { this.tenantId = v; }
        public void setClassification(String v) { this.classification = v; }
        public void setSorContainerId(Long v) { this.sorContainerId = v; }
        public void setSorItemName(String v) { this.sorItemName = v; }
        public void setSorItemId(Long v) { this.sorItemId = v; }
        public void setStatus(String v) { this.status = v; }

        @Override
        public List<Object> getRowData() {
            List<Object> row = new ArrayList<>();
            row.add(rootPipelineId);
            row.add(createdOn);
            row.add(groupId);
            row.add(fileName);
            row.add(originId);
            row.add(batchId);
            row.add(paperNo);
            row.add(actualValue);
            row.add(extractedValue);
            row.add(matchStatus);
            row.add(mismatchCount);
            row.add(tenantId);
            row.add(classification);
            row.add(sorContainerId);
            row.add(sorItemName);
            row.add(sorItemId);
            return row;
        }

        @Override
        public String getStatus() {
            return status;
        }
    }
}
