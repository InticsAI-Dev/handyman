package in.handyman.raven.lib.adapters.selections;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.lib.adapters.selections.models.SelectionFilteringInputTable;

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
                        (first.getWhitelistedLabelsWithPriority() == null || first.getWhitelistedLabelsWithPriority().isBlank() || extractPriorityMap(rows).isEmpty());


        if (isFirstEmpty) {
            rows.forEach(r -> {
                r.setLabelMatching(true);
                r.setLabelMatchMessage(appendMsg(r,"Update with empty priority labels returning everything"));
            });
            return rows.get(0);
        }

        if (rows.size() == 1) return handleSingleRow(rows.get(0), messages);

        if (rows.size() == 2 && rows.stream().filter(this::hasNonEmptyAnswer).count() == 1)
            return handleTwoRowsOneNonEmpty(rows, messages);

        if (rows.size() == 2 && rows.stream().noneMatch(this::hasNonEmptyAnswer))
            return handleTwoRowsBothEmpty(rows, messages);

        Map<String, Integer> priorityMap = extractPriorityMap(rows);

        boolean hasValidLabels = rows.stream()
                .anyMatch(r -> r.getSorItemLabel() != null && !r.getSorItemLabel().isBlank());

        if (priorityMap.isEmpty() || !hasValidLabels) {
            return handleNoLabelPriority(rows, messages);
        }

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

        row.setLabelPriorityIdx("1");
        row.setLabelMatching(true);
        row.setLabelMatchMessage(appendMsg(row, "Single row → priority 1"));
        messages.add("Single row → origin: " + row.getOriginId() + ", sorItem: " + row.getSorItemName());
        return row;
    }

    private SelectionFilteringInputTable handleTwoRowsOneNonEmpty(
            List<SelectionFilteringInputTable> rows,
            List<String> messages) {

        SelectionFilteringInputTable winner = rows.stream()
                .filter(this::hasNonEmptyAnswer)
                .findFirst().orElseThrow();

        winner.setLabelPriorityIdx("1");
        winner.setLabelMatching(true);
        winner.setLabelMatchMessage(appendMsg(winner, "Winner of two rows (one non-empty) → priority 1"));

        rows.stream()
                .filter(r -> r != winner)
                .forEach(r -> {
                    r.setLabelMatching(false);
                    r.setLabelMatchMessage(appendMsg(r, "Discarded (empty)"));
                });

        messages.add("Two rows with one non-empty → origin: " + winner.getOriginId() + ", sorItem: " + winner.getSorItemName());
        return winner;
    }

    private SelectionFilteringInputTable handleTwoRowsBothEmpty(
            List<SelectionFilteringInputTable> rows,
            List<String> messages) {

        rows.forEach(r -> {
            r.setLabelPriorityIdx("1");
            r.setLabelMatching(true);
            r.setLabelMatchMessage(appendMsg(r, "Both answers empty → default priority 1"));
        });

        messages.add("Two rows both empty → origin: " + rows.get(0).getOriginId() + ", sorItem: " + rows.get(0).getSorItemName());
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
            r.setLabelPriorityIdx("1");
            r.setLabelMatchMessage(appendMsg(r, r == winner ? "No label → selected min paperNo/min id" : "No label → not selected"));
        });

        messages.add("No labels present → origin: " + winner.getOriginId() + ", sorItem: " + winner.getSorItemName());
        return winner;
    }
    private SelectionFilteringInputTable handlePriorityBasedSelection(
            List<SelectionFilteringInputTable> rows,
            Map<String, Integer> priorityMap,
            List<String> messages) {

        // Step 1: Assign whitelist priorities
        assignPriorities(rows, priorityMap);

        // Step 2: Find minimum priority
        int minPriority = rows.stream()
                .mapToInt(r -> Integer.parseInt(r.getLabelPriorityIdx()))
                .min()
                .orElse(Integer.MAX_VALUE);

        // Step 3: Collect rows with same min priority
        List<SelectionFilteringInputTable> topPriorityRows = rows.stream()
                .filter(r -> Integer.parseInt(r.getLabelPriorityIdx()) == minPriority)
                .collect(Collectors.toList());

        SelectionFilteringInputTable winner;

        if (topPriorityRows.size() == 1) {
            winner = topPriorityRows.get(0);
        } else {

            // Check if all answers are equal
            boolean allEqualAnswers = topPriorityRows.stream()
                    .map(SelectionFilteringInputTable::getAnswer)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet())
                    .size() <= 1;

            // ★ NEW RULE ★
            // Same priority & same (or different) answers → pick MIN ID
            winner = topPriorityRows.stream()
                    .min(Comparator.comparingLong(SelectionFilteringInputTable::getId))
                    .orElseThrow();

            if (allEqualAnswers) {
                winner.setLabelMatchMessage(
                        appendMsg(winner, "Equal answers → selected min id")
                );
            } else {
                winner.setLabelMatchMessage(
                        appendMsg(winner, "Different answers → selected min id")
                );
            }
        }

        // Mark winner and losers
        for (SelectionFilteringInputTable r : rows) {
            boolean isWinner = (r == winner);
            r.setLabelMatching(isWinner);
            r.setLabelMatchMessage(
                    appendMsg(r,
                            isWinner ? "Selected using priority + id tiebreak"
                                    : "Rejected"
                    )
            );
        }

        messages.add("Whitelist priority applied → origin: " + winner.getOriginId()
                + ", sorItem: " + winner.getSorItemName());

        return winner;
    }


    // ========================= STAGE 4: PRIORITY MAP =========================
    private Map<String, Integer> extractPriorityMap(List<SelectionFilteringInputTable> rows) {
        String json = rows.get(0).getWhitelistedLabelsWithPriority();
        if (json == null || json.isBlank()) return Map.of();

        try {
            Map<String, Integer> output = mapper.readValue(json, new TypeReference<Map<String, Integer>>() {});

            Map<String, Integer> temp = new HashMap<>();

            output.forEach((key, value) -> {
                temp.put(removeSpecialCharacters(key), value);
            });

            output.clear();
            output.putAll(temp);

            return output;

        } catch (Exception e) {
            return Map.of();
        }
    }

    private void assignPriorities(List<SelectionFilteringInputTable> rows, Map<String, Integer> priorityMap) {
        rows.forEach(r -> {
            String key = removeSpecialCharacters(r.getSorItemLabel());
            int p = priorityMap.getOrDefault(key, Integer.MAX_VALUE);
            r.setLabelPriorityIdx(String.valueOf(p));
        });
    }


    public String removeSpecialCharacters(String input) {
        if (input == null) return "";
        return input.replaceAll("[^a-zA-Z0-9]", "").toLowerCase().trim();
    }

    // ========================= STAGE 5: MESSAGES =========================
    private String appendMsg(SelectionFilteringInputTable row, String message) {
        String msgWithPriority = "[p=" + row.getLabelPriorityIdx() + "] " + message;
        String existing = row.getLabelMatchMessage();
        if (existing == null || existing.isBlank()) return msgWithPriority;
        return existing + " | " + msgWithPriority;
    }

}
