package in.handyman.raven.lib;

import in.handyman.raven.core.enums.EncryptionConstants;
import in.handyman.raven.lambda.access.ResourceAccess;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.MultivalueSorItemHandling;
import in.handyman.raven.lib.model.soritemhandling.MultivalueSorItemHandlingActionInput;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

@Slf4j
class multiValueSorItemHandlingTest {

    @Test
    void execute() throws Exception {
        MultivalueSorItemHandling multivalueSorItemHandling = MultivalueSorItemHandling.builder()
                .name("Multivalue Concatenation Action")
                .condition(true)
                    .outputTable("entity_voting.sor_item_multivalue_filtering_output_audit")
                .resourceConn("intics_zio_db_conn")
                .querySet("             select vqa.created_on, vqa.created_user_id, vqa.last_updated_on, vqa.last_updated_user_id, vqa.status,\n" +
                        "            vqa.version, vqa.answer , vqa.b_box, vqa.document_id, vqa.extracted_image_unit, vqa.group_id, vqa.image_dpi,\n" +
                        "            vqa.image_height, vqa.image_width, vqa.model_id, vqa.model_info, vqa.origin_id, vqa.paper_no, vqa.question_id,\n" +
                        "            vqa.root_pipeline_id, vqa.score, vqa.sor_item_attribution_id, vqa.sor_item_name, vqa.sor_question, vqa.synonym_id,\n" +
                        "            vqa.tenant_id, vqa.vqa_score, vqa.weight, vqa.model_registry, vqa.category, vqa.model_registry_id, vqa.stage, vqa.batch_id, ep.line_item_type,\n" +
                        "            ep.encryption_policy_id, ep.is_encrypted, ep.encryption_policy, false as is_multi_entity_enabled, vqa.sor_container_instance, ep.sor_container \n" +
                        "            from sor_transaction.vqa_transaction vqa\n" +
                        "            join transit_data.sor_meta_consolidated_13110 ep on vqa.synonym_id=ep.synonym_id and vqa.sor_item_name=ep.sor_item_name and vqa.group_id=ep.group_id\n" +
                        "            where vqa.origin_id='ORIGIN-69';")
                .build();

        final ActionExecutionAudit action = ActionExecutionAudit.builder().build();
        action.getContext().put("tenant_id", "1");
        action.getContext().put("group_id", "2014");
        action.getContext().put("batch_id", "BATCH-2014_0_new");
        action.getContext().put("created_user_id", "1");
        action.getContext().put(EncryptionConstants.ENCRYPT_ITEM_WISE_ENCRYPTION, "false");
//        action.setRootPipelineId(929L);

        MultivalueSorItemHandlingAction multivalueSorItemHandlingAction = new MultivalueSorItemHandlingAction(action, log, multivalueSorItemHandling);
        multivalueSorItemHandlingAction.execute();
    }


    @Test
    void testRemapContainerInstance() throws Exception {
        MultivalueSorItemHandling multivalueSorItemHandling = MultivalueSorItemHandling.builder()
                .name("Multivalue Concatenation Action")
                .condition(true)
                .outputTable("entity_voting.sor_item_multivalue_filtering_output")
                .resourceConn("intics_zio_db_conn")
                .querySet("\n" +
                        "             select vqa.created_on, vqa.created_user_id, vqa.last_updated_on, vqa.last_updated_user_id, vqa.status,\n" +
                        "            vqa.version, vqa.answer , vqa.b_box, vqa.document_id, vqa.extracted_image_unit, vqa.group_id, vqa.image_dpi,\n" +
                        "            vqa.image_height, vqa.image_width, vqa.model_id, vqa.model_info, vqa.origin_id, vqa.paper_no, vqa.question_id,\n" +
                        "            vqa.root_pipeline_id, vqa.score, vqa.sor_item_attribution_id, vqa.sor_item_name, vqa.sor_question, vqa.synonym_id,\n" +
                        "            vqa.tenant_id, vqa.vqa_score, vqa.weight, vqa.model_registry, vqa.category, vqa.model_registry_id, vqa.stage, vqa.batch_id, ep.line_item_type,\n" +
                        "            ep.encryption_policy_id, ep.is_encrypted, ep.encryption_policy, false as is_multi_entity_enabled, vqa.sor_container_instance ,ep.sor_container_name\n" +
                        "            from sor_transaction.vqa_transaction vqa\n" +
                        "            join transit_data.sor_meta_consolidated_15280 ep on vqa.synonym_id=ep.synonym_id and vqa.sor_item_name=ep.sor_item_name and vqa.group_id=ep.group_id\n" +
                        "            where vqa.origin_id='ORIGIN-80' and ep.sor_container_name like ('MEMBER_DETAILS');\n")
                .build();

        final ActionExecutionAudit action = ActionExecutionAudit.builder().build();
        action.getContext().put("tenant_id", "1");
        action.getContext().put("group_id", "2014");
        action.getContext().put("batch_id", "BATCH-2014_0_new");
        action.getContext().put("created_user_id", "1");
        action.getContext().put(EncryptionConstants.ENCRYPT_ITEM_WISE_ENCRYPTION, "false");

        MultivalueSorItemHandlingAction multivalueSorItemHandlingAction = new MultivalueSorItemHandlingAction(action, log, multivalueSorItemHandling);
        List<MultivalueSorItemHandlingActionInput> list = new ArrayList<>();
        final Jdbi jdbi = ResourceAccess.rdbmsJDBIConn(multivalueSorItemHandling.getResourceConn());
        final List<MultivalueSorItemHandlingActionInput> multivalueConcatenationInputs = multivalueSorItemHandlingAction.fetchValuesFromDB(jdbi);
        System.out.println("Fetched Inputs:");
        for (MultivalueSorItemHandlingActionInput multivalueConcatenationInput : multivalueConcatenationInputs) {
            System.out.println(multivalueConcatenationInput);
        }

        List<MultivalueSorItemHandlingActionInput> remappedMultivalueSorItemHandlingAction= multivalueSorItemHandlingAction.selectMaxCountNodes(multivalueConcatenationInputs);
        System.out.println("Max sor Instance Outputs:");
        remappedMultivalueSorItemHandlingAction.forEach(multivalueSorItemHandlingActionInput -> {
            System.out.println(multivalueSorItemHandlingActionInput);
        });

    }
    private List<MultivalueSorItemHandlingActionInput> buildTestData() {

        List<MultivalueSorItemHandlingActionInput> list = new ArrayList<>();

        // ---------- ORIGIN-1 : PAGE 1 ----------

        // Page 1 → member_details_1 → 3 sor items
        list.add(item("COMPLETED", "ans1", "10,20,30,40", "DOC1", 1L,
                101, "M1", "ORIGIN-1", 1L, 1001L, 5001L,
                0.81, 9001L, "NAME1", "Q1", 3001L, 1L, 0.80,
                "regA", "catA", "stageA", "B1", "single_value",
                "false", 101, "POLICY_A",
                "member_details_1", "false"));

        list.add(item("COMPLETED", "ans2", "11,22,33,44", "DOC1", 1L,
                101, "M1", "ORIGIN-1", 1L, 1001L, 5001L,
                0.75, 9002L, "NAME2", "Q2", 3002L, 1L, 0.76,
                "regA", "catA", "stageA", "B1", "single_value",
                "false", 101, "POLICY_A",
                "member_details_1", "false"));

        list.add(item("COMPLETED", "ans3", "12,23,34,45", "DOC1", 1L,
                101, "M1", "ORIGIN-1", 1L, 1001L, 5001L,
                0.69, 9003L, "NAME3", "Q3", 3003L, 1L, 0.70,
                "regA", "catA", "stageA", "B1", "single_value",
                "false", 101, "POLICY_A",
                "member_details_1", "false"));

        // Page 1 → member_details_2 → 2 sor items
        list.add(item("COMPLETED", "ans4", "20,30,40,50", "DOC1", 1L,
                101, "M1", "ORIGIN-1", 1L, 1002L, 5002L,
                0.85, 9101L, "NAME4", "Q4", 3101L, 1L, 0.83,
                "regA", "catA", "stageA", "B1", "single_value",
                "false", 101, "POLICY_A",
                "member_details_2", "false"));

        list.add(item("COMPLETED", "ans5", "21,31,41,51", "DOC1", 1L,
                101, "M1", "ORIGIN-1", 1L, 1002L, 5002L,
                0.73, 9102L, "NAME5", "Q5", 3102L, 1L, 0.75,
                "regA", "catA", "stageA", "B1", "single_value",
                "false", 101, "POLICY_A",
                "member_details_2", "false"));

        // ---------- ORIGIN-1 : PAGE 2 ----------

        // Page 2 → member_details_1 → 2 sor items
        list.add(item("COMPLETED", "ans6", "30,40,50,60", "DOC1", 2L,
                101, "M1", "ORIGIN-1", 2L, 1003L, 5003L,
                0.92, 9201L, "NAME6", "Q6", 3201L, 1L, 0.91,
                "regA", "catA", "stageA", "B1", "single_value",
                "false", 101, "POLICY_A",
                "member_details_1", "false"));

        list.add(item("COMPLETED", "ans7", "31,41,51,61", "DOC1", 2L,
                101, "M1", "ORIGIN-1", 2L, 1003L, 5003L,
                0.88, 9202L, "NAME7", "Q7", 3202L, 1L, 0.87,
                "regA", "catA", "stageA", "B1", "single_value",
                "false", 101, "POLICY_A",
                "member_details_1", "false"));

        // Page 2 → member_details_2 → 1 sor item
        list.add(item("COMPLETED", "ans8", "32,42,52,62", "DOC1", 2L,
                101, "M1", "ORIGIN-1", 2L, 1004L, 5004L,
                0.78, 9301L, "NAME8", "Q8", 3301L, 1L, 0.79,
                "regA", "catA", "stageA", "B1", "single_value",
                "false", 101, "POLICY_A",
                "member_details_2", "false"));

        return list;
    }

    private MultivalueSorItemHandlingActionInput item(
            String status, String answer, String bBox, String docId, Long groupId,
            Integer modelId, String modelInfo, String originId, Long paperNo,
            Long questionId, Long rootPipelineId, Double score,
            Long attrId, String sorItemName, String sorQuestion, Long synonymId,
            Long tenantId, Double vqaScore, String modelRegistry, String category,
            String stage, String batchId, String lineItemType, String isEncrypted,
            Integer encryptPolicyId, String encryptPolicy, String sorContainerInstance,
            String isMultiEntityEnabled) {

        MultivalueSorItemHandlingActionInput obj = new MultivalueSorItemHandlingActionInput();

        obj.setStatus(status);
        obj.setAnswer(answer);
        obj.setBBox(bBox);
        obj.setDocumentId(docId);
        obj.setGroupId(groupId);
        obj.setModelId(modelId);
        obj.setModelInfo(modelInfo);
        obj.setOriginId(originId);
        obj.setPaperNo(paperNo);
        obj.setQuestionId(questionId);
        obj.setRootPipelineId(rootPipelineId);
        obj.setScore(score);
        obj.setSorItemAttributionId(attrId);
        obj.setSorItemName(sorItemName);
        obj.setSorQuestion(sorQuestion);
        obj.setSynonymId(synonymId);
        obj.setTenantId(tenantId);
        obj.setVqaScore(vqaScore);
        obj.setModelRegistry(modelRegistry);
        obj.setCategory(category);
        obj.setStage(stage);
        obj.setBatchId(batchId);
        obj.setLineItemType(lineItemType);
        obj.setIsEncrypted(isEncrypted);
        obj.setEncryptionPolicyId(encryptPolicyId);
        obj.setEncryptionPolicy(encryptPolicy);
        obj.setSorContainerInstance(sorContainerInstance);
        obj.setIsMultiEntityEnabled(isMultiEntityEnabled);

        return obj;
    }


}
