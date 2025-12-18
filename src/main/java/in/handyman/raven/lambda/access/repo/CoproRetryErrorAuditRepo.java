package in.handyman.raven.lambda.access.repo;


import in.handyman.raven.lib.services.retry.CoproRetryErrorAuditTable;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;

import java.util.List;

public interface CoproRetryErrorAuditRepo {

    String COLUMNS =
            " id, origin_id, group_id, tenant_id, process_id, file_path, " +
                    " paper_no, status, stage, " +
                    " message, created_on, root_pipeline_id, batch_id, last_updated_on, " +
                    " request, response, endpoint, attempt, copro_service_id ";

    // -----------------------------------------------------------------------
    // 🔹 1. Find all
    // -----------------------------------------------------------------------
    @SqlQuery("SELECT " + COLUMNS + " FROM macro.copro_retry_error_audit")
    @RegisterBeanMapper(value = CoproRetryErrorAuditTable.class)
    List<CoproRetryErrorAuditTable> findAll();

    // -----------------------------------------------------------------------
    // 🔹 2. Find by root_pipeline_id
    // -----------------------------------------------------------------------
    @SqlQuery("SELECT " + COLUMNS + " FROM macro.copro_retry_error_audit WHERE root_pipeline_id = :rootPipelineId")
    @RegisterBeanMapper(value = CoproRetryErrorAuditTable.class)
    List<CoproRetryErrorAuditTable> findByRootPipelineId(@Bind("rootPipelineId") Long rootPipelineId);

    // -----------------------------------------------------------------------
    // 🔹 3. Find by origin_id
    // -----------------------------------------------------------------------
    @SqlQuery("SELECT " + COLUMNS + " FROM macro.copro_retry_error_audit WHERE origin_id = :originId")
    @RegisterBeanMapper(value = CoproRetryErrorAuditTable.class)
    List<CoproRetryErrorAuditTable> findByOriginId(@Bind("originId") String originId);

    // -----------------------------------------------------------------------
    // 🔹 4. Find by root_pipeline_id and origin_id
    // -----------------------------------------------------------------------
    @SqlQuery("SELECT " + COLUMNS + " FROM macro.copro_retry_error_audit " +
            "WHERE root_pipeline_id = :rootPipelineId AND origin_id = :originId")
    @RegisterBeanMapper(value = CoproRetryErrorAuditTable.class)
    List<CoproRetryErrorAuditTable> findByRootPipelineIdAndOriginId(@Bind("rootPipelineId") Long rootPipelineId,
                                                                    @Bind("originId") String originId);

    // -----------------------------------------------------------------------
    // 🔹 5. Find by status
    // -----------------------------------------------------------------------
    @SqlQuery("SELECT " + COLUMNS + " FROM macro.copro_retry_error_audit WHERE status = :status")
    @RegisterBeanMapper(value = CoproRetryErrorAuditTable.class)
    List<CoproRetryErrorAuditTable> findByStatus(@Bind("status") String status);

    // -----------------------------------------------------------------------
    // 🔹 6. Find by stage
    // -----------------------------------------------------------------------
    @SqlQuery("SELECT " + COLUMNS + " FROM macro.copro_retry_error_audit WHERE stage = :stage")
    @RegisterBeanMapper(value = CoproRetryErrorAuditTable.class)
    List<CoproRetryErrorAuditTable> findByStage(@Bind("stage") String stage);

    @SqlQuery("SELECT " + COLUMNS + " FROM macro.copro_retry_error_audit WHERE origin_id = :originId AND stage = :stage")
    @RegisterBeanMapper(value = CoproRetryErrorAuditTable.class)
    List<CoproRetryErrorAuditTable> findByOriginIdAndStage(@Bind("originId") String originId,@Bind("stage") String stage);

    // -----------------------------------------------------------------------
    // 🔹 7. Find by status and stage
    // -----------------------------------------------------------------------
    @SqlQuery("SELECT " + COLUMNS + " FROM macro.copro_retry_error_audit WHERE status = :status AND stage = :stage")
    @RegisterBeanMapper(value = CoproRetryErrorAuditTable.class)
    List<CoproRetryErrorAuditTable> findByStatusAndStage(@Bind("status") String status,
                                                         @Bind("stage") String stage);
}
