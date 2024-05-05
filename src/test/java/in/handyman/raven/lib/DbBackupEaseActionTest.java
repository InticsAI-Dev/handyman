package in.handyman.raven.lib;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.DbBackupEase;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;


@Slf4j
class DbBackupEaseActionTest {

    @Test
    void execute() throws Exception {
        DbBackupEase dataBaseModel = DbBackupEase.builder()
                .name("database backup pipeline")
                .condition(true)
                .dataBaseName("zio_pipeline")
                .auditTable("sanitary_hub.db_data_backup_audit")
                .resourceConn("intics_zio_db_conn")
                .querySet("SELECT array_agg(ARRAY['info', 'sor_transaction', 'paper_classification', 'urgency_triage']) AS backupSchemaList,\n" +
                        "       array_agg(ARRAY['audit', 'sor_meta']) AS restrictedSchemaList,\n" +
                        "       '/home/dineshkumar.anandan@zucisystems.com/Documents/database_backup/' AS targetDirectory;\n")
                .build();
        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
        DbBackupEaseAction dbBackupEaseAction = new DbBackupEaseAction(actionExecutionAudit, log, dataBaseModel);
        dbBackupEaseAction.execute();
    }


}