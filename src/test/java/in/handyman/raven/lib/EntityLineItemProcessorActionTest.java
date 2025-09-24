package in.handyman.raven.lib;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.EntityLineItemProcessor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static in.handyman.raven.core.encryption.EncryptionConstants.ENCRYPT_ITEM_WISE_ENCRYPTION;
import static in.handyman.raven.core.encryption.EncryptionConstants.ENCRYPT_TEXT_EXTRACTION_OUTPUT;

@Slf4j
class EntityLineItemProcessorActionTest {

    private static final Pattern JSON_MARKER_PATTERN = Pattern.compile("(?s)```json\\s*(.*?)\\s*```");


    @Test
    void execute() throws Exception {

        EntityLineItemProcessor radonKvp = EntityLineItemProcessor.builder()
                .name("multi entity line item processor for sor transaction")
                .condition(true)
                .resourceConn("intics_zio_db_conn")
                .endpoint("https://intics.elevance.ngrok.dev/radon-vllm-server/predict")
                .outputTable("sor_transaction.radon_kvp_output_1")
                .querySet("SELECT a.input_file_path, a.user_prompt, a.process, a.paper_no, a.origin_id, a.process_id, a.group_id, a.tenant_id, a.root_pipeline_id, a.system_prompt,a.model_name,\n" +
                        "                    a.batch_id, a.model_registry, a.category, now() as created_on, (CASE WHEN '${sor.kvp.service.name.activator}' = 'RADON' then 'RADON START'\n" +
                        "                    WHEN '${sor.kvp.service.name.activator}' = 'KRYPTON' then 'KRYPTON START'\n" +
                        "                    WHEN '${sor.kvp.service.name.activator}' = 'NEON' then 'NEON START' end) as api_name,\n" +
                        "                    sc.sor_container_id, mep.container_name, entity, process_name as radon_process_name,\n" +
                        "                    priority_order, postprocessing_script\n" +
                        "                    FROM transit_data.radon_multi_section_kvp_input_990 a\n" +
                        "                    JOIN sor_meta.sor_container sc on a.sor_container_id=sc.sor_container_id\n" +
                        "                    join paper_filter.agentic_entity_level_score_audit acfbta\n" +
                        "                    on acfbta.sor_container_id = a.sor_container_id and a.origin_id = acfbta.origin_id \n" +
                        "                    and a.paper_no = acfbta.paper_no and a.batch_id = acfbta.batch_id\n" +
                        "                    join sor_meta.multi_entity_processing mep\n" +
                        "                    on mep.sor_container_id= acfbta.sor_container_id and mep.status = true and acfbta.layout = mep.entity\n" +
                        "WHERE a.model_registry = 'RADON'  and a.group_id ='14' and a.tenant_id='1' and a.batch_id ='BATCH-14_0';")
                .build();

        ActionExecutionAudit ac = new ActionExecutionAudit();
        ac.setRootPipelineId(1234L);
        ac.setActionId(1234L);
        ac.setProcessId(123L);
        ac.getContext().put("Radon.kvp.consumer.API.count", "2");
        ac.getContext().put("write.batch.size", "1");
        ac.getContext().put("read.batch.size", "1");
        ac.getContext().put("text.to.replace.prompt", "{%sreplaceable_value_of_the_previous_json}");
        ac.getContext().put("triton.request.radon.kvp.activator", "true");
        ac.getContext().put("prompt.base64.activator", "false");
        ac.getContext().put("copro.client.socket.timeout", "10");
        ac.getContext().put("copro.client.api.sleeptime", "10");
        ac.getContext().put("pipeline.copro.api.process.file.format", "BASE64");
        ac.getContext().put("pipeline.encryption.default.holder", "");
        ac.getContext().put(ENCRYPT_TEXT_EXTRACTION_OUTPUT, "true");
        ac.getContext().put("bbox.radon_bbox_activator", "false");
        ac.getContext().put(ENCRYPT_ITEM_WISE_ENCRYPTION, "false");
        ac.getContext().put("document_type", "MEDICAL_GBD");
        ac.getContext().put("pipeline.req.res.encryption", "true");
        ac.getContext().put("tenant_id", "1");
        ac.getContext().put("copro.request.activator.handler.name", "TRITON");
        ac.getContext().put("prompt.bbox.json.placeholder.name", "{%sreplaceable_value_of_the_previous_json}");
        ac.getContext().put("root-pipeline-name", "root.processor#6");
        ac.getContext().put("copro.processor.thread.creator", "FIXED_THREAD");


        EntityLineItemProcessorAction entityLineItemProcessorAction = new EntityLineItemProcessorAction(ac, log, radonKvp);

        entityLineItemProcessorAction.execute();
    }
    @Test
    void test() {
        String s = "{\\\\\\\\\\\\\\\"members\\\\\\\\\\\\\\\": [{\\\\\\\\\\\\\\\"id\\\\\\\\\\\\\\\": \\\\\\\\\\\\\\\"E1\\\\\\\\\\\\\\\", \\\\\\\\\\\\\\\"m\\\\\\\\\\\\\\\": \\\\\\\\\\\\\\\"T\\\\\\\\\\\\\\\", \\\\\\\\\\\\\\\"h\\\\\\\\\\\\\\\": \\\\\\\\\\\\\\\"Patient Information\\\\\\\\\\\\\\\", \\\\\\\\\\\\\\\"f\\\\\\\\\\\\\\\": {\\\\\\\\\\\\\\\"member_id\\\\\\\\\\\\\\\": [{\\\\\\\\\\\\\\\"v\\\\\\\\\\\\\\\": \\\\\\\\\\\\\\\"7470673897\\\\\\\\\\\\\\\", \\\\\\\\\\\\\\\"sl\\\\\\\\\\\\\\\": \\\\\\\\\\\\\\\"Medicaid ID\\\\\\\\\\\\\\\"}], \\\\\\\\\\\\\\\"member_name\\\\\\\\\\\\\\\": [{\\\\\\\\\\\\\\\"v\\\\\\\\\\\\\\\": \\\\\\\\\\\\\\\"HILLEN ILA\\\\\\\\\\\\\\\", \\\\\\\\\\\\\\\"sl\\\\\\\\\\\\\\\": \\\\\\\\\\\\\\\"Patient\\\\\\\\\\\\\\\"}], \\\\\\\\\\\\\\\"first_name\\\\\\\\\\\\\\\": [{\\\\\\\\\\\\\\\"v\\\\\\\\\\\\\\\": \\\\\\\\\\\\\\\"ILA\\\\\\\\\\\\\\\", \\\\\\\\\\\\\\\"sl\\\\\\\\\\\\\\\": \\\\\\\\\\\\\\\"First Name\\\\\\\\\\\\\\\"}], \\\\\\\\\\\\\\\"middle_name\\\\\\\\\\\\\\\": [], \\\\\\\\\\\\\\\"last_name\\\\\\\\\\\\\\\": [{\\\\\\\\\\\\\\\"v\\\\\\\\\\\\\\\": \\\\\\\\\\\\\\\"HILLEN\\\\\\\\\\\\\\\", \\\\\\\\\\\\\\\"sl\\\\\\\\\\\\\\\": \\\\\\\\\\\\\\\"Last Name\\\\\\\\\\\\\\\"}], \\\\\\\\\\\\\\\"date_of_birth\\\\\\\\\\\\\\\": [{\\\\\\\\\\\\\\\"v\\\\\\\\\\\\\\\": \\\\\\\\\\\\\\\"06/01/1985\\\\\\\\\\\\\\\", \\\\\\\\\\\\\\\"sl\\\\\\\\\\\\\\\": \\\\\\\\\\\\\\\"Date of Birth (MM/DD/YYYY)\\\\\\\\\\\\\\\"}], \\\\\\\\\\\\\\\"gender\\\\\\\\\\\\\\\": [], \\\\\\\\\\\\\\\"address_line1\\\\\\\\\\\\\\\": [{\\\\\\\\\\\\\\\"v\\\\\\\\\\\\\\\": \\\\\\\\\\\\\\\"12173 Volcano Rd\\\\\\\\\\\\\\\", \\\\\\\\\\\\\\\"sl\\\\\\\\\\\\\\\": \\\\\\\\\\\\\\\"Street Address\\\\\\\\\\\\\\\"}], \\\\\\\\\\\\\\\"address_line2\\\\\\\\\\\\\\\": [{\\\\\\\\\\\\\\\"v\\\\\\\\\\\\\\\": \\\\\\\\\\\\\\\"Poujol, Alain\\\\\\\\\\\\\\\", \\\\\\\\\\\\\\\"sl\\\\\\\\\\\\\\\": \\\\\\\\\\\\\\\"Emergency Contact Name\\\\\\\\\\\\\\\"}], \\\\\\\\\\\\\\\"city\\\\\\\\\\\\\\\": [{\\\\\\\\\\\\\\\"v\\\\\\\\\\\\\\\": \\\\\\\\\\\\\\\"813-768-4001\\\\\\\\\\\\\\\", \\\\\\\\\\\\\\\"sl\\\\\\\\\\\\\\\": \\\\\\\\\\\\\\\"City, State\\\\\\\\\\\\\\\"}], \\\\\\\\\\\\\\\"state\\\\\\\\\\\\\\\": [], \\\\\\\\\\\\\\\"zipcode\\\\\\\\\\\\\\\": []}}]}";

        String result = unescapeJsonString(s);
        System.out.println(result);
    }

    public String unescapeJsonString(String escapedJson) {
        ObjectMapper mapper = new ObjectMapper();
        String current = escapedJson;
        int maxTries = 10;

        for (int i = 0; i < maxTries; i++) {
            try {
                String unescaped = mapper.readValue("\"" + current + "\"", String.class);

                if (!unescaped.contains("\\\"")) {
                    return unescaped;
                }

                current = unescaped;
            } catch (Exception e) {
                log.warn("Failed to unescape JSON string at level " + i, e);
                break;
            }
        }

        return current;
    }

    public String unescapeJsonString1(String escapedJson) {
        try {
            return new ObjectMapper().readValue("\"" + escapedJson + "\"", String.class);
        } catch (Exception e) {
            log.warn("Failed to unescape JSON string", e);
            return escapedJson;
        }
    }

    @Test
    void testFormattedJsonString() {
        String input = "{\\\"members\\\": [{\\\"id\\\": \\\"E1\\\", \\\"m\\\": \\\"K\\\", \\\"h\\\": \\\"MRN\\\", \\\"f\\\": {\\\"member_id\\\": [{\\\"v\\\": \\\"D3774445\\\", \\\"sl\\\": \\\"MRN\\\"}], \\\"date_of_birth\\\": [{\\\"v\\\": \\\"6/5/2025\\\", \\\"sl\\\": \\\"DOB\\\"}], \\\"gender\\\": [{\\\"v\\\": \\\"M\\\", \\\"sl\\\": \\\"Legal Sex\\\"}]}}]}";

        String result = formattedJsonString(input);
        System.out.println("Extracted JSON:\n" + result);
    }


    public String formattedJsonString(String jsonResponse) {
        try {
            if (jsonResponse == null) {
                return null;
            }

            jsonResponse = jsonResponse.trim();

            if (jsonResponse.contains("```json")) {
                log.debug("Input contains ```json``` markers; extracting JSON block");
                Matcher matcher = JSON_MARKER_PATTERN.matcher(jsonResponse);
                if (matcher.find()) {
                    String jsonString = matcher.group(1).replace("\n", "");
                    String repaired = repairJson(jsonString);
                    return repaired.isEmpty() ? null : repaired;
                } else {
                    return repairJson(jsonResponse);
                }
            } else if (jsonResponse.contains("{") || jsonResponse.contains("[")) {
                log.debug("Input seems like JSON, returning as-is (no markers)");
                return jsonResponse;
            } else {
                log.debug("Input not JSON-like or missing markers");
                return null;
            }
        } catch (Exception e) {
            HandymanException exception = new HandymanException(e);
            HandymanException.insertException("Error in formattedJsonString for LLM JSON parser action", exception, new ActionExecutionAudit());
            return null;
        }
    }



    private String repairJson(String jsonString) {
        jsonString = addMissingQuotes(jsonString);
        jsonString = balanceBracesAndBrackets(jsonString);
        jsonString = assignEmptyValues(jsonString);
        return jsonString;
    }

    private String addMissingQuotes(String jsonString) {
        jsonString = jsonString.replaceAll("(\\{|,\\s*)(\\w+)(?=\\s*:)", "$1\"$2\"");
        jsonString = jsonString.replaceAll("(?<=:)\\s*([^\"\\s,\\n}\\]]+)(?=\\s*(,|}|\\n|\\]))", "\"$1\"");
        return jsonString;
    }

    private String balanceBracesAndBrackets(String jsonString) {
        int openBraces = 0;
        int closeBraces = 0;
        int openBrackets = 0;
        int closeBrackets = 0;
        for (char c : jsonString.toCharArray()) {
            if (c == '{') openBraces++;
            if (c == '}') closeBraces++;
            if (c == '[') openBrackets++;
            if (c == ']') closeBrackets++;
        }
        StringBuilder builder = new StringBuilder(jsonString);
        while (openBraces > closeBraces) {
            builder.append('}');
            closeBraces++;
        }
        while (openBrackets > closeBrackets) {
            builder.append(']');
            closeBrackets++;
        }
        return builder.toString();
    }

    private String assignEmptyValues(String jsonString) {
        jsonString = jsonString.replaceAll("(?<=:)\\s*(?=,|\\s*}|\\s*\\])", "\"\"");
        return jsonString;
    }

}