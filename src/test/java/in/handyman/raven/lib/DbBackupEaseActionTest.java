package in.handyman.raven.lib;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.DbBackupEase;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;


@Slf4j
class DbBackupEaseActionTest {

    @Test
    void execute() throws Exception {
        DbBackupEase dataBaseModel = DbBackupEase.builder()
                .name("database backup pipeline")
                .condition(true)
                .dataBaseName("zio_pipeline_ui")
                .auditTable("sanitary_hub.db_data_backup_audit")
                .resourceConn("intics_zio_db_conn")
                .querySet("SELECT Array['config'] AS backupSchemaList,\n" +
                        "    Array['audit'] AS restrictedSchemaList,\n" +
                        "    '/home/anandh.andrews@zucisystems.com/intics-workspace/testing/database-backup/' AS targetDirectory,\n" +
                        "    1 AS groupId,\n" +
                        "    1 AS tenantId,\n" +
                        "    1112 AS processId,\n" +
                        "    52732 AS rootPipelineId;\n")
                .build();
        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
        actionExecutionAudit.getContext().put("database.backup.file.name", "pg_dump_file");
        actionExecutionAudit.getContext().put("allow.backup.by.schema", "false");
        actionExecutionAudit.getContext().put("current.date.directory.format", "DD_MM_YYYY");
        actionExecutionAudit.getContext().put("db.docker.user.name", "postgres");
        actionExecutionAudit.getContext().put("db.docker.host.value", "localhost");
        actionExecutionAudit.getContext().put("db.docker.port.value", "5432");
        actionExecutionAudit.getContext().put("db.docker.container.name.value", "pedantic_lovelace");
        DbBackupEaseAction dbBackupEaseAction = new DbBackupEaseAction(actionExecutionAudit, log, dataBaseModel);
        dbBackupEaseAction.execute();
    }

    @Test
    public void dbCommand() throws InterruptedException, IOException {
        // Define the backup command
        String backupCommand = "docker exec pedantic_lovelace sh -c 'pg_dump  -U postgres -d zio_pipeline -h localhost -p 5432 -n info -n sor_transaction -n paper_classification -n  urgency_triage'";

        // Specify the absolute path for the output file
        String outputFile = "/home/dineshkumar.anandan@zucisystems.com/Documents/database_backup/psql_dump.sql";

        // Execute the backup command and redirect output to the file
        Process process = new ProcessBuilder()
                .command("bash", "-c", backupCommand + " > " + outputFile)
                .start();

        // Wait for the process to finish
        process.waitFor();

        // Check the exit status
        int exitCode = process.exitValue();
        if (exitCode == 0) {
            System.out.println("Backup successful");
        } else {
            System.out.println("Backup failed");
        }

        // Verify that the file is created
        if (Files.exists(Paths.get(outputFile))) {
            System.out.println("Output file exists at: " + outputFile);
        } else {
            System.out.println("Output file does not exist");
        }
    }

    @Test
    void executeDb() throws Exception {
        DbBackupEase dataBaseModel = DbBackupEase.builder()
                .name("database backup pipeline")
                .condition(true)
                .dataBaseName("zio_pipeline")
                .auditTable("sanitary_hub.db_data_backup_audit")
                .resourceConn("intics_zio_db_conn")
                .querySet("SELECT '/home/dineshkumar.anandan@zucisystems.com/Documents/database_backup/' AS targetDirectory,\n" +
                        "    1 AS groupId,\n" +
                        "    1 AS tenantId,\n" +
                        "    1112 AS processId,\n" +
                        "    52732 AS rootPipelineId;\n")
                .build();
        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
        actionExecutionAudit.getContext().put("database.backup.file.name", "pg_dump_file");
        actionExecutionAudit.getContext().put("allow.backup.by.schema", "false");
        actionExecutionAudit.getContext().put("db.docker.user.name", "postgres");
        actionExecutionAudit.getContext().put("db.docker.host.value", "localhost");
        actionExecutionAudit.getContext().put("db.docker.port.value", "5432");
        actionExecutionAudit.getContext().put("db.docker.container.name.value", "pedantic_lovelace");
        DbBackupEaseAction dbBackupEaseAction = new DbBackupEaseAction(actionExecutionAudit, log, dataBaseModel);
        dbBackupEaseAction.execute();
    }

    @Test
    void executeBackupSchema() throws Exception {
        DbBackupEase dataBaseModel = DbBackupEase.builder()
                .name("database backup pipeline")
                .condition(true)
                .dataBaseName("zio_pipeline")
                .auditTable("sanitary_hub.db_data_backup_audit")
                .resourceConn("intics_zio_db_conn")
                .querySet("SELECT \n" +
                        "    ARRAY['info', 'sor_transaction', 'paper_classification', 'urgency_triage'] AS backupSchemaList," +
                        "   '/home/dineshkumar.anandan@zucisystems.com/Documents/database_backup/' AS targetDirectory,\n" +
                        "    1 AS groupId,\n" +
                        "    1 AS tenantId,\n" +
                        "    1112 AS processId,\n" +
                        "    52732 AS rootPipelineId;\n")
                .build();
        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
        actionExecutionAudit.getContext().put("database.backup.file.name", "pg_dump_file");
        actionExecutionAudit.getContext().put("allow.backup.by.schema", "false");
        actionExecutionAudit.getContext().put("db.docker.user.name", "postgres");
        actionExecutionAudit.getContext().put("db.docker.host.value", "localhost");
        actionExecutionAudit.getContext().put("db.docker.port.value", "5432");
        actionExecutionAudit.getContext().put("db.docker.container.name.value", "pedantic_lovelace");
        DbBackupEaseAction dbBackupEaseAction = new DbBackupEaseAction(actionExecutionAudit, log, dataBaseModel);
        dbBackupEaseAction.execute();
    }

}