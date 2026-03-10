package in.handyman.raven.lambda.access.repo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import in.handyman.raven.core.azure.adapters.HikariJdbiProvider;
import in.handyman.raven.core.encryption.ProtegrityApiAudit;
import in.handyman.raven.core.encryption.impl.AESEncryptionImpl;
import in.handyman.raven.core.encryption.inticsgrity.InticsIntegrity;
import in.handyman.raven.core.utils.ConfigEncryptionUtils;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.DoaConstant;
import in.handyman.raven.lambda.doa.audit.*;
import in.handyman.raven.lambda.doa.config.*;
import in.handyman.raven.lib.model.retry.CoproRetryErrorAuditTable;
import in.handyman.raven.util.ExceptionUtil;
import in.handyman.raven.util.PropertyHandler;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class HandymanRepoImpl extends AbstractAccess implements HandymanRepo {

    public static final String DOT = ".";
    protected static final String CONFIG_URL = "raven.db.url";
    private static final String CONFIG_PASSWORD = "raven.db.password";
    private static final String CONFIG_USER = "raven.db.user";
    private static final String MAX_CONNECTION = "raven.max.connection";

    private static Jdbi JDBI;

    public static final String LEGACY_RESOURCE_CONNECTION_TYPE = "legacy.resource.connection.type";

    public static final String LEGACY = "LEGACY";

    public static final String AZURE = "AZURE";

    public static final String AZURE_TENANT_ID = "azure.identity.tenantId";

    public static final String AZURE_CLIENT_ID = "azure.identity.clientId";

    public static final String AZURE_CLIENT_SECRET = "azure.identity.clientSecret";

    public static final String AZURE_DATABASE_URL = "azure.database.url";

    public static final String AZURE_TOKEN_SCOPE = "azure.token.scope";

    private static final String COPRO_RETRY_ERROR_AUDIT = "copro_retry_error_audit";

    private static final String SQL_INSERT_COPRO_AUDIT = "INSERT INTO macro." + COPRO_RETRY_ERROR_AUDIT + " (" +
            "origin_id, group_id, attempt, tenant_id, process_id, file_path, paper_no, message, status, stage, " +
            "created_on, root_pipeline_id, batch_id, last_updated_on, request, response, endpoint, copro_service_id, " +
            "computation_details, copro_status_code, copro_log, copro_details, critical_data_present" +
            ") VALUES (" +
            ":originId, :groupId, :attempt, :tenantId, :processId, :filePath, :paperNo, :message, :status, :stage, " +
            ":createdOn, :rootPipelineId, :batchId, NOW(), :request, :response, :endpoint, :coproServiceId ," +
            ":computationDetails, :coproStatusCode, :coproLog, :coproDetails, :criticalDataPresent" +
            ")";

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

    static {
        getDatabaseConnectionByConnectionType();
    }

    public static Jdbi getDatabaseConnectionByConnectionType() {
        String legacyResourceConnection = PropertyHandler.get(LEGACY_RESOURCE_CONNECTION_TYPE);
        if (legacyResourceConnection.equals(AZURE)) {

            String azureClientId = PropertyHandler.get(AZURE_CLIENT_ID);
            String azureDatabaseUrl = PropertyHandler.get(AZURE_DATABASE_URL);
            log.info("Try connecting with this config {} {}", azureDatabaseUrl, azureClientId);

            JDBI=HikariJdbiProvider.getJdbi();
            JDBI.installPlugin(new SqlObjectPlugin());
            try (var ignored = JDBI.open()) {
                log.info("Connected {} {}", azureDatabaseUrl, azureClientId);
                return JDBI;
            } catch (Exception e) {
                log.error("Error in Connecting database with credentials {} {} with exception {}", azureDatabaseUrl, azureClientId, e.getMessage());
                throw new HandymanException("Error in Connecting database" + e.getMessage());
            }
        } else {

            final String username = PropertyHandler.get(CONFIG_USER);
            final String password = PropertyHandler.get(CONFIG_PASSWORD);
            final String url = PropertyHandler.get(CONFIG_URL);
            final int maxConnection = Integer.parseInt(PropertyHandler.get(MAX_CONNECTION));

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(url);
            config.setUsername(username);
            config.setPassword(password);
            config.setMinimumIdle(0);
            config.setConnectionTimeout(30000);
            config.setIdleTimeout(35000);
            config.setMaxLifetime(45000);
            config.setMaximumPoolSize(maxConnection);
            HikariDataSource hikariDataSource = new HikariDataSource(config);

            JDBI = Jdbi.create(hikariDataSource);
            JDBI.installPlugin(new SqlObjectPlugin());
            try (var ignored = JDBI.open()) {
                log.info("Connected {} {}", url, username);
                return JDBI;
            } catch (Exception e) {
                log.error("Error in Connecting database with credentials {} {} with exception {}", url, username, e.getMessage());
                throw new HandymanException("Error in Connecting database" + e.getMessage());
            }
        }
    }

    public static void checkJDBIConnection() {
        try {
            JDBI.withHandle(handle -> {
                handle.execute("SELECT 1");
                log.info("JDBI connection healthy");
                return null;
            });
        } catch (Exception e) {
            log.error("JDBI connection failed, reconnecting...", e);
            getDatabaseConnectionByConnectionType(); // if needed
        }
    }

    @Override
    public Map<String, String> getAllConfig(final String pipelineName) {
        final String lambdaName = getLambdaName(pipelineName);
        final Map<String, String> instanceConfig = findAllByInstance(pipelineName).stream()
                .collect(Collectors
                        .toMap((SpwInstanceConfig::getVariable),
                                SpwInstanceConfig::getValue,
                                (p, q) -> p));

        final Map<String, String> processConfig = findAllByProcess(lambdaName).stream()
                .collect(Collectors
                        .toMap((SpwProcessConfig::getVariable),
                                SpwProcessConfig::getValue,
                                (p, q) -> p));

        final Map<String, String> commonConfig = getCommonConfig();

        final Map<String, String> finalMap = new HashMap<>();
        finalMap.putAll(commonConfig);
        finalMap.putAll(processConfig);
        finalMap.putAll(instanceConfig);

        return Map.copyOf(finalMap);
    }

    @Override
    public List<SpwInstanceConfig> findAllByInstance(final String instance) {
        checkJDBIConnection();
        return JDBI.withHandle(handle -> {
            var repo = handle.attach(SpwInstanceConfigRepo.class);
            return repo.findAllByInstance(instance);
        });
    }

    @Override
    public List<SpwProcessConfig> findAllByProcess(final String process) {
        checkJDBIConnection();
        return JDBI.withHandle(handle -> {
            var repo = handle.attach(SpwProcessConfigRepo.class);
            return repo.findAllByProcess(process);
        });
    }

    @Override
    public Map<String, String> getCommonConfig() {
        checkJDBIConnection();
        return findAllCommonConfigs().stream()
                .collect(Collectors
                        .toMap((SpwCommonConfig::getVariable),
                                SpwCommonConfig::getValue,
                                (p, q) -> p));
    }

    @Override
    public List<SpwCommonConfig> findAllCommonConfigs() {
        checkJDBIConnection();
        return JDBI.withHandle(handle -> {
            var repo = handle.attach(SpwCommonConfigRepo.class);
            return repo.findAll();
        });
    }

    @Override
    public List<DatumDriftConfig> findAllDatumDrifts() {
        checkJDBIConnection();
        return JDBI.withHandle(handle -> {
            var repo = handle.attach(DatumDriftConfigRepo.class);
            return repo.findAll();
        });
    }

    @Override
    public SpwResourceConfig getResourceConfig(final String name) {
        checkJDBIConnection();
        SpwResourceConfig resourceConfig = findOneResourceConfig(name).orElseThrow();
        resourceConfig.setPassword(ConfigEncryptionUtils.fromEnv().decryptProperty(resourceConfig.getPassword()));
        resourceConfig.setResourceUrl(ConfigEncryptionUtils.fromEnv().decryptProperty(resourceConfig.getResourceUrl()));
        resourceConfig.setUserName(ConfigEncryptionUtils.fromEnv().decryptProperty(resourceConfig.getUserName()));
        resourceConfig.setDatabaseName(ConfigEncryptionUtils.fromEnv().decryptProperty(resourceConfig.getDatabaseName()));
        resourceConfig.setPort(ConfigEncryptionUtils.fromEnv().decryptProperty(resourceConfig.getPort()));

        return resourceConfig;
    }

    @Override
    public Optional<SpwResourceConfig> findOneResourceConfig(final String configName) {
        checkJDBIConnection();
        return JDBI.withHandle(handle -> {
            var repo = handle.attach(SpwResourceConfigRepo.class);
            return repo.findOne(configName);
        });
    }

    @Override
    public Set<String> getPackageAction() {
        checkJDBIConnection();
        return findAllByProcess(SYS_PACKAGE).stream().map(SpwProcessConfig::getValue).collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public void insertPipeline(final PipelineExecutionAudit audit) {
        checkJDBIConnection();
        JDBI.useHandle(handle -> {
            audit.setLastModifiedDate(LocalDateTime.now());
            var repo = handle.attach(PipelineExecutionAuditRepo.class);
            Long pipelineId = repo.insert(audit);
            if (Objects.equals(audit.getPipelineId(), audit.getRootPipelineId())) {
                audit.setRootPipelineId(pipelineId);
            }
            audit.setPipelineId(pipelineId);
        });
    }

    @Override
    public void insertAction(final ActionExecutionAudit audit) {
        checkJDBIConnection();
        JDBI.useHandle(handle -> {
            audit.setLastModifiedDate(LocalDateTime.now());
            var repo = handle.attach(ActionExecutionAuditRepo.class);
            Long actionId = repo.insert(audit);
            audit.setActionId(actionId);
        });

    }


    @Override
    public void update(final ActionExecutionAudit audit) {
        checkJDBIConnection();
        JDBI.useHandle(handle -> {
            audit.setLastModifiedDate(LocalDateTime.now());
            var repo = handle.attach(ActionExecutionAuditRepo.class);
            repo.update(audit);
        });

    }

    @Override
    public List<ActionExecutionAudit> findActions(final Long pipelineId) {
        checkJDBIConnection();
        return JDBI.withHandle(handle -> {
            var repo = handle.attach(ActionExecutionAuditRepo.class);
            return repo.findAllActionsByPipelineId(pipelineId);
        });
    }

    @Override
    public List<ActionExecutionAudit> findAllActionsByRootPipelineId(final Long rootPipelineId) {
        checkJDBIConnection();
        return JDBI.withHandle(handle -> {
            var repo = handle.attach(ActionExecutionAuditRepo.class);
            return repo.findAllActionsByRootPipelineId(rootPipelineId);
        });
    }

    @Override
    public List<ActionExecutionAudit> findActionByRootPipelineIdAndActionName(final Long rootPipelineId, final String actionName) {
        checkJDBIConnection();
        return JDBI.withHandle(handle -> {
            var repo = handle.attach(ActionExecutionAuditRepo.class);
            return repo.findAllActionsByRootPipelineIdAndActionName(rootPipelineId, actionName);
        });
    }

    @Override
    public ActionExecutionAudit findActionByActionId(final Long actionId) {
        checkJDBIConnection();
        return JDBI.withHandle(handle -> {
            var repo = handle.attach(ActionExecutionAuditRepo.class);
            return repo.findActionByActionId(actionId);
        });
    }

    @Override
    public List<ActionExecutionAudit> findAllActionsByPipelineIdAndExecutionStatusId(final Long pipelineId, final Integer executionStatusId) {
        checkJDBIConnection();
        return JDBI.withHandle(handle -> {
            var repo = handle.attach(ActionExecutionAuditRepo.class);
            return repo.findAllActionsByPipelineIdAndExecutionStatusId(pipelineId, executionStatusId);
        });
    }

    @Override
    public void insertStatement(final StatementExecutionAudit audit) {
        checkJDBIConnection();
        audit.setLastModifiedDate(LocalDateTime.now());
        JDBI.useHandle(handle ->
                handle.createUpdate("INSERT INTO " + DoaConstant.AUDIT_SCHEMA_NAME + DOT + DoaConstant.SEA_TABLE_NAME + " ( created_by, created_date, last_modified_by, last_modified_date, action_id, rows_processed, rows_read, rows_written, statement_content, time_taken,root_pipeline_id) VALUES( :createdBy, :createdDate, :lastModifiedBy, :lastModifiedDate, :actionId, :rowsProcessed, :rowsRead, :rowsWritten, :statementContent, :timeTaken,:rootPipelineId);")
                .bindBean(audit).execute());
    }

    @Override
    public void save(final PipelineExecutionStatusAudit audit) {
        checkJDBIConnection();
        audit.setLastModifiedDate(LocalDateTime.now());
        JDBI.useHandle(handle -> handle.createUpdate("INSERT INTO " + DoaConstant.AUDIT_SCHEMA_NAME + DOT + DoaConstant.PESA_TABLE_NAME + " ( created_by, created_date, last_modified_by, last_modified_date, execution_status_id, pipeline_id,root_pipeline_id) VALUES( :createdBy, :createdDate, :lastModifiedBy, :lastModifiedDate, :executionStatusId, :pipelineId,:rootPipelineId);")
                .bindBean(audit).execute());
    }

    @Override
    public void save(final ActionExecutionStatusAudit audit) {
        checkJDBIConnection();
        audit.setLastModifiedDate(LocalDateTime.now());
        JDBI.useHandle(handle -> handle.createUpdate("INSERT INTO " + DoaConstant.AUDIT_SCHEMA_NAME + DOT + DoaConstant.AESA_TABLE_NAME + " ( created_by, created_date, last_modified_by, last_modified_date, action_id, execution_status_id, pipeline_id,root_pipeline_id) VALUES( :createdBy, :createdDate, :lastModifiedBy, :lastModifiedDate, :actionId, :executionStatusId, :pipelineId,:rootPipelineId);")
                .bindBean(audit).execute());
    }

    @Override
    public void update(final PipelineExecutionAudit audit) {
        checkJDBIConnection();
        JDBI.useHandle(handle -> {
            audit.setLastModifiedDate(LocalDateTime.now());
            var repo = handle.attach(PipelineExecutionAuditRepo.class);
            repo.update(audit);
        });
    }


    @Override
    public Optional<PipelineExecutionAudit> findPipeline(final Long pipelineId) {
        checkJDBIConnection();
        return JDBI.withHandle(handle -> {
            var repo = handle.attach(PipelineExecutionAuditRepo.class);
            return repo.findOneByPipelineId(pipelineId);
        });
    }


    @Override
    public List<PipelineExecutionAudit> findAllPipelinesByRootPipelineId(final Long rootPipelineId) {
        checkJDBIConnection();
        return JDBI.withHandle(handle -> {
            var repo = handle.attach(PipelineExecutionAuditRepo.class);
            return repo.findAllPipelinesByRootPipelineId(rootPipelineId);
        });
    }


    @Override
    public List<PipelineExecutionAudit> findAllPipelinesByParentActionId(final Long parentActionId) {
        checkJDBIConnection();
        return JDBI.withHandle(handle -> {
            var repo = handle.attach(PipelineExecutionAuditRepo.class);
            return repo.findAllPipelinesByParentActionId(parentActionId);
        });
    }

    @Override
    public List<PipelineExecutionAudit> findAllPipelines() {
        checkJDBIConnection();
        return JDBI.withHandle(handle -> {
            var repo = handle.attach(PipelineExecutionAuditRepo.class);
            return repo.findAllPipelines();
        });
    }

    @Override
    public List<PipelineExecutionAudit> findAllByPipelineName(final String pipelineName) {
        checkJDBIConnection();
        return JDBI.withHandle(handle -> {
            var repo = handle.attach(PipelineExecutionAuditRepo.class);
            return repo.findAllByPipelineName(pipelineName);
        });
    }

    @Override
    public List<PipelineExecutionAudit> findAllProcessName(final String processName) {
        checkJDBIConnection();
        return JDBI.withHandle(handle -> {
            var repo = handle.attach(PipelineExecutionAuditRepo.class);
            return repo.findAllByProcessName(processName);
        });
    }

    // Spw Instance

    @Override
    public void insert(final SpwInstanceConfig spwInstanceConfig) {
        checkJDBIConnection();
        JDBI.useHandle(handle -> {
            var repo = handle.attach(SpwInstanceConfigRepo.class);
            final Long nextVersion = repo.getNextVersion(spwInstanceConfig);
            spwInstanceConfig.setVersion(nextVersion.intValue());
            repo.insert(spwInstanceConfig);
        });
    }

    @Override
    public void update(final SpwInstanceConfig spwInstanceConfig) {
        checkJDBIConnection();
        JDBI.useHandle(handle -> {
            var repo = handle.attach(SpwInstanceConfigRepo.class);
            repo.update(spwInstanceConfig);
        });
    }

    @Override
    public List<SpwInstanceConfig> findAllInstances() {
        checkJDBIConnection();
        return JDBI.withHandle(handle -> {
            var repo = handle.attach(SpwInstanceConfigRepo.class);
            return repo.findAll();
        });
    }

    @Override
    public List<SpwInstanceConfig> findAllByInstanceVariable(final String variable) {
        checkJDBIConnection();
        return JDBI.withHandle(handle -> {
            var repo = handle.attach(SpwInstanceConfigRepo.class);
            return repo.findAllByInstanceVariable(variable);
        });
    }

    @Override
    public Optional<SpwInstanceConfig> findOneInstance(final String instance, final String variable) {
        checkJDBIConnection();
        return JDBI.withHandle(handle -> {
            var repo = handle.attach(SpwInstanceConfigRepo.class);
            return repo.findOne(instance, variable);
        });
    }

    @Override
    public void insert(final SpwProcessConfig spwProcessConfig) {
        checkJDBIConnection();
        JDBI.useHandle(handle -> {
            var repo = handle.attach(SpwProcessConfigRepo.class);
            final Long nextVersion = repo.getNextVersion(spwProcessConfig);
            spwProcessConfig.setVersion(nextVersion.intValue());
            repo.insert(spwProcessConfig);
        });
    }

    @Override
    public void update(final SpwProcessConfig spwProcessConfig) {
        checkJDBIConnection();
        JDBI.useHandle(handle -> {
            var repo = handle.attach(SpwProcessConfigRepo.class);
            repo.update(spwProcessConfig);
        });
    }

    @Override
    public List<SpwProcessConfig> findAllProcesses() {
        checkJDBIConnection();
        return JDBI.withHandle(handle -> {
            var repo = handle.attach(SpwProcessConfigRepo.class);
            return repo.findAll();
        });
    }

    @Override
    public Optional<SpwProcessConfig> findOneProcess(final String process, final String variable) {
        checkJDBIConnection();
        return JDBI.withHandle(handle -> {
            var repo = handle.attach(SpwProcessConfigRepo.class);
            return repo.findOne(process, variable);
        });
    }

    @Override
    public void insert(final SpwCommonConfig spwCommonConfig) {
        checkJDBIConnection();
        JDBI.useHandle(handle -> {
            var repo = handle.attach(SpwCommonConfigRepo.class);
            final Long nextVersion = repo.getNextVersion(spwCommonConfig);
            spwCommonConfig.setVersion(nextVersion.intValue());
            repo.insert(spwCommonConfig);
        });
    }

    @Override
    public void update(final SpwCommonConfig spwCommonConfig) {
        checkJDBIConnection();
        JDBI.useHandle(handle -> {
            var repo = handle.attach(SpwCommonConfigRepo.class);
            repo.update(spwCommonConfig);
        });
    }

    @Override
    public Optional<SpwCommonConfig> findOneCommonConfig(final String variable) {
        checkJDBIConnection();
        return JDBI.withHandle(handle -> {
            var repo = handle.attach(SpwCommonConfigRepo.class);
            return repo.findOne(variable);
        });
    }

    @Override
    public void insert(final SpwResourceConfig spwResourceConfig) {
        checkJDBIConnection();
        JDBI.useHandle(handle -> {
            var repo = handle.attach(SpwResourceConfigRepo.class);
            final Long nextVersion = repo.getNextVersion(spwResourceConfig);
            spwResourceConfig.setVersion(nextVersion.intValue());
            repo.insert(spwResourceConfig);
        });
    }

    @Override
    public void update(final SpwResourceConfig spwResourceConfig) {
        checkJDBIConnection();
        JDBI.useHandle(handle -> {
            var repo = handle.attach(SpwResourceConfigRepo.class);
            repo.update(spwResourceConfig);
        });
    }

    @Override
    public List<SpwResourceConfig> findAllResourceConfigs() {
        checkJDBIConnection();
        return JDBI.withHandle(handle -> {
            var repo = handle.attach(SpwResourceConfigRepo.class);
            return repo.findAll();
        });
    }

    @Override
    public List<HandymanExceptionAuditDetails> findAllHandymanExceptions() {
        checkJDBIConnection();
        return JDBI.withHandle(handle -> {
            var repo = handle.attach(HandymanExceptionRepo.class);
            return repo.findAllHandymanExceptions();
        });
    }

    @Override
    public List<HandymanExceptionAuditDetails> findHandymanExceptionsByRootPipelineId(final Integer rootPipelineId) {
        checkJDBIConnection();
        return JDBI.withHandle(handle -> {
            var repo = handle.attach(HandymanExceptionRepo.class);
            return repo.findHandymanExceptionsByRootPipelineId(rootPipelineId);
        });
    }

    public String encryptString(String message) {
        InticsIntegrity inticsIntegrity = new InticsIntegrity(new AESEncryptionImpl());

        String messageEncrypted = inticsIntegrity.encrypt(message, "AES256", "SQL_DATA");
        return messageEncrypted;
    }

    public void insertExceptionLog(ActionExecutionAudit actionExecutionAudit, Throwable exception, String message) {
        checkJDBIConnection();
        HandymanExceptionAuditDetails exceptionAuditDetails = HandymanExceptionAuditDetails.builder()
                //      .groupId(Integer.parseInt(actionExecutionAudit.getContext().get("gen_group_id.group_id")))
                .rootPipelineId(actionExecutionAudit.getRootPipelineId())
                .rootPipelineName(actionExecutionAudit.getParentPipelineName())
                .pipelineName(actionExecutionAudit.getPipelineName())
                .actionId(actionExecutionAudit.getActionId())
                .actionName(actionExecutionAudit.getActionName())
                .exceptionInfo(encryptString(ExceptionUtil.toString(exception)))
                .message(encryptString(message))
                .processId(actionExecutionAudit.getProcessId())
                .createdBy(actionExecutionAudit.getCreatedBy())
                .createdDate(actionExecutionAudit.getCreatedDate())
                .lastModifiedBy(actionExecutionAudit.getLastModifiedBy())
                .lastModifiedDate(actionExecutionAudit.getLastModifiedDate()).build();
        JDBI.useHandle(handle -> handle.createUpdate("INSERT INTO audit.handyman_exception_audit (group_id, root_pipeline_id, root_pipeline_name, pipeline_name, action_id, action_name, exception_Info, message, process_id, created_by, created_date, last_modified_by, last_modified_date) " +
                        "VALUES(:groupId, :rootPipelineId, :rootPipelineName, :pipelineName, :actionId, :actionName, :exceptionInfo, :message, :processId, :createdBy, :createdDate, :lastModifiedBy, :lastModifiedDate);")
                .bindBean(exceptionAuditDetails).execute());
        log.info("inserting exception audit details has been completed");

    }

    public void updateProtegrityAuditRecord(
            long id,
            String status,
            String message
    ) {
        checkJDBIConnection();
        JDBI.withHandle(handle -> handle.createUpdate(" UPDATE audit.protegrity_api_audit " +
                        " SET completed_on = :completed_on, status = :status, message = :message" +
                        "            WHERE id = :id")
                .bind("completed_on", Timestamp.valueOf(LocalDateTime.now()))
                .bind("status", status)
                .bind("message", message)
                .bind("id", id)
                .execute());
    }

    public long insertProtegrityAuditRecord(
            String key,
            String encryptionType,
            String endpoint,
            Long rootPipelineId,
            Long actionId,
            String threadName,
            String uuid
    ) {
        checkJDBIConnection();

        return JDBI.withHandle(handle ->
                handle.createUpdate("INSERT INTO audit.protegrity_api_audit " +
                                "(key, encryption_type, endpoint, started_on, root_pipeline_id, action_id, thread_name, uuid) " +
                                "VALUES (:key, :encryption_type, :endpoint, :started_on, :root_pipeline_id, :action_id, :thread_name, :uuid)")
                        .bind("key", key)
                        .bind("encryption_type", encryptionType)
                        .bind("endpoint", endpoint)
                        .bind("started_on", Timestamp.valueOf(LocalDateTime.now()))
                        .bind("root_pipeline_id", rootPipelineId)
                        .bind("action_id", actionId)
                        .bind("thread_name", threadName)
                        .bind("uuid", uuid)
                        .executeAndReturnGeneratedKeys("id")
                        .mapTo(Long.class)
                        .one()
        );
    }


    public void insertProtegrityAudit(
            ProtegrityApiAudit protegrityApiAudit
    ) {
        checkJDBIConnection();

        JDBI.withHandle(handle ->
                handle.createUpdate("INSERT INTO audit.protegrity_api_audit " +
                                "(key, encryption_type, endpoint, started_on, root_pipeline_id, action_id, thread_name, uuid, status, message, completed_on) " +
                                "VALUES (:key, :encryption_type, :endpoint, :started_on, :root_pipeline_id, :action_id, :thread_name, :uuid, :status, :message, :completed_on)")
                        .bind("key", protegrityApiAudit.getKey())
                        .bind("encryption_type", protegrityApiAudit.getEncryptionType())
                        .bind("endpoint", protegrityApiAudit.getEndpoint())
                        .bind("started_on", protegrityApiAudit.getStartedOn())
                        .bind("root_pipeline_id", protegrityApiAudit.getRootPipelineId())
                        .bind("action_id", protegrityApiAudit.getActionId())
                        .bind("thread_name", protegrityApiAudit.getThreadName())
                        .bind("uuid", protegrityApiAudit.getUuid())
                        .bind("status", protegrityApiAudit.getStatus())
                        .bind("message", protegrityApiAudit.getMessage())
                        .bind("completed_on", protegrityApiAudit.getCompletedOn())
                        .execute()
        );
    }

    public long insertAuditToDb(CoproRetryErrorAuditTable retryAudit, ActionExecutionAudit action) {
        checkJDBIConnection();
        try {
            // Ensure createdOn is not null
            if (retryAudit.getCreatedOn() == null) {
                retryAudit.setCreatedOn(Timestamp.valueOf(LocalDateTime.now()));
            }

            long id = JDBI.withHandle(handle ->
                    handle.createUpdate(SQL_INSERT_COPRO_AUDIT)
                            .bindBean(retryAudit)
                            .executeAndReturnGeneratedKeys("id")
                            .mapTo(Long.class)
                            .one()
            );

            // Insert into triton_results.krypton_model only when activator is true, message indicates success (200), and computation_details is present
            if (isCoproMetricsActivatorEnabled(action) && isSuccessMessage(retryAudit.getMessage()) && retryAudit.getComputationDetails() != null && !retryAudit.getComputationDetails().isBlank()) {
                insertKryptonModelIfSuccess(retryAudit, action);
            }

            return id;
        } catch (Exception ex) {
            log.error("Failed to insert copro retry audit", ex);
            HandymanException handymanException = new HandymanException(ex);
            HandymanException.insertException("Error executing prepared insert query", handymanException, action);
            throw handymanException;
        }
    }

    /** Returns true if message indicates HTTP success, e.g. "200 -> OK", "200 ->". */
    private static boolean isSuccessMessage(String message) {
        return message != null && message.contains("200");
    }

    /** Returns true if copro.metrics.activator is true in context (config.spw_instance_config). Default false when not present. */
    private static boolean isCoproMetricsActivatorEnabled(ActionExecutionAudit action) {
        if (action == null || action.getContext() == null) return false;
        String value = action.getContext().getOrDefault("copro.metrics.activator", "false");
        return Boolean.parseBoolean(value);
    }

    /**
     * Inserts a row into triton_results.krypton_model using data from the copro retry audit.
     * machine_ip from audit.pipeline_execution_audit; all metrics from computation_details JSON (key-value columns).
     */
    private void insertKryptonModelIfSuccess(CoproRetryErrorAuditTable retryAudit, ActionExecutionAudit action) {
        try {
            String machineIp = getMachineIpByRootPipelineId(retryAudit.getRootPipelineId(), action);
            KryptonModelMetrics metrics = parseComputationDetailsToMetrics(retryAudit.getComputationDetails(), action);
            if (metrics == null) {
                log.info("Skipping krypton_model insert: could not parse computation_details");
                return;
            }

            String device = metrics.getBeforeGpuName() != null ? metrics.getBeforeGpuName() : metrics.getAfterGpuName();

            JDBI.useHandle(handle -> {
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
                        .bind("cpuBefore", metrics.getBeforeCpuUsage())
                        .bind("cpuAfter", metrics.getAfterCpuUsage())
                        .bind("gpuBefore", metrics.getBeforeGpuLoadPercent())
                        .bind("gpuAfter", metrics.getAfterGpuLoadPercent())
                        .bind("device", device)
                        .bind("processName", (String) null);
                update.execute();
            });
            log.info("Inserted krypton_model row for root_pipeline_id={}, origin_id={}", retryAudit.getRootPipelineId(), retryAudit.getOriginId());
        } catch (Exception e) {
            log.error("Failed to insert into triton_results.krypton_model: {}", ExceptionUtil.toString(e));
            HandymanException.insertException("Failed to insert into triton_results.krypton_model", new HandymanException(e), action);
        }
    }

    /** Fetches machine_ip (host_name) from audit.pipeline_execution_audit by root_pipeline_id. */
    private String getMachineIpByRootPipelineId(Long rootPipelineId, ActionExecutionAudit action) {
        if (rootPipelineId == null) return null;
        try {
            List<PipelineExecutionAudit> list = JDBI.withHandle(handle -> {
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

    /**
     * Parses computation_details JSON into KryptonModelMetrics.
     * Extracts beforeMetricsData, afterMetricsData, duration, and first GPU object from each gpus array when non-empty.
     */
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

    @Override
    public List<CoproRetryErrorAuditTable> findAllCoproApiCalls() {
        checkJDBIConnection();
        return JDBI.withHandle(handle -> {
            var repo = handle.attach(CoproRetryErrorAuditRepo.class);
            return repo.findAll();
        });
    }

    @Override
    public List<CoproRetryErrorAuditTable> findCoproApiCallsByRootPipelineId(Long rootPipelineId) {
        checkJDBIConnection();
        return JDBI.withHandle(handle -> {
            var repo = handle.attach(CoproRetryErrorAuditRepo.class);
            return repo.findByRootPipelineId(rootPipelineId);
        });
    }

    @Override
    public List<CoproRetryErrorAuditTable> findCoproApiCallsByOriginId(String originId) {
        checkJDBIConnection();
        return JDBI.withHandle(handle -> {
            var repo = handle.attach(CoproRetryErrorAuditRepo.class);
            return repo.findByOriginId(originId);
        });
    }

    @Override
    public List<CoproRetryErrorAuditTable> findCoproApiCallsByRootPipelineIdAndOriginId(Long rootPipelineId, String originId) {
        checkJDBIConnection();
        return JDBI.withHandle(handle -> {
            var repo = handle.attach(CoproRetryErrorAuditRepo.class);
            return repo.findByRootPipelineIdAndOriginId(rootPipelineId, originId);
        });
    }

    @Override
    public List<CoproRetryErrorAuditTable> findCoproApiCallsByStatus(String status) {
        checkJDBIConnection();
        return JDBI.withHandle(handle -> {
            var repo = handle.attach(CoproRetryErrorAuditRepo.class);
            return repo.findByStatus(status);
        });
    }
    @Override
    public List<CoproRetryErrorAuditTable> findCoproApiCallsByOriginIdAndStage(String originId,String stage) {
        checkJDBIConnection();
        return JDBI.withHandle(handle -> {
            var repo = handle.attach(CoproRetryErrorAuditRepo.class);
            return repo.findByOriginIdAndStage(originId,stage);
        });
    }

    @Override
    public List<CoproRetryErrorAuditTable> findCoproApiCallsByStage(String stage) {
        checkJDBIConnection();
        return JDBI.withHandle(handle -> {
            var repo = handle.attach(CoproRetryErrorAuditRepo.class);
            return repo.findByStage(stage);
        });
    }

    @Override
    public List<CoproRetryErrorAuditTable> findCoproApiCallsByStatusAndStage(String status, String stage) {
        checkJDBIConnection();
        return JDBI.withHandle(handle -> {
            var repo = handle.attach(CoproRetryErrorAuditRepo.class);
            return repo.findByStatusAndStage(status, stage);
        });
    }

    @Override
    public List<SpwBshConfig> findAllBshClassesByTenantId(Long tenantId) {
        checkJDBIConnection();
        return JDBI.withHandle(handle -> {
            var repo = handle.attach(SpwBshConfigRepo.class);
            return repo.findAllByTenantId(tenantId);
        });
    }


}
