package in.handyman.raven.lib.custom.krypton.post.processing.bsh;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.lambda.access.ResourceAccess;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.encryption.inticsgrity.InticsIntegrity;
import in.handyman.raven.lib.model.kvp.llm.radon.processor.RadonQueryInputTable;
import in.handyman.raven.lib.model.kvp.llm.radon.processor.RadonQueryOutputTable;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class KryptonJsonDataTransformerTest {

    @Mock
    private Logger logger;
    @Mock
    private Marker marker;
    @Mock
    private ActionExecutionAudit action;
    @Mock
    private InticsIntegrity encryption;

    private KryptonJsonDataTransformer transformer;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        MockitoAnnotations.openMocks(this);
        Jdbi jdbi = ResourceAccess.rdbmsJDBIConn("intics_zio_db_conn");
        transformer = new KryptonJsonDataTransformer(logger, marker, objectMapper, action, jdbi, encryption);
    }

    @Test
    void processKryptonJsonData_shouldReturnMappedData() throws IOException {
        // Arrange
        ObjectMapper objectMapper = new ObjectMapper();

        String instanceVariableName = "KryptonTransformerFinalBsh";
        String className = "KryptonTransformerFinalBsh";
        String dummyJson = "{\"key\": \"value\"}";
        File jsonFile = new File("src/main/resources/input-json/sample.json");

        JsonNode jsonNode = objectMapper.readTree(jsonFile);

        String kryptonJson = jsonNode.toString();

        RadonQueryInputTable entity = mock(RadonQueryInputTable.class);
        when(entity.getCreatedOn()).thenReturn(Timestamp.valueOf(LocalDateTime.now()));
        when(entity.getTenantId()).thenReturn(1L);
        when(entity.getOriginId()).thenReturn("ORIGIN-1");
        when(entity.getPaperNo()).thenReturn(1);
        when(entity.getGroupId()).thenReturn(1L);
        when(entity.getInputFilePath()).thenReturn("/path/");
        when(entity.getProcessId()).thenReturn(1L);
        when(entity.getRootPipelineId()).thenReturn(1L);
        when(entity.getModelRegistry()).thenReturn("model");
        when(entity.getProcess()).thenReturn("process");
        when(entity.getApiName()).thenReturn("api");
        when(entity.getBatchId()).thenReturn("batch");
        when(entity.getCategory()).thenReturn("CATEGORY");

        Map<String, String> contextMap = new HashMap<>();
        contextMap.put(instanceVariableName, className);
        contextMap.put("document_type", "HEALTH_CARE");
        contextMap.put("tenant_id", "1");
        contextMap.put("KryptonTransformerFinalBsh", "KryptonTransformerFinalBsh");

        when(action.getContext()).thenReturn(contextMap);

        List<RadonQueryOutputTable> result = transformer.processKryptonJsonData(
                instanceVariableName, kryptonJson, entity, dummyJson, dummyJson, "endpoint"
        );

        System.out.println(result.toString());
    }

}
