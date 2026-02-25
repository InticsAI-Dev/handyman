package in.handyman.raven.lib.bsh;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.core.encryption.SecurityEngine;
import in.handyman.raven.core.encryption.impl.AESEncryptionImpl;
import in.handyman.raven.core.encryption.inticsgrity.InticsIntegrity;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lambda.doa.config.SpwBshConfig;
import in.handyman.raven.lib.PostProcessingExecutorAction;
import in.handyman.raven.lib.custom.kvp.post.processing.processor.ProviderDataTransformer;
import in.handyman.raven.lib.model.PostProcessingExecutor;
import in.handyman.raven.lib.model.common.CreateTimeStamp;
import in.handyman.raven.lib.model.kvp.llm.radon.processor.RadonQueryInputTable;
import in.handyman.raven.lib.model.kvp.llm.radon.processor.RadonQueryOutputTable;
import in.handyman.raven.lib.model.scalar.ValidatorByBeanShellExecutor;
import in.handyman.raven.lib.model.triton.ConsumerProcessApiStatus;
import in.handyman.raven.lib.services.sor.transform.PostProcessingFieldsInput;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class ProviderDataTransformerTest {



    private ValidatorByBeanShellExecutor validator;
    private List<PostProcessingFieldsInput> inputList;
    private Map<String, String> contextMap;
    private ActionExecutionAudit actionExecutionAudit;
    private ObjectMapper objectMapper;

    private Marker aMarker;
    @BeforeEach
    void setUp() throws IOException {
        inputList = new ArrayList<>();
        contextMap = new HashMap<>();
        actionExecutionAudit = new ActionExecutionAudit();
        actionExecutionAudit.getContext().put("multi.line.item.activator", ""); // To avoid NPE

        actionExecutionAudit.getContext().put("outbound.mapper.bsh.class.order","AumiGenderMapper, AumiMemberNameMapper, AumiMultiMemberMapper, AuthDischargeDateValidator, ClinicalPresentProcessor, FaxFromDateMapper, FaxReportProcessor, MedicaidMemberIdValidator, MemberAddressMapper, MemberDOBandServiceFromDateMapper, MemberIdValidator, MemberZipcodeMapper, NewbornDOBMapper, NewbornGenderMapper, NewbornNameMapper, NewBornRequestMapper, ProviderAddressMapper, ProviderNpiTinValidator, ProviderZipCodeMapper, ServiceToDateMapper,DiagnosisCodeValidator,ServiceCodeValidator");
        List<SpwBshConfig> bshConfigs = new ArrayList<>();
        actionExecutionAudit.getContext().put("ServiceCodeTransformerFinalBsh", "ServiceCodeTransformerFinalBsh");
        actionExecutionAudit.getContext().put("document_type", "MEDICAL_GBD");
        actionExecutionAudit.getContext().put("tenant_id", "1");



        actionExecutionAudit.getContext().put("tenant_id","1");
        actionExecutionAudit.setRootPipelineId(1L);
        // Default Context
        contextMap.put("multi.line.item.activator", "true");

        validator = new ValidatorByBeanShellExecutor(inputList, actionExecutionAudit, log, 2, bshConfigs);
        aMarker= MarkerFactory.getMarker("ProviderDataTransformerTest");
        objectMapper = new ObjectMapper();

    }

    @Test
    public void executeTest() throws Exception {
        InticsIntegrity securityEngine = SecurityEngine.getInticsIntegrityMethod(actionExecutionAudit, log);
        RadonQueryInputTable radonQueryInputTable = RadonQueryInputTable
                .builder()
                .createdOn(CreateTimeStamp.currentTimestamp())
                .originId("originId")
                .paperNo(123)
                .groupId(1L)
                .inputFilePath("")
                .actionId(1L)
                .tenantId(1L)
                .processId(1L)
                .rootPipelineId(1L)
                .modelRegistry("modelRegistry")
                .process("process")
                .batchId("batchId")
                .category("category")
                .sorContainerId(1L)
                .batchId("batchId")
                .category("category")
                .sorContainerId(1L)
                .build();
        String responsePayload = "{\n" +
                "  \"service\": [\n" +
                "    {\n" +
                "      \"service_code\": \"C1778\",\n" +
                "      \"modifiers\": \"1\",\n" +
                "      \"units\": \"2\",\n" +
                "      \"visits\": \"3\",\n" +
                "      \"source_label\": \"HCPCS Billing Code\",\n" +
                "      \"section_alias\": \"Authorization Status\"\n" +
                "    }," +
                "{\n" +
                "      \"service_code\": \"C171\",\n" +
                "      \"modifiers\": \"9\",\n" +
                "      \"units\": \"8\",\n" +
                "      \"visits\": \"7\",\n" +
                "      \"source_label\": \"Billing\",\n" +
                "      \"section_alias\": \"Alias\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        ProviderDataTransformer providerDataTransformer = new ProviderDataTransformer(log, aMarker, objectMapper,actionExecutionAudit, "intics_zio_db_conn", securityEngine);
        List<RadonQueryOutputTable> processProviderData = providerDataTransformer.processProviderData(getAuthIdValidator(),"ServiceCodeTransformerFinalBsh",responsePayload,radonQueryInputTable,"","","" );
        for (RadonQueryOutputTable processProviderDatum : processProviderData) {
            System.out.println("Transformed Data: " + processProviderDatum.getSorContainerInstance() + " \t\n " + processProviderDatum.getTotalResponseJson());
        }
    }


    String getAuthIdValidator() throws IOException {
        // read the code from a file
        File file = new File("src/test/java/in/handyman/raven/lib/bsh/kvp/ServiceCodeTransformerFinalBsh.txt");
        return fileReader(file);
    }

    @NotNull
    private String fileReader(File file) throws IOException {
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");

            }
            br.close();
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }


}
