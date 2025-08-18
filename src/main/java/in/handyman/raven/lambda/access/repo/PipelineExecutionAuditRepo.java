package in.handyman.raven.lambda.access.repo;


import in.handyman.raven.lambda.doa.DoaConstant;
import in.handyman.raven.lambda.doa.audit.PipelineExecutionAudit;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;
import java.util.Optional;

public interface PipelineExecutionAuditRepo {

    String COLUMNS = "  created_by, created_date, last_modified_by, last_modified_date, context_node, execution_status_id, lambda_name, parent_action_id, parent_action_name, parent_pipeline_id, parent_pipeline_name, pipeline_name, file_content, host_name, mode_of_execution, pipeline_load_type, relative_path, request_body, thread_name, process_name, root_pipeline_id, process_id ";
    String DOT = ".";

    @SqlUpdate("insert into " + DoaConstant.AUDIT_SCHEMA_NAME + DOT + DoaConstant.PEA_TABLE_NAME + "  ( " + COLUMNS + " ) VALUES( :createdBy, :createdDate, :lastModifiedBy, :lastModifiedDate, :contextNode,:executionStatusId,:lambdaName,:parentActionId, :parentActionName, :parentPipelineId, :parentPipelineName, :pipelineName, :fileContent,:hostName,:modeOfExecution,:pipelineLoadType,:relativePath,:requestBody,:threadName,:processName,:rootPipelineId,:processId ); ")
    @GetGeneratedKeys
    Long insert(@BindBean final PipelineExecutionAudit actionExecutionAudit);

    @SqlUpdate("update  " + DoaConstant.AUDIT_SCHEMA_NAME + DOT + DoaConstant.PEA_TABLE_NAME + " SET created_by = :createdBy, created_date = :createdDate, last_modified_by = :lastModifiedBy, last_modified_date = :lastModifiedDate, context_node = :contextNode, execution_status_id = :executionStatusId, lambda_name = :lambdaName, parent_action_id = :parentActionId, parent_action_name = :parentActionName, parent_pipeline_id = :parentPipelineId, parent_pipeline_name = :parentPipelineName, pipeline_name = :pipelineName, file_content = :fileContent, host_name = :hostName, mode_of_execution = :modeOfExecution, pipeline_load_type = :pipelineLoadType , relative_path = :relativePath, request_body = :requestBody,  process_name = :processName , root_pipeline_id = :rootPipelineId , process_id = :processId WHERE pipeline_id = :pipelineId ")
    void update(@BindBean final PipelineExecutionAudit actionExecutionAudit);


    @SqlQuery("SELECT " + COLUMNS + " FROM  " + DoaConstant.AUDIT_SCHEMA_NAME + DOT + DoaConstant.PEA_TABLE_NAME + " ; ")
    @RegisterBeanMapper(value = PipelineExecutionAudit.class)
    List<PipelineExecutionAudit> findAllPipelines();

    @SqlQuery("SELECT " + COLUMNS + " FROM  " + DoaConstant.AUDIT_SCHEMA_NAME + DOT + DoaConstant.PEA_TABLE_NAME + " where root_pipeline_id= :rootPipelineId ; ")
    @RegisterBeanMapper(value = PipelineExecutionAudit.class)
    List<PipelineExecutionAudit> findAllPipelinesByRootPipelineId(@Bind("rootPipelineId") final Long rootPipelineId);


    @SqlQuery("SELECT " + COLUMNS + " FROM  " + DoaConstant.AUDIT_SCHEMA_NAME + DOT + DoaConstant.PEA_TABLE_NAME + " where pipeline_name= :pipelineName ; ")
    @RegisterBeanMapper(value = PipelineExecutionAudit.class)
    List<PipelineExecutionAudit> findAllByPipelineName(@Bind("pipelineName") final String pipelineName);

    @SqlQuery("SELECT " + COLUMNS + " FROM  " + DoaConstant.AUDIT_SCHEMA_NAME + DOT + DoaConstant.PEA_TABLE_NAME + " where parent_action_id= :parentActionId ; ")
    @RegisterBeanMapper(value = PipelineExecutionAudit.class)
    List<PipelineExecutionAudit> findAllPipelinesByParentActionId(@Bind("parentActionId") final Long parentActionId);

    @SqlQuery("SELECT " + COLUMNS + " FROM  " + DoaConstant.AUDIT_SCHEMA_NAME + DOT + DoaConstant.PEA_TABLE_NAME + " where pipeline_id= :pipelineId ; ")
    @RegisterBeanMapper(value = PipelineExecutionAudit.class)
    Optional<PipelineExecutionAudit> findOneByPipelineId(@Bind("pipelineId") final Long pipelineId);

    @SqlQuery("SELECT DISTINCT" + COLUMNS + " FROM " + DoaConstant.AUDIT_SCHEMA_NAME + DOT + DoaConstant.PEA_TABLE_NAME + " where process_name= :process_name ;")
    @RegisterBeanMapper(value = PipelineExecutionAudit.class)
    List<PipelineExecutionAudit> findAllByProcessName(@Bind("process_name") final String process_name);

}
