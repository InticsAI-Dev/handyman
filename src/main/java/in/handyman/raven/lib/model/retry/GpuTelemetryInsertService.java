package in.handyman.raven.lib.model.retry;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.util.ExceptionUtil;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Slf4j
public class GpuTelemetryInsertService {

    private static final String SNAPSHOTS_TABLE = "pod_metrics.gpu_telemetry_snapshots";
    private static final String FIELDS_TABLE = "pod_metrics.gpu_telemetry_fields";

    private static final String SQL_INSERT_SNAPSHOT = "INSERT INTO " + SNAPSHOTS_TABLE + " (" +
            "snapshot_id, copro_service_id, captured_at, gpu_id, gpu_name, node_id, pod_name, pod_namespace, host_ip, model_name, framework" +
            ") VALUES (" +
            ":snapshotId, :coproServiceId, :capturedAt, :gpuId, :gpuName, :nodeId, :podName, :podNamespace, :hostIp, :modelName, :framework" +
            ") ON CONFLICT (snapshot_id) DO NOTHING";

    private static final String SQL_INSERT_FIELD = "INSERT INTO " + FIELDS_TABLE + " (" +
            "snapshot_id, copro_service_id, section_name, field_name, field_value, tier, captured_at" +
            ") VALUES (" +
            ":snapshotId, :coproServiceId, :sectionName, :fieldName, :fieldValue, :tier, :capturedAt" +
            ")";
    private static final String SQL_DELETE_FIELDS_BY_SNAPSHOT =
            "DELETE FROM " + FIELDS_TABLE + " WHERE snapshot_id = :snapshotId";

    private static final List<String> SECTION_KEYS = Arrays.asList(
            "computeMetrics",
            "memoryMetrics",
            "transferMetrics",
            "executionMetrics",
            "healthMetrics"
    );

    private final Jdbi jdbi;

    public GpuTelemetryInsertService(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    public void insertIfEnabled(CoproRetryErrorAuditTable retryAudit, ActionExecutionAudit action) {
        if (!isCoproMetricsActivatorEnabled(action)) {
            log.info("Skipping GPU telemetry insert: copro.metrics.activator is disabled");
            return;
        }
        if (!isSuccessMessage(retryAudit.getMessage())) {
            log.info("Skipping GPU telemetry insert: retry audit message is not a success status. message={}", retryAudit.getMessage());
            return;
        }
        if (retryAudit.getComputationDetails() == null || retryAudit.getComputationDetails().isBlank()) {
            log.info("Skipping GPU telemetry insert: computationDetails is empty for coproServiceId={}", retryAudit.getCoproServiceId());
            return;
        }
        log.info("Starting GPU telemetry insert for coproServiceId={}", retryAudit.getCoproServiceId());
        insertGpuTelemetry(retryAudit.getComputationDetails(), retryAudit.getCoproServiceId(), action);
    }

    private void insertGpuTelemetry(String computationDetails, String coproServiceId, ActionExecutionAudit action) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(computationDetails);
            JsonNode gpuTelemetry = root.path("gpuTelemetry");

            if (!gpuTelemetry.isArray() || gpuTelemetry.isEmpty()) {
                log.info("No gpuTelemetry array found in computationDetails for coproServiceId={}", coproServiceId);
                return;
            }
            log.info("Found {} gpuTelemetry snapshot(s) for coproServiceId={}", gpuTelemetry.size(), coproServiceId);

            jdbi.useTransaction(handle -> {
                for (JsonNode item : gpuTelemetry) {
                    String snapshotId = textOrNull(item, "snapshotId");
                    if (snapshotId == null || snapshotId.isBlank()) {
                        log.error("Skipping GPU telemetry item due to missing snapshotId for coproServiceId={}", coproServiceId);
                        continue;
                    }

                    OffsetDateTime capturedAt = offsetDateTimeOrNow(item.path("timestamp"));
                    handle.createUpdate(SQL_INSERT_SNAPSHOT)
                            .bind("snapshotId", snapshotId)
                            .bind("coproServiceId", coproServiceId)
                            .bind("capturedAt", capturedAt)
                            .bind("gpuId", textOrNull(item, "gpuId"))
                            .bind("gpuName", textOrNull(item, "gpuName"))
                            .bind("nodeId", textOrNull(item, "nodeId"))
                            .bind("podName", textOrNull(item, "podName"))
                            .bind("podNamespace", textOrNull(item, "podNamespace"))
                            .bind("hostIp", textOrNull(item, "hostIp"))
                            .bind("modelName", textOrNull(item, "modelName"))
                            .bind("framework", textOrNull(item, "framework"))
                            .execute();
                    log.info("Inserted/validated GPU telemetry snapshot row for snapshotId={} coproServiceId={}", snapshotId, coproServiceId);
                    handle.createUpdate(SQL_DELETE_FIELDS_BY_SNAPSHOT)
                            .bind("snapshotId", snapshotId)
                            .execute();
                    log.info("Cleared existing GPU telemetry fields for snapshotId={}", snapshotId);

                    for (String sectionKey : SECTION_KEYS) {
                        JsonNode sectionNode = item.path(sectionKey);
                        if (!sectionNode.isObject() || sectionNode.isEmpty()) {
                            log.info("No section data for sectionName={} snapshotId={}", sectionKey, snapshotId);
                            continue;
                        }

                        Iterator<Map.Entry<String, JsonNode>> fields = sectionNode.fields();
                        int insertedFieldCount = 0;
                        while (fields.hasNext()) {
                            Map.Entry<String, JsonNode> entry = fields.next();
                            handle.createUpdate(SQL_INSERT_FIELD)
                                    .bind("snapshotId", snapshotId)
                                    .bind("coproServiceId", coproServiceId)
                                    .bind("sectionName", sectionKey)
                                    .bind("fieldName", entry.getKey())
                                    .bind("fieldValue", valueAsTextOrNull(entry.getValue()))
                                    .bind("tier", 1)
                                    .bind("capturedAt", capturedAt)
                                    .execute();
                            insertedFieldCount++;
                        }
                        log.info("Inserted {} GPU telemetry fields for sectionName={} snapshotId={}", insertedFieldCount, sectionKey, snapshotId);
                    }
                }
            });
            log.info("Completed GPU telemetry insert transaction for coproServiceId={}", coproServiceId);
        } catch (Exception e) {
            log.error("Failed to insert into pod_metrics gpu telemetry tables: {}", ExceptionUtil.toString(e));
            HandymanException.insertException("Failed to insert into pod_metrics gpu telemetry tables", new HandymanException(e), action);
        }
    }

    private static boolean isSuccessMessage(String message) {
        boolean isCoproSuccess = message != null && message.contains("200");
        if (!isCoproSuccess) {
            log.info("Skipping GPU telemetry insert: retry audit message does not contain success code 200. message={}", message);
        }
        return isCoproSuccess;
    }

    private static boolean isCoproMetricsActivatorEnabled(ActionExecutionAudit action) {
        if (action == null || action.getContext() == null) {
            log.info("Skipping GPU telemetry insert: action or action.context is null (cannot read copro.metrics.activator).");
            boolean telemetryInsertEnabled = false;
            return telemetryInsertEnabled;
        }
        String value = action.getContext().getOrDefault("copro.metrics.activator", "false");
        boolean telemetryInsertEnabled = Boolean.parseBoolean(value);
        if (!telemetryInsertEnabled) {
            log.info("Skipping GPU telemetry insert: copro.metrics.activator is false. value={}", value);
        }
        return telemetryInsertEnabled;
    }


    private static String textOrNull(JsonNode node, String key) {
        JsonNode n = node.path(key);
        if (n.isMissingNode() || n.isNull()) {
            return null;
        }
        String s = n.asText();
        String resolvedText = (s == null || s.isBlank()) ? null : s;
        return resolvedText;
    }

    private static String valueAsTextOrNull(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        if (node.isTextual()) {
            String fieldText = node.asText();
            return fieldText;
        }
        if (node.isNumber() || node.isBoolean()) {
            String fieldText = node.asText();
            return fieldText;
        }
        String fieldJsonText = node.toString();
        return fieldJsonText;
    }

    private static OffsetDateTime offsetDateTimeOrNow(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull() || node.asText().isBlank()) {
            return OffsetDateTime.now();
        }
        try {
            OffsetDateTime parsedTimestamp = OffsetDateTime.parse(node.asText());
            return parsedTimestamp;
        } catch (Exception ignore) {
            log.info("Timestamp parse failed for GPU telemetry; using current time. timestampRaw={}", node.asText());
            return OffsetDateTime.now();
        }
    }
}
