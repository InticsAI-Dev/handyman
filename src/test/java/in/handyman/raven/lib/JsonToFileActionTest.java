package in.handyman.raven.lib;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.JsonToFile;
import org.junit.jupiter.api.Test;

import static org.reflections.Reflections.log;

class JsonToFileActionTest {

    @Test
    void execute() throws Exception {

        JsonToFile jsonToFile = JsonToFile.builder()
                .filePath("/home/manikandan.tm@zucisystems.com/data/file.json")
                .resourceConn("intics_agadia_db_conn")
                .condition(true)
                .jsonSql("")
                .name("jsonToFile")
                .build();

        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
        actionExecutionAudit.setProcessId(138980079308730208L);

        JsonToFileAction jsonToFileAction = new JsonToFileAction(actionExecutionAudit, log, jsonToFile);

        jsonToFileAction.execute();
    }
}