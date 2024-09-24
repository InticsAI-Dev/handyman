package in.handyman.raven.lib;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.DbDataDart;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
class DbDataDartActionTest {

    @Test
    void execute() throws Exception {
        DbDataDart dbDataDart = DbDataDart.builder()
                .name("database backup pipeline")
                .condition(true)
                .dataBaseName("zio_pipeline_ui")
                .auditTable("sanitary_hub.db_data_truncate_audit")
                .resourceConn("intics_zio_db_conn")
                .querySet("SELECT \n" +
                        "    ARRAY['urgency_triage'] AS truncateSchemaList,\n" +
                        "    ARRAY['urgency_triage.ut_model_result'] AS excludeTableWithSchemaList,\n" +
                        "    1 AS groupId,\n" +
                        "    1 AS tenantId,\n" +
                        "    1112 AS processId,\n" +
                        "    52732 AS rootPipelineId;\n")
                .build();
        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
        actionExecutionAudit.getContext().put("db.truncate.exclude.table.list", "true");
        DbDataDartAction dbDataDartAction = new DbDataDartAction(actionExecutionAudit, log, dbDataDart);
        dbDataDartAction.execute();
    }
}