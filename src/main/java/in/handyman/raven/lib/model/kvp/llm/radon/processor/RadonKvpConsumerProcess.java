package in.handyman.raven.lib.model.kvp.llm.radon.processor;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.CoproProcessor;
import in.handyman.raven.lib.RadonKvpAction;
import in.handyman.raven.lib.model.common.CreateTimeStamp;
import in.handyman.raven.lib.model.triton.*;
import in.handyman.raven.util.ExceptionUtil;

import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RadonKvpConsumerProcess implements CoproProcessor.ConsumerProcess<RadonQueryInputTable, RadonQueryOutputTable> {

    public static final String TRITON_REQUEST_ACTIVATOR = "triton.request.radon.kvp.activator";
    public static final String PROCESS_NAME = PipelineName.RADON_KVP_ACTION.getProcessName();
    public static final String RADON_START = "RADON START";
    private final Logger log;
    private final Marker aMarker;
    private final ObjectMapper mapper = new ObjectMapper();
    private static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");

    public final ActionExecutionAudit action;
    private final OkHttpClient httpclient;

    public RadonKvpConsumerProcess(final Logger log, final Marker aMarker, ActionExecutionAudit action, RadonKvpAction aAction) {
        this.log = log;
        this.aMarker = aMarker;
        this.action = action;
        int timeOut = aAction.getTimeOut();
        this.httpclient = new OkHttpClient.Builder().connectTimeout(timeOut, TimeUnit.MINUTES).writeTimeout(timeOut, TimeUnit.MINUTES).readTimeout(timeOut, TimeUnit.MINUTES).build();
    }


    @Override
    public List<RadonQueryOutputTable> process(URL endpoint, RadonQueryInputTable entity) throws Exception {
        List<RadonQueryOutputTable> parentObj = new ArrayList<>();
        String rootPipelineId = String.valueOf(entity.getRootPipelineId());
        String filePath = String.valueOf(entity.getInputFilePath());
        Long actionId = action.getActionId();
        Long groupId = entity.getGroupId();
        String prompt = entity.getPrompt();
        String modelRegistry = entity.getModelRegistry();
        Integer paperNo = entity.getPaperNo();
        String originId = entity.getOriginId();
        Long processId = entity.getProcessId();
        Long tenantId = entity.getTenantId();

        //payload
        RadonKvpExtractionRequest radonKvpExtractionRequest = new RadonKvpExtractionRequest();

        radonKvpExtractionRequest.setRootPipelineId(Long.valueOf(rootPipelineId));
        radonKvpExtractionRequest.setActionId(actionId);
        radonKvpExtractionRequest.setProcess(PROCESS_NAME);
        radonKvpExtractionRequest.setInputFilePath(filePath);
        radonKvpExtractionRequest.setGroupId(groupId);
        radonKvpExtractionRequest.setPrompt(prompt);
        radonKvpExtractionRequest.setProcessId(processId);
        radonKvpExtractionRequest.setPaperNo(paperNo);
        radonKvpExtractionRequest.setTenantId(tenantId);
        radonKvpExtractionRequest.setOriginId(originId);
        radonKvpExtractionRequest.setBatchId(entity.getBatchId());


        String jsonInputRequest = mapper.writeValueAsString(radonKvpExtractionRequest);


        TritonRequest requestBody = new TritonRequest();
        requestBody.setName(entity.getApiName());
        requestBody.setShape(List.of(1, 1));
        requestBody.setDatatype(TritonDataTypes.BYTES.name());
        requestBody.setData(Collections.singletonList(jsonInputRequest));


        TritonInputRequest tritonInputRequest = new TritonInputRequest();
        tritonInputRequest.setInputs(Collections.singletonList(requestBody));

        String jsonRequest = mapper.writeValueAsString(tritonInputRequest);


        log.info(aMarker, " Input variables id : {}", action.getActionId());


        if (log.isInfoEnabled()) {
            log.info(aMarker, "Request has been build with the parameters \n URI : {}, with inputFilePath {} and prompt {}", endpoint, filePath, prompt);
        }
        String tritonRequestActivator = action.getContext().get(TRITON_REQUEST_ACTIVATOR);


        if (Objects.equals("false", tritonRequestActivator)) {
            log.info("Triton request activator variable: {} value: {}, Copro API running in legacy mode and json input {}", TRITON_REQUEST_ACTIVATOR, tritonRequestActivator, jsonInputRequest);
            Request request = new Request.Builder().url(endpoint).post(RequestBody.create(jsonInputRequest, MEDIA_TYPE_JSON)).build();
            coproResponseBuider(entity, request, parentObj, jsonInputRequest, endpoint);
        } else {
            log.info("Triton request activator variable: {} value: {}, Copro API running in Triton mode  and json input {} ", TRITON_REQUEST_ACTIVATOR, tritonRequestActivator, jsonRequest);
            Request request = new Request.Builder().url(endpoint).post(RequestBody.create(jsonRequest, MEDIA_TYPE_JSON)).build();
            tritonRequestBuilder(entity, request, parentObj, jsonRequest, endpoint);
        }


        return parentObj;
    }

    private void tritonRequestBuilder(RadonQueryInputTable entity, Request request, List<RadonQueryOutputTable> parentObj, String jsonRequest, URL endpoint) {
        Long groupId = entity.getGroupId();
        Long processId = entity.getProcessId();
        Long tenantId = entity.getTenantId();
        Integer paperNo = entity.getPaperNo();
        Long rootPipelineId = entity.getRootPipelineId();


        try (Response response = httpclient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                assert response.body() != null;
                String responseBody = "{\"model_name\":\"krypton-service\",\"model_version\":\"1\",\"outputs\":[{\"name\":\"KRYPTON END\",\"datatype\":\"BYTES\",\"shape\":[1],\"data\":[\"{\\\"model\\\": \\\"RADON_KVP_ACTION\\\", \\\"infer_response\\\": \\\"```json\\\\n{\\\\n  \\\\\\\"Member Information\\\\\\\": {\\\\n    \\\\\\\"First name\\\\\\\": \\\\\\\"Cynthia\\\\\\\",\\\\n    \\\\\\\"Last name\\\\\\\": \\\\\\\"Beard\\\\\\\",\\\\n    \\\\\\\"Amerigroup member ID\\\\\\\": \\\\\\\"814199\\\\\\\",\\\\n    \\\\\\\"DOB\\\\\\\": \\\\\\\"1/19/1948\\\\\\\",\\\\n    \\\\\\\"Contact Phone\\\\\\\": \\\\\\\"+49 69 3896882\\\\\\\",\\\\n    \\\\\\\"Additional member information\\\\\\\": {\\\\n      \\\\\\\"Referring Provider\\\\\\\": \\\\\\\"Participating\\\\\\\",\\\\n      \\\\\\\"Nonparticipating\\\\\\\": null\\\\n    }\\\\n  },\\\\n  \\\\\\\"Provider Information\\\\\\\": {\\\\n    \\\\\\\"Full name\\\\\\\": \\\\\\\"KATHRYN CZERNIAK\\\\\\\",\\\\n    \\\\\\\"NPI\\\\\\\": \\\\\\\"1215605456\\\\\\\",\\\\n    \\\\\\\"Provider ID\\\\\\\": \\\\\\\"1962000597\\\\\\\",\\\\n    \\\\\\\"Tax ID number (TIN)\\\\\\\": \\\\\\\"264440290\\\\\\\",\\\\n    \\\\\\\"Office contact name\\\\\\\": \\\\\\\"CHRISTINA GAMBOA\\\\\\\",\\\\n    \\\\\\\"Office phone\\\\\\\": \\\\\\\"919373219\\\\\\\",\\\\n    \\\\\\\"Office fax\\\\\\\": \\\\\\\"4105454878\\\\\\\",\\\\n    \\\\\\\"Address\\\\\\\": \\\\\\\"125 PASSAIC AVE APT 125-103\\\\\\\",\\\\n    \\\\\\\"City\\\\\\\": \\\\\\\"KEARNY\\\\\\\",\\\\n    \\\\\\\"State\\\\\\\": \\\\\\\"NJ\\\\\\\",\\\\n    \\\\\\\"ZIP code\\\\\\\": \\\\\\\"070321137\\\\\\\",\\\\n    \\\\\\\"Specialty\\\\\\\": \\\\\\\"Emergency Medical Technician (EMT)\\\\\\\",\\\\n    \\\\\\\"Referring Provider\\\\\\\": \\\\\\\"Participating\\\\\\\",\\\\n    \\\\\\\"Nonparticipating\\\\\\\": null\\\\n  },\\\\n  \\\\\\\"Provider Information\\\\\\\": {\\\\n    \\\\\\\"Full name\\\\\\\": \\\\\\\"HANNAH ELIZABETH BODENHORN\\\\\\\",\\\\n    \\\\\\\"NPI\\\\\\\": \\\\\\\"1992090328\\\\\\\",\\\\n    \\\\\\\"Provider ID\\\\\\\": \\\\\\\"656403\\\\\\\",\\\\n    \\\\\\\"TIN\\\\\\\": \\\\\\\"262487508\\\\\\\",\\\\n    \\\\\\\"Office contact name\\\\\\\": \\\\\\\"HANNAH\\\\\\\",\\\\n    \\\\\\\"Office phone\\\\\\\": \\\\\\\"(471) 459-3433\\\\\\\",\\\\n    \\\\\\\"Office fax\\\\\\\": \\\\\\\"(879) 725-2646\\\\\\\",\\\\n    \\\\\\\"Address\\\\\\\": \\\\\\\"7622 OAKLAND RD STE 104\\\\\\\",\\\\n    \\\\\\\"City\\\\\\\": \\\\\\\"SANTA ANA\\\\\\\",\\\\n    \\\\\\\"State\\\\\\\": \\\\\\\"CA\\\\\\\",\\\\n    \\\\\\\"ZIP code\\\\\\\": \\\\\\\"927057875\\\\\\\",\\\\n    \\\\\\\"Specialty\\\\\\\": \\\\\\\"Dermatology\\\\\\\",\\\\n    \\\\\\\"Referring Provider\\\\\\\": \\\\\\\"Participating\\\\\\\",\\\\n    \\\\\\\"Nonparticipating\\\\\\\": null\\\\n  },\\\\n  \\\\\\\"Provider Information\\\\\\\": {\\\\n    \\\\\\\"Name\\\\\\\": \\\\\\\"PINNACLE PEAK ORAL SURGERY, PLLC\\\\\\\",\\\\n    \\\\\\\"NPI\\\\\\\": \\\\\\\"1063993295\\\\\\\",\\\\n    \\\\\\\"Provider ID\\\\\\\": \\\\\\\"1245993849\\\\\\\",\\\\n    \\\\\\\"TIN\\\\\\\": \\\\\\\"280596460\\\\\\\",\\\\n    \\\\\\\"Facility contact name\\\\\\\": \\\\\\\"Facility phone: (732) 305-0280\\\\\\\",\\\\n    \\\\\\\"Facility fax\\\\\\\": \\\\\\\"(907) 520-1621\\\\\\\",\\\\n    \\\\\\\"Address\\\\\\\": \\\\\\\"PO BOX 198054\\\\\\\",\\\\n    \\\\\\\"City\\\\\\\": \\\\\\\"ATLANTA\\\\\\\",\\\\n    \\\\\\\"State\\\\\\\": \\\\\\\"GA\\\\\\\",\\\\n    \\\\\\\"ZIP code\\\\\\\": \\\\\\\"303648054\\\\\\\",\\\\n    \\\\\\\"Specialty\\\\\\\": \\\\\\\"Dermatology\\\\\\\",\\\\n    \\\\\\\"Referring Provider\\\\\\\": \\\\\\\"Participating\\\\\\\",\\\\n    \\\\\\\"Nonparticipating\\\\\\\": null\\\\n  },\\\\n  \\\\\\\"Additional Information\\\\\\\": {\\\\n    \\\\\\\"Requested Service\\\\\\\": \\\\\\\"Date/Date range of service: 10/25/2022\\\\\\\",\\\\n    \\\\\\\"ICD-10 code(s)\\\\\\\": \\\\\\\"S64.1\\\\\\\",\\\\n    \\\\\\\"CPT code(s): (include requested units) 9509\\\\\\\",\\\\n    \\\\\\\"Type of service\\\\\\\": \\\\\\\"Outpatient\\\\\\\",\\\\n    \\\\\\\"Place of service\\\\\\\": \\\\\\\"Hospital\\\\\\\",\\\\n    \\\\\\\"Additional Information\\\\\\\": \\\\\\\"Please submit all appropriate clinical information, provider contact information and any other required documents with this form to support your request. If this is a request for extension or modification of an existing authorization from Amerigroup, Please provide authorization number with your submission.\\\\\\\",\\\\n    \\\\\\\"Emergency-use for ALL non elective INPATIENT admissions only, when provider indicates that the admission was urgent, emergent or expedited(for admission on same day)\\\\\\\",\\\\n    \\\\\\\"Urgent-use for OUTPATIENT services only, when provider indicates that the service is urgent, emergent or expedited\\\\\\\",\\\\n    \\\\\\\"Disclaimer\\\\\\\": \\\\\\\"Authorization is based on verification of member eligibility and benefit coverage at the time of service and is subject to Amerigroup Community care claims payment policy and procedures.\\\\\\\"\\\\n  }\\\\n}\\\\n```\\\", \\\"confidence_score\\\": \\\"\\\", \\\"bboxes\\\": \\\"\\\", \\\"originId\\\": \\\"ORIGIN-467\\\", \\\"paperNo\\\": 1, \\\"processId\\\": 108679, \\\"groupId\\\": 423, \\\"tenantId\\\": 115, \\\"rootPipelineId\\\": 108679, \\\"actionId\\\": 1039072}\"]}]}";
                RadonKvpExtractionResponse modelResponse = mapper.readValue(responseBody, RadonKvpExtractionResponse.class);
                if (modelResponse.getOutputs() != null && !modelResponse.getOutputs().isEmpty()) {
                    modelResponse.getOutputs().forEach(o -> o.getData().forEach(radonDataItem -> {
                        try {
                            extractTritonOutputDataResponse(entity, radonDataItem, parentObj, jsonRequest, responseBody, endpoint.toString());
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    }));

                }
            } else {
                parentObj.add(RadonQueryOutputTable.builder()
                        .originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null))
                        .paperNo(paperNo)
                        .groupId(groupId)
                        .inputFilePath(entity.getInputFilePath())
                        .tenantId(tenantId)
                        .actionId(action.getActionId())
                        .processId(processId)
                        .rootPipelineId(rootPipelineId)
                        .process(entity.getProcess())
                        .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                        .stage(entity.getApiName())
                        .message(response.message())
                        .batchId(entity.getBatchId())
                        .createdOn(entity.getCreatedOn())
                        .createdUserId(tenantId)
                        .lastUpdatedOn(CreateTimeStamp.currentTimestamp())
                        .lastUpdatedUserId(tenantId)
                        .category(entity.getCategory())
                        .request(jsonRequest)
                        .response(response.message())
                        .endpoint(String.valueOf(endpoint))
                        .build());
                log.info(aMarker, "Error in getting response from triton response {}", response.message());
            }
        } catch (IOException e) {
            parentObj.add(RadonQueryOutputTable.builder()
                    .originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null))
                    .paperNo(paperNo)
                    .groupId(groupId)
                    .inputFilePath(entity.getInputFilePath())
                    .tenantId(tenantId)
                    .processId(processId)
                    .rootPipelineId(rootPipelineId)
                    .actionId(action.getActionId())
                    .process(entity.getProcess())
                    .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                    .stage(entity.getApiName())
                    .message(ExceptionUtil.toString(e))
                    .batchId(entity.getBatchId())
                    .createdOn(entity.getCreatedOn())
                    .createdUserId(tenantId)
                    .lastUpdatedOn(CreateTimeStamp.currentTimestamp())
                    .lastUpdatedUserId(tenantId)
                    .category(entity.getCategory())
                    .build());

            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("radon kvp consumer failed for batch/group " + groupId, handymanException, this.action);
            log.error(aMarker, "The Exception occurred in getting response  from triton server {}", ExceptionUtil.toString(e));
        }
    }

    public JsonNode convertFormattedJsonStringToJsonNode(String jsonResponse, ObjectMapper objectMapper) {
        try {
            if (jsonResponse.contains("```json")) {
                // Define the regex pattern to match content between ```json and ```
                Pattern pattern = Pattern.compile("(?s)```json\\s*(.*?)\\s*```");
                Matcher matcher = pattern.matcher(jsonResponse);

                if (matcher.find()) {
                    // Extract the JSON string from the matched group
                    String jsonString = matcher.group(1);
                    jsonString = jsonString.replace("\n", "");


                    // Convert the cleaned JSON string to a JsonNode
                    JsonNode rootNode = objectMapper.readTree(jsonString);

                    return rootNode;
                }else {
                    JsonNode rootNode = objectMapper.readTree(jsonResponse);
                    return rootNode;
                }

            } else {
                // Handle the case where the expected markers are not found
                throw new IllegalArgumentException("Input does not contain the required ```json``` markers.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void extractTritonOutputDataResponse(RadonQueryInputTable entity, String radonDataItem, List<RadonQueryOutputTable> parentObj, String request, String response, String endpoint) throws JsonProcessingException {
        Long groupId = entity.getGroupId();
        Long processId = entity.getProcessId();

        Long tenantId = entity.getTenantId();
        Integer paperNo = entity.getPaperNo();
        Long rootPipelineId = entity.getRootPipelineId();
        String processedFilePaths = entity.getInputFilePath();
        String originId = entity.getOriginId();
        RadonKvpLineItem modelResponse = mapper.readValue(radonDataItem, RadonKvpLineItem.class);
        JsonNode stringObjectMap=convertFormattedJsonStringToJsonNode(modelResponse.getInferResponse(), objectMapper);


        parentObj.add(RadonQueryOutputTable.builder()
                .createdOn(CreateTimeStamp.currentTimestamp())
                .createdUserId(tenantId)
                .lastUpdatedOn(CreateTimeStamp.currentTimestamp())
                .lastUpdatedUserId(tenantId)
                .originId(originId)
                .paperNo(paperNo)
                .totalResponseJson(mapper.writeValueAsString(stringObjectMap))
                .groupId(groupId)
                .inputFilePath(processedFilePaths)
                .actionId(action.getActionId())
                .tenantId(tenantId)
                .processId(processId)
                .rootPipelineId(rootPipelineId)
                .process(entity.getProcess())
                .batchId(modelResponse.getBatchId())
                .modelRegistry(entity.getModelRegistry())
                .status(ConsumerProcessApiStatus.COMPLETED.getStatusDescription())
                .stage(entity.getApiName())
                .batchId(entity.getBatchId())
                .category(entity.getCategory())
                .message("Radon kvp action macro completed")
                .request(request)
                .response(response)
                .endpoint(String.valueOf(endpoint))
                .build()
        );


    }

    private void coproResponseBuider(RadonQueryInputTable entity, Request request, List<RadonQueryOutputTable> parentObj, String jsonInputRequest, URL endpoint) {
        Long groupId = entity.getGroupId();
        Long processId = entity.getProcessId();
        Long tenantId = entity.getTenantId();
        Integer paperNo = entity.getPaperNo();
        Long rootPipelineId = entity.getRootPipelineId();

        try (Response response = httpclient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                RadonKvpExtractionResponse modelResponse = mapper.readValue(responseBody, RadonKvpExtractionResponse.class);
                if (modelResponse.getOutputs() != null && !modelResponse.getOutputs().isEmpty()) {
                    modelResponse.getOutputs().forEach(o -> o.getData().forEach(radonDataItem -> {
                        try {
                            extractedCoproOutputResponse(entity, radonDataItem, parentObj, jsonInputRequest, responseBody, endpoint.toString());
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    }));

                }
            } else {
                parentObj.add(RadonQueryOutputTable.builder()
                        .originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null))
                        .paperNo(paperNo)
                        .groupId(groupId)
                        .inputFilePath(entity.getInputFilePath())
                        .actionId(action.getActionId())
                        .tenantId(tenantId)
                        .processId(processId)
                        .rootPipelineId(rootPipelineId)
                        .process(entity.getProcess())
                        .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                        .stage(entity.getApiName())
                        .message(response.message())
                        .batchId(entity.getBatchId())
                        .category(entity.getCategory())
                        .request(jsonInputRequest)
                        .response(response.message())
                        .endpoint(String.valueOf(endpoint))
                        .build());
                log.info(aMarker, "Error in converting response from copro server {}", response.message());
            }
        } catch (IOException e) {
            parentObj.add(RadonQueryOutputTable.builder()
                    .originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null))
                    .paperNo(paperNo)
                    .groupId(groupId)
                    .inputFilePath(entity.getInputFilePath())
                    .tenantId(tenantId)
                    .actionId(action.getActionId())
                    .processId(processId)
                    .rootPipelineId(rootPipelineId)
                    .process(entity.getProcess())
                    .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                    .stage(entity.getApiName())
                    .message(ExceptionUtil.toString(e))
                    .batchId(entity.getBatchId())
                    .category(entity.getCategory())
                    .build());

            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("Radon kvp action consumer failed for batch/group " + groupId, handymanException, this.action);
            log.error(aMarker, "The Exception occurred in getting response from copro server {}", ExceptionUtil.toString(e));
        }
    }

    private void extractedCoproOutputResponse(RadonQueryInputTable entity, String radonDataItem, List<RadonQueryOutputTable> parentObj, String request, String response, String endpoint) throws JsonProcessingException {
        Long groupId = entity.getGroupId();
        Long processId = entity.getProcessId();

        Long tenantId = entity.getTenantId();
        Integer paperNo = entity.getPaperNo();
        Long rootPipelineId = entity.getRootPipelineId();
        String processedFilePaths = entity.getInputFilePath();
        String originId = entity.getOriginId();

        RadonKvpLineItem modelResponse = mapper.readValue(radonDataItem, RadonKvpLineItem.class);

        parentObj.add(RadonQueryOutputTable.builder()
                .createdOn(entity.getCreatedOn())
                .createdUserId(tenantId)
                .lastUpdatedOn(CreateTimeStamp.currentTimestamp())
                .lastUpdatedUserId(tenantId)
                .originId(originId)
                .paperNo(paperNo)
                .totalResponseJson(modelResponse.getInferResponse())
                .groupId(groupId)
                .inputFilePath(processedFilePaths)
                .actionId(action.getActionId())
                .tenantId(tenantId)
                .processId(processId)
                .rootPipelineId(rootPipelineId)
                .modelRegistry(entity.getModelRegistry())
                .process(entity.getProcess())
                .status(ConsumerProcessApiStatus.COMPLETED.getStatusDescription())
                .stage(entity.getApiName())
                .batchId(entity.getBatchId())
                .message("Radon kvp action macro completed")
                .category(entity.getCategory())
                .request(request)
                .response(response)
                .endpoint(String.valueOf(endpoint))
                .build()
        );
    }

    private final ObjectMapper objectMapper = new ObjectMapper();
    public Map<String, Object> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
            throws IOException {
        String json = jsonParser.getText();
        // Remove the ```json block and any leading/trailing spaces
        json = json.replace("```json", "").replace("```", "").trim();
        // Deserialize the cleaned JSON string into a Map
        return objectMapper.readValue(json, Map.class);
    }
}
