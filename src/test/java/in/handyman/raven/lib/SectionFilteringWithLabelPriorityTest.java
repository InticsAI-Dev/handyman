package in.handyman.raven.lib;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.lib.adapters.selections.LabelWithPriorityProcessor;
import in.handyman.raven.lib.adapters.selections.models.SelectionFilteringInputTable;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class SectionFilteringWithLabelPriorityTest {

    // Create a logger instance for the test class
    private static final Logger logger = LoggerFactory.getLogger(SectionFilteringWithLabelPriorityTest.class);

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
        r.setIsMultiEntityEnabled(false);
        r.setIsEncrypted(false);
        r.setLineItemType("single_value");

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

    @Test
    public void testSingleRow() {
        List<SelectionFilteringInputTable> input = List.of(
                row("origin1", "ITEM_A", "someAnswer", true, "", null)
        );
        var processor = new LabelWithPriorityProcessor(new ObjectMapper(), logger);
        var result = processor.process(input);
        Map<String, Map<String, List<SelectionFilteringInputTable>>> grouped = getStringMapMap(result);
        System.out.println("testSingleRow");
        printProcessedResult(grouped);
    }

    @Test
    public void testTwoRowsOneNonEmpty() {
        List<SelectionFilteringInputTable> input = List.of(
                row("origin1", "ITEM_A", "VALUE", true, "", null),
                row("origin1", "ITEM_A", "", true, "", null)
        );
        var processor = new LabelWithPriorityProcessor(new ObjectMapper(), logger);
        var result = processor.process(input);
        Map<String, Map<String, List<SelectionFilteringInputTable>>> grouped = getStringMapMap(result);
        System.out.println("testTwoRowsOneNonEmpty");
        printProcessedResult(grouped);
    }

    @Test
    public void testTwoRowsBothEmpty() {
        List<SelectionFilteringInputTable> input = List.of(
                row("origin1", "ITEM_A", "", true, "", null),
                row("origin1", "ITEM_A", "", true, "", null)
        );
        var processor = new LabelWithPriorityProcessor(new ObjectMapper(), logger);
        var result = processor.process(input);
        Map<String, Map<String, List<SelectionFilteringInputTable>>> grouped = getStringMapMap(result);
        System.out.println("testTwoRowsBothEmpty");
        printProcessedResult(grouped);
    }

    @Test
    public void testPriorityMapSelection() {
        String priorityJson = "[{\"whitelistKey\": \"HIC\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Policy No\", \"labelSearchConfig\": \"CONTAINS\"}, {\"whitelistKey\": \"Policy No.\", \"labelSearchConfig\": \"CONTAINS\"}, {\"whitelistKey\": \"Policy ID\", \"labelSearchConfig\": \"CONTAINS\"}, {\"whitelistKey\": \"Policy ID #\", \"labelSearchConfig\": \"CONTAINS\"}, {\"whitelistKey\": \"HCID # HIC\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"HCID#\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Insurance No.\", \"labelSearchConfig\": \"CONTAINS\"}, {\"whitelistKey\": \"HCID No\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"hcp member id\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"ihcp member id (rid):\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"ihcp member id:\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"medical/member id\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"member id or ma recipient no.\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"member/medicaid id\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"member medicaid id\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"member medicaid id number\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"member or medicaid id #\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"member nbr\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Mbr Number\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Mbr No\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Mbr #\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"mem id#\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"mom member id #\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"member or medicaid id #:\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"medicaid/member id\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"medicare/medicaid #\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Member Medicare/ Medicaid #\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"member policy #\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Anthem Member ID\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"amerigroup id number:\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"amerigroup member id\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"amerigroup member id number\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Amerigroup Community Care member ID\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"healthkeepers, inc. member id\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"BCBS Member ID\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"UHC Member ID\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"HCID Number HIC\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"HICP Member ID\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"HIC #\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"policy/group #:\", \"labelSearchConfig\": \"CONTAINS\"}, {\"whitelistKey\": \"Insurance No\", \"labelSearchConfig\": \"CONTAINS\"}, {\"whitelistKey\": \"HICN\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"hic#\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"id: vnv\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"id/certification no.\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"policy/group no.:\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"POLICY ID (S.S.)\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Pol #\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Pol No\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Pol Num\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Pol ID\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"pol/claim#\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"hmo/managed policy#\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"primary coverage id\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"policy / group# / insured\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"insurance policy number\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"amerigroup enrollee id\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"medicare beneficiary id\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Primary Insurance ID\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Insurance Member ID\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Ins. ID\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"ins id#\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"ins#\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"ins co #:\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Ins#: - (BCBS Other)\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Health Insurance ID\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Ins#/Member ID\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"insurance information\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"insured id:\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"insured hcin\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Patient Insurance Number\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"HCID\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Insurance-Identification-Number\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Member Identification Number\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Member Identification\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Patient / Insured ID\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Patient #\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"ptid\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"ptid(s)\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Pt ID\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"patnum\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Pt #\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Pt No\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Pt Number\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"pat number\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Sub Insurance ID\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Insurance Subscriber Number\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Pt Num\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"pat id\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Patient Insurance ID\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Patient Insurance Member ID\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"patients insurance id #\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"patient insurance\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Patient Identification Number\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"patient subscriber no:\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"mrn/patient id #\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"patient 2nd id\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"patient id (m0020)\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"patient id/account #\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"patient id/account # (assigned by dentist)\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"patients medical id #\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"patients medicare no.\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"caregiver identification number\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Plan Number\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Plan No\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Plan No.\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Plan ID\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Plan ID #\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Plan ID Number\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"PlanID\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Plan-ID\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"hi claim no\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"patients hl claim no.\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"patient hi claim no.\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"patient hic no.\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"medicare number/numero de medicare\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"resident insurance id number\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Ins#/Medicaid ID\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Patient Member ID\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Patient Member Number\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"wellpoint ia medicaid id#\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"alternate patient id\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Sub ID\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Sub ID #\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Sub #\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"ID #\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Member\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Member ID Number\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Members ID Number\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Wellpoint Member ID\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Membership ID Number\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Membership #\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Member #\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Simply ID\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Member/Medicaid ID\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"HCID Number\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"ID NO\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"ID No\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"ID Number\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Policy\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Reference Number\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"ID#\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Mbr ID\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"ID\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Beneficiary ID\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Medicare Replacement #\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"BCBS ID #\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Medicaid ID\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Member/Medicaid\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Member Medicare/Medicaid ID\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Medicaid/CHIP #\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Insurance ID#\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Insureds ID\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Insurance #\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Insureds ID Number\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Insurance Identification Number\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Identification\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Identification Number\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Patient ID\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Patient\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Patient/Insured ID\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Subscriber\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Subscr ID\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Subscr #\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Subscr No\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Sub No\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Subscr No / Subscr #\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Subsc. ID\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Subr ID\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Subs ID\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Sbsc ID\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Sb ID\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Sub#\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Sub Num\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Member/Subscriber ID\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Member/Subscriber Number\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Member/Subscriber No\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Member/Subscriber #\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Subscriber/Member ID\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Subscriber/Member #\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Subscriber/Member No\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Subscriber/Member Number\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"medicaid id/subscriber id:\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Medicaid/Subscriber ID\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Subscriber Insurance ID\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Subscriber Insurance ID #\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Subscriber Insurance Number\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Subscriber Insurance No\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Subscriber Insurance No.\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Sub Ins ID\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Subscr Insurance ID\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Sub Ins No\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Sub Ins #\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"HIC/SUBSCRIBER\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Insured Subscriber Number\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Subscriber Identification Number\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Insurance ID (Subscriber)\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Insurance Number (Subscriber)\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Insurance Subscriber ID\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Insurance Subscriber Number\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Subscribers Insurance ID Number\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Subscribers Insurance Identification Number\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Primary Subscriber Insurance ID\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Primary Insurance Subscriber ID\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Insurance Policyholder ID (Subscriber)\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Insurance Card ID (Subscriber)\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Member/Subscriber Insurance ID\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Subscriber/Insured Insurance ID\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Insured / Subscriber Insurance Number\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Insureds Insurance ID (Subscriber)\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"policyholder/subscriber id (assigned by plan)\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"policyholder/subscriber id\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Healthy Blue ID\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Members ID#\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Member ID Number#\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Member ID#\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Member No.\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Member#\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Enrollee #\", \"labelSearchConfig\": \"CONTAINS\"}, {\"whitelistKey\": \"Policy NBR\", \"labelSearchConfig\": \"CONTAINS\"}, {\"whitelistKey\": \"Insurance ID\", \"labelSearchConfig\": \"CONTAINS\"}, {\"whitelistKey\": \"Insured ID\", \"labelSearchConfig\": \"CONTAINS\"}, {\"whitelistKey\": \"Patient ID Number\", \"labelSearchConfig\": \"CONTAINS\"}, {\"whitelistKey\": \"Patient ID #\", \"labelSearchConfig\": \"CONTAINS\"}, {\"whitelistKey\": \"Subscriber ID #\", \"labelSearchConfig\": \"CONTAINS\"}, {\"whitelistKey\": \"Subscribers ID\", \"labelSearchConfig\": \"CONTAINS\"}, {\"whitelistKey\": \"Subscriber No\", \"labelSearchConfig\": \"CONTAINS\"}, {\"whitelistKey\": \"Subscribers Number\", \"labelSearchConfig\": \"CONTAINS\"}, {\"whitelistKey\": \"Primary Subscriber ID\", \"labelSearchConfig\": \"CONTAINS\"}, {\"whitelistKey\": \"Subscriber No.\", \"labelSearchConfig\": \"CONTAINS\"}, {\"whitelistKey\": \"Subscribers ID Number\", \"labelSearchConfig\": \"CONTAINS\"}, {\"whitelistKey\": \"Primary Subscriber Number\", \"labelSearchConfig\": \"CONTAINS\"}, {\"whitelistKey\": \"HCID #\", \"labelSearchConfig\": \"CONTAINS\"}, {\"whitelistKey\": \"HIC No\", \"labelSearchConfig\": \"CONTAINS\"}, {\"whitelistKey\": \"HICN #\", \"labelSearchConfig\": \"CONTAINS\"}, {\"whitelistKey\": \"HICN No\", \"labelSearchConfig\": \"CONTAINS\"}, {\"whitelistKey\": \"PolicyID\", \"labelSearchConfig\": \"CONTAINS\"}, {\"whitelistKey\": \"Policy-ID\", \"labelSearchConfig\": \"CONTAINS\"}, {\"whitelistKey\": \"Policy Num.\", \"labelSearchConfig\": \"CONTAINS\"}, {\"whitelistKey\": \"Policy Identification Number\", \"labelSearchConfig\": \"CONTAINS\"}, {\"whitelistKey\": \"identification no.\", \"labelSearchConfig\": \"CONTAINS\"}, {\"whitelistKey\": \"Patient Number\", \"labelSearchConfig\": \"CONTAINS\"}, {\"whitelistKey\": \"Patient No\", \"labelSearchConfig\": \"CONTAINS\"}, {\"whitelistKey\": \"Patient Identifier\", \"labelSearchConfig\": \"CONTAINS\"}, {\"whitelistKey\": \"medicaid number\", \"labelSearchConfig\": \"CONTAINS\"}, {\"whitelistKey\": \"medicaid number ():\", \"labelSearchConfig\": \"CONTAINS\"}, {\"whitelistKey\": \"medicaid number (pcn)*\", \"labelSearchConfig\": \"CONTAINS\"}, {\"whitelistKey\": \"member id number (including alpha prefix):\", \"labelSearchConfig\": \"CONTAINS\"}, {\"whitelistKey\": \"Insured ID Number\", \"labelSearchConfig\": \"CONTAINS\"}, {\"whitelistKey\": \"Member ID No\", \"labelSearchConfig\": \"CONTAINS\"}, {\"whitelistKey\": \"Subscriber ID\", \"labelSearchConfig\": \"CONTAINS\"}, {\"whitelistKey\": \"Subscriber #\", \"labelSearchConfig\": \"CONTAINS\"}, {\"whitelistKey\": \"Subscriber Number\", \"labelSearchConfig\": \"CONTAINS\"}, {\"whitelistKey\": \"Member ID\", \"labelSearchConfig\": \"CONTAINS\"}, {\"whitelistKey\": \"HIC Number\", \"labelSearchConfig\": \"CONTAINS\"}, {\"whitelistKey\": \"Policy #\", \"labelSearchConfig\": \"CONTAINS\"}, {\"whitelistKey\": \"Policy Num\", \"labelSearchConfig\": \"CONTAINS\"}, {\"whitelistKey\": \"Policy Number\", \"labelSearchConfig\": \"CONTAINS\"}, {\"whitelistKey\": \"Policy/Group Number\", \"labelSearchConfig\": \"CONTAINS\"}, {\"whitelistKey\": \"Enrollee ID\", \"labelSearchConfig\": \"CONTAINS\"}, {\"whitelistKey\": \"Member ID 1\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Member ID 2\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"MemberID No\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Member ID Num\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"IHCP Member ID\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Member Num\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"Member No\", \"labelSearchConfig\": \"EXACT\"}, {\"whitelistKey\": \"HICD\", \"labelSearchConfig\": \"EXACT\"}]";
        List<SelectionFilteringInputTable> input = List.of(
                row("origin1", "ITEM_A", "520320695", true, "Subscriber/Member #", priorityJson),
                row("origin1", "ITEM_A", "INDH8000547336", true, "Account:", priorityJson),
                row("origin1", "ITEM_A", "INDH8000547336", true, "FIN:", priorityJson),
                row("origin1", "ITEM_A", "INMCDWPO", true, "Group/Plan #", priorityJson)
        );
        var processor = new LabelWithPriorityProcessor(new ObjectMapper(), logger);
        var result = processor.process(input);
        Map<String, Map<String, List<SelectionFilteringInputTable>>> grouped = getStringMapMap(result);
        System.out.println("testPriorityMapSelection");
        printProcessedResult(grouped);
    }

    @Test
    public void testNoPriorityMap() {
        List<SelectionFilteringInputTable> input = List.of(
                row("origin1", "ITEM_A", "A", true, "X", null),
                row("origin1", "ITEM_A", "B", true, "Y", null),
                row("origin1", "ITEM_A", "C", true, "Z", "")
        );
        var processor = new LabelWithPriorityProcessor(new ObjectMapper(), logger);
        var result = processor.process(input);
        Map<String, Map<String, List<SelectionFilteringInputTable>>> grouped = getStringMapMap(result);
        System.out.println("testNoPriorityMap");
        printProcessedResult(grouped);
    }

    @Test
    public void testMultipleSorItems() {
        List<SelectionFilteringInputTable> input = List.of(
                row("origin1", "ITEM_A", "A1", true, "KEY1", "{\"KEY1\":1}"),
                row("origin1", "ITEM_A", "A2", true, "KEY2", "{\"KEY1\":1}"),
                row("origin1", "ITEM_B", "B1", true, "K2", null),
                row("origin1", "ITEM_B", "B2", true, "K3", null)
        );
        var processor = new LabelWithPriorityProcessor(new ObjectMapper(), logger);
        var result = processor.process(input);
        Map<String, Map<String, List<SelectionFilteringInputTable>>> grouped = getStringMapMap(result);
        System.out.println("testMultipleSorItems");
        printProcessedResult(grouped);
    }

    @Test
    public void testIgnoreNonMatchingRows() {
        List<SelectionFilteringInputTable> input = List.of(
                row("origin1", "ITEM_A", "A", false, "KEY1", "{\"KEY1\":1}"),
                row("origin1", "ITEM_A", "B", true, "KEY2", "{\"KEY2\":2}")
        );
        var processor = new LabelWithPriorityProcessor(new ObjectMapper(), logger);
        var result = processor.process(input);
        Map<String, Map<String, List<SelectionFilteringInputTable>>> grouped = getStringMapMap(result);
        System.out.println("testIgnoreNonMatchingRows");
        printProcessedResult(grouped);
    }

    @Test
    public void testInvalidPriorityMap() {
        List<SelectionFilteringInputTable> input = List.of(
                row("origin1", "ITEM_A", "A", true, "1", "{invalid_json"),
                row("origin1", "ITEM_A", "B", true, "1", "{invalid_json")
        );
        var processor = new LabelWithPriorityProcessor(new ObjectMapper(), logger);
        var result = processor.process(input);
        Map<String, Map<String, List<SelectionFilteringInputTable>>> grouped = getStringMapMap(result);
        System.out.println("testInvalidPriorityMap");
        printProcessedResult(grouped);
    }

    @Test
    public void testMissingKeysInPriorityMap() {
        String priorityJson = "{\"X\":1}";
        List<SelectionFilteringInputTable> input = List.of(
                row("origin1", "ITEM_A", "A", true, "X", priorityJson),
                row("origin1", "ITEM_A", "B", true, "Y", priorityJson) // no key Y → becomes MAX_VALUE
        );
        var processor = new LabelWithPriorityProcessor(new ObjectMapper(), logger);
        var result = processor.process(input);
        Map<String, Map<String, List<SelectionFilteringInputTable>>> grouped = getStringMapMap(result);
        System.out.println("testMissingKeysInPriorityMap");
        printProcessedResult(grouped);
    }

    @Test
    public void testMultipleSorItems2() {
        List<SelectionFilteringInputTable> input = List.of(
                // ITEM_A row contains three entries with different priorities
                rowId(1L, "origin1", 1L, "ITEM_A", "A1", true, "KEY2", "{\"KEY1\":2,\"KEY2\":1}"),
                rowId(2L, "origin1", 3L, "ITEM_A", "A2", true, "KEY1", "{\"KEY1\":2,\"KEY2\":1}"),
                rowId(3L, "origin1", 2L, "ITEM_A", "A3", true, "", "{\"KEY1\":2,\"KEY2\":1}"),
                // ITEM_D row contains three entries with different priorities
                rowId(1L, "origin1", 1L, "ITEM_D", "A1", true, "KEY1", "{\"KEY1\":2,\"KEY2\":1}"),
                rowId(2L, "origin1", 3L, "ITEM_D", "A2", true, "KEY1", "{\"KEY1\":2,\"KEY2\":1}"),
                rowId(3L, "origin1", 2L, "ITEM_D", "A3", true, "", "{\"KEY1\":2,\"KEY2\":1}"),
                // ITEM_E row contains three entries with different priorities
                rowId(2L, "origin1", 1L, "ITEM_E", "A1", true, "KEY1", "{\"KEY1\":2,\"KEY2\":1}"),
                rowId(1L, "origin1", 1L, "ITEM_E", "A2", true, "KEY1", "{\"KEY1\":2,\"KEY2\":1}"),
                rowId(3L, "origin1", 2L, "ITEM_E", "A3", true, "", "{\"KEY1\":2,\"KEY2\":1}"),
                //ITEM_B row contains two entries without priority map -> min of paper wins
                rowId(4L, "origin1", 2L, "ITEM_B", "B1", true, "K2", null),
                rowId(5L, "origin1", 1L, "ITEM_B", "B2", true, "K3", null),
                //ITEM_F row contains two entries without priority map -> min of paper wins
                rowId(4L, "origin1", 2L, "ITEM_f", "B1", true, null, null),
                rowId(5L, "origin1", 1L, "ITEM_f", "B2", true, null, null),
                // ITEM_C row contains three entries with no labels present -> min of paper wins
                rowId(1L, "origin1", 1L, "ITEM_C", "A1", false, "", "{\"KEY1\":1}"),
                rowId(2L, "origin1", 1L, "ITEM_C", "A2", false, "", "{\"KEY1\":1}"),
                rowId(3L, "origin1", 2L, "ITEM_C", "A3", false, "", "{\"KEY1\":1}")
        );
        var processor = new LabelWithPriorityProcessor(new ObjectMapper(), logger);
        var result = processor.process(input);
        Map<String, Map<String, List<SelectionFilteringInputTable>>> grouped = getStringMapMap(result);
        System.out.println("testMultipleSorItems2");
        printProcessedResult(grouped);
    }

    @NotNull
    private static Map<String, Map<String, List<SelectionFilteringInputTable>>> getStringMapMap(List<SelectionFilteringInputTable> result) {
        return result.stream()
                .collect(Collectors.groupingBy(
                        SelectionFilteringInputTable::getOriginId,
                        Collectors.groupingBy(SelectionFilteringInputTable::getSorItemName)
                ));
    }

    public static void printProcessedResult(Map<String, Map<String, List<SelectionFilteringInputTable>>> result) {
        if (result == null || result.isEmpty()) {
            System.out.println("<empty result>");
            return;
        }

        System.out.println("\n================= PROCESSED LABEL RESULTS =================");
        result.forEach((originId, sorMap) -> {
            System.out.println("Origin ID: " + originId);
            sorMap.forEach((sorItemName, rows) -> {
                System.out.println("  SOR Item: " + sorItemName);
                for (SelectionFilteringInputTable row : rows) {
                    System.out.println("    Paper No          : " + row.getPaperNo());
                    System.out.println("    Answer            : " + row.getAnswer());
                    System.out.println("    Label             : " + row.getSorItemLabel());
                    System.out.println("    Label PriorityIdx : " + row.getLabelPriorityIdx());
                    System.out.println("    Label Matching    : " + row.getLabelMatching());
                    System.out.println("    Label Match Msg   : " + row.getLabelMatchMessage());
                    System.out.println("---------------------------------------------------------");
                }
                System.out.println();
            });
            System.out.println();
        });
        System.out.println("============================================================\n");
    }

    @Test
    public void testNullInputList() {
        var processor = new LabelWithPriorityProcessor(new ObjectMapper(), logger);
        // Should return empty list, not throw NPE
        var result = processor.process(null);
        assert result != null && result.isEmpty();
        System.out.println("testNullInputList: Handled successfully");
    }

    @Test
    public void testInputListWithNullElements() {
        // This simulates a list that has null objects inside it
        List<SelectionFilteringInputTable> input = new ArrayList<>();
        input.add(row("origin1", "ITEM_A", "A", true, "KEY1", "{\"KEY1\":1}"));
        input.add(null); // The culprit

        var processor = new LabelWithPriorityProcessor(new ObjectMapper(), logger);
        // If this throws NPE, the fix in assignPriorities/process is missing
        System.out.println("testInputListWithNullElements:");
        try {
            var result = processor.process(input);
            printProcessedResult(getStringMapMap(result));
        } catch (NullPointerException e) {
            System.err.println("CRASHED: NPE found with null elements in list!");
            throw e;
        }
    }

    @Test
    public void testNullLabelInRow() {
        String priorityJson = "{\"KEY1\":1}";
        List<SelectionFilteringInputTable> input = List.of(
                row("origin1", "ITEM_A", "A", true, null, priorityJson) // Label is null
        );
        var processor = new LabelWithPriorityProcessor(new ObjectMapper(), logger);
        var result = processor.process(input);
        System.out.println("testNullLabelInRow:");
        printProcessedResult(getStringMapMap(result));
    }

    @Test
    public void testNullOrLiteralNullPriorityJson() {
        List<SelectionFilteringInputTable> input = List.of(
                row("origin1", "ITEM_A", "A", true, "KEY1", null), // Java null
                row("origin1", "ITEM_A", "B", true, "KEY1", "null") // String literal "null"
        );
        var processor = new LabelWithPriorityProcessor(new ObjectMapper(), logger);
        var result = processor.process(input);
        System.out.println("testNullOrLiteralNullPriorityJson:");
        printProcessedResult(getStringMapMap(result));
    }

    @Test
    public void testEmptyJsonPriorityMap() {
        List<SelectionFilteringInputTable> input = List.of(
                row("origin1", "ITEM_A", "A", true, "KEY1", "[]"),
                row("origin1", "ITEM_A", "B", true, "KEY1", "{}")
        );
        var processor = new LabelWithPriorityProcessor(new ObjectMapper(), logger);
        var result = processor.process(input);
        System.out.println("testEmptyJsonPriorityMap:");
        printProcessedResult(getStringMapMap(result));
    }
}