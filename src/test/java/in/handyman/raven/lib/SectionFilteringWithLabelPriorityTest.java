package in.handyman.raven.lib;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.lib.adapters.selections.LabelWithPriorityProcessor;
import in.handyman.raven.lib.adapters.selections.models.SelectionFilteringInputTable;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class SectionFilteringWithLabelPriorityTest {

    private SelectionFilteringInputTable row(
            String origin,
            String sorItem,
            String answer,
            boolean labelMatching,
            String label,
            String whitelistPriorityJson
    ) {
        SelectionFilteringInputTable r = new SelectionFilteringInputTable();
        r.setOriginId(origin);
        r.setSorItemName(sorItem);
        r.setAnswer(answer);
        r.setSorItemLabel(label);
        r.setLabelMatching(labelMatching);
        r.setWhitelistedLabelsWithPriority(whitelistPriorityJson);
        r.setLabelMatchMessage("");
        return r;
    }

    private SelectionFilteringInputTable rowId(
            Long id,
            String origin,
            Long paperNo,
            String sorItem,
            String answer,
            boolean labelMatching,
            String label,
            String whitelistPriorityJson
    ) {
        SelectionFilteringInputTable r = new SelectionFilteringInputTable();
        r.setId(id);
        r.setOriginId(origin);
        r.setPaperNo(paperNo);
        r.setSorItemName(sorItem);
        r.setAnswer(answer);
        r.setSorItemLabel(label);
        r.setLabelMatching(labelMatching);
        r.setWhitelistedLabelsWithPriority(whitelistPriorityJson);
        r.setLabelMatchMessage("");
        return r;
    }
//
//    @Test
//    public void testSingleRow() {
//        List<SelectionFilteringInputTable> input = List.of(
//                row("origin1", "ITEM_A", "someAnswer", true, "", null)
//        );
//
//        List<String> messages = new ArrayList<>();
//        LabelWithPriorityProcessor processor = new LabelWithPriorityProcessor(new ObjectMapper());
//
//        var result = processor.process(input);
//        Map<String, Map<String, List<SelectionFilteringInputTable>>> grouped = getStringMapMap(result);
//        System.out.println("testSingleRow");
//
//        printProcessedResult(grouped);
//    }
//
//
//    @Test
//    public void testTwoRowsOneNonEmpty() {
//        List<SelectionFilteringInputTable> input = List.of(
//                row("origin1", "ITEM_A", "VALUE", true, "", null),
//                row("origin1", "ITEM_A", "",      true, "", null)
//        );
//
//        List<String> messages = new ArrayList<>();
//        var processor = new LabelWithPriorityProcessor(new ObjectMapper());
//
//        var result = processor.process(input);
//        Map<String, Map<String, List<SelectionFilteringInputTable>>> grouped = getStringMapMap(result);
//        System.out.println("testTwoRowsOneNonEmpty");
//        printProcessedResult(grouped);
//    }
//
//
//    @Test
//    public void testTwoRowsBothEmpty() {
//        List<SelectionFilteringInputTable> input = List.of(
//                row("origin1", "ITEM_A", "", true, "", null),
//                row("origin1", "ITEM_A", "", true, "", null)
//        );
//
//        List<String> messages = new ArrayList<>();
//        var processor = new LabelWithPriorityProcessor(new ObjectMapper());
//
//        var result = processor.process(input);
//        Map<String, Map<String, List<SelectionFilteringInputTable>>> grouped = getStringMapMap(result);
//        System.out.println("testTwoRowsBothEmpty");
//        printProcessedResult(grouped);
//    }
//
//
//    @Test
//    public void testPriorityMapSelection() {
//        String priorityJson = "{\"KEY1\":1, \"KEY2\":2}";
//
//        List<SelectionFilteringInputTable> input = List.of(
//                row("origin1", "ITEM_A", "A", true, "KEY2", priorityJson),
//                row("origin1", "ITEM_A", "B", true, "KEY1", priorityJson)
//        );
//
//        List<String> messages = new ArrayList<>();
//        var processor = new LabelWithPriorityProcessor(new ObjectMapper());
//
//        var result = processor.process(input);
//        Map<String, Map<String, List<SelectionFilteringInputTable>>> grouped = getStringMapMap(result);
//        System.out.println("testPriorityMapSelection");
//        printProcessedResult(grouped);
//    }
//
//
//    @Test
//    public void testNoPriorityMap() {
//        List<SelectionFilteringInputTable> input = List.of(
//                row("origin1", "ITEM_A", "A", true, "X", null),
//                row("origin1", "ITEM_A", "B", true, "Y", null),
//                row("origin1", "ITEM_A", "C", true, "Z", "")
//        );
//
//        List<String> messages = new ArrayList<>();
//        var processor = new LabelWithPriorityProcessor(new ObjectMapper());
//
//        var result = processor.process(input);
//        Map<String, Map<String, List<SelectionFilteringInputTable>>> grouped = getStringMapMap(result);
//        System.out.println("testNoPriorityMap");
//
//        printProcessedResult(grouped);
//    }
//
//
//    @Test
//    public void testMultipleSorItems() {
//        List<SelectionFilteringInputTable> input = List.of(
//                row("origin1", "ITEM_A", "A1", true, "KEY1", "{\"KEY1\":1}"),
//                row("origin1", "ITEM_A", "A2", true, "KEY2", "{\"KEY1\":1}"),
//
//                row("origin1", "ITEM_B", "B1", true, "K2", null),
//                row("origin1", "ITEM_B", "B2", true, "K3", null)
//        );
//
//        List<String> messages = new ArrayList<>();
//        var processor = new LabelWithPriorityProcessor(new ObjectMapper());
//
//        var result = processor.process(input);
//        Map<String, Map<String, List<SelectionFilteringInputTable>>> grouped = getStringMapMap(result);
//        System.out.println("testMultipleSorItems");
//
//        printProcessedResult(grouped);
//    }
//
//    @Test
//    public void testIgnoreNonMatchingRows() {
//        List<SelectionFilteringInputTable> input = List.of(
//                row("origin1", "ITEM_A", "A", false, "KEY1", "{\"whitelistKey\":\"key1\",\"sectionPriority\":\"1\",\"sectionSearchConfig\":\"CONTAINS\"}"),
//                row("origin1", "ITEM_A", "B", true,  "KEY2", "{\"whitelistKey\":\"key2\",\"sectionPriority\":\"2\",\"sectionSearchConfig\":\"CONTAINS\"}")
//        );
//
//        List<String> messages = new ArrayList<>();
//        var processor = new LabelWithPriorityProcessor(new ObjectMapper());
//
//        var result = processor.process(input);
//        Map<String, Map<String, List<SelectionFilteringInputTable>>> grouped = getStringMapMap(result);
//        System.out.println("testIgnoreNonMatchingRows");
//
//        printProcessedResult(grouped);
//    }
//
//
//    @Test
//    public void testInvalidPriorityMap() {
//        List<SelectionFilteringInputTable> input = List.of(
//                row("origin1", "ITEM_A", "A", true, "1", "{invalid_json"),
//                row("origin1", "ITEM_A", "B", true, "1", "{invalid_json")
//        );
//
//        List<String> messages = new ArrayList<>();
//        var processor = new LabelWithPriorityProcessor(new ObjectMapper());
//
//        var result = processor.process(input);
//        Map<String, Map<String, List<SelectionFilteringInputTable>>> grouped = getStringMapMap(result);
//        System.out.println("testInvalidPriorityMap");
//
//        printProcessedResult(grouped);
//    }
//
//
//    @Test
//    public void testMissingKeysInPriorityMap() {
//        String priorityJson = "{\"whitelistKey\":\"X\",\"sectionPriority\":\"1\",\"sectionSearchConfig\":\"CONTAINS\"}";
//
//        List<SelectionFilteringInputTable> input = List.of(
//                row("origin1", "ITEM_A", "A", true, "X", priorityJson),
//                row("origin1", "ITEM_A", "B", true, "Y", priorityJson) // no key Y → becomes MAX_VALUE
//        );
//
//        List<String> messages = new ArrayList<>();
//        var processor = new LabelWithPriorityProcessor(new ObjectMapper());
//
//        var result = processor.process(input);
//        Map<String, Map<String, List<SelectionFilteringInputTable>>> grouped = getStringMapMap(result);
//        System.out.println("testMissingKeysInPriorityMap");
//
//        printProcessedResult(grouped);
//    }
//
//    @Test
//    public void testMultipleSorItems2() {
//        List<SelectionFilteringInputTable> input = List.of(
//                // ITEM_A row contains three entries with different priorities
//                rowId(1L,"origin1", 1L, "ITEM_A", "A1", true, "KEY2", "[{\"whitelistKey\":\"key1\",\"sectionPriority\":\"1\",\"sectionSearchConfig\":\"CONTAINS\"},{\"whitelistKey\":\"key2\",\"sectionPriority\":\"2\",\"sectionSearchConfig\":\"CONTAINS\"}]"),
//                rowId(2L,"origin1", 3L,"ITEM_A", "A2", true, "KEY1", "[{\"whitelistKey\":\"key1\",\"sectionPriority\":\"1\",\"sectionSearchConfig\":\"CONTAINS\"},{\"whitelistKey\":\"key2\",\"sectionPriority\":\"2\",\"sectionSearchConfig\":\"CONTAINS\"}]"),
//                rowId(3L,"origin1", 2L,"ITEM_A", "A3", true, "", "[{\"whitelistKey\":\"key1\",\"sectionPriority\":\"1\",\"sectionSearchConfig\":\"CONTAINS\"},{\"whitelistKey\":\"key2\",\"sectionPriority\":\"2\",\"sectionSearchConfig\":\"CONTAINS\"}]"),
//
//                // ITEM_A row contains three entries with different priorities
//                rowId(1L,"origin1", 1L, "ITEM_D", "A1", true, "KEY1", "[{\"whitelistKey\":\"key1\",\"sectionPriority\":\"1\",\"sectionSearchConfig\":\"CONTAINS\"},{\"whitelistKey\":\"key2\",\"sectionPriority\":\"2\",\"sectionSearchConfig\":\"CONTAINS\"}]"),
//                rowId(2L,"origin1", 3L,"ITEM_D", "A2", true, "KEY1", "[{\"whitelistKey\":\"key1\",\"sectionPriority\":\"1\",\"sectionSearchConfig\":\"CONTAINS\"},{\"whitelistKey\":\"key2\",\"sectionPriority\":\"2\",\"sectionSearchConfig\":\"CONTAINS\"}]"),
//                rowId(3L,"origin1", 2L,"ITEM_D", "A3", true, "", "[{\"whitelistKey\":\"key1\",\"sectionPriority\":\"1\",\"sectionSearchConfig\":\"CONTAINS\"},{\"whitelistKey\":\"key2\",\"sectionPriority\":\"2\",\"sectionSearchConfig\":\"CONTAINS\"}]"),
//
//                // ITEM_A row contains three entries with different priorities
//                rowId(2L,"origin1", 1L, "ITEM_E", "A1", true, "KEY1", "[{\"whitelistKey\":\"key1\",\"sectionPriority\":\"1\",\"sectionSearchConfig\":\"CONTAINS\"},{\"whitelistKey\":\"key2\",\"sectionPriority\":\"2\",\"sectionSearchConfig\":\"CONTAINS\"}]"),
//                rowId(1L,"origin1", 1L,"ITEM_E", "A2", true, "KEY1", "[{\"whitelistKey\":\"key1\",\"sectionPriority\":\"1\",\"sectionSearchConfig\":\"CONTAINS\"},{\"whitelistKey\":\"key2\",\"sectionPriority\":\"2\",\"sectionSearchConfig\":\"CONTAINS\"}]"),
//                rowId(3L,"origin1", 2L,"ITEM_E", "A3", true, "", "[{\"whitelistKey\":\"key1\",\"sectionPriority\":\"1\",\"sectionSearchConfig\":\"CONTAINS\"},{\"whitelistKey\":\"key2\",\"sectionPriority\":\"2\",\"sectionSearchConfig\":\"CONTAINS\"}]"),
//
//                //ITEM_B row contains two entries without priority map -> min of paper wins
//                rowId(4L,"origin1", 2L,"ITEM_B", "B1", true, "K2", null),
//                rowId(5L,"origin1", 1L,"ITEM_B", "B2", true, "K3", null),
//
//                //ITEM_B row contains two entries without priority map -> min of paper wins
//                rowId(4L,"origin1", 2L,"ITEM_f", "B1", true, null, null),
//                rowId(5L,"origin1", 1L,"ITEM_f", "B2", true, null, null),
//
//
//                // ITEM_C row contains three entries with no labels present -> min of paper wins
//                rowId(1L,"origin1", 1L, "ITEM_C", "A1", false, "", "[{\"whitelistKey\":\"key1\",\"sectionPriority\":\"1\",\"sectionSearchConfig\":\"CONTAINS\"},{\"whitelistKey\":\"key2\",\"sectionPriority\":\"2\",\"sectionSearchConfig\":\"CONTAINS\"}]"),
//                rowId(2L,"origin1", 1L,"ITEM_C", "A2", false, "", "[{\"whitelistKey\":\"key1\",\"sectionPriority\":\"1\",\"sectionSearchConfig\":\"CONTAINS\"},{\"whitelistKey\":\"key2\",\"sectionPriority\":\"2\",\"sectionSearchConfig\":\"CONTAINS\"}]"),
//                rowId(3L,"origin1", 2L,"ITEM_C", "A3", false, "", "[{\"whitelistKey\":\"key1\",\"sectionPriority\":\"1\",\"sectionSearchConfig\":\"CONTAINS\"},{\"whitelistKey\":\"key2\",\"sectionPriority\":\"2\",\"sectionSearchConfig\":\"CONTAINS\"}]")
//
//                );
//
//        List<String> messages = new ArrayList<>();
//        var processor = new LabelWithPriorityProcessor(new ObjectMapper());
//
//        var result = processor.process(input);
//        Map<String, Map<String, List<SelectionFilteringInputTable>>> grouped = getStringMapMap(result);
//
//        System.out.println("testMultipleSorItems2");
//
//        printProcessedResult(grouped);
//    }
//
//
//
//
//    @NotNull
//    private static Map<String, Map<String, List<SelectionFilteringInputTable>>> getStringMapMap(List<SelectionFilteringInputTable> result) {
//        Map<String, Map<String, List<SelectionFilteringInputTable>>> grouped = result.stream()
//                .collect(Collectors.groupingBy(
//                        SelectionFilteringInputTable::getOriginId,
//                        Collectors.groupingBy(SelectionFilteringInputTable::getSorItemName)
//                ));
//        return grouped;
//    }
//
//    public static void printProcessedResult(Map<String, Map<String, List<SelectionFilteringInputTable>>> result) {
//        if (result == null || result.isEmpty()) {
//            System.out.println("<No results>");
//            return;
//        }
//
//        System.out.println("\n================= PROCESSED LABEL RESULTS =================");
//
//        result.forEach((originId, sorMap) -> {
//            System.out.println("Origin ID: " + originId);
//            sorMap.forEach((sorItemName, rows) -> {
//                System.out.println("  SOR Item: " + sorItemName);
//                for (SelectionFilteringInputTable row : rows) {
//                    System.out.println("    Paper No           : " + row.getPaperNo());
//                    System.out.println("    Answer             : " + row.getAnswer());
//                    System.out.println("    Label              : " + row.getSorItemLabel());
//                    System.out.println("    Label PriorityIdx  : " + row.getLabelPriorityIdx());
//                    System.out.println("    Label Matching     : " + row.isLabelMatching());
//                    System.out.println("    Label Match Message: " + row.getLabelMatchMessage());
////                    System.out.println("    Additional Info    : ");
////                    System.out.println("      Tenant ID        : " + row.getTenantId());
////                    System.out.println("      Batch ID         : " + row.getBatchId());
////                    System.out.println("      Section Alias    : " + row.getSectionAlias());
////                    System.out.println("      Confidence       : " + row.getConfidence());
////                    System.out.println("      BBox             : " + row.getBbox());
//                    System.out.println("---------------------------------------------------------");
//                }
//                System.out.println();
//            });
//            System.out.println();
//        });
//
//        System.out.println("============================================================\n");
//    }

}
