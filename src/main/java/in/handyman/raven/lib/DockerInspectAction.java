package in.handyman.raven.lib;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.access.ResourceAccess;
import in.handyman.raven.lambda.action.ActionExecution;
import in.handyman.raven.lambda.action.IActionExecution;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.DockerInspect;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.net.URL;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import in.handyman.raven.lib.model.dockerinspect.dto.DockerInspectApiResponseDTO;
import in.handyman.raven.lib.model.dockerinspect.dto.ContainerInfoDTO;
import in.handyman.raven.lib.model.dockerinspect.dto.DockerInspectInputTable;
import in.handyman.raven.util.CommonQueryUtil;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.argument.Arguments;
import org.jdbi.v3.core.argument.NullArgument;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

/**
 * Auto Generated By Raven
 */
@ActionExecution(
    actionName = "DockerInspect"
)
public class DockerInspectAction implements IActionExecution {
  private final ActionExecutionAudit action;

  private final Logger log;

  private final DockerInspect dockerInspect;

  private final Marker aMarker;


  final OkHttpClient httpclient = new OkHttpClient.Builder()
          .connectTimeout(10, TimeUnit.MINUTES)
          .writeTimeout(10, TimeUnit.MINUTES)
          .readTimeout(10, TimeUnit.MINUTES)
          .build();


  public DockerInspectAction(final ActionExecutionAudit action, final Logger log,
      final Object dockerInspect) {
    this.dockerInspect = (DockerInspect) dockerInspect;
    this.action = action;
    this.log = log;
    this.aMarker = MarkerFactory.getMarker(" DockerInspect:"+this.dockerInspect.getName());
  }

  @Override
  public void execute() throws Exception {

    List<DockerInspectInputTable> dockerContainerInfoList = new ArrayList<>();

    try {
      final Jdbi jdbi = ResourceAccess.rdbmsJDBIConn(dockerInspect.getResourceConn());
      jdbi.getConfig(Arguments.class).setUntypedNullArgument(new NullArgument(Types.NULL));
      log.info(aMarker, "Docker Inspect Action for {} has started", dockerInspect.getName());

      // Assuming dockerInspectQuerySet fetches the list of containers you want to inspect
      final List<String> formattedQuery = CommonQueryUtil.getFormattedQuery(dockerInspect.getQuerySet());
      formattedQuery.forEach(sql -> jdbi.useTransaction(handle ->
              handle.createQuery(sql)
                      .mapToBean(DockerInspectInputTable.class)
                      .forEach(dockerContainerInfoList::add)));

      if (!dockerContainerInfoList.isEmpty()) {
          int endpointSize = dockerContainerInfoList.size();
          log.info("Endpoints are not empty for Docker Inspect with nodes count {}", endpointSize);

          final ExecutorService executorService = Executors.newFixedThreadPool(endpointSize);
          final CountDownLatch countDownLatch = new CountDownLatch(endpointSize);
          log.info("Total consumers {}", countDownLatch.getCount());

        dockerContainerInfoList.forEach(containerInfo -> executorService.submit(() -> {

          try {
            URL endpoint = new URL(containerInfo.getUtilsUrl());

            inspectContainer(containerInfo, endpoint, jdbi);
          } catch (Exception e) {
            log.error(aMarker, "The Exception occurred in Docker Inspect with exception {}", e.getMessage());
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("Exception occurred in Docker Inspect ", handymanException, this.action);
          } finally {
            log.info("Consumer {} completed the process", countDownLatch.getCount());
            countDownLatch.countDown();
          }
        }));

          try {
            countDownLatch.await();
          } catch (InterruptedException e) {
            log.error("Consumer Interrupted with exception", e);
            throw new HandymanException("Error in Docker Inspect execute method", e, action);
          } finally {
            executorService.shutdown();

        }
      } else {
        log.info("Docker Inspect input request list is empty");
      }

    } catch (Exception e) {
      throw new HandymanException("Error in Docker Inspect", e, action);
    }
  }


  public void inspectContainer(final DockerInspectInputTable containerInfo,final URL endpoint, final Jdbi jdbi) throws Exception {


    // Create URL for the Docker Inspect API request
    final URL url = new URL(endpoint.toString());

    // Build the request
    final Request request = new Request.Builder()
            .url(url)
            .addHeader("accept", "application/json")
            .get()
            .build();

    try (final Response response = httpclient.newCall(request).execute()) {
      final ObjectMapper objectMapper = new ObjectMapper();
      if (response.isSuccessful()) {
        if (response.body() != null) {
          final String responseBody = response.body().string();
          final DockerInspectApiResponseDTO containerInspectResponseDTO = objectMapper.readValue(responseBody, DockerInspectApiResponseDTO.class);
          containerInspectResponseDTO.getRunningContainers().forEach(containerInspectResponse -> handleInspectResponse(containerInfo,jdbi, containerInspectResponse ,objectMapper));

        }
      } else {
        // Handle unsuccessful response
        log.error("Request was not successful. HTTP Status: {}", response.code());
      }
    } catch (final Exception e) {
      log.error(aMarker, "Exception occurred during Docker Inspect with exception {}", e.getMessage());
      final HandymanException handymanException = new HandymanException(e);
      HandymanException.insertException("Exception occurred in Docker Inspect for container - " , handymanException, this.action);
    }
  }

  private void handleInspectResponse(final DockerInspectInputTable containerInfo, Jdbi jdbi, ContainerInfoDTO containerInspectResponse, ObjectMapper objectMapper) {
    try {

      jdbi.useHandle(handle -> {
        String sql = "INSERT INTO "+dockerInspect.getOutputTable()+"(" +
                "container_id, name, image, status, created, state, config, " +
                "host_config, network_settings, mounts, volumes, " +
                "ports, environment, labels, command, working_dir, " +
                "entrypoint, log_path, deleted_time, " +  // existing fields
                "tenant_id, root_pipeline_id, group_id, machine_id) " +  // new fields
                "VALUES (:containerId, :name, :image, :status, :created, :state, :config, " +
                ":hostConfig, :networkSettings, :mounts, :volumes, " +
                ":ports, :environment, :labels, :command, :workingDir, " +
                ":entrypoint, :logPath, :deletedTime, " +  // existing values
                ":tenantId, :rootPipelineId, :groupId, :machineId)";


        handle.createUpdate(sql)
                .bind("containerId", containerInspectResponse.getId())
                .bind("name", containerInspectResponse.getName())
                .bind("image", containerInspectResponse.getImage())
                .bind("status", containerInspectResponse.getStatus())
                .bind("created", containerInspectResponse.getCreated())
                .bind("state", objectMapper.writeValueAsString(containerInspectResponse.getState()))
                .bind("config", objectMapper.writeValueAsString(containerInspectResponse.getConfig()))
                .bind("hostConfig", objectMapper.writeValueAsString(containerInspectResponse.getHostConfig()))
                .bind("networkSettings", objectMapper.writeValueAsString(containerInspectResponse.getDockerIp()))
                .bind("mounts", objectMapper.writeValueAsString(containerInspectResponse.getMounts()))
                .bind("volumes", objectMapper.writeValueAsString(containerInspectResponse.getVolumes()))
                .bind("ports", objectMapper.writeValueAsString(containerInspectResponse.getPorts()))
                .bind("environment", containerInspectResponse.getEnvironment())
                .bind("labels", objectMapper.writeValueAsString(containerInspectResponse.getLabels()))
                .bind("command", containerInspectResponse.getCommand())
                .bind("workingDir", containerInspectResponse.getWorkingDir())
                .bind("entrypoint", containerInspectResponse.getEntrypoint())
                .bind("logPath", containerInspectResponse.getLogPath())
                .bind("deletedTime", LocalDateTime.now())
                .bind("tenantId", containerInfo.getTenantId())
                .bind("rootPipelineId", containerInfo.getRootPipelineId())
                .bind("groupId", containerInfo.getGroupId())
                .bind("machineId", containerInfo.getMachineId())
                .execute();
      });
    } catch (UnableToExecuteStatementException | JsonProcessingException exception) {
      log.error(aMarker, "Exception occurred in Docker Inspect insert: {}", exception.getMessage(), exception);
      HandymanException handymanException = new HandymanException(exception);
      HandymanException.insertException("Exception occurred in Docker Inspect insert for container - " + containerInspectResponse.getId(), handymanException, this.action);
    }
  }

  @Override
  public boolean executeIf() throws Exception {
    return dockerInspect.getCondition();
  }
}
