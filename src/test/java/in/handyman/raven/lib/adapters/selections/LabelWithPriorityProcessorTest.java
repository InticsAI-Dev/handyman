package in.handyman.raven.lib.adapters.selections;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.lib.adapters.selections.models.AggregationEvaluatorInputModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class LabelWithPriorityProcessorTest {
//
//    private LabelWithPriorityProcessor processor;
//    private ObjectMapper mapper = new ObjectMapper();
//    private Logger log;
//
//    @BeforeEach
//    void setUp() {
//        log = Mockito.mock(Logger.class);
//        processor = new LabelWithPriorityProcessor(mapper, log);
//    }
//
//    // --- TEST BRANCH A: SECTION PRIORITY ---
//    // Triggered when sectionAlias is PRESENT
//
//    @Test
//    void testSectionPriority_BranchA() {
//        // Has sectionAlias -> enforces Section Priority logic
//        // Rule: sectionA(1) < sectionB(10)
//
//        String priorityJson = "[{\"whitelistKey\":\"sectionA\",\"labelPriority\":1, \"labelSearchConfig\":\"EXACT\"}, {\"whitelistKey\":\"sectionB\",\"labelPriority\":10, \"labelSearchConfig\":\"EXACT\"}]";
//
//        AggregationEvaluatorInputModel item1 = createItem(1L, "o1", "s1", "A", "sectionB", 1L, 100L);
//        item1.setWhitelistedSectionsWithPriority(priorityJson);
//
//        AggregationEvaluatorInputModel item2 = createItem(2L, "o1", "s1", "B", "sectionA", 1L, 200L);
//        item2.setWhitelistedSectionsWithPriority(priorityJson);
//
//        List<AggregationEvaluatorInputModel> result = processor.process(List.of(item1, item2));
//
//        assertEquals(1, result.size());
//        assertEquals(2L, result.get(0).getId());
//        assertTrue(result.get(0).getLabelMatchMessage().contains("Section Priority"));
//    }
//
//    @Test
//    void testSectionPriority_BranchA_NoMatchFallback() {
//        // Has sectionAlias, but no matching rules. Fallback to min PaperNo.
//        AggregationEvaluatorInputModel item1 = createItem(1L, "o1", "s1", "A", "sectionZ", 5L, 100L);
//        AggregationEvaluatorInputModel item2 = createItem(2L, "o1", "s1", "B", "sectionY", 2L, 200L);
//
//        List<AggregationEvaluatorInputModel> result = processor.process(List.of(item1, item2));
//
//        assertEquals(1, result.size());
//        assertEquals(2L, result.get(0).getId()); // Min paperNo
//    }
//
//    // --- TEST BRANCH B: VOTING ---
//    // Triggered when sectionAlias is NULL/BLANK
//
//    @Test
//    void testConsensus_BranchB() {
//        // No sectionAlias -> Voting Logic
//        AggregationEvaluatorInputModel item1 = createItem(10L, "o1", "s1", "ANSWER_A", null, 5L, 100L);
//        AggregationEvaluatorInputModel item2 = createItem(11L, "o1", "s1", "ANSWER_A", null, 2L, 200L);
//        AggregationEvaluatorInputModel item3 = createItem(12L, "o1", "s1", "ANSWER_A", null, 5L, 300L);
//
//        List<AggregationEvaluatorInputModel> result = processor.process(List.of(item1, item2, item3));
//
//        assertEquals(1, result.size());
//        assertEquals(11L, result.get(0).getId());
//        assertTrue(result.get(0).getLabelMatchMessage().contains("Consensus"));
//    }
//
//    @Test
//    void testContainerMajority_BranchB() {
//        // No sectionAlias
//        // C100: 2 records (A, B)
//        // C200: 1 record (C)
//        // Winner from C100 (A has lower paperNo than B)
//
//        AggregationEvaluatorInputModel itemA = createItem(1L, "o1", "s1", "A", "", 5L, 100L);
//        AggregationEvaluatorInputModel itemB = createItem(2L, "o1", "s1", "B", "", 6L, 100L);
//        AggregationEvaluatorInputModel itemC = createItem(3L, "o1", "s1", "C", "", 1L, 200L);
//
//        List<AggregationEvaluatorInputModel> result = processor.process(List.of(itemA, itemB, itemC));
//
//        assertEquals(1, result.size());
//        assertEquals(1L, result.get(0).getId());
//        // Logic path: Not consensus -> Container 100 majority -> A wins tie-break
//    }
//
//    /*
//     * DEPRECATED SCENARIO?
//     * User said:
//     * "dont use sorItemLabel no need for that here i need only the section filtering"
//     *
//     * However, the code path for Branch B (Voting) still has a "Priority Rules"
//     * step (Step 3c).
//     * And in current code (Step 158 diff), it calls extractPriorityList(group) and
//     * uses getPriority(r, rules).
//     * getPriority uses row.getSectionAlias().
//     * BUT in Branch B, sectionAlias is NULL/BLANK (that's why we are in Branch B!).
//     * So getPriority(row, rules) -> getSectionAlias() -> null -> returns null
//     * priority.
//     * UseCase: Maybe user wants to check sectionAlias even if representative
//     * checked null?
//     * Or maybe Step 3c is effectively effectively dead for Branch B if sectionAlias
//     * is consistently null.
//     *
//     * If sectionAlias is truly null for all items in Branch B, then Priority Step
//     * 3c is skipped.
//     *
//     * Let's test consistent null fallback.
//     */
//
//    @Test
//    void testVotingFallback_BranchB() {
//        // No consensus, equal containers, no sectionAlias (so no priority match).
//        // Fallback to min PaperNo.
//        AggregationEvaluatorInputModel item1 = createItem(10L, "o1", "s1", "A", null, 5L, 100L);
//        AggregationEvaluatorInputModel item2 = createItem(20L, "o1", "s1", "B", null, 2L, 200L);
//
//        List<AggregationEvaluatorInputModel> result = processor.process(List.of(item1, item2));
//
//        assertEquals(1, result.size());
//        assertEquals(20L, result.get(0).getId()); // Min paperNo
//    }
//
//    private AggregationEvaluatorInputModel createItem(Long id, String originId, String sorItemName,
//            String answer, String sectionAlias, Long paperNo, Long containerId) {
//        AggregationEvaluatorInputModel item = new AggregationEvaluatorInputModel();
//        item.setId(id);
//        item.setOriginId(originId);
//        item.setSorItemName(sorItemName);
//        item.setAnswer(answer);
//        // sorItemLabel removed
//        item.setSectionAlias(sectionAlias);
//        item.setPaperNo(paperNo != null ? paperNo.intValue() : null);
//        item.setSorContainerId(containerId);
//        item.setLabelMatching(true);
//        return item;
//    }
//
//    @Test
//    void testMemberPriorityWorking() throws JsonProcessingException {
//        // 1. Define Priority Rules
//        String priorityJson = "[{\"whitelistKey\": \"PATIENT\", \"labelPriority\": 1, \"labelSearchConfig\": \"CONTAINS\"}, " +
//                "{\"whitelistKey\": \"ENROLLEE\", \"labelPriority\": 3, \"labelSearchConfig\": \"CONTAINS\"}, " +
//                "{\"whitelistKey\": \"PRIMARY INSURANCE\", \"labelPriority\": 5, \"labelSearchConfig\": \"CONTAINS\"}, " +
//                "{\"whitelistKey\": \"COVERAGE\", \"labelPriority\": 6, \"labelSearchConfig\": \"CONTAINS\"}, " +
//                "{\"whitelistKey\": \"MEMBER\", \"labelPriority\": 2, \"labelSearchConfig\": \"CONTAINS\"}, " +
//                "{\"whitelistKey\": \"CONSERVATOR\", \"labelPriority\": 9, \"labelSearchConfig\": \"CONTAINS\"}, " +
//                "{\"whitelistKey\": \"PAYER\", \"labelPriority\": 10, \"labelSearchConfig\": \"CONTAINS\"}, " +
//                "{\"whitelistKey\": \"Resident Information\", \"labelPriority\": 10, \"labelSearchConfig\": \"CONTAINS\"}, " +
//                "{\"whitelistKey\": \"SUBSCRIBER\", \"labelPriority\": 4, \"labelSearchConfig\": \"CONTAINS\"}, " +
//                "{\"whitelistKey\": \"RECIPIENT INFORMATION\", \"labelPriority\": 8, \"labelSearchConfig\": \"CONTAINS\"}]";
//        List<AggregationEvaluatorInputModel> input = new ArrayList<>();
//
//        // Helper to add row
//        // Mapping: transaction_id -> id, sor_container_instance -> sorContainerId (hashcode for simplicity)
//        // section_alias -> sectionAlias, origin_id -> originId, sor_item_name -> sorItemName
//
//        // Multi-row scenarios
//        input.add(createRow(
//                684L,
//                "member_full_name",
//                "MEMBER_DETAILS_0",
//                "Newsome, Christopher A",
//                "",
//                priorityJson
//        ));
//
//        input.add(createRow(
//                1236L,
//                "member_full_name",
//                "MEMBER_DETAILS_0",
//                "Newsome, Christopher A",
//                "",
//                priorityJson
//        ));
//
//        input.add(createRow(
//                1837L,
//                "member_full_name",
//                "MEMBER_DETAILS_0",
//                "Newsome, Christopher A",
//                "",
//                priorityJson
//        ));
//
//        input.add(createRow(
//                1948L,
//                "member_full_name",
//                "MEMBER_DETAILS_0",
//                "Newsome, Christopher A",
//                "",
//                priorityJson
//        ));
//
//        input.add(createRow(
//                1973L,
//                "member_full_name",
//                "MEMBER_DETAILS_0",
//                "RAN NVA",
//                "Patient Information",
//                priorityJson
//        ));
//
//        input.add(createRow(
//                2367L,
//                "member_full_name",
//                "MEMBER_DETAILS_0",
//                "Newsome, Christopher A",
//                "",
//                priorityJson
//        ));
//
//        input.add(createRow(
//                2446L,
//                "member_full_name",
//                "MEMBER_DETAILS_0",
//                "Newsome, Christopher A",
//                "",
//                priorityJson
//        ));
//
//
//        // PROCESS
//        List<AggregationEvaluatorInputModel> results = processor.process(input);
//
//        // ASSERTIONS
//        // Total groups = unique sor_item_name
//        Set<String> uniqueItems = input.stream().map(AggregationEvaluatorInputModel::getSorItemName).collect(Collectors.toSet());
//        assertEquals(uniqueItems.size(), results.size(), "Should produce one winner per sor_item_name");
//
//        // Check specific winners
//        Map<String, AggregationEvaluatorInputModel> winnersMap = results.stream()
//                .collect(Collectors.toMap(AggregationEvaluatorInputModel::getSorItemName, r -> r));
//
//        AggregationEvaluatorInputModel memberNameWinner = winnersMap.get("member_full_name");
//
//
//        System.out.println("All validations passed "+ memberNameWinner);
//    }
//
//
//
//    @Test
//    void testFullVqaTransactionScenario() throws JsonProcessingException {
//        // 1. Define Priority Rules
//        String priorityJson = "[{\"whitelistKey\": \"PATIENT\", \"sectionPriority\": 1, \"sectionSearchConfig\": \"CONTAINS\"}, " +
//                "{\"whitelistKey\": \"ENROLLEE\", \"sectionPriority\": 3, \"sectionSearchConfig\": \"CONTAINS\"}, " +
//                "{\"whitelistKey\": \"PRIMARY INSURANCE\", \"sectionPriority\": 5, \"sectionSearchConfig\": \"CONTAINS\"}, " +
//                "{\"whitelistKey\": \"COVERAGE\", \"sectionPriority\": 6, \"sectionSearchConfig\": \"CONTAINS\"}, " +
//                "{\"whitelistKey\": \"MEMBER\", \"sectionPriority\": 2, \"sectionSearchConfig\": \"CONTAINS\"}, " +
//                "{\"whitelistKey\": \"CONSERVATOR\", \"sectionPriority\": 9, \"sectionSearchConfig\": \"CONTAINS\"}, " +
//                "{\"whitelistKey\": \"PAYER\", \"sectionPriority\": 10, \"sectionSearchConfig\": \"CONTAINS\"}, " +
//                "{\"whitelistKey\": \"Resident Information\", \"sectionPriority\": 10, \"sectionSearchConfig\": \"CONTAINS\"}, " +
//                "{\"whitelistKey\": \"SUBSCRIBER\", \"sectionPriority\": 4, \"sectionSearchConfig\": \"CONTAINS\"}, " +
//                "{\"whitelistKey\": \"RECIPIENT INFORMATION\", \"sectionPriority\": 8, \"sectionSearchConfig\": \"CONTAINS\"}]";
//
//        List<AggregationEvaluatorInputModel> input = new ArrayList<>();
//
//        // Helper to add row
//        // Mapping: transaction_id -> id, sor_container_instance -> sorContainerId (hashcode for simplicity)
//        // section_alias -> sectionAlias, origin_id -> originId, sor_item_name -> sorItemName
//
//        // --- ADDING DATA ROWS ---
//        input.add(createRow(2471L, "servicing_facility_city", "SERVICING_FACILITY_DETAILS", "", null, priorityJson));
//        input.add(createRow(2469L, "servicing_facility_address_line1", "SERVICING_FACILITY_DETAILS", "", null, priorityJson));
//        input.add(createRow(2467L, "servicing_facility_zipcode", "SERVICING_FACILITY_DETAILS", "", null, priorityJson));
//        input.add(createRow(2465L, "referring_provider_npi", "REFERRING_PROVIDER_DETAILS", "", null, priorityJson));
//        input.add(createRow(2462L, "ordering_provider_state", "ORDERING_PROVIDER_DETAILS", "", null, priorityJson));
//        input.add(createRow(2460L, "servicing_facility_state", "SERVICING_FACILITY_DETAILS", "", null, priorityJson));
//        input.add(createRow(2458L, "servicing_facility_tin", "SERVICING_FACILITY_DETAILS", "", null, priorityJson));
//        input.add(createRow(2456L, "servicing_facility_npi", "SERVICING_FACILITY_DETAILS", "", null, priorityJson));
//        input.add(createRow(2454L, "referring_provider_address_line1", "REFERRING_PROVIDER_DETAILS", "", null, priorityJson));
//        input.add(createRow(2452L, "referring_provider_city", "REFERRING_PROVIDER_DETAILS", "", null, priorityJson));
//        input.add(createRow(2450L, "referring_provider_state", "REFERRING_PROVIDER_DETAILS", "", null, priorityJson));
//        input.add(createRow(2448L, "referring_provider_last_name", "REFERRING_PROVIDER_DETAILS", "", null, priorityJson));
//        input.add(createRow(2446L, "servicing_facility_last_name", "SERVICING_FACILITY_DETAILS", "", null, priorityJson));
//        input.add(createRow(2444L, "referring_provider_zipcode", "REFERRING_PROVIDER_DETAILS", "", null, priorityJson));
//        input.add(createRow(2442L, "referring_provider_tin", "REFERRING_PROVIDER_DETAILS", "", null, priorityJson));
//        input.add(createRow(2439L, "ordering_provider_address_line1", "ORDERING_PROVIDER_DETAILS", "", null, priorityJson));
//        input.add(createRow(2436L, "ordering_provider_city", "ORDERING_PROVIDER_DETAILS", "", null, priorityJson));
//        input.add(createRow(2433L, "ordering_provider_zipcode", "ORDERING_PROVIDER_DETAILS", "", null, priorityJson));
//        input.add(createRow(2430L, "ordering_provider_last_name", "ORDERING_PROVIDER_DETAILS", "", null, priorityJson));
//        input.add(createRow(2427L, "ordering_provider_npi", "ORDERING_PROVIDER_DETAILS", "", null, priorityJson));
//        input.add(createRow(2424L, "ordering_provider_tin", "ORDERING_PROVIDER_DETAILS", "", null, priorityJson));
//        input.add(createRow(2421L, "auth_discharge_date", "AUTH_DISCHARGE_DATE", "", null, priorityJson));
//        input.add(createRow(2418L, "undefined_provider_last_name", "UNDEFINED_PROVIDER_DETAILS", "", null, priorityJson));
//        input.add(createRow(2415L, "undefined_provider_first_name", "UNDEFINED_PROVIDER_DETAILS", "", null, priorityJson));
//        input.add(createRow(2412L, "undefined_provider_full_name", "UNDEFINED_PROVIDER_DETAILS", "", null, priorityJson));
//        input.add(createRow(2409L, "undefined_provider_tin", "UNDEFINED_PROVIDER_DETAILS", "", null, priorityJson));
//        input.add(createRow(2406L, "undefined_provider_npi", "UNDEFINED_PROVIDER_DETAILS", "", null, priorityJson));
//        input.add(createRow(2403L, "undefined_provider_zipcode", "UNDEFINED_PROVIDER_DETAILS", "", null, priorityJson));
//        input.add(createRow(2400L, "undefined_provider_state", "UNDEFINED_PROVIDER_DETAILS", "", null, priorityJson));
//        input.add(createRow(2397L, "undefined_provider_city", "UNDEFINED_PROVIDER_DETAILS", "", null, priorityJson));
//        input.add(createRow(2394L, "undefined_provider_address_line1", "UNDEFINED_PROVIDER_DETAILS", "", null, priorityJson));
//
//        input.add(createRow(2391L, "newborn_date_of_birth", "NEWBORN_DETAILS", "", null, priorityJson));
//        input.add(createRow(2388L, "newborn_first_name", "NEWBORN_DETAILS", "", null, priorityJson));
//        input.add(createRow(2385L, "newborn_full_name", "NEWBORN_DETAILS", "", null, priorityJson));
//        input.add(createRow(2382L, "newborn_gender", "NEWBORN_DETAILS", "", null, priorityJson));
//        input.add(createRow(2379L, "newborn_last_name", "NEWBORN_DETAILS", "", null, priorityJson));
//        input.add(createRow(2376L, "newborn_request", "NEWBORN_DETAILS", "", null, priorityJson));
//
//        input.add(createRow(2373L, "ordering_provider_full_name", "ORDERING_PROVIDER_DETAILS", "", null, priorityJson));
//        input.add(createRow(2370L, "ordering_provider_first_name", "ORDERING_PROVIDER_DETAILS", "", null, priorityJson));
//        input.add(createRow(2368L, "referring_provider_full_name", "REFERRING_PROVIDER_DETAILS", "", null, priorityJson));
//        input.add(createRow(2366L, "referring_provider_first_name", "REFERRING_PROVIDER_DETAILS", "", null, priorityJson));
//
//        input.add(createRow(2364L, "servicing_facility_full_name", "SERVICING_FACILITY_DETAILS", "", null, priorityJson));
//        input.add(createRow(2362L, "servicing_facility_first_name", "SERVICING_FACILITY_DETAILS", "", null, priorityJson));
//        input.add(createRow(2360L, "service_to_date", "SERVICE_TO_DATE", "", null, priorityJson));
//        input.add(createRow(2357L, "total_service_days", "TOTAL_SERVICE_DAYS", "", null, priorityJson));
//
//        // Multi-row scenarios
//        input.add(createRow(2336L, "member_full_name", "MEMBER_DETAILS", "", null, priorityJson));
//        input.add(createRow(2333L, "member_full_name", "MEMBER_DETAILS", "", null, priorityJson));
//        input.add(createRow(2331L, "member_full_name", "MEMBER_DETAILS", "", null, priorityJson));
//
//        // Priority Scenarios (Branch A)
//        input.add(createRow(2323L, "member_id", "MEMBER_DETAILS_0", "JQC98958556G", "PRIMARY INSURANCE NAME", priorityJson));
//        input.add(createRow(2320L, "member_last_name", "MEMBER_DETAILS_0", "Barajas", "PATIENT INFORMATION", priorityJson));
//        input.add(createRow(2315L, "member_gender", "MEMBER_DETAILS_0", "F", "PATIENT INFORMATION", priorityJson));
//        input.add(createRow(2308L, "member_address_line1", "MEMBER_DETAILS_0", "830 S Azusa Ave Trlr 16", "PATIENT INFORMATION", priorityJson));
//        input.add(createRow(2301L, "member_city", "MEMBER_DETAILS_0", "Azusa", "PATIENT INFORMATION", priorityJson));
//        input.add(createRow(2295L, "member_zipcode", "MEMBER_DETAILS_0", "91702", "PATIENT INFORMATION", priorityJson));
//        input.add(createRow(2288L, "member_state", "MEMBER_DETAILS_0", "CA", "PATIENT INFORMATION", priorityJson));
//        input.add(createRow(2275L, "member_first_name", "MEMBER_DETAILS_0", "Darlin", "PATIENT INFORMATION", priorityJson));
//
//        input.add(createRow(2269L, "multiple_member_indicator", "MEMBER_DETAILS_0", "false", null, priorityJson));
//
//        // Voting Scenarios (Branch B)
//        input.add(createRow(2259L, "servicing_provider_address_line1", "SERVICING_PROVIDER_DETAILS_1", "", null, priorityJson));
//        input.add(createRow(2258L, "servicing_provider_address_line1", "SERVICING_PROVIDER_DETAILS_2", "", null, priorityJson));
//        input.add(createRow(2256L, "servicing_provider_address_line1", "SERVICING_PROVIDER_DETAILS_0", "", null, priorityJson));
//
//        input.add(createRow(2249L, "servicing_provider_city", "SERVICING_PROVIDER_DETAILS_2", "", null, priorityJson));
//        input.add(createRow(2248L, "servicing_provider_city", "SERVICING_PROVIDER_DETAILS_1", "", null, priorityJson));
//        input.add(createRow(2246L, "servicing_provider_city", "SERVICING_PROVIDER_DETAILS_0", "", null, priorityJson));
//
//        input.add(createRow(2239L, "servicing_provider_state", "SERVICING_PROVIDER_DETAILS_1", "", null, priorityJson));
//        input.add(createRow(2238L, "servicing_provider_state", "SERVICING_PROVIDER_DETAILS_2", "", null, priorityJson));
//        input.add(createRow(2236L, "servicing_provider_state", "SERVICING_PROVIDER_DETAILS_0", "", null, priorityJson));
//
//        input.add(createRow(2229L, "servicing_provider_zipcode", "SERVICING_PROVIDER_DETAILS_1", "", null, priorityJson));
//        input.add(createRow(2228L, "servicing_provider_zipcode", "SERVICING_PROVIDER_DETAILS_2", "", null, priorityJson));
//        input.add(createRow(2226L, "servicing_provider_zipcode", "SERVICING_PROVIDER_DETAILS_0", "", null, priorityJson));
//
//        // Interesting Voting Case: Last Name
//        input.add(createRow(2219L, "servicing_provider_last_name", "SERVICING_PROVIDER_DETAILS_2", "Ball", null, priorityJson));
//        input.add(createRow(2218L, "servicing_provider_last_name", "SERVICING_PROVIDER_DETAILS_1", "Patel", null, priorityJson));
//        input.add(createRow(2216L, "servicing_provider_last_name", "SERVICING_PROVIDER_DETAILS_0", "Onyekwuluje", null, priorityJson));
//
//        input.add(createRow(2209L, "servicing_provider_npi", "SERVICING_PROVIDER_DETAILS_2", "1194183087", null, priorityJson));
//        input.add(createRow(2208L, "servicing_provider_npi", "SERVICING_PROVIDER_DETAILS_1", "1811216989", null, priorityJson));
//        input.add(createRow(2206L, "servicing_provider_npi", "SERVICING_PROVIDER_DETAILS_0", "1649210493", null, priorityJson));
//
//        input.add(createRow(2199L, "servicing_provider_tin", "SERVICING_PROVIDER_DETAILS_1", "", null, priorityJson));
//        input.add(createRow(2198L, "servicing_provider_tin", "SERVICING_PROVIDER_DETAILS_2", "", null, priorityJson));
//        input.add(createRow(2196L, "servicing_provider_tin", "SERVICING_PROVIDER_DETAILS_0", "", null, priorityJson));
//
//        input.add(createRow(2157L, "fax_received_date", "FAX_DETAILS_0", "06/01/2025 11:58:59 PM ET", "FOOTER", priorityJson));
//        input.add(createRow(2154L, "member_date_of_birth", "MEMBER_DETAILS_0", "02/17/1979", "PATIENT INFORMATION", priorityJson));
//        input.add(createRow(2148L, "level_of_service", "LEVEL_OF_SERVICE_0", "UR", null, priorityJson));
//        input.add(createRow(2141L, "service_from_date", "SERVICE_FROM_DATE_0", "06/01/25", null, priorityJson));
//
//        // Full Names
//        input.add(createRow(2118L, "servicing_provider_full_name", "SERVICING_PROVIDER_DETAILS_2", "Ball, Benjamin", null, priorityJson));
//        input.add(createRow(2117L, "servicing_provider_full_name", "SERVICING_PROVIDER_DETAILS_1", "Patel, Katan Pravin", null, priorityJson));
//        input.add(createRow(2115L, "servicing_provider_full_name", "SERVICING_PROVIDER_DETAILS_0", "Onyekwuluje, Anne N", null, priorityJson));
//
//        // First Names
//        input.add(createRow(2108L, "servicing_provider_first_name", "SERVICING_PROVIDER_DETAILS_1", "Katan Pravin", null, priorityJson));
//        input.add(createRow(2107L, "servicing_provider_first_name", "SERVICING_PROVIDER_DETAILS_2", "Benjamin", null, priorityJson));
//        input.add(createRow(2105L, "servicing_provider_first_name", "SERVICING_PROVIDER_DETAILS_0", "Anne N", null, priorityJson));
//
//        // PROCESS
//        List<AggregationEvaluatorInputModel> results = processor.process(input);
//
//        // ASSERTIONS
//        // Total groups = unique sor_item_name
//        Set<String> uniqueItems = input.stream().map(AggregationEvaluatorInputModel::getSorItemName).collect(Collectors.toSet());
//        assertEquals(uniqueItems.size(), results.size(), "Should produce one winner per sor_item_name");
//
//        // Check specific winners
//        Map<String, AggregationEvaluatorInputModel> winnersMap = results.stream()
//                .collect(Collectors.toMap(AggregationEvaluatorInputModel::getSorItemName, r -> r));
//
//        // 1. Single valued priority checks
//        assertEquals(2320L, winnersMap.get("member_last_name").getId());
//        assertEquals(2275L, winnersMap.get("member_first_name").getId());
//
//        // 2. Voting Logic - Servicing Provider First Name
//        // IDs: 2108, 2107, 2105. 2362 (empty)
//        // No match -> Fallback to min ID -> 2105 ("Anne N")
//        assertEquals(2105L, winnersMap.get("servicing_provider_first_name").getId());
//
//        // 3. Voting Logic - Servicing Provider Last Name
//        // 2219, 2218, 2216. Min ID -> 2216 ("Onyekwuluje")
//        assertEquals(2216L, winnersMap.get("servicing_provider_last_name").getId());
//
//        // 4. Single item empty check (should be selected anyway if it's the only one, or fallback)
//        // 2336 (empty), 2333 (empty), 2331 (empty) -> Fallback to min ID -> 2331
//        assertEquals(2331L, winnersMap.get("member_full_name").getId());
//
//        System.out.println("All validations passed!");
//    }
//
//    private AggregationEvaluatorInputModel createRow(Long id, String sorItemName, String containerInstance, String answer, String sectionAlias, String json) {
//        AggregationEvaluatorInputModel r = new AggregationEvaluatorInputModel();
//        r.setId(id);
//        r.setSorItemName(sorItemName);
//        r.setSorContainerId((long) containerInstance.hashCode()); // Simulate ID from instance name
//        r.setAnswer(answer);
//        r.setSectionAlias(sectionAlias);
//        r.setWhitelistedSectionsWithPriority(json);
//        r.setOriginId("ORIGIN-38");
//        r.setPaperNo(1);
//        return r;
//    }
}

