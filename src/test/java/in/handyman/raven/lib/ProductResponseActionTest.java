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
                .name("alchemy info action")
                .querySet("SELECT '' as origin_id, 'ITX-514' as transaction_id,'184789' as process_id,\n" +
                        "                 '4975' as group_id,\n" +
                        "                    ampq.tenant_id, '184789' as root_pipeline_id, 'OUTBOUND_ASIS' as feature, 'BATCH-4975_0' as batch_id\n" +
                        "                    FROM info.inbound_transaction ampq\n" +
                        "                    where ampq.inbound_transaction_id='ITX-514' and ampq.transaction_status = 'COMPLETED'\n" +
                        "                    and ampq.tenant_id = 115 ;")
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