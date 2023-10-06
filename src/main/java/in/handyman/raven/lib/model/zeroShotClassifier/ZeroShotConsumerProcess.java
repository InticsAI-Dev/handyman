package in.handyman.raven.lib.model.zeroShotClassifier;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.CoproProcessor;
import in.handyman.raven.lib.model.paperItemizer.PaperItemizerDataItem;
import in.handyman.raven.lib.model.triton.TritonInputRequest;
import in.handyman.raven.lib.model.triton.TritonRequest;
import in.handyman.raven.util.ExceptionUtil;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;




    public class ZeroShotConsumerProcess implements CoproProcessor.ConsumerProcess<PaperFilteringZeroShotClassifierInputTable, PaperFilteringZeroShotClassifierOutputTable> {
        private final Logger log;
        private final Marker aMarker;
        private final ObjectMapper mapper = new ObjectMapper();
        private static final MediaType MediaTypeJSON = MediaType
                .parse("application/json; charset=utf-8");

        private static final String actionName = "ZERO_SHOT_CLASSIFIER";

        public final ActionExecutionAudit action;
        final OkHttpClient httpclient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.MINUTES)
                .writeTimeout(10, TimeUnit.MINUTES)
                .readTimeout(10, TimeUnit.MINUTES)
                .build();

        public ZeroShotConsumerProcess(final Logger log, final Marker aMarker, ActionExecutionAudit action) throws JsonMappingException, JsonProcessingException {
            this.log = log;
            this.aMarker = aMarker;
            this.action = action;
        }

        @Override
        public List<PaperFilteringZeroShotClassifierOutputTable> process(URL endpoint, PaperFilteringZeroShotClassifierInputTable entity) throws JsonProcessingException {
            List<PaperFilteringZeroShotClassifierOutputTable> parentObj = new ArrayList<>();

            String originId = entity.getOriginId();
            String groupId = entity.getGroupId();
            String rootPipelineId = String.valueOf(entity.getRootPipelineId());
            String processId = String.valueOf(entity.getProcessId());
            String paperNo = String.valueOf(entity.getPaperNo());
            Long actionId = action.getActionId();
            String pageContent = String.valueOf(entity.getPageContent());


            ObjectMapper objectMapper = new ObjectMapper();
            Map<String,List<String>> keysToFilterObject = objectMapper.readValue(entity.getTruthPlaceholder(), new TypeReference<Map<String, List<String>>>() {
            });


            //payload

            ZeroShotClassifierData data = new ZeroShotClassifierData();
            data.setRootPipelineId(rootPipelineId);
            data.setActionId(actionId);
            data.setProcess(entity.getProcessId());
            data.setOriginId(originId);
            data.setPaperNo(paperNo);
            data.setGroupId(groupId);
            data.setPageContent(pageContent);
            data.setKeysToFilter(keysToFilterObject);
            String jsonInputRequest = objectMapper.writeValueAsString(data);


            TritonRequest requestBody = new TritonRequest();
            requestBody.setName("ZSC START");
            requestBody.setShape(List.of(1, 1));
            requestBody.setDatatype("BYTES");
            requestBody.setData(Collections.singletonList(jsonInputRequest));


            TritonInputRequest tritonInputRequest=new TritonInputRequest();
            tritonInputRequest.setInputs(Collections.singletonList(requestBody));


            String jsonRequest = objectMapper.writeValueAsString(tritonInputRequest);

            try {
                String truthPlaceholder = entity.getTruthPlaceholder();

                Request request = new Request.Builder().url(endpoint)
                        .post(RequestBody.create(jsonRequest, MediaTypeJSON)).build();
                if (log.isInfoEnabled()) {
                    log.info(aMarker, "Input variables id : {}", actionId);
                    log.info(aMarker, "Request has been built with the parameters\nURI: {}, originId {}, truthPlaceHolder {}, groupId {}, paperNo {}", endpoint,originId, truthPlaceholder, groupId, paperNo);
                }
                coproAPIProcessor(entity, parentObj, request);
            } catch (Exception e) {
                log.error("Error in the zero-shot classifier paper filter copro api call {}", e.toString());
                HandymanException handymanException = new HandymanException(e);
                HandymanException.insertException("Exception occurred in urgency triage model action for group id - " + groupId + " and originId - " + originId, handymanException, this.action);
            }
            return parentObj;
        }

        private <EntityConfidenceScore> void coproAPIProcessor(PaperFilteringZeroShotClassifierInputTable entity, List<PaperFilteringZeroShotClassifierOutputTable> parentObj, Request request) throws IOException {
            String originId = entity.getOriginId();
            String groupId = entity.getGroupId();

            final Integer paperNo = Optional.ofNullable(entity.getPaperNo()).map(String::valueOf).map(Integer::parseInt).orElse(null);
            Long rootPipelineId = entity.getRootPipelineId();

            try (Response response = httpclient.newCall(request).execute()) {
                String responseBody = Objects.requireNonNull(response.body()).string();
                if (response.isSuccessful()) {
                    ObjectMapper objectMapper = new ObjectMapper();
                    ZeroShotClassifierModelResponse modelResponse = objectMapper.readValue(responseBody, ZeroShotClassifierModelResponse.class);
                    if (modelResponse.getOutputs() != null && !modelResponse.getOutputs().isEmpty()) {
                        modelResponse.getOutputs().forEach(o -> {
                            o.getData().forEach(ZeroShotClassifierDataItem -> {

                                    try {
                                                    ZeroShotClassifierDataItem zeroshotclassifierOutputData = objectMapper.readValue(ZeroShotClassifierDataItem, ZeroShotClassifierDataItem.class);
                                                    zeroshotclassifierOutputData. getEntityConfidenceScore().forEach(score -> {
                                                    String truthEntity = score.getTruthEntity();
                                                    String key = score.getKey();
                                                    double scoreValue = score.getScore();

                                                    parentObj.add(PaperFilteringZeroShotClassifierOutputTable
                                                            .builder()
                                                        .originId(Optional.ofNullable(originId).map(String::valueOf).orElse(null))
                                                        .groupId(Optional.ofNullable(groupId).map(String::valueOf).orElse(null))
                                                        .truthEntity(Optional.ofNullable(truthEntity).map(String::valueOf).orElse(null))
                                                        .entity(Optional.ofNullable(key).map(String::valueOf).orElse(null))
                                                        .confidenceScore(Optional.of(scoreValue).map(String::valueOf).orElse(null))
                                                        .paperNo(paperNo)
                                                        .status("COMPLETED")
                                                        .stage(actionName)
                                                        .message("Completed API call zero shot classifier")
                                                        .rootPipelineId(rootPipelineId)
                                                        .modelName(modelResponse.getModelName())
                                                        .modelVersion(modelResponse.getModelVersion())
                                                        .build());
                                                });
                                    } catch (JsonMappingException e) {
                                        throw new RuntimeException(e);
                                    } catch (JsonProcessingException e) {
                                        throw new RuntimeException(e);
                                    }
                            });
                        });
                    }
                } else {
                    parentObj.add(
                            PaperFilteringZeroShotClassifierOutputTable
                                    .builder()
                                    .originId(Optional.ofNullable(originId).map(String::valueOf).orElse(null))
                                    .groupId(Optional.ofNullable(groupId).map(String::valueOf).orElse(null))
                                    .status("FAILED")
                                    .paperNo(paperNo)
                                    .stage(actionName)
                                    .message(Optional.of(responseBody).map(String::valueOf).orElse(null))
                                    .rootPipelineId(rootPipelineId)
                                    .build());
                    log.error(aMarker, "Exception occurred in zero shot classifier API call");
                }
            } catch (Exception exception) {
                parentObj.add(
                        PaperFilteringZeroShotClassifierOutputTable
                                .builder()
                                .originId(Optional.ofNullable(originId).map(String::valueOf).orElse(null))
                                .groupId(Optional.ofNullable(groupId).map(String::valueOf).orElse(null))
                                .status("FAILED")
                                .paperNo(paperNo)
                                .stage(actionName)
                                .message(exception.getMessage())
                                .rootPipelineId(rootPipelineId)
                                .build());
                log.error(aMarker, "Exception occurred in the zero shot classifier paper filter action {}", ExceptionUtil.toString(exception));
                HandymanException handymanException = new HandymanException(exception);
                HandymanException.insertException("Zero shot classifier paper filter action failed for groupId - " + groupId + "and originId - " + originId + "and paperNo -" + paperNo, handymanException, action);
            }
        }
    }



