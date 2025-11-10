package in.handyman.raven.lib;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.KafkaPublish;
import in.handyman.raven.lib.model.OutboundDeliveryNotify;
import in.handyman.raven.util.EncryptDecrypt;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
@Slf4j
class KafkaPublishActionTest {

    private static final String AES_ENCRYPTION = "AES";


    @Test
    void doMessageEncryption() throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {

        String messageNode = "{\n" +
                "  \"originId\": \"ORIGIN-6488\",\n" +
                "  \"documentId\": \"SYNTH_HW_Anthem_Blue_Cross_54\",\n" +
                "  \"processStartedOn\": \"2024-11-20T21:01:55.270471\",\n" +
                "  \"processCompletedOn\": \"2024-11-20T21:05:01.180844165\",\n" +
                "  \"totalProcessedDuration\": 185,\n" +
                "  \"totalPages\": 1,\n" +
                "  \"extension\": \"pdf\",\n" +
                "  \"sourceFileURI\": \"https://demo.intics.ai/alchemy/data/multipart-files/data/115/transaction/TRZ-497/139514095153010083/SYNTH_HW_Anthem_Blue_Cross_54.pdf\",\n" +
                "  \"preprocessedFileURI\": \"https://demo.intics.ai/alchemy/data/output/processed_images/20-11-2024_09_11_55/tenant_115/group_4190/file-merger-pdf/output/merge_image_to_pdf/0036a78e-a755-11ef-99c2-0242ac14002b/SYNTH_HW_Anthem_Blue_Cross_54_1_cleaned_pdf.pdf\",\n" +
                "  \"kvpSummary\": {\n" +
                "    \"SERVICING_PROVIDER_DETAILS\": {\n" +
                "      \"servicing_provider_address\": \"16782 VON KARHAN AVE STE 22508\",\n" +
                "      \"servicing_provider_phone\": \"(317) 871-1350\",\n" +
                "      \"servicing_provider_office_contact_name\": \"JOSEPH STAMM\",\n" +
                "      \"servicing_provider_last_name\": \"CLER\",\n" +
                "      \"servicing_provider_npi\": \"1649822508\",\n" +
                "      \"servicing_provider_state\": \"IN\",\n" +
                "      \"servicing_provider_name\": \"LESLIE CLER\",\n" +
                "      \"servicing_provider_specialty\": \"INFECTIOUS DISEASE\",\n" +
                "      \"servicing_provider_first_name\": \"LESLIE\",\n" +
                "      \"servicing_provider_city\": \"PLAINFIELD\",\n" +
                "      \"servicing_provider_fax\": \"(317) 870-9680\",\n" +
                "      \"servicing_provider_zipcode\": \"46184805\",\n" +
                "      \"servicing_provider_tin\": \"447433769\",\n" +
                "      \"servicing_provider_city_state_zipcode\": \"PLAINFIELD, IN 46184805\",\n" +
                "      \"servicing_provider_id\": \"569669\"\n" +
                "    },\n" +
                "    \"REQUESTED_SERVICE\": {\n" +
                "      \"type_of_service\": \"Special Treatment Education\",\n" +
                "      \"service_date\": \"21/08/2024\",\n" +
                "      \"icd10_code\": \"N46.8\",\n" +
                "      \"cpt_code\": \"8755\",\n" +
                "      \"place_of_service\": \"Office\"\n" +
                "    },\n" +
                "    \"REQUESTING_PROVIDER_DETAILS\": {\n" +
                "      \"requesting_provider_city\": null,\n" +
                "      \"requesting_provider_zipcode\": null,\n" +
                "      \"requesting_provider_tax_id\": null,\n" +
                "      \"requesting_provider_id\": null,\n" +
                "      \"requesting_provider_address_line_1\": null,\n" +
                "      \"requesting_provider_name\": null,\n" +
                "      \"requesting_provider_npi\": null,\n" +
                "      \"requesting_provider_last_name\": null,\n" +
                "      \"requesting_provider_state\": null,\n" +
                "      \"requesting_provider_first_name\": null\n" +
                "    },\n" +
                "    \"REFERRING_PROVIDER_DETAILS\": {\n" +
                "      \"referring_provider_last_name\": null,\n" +
                "      \"referring_provider_city\": null,\n" +
                "      \"referring_provider_name\": null,\n" +
                "      \"referring_provider_state\": null,\n" +
                "      \"referring_provider_zipcode\": null,\n" +
                "      \"referring_provider_city_state_zipcode\": null,\n" +
                "      \"referring_provider_office_fax\": null,\n" +
                "      \"referring_provider_specialty\": null,\n" +
                "      \"referring_provider_tin\": null,\n" +
                "      \"referring_provider_address\": null,\n" +
                "      \"referring_provider_office_phone\": null,\n" +
                "      \"referring_provider_office_contact_name\": null,\n" +
                "      \"referring_provider_first_name\": null,\n" +
                "      \"referring_provider_id\": null,\n" +
                "      \"referring_provider_npi\": null\n" +
                "    },\n" +
                "    \"FAX_DETAILS\": {\n" +
                "      \"fax_source_system\": null,\n" +
                "      \"fax_document_type\": null,\n" +
                "      \"fhps_fax_document_type\": null,\n" +
                "      \"fax_receipt_date\": null,\n" +
                "      \"fax_scan_date\": null,\n" +
                "      \"fax_doc_id\": null,\n" +
                "      \"fax_target_system\": null,\n" +
                "      \"fhps_fax_batch_id\": null,\n" +
                "      \"fax_indexing_date\": null,\n" +
                "      \"fax_number\": null\n" +
                "    },\n" +
                "    \"SERVICING_FACILITY_DETAILS\": {\n" +
                "      \"servicing_facility_provider_id\": \"\",\n" +
                "      \"servicing_facility_tin\": \"\",\n" +
                "      \"servicing_facility_first_name\": \"\",\n" +
                "      \"servicing_facility_fax\": \"\",\n" +
                "      \"servicing_facility_city_state_zipcode\": \"BAKERSFIELD, CA 93301\",\n" +
                "      \"servicing_facility_phone\": \"\",\n" +
                "      \"servicing_facility_last_name\": \"\",\n" +
                "      \"servicing_facility_state\": \"CA\",\n" +
                "      \"servicing_facility_npi\": \"1538540497\",\n" +
                "      \"servicing_facility_city\": \"BAKERSFIELD\",\n" +
                "      \"servicing_facility_name\": \"SPECIAL TREATMENT EDUCATION\",\n" +
                "      \"servicing_facility_zipcode\": \"93301\",\n" +
                "      \"servicing_facility_address\": \"3838 SAN DUMAS ST STE B331\"\n" +
                "    },\n" +
                "    \"MEMBER_DETAILS\": {\n" +
                "      \"member_id\": \"808732\",\n" +
                "      \"member_date_of_birth\": \"21081996\",\n" +
                "      \"member_zip_code\": \"44531759\",\n" +
                "      \"member_phone\": \"855-576-1305\",\n" +
                "      \"member_address\": \"8340 KELLY PARK\",\n" +
                "      \"member_city\": \"NEW ADAMSBIDE\",\n" +
                "      \"member_name\": \"MICHAEL PHILIPS\",\n" +
                "      \"member_age\": \"\",\n" +
                "      \"member_state\": \"NE\",\n" +
                "      \"member_first_name\": \"MICHAEL\",\n" +
                "      \"member_city_state_zip_code\": \"NEW ADAMSBIDE, NE, 44531759\",\n" +
                "      \"member_group_id\": \"\",\n" +
                "      \"member_last_name\": \"PHILIPS\",\n" +
                "      \"member_gender\": \"\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"kvpCfScoreSummary\": {\n" +
                "    \"PROVIDER_DETAILS\": {\n" +
                "      \"provider_phone\": null,\n" +
                "      \"provider_state\": null,\n" +
                "      \"provider_npi\": null,\n" +
                "      \"provider_address\": null,\n" +
                "      \"provider_tax_id\": null,\n" +
                "      \"provider_fax\": null,\n" +
                "      \"provider_email\": null,\n" +
                "      \"provider_city_state_zip\": null,\n" +
                "      \"provider_city\": null,\n" +
                "      \"provider_zip\": null,\n" +
                "      \"provider_organization_name\": null\n" +
                "    },\n" +
                "    \"SERVICING_PROVIDER_DETAILS\": {\n" +
                "      \"servicing_provider_address\": 80,\n" +
                "      \"servicing_provider_phone\": 80,\n" +
                "      \"servicing_provider_office_contact_name\": 80,\n" +
                "      \"servicing_provider_last_name\": 80,\n" +
                "      \"servicing_provider_npi\": 80,\n" +
                "      \"servicing_provider_state\": 80,\n" +
                "      \"servicing_provider_name\": 80,\n" +
                "      \"servicing_provider_specialty\": 80,\n" +
                "      \"servicing_provider_first_name\": 80,\n" +
                "      \"servicing_provider_city\": 80,\n" +
                "      \"servicing_provider_fax\": 80,\n" +
                "      \"servicing_provider_zipcode\": 80,\n" +
                "      \"servicing_provider_tin\": 80,\n" +
                "      \"servicing_provider_city_state_zipcode\": 80,\n" +
                "      \"servicing_provider_id\": 80\n" +
                "    },\n" +
                "    \"REQUESTED_SERVICE\": {\n" +
                "      \"service_phone\": null,\n" +
                "      \"service_end_date\": null,\n" +
                "      \"service_start_date\": null,\n" +
                "      \"type_of_service\": null,\n" +
                "      \"requested_unit\": null,\n" +
                "      \"service_name\": null,\n" +
                "      \"cpt_code\": null,\n" +
                "      \"icd10_code\": null,\n" +
                "      \"place_of_service\": null\n" +
                "    },\n" +
                "    \"REQUESTING_PROVIDER_DETAILS\": {\n" +
                "      \"requesting_provider_city\": null,\n" +
                "      \"requesting_provider_zipcode\": null,\n" +
                "      \"requesting_provider_tax_id\": null,\n" +
                "      \"requesting_provider_id\": null,\n" +
                "      \"requesting_provider_address_line_1\": null,\n" +
                "      \"requesting_provider_name\": null,\n" +
                "      \"requesting_provider_npi\": null,\n" +
                "      \"requesting_provider_last_name\": null,\n" +
                "      \"requesting_provider_state\": null,\n" +
                "      \"requesting_provider_first_name\": null\n" +
                "    },\n" +
                "    \"REFERRING_PROVIDER_DETAILS\": {\n" +
                "      \"referring_provider_last_name\": null,\n" +
                "      \"referring_provider_city\": null,\n" +
                "      \"referring_provider_name\": null,\n" +
                "      \"referring_provider_state\": null,\n" +
                "      \"referring_provider_zipcode\": null,\n" +
                "      \"referring_provider_city_state_zipcode\": null,\n" +
                "      \"referring_provider_office_fax\": null,\n" +
                "      \"referring_provider_specialty\": null,\n" +
                "      \"referring_provider_tin\": null,\n" +
                "      \"referring_provider_address\": null,\n" +
                "      \"referring_provider_office_phone\": null,\n" +
                "      \"referring_provider_office_contact_name\": null,\n" +
                "      \"referring_provider_first_name\": null,\n" +
                "      \"referring_provider_id\": null,\n" +
                "      \"referring_provider_npi\": null\n" +
                "    },\n" +
                "    \"FAX_DETAILS\": {\n" +
                "      \"fax_source_system\": null,\n" +
                "      \"fax_document_type\": null,\n" +
                "      \"fhps_fax_document_type\": null,\n" +
                "      \"fax_receipt_date\": null,\n" +
                "      \"fax_scan_date\": null,\n" +
                "      \"fax_doc_id\": null,\n" +
                "      \"fax_target_system\": null,\n" +
                "      \"fhps_fax_batch_id\": null,\n" +
                "      \"fax_indexing_date\": null,\n" +
                "      \"fax_number\": null\n" +
                "    },\n" +
                "    \"SERVICING_FACILITY_DETAILS\": {\n" +
                "      \"servicing_facility_provider_id\": 0,\n" +
                "      \"servicing_facility_tin\": 0,\n" +
                "      \"servicing_facility_first_name\": 0,\n" +
                "      \"servicing_facility_fax\": 0,\n" +
                "      \"servicing_facility_city_state_zipcode\": 80,\n" +
                "      \"servicing_facility_phone\": 0,\n" +
                "      \"servicing_facility_last_name\": 0,\n" +
                "      \"servicing_facility_state\": 80,\n" +
                "      \"servicing_facility_npi\": 80,\n" +
                "      \"servicing_facility_city\": 80,\n" +
                "      \"servicing_facility_name\": 80,\n" +
                "      \"servicing_facility_zipcode\": 80,\n" +
                "      \"servicing_facility_address\": 80,\n" +
                "      \"servicing_facility_specialty\": null,\n" +
                "      \"servicing_facility_address_line_1\": null\n" +
                "    },\n" +
                "    \"MEMBER_DETAILS\": {\n" +
                "      \"member_date_of_birth\": null,\n" +
                "      \"medicaid_id\": null,\n" +
                "      \"member_phone\": null,\n" +
                "      \"member_address\": null,\n" +
                "      \"member_first_name\": null,\n" +
                "      \"member_name\": null,\n" +
                "      \"member_plan_id\": null,\n" +
                "      \"member_last_name\": null,\n" +
                "      \"member_gender\": null\n" +
                "    }\n" +
                "  },\n" +
                "  \"tableDataSummary\": [],\n" +
                "  \"currencySummary\": [],\n" +
                "  \"paragraphSummary\": [],\n" +
                "  \"bulletinSummary\": [],\n" +
                "  \"paperInfo\": [\n" +
                "    {\n" +
                "      \"pageNo\": 1,\n" +
                "      \"paperType\": \"PRINTED\",\n" +
                "      \"featureInfo\": {\n" +
                "        \"kie\": {\n" +
                "          \"entityDetails\": {\n" +
                "            \"sorContainerDetails\": {\n" +
                "              \"SERVICING_PROVIDER_DETAILS\": {\n" +
                "                \"naturalKey\": \"SERVICING_PROVIDER_DETAILS\",\n" +
                "                \"sorItemsDetails\": {\n" +
                "                  \"servicing_provider_address\": {\n" +
                "                    \"naturalKey\": \"servicing_provider_address\",\n" +
                "                    \"predictedValue\": \"16782 VON KARHAN AVE STE 22508\",\n" +
                "                    \"confidenceScore\": 80,\n" +
                "                    \"paperType\": \"PRINTED\",\n" +
                "                    \"boundingBox\": {\n" +
                "                      \"leftPosition\": 248,\n" +
                "                      \"lowerPosition\": 942,\n" +
                "                      \"rightPosition\": 483,\n" +
                "                      \"upperPosition\": 900\n" +
                "                    }\n" +
                "                  },\n" +
                "                  \"servicing_provider_phone\": {\n" +
                "                    \"naturalKey\": \"servicing_provider_phone\",\n" +
                "                    \"predictedValue\": \"(317) 871-1350\",\n" +
                "                    \"confidenceScore\": 80,\n" +
                "                    \"paperType\": \"PRINTED\",\n" +
                "                    \"boundingBox\": {\n" +
                "                      \"leftPosition\": 843,\n" +
                "                      \"lowerPosition\": 1161,\n" +
                "                      \"rightPosition\": 1115,\n" +
                "                      \"upperPosition\": 1095\n" +
                "                    }\n" +
                "                  },\n" +
                "                  \"servicing_provider_office_contact_name\": {\n" +
                "                    \"naturalKey\": \"servicing_provider_office_contact_name\",\n" +
                "                    \"predictedValue\": \"JOSEPH STAMM\",\n" +
                "                    \"confidenceScore\": 80,\n" +
                "                    \"paperType\": \"PRINTED\",\n" +
                "                    \"boundingBox\": {\n" +
                "                      \"leftPosition\": 361,\n" +
                "                      \"lowerPosition\": 995,\n" +
                "                      \"rightPosition\": 539,\n" +
                "                      \"upperPosition\": 870\n" +
                "                    }\n" +
                "                  },\n" +
                "                  \"servicing_provider_last_name\": {\n" +
                "                    \"naturalKey\": \"servicing_provider_last_name\",\n" +
                "                    \"predictedValue\": \"CLER\",\n" +
                "                    \"confidenceScore\": 80,\n" +
                "                    \"paperType\": \"PRINTED\",\n" +
                "                    \"boundingBox\": {\n" +
                "                      \"leftPosition\": 628,\n" +
                "                      \"lowerPosition\": 614,\n" +
                "                      \"rightPosition\": 826,\n" +
                "                      \"upperPosition\": 526\n" +
                "                    }\n" +
                "                  },\n" +
                "                  \"servicing_provider_npi\": {\n" +
                "                    \"naturalKey\": \"servicing_provider_npi\",\n" +
                "                    \"predictedValue\": \"1649822508\",\n" +
                "                    \"confidenceScore\": 80,\n" +
                "                    \"paperType\": \"PRINTED\",\n" +
                "                    \"boundingBox\": {\n" +
                "                      \"leftPosition\": 242,\n" +
                "                      \"lowerPosition\": 941,\n" +
                "                      \"rightPosition\": 485,\n" +
                "                      \"upperPosition\": 901\n" +
                "                    }\n" +
                "                  },\n" +
                "                  \"servicing_provider_state\": {\n" +
                "                    \"naturalKey\": \"servicing_provider_state\",\n" +
                "                    \"predictedValue\": \"IN\",\n" +
                "                    \"confidenceScore\": 80,\n" +
                "                    \"paperType\": \"PRINTED\",\n" +
                "                    \"boundingBox\": {\n" +
                "                      \"leftPosition\": 256,\n" +
                "                      \"lowerPosition\": 1067,\n" +
                "                      \"rightPosition\": 1324,\n" +
                "                      \"upperPosition\": 955\n" +
                "                    }\n" +
                "                  },\n" +
                "                  \"servicing_provider_name\": {\n" +
                "                    \"naturalKey\": \"servicing_provider_name\",\n" +
                "                    \"predictedValue\": \"LESLIE CLER\",\n" +
                "                    \"confidenceScore\": 80,\n" +
                "                    \"paperType\": \"PRINTED\",\n" +
                "                    \"boundingBox\": {\n" +
                "                      \"leftPosition\": 275,\n" +
                "                      \"lowerPosition\": 944,\n" +
                "                      \"rightPosition\": 604,\n" +
                "                      \"upperPosition\": 862\n" +
                "                    }\n" +
                "                  },\n" +
                "                  \"servicing_provider_specialty\": {\n" +
                "                    \"naturalKey\": \"servicing_provider_specialty\",\n" +
                "                    \"predictedValue\": \"INFECTIOUS DISEASE\",\n" +
                "                    \"confidenceScore\": 80,\n" +
                "                    \"paperType\": \"PRINTED\",\n" +
                "                    \"boundingBox\": {\n" +
                "                      \"leftPosition\": 280,\n" +
                "                      \"lowerPosition\": 1053,\n" +
                "                      \"rightPosition\": 659,\n" +
                "                      \"upperPosition\": 987\n" +
                "                    }\n" +
                "                  },\n" +
                "                  \"servicing_provider_city\": {\n" +
                "                    \"naturalKey\": \"servicing_provider_city\",\n" +
                "                    \"predictedValue\": \"PLAINFIELD\",\n" +
                "                    \"confidenceScore\": 80,\n" +
                "                    \"paperType\": \"PRINTED\",\n" +
                "                    \"boundingBox\": {\n" +
                "                      \"leftPosition\": 1232,\n" +
                "                      \"lowerPosition\": 1189,\n" +
                "                      \"rightPosition\": 1594,\n" +
                "                      \"upperPosition\": 1085\n" +
                "                    }\n" +
                "                  },\n" +
                "                  \"servicing_provider_first_name\": {\n" +
                "                    \"naturalKey\": \"servicing_provider_first_name\",\n" +
                "                    \"predictedValue\": \"LESLIE\",\n" +
                "                    \"confidenceScore\": 80,\n" +
                "                    \"paperType\": \"PRINTED\",\n" +
                "                    \"boundingBox\": {\n" +
                "                      \"leftPosition\": 271,\n" +
                "                      \"lowerPosition\": 766,\n" +
                "                      \"rightPosition\": 659,\n" +
                "                      \"upperPosition\": 689\n" +
                "                    }\n" +
                "                  },\n" +
                "                  \"servicing_provider_fax\": {\n" +
                "                    \"naturalKey\": \"servicing_provider_fax\",\n" +
                "                    \"predictedValue\": \"(317) 870-9680\",\n" +
                "                    \"confidenceScore\": 80,\n" +
                "                    \"paperType\": \"PRINTED\",\n" +
                "                    \"boundingBox\": {\n" +
                "                      \"leftPosition\": 265,\n" +
                "                      \"lowerPosition\": 1047,\n" +
                "                      \"rightPosition\": 942,\n" +
                "                      \"upperPosition\": 955\n" +
                "                    }\n" +
                "                  },\n" +
                "                  \"servicing_provider_zipcode\": {\n" +
                "                    \"naturalKey\": \"servicing_provider_zipcode\",\n" +
                "                    \"predictedValue\": \"46184805\",\n" +
                "                    \"confidenceScore\": 80,\n" +
                "                    \"paperType\": \"PRINTED\",\n" +
                "                    \"boundingBox\": {\n" +
                "                      \"leftPosition\": 844,\n" +
                "                      \"lowerPosition\": 996,\n" +
                "                      \"rightPosition\": 1156,\n" +
                "                      \"upperPosition\": 904\n" +
                "                    }\n" +
                "                  },\n" +
                "                  \"servicing_provider_tin\": {\n" +
                "                    \"naturalKey\": \"servicing_provider_tin\",\n" +
                "                    \"predictedValue\": \"447433769\",\n" +
                "                    \"confidenceScore\": 80,\n" +
                "                    \"paperType\": \"PRINTED\",\n" +
                "                    \"boundingBox\": {\n" +
                "                      \"leftPosition\": 1235,\n" +
                "                      \"lowerPosition\": 779,\n" +
                "                      \"rightPosition\": 1324,\n" +
                "                      \"upperPosition\": 708\n" +
                "                    }\n" +
                "                  },\n" +
                "                  \"servicing_provider_city_state_zipcode\": {\n" +
                "                    \"naturalKey\": \"servicing_provider_city_state_zipcode\",\n" +
                "                    \"predictedValue\": \"PLAINFIELD, IN 46184805\",\n" +
                "                    \"confidenceScore\": 80,\n" +
                "                    \"paperType\": \"PRINTED\",\n" +
                "                    \"boundingBox\": {\n" +
                "                      \"leftPosition\": 827,\n" +
                "                      \"lowerPosition\": 1038,\n" +
                "                      \"rightPosition\": 1161,\n" +
                "                      \"upperPosition\": 903\n" +
                "                    }\n" +
                "                  },\n" +
                "                  \"servicing_provider_id\": {\n" +
                "                    \"naturalKey\": \"servicing_provider_id\",\n" +
                "                    \"predictedValue\": \"569669\",\n" +
                "                    \"confidenceScore\": 80,\n" +
                "                    \"paperType\": \"PRINTED\",\n" +
                "                    \"boundingBox\": {\n" +
                "                      \"leftPosition\": 816,\n" +
                "                      \"lowerPosition\": 941,\n" +
                "                      \"rightPosition\": 1024,\n" +
                "                      \"upperPosition\": 875\n" +
                "                    }\n" +
                "                  }\n" +
                "                }\n" +
                "              },\n" +
                "              \"REQUESTED_SERVICE\": {\n" +
                "                \"naturalKey\": \"REQUESTED_SERVICE\",\n" +
                "                \"sorItemsDetails\": {\n" +
                "                  \"type_of_service\": {\n" +
                "                    \"naturalKey\": \"type_of_service\",\n" +
                "                    \"predictedValue\": \"Special Treatment Education\",\n" +
                "                    \"confidenceScore\": 80,\n" +
                "                    \"paperType\": \"PRINTED\",\n" +
                "                    \"boundingBox\": {\n" +
                "                      \"leftPosition\": 361,\n" +
                "                      \"lowerPosition\": 1300,\n" +
                "                      \"rightPosition\": 601,\n" +
                "                      \"upperPosition\": 1258\n" +
                "                    }\n" +
                "                  },\n" +
                "                  \"service_date\": {\n" +
                "                    \"naturalKey\": \"service_date\",\n" +
                "                    \"predictedValue\": \"21/08/2024\",\n" +
                "                    \"confidenceScore\": 80,\n" +
                "                    \"paperType\": \"PRINTED\",\n" +
                "                    \"boundingBox\": {\n" +
                "                      \"leftPosition\": 251,\n" +
                "                      \"lowerPosition\": 1188,\n" +
                "                      \"rightPosition\": 721,\n" +
                "                      \"upperPosition\": 1123\n" +
                "                    }\n" +
                "                  },\n" +
                "                  \"cpt_code\": {\n" +
                "                    \"naturalKey\": \"cpt_code\",\n" +
                "                    \"predictedValue\": \"8755\",\n" +
                "                    \"confidenceScore\": 80,\n" +
                "                    \"paperType\": \"PRINTED\",\n" +
                "                    \"boundingBox\": {\n" +
                "                      \"leftPosition\": 316,\n" +
                "                      \"lowerPosition\": 1244,\n" +
                "                      \"rightPosition\": 489,\n" +
                "                      \"upperPosition\": 1203\n" +
                "                    }\n" +
                "                  },\n" +
                "                  \"icd10_code\": {\n" +
                "                    \"naturalKey\": \"icd10_code\",\n" +
                "                    \"predictedValue\": \"N46.8\",\n" +
                "                    \"confidenceScore\": 80,\n" +
                "                    \"paperType\": \"PRINTED\",\n" +
                "                    \"boundingBox\": {\n" +
                "                      \"leftPosition\": 305,\n" +
                "                      \"lowerPosition\": 1243,\n" +
                "                      \"rightPosition\": 489,\n" +
                "                      \"upperPosition\": 1204\n" +
                "                    }\n" +
                "                  },\n" +
                "                  \"place_of_service\": {\n" +
                "                    \"naturalKey\": \"place_of_service\",\n" +
                "                    \"predictedValue\": \"Office\",\n" +
                "                    \"confidenceScore\": 80,\n" +
                "                    \"paperType\": \"PRINTED\",\n" +
                "                    \"boundingBox\": {\n" +
                "                      \"leftPosition\": 366,\n" +
                "                      \"lowerPosition\": 1300,\n" +
                "                      \"rightPosition\": 546,\n" +
                "                      \"upperPosition\": 1258\n" +
                "                    }\n" +
                "                  }\n" +
                "                }\n" +
                "              },\n" +
                "              \"REQUESTING_PROVIDER_DETAILS\": {\n" +
                "                \"naturalKey\": \"REQUESTING_PROVIDER_DETAILS\",\n" +
                "                \"sorItemsDetails\": null\n" +
                "              },\n" +
                "              \"REFERRING_PROVIDER_DETAILS\": {\n" +
                "                \"naturalKey\": \"REFERRING_PROVIDER_DETAILS\",\n" +
                "                \"sorItemsDetails\": null\n" +
                "              },\n" +
                "              \"FAX_DETAILS\": {\n" +
                "                \"naturalKey\": \"FAX_DETAILS\",\n" +
                "                \"sorItemsDetails\": null\n" +
                "              },\n" +
                "              \"SERVICING_FACILITY_DETAILS\": {\n" +
                "                \"naturalKey\": \"SERVICING_FACILITY_DETAILS\",\n" +
                "                \"sorItemsDetails\": {\n" +
                "                  \"servicing_facility_provider_id\": {\n" +
                "                    \"naturalKey\": \"servicing_facility_provider_id\",\n" +
                "                    \"predictedValue\": \"\",\n" +
                "                    \"confidenceScore\": 0,\n" +
                "                    \"paperType\": \"PRINTED\",\n" +
                "                    \"boundingBox\": null\n" +
                "                  },\n" +
                "                  \"servicing_facility_tin\": {\n" +
                "                    \"naturalKey\": \"servicing_facility_tin\",\n" +
                "                    \"predictedValue\": \"\",\n" +
                "                    \"confidenceScore\": 0,\n" +
                "                    \"paperType\": \"PRINTED\",\n" +
                "                    \"boundingBox\": null\n" +
                "                  },\n" +
                "                  \"servicing_facility_first_name\": {\n" +
                "                    \"naturalKey\": \"servicing_facility_first_name\",\n" +
                "                    \"predictedValue\": \"\",\n" +
                "                    \"confidenceScore\": 0,\n" +
                "                    \"paperType\": \"PRINTED\",\n" +
                "                    \"boundingBox\": null\n" +
                "                  },\n" +
                "                  \"servicing_facility_city_state_zipcode\": {\n" +
                "                    \"naturalKey\": \"servicing_facility_city_state_zipcode\",\n" +
                "                    \"predictedValue\": \"BAKERSFIELD, CA 93301\",\n" +
                "                    \"confidenceScore\": 80,\n" +
                "                    \"paperType\": \"PRINTED\",\n" +
                "                    \"boundingBox\": {\n" +
                "                      \"leftPosition\": 892,\n" +
                "                      \"lowerPosition\": 1214,\n" +
                "                      \"rightPosition\": 1394,\n" +
                "                      \"upperPosition\": 1128\n" +
                "                    }\n" +
                "                  },\n" +
                "                  \"servicing_facility_fax\": {\n" +
                "                    \"naturalKey\": \"servicing_facility_fax\",\n" +
                "                    \"predictedValue\": \"\",\n" +
                "                    \"confidenceScore\": 0,\n" +
                "                    \"paperType\": \"PRINTED\",\n" +
                "                    \"boundingBox\": null\n" +
                "                  },\n" +
                "                  \"servicing_facility_phone\": {\n" +
                "                    \"naturalKey\": \"servicing_facility_phone\",\n" +
                "                    \"predictedValue\": \"\",\n" +
                "                    \"confidenceScore\": 0,\n" +
                "                    \"paperType\": \"PRINTED\",\n" +
                "                    \"boundingBox\": null\n" +
                "                  },\n" +
                "                  \"servicing_facility_last_name\": {\n" +
                "                    \"naturalKey\": \"servicing_facility_last_name\",\n" +
                "                    \"predictedValue\": \"\",\n" +
                "                    \"confidenceScore\": 0,\n" +
                "                    \"paperType\": \"PRINTED\",\n" +
                "                    \"boundingBox\": null\n" +
                "                  },\n" +
                "                  \"servicing_facility_state\": {\n" +
                "                    \"naturalKey\": \"servicing_facility_state\",\n" +
                "                    \"predictedValue\": \"CA\",\n" +
                "                    \"confidenceScore\": 80,\n" +
                "                    \"paperType\": \"PRINTED\",\n" +
                "                    \"boundingBox\": {\n" +
                "                      \"leftPosition\": 828,\n" +
                "                      \"lowerPosition\": 1214,\n" +
                "                      \"rightPosition\": 1394,\n" +
                "                      \"upperPosition\": 1127\n" +
                "                    }\n" +
                "                  },\n" +
                "                  \"servicing_facility_npi\": {\n" +
                "                    \"naturalKey\": \"servicing_facility_npi\",\n" +
                "                    \"predictedValue\": \"1538540497\",\n" +
                "                    \"confidenceScore\": 80,\n" +
                "                    \"paperType\": \"PRINTED\",\n" +
                "                    \"boundingBox\": {\n" +
                "                      \"leftPosition\": 235,\n" +
                "                      \"lowerPosition\": 941,\n" +
                "                      \"rightPosition\": 487,\n" +
                "                      \"upperPosition\": 882\n" +
                "                    }\n" +
                "                  },\n" +
                "                  \"servicing_facility_city\": {\n" +
                "                    \"naturalKey\": \"servicing_facility_city\",\n" +
                "                    \"predictedValue\": \"BAKERSFIELD\",\n" +
                "                    \"confidenceScore\": 80,\n" +
                "                    \"paperType\": \"PRINTED\",\n" +
                "                    \"boundingBox\": {\n" +
                "                      \"leftPosition\": 738,\n" +
                "                      \"lowerPosition\": 1218,\n" +
                "                      \"rightPosition\": 1395,\n" +
                "                      \"upperPosition\": 1128\n" +
                "                    }\n" +
                "                  },\n" +
                "                  \"servicing_facility_name\": {\n" +
                "                    \"naturalKey\": \"servicing_facility_name\",\n" +
                "                    \"predictedValue\": \"SPECIAL TREATMENT EDUCATION\",\n" +
                "                    \"confidenceScore\": 80,\n" +
                "                    \"paperType\": \"PRINTED\",\n" +
                "                    \"boundingBox\": {\n" +
                "                      \"leftPosition\": 245,\n" +
                "                      \"lowerPosition\": 1108,\n" +
                "                      \"rightPosition\": 491,\n" +
                "                      \"upperPosition\": 1043\n" +
                "                    }\n" +
                "                  },\n" +
                "                  \"servicing_facility_address\": {\n" +
                "                    \"naturalKey\": \"servicing_facility_address\",\n" +
                "                    \"predictedValue\": \"3838 SAN DUMAS ST STE B331\",\n" +
                "                    \"confidenceScore\": 80,\n" +
                "                    \"paperType\": \"PRINTED\",\n" +
                "                    \"boundingBox\": {\n" +
                "                      \"leftPosition\": 285,\n" +
                "                      \"lowerPosition\": 1052,\n" +
                "                      \"rightPosition\": 558,\n" +
                "                      \"upperPosition\": 1010\n" +
                "                    }\n" +
                "                  },\n" +
                "                  \"servicing_facility_zipcode\": {\n" +
                "                    \"naturalKey\": \"servicing_facility_zipcode\",\n" +
                "                    \"predictedValue\": \"93301\",\n" +
                "                    \"confidenceScore\": 80,\n" +
                "                    \"paperType\": \"PRINTED\",\n" +
                "                    \"boundingBox\": {\n" +
                "                      \"leftPosition\": 906,\n" +
                "                      \"lowerPosition\": 1215,\n" +
                "                      \"rightPosition\": 1394,\n" +
                "                      \"upperPosition\": 1130\n" +
                "                    }\n" +
                "                  }\n" +
                "                }\n" +
                "              },\n" +
                "              \"MEMBER_DETAILS\": {\n" +
                "                \"naturalKey\": \"MEMBER_DETAILS\",\n" +
                "                \"sorItemsDetails\": {\n" +
                "                  \"member_date_of_birth\": {\n" +
                "                    \"naturalKey\": \"member_date_of_birth\",\n" +
                "                    \"predictedValue\": \"21081996\",\n" +
                "                    \"confidenceScore\": 80,\n" +
                "                    \"paperType\": \"PRINTED\",\n" +
                "                    \"boundingBox\": {\n" +
                "                      \"leftPosition\": 331,\n" +
                "                      \"lowerPosition\": 557,\n" +
                "                      \"rightPosition\": 602,\n" +
                "                      \"upperPosition\": 488\n" +
                "                    }\n" +
                "                  },\n" +
                "                  \"member_id\": {\n" +
                "                    \"naturalKey\": \"member_id\",\n" +
                "                    \"predictedValue\": \"808732\",\n" +
                "                    \"confidenceScore\": 80,\n" +
                "                    \"paperType\": \"PRINTED\",\n" +
                "                    \"boundingBox\": {\n" +
                "                      \"leftPosition\": 1046,\n" +
                "                      \"lowerPosition\": 584,\n" +
                "                      \"rightPosition\": 1301,\n" +
                "                      \"upperPosition\": 541\n" +
                "                    }\n" +
                "                  },\n" +
                "                  \"member_zip_code\": {\n" +
                "                    \"naturalKey\": \"member_zip_code\",\n" +
                "                    \"predictedValue\": \"44531759\",\n" +
                "                    \"confidenceScore\": 80,\n" +
                "                    \"paperType\": \"PRINTED\",\n" +
                "                    \"boundingBox\": {\n" +
                "                      \"leftPosition\": 1077,\n" +
                "                      \"lowerPosition\": 661,\n" +
                "                      \"rightPosition\": 1586,\n" +
                "                      \"upperPosition\": 544\n" +
                "                    }\n" +
                "                  },\n" +
                "                  \"member_phone\": {\n" +
                "                    \"naturalKey\": \"member_phone\",\n" +
                "                    \"predictedValue\": \"855-576-1305\",\n" +
                "                    \"confidenceScore\": 80,\n" +
                "                    \"paperType\": \"PRINTED\",\n" +
                "                    \"boundingBox\": {\n" +
                "                      \"leftPosition\": 826,\n" +
                "                      \"lowerPosition\": 996,\n" +
                "                      \"rightPosition\": 1159,\n" +
                "                      \"upperPosition\": 910\n" +
                "                    }\n" +
                "                  },\n" +
                "                  \"member_address\": {\n" +
                "                    \"naturalKey\": \"member_address\",\n" +
                "                    \"predictedValue\": \"8340 KELLY PARK\",\n" +
                "                    \"confidenceScore\": 80,\n" +
                "                    \"paperType\": \"PRINTED\",\n" +
                "                    \"boundingBox\": {\n" +
                "                      \"leftPosition\": 256,\n" +
                "                      \"lowerPosition\": 637,\n" +
                "                      \"rightPosition\": 601,\n" +
                "                      \"upperPosition\": 571\n" +
                "                    }\n" +
                "                  },\n" +
                "                  \"member_city\": {\n" +
                "                    \"naturalKey\": \"member_city\",\n" +
                "                    \"predictedValue\": \"NEW ADAMSBIDE\",\n" +
                "                    \"confidenceScore\": 80,\n" +
                "                    \"paperType\": \"PRINTED\",\n" +
                "                    \"boundingBox\": {\n" +
                "                      \"leftPosition\": 676,\n" +
                "                      \"lowerPosition\": 861,\n" +
                "                      \"rightPosition\": 1364,\n" +
                "                      \"upperPosition\": 785\n" +
                "                    }\n" +
                "                  },\n" +
                "                  \"member_name\": {\n" +
                "                    \"naturalKey\": \"member_name\",\n" +
                "                    \"predictedValue\": \"MICHAEL PHILIPS\",\n" +
                "                    \"confidenceScore\": 80,\n" +
                "                    \"paperType\": \"PRINTED\",\n" +
                "                    \"boundingBox\": {\n" +
                "                      \"leftPosition\": 283,\n" +
                "                      \"lowerPosition\": 592,\n" +
                "                      \"rightPosition\": 519,\n" +
                "                      \"upperPosition\": 543\n" +
                "                    }\n" +
                "                  },\n" +
                "                  \"member_age\": {\n" +
                "                    \"naturalKey\": \"member_age\",\n" +
                "                    \"predictedValue\": \"\",\n" +
                "                    \"confidenceScore\": 0,\n" +
                "                    \"paperType\": \"PRINTED\",\n" +
                "                    \"boundingBox\": null\n" +
                "                  },\n" +
                "                  \"member_state\": {\n" +
                "                    \"naturalKey\": \"member_state\",\n" +
                "                    \"predictedValue\": \"NE\",\n" +
                "                    \"confidenceScore\": 80,\n" +
                "                    \"paperType\": \"PRINTED\",\n" +
                "                    \"boundingBox\": {\n" +
                "                      \"leftPosition\": 991,\n" +
                "                      \"lowerPosition\": 661,\n" +
                "                      \"rightPosition\": 1588,\n" +
                "                      \"upperPosition\": 545\n" +
                "                    }\n" +
                "                  },\n" +
                "                  \"member_first_name\": {\n" +
                "                    \"naturalKey\": \"member_first_name\",\n" +
                "                    \"predictedValue\": \"MICHAEL\",\n" +
                "                    \"confidenceScore\": 80,\n" +
                "                    \"paperType\": \"PRINTED\",\n" +
                "                    \"boundingBox\": {\n" +
                "                      \"leftPosition\": 276,\n" +
                "                      \"lowerPosition\": 598,\n" +
                "                      \"rightPosition\": 522,\n" +
                "                      \"upperPosition\": 544\n" +
                "                    }\n" +
                "                  },\n" +
                "                  \"member_city_state_zip_code\": {\n" +
                "                    \"naturalKey\": \"member_city_state_zip_code\",\n" +
                "                    \"predictedValue\": \"NEW ADAMSBIDE, NE, 44531759\",\n" +
                "                    \"confidenceScore\": 80,\n" +
                "                    \"paperType\": \"PRINTED\",\n" +
                "                    \"boundingBox\": {\n" +
                "                      \"leftPosition\": 793,\n" +
                "                      \"lowerPosition\": 859,\n" +
                "                      \"rightPosition\": 1363,\n" +
                "                      \"upperPosition\": 788\n" +
                "                    }\n" +
                "                  },\n" +
                "                  \"member_group_id\": {\n" +
                "                    \"naturalKey\": \"member_group_id\",\n" +
                "                    \"predictedValue\": \"\",\n" +
                "                    \"confidenceScore\": 0,\n" +
                "                    \"paperType\": \"PRINTED\",\n" +
                "                    \"boundingBox\": null\n" +
                "                  },\n" +
                "                  \"member_gender\": {\n" +
                "                    \"naturalKey\": \"member_gender\",\n" +
                "                    \"predictedValue\": \"\",\n" +
                "                    \"confidenceScore\": 0,\n" +
                "                    \"paperType\": \"PRINTED\",\n" +
                "                    \"boundingBox\": null\n" +
                "                  },\n" +
                "                  \"member_last_name\": {\n" +
                "                    \"naturalKey\": \"member_last_name\",\n" +
                "                    \"predictedValue\": \"PHILIPS\",\n" +
                "                    \"confidenceScore\": 80,\n" +
                "                    \"paperType\": \"PRINTED\",\n" +
                "                    \"boundingBox\": {\n" +
                "                      \"leftPosition\": 621,\n" +
                "                      \"lowerPosition\": 612,\n" +
                "                      \"rightPosition\": 830,\n" +
                "                      \"upperPosition\": 536\n" +
                "                    }\n" +
                "                  }\n" +
                "                }\n" +
                "              }\n" +
                "            }\n" +
                "          }\n" +
                "        },\n" +
                "        \"checkbox\": null,\n" +
                "        \"table\": null,\n" +
                "        \"currency\": null,\n" +
                "        \"bulletin\": null,\n" +
                "        \"paragraph\": null,\n" +
                "        \"aggregatedTableResult\": null\n" +
                "      }\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        String encryptionKey = "RgF7I3z5FC8k9HkKUm3Htb1HhZPBczdk";
        String encryptionType = AES_ENCRYPTION;
        String message = EncryptDecrypt.encrypt(messageNode, encryptionKey, encryptionType);
        System.out.println("Encrypted message\n" + message + "\nusing algorithm {}" + encryptionType);
    }


    @Test
    void executeProduct() throws Exception {

        KafkaPublish kafkaPublish = KafkaPublish.builder()
                .name("kafka")
                .condition(true)
                .resourceConn("intics_zio_db_conn")
                .outputTable("product_outbound.kafka_response_info")
                .querySet("select 'localhost' as endpoint, " +
                        "'elv_json' as topic_name, " +
                        "'SASL_PLAINTEXT' as auth_security_protocol, " +
                        "'PLAIN' as sasl_mechanism, " +
                        "'elevance' as user_name, " +
                        "'elevance@123' as password, " +
                        "'AES' as encryption_type, " +
                        "'RgF7I3z5FC8k9HkKUm3Htb1HhZPBczdk' as encryption_key, " +
                        "'{\"requestTxnId\":\"59af73da-7936-46f2-92f7-3da9bd997101\"," +
                        "\"status\":\"SUCCESS\"," +
                        "\"errorMessage\":\"\"," +
                        "\"errorMessageDetail\":\"\"," +
                        "\"errorCd\":null," +
                        "\"documentId\":\"IPCOMCA\"," +
                        "\"inboundTransactionId\":\"ITX-19\"," +
                        "\"metadata\":{" +
                        "\"documentType\":\"MEDICAL_COMMERCIAL\"," +
                        "\"documentExtension\":\"pdf\"," +
                        "\"transactionId\":\"TRZ-101\"," +
                        "\"inboundDocumentName\":\"IPCOMCA\"," +
                        "\"processStartTime\":\"2025-10-28T16:01:00.020994\"," +
                        "\"processEndTime\":\"2025-10-28T16:02:07.715301\"," +
                        "\"processingTimeMs\":67," +
                        "\"processedAt\":\"2025-10-28T16:01:19.179377\"," +
                        "\"pageCount\":0," +
                        "\"candidatePaper\":[]," +
                        "\"overallConfidence\":0" +
                        "}," +
                        "\"aumipayload\":{" +
                        "\"service\":[{\"cd\":{\"value\":\"\",\"page\":0,\"confidence\":0,\"boundingBox\":{\"x\":0.0,\"width\":0.0,\"y\":0.0,\"height\":0.0}}}]," +
                        "\"diagnosis\":[{\"cd\":{\"value\":\"\",\"page\":0,\"confidence\":0,\"boundingBox\":{\"x\":0.0,\"width\":0.0,\"y\":0.0,\"height\":0.0}},\"desc\":{\"value\":\"\",\"page\":0,\"confidence\":0,\"boundingBox\":{\"x\":0.0,\"width\":0.0,\"y\":0.0,\"height\":0.0}},\"codePointer\":{\"value\":\"\",\"page\":0,\"confidence\":0,\"boundingBox\":{\"x\":0.0,\"width\":0.0,\"y\":0.0,\"height\":0.0}}}]," +
                        "\"multipleMemberIndicator\":{\"value\":\"N\",\"page\":null,\"confidence\":null,\"boundingBox\":null}" +
                        "}" +
                        "}' as json_data, " +
                        "'FM12345678' as document_id, " +
                        "'1234567890' as file_checksum, " +
                        "'ORIGIN-123' as origin_id, " +
                        "'BATCH-1243' as batch_id, " +
                        "1 as tenant_id, " +
                        "'TRZ-XOO' as transaction_id")
                .build();

        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();
        actionExecutionAudit.getContext().putAll(Map.ofEntries(
                // ===== BASIC CONFIGURATION =====
                Map.entry("read.batch.size", "1"),
                Map.entry("write.batch.size", "1"),

                // ===== KAFKA BROKER CONFIGURATION =====
                Map.entry("kafka.endpoint", "localhost:9092"),
                Map.entry("kafka.topic.name", "elv_json"),
                Map.entry("kafka.groupid", "fastapi-group"),

                // ===== AUTHENTICATION CONFIGURATION =====
                Map.entry("kafka.auth.security.protocol", "SASL_PLAINTEXT"),
                Map.entry("kafka.sasl.mechanism", "PLAIN"),
                Map.entry("kafka.sasl.username", "elevance"),
                Map.entry("kafka.sasl.password", "elevance@123"),
                Map.entry("kafka.authentication.sasl.ssl.include", "certs"),

                // ===== AGGRESSIVE TIMEOUT CONFIGURATION TO TRIGGER RETRIES =====
                Map.entry("kafka.ssl.request.timeout.ms", "10"),        // Very low request timeout
                Map.entry("kafka.ssl.delivery.timeout.ms", "50"),      // Total delivery timeout
                Map.entry("kafka.ssl.linger.ms", "0"),                   // No batching delay
                Map.entry("kafka.ssl.acks", "0"),
                // ===== RETRY CONFIGURATION =====
                Map.entry("kafka.enable.retry", "true"),
                Map.entry("kafka.max.retry.attempts", "3"),
                Map.entry("kafka.retry.backoff.ms", "100"),
           // Short backoff
// Short backoff

                // ===== CONNECTION CONFIGURATION =====
                Map.entry("kafka.connections.max.idle.ms", "100"),   // Aggressive connection timeout
                Map.entry("kafka.reconnect.backoff.ms", "50"),       // Quick reconnection
                Map.entry("kafka.reconnect.backoff.max.ms", "100"),  // Max reconnection time

                // ===== OTHER CONFIGURATION =====
                Map.entry("kafka.publish.activator", "true"),
                Map.entry("kafka.topic.partition", "10"),
                Map.entry("outbound.context.condition", "Product"),
                Map.entry("consumer.API.count", "1"),
                Map.entry("outbound.doc.delivery.notify.url", ""),
                Map.entry("gen_group_id.group_id", "1"),
                Map.entry("agadia.secretKey", "")
        ));
        KafkaPublishAction kafkaPublishAction = new KafkaPublishAction(
                actionExecutionAudit,
                log,
                kafkaPublish
        );

        // This should now retry 3 times with exponential backoff
        kafkaPublishAction.execute();

        System.out.println("✓ Kafka publish completed (check logs for retry attempts)!");
    }

    }