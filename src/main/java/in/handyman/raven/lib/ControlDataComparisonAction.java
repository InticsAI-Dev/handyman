package in.handyman.raven.lib;

import in.handyman.raven.core.encryption.SecurityEngine;
import in.handyman.raven.core.encryption.inticsgrity.InticsIntegrity;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.action.IActionExecution;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.ControlDataComparison;
import in.handyman.raven.lib.model.controldatacomaprison.ControlDataComparisonQueryInputTable;
import in.handyman.raven.util.CommonQueryUtil;
import org.jetbrains.annotations.NotNull;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.Query;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import static in.handyman.raven.core.encryption.EncryptionConstants.ENCRYPT_ITEM_WISE_ENCRYPTION;

/**
 * ControlDataComparison Action using CoproProcessor pattern
 */
@in.handyman.raven.lambda.action.ActionExecution(actionName = "ControlDataComparison")
public class ControlDataComparisonAction implements IActionExecution {

    public static final String ACTUAL_ENCRYPTION_VARIABLE = "actual.encryption.variable";

    private final ActionExecutionAudit action;
    private final ControlDataComparison controlDataComparison;
    private final Logger log;
    private final Marker aMarker;

    public ControlDataComparisonAction(final ActionExecutionAudit action, final Logger log, final Object controlDataComparison) {
        this.action = action;
        this.log = log;
        this.controlDataComparison = (ControlDataComparison) controlDataComparison;
        this.aMarker = MarkerFactory.getMarker("ControlDataComparison:" + this.controlDataComparison.getName());
    }

    @Override
    public void execute() throws Exception {
        try {
            log.info(aMarker, "Control Data Comparison Action (Copro) for {} has been started", controlDataComparison.getName());

            // JDBI resource and queries
            final Jdbi jdbi = in.handyman.raven.lambda.access.ResourceAccess.rdbmsJDBIConn(controlDataComparison.getResourceConn());
            final String querySet = controlDataComparison.getQuerySet();
            final String outputTable = controlDataComparison.getOutputTable();

            // Insert SQL - same column list you used previously
            final String insertSql = "INSERT INTO " + outputTable + " (" +
                    "root_pipeline_id, created_on, group_id, file_name, origin_id, batch_id, " +
                    "paper_no, actual_value, extracted_value, match_status, mismatch_count, " +
                    "tenant_id, classification, sor_container_id, sor_item_name, sor_item_id" +
                    ") VALUES (" +
                    ":rootPipelineId, :createdOn, :groupId, :fileName, :originId, :batchId, :paperNo, " +
                    ":actualValue, :extractedValue, :matchStatus, :mismatchCount, :tenantId, " +
                    ":classification, :sorContainerId, :sorItemName, :sorItemId" +
                    ")";

            // Build input list by streaming querySet (this is used by CoproProcessor producer)
            final List<URL> dummyNodes = new ArrayList<>(); // not used by this action, but CoproProcessor expects a list
            // create a stopping seed instance of ControlDataComparisonQueryInputTable (empty marker)
            final ControlDataComparisonQueryInputTable stoppingSeed = new ControlDataComparisonQueryInputTable();

            // create CoproProcessor with a LinkedBlockingQueue
            final CoproProcessor<ControlDataComparisonQueryInputTable, ControlDataComparisonOutputTable> coproProcessor =
                    new CoproProcessor<>(new LinkedBlockingQueue<>(),
                            ControlDataComparisonOutputTable.class,
                            ControlDataComparisonQueryInputTable.class,
                            controlDataComparison.getResourceConn(), log,
                            stoppingSeed, dummyNodes, action);

            // Start producer
            coproProcessor.startProducer(querySet, parseContextValue(action, "read.batch.size", "100"));

            // create consumer process and start consumers
            ControlDataComparisonConsumerProcess consumerProcess =
                    new ControlDataComparisonConsumerProcess(log, aMarker, action, controlDataComparison.getResourceConn(), outputTable);

            // determine consumer count and write batch size
            int consumerCount = parseContextValue(action, "control.data.consumer.count", "1");
            int writeBatchSize = parseContextValue(action, "write.batch.size", "50");

            log.info(aMarker, "Starting copro consumer with count {} and writeBatchSize {}", consumerCount, writeBatchSize);

            coproProcessor.startConsumer(insertSql, consumerCount, writeBatchSize, consumerProcess);

            log.info(aMarker, "Control Data Comparison Copro Action completed: {}", controlDataComparison.getName());
            action.getContext().put(controlDataComparison.getName() + ".isSuccessful", "true");
        } catch (Exception e) {
            action.getContext().put(controlDataComparison.getName() + ".isSuccessful", "false");
            log.error(aMarker, "Error in ControlDataComparisonAction.execute", e);
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("ControlDataComparison action failed", handymanException, action);
            throw handymanException;
        }
    }

    private int parseContextValue(ActionExecutionAudit action, String key, String defaultValue) {
        String value = action.getContext().getOrDefault(key, defaultValue).trim();
        int result;
        try {
            if (value.isEmpty()) {
                result = Integer.parseInt(defaultValue);
            } else {
                result = Integer.parseInt(value);
            }
        } catch (Exception e) {
            log.warn(aMarker, "Failed to parse context value for key {}. Using default {}", key, defaultValue);
            result = Integer.parseInt(defaultValue);
        }
        return result;
    }

    public static String calculateValidationScores(Long mismatchCount, String oneTouch, String lowTouch) {
        String matchStatus;
        if (mismatchCount == 0) {
            matchStatus = "NO TOUCH";
        } else if (mismatchCount <= Long.parseLong(oneTouch)) {
            matchStatus = "ONE TOUCH";
        } else if (mismatchCount <= Long.parseLong(lowTouch)) {
            matchStatus = "LOW TOUCH";
        } else {
            matchStatus = "HIGH TOUCH";
        }
        return matchStatus;
    }

    @Override
    public boolean executeIf() throws Exception {
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
