package in.handyman.raven.lib.model.visualextraction.coproprocessor;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.CoproProcessor;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.net.URL;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.HashMap;
import java.util.Map;

public class VisualExtractionConsumerProcess
        implements CoproProcessor.ConsumerProcess<VisualExtractionInputTable, VisualExtractionOutputTable> {

    private final Logger log;
    private final Marker aMarker;
    public final ActionExecutionAudit action;
    private final ObjectMapper mapper = new ObjectMapper();
    private static final MediaType mediaTypeJSON = MediaType.parse("application/json; charset=utf-8");

    final OkHttpClient httpclient = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.MINUTES)
            .writeTimeout(10, TimeUnit.MINUTES)
            .readTimeout(10, TimeUnit.MINUTES)
            .build();

    public VisualExtractionConsumerProcess(Logger log, Marker aMarker, ActionExecutionAudit action) {
        this.log = log;
        this.aMarker = aMarker;
        this.action = action;
    }

    @Override
    public List<VisualExtractionOutputTable> process(URL endpoint, VisualExtractionInputTable entity) throws Exception {
        log.info(aMarker, "VisualExtraction consumer process started with endpoint {} and entity {}", endpoint, entity);

        List<VisualExtractionOutputTable> parentObj = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();

        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("origin_id", entity.getOriginId());
        requestMap.put("tenant_id", String.valueOf(entity.getTenantId()));
        requestMap.put("input_file_path", entity.getFilePath());

        String jsonInputRequest = objectMapper.writeValueAsString(requestMap);

        Request request = new Request.Builder().url(endpoint)
                .post(RequestBody.create(jsonInputRequest, mediaTypeJSON)).build();

        try (Response response = httpclient.newCall(request).execute()) {
            if (response.isSuccessful()) {
                String responseBody = Objects.requireNonNull(response.body()).string();
                log.info(aMarker, "VisualExtraction consumer process response successful");

                parentObj.add(VisualExtractionOutputTable.builder()
                        .originId(entity.getOriginId())
                        .paperNo(entity.getPaperNo())
                        .tenantId(entity.getTenantId())
                        .groupId(entity.getGroupId())
                        .batchId(entity.getBatchId())
                        .rootPipelineId(entity.getRootPipelineId())
                        .processId(String.valueOf(action.getProcessId()))
                        .documentType(entity.getDocumentType())
                        .modelName(entity.getModelName())
                        .status("COMPLETED")
                        .stage("VISUAL_EXTRACTION")
                        .response(responseBody)
                        .request(jsonInputRequest)
                        .endpoint(endpoint.toString())
                        .build());
            } else {
                log.error(aMarker, "Error in response {}", response.message());
                parentObj.add(VisualExtractionOutputTable.builder()
                        .originId(entity.getOriginId())
                        .paperNo(entity.getPaperNo())
                        .tenantId(entity.getTenantId())
                        .groupId(entity.getGroupId())
                        .batchId(entity.getBatchId())
                        .rootPipelineId(entity.getRootPipelineId())
                        .processId(String.valueOf(action.getProcessId()))
                        .status("FAILED")
                        .stage("VISUAL_EXTRACTION")
                        .errorMessage(response.message())
                        .request(jsonInputRequest)
                        .endpoint(endpoint.toString())
                        .build());
            }
        } catch (Exception exception) {
            log.error(aMarker, "Exception occurred", exception);
            parentObj.add(VisualExtractionOutputTable.builder()
                    .originId(entity.getOriginId())
                    .paperNo(entity.getPaperNo())
                    .tenantId(entity.getTenantId())
                    .groupId(entity.getGroupId())
                    .batchId(entity.getBatchId())
                    .rootPipelineId(entity.getRootPipelineId())
                    .processId(String.valueOf(action.getProcessId()))
                    .status("FAILED")
                    .stage("VISUAL_EXTRACTION")
                    .errorMessage(exception.getMessage())
                    .request(jsonInputRequest)
                    .endpoint(endpoint.toString())
                    .build());
            HandymanException handymanException = new HandymanException(exception);
            HandymanException.insertException("VisualExtraction consumer failed", handymanException, action);
        }

        return parentObj;
    }
}
