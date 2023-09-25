package in.handyman.raven.lib.model.paperItemizer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.CoproProcessor;
import in.handyman.raven.lib.model.triton.TritonInputRequest;
import in.handyman.raven.lib.model.triton.TritonRequest;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.net.URL;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.checkerframework.checker.units.UnitsTools.s;

public class PaperItemizerConsumerProcess implements CoproProcessor.ConsumerProcess<PaperItemizerInputTable, PaperItemizerOutputTable> {

    private final Logger log;
    private final Marker aMarker;
    private final ObjectMapper mapper = new ObjectMapper();
    private static final MediaType MediaTypeJSON = MediaType
            .parse("application/json; charset=utf-8");
    private final String outputDir;

    public final ActionExecutionAudit action;
    final OkHttpClient httpclient = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.MINUTES)
            .writeTimeout(10, TimeUnit.MINUTES)
            .readTimeout(10, TimeUnit.MINUTES)
            .build();

    public PaperItemizerConsumerProcess(Logger log, Marker aMarker, String outputDir, ActionExecutionAudit action) {
        this.log = log;
        this.aMarker = aMarker;
        this.outputDir = outputDir;
        this.action = action;
    }

    @Override
    public List<PaperItemizerOutputTable> process(URL endpoint, PaperItemizerInputTable entity) throws Exception {
        log.info(aMarker, "coproProcessor consumer process started with endpoint {} and entity {}", endpoint, entity);
        List<PaperItemizerOutputTable> parentObj = new ArrayList<>();
        String inputFilePath = entity.getFilePath();
        Long rootPipelineId = entity.getRootPipelineId();
        final String paperItemizerProcessName = "PAPER_ITEMIZER";
        Long actionId = action.getActionId();
        ObjectMapper objectMapper = new ObjectMapper();
//payload
        PaperItemizerData paperitemizerData = new PaperItemizerData();
        paperitemizerData.setRootPipelineId(rootPipelineId);
        paperitemizerData.setProcess(paperItemizerProcessName);
        paperitemizerData.setInputFilePath(inputFilePath);
        paperitemizerData.setOutputDir(outputDir);
        paperitemizerData.setActionId(actionId);
        String jsonInputRequest = objectMapper.writeValueAsString(paperitemizerData);

        TritonRequest requestBody = new TritonRequest();
        requestBody.setName("PAPER ITERATOR START");
        requestBody.setShape(List.of(1, 1));
        requestBody.setDatatype("BYTES");
        requestBody.setData(Collections.singletonList(jsonInputRequest));

        TritonInputRequest tritonInputRequest = new TritonInputRequest();
        tritonInputRequest.setInputs(Collections.singletonList(requestBody));


        String jsonRequest = objectMapper.writeValueAsString(tritonInputRequest);

        log.info(aMarker, "coproProcessor mapper object node {}", jsonRequest);
        Request request = new Request.Builder().url(endpoint)
                .post(RequestBody.create(jsonRequest, MediaTypeJSON)).build();

        if (log.isInfoEnabled()) {
            log.info(aMarker, "Request has been build with the parameters \n URI : {}, with inputFilePath {} and outputDir {}", endpoint, inputFilePath, outputDir);
        }
        AtomicInteger atomicInteger = new AtomicInteger();
        String originId = entity.getOriginId();
        Integer groupId = entity.getGroupId();
        String templateId = entity.getTemplateId();
        Long tenantId = entity.getTenantId();
        Long processId = entity.getProcessId();


        try (Response response = httpclient.newCall(request).execute()) {

            if (log.isInfoEnabled()) {
                log.info(aMarker, "coproProcessor consumer process response with status{}, and message as {}, ", response.isSuccessful(), response.message());
            }
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                ObjectMapper objectMappers = new ObjectMapper();
                PaperItemizerResponse paperItemizerResponse = objectMappers.readValue(responseBody, PaperItemizerResponse.class);
                if (paperItemizerResponse.getOutputs() != null && !paperItemizerResponse.getOutputs().isEmpty()) {
                    paperItemizerResponse.getOutputs().forEach(o -> o.getData().forEach(paperItemizerDataItem ->
                            {
                                try {
                                    PaperItemizerDataItem paperItemizeOutputData = objectMappers.readValue(paperItemizerDataItem, PaperItemizerDataItem.class);
                                    paperItemizeOutputData.getItemizedPapers().forEach(itemizerPapers -> {
                                        parentObj.add(
                                        PaperItemizerOutputTable
                                                .builder()
                                                .processedFilePath(itemizerPapers)
                                                .originId(Optional.ofNullable(originId).map(String::valueOf).orElse(null))
                                                .groupId(groupId)
                                                .templateId(templateId)
                                                .tenantId(tenantId)
                                                .processId(processId)
                                                .paperNo(atomicInteger.incrementAndGet())
                                                .status("COMPLETED")
                                                .stage(paperItemizerProcessName)
                                                .message("Paper Itemizer macro completed")
                                                .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                                                .rootPipelineId(rootPipelineId)
                                                .modelName(paperItemizerResponse.getModelName())
                                                .modelVersion(paperItemizerResponse.getModelVersion())
                                                .build());
                                    });

                                } catch (JsonProcessingException e) {
                                    parentObj.add(
                                            PaperItemizerOutputTable
                                                    .builder()
                                                    .originId(Optional.ofNullable(originId).map(String::valueOf).orElse(null))
                                                    .groupId(groupId)
                                                    .processId(processId)
                                                    .templateId(templateId)
                                                    .tenantId(tenantId)
                                                    .paperNo(atomicInteger.incrementAndGet())
                                                    .status("FAILED")
                                                    .stage(paperItemizerProcessName)
                                                    .message(e.getMessage())
                                                    .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                                                    .rootPipelineId(rootPipelineId)
                                                    .build());
                                    HandymanException handymanException = new HandymanException(e);
                                    HandymanException.insertException("Paper Itemizer  consumer failed for originId " + originId, handymanException, this.action);
                                    log.error(aMarker, "The Exception occurred in request {}", request, e);
                                }


                }
                    ));
                }

            } else {
                parentObj.add(
                        PaperItemizerOutputTable
                                .builder()
                                .originId(Optional.ofNullable(originId).map(String::valueOf).orElse(null))
                                .groupId(groupId)
                                .processId(processId)
                                .templateId(templateId)
                                .tenantId(tenantId)
                                .paperNo(atomicInteger.incrementAndGet())
                                .status("FAILED")
                                .stage(paperItemizerProcessName)
                                .message(response.message())
                                .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                                .rootPipelineId(rootPipelineId)
                                .build());
                log.error(aMarker, "Error in response {}", response.message());
            }

        } catch (Exception exception) {
            parentObj.add(
                    PaperItemizerOutputTable
                            .builder()
                            .originId(Optional.ofNullable(originId).map(String::valueOf).orElse(null))
                            .groupId(groupId)
                            .processId(processId)
                            .templateId(templateId)
                            .tenantId(tenantId)
                            .paperNo(atomicInteger.incrementAndGet())
                            .status("FAILED")
                            .stage(paperItemizerProcessName)
                            .message(exception.getMessage())
                            .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                            .rootPipelineId(rootPipelineId)
                            .build());
            HandymanException handymanException = new HandymanException(exception);
            HandymanException.insertException("Paper Itemizer  consumer failed for originId " + originId, handymanException, this.action);
            log.error(aMarker, "The Exception occurred in request {}", request, exception);
        }
        atomicInteger.set(0);
        log.info(aMarker, "coproProcessor consumer process with output entity {}", parentObj);
        return parentObj;
    }
}