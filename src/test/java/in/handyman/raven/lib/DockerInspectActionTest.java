package in.handyman.raven.lib;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.AssetInfo;
import in.handyman.raven.lib.model.DockerInspect;
import in.handyman.raven.lib.model.dockerinspect.dto.ContainerInfoDTO;
import in.handyman.raven.lib.model.dockerinspect.dto.DockerInspectApiResponseDTO;
import in.handyman.raven.lib.model.dockerinspect.dto.DockerInspectInputTable;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.Update;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class DockerInspectActionTest {
//
//    @Mock
//    private ActionExecutionAudit mockActionExecutionAudit;
//
//    @Mock
//    private DockerInspect mockDockerInspect;
//
//    @Mock
//    private Jdbi mockJdbi;
//
//    @Mock
//    private OkHttpClient mockHttpClient;
//
//    @Mock
//    private Response mockResponse;
//
//    @Mock
//    private Update mockUpdate;
//
//    @Mock
//    private ExecutorService mockExecutorService;
//
//    @InjectMocks
//    private DockerInspectAction dockerInspectAction;
//
//    private MockWebServer mockWebServer;
//
//    @BeforeEach
//    void setUp() throws Exception {
//        MockitoAnnotations.openMocks(this);
//        mockWebServer = new MockWebServer();
//        mockWebServer.start();
//        dockerInspectAction = new DockerInspectAction(mockActionExecutionAudit, mock(Logger.class), mockDockerInspect);
//    }
//
//    @Test
//    void testExecute_Success() throws Exception {
//        // Arrange
//        when(mockDockerInspect.getResourceConn()).thenReturn("mock-db");
//        when(mockDockerInspect.getQuerySet()).thenReturn("SELECT * FROM containers");
//        when(mockJdbi.getConfig(Arguments.class)).thenReturn(mock(Arguments.class));
//
//        List<DockerInspectInputTable> mockDockerInspectInputTables = new ArrayList<>();
//        DockerInspectInputTable inputTable = new DockerInspectInputTable();
//        inputTable.setUtilsUrl(mockWebServer.url("/").toString());
//        mockDockerInspectInputTables.add(inputTable);
//
//        // Mock query execution and return data
//        doAnswer(invocation -> {
//            mockDockerInspectInputTables.forEach(invocation.getArgument(0)::add);
//            return null;
//        }).when(mockJdbi).useTransaction(any());
//
//        mockWebServer.enqueue(new MockResponse().setBody("{ \"runningContainers\": [] }").setResponseCode(200));
//
//        // Act
//        dockerInspectAction.execute();
//
//        // Assert
//        verify(mockJdbi, times(1)).useTransaction(any());
//        assertEquals(0, mockWebServer.getRequestCount());
//    }
//
//    @Test
//    void testExecute_EmptyList() throws Exception {
//        // Arrange
//        when(mockDockerInspect.getResourceConn()).thenReturn("mock-db");
//        when(mockDockerInspect.getQuerySet()).thenReturn("SELECT * FROM containers");
//
//        // Return empty list
//        doAnswer(invocation -> null).when(mockJdbi).useTransaction(any());
//
//        // Act
//        dockerInspectAction.execute();
//
//        // Assert
//        verify(mockJdbi, times(1)).useTransaction(any());
//        verify(mockExecutorService, never()).submit(any(Runnable.class));
//    }
//
//    @Test
//    void testExecute_ThrowsException() {
//        // Arrange
//        when(mockDockerInspect.getResourceConn()).thenReturn("mock-db");
//        when(mockDockerInspect.getQuerySet()).thenReturn("SELECT * FROM containers");
//        doThrow(new RuntimeException("DB Error")).when(mockJdbi).useTransaction(any());
//
//        // Act & Assert
//        assertThrows(HandymanException.class, () -> dockerInspectAction.execute());
//    }
//
//    @Test
//    void testInspectContainer_Success() throws Exception {
//        // Arrange
//        URL endpoint = new URL(mockWebServer.url("/").toString());
//
//        mockWebServer.enqueue(new MockResponse().setBody("{ \"runningContainers\": [] }").setResponseCode(200));
//
//        // Act
//        dockerInspectAction.inspectContainer(endpoint, mockJdbi);
//
//        // Assert
//        assertEquals(1, mockWebServer.getRequestCount());
//    }
//
//    @Test
//    void testInspectContainer_Failure() throws Exception {
//        // Arrange
//        URL endpoint = new URL(mockWebServer.url("/").toString());
//        mockWebServer.enqueue(new MockResponse().setResponseCode(500)); // Simulate server error
//
//        // Act
//        dockerInspectAction.inspectContainer(endpoint, mockJdbi);
//
//        // Assert
//        assertEquals(1, mockWebServer.getRequestCount());
//    }
//
//    @Test
//    void testHandleInspectResponse_InsertSuccess() throws Exception {
//        // Arrange
//        ContainerInfoDTO mockContainerInfo = new ContainerInfoDTO();
//        mockContainerInfo.setId("container-id");
//
//        ObjectMapper objectMapper = new ObjectMapper();
//        when(mockJdbi.useHandle(any())).thenReturn(mockUpdate);
//
//        // Act
//        dockerInspectAction.handleInspectResponse(mockJdbi, mockContainerInfo, objectMapper);
//
//        // Assert
//        verify(mockUpdate, times(1)).execute();
//    }
//
//    @Test
//    void testHandleInspectResponse_InsertFailure() throws Exception {
//        // Arrange
//        ContainerInfoDTO mockContainerInfo = new ContainerInfoDTO();
//        mockContainerInfo.setId("container-id");
//
//        ObjectMapper objectMapper = new ObjectMapper();
//        doThrow(new UnableToExecuteStatementException("DB Error")).when(mockJdbi).useHandle(any());
//
//        // Act & Assert
//        assertThrows(HandymanException.class, () -> dockerInspectAction.handleInspectResponse(mockJdbi, mockContainerInfo, objectMapper));
//    }


    @Test
    void testingDockerInspect() throws Exception {
        DockerInspect assetInfo= DockerInspect
                .builder()
                .name("info")
                .resourceConn("intics_zio_db_conn")
                .condition(Boolean.TRUE)
                .apiUrl("http://localhost:8000/docker-inspect/running")
                .consumerApiCount("1")
                .inputTable("instance_audit.machine")
                .outputTable("instance_audit.container_inspect")
                .querySet("select m.machine_name ,m.machine_ip , m.machine_hostname ,m.etc_host_name ,m.utils_url ,m.cluster_id,1 as tenant_id,1 as root_pipeline_id,1 as group_id,m.machine_id as machine_id \n" +
                        "from instance_config.cluster c \n" +
                        "join instance_config.machine m on c.cluster_id =m.cluster_id \n" +
                        "where m.created_user_id =1;")
                .build();
        final ActionExecutionAudit action = ActionExecutionAudit.builder().build();
        action.setRootPipelineId(11011L);
        action.getContext().put("process-id","1234567");
        action.getContext().put("tenant_id","1");
        action.getContext().put("root_pipeline_id","1234567");
        action.getContext().put("group_id","1234567");
        DockerInspectAction assetInfoAction=new DockerInspectAction(action ,log,assetInfo);
        assetInfoAction.execute();
    }

}

