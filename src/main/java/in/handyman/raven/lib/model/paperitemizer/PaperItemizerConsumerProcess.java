package in.handyman.raven.lib.model.paperitemizer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.CoproProcessor;
import in.handyman.raven.lib.encryption.SecurityEngine;
import in.handyman.raven.lib.model.PaperItemizer;
import in.handyman.raven.lib.model.common.CreateTimeStamp;
import in.handyman.raven.lib.model.paperitemizer.copro.PaperItemizerDataItemCopro;
import in.handyman.raven.lib.model.triton.*;
import in.handyman.raven.lib.utils.FileProcessingUtils;
import in.handyman.raven.lib.utils.ProcessFileFormatE;
import javassist.NotFoundException;
import okhttp3.*;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static in.handyman.raven.lib.encryption.EncryptionConstants.ENCRYPT_REQUEST_RESPONSE;

public class PaperItemizerConsumerProcess implements CoproProcessor.ConsumerProcess<PaperItemizerInputTable, PaperItemizerOutputTable> {

    public static final String TRITON_REQUEST_ACTIVATOR = "triton.request.activator";
    public static final String modelRegistry = "paper.itemizer.model.name";
    public static final String PROCESS_NAME = PipelineName.PAPER_ITEMIZER.getProcessName();
    public static final String PAPER_ITERATOR_START = "PAPER ITERATOR START";
    private final Logger log;
    private final Marker aMarker;
    private static final MediaType mediaTypeJson = MediaType.parse("application/json; charset=utf-8");
    private final String outputDir;
    private final FileProcessingUtils fileProcessingUtils;

    public final ActionExecutionAudit action;
    final OkHttpClient httpclient = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.MINUTES).writeTimeout(10, TimeUnit.MINUTES).readTimeout(10, TimeUnit.MINUTES).build();

    private final String processBase64;

    private final PaperItemizer paperItemizer;

    public PaperItemizerConsumerProcess(Logger log, Marker aMarker, String outputDir, FileProcessingUtils fileProcessingUtils, ActionExecutionAudit action, String processBase64, PaperItemizer paperItemizer) {
        this.log = log;
        this.aMarker = aMarker;
        this.outputDir = outputDir;
        this.fileProcessingUtils = fileProcessingUtils;
        this.action = action;

        this.processBase64 = processBase64;
        this.paperItemizer = paperItemizer;
    }

    @Override
    public List<PaperItemizerOutputTable> process(URL endpoint, PaperItemizerInputTable entity) throws Exception {
        log.info(aMarker, "coproProcessor consumer process started with endpoint {} and File path {}", endpoint, entity.getFilePath());
        PdfToPaperItemizer pdfToPaperItemizer = new PdfToPaperItemizer(action, log);
        List<PaperItemizerOutputTable> parentObj = new ArrayList<>();
        String selectedModelName = action.getContext().get(modelRegistry);


        if (ModelRegistry.ARGON.name().equals(selectedModelName)) {
            parentObj = paperItemizationCoproApi(entity, action, endpoint, paperItemizer.getOutputDir());
        } else if (ModelRegistry.XENON.name().equals(selectedModelName)) {
            parentObj = pdfToPaperItemizer.paperItemizer(entity.getFilePath(), paperItemizer.getOutputDir(), entity);
        } else {
            String errorMessage = "Invalid model selected for paper itemizer: " + modelRegistry;
            throw new HandymanException(errorMessage, new NotFoundException(errorMessage), action);
        }

        if (log.isInfoEnabled()) {
            log.info(aMarker, "Request has been build with the parameters \n URI : {}, with entity {}", endpoint, entity.getFilePath());
        }

        return parentObj;
    }


    private void coproRequestBuilder(PaperItemizerInputTable entity, Request request, ObjectMapper objectMapper, List<PaperItemizerOutputTable> parentObj, String jsonInputRequest, URL endpoint) {
        String originId = entity.getOriginId();
        Integer groupId = entity.getGroupId();
        String templateId = entity.getTemplateId();
        Long tenantId = entity.getTenantId();
        Long processId = entity.getProcessId();
        String batchId = entity.getBatchId();

        try (Response response = httpclient.newCall(request).execute()) {

            if (response.isSuccessful()) {
                if (log.isInfoEnabled()) {
                    log.info(aMarker, "successfully received coproProcessor consumer process response for file {} ", entity.getFilePath());
                }
                String responseBody = Objects.requireNonNull(response.body()).string();
                extractedCoproOutputResponse(entity, objectMapper, parentObj, "", "", responseBody, jsonInputRequest, responseBody, endpoint.toString());

            } else {
                String errorMessage = "Unsuccessful response for batch/group " + entity.getGroupId() + " origin Id " + entity.getOriginId() + "code : " + response.code() + " message : " + response.message();
                parentObj.add(PaperItemizerOutputTable.builder().originId(Optional.ofNullable(originId).map(String::valueOf).orElse(null)).groupId(groupId).processId(processId).templateId(templateId).tenantId(tenantId).status(ConsumerProcessApiStatus.FAILED.getStatusDescription()).stage(PROCESS_NAME).message(response.message()).createdOn(entity.getCreatedOn()).lastUpdatedOn(CreateTimeStamp.currentTimestamp()).rootPipelineId(entity.getRootPipelineId()).batchId(batchId).request(encryptRequestResponse(jsonInputRequest)).response(encryptRequestResponse(errorMessage)).endpoint(String.valueOf(endpoint)).build());
                HandymanException handymanException = new HandymanException(errorMessage);
                HandymanException.insertException(errorMessage, handymanException, this.action);

                log.error(aMarker, errorMessage);
            }

        } catch (Exception exception) {
            String errorMessage = "Exception occurred for batch/group " + entity.getGroupId() + " origin Id " + entity.getOriginId() + " message : " + exception.getMessage();
            parentObj.add(PaperItemizerOutputTable.builder().originId(Optional.ofNullable(originId).map(String::valueOf).orElse(null)).groupId(groupId).processId(processId).templateId(templateId).tenantId(tenantId).status(ConsumerProcessApiStatus.FAILED.getStatusDescription()).stage(PROCESS_NAME).message(errorMessage).createdOn(entity.getCreatedOn()).lastUpdatedOn(CreateTimeStamp.currentTimestamp()).rootPipelineId(entity.getRootPipelineId()).batchId(batchId).build());
            HandymanException handymanException = new HandymanException(errorMessage);
            HandymanException.insertException(errorMessage, handymanException, this.action);
        }
    }


    private void tritonRequestBuilder(PaperItemizerInputTable entity, Request request, ObjectMapper objectMapper, List<PaperItemizerOutputTable> parentObj, String jsonInputRequest, URL endpoint) {
        String originId = entity.getOriginId();
        Integer groupId = entity.getGroupId();
        String templateId = entity.getTemplateId();
        Long tenantId = entity.getTenantId();
        Long processId = entity.getProcessId();


        try (Response response = httpclient.newCall(request).execute()) {

            if (response.isSuccessful()) {
                if (log.isInfoEnabled()) {
                    log.info(aMarker, "coproProcessor consumer process response successful for origin_id {}", entity.getOriginId());
                }
                String responseBody = Objects.requireNonNull(response.body()).string();
                PaperItemizerResponse paperItemizerResponse = objectMapper.readValue(responseBody, PaperItemizerResponse.class);
                if (paperItemizerResponse.getOutputs() != null && !paperItemizerResponse.getOutputs().isEmpty()) {
                    paperItemizerResponse.getOutputs().forEach(o -> o.getData().forEach(paperItemizerDataItem -> extractedOutputRequest(entity, objectMapper, parentObj, paperItemizerResponse.getModelName(), paperItemizerResponse.getModelVersion(), paperItemizerDataItem, jsonInputRequest, responseBody, endpoint.toString())));
                }

            } else {
                parentObj.add(PaperItemizerOutputTable.builder().originId(Optional.ofNullable(originId).map(String::valueOf).orElse(null)).groupId(groupId).processId(processId).templateId(templateId).tenantId(tenantId).status(ConsumerProcessApiStatus.FAILED.getStatusDescription()).stage(PROCESS_NAME).message(response.message()).createdOn(entity.getCreatedOn()).lastUpdatedOn(CreateTimeStamp.currentTimestamp()).rootPipelineId(entity.getRootPipelineId()).batchId(entity.getBatchId()).request(encryptRequestResponse(jsonInputRequest)).response(encryptRequestResponse(response.message())).endpoint(String.valueOf(endpoint)).build());
                HandymanException handymanException = new HandymanException("Unsuccessful response code : " + response.code() + " message : " + response.message());
                HandymanException.insertException("Paper itemizer consumer failed for batch/group " + entity.getGroupId() + " origin Id " + entity.getOriginId(), handymanException, this.action);

                log.error(aMarker, "Error in getting response from copro {}", response.message());
            }

        } catch (Exception exception) {
            parentObj.add(PaperItemizerOutputTable.builder().originId(Optional.ofNullable(originId).map(String::valueOf).orElse(null)).groupId(groupId).processId(processId).templateId(templateId).tenantId(tenantId).status(ConsumerProcessApiStatus.FAILED.getStatusDescription()).stage(PROCESS_NAME).message(exception.getMessage()).createdOn(entity.getCreatedOn()).lastUpdatedOn(CreateTimeStamp.currentTimestamp()).rootPipelineId(entity.getRootPipelineId()).batchId(entity.getBatchId()).request(encryptRequestResponse(jsonInputRequest)).response("Error In getting Response from copro").endpoint(String.valueOf(endpoint)).build());
            HandymanException handymanException = new HandymanException(exception);
            HandymanException.insertException("Paper Itemizer consumer failed for origin Id " + originId, handymanException, this.action);
            log.error(aMarker, "The Exception occurred in request {}", exception.getMessage(), exception);
        }
    }

    private void extractedOutputRequest(PaperItemizerInputTable entity, ObjectMapper objectMapper, List<PaperItemizerOutputTable> parentObj, String modelName, String modelVersion, String paperItemizerDataItem, String request, String response, String endpoint) {
        String originId = entity.getOriginId();
        Integer groupId = entity.getGroupId();
        String templateId = entity.getTemplateId();
        Long tenantId = entity.getTenantId();
        Long processId = entity.getProcessId();
        try {

            List<PaperItemizerDataItem> paperItemizeOutputDataList = objectMapper.readValue(paperItemizerDataItem, new TypeReference<>() {
            });

            for (PaperItemizerDataItem paperItemizeOutputData : paperItemizeOutputDataList) {
                String itemizedPapers = paperItemizeOutputData.getItemizedPapers();

                if (processBase64.equals(ProcessFileFormatE.BASE64.name())) {
                    fileProcessingUtils.convertBase64ToFile(paperItemizeOutputData.getBase64Img(), itemizedPapers);
                }

                parentObj.add(PaperItemizerOutputTable.builder().processedFilePath(itemizedPapers).originId(paperItemizeOutputData.getOriginId()).groupId(paperItemizeOutputData.getGroupId()).templateId(templateId).tenantId(paperItemizeOutputData.getTenantId()).processId(paperItemizeOutputData.getProcessId()).paperNo(paperItemizeOutputData.getPaperNumber()).status(ConsumerProcessApiStatus.COMPLETED.getStatusDescription()).stage(PROCESS_NAME).message("Paper Itemize macro completed").createdOn(entity.getCreatedOn()).lastUpdatedOn(CreateTimeStamp.currentTimestamp()).rootPipelineId(entity.getRootPipelineId()).modelName(modelName).modelVersion(modelVersion).batchId(paperItemizeOutputData.getBatchId()).request(encryptRequestResponse(request)).response(encryptRequestResponse(response)).endpoint(endpoint).build());
            }

        } catch (JsonProcessingException e) {
            parentObj.add(PaperItemizerOutputTable.builder().originId(Optional.ofNullable(originId).map(String::valueOf).orElse(null)).groupId(groupId).processId(processId).templateId(templateId).tenantId(tenantId).status(ConsumerProcessApiStatus.FAILED.getStatusDescription()).stage(PROCESS_NAME).message(e.getMessage()).createdOn(entity.getCreatedOn()).lastUpdatedOn(CreateTimeStamp.currentTimestamp()).rootPipelineId(entity.getRootPipelineId()).batchId(entity.getBatchId()).request(encryptRequestResponse(request)).response(encryptRequestResponse(response)).endpoint(endpoint).build());
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("Paper Itemize consumer failed for origin Id " + originId, handymanException, this.action);
            log.error(aMarker, "The Exception occurred in processing json into object {}", e.toString());
        } catch (IOException e) {
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("Paper Itemize failed for origin Id " + originId, handymanException, this.action);
            log.error(aMarker, "The Exception occurred in converting the base64 to file {}", e.toString());
        }
    }


    private void extractedCoproOutputResponse(PaperItemizerInputTable entity, ObjectMapper objectMapper, List<PaperItemizerOutputTable> parentObj, String modelName, String modelVersion, String paperItemizerDataItem, String request, String response, String endpoint) {
        String originId = entity.getOriginId();
        Integer groupId = entity.getGroupId();
        String templateId = entity.getTemplateId();
        Long tenantId = entity.getTenantId();
        Long processId = entity.getProcessId();
        try {


            PaperItemizerDataItemCopro paperItemizeOutputData = objectMapper.readValue(paperItemizerDataItem, PaperItemizerDataItemCopro.class);
            List<String> itemizedPapers = paperItemizeOutputData.getItemizedPapers();
            itemizedPapers.forEach(itemizedPaper -> {
                Long paperNo = getPaperNobyFileName(itemizedPaper);
                parentObj.add(PaperItemizerOutputTable.builder().processedFilePath(itemizedPaper).paperNo(paperNo).originId(Optional.ofNullable(originId).map(String::valueOf).orElse(null)).groupId(groupId).templateId(templateId).tenantId(tenantId).processId(processId).status(ConsumerProcessApiStatus.COMPLETED.getStatusDescription()).stage(PROCESS_NAME).message("Paper Itemizer macro completed").createdOn(entity.getCreatedOn()).lastUpdatedOn(CreateTimeStamp.currentTimestamp()).rootPipelineId(entity.getRootPipelineId()).modelName(modelName).modelVersion(modelVersion).batchId(entity.getBatchId()).request(encryptRequestResponse(request)).response(encryptRequestResponse(response)).endpoint(endpoint).build());
            });

        } catch (JsonProcessingException e) {
            parentObj.add(PaperItemizerOutputTable.builder().originId(Optional.ofNullable(originId).map(String::valueOf).orElse(null)).groupId(groupId).processId(processId).templateId(templateId).tenantId(tenantId).status(ConsumerProcessApiStatus.FAILED.getStatusDescription()).stage(PROCESS_NAME).message(e.getMessage()).createdOn(entity.getCreatedOn()).lastUpdatedOn(CreateTimeStamp.currentTimestamp()).rootPipelineId(entity.getRootPipelineId()).batchId(entity.getBatchId()).request(encryptRequestResponse(request)).response(encryptRequestResponse(response)).endpoint(endpoint).build());
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("Paper Itemize consumer failed for origin Id " + originId, handymanException, this.action);
            log.error(aMarker, "The Exception occurred in request {}", e.toString());
        }
    }

    public static Long getPaperNobyFileName(String filePath) {
        Long extractedNumber = null;
        File file = new File(filePath);

        String fileNameStr = FilenameUtils.removeExtension(file.getName());

        String[] parts = fileNameStr.split("_");

        // Check if there are at least two parts (0 and 1 after the first underscore)
        if (parts.length >= 1) {
            // Extract the second part (index 1 in the array after splitting)
            String number = parts[parts.length - 1];

            // Convert the extracted string to an integer if needed
            extractedNumber = Long.parseLong(number);

            // Print the extracted number
            return extractedNumber + 1;
        }

        return extractedNumber;
    }

    public String encryptRequestResponse(String request) {
        String encryptReqRes = action.getContext().get(ENCRYPT_REQUEST_RESPONSE);
        String requestStr;
        if ("true".equals(encryptReqRes)) {
            String encryptedRequest = SecurityEngine.getInticsIntegrityMethod(action).encrypt(request, "AES256", "COPRO_REQUEST");
            requestStr = encryptedRequest;
        } else {
            requestStr = request;
        }
        return requestStr;
    }

    private List<PaperItemizerOutputTable> paperItemizationCoproApi(PaperItemizerInputTable entity, ActionExecutionAudit action, URL endpoint, String outputDir) throws IOException {
        List<PaperItemizerOutputTable> parentObj = new ArrayList<>();
        String inputFilePath = entity.getFilePath();
        Long rootPipelineId = entity.getRootPipelineId();
        Long actionId = action.getActionId();
        String originId = entity.getOriginId();
        Integer groupId = entity.getGroupId();
        String batchId = entity.getBatchId();
        String processId = String.valueOf(entity.getProcessId());
        Long tenantId = entity.getTenantId();
        ObjectMapper objectMapper = new ObjectMapper();
//payload
        PaperItemizerData paperitemizerData = new PaperItemizerData();
        paperitemizerData.setOriginId(originId);
        paperitemizerData.setGroupId(groupId);
        paperitemizerData.setProcessId(Long.valueOf(processId));
        paperitemizerData.setTenantId(tenantId);
        paperitemizerData.setRootPipelineId(rootPipelineId);
        paperitemizerData.setProcess(PROCESS_NAME);
        paperitemizerData.setInputFilePath(inputFilePath);
        paperitemizerData.setOutputDir(outputDir);
        paperitemizerData.setActionId(actionId);
        paperitemizerData.setBatchId(batchId);

        String tritonRequestActivator = action.getContext().get(TRITON_REQUEST_ACTIVATOR);

        if ("false".equals(tritonRequestActivator)) {
            String jsonInputRequest = objectMapper.writeValueAsString(paperitemizerData);
            Request request = new Request.Builder().url(endpoint).post(RequestBody.create(jsonInputRequest, mediaTypeJson)).build();
            coproRequestBuilder(entity, request, objectMapper, parentObj, jsonInputRequest, endpoint);
        } else {
            if (processBase64.equals(ProcessFileFormatE.BASE64.name())) {
                paperitemizerData.setBase64Img(fileProcessingUtils.convertFileToBase64(inputFilePath));

            }
            String jsonInputRequest = objectMapper.writeValueAsString(paperitemizerData);

            TritonRequest requestBody = new TritonRequest();
            requestBody.setName(PAPER_ITERATOR_START);
            requestBody.setShape(List.of(1, 1));
            requestBody.setDatatype(TritonDataTypes.BYTES.name());
            requestBody.setData(Collections.singletonList(jsonInputRequest));

            TritonInputRequest tritonInputRequest = new TritonInputRequest();
            tritonInputRequest.setInputs(Collections.singletonList(requestBody));


            String jsonRequest = objectMapper.writeValueAsString(tritonInputRequest);

            Request request = new Request.Builder().url(endpoint).post(RequestBody.create(jsonRequest, mediaTypeJson)).build();
            tritonRequestBuilder(entity, request, objectMapper, parentObj, jsonRequest, endpoint);
        }
        return parentObj;
    }


}
