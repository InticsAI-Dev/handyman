package in.handyman.raven.lib.model.filemergerpdf;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.access.ResourceAccess;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.CoproProcessor;
import in.handyman.raven.lib.model.FileMergerPdf;
import okhttp3.*;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class FileMergerPdfConsumerProcess implements CoproProcessor.ConsumerProcess<FileMergerpdfInputEntity, FileMergerpdfOutputEntity> {
  private static final MediaType mediaTypeJSON = MediaType.parse("application/json; charset=utf-8");
  private final ActionExecutionAudit action;

  private final Logger log;

  private final Marker aMarker;
  private final ObjectMapper mapper = new ObjectMapper();


  private final FileMergerPdf fileMergerPdf;


  final OkHttpClient httpclient = new OkHttpClient.Builder()
          .connectTimeout(10, TimeUnit.MINUTES)
          .writeTimeout(10, TimeUnit.MINUTES)
          .readTimeout(10, TimeUnit.MINUTES)
          .build();

  public FileMergerPdfConsumerProcess(final Logger log, final Marker aMarker, final ActionExecutionAudit action, final Object fileMergerPdf) {
    this.fileMergerPdf = (FileMergerPdf) fileMergerPdf;

    this.action = action;
    this.log = log;
    this.aMarker = aMarker;

  }

  @Override
  public List<FileMergerpdfOutputEntity> process(URL endpoint, FileMergerpdfInputEntity entity) throws JsonProcessingException {
    List<FileMergerpdfOutputEntity> parentObj = new ArrayList<>();

    try {
      log.info(aMarker, "File merger Action for {} has been started", fileMergerPdf.getName());
      final Jdbi jdbi = ResourceAccess.rdbmsJDBIConn(fileMergerPdf.getResourceConn());


                try {
                  final List<String> filePathString = entity.getFilePaths();
                  final String outputDir = fileMergerPdf.getOutputDir();
                  final Long rootPipelineId =entity.getRootPipelineId();
                  final Long tenantId=entity.getTenantId();
                  final Long group_id= entity.getGroupId();
                  final String fileId=entity.getFileId();
                  final Long actionId =action.getActionId();
                  String fileMerger = "FILE_MERGER";
                  final String outputFileName = entity.getOutputFileName();
                  log.info(aMarker, "file path string {}", filePathString);
                  File file = new File(String.valueOf(filePathString));
                  log.info(aMarker, "created file {}", file);
                  log.info("check file is directory {}", file.isDirectory());
                  log.info("check file is a file path {}", file.isFile());


                  FileMergerPayload fileMergerPayload = new FileMergerPayload();
                  fileMergerPayload.setRootPipelineId(rootPipelineId);
                  fileMergerPayload.setProcess(fileMerger);
                  fileMergerPayload.setInputFilePaths(filePathString);
                  fileMergerPayload.setActionId(actionId);
                  fileMergerPayload.setOutputDir(outputDir);
                  fileMergerPayload.setOutputFileName(outputFileName);
                  fileMergerPayload.setProcessId(entity.getProcessId());
                  fileMergerPayload.setGroupId((entity.getGroupId()));
                  fileMergerPayload.setOriginId(entity.getOriginId());
                  fileMergerPayload.setTenantId(entity.getTenantId());

                  ObjectMapper objectMapper = new ObjectMapper();
                  String jsonInputRequest = objectMapper.writeValueAsString(fileMergerPayload);

                  TritonRequest requestBody = new TritonRequest();
                  requestBody.setName("MERGER START");
                  requestBody.setShape(List.of(1, 1));
                  requestBody.setDatatype("BYTES");
                  requestBody.setData(Collections.singletonList(jsonInputRequest));

                  TritonInputRequest tritonInputRequest = new TritonInputRequest();
                  tritonInputRequest.setInputs(Collections.singletonList(requestBody));

                  String jsonRequest = objectMapper.writeValueAsString(tritonInputRequest);


                  String tritonRequestActivator = action.getContext().get("triton.request.activator");

                  if (Objects.equals("false" , tritonRequestActivator)) {
                    Request request = new Request.Builder().url(endpoint)
                            .post(RequestBody.create(jsonInputRequest, mediaTypeJSON)).build();
                    coproRequestBuilder(entity, request, parentObj);
                  } else {
                    Request request = new Request.Builder().url(endpoint)
                            .post(RequestBody.create(jsonRequest, mediaTypeJSON)).build();
                    tritonRequestBuilder(entity, request, parentObj);
                  }


                  if (log.isInfoEnabled()) {
                    log.info("input object node in the consumer fileMerger  inputFilePath {}", filePathString);
                  }


                  if (log.isInfoEnabled()) {
                    log.info("input object node in the consumer fileMerger coproURL {}, inputFilePath {}", endpoint, filePathString);
                  }



                } catch (IOException ex) {
                  throw new RuntimeException(ex);
                }



    log.info(aMarker, "file merger Info Action for {} has been completed", fileMergerPdf.getName());

} catch (Exception e) {
      log.error(aMarker, "Error in file merger execute", e);
      throw new HandymanException("Exception occurred in file merger execute", e, action);
    }

      return parentObj;
    }

  private void coproRequestBuilder(FileMergerpdfInputEntity entity, Request request, List<FileMergerpdfOutputEntity> parentObj) {
    try (Response response = new OkHttpClient().newCall(request).execute()) {
      String responseBody = Objects.requireNonNull(response.body()).string();
      if (response.isSuccessful()) {

        extractedOutputDataRequest(entity, responseBody, parentObj, "", "");
      }
          else {
        // Handle non-successful response here
        log.error(aMarker, "Unsuccessful response received in copro: {}", response.code());
      }

  } catch (IOException e) {
        throw new RuntimeException(e);
    }

  }

  private void tritonRequestBuilder(FileMergerpdfInputEntity entity, Request request, List<FileMergerpdfOutputEntity> parentObj) throws IOException {
    try (Response response = new OkHttpClient().newCall(request).execute()) {
      String responseBody = Objects.requireNonNull(response.body()).string();
      if (response.isSuccessful()) {
        FileMergerResponse modelResponse = mapper.readValue(responseBody, FileMergerResponse.class);

        if (modelResponse.getOutputs() != null && !modelResponse.getOutputs().isEmpty()) {
          modelResponse.getOutputs().forEach(o -> o.getData().forEach(fileMergerDataItem -> {
            extractedOutputDataRequest(entity, fileMergerDataItem, parentObj, modelResponse.getModelName(), modelResponse.getModelVersion());
          }));
        }
      } else {
        // Handle non-successful response here
        log.error(aMarker, "Unsuccessful response received in triton: {}", response.code());
      }
    }
    catch(Exception e){
      log.error(aMarker, "Unsuccessful response received in triton: {}", e);
    }
  }

  private void extractedOutputDataRequest(FileMergerpdfInputEntity entity, String fileMergerDataItem, List<FileMergerpdfOutputEntity> parentObj, String modelName, String modelVersion) {
    Long rootPipelineId =entity.getRootPipelineId();
    Long tenantId=entity.getTenantId();
    try {
      FileMergerDataItem fileMergerDataItem1 = mapper.readValue(fileMergerDataItem, new TypeReference<>() {
      });
      parentObj.add(FileMergerpdfOutputEntity
              .builder()
              .processedFilePath(fileMergerDataItem1.getProcessedFilePath())
              .status(ConsumerProcessApiStatus.COMPLETED.getStatusDescription())
              .stage("fileMerger")
              .processId(fileMergerDataItem1.getProcessId())
              .message("file merger macro completed")
              .createdOn(Timestamp.valueOf(LocalDateTime.now()))
              .lastUpdatedOn(Timestamp.valueOf(LocalDateTime.now()))
              .rootPipelineId(rootPipelineId)
              .modelName(modelName)
              .tenantId(tenantId)
              .originId(fileMergerDataItem1.getOriginId())
              .groupId(fileMergerDataItem1.getGroupId())
              .processId(fileMergerDataItem1.getProcessId())
              .tenantId(fileMergerDataItem1.getTenantId())
              .modelName(modelName)
              .fileName(fileMergerDataItem1.getOutputFileName())
              .modelVersion(modelVersion)
              .build());
    } catch (JsonMappingException e) {

      parentObj.add(FileMergerpdfOutputEntity
              .builder()
              .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
              .stage("fileMerger")
              .originId(entity.getOriginId())
              .groupId(entity.getGroupId())
              .message("file merger macro failed")
              .createdOn(Timestamp.valueOf(LocalDateTime.now()))
              .lastUpdatedOn(Timestamp.valueOf(LocalDateTime.now()))
              .rootPipelineId(rootPipelineId)
              .tenantId(tenantId)
              .modelName(modelName)
              .modelVersion(modelVersion)
              .build());
      throw new HandymanException("exception in processing triton output response node",e,action);
    } catch (JsonProcessingException e) {
      parentObj.add(FileMergerpdfOutputEntity
              .builder()
              .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
              .stage("fileMerger")
              .originId(entity.getOriginId())
              .groupId(entity.getGroupId())
              .message("file merger macro failed")
              .createdOn(Timestamp.valueOf(LocalDateTime.now()))
              .lastUpdatedOn(Timestamp.valueOf(LocalDateTime.now()))
              .rootPipelineId(rootPipelineId)
              .tenantId(tenantId)
              .modelName(modelName)
              .modelVersion(modelVersion)
              .build());
      throw new HandymanException("exception in processing triton input node",e,action);
    }
  }

}





