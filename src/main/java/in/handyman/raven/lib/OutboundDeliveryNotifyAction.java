package in.handyman.raven.lib;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.access.ResourceAccess;
import in.handyman.raven.lambda.action.ActionExecution;
import in.handyman.raven.lambda.action.IActionExecution;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.agadia.outbound.delivery.adapters.OutboundAdapter;
import in.handyman.raven.lib.agadia.outbound.delivery.adapters.OutboundAdapterProduct;
import in.handyman.raven.lib.model.OutboundDeliveryNotify;
import in.handyman.raven.util.CommonQueryUtil;
import in.handyman.raven.util.InstanceUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.pdfbox.util.Hex;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.result.ResultIterable;
import org.jdbi.v3.core.statement.Query;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Auto Generated By Raven
 */
@ActionExecution(
        actionName = "OutboundDeliveryNotify"
)
public class OutboundDeliveryNotifyAction implements IActionExecution {
    private final ActionExecutionAudit action;

    private final Logger log;
    private final OutboundDeliveryNotify outboundDeliveryNotify;
    private final MediaType MediaTypeJSON = MediaType.parse("application/json; charset=utf-8");
    final ObjectMapper MAPPER = new ObjectMapper();
    private final Marker aMarker;
    String deliveryNotifyUrl;
    String appId;
    String appKeyId;
    private final OutboundAdapter outboundAdapter;
    private final OutboundAdapterProduct outboundAdapterProduct;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OutboundDeliveryNotifyAction(final ActionExecutionAudit action, final Logger log,
                                        final Object outboundDeliveryNotify) {
        this.outboundDeliveryNotify = (OutboundDeliveryNotify) outboundDeliveryNotify;
        this.action = action;
        this.log = log;
        this.appId = action.getContext().get("agadia.appId");
        this.appKeyId = action.getContext().get("agadia.appKeyId");
        this.deliveryNotifyUrl = action.getContext().get("doc.delivery.notify.url");
        this.outboundAdapter = new OutboundAdapter(log,objectMapper,action);
        this.outboundAdapterProduct = new OutboundAdapterProduct(log,objectMapper,action);
        this.aMarker = MarkerFactory.getMarker(" OutboundDeliveryNotify:" + this.outboundDeliveryNotify.getName());
    }

    @Override
    public void execute() throws Exception {
        try {

            final Jdbi jdbi = ResourceAccess.rdbmsJDBIConn(outboundDeliveryNotify.getResourceConn());
            final List<TableInputQuerySet> tableInfos = new ArrayList<>();

            jdbi.useTransaction(handle -> {
                final List<String> formattedQuery = CommonQueryUtil.getFormattedQuery(outboundDeliveryNotify.getQuerySet());
                AtomicInteger i = new AtomicInteger(0);
                formattedQuery.forEach(sqlToExecute -> {
                    log.info(aMarker, "executing  query {} from index {}", sqlToExecute, i.getAndIncrement());
                    Query query = handle.createQuery(sqlToExecute);
                    ResultIterable<TableInputQuerySet> resultIterable = query.mapToBean(TableInputQuerySet.class);
                    List<TableInputQuerySet> detailList = resultIterable.stream().collect(Collectors.toList());
                    tableInfos.addAll(detailList);
                    log.info(aMarker, "executed query from index {}", i.get());
                });
            });


            tableInfos.forEach(tableInputQuerySet -> {

                String response = doOutboundApiCall(tableInputQuerySet.getOutboundCondition(), tableInputQuerySet);


            });

        } catch (Exception ex) {
            log.error(aMarker, "Error in execute method for Outbound Delivery Notification Action", ex);
            throw new HandymanException("Error in execute method for Outbound Delivery Notification Action", ex, action);

        }
    }

    @Override
    public boolean executeIf() throws Exception {
        return outboundDeliveryNotify.getCondition();
    }

    public String doOutboundApiCall(final String outboundCondition,TableInputQuerySet tableInputQuerySet){
        String response = null;
        switch (outboundCondition) {
            case "Agadia":
                response= this.outboundAdapter.requestApiCaller(tableInputQuerySet);
                break;
            case "Product":
                response = this.outboundAdapterProduct.requestApiCaller(tableInputQuerySet);
                break;

        }
        return response;
        
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class TableInputQuerySet{
        private String fileName;
        private String fileUri;
        private String zipFileCheckSum;
        private String endpoint;
        private String appId;
        private String appSecretKey;
        private String bearerToken;
        private String documentId;
        private String outboundCondition;
        private String outboundJson;
        private String outboundZip;
    }
}
