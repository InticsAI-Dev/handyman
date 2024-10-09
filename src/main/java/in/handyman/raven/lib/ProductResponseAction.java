package in.handyman.raven.lib;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.access.ResourceAccess;
import in.handyman.raven.lambda.action.ActionExecution;
import in.handyman.raven.lambda.action.IActionExecution;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.alchemy.common.AlchemyApiPayload;
import in.handyman.raven.lib.model.ProductResponse;
import in.handyman.raven.util.ExceptionUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import okhttp3.*;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.argument.Arguments;
import org.jdbi.v3.core.argument.NullArgument;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Types;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Auto Generated By Raven
 */
@ActionExecution(
        actionName = "ProductResponse"
)
public class ProductResponseAction implements IActionExecution {
    private final ActionExecutionAudit action;

    private final Logger log;

    private final ProductResponse productResponse;

    private final Marker aMarker;

    public ProductResponseAction(final ActionExecutionAudit action, final Logger log,
                                 final Object productResponse) {
        this.productResponse = (ProductResponse) productResponse;
        this.action = action;
        this.log = log;
        this.aMarker = MarkerFactory.getMarker(" ProductResponse:" + this.productResponse.getName());
    }

    @Override
    public void execute() throws Exception {
        try {
            final Jdbi jdbi = ResourceAccess.rdbmsJDBIConn(productResponse.getResourceConn());
            jdbi.getConfig(Arguments.class).setUntypedNullArgument(new NullArgument(Types.NULL));
            log.info(aMarker, "Product Response Action for {} has been started", productResponse.getName());
            final String insertQuery = "INSERT INTO " + productResponse.getResultTable() +
                    "(process_id,group_id,origin_id,product_response, tenant_id,root_pipeline_id,status,stage,message,feature,triggered_url, batch_id) " +
                    " VALUES(?,?,?,?,?,?,?,?,?,?,?, ?)";
            String moduleVariable = "alchemy.product.response.url";
            final List<URL> urls = Optional.ofNullable(action.getContext().get(moduleVariable))
                    .map(s -> Arrays.stream(s.split(",")).map(s1 -> {
                        try {
                            return new URL(s1);
                        } catch (MalformedURLException e) {
                            log.error("Error in processing the URL ", e);
                            throw new HandymanException("Malformed URL: " + s1, e);
                        }
                    }).collect(Collectors.toList()))
                    .orElse(Collections.emptyList());

            final CoproProcessor<ProductResponseAction.ProductResponseInputTable, ProductResponseAction.ProductResponseOutputTable> coproProcessor =
                    new CoproProcessor<>(new LinkedBlockingQueue<>(),
                            ProductResponseAction.ProductResponseOutputTable.class,
                            ProductResponseAction.ProductResponseInputTable.class,
                            jdbi, log,
                            new ProductResponseAction.ProductResponseInputTable(), urls, action);
            coproProcessor.startProducer(productResponse.getQuerySet(), Integer.valueOf(action.getContext().get("read.batch.size")));
            Thread.sleep(1000);
            coproProcessor.startConsumer(insertQuery, 1, Integer.valueOf(action.getContext().get("write.batch.size")), new ProductResponseAction.ProductResponseConsumerProcess(log, aMarker, action), moduleVariable);
            log.info(aMarker, "Product Response has been completed {}  ", productResponse.getName());
        } catch (Exception t) {
            log.error(aMarker, "Error at Product Response execute method {}", ExceptionUtil.toString(t));
            throw new HandymanException("Error at Product Response execute method ", t, action);
        }
    }

    @Override
    public boolean executeIf() throws Exception {
        return productResponse.getCondition();
    }

    public static class ProductResponseConsumerProcess implements CoproProcessor.ConsumerProcess<ProductResponseAction.ProductResponseInputTable, ProductResponseAction.ProductResponseOutputTable> {
        private final Logger log;
        private final Marker aMarker;

        public final ActionExecutionAudit action;

        private final Long tenantId;
        private final String authToken;
        private final ObjectMapper mapper = JsonMapper.builder()
                .configure(SerializationFeature.INDENT_OUTPUT, true)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build().registerModule(new JavaTimeModule());
        final OkHttpClient httpclient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.MINUTES)
                .writeTimeout(10, TimeUnit.MINUTES)
                .readTimeout(10, TimeUnit.MINUTES)
                .build();

        public ProductResponseConsumerProcess(final Logger log, final Marker aMarker, ActionExecutionAudit action) {
            this.log = log;
            this.aMarker = aMarker;
            this.action = action;
            this.tenantId = Long.valueOf(action.getContext().get("alchemyAuth.tenantId"));
            this.authToken = action.getContext().get("alchemyAuth.token");
        }

        @Override
        public List<ProductResponseAction.ProductResponseOutputTable> process(URL endpoint, ProductResponseAction.ProductResponseInputTable entity) throws Exception {

            List<ProductResponseAction.ProductResponseOutputTable> parentObj = new ArrayList<>();
            Long rootPipelineId = entity.getRootPipelineId();

            RequestBody requestBody = RequestBody.create("", MediaType.parse("application/json; charset=utf-8"));

            String originId = entity.getOriginId();

            Request request = getUrlFromFeature(endpoint, entity.getFeature(), entity.getTransactionId(), originId, entity.getTenantId(), requestBody);


            try (Response response = httpclient.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String responseBody = Objects.requireNonNull(response.body()).string();
                    AlchemyApiPayload alchemyApiPayload = mapper.readValue(responseBody, AlchemyApiPayload.class);

                    if (!alchemyApiPayload.getPayload().isEmpty() && !alchemyApiPayload.getPayload().isNull() && alchemyApiPayload.isSuccess()) {

                        parentObj.add(ProductResponseOutputTable
                                .builder()
                                .processId(entity.getProcessId())
                                .tenantId(tenantId)
                                .groupId(entity.getGroupId())
                                .productResponse(String.valueOf(alchemyApiPayload.getPayload()))
                                .originId(originId)
                                .rootPipelineId(rootPipelineId)
                                .stage("PRODUCT_OUBOUND")
                                .triggeredUrl(request.url().toString())
                                .feature(entity.getFeature())
                                .status("COMPLETED")
                                .message("alchemy product response completed for origin_id - " + originId)
                                .batchId(entity.getBatchId())
                                .build());
                    }
                } else {
                    parentObj.add(ProductResponseOutputTable
                            .builder()
                            .processId(entity.getProcessId())
                            .tenantId(tenantId)
                            .groupId(entity.getGroupId())
                            .originId(originId)
                            .rootPipelineId(rootPipelineId)
                            .triggeredUrl(request.url().toString())
                            .feature(entity.getFeature())
                            .stage("PRODUCT_OUBOUND")
                            .status("FAILED")
                            .message("alchemy product response failed for origin_id - " + originId)
                            .batchId(entity.getBatchId())
                            .build());
                }
            } catch (Exception e) {
                log.error(aMarker, "The Exception occurred in product response action", e);
                HandymanException handymanException = new HandymanException(e);
                HandymanException.insertException("Exception occurred in Product Response action for originId - " + originId, handymanException, this.action);
            }
            return parentObj;
        }

        @NotNull
        public Request getUrlFromFeature(URL baseUrl,@NotNull String feature, String transactionId, String originId, Long tenantId, RequestBody requestBody) throws MalformedURLException {
            Request request;
            if (Objects.equals(feature, "Product")) {

                URL url = new URL(baseUrl + "response/" + transactionId + "/" + originId + "/?tenantId=" + tenantId);
                log.info(aMarker, "product api called with the url {}", url);
                request = new Request.Builder().url(url)
                        .addHeader("accept", "*/*")
                        .addHeader("Authorization", "Bearer " + authToken)
                        .addHeader("Content-Type", "application/json")
                        .post(requestBody)
                        .build();

                return request;


            } else {
                URL url = new URL(baseUrl + "response/featureResponse/" + transactionId + "/" + originId + "/" + feature + "?tenantId=" + tenantId);
                log.info(aMarker, "Feature based {} api called with the url {}", feature, url);
                request = new Request.Builder().url(url)
                        .addHeader("accept", "*/*")
                        .addHeader("Authorization", "Bearer " + authToken)
                        .addHeader("Content-Type", "application/json")
                        .build();

            }
            return request;

        }
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Builder
    public static class ProductResponseInputTable implements CoproProcessor.Entity {
        private String originId;
        private String transactionId;
        private Integer processId;
        private Long groupId;
        private Long tenantId;
        private Long rootPipelineId;
        private String baseUrl;
        private String feature;
        private String batchId;

        @Override
        public List<Object> getRowData() {
            return null;
        }
    }


    @AllArgsConstructor
    @Data
    @Builder
    public static class ProductResponseOutputTable implements CoproProcessor.Entity {

        private Integer processId;
        private Long groupId;
        private String productResponse;
        private String originId;
        private Long tenantId;
        private Long rootPipelineId;
        private String status;
        private String stage;
        private String message;
        private String triggeredUrl;
        private String feature;
        private String batchId;

        @Override
        public List<Object> getRowData() {
            return Stream.of(this.processId, this.groupId, this.originId, this.productResponse, this.tenantId, this.rootPipelineId, this.status, this.stage, this.message, this.feature, this.triggeredUrl,this.batchId).collect(Collectors.toList());
        }
    }
}