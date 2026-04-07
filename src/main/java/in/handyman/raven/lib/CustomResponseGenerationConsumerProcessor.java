package in.handyman.raven.lib;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import in.handyman.raven.lib.custom.outbound.dao.PredictionDTO;
import in.handyman.raven.lib.custom.outbound.mapper.MedicalPayloadGeneration;
import in.handyman.raven.lib.custom.outbound.model.CustomResponseOutputTable;
import in.handyman.raven.lib.custom.outbound.model.MedicalOutboundResponse;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

public class CustomResponseGenerationConsumerProcessor implements CoproProcessor.ConsumerProcess<PredictionDTO, CustomResponseOutputTable> {
    private final Logger log;
    private final Marker aMarker;
    private final ActionExecutionAudit action;
    private final MedicalPayloadGeneration medicalPayloadGeneration;
    private final ObjectMapper objectMapper;

    public CustomResponseGenerationConsumerProcessor(final Logger log, final Marker aMarker, final ActionExecutionAudit action) {
        this.log = log;
        this.aMarker = aMarker;
        this.action = action;
        this.medicalPayloadGeneration = new MedicalPayloadGeneration(log);
        this.objectMapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .serializationInclusion(JsonInclude.Include.NON_NULL)
                .build();
    }

    @Override
    public List<CustomResponseOutputTable> process(URL endpoint, PredictionDTO entity) throws Exception {
        if (entity == null) {
            return Collections.emptyList();
        }

        List<PredictionDTO> predictionDTOList = Collections.singletonList(entity);
        String originId = entity.getOriginId();
        String metadata = entity.getMetadataJson();
        Long groupId = entity.getGroupId();
        Long tenantId = entity.getTenantId();
        String batchId = entity.getBatchId();
        String rootPipelineId = entity.getRootPipelineId();
        Long parsedRootPipelineId = rootPipelineId != null ? Long.valueOf(rootPipelineId) : 0L;
        Integer parsedProcessId = rootPipelineId != null ? Integer.valueOf(rootPipelineId) : 0;

        MedicalOutboundResponse medicalOutboundResponse = medicalPayloadGeneration.buildMedicalOutboundResponse(
                predictionDTOList,
                action.getContext(),
                metadata
        );
        String customResponseStr = objectMapper.writeValueAsString(medicalOutboundResponse);

        CustomResponseOutputTable out = CustomResponseOutputTable.builder()
                .processId(parsedProcessId)
                .groupId(groupId)
                .customResponse(customResponseStr)
                .originId(originId)
                .tenantId(tenantId)
                .rootPipelineId(parsedRootPipelineId)
                .status("SUCCESS")
                .stage("Custom Response Generation")
                .message("Custom Response generated successfully")
                .triggeredUrl("")
                .feature("Custom")
                .batchId(batchId)
                .inboundTransactionId(medicalOutboundResponse.getInboundTransactionId())
                .createdOn(LocalDateTime.now())
                .lastUpdatedOn(LocalDateTime.now())
                .build();

        log.debug(aMarker, "Custom response generated for originId {}", originId);
        return Collections.singletonList(out);
    }
}
