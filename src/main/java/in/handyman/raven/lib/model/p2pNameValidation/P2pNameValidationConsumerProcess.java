package in.handyman.raven.lib.model.p2pNameValidation;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.CoproProcessor;
import in.handyman.raven.lib.P2pNameValidationAction;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class P2pNameValidationConsumerProcess implements CoproProcessor.ConsumerProcess<P2PNameValidationInputTable, P2PNameValidationOutputTable> {
    private final Logger log;
    private final Marker aMarker;
    private final ActionExecutionAudit action;

    public P2pNameValidationConsumerProcess(ActionExecutionAudit actionExecutionAudit,  Logger log, Marker aMarker, P2pNameValidationAction p2pNameValidationAction) {
        this.log = log;
        this.aMarker = aMarker;
        this.action = actionExecutionAudit;
    }

    @Override
    public List<P2PNameValidationOutputTable> process(URL endpoint, P2PNameValidationInputTable entity) throws Exception {
        log.info("p2p input entity: {} ", entity);
        final List<P2PNameValidationOutputTable> p2PNameValidationOutputTableArrayList = new ArrayList<>();
        try {
            log.info(aMarker, "mapping inputs for the results {}", entity);
            final String p2pBboxFinal = entity.getP2pFirstNameBbox();
            final String p2pFullName = entity.getP2pFullName();

            final String p2pFirstName = cleanAndExtractAlphabets(entity.getP2pFirstName());
            final String p2pLastName = cleanAndExtractAlphabets(entity.getP2pLastName());

            final Double p2pFistNameCfScore = entity.getP2pFirstNameConfidenceScore() != null ? entity.getP2pFirstNameConfidenceScore() : 0;
            final Double p2pLastNameCfScore = entity.getP2pLastNameConfidenceScore() != null ? entity.getP2pLastNameConfidenceScore() : 0;
            final Double finalConfidenceScore = (p2pFistNameCfScore + p2pLastNameCfScore) / 2;

            final Double p2pFirstNameMaxScore = entity.getP2pFirstNameMaximumScore() != null ? entity.getP2pFirstNameMaximumScore() : 0;
            final Double p2pLastNameMaxScore = entity.getP2pLastNameMaximumScore() != null ? entity.getP2pLastNameMaximumScore() : 0;
            final Double finalMaxScore = (p2pFirstNameMaxScore + p2pLastNameMaxScore) / 2;

            final Double p2pFirstNameFilterScore = entity.getP2pFirstNameFilterScore() != null ? entity.getP2pFirstNameFilterScore() : 0;
            final Double p2pLastNameFilterScore = entity.getP2pLastNameFilterScore() != null ? entity.getP2pLastNameFilterScore() : 0;
            final Double finalFilterScore = (p2pFirstNameFilterScore + p2pLastNameFilterScore) / 2;

            String finalConcatenatedName;
            if (p2pFirstName.equalsIgnoreCase(p2pLastName)) {
                finalConcatenatedName = p2pFirstName;
            } else if (p2pFirstName.contains(p2pLastName)) {
                finalConcatenatedName = p2pFirstName;
            } else if (p2pLastName.contains(p2pFirstName)) {
                finalConcatenatedName = p2pLastName;
            } else {
                finalConcatenatedName = p2pFirstName + " " + p2pLastName;
            }

            log.info(aMarker, "Concatenated Name before Full name validation {}", finalConcatenatedName);
            boolean isContains = finalConcatenatedName.contains(p2pFullName);
            if (!p2pFullName.isBlank() && !p2pFullName.isEmpty() && !isContains) {
                finalConcatenatedName = p2pFullName;
            }
            log.info(aMarker, "Concatenated Name after Full name validation {}", finalConcatenatedName);

            p2PNameValidationOutputTableArrayList.add(P2PNameValidationOutputTable.builder()
                    .p2pConcatenatedName(finalConcatenatedName)
                    .groupId(entity.getGroupId())
                    .rootPipelineId(entity.getRootPipelineId())
                    .paperNo(entity.getPaperNo())
                    .originId(entity.getOriginId())
                    .p2pBbox(p2pBboxFinal)
                    .p2pConfidenceScore(finalConfidenceScore)
                    .p2pFilterScore(finalFilterScore)
                    .p2pMaximumScore(finalMaxScore)
                    .tenantId(entity.getTenantId())
                    .sorItemName(entity.getSorItemName())
                    .batchId(entity.getBatchId())
                    .build());
            log.info(aMarker, "p2PNameValidationOutputTableArrayList {}", p2PNameValidationOutputTableArrayList);
        } catch (Exception e) {
            log.error(aMarker, "error in execute method for p2p name concatenation ", e);
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("Error in execute method for p2p name concatenation ", handymanException, this.action);
        }

        return p2PNameValidationOutputTableArrayList;
    }

    private String cleanAndExtractAlphabets(String value) {
            return value.replaceAll("[^a-zA-Z]+", "");
    }
}
