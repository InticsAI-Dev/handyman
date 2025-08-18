package in.handyman.raven.lambda.access.repo;


import in.handyman.raven.lambda.doa.DoaConstant;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;

public interface ActionExecutionAuditRepo {

    String COLUMNS = " created_by, created_date, last_modified_by, last_modified_date, context_node, execution_status_id, lambda_name, parent_action_id, parent_action_name, parent_pipeline_id, parent_pipeline_name, pipeline_name, action_name, execution_group_id, input_node, log, pipeline_id, root_pipeline_id ";
    String DOT = ".";

    @SqlUpdate("insert into " + DoaConstant.AUDIT_SCHEMA_NAME + DOT + DoaConstant.AEA_TABLE_NAME + "  ( " + COLUMNS + " ) VALUES( :createdBy, :createdDate, :lastModifiedBy, :lastModifiedDate, :contextNode, :executionStatusId, :lambdaName, :parentActionId, :parentActionName, :parentPipelineId, :parentPipelineName, :pipelineName, :actionName, :executionGroupId, :inputNode, :log, :pipelineId ,:rootPipelineId); ")
    @GetGeneratedKeys
    Long insert(@BindBean final ActionExecutionAudit actionExecutionAudit);

    @SqlUpdate("update  " + DoaConstant.AUDIT_SCHEMA_NAME + DOT + DoaConstant.AEA_TABLE_NAME + " SET created_by = :createdBy, created_date = :createdDate, last_modified_by = :lastModifiedBy," +
            " last_modified_date = :lastModifiedDate, context_node = :contextNode, execution_status_id = :executionStatusId, lambda_name = :lambdaName, " +
            "parent_action_id = :parentActionId, parent_action_name = :parentActionName, parent_pipeline_id = :parentPipelineId, " +
            "parent_pipeline_name = :parentPipelineName, pipeline_name = :pipelineName, action_name = :actionName, execution_group_id = :executionGroupId," +
            " input_node = :inputNode, log = :log, pipeline_id = :pipelineId,thread_name = :threadName, root_pipeline_id = :rootPipelineId  WHERE action_id = :actionId ;")
    void update(@BindBean final ActionExecutionAudit actionExecutionAudit);


    @SqlQuery("SELECT " + COLUMNS + " FROM  " + DoaConstant.AUDIT_SCHEMA_NAME + DOT + DoaConstant.AEA_TABLE_NAME + " ; ")
    @RegisterBeanMapper(value = ActionExecutionAudit.class)
    List<ActionExecutionAudit> findAllActions();

    @SqlQuery("SELECT " + COLUMNS + " FROM  " + DoaConstant.AUDIT_SCHEMA_NAME + DOT + DoaConstant.AEA_TABLE_NAME + " where root_pipeline_id= :rootPipelineId ; ")
    @RegisterBeanMapper(value = ActionExecutionAudit.class)
    List<ActionExecutionAudit> findAllActionsByRootPipelineId(@Bind("rootPipelineId") final Long rootPipelineId);

    @SqlQuery("SELECT " + COLUMNS + " FROM  " + DoaConstant.AUDIT_SCHEMA_NAME + DOT + DoaConstant.AEA_TABLE_NAME + " where pipeline_id= :pipelineId ; ")
    @RegisterBeanMapper(value = ActionExecutionAudit.class)
    List<ActionExecutionAudit> findAllActionsByPipelineId(@Bind("pipelineId") final Long pipelineId);

    @SqlQuery("SELECT " + COLUMNS + " FROM  " + DoaConstant.AUDIT_SCHEMA_NAME + DOT + DoaConstant.AEA_TABLE_NAME + " where root_pipeline_id= :rootPipelineId and action_name= :actionName ; ")
    @RegisterBeanMapper(value = ActionExecutionAudit.class)
    List<ActionExecutionAudit> findAllActionsByRootPipelineIdAndActionName(@Bind("rootPipelineId") final Long rootPipelineId, @Bind("actionName") final String actionName);

    @SqlQuery("SELECT " + COLUMNS + " FROM  " + DoaConstant.AUDIT_SCHEMA_NAME + DOT + DoaConstant.AEA_TABLE_NAME + " where action_id= :actionId ; ")
    @RegisterBeanMapper(value = ActionExecutionAudit.class)
    ActionExecutionAudit findActionByActionId(Long actionId);

    @SqlQuery("SELECT " + COLUMNS + " FROM  " + DoaConstant.AUDIT_SCHEMA_NAME + DOT + DoaConstant.AEA_TABLE_NAME + " where pipeline_id= :pipelineId and execution_status_id= :executionStatusId ; ")
    @RegisterBeanMapper(value = ActionExecutionAudit.class)
    List<ActionExecutionAudit> findAllActionsByPipelineIdAndExecutionStatusId(Long pipelineId, Integer executionStatusId);
}
