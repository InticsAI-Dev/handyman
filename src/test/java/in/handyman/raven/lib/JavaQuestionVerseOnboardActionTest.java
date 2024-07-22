package in.handyman.raven.lib;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.JavaQuestionVerseOnboard;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.reflections.Reflections.log;

class JavaQuestionVerseOnboardActionTest {


    @Test
    void execute() throws Exception {

        JavaQuestionVerseOnboard javaQuestionVerseOnboard = JavaQuestionVerseOnboard.builder()
                .name("java QuestionVerse Onboard Action")
                .condition(true)
                .querySet("SELECT '/home/manikandan.tm@zucisystems.com/workspace/Load-balancer-spring-boot' as folder_path, 'JAVA' as project_type, 'PO' as document_type, 'Generate Unit test cases using mockito for the method: ${methodName} for the class: ${className} covering all corner cases. Make sure the generated test case should be compilable format.The test case name should be {className}{MethodNameWithoutParameters}Test.java\n" +
                        "Here is the Code:\n" +
                        "${code}' as default_prompt, 2 as tenant_id, 2 as user_id, 'CODE_SIP' as sip_type, 10 as model_registry_id;")
                .schemaName("sor_meta")
                .resourceConn("intics_agadia_db_conn")
                .build();

        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
        actionExecutionAudit.getContext().putAll(Map.ofEntries(Map.entry("read.batch.size","5"),
                Map.entry("write.batch.size","5"),
                Map.entry("codeGen.thread.count","1"),
                Map.entry("copro.codeGen.url","http://192.168.10.240:10009/chat/codestral?instruction=&prompt=")));

        JavaQuestionVerseOnboardAction javaQuestionVerseOnboardAction = new JavaQuestionVerseOnboardAction(actionExecutionAudit, log, javaQuestionVerseOnboard);
        javaQuestionVerseOnboardAction.execute();
    }

}