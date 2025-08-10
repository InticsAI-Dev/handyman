package in.handyman.raven.lib.model.deepSiftSearch;

import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.CoproProcessor;
import in.handyman.raven.lib.model.triton.ConsumerProcessApiStatus;
import in.handyman.raven.util.ExceptionUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.Marker;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import java.sql.Timestamp;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@Builder
public class DeepSiftSearchConsumerProcess implements CoproProcessor.ConsumerProcess<DeepSiftSearchInputTable, DeepSiftSearchOutputTable> {

    private final Logger log;
    private final Marker marker;
    private final ActionExecutionAudit action;

    @Override
    public List<DeepSiftSearchOutputTable> process(URL endpoint, DeepSiftSearchInputTable entity) {
        List<DeepSiftSearchOutputTable> outputRecords = new ArrayList<>();
        log.info(marker, "Starting process for sorItemId: {}", entity.getSorItemId());

        try {
            if (entity.getSorItemId() == null || entity.getSorItemName() == null || entity.getSearchName() == null) {
                log.error(marker, "Invalid input for sorItemId: {} - missing required fields", entity.getSorItemId());
                HandymanException handymanException = new HandymanException("Missing required fields: sorItemId, sorItemName, or searchType");
                HandymanException.insertException("Deep sift search failed for sorItemId: " + entity.getSorItemId(), handymanException, action);
                outputRecords.add(buildOutputTable(entity, ConsumerProcessApiStatus.FAILED.getStatusDescription(),
                        "Missing required fields"));
                return outputRecords;
            }

            String searchType = entity.getSearchName().trim().toLowerCase();
            String extractedText = entity.getExtractedText() != null ? entity.getExtractedText().toLowerCase() : "";

            List<String> keywordList = new ArrayList<>();
            if (entity.getKeywords() != null && !entity.getKeywords().trim().isEmpty()) {
                keywordList = Arrays.stream(entity.getKeywords().split("\\s*,\\s*"))
                        .filter(k -> !k.isEmpty())
                        .collect(Collectors.toList());
            }

            List<String> matchedKeywords = new ArrayList<>();
            boolean matchFound = false;

            log.debug(marker, "Processing searchType: {} for sorItemId: {}, total keywords: {}",
                    searchType, entity.getSorItemId(), keywordList.size());

            if ("exact".equals(searchType)) {
                for (String keyword : keywordList) {
                    String pattern = "\\b" + Pattern.quote(keyword) + "\\b";
                    if (Pattern.compile(pattern, Pattern.CASE_INSENSITIVE)
                            .matcher(extractedText)
                            .find()) {
                        matchedKeywords.add(keyword);
                    }
                }
            } else if ("contains".equals(searchType)) {
                for (String kw : keywordList) {
                    if (extractedText.toLowerCase().contains(kw.toLowerCase())) {
                        matchedKeywords.add(kw);
                    }
                }
            } else {
                log.error(marker, "Invalid searchType: {} for sorItemId: {}", searchType, entity.getSorItemId());
                HandymanException handymanException = new HandymanException("Invalid searchType: " + searchType);
                HandymanException.insertException("Deep sift search failed for sorItemId: " + entity.getSorItemId(),
                        handymanException, action);
                outputRecords.add(buildOutputTable(entity, ConsumerProcessApiStatus.FAILED.getStatusDescription(),
                        "Invalid searchType"));
                return outputRecords;
            }

            if (matchFound) {
                log.info(marker, "Match found for sorItemId: {}, searchType: {}, matched keyword count: {}",
                        entity.getSorItemId(), searchType, matchedKeywords.size());
                outputRecords.add(DeepSiftSearchOutputTable.builder()
                        .sorItemId(entity.getSorItemId())
                        .sorItemName(entity.getSorItemName())
                        .sorContainerId(entity.getSorContainerId())
                        .sorContainerName(entity.getSorContainerName())
                        .sourceDocumentType(entity.getSourceDocumentType())
                        .originId(entity.getOriginId())
                        .rootPipelineId(entity.getRootPipelineId())
                        .searchId(entity.getSearchId())
                        .searchName(entity.getSearchName())
                        .batchId(entity.getBatchId())
                        .tenantId(entity.getTenantId())
                        .createdOn(entity.getCreatedOn())
                        .createdBy("-1")
                        .searchOutput(matchedKeywords)
                        .paperNo(entity.getPaperNo())
                        .groupId(entity.getGroupId())
                        .build());
            } else {
                log.info(marker, "No keyword match found for sorItemId: {}", entity.getSorItemId());
                outputRecords.add(DeepSiftSearchOutputTable.builder()
                        .sorItemId(entity.getSorItemId())
                        .sorItemName(entity.getSorItemName())
                        .sorContainerId(entity.getSorContainerId())
                        .sorContainerName(entity.getSorContainerName())
                        .sourceDocumentType(entity.getSourceDocumentType())
                        .originId(entity.getOriginId())
                        .rootPipelineId(entity.getRootPipelineId())
                        .searchId(entity.getSearchId())
                        .searchName(entity.getSearchName())
                        .batchId(entity.getBatchId())
                        .tenantId(entity.getTenantId())
                        .createdOn(entity.getCreatedOn())
                        .createdBy("-1")
                        .searchOutput(matchedKeywords)
                        .paperNo(entity.getPaperNo())
                        .groupId(entity.getGroupId())
                        .build());
            }

        } catch (Exception e) {
            log.error(marker, "Exception processing sorItemId: {}", entity.getSorItemId(), e);
            HandymanException handymanException = new HandymanException("Deep sift search failed for sorItemId: " +
                    entity.getSorItemId(), e);
            HandymanException.insertException("Deep sift search failed for sorItemId: " + entity.getSorItemId(),
                    handymanException, action);
            outputRecords.add(buildOutputTable(entity, ConsumerProcessApiStatus.FAILED.getStatusDescription(),
                    ExceptionUtil.toString(e)));
        }

        log.info(marker, "Completed process for sorItemId: {}, output records: {}",
                entity.getSorItemId(), outputRecords.size());
        return outputRecords;
    }

    private DeepSiftSearchOutputTable buildOutputTable(DeepSiftSearchInputTable entity, String status, String message) {
        return buildOutputTable(entity, status, message, new ArrayList<>());
    }

    private DeepSiftSearchOutputTable buildOutputTable(DeepSiftSearchInputTable entity, String status, String message, List<String> matchedKeywords) {
        log.debug(marker, "Building output table for sorItemId: {} with status: {}", entity.getSorItemId(), status);
        return DeepSiftSearchOutputTable.builder()
                .sorItemId(entity.getSorItemId())
                .sorItemName(entity.getSorItemName())
                .sorContainerId(entity.getSorContainerId())
                .sorContainerName(entity.getSorContainerName())
                .sourceDocumentType(entity.getSourceDocumentType())
                .originId(entity.getOriginId())
                .rootPipelineId(entity.getRootPipelineId())
                .searchId(entity.getSearchId())
                .searchName(entity.getSearchName())
                .batchId(entity.getBatchId())
                .tenantId(entity.getTenantId())
                .createdOn(entity.getCreatedOn())
                .createdBy("-1")
                .searchOutput(matchedKeywords)
                .paperNo(entity.getPaperNo())
                .groupId(entity.getGroupId())
                .build();
    }
}
