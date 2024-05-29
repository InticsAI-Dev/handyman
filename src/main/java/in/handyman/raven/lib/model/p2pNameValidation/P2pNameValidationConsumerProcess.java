package in.handyman.raven.lib.model.p2pNameValidation;

import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.CoproProcessor;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class P2pNameValidationConsumerProcess implements CoproProcessor.ConsumerProcess<P2PNameValidationInputTable, P2PNameValidationOutputTable> {
    private final Logger log;
    private final Marker aMarker;
    private final ActionExecutionAudit action;

    public P2pNameValidationConsumerProcess(ActionExecutionAudit actionExecutionAudit, Logger log, Marker aMarker) {
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

            final String p2pFullName = cleanAndExtractAlphabets(entity.getP2pFullName());
            final String p2pFirstName = cleanAndExtractAlphabets(entity.getP2pFirstName());
            final String p2pLastName = cleanAndExtractAlphabets(entity.getP2pLastName());

            final String fullNameBBox = entity.getP2pFullNameBBox();
            final String firstNameBBox = entity.getP2pFirstNameBBox();
            final String lastNameBBox = entity.getP2pLastNameBBox();

            final Double finalConfidenceScore = getFinalConfidenceScore(entity.getP2pFullNameConfidenceScore(), entity.getP2pFirstNameConfidenceScore(), entity.getP2pLastNameConfidenceScore());
            final Double finalMaxScore = getFinalConfidenceScore(entity.getP2pFullNameMaximumScore(), entity.getP2pFirstNameMaximumScore(), entity.getP2pLastNameMaximumScore());

            Integer finalQuestionId = getFinalQuestionId(entity);
            Integer finalSynonymId = getFinalSynonymId(entity);

            String finalConcatenatedName;
            String finalBBox;
            if (p2pFirstName.equalsIgnoreCase(p2pLastName)) {
                finalConcatenatedName = p2pFirstName;
                finalBBox = firstNameBBox;
            } else if (p2pFirstName.contains(p2pLastName)) {
                finalConcatenatedName = p2pFirstName;
                finalBBox = firstNameBBox;
            } else if (p2pLastName.contains(p2pFirstName)) {
                finalConcatenatedName = p2pLastName;
                finalBBox = lastNameBBox;
            } else {
                finalConcatenatedName = p2pFirstName + " " + p2pLastName;
                finalBBox = firstNameBBox;
            }
            log.info(aMarker, "Concatenated Name before Full name validation {}", finalConcatenatedName);
            boolean isContains = finalConcatenatedName.contains(p2pFullName) && finalConcatenatedName.equalsIgnoreCase(p2pFullName);
            if (!p2pFullName.isBlank() && !p2pFullName.isEmpty() && isContains) {
                finalConcatenatedName = p2pFullName;
                finalBBox = fullNameBBox;
            }

            System.out.println(finalConcatenatedName);
            log.info(aMarker, "Concatenated Name after Full name validation {}", finalConcatenatedName);

            p2PNameValidationOutputTableArrayList.add(P2PNameValidationOutputTable.builder()
                    .p2pConcatenatedName(finalConcatenatedName)
                    .groupId(entity.getGroupId())
                    .rootPipelineId(entity.getRootPipelineId())
                    .paperNo(entity.getPaperNo())
                    .originId(entity.getOriginId())
                    .p2pBBox(finalBBox)
                    .p2pConfidenceScore(finalConfidenceScore)
                    .p2pMaximumScore(finalMaxScore)
                    .tenantId(entity.getTenantId())
                    .sorItemName(entity.getSorItemName())
                    .synonymId(finalSynonymId)
                    .questionId(finalQuestionId)
                    .modelRegistry(entity.getModelRegistry())
                    .build());
            log.info(aMarker, "p2PNameValidationOutputTableArrayList {}", p2PNameValidationOutputTableArrayList);
        } catch (Exception e) {
            log.error(aMarker, "error in execute method for p2p name concatenation ", e);
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("Error in execute method for p2p name concatenation ", handymanException, this.action);
        }
        return p2PNameValidationOutputTableArrayList;
    }

    private static @NotNull Double getFinalConfidenceScore(Double entity, Double entity1, Double entity2) {
        final Double fullNameScore = entity != null ? entity : 0;
        final Double p2pFistNameScore = entity1 != null ? entity1 : 0;
        final Double p2pLastNameScore = entity2 != null ? entity2 : 0;
        return (fullNameScore + p2pFistNameScore + p2pLastNameScore) / 3;
    }

    private static Integer getFinalSynonymId(P2PNameValidationInputTable entity) {
        Integer finalSynonymId;
        if (entity.getSynonymId() != null) {
            finalSynonymId = entity.getSynonymId();
        } else {
            if (entity.getFirstNameQuestionId() != null) {
                finalSynonymId = entity.getFirstNameSynonymId();
            } else {
                finalSynonymId = entity.getLastNameSynonymId();
            }
        }
        return finalSynonymId;
    }

    private static Integer getFinalQuestionId(P2PNameValidationInputTable entity) {
        Integer finalQuestionId;
        if (entity.getQuestionId() != null) {
            finalQuestionId = entity.getQuestionId();
        } else {
            if (entity.getFirstNameQuestionId() != null) {
                finalQuestionId = entity.getFirstNameQuestionId();
            } else {
                finalQuestionId = entity.getLastNameQuestionId();
            }
        }
        return finalQuestionId;
    }

    private String cleanAndExtractAlphabets(String value) {
        return value.replaceAll("[^a-zA-Z]+", "");
    }
}
