package in.handyman.raven.lib.model.radonbbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.CoproProcessor;
import in.handyman.raven.lib.model.RadonKvpBbox;
import in.handyman.raven.lib.model.radonbbox.query.input.RadonBboxInputEntity;
import in.handyman.raven.lib.model.radonbbox.query.output.RadonBboxOutputEntity;
import in.handyman.raven.lib.model.radonbbox.request.RadonBboxRequest;
import in.handyman.raven.lib.model.radonbbox.request.RadonBboxRequestLineItem;
import in.handyman.raven.lib.model.radonbbox.response.RadonBboxResponse;
import in.handyman.raven.lib.model.radonbbox.response.RadonBboxResponseData;
import in.handyman.raven.lib.CipherStreamUtil;
import in.handyman.raven.lib.model.triton.*;
import in.handyman.raven.util.ExceptionUtil;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


public class RadonBboxConsumerProcess implements CoproProcessor.ConsumerProcess<RadonBboxInputEntity, RadonBboxOutputEntity> {
    public static final String RADON_BBOX = PipelineName.RADON_KVP_BBOX.getProcessName();
    public static final String RADON_BBOX_START = "RADON BBOX START";
    public static final String OKHTTP_CLIENT_TIMEOUT = "okhttp.client.timeout";
    public static final String SOR_ITEM_NAME = "sor_item_name";
    public static final String ANSWER = "answer";
    public static final String PAPER_TYPE = "paper_type";
    public static final String RADON_KVP_BBOX = "RADON_KVP_BBOX";
    public String predictedAttributionValue = "";

    private final Logger log;
    private final Marker aMarker;
    private static final MediaType MediaTypeJSON = MediaType
            .parse("application/json; charset=utf-8");

    private final ObjectMapper objectMapper;
    public final ActionExecutionAudit action;
    public final String httpClientTimeout;
    final OkHttpClient httpclient = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.MINUTES)
            .writeTimeout(10, TimeUnit.MINUTES)
            .readTimeout(10, TimeUnit.MINUTES)
            .build();

    public final RadonKvpBbox radonKvpBbox;

    public RadonBboxConsumerProcess(Logger log, Marker aMarker, ActionExecutionAudit action, RadonKvpBbox radonKvpBbox, ObjectMapper objectMapper) {
        this.log = log;
        this.aMarker = aMarker;
        this.action = action;
        this.httpClientTimeout = action.getContext().get(OKHTTP_CLIENT_TIMEOUT);
        this.radonKvpBbox = radonKvpBbox;
        this.objectMapper = objectMapper;
    }


    @Override
    public List<RadonBboxOutputEntity> process(URL endpoint, RadonBboxInputEntity entity) throws Exception {
        log.info("triton consumer process started");
        List<RadonBboxOutputEntity> radonBboxOutputEntities = new ArrayList<>();

        final RadonBboxRequest radonBboxRequestData = getRadonBboxRequestData(entity);

        final String jsonInputRequest = objectMapper.writeValueAsString(radonBboxRequestData);

        TritonRequest requestBody = new TritonRequest();
        requestBody.setName(RADON_BBOX_START);
        requestBody.setShape(List.of(1, 1));
        requestBody.setDatatype(TritonDataTypes.BYTES.name());
        requestBody.setData(Collections.singletonList(jsonInputRequest));

        TritonInputRequest tritonInputRequest = new TritonInputRequest();
        tritonInputRequest.setInputs(Collections.singletonList(requestBody));

        final String jsonRequest = objectMapper.writeValueAsString(tritonInputRequest);

        String tritonRequestActivator = radonKvpBbox.getTritonActivator();

        if (!Objects.equals("false", tritonRequestActivator)) {
            final Request tritonRequest = new Request.Builder().url(endpoint).post(RequestBody.create(jsonRequest, MediaTypeJSON)).build();
            tritonRequestBuilder(entity, tritonRequest, objectMapper, radonBboxOutputEntities);

        }

        return radonBboxOutputEntities;
    }

    @NotNull
    private RadonBboxRequest getRadonBboxRequestData(RadonBboxInputEntity entity) throws Exception {
        String encryptionActivator = action.getContext().get("encryption.activator");
        List<RadonBboxRequestLineItem> lineItems = new ArrayList<>();
        String applicationName = "APP";
        String pipelineName = "TEXT EXTRACTION";

        final RadonBboxRequest radonBboxRequestData = new RadonBboxRequest();
        radonBboxRequestData.setOriginId(entity.getOriginId());
        radonBboxRequestData.setPaperNumber(entity.getPaperNo());
        radonBboxRequestData.setProcessId(action.getProcessId());
        radonBboxRequestData.setGroupId(entity.getGroupId());
        radonBboxRequestData.setTenantId(entity.getTenantId());
        radonBboxRequestData.setRootPipelineId(entity.getRootPipelineId());
        radonBboxRequestData.setActionId(action.getActionId());
        radonBboxRequestData.setProcess(RADON_BBOX);
        radonBboxRequestData.setInputFilePath(entity.getInputFilePath());
        radonBboxRequestData.setOutputDir(radonKvpBbox.getOutputDir());
        radonBboxRequestData.setBatchId(entity.getBatchId());
        List<RadonBboxRequestLineItem> items = objectMapper.readValue(entity.getRadonOutput(), new TypeReference<>() {
        });


        if (Objects.equals("true",encryptionActivator)){
                // encryption calling part
                JSONObject listToJson = new JSONObject();

                AtomicInteger keyNum = new AtomicInteger(1);
                // creating unique id to map the list values
                items.forEach(value->{
                    listToJson.put("keyNum"+ keyNum.getAndIncrement(), List.of(value.getSorItemName(), value.getAnswer(), value.getValueType()));

                });

                // new json to store answer and key of list
                JSONObject answerKeyCombination = new JSONObject();
                listToJson.keys().forEachRemaining(key->{
                    JSONArray row = listToJson.getJSONArray(key);
                    String answer = row.getString(1);
                    answerKeyCombination.put(key, answer);              // value to pass into api call
                });

                ActionExecutionAudit actionAudit = new ActionExecutionAudit(); // Initialize as needed
                CipherStreamUtil cipherUtil = new CipherStreamUtil(log, actionAudit);
                // encryption call
                String encryptionCall = cipherUtil.encryptionApi(answerKeyCombination, action, entity.getRootPipelineId(), entity.getGroupId(), entity.getBatchId(), entity.getTenantId(), pipelineName, entity.getOriginId(), applicationName, Math.toIntExact(entity.getPaperNo()));

                ObjectMapper encryptionParsing = new ObjectMapper();
                JsonNode data = encryptionParsing.readTree(encryptionCall);
                JsonNode encryptedData = data.get("encryptedData");  // data from encryption result

                // New JSON object to store results
                JSONObject resultJson = new JSONObject();

                listToJson.keys().forEachRemaining(key -> {
                    if (encryptedData.has(key)) {
                        String encryptedValue = encryptedData.get(key).asText();
                        // Get the answer (index 1) from jsonB
                        String answer = listToJson.getJSONArray(key).getString(1);
                        // Put the answer and encrypted value in the result JSON
                        resultJson.put(answer, encryptedValue);
                    }

                });


                // Create an instance of RadonBboxRequest

                items.forEach(finalValues->{
                    RadonBboxRequestLineItem itemsFinal = new RadonBboxRequestLineItem();
                    if(resultJson.has(finalValues.getAnswer())){
                        itemsFinal.setAnswer((String) resultJson.get(finalValues.getAnswer()));
                        itemsFinal.setSorItemName(finalValues.getSorItemName());
                        itemsFinal.setValueType(finalValues.getValueType());

                    }
                    lineItems.add(itemsFinal);
                    log.info("encrytion is true,  RadonBboxLineItems :"+ lineItems);
                });
                     radonBboxRequestData.setRadonBboxLineItems(lineItems);
                }else {
                    // when encryption is turned off
                    radonBboxRequestData.setRadonBboxLineItems(items);
                    log.info("encrytion is false,  RadonBboxLineItems :"+ items);

        }

        return radonBboxRequestData;
    }


    private void tritonRequestBuilder(RadonBboxInputEntity entity, Request request, ObjectMapper objectMapper, List<RadonBboxOutputEntity> parentObj) throws Exception {

        try (Response response = httpclient.newCall(request).execute()) {

            if (response.isSuccessful()) {
                String responseBody = Objects.requireNonNull(response.body()).string();
                RadonBboxResponse radonBboxModelResponse = objectMapper.readValue(responseBody, RadonBboxResponse.class);

                if (radonBboxModelResponse.getOutputs() != null && !radonBboxModelResponse.getOutputs().isEmpty()) {
                    radonBboxModelResponse.getOutputs().forEach(o -> o.getData().forEach(noiseModelDataItem ->
                            {
                                try {
                                    extractedOutputRequest(entity, objectMapper, parentObj, radonBboxModelResponse.getModelName(), radonBboxModelResponse.getModelVersion(), noiseModelDataItem);
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            }
                    ));
                }

            } else {
                buildOutputParentObject(parentObj, entity, false, "Error in response status code " + response.message(), new RadonBboxResponseData());
            }

        } catch (Exception exception) {
            buildOutputParentObject(parentObj, entity, false, "Error in processing the request " + ExceptionUtil.toString(exception), new RadonBboxResponseData());

            HandymanException handymanException = new HandymanException(exception);
            HandymanException.insertException("NOISE_DETECTION_MODEL  consumer failed for originId " + entity.getOriginId(), handymanException, this.action);
            log.error(aMarker, "The Exception occurred in request {}", request, exception);
        }
    }


    private void extractedOutputRequest(RadonBboxInputEntity entity, ObjectMapper objectMapper, List<RadonBboxOutputEntity> parentObj, String modelName, String modelVersion, String radonKvpBboxDataItem) throws Exception {

        try {

            RadonBboxResponseData radonBboxResponse = objectMapper.readValue(radonKvpBboxDataItem, RadonBboxResponseData.class);
            buildOutputParentObject(parentObj, entity, true, "Completed the radon kvp bbox api", radonBboxResponse);

        } catch (JsonProcessingException e) {
            buildOutputParentObject(parentObj, entity, false, "Error in processing the output json " + ExceptionUtil.toString(e), new RadonBboxResponseData());

            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("RADON_BBOX_MODEL consumer failed for originId " + entity.getOriginId(), handymanException, this.action);
            log.error(aMarker, "The Exception occurred in request {}", e.toString());
        }
    }


    private void buildOutputParentObject(List<RadonBboxOutputEntity> parentObj, RadonBboxInputEntity entity, Boolean status, String message, RadonBboxResponseData radonBboxResponse) throws Exception {
            AtomicInteger keyNum = new AtomicInteger(0);
            JSONObject listToJson = new JSONObject();
            radonBboxResponse.getRadonBboxLineItems().forEach(value -> {
                    // creating unique id to map the list values
                    listToJson.put("keyNum" + keyNum.getAndIncrement(), List.of(value.getSorItemName(), value.getAnswer(), value.getValueType()));
            });

            // new json to store answer and key of list
            JSONObject answerKeyDecryption = new JSONObject();
            // AtomicInteger keyNum = new AtomicInteger(0);
            listToJson.keys().forEachRemaining(key -> {
                JSONArray row = listToJson.getJSONArray(key);
                String answer = row.getString(1);
                answerKeyDecryption.put(key, answer);              // value to pass into api call
             });


            String databaseDecryption = action.getContext().get("database.encryption.activator");
            String applicationName = "APP";
            String pipelineName = "Radon Bbox";
            JSONObject resultJson = new JSONObject();

            String encryptionCall = "";
            if (Objects.equals("true", databaseDecryption)) {
                JSONObject value = new JSONObject();

                try {
                    ActionExecutionAudit actionAudit = new ActionExecutionAudit(); // Initialize as needed
                    CipherStreamUtil cipherUtil = new CipherStreamUtil(log, actionAudit);

                    encryptionCall = cipherUtil.encryptionApi(answerKeyDecryption, action, entity.getRootPipelineId(),
                    entity.getGroupId(), entity.getBatchId(), entity.getTenantId(), pipelineName, entity.getOriginId(), applicationName,Math.toIntExact(entity.getPaperNo()));
                } catch (Exception e) {
                    log.error("Error during decryption API call: {}", e.getMessage(), e);
                    // Handle the exception appropriately, maybe return null or an empty string
                }
                ObjectMapper encryptionParsing = new ObjectMapper();
                JsonNode data = encryptionParsing.readTree(encryptionCall);
                JsonNode enryptedData = data.get("encryptedData");

                listToJson.keys().forEachRemaining(key -> {
                    if (enryptedData.has(key)) {
                        String encryptedValue = enryptedData.get(key).asText();
                        // Get the answer (index 1) from jsonB
                        String answer = listToJson.getJSONArray(key).getString(1);
                        // Put the answer and encrypted value in the result JSON
                        resultJson.put(answer, encryptedValue);
                    }else {
                        log.info("key is not present");
                    }

                });
            }

        if (Boolean.TRUE.equals(status)) {
            radonBboxResponse.getRadonBboxLineItems().forEach(radonResponseBboxLineItem -> {
                try {

                if (Objects.equals("true",databaseDecryption)){
                    predictedAttributionValue = CipherStreamUtil.replaceQuotes(resultJson.get(radonResponseBboxLineItem.getAnswer()).toString());
                } else {
                    predictedAttributionValue = radonResponseBboxLineItem.getAnswer();
                }

                 parentObj.add(RadonBboxOutputEntity.builder()
                    .modelRegistry(entity.getModelRegistry())
                    .inputFilePath(entity.getInputFilePath())
                    .sorContainerName(entity.getSorContainerName())
                    .batchId(entity.getBatchId())
                    .paperNo(radonBboxResponse.getPaperNumber())
                    .originId(radonBboxResponse.getOriginId())
                    .groupId(radonBboxResponse.getGroupId())
                    .tenantId(radonBboxResponse.getTenantId())
                    .rootPipelineId(radonBboxResponse.getRootPipelineId())
                    .bBox(objectMapper.writeValueAsString(radonResponseBboxLineItem.getBBox()))
                    .status(ConsumerProcessApiStatus.COMPLETED.getStatusDescription())
                    .stage(RADON_KVP_BBOX)
                    .message(message)
                    .sorItemName(radonResponseBboxLineItem.getSorItemName())
                    .answer(predictedAttributionValue)
                    .valueType(radonResponseBboxLineItem.getValueType())
                    .build());
                } catch (JsonProcessingException e) {
                    parentObj.add(RadonBboxOutputEntity.builder()
                    .modelRegistry(entity.getModelRegistry())
                    .inputFilePath(entity.getInputFilePath())
                    .sorContainerName(entity.getSorContainerName())
                    .batchId(entity.getBatchId())
                    .paperNo(entity.getPaperNo())
                    .originId(entity.getOriginId())
                    .groupId(entity.getGroupId())
                    .tenantId(entity.getTenantId())
                    .rootPipelineId(entity.getRootPipelineId())
                    .message(message)
                    .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                    .stage(RADON_KVP_BBOX)
//                    .sorItemName(entity.getRadonOutput().get(SOR_ITEM_NAME))
//                    .answer(entity.getRadonOutput().get(ANSWER))
//                    .paperType(entity.getRadonOutput().get(PAPER_TYPE))
//                    .bBox(new BoundingBoxObject())
                    .build());
                }
            });

        } else {
            parentObj.add(RadonBboxOutputEntity.builder()
                    .modelRegistry(entity.getModelRegistry())
                    .inputFilePath(entity.getInputFilePath())
                    .sorContainerName(entity.getSorContainerName())
                    .batchId(entity.getBatchId())
                    .paperNo(entity.getPaperNo())
                    .originId(entity.getOriginId())
                    .groupId(entity.getGroupId())
                    .tenantId(entity.getTenantId())
                    .rootPipelineId(entity.getRootPipelineId())
                    .message(message)
                    .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                    .stage(RADON_KVP_BBOX)
//                    .sorItemName(entity.getRadonOutput().get(SOR_ITEM_NAME))
//                    .answer(entity.getRadonOutput().get(ANSWER))
//                    .paperType(entity.getRadonOutput().get(PAPER_TYPE))
//                    .bBox(new BoundingBoxObject())
                    .build())
            ;
        }

    }



}


