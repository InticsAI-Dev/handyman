package in.handyman.raven.lib.adapters.ocr;

import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static in.handyman.raven.core.enums.ConsumerApiCountConstants.P4_A1_OCR_COMPARATOR_API;

/**
 * Parallel OCR Text Comparison Executor using CompletableFuture
 */
public class OcrTextComparisonExecutor {

    private final Logger log;
    private final Marker aMarker;
    private final ActionExecutionAudit action;
    private final int parallelism;

    private final ExecutorService executor;

    public OcrTextComparisonExecutor(Logger log, Marker aMarker, ActionExecutionAudit action, int parallelism) {
        this.log = log;
        this.aMarker = aMarker;
        this.action = action;
        this.parallelism = Integer.parseInt(action.getContext().getOrDefault(P4_A1_OCR_COMPARATOR_API, "40"));

        this.executor = new ThreadPoolExecutor(
                this.parallelism,
                this.parallelism * 2,
                30L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(1000),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }

    /**
     * Executes OCR comparison across multiple threads
     */
    public List<OcrTextComparatorInput> executeComparisons(List<OcrTextComparatorInput> inputs,
                                                           double fuzzyMatchThreshold) {

        AtomicInteger processedCount = new AtomicInteger();
        AtomicInteger errorCount = new AtomicInteger();

        log.info(aMarker, "Starting parallel OCR text comparison for {} inputs using {} threads",
                inputs.size(), parallelism);

        List<CompletableFuture<OcrTextComparatorInput>> futures = inputs.stream()
                .filter(this::isComparable)
                .map(input -> CompletableFuture.supplyAsync(() -> processInput(input, fuzzyMatchThreshold, processedCount, errorCount), executor))
                .collect(Collectors.toList());

        // Wait for all comparisons to finish
        List<OcrTextComparatorInput> results = futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());

        log.info(aMarker, "Completed OCR text comparison: Processed={}, Errors={}, Total={}",
                processedCount.get(), errorCount.get(), inputs.size());

        shutdownExecutor();

        return results;
    }

    private boolean isComparable(OcrTextComparatorInput input) {
        Boolean comparable = input.getIsOcrFieldComparable();
        return Boolean.TRUE.equals(comparable) || "t".equalsIgnoreCase(String.valueOf(comparable));
    }

    private OcrTextComparatorInput processInput(OcrTextComparatorInput input,
                                                double fuzzyMatchThreshold,
                                                AtomicInteger processedCount,
                                                AtomicInteger errorCount) {
        try {
            OcrComparisonAdapter adapter = OcrComparisonAdapterFactory.getAdapter(input.getAllowedAdapter());

            String answer = safeTrim(input.getAnswer());
            String extractedText = safeTrim(input.getExtractedText());

            OcrComparisonResult result = adapter.compareValues(answer, extractedText, fuzzyMatchThreshold);

            input.setBestMatch(result.getBestMatch());
            input.setBestScore(result.getBestScore());
            input.setCandidatesList(result.getCandidatesList());
            input.setIsOcrFieldComparable(result.isMatch());
            input.setThreshold((int) (fuzzyMatchThreshold * 100));

            processedCount.incrementAndGet();
            return input;
        } catch (Exception e) {
            errorCount.incrementAndGet();
            log.error(aMarker, "Comparison failed for originId={}, sorItemName={}, paperNo={}",
                    input.getOriginId(), input.getSorItemName(), input.getPaperNo(), e);
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("OCR comparison failed for originId: " + input.getOriginId(), handymanException, action);
            return input;
        }
    }

    private String safeTrim(String str) {
        return str == null ? "" : str.trim();
    }

    private void shutdownExecutor() {
        try {
            executor.shutdown();
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                log.warn(aMarker, "Forcing shutdown after timeout...");
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error(aMarker, "Executor termination interrupted", e);
        }
    }
}
