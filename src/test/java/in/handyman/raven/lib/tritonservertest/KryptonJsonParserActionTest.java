package in.handyman.raven.lib.tritonservertest;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.KryptonJsonParserAction;
import in.handyman.raven.lib.model.KryptonJsonParser;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class KryptonJsonParserActionTest {
    @Test
    public void tritonTest() throws Exception {
        KryptonJsonParser kryptonJsonParser = KryptonJsonParser.builder()
                .name("krypton json parser")
                .condition(true)
                .resourceConn("intics_zio_db_conn")
                .outputTable("krypton.krypton_json_parser_output")
                .querySet("select '{ \n" +
                        "            \"Patient\": { \n" +
                        "              \"patient_address\": null, \n" +
                        "              \"patient_city\": null, \n" +
                        "              \"patient_city_state_zip\": null, \n" +
                        "              \"patient_dob\": \"08/16/1978\", \n" +
                        "              \"patient_eoc\": null, \n" +
                        "              \"patient_first_name\": \"Kari\", \n" +
                        "              \"patient_group_number\": null, \n" +
                        "              \"patient_last_name\": \"Tanner\", \n" +
                        "              \"patient_member_id\": null, \n" +
                        "              \"patient_name\": \"Kari Tanner\", \n" +
                        "              \"patient_phone\": \"8565724226\", \n" +
                        "              \"patient_state\": null, \n" +
                        "              \"patient_zip\": null \n" +
                        "            }, \n" +
                        "            \"Prescriber\": { \n" +
                        "              \"prescriber_address\": \"44405 WOODWARD AVE\", \n" +
                        "              \"prescriber_city\": null, \n" +
                        "              \"prescriber_city_state_zip\": null, \n" +
                        "              \"prescriber_fax\": \"1548844491\", \n" +
                        "              \"prescriber_first_name\": \"YASHAR\", \n" +
                        "              \"prescriber_last_name\": \"DANESHJOO\", \n" +
                        "              \"prescriber_name\": \"YASHAR DANESHJOO\", \n" +
                        "              \"prescriber_npi\": \"1548844491\", \n" +
                        "              \"prescriber_state_lic_id\": null, \n" +
                        "              \"prescriber_phone\": \"8565724226\", \n" +
                        "              \"prescriber_zip\": null, \n" +
                        "              \"prescriber_state\": null, \n" +
                        "              \"prescriber_tax_id\": null \n" +
                        "            }, \n" +
                        "            \"Drug\": [ \n" +
                        "              { \n" +
                        "                \"date_of_service_from\": null, \n" +
                        "                \"date_of_service_to\": null, \n" +
                        "                \"diagnosis\": \"A052\", \n" +
                        "                \"direction_sig\": \"Apply a thin layer of cream to the affected area twice a day\", \n" +
                        "                \"drug_dose\": \"125mg + 125mg / 40ml\", \n" +
                        "                \"drug_name\": \"Ampicillin & Cloxacillin\", \n" +
                        "                \"drug_quantity\": null, \n" +
                        "                \"drug_strength\": \"125mg + 125mg / 40ml\", \n" +
                        "                \"hcpcs\": null, \n" +
                        "                \"icd10\": null, \n" +
                        "                \"j_code\": null \n" +
                        "              } \n" +
                        "            ], \n" +
                        "            \"Provider\": [ \n" +
                        "              { \n" +
                        "                \"provider_address\": null, \n" +
                        "                \"provider_city\": null, \n" +
                        "                \"provider_city_state_zip\": null, \n" +
                        "                \"provider_fax\": null}, {\n" +
                        "                \"provider_name\": null, \n" +
                        "                \"provider_npi\": null, \n" +
                        "                \"provider_phone\": null, \n" +
                        "                \"provider_state\": null, \n" +
                        "                \"provider_tax_id\": null, \n" +
                        "                \"provider_zip\": null \n" +
                        "              } \n" +
                        "            ] \n" +
                        "          }' as response, 1 as paper_no, \'ORIGIN-1\' as origin_id, 1 as group_id, 1 as tenant_id, 1 as root_pipeline_id, \n" +
                        "          \'Batch_1\' as batch_id, \'KRYPTON\' as model_registry")
                .build();

        ActionExecutionAudit ac = new ActionExecutionAudit();
        ac.setRootPipelineId(1234L);
        ac.setActionId(1234L);
        ac.setProcessId(123L);
        ac.getContext().put("krypton.kvp.parser.consumer.API.count", "1");
        ac.getContext().put("write.batch.size", "1");
        ac.getContext().put("read.batch.size", "1");


        KryptonJsonParserAction kryptonJsonParserAction = new KryptonJsonParserAction(ac, log, kryptonJsonParser);

        kryptonJsonParserAction.execute();

    }
}
