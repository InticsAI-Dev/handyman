package in.handyman.raven.lambda.access.repo;

import in.handyman.raven.lambda.doa.audit.*;
import in.handyman.raven.lambda.doa.config.*;
import in.handyman.raven.lib.model.agentic.paper.filter.copro.CoproRetryErrorAuditTable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface HandymanRepo {

    String SYS_PACKAGE = "SYS_PACKAGE";

    Map<String, String> getAllConfig(final String pipelineName);

    Map<String, String> getCommonConfig();

    SpwResourceConfig getResourceConfig(final String name);

    Set<String> getPackageAction();

    // SPW Instance

    void insert(final SpwInstanceConfig spwInstanceConfig);

    void update(final SpwInstanceConfig spwInstanceConfig);

    List<SpwInstanceConfig> findAllInstances();

    List<DatumDriftConfig> findAllDatumDrifts();

    List<SpwInstanceConfig> findAllByInstanceVariable(final String variable);

    List<SpwInstanceConfig> findAllByInstance(final String instance);


    Optional<SpwInstanceConfig> findOneInstance(final String instance, final String variable);


    // SPW Process

    void insert(final SpwProcessConfig spwProcessConfig);

    void update(final SpwProcessConfig spwProcessConfig);

    List<SpwProcessConfig> findAllProcesses();

    List<SpwProcessConfig> findAllByProcess(final String process);

    Optional<SpwProcessConfig> findOneProcess(final String process, final String variable);


    // SPW Common

    void insert(final SpwCommonConfig spwCommonConfig);

    void update(final SpwCommonConfig spwCommonConfig);

    List<SpwCommonConfig> findAllCommonConfigs();

    Optional<SpwCommonConfig> findOneCommonConfig(final String variable);

    // SPW Resource

    void insert(final SpwResourceConfig spwResourceConfig);

    void update(final SpwResourceConfig spwResourceConfig);

    List<SpwResourceConfig> findAllResourceConfigs();

    Optional<SpwResourceConfig> findOneResourceConfig(final String configName);

    // Audit

    void insertPipeline(final PipelineExecutionAudit audit);

    void insertAction(final ActionExecutionAudit audit);

    void insertStatement(final StatementExecutionAudit audit);

    void save(final PipelineExecutionStatusAudit audit);

    void save(final ActionExecutionStatusAudit audit);

    void update(final PipelineExecutionAudit audit);

    void update(final ActionExecutionAudit audit);

    List<PipelineExecutionAudit> findAllPipelinesByRootPipelineId(final Long rootPipelineId);

    Optional<PipelineExecutionAudit> findPipeline(final Long pipelineId);

    List<ActionExecutionAudit> findActions(final Long pipelineId);

    List<ActionExecutionAudit> findAllActionsByRootPipelineId(final Long rootPipelineId);

    List<ActionExecutionAudit> findAllActionsByPipelineIdAndExecutionStatusId(final Long pipelineId, final Integer statusId);

    List<ActionExecutionAudit> findActionByRootPipelineIdAndActionName(final Long rootPipelineId, final String actionName);

    ActionExecutionAudit findActionByActionId(final Long actionId);

    List<PipelineExecutionAudit> findAllPipelinesByParentActionId(final Long parentActionId);

    List<PipelineExecutionAudit> findAllPipelines();

    List<PipelineExecutionAudit> findAllByPipelineName(final String pipelineName);

    List<PipelineExecutionAudit> findAllProcessName(final String processName);

    List<HandymanExceptionAuditDetails> findHandymanExceptionsByRootPipelineId(final Integer rootPipelineId);

    List<HandymanExceptionAuditDetails> findAllHandymanExceptions();

    long insertProtegrityAuditRecord(String key, String encryptionType, String endpoint, Long rootPipelineId, Long actionId, String threadName,String uuid);

    void updateProtegrityAuditRecord(long id, String status, String message);

    long insertAuditToDb(CoproRetryErrorAuditTable retryAudit, ActionExecutionAudit action);
}
