package in.handyman.raven.lib.model.common;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.CoproProcessor;
import in.handyman.raven.lib.model.common.copro.ComparisonDataItemCopro;
import in.handyman.raven.lib.model.triton.TritonInputRequest;
import in.handyman.raven.lib.model.triton.TritonRequest;
import in.handyman.raven.util.ExceptionUtil;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class MasterdataComparisonProcess implements CoproProcessor.ConsumerProcess<MasterDataInputTable, MasterDataOutputTable> {
    private final Logger log;
    private final Marker aMarker;
    public final ActionExecutionAudit action;
    private static ObjectMapper mapper = new ObjectMapper();

    public static final String TRITON_REQUEST_ACTIVATOR = "triton.request.activator";
    private static final MediaType MEDIA_TYPE_JSON = MediaType
            .parse("application/json; charset=utf-8");
    private final OkHttpClient httpclient = new OkHttpClient.Builder()
            .connectTimeout(100, TimeUnit.MINUTES)
            .writeTimeout(100, TimeUnit.MINUTES)
            .readTimeout(100, TimeUnit.MINUTES)
            .build();
    String URI;

    public MasterdataComparisonProcess(Logger log, Marker aMarker, ActionExecutionAudit action) {
        this.log = log;
        this.aMarker = aMarker;
        this.mapper = new ObjectMapper();
        this.action = action;

    }

    @Override
    public List<MasterDataOutputTable> process(URL endpoint, MasterDataInputTable result) throws Exception {
        log.info(aMarker, "coproProcessor consumer process started with endpoint {} and entity {}", endpoint, result);
        List<MasterDataOutputTable> parentObj = new ArrayList<>();
        AtomicInteger atomicInteger = new AtomicInteger();


        String eocIdentifier = result.getEocIdentifier();
        String extractedValue = result.getExtractedValue();
        String actualValue = result.getActualValue();
        Integer paperNo = result.getPaperNo();
        String originId = result.getOriginId();
        String process = "MASTER_DATA";
        Long actionId = action.getActionId();
        Long rootpipelineId = action.getRootPipelineId();
        String inputSentence = result.getActualValue();
        List<String> sentences = Collections.singletonList(result.getExtractedValue());
        ObjectMapper objectMapper = new ObjectMapper();

//payload
            ComparisonPayload comparisonPayload = new ComparisonPayload();
        comparisonPayload.setRootPipelineId(rootpipelineId);
        comparisonPayload.setActionId(actionId);
        comparisonPayload.setProcessId(action.getProcessId());
        comparisonPayload.setOriginId(result.getOriginId());
        comparisonPayload.setProcess(process);
        comparisonPayload.setInputSentence(inputSentence);
        comparisonPayload.setSentences(sentences);
        comparisonPayload.setTenantId(result.getTenantId());
        comparisonPayload.setGroupId(result.getGroupId());
        String jsonInputRequest = objectMapper.writeValueAsString(comparisonPayload);

        TritonRequest requestBody = new TritonRequest();
        requestBody.setName("COS START");
        requestBody.setShape(List.of(1, 1));
        requestBody.setDatatype("BYTES");
        requestBody.setData(Collections.singletonList(jsonInputRequest));

        TritonInputRequest tritonInputRequest = new TritonInputRequest();
        tritonInputRequest.setInputs(Collections.singletonList(requestBody));
        if (result.getActualValue() != null) {
            String tritonRequestActivator = action.getContext().get(TRITON_REQUEST_ACTIVATOR);
            String jsonRequest = objectMapper.writeValueAsString(tritonInputRequest);
            if (Objects.equals("false", tritonRequestActivator)) {
                Request request = new Request.Builder().url(endpoint)
                        .post(RequestBody.create(jsonInputRequest, MEDIA_TYPE_JSON)).build();
                coproRequestBuider(result, request, parentObj);
            } else {
                Request request = new Request.Builder().url(endpoint)
                        .post(RequestBody.create(jsonRequest, MEDIA_TYPE_JSON)).build();
                tritonRequestBuilder(result, request, parentObj);
            }
        } else {
            parentObj.add(
                    MasterDataOutputTable.builder()
                            .originId(Optional.ofNullable(originId).map(String::valueOf).orElse(null))
                            .eocIdentifier(eocIdentifier)
                            .paperNo(paperNo)
                            .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                            .extractedValue(extractedValue)
                            .actualValue(actualValue)
                            .intelliMatch(0)
                            .status("COMPLETED")
                            .stage("MASTER-DATA-COMPARISON")
                            .message("Master data comparison macro completed")
                            .build()
            );
            log.info(aMarker, "coproProcessor consumer process with empty actual value entity {}", result);
        }
        atomicInteger.set(0);
        log.info(aMarker, "coproProcessor consumer process with output entity {}", parentObj);
        return parentObj;
    }

    private void coproRequestBuider(MasterDataInputTable result, Request request, List<MasterDataOutputTable> parentObj) throws IOException {
        String eocIdentifier = result.getEocIdentifier();
        String originId = result.getOriginId();
        Integer paperNo = result.getPaperNo();
        String extractedValue = result.getExtractedValue();
        String actualValue = result.getActualValue();
        try (Response response = httpclient.newCall(request).execute()) {
            log.info("master data comparison response body {}", response.body());
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                extractedCoproOutputResponse(result, parentObj, responseBody, "", "");
            } else {
                parentObj.add(
                        MasterDataOutputTable.builder()
                                .originId(Optional.ofNullable(originId).map(String::valueOf).orElse(null))
                                .eocIdentifier(eocIdentifier)
                                .paperNo(paperNo)
                                .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                                .extractedValue(extractedValue)
                                .actualValue(actualValue)
                                .intelliMatch(0)
                                .status("FAILED")
                                .stage("MASTER-DATA-COMPARISON")
                                .message("Master data comparison macro failed")
                                .rootPipelineId(action.getRootPipelineId())
                                .build()
                );

            }
        }
    }

    private void tritonRequestBuilder(MasterDataInputTable result, Request request, List<MasterDataOutputTable> parentObj) {
        String eocIdentifier = result.getEocIdentifier();
        String originId = result.getOriginId();
        Integer paperNo = result.getPaperNo();
        String extractedValue = result.getExtractedValue();
        String actualValue = result.getActualValue();
        try (Response response = httpclient.newCall(request).execute()) {
            log.info("master data comparison response body {}", response.body());
            String responseBody = Objects.requireNonNull(response.body()).string();
            if (response.isSuccessful()) {
                ObjectMapper objectMappers = new ObjectMapper();
                ComparisonResponse Response = objectMappers.readValue(responseBody, ComparisonResponse.class);
                if (Response.getOutputs() != null && !Response.getOutputs().isEmpty()) {
                    Response.getOutputs().forEach(o -> {
                        o.getData().forEach(comparisonDataItem -> {
                            extractOuputDataRequest(result, parentObj, comparisonDataItem, Response.getModelName(), Response.getModelVersion());
                        });
                    });
                }
            } else {
                parentObj.add(
                        MasterDataOutputTable.builder()
                                .originId(Optional.ofNullable(originId).map(String::valueOf).orElse(null))
                                .eocIdentifier(eocIdentifier)
                                .paperNo(paperNo)
                                .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                                .extractedValue(extractedValue)
                                .actualValue(actualValue)
                                .intelliMatch(0)
                                .status("FAILED")
                                .stage("MASTER-DATA-COMPARISON")
                                .message("Master data comparison macro failed")
                                .rootPipelineId(action.getRootPipelineId())
                                .build()
                );
                log.error(aMarker, "The Exception occurred in master data comparison by {} ", response);
                throw new HandymanException(responseBody);
            }
        } catch (Exception exception) {
            parentObj.add(
                    MasterDataOutputTable.builder()
                            .originId(Optional.ofNullable(originId).map(String::valueOf).orElse(null))
                            .eocIdentifier(eocIdentifier)
                            .paperNo(paperNo)
                            .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                            .extractedValue(extractedValue)
                            .actualValue(actualValue)
                            .intelliMatch(0)
                            .status("FAILED")
                            .stage("MASTER-DATA-COMPARISON")
                            .message("Master data comparison macro failed")
                            .rootPipelineId(action.getRootPipelineId())
                            .build()
            );

            log.error(aMarker, "Exception occurred in copro api for master data comparison - {} ", ExceptionUtil.toString(exception));
            HandymanException handymanException = new HandymanException(exception);
            HandymanException.insertException("Paper classification (hw-detection) consumer failed for originId " + originId, handymanException, this.action);
        }
    }

    private static void extractOuputDataRequest(MasterDataInputTable result, List<MasterDataOutputTable> parentObj, String comparisonDataItem, String modelName, String modelVersion) {
        String eocIdentifier = result.getEocIdentifier();
        String originId = result.getOriginId();
        Integer paperNo = result.getPaperNo();
        String extractedValue = result.getExtractedValue();
        String actualValue = result.getActualValue();
        Long tenantId = result.getTenantId();

        try {
            List<ComparisonDataItem> comparisonDataItem1 = mapper.readValue(comparisonDataItem, new TypeReference<>() {
            });
            for (ComparisonDataItem item : comparisonDataItem1) {
                parentObj.add(MasterDataOutputTable
                        .builder()
                        .originId(item.getOriginId())
                        .eocIdentifier(eocIdentifier)
                        .paperNo(paperNo)
                        .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                        .extractedValue(item.getSentence())
                        .actualValue(result.getActualValue())
                        .intelliMatch(item.getSimilarityPercent())
                        .status("COMPLETED")
                        .stage("MASTER-DATA-COMPARISON")
                        .message("Master data comparison macro completed")
                        .rootPipelineId(item.getRootPipelineId())
                        .modelName(modelName)
                        .tenantId(tenantId)
                        .modelVersion(modelVersion)
                        .build());
            }

            }catch (Exception exception) {
            parentObj.add(
                    MasterDataOutputTable.builder()
                            .originId(Optional.ofNullable(originId).map(String::valueOf).orElse(null))
                            .eocIdentifier(eocIdentifier)
                            .paperNo(paperNo)
                            .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                            .extractedValue(extractedValue)
                            .actualValue(actualValue)
                            .intelliMatch(0)
                            .status("FAILED")
                            .stage("MASTER-DATA-COMPARISON")
                            .message("Master data comparison macro failed")
                            .tenantId(tenantId)
                            .rootPipelineId(result.getRootPipelineId())
                            .build()
            );
        }
    }
    private static void extractedCoproOutputResponse(MasterDataInputTable result, List<MasterDataOutputTable> parentObj, String comparisonDataItem, String modelName, String modelVersion) {
        String eocIdentifier = result.getEocIdentifier();
        String originId = result.getOriginId();
        Integer paperNo = result.getPaperNo();
        String extractedValue = result.getExtractedValue();
        String actualValue = result.getActualValue();
        Long tenantId = result.getTenantId();

        try {
            List<ComparisonDataItemCopro> comparisonDataItemCopros = mapper.readValue(
                    comparisonDataItem, new TypeReference<List<ComparisonDataItemCopro>>() {
                    });

            for (ComparisonDataItemCopro item: comparisonDataItemCopros) {
                parentObj.add(MasterDataOutputTable
                        .builder()
                        .originId(result.getOriginId())
                        .eocIdentifier(eocIdentifier)
                        .paperNo(paperNo)
                        .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                        .extractedValue(item.getSentence())
                        .actualValue(result.getActualValue())
                        .intelliMatch(item.getSimilarityPercent())
                        .status("COMPLETED")
                        .stage("MASTER-DATA-COMPARISON")
                        .message("Master data comparison macro completed")
                        .rootPipelineId(result.getRootPipelineId())
                        .modelName(modelName)
                        .tenantId(tenantId)
                        .modelVersion(modelVersion)
                        .build());
            }

        }catch (Exception exception) {
            parentObj.add(
                    MasterDataOutputTable.builder()
                            .originId(Optional.ofNullable(originId).map(String::valueOf).orElse(null))
                            .eocIdentifier(eocIdentifier)
                            .paperNo(paperNo)
                            .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                            .extractedValue(extractedValue)
                            .actualValue(actualValue)
                            .intelliMatch(0)
                            .status("FAILED")
                            .stage("MASTER-DATA-COMPARISON")
                            .message("Master data comparison macro failed")
                            .tenantId(tenantId)
                            .rootPipelineId(result.getRootPipelineId())
                            .build()
            );
        }
    }
}


