package in.handyman.raven.lib.model.scalar;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.CoproProcessor;
import in.handyman.raven.lib.model.DocumentWisePostProcessingInput;
import org.slf4j.Logger;

import java.net.URL;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ValidationByDocumentWiseExecutor {

    private final List<DocumentWisePostProcessingInput> documentWisePostProcessingInputs;

    private final ActionExecutionAudit actionExecutionAudit;
    private final Logger log;
    private final int consumerCount;
    private final String outputTable;

    public ValidationByDocumentWiseExecutor(List<DocumentWisePostProcessingInput> documentWisePostProcessingInputs,
                                            ActionExecutionAudit actionExecutionAudit,
                                            final Logger log,
                                            int threadPoolSize,
                                            String outputTable) {
        this.documentWisePostProcessingInputs = documentWisePostProcessingInputs;
        this.actionExecutionAudit = actionExecutionAudit;
        this.log = log;
        this.consumerCount = threadPoolSize;
        this.outputTable = outputTable;
    }

    public List<DocumentWisePostProcessingInput> doDocumentWiseValidator() {
        int inputSize = documentWisePostProcessingInputs.size();
        log.info("Starting document-wise validation for {} records", inputSize);

        if (documentWisePostProcessingInputs.isEmpty()) {
            log.warn("No inputs found to process");
            return documentWisePostProcessingInputs;
        }

        return processWithCoproProcessor();
    }

    private List<DocumentWisePostProcessingInput> processWithCoproProcessor() {

        BlockingQueue<DocumentWisePostProcessingOriginInput> queue = new LinkedBlockingQueue<>();

        List<URL> coproNodes = new ArrayList<>();

        String resourceConn = actionExecutionAudit.getContext().get("resource.conn");
        if (resourceConn == null || resourceConn.isEmpty()) {
            log.error("Resource connection not found in context. Cannot proceed with CoproProcessor.");
            return documentWisePostProcessingInputs;
        }
        
        DocumentWisePostProcessingOriginInput stoppingSeed = new DocumentWisePostProcessingOriginInput();
        
        CoproProcessor<DocumentWisePostProcessingOriginInput, DocumentWisePostProcessingOriginOutput> coproProcessor =
                new CoproProcessor<>(
                        queue,
                        DocumentWisePostProcessingOriginOutput.class,
                        DocumentWisePostProcessingOriginInput.class,
                        resourceConn,
                        log,
                        stoppingSeed,
                        coproNodes,
                        actionExecutionAudit
                );

        log.info("CoproProcessor initialized for document-wise validation with resource: {}", resourceConn);

        Map<String, List<DocumentWisePostProcessingInput>> originMap = new LinkedHashMap<>();
        
        for (DocumentWisePostProcessingInput input : documentWisePostProcessingInputs) {
            String originId = input.getOriginId();
            if (originId != null) {
                originMap.computeIfAbsent(originId, k -> new ArrayList<>()).add(input);
            }
        }

        int originCount = originMap.size();
        log.info("Found {} unique origins to process", originCount);

        if (originCount == 0) {
            log.warn("No origins found to process");
            return documentWisePostProcessingInputs;
        }

        for (Map.Entry<String, List<DocumentWisePostProcessingInput>> originEntry : originMap.entrySet()) {
            DocumentWisePostProcessingOriginInput originInput = DocumentWisePostProcessingOriginInput.builder()
                    .originId(originEntry.getKey())
                    .inputs(new ArrayList<>(originEntry.getValue())) // Create a copy to avoid modification issues
                    .build();
            queue.add(originInput);
        }

        queue.add(stoppingSeed);
        log.info("Added {} origins to CoproProcessor queue for multithreaded processing (plus stopping seed)", originCount);

        DocumentWisePostProcessingConsumerProcess consumerProcess = 
                new DocumentWisePostProcessingConsumerProcess(actionExecutionAudit, log);

        if (outputTable == null || outputTable.isEmpty()) {
            log.error("Output table is not set. Cannot proceed with CoproProcessor insert.");
            return documentWisePostProcessingInputs;
        }
        
        String insertSql = buildInsertSQL(outputTable);
        log.info("Using insert SQL for output table: {}", outputTable);

        int finalConsumerCount = Math.min(consumerCount, originCount);
        if (finalConsumerCount <= 0) {
            finalConsumerCount = 1;
        }
        
        log.info("Starting CoproProcessor with {} consumer threads for parallel processing", finalConsumerCount);
        log.info("Queue size before starting consumer: {}", queue.size());
        
        try {
            coproProcessor.startConsumer(insertSql, finalConsumerCount, 1, consumerProcess);
            log.info("CoproProcessor startConsumer returned successfully");
        } catch (Exception e) {
            log.error("Error during CoproProcessor consumer execution: {}", e.getMessage(), e);

            return documentWisePostProcessingInputs;
        }

        log.info("CoproProcessor consumer completed multithreaded processing");

        Map<String, List<DocumentWisePostProcessingInput>> resultsMap = consumerProcess.getResultsMap();
        log.info("Results map contains {} origins after processing", resultsMap.size());
        resultsMap.forEach((oid, inputs) -> log.info("Origin {} has {} processed records", oid, inputs.size()));

        List<DocumentWisePostProcessingInput> finalResults = new ArrayList<>();
        int matchedCount = 0;
        int unmatchedCount = 0;
        
        for (DocumentWisePostProcessingInput originalInput : documentWisePostProcessingInputs) {
            String originId = originalInput.getOriginId();
            if (originId != null && resultsMap.containsKey(originId)) {
                List<DocumentWisePostProcessingInput> processedInputs = resultsMap.get(originId);

                DocumentWisePostProcessingInput processedInput = processedInputs.stream()
                        .filter(input -> input.getSorItemId() != null && 
                                input.getSorItemId().equals(originalInput.getSorItemId()))
                        .findFirst()
                        .orElse(originalInput);
                
                if (processedInput != originalInput) {
                    matchedCount++;
                }
                finalResults.add(processedInput);
            } else {
                unmatchedCount++;
                log.debug("Origin {} not found in resultsMap, using original input", originId);
                finalResults.add(originalInput);
            }
        }

        log.info("Completed all validations for document-wise post processing. Total: {}, Matched: {}, Unmatched: {}", 
                finalResults.size(), matchedCount, unmatchedCount);
        return finalResults;
    }

    private String buildInsertSQL(String outputTable) {
        return "INSERT INTO " + outputTable + " (" +
                "transaction_id, created_on, created_user_id, last_updated_on, last_updated_user_id, status, version, " +
                "feature, label, left_pos, lower_pos, right_pos, upper_pos, b_box, precision_val, predicted_value, " +
                "section_alias, sor_container_instance, document_id, truth_id, channel_id, group_id, origin_id, " +
                "paper_no, question_id, root_pipeline_id, score, sor_item_name, sor_question, synonym_id, tenant_id, " +
                "vqa_score, category, stage, batch_id, line_item_type, is_encrypted, encryption_policy, " +
                "is_removed_after_filtering, message, sor_container_id, truth_entity_id, sor_item_id, is_multi_entity_enabled) VALUES (" +
                "?, ?, ?, ?, ?, ?, ?, " +
                "?, ?, ?, ?, ?, ?, ?, ?, ?, " +
                "?, ?, ?, ?, ?, ?, ?, " +
                "?, ?, ?, ?, ?, ?, ?, ?, " +
                "?, ?, ?, ?, ?, ?, ?, " +
                "?, ?, ?, ?, ?, ?)";
    }

}
