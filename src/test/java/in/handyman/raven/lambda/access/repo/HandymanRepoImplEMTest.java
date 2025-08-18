package in.handyman.raven.lambda.access.repo;

import in.handyman.raven.lambda.doa.audit.HandymanExceptionAuditDetails;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

class HandymanRepoImplEMTest {

    @Test
    void getAllConfig() {
        var info = new HandymanRepoImpl();
        final Map<String, String> restApiActionTest = info.getAllConfig("RestApiActionTest");
        assert restApiActionTest != null && !restApiActionTest.isEmpty();
    }

    @Test
    void findAllHandymanExceptions() {
        var info = new HandymanRepoImpl();
        final List<HandymanExceptionAuditDetails> restApiActionTest = info.findAllHandymanExceptions();
        assert restApiActionTest != null && !restApiActionTest.isEmpty();

    }
    @Test
    void findHandymanExceptionsByRootPipelineId() {
        var info = new HandymanRepoImpl();
        Integer rootPipelineId=20803;
        final List<HandymanExceptionAuditDetails> restApiActionTest = info.findHandymanExceptionsByRootPipelineId(rootPipelineId);
        assert restApiActionTest != null && !restApiActionTest.isEmpty();
    }
}
