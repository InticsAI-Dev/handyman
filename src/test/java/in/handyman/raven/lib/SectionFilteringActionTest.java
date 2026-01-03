package in.handyman.raven.lib;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.lib.adapters.selections.models.SelectionFilteringInputTable;
import in.handyman.raven.lib.model.SectionFiltering;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

public class SectionFilteringActionTest {

    private SectionFilteringAction action;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setUp() {
        action = new SectionFilteringAction(null, null, SectionFiltering.builder().name("Test").build());
    }

    @Test
    public void testComplexScenario_MixedBlacklistAndWhitelist() throws Exception {
        // Scenario: 10 rows
        // - 2 Blacklisted Labels
        // - 2 Blacklisted Sections
        // - 1 Whitelisted Label (Priority 1) -> Winner?
        // - 2 Whitelisted Section (Priority 2, Priority 5)
        // - 3 Normal (No match)

        List<SelectionFilteringInputTable> input = new ArrayList<>();
        String origin = "o1";
        String container = "c1";

        // Setup Configs
        String blacklistedLabels = "BL_Label1, BL_Label2";
        String blacklistedSections = "BL_Sec1, BL_Sec2";
        String wlLabelConfig = createWhitelistLabelJson("TargetLabel"); // P=1
        String wlSectionConfig = createWhitelistSectionJson(Map.of("TargetSectionHigh", 2, "TargetSectionLow", 5));

        // 1-2. Blacklisted Labels
        input.add(createRow(1L, origin, container, "BL_Label1", "S_None", blacklistedLabels, blacklistedSections,
                wlLabelConfig, wlSectionConfig, "0.9"));
        input.add(createRow(2L, origin, container, "BL_Label2", "S_None", blacklistedLabels, blacklistedSections,
                wlLabelConfig, wlSectionConfig, "0.9"));

        // 3-4. Blacklisted Sections
        input.add(createRow(3L, origin, container, "L_None", "BL_Sec1", blacklistedLabels, blacklistedSections,
                wlLabelConfig, wlSectionConfig, "0.9"));
        input.add(createRow(4L, origin, container, "L_None", "BL_Sec2", blacklistedLabels, blacklistedSections,
                wlLabelConfig, wlSectionConfig, "0.9"));

        // 5. Whitelisted Label Match (TargetLabel) - Should be P=1. ID=5
        input.add(createRow(5L, origin, container, "TargetLabel", "S_Random", blacklistedLabels, blacklistedSections,
                wlLabelConfig, wlSectionConfig, "0.8"));

        // 6. Whitelisted Section Match (TargetSectionHigh) - P=2. ID=6
        input.add(createRow(6L, origin, container, "L_Other", "TargetSectionHigh", blacklistedLabels,
                blacklistedSections, wlLabelConfig, wlSectionConfig, "0.9"));

        // 7. Whitelisted Section Match (TargetSectionLow) - P=5. ID=7
        input.add(createRow(7L, origin, container, "L_Other", "TargetSectionLow", blacklistedLabels,
                blacklistedSections, wlLabelConfig, wlSectionConfig, "0.9"));

        // 8-10. No Matches
        input.add(createRow(8L, origin, container, "L_Noise", "S_Noise", blacklistedLabels, blacklistedSections,
                wlLabelConfig, wlSectionConfig, "0.5"));
        input.add(createRow(9L, origin, container, "L_Noise", "S_Noise", blacklistedLabels, blacklistedSections,
                wlLabelConfig, wlSectionConfig, "0.5"));
        input.add(createRow(10L, origin, container, "L_Noise", "S_Noise", blacklistedLabels, blacklistedSections,
                wlLabelConfig, wlSectionConfig, "0.5"));

        // EXECUTE
        List<SelectionFilteringInputTable> result = action.processFiltering(input);

        // VERIFY
        // Winner should be ID=5 (Label match P=1 vs Section P=2 vs Section P=5)
        Assertions.assertEquals(1, result.size());
        SelectionFilteringInputTable winner = result.get(0);
        Assertions.assertEquals(5L, winner.getId());
        Assertions.assertTrue(winner.isLabelMatching());
        Assertions.assertTrue(winner.getLabelMatchMessage().contains("Whitelisted Label"));
        Assertions.assertEquals("1", winner.getLabelPriorityIdx());
    }

    @Test
    public void testTieBreaking_SamePriority_Confidence() throws Exception {
        // Scenario: 5 rows
        // - 3 Rows match Whitelisted Section (Priority 2)
        // - Confidences: 0.5, 0.9, 0.7
        // - Winner should be 0.9

        List<SelectionFilteringInputTable> input = new ArrayList<>();
        String wlSectionConfig = createWhitelistSectionJson(Map.of("TargetSec", 2));

        input.add(createRow(10L, "o1", "c1", "L1", "TargetSec", null, null, null, wlSectionConfig, "0.5"));
        input.add(createRow(11L, "o1", "c1", "L2", "TargetSec", null, null, null, wlSectionConfig, "0.9")); // Winner
        input.add(createRow(12L, "o1", "c1", "L3", "TargetSec", null, null, null, wlSectionConfig, "0.7"));
        input.add(createRow(13L, "o1", "c1", "L4", "OtherSec", null, null, null, wlSectionConfig, "0.9"));
        input.add(createRow(14L, "o1", "c1", "L5", "OtherSec", null, null, null, wlSectionConfig, "0.9"));

        List<SelectionFilteringInputTable> result = action.processFiltering(input);

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(11L, result.get(0).getId());
        Assertions.assertEquals("2", result.get(0).getLabelPriorityIdx());
    }

    @Test
    public void testTieBreaking_SamePriority_SameConfidence_MinID() throws Exception {
        // Scenario:
        // - 3 Rows match Whitelisted Section (Priority 2)
        // - Confidences: All 0.8
        // - IDs: 100, 50, 75
        // - Winner should be ID 50

        List<SelectionFilteringInputTable> input = new ArrayList<>();
        String wlSectionConfig = createWhitelistSectionJson(Map.of("TargetSec", 2));

        input.add(createRow(100L, "o1", "c1", "L1", "TargetSec", null, null, null, wlSectionConfig, "0.8"));
        input.add(createRow(50L, "o1", "c1", "L2", "TargetSec", null, null, null, wlSectionConfig, "0.8")); // Winner
        input.add(createRow(75L, "o1", "c1", "L3", "TargetSec", null, null, null, wlSectionConfig, "0.8"));

        List<SelectionFilteringInputTable> result = action.processFiltering(input);

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(50L, result.get(0).getId());
    }

    @Test
    public void testFallback_ReturnEverything() throws Exception {
        // Scenario: 8 rows
        // - 2 Blacklisted
        // - 6 Normal (No Whitelist match)
        // Expect: All 8 returned (Blacklisted marked false, others marked true/default)

        List<SelectionFilteringInputTable> input = new ArrayList<>();
        String blLabel = "BL1";

        input.add(createRow(1L, "o1", "c1", "BL1", "S1", "BL1", null, null, null, "0.5")); // Blacklisted
        input.add(createRow(2L, "o1", "c1", "BL1", "S2", "BL1", null, null, null, "0.5")); // Blacklisted

        for (long i = 3; i <= 8; i++) {
            input.add(createRow(i, "o1", "c1", "Normal", "S" + i, "BL1", null, null, null, "0.5"));
        }

        List<SelectionFilteringInputTable> result = action.processFiltering(input);

        Assertions.assertEquals(8, result.size());

        // Verify Blacklisted ones
        List<SelectionFilteringInputTable> blRows = result.stream().filter(r -> r.getSorItemLabel().equals("BL1"))
                .collect(Collectors.toList());
        Assertions.assertEquals(2, blRows.size());
        Assertions.assertFalse(blRows.get(0).isLabelMatching());
        Assertions.assertTrue(blRows.get(0).getLabelMatchMessage().contains("Blacklisted"));

        // Verify Others
        List<SelectionFilteringInputTable> normalRows = result.stream()
                .filter(r -> r.getSorItemLabel().equals("Normal")).collect(Collectors.toList());
        Assertions.assertEquals(6, normalRows.size());
        // Logic says: if nothing matched whitelist, return everything.
        // Usually implied 'valid' ones are true? The implementation sets
        // labelMatching=true for fallback.
        Assertions.assertTrue(normalRows.get(0).isLabelMatching());
        Assertions.assertTrue(normalRows.get(0).getLabelMatchMessage().contains("Returned"));
    }

    @Test
    public void testMultiGroupScenario() throws Exception {
        // Group A: Has Winner
        // Group B: Fallback

        List<SelectionFilteringInputTable> input = new ArrayList<>();
        String wlLabelJson = createWhitelistLabelJson("Winner");

        // Group A (o1, c1) - 5 rows
        input.add(createRow(1L, "o1", "c1", "Winner", "S1", null, null, wlLabelJson, null, "0.9")); // Winner
        input.add(createRow(2L, "o1", "c1", "Loser1", "S1", null, null, wlLabelJson, null, "0.9"));
        input.add(createRow(3L, "o1", "c1", "Loser2", "S1", null, null, wlLabelJson, null, "0.9"));
        input.add(createRow(4L, "o1", "c1", "Loser3", "S1", null, null, wlLabelJson, null, "0.9"));
        input.add(createRow(5L, "o1", "c1", "Loser4", "S1", null, null, wlLabelJson, null, "0.9"));

        // Group B (o2, c2) - 5 rows, no config matches
        for (long i = 6; i <= 10; i++) {
            input.add(createRow(i, "o2", "c2", "Gen" + i, "S" + i, null, null, wlLabelJson, null, "0.5"));
        }

        List<SelectionFilteringInputTable> result = action.processFiltering(input);

        // Expect:
        // Group A -> 1 row (Winner)
        // Group B -> 5 rows (All)
        // Total -> 6 rows
        Assertions.assertEquals(6, result.size());

        Assertions.assertTrue(result.stream().anyMatch(r -> r.getId() == 1L));
        Assertions.assertFalse(result.stream().anyMatch(r -> r.getId() == 2L)); // Ensure Loser1 is gone

        long groupBCount = result.stream().filter(r -> r.getOriginId().equals("o2")).count();
        Assertions.assertEquals(5, groupBCount);
    }

    // --- Helpers ---

    private SelectionFilteringInputTable createRow(Long id, String originId, String container,
            String label, String section,
            String blLabels, String blSections,
            String wlLabels, String wlSections,
            String confidence) {
        return SelectionFilteringInputTable.builder()
                .id(id)
                .originId(originId)
                .sorContainerName(container)
                .sorItemLabel(label)
                .sectionAlias(section)
                .blacklistedLabels(blLabels)
                .blacklistedSections(blSections)
                .whitelistedLabels(wlLabels)
                .whitelistedSectionsWithPriority(wlSections)
                .confidence(confidence)
                .build();
    }

    private String createWhitelistLabelJson(String... labels) throws Exception {
        List<Map<String, Object>> list = new ArrayList<>();
        for (String label : labels) {
            Map<String, Object> map = new HashMap<>();
            map.put("whitelistKey", label);
            map.put("labelSearchConfig", "defaultConfig");
            list.add(map);
        }
        return objectMapper.writeValueAsString(list);
    }

    private String createWhitelistSectionJson(Map<String, Integer> priorities) throws Exception {
        List<Map<String, Object>> list = new ArrayList<>();
        priorities.forEach((k, v) -> {
            Map<String, Object> map = new HashMap<>();
            map.put("whitelistKey", k);
            map.put("sectionPriority", v);
            map.put("sectionSearchConfig", "defaultConfig");
            list.add(map);
        });
        return objectMapper.writeValueAsString(list);
    }
}
