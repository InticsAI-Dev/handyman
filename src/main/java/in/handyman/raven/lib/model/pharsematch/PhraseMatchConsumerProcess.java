package in.handyman.raven.lib.model.pharsematch;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.CoproProcessor;
import in.handyman.raven.lib.model.pharsematch.copro.PharseMatchDataItemCopro;
import in.handyman.raven.lib.model.triton.ConsumerProcessApiStatus;
import in.handyman.raven.lib.model.triton.PipelineName;
import in.handyman.raven.lib.model.triton.TritonInputRequest;
import in.handyman.raven.lib.model.triton.TritonRequest;
import in.handyman.raven.util.ExceptionUtil;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class PhraseMatchConsumerProcess implements CoproProcessor.ConsumerProcess<PhraseMatchInputTable, PhraseMatchOutputTable> {
    private final Logger log;
    private final Marker aMarker;
    private final ObjectMapper mapper = new ObjectMapper();
    private static final MediaType MediaTypeJSON = MediaType
            .parse("application/json; charset=utf-8");
    public final ActionExecutionAudit action;
    private static final String PROCESS_NAME = PipelineName.PHRASE_MATCH.getProcessName();

    final OkHttpClient httpclient = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.MINUTES)
            .writeTimeout(10, TimeUnit.MINUTES)
            .readTimeout(10, TimeUnit.MINUTES)
            .build();

    public PhraseMatchConsumerProcess(final Logger log, final Marker aMarker, ActionExecutionAudit action) {
        this.log = log;
        this.aMarker = aMarker;
        this.action = action;
    }

    @Override
    public List<PhraseMatchOutputTable> process(URL endpoint, PhraseMatchInputTable entity) throws JsonProcessingException {
        List<PhraseMatchOutputTable> parentObj = new ArrayList<>();
        String originId = entity.getOriginId();
        String groupId = entity.getGroupId();
        String paperNo = String.valueOf(entity.getPaperNo());
        Long actionId = action.getActionId();
        String pageContent = String.valueOf(entity.getPageContent());


        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, List<String>> keysToFilterObject = objectMapper.readValue(entity.getTruthPlaceholder(), new TypeReference<>() {
        });

        //payload
        PharseMatchData data = new PharseMatchData();
        data.setRootPipelineId(Math.toIntExact(action.getRootPipelineId()));
        data.setActionId(actionId);
        data.setProcess(entity.getProcessId());
        data.setOriginId(originId);
        data.setPaperNo(Long.valueOf(paperNo));
        data.setGroupId(groupId);
        data.setPageContent(pageContent);
        data.setKeysToFilter(keysToFilterObject);
        data.setProcess(PROCESS_NAME);


        String jsonInputRequest = objectMapper.writeValueAsString(data);

        TritonRequest requestBody = new TritonRequest();
        requestBody.setName("PM START");
        requestBody.setShape(List.of(1, 1));
        requestBody.setDatatype("BYTES");
        requestBody.setData(Collections.singletonList(jsonInputRequest));


        TritonInputRequest tritonInputRequest = new TritonInputRequest();
        tritonInputRequest.setInputs(Collections.singletonList(requestBody));

        String jsonRequest = objectMapper.writeValueAsString(tritonInputRequest);

        String tritonRequestActivator = action.getContext().get("triton.request.activator");


        if (Objects.equals("false", tritonRequestActivator)) {
            Request request = new Request.Builder().url(endpoint)
                    .post(RequestBody.create(jsonInputRequest, MediaTypeJSON)).build();
            coproRequestBuilder(entity, parentObj, request, objectMapper);
        } else {
            Request request = new Request.Builder().url(endpoint)
                    .post(RequestBody.create(jsonRequest, MediaTypeJSON)).build();
            tritonRequestBuilder(entity, parentObj, request);
        }

        return parentObj;
    }

    private void coproRequestBuilder(PhraseMatchInputTable entity, List<PhraseMatchOutputTable> parentObj, Request request, ObjectMapper objectMapper) {
        final Integer paperNo = Optional.ofNullable(entity.getPaperNo()).map(String::valueOf).map(Integer::parseInt).orElse(null);
        Long tenantId = entity.getTenantId();
        Long rootPipelineId = entity.getRootPipelineId();
        try (Response response = httpclient.newCall(request).execute()) {
            String responseBody = Objects.requireNonNull(response.body()).string();
            if (response.isSuccessful()) {
                extractedCoproOutputResponse(entity, parentObj, responseBody, objectMapper, "", "");

            } else {
                parentObj.add(
                        PhraseMatchOutputTable
                                .builder()
                                .originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null))
                                .groupId(Optional.ofNullable(entity.getGroupId()).map(String::valueOf).orElse(null))
                                .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                                .tenantId(tenantId)
                                .paperNo(paperNo)
                                .stage(PROCESS_NAME)
                                .message(Optional.of(responseBody).map(String::valueOf).orElse(null))
                                .rootPipelineId(rootPipelineId)
                                .batchId(entity.getBatchId())
                                .build());
                log.info(aMarker, "The Exception occurred in Phrase match API call");
            }

        } catch (Exception exception) {
            parentObj.add(
                    PhraseMatchOutputTable
                            .builder()
                            .originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null))
                            .groupId(Optional.ofNullable(entity.getGroupId()).map(String::valueOf).orElse(null))
                            .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                            .paperNo(paperNo)
                            .tenantId(tenantId)
                            .stage(PROCESS_NAME)
                            .message(exception.getMessage())
                            .rootPipelineId(rootPipelineId)
                            .batchId(entity.getBatchId())
                            .build());
            log.error(aMarker, "Exception occurred in the phrase match paper filter action {}", ExceptionUtil.toString(exception));
            HandymanException handymanException = new HandymanException(exception);
            HandymanException.insertException("Error in inserting Intellimatch result table", handymanException, this.action);

        }
    }


    private void tritonRequestBuilder(PhraseMatchInputTable entity, List<PhraseMatchOutputTable> parentObj, Request request) {
        Long tenantId = entity.getTenantId();
        final Integer paperNo = Optional.ofNullable(entity.getPaperNo()).map(String::valueOf).map(Integer::parseInt).orElse(null);
        Long rootPipelineId = entity.getRootPipelineId();
        try (Response response = httpclient.newCall(request).execute()) {
            String responseBody = Objects.requireNonNull(response.body()).string();

            if (response.isSuccessful()) {
                ObjectMapper objectMapper = new ObjectMapper();
                PharseMatchResponse modelResponse = objectMapper.readValue(responseBody, PharseMatchResponse.class);
                if (modelResponse.getOutputs() != null && !modelResponse.getOutputs().isEmpty()) {
                    modelResponse.getOutputs().forEach(o -> {
                        o.getData().forEach(phraseMatchDataItem -> {
                            extractedOutputDataRequest(entity, parentObj, phraseMatchDataItem, objectMapper, modelResponse.getModelName(), modelResponse.getModelVersion());
                        });
                    });
                } else {
                    parentObj.add(
                            PhraseMatchOutputTable
                                    .builder()
                                    .originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null))
                                    .groupId(Optional.ofNullable(entity.getGroupId()).map(String::valueOf).orElse(null))
                                    .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                                    .tenantId(tenantId)
                                    .paperNo(paperNo)
                                    .stage(PROCESS_NAME)
                                    .message(Optional.of(responseBody).map(String::valueOf).orElse(null))
                                    .rootPipelineId(rootPipelineId)
                                    .batchId(entity.getBatchId())
                                    .build());
                    log.info(aMarker, "The Exception occurred in Phrase match API call");
                }
            }
        } catch (Exception exception) {
            parentObj.add(
                    PhraseMatchOutputTable
                            .builder()
                            .originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null))
                            .groupId(Optional.ofNullable(entity.getGroupId()).map(String::valueOf).orElse(null))
                            .status(ConsumerProcessApiStatus.FAILED.getStatusDescription())
                            .tenantId(tenantId)
                            .paperNo(paperNo)
                            .stage(PROCESS_NAME)
                            .message(exception.getMessage())
                            .rootPipelineId(rootPipelineId)
                            .batchId(entity.getBatchId())
                            .build());
            log.error(aMarker, "Exception occurred in the phrase match paper filter action {}", ExceptionUtil.toString(exception));
            HandymanException handymanException = new HandymanException(exception);
            HandymanException.insertException("Error in inserting Intellimatch result table", handymanException, this.action);

        }
    }

    private static void extractedOutputDataRequest(PhraseMatchInputTable entity, List<PhraseMatchOutputTable> parentObj, String pharseMatchDataItem, ObjectMapper objectMapper, String modelName, String modelVersion) {
        Long tenantId = entity.getTenantId();

        Long rootPipelineId = entity.getRootPipelineId();
        try {
            List<PharseMatchDataItem> phraseMatchOutputData = objectMapper.readValue(pharseMatchDataItem, new TypeReference<>() {

            });

            for (PharseMatchDataItem item : phraseMatchOutputData) {
                parentObj.add(
                        PhraseMatchOutputTable
                                .builder()
                                .originId(item.getOriginId())
                                .groupId(item.getGroupId())
                                .paperNo(item.getPaperNo())
                                .status(ConsumerProcessApiStatus.COMPLETED.getStatusDescription())
                                .tenantId(tenantId)
                                .stage(PROCESS_NAME)
                                .message("Completed API call zero shot classifier")
                                .rootPipelineId(rootPipelineId)
                                .modelName(modelName)
                                .truthEntity(item.getTruthEntity())
                                .isKeyPresent(String.valueOf(item.getIsKeyPresent()))
                                .entity(item.getEntity())
                                .modelVersion(modelVersion)
                                .batchId(entity.getBatchId())
                                .build());
            }

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private static void extractedCoproOutputResponse(PhraseMatchInputTable entity, List<PhraseMatchOutputTable> parentObj, String pharseMatchDataItem, ObjectMapper objectMapper, String modelName, String modelVersion) {
        String originId = entity.getOriginId();
        String groupId = entity.getGroupId();
        Long tenantId = entity.getTenantId();

        final Integer paperNo = Optional.ofNullable(entity.getPaperNo()).map(String::valueOf).map(Integer::parseInt).orElse(null);
        Long rootPipelineId = entity.getRootPipelineId();
        try {
            List<PharseMatchDataItemCopro> pharseMatchOutputData = objectMapper.readValue(pharseMatchDataItem, new TypeReference<List<PharseMatchDataItemCopro>>() {

            });

            for (PharseMatchDataItemCopro item : pharseMatchOutputData) {

                parentObj.add(
                        PhraseMatchOutputTable
                                .builder()
                                .originId(Optional.ofNullable(item.getOriginId()).map(String::valueOf).orElse(null))
                                .groupId(Optional.ofNullable(item.getGroupId()).map(String::valueOf).orElse(null))
                                .paperNo(item.getPaperNo())
                                .status(ConsumerProcessApiStatus.COMPLETED.getStatusDescription())
                                .tenantId(tenantId)
                                .stage(PROCESS_NAME)
                                .message("Completed API call zero shot classifier")
                                .rootPipelineId(rootPipelineId)
                                .modelName(modelName)
                                .truthEntity(item.getTruthEntity())
                                .isKeyPresent(String.valueOf(item.getIsKeyPresent()))
                                .entity(item.getEntity())
                                .modelVersion(modelVersion)
                                .batchId(entity.getBatchId())
                                .build());
            }

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}

