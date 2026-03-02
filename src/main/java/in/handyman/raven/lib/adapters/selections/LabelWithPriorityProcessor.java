package in.handyman.raven.lib.adapters.selections;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.lib.adapters.selections.models.SelectionFilteringInputTable;
import in.handyman.raven.lib.adapters.selections.models.WhitelistLabelPriority;
import org.slf4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

public class LabelWithPriorityProcessor {

    private final Logger logger;
    private final ObjectMapper mapper;

    public LabelWithPriorityProcessor(ObjectMapper mapper, Logger logger) {
        this.mapper = mapper;
        this.logger=logger;
        logger.info("LabelWithPriorityProcessor initialized");
    }
    /**
     * Process input rows and return filtered rows with proper priority and matching flags.
     */
    public List<SelectionFilteringInputTable> process(
            List<SelectionFilteringInputTable> input
    ) {
        logger.info("Starting process with {} input rows", input != null ? input.size() : 0);
        List<String> messages = new ArrayList<>();

        if (input == null || input.isEmpty()) {
            logger.info("Input is null or empty, returning empty list");
            return Collections.emptyList();
        }

        // FIX 1: Filter out null entries from input
        int originalSize = input.size();
        input = input.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        int nullCount = originalSize - input.size();
        if (nullCount > 0) {
            logger.info("Filtered out {} null entries from input", nullCount);
        }

        if (input.isEmpty()) {
            logger.info("All input entries were null, returning empty list");
            return Collections.emptyList();
        }

        logger.info("Processing {} valid rows after null filtering", input.size());

        // 1️⃣ Group by originId → sorItemName
        // NULL is treated as "not yet determined" → include for processing (same as true)
        List<SelectionFilteringInputTable> filtered =
                input.stream()
                        .filter(Objects::nonNull)
                        .filter(r -> !Boolean.FALSE.equals(r.getLabelMatching()))
                        .collect(Collectors.toList());

        logger.info("Found {} rows with label matching = true or null", filtered.size());

        // 1️⃣ Group by originId → sorItemName
        // Only rows explicitly set to false are considered non-matching
        List<SelectionFilteringInputTable> filteredNotMatching =
                input.stream()
                        .filter(Objects::nonNull)
                        .filter(r -> Boolean.FALSE.equals(r.getLabelMatching()))
                        .collect(Collectors.toList());

        logger.info("Found {} rows with label matching = false", filteredNotMatching.size());

        Map<String, Map<String, List<SelectionFilteringInputTable>>> grouped = filtered.stream()
                .collect(Collectors.groupingBy(
                        SelectionFilteringInputTable::getOriginId,
                        Collectors.groupingBy(SelectionFilteringInputTable::getSorItemName)
                ));

        logger.info("Grouped into {} origin groups", grouped.size());

        List<SelectionFilteringInputTable> result = new ArrayList<>();
        for (Map<String, List<SelectionFilteringInputTable>> originMap : grouped.values()) {
            for (List<SelectionFilteringInputTable> rows : originMap.values()) {
                if (!rows.isEmpty()) {
                    logger.info("Processing group with {} rows - originId: {}, sorItemName: {}",
                            rows.size(),
                            rows.get(0).getOriginId(),
                            rows.get(0).getSorItemName());
                }
                SelectionFilteringInputTable winner = processSorItemRows(rows, messages);
                result.addAll(rows);
            }
        }
        result.addAll(filteredNotMatching);

        logger.info("Process completed. Total result rows: {}", result.size());
        return result;
    }

    // ========================= STAGE 1: PROCESS SOR ITEM ROWS =========================
    private SelectionFilteringInputTable processSorItemRows(
            List<SelectionFilteringInputTable> rows,
            List<String> messages) {

        if (rows.isEmpty()) {
            logger.warn("processSorItemRows called with empty list");
            return null;
        }

        SelectionFilteringInputTable first = rows.get(0);
        String originId = first.getOriginId();
        String sorItemName = first.getSorItemName();

        logger.info("[originId: {}, sorItemName: {}] Processing SOR item rows. Count: {}",
                originId, sorItemName, rows.size());

        boolean isFirstEmpty = (first.getWhitelistedLabelsWithPriority() == null ||
                first.getWhitelistedLabelsWithPriority().isBlank());

        if (isFirstEmpty) {
            logger.info("[originId: {}, sorItemName: {}] Whitelist is empty - allowing all rows",
                    originId, sorItemName);
            rows.forEach(r -> {
                r.setLabelPriorityIdx("N/A");
                r.setLabelMatching(true);
                r.setLabelMatchMessage(appendMsg(r, "Update with empty priority labels returning everything"));
            });
            return rows.get(0);
        }

        Map<String, Integer> priorityMap = extractPriorityMap(rows);
        logger.info("[originId: {}, sorItemName: {}] Extracted priority map with {} entries",
                originId, sorItemName, priorityMap.size());

        // Assign priorities FIRST before any processing
        assignPriorities(rows, priorityMap);

        boolean allPrioritiesEmpty = priorityMap.values().stream()
                .allMatch(priority -> priority == null || priority == Integer.MAX_VALUE);

        if (allPrioritiesEmpty) {
            logger.info("[originId: {}, sorItemName: {}] All priorities are empty/null - allowing all rows",
                    originId, sorItemName);
            rows.forEach(r -> {
                r.setLabelPriorityIdx("N/A");
                r.setLabelMatching(true);
                r.setLabelMatchMessage(appendMsg(r, "Whitelist without priorities → all labels allowed"));
            });
            return rows.get(0);
        }

        if (rows.size() == 1) {
            logger.info("[originId: {}, sorItemName: {}] Single row detected - handling single row case",
                    originId, sorItemName);
            return handleSingleRow(rows.get(0), messages);
        }

        if (rows.size() == 2 && rows.stream().filter(this::hasNonEmptyAnswer).count() == 1) {
            logger.info("[originId: {}, sorItemName: {}] Two rows with one non-empty answer detected",
                    originId, sorItemName);
            return handleTwoRowsOneNonEmpty(rows, messages);
        }

        if (rows.size() == 2 && rows.stream().noneMatch(this::hasNonEmptyAnswer)) {
            logger.info("[originId: {}, sorItemName: {}] Two rows both with empty answers detected",
                    originId, sorItemName);
            return handleTwoRowsBothEmpty(rows, messages);
        }

        boolean hasValidLabels = rows.stream()
                .anyMatch(r -> r.getSorItemLabel() != null && !r.getSorItemLabel().isBlank());

        boolean emptyLabelWhitelisted = priorityMap.containsKey("");

        if (!hasValidLabels && !emptyLabelWhitelisted) {
            logger.info("[originId: {}, sorItemName: {}] No valid labels present and empty label not whitelisted",
                    originId, sorItemName);
            return handleNoLabelPriority(rows, messages);
        }

        logger.info("[originId: {}, sorItemName: {}] Using priority-based selection",
                originId, sorItemName);
        return handlePriorityBasedSelection(rows, priorityMap, messages);
    }

    // ========================= STAGE 2: ROW CHECKS =========================
    private boolean hasNonEmptyAnswer(SelectionFilteringInputTable row) {
        return row.getAnswer() != null && !row.getAnswer().isBlank();
    }

    // ========================= STAGE 3: HANDLERS =========================
    private SelectionFilteringInputTable handleSingleRow(
            SelectionFilteringInputTable row,
            List<String> messages) {

        String originId = row.getOriginId();
        String sorItemName = row.getSorItemName();
        long paperNo = row.getPaperNo();

        logger.info("[originId: {}, sorItemName: {}, paperNo: {}] Handling single row - id: {}",
                originId, sorItemName, paperNo, row.getId());

        // Priority already set by assignPriorities
        row.setLabelMatching(true);
        row.setLabelMatchMessage(appendMsg(row, "Single row → selected"));
        messages.add("Single row → origin: " + originId + ", sorItem: " + sorItemName);
        return row;
    }

    private SelectionFilteringInputTable handleTwoRowsOneNonEmpty(
            List<SelectionFilteringInputTable> rows,
            List<String> messages) {

        SelectionFilteringInputTable first = rows.get(0);
        String originId = first.getOriginId();
        String sorItemName = first.getSorItemName();

        logger.info("[originId: {}, sorItemName: {}] Handling two rows with one non-empty answer",
                originId, sorItemName);

        SelectionFilteringInputTable winner = rows.stream()
                .filter(this::hasNonEmptyAnswer)
                .findFirst().orElseThrow();

        logger.info("[originId: {}, sorItemName: {}, paperNo: {}] Winner selected - id: {}",
                originId, sorItemName, winner.getPaperNo(), winner.getId());

        winner.setLabelMatching(true);
        winner.setLabelMatchMessage(appendMsg(winner, "Winner of two rows (has answer)"));

        rows.stream()
                .filter(r -> r != winner)
                .forEach(r -> {
                    r.setLabelMatching(false);
                    r.setLabelMatchMessage(appendMsg(r, "Rejected (empty answer)"));
                    logger.info("[originId: {}, sorItemName: {}, paperNo: {}] Rejected row - id: {}",
                            originId, sorItemName, r.getPaperNo(), r.getId());
                });

        messages.add("Two rows with one non-empty → origin: " + winner.getOriginId() +
                ", sorItem: " + winner.getSorItemName());
        return winner;
    }

    private SelectionFilteringInputTable handleTwoRowsBothEmpty(
            List<SelectionFilteringInputTable> rows,
            List<String> messages) {

        SelectionFilteringInputTable first = rows.get(0);
        String originId = first.getOriginId();
        String sorItemName = first.getSorItemName();

        logger.info("[originId: {}, sorItemName: {}] Handling two rows both with empty answers - both selected",
                originId, sorItemName);

        rows.forEach(r -> {
            r.setLabelMatching(true);
            r.setLabelMatchMessage(appendMsg(r, "Both answers empty → both selected"));
        });

        messages.add("Two rows both empty → origin: " + originId + ", sorItem: " + sorItemName);
        return rows.get(0);
    }

    private SelectionFilteringInputTable handleNoLabelPriority(
            List<SelectionFilteringInputTable> rows,
            List<String> messages) {

        SelectionFilteringInputTable first = rows.get(0);
        String originId = first.getOriginId();
        String sorItemName = first.getSorItemName();

        logger.info("[originId: {}, sorItemName: {}] Handling no label priority case - selecting by min paperNo/id",
                originId, sorItemName);

        SelectionFilteringInputTable winner = rows.stream()
                .min(Comparator.comparingLong(SelectionFilteringInputTable::getPaperNo)
                        .thenComparingLong(SelectionFilteringInputTable::getId))
                .orElseThrow();

        logger.info("[originId: {}, sorItemName: {}, paperNo: {}] Winner selected - id: {}",
                originId, sorItemName, winner.getPaperNo(), winner.getId());

        rows.forEach(r -> {
            r.setLabelMatching(r == winner);
            r.setLabelPriorityIdx("N/A");
            r.setLabelMatchMessage(appendMsg(r,
                    r == winner ? "No label priority → selected min paperNo/id"
                            : "No label priority → not selected"));
        });

        messages.add("No labels present → origin: " + winner.getOriginId() +
                ", sorItem: " + winner.getSorItemName());
        return winner;
    }

    private SelectionFilteringInputTable handlePriorityBasedSelection(
            List<SelectionFilteringInputTable> rows,
            Map<String, Integer> priorityMap,
            List<String> messages) {

        SelectionFilteringInputTable first = rows.get(0);
        String originId = first.getOriginId();
        String sorItemName = first.getSorItemName();

        logger.info("[originId: {}, sorItemName: {}] Starting priority-based selection with {} rows",
                originId, sorItemName, rows.size());

        // Priorities already assigned by assignPriorities() earlier

        // Separate rows into three categories:
        // 1. Rows with defined priority (numeric values)
        // 2. Rows with null priority (in whitelist but priority is null/0)
        // 3. Rows NOT in whitelist (rejected)

        List<SelectionFilteringInputTable> rowsWithDefinedPriority = new ArrayList<>();
        List<SelectionFilteringInputTable> rowsWithNullPriority = new ArrayList<>();
        List<SelectionFilteringInputTable> rowsNotInWhitelist = new ArrayList<>();

        for (SelectionFilteringInputTable r : rows) {
            String label = r.getSorItemLabel();
            String normalizedLabel = (label != null) ? removeSpecialCharacters(label) : "";
            String priorityStr = r.getLabelPriorityIdx();

            // Check if label exists in whitelist
            if (!priorityMap.containsKey(normalizedLabel)) {
                // Label NOT in whitelist → REJECT
                rowsNotInWhitelist.add(r);
                logger.info("[originId: {}, sorItemName: {}, paperNo: {}] Row id {} - NOT in whitelist",
                        originId, sorItemName, r.getPaperNo(), r.getId());
            } else if (priorityStr != null && !priorityStr.equals("N/A")) {
                // Label in whitelist with numeric priority
                rowsWithDefinedPriority.add(r);
                logger.info("[originId: {}, sorItemName: {}, paperNo: {}] Row id {} - Label in whitelist with numeric priority",
                        originId, sorItemName, r.getPaperNo(), r.getId());
            } else {
                // Label in whitelist with null/0 priority
                rowsWithNullPriority.add(r);
                logger.info("[originId: {}, sorItemName: {}, paperNo: {}] Row id {} - in whitelist with null priority",
                        originId, sorItemName, r.getPaperNo(), r.getId());
            }
        }

        logger.info("[originId: {}, sorItemName: {}] Categorized rows - defined priority: {}, null priority: {}, not in whitelist: {}",
                originId, sorItemName, rowsWithDefinedPriority.size(), rowsWithNullPriority.size(), rowsNotInWhitelist.size());

        // Reject all rows NOT in whitelist
        rowsNotInWhitelist.forEach(r -> {
            r.setLabelMatching(false);
            r.setLabelMatchMessage(appendMsg(r, "Label not in whitelist → rejected"));
        });

        // Allow all rows with null priority (in whitelist but no priority defined)
        rowsWithNullPriority.forEach(r -> {
            r.setLabelMatching(true);
            r.setLabelMatchMessage(appendMsg(r, "Label in whitelist (no priority restriction) → allowed"));
        });

        // If no rows have defined priority, all null priority rows are allowed
        if (rowsWithDefinedPriority.isEmpty()) {
            logger.info("[originId: {}, sorItemName: {}] No rows with defined priority - returning first available row",
                    originId, sorItemName);
            if (!rowsWithNullPriority.isEmpty()) {
                return rowsWithNullPriority.get(0);
            }
            // All rows were not in whitelist
            return rows.get(0);
        }

        // Find minimum priority among rows with defined priority
        int minPriority = rowsWithDefinedPriority.stream()
                .mapToInt(r -> Integer.parseInt(r.getLabelPriorityIdx()))
                .min()
                .orElse(Integer.MAX_VALUE);

        logger.info("[originId: {}, sorItemName: {}] Minimum priority found: {}",
                originId, sorItemName, minPriority);

        // Only keep rows with minimum priority; reject higher priorities
        List<SelectionFilteringInputTable> topPriorityRows = rowsWithDefinedPriority.stream()
                .filter(r -> Integer.parseInt(r.getLabelPriorityIdx()) == minPriority)
                .collect(Collectors.toList());

        logger.info("[originId: {}, sorItemName: {}] Found {} rows with top priority {}",
                originId, sorItemName, topPriorityRows.size(), minPriority);

        // Reject rows with higher priority
        rowsWithDefinedPriority.stream()
                .filter(r -> Integer.parseInt(r.getLabelPriorityIdx()) > minPriority)
                .forEach(r -> {
                    r.setLabelMatching(false);
                    r.setLabelMatchMessage(
                            appendMsg(r, "Rejected: priority " + r.getLabelPriorityIdx() +
                                    " > min priority " + minPriority)
                    );
                    logger.info("[originId: {}, sorItemName: {}, paperNo: {}] Rejected row id {} - priority {} > min {}",
                            originId, sorItemName, r.getPaperNo(), r.getId(), r.getLabelPriorityIdx(), minPriority);
                });

        SelectionFilteringInputTable winner;
        if (topPriorityRows.size() == 1) {
            winner = topPriorityRows.get(0);
            winner.setLabelMatchMessage(appendMsg(winner, "Selected: highest priority"));
            logger.info("[originId: {}, sorItemName: {}, paperNo: {}] Single winner - id: {}, priority: {}",
                    originId, sorItemName, winner.getPaperNo(), winner.getId(), minPriority);
        } else {
            logger.info("[originId: {}, sorItemName: {}] Multiple rows with same priority - applying tiebreaker logic",
                    originId, sorItemName);
            boolean allLabelsIdentical = topPriorityRows.stream()
                    .map(r -> {
                        String label = r.getSorItemLabel();
                        return (label != null) ? removeSpecialCharacters(label) : "";
                    })
                    .filter(label -> label != null && !label.isEmpty())
                    .collect(Collectors.toSet())
                    .size() <= 1;

            logger.info("[originId: {}, sorItemName: {}] All labels identical: {}",
                    originId, sorItemName, allLabelsIdentical);

            if (allLabelsIdentical) {
                winner = topPriorityRows.stream()
                        .min(Comparator
                                .comparingLong(SelectionFilteringInputTable::getPaperNo)
                                .thenComparing((SelectionFilteringInputTable r) -> !hasNonEmptyAnswer(r))
                                .thenComparingLong(SelectionFilteringInputTable::getId)
                        )
                        .orElseThrow();

                long winnerPageNo = winner.getPaperNo();
                long samePageCount = topPriorityRows.stream()
                        .filter(r -> r.getPaperNo() == winnerPageNo)
                        .count();

                logger.info("[originId: {}, sorItemName: {}, paperNo: {}] Winner selected by paperNo - id: {}, same page count: {}",
                        originId, sorItemName, winnerPageNo, winner.getId(), samePageCount);

                if (samePageCount > 1) {
                    winner.setLabelMatchMessage(
                            appendMsg(winner, "Same label on page " + winnerPageNo +
                                    " → selected with answer/min id=" + winner.getId())
                    );
                } else {
                    winner.setLabelMatchMessage(
                            appendMsg(winner, "Same labels → selected min paperNo=" + winner.getPaperNo())
                    );
                }
            } else {
                winner = topPriorityRows.stream()
                        .min(Comparator
                                .comparing((SelectionFilteringInputTable r) -> !hasNonEmptyAnswer(r))
                                .thenComparingLong(SelectionFilteringInputTable::getId)
                        )
                        .orElseThrow();

                boolean allEqualAnswers = topPriorityRows.stream()
                        .map(SelectionFilteringInputTable::getAnswer)
                        .filter(Objects::nonNull)
                        .filter(a -> !a.isBlank())
                        .collect(Collectors.toSet())
                        .size() <= 1;

                logger.info("[originId: {}, sorItemName: {}, paperNo: {}] Winner selected by answer/id - id: {}, hasAnswer: {}, allEqualAnswers: {}",
                        originId, sorItemName, winner.getPaperNo(), winner.getId(), hasNonEmptyAnswer(winner), allEqualAnswers);

                if (hasNonEmptyAnswer(winner)) {
                    winner.setLabelMatchMessage(
                            appendMsg(winner, "Selected: has answer with priority " + minPriority)
                    );
                } else if (allEqualAnswers) {
                    winner.setLabelMatchMessage(
                            appendMsg(winner, "Equal answers → selected min id")
                    );
                } else {
                    winner.setLabelMatchMessage(
                            appendMsg(winner, "Selected min id (no answers present)")
                    );
                }
            }
        }

        for (SelectionFilteringInputTable r : topPriorityRows) {
            boolean isWinner = (r == winner);
            r.setLabelMatching(isWinner);
            if (!isWinner) {
                r.setLabelMatchMessage(
                        appendMsg(r, "Rejected: " +
                                (hasNonEmptyAnswer(winner) && !hasNonEmptyAnswer(r)
                                        ? "winner has answer"
                                        : "lower paperNo/id chosen"))
                );
            }
        }

        messages.add("Whitelist priority applied → origin: " + winner.getOriginId() +
                ", sorItem: " + winner.getSorItemName());

        logger.info("[originId: {}, sorItemName: {}, paperNo: {}] Priority-based selection completed - winner id: {}, priority: {}",
                originId, sorItemName, winner.getPaperNo(), winner.getId(), winner.getLabelPriorityIdx());

        return winner;
    }

    // ========================= STAGE 4: PRIORITY MAP =========================
    private Map<String, Integer> extractPriorityMap(List<SelectionFilteringInputTable> rows) {
        if (rows.isEmpty()) {
            logger.warn("extractPriorityMap called with empty list");
            return Map.of();
        }

        SelectionFilteringInputTable first = rows.get(0);
        String originId = first.getOriginId();
        String sorItemName = first.getSorItemName();
        String json = first.getWhitelistedLabelsWithPriority();

        if (json == null || json.isBlank()) {
            logger.info("[originId: {}, sorItemName: {}] No whitelist JSON found", originId, sorItemName);
            return Map.of();
        }

        try {
            List<WhitelistLabelPriority> list = mapper.readValue(json,
                    new TypeReference<List<WhitelistLabelPriority>>() {});
            Map<String, Integer> output = new HashMap<>();
            for (WhitelistLabelPriority row : list) {
                Integer priority = row.getLabelPriority();
                if (priority == null || priority == 0) {
                    priority = Integer.MAX_VALUE;
                }
                output.put(removeSpecialCharacters(row.getWhitelistKey()), priority);
            }
            logger.info("[originId: {}, sorItemName: {}] Successfully parsed priority map with {} entries",
                    originId, sorItemName, output.size());
            return output;
        } catch (Exception e) {
            logger.error("[originId: {}, sorItemName: {}] Failed to parse whitelist JSON: {}",
                    originId, sorItemName, e.getMessage(), e);
            return Map.of();
        }
    }

    // FIX 2: Added null check inside assignPriorities method
    private void assignPriorities(List<SelectionFilteringInputTable> rows,
                                  Map<String, Integer> priorityMap) {
        if (rows.isEmpty()) {
            logger.warn("assignPriorities called with empty list");
            return;
        }

        SelectionFilteringInputTable first = rows.get(0);
        String originId = first.getOriginId();
        String sorItemName = first.getSorItemName();

        logger.info("[originId: {}, sorItemName: {}] Assigning priorities to {} rows",
                originId, sorItemName, rows.size());

        rows.forEach(r -> {
            // Guard against null rows
            if (r == null) {
                logger.info("[originId: {}, sorItemName: {}] Encountered null row in assignPriorities - skipping",
                        originId, sorItemName);
                return;
            }

            String label = r.getSorItemLabel();
            String key = (label != null) ? removeSpecialCharacters(label) : "";
            Integer p = priorityMap.get(key);

            if (p == null || p == Integer.MAX_VALUE) {
                r.setLabelPriorityIdx("N/A");
                logger.trace("[originId: {}, sorItemName: {}, paperNo: {}] Row id {} - assigned priority: N/A",
                        originId, sorItemName, r.getPaperNo(), r.getId());
            } else {
                r.setLabelPriorityIdx(String.valueOf(p));
                logger.trace("[originId: {}, sorItemName: {}, paperNo: {}] Row id {} - assigned priority: {}",
                        originId, sorItemName, r.getPaperNo(), r.getId(), p);
            }
        });

        logger.info("[originId: {}, sorItemName: {}] Priority assignment completed", originId, sorItemName);
    }

    /**
     * Normalize label by removing all special characters EXCEPT # symbol.
     * Removes spaces and special characters, but preserves # and alphanumeric characters.
     */
    private String removeSpecialCharacters(String input) {
        if (input == null) return "";
        // Remove all characters except alphanumeric and # symbol
        return input.replaceAll("[^a-zA-Z0-9#]", "").toLowerCase().trim();
    }

    // ========================= STAGE 5: MESSAGES =========================
    private String appendMsg(SelectionFilteringInputTable row, String message) {
        String priority = row.getLabelPriorityIdx();
        String msgWithPriority = "[p=" + (priority != null ? priority : "N/A") + "] " + message;
        String existing = row.getLabelMatchMessage();
        if (existing == null || existing.isBlank()) return msgWithPriority;
        return existing + " | " + msgWithPriority;
    }
}
