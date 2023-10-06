package in.handyman.raven.lib.model.fileMergerPdf;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.access.ResourceAccess;
import in.handyman.raven.lambda.action.ActionExecution;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.CoproProcessor;

import java.sql.Timestamp;
import java.time.LocalDateTime;


import in.handyman.raven.lib.model.FileMergerPdf;
import in.handyman.raven.util.CommonQueryUtil;
import in.handyman.raven.util.ExceptionUtil;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.result.ResultIterable;
import org.jdbi.v3.core.statement.Query;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class FileMergerPdfConsumerProcess implements CoproProcessor.ConsumerProcess<FileMergerpdfInputEntity, FileMergerpdfOutputEntity> {
  private static final MediaType MediaTypeJSON = MediaType.parse("application/json; charset=utf-8");
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


                  if (log.isInfoEnabled()) {
                    log.info("input object node in the consumer fileMerger  inputFilePath {}", filePathString);
                  }

                  Request Requests = new Request.Builder().url(endpoint).post(RequestBody.create(jsonRequest, MediaTypeJSON)).build();

                  if (log.isInfoEnabled()) {
                    log.info("input object node in the consumer fileMerger coproURL {}, inputFilePath {}", endpoint, filePathString);
                  }

                  try (Response response = new OkHttpClient().newCall(Requests).execute()) {
                    String responseBody = Objects.requireNonNull(response.body()).string();
                    if (response.isSuccessful()) {
                      FileMergerResponse modelResponse = mapper.readValue(responseBody, FileMergerResponse.class);

                      if (modelResponse.getOutputs() != null && !modelResponse.getOutputs().isEmpty()) {
                        modelResponse.getOutputs().forEach(o -> o.getData().forEach(fileMergerDataItem -> {
                          try {
                            FileMergerDataItem fileMergerDataItem1 = mapper.readValue(fileMergerDataItem, new TypeReference<>() {
                            });
                            parentObj.add(FileMergerpdfOutputEntity
                                    .builder()
                                    .processedFilePath(fileMergerDataItem1.getProcessedFilePath())
                                    .status(ConsumerProcessApiStatus.COMPLETED.getStatusDescription())
                                    .stage(fileMerger)
                                    .processId(entity.getProcessId())
                                    .message("file merger macro completed")
                                    .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                                    .lastUpdatedOn(Timestamp.valueOf(LocalDateTime.now()))
                                    .rootPipelineId(rootPipelineId)
                                    .modelName(modelResponse.getModelName())
                                    .tenantId(tenantId)
                                    .originId(entity.getOriginId())
                                    .groupId(group_id)
                                    .modelName(modelResponse.getModelName())
                                    .fileName(outputFileName)
                                    .processedFilePath(fileMergerDataItem1.getProcessedFilePath())
                                    .modelVersion(modelResponse.getModelVersion())
                                    .build());
                          } catch (JsonMappingException e) {

                            parentObj.add(FileMergerpdfOutputEntity
                                    .builder()
                                    .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                                    .stage(fileMerger)
                                    .originId(entity.getOriginId())
                                    .groupId(entity.getGroupId())
                                    .message("file merger macro failed")
                                    .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                                    .lastUpdatedOn(Timestamp.valueOf(LocalDateTime.now()))
                                    .rootPipelineId(rootPipelineId)
                                    .tenantId(tenantId)
                                    .modelName(modelResponse.getModelName())
                                    .modelVersion(modelResponse.getModelVersion())
                                    .build());
                            throw new HandymanException("exception in processing triton output response node",e,action);
                          } catch (JsonProcessingException e) {
                            parentObj.add(FileMergerpdfOutputEntity
                                    .builder()
                                    .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                                    .stage(fileMerger)
                                    .originId(entity.getOriginId())
                                    .groupId(entity.getGroupId())
                                    .message("file merger macro failed")
                                    .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                                    .lastUpdatedOn(Timestamp.valueOf(LocalDateTime.now()))
                                    .rootPipelineId(rootPipelineId)
                                    .tenantId(tenantId)
                                    .modelName(modelResponse.getModelName())
                                    .modelVersion(modelResponse.getModelVersion())
                                    .build());
                            throw new HandymanException("exception in processing triton input node",e,action);
                          }
                        }));
                      }
                    } else {
                      // Handle non-successful response here
                      log.error(aMarker, "Non-successful response received: {}", response.code());
                    }
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

    }





