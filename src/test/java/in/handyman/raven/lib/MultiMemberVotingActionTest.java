package in.handyman.raven.lib;

import in.handyman.raven.core.enums.EncryptionConstants;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.MultiValueMemberMapper;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class MultiMemberVotingActionTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    void execute() throws Exception {
        MultiValueMemberMapper multiValueMemberMapper = MultiValueMemberMapper.builder()
                .name("AUMI multi-value member mapper voting result")
                .batchId("BATCH-78_0_new")
                .condition(true)
                .outputTable("voting.cummulative_result")
                .resourceConn("intics_zio_db_conn")
                .querySet("select ae.root_pipeline_id, ae.batch_id, ae.group_id, ae.origin_id, sc.sor_container_name, ae.sor_item_name, ae.predicted_value, ae.paper_no, ae.tenant_id,si.encryption_policy_id, si.is_encrypted, si.line_item_type, ae.synonym_id , ae.question_id, ae.frequency, ae.vqa_score, ae.b_box, ae.model_registry, ROUND(ae.vqa_score) as confidence_score, sc.document_type as document_type\n" +
                        "from score.aggregation_evaluator ae\n" +
                        "join sor_meta.sor_item si on si.sor_item_name = ae.sor_item_name and si.tenant_id = ae.tenant_id and si.status = 'ACTIVE'\n" +
                        "join sor_meta.sor_tsynonym st on st.sor_item_id = si.sor_item_id and st.synonym_id = ae.synonym_id and st.status = 'ACTIVE'\n" +
                        "join sor_meta.sor_container sc on sc.sor_container_id = si.sor_container_id and sc.tenant_id = si.tenant_id and sc.status = 'ACTIVE'\n" +
                        "where ae.root_pipeline_id = '9413' and sc.sor_container_name = 'MEMBER_DETAILS' and ae.origin_id='ORIGIN-855';")
                .build();

        final ActionExecutionAudit action = ActionExecutionAudit.builder().build();
        action.getContext().put("tenant_id", "1");
        action.getContext().put("group_id", "78");
        action.getContext().put("batch_id", "BATCH-78_0_new");
        action.getContext().put("created_user_id", "1");
        action.getContext().put("document_type", "MEDICAL_GBD");
        action.getContext().put("multi.member.name.similarity.threshold","0.85");
        action.getContext().put("multi.member.id.similarity.threshold","0.80");
        action.getContext().put("multi.member.indicator.fields","member_id,member_last_name,member_first_name,multiple_member_indicator");
        action.getContext().put("radon.kvp.bbox.vqa.score.default","50");
        action.getContext().put("multi.member.consumer.API.count","1");
        action.getContext().put("multi.member.voting.v1","true");
        action.getContext().put(EncryptionConstants.ENCRYPT_ITEM_WISE_ENCRYPTION, "false");
        action.setRootPipelineId(9413L);

        MultiValueMemberMapperAction multiValueMemberMapperAction = new MultiValueMemberMapperAction(action, logger, multiValueMemberMapper);
        multiValueMemberMapperAction.execute();
    }
}
