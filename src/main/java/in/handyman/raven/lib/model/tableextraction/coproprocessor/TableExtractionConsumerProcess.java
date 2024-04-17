package in.handyman.raven.lib.model.tableextraction.coproprocessor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.CoproProcessor;
import in.handyman.raven.lib.model.paperitemizer.ProcessAuditOutputTable;
import in.handyman.raven.lib.model.tableextraction.response.TableOutputResponse;
import in.handyman.raven.lib.model.triton.ConsumerProcessApiStatus;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class TableExtractionConsumerProcess implements CoproProcessor.ConsumerProcess<TableExtractionInputTable, TableExtractionOutputTable> {

    private final Logger log;
    private final Marker aMarker;
    private final ObjectMapper mapper = new ObjectMapper();
    private static final MediaType MediaTypeJSON = MediaType
            .parse("application/json; charset=utf-8");
    private final String outputDir;
    private final List<ProcessAuditOutputTable> processAuditOutput = new ArrayList<>();

    public final ActionExecutionAudit action;
    final OkHttpClient httpclient = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.MINUTES)
            .writeTimeout(10, TimeUnit.MINUTES)
            .readTimeout(10, TimeUnit.MINUTES)
            .build();

    public TableExtractionConsumerProcess(Logger log, Marker aMarker, String outputDir, ActionExecutionAudit action) {
        this.log = log;
        this.aMarker = aMarker;
        this.outputDir = outputDir;
        this.action = action;
    }

    @Override
    public List<TableExtractionOutputTable> process(URL endpoint, TableExtractionInputTable entity) throws Exception {
        log.info(aMarker, "coproProcessor consumer process started with endpoint {} and entity {}", endpoint, entity);
        List<TableExtractionOutputTable> parentObj = new ArrayList<>();
        final ObjectNode objectNode = mapper.createObjectNode();
        String inputFilePath = entity.getFilePath();
        Long rootPipelineId = action.getRootPipelineId();
        final String tableExtractionProcessName = "TABLE_EXTRACTION";
        Long actionId = action.getActionId();
        objectNode.put("rootPipelineId", rootPipelineId);
        objectNode.put("process", tableExtractionProcessName);
        objectNode.put("inputFilePath", inputFilePath);
        objectNode.put("outputDir", outputDir);
        objectNode.put("actionId", actionId);

        log.info(aMarker, "coproProcessor mapper object node {}", objectNode);
        Request request = new Request.Builder().url(endpoint)
                .post(RequestBody.create(objectNode.toString(), MediaTypeJSON)).build();

        if (log.isInfoEnabled()) {
            log.info(aMarker, "Request has been build with the parameters \n URI : {}, with inputFilePath {} and outputDir {}", endpoint, inputFilePath, outputDir);
        }
        String originId = entity.getOriginId();
        Integer groupId = entity.getGroupId();
        String templateId = entity.getTemplateId();
        Long tenantId = entity.getTenantId();
        Long processId = entity.getProcessId();


        try (Response response = httpclient.newCall(request).execute()) {

            if (log.isInfoEnabled())
                log.info(aMarker, "coproProcessor consumer process response with status{}, and message as {}, ", response.isSuccessful(), response.message());
            if (response.isSuccessful()) {
                log.info(aMarker, "coproProcessor consumer process response status {}", response.message());

                String responseBody = Objects.requireNonNull(response.body()).string();
                processAuditOutput.add(
                        ProcessAuditOutputTable.builder()
                                .originId(originId)
                                .tenantId(tenantId)
                                .batchId("1")
                                .endpoint(String.valueOf(endpoint))
                                .rootPipelineId(entity.getRootPipelineId())
                                .request(objectNode.toString())
                                .response(responseBody)
                                .stage(tableExtractionProcessName)
                                .message("Table Extraction macro completed")
                                .status(ConsumerProcessApiStatus.COMPLETED.getStatusDescription())
                                .build());
                List<TableOutputResponse> tableOutputResponses = mapper.readValue(responseBody, new TypeReference<>() {
                });
                tableOutputResponses.forEach(tableOutputResponse1 -> {
                    String csvTablesPath = tableOutputResponse1.getCsvTablesPath();
                    String croppedImagePath = tableOutputResponse1.getCroppedImage();
                    doMultipartDownloadCheck(csvTablesPath, croppedImagePath);
                    String tableResponse;
                    try {
                        tableResponse = tableDataJson(csvTablesPath, action);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }

                    parentObj.add(
                            TableExtractionOutputTable
                                    .builder()
                                    .originId(entity.getOriginId())
                                    .paperNo(entity.getPaperNo())
                                    .tableResponse(tableResponse)
                                    .processedFilePath(tableOutputResponse1.getCsvTablesPath())
                                    .croppedImage(croppedImagePath)
                                    .bboxes(tableOutputResponse1.getBboxes().asText())
                                    .groupId(groupId)
                                    .processId(action.getProcessId())
                                    .templateId(templateId)
                                    .tenantId(tenantId)
                                    .status("COMPLETED")
                                    .stage(tableExtractionProcessName)
                                    .message(response.message())
                                    .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                                    .rootPipelineId(action.getRootPipelineId())
                                    .truthEntityId(entity.getTruthEntityId())
                                    .sorContainerId(entity.getSorContainerId())
                                    .channelId(entity.getChannelId())
                                    .modelName(action.getContext().get("NEON_MODEL_NAME"))
                                    .build());

                });

            } else {
                parentObj.add(
                        TableExtractionOutputTable
                                .builder()
                                .originId(Optional.ofNullable(originId).map(String::valueOf).orElse(null))
                                .groupId(groupId)
                                .processId(action.getProcessId())
                                .templateId(templateId)
                                .tenantId(tenantId)
                                .status("FAILED")
                                .stage(tableExtractionProcessName)
                                .message(response.message())
                                .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                                .rootPipelineId(action.getRootPipelineId())
                                .truthEntityId(entity.getTruthEntityId())
                                .sorContainerId(entity.getSorContainerId())
                                .channelId(entity.getChannelId())
                                .modelName(action.getContext().get("NEON_MODEL_NAME"))
                                .build());
                log.error(aMarker, "Error in response {}", response.message());
                processAuditOutput.add(
                        ProcessAuditOutputTable.builder()
                                .originId(originId)
                                .tenantId(tenantId)
                                .batchId("1")
                                .rootPipelineId(entity.getRootPipelineId())
                                .stage(tableExtractionProcessName)
                                .message(response.message())
                                .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                                .build());
            }
        } catch (Exception exception) {
            parentObj.add(
                    TableExtractionOutputTable
                            .builder()
                            .originId(Optional.ofNullable(originId).map(String::valueOf).orElse(null))
                            .groupId(groupId)
                            .processId(action.getProcessId())
                            .templateId(templateId)
                            .tenantId(tenantId)
                            .status("FAILED")
                            .stage(tableExtractionProcessName)
                            .message(exception.getMessage())
                            .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                            .rootPipelineId(action.getRootPipelineId())
                            .truthEntityId(entity.getTruthEntityId())
                            .sorContainerId(entity.getSorContainerId())
                            .channelId(entity.getChannelId())
                            .modelName(action.getContext().get("NEON_MODEL_NAME"))
                            .build());
            HandymanException handymanException = new HandymanException(exception);
            HandymanException.insertException("Table Extraction  consumer failed for originId " + originId, handymanException, this.action);
            log.error(aMarker, "The Exception occurred in request {}", request, exception);
        }
        log.info(aMarker, "coproProcessor consumer process with output entity {}", parentObj);
        return parentObj;
    }

    @Override
    public List<ProcessAuditOutputTable> processAudit() throws Exception {
        return processAuditOutput;
    }

    private void doMultipartDownloadCheck(String csvTablesPath, String croppedImagePath) {
        String multipartUploadActivatorVariable = "multipart.file.upload.activator";
        String multipartUploadActivatorValue = action.getContext().get(multipartUploadActivatorVariable);
        if (multipartUploadActivatorValue.equalsIgnoreCase("true")) {
            log.info("MultipartUploadActivator is true");
            String MultipartDownloadUrlVariable = "table.response.download.url";
            String endPoint = action.getContext().get(MultipartDownloadUrlVariable);

            final List<URL> urls = Optional.ofNullable(endPoint).map(s -> Arrays.stream(s.split(",")).map(s1 -> {
                try {
                    return new URL(s1);
                } catch (MalformedURLException e) {
                    log.error("Error in processing the URL ", e);
                    throw new RuntimeException(e);
                }
            }).collect(Collectors.toList())).orElse(Collections.emptyList());
            if (csvTablesPath != null && !csvTablesPath.isEmpty()) {
                try {
                    log.info("MultipartUploadActivator is true downloading file " + csvTablesPath);
                    downloadResponseFile(csvTablesPath, action, httpclient, log, aMarker, urls);
                } catch (MalformedURLException e) {
                    log.error("Error writing table Response csv file: {}", e.getMessage());
                }
            }
            if (croppedImagePath != null && !croppedImagePath.isEmpty()) {
                try {
                    log.info("MultipartUploadActivator is true downloading file " + csvTablesPath);
                    downloadResponseFile(croppedImagePath, action, httpclient, log, aMarker, urls);
                } catch (MalformedURLException e) {
                    log.error("Error writing table Response cropped image file: {}", e.getMessage());
                }
            }
        }
    }

    private static void downloadResponseFile(String outputFilePath, ActionExecutionAudit action, OkHttpClient httpclient, Logger log, Marker aMarker, List<URL> urls) throws MalformedURLException {

        MediaType MEDIA_TYPE = MediaType.parse("application/json");

        if (!urls.isEmpty()) {

            int endpointSize = urls.size();
            log.info("Endpoints are not empty for multipart download with nodes count {}", endpointSize);

            urls.forEach(url -> {
                try {
                    downloadResponseFileFromServer(outputFilePath, action, httpclient, log, aMarker, url.toString(), MEDIA_TYPE);
                } catch (Exception e) {
                    log.error(aMarker, "The Exception occurred in multipart file download for file {} with exception {}", outputFilePath, e.getMessage());
                    HandymanException handymanException = new HandymanException(e);
                    HandymanException.insertException("Exception occurred in multipart download for file - " + outputFilePath, handymanException, action);
                }
            });
        } else {
            log.error(aMarker, "Endpoints for multipart download is empty");
        }

    }

    private static void downloadResponseFileFromServer(String outputFilePath, ActionExecutionAudit action, OkHttpClient httpclient, Logger log, Marker aMarker, String endPoint, MediaType MEDIA_TYPE) throws MalformedURLException {
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

    public static String tableDataJson(String filePath, ActionExecutionAudit action) throws JsonProcessingException {
        String fileNameStr = getCsvFilePathName(filePath);

        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            String removeFirstRow = action.getContext().get("table.extraction.header.exclude");
            if (Objects.equals("true", removeFirstRow)) {
                reader.readNext();
            }

            String[] headers = reader.readNext(); // Read the headers

            JSONArray dataArray = new JSONArray(); // Array for data rows
            JSONArray headersArray = new JSONArray(); // Array for column headers

            // Convert headers to JSON
            for (String header : headers) {
                headersArray.put(header);
            }

            String[] row;

            while ((row = reader.readNext()) != null) {
                JSONArray rowArray = new JSONArray();

                // Convert data row to JSON
                for (int i = 0; i < headers.length; i++) {
                    rowArray.put(row[i]);
                }
                dataArray.put(rowArray);
            }

            // Create the main JSON object
            JSONObject json = new JSONObject();
            json.put("csvFilePath", filePath);
            json.put("csvFileName", fileNameStr);
            json.put("data", dataArray);
            json.put("columnHeaders", headersArray);
            return json.toString();


        } catch (CsvValidationException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    private static String getCsvFilePathName(String filePath) {
        File file = new File(filePath);

        String fileNameStr = FilenameUtils.removeExtension(file.getName());
        return fileNameStr;
    }

    public static Long getPaperNobyFileName(String filePath) {
        Long extractedNumber = null;
        String fileNameStr = getCsvFilePathName(filePath);

        String[] parts = fileNameStr.split("_");

        // Check if there are at least two parts (0 and 1 after the first underscore)
        if (parts.length >= 2) {
            // Extract the second part (index 1 in the array after splitting)
            String number = parts[parts.length - 2];

            // Convert the extracted string to an integer if needed
            extractedNumber = Long.parseLong(number);

            // Print the extracted number
            return extractedNumber + 1;
        }

        return extractedNumber;
    }


}
