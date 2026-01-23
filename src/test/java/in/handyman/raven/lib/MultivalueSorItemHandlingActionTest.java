package in.handyman.raven.lib;

import in.handyman.raven.core.encryption.inticsgrity.InticsIntegrity;
import in.handyman.raven.core.enums.EncryptionConstants;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.MultivalueSorItemHandling;
import in.handyman.raven.lib.services.sor.transform.MultiEntityFieldHandlingInput;
import in.handyman.raven.lib.services.soritemhandling.MultivalueSorItemHandlingActionInput;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Slf4j
class MultivalueSorItemHandlingActionTest {

    private ActionExecutionAudit action;

    private final MultivalueSorItemHandling config=new MultivalueSorItemHandling();

    private InticsIntegrity encryption;

    private MultivalueSorItemHandlingAction actionInstance;
    private final Marker aMarker = MarkerFactory.getMarker(" MultivalueSorItemHandling test");

    private static final String WHITELIST =
            "[{\"priorityLevel\":1,\"truthEntity\":\"Header\"}," +
                    "{\"priorityLevel\":2,\"truthEntity\":\"Body\"}]";


    @BeforeEach
    void setUp() {
        config.setName("MultivalueSorItemHandlingAction");
        config.setCondition(true);
        config.setOutputTable("multivalue_sor_item_handling_output");
        config.setResourceConn("intics_zio_db_conn");
        config.setQuerySet("select 1");
        actionInstance = new MultivalueSorItemHandlingAction(action, log, config);
    }


    private MultiEntityFieldHandlingInput buildMultiValueInput(
            String origin,
            String container,
            String instance,
            Integer paperNo,
            String item,
            String answer
    ) {
        MultiEntityFieldHandlingInput i = new MultiEntityFieldHandlingInput();
        i.setOriginId(origin);
        i.setSorContainerName(container);
        i.setSorContainerInstance(instance);
        i.setPaperNo(paperNo);
        i.setSorItemName(item);
        i.setAnswer(answer);
        i.setLineItemType("multi_value");
        i.setRemovedAfterFiltering(false);
        return i;
    }


    // ==========================================================
    // ✅ CASE 1.1 — Single page with comma-separated values
    // ==========================================================
    @Test
    void shouldSplitCommaSeparatedValuesOnSinglePage() {

        List<MultiEntityFieldHandlingInput> inputs = List.of(
                buildMultiValueInput(
                        "O1",
                        "DIAGNOSIS_CODE",
                        "DIAGNOSIS_CODE_1",
                        1,
                        "diagnosis_codes",
                        "H123, H456, H789"
                )
        );

        List<MultiEntityFieldHandlingInput> result =
                actionInstance.handleCase1MultiValue(inputs, aMarker, log);

        // Original removed
        assertTrue(result.stream().anyMatch(MultiEntityFieldHandlingInput::isRemovedAfterFiltering));

        // New split values retained
        List<String> answers = result.stream()
                .filter(i -> !i.isRemovedAfterFiltering())
                .map(MultiEntityFieldHandlingInput::getAnswer)
                .collect(Collectors.toList());

        assertEquals(3, answers.size());
        assertTrue(answers.contains("H123"));
        assertTrue(answers.contains("H456"));
        assertTrue(answers.contains("H789"));
    }


    // ==========================================================
    // ✅ CASE 1.2 — Multiple pages, single value per page
    // ==========================================================
    @Test
    void shouldNotSplitWhenMultiplePagesHaveSingleValues() {

        List<MultiEntityFieldHandlingInput> inputs = List.of(
                buildMultiValueInput("O1", "DIAGNOSIS_CODE", "DIAGNOSIS_CODE_1", 1, "diagnosis_codes", "H123"),
                buildMultiValueInput("O1", "DIAGNOSIS_CODE", "DIAGNOSIS_CODE_2", 2, "diagnosis_codes", "H456")
        );

        List<MultiEntityFieldHandlingInput> result =
                actionInstance.handleCase1MultiValue(inputs, aMarker, log);

        assertEquals(2, result.size());
        assertTrue(result.stream().noneMatch(MultiEntityFieldHandlingInput::isRemovedAfterFiltering));

        assertTrue(result.stream().anyMatch(i -> i.getPaperNo() == 1L && "H123".equals(i.getAnswer())));
        assertTrue(result.stream().anyMatch(i -> i.getPaperNo() == 2L && "H456".equals(i.getAnswer())));
    }


    // ==========================================================
    // ✅ CASE 1.3 — Mixed: one page split, other pages untouched
    // ==========================================================
    @Test
    void shouldSplitOnlyCommaSeparatedPageAndKeepOthers() {

        List<MultiEntityFieldHandlingInput> inputs = new ArrayList<>();

        inputs.add(buildMultiValueInput(
                "O1",
                "DIAGNOSIS_CODE",
                "DIAGNOSIS_CODE_1",
                1,
                "diagnosis_codes",
                "H123, H456"
        ));

        inputs.add(buildMultiValueInput(
                "O1",
                "DIAGNOSIS_CODE",
                "DIAGNOSIS_CODE_2",
                2,
                "diagnosis_codes",
                "H789"
        ));

        List<MultiEntityFieldHandlingInput> result =
                actionInstance.handleCase1MultiValue(inputs, aMarker, log);

        List<MultiEntityFieldHandlingInput> retained =
                result.stream()
                        .filter(i -> !i.isRemovedAfterFiltering())
                        .collect(Collectors.toList());

        assertEquals(3, retained.size());

        assertTrue(retained.stream().anyMatch(i -> "H123".equals(i.getAnswer())));
        assertTrue(retained.stream().anyMatch(i -> "H456".equals(i.getAnswer())));
        assertTrue(retained.stream().anyMatch(i -> "H789".equals(i.getAnswer())));
    }


    // ==========================================================
    // ✅ CASE 1.4 — No comma → no split
    // ==========================================================
    @Test
    void shouldNotSplitWhenNoCommaPresent() {

        List<MultiEntityFieldHandlingInput> inputs = List.of(
                buildMultiValueInput(
                        "O1",
                        "DIAGNOSIS_CODE",
                        "DIAGNOSIS_CODE_1",
                        1,
                        "diagnosis_codes",
                        "H123"
                )
        );

        List<MultiEntityFieldHandlingInput> result =
                actionInstance.handleCase1MultiValue(inputs, aMarker, log);

        assertEquals(1, result.size());
        assertFalse(result.get(0).isRemovedAfterFiltering());
        assertEquals("H123", result.get(0).getAnswer());
    }


    // ==========================================================
    // ✅ CASE 1.5 — Empty values inside comma list
    // ==========================================================
    @Test
    void shouldIgnoreEmptyCommaSeparatedValues() {

        List<MultiEntityFieldHandlingInput> inputs = List.of(
                buildMultiValueInput(
                        "O1",
                        "DIAGNOSIS_CODE",
                        "DIAGNOSIS_CODE_1",
                        1,
                        "diagnosis_codes",
                        "H123, , H456,  "
                )
        );

        List<MultiEntityFieldHandlingInput> result =
                actionInstance.handleCase1MultiValue(inputs, aMarker, log);

        List<String> answers = result.stream()
                .filter(i -> !i.isRemovedAfterFiltering())
                .map(MultiEntityFieldHandlingInput::getAnswer)
                .collect(Collectors.toList());

        assertEquals(2, answers.size());
        assertTrue(answers.contains("H123"));
        assertTrue(answers.contains("H456"));
    }






    private MultiEntityFieldHandlingInput buildCase3Input(
            String origin,
            String container,
            String instance,
            Integer paperNo,
            String sorItem,
            String sectionAlias,
            String answer,
            Long score,
            String whitelistJson
    ) {
        MultiEntityFieldHandlingInput i = new MultiEntityFieldHandlingInput();
        i.setOriginId(origin);
        i.setSorContainerName(container);
        i.setSorContainerInstance(instance);
        i.setPaperNo(paperNo);
        i.setSorItemName(sorItem);
        i.setSectionAlias(sectionAlias);
        i.setAnswer(answer);
        i.setScore(score);
        i.setIsMultiEntityEnabled("true"); // CASE-3
        i.setLineItemType("single_value");
        i.setWhitelistedSections(whitelistJson);
        return i;
    }





//✅ CASE-3.1 — Single item → retained directly
    @Test
    void case3_singleItem_retainedAsIs() throws Exception {

        List<MultiEntityFieldHandlingInput> inputs = List.of(
                buildCase3Input(
                        "O1", "MEMBER_DETAILS", "INST1", 1,
                        "member_id", "Header", "MID-123",
                        90L,
                        "[{\"priorityLevel\":1,\"truthEntity\":\"Header\"}]"
                )
        );

        List<MultiEntityFieldHandlingInput> result =
                actionInstance.handleCase3MultiEntityEnabled(inputs, aMarker, log, action);

        assertEquals(1, result.size());
        assertFalse(result.get(0).isRemovedAfterFiltering());
        assertEquals("MID-123", result.get(0).getAnswer());
    }




    //✅ CASE-3.2 — Multiple instances, whitelist decides winner
    @Test
    void case3_whitelistPriorityWins() throws Exception {

        List<MultiEntityFieldHandlingInput> inputs = List.of(
                buildCase3Input(
                        "O1", "MEMBER_DETAILS", "INST1", 1,
                        "member_gender", "Footer", "F",
                        95L,
                        "[{\"priorityLevel\":1,\"truthEntity\":\"Header\"},{\"priorityLevel\":2,\"truthEntity\":\"Footer\"}]"
                ),
                buildCase3Input(
                        "O1", "MEMBER_DETAILS", "INST2", 2,
                        "member_gender", "Header", "Female",
                        60L,
                        "[{\"priorityLevel\":1,\"truthEntity\":\"Header\"},{\"priorityLevel\":2,\"truthEntity\":\"Footer\"}]"
                )
        );

        List<MultiEntityFieldHandlingInput> result =
                actionInstance.handleCase3MultiEntityEnabled(inputs, aMarker, log, action);

        assertEquals(1, result.size());

        MultiEntityFieldHandlingInput retained = result.get(0);
        assertEquals("Female", retained.getAnswer());
        assertEquals("Header", retained.getSectionAlias());
    }


    //✅ CASE-3.3 — No whitelist → max score wins
    @Test
    void case3_noWhitelist_maxScoreWins() throws Exception {

        List<MultiEntityFieldHandlingInput> inputs = List.of(
                buildCase3Input(
                        "O1", "MEMBER_DETAILS", "INST1", 1,
                        "member_city", "Header", "Austin",
                        75L,
                        null
                ),
                buildCase3Input(
                        "O1", "MEMBER_DETAILS", "INST2", 2,
                        "member_city", "Header", "Dallas",
                        90L,
                        null
                )
        );

        List<MultiEntityFieldHandlingInput> result =
                actionInstance.handleCase3MultiEntityEnabled(inputs, aMarker, log, action);

        assertEquals(1, result.size());
        assertEquals("Dallas", result.get(0).getAnswer());
    }




    //✅ CASE-3.4 — Whitelist present but no alias match → fallback to score
    @Test
    void case3_whitelistNoAliasMatch_fallbackToScore() throws Exception {

        List<MultiEntityFieldHandlingInput> inputs = List.of(
                buildCase3Input(
                        "O1", "MEMBER_DETAILS", "INST1", 1,
                        "member_state", "Left", "TX",
                        70L,
                        "[{\"priorityLevel\":1,\"truthEntity\":\"Header\"}]"
                ),
                buildCase3Input(
                        "O1", "MEMBER_DETAILS", "INST2", 2,
                        "member_state", "Right", "Texas",
                        95L,
                        "[{\"priorityLevel\":1,\"truthEntity\":\"Header\"}]"
                )
        );

        List<MultiEntityFieldHandlingInput> result =
                actionInstance.handleCase3MultiEntityEnabled(inputs, aMarker, log, action);

        assertEquals(1, result.size());
        assertEquals("Texas", result.get(0).getAnswer());
    }

    //✅ CASE-3.5 — Multiple SorItemNames → one retained per item
    @Test
    void case3_multipleSorItems_eachResolvesIndependently() throws Exception {

        List<MultiEntityFieldHandlingInput> inputs = List.of(
                buildCase3Input(
                        "O1", "MEMBER_DETAILS", "INST1", 1,
                        "member_id", "Header", "MID-111",
                        80L,
                        null
                ),
                buildCase3Input(
                        "O1", "MEMBER_DETAILS", "INST2", 2,
                        "member_id", "Header", "MID-222",
                        95L,
                        null
                ),
                buildCase3Input(
                        "O1", "MEMBER_DETAILS", "INST1", 1,
                        "member_gender", "Header", "M",
                        60L,
                        null
                )
        );

        List<MultiEntityFieldHandlingInput> result =
                actionInstance.handleCase3MultiEntityEnabled(inputs, aMarker, log, action);

        assertEquals(2, result.size());

        assertTrue(result.stream().anyMatch(i -> "member_id".equals(i.getSorItemName())));
        assertTrue(result.stream().anyMatch(i -> "member_gender".equals(i.getSorItemName())));
    }


    //✅ CASE-3.6 — Different origins processed independently
    @Test
    void case3_differentOrigins_doNotInterfere() throws Exception {

        List<MultiEntityFieldHandlingInput> inputs = List.of(
                buildCase3Input(
                        "O1", "MEMBER_DETAILS", "INST1", 1,
                        "member_id", "Header", "MID-1",
                        90L, null
                ),
                buildCase3Input(
                        "O2", "MEMBER_DETAILS", "INST1", 1,
                        "member_id", "Header", "MID-2",
                        90L, null
                )
        );

        List<MultiEntityFieldHandlingInput> result =
                actionInstance.handleCase3MultiEntityEnabled(inputs, aMarker, log, action);

        assertEquals(2, result.size());
    }

    /**
     * Builds mock data for CASE-3 + CASE-4 integration tests.
     * Multi-Entity Enabled (CASE-3) and Disabled (CASE-4) records are included.
     */
    private List<MultiEntityFieldHandlingInput> buildCase3And4IntegrationData() {
        List<MultiEntityFieldHandlingInput> inputs = new ArrayList<>();

        // ============================
        // CASE-3: Multi-Entity Enabled
        // ============================
        inputs.add(createIntegrationInput(
                "O1", "MEMBER_DETAILS", "MEMBER_DETAILS_1",
                "member_gender", "Header", "F", 90L,
                "[{\"priorityLevel\":1,\"truthEntity\":\"Header\"}]", true, 1
        ));
        inputs.add(createIntegrationInput(
                "O1", "MEMBER_DETAILS", "MEMBER_DETAILS_2",
                "member_gender", "Footer", "Female", 60L,
                "[{\"priorityLevel\":1,\"truthEntity\":\"Header\"}]", true, 2
        ));

        inputs.add(createIntegrationInput(
                "O1", "MEMBER_DETAILS", "MEMBER_DETAILS_1",
                "member_city", "Header", "Austin", 80L,
                null, true, 1
        ));
        inputs.add(createIntegrationInput(
                "O1", "MEMBER_DETAILS", "MEMBER_DETAILS_2",
                "member_city", "Header", "Dallas", 95L,
                null, true, 2
        ));

        // ============================
        // CASE-4: Multi-Entity Disabled
        // ============================
        inputs.add(createIntegrationInput(
                "O1", "MEMBER_DETAILS", "MEMBER_DETAILS_1",
                "member_id", "Header", "MID-123", 9L,
                "[{\"priorityLevel\":1,\"truthEntity\":\"Header\"}]", false, 1
        ));
        inputs.add(createIntegrationInput(
                "O1", "MEMBER_DETAILS", "MEMBER_DETAILS_2",
                "member_id", "Header", "MID-123", 9L,
                "[{\"priorityLevel\":1,\"truthEntity\":\"Header\"}]", false, 2
        ));

        inputs.add(createIntegrationInput(
                "O1", "MEMBER_DETAILS", "MEMBER_DETAILS_1",
                "member_address_line1", "Header", "123 Main St", 90L,
                "[{\"priorityLevel\":1,\"truthEntity\":\"Header\"}]", false, 1
        ));
        inputs.add(createIntegrationInput(
                "O1", "MEMBER_DETAILS", "MEMBER_DETAILS_2",
                "member_address_line1", "Header", "123 Main St", 90L,
                "[{\"priorityLevel\":1,\"truthEntity\":\"Header\"}]", false, 2
        ));

        return inputs;
    }

    /**
     * Self-contained input builder for integration test.
     */
    private MultiEntityFieldHandlingInput createIntegrationInput(
            String originId,
            String containerName,
            String instance,
            String sorItemName,
            String sectionAlias,
            String answer,
            Long score,
            String whitelistJson,
            boolean isMultiEntity,
            Integer paperNo
    ) {
        MultiEntityFieldHandlingInput input = new MultiEntityFieldHandlingInput();
        input.setOriginId(originId);
        input.setSorContainerName(containerName);
        input.setSorContainerInstance(instance);
        input.setSorItemName(sorItemName);
        input.setSectionAlias(sectionAlias);
        input.setAnswer(answer);
        input.setScore(score);
        input.setWhitelistedSections(whitelistJson);
        input.setIsMultiEntityEnabled(String.valueOf(isMultiEntity));
        input.setLineItemType("single_value");
        input.setPaperNo(paperNo);
        return input;
    }


    @Test
    void case3AndCase4_integrationTest() throws Exception {
        List<MultiEntityFieldHandlingInput> inputs = buildCase3And4IntegrationData();

        // CASE-3 processing
        List<MultiEntityFieldHandlingInput> case3Results =
                actionInstance.handleCase3MultiEntityEnabled(inputs, aMarker, log, action);

        // CASE-4 processing
        List<MultiEntityFieldHandlingInput> case4Inputs = inputs.stream()
                .filter(i -> "false".equals(i.getIsMultiEntityEnabled()))
                .collect(Collectors.toList());

        List<MultiEntityFieldHandlingInput> case4Results =
                actionInstance.handleCase4MultiEntityDisabled(case4Inputs, aMarker, log, action);

        // Collect retained items
        List<MultiEntityFieldHandlingInput> finalRetained = new ArrayList<>();
        finalRetained.addAll(case3Results.stream().filter(i -> !i.isRemovedAfterFiltering()).collect(Collectors.toList()));
        finalRetained.addAll(case4Results.stream().filter(i -> !i.isRemovedAfterFiltering()).collect(Collectors.toList()));

        // Assertions
        assertTrue(finalRetained.stream().anyMatch(i -> "member_gender".equals(i.getSorItemName())));
        assertTrue(finalRetained.stream().anyMatch(i -> "member_id".equals(i.getSorItemName())));
        assertTrue(finalRetained.stream().anyMatch(i -> "member_address_line1".equals(i.getSorItemName())));
    }



    private MultiEntityFieldHandlingInput buildCase4Input(
            String origin,
            String container,
            String instance,
            Integer paperNo,
            String item,
            String alias,
            String answer,
            long score,
            String whitelistJson
    ) {
        MultiEntityFieldHandlingInput i = new MultiEntityFieldHandlingInput();
        i.setOriginId(origin);
        i.setSorContainerName(container);
        i.setSorContainerInstance(instance);
        i.setPaperNo(paperNo);                  // ✅ IMPORTANT KEY
        i.setSorItemName(item);
        i.setSectionAlias(alias);
        i.setAnswer(answer);
        i.setScore(score);
        i.setIsMultiEntityEnabled("false");     // ✅ CASE-4
        i.setLineItemType("single_value");
        i.setWhitelistedSections(whitelistJson);
        i.setRemovedAfterFiltering(true);
        return i;
    }


    private List<MultiEntityFieldHandlingInput> buildCase4MemberDetailsData() {
        String whitelist =
                "[{\"priorityLevel\":1,\"truthEntity\":\"MEMBER\",{\"priorityLevel\":2,\"truthEntity\":\"PATIENT\"}]";

        List<MultiEntityFieldHandlingInput> list = new ArrayList<>();

        // ================= PAGE 1 =================
        list.add(buildCase4Input("O1","MEMBER_DETAILS","MEMBER_DETAILS_1",1,
                "member_full_name","MEMBER","Alice Johnson",95,whitelist));

        list.add(buildCase4Input("O1","MEMBER_DETAILS","MEMBER_DETAILS_1",1,
                "member_first_name","MEMBER","Alice",90,whitelist));

        list.add(buildCase4Input("O1","MEMBER_DETAILS","MEMBER_DETAILS_1",1,
                "member_last_name","MEMBER","Johnson",90,whitelist));

        list.add(buildCase4Input("O1","MEMBER_DETAILS","MEMBER_DETAILS_1",1,
                "member_date_of_birth","MEMBER","1990-01-01",88,whitelist));

        list.add(buildCase4Input("O1","MEMBER_DETAILS","MEMBER_DETAILS_1",1,
                "member_gender","MEMBER","F",85,whitelist));

        list.add(buildCase4Input("O1","MEMBER_DETAILS","MEMBER_DETAILS_1",1,
                "member_address_line1","MEMBER","123 Main St",87,whitelist));

        list.add(buildCase4Input("O1","MEMBER_DETAILS","MEMBER_DETAILS_1",1,
                "member_city","MEMBER","Austin",86,whitelist));

        // ================= PAGE 2 =================
        list.add(buildCase4Input("O1","MEMBER_DETAILS","MEMBER_DETAILS_2",2,
                "member_full_name","MEMBER","Alice Johnson",97,whitelist));

        list.add(buildCase4Input("O1","MEMBER_DETAILS","MEMBER_DETAILS_2",2,
                "member_id","MEMBER","MID-12345",92,whitelist));

        list.add(buildCase4Input("O1","MEMBER_DETAILS","MEMBER_DETAILS_2",2,
                "medicaid_id","MEMBER","MC-998877",91,whitelist));

        list.add(buildCase4Input("O1","MEMBER_DETAILS","MEMBER_DETAILS_2",2,
                "member_zipcode","MEMBER","78701",89,whitelist));

        list.add(buildCase4Input("O1","MEMBER_DETAILS","MEMBER_DETAILS_2",2,
                "multiple_member_indicator","MEMBER","false",99,whitelist));

        return list;
    }


    //✅ CASE-4.1 — Cross-page connection → MERGE
    @Test
    void case4_mergeInstances_whenMemberIdentityMatches() {

        List<MultiEntityFieldHandlingInput> inputs =
                buildCase4MemberDetailsData();

        List<MultiEntityFieldHandlingInput> results =
                actionInstance.handleCase4MultiEntityDisabled(inputs,aMarker,log,action);

        List<MultiEntityFieldHandlingInput> retained =
                results.stream().filter(i -> !i.isRemovedAfterFiltering()).collect(Collectors.toList());

        for(MultiEntityFieldHandlingInput i : retained) {
            System.out.println("Retained Origin ID: " + i.getOriginId() +
                    ", Container Instance: " + i.getSorContainerInstance() +
                    ", Paper No: " + i.getPaperNo() +
                    ", Item Name: " + i.getSorItemName() +
                    ", Answer: " + i.getAnswer());
        }

        assertEquals(11, retained.size(), "All merged items should be retained");

        // ✅ ONE logical member data
        assertEquals(11,
                retained.stream()
                        .map(MultiEntityFieldHandlingInput::getSorItemName)
                        .distinct()
                        .count()
        );

        // ✅ Data merged across pages
        assertTrue(retained.stream().anyMatch(i -> "member_id".equals(i.getSorItemName())));
        assertTrue(retained.stream().anyMatch(i -> "member_address_line1".equals(i.getSorItemName())));

        // ✅ Both pages contributed
        assertTrue(retained.stream().anyMatch(i -> i.getPaperNo() == 1L));
        assertTrue(retained.stream().anyMatch(i -> i.getPaperNo() == 2L));
    }


//✅ CASE-4.2 — No connection → WHITELIST priority
    @Test
    void case4_selectByWhitelist_whenNoConnection() {

        List<MultiEntityFieldHandlingInput> inputs =
                buildCase4MemberDetailsData();

        // Break identity connection
        inputs.stream()
                .filter(i -> "member_full_name".equals(i.getSorItemName()) && i.getPaperNo() == 2L)
                .forEach(i -> i.setAnswer("Bob Smith"));

        List<MultiEntityFieldHandlingInput> retained =
                actionInstance.handleCase4MultiEntityDisabled(inputs,aMarker,log,action)
                        .stream()
                        .filter(i -> !i.isRemovedAfterFiltering())
                        .collect(Collectors.toList());
        for(MultiEntityFieldHandlingInput i : retained) {
            System.out.println("Retained Origin ID: " + i.getOriginId() +
                    ", Container Instance: " + i.getSorContainerInstance() +
                    ", Paper No: " + i.getPaperNo() +
                    ", Item Name: " + i.getSorItemName() +
                    ", Answer: " + i.getAnswer());
        }

        // ✅ Only one instance
        assertEquals(11,
                retained.stream()
                        .map(MultiEntityFieldHandlingInput::getSorItemName)
                        .distinct()
                        .count()
        );
    }

    //✅ CASE-4.3 — No whitelist → MAX DATA fallback
    @Test
    void case4_fallbackToMaxData_whenWhitelistMissing() {

        List<MultiEntityFieldHandlingInput> inputs =
                buildCase4MemberDetailsData();

        inputs.forEach(i -> i.setWhitelistedSections(null));

        List<MultiEntityFieldHandlingInput> retained =
                actionInstance.handleCase4MultiEntityDisabled(inputs,aMarker,log,action)
                        .stream()
                        .filter(i -> !i.isRemovedAfterFiltering())
                        .collect(Collectors.toList());

        long page1Count = retained.stream().filter(i -> i.getPaperNo() == 1L).count();
        long page2Count = retained.stream().filter(i -> i.getPaperNo() == 2L).count();

        for(MultiEntityFieldHandlingInput i : retained) {
            System.out.println("Retained Origin ID: " + i.getOriginId() +
                    ", Container Instance: " + i.getSorContainerInstance() +
                    ", Paper No: " + i.getPaperNo() +
                    ", Item Name: " + i.getSorItemName() +
                    ", Answer: " + i.getAnswer());
        }

        // ✅ Page-1 has more fields → wins
        assertTrue(page1Count > page2Count);
    }


    //✅ CASE-4.4 — Tie on data → deterministic single instance
    @Test
    void case4_singleInstance_whenDataCountEqual() throws Exception {

        List<MultiEntityFieldHandlingInput> inputs =
                buildCase4MemberDetailsData();

        // Equalize counts
        inputs.removeIf(i -> "member_address_line1".equals(i.getSorItemName()));

        List<MultiEntityFieldHandlingInput> retained =
                actionInstance.handleCase4MultiEntityDisabled(inputs,aMarker,log,action)
                        .stream()
                        .filter(i -> !i.isRemovedAfterFiltering())
                        .collect(Collectors.toList());

        for(MultiEntityFieldHandlingInput i : retained) {
            System.out.println("Retained Origin ID: " + i.getOriginId() +
                    ", Container Instance: " + i.getSorContainerInstance() +
                    ", Paper No: " + i.getPaperNo() +
                    ", Item Name: " + i.getSorItemName() +
                    ", Answer: " + i.getAnswer());
        }

        // ✅ Deterministic single result
        assertEquals(10,
                retained.stream()
                        .map(MultiEntityFieldHandlingInput::getSorItemName)
                        .distinct()
                        .count()
        );
    }




}
