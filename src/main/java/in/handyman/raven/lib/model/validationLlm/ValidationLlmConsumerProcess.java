package in.handyman.raven.lib.model.validationLlm;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;

import in.handyman.raven.lib.CoproProcessor;
import in.handyman.raven.lib.ValidationLlmAction;


import in.handyman.raven.lib.model.triton.ConsumerProcessApiStatus;
import in.handyman.raven.lib.model.triton.PipelineName;
import in.handyman.raven.lib.model.triton.TritonInputRequest;
import in.handyman.raven.lib.model.triton.TritonRequest;
import in.handyman.raven.util.ExceptionUtil;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.net.URL;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValidationLlmConsumerProcess implements CoproProcessor.ConsumerProcess<ValidationLlmInputTable, ValidationLlmOutputTable>{
    public static final String TRITON_REQUEST_ACTIVATOR = "triton.request.validation.llm.activator";
    public static final String PROCESS_NAME = PipelineName.VALIDATION_LLM.getProcessName();
    private final Logger log;
    private final Marker aMarker;
    private final ObjectMapper mapper = new ObjectMapper();
    private static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");

    public final ActionExecutionAudit action;
    private final OkHttpClient httpclient;
    private final ValidationLlmAction aAction;

    private final int timeOut;

    public ValidationLlmConsumerProcess(final Logger log, final Marker aMarker, ActionExecutionAudit action, ValidationLlmAction aAction) {
        this.log = log;
        this.aMarker = aMarker;
        this.action = action;
        this.aAction = aAction;
        this.timeOut = aAction.getTimeOut();
        this.httpclient = new OkHttpClient.Builder().connectTimeout(this.timeOut, TimeUnit.MINUTES).writeTimeout(this.timeOut, TimeUnit.MINUTES).readTimeout(this.timeOut, TimeUnit.MINUTES).build();
    }

    public List<ValidationLlmOutputTable> process(URL endpoint, ValidationLlmInputTable entity) throws Exception {
        List<ValidationLlmOutputTable> parentObj = new ArrayList<>();
        String input = entity.getInput();
        String rootPipelineId = String.valueOf(entity.getRootPipelineId());
        Long actionId = entity.getActionId();
        String task = entity.getTask();
        String text = entity.getText();

        ValidationLlmExtractionRequest validationLlmExtractionRequest = new ValidationLlmExtractionRequest();


        validationLlmExtractionRequest.setRootPipelineId(Long.valueOf(rootPipelineId));
        validationLlmExtractionRequest.setActionId(actionId);
        validationLlmExtractionRequest.setProcess(PROCESS_NAME);
        validationLlmExtractionRequest.setInput(input);
        validationLlmExtractionRequest.setTask(task);
        validationLlmExtractionRequest.setOutputDir(entity.getOutputDir());
        validationLlmExtractionRequest.setText(text);

        List<ValidationExtractionLineItems> validationExtractionLineItems = new ArrayList<>();
        ValidationExtractionLineItems validationExtractionLineItems1 = new ValidationExtractionLineItems();
        validationExtractionLineItems1.setSectionHeader(entity.getSectionHeader());
        validationExtractionLineItems1.setPrompt(entity.getPrompt());

        validationExtractionLineItems.add(validationExtractionLineItems1);

        validationLlmExtractionRequest.setPrompt(validationExtractionLineItems);

        String jsonInputRequest = mapper.writeValueAsString(validationLlmExtractionRequest);


        TritonRequest requestBody = new TritonRequest();
        requestBody.setName("VALIDATION LLM START");
        requestBody.setShape(List.of(1, 1));
        requestBody.setDatatype("BYTES");
        requestBody.setData(Collections.singletonList(jsonInputRequest));


        TritonInputRequest tritonInputRequest = new TritonInputRequest();
        tritonInputRequest.setInputs(Collections.singletonList(requestBody));

        String jsonRequest = mapper.writeValueAsString(tritonInputRequest);


        log.info(aMarker, " Input variables id : {}", action.getActionId());


        if (log.isInfoEnabled()) {
            log.info(aMarker, "Request has been build with the parameters \n URI : {} , prompt {}", endpoint, jsonInputRequest);
        }
        String tritonRequestActivator = action.getContext().get(TRITON_REQUEST_ACTIVATOR);


        if (Objects.equals("false", tritonRequestActivator)) {
            log.info("Triton request activator variable: {} value: {}, Copro API running in legacy mode and json input {}", TRITON_REQUEST_ACTIVATOR, tritonRequestActivator, jsonInputRequest);
            Request request = new Request.Builder().url(endpoint).post(RequestBody.create(jsonInputRequest, MEDIA_TYPE_JSON)).build();
            coproResponseBuilder(entity, request, parentObj);
        } else {
            log.info("Triton request activator variable: {} value: {}, Copro API running in Triton mode  and json input {} ", TRITON_REQUEST_ACTIVATOR, tritonRequestActivator, jsonRequest);
            Request request = new Request.Builder().url(endpoint).post(RequestBody.create(jsonRequest, MEDIA_TYPE_JSON)).build();
            tritonRequestBuilder(entity, request, parentObj);
        }


        return parentObj;
    }

    private void tritonRequestBuilder(ValidationLlmInputTable entity, Request request, List<ValidationLlmOutputTable> parentObj) {
        Integer groupId = entity.getGroupId();
        Integer processId = entity.getProcessId();
        Long tenantId = entity.getTenantId();
        Integer paperNo = entity.getPaperNo();
        Long rootPipelineId = entity.getRootPipelineId();
        try (Response response = httpclient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                ValidationExtractionResponse modelResponse = mapper.readValue(responseBody, ValidationExtractionResponse.class);

                if (modelResponse.getSectionPoints() != null && !modelResponse.getSectionPoints().isEmpty()) {
                        extractTritonOutputDataResponse(entity, modelResponse.getSectionPoints(), parentObj);
                    }

            } else {
                parentObj.add(ValidationLlmOutputTable.builder()
                        .originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null))
                        .groupId(groupId)
                        .processId(processId)
                        .tenantId(tenantId)
                        .paperNo(paperNo)
                        .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                        .stage(PROCESS_NAME)
                        .message(response.message())
                        .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                        .rootPipelineId(rootPipelineId)
                        .build());
                log.info(aMarker, "Error in getting response {}", response.message());
            }
        } catch (Exception e) {
            parentObj.add(ValidationLlmOutputTable.builder()
                    .originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null))
                    .groupId(groupId)
                    .processId(processId)
                    .tenantId(tenantId)
                    .paperNo(paperNo)
                    .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                    .stage(PROCESS_NAME)
                    .message(ExceptionUtil.toString(e))
                    .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                    .rootPipelineId(rootPipelineId)
                    .build());
            log.error(aMarker, "The Exception occurred in getting response {}", ExceptionUtil.toString(e));
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("paragraph detection consumer failed for batch/group " + groupId, handymanException, this.action);
            log.error(aMarker, "The Exception occurred in getting response {}", ExceptionUtil.toString(e));
        }
    }


    private void coproResponseBuilder(ValidationLlmInputTable entity, Request request, List<ValidationLlmOutputTable> parentObj) {
        Long rootPipelineId = entity.getRootPipelineId();
        Long actionId = entity.getActionId();
        String process = entity.getProcess();
        String input = entity.getInput();
        String outputDir = entity.getOutputDir();
        String task = entity.getTask();
        String prompt = entity.getPrompt();
        Integer paperNo = entity.getPaperNo();
        Integer groupId = entity.getGroupId();
        Integer processId = entity.getProcessId();
        Integer sorItemId = entity.getSorItemId();
        String sorItemName = entity.getSorItemName();
        String sorQuestion  = entity.getSorQuestion();
        Integer questionId = entity.getQuestionId();
        Integer synonymId = entity.getSynonymId();
        String answer =  entity.getAnswer();
        Float vqaScore = entity.getVqaScore();

        Long tenantId = entity.getTenantId();


        try (Response response = httpclient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                extractedCoproOutputResponse(entity, responseBody, parentObj);
            } else {
                parentObj.add(ValidationLlmOutputTable.builder()
                        .rootPipelineId(rootPipelineId)
                        .originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null))
                        .paperNo(paperNo)
                        .groupId(groupId)
                        .processId(processId)
                        .sorItemId(sorItemId)
                        .sorItemName(sorItemName)
                        .sorQuestion(sorQuestion)
                        .questionId(questionId)
                        .synonymId(synonymId)
                        .answer(answer)
                        .vqaScore(vqaScore)
                        .tenantId(tenantId)
                        .answer(answer)
                        .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                        .stage(PROCESS_NAME)
                        .message(response.message())
                        .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                        .build());
                log.info(aMarker, "Error in getting response {}", response.message());
            }
        } catch (Exception e) {
            parentObj.add(ValidationLlmOutputTable.builder()
                    .originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null))
                    .groupId(groupId)
                    .processId(processId)
                    .tenantId(tenantId)
                    .paperNo(paperNo)
                    .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                    .stage(PROCESS_NAME)
                    .message(ExceptionUtil.toString(e))
                    .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                    .rootPipelineId(rootPipelineId)
                    .build());
            log.error(aMarker, "The Exception occurred in getting response {}", ExceptionUtil.toString(e));
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("Validation llm consumer failed for batch/group " + groupId, handymanException, this.action);
            log.error(aMarker, "The Exception occurred in getting response {}", ExceptionUtil.toString(e));
        }
    }

    private void extractedCoproOutputResponse(ValidationLlmInputTable entity, String responseBody, List<ValidationLlmOutputTable> parentObj) {
        Integer groupId = entity.getGroupId();
        Integer processId = entity.getProcessId();

        Long tenantId = entity.getTenantId();
        Integer paperNo = entity.getPaperNo();
        Long rootPipelineId = entity.getRootPipelineId();
        String originId = entity.getOriginId();
       try {
            List<ValidationExtractionResponse> detectionDataItem = mapper.readValue(responseBody, new TypeReference<>() {
            });
            detectionDataItem.forEach(validationExtractionResponse -> {
                try {
                    String cleanedResponseBody = cleanJsonBlock(validationExtractionResponse.getSectionPoints());
                    ValidationLineItem customObjects = mapper.readValue(cleanedResponseBody, new TypeReference<>() {
                    });
//                    if (customObjects.getValidatedOutput().contains("```json")) {
//                        Pattern pattern = Pattern.compile("\\{(?:[^{}]|(?R))*\\}");
//                        Matcher matcher = pattern.matcher(customObjects.getValidatedOutput());
//                        if (matcher.find()) {
//                            String jsonPart = matcher.group();

                    parentObj.add(ValidationLlmOutputTable.builder()
                            .originId(entity.getOriginId())
                            .paperNo(entity.getPaperNo())
                            .groupId(groupId)
                            .processId(processId)
                            .sorItemId(entity.getSorItemId())
                            .sorItemName(entity.getSorItemName())
                            .sorQuestion(entity.getSorQuestion())
                            .answer(entity.getAnswer())
                            .validatedAnswer(customObjects.getValidatedOutput())
                            .vqaScore(entity.getVqaScore())
                            .weight(entity.getWeight())
                            .createdUserId(entity.getCreatedUserId())
                            .tenantId(entity.getTenantId())
                            .createdOn(entity.getCreatedOn())
                            .confidenceScore(customObjects.getConfidenceScore())
                            .validationName(entity.getValidationName())
                            .bBox(entity.getBBox())
                            .status(ConsumerProcessApiStatus.COMPLETED.getStatusDescription())
                            .stage("VALIDATION LLM")
                            .message("Validation llm macro completed")
                            .rootPipelineId(entity.getRootPipelineId())
                            .questionId(entity.getQuestionId())
                            .synonymId(entity.getSynonymId())
                            .modelRegistry(entity.getModelRegistry())
                            .build());

                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }

            });


        } catch (JsonProcessingException e) {
            parentObj.add(ValidationLlmOutputTable.builder()
                    .originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null))
                    .paperNo(entity.getPaperNo())
                    .groupId(groupId)
                    .processId(processId)
                    .sorItemId(entity.getSorItemId())
                    .sorItemName(entity.getSorItemName())
                    .sorQuestion(entity.getSorQuestion())
                    .answer(entity.getAnswer())
                    .vqaScore(entity.getVqaScore())
                    .weight(entity.getWeight())
                    .createdUserId(entity.getCreatedUserId())
                    .tenantId(entity.getTenantId())
                    .createdOn(entity.getCreatedOn())
                    .validationName(entity.getValidationName())
                    .bBox(entity.getBBox())
                    .groupId(groupId)
                    .processId(processId)
                    .tenantId(tenantId)
                    .paperNo(paperNo)
                    .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                    .stage(PROCESS_NAME)
                    .message(ExceptionUtil.toString(e))
                    .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                    .rootPipelineId(rootPipelineId)
                    .build());
            log.error(aMarker, "The Exception occurred in processing response {}", ExceptionUtil.toString(e));
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("paragraph Extraction consumer failed for batch/group " + groupId, handymanException, this.action);
        }
    }

    private String cleanJsonBlock(String responseBody) {
        if (responseBody.contains("```json")) {
            // Define the regex pattern to match the JSON object inside the ```json block
            String regex = "\\{[^{}]*\\}";

            // Compile the pattern
            Pattern pattern = Pattern.compile(regex);

            // Create a matcher for the input string
            Matcher matcher = pattern.matcher(responseBody);

            // Find and return the JSON object
            if (matcher.find()) {
                return matcher.group();
            }
        }
        return responseBody;
    }

    private void extractTritonOutputDataResponse(ValidationLlmInputTable entity, String validationLlmDataItem, List<ValidationLlmOutputTable> parentObj) {
        Integer groupId = entity.getGroupId();
        Integer processId = entity.getProcessId();

        Long tenantId = entity.getTenantId();
        Integer paperNo = entity.getPaperNo();
        Long rootPipelineId = entity.getRootPipelineId();
        String originId = entity.getOriginId();
        try {

            List<ValidationExtractionResponse> detectionDataItem = mapper.readValue(validationLlmDataItem, new TypeReference<>() {
            });
            detectionDataItem.forEach(validationExtractionResponse -> {
                try {
                    ValidationLineItem customObjects = mapper.readValue(validationExtractionResponse.getSectionPoints(), new TypeReference<>() {
                    });
                    parentObj.add(ValidationLlmOutputTable.builder()
                            .synonymId(entity.getSynonymId())
                            .originId(originId)
                            .groupId(groupId)
                            .processId(processId)
                            .tenantId(tenantId)
                            .paperNo(paperNo)
                            .status(ConsumerProcessApiStatus.COMPLETED.getStatusDescription())
                            .stage(PROCESS_NAME)
                            .message("bulletin Extraction macro completed")
                            .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                                    .validatedAnswer(customObjects.getValidatedOutput())
                                    .confidenceScore(customObjects.getConfidenceScore())
                            .rootPipelineId(rootPipelineId)
                            .processId(entity.getProcessId())
                            .build());


                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }

            });

        } catch (JsonProcessingException e) {
            parentObj.add(ValidationLlmOutputTable.builder()
                    .originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null))
                    .groupId(groupId)
                    .processId(processId)
                    .tenantId(tenantId)
                    .paperNo(paperNo)
                    .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                    .stage(PROCESS_NAME)
                    .message(ExceptionUtil.toString(e))
                    .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                    .rootPipelineId(rootPipelineId)
                    .build());
            log.error(aMarker, "The Exception occurred in processing response {}", ExceptionUtil.toString(e));
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("validation llm consumer failed for batch/group " + groupId, handymanException, this.action);
        }
    }


}
