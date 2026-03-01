package in.handyman.raven.lib;

import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.services.sor.transform.OcrTextComparisonInput;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.time.LocalDateTime;
import java.util.List;

import static in.handyman.raven.core.enums.DatabaseConstants.DB_INSERT_WRITE_BATCH_SIZE;

public class OcrDatabaseHandler {
    private final Logger log;
    private final Marker aMarker;
    private final ActionExecutionAudit action;
    private final int BATCH_INSERT_SIZE;

    public OcrDatabaseHandler(Logger log, Marker aMarker, ActionExecutionAudit action) {
        this.log = log;
        this.aMarker = aMarker;
        this.action = action;
        this.BATCH_INSERT_SIZE = Integer.parseInt(action.getContext().getOrDefault(DB_INSERT_WRITE_BATCH_SIZE, "500"));
    }

    public void batchInsertResults(Jdbi jdbi, String outputTable, List<OcrTextComparisonInput> results,
            LocalDateTime executionStartTime) {
        log.info(aMarker, "Starting batch insert of {} records into {}", results.size(), outputTable);

        int totalInserted = 0;
        int batchCount = 0;

        for (int i = 0; i < results.size(); i += BATCH_INSERT_SIZE) {
            int endIndex = Math.min(i + BATCH_INSERT_SIZE, results.size());
            List<OcrTextComparisonInput> batch = results.subList(i, endIndex);
            batchCount++;

            try {
                jdbi.useHandle(handle -> {
                    PreparedBatch preparedBatch = handle.prepareBatch(
                            "INSERT INTO " + outputTable + " (" +
                                    "transaction_id, created_on, created_user_id, last_updated_on, last_updated_user_id, "
                                    +
                                    "root_pipeline_id, tenant_id, document_id, group_id, batch_id, origin_id, paper_no, "
                                    +
                                    "truth_id, status, stage, message, version, extracted_image_unit, image_dpi, " +
                                    "image_height, image_width, section_priority_after_filter, sor_container_id, " +
                                    "sor_container_name, sor_container_instance, sor_item_name, sor_item_id, " +
                                    "sor_item_attribution_id, model_id, model_info, model_registry, model_registry_id, "
                                    +
                                    "answer, vqa_score, score, b_box, label, section_alias, synonym_id, sor_synonym, " +
                                    "question_id, sor_question, weight, category, line_item_type, " +
                                    "is_multi_entity_enabled, encryption_policy, is_encrypted, ocr_field_id, " +
                                    "is_ocr_field_comparable, extracted_text, threshold, best_match, best_score, " +
                                    "regex_pattern, candidates_list, mismatch_count, match_status, allowed_adapter" +
                                    ") VALUES (" +
                                    ":transactionId, :createdOn, :createdUserId, :lastUpdatedOn, :lastUpdatedUserId, " +
                                    ":rootPipelineId, :tenantId, :documentId, :groupId, :batchId, :originId, :paperNo, "
                                    +
                                    ":truthId, :status, :stage, :message, :version, :extractedImageUnit, :imageDpi, " +
                                    ":imageHeight, :imageWidth, :sectionPriorityAfterFilter, :sorContainerId, " +
                                    ":sorContainerName, :sorContainerInstance, :sorItemName, :sorItemId, " +
                                    ":sorItemAttributionId, :modelId, :modelInfo, :modelRegistry, :modelRegistryId, " +
                                    ":answer, :vqaScore, :score, :bBox, :label, :sectionAlias, :synonymId, :sorSynonym, "
                                    +
                                    ":questionId, :sorQuestion, :weight, :category, :lineItemType, " +
                                    ":isMultiEntityEnabled, :encryptionPolicy, :isEncrypted, :ocrFieldId, " +
                                    ":isOcrFieldComparable, :extractedText, :threshold, :bestMatch, :bestScore, " +
                                    ":regexPattern, :candidatesList, :mismatchCount, :matchStatus, :allowedAdapter" +
                                    ")");

                    LocalDateTime now = LocalDateTime.now();

                    for (OcrTextComparisonInput r : batch) {
                        Long tenantId = r.getTenantId() != null ? r.getTenantId() : 1L;

                        preparedBatch
                                .bind("transactionId", r.getTransactionId())
                                .bind("createdOn", executionStartTime)
                                .bind("createdUserId", tenantId)
                                .bind("lastUpdatedOn", now)
                                .bind("lastUpdatedUserId", tenantId)
                                .bind("rootPipelineId", r.getRootPipelineId())
                                .bind("tenantId", tenantId)
                                .bind("documentId", r.getDocumentId())
                                .bind("groupId", r.getGroupId())
                                .bind("batchId", r.getBatchId())
                                .bind("originId", r.getOriginId())
                                .bind("paperNo", r.getPaperNo())
                                .bind("truthId", r.getTruthId())
                                .bind("status", r.getStatus())
                                .bind("stage", r.getStage())
                                .bind("message", r.getMessage())
                                .bind("version", r.getVersion())
                                .bind("extractedImageUnit", r.getExtractedImageUnit())
                                .bind("imageDpi", r.getImageDpi())
                                .bind("imageHeight", r.getImageHeight())
                                .bind("imageWidth", r.getImageWidth())
                                .bind("sectionPriorityAfterFilter", r.getSectionPriorityAfterFilter())
                                .bind("sorContainerId", r.getSorContainerId())
                                .bind("sorContainerName", r.getSorContainerName())
                                .bind("sorContainerInstance", r.getSorContainerInstance())
                                .bind("sorItemName", r.getSorItemName())
                                .bind("sorItemId", r.getSorItemId())
                                .bind("sorItemAttributionId", r.getSorItemAttributionId())
                                .bind("modelId", r.getModelId())
                                .bind("modelInfo", r.getModelInfo())
                                .bind("modelRegistry", r.getModelRegistry())
                                .bind("modelRegistryId", r.getModelRegistryId())
                                .bind("answer", r.getAnswer())
                                .bind("vqaScore", r.getVqaScore())
                                .bind("score", r.getScore())
                                .bind("bBox", r.getBBox())
                                .bind("label", r.getLabel())
                                .bind("sectionAlias", r.getSectionAlias())
                                .bind("synonymId", r.getSynonymId())
                                .bind("sorSynonym", r.getSorSynonym())
                                .bind("questionId", r.getQuestionId())
                                .bind("sorQuestion", r.getSorQuestion())
                                .bind("weight", r.getWeight())
                                .bind("category", r.getCategory())
                                .bind("lineItemType", r.getLineItemType())
                                .bind("isMultiEntityEnabled", r.getIsMultiEntityEnabled())
                                .bind("encryptionPolicy", r.getEncryptionPolicy())
                                .bind("isEncrypted", r.getIsEncrypted())
                                .bind("ocrFieldId", r.getOcrFieldId())
                                .bind("isOcrFieldComparable", r.getIsOcrFieldComparable())
                                .bind("extractedText", r.getExtractedText())
                                .bind("threshold", r.getThreshold())
                                .bind("bestMatch", r.getBestMatch())
                                .bind("bestScore", r.getBestScore())
                                .bind("regexPattern", r.getRegexPattern())
                                .bind("candidatesList", r.getCandidatesList())
                                .bind("mismatchCount", r.getMismatchCount())
                                .bind("matchStatus", r.getMatchStatus())
                                .bind("allowedAdapter", r.getAllowedAdapter())
                                .add();
                    }

                    preparedBatch.execute();
                });

                totalInserted += batch.size();
                log.info(aMarker, "Progress: {}/{} records inserted", totalInserted, results.size());

            } catch (Exception e) {
                log.error(aMarker, "Batch insert failed for batch #{} (records {}-{})",
                        batchCount, i, endIndex, e);
                HandymanException handymanException = new HandymanException(e);
                HandymanException.insertException(
                        "Batch insert failed for batch #" + batchCount,
                        handymanException,
                        action);
            }
        }

        log.info(aMarker, "Batch insert completed - Total inserted: {}/{}, Batches: {}",
                totalInserted, results.size(), batchCount);
    }
}
