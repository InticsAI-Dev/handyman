package in.handyman.raven.lib.model.retry;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.access.repo.PipelineExecutionAuditRepo;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lambda.doa.audit.PipelineExecutionAudit;
import in.handyman.raven.util.ExceptionUtil;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;

import java.util.List;

/**
 * Handles insertion of successful Copro audit data into triton_results.krypton_model.
 * Only runs when copro.metrics.activator is true, message indicates HTTP 200, and computation_details is present.
 * This is Copro/retry feature logic and belongs in the service layer, not in the repo.
 */
@Slf4j
public class KryptonModelInsertService {

    private static final String KRYPTON_MODEL_TABLE = "triton_results.krypton_model";
    private static final String SQL_INSERT_KRYPTON_MODEL = "INSERT INTO " + KRYPTON_MODEL_TABLE + " (" +
            "machine_ip, root_pipeline_id, origin_id, paper_no, input_request, output, exec_time, batch_id, " +
            "before_cpu_usage, before_total_cores, before_available_cores, before_used_cores, before_core_utilization_percent, " +
            "before_ram_usage, before_ram_used_mb, before_ram_total_mb, before_ram_available_mb, before_disk_usage, " +
            "before_disk_total_gb, before_disk_free_gb, before_disk_total_mb, before_disk_free_mb, before_source, " +
            "before_gpu_name, before_gpu_load_percent, before_gpu_ram_used_mb, before_gpu_ram_total_mb, before_gpu_ram_utilization_percent, " +
            "after_cpu_usage, after_total_cores, after_available_cores, after_used_cores, after_core_utilization_percent, " +
            "after_ram_usage, after_ram_used_mb, after_ram_total_mb, after_ram_available_mb, after_disk_usage, " +
            "after_disk_total_gb, after_disk_free_gb, after_disk_total_mb, after_disk_free_mb, after_source, " +
            "after_gpu_name, after_gpu_load_percent, after_gpu_ram_used_mb, after_gpu_ram_total_mb, after_gpu_ram_utilization_percent, " +
            "cpu_before, cpu_after, gpu_before, gpu_after, device, process_name" +
            ") VALUES (" +
            ":machineIp, :rootPipelineId, :originId, :paperNo, :inputRequest, :output, :execTime, :batchId, " +
            ":beforeCpuUsage, :beforeTotalCores, :beforeAvailableCores, :beforeUsedCores, :beforeCoreUtilizationPercent, " +
            ":beforeRamUsage, :beforeRamUsedMb, :beforeRamTotalMb, :beforeRamAvailableMb, :beforeDiskUsage, " +
            ":beforeDiskTotalGb, :beforeDiskFreeGb, :beforeDiskTotalMb, :beforeDiskFreeMb, :beforeSource, " +
            ":beforeGpuName, :beforeGpuLoadPercent, :beforeGpuRamUsedMb, :beforeGpuRamTotalMb, :beforeGpuRamUtilizationPercent, " +
            ":afterCpuUsage, :afterTotalCores, :afterAvailableCores, :afterUsedCores, :afterCoreUtilizationPercent, " +
            ":afterRamUsage, :afterRamUsedMb, :afterRamTotalMb, :afterRamAvailableMb, :afterDiskUsage, " +
            ":afterDiskTotalGb, :afterDiskFreeGb, :afterDiskTotalMb, :afterDiskFreeMb, :afterSource, " +
            ":afterGpuName, :afterGpuLoadPercent, :afterGpuRamUsedMb, :afterGpuRamTotalMb, :afterGpuRamUtilizationPercent, " +
            ":cpuBefore, :cpuAfter, :gpuBefore, :gpuAfter, :device, :processName)";

    private final Jdbi jdbi;

    public KryptonModelInsertService(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    /**
     * Inserts into triton_results.krypton_model when activator is true, message indicates success (200), and computation_details is present.
     */
    public void insertIfEnabled(CoproRetryErrorAuditTable retryAudit, ActionExecutionAudit action) {
        if (!isCoproMetricsActivatorEnabled(action)) return;
        if (!isSuccessMessage(retryAudit.getMessage())) return;
        if (retryAudit.getComputationDetails() == null || retryAudit.getComputationDetails().isBlank()) return;
        insertKryptonModelIfSuccess(retryAudit, action);
    }

    private static boolean isSuccessMessage(String message) {
        return message != null && message.contains("200");
    }

    private static boolean isCoproMetricsActivatorEnabled(ActionExecutionAudit action) {
        if (action == null || action.getContext() == null) return false;
        String value = action.getContext().getOrDefault("copro.metrics.activator", "false");
        return Boolean.parseBoolean(value);
    }

    private void insertKryptonModelIfSuccess(CoproRetryErrorAuditTable retryAudit, ActionExecutionAudit action) {
        try {
            String machineIp = getMachineIpByRootPipelineId(retryAudit.getRootPipelineId(), action);
            KryptonModelMetrics metrics = parseComputationDetailsToMetrics(retryAudit.getComputationDetails(), action);
            if (metrics == null) {
                log.info("Skipping krypton_model insert: could not parse computation_details");
                return;
            }

            jdbi.useHandle(handle -> {
                var update = handle.createUpdate(SQL_INSERT_KRYPTON_MODEL)
                        .bind("machineIp", machineIp)
                        .bind("rootPipelineId", retryAudit.getRootPipelineId())
                        .bind("originId", retryAudit.getOriginId())
                        .bind("paperNo", retryAudit.getPaperNo())
                        .bind("inputRequest", retryAudit.getRequest())
                        .bind("output", retryAudit.getResponse())
                        .bind("execTime", metrics.getExecTime())
                        .bind("batchId", retryAudit.getBatchId())
                        .bind("beforeCpuUsage", metrics.getBeforeCpuUsage())
                        .bind("beforeTotalCores", metrics.getBeforeTotalCores())
                        .bind("beforeAvailableCores", metrics.getBeforeAvailableCores())
                        .bind("beforeUsedCores", metrics.getBeforeUsedCores())
                        .bind("beforeCoreUtilizationPercent", metrics.getBeforeCoreUtilizationPercent())
                        .bind("beforeRamUsage", metrics.getBeforeRamUsage())
                        .bind("beforeRamUsedMb", metrics.getBeforeRamUsedMb())
                        .bind("beforeRamTotalMb", metrics.getBeforeRamTotalMb())
                        .bind("beforeRamAvailableMb", metrics.getBeforeRamAvailableMb())
                        .bind("beforeDiskUsage", metrics.getBeforeDiskUsage())
                        .bind("beforeDiskTotalGb", metrics.getBeforeDiskTotalGb())
                        .bind("beforeDiskFreeGb", metrics.getBeforeDiskFreeGb())
                        .bind("beforeDiskTotalMb", metrics.getBeforeDiskTotalMb())
                        .bind("beforeDiskFreeMb", metrics.getBeforeDiskFreeMb())
                        .bind("beforeSource", metrics.getBeforeSource())
                        .bind("beforeGpuName", metrics.getBeforeGpuName())
                        .bind("beforeGpuLoadPercent", metrics.getBeforeGpuLoadPercent())
                        .bind("beforeGpuRamUsedMb", metrics.getBeforeGpuRamUsedMb())
                        .bind("beforeGpuRamTotalMb", metrics.getBeforeGpuRamTotalMb())
                        .bind("beforeGpuRamUtilizationPercent", metrics.getBeforeGpuRamUtilizationPercent())
                        .bind("afterCpuUsage", metrics.getAfterCpuUsage())
                        .bind("afterTotalCores", metrics.getAfterTotalCores())
                        .bind("afterAvailableCores", metrics.getAfterAvailableCores())
                        .bind("afterUsedCores", metrics.getAfterUsedCores())
                        .bind("afterCoreUtilizationPercent", metrics.getAfterCoreUtilizationPercent())
                        .bind("afterRamUsage", metrics.getAfterRamUsage())
                        .bind("afterRamUsedMb", metrics.getAfterRamUsedMb())
                        .bind("afterRamTotalMb", metrics.getAfterRamTotalMb())
                        .bind("afterRamAvailableMb", metrics.getAfterRamAvailableMb())
                        .bind("afterDiskUsage", metrics.getAfterDiskUsage())
                        .bind("afterDiskTotalGb", metrics.getAfterDiskTotalGb())
                        .bind("afterDiskFreeGb", metrics.getAfterDiskFreeGb())
                        .bind("afterDiskTotalMb", metrics.getAfterDiskTotalMb())
                        .bind("afterDiskFreeMb", metrics.getAfterDiskFreeMb())
                        .bind("afterSource", metrics.getAfterSource())
                        .bind("afterGpuName", metrics.getAfterGpuName())
                        .bind("afterGpuLoadPercent", metrics.getAfterGpuLoadPercent())
                        .bind("afterGpuRamUsedMb", metrics.getAfterGpuRamUsedMb())
                        .bind("afterGpuRamTotalMb", metrics.getAfterGpuRamTotalMb())
                        .bind("afterGpuRamUtilizationPercent", metrics.getAfterGpuRamUtilizationPercent())
                        .bind("cpuBefore", (Double) null)
                        .bind("cpuAfter", (Double) null)
                        .bind("gpuBefore", (Integer) null)
                        .bind("gpuAfter", (Integer) null)
                        .bind("device", (String) null)
                        .bind("processName", (String) null);
                update.execute();
            });
            log.info("Inserted krypton_model row for root_pipeline_id={}, origin_id={}", retryAudit.getRootPipelineId(), retryAudit.getOriginId());
        } catch (Exception e) {
            log.error("Failed to insert into triton_results.krypton_model: {}", ExceptionUtil.toString(e));
            HandymanException.insertException("Failed to insert into triton_results.krypton_model", new HandymanException(e), action);
        }
    }

    private String getMachineIpByRootPipelineId(Long rootPipelineId, ActionExecutionAudit action) {
        if (rootPipelineId == null) return null;
        try {
            List<PipelineExecutionAudit> list = jdbi.withHandle(handle -> {
                var repo = handle.attach(PipelineExecutionAuditRepo.class);
                return repo.findAllPipelinesByRootPipelineId(rootPipelineId);
            });
            if (list != null && !list.isEmpty() && list.get(0).getHostName() != null) {
                return list.get(0).getHostName();
            }
        } catch (Exception e) {
            log.error("Could not resolve machine_ip for root_pipeline_id={}: {}", rootPipelineId, e.getMessage());
            HandymanException.insertException("Could not resolve machine_ip for root_pipeline_id " + rootPipelineId, new HandymanException(e), action);
        }
        return null;
    }

    private KryptonModelMetrics parseComputationDetailsToMetrics(String computationDetails, ActionExecutionAudit action) {
        if (computationDetails == null || computationDetails.isBlank()) return null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(computationDetails);

            Double execTime = null;
            JsonNode d = root.path("duration");
            if (!d.isMissingNode() && !d.isNull()) execTime = d.asDouble();

            JsonNode before = root.path("beforeMetricsData");
            JsonNode after = root.path("afterMetricsData");

            KryptonModelMetrics.KryptonModelMetricsBuilder b = KryptonModelMetrics.builder().execTime(execTime);

            if (!before.isMissingNode() && !before.isNull()) {
                b.beforeCpuUsage(doubleOrNull(before, "cpuUsage"))
                        .beforeTotalCores(intOrNull(before, "totalCores"))
                        .beforeAvailableCores(intOrNull(before, "availableCores"))
                        .beforeUsedCores(doubleOrNull(before, "usedCores"))
                        .beforeCoreUtilizationPercent(doubleOrNull(before, "coreUtilizationPercent"))
                        .beforeRamUsage(doubleOrNull(before, "ramUsage"))
                        .beforeRamUsedMb(textOrNull(before, "ramUsedMb"))
                        .beforeRamTotalMb(textOrNull(before, "ramTotalMb"))
                        .beforeRamAvailableMb(textOrNull(before, "ramAvailableMb"))
                        .beforeDiskUsage(doubleOrNull(before, "diskUsage"))
                        .beforeDiskTotalGb(textOrNull(before, "diskTotalGb"))
                        .beforeDiskFreeGb(textOrNull(before, "diskFreeGb"))
                        .beforeDiskTotalMb(textOrNull(before, "diskTotalMb"))
                        .beforeDiskFreeMb(textOrNull(before, "diskFreeMb"))
                        .beforeSource(textOrNull(before, "source"));
                setFirstGpu(before, b, true);
            }
            if (!after.isMissingNode() && !after.isNull()) {
                b.afterCpuUsage(doubleOrNull(after, "cpuUsage"))
                        .afterTotalCores(intOrNull(after, "totalCores"))
                        .afterAvailableCores(intOrNull(after, "availableCores"))
                        .afterUsedCores(doubleOrNull(after, "usedCores"))
                        .afterCoreUtilizationPercent(doubleOrNull(after, "coreUtilizationPercent"))
                        .afterRamUsage(doubleOrNull(after, "ramUsage"))
                        .afterRamUsedMb(textOrNull(after, "ramUsedMb"))
                        .afterRamTotalMb(textOrNull(after, "ramTotalMb"))
                        .afterRamAvailableMb(textOrNull(after, "ramAvailableMb"))
                        .afterDiskUsage(doubleOrNull(after, "diskUsage"))
                        .afterDiskTotalGb(textOrNull(after, "diskTotalGb"))
                        .afterDiskFreeGb(textOrNull(after, "diskFreeGb"))
                        .afterDiskTotalMb(textOrNull(after, "diskTotalMb"))
                        .afterDiskFreeMb(textOrNull(after, "diskFreeMb"))
                        .afterSource(textOrNull(after, "source"));
                setFirstGpu(after, b, false);
            }

            return b.build();
        } catch (Exception e) {
            log.error("Could not parse computation_details to metrics: {}", e.getMessage());
            HandymanException.insertException("Could not parse computation_details to metrics", new HandymanException(e), action);
            return null;
        }
    }

    private void setFirstGpu(JsonNode metricsNode, KryptonModelMetrics.KryptonModelMetricsBuilder b, boolean isBefore) {
        JsonNode gpus = metricsNode.path("gpus");
        if (gpus.isMissingNode() || gpus.isNull() || !gpus.isArray() || gpus.size() == 0) return;
        JsonNode first = gpus.get(0);
        if (isBefore) {
            b.beforeGpuName(textOrNull(first, "gpu"))
                    .beforeGpuLoadPercent(intOrNull(first, "gpuLoadPercent"))
                    .beforeGpuRamUsedMb(longOrNull(first, "gpuRamUsedMb"))
                    .beforeGpuRamTotalMb(longOrNull(first, "gpuRamTotalMb"))
                    .beforeGpuRamUtilizationPercent(doubleOrNull(first, "gpuRamUtilizationPercent"));
        } else {
            b.afterGpuName(textOrNull(first, "gpu"))
                    .afterGpuLoadPercent(intOrNull(first, "gpuLoadPercent"))
                    .afterGpuRamUsedMb(longOrNull(first, "gpuRamUsedMb"))
                    .afterGpuRamTotalMb(longOrNull(first, "gpuRamTotalMb"))
                    .afterGpuRamUtilizationPercent(doubleOrNull(first, "gpuRamUtilizationPercent"));
        }
    }

    private Double doubleOrNull(JsonNode node, String key) {
        JsonNode n = node.path(key);
        if (n.isMissingNode() || n.isNull()) return null;
        try {
            Double value = n.asDouble();
            return value;
        } catch (Exception e) {
            log.error("Error parsing double for key {}: {}", key, e.getMessage());
            return null;
        }
    }

    private Integer intOrNull(JsonNode node, String key) {
        JsonNode n = node.path(key);
        if (n.isMissingNode() || n.isNull()) return null;
        try {
            Integer value = n.asInt();
            return value;
        } catch (Exception e) {
            log.error("Error parsing integer for key {}: {}", key, e.getMessage());
            return null;
        }
    }

    private Long longOrNull(JsonNode node, String key) {
        JsonNode n = node.path(key);
        if (n.isMissingNode() || n.isNull()) return null;
        try {
            Long value = n.asLong();
            return value;
        } catch (Exception e) {
            log.error("Error parsing long for key {}: {}", key, e.getMessage());
            return null;
        }
    }

    private static String textOrNull(JsonNode node, String key) {
        JsonNode n = node.path(key);
        if (n.isMissingNode() || n.isNull()) return null;
        String s = n.asText();
        String result = (s == null || s.isEmpty()) ? null : s;
        return result;
    }
}
