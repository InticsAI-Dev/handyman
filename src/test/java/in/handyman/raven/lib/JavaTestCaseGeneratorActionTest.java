package in.handyman.raven.lib;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.JavaTestCaseGenerator;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.reflections.Reflections.log;

class JavaTestCaseGeneratorActionTest {

    @Test
    void execute() throws Exception {

        JavaTestCaseGenerator javaTestCaseGenerator = JavaTestCaseGenerator.builder()
                .name("Java Test Case Generator")
                .querySet("SELECT 'NPOS' as document_type, 'JAVA' as project_type, '/home/manikandan.tm@zucisystems.com/workspace/alchemy/src/main/java/' as folder_path, 1 as tenant_id, array['ImportService', 'TruthService']::text[] as class_values, 1 as group_id, 'sor_meta_code' as question_verse_schema;")
                .outputTable("test_case_details.project_details")
                .condition(true)
                .resourceConn("intics_agadia_db_conn")
                .build();

        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
        actionExecutionAudit.getContext().putAll(Map.ofEntries(Map.entry("read.batch.size","5"),
                Map.entry("write.batch.size","5"),
                Map.entry("codeGen.thread.count","1"),
                Map.entry("copro.codeGen.url","http://192.168.10.240:10009/chat/codestral?instruction=&prompt=")));

        JavaTestCaseGeneratorAction javaTestCaseGeneratorAction = new JavaTestCaseGeneratorAction(actionExecutionAudit, log, javaTestCaseGenerator);
        javaTestCaseGeneratorAction.execute();

    }
}