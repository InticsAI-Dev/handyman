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
import in.handyman.raven.lib.model.filemergerpdf.copro.FileMergerDataItemCopro;
import in.handyman.raven.lib.model.triton.PipelineName;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.commons.io.FilenameUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.Marker;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class FileMergerPdfConsumerProcess implements CoproProcessor.ConsumerProcess<FileMergerpdfInputEntity, FileMergerpdfOutputEntity> {
    private static final MediaType mediaTypeJSON = MediaType.parse("application/json; charset=utf-8");
    public static final String FILE_MERGER_PROCESS_NAME = PipelineName.FILE_MERGER.getProcessName();
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
                final Long rootPipelineId = entity.getRootPipelineId();
                final Long tenantId = entity.getTenantId();
                final Long group_id = entity.getGroupId();
                final String fileId = entity.getFileId();
                final Long actionId = action.getActionId();
                final String outputFileName = entity.getOutputFileName();
                log.info(aMarker, "file path string {}", filePathString);
                File file = new File(String.valueOf(filePathString));
                log.info(aMarker, "created file {}", file);
                log.info("check file is directory {}", file.isDirectory());
                log.info("check file is a file path {}", file.isFile());


                FileMergerPayload fileMergerPayload = new FileMergerPayload();
                fileMergerPayload.setRootPipelineId(rootPipelineId);
                fileMergerPayload.setProcess(FILE_MERGER_PROCESS_NAME);
                fileMergerPayload.setInputFilePaths(filePathString);
                fileMergerPayload.setActionId(actionId);
                fileMergerPayload.setOutputDir(outputDir);
                fileMergerPayload.setOutputFileName(outputFileName);
                fileMergerPayload.setProcessId(entity.getProcessId());
                fileMergerPayload.setGroupId(group_id);
                fileMergerPayload.setOriginId(entity.getOriginId());
                fileMergerPayload.setTenantId(tenantId);

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

                if (Objects.equals("false", tritonRequestActivator)) {
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
        try (Response response = httpclient.newCall(request).execute()) {
            String responseBody = Objects.requireNonNull(response.body()).string();
            if (response.isSuccessful()) {

                extractedCoproOutputResponse(entity, responseBody, parentObj, "", "");
            } else {
                // Handle non-successful response here
                log.error(aMarker, "Unsuccessful response received in copro: {}", response.code());
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void tritonRequestBuilder(FileMergerpdfInputEntity entity, Request request, List<FileMergerpdfOutputEntity> parentObj) throws IOException {
        try (Response response = httpclient.newCall(request).execute()) {
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
        } catch (Exception e) {
            log.error(aMarker, "Unsuccessful response received in triton: {}", e.getMessage());
        }
    }

    private void extractedOutputDataRequest(FileMergerpdfInputEntity entity, String fileMergerDataItem, List<FileMergerpdfOutputEntity> parentObj, String modelName, String modelVersion) {
        Long rootPipelineId = entity.getRootPipelineId();
        Long tenantId = entity.getTenantId();
        try {
            FileMergerDataItem fileMergerDataItem1 = mapper.readValue(fileMergerDataItem, new TypeReference<>() {
            });
            String processedFilePath = fileMergerDataItem1.getProcessedFilePath();
            String MultipartUploadActivatorVariable = "multipart.file.upload.activator";
            String MultipartUploadActivatorValue = action.getContext().get(MultipartUploadActivatorVariable);
            if (MultipartUploadActivatorValue.equalsIgnoreCase("true")) {
                try {
                    log.info("MultipartUploadActivator is true downloading file " + processedFilePath);
                    downloadResponseFile(processedFilePath, action, httpclient, log, aMarker);
                } catch (MalformedURLException e) {
                    log.error("Error in downloading merger response file with exception: {}", e.getMessage());
                }
            }
            File file = new File(processedFilePath);
            String fileExtension = FilenameUtils.getExtension(file.getName());
            float pageWidth = 0f;
            float pageHeight = 0f;
            int dpi = 0;
            if (fileExtension.equalsIgnoreCase("pdf")) {
                try (PDDocument document = PDDocument.load(file)) {

                    PDPage firstPage = document.getPage(0);
                    pageWidth = firstPage.getMediaBox().getWidth();
                    pageHeight = firstPage.getMediaBox().getHeight();

                    float width_inches = pageWidth / 72;
                    dpi = (int) (pageWidth / width_inches);
                    log.info("Page width: {}, height: {}, dpi {}", pageWidth, pageHeight, dpi);
                } catch (IOException e) {
                    log.error("Error in calculating width, height, dpi for pdf file with exception {}", e.getMessage());
                }
            } else {
                BufferedImage image = null;
                try {
                    image = ImageIO.read(file);
                } catch (IOException e) {
                    log.error("Error in calculating width, height, dpi for image file with exception {}", e.getMessage());
                }
                if (image != null) {
                    pageWidth = image.getWidth();
                    pageHeight = image.getHeight();
                    float width_inches = pageWidth / 72;
                    dpi = (int) (pageWidth / width_inches);
                } else {
                    log.error("Error in calculating width, height, dpi for image");
                }
            }
            parentObj.add(FileMergerpdfOutputEntity
                    .builder()
                    .processedFilePath(processedFilePath)
                    .status(ConsumerProcessApiStatus.COMPLETED.getStatusDescription())
                    .stage(FILE_MERGER_PROCESS_NAME)
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
                    .width(pageWidth)
                    .height(pageHeight)
                    .dpi(dpi)
                    .build());
        } catch (JsonMappingException e) {

            parentObj.add(FileMergerpdfOutputEntity
                    .builder()
                    .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                    .stage(FILE_MERGER_PROCESS_NAME)
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
            throw new HandymanException("exception in processing triton output response node", e, action);
        } catch (JsonProcessingException e) {
            parentObj.add(FileMergerpdfOutputEntity
                    .builder()
                    .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                    .stage(FILE_MERGER_PROCESS_NAME)
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
            throw new HandymanException("exception in processing triton input node", e, action);
        }
    }

    private void extractedCoproOutputResponse(FileMergerpdfInputEntity entity, String fileMergerDataItem, List<FileMergerpdfOutputEntity> parentObj, String modelName, String modelVersion) {
        Long rootPipelineId = entity.getRootPipelineId();
        Long tenantId = entity.getTenantId();
        Long group_id = entity.getGroupId();
        String outputFileName = entity.getOutputFileName();
        try {
            FileMergerDataItemCopro fileMergerDataItem1 = mapper.readValue(fileMergerDataItem, new TypeReference<>() {
            });
            String processedFilePath = fileMergerDataItem1.getProcessedFilePath();
            String MultipartUploadActivatorVariable = "multipart.file.upload.activator";
            String MultipartUploadActivatorValue = action.getContext().get(MultipartUploadActivatorVariable);
            if (MultipartUploadActivatorValue.equalsIgnoreCase("true")) {
                try {
                    downloadResponseFile(processedFilePath, action, httpclient, log, aMarker);
                } catch (MalformedURLException e) {
                    log.error("Error in downloading merger response file with exception: {}", e.getMessage());
                }
            }
            File file = new File(processedFilePath);
            String fileExtension = FilenameUtils.getExtension(file.getName());
            float pageWidth = 0f;
            float pageHeight = 0f;
            int dpi = 0;
            if (fileExtension.equalsIgnoreCase("pdf")) {
                try (PDDocument document = PDDocument.load(file)) {

                    PDPage firstPage = document.getPage(0);
                    pageWidth = firstPage.getMediaBox().getWidth();
                    pageHeight = firstPage.getMediaBox().getHeight();

                    float width_inches = pageWidth / 72;
                    dpi = (int) (pageWidth / width_inches);
                    log.info("Page width: {}, height: {}, dpi {}", pageWidth, pageHeight, dpi);
                } catch (IOException e) {
                    log.error("Error in calculating width, height, dpi with exception {}", e.getMessage());
                }
            } else {
                BufferedImage image = null;
                try {
                    image = ImageIO.read(file);
                } catch (IOException e) {
                    log.error("Error in calculating width, height, dpi for image file with exception {}", e.getMessage());
                }
                if (image != null) {
                    pageWidth = image.getWidth();
                    pageHeight = image.getHeight();
                    float width_inches = pageWidth / 72;
                    dpi = (int) (pageWidth / width_inches);
                } else {
                    System.err.println("Failed to read the image.");
                }
            }
            parentObj.add(FileMergerpdfOutputEntity
                    .builder()
                    .processedFilePath(processedFilePath)
                    .status(ConsumerProcessApiStatus.COMPLETED.getStatusDescription())
                    .stage(FILE_MERGER_PROCESS_NAME)
                    .processId(entity.getProcessId())
                    .message("file merger macro completed")
                    .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                    .lastUpdatedOn(Timestamp.valueOf(LocalDateTime.now()))
                    .rootPipelineId(rootPipelineId)
                    .modelName(modelName)
                    .tenantId(tenantId)
                    .originId(entity.getOriginId())
                    .groupId(group_id)
                    .modelName(modelName)
                    .fileName(outputFileName)
                    .processedFilePath(processedFilePath)
                    .modelVersion(modelVersion)
                    .width(pageWidth)
                    .height(pageHeight)
                    .dpi(dpi)
                    .build());
        } catch (JsonMappingException e) {

            parentObj.add(FileMergerpdfOutputEntity
                    .builder()
                    .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                    .stage(FILE_MERGER_PROCESS_NAME)
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
            throw new HandymanException("exception in processing triton output response node", e, action);
        } catch (JsonProcessingException e) {
            parentObj.add(FileMergerpdfOutputEntity
                    .builder()
                    .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                    .stage(FILE_MERGER_PROCESS_NAME)
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
            throw new HandymanException("exception in processing triton input node", e, action);
        }
    }

    private static void downloadResponseFile(String outputFilePath, ActionExecutionAudit action, OkHttpClient httpclient, Logger log, Marker aMarker) throws MalformedURLException {

        MediaType MEDIA_TYPE = MediaType.parse("application/json");
        String MultipartDownloadUrlVariable = "merger.response.download.url";
        String endPoint = action.getContext().get(MultipartDownloadUrlVariable);

        URL url = new URL(endPoint + "?filepath=" + outputFilePath);
        Request request = new Request.Builder().url(url)
                .addHeader("accept", "*/*")
                .post(RequestBody.create("{}", MEDIA_TYPE))
                .build();

        try (Response response = httpclient.newCall(request).execute()) {
            if (response.isSuccessful()) {

                log.info("Response is successful and Response Details: {}", response);
                log.info("Response is successful and header Details: {}", response.headers());

                try (ResponseBody responseBody = response.body()) {
                    if (responseBody != null) {
                        log.info("Response body is not null and content length is {}, and content type is {}", responseBody.contentLength(), responseBody.contentType());
                        try (InputStream inputStream = responseBody.byteStream()) {
                            Path path = Paths.get(outputFilePath);
                            File file = new File(outputFilePath);
                            if (!file.exists()) {
                                Files.createDirectories(path.getParent());
                                Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);
                            }
                        }
                    } else {
                        log.error("Error writing file response body is null");
                        HandymanException handymanException = new HandymanException("Error writing file response body is null");
                        HandymanException.insertException("Exception occurred in Writing multipart File for file - " + outputFilePath, handymanException, action);
                    }
                } catch (Exception e) {
                    log.error("Error writing file: {}", e.getMessage());
                    HandymanException handymanException = new HandymanException(e);
                    HandymanException.insertException("Exception occurred in Writing multipart File for table response file - " + outputFilePath, handymanException, action);
                }
            }
        } catch (Exception e) {
            log.error(aMarker, "The Exception occurred in Download multipart File for table response file {} with exception {}", outputFilePath, e.getMessage());
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("Exception occurred in Download multipart File for table response  file - " + outputFilePath, handymanException, action);
        }
    }

}





