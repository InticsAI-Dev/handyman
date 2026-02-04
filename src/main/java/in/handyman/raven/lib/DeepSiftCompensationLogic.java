package in.handyman.raven.lib;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;

public class DeepSiftCompensationLogic {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class ProcessedPage {
        private int paperNo;
        private boolean isBlank;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class RangeConfig {
        private int start;
        private int end;
        private int priority; // Lower value = higher priority
    }

    /**
     * Selects pages based on the provided ranges and compensation rules.
     *
     * @param allPages           List of all available pages (sorted by paperNo).
     * @param ranges             List of configured ranges.
     * @param fieldPaperCount    Required valid pages per range (limit).
     * @param considerBlankPages Whether to count blank pages as valid.
     * @return List of selected paper numbers.
     */
    public static List<Integer> selectPages(List<ProcessedPage> allPages, List<RangeConfig> ranges,
            int fieldPaperCount, boolean considerBlankPages) {

        // Ensure pages are sorted
        allPages.sort(Comparator.comparingInt(ProcessedPage::getPaperNo));

        // Ensure ranges are sorted by priority (if priority is used for ordering
        // processing)
        // The user requirement says "proceed to the next specified range". implying
        // order matters.
        // Assuming the list order is the processing order, or sorted by range start?
        // Let's assume ranges should be processed in the order they are defined or by
        // start page.
        // Usually ranges are like 6-10, 23-27. Let's start by sorting by start page.
        // But the SQL used 'ord' as priority. Let's respect the input list order if
        // explicit priority isn't clear,
        // but normally we process ranges in document order.
        // Let's copy and sort by priority if present, else by start.
        List<RangeConfig> sortedRanges = new ArrayList<>(ranges);
        // If priority is not set (0), maybe sort by start?
        // logic: The requirement implies a sequence: "proceed to the next specified
        // range".
        // We will assume the caller passes them in the desired order.

        Set<Integer> selectedPageNumbers = new HashSet<>();
        List<Integer> resultSequence = new ArrayList<>();

        // Map paperNo to Page object for quick lookup
        Map<Integer, ProcessedPage> pageMap = allPages.stream()
                .collect(Collectors.toMap(ProcessedPage::getPaperNo, p -> p, (a, b) -> a));

        // Set of used pages to avoid reusing same page for multiple compensations?
        // "Do not pull pages from page 1 or from unrelated ranges."
        // "It must come from the immediate next pages after the same range."
        Set<Integer> consumedPages = new HashSet<>();

        for (RangeConfig range : sortedRanges) {
            int needed = (range.end - range.start) + 1; // Default target is the range size
            // Wait, user said: "Once the required number of non-blank pages
            // (field_paper_count) is satisfied within a range"
            // And also: "range_target_count" in SQL.
            // Let's use the smaller of calculated range size or fieldPaperCount if provided
            // > 0?
            // Actually, the example says: range 6-10 (5 pages).
            // If fieldPaperCount is not specified per range in the simple function
            // signature, we might need it.
            // The SQL snippet has "COALESCE(pr.range_target_count, si.paper_count, 10000)".
            // Let's assume the target count IS the range size (end - start + 1), unless
            // fieldPaperCount limits it?
            // "Once the required number of non-blank pages (field_paper_count) is
            // satisfied"
            // Let's assume we want valid pages equal to the range size.

            // Start with the range size
            int rangeSize = (range.end - range.start) + 1;
            // If fieldPaperCount is provided (valid assumption > 0) and meaningful, use it
            // as a limit??
            // "Required valid pages per range (limit)"
            // If fieldPaperCount is 10000 (default), we want rangeSize.
            // If fieldPaperCount is 2, and rangeSize is 5, we likely want 2.
            // If fieldPaperCount is 10, and rangeSize is 5, we likely want 5 (unless we
            // want to over-fetch?)
            // Let's assume fieldPaperCount is a LIMIT. So min(rangeSize, fieldPaperCount).
            // BUT, if fieldPaperCount > rangeSize? Do we extend?
            // "Only if compensation cannot be fulfilled ... proceed to next range".
            // If we want 10 pages from a 5-page range, we effectively want compensation
            // immediately?
            // Let's assume targetCount is fieldPaperCount if it's "reasonable" (not default
            // large).
            // Actually, if fieldPaperCount is passed, we should respect it.
            // If it's 10000 (the default fallback), we bound it by rangeSize implicitly
            // because we only iterate range?
            // No, the logic loops 'searchStart' for compensation until targetCount is met.
            // So if targetCount is 10000, we would fetch 10000 pages! That's bad if we go
            // beyond the range into "unrelated" pages.
            // So default behavior (10000) MUST be bounded by rangeSize.
            // If explicit small count (e.g. 1) is given, we want 1.

            // Logic:
            // If fieldPaperCount >= 10000 (arbitrary large default), target = rangeSize.
            // Else target = fieldPaperCount.

            int targetCount;
            if (fieldPaperCount >= 10000 || fieldPaperCount <= 0) {
                targetCount = rangeSize;
            } else {
                targetCount = fieldPaperCount;
            }
            int validCount = 0;
            List<Integer> currentRangeSelection = new ArrayList<>();

            // 1. Try to fulfill from the range itself
            for (int i = range.start; i <= range.end; i++) {
                if (validCount >= targetCount) {
                    break;
                }

                if (consumedPages.contains(i)) {
                    continue; // Should not happen if ranges don't overlap, but good safety
                }

                ProcessedPage p = pageMap.get(i);
                if (p != null) {
                    boolean isValid = considerBlankPages || !p.isBlank;
                    if (isValid) {
                        currentRangeSelection.add(i);
                        consumedPages.add(i);
                        validCount++;
                    } else {
                        // Mark as consumed so we don't try to use it as compensation later (though
                        // unlikely)
                        consumedPages.add(i);
                    }
                }
            }

            // 2. Compensation
            if (validCount < targetCount) {
                // We need more valid pages.
                // "It must come from the immediate next pages after the same range."
                int searchStart = range.end + 1;
                // How far to search? Until we find enough or hit end of document?
                // "Do not pull pages from ... unrelated ranges." - implies we shouldn't steal
                // from next range?
                // But: "Only if compensation cannot be fulfilled for the current range, then
                // and only then, proceed to the next specified range"
                // implies we process ranges sequentially. If Range 1 is 6-10 and Range 2 starts
                // at 23.
                // We can pull from 11, 12... up to 22.
                // We should stop if we hit the start of the NEXT range?
                // Example 1: 6-10, 23-27. Compensation for 6-10 took 11.
                // Example 2: Blank in R1, comp from 11.

                int maxPage = allPages.isEmpty() ? 0 : allPages.get(allPages.size() - 1).getPaperNo();

                for (int i = searchStart; i <= maxPage && validCount < targetCount; i++) {
                    // Check if this page is part of ANY subsequent defined range?
                    // "Do not pull pages from ... unrelated ranges." could mean don't jump.
                    // But "Do not pull pages ... from unrelated ranges" might mean "don't go fetch
                    // from range 2 to satisfy range 1".
                    // However, Example 3 uses 11, 12 for Range 1.
                    // If Range 2 starts at 23, 11 and 12 are "free" space between ranges.
                    // Check if 'i' is inside another range in the FUTURE list.
                    if (isInAnyRange(i, sortedRanges)) {
                        // Stop? or Skip?
                        // "Only if compensation cannot be fulfilled for the current range, then and
                        // only then, proceed to the next specified range"
                        // This implies we cannot steal from a future range.
                        // But if we are desperate?
                        // "Do not pull pages from unrelated ranges" -> likely means don't touch future
                        // ranges.
                        continue; // Skip pages belonging to other ranges? Or stop?
                        // Usually we pick from the gap. If we hit the next range, we probably can't
                        // compensate more.
                    }

                    if (consumedPages.contains(i)) {
                        continue;
                    }

                    ProcessedPage p = pageMap.get(i);
                    if (p != null) {
                        // Found a candidate
                        // Is it valid?
                        boolean isValid = considerBlankPages || !p.isBlank;
                        if (isValid) {
                            currentRangeSelection.add(i);
                            consumedPages.add(i);
                            validCount++;
                        } else {
                            // It's blank. If we verify blank, we take it?
                            // No, if it's blank and we don't consider blanks, we skip it and keep looking?
                            // "If page 7 is blank -> consider page 11 (if 7 blank)".
                            // If 11 is also blank (and we want non-blank), do we take 12?
                            // The example doesn't explicitly say what if compensation page is blank.
                            // Presumably we want *valid* pages. So skip blanks in compensation too.
                        }
                    }
                }
            }

            // Add selected to result
            resultSequence.addAll(currentRangeSelection);
        }

        // Sort execution result ??
        // Example output: 6, 7, 8, 9, 10, 23...
        // The result should probably be sorted.
        Collections.sort(resultSequence);
        return resultSequence;
    }

    private static boolean isInAnyRange(int pageNo, List<RangeConfig> ranges) {
        for (RangeConfig r : ranges) {
            if (pageNo >= r.start && pageNo <= r.end) {
                return true;
            }
        }
        return false;
    }
}
