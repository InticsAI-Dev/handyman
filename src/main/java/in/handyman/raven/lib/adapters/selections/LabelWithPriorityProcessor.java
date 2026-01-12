package in.handyman.raven.lib.adapters.selections;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.lib.adapters.selections.models.AggregationEvaluatorInputModel;
import in.handyman.raven.lib.adapters.selections.models.WhitelistLabelPriority;
import org.slf4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

public class LabelWithPriorityProcessor {

    private final ObjectMapper mapper;
    private final Logger log;

    public LabelWithPriorityProcessor(ObjectMapper mapper, Logger log) {
        this.mapper = mapper;
        this.log = log;
    }

    /**
     * Process input rows and return filtered rows.
     * Logic:
     * 1. Group by originId + sorItemName.
     * 2. For each group, select a SINGLE winner based on priority logic.
     * 3. Return the list of winners.
     */
    public List<AggregationEvaluatorInputModel> process(List<AggregationEvaluatorInputModel> input) {
        if (input == null || input.isEmpty()) {
            log.info("LabelWithPriorityProcessor: Input is empty.");
            return Collections.emptyList();
        }

        log.info("LabelWithPriorityProcessor: Processing {} input records.", input.size());

        // Group by originId + sorItemName
        Map<String, List<AggregationEvaluatorInputModel>> groupedOriginWithItems = input.stream()
                .collect(Collectors.groupingBy(r -> String.format("%s|%s", r.getOriginId(), r.getSorItemName())));

        log.info("LabelWithPriorityProcessor: Created {} groups.", groupedOriginWithItems.size());

        List<AggregationEvaluatorInputModel> result = new ArrayList<>();

        for (Map.Entry<String, List<AggregationEvaluatorInputModel>> entry : groupedOriginWithItems.entrySet()) {
            String groupKey = entry.getKey();
            List<AggregationEvaluatorInputModel> group = entry.getValue();

            if (group == null || group.isEmpty())
                continue;
            AggregationEvaluatorInputModel winer = processGroup(group, groupKey);
            if (winer != null) {
                result.add(winer);
            }
        }

        return result;
    }

    private AggregationEvaluatorInputModel processGroup(List<AggregationEvaluatorInputModel> group, String contextKey) {
        log.info("[{}] Processing group with {} records.", contextKey, group.size());

        // 1. Single value check
        if (group.size() == 1) {
            AggregationEvaluatorInputModel row = group.get(0);
            row.setLabelMatching(true);
            row.setLabelMatchMessage(appendMsg(row, "Single row → selected"));
            log.info("[{}] Single row group. Selected ID: {}", contextKey, row.getId());
            return row;
        }

        // 2. Filter Empty Answers
        List<AggregationEvaluatorInputModel> nonEmptyAnswers = group.stream()
                .filter(this::hasNonEmptyAnswer)
                .collect(Collectors.toList());

        if (nonEmptyAnswers.isEmpty()) {
            // All empty, return fallback (first by paperNo)
            AggregationEvaluatorInputModel winner = group.stream()
                    .min(Comparator.comparingLong(this::getSafePaperNo).thenComparingLong(this::getSafeId))
                    .orElse(group.get(0));

            winner.setLabelMatching(true);
            winner.setLabelMatchMessage(appendMsg(winner, "All answers empty → selected by fallback"));

            group.stream().filter(r -> r != winner).forEach(r -> {
                r.setLabelMatching(false);
                r.setLabelMatchMessage(appendMsg(r, "All answers empty → rejected"));
            });
            log.info("[{}] All answers empty. Selected Fallback ID: {}", contextKey, winner.getId());
            return winner;
        }

        // 3. Branching Logic
        boolean hasSectionAlias = nonEmptyAnswers.stream()
                .anyMatch(r -> r.getSectionAlias() != null && !r.getSectionAlias().isBlank());

        log.info("[{}] Branch Decision: hasSectionAlias={} (checked {} candidates)", contextKey, hasSectionAlias,
                nonEmptyAnswers.size());

        AggregationEvaluatorInputModel winner;
        if (hasSectionAlias) {
            winner = filterBySectionPriority(nonEmptyAnswers, group, contextKey);
        } else {
            winner = filterByConsensusAndMajority(nonEmptyAnswers, group, contextKey);
        }

        return winner;
    }

    // --- CASE A: Section Priority Logic ---
    private AggregationEvaluatorInputModel filterBySectionPriority(List<AggregationEvaluatorInputModel> candidates,
            List<AggregationEvaluatorInputModel> allGroupRows, String contextKey) {
        log.info("[{}] Executing Section Priority Logic. Candidates: {}", contextKey, candidates.size());

        List<WhitelistLabelPriority> priorityRules = extractPriorityList(allGroupRows);
        log.info("[{}] Found {} priority rules.", contextKey, priorityRules.size());

        if (!priorityRules.isEmpty()) {
            // Find min priority value across CURRENT candidates using SECTION ALIAS
            Integer minPriority = null;
            for (AggregationEvaluatorInputModel r : candidates) {
                Integer p = getSectionPriority(r, priorityRules);
                if (p != null) {
                    if (minPriority == null || p < minPriority) {
                        minPriority = p;
                    }
                }
            }

            if (minPriority != null) {
                final int best = minPriority;
                log.info("[{}] Best Section Priority found: {}", contextKey, best);
                List<AggregationEvaluatorInputModel> priorityWinners = candidates.stream()
                        .filter(r -> {
                            Integer p = getSectionPriority(r, priorityRules);
                            return p != null && p == best;
                        })
                        .collect(Collectors.toList());

                if (!priorityWinners.isEmpty()) {
                    candidates = priorityWinners;
                    log.info("[{}] Filtered to {} candidates by priority.", contextKey, candidates.size());
                }
            } else {
                log.info("[{}] No matching Section Priority found for candidates.", contextKey);
            }
        } else {
            log.info("[{}] No priority rules configured.", contextKey);
        }

        // Fallback
        return finalizeWinner(candidates, allGroupRows, "Selected by Section Priority/Fallback", contextKey);
    }

    // --- CASE B: Voting / Consensus Logic ---
    private AggregationEvaluatorInputModel filterByConsensusAndMajority(List<AggregationEvaluatorInputModel> candidates,
            List<AggregationEvaluatorInputModel> allGroupRows, String contextKey) {
        log.info("[{}] Executing Voting Logic. Candidates: {}", contextKey, candidates.size());

        // Step 1: Consensus
        boolean consensus = candidates.stream()
                .map(AggregationEvaluatorInputModel::getAnswer)
                .distinct()
                .count() == 1;

        log.info("[{}] Consensus Check: {}", contextKey, consensus);

        if (consensus) {
            // Select min paperNo
            return finalizeWinner(candidates, allGroupRows, "Consensus → selected min paperNo", contextKey);
        }

        // Step 2: Container Majority Voting
        // Count entries per SOR Container
        Map<Long, Long> containerCounts = candidates.stream()
                .filter(r -> r.getSorContainerId() != null)
                .collect(Collectors.groupingBy(AggregationEvaluatorInputModel::getSorContainerId,
                        Collectors.counting()));

        if (!containerCounts.isEmpty()) {
            long maxCount = containerCounts.values().stream().max(Long::compare).orElse(0L);
            log.info("[{}] Container Majority Max Count: {}", contextKey, maxCount);

            // Filter candidates to those belonging to ANY container with maxCount
            List<AggregationEvaluatorInputModel> majorityCandidates = candidates.stream()
                    .filter(r -> r.getSorContainerId() != null
                            && containerCounts.get(r.getSorContainerId()) == maxCount)
                    .collect(Collectors.toList());

            if (!majorityCandidates.isEmpty()) {
                candidates = majorityCandidates; // Narrow down candidates
                log.info("[{}] Filtered to {} candidates by Majority.", contextKey, candidates.size());
            }
        }

        // Step 3c: Priority Rules (Whitelist)
        // Now matching on sorItemLabel (or sectionAlias as fallback if needed, but SQL
        // suggested whitelist_key ~ label)
        List<WhitelistLabelPriority> priorityRules = extractPriorityList(allGroupRows);
        if (!priorityRules.isEmpty()) {
            // Find min priority value across CURRENT candidates
            Integer minPriority = null;
            for (AggregationEvaluatorInputModel r : candidates) {
                Integer p = getPriority(r, priorityRules);
                if (p != null) {
                    if (minPriority == null || p < minPriority) {
                        minPriority = p;
                    }
                }
            }

            if (minPriority != null) {
                final int best = minPriority;
                log.info("[{}] Best Priority (in Voting) found: {}", contextKey, best);
                List<AggregationEvaluatorInputModel> priorityWinners = candidates.stream()
                        .filter(r -> {
                            Integer p = getPriority(r, priorityRules);
                            return p != null && p == best;
                        })
                        .collect(Collectors.toList());

                if (!priorityWinners.isEmpty()) {
                    candidates = priorityWinners; // Narrow down further
                    log.info("[{}] Filtered to {} candidates by Priority (Voting phase).", contextKey,
                            candidates.size());
                }
            }
        }

        // Step 3d: Final Tie-Breaker (PageNo -> ID)
        return finalizeWinner(candidates, allGroupRows, "Selected by Voting Logic/Fallback", contextKey);
    }

    // --- Common Finalizer ---
    private AggregationEvaluatorInputModel finalizeWinner(List<AggregationEvaluatorInputModel> candidates,
            List<AggregationEvaluatorInputModel> allGroupRows, String successMsg, String contextKey) {
        AggregationEvaluatorInputModel winner = candidates.stream()
                .min(Comparator.comparingLong(this::getSafePaperNo)
                        .thenComparingLong(this::getSafeId))
                .orElse(candidates.get(0));

        winner.setLabelMatching(true);
        winner.setLabelMatchMessage(appendMsg(winner, successMsg));

        log.info("[{}] Final Selection - ID: {}, PaperNo: {}, Reason: {}", contextKey, winner.getId(),
                winner.getPaperNo(), successMsg);

        final AggregationEvaluatorInputModel finalWinner = winner;
        allGroupRows.stream().filter(r -> r != finalWinner).forEach(r -> {
            r.setLabelMatching(false);
            r.setLabelMatchMessage(appendMsg(r, "Rejected in voting phase"));
        });

        return winner;
    }

    private Integer getSectionPriority(AggregationEvaluatorInputModel row, List<WhitelistLabelPriority> rules) {
        return getPriority(row, rules);
    }

    private Integer getPriority(AggregationEvaluatorInputModel row, List<WhitelistLabelPriority> rules) {
        // SQL join: tsw.whitelist_key = t.sor_item_label
        String label = row.getSectionAlias();
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

    private boolean hasNonEmptyAnswer(AggregationEvaluatorInputModel row) {
        return row.getAnswer() != null && !row.getAnswer().isBlank();
    }

    private long getSafePaperNo(AggregationEvaluatorInputModel row) {
        return row.getPaperNo() != null ? row.getPaperNo() : Long.MAX_VALUE;
    }

    private long getSafeId(AggregationEvaluatorInputModel row) {
        return row.getId() != null ? row.getId() : Long.MAX_VALUE;
    }

    private List<WhitelistLabelPriority> extractPriorityList(List<AggregationEvaluatorInputModel> rows) {
        // Try to find valid priority rules from any row in the group
        for (AggregationEvaluatorInputModel row : rows) {
            String json = row.getWhitelistedSectionsWithPriority();
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
    private String appendMsg(AggregationEvaluatorInputModel row, String message) {
        String existing = row.getLabelMatchMessage();
        if (existing == null || existing.isBlank())
            return message;
        return existing + " | " + message;
    }
}