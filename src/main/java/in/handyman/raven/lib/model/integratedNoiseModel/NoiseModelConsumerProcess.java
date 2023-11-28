package in.handyman.raven.lib.model.integratedNoiseModel;




import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.CoproProcessor;
import in.handyman.raven.lib.model.qrextraction.QrInputEntity;
import in.handyman.raven.lib.model.qrextraction.QrOutputEntity;
import in.handyman.raven.lib.model.triton.TritonInputRequest;
import in.handyman.raven.lib.model.triton.TritonRequest;
import jakarta.json.Json;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.net.URL;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


public class NoiseModelConsumerProcess implements CoproProcessor.ConsumerProcess<NoiseModelInputEntity,NoiseModelOutputEnitity>{
    public static final String TRITON_REQUEST_ACTIVATOR = "triton.request.activator";

    private final Logger log;
    private final Marker aMarker;
    private final ObjectMapper mapper = new ObjectMapper();
    private static final MediaType MediaTypeJSON = MediaType
            .parse("application/json; charset=utf-8");

    public final ActionExecutionAudit action;
    public static String httpClientTimeout = new String();
    final OkHttpClient httpclient = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.MINUTES)
            .writeTimeout(10, TimeUnit.MINUTES)
            .readTimeout(10, TimeUnit.MINUTES)
            .build();

    public NoiseModelConsumerProcess(Logger log, Marker aMarker, ActionExecutionAudit action) {
        this.log = log;
        this.aMarker = aMarker;
        this.action = action;
        this.httpClientTimeout = action.getContext().get("okhttp.client.timeout");
    }





    @Override
    public List<NoiseModelOutputEnitity> process(URL endpoint, NoiseModelInputEntity entity) throws Exception {
        log.info("copro consumer process started");
       List<NoiseModelOutputEnitity> noiseOutputEntities = new ArrayList<>();
       // List<NoiseModelOutputEnitity> noiseOutputEntity = new ArrayList<>();
        String filePath = entity.getFilePath();
        final String noiseDetectionModel = "NOISE_DETECTION_MODEL";
        Long rootPipelineId = action.getRootPipelineId();
        Long actionId = Long.valueOf(action.getContext().get("actionId"));

        ObjectMapper objectMapper = new ObjectMapper();
        //payload
        NoiseModelData NoiseModelData = new NoiseModelData();
        NoiseModelData.setRootPipelineId(1L);
        NoiseModelData.setProcess(noiseDetectionModel);
        NoiseModelData.setInputFilePath(filePath);
        NoiseModelData.setActionId(actionId);
        NoiseModelData.setProcessId(action.getProcessId());
        NoiseModelData.setOriginId(entity.getOriginId());
        NoiseModelData.setPaperNo(entity.getPaperNo());
        NoiseModelData.setGroupId(entity.getGroupId());
        NoiseModelData.setOutputDir(entity.getOutputDir());
        NoiseModelData.setTenantId(entity.getTenantId());
        String jsonInputRequest = objectMapper.writeValueAsString(NoiseModelData);

        TritonRequest requestBody = new TritonRequest();
        requestBody.setName("NOISE-DETECTION");
        requestBody.setShape(List.of(1, 1));
        requestBody.setDatatype("BYTES");
        requestBody.setData(Collections.singletonList(jsonInputRequest));

        TritonInputRequest tritonInputRequest = new TritonInputRequest();
        tritonInputRequest.setInputs(Collections.singletonList(requestBody));

        String jsonRequest = objectMapper.writeValueAsString(tritonInputRequest);

        if (log.isInfoEnabled()) {
            log.info("input object node in the consumer process  inputFilePath {}", filePath);
        }
        String tritonRequestActivator = action.getContext().get(TRITON_REQUEST_ACTIVATOR);

        Request Requests = new Request.Builder().url(endpoint).post(RequestBody.create(jsonRequest, MediaTypeJSON)).build();
        if (Objects.equals("false", tritonRequestActivator)) {
            Request request = new Request.Builder().url(endpoint)
                    .post(RequestBody.create(jsonInputRequest, MediaTypeJSON)).build();
            coproRequestBuilder(entity, request,  objectMapper, rootPipelineId,noiseOutputEntities);
        }
//        else {
//            Request request = new Request.Builder().url(endpoint)
//                    .post(RequestBody.create(jsonRequest, MediaTypeJSON)).build();
//            tritonRequestBuilder(entity, request, objectMapper, qrOutputEntities, rootPipelineId);
//        }
        if (log.isInfoEnabled()) {
            log.info("input object node in the consumer process coproURL {}, inputFilePath {}", endpoint, filePath);
        }
        return noiseOutputEntities;
    }
    private void coproRequestBuilder (NoiseModelInputEntity entity, Request request, ObjectMapper objectMapper, Long rootPipelineId ,List<NoiseModelOutputEnitity> noiseOutputEntities) {
        String originId = entity.getOriginId();
        Integer paperNo = entity.getPaperNo();
        Integer groupId = entity.getGroupId();
        String fileId = entity.getFileId();
        String filePath=entity.getFilePath();

        // exectution is after getting resopnse

        try (Response response = httpclient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String responseBody = Objects.requireNonNull(response.body()).string();
                // Parse the JSON string
                JsonNode rootNode = objectMapper.readTree(responseBody);


                if (rootNode != null && !rootNode.isEmpty()) {
                    Integer processId = rootNode.path("processId").asInt();
//                    Integer groupId = rootNode.path("groupId").asInt();
                    Integer tenantId = rootNode.path("tenantId").asInt();
                    String inputFilePath = rootNode.path("inputFilePath").asText();
                    String consolidatedClass = rootNode.path("consolidatedClass").asText();
                    Double consolidatedConfidenceScore = rootNode.path("consolidatedConfidenceScore").asDouble();
                    String extractedValue = rootNode.path("noiseModelsResult").toString();

                    //              getting the output of hwnoise model

                    String hwClass = rootNode
                            .path("noiseModelsResult")
                            .path("hwNoiseDetectionOutput").toString();


                    String checkBoxClass = rootNode
                            .path("noiseModelsResult")
                            .path("checkNoiseDetectionOutput").toString();
                    String tickNoiseClass = rootNode
                            .path("noiseModelsResult")
                            .path("tickNoiseDetectionOutput").toString();
                    String speckleClass = rootNode
                            .path("noiseModelsResult")
                            .path("speckleNoiseDetection").toString();
                    if (10 > 1) {  //print statements
                        System.out.println("processId:" + processId);
                        System.out.println("group_id:" + groupId);
                        System.out.println("tenant_id:" + tenantId);
                        System.out.println("input_file_path:" + inputFilePath);
                        System.out.println("consolidated_class:" + consolidatedClass);
                        System.out.println("consolidated_confidence_score:" + consolidatedConfidenceScore);

                        System.out.println("checkBoxClass:" + checkBoxClass);
                        System.out.println("tickNoiseClass:" + tickNoiseClass);
                        System.out.println("speckleClass:" + speckleClass);
                    }
                    noiseOutputEntities.add(NoiseModelOutputEnitity.builder()
                            .fileId(fileId)
                            .originId(originId)
                            .paperNo(paperNo)
                            .groupId(groupId)
                            .processId(processId)
                            .tenantId(tenantId)
                            .inputFilePath(filePath)
                            .consolidatedConfidenceScore(consolidatedConfidenceScore)
                            .consolidatedClass(consolidatedClass)
                            .extractedValue(extractedValue)
                            .model1NoiseDetectionOutput(hwClass)
                            .model2NoiseDetectionOutput(checkBoxClass)
                            .model3NoiseDetectionOutput(tickNoiseClass)
                            .model4NoiseDetectionOutput(speckleClass)
                            .createdOn(LocalDateTime.now())
                            .rootPipelineId(rootPipelineId)
                            .status("COMPLETED")
                            .stage("NOISE_DETECTION_MODEL")
                            .message("noise detection completed")
                            .build());


                } else {
                    noiseOutputEntities.add(NoiseModelOutputEnitity.builder()
                            .originId(originId)
                            .paperNo(paperNo)
                            .groupId(groupId)
                            .fileId(fileId)
                            .createdOn(LocalDateTime.now())
                            .rootPipelineId(rootPipelineId)
                            .status("ABSENT")
                            .stage("NOISE_DETECTION")
                            .message("noise detection code absent in the given file")
                            .build());
                }

            } else {
                noiseOutputEntities.add(NoiseModelOutputEnitity.builder()
                        .originId(originId)
                        .paperNo(paperNo)
                        .groupId(groupId)
                        .fileId(fileId)
                        .createdOn(LocalDateTime.now())
                        .rootPipelineId(rootPipelineId)
                        .status("FAILED")
                        .stage("NOISE_DETECTION")
                        .message(response.message())
                        .build());
                log.error(aMarker, "The Exception occurred in episode of coverage in response {}", response);
            }

        } catch (Exception e) {
            noiseOutputEntities.add(NoiseModelOutputEnitity.builder()
                    .originId(originId)
                    .paperNo(paperNo)
                    .groupId(groupId)
                    .fileId(fileId)
                    .createdOn(LocalDateTime.now())
                    .status("FAILED")
                    .rootPipelineId(rootPipelineId)
                    .stage("NOISE_DETECTION")
                    .message(e.getMessage())
                    .build());
            log.error("Error in the copro process api hit {}", request);
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("Error in noise detection action for group id - " + groupId, handymanException, this.action);
        }


    }
    }
