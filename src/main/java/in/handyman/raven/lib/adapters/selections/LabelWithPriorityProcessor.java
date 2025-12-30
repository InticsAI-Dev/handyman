package in.handyman.raven.lib.adapters.selections;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.lib.adapters.selections.models.SelectionFilteringInputTable;
import in.handyman.raven.lib.adapters.selections.models.WhitelistLabelPriority;

import java.util.*;
import java.util.stream.Collectors;

public class LabelWithPriorityProcessor {

    private final ObjectMapper mapper;

    public LabelWithPriorityProcessor(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * Process input rows and return filtered rows with proper priority and matching flags.
     */
    public List<SelectionFilteringInputTable> process(
            List<SelectionFilteringInputTable> input
    ) {
        List<String> messages = new ArrayList<>();

        if (input == null || input.isEmpty()) return Collections.emptyList();

        // 1️⃣ Group by originId → sorItemName
        List<SelectionFilteringInputTable> filtered =
                input.stream()
                        .filter(SelectionFilteringInputTable::isLabelMatching)
                        .collect(Collectors.toList());

        // 1️⃣ Group by originId → sorItemName
        List<SelectionFilteringInputTable> filteredNotMatching =
                input.stream()
                        .filter(r -> !r.isLabelMatching())
                        .collect(Collectors.toList());


        Map<String, Map<String, List<SelectionFilteringInputTable>>> grouped =
                filtered.stream()
                        .collect(Collectors.groupingBy(
                                SelectionFilteringInputTable::getOriginId,
                                Collectors.groupingBy(
                                        SelectionFilteringInputTable::getSorItemName
                                )
                        ));


        List<SelectionFilteringInputTable> result = new ArrayList<>();

        for (Map<String, List<SelectionFilteringInputTable>> originMap : grouped.values()) {
            for (List<SelectionFilteringInputTable> rows : originMap.values()) {
                SelectionFilteringInputTable winner = processSorItemRows(rows, messages);
                result.addAll(rows); // add all rows with updated flags & messages
            }
        }
        result.addAll(filteredNotMatching); // add back non-matching rows without changes

        return result;
    }

    // ========================= STAGE 1: PROCESS SOR ITEM ROWS =========================
    private SelectionFilteringInputTable processSorItemRows(
            List<SelectionFilteringInputTable> rows,
            List<String> messages) {

        SelectionFilteringInputTable first = rows.get(0);
        boolean isFirstEmpty =
                (first.getWhitelistedLabelsWithPriority() == null || first.getWhitelistedLabelsWithPriority().isBlank());

        if (isFirstEmpty) {
            rows.forEach(r -> {
                r.setLabelPriorityIdx("N/A");
                r.setLabelMatching(true);
                r.setLabelMatchMessage(appendMsg(r,"Update with empty priority labels returning everything"));
            });
            return rows.get(0);
        }

        // Extract priority map to check if priorities are actually defined
        Map<String, Integer> priorityMap = extractPriorityMap(rows);

        // Check if ALL whitelist entries have null/empty priorities
        boolean allPrioritiesEmpty = priorityMap.values().stream()
                .allMatch(priority -> priority == null || priority == Integer.MAX_VALUE);

        if (allPrioritiesEmpty) {
            // No priorities defined - just allow all matching labels
            rows.forEach(r -> {
                r.setLabelPriorityIdx("N/A");
                r.setLabelMatching(true);
                r.setLabelMatchMessage(appendMsg(r, "Whitelist without priorities → all labels allowed"));
            });
            return rows.get(0);
        }

        if (rows.size() == 1) return handleSingleRow(rows.get(0), messages);

        if (rows.size() == 2 && rows.stream().filter(this::hasNonEmptyAnswer).count() == 1)
            return handleTwoRowsOneNonEmpty(rows, messages);

        if (rows.size() == 2 && rows.stream().noneMatch(this::hasNonEmptyAnswer))
            return handleTwoRowsBothEmpty(rows, messages);

        // ✅ FIXED: Check if empty label exists in whitelist
        boolean hasValidLabels = rows.stream()
                .anyMatch(r -> r.getSorItemLabel() != null && !r.getSorItemLabel().isBlank());

        // Check if empty string "" is in the whitelist
        boolean emptyLabelWhitelisted = priorityMap.containsKey("");

        if (!hasValidLabels && !emptyLabelWhitelisted) {
            // No labels AND empty label not whitelisted → fallback logic
            return handleNoLabelPriority(rows, messages);
        }

        // ✅ If we reach here, either we have valid labels OR empty label is whitelisted
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

        // Priority already set by assignPriorities
        row.setLabelMatching(true);
        row.setLabelMatchMessage(appendMsg(row, "Single row → selected"));
        messages.add("Single row → origin: " + row.getOriginId() + ", sorItem: " + row.getSorItemName());
        return row;
    }

    private SelectionFilteringInputTable handleTwoRowsOneNonEmpty(
            List<SelectionFilteringInputTable> rows,
            List<String> messages) {

        SelectionFilteringInputTable winner = rows.stream()
                .filter(this::hasNonEmptyAnswer)
                .findFirst().orElseThrow();

        winner.setLabelMatching(true);
        winner.setLabelMatchMessage(appendMsg(winner, "Winner of two rows (has answer)"));

        rows.stream()
                .filter(r -> r != winner)
                .forEach(r -> {
                    r.setLabelMatching(false);
                    r.setLabelMatchMessage(appendMsg(r, "Rejected (empty answer)"));
                });

        messages.add("Two rows with one non-empty → origin: " + winner.getOriginId() +
                ", sorItem: " + winner.getSorItemName());
        return winner;
    }

    private SelectionFilteringInputTable handleTwoRowsBothEmpty(
            List<SelectionFilteringInputTable> rows,
            List<String> messages) {

        rows.forEach(r -> {
            r.setLabelMatching(true);
            r.setLabelMatchMessage(appendMsg(r, "Both answers empty → both selected"));
        });

        messages.add("Two rows both empty → origin: " + rows.get(0).getOriginId() +
                ", sorItem: " + rows.get(0).getSorItemName());
        return rows.get(0);
    }

    private SelectionFilteringInputTable handleNoLabelPriority(
            List<SelectionFilteringInputTable> rows,
            List<String> messages) {

        // Select row with min paperNo, if tie → min id
        SelectionFilteringInputTable winner = rows.stream()
                .min(Comparator.comparingLong(SelectionFilteringInputTable::getPaperNo)
                        .thenComparingLong(SelectionFilteringInputTable::getId))
                .orElseThrow();

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

        // Priorities already assigned by assignPriorities() earlier

        // Separate rows into three categories:
        // 1. Rows with defined priority (numeric values)
        // 2. Rows with null priority (in whitelist but priority is null/0)
        // 3. Rows NOT in whitelist (rejected)

        List<SelectionFilteringInputTable> rowsWithDefinedPriority = new ArrayList<>();
        List<SelectionFilteringInputTable> rowsWithNullPriority = new ArrayList<>();
        List<SelectionFilteringInputTable> rowsNotInWhitelist = new ArrayList<>();

        for (SelectionFilteringInputTable r : rows) {
            String normalizedLabel = removeSpecialCharacters(r.getSorItemLabel());
            String priorityStr = r.getLabelPriorityIdx();

            // Check if label exists in whitelist
            if (!priorityMap.containsKey(normalizedLabel)) {
                // Label NOT in whitelist → REJECT
                rowsNotInWhitelist.add(r);
            } else if (priorityStr != null && !priorityStr.equals("N/A")) {
                // Label in whitelist with numeric priority
                rowsWithDefinedPriority.add(r);
            } else {
                // Label in whitelist with null/0 priority
                rowsWithNullPriority.add(r);
            }
        }

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

        // Only keep rows with minimum priority; reject higher priorities
        List<SelectionFilteringInputTable> topPriorityRows = rowsWithDefinedPriority.stream()
                .filter(r -> Integer.parseInt(r.getLabelPriorityIdx()) == minPriority)
                .collect(Collectors.toList());

        // Reject rows with higher priority
        rowsWithDefinedPriority.stream()
                .filter(r -> Integer.parseInt(r.getLabelPriorityIdx()) > minPriority)
                .forEach(r -> {
                    r.setLabelMatching(false);
                    r.setLabelMatchMessage(
                            appendMsg(r, "Rejected: priority " + r.getLabelPriorityIdx() +
                                    " > min priority " + minPriority)
                    );
                });

        SelectionFilteringInputTable winner;
        if (topPriorityRows.size() == 1) {
            winner = topPriorityRows.get(0);
            winner.setLabelMatchMessage(appendMsg(winner, "Selected: highest priority"));
        } else {
            boolean allLabelsIdentical = topPriorityRows.stream()
                    .map(r -> removeSpecialCharacters(r.getSorItemLabel()))
                    .filter(label -> label != null && !label.isEmpty())
                    .collect(Collectors.toSet())
                    .size() <= 1;

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
        return winner;
    }

    // ========================= STAGE 4: PRIORITY MAP =========================
    private Map<String, Integer> extractPriorityMap(List<SelectionFilteringInputTable> rows) {
        String json = rows.get(0).getWhitelistedLabelsWithPriority();
        if (json == null || json.isBlank()) return Map.of();

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
            return output;
        } catch (Exception e) {
            return Map.of();
        }
    }

    private void assignPriorities(List<SelectionFilteringInputTable> rows,
                                  Map<String, Integer> priorityMap) {
        rows.forEach(r -> {
            String key = removeSpecialCharacters(r.getSorItemLabel());
            Integer p = priorityMap.get(key);

            if (p == null || p == Integer.MAX_VALUE) {
                r.setLabelPriorityIdx("N/A");
            } else {
                r.setLabelPriorityIdx(String.valueOf(p));
            }
        });
    }

    public String removeSpecialCharacters(String input) {
        if (input == null) return "";
        return input.replaceAll("[^a-zA-Z0-9]", "").toLowerCase().trim();
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
