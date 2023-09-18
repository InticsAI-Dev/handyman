package in.handyman.raven.lib;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.ProductResponse;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
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

        URL url = new URL("http://localhost:8189/alchemy/api/v1/transaction/product/response/TRZ-17/ORIGIN-3?tenantId=1");
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), "");

        Request request = new Request.Builder().url(url)
                .addHeader("accept", "*/*")
                .addHeader("Authorization", "Bearer " + "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJVc2VyIERldGFpbHMiLCJpc3MiOiJJbnRpY3NBSSBBbGNoZW15IiwiZXhwIjoxNjk1MDA0MTg4LCJpYXQiOjE2OTQ5NjA5ODgsImVtYWlsIjoiYWdhZGlhQGludGljcy5haSJ9.Zg6X5t6DsyV5asVMP6NYgRavXohl2FV33Tr8NgGN0jw")
                .addHeader("Content-Type", "application/json")
                .post(requestBody)
                .build();

        ActionExecutionAudit actionExecutionAudit=new ActionExecutionAudit();

        actionExecutionAudit.getContext().put("alchemy.product.response.url","http://localhost:8189/alchemy/api/v1/transaction/product/response");
        actionExecutionAudit.getContext().put("alchemyAuth.token","eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJVc2VyIERldGFpbHMiLCJpc3MiOiJJbnRpY3NBSSBBbGNoZW15IiwiZXhwIjoxNjk1MDYxMDAyLCJpYXQiOjE2OTUwMTc4MDIsImVtYWlsIjoiYWdhZGlhQGludGljcy5haSJ9.cJZqcGvaG1ehkhoR09h5GOASHv5otLrCRRzGyHG_LSc");
        actionExecutionAudit.getContext().put("alchemyAuth.tenantId","1");
        actionExecutionAudit.getContext().put("gen_group_id.group_id","1");
        actionExecutionAudit.getContext().put("read.batch.size","1");
        actionExecutionAudit.getContext().put("write.batch.size","1");

        ProductResponse productResponse = ProductResponse.builder()
                .tenantId(1L)
                .condition(true)
                .name("alchemy info action")
                .querySet("SELECT ap.alchemy_origin_id, 'TRZ-30' as alchemy_root_pipeline_id\n" +
                        "            FROM alchemy_migration.alchemy_migration_payload_queue ampq join alchemy_migration.alchemy_papers ap on ampq.origin_id = ap.pipeline_origin_id\n" +
                        "            where status = 'IN_PROGRESS' and ampq.group_id = 2 group by ap.alchemy_origin_id;")
                .resourceConn("intics_zio_db_conn")
                .token("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJVc2VyIERldGFpbHMiLCJpc3MiOiJJbnRpY3NBSSBBbGNoZW15IiwiZXhwIjoxNjk1MDYxMDAyLCJpYXQiOjE2OTUwMTc4MDIsImVtYWlsIjoiYWdhZGlhQGludGljcy5haSJ9.cJZqcGvaG1ehkhoR09h5GOASHv5otLrCRRzGyHG_LSc")
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
}