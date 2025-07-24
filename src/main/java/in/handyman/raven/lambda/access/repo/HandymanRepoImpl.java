package in.handyman.raven.lambda.access.repo;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import in.handyman.raven.core.azure.adapters.AzureJdbiConnection;
import in.handyman.raven.core.encryption.impl.AESEncryptionImpl;
import in.handyman.raven.core.encryption.inticsgrity.InticsIntegrity;
import in.handyman.raven.core.utils.ConfigEncryptionUtils;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.DoaConstant;
import in.handyman.raven.lambda.doa.audit.*;
import in.handyman.raven.lambda.doa.config.*;
import in.handyman.raven.util.ExceptionUtil;
import in.handyman.raven.util.PropertyHandler;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;

import java.sql.Timestamp;
import java.time.Instant;
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

    static {
        getDatabaseConnectionByConnectionType();
    }

    public static Jdbi getDatabaseConnectionByConnectionType() {
        String legacyResourceConnection = PropertyHandler.get(LEGACY_RESOURCE_CONNECTION_TYPE);
        if (legacyResourceConnection.equals(AZURE)) {

            String azureTenantId = PropertyHandler.get(AZURE_TENANT_ID);
            String azureClientId = PropertyHandler.get(AZURE_CLIENT_ID);
            String azureClientSecret = PropertyHandler.get(AZURE_CLIENT_SECRET);
            String azureDatabaseUrl = PropertyHandler.get(AZURE_DATABASE_URL);
            String azureTokenScope = PropertyHandler.get(AZURE_TOKEN_SCOPE);
            String azureUserName = PropertyHandler.get(CONFIG_USER);

            AzureJdbiConnection connection = new AzureJdbiConnection(azureTenantId, azureClientId, azureClientSecret, azureDatabaseUrl, azureTokenScope, azureUserName);
            JDBI = connection.getAzureJdbiConnection();
            JDBI.installPlugin(new SqlObjectPlugin());
            try (var ignored = JDBI.open()) {
                log.debug("Connected {} {}", azureDatabaseUrl, azureClientId);
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
                log.debug("JDBI connection healthy");
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
        final Map<String, String> pipelineConfig = findAllByInstance(pipelineName).stream()
                .collect(Collectors
                        .toMap((SpwInstanceConfig::getVariable),
                                SpwInstanceConfig::getValue,
                                (p, q) -> p));

        final Map<String, String> lambdaConfig = findAllByProcess(lambdaName).stream()
                .collect(Collectors
                        .toMap((SpwProcessConfig::getVariable),
                                SpwProcessConfig::getValue,
                                (p, q) -> p));

        final Map<String, String> commonConfig = getCommonConfig();

        final Map<String, String> finalMap = new HashMap<>(pipelineConfig);
        finalMap.putAll(lambdaConfig);
        finalMap.putAll(commonConfig);

        return Map.copyOf(finalMap);
    }

    @Override
    public List<SpwInstanceConfig> findAllByInstance(final String instance) {
        checkJDBIConnection();
        try {
            return JDBI.inTransaction(handle -> {
                var repo = handle.attach(SpwInstanceConfigRepo.class);
                return repo.findAllByInstance(instance);
            });
        } catch (Exception e) {
            log.error("Error fetching instance configs for {}", instance, e);
            throw new HandymanException("Failed to fetch instance configs for " + instance, e);
        }
    }
    @Override
    public List<SpwProcessConfig> findAllByProcess(final String process) {
        checkJDBIConnection();
        try {
            return JDBI.inTransaction(handle -> {
                var repo = handle.attach(SpwProcessConfigRepo.class);
                return repo.findAllByProcess(process);
            });
        } catch (Exception e) {
            log.error("Error fetching process configs for {}", process, e);
            throw new HandymanException("Failed to fetch process configs for " + process, e);
        }
    }


    @Override
    public Map<String, String> getCommonConfig() {
        checkJDBIConnection();
        try {
            return findAllCommonConfigs().stream()
                    .collect(Collectors.toMap(
                            SpwCommonConfig::getVariable,
                            SpwCommonConfig::getValue,
                            (p, q) -> p));
        } catch (Exception e) {
            log.error("Error fetching common configs", e);
            throw new HandymanException("Failed to fetch common configs", e);
        }
    }
    @Override
    public List<SpwCommonConfig> findAllCommonConfigs() {
        checkJDBIConnection();
        try {
            return JDBI.inTransaction(handle -> {
                var repo = handle.attach(SpwCommonConfigRepo.class);
                return repo.findAll();
            });
        } catch (Exception e) {
            log.error("Error fetching all common configs", e);
            throw new HandymanException("Failed to fetch all common configs", e);
        }
    }


    @Override
    public List<DatumDriftConfig> findAllDatumDrifts() {
        checkJDBIConnection();
        return JDBI.inTransaction(handle -> {
            var repo = handle.attach(DatumDriftConfigRepo.class);
            return repo.findAll();
        });
    }
    @Override
    public SpwResourceConfig getResourceConfig(final String name) {
        checkJDBIConnection();
        try {
            SpwResourceConfig resourceConfig = findOneResourceConfig(name).orElseThrow();
            resourceConfig.setPassword(ConfigEncryptionUtils.fromEnv().decryptProperty(resourceConfig.getPassword()));
            resourceConfig.setResourceUrl(ConfigEncryptionUtils.fromEnv().decryptProperty(resourceConfig.getResourceUrl()));
            resourceConfig.setUserName(ConfigEncryptionUtils.fromEnv().decryptProperty(resourceConfig.getUserName()));
            resourceConfig.setDatabaseName(ConfigEncryptionUtils.fromEnv().decryptProperty(resourceConfig.getDatabaseName()));
            resourceConfig.setPort(ConfigEncryptionUtils.fromEnv().decryptProperty(resourceConfig.getPort()));
            return resourceConfig;
        } catch (Exception e) {
            log.error("Error fetching resource config for {}", name, e);
            throw new HandymanException("Failed to fetch resource config for " + name, e);
        }
    }
    @Override
    public Optional<SpwResourceConfig> findOneResourceConfig(final String configName) {
        checkJDBIConnection();
        try {
            return JDBI.inTransaction(handle -> {
                var repo = handle.attach(SpwResourceConfigRepo.class);
                return repo.findOne(configName);
            });
        } catch (Exception e) {
            log.error("Error fetching resource config for {}", configName, e);
            throw new HandymanException("Failed to fetch resource config for " + configName, e);
        }
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
        return JDBI.inTransaction(handle -> {
            var repo = handle.attach(ActionExecutionAuditRepo.class);
            return repo.findAllActionsByPipelineId(pipelineId);
        });
    }

    @Override
    public List<ActionExecutionAudit> findAllActionsByRootPipelineId(final Long rootPipelineId) {
        checkJDBIConnection();
        return JDBI.inTransaction(handle -> {
            var repo = handle.attach(ActionExecutionAuditRepo.class);
            return repo.findAllActionsByRootPipelineId(rootPipelineId);
        });
    }

    @Override
    public List<ActionExecutionAudit> findActionByRootPipelineIdAndActionName(final Long rootPipelineId, final String actionName) {
        checkJDBIConnection();
        return JDBI.inTransaction(handle -> {
            var repo = handle.attach(ActionExecutionAuditRepo.class);
            return repo.findAllActionsByRootPipelineIdAndActionName(rootPipelineId, actionName);
        });
    }

    @Override
    public ActionExecutionAudit findActionByActionId(final Long actionId) {
        checkJDBIConnection();
        return JDBI.inTransaction(handle -> {
            var repo = handle.attach(ActionExecutionAuditRepo.class);
            return repo.findActionByActionId(actionId);
        });
    }

    @Override
    public List<ActionExecutionAudit> findAllActionsByPipelineIdAndExecutionStatusId(final Long pipelineId, final Integer executionStatusId) {
        checkJDBIConnection();
        return JDBI.inTransaction(handle -> {
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
        return JDBI.inTransaction(handle -> {
            var repo = handle.attach(PipelineExecutionAuditRepo.class);
            return repo.findOneByPipelineId(pipelineId);
        });
    }


    @Override
    public List<PipelineExecutionAudit> findAllPipelinesByRootPipelineId(final Long rootPipelineId) {
        checkJDBIConnection();
        return JDBI.inTransaction(handle -> {
            var repo = handle.attach(PipelineExecutionAuditRepo.class);
            return repo.findAllPipelinesByRootPipelineId(rootPipelineId);
        });
    }


    @Override
    public List<PipelineExecutionAudit> findAllPipelinesByParentActionId(final Long parentActionId) {
        checkJDBIConnection();
        return JDBI.inTransaction(handle -> {
            var repo = handle.attach(PipelineExecutionAuditRepo.class);
            return repo.findAllPipelinesByParentActionId(parentActionId);
        });
    }

    @Override
    public List<PipelineExecutionAudit> findAllPipelines() {
        checkJDBIConnection();
        return JDBI.inTransaction(handle -> {
            var repo = handle.attach(PipelineExecutionAuditRepo.class);
            return repo.findAllPipelines();
        });
    }

    @Override
    public List<PipelineExecutionAudit> findAllByPipelineName(final String pipelineName) {
        checkJDBIConnection();
        return JDBI.inTransaction(handle -> {
            var repo = handle.attach(PipelineExecutionAuditRepo.class);
            return repo.findAllByPipelineName(pipelineName);
        });
    }

    @Override
    public List<PipelineExecutionAudit> findAllProcessName(final String processName) {
        checkJDBIConnection();
        return JDBI.inTransaction(handle -> {
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
        return JDBI.inTransaction(handle -> {
            var repo = handle.attach(SpwInstanceConfigRepo.class);
            return repo.findAll();
        });
    }

    @Override
    public List<SpwInstanceConfig> findAllByInstanceVariable(final String variable) {
        checkJDBIConnection();
        return JDBI.inTransaction(handle -> {
            var repo = handle.attach(SpwInstanceConfigRepo.class);
            return repo.findAllByInstanceVariable(variable);
        });
    }

    @Override
    public Optional<SpwInstanceConfig> findOneInstance(final String instance, final String variable) {
        checkJDBIConnection();
        return JDBI.inTransaction(handle -> {
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
        return JDBI.inTransaction(handle -> {
            var repo = handle.attach(SpwProcessConfigRepo.class);
            return repo.findAll();
        });
    }

    @Override
    public Optional<SpwProcessConfig> findOneProcess(final String process, final String variable) {
        checkJDBIConnection();
        return JDBI.inTransaction(handle -> {
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
        return JDBI.inTransaction(handle -> {
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
        return JDBI.inTransaction(handle -> {
            var repo = handle.attach(SpwResourceConfigRepo.class);
            return repo.findAll();
        });
    }

    @Override
    public List<HandymanExceptionAuditDetails> findAllHandymanExceptions() {
        checkJDBIConnection();
        return JDBI.inTransaction(handle -> {
            var repo = handle.attach(HandymanExceptionRepo.class);
            return repo.findAllHandymanExceptions();
        });
    }

    @Override
    public List<HandymanExceptionAuditDetails> findHandymanExceptionsByRootPipelineId(final Integer rootPipelineId) {
        checkJDBIConnection();
        return JDBI.inTransaction(handle -> {
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
        JDBI.inTransaction(handle -> handle.createUpdate(" UPDATE audit.protegrity_api_audit " +
                        " SET completed_on = :completed_on, status = :status, message = :message" +
                        "            WHERE id = :id")
                .bind("completed_on", Timestamp.from(Instant.now()))
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

        return JDBI.inTransaction(handle ->
                handle.createUpdate("INSERT INTO audit.protegrity_api_audit " +
                                "(key, encryption_type, endpoint, started_on, root_pipeline_id, action_id, thread_name, uuid) " +
                                "VALUES (:key, :encryption_type, :endpoint, :started_on, :root_pipeline_id, :action_id, :thread_name, :uuid)")
                        .bind("key", key)
                        .bind("encryption_type", encryptionType)
                        .bind("endpoint", endpoint)
                        .bind("started_on", Timestamp.from(Instant.now()))
                        .bind("root_pipeline_id", rootPipelineId)
                        .bind("action_id", actionId)
                        .bind("thread_name", threadName)
                        .bind("uuid", uuid)
                        .executeAndReturnGeneratedKeys("id")
                        .mapTo(Long.class)
                        .one()
        );
    }



}
