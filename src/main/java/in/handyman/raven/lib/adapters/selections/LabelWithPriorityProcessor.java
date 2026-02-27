package in.handyman.raven.lib.adapters.selections;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.lib.adapters.selections.models.SelectionFilteringInputTable;
import in.handyman.raven.lib.adapters.selections.models.WhitelistLabelPriority;
import org.slf4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

public class LabelWithPriorityProcessor {

    private final ObjectMapper mapper;
    private final Logger logger;

    public LabelWithPriorityProcessor(ObjectMapper mapper, Logger log) {
        this.mapper = mapper;
        this.logger = log;
        logger.info("LabelWithPriorityProcessor initialized");
    }

    /**
     * Process input rows and return filtered rows.
     * Logic:
     * 1. Group by originId + sorItemName.
     * 2. For each group, select a SINGLE winner based on priority logic.
     * 3. Return the list of winners.
     */
    public List<SelectionFilteringInputTable> process(List<SelectionFilteringInputTable> input) {
        if (input == null || input.isEmpty()) {
            logger.info("LabelWithPriorityProcessor: Input is empty.");
            return Collections.emptyList();
        }

        logger.info("LabelWithPriorityProcessor: Processing {} input records.", input.size());

        // Group by originId + sorItemName
        Map<String, List<SelectionFilteringInputTable>> groupedOriginWithItems = input.stream()
                .collect(Collectors.groupingBy(r -> String.format("%s|%s", r.getOriginId(), r.getSorItemName())));

        logger.info("LabelWithPriorityProcessor: Created {} groups.", groupedOriginWithItems.size());

        List<SelectionFilteringInputTable> result = new ArrayList<>();

        for (Map.Entry<String, List<SelectionFilteringInputTable>> entry : groupedOriginWithItems.entrySet()) {

            String groupKey = entry.getKey();
            List<SelectionFilteringInputTable> group = entry.getValue();

            logger.info("[{}] Processing group with {} records.", groupKey, group.size());


            List<SelectionFilteringInputTable> singleEntityRecords = group.stream()
                    .filter(record -> !record.getIsMultiEntityEnabled())
                    .collect(Collectors.toList());
            logger.info("[{}] Group has {} single_entity records.", groupKey, singleEntityRecords.size());

            List<SelectionFilteringInputTable> multiEntityRecords = group.stream()
                    .filter(SelectionFilteringInputTable::getIsMultiEntityEnabled)
                    .collect(Collectors.toList());

            logger.info("[{}] Group has {} multi_entity records.", groupKey, multiEntityRecords.size());

            List<SelectionFilteringInputTable> singleValueRecords = singleEntityRecords.stream()
                    .filter(record -> "single_value".equals(record.getLineItemType()))
                    .collect(Collectors.toList());
            logger.info("[{}] Group has {} single_value records.", groupKey, singleValueRecords.size());

            List<SelectionFilteringInputTable> multiValueRecords = singleEntityRecords.stream()
                    .filter(record -> "multi_value".equals(record.getLineItemType()))
                    .collect(Collectors.toList());
            logger.info("[{}] Group has {} multi_value records.", groupKey, multiValueRecords.size());

            multiValueRecords.forEach(selectionFilteringInputTable -> {
                logger.info("[{}] Marking multi_value record ID {} as matching without priority check.", groupKey,
                        selectionFilteringInputTable.getId());
                selectionFilteringInputTable.setLabelMatching(true);
                selectionFilteringInputTable.setLabelMatchMessage(appendMsg(selectionFilteringInputTable, "| SECTION_FILTER[Multi-value record, No-priority Required]"));
            });


            multiEntityRecords.forEach(selectionFilteringInputTable -> {
                logger.info("[{}] Marking multi_entity record ID {} as matching without priority check.", groupKey,
                        selectionFilteringInputTable.getId());
                selectionFilteringInputTable.setLabelMatching(true);
                selectionFilteringInputTable.setLabelMatchMessage(appendMsg(selectionFilteringInputTable, "| SECTION_FILTER[Multi-entity record, No-priority Required]"));
            });

            result.addAll(multiValueRecords);
            result.addAll(multiEntityRecords);
            logger.info("[{}] Added {} multi_value records directly to result.", groupKey, multiValueRecords.size());

            if (singleValueRecords.isEmpty())
                continue;
            SelectionFilteringInputTable winer = processGroup(singleValueRecords, groupKey);
            if (winer != null) {
                result.add(winer);
            }
        }


        return result;
    }

    private SelectionFilteringInputTable processGroup(List<SelectionFilteringInputTable> group, String contextKey) {
        logger.info("[{}] Processing group with {} Single value records.", contextKey, group.size());

        // 1. Single value check
        if (group.size() == 1) {
            SelectionFilteringInputTable row = group.get(0);
            row.setLabelMatching(true);
            row.setLabelMatchMessage(appendMsg(row, "Single row → selected"));
            logger.info("[{}] Single row group. Selected ID: {}", contextKey, row.getId());
            return row;
        }else {

            // 2. Filter Empty Answers
            List<SelectionFilteringInputTable> nonEmptyAnswers = group.stream()
                    .filter(this::hasNonEmptyAnswer)
                    .collect(Collectors.toList());

            if (nonEmptyAnswers.isEmpty()) {
                // All empty, return fallback (first by paperNo)
                SelectionFilteringInputTable winner = group.stream()
                        .min(Comparator.comparingLong(this::getSafePaperNo).thenComparingLong(this::getSafeId))
                        .orElse(group.get(0));

                winner.setLabelMatching(true);
                winner.setLabelMatchMessage(appendMsg(winner, "All answers empty → selected by fallback"));

                group.stream().filter(r -> r != winner).forEach(r -> {
                    r.setLabelMatching(false);
                    r.setLabelMatchMessage(appendMsg(r, "All answers empty → rejected"));
                });
                logger.info("[{}] All answers empty. Selected Fallback ID: {}", contextKey, winner.getId());
                return winner;
        }

            // 3. Branching Logic
            boolean hasSectionAlias = nonEmptyAnswers.stream()
                    .anyMatch(r -> r.getSectionAlias() != null && !r.getSectionAlias().isBlank());

            logger.info("[{}] Branch Decision: hasSectionAlias={} (checked {} candidates)", contextKey, hasSectionAlias,
                    nonEmptyAnswers.size());

            SelectionFilteringInputTable winner;
            if (hasSectionAlias) {
                winner = filterBySectionPriority(nonEmptyAnswers, group, contextKey);
            } else {
                winner = filterByConsensusAndMajority(nonEmptyAnswers, group, contextKey);
            }

            return winner;
        }

    }

    // --- CASE A: Section Priority Logic ---
    private SelectionFilteringInputTable filterBySectionPriority(List<SelectionFilteringInputTable> candidates,
            List<SelectionFilteringInputTable> allGroupRows, String contextKey) {
        logger.info("[{}] Executing Section Priority Logic. Candidates: {}", contextKey, candidates.size());

        List<WhitelistLabelPriority> priorityRules = extractPriorityList(allGroupRows);
        logger.info("[{}] Found {} priority rules.", contextKey, priorityRules.size());

        if (!priorityRules.isEmpty()) {
            // Find min priority value across CURRENT candidates using SECTION ALIAS
            Integer minPriority = null;
            for (SelectionFilteringInputTable r : candidates) {
                Integer p = getLabelPriority(r, priorityRules);
                if (p != null) {
                    if (minPriority == null || p < minPriority) {
                        minPriority = p;
                    }
                }
            }

            if (minPriority != null) {
                final int best = minPriority;
                logger.info("[{}] Best Section Priority found: {}", contextKey, best);
                List<SelectionFilteringInputTable> priorityWinners = candidates.stream()
                        .filter(r -> {
                            Integer p = getLabelPriority(r, priorityRules);
                            return p != null && p == best;
                        })
                        .collect(Collectors.toList());

                if (!priorityWinners.isEmpty()) {
                    candidates = priorityWinners;
                    logger.info("[{}] Filtered to {} candidates by priority.", contextKey, candidates.size());
                }
            } else {
                logger.info("[{}] No matching Section Priority found for candidates.", contextKey);
            }
        } else {
            logger.info("[{}] No priority rules configured.", contextKey);
        }

        // Fallback
        return finalizeWinner(candidates, allGroupRows, "Selected by Section Priority/Fallback", contextKey);
    }

    // --- CASE B: Voting / Consensus Logic ---
    private SelectionFilteringInputTable filterByConsensusAndMajority(List<SelectionFilteringInputTable> candidates,
            List<SelectionFilteringInputTable> allGroupRows, String contextKey) {
        logger.info("[{}] Executing Voting Logic. Candidates: {}", contextKey, candidates.size());

        // Step 1: Consensus
        boolean consensus = candidates.stream()
                .map(SelectionFilteringInputTable::getAnswer)
                .distinct()
                .count() == 1;

        logger.info("[{}] Consensus Check: {}", contextKey, consensus);

        if (consensus) {
            // Select min paperNo
            return finalizeWinner(candidates, allGroupRows, "Consensus → selected min paperNo", contextKey);
        }

        // Step 2: Container Majority Voting
        // Count entries per SOR Container
        Map<Long, Long> containerCounts = candidates.stream()
                .filter(r -> r.getSorContainerId() != null)
                .collect(Collectors.groupingBy(SelectionFilteringInputTable::getSorContainerId,
                        Collectors.counting()));

        if (!containerCounts.isEmpty()) {
            long maxCount = containerCounts.values().stream().max(Long::compare).orElse(0L);
            logger.info("[{}] Container Majority Max Count: {}", contextKey, maxCount);

            // Filter candidates to those belonging to ANY container with maxCount
            List<SelectionFilteringInputTable> majorityCandidates = candidates.stream()
                    .filter(r -> r.getSorContainerId() != null
                            && containerCounts.get(r.getSorContainerId()) == maxCount)
                    .collect(Collectors.toList());

            if (!majorityCandidates.isEmpty()) {
                candidates = majorityCandidates; // Narrow down candidates
                logger.info("[{}] Filtered to {} candidates by Majority.", contextKey, candidates.size());
            }
        }

        // Step 3c: Priority Rules (Whitelist)
        // Now matching on sorItemLabel (or sectionAlias as fallback if needed, but SQL
        // suggested whitelist_key ~ label)
        List<WhitelistLabelPriority> priorityRules = extractPriorityList(allGroupRows);
        if (!priorityRules.isEmpty()) {
            // Find min priority value across CURRENT candidates
            Integer minPriority = null;
            for (SelectionFilteringInputTable r : candidates) {
                Integer p = getPriority(r, priorityRules);
                if (p != null) {
                    if (minPriority == null || p < minPriority) {
                        minPriority = p;
                    }
                }
            }

            if (minPriority != null) {
                final int best = minPriority;
                logger.info("[{}] Best Priority (in Voting) found: {}", contextKey, best);
                List<SelectionFilteringInputTable> priorityWinners = candidates.stream()
                        .filter(r -> {
                            Integer p = getPriority(r, priorityRules);
                            return p != null && p == best;
                        })
                        .collect(Collectors.toList());

                if (!priorityWinners.isEmpty()) {
                    candidates = priorityWinners; // Narrow down further
                    logger.info("[{}] Filtered to {} candidates by Priority (Voting phase).", contextKey,
                            candidates.size());
                }
            }
        }

        // Step 3d: Final Tie-Breaker (PageNo -> ID)
        return finalizeWinner(candidates, allGroupRows, "Selected by Voting Logic/Fallback", contextKey);
    }

    // --- Common Finalizer ---
    private SelectionFilteringInputTable finalizeWinner(List<SelectionFilteringInputTable> candidates,
            List<SelectionFilteringInputTable> allGroupRows, String successMsg, String contextKey) {
        SelectionFilteringInputTable winner = candidates.stream()
                .min(Comparator.comparingLong(this::getSafePaperNo)
                        .thenComparingLong(this::getSafeId))
                .orElse(candidates.get(0));

        winner.setLabelMatching(true);
        winner.setLabelMatchMessage(appendMsg(winner, successMsg));

        logger.info("[{}] Final Selection - ID: {}, PaperNo: {}, Reason: {}", contextKey, winner.getId(),
                winner.getPaperNo(), successMsg);

        final SelectionFilteringInputTable finalWinner = winner;
        allGroupRows.stream().filter(r -> r != finalWinner).forEach(r -> {
            r.setLabelMatching(false);
            r.setLabelMatchMessage(appendMsg(r, "Rejected in voting phase"));
        });

        return winner;
    }

    private Integer getLabelPriority(SelectionFilteringInputTable row, List<WhitelistLabelPriority> rules) {
        return getPriority(row, rules);
    }

    private Integer getPriority(SelectionFilteringInputTable row, List<WhitelistLabelPriority> rules) {
        // SQL join: tsw.whitelist_key = t.sor_item_label
        String label = row.getSorItemLabel();
        if (label == null)
            return null; // Or try sectionAlias? sticking to label as per SQL

        String normalizedLabel = removeSpecialCharacters(label);
        Integer bestPriority = null;

        for (WhitelistLabelPriority rule : rules) {
            String key = removeSpecialCharacters(rule.getWhitelistKey());
            String matchType = rule.getLabelSearchConfig();

            boolean match = false;
            if ("CONTAINS".equalsIgnoreCase(matchType)) {
                if (normalizedLabel.contains(key))
                    match = true;
            } else {
                if (normalizedLabel.equals(key))
                    match = true;
            }

            if (match) {
                Integer p = rule.getLabelPriority();
                if (p != null) {
                    if (bestPriority == null || p < bestPriority) {
                        bestPriority = p;
                    }
                }
            }
        }
        return bestPriority;
    }

    private boolean hasNonEmptyAnswer(SelectionFilteringInputTable row) {
        return row.getAnswer() != null && !row.getAnswer().isBlank();
    }

    private long getSafePaperNo(SelectionFilteringInputTable row) {
        return row.getPaperNo() != null ? row.getPaperNo() : Long.MAX_VALUE;
    }

    private long getSafeId(SelectionFilteringInputTable row) {
        return row.getId() != null ? row.getId() : Long.MAX_VALUE;
    }

    private List<WhitelistLabelPriority> extractPriorityList(List<SelectionFilteringInputTable> rows) {
        // Try to find valid priority rules from any row in the group
        for (SelectionFilteringInputTable row : rows) {
            String json = row.getWhitelistedLabelsWithPriority();
            if (json != null && !json.isBlank()) {
                try {
                    List<WhitelistLabelPriority> list = mapper.readValue(json,
                            new TypeReference<List<WhitelistLabelPriority>>() {
                            });
                    if (list != null && !list.isEmpty())
                        return list;
                } catch (Exception e) {
                    // Ignore and try next
                }
            }
        }
        return Collections.emptyList();
    }

    public String removeSpecialCharacters(String input) {
        if (input == null)
            return "";
        return input.replaceAll("[^a-zA-Z0-9]", "").toLowerCase().trim();
    }

    // ========================= STAGE 5: MESSAGES =========================
    private String appendMsg(SelectionFilteringInputTable row, String message) {
        String existing = row.getLabelMatchMessage();
        if (existing == null || existing.isBlank())
            return message;
        return existing + " | " + message;
    }
}