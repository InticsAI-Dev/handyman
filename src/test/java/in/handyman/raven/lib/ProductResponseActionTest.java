package in.handyman.raven.lib;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.ProductResponse;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.util.concurrent.TimeUnit;

@Slf4j
class ProductResponseActionTest {

    final OkHttpClient httpclient = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.MINUTES)
            .writeTimeout(10, TimeUnit.MINUTES)
            .readTimeout(10, TimeUnit.MINUTES)
            .build();

    @Test
    void execute() throws Exception {

        URL url = new URL("http://localhost:8189/alchemy/api/v1/response/TRZ-26/INT-36?tenantId=1");
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), "");

        Request request = new Request.Builder().url(url)
                .addHeader("accept", "*/*")
                .addHeader("Authorization", "Bearer " + "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJVc2VyIERldGFpbHMiLCJpc3MiOiJJbnRpY3NBSSBBbGNoZW15IiwiZXhwIjoxNjk3ODM3MTk0LCJpYXQiOjE2OTc3NTA3OTQsImVtYWlsIjoiZGpAaW50aWNzLmFpIn0.OxBLAc4BQHeyQBoDjuAzaqea5ShEKrckgrjKhQ9iWAs")
                .addHeader("Content-Type", "application/json")
                .post(requestBody)
                .build();

        ActionExecutionAudit actionExecutionAudit=new ActionExecutionAudit();

        actionExecutionAudit.getContext().put("alchemy.product.response.url","http://localhost:8189/alchemy/api/v1/response");
        actionExecutionAudit.getContext().put("alchemyAuth.token","eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJVc2VyIERldGFpbHMiLCJpc3MiOiJJbnRpY3NBSSBBbGNoZW15IiwiZXhwIjoxNjk3ODM3MTk0LCJpYXQiOjE2OTc3NTA3OTQsImVtYWlsIjoiZGpAaW50aWNzLmFpIn0.OxBLAc4BQHeyQBoDjuAzaqea5ShEKrckgrjKhQ9iWAs");
        actionExecutionAudit.getContext().put("alchemyAuth.tenantId","8");
        actionExecutionAudit.getContext().put("gen_group_id.group_id","33");
        actionExecutionAudit.getContext().put("read.batch.size","1");
        actionExecutionAudit.getContext().put("write.batch.size","1");

        ProductResponse productResponse = ProductResponse.builder()
                .tenantId(1L)
                .condition(true)
                .resultTable("alchemy_response.alchemy_product_response")
                .name("alchemy info action")
                .querySet("SELECT\n" +
                        "    p.prediction_id,\n" +
                        "    p.created_on,\n" +
                        "    p.created_user_id,\n" +
                        "    p.last_updated_on,\n" +
                        "    p.last_updated_user_id,\n" +
                        "    p.status,\n" +
                        "    p.version,\n" +
                        "    p.encode,\n" +
                        "    p.feature,\n" +
                        "    p.label,\n" +
                        "    p.left_pos,\n" +
                        "    p.lower_pos,\n" +
                        "    p.origin_id,\n" +
                        "    p.precision,\n" +
                        "    p.predicted_value,\n" +
                        "    p.question_id,\n" +
                        "    p.right_pos,\n" +
                        "    p.root_pipeline_id,\n" +
                        "    p.state,\n" +
                        "    p.synonym_id,\n" +
                        "    p.table_data,\n" +
                        "    p.tenant_id,\n" +
                        "    p.transaction_id,\n" +
                        "    p.upper_pos,\n" +
                        "    p.workspace_id,\n" +
                        "    p.truth_id,\n" +
                        "    p.channel_id,\n" +
                        "    p.csv_file_path,\n" +
                        "    p.sor_container_id,\n" +
                        "    p.truth_entity_id,\n" +
                        "    p.currency_ascii_value,\n" +
                        "    p.currency_value,\n" +
                        "    p.aggregated_json,\n" +
                        "    p.bulletin_points,\n" +
                        "    p.bulletin_section,\n" +
                        "    p.paragraph_points,\n" +
                        "    p.paragraph_section,\n" +
                        "    p.sor_item_id,\n" +
                        "    si.line_item_type,\n" +
                        "    si.sor_item_name,\n" +
                        "    sc.sor_container_name AS container_name,\n" +
                        "    p.sor_container_instance,\n" +
                        "    sc.is_multi_entity_enabled,\n" +
                        "    sot.paper_no,\n" +
                        "    a.image_width,\n" +
                        "    a.image_height,\n" +
                        "    m.meta_data AS metadata_json,\n" +
                        "    soo.group_id,\n" +
                        "    soo.batch_id\n" +
                        "FROM alchemy_migration.alchemy_migration_payload_queue_archive ampq\n" +
                        "join valuation.prediction p on ampq.origin_id =p.origin_id \n" +
                        "JOIN sor_meta.sor_item si \n" +
                        "    ON p.sor_item_id = si.sor_item_id\n" +
                        "JOIN sor_meta.sor_container sc \n" +
                        "    ON sc.sor_container_id = si.sor_container_id\n" +
                        "JOIN info.source_of_truth sot \n" +
                        "    ON sot.truth_id = p.truth_id\n" +
                        "JOIN info.asset a \n" +
                        "    ON a.asset_id = sot.asset_id\n" +
                        "JOIN info.source_of_origin soo \n" +
                        "    ON soo.origin_id = p.origin_id\n" +
                        "JOIN (\n" +
                        "    SELECT\n" +
                        "        soo.origin_id,\n" +
                        "        json_build_object(\n" +
                        "            'requestTxnId', idfd.request_txn_id,\n" +
                        "            'documentId', idfd.document_id,\n" +
                        "            'inboundTransactionId', idfd.inbound_transaction_id,\n" +
                        "            'transactionId', soo.transaction_id,\n" +
                        "            'inboundDocumentName', a.file_name,\n" +
                        "            'documentExtension', a.file_extension,\n" +
                        "            'documentType', '${document_type}','uploadStatus', ped.status,\n" +
                        "            'processStartTime', soo.created_on::timestamp ,\n" +
                        "            'processEndTime', ped.created_on::timestamp ,\n" +
                        "            'processedAt', soo.created_on,\n" +
                        "            'candidatePapers', array_agg(aelsa.paper_no),\n" +
                        "            'errorMessage', ped.error_message,\n" +
                        "            'errorMessageDetail', '',\n" +
                        "            'errorCode', ped.error_code\n" +
                        "        )::varchar AS meta_data\n" +
                        "    FROM info.source_of_origin soo\n" +
                        "    JOIN alchemy_response.pipeline_error_details ped \n" +
                        "        ON ped.origin_id = soo.origin_id\n" +
                        "    JOIN inbound_config.ingestion_downloaded_file_details idfd \n" +
                        "        ON idfd.transaction_id = soo.transaction_id\n" +
                        "    JOIN transit_data.agentic_entity_level_score_14785 aelsa \n" +
                        "        ON aelsa.origin_id = soo.origin_id\n" +
                        "    JOIN info.asset a \n" +
                        "        ON soo.asset_id = a.asset_id\n" +
                        "       AND idfd.file_name = a.file_name\n" +
                        "    GROUP BY\n" +
                        "        soo.origin_id,\n" +
                        "        idfd.request_txn_id,\n" +
                        "        ped.status,\n" +
                        "        ped.error_message,\n" +
                        "        ped.error_code,\n" +
                        "        idfd.document_id,\n" +
                        "        idfd.inbound_transaction_id,\n" +
                        "        a.file_extension,\n" +
                        "        soo.transaction_id,\n" +
                        "        a.file_name,\n" +
                        "        soo.created_on,\n" +
                        "        ped.created_on,\n" +
                        "        idfd.page_count\n" +
                        ") m\n" +
                        "    ON m.origin_id = p.origin_id;")
                .resourceConn("intics_zio_db_conn")
                .token("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJVc2VyIERldGFpbHMiLCJpc3MiOiJJbnRpY3NBSSBBbGNoZW15IiwiZXhwIjoxNjk3ODM3MTk0LCJpYXQiOjE2OTc3NTA3OTQsImVtYWlsIjoiZGpAaW50aWNzLmFpIn0.OxBLAc4BQHeyQBoDjuAzaqea5ShEKrckgrjKhQ9iWAs")
                .build();

//        try (Response response = httpclient.newCall(request).execute()) {
//            if (response.isSuccessful()) {
//                log.info("Response Details: {}", response);
//            }
//        } catch (Exception e) {
//            throw new IllegalStateException();

        ProductResponseAction productResponseAction = new ProductResponseAction(actionExecutionAudit, log, productResponse);
        productResponseAction.execute();
//        }

    }

    @Test
    void jsonNodeTest() throws JsonProcessingException {
        final ObjectMapper mapper = new ObjectMapper();
        JSONObject parentResponse = new JSONObject("{\"csvTablesPath\": [{\"rcnn_padd.cm1882524_0_0\": \"/home/logesh.b@zucisystems.com/workspace/dev/intics-agadia/pipeline/data/output/2/table_extraction/1392/INT-3/139147003665780118/tabel-extraction/CM1882524/rcnn_padd/CM1882524_0_0.csv\"}], \"tableResponse\": {\"payload\": [{\"encode\": \"\", \"tableData\": {\"columns\": [0, 1, 2, 3, 4, 5], \"data\": [[\"ITEMNUMBER\", \"DESCRIPTION\", \"QTY\", \"U/M\", \"UNITPRICE\", \"EXTPRICE\"], [\"SAN1735790\", \"MARKER,SHARPIE,UF,RT,BK refused. no paperwork.\", \"-1\", \"DZ\", \"8.65\", \"-8.65\"]]}}]}}");
        JSONArray filePathArray = new JSONArray(parentResponse.get("csvTablesPath").toString());
        JsonNode jsonNode = mapper.readTree(parentResponse.toString());
        JsonNode tableResponse = jsonNode.get("tableResponse").get("payload").get(0);
        System.out.println(filePathArray);
        System.out.println(tableResponse);

    }

    @Test
    void tableData() throws JsonProcessingException {
        final ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree("{\"payload\":[{\"encode\":\"\",\"tableData\":{\"data\":[[\"ITEMNUMBER\",\"DESCRIPTION\",\"QTY\",\"U/M\",\"UNITPRICE\",\"EXTPRICE\"],[\"SAN1735790\",\"MARKER,SHARPIE,UF,RT,BK refused. no paperwork.\",\"-1\",\"DZ\",\"8.65\",\"-8.65\"]],\"columns\":[0,1,2,3,4,5]}}]}");
        System.out.println(mapper.writeValueAsString(jsonNode.get("payload").get(0).get("tableData")));
    }
}