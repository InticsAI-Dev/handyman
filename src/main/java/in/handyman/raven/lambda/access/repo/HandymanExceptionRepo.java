package in.handyman.raven.lambda.access.repo;

import in.handyman.raven.lambda.doa.DoaConstant;
import in.handyman.raven.lambda.doa.audit.HandymanExceptionAuditDetails;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

import java.util.List;

public interface HandymanExceptionRepo {
    String COLUMNS = " group_id, root_pipeline_id, root_pipeline_name, pipeline_name, action_id, action_name, exception_info, message, process_id, created_by, created_date, last_modified_by, last_modified_date ";

    @SqlQuery("SELECT " + COLUMNS + " FROM  " + DoaConstant.AUDIT_SCHEMA_NAME + "." + DoaConstant.HEA_TABLE_NAME)
    @RegisterBeanMapper(value = HandymanExceptionAuditDetails.class)
    List<HandymanExceptionAuditDetails> findAllHandymanExceptions();


    @SqlQuery("SELECT " + COLUMNS + " FROM  " + DoaConstant.AUDIT_SCHEMA_NAME + "." + DoaConstant.HEA_TABLE_NAME + " where root_pipeline_id=:rootPipelineId")
    @RegisterBeanMapper(value = HandymanExceptionAuditDetails.class)
    List<HandymanExceptionAuditDetails> findHandymanExceptionsByRootPipelineId(@Bind("rootPipelineId") final Integer rootPipelineId);
}
