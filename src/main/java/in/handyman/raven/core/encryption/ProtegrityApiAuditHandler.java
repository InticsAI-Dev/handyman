package in.handyman.raven.core.encryption;

import org.jdbi.v3.core.Handle;

import java.sql.Timestamp;
import java.time.Instant;

public class ProtegrityApiAuditHandler {

    public static long insertAuditRecord(
            Handle handle,
            String key,
            String encryptionType,
            String endpoint,
            Long rootPipelineId,
            Long actionId,
            String threadName
    ) {
        return handle.createUpdate("INSERT INTO audit.protegrity_api_audit         " +
                        "   (key, encryption_type, endpoint, started_on, root_pipeline_id, action_id, thread_name)" +
                        "  VALUES (:key, :encryption_type, :endpoint, :started_on, :root_pipeline_id, :action_id, :thread_name)")
                .bind("key", key)
                .bind("encryption_type", encryptionType)
                .bind("endpoint", endpoint)
                .bind("started_on", Timestamp.from(Instant.now()))
                .bind("root_pipeline_id", rootPipelineId)
                .bind("action_id", actionId)
                .bind("thread_name", threadName)
                .executeAndReturnGeneratedKeys("id")
                .mapTo(Long.class)
                .one();
    }

    public static void updateAuditRecord(
            Handle handle,
            long id,
            String status,
            String message
    ) {
        handle.createUpdate(" UPDATE audit.protegrity_api_audit           " +
                " SET completed_on = :completed_on, status = :status, message = :message" +
                "            WHERE id = :id")
                .bind("completed_on", Timestamp.from(Instant.now()))
                .bind("status", status)
                .bind("message", message)
                .bind("id", id)
                .execute();
    }
}
