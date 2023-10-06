package in.handyman.raven.lib.model.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.CoproProcessor;
import in.handyman.raven.lib.model.triton.TritonInputRequest;
import in.handyman.raven.lib.model.triton.TritonRequest;
import in.handyman.raven.util.ExceptionUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.net.URL;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static in.handyman.raven.lib.HwDetectionAction.httpClientTimeout;
import static in.handyman.raven.lib.MasterdataComparisonAction.MediaTypeJSON;

public class MasterdataComparisonProcess implements CoproProcessor.ConsumerProcess<MasterDataInputTable, MasterDataOutputTable> {
    private final Logger log;
    private final Marker aMarker;
    public final ActionExecutionAudit action;
    final ObjectMapper mapper;
    private final OkHttpClient httpclient = new OkHttpClient.Builder()
            .connectTimeout(Long.parseLong(httpClientTimeout), TimeUnit.MINUTES)
            .writeTimeout(Long.parseLong(httpClientTimeout), TimeUnit.MINUTES)
            .readTimeout(Long.parseLong(httpClientTimeout), TimeUnit.MINUTES)
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
        String process="MASTER_DATA";
        Long actionId= action.getActionId();
        Long rootpipelineId= result.getRootPipelineId();
        String inputSentence = result.getActualValue();
        List<String> sentence = Collections.singletonList(result.getExtractedValue());
        ObjectMapper objectMapper = new ObjectMapper();

//payload
        ComparisonPayload Comparisonpayload = new ComparisonPayload();
        Comparisonpayload.setRootPipelineId(rootpipelineId);
        Comparisonpayload.setActionId(actionId);
        Comparisonpayload.setProcess(process);
        Comparisonpayload.setInputSentence(inputSentence);
        Comparisonpayload.setSentence(sentence);
        String jsonInputRequest = objectMapper.writeValueAsString(Comparisonpayload);

        ComparisonResquest requests = new ComparisonResquest();
        TritonRequest requestBody = new TritonRequest();
        requestBody.setName("COS START");
        requestBody.setShape(List.of(1, 1));
        requestBody.setDatatype("BYTES");
        requestBody.setData(Collections.singletonList(jsonInputRequest));

        //  requestBody.setData(Collections.singletonList(jsonNodeRequest));


        // requestBody.setData(Collections.singletonList(Comparisonpayload));

        TritonInputRequest tritonInputRequest=new TritonInputRequest();
        tritonInputRequest.setInputs(Collections.singletonList(requestBody));

        String jsonRequest = objectMapper.writeValueAsString(tritonInputRequest);

        if (result.getActualValue() != null) {
            final Request request = new Request.Builder().url(endpoint)
                    .post(RequestBody.create(jsonInputRequest.toString(), MediaTypeJSON)).build();
            log.info("master data comparison reqest body {}",request);
            try (Response response = httpclient.newCall(request).execute()) {
                log.info("master data comparison response body {}",response.body());
                String responseBody = Objects.requireNonNull(response.body()).string();
                if (response.isSuccessful()) {

                    List<IntelliMatchCopro> output = mapper.readValue(responseBody, new TypeReference<>() {
                    });
                    double matchPercent = output.get(0) != null ? Math.round(output.get(0).getSimilarityPercent() * 100.0) / 100.0 : 0.0;
                    ObjectMapper objectMappers = new ObjectMapper();
                    ComparisonResponse Response = objectMappers.readValue(responseBody, ComparisonResponse.class);
                    if (Response.getOutputs() != null && !Response.getOutputs().isEmpty()) {
                        Response.getOutputs().forEach(o -> {
                            o.getData().forEach(ComparisonDataItem -> {
                                parentObj.add(
                                        MasterDataOutputTable
                                                .builder()
                                                .originId(originId)
                                                .eocIdentifier(eocIdentifier)
                                                .paperNo(paperNo)
                                                .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                                                .extractedValue(extractedValue)
                                                .actualValue(actualValue)
                                                .intelliMatch(ComparisonDataItem.getSimilarityPercent())
                                                .status("COMPLETED")
                                                .stage("MASTER-DATA-COMPARISON")
                                                .message("Master data comparison macro completed")
                                                .rootPipelineId(result.getRootPipelineId())
                                                .modelName(Response.getModelName())
                                                .modelVersion(Response.getModelVersion())
                                                .build());

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
                                    .rootPipelineId(result.getRootPipelineId())
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
                                .rootPipelineId(result.getRootPipelineId())
                                .build()
                );

                log.error(aMarker, "Exception occurred in copro api for master data comparison - {} ", ExceptionUtil.toString(exception));
                HandymanException handymanException = new HandymanException(exception);
                HandymanException.insertException("Paper classification (hw-detection) consumer failed for originId "+ originId, handymanException, this.action);
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
}


