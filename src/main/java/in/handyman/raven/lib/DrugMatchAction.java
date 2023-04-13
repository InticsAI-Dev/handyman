package in.handyman.raven.lib;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.access.ResourceAccess;
import in.handyman.raven.lambda.action.ActionExecution;
import in.handyman.raven.lambda.action.IActionExecution;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.*;

import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import in.handyman.raven.util.InstanceUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import okhttp3.*;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.argument.Arguments;
import org.jdbi.v3.core.argument.NullArgument;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

/**
 * Auto Generated By Raven
 */
@ActionExecution(
        actionName = "DrugMatch"
)
public class DrugMatchAction implements IActionExecution {
    private final ActionExecutionAudit action;
    private final Logger log;
    private final DrugMatch drugMatch;
    private final Marker aMarker;

    public DrugMatchAction(final ActionExecutionAudit action, final Logger log,
                           final Object drugMatch) {
        this.drugMatch = (DrugMatch) drugMatch;
        this.action = action;
        this.log = log;
        this.aMarker = MarkerFactory.getMarker(" DrugMatch:" + this.drugMatch.getName());
    }

    @Override
    public void execute() throws Exception {
        log.info(aMarker, "drug match process for {} has been started" + drugMatch.getName());
        try {

            final Jdbi jdbi = ResourceAccess.rdbmsJDBIConn(drugMatch.getResourceConn());
            jdbi.getConfig(Arguments.class).setUntypedNullArgument(new NullArgument(Types.NULL));
            // build insert prepare statement with output table columns
            final String insertQuery = "INSERT INTO " + drugMatch.getDrugCompare() +
                    " (origin_id ,eoc_identifier,paper_no,created_on,document_id,drug_name,drug_jcode,actual_value,status,stage,message)" +
                    " VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
            log.info(aMarker, "Drug Match Insert query {}", insertQuery);

            //3. initiate copro processor and copro urls
            final List<URL> urls = Optional.ofNullable(action.getContext().get("drugname.api.url")).map(s -> Arrays.stream(s.split(",")).map(s1 -> {
                try {
                    return new URL(s1);
                } catch (MalformedURLException e) {
                    log.error("Error in processing the URL ", e);
                    throw new RuntimeException(e);
                }
            }).collect(Collectors.toList())).orElse(Collections.emptyList());
            log.info(aMarker, "Drug Match copro urls {}", urls);

            final CoproProcessor<DrugMatchInputTable, DrugMatchOutputTable> coproProcessor =
                    new CoproProcessor<>(new LinkedBlockingQueue<>(),
                            DrugMatchOutputTable.class,
                            DrugMatchInputTable.class,
                            jdbi, log,
                            new DrugMatchInputTable(), urls, action);

            log.info(aMarker, "Drug Match coproProcessor initialization  {}", coproProcessor);

            //4. call the method start producer from coproprocessor
            coproProcessor.startProducer(drugMatch.getInputSet(), Integer.valueOf(action.getContext().get("read.batch.size")));
            log.info(aMarker, "Drug Match copro coproProcessor startProducer called read batch size {}", action.getContext().get("read.batch.size"));
            Thread.sleep(1000);
            coproProcessor.startConsumer(insertQuery, Integer.valueOf(action.getContext().get("consumer.API.count")), Integer.valueOf(action.getContext().get("write.batch.size")),
                    new DrugMatchConsumerProcess(log, aMarker, action));
            log.info(aMarker, "Drug Match copro coproProcessor startConsumer called consumer count {} write batch count {} ", Integer.valueOf(action.getContext().get("consumer.API.count")), Integer.valueOf(action.getContext().get("write.batch.size")));


        } catch (Exception ex) {
            log.error(aMarker, "error in execute method for Drug Match  ", ex);
        }
        log.info(aMarker, "drug match process for {} has been completed" + drugMatch.getName());
    }


    public static class DrugMatchConsumerProcess implements CoproProcessor.ConsumerProcess<DrugMatchInputTable, DrugMatchOutputTable> {
        private final Logger log;
        private final Marker aMarker;
        public final ActionExecutionAudit action;
        private String appId;
        private String appKeyId;
        String URI;
        final OkHttpClient httpclient;

        private final MediaType MediaTypeJSON = MediaType.parse("application/json; charset=utf-8");

        public DrugMatchConsumerProcess(Logger log, Marker aMarker, ActionExecutionAudit action) {
            this.log = log;
            this.aMarker = aMarker;
            this.action = action;
            this.appId = action.getContext().get("agadia.appId");
            this.appKeyId = action.getContext().get("agadia.appKeyId");
            this.httpclient = InstanceUtil.createOkHttpClient();
        }

        @Override
        public List<DrugMatchOutputTable> process(URL endpoint, DrugMatchInputTable result) throws Exception {
            URI = String.valueOf(endpoint);
            log.info(aMarker, "coproProcessor consumer process started with endpoint {} and entity {}", endpoint, result);
            List<DrugMatchOutputTable> parentObj = new ArrayList<>();
            AtomicInteger atomicInteger = new AtomicInteger();
            final String requestString;
            ObjectMapper mapper = new ObjectMapper();
            if (result.getDrugName() != null) {
                DrugNameRequest drugNameRequest = DrugNameRequest.builder()
                        .drugName(result.drugName)
                        .jCode(result.jCode)
                        .build();
                try {
                    requestString = mapper.writeValueAsString(drugNameRequest);
                } catch (JsonProcessingException e) {
                    log.error(aMarker, "error in mapper value {}", e);
                    throw new HandymanException(e.toString());
                }

                final Request request = new Request.Builder().url(URI).header("appId", appId).header("appKeyId", appKeyId)
                        .post(RequestBody.create(requestString, MediaTypeJSON)).build();
                try (Response response = httpclient.newCall(request).execute()) {
                    String responseBody = Objects.requireNonNull(response.body()).string();
                    if (response.isSuccessful()) {
                        JSONObject responseObject = new JSONObject(responseBody);
                        parentObj.add(
                                DrugMatchOutputTable
                                        .builder()
                                        .originId(Optional.ofNullable(result.getOriginId()).map(String::valueOf).orElse(null))
                                        .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                                        .eocIdentifier(result.eocIdentifier).paperNo(result.paperNo)
                                        .documentId(result.documentId)
                                        .drugJCode(result.getJCode())
                                        .drugName(result.getDrugName())
                                        .actualValue(responseObject.getString("drug_name"))
                                        .status("COMPLETED")
                                        .stage("PAHUB-DRUGNAME")
                                        .message("Drug name master data extracted")
                                        .build());
                    } else {
                        parentObj.add(
                                DrugMatchOutputTable
                                        .builder()
                                        .originId(Optional.ofNullable(result.getOriginId()).map(String::valueOf).orElse(null))
                                        .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                                        .eocIdentifier(result.eocIdentifier).paperNo(result.paperNo)
                                        .documentId(result.documentId)
                                        .drugJCode(result.getJCode())
                                        .drugName(result.getDrugName())
                                        .actualValue("")
                                        .status("COMPLETED")
                                        .stage("PAHUB-DRUGNAME")
                                        .message("Drug name with pahub instance has been failed")
                                        .build());
                        log.error(aMarker, "failed for request {} and response {}", request, response);
                        throw new HandymanException(responseBody);
                    }
                } catch (Throwable t) {
                    parentObj.add(
                            DrugMatchOutputTable
                                    .builder()
                                    .originId(Optional.ofNullable(result.getOriginId()).map(String::valueOf).orElse(null))
                                    .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                                    .eocIdentifier(result.eocIdentifier).paperNo(result.paperNo)
                                    .documentId(result.documentId)
                                    .drugJCode(result.getJCode())
                                    .drugName(result.getDrugName())
                                    .actualValue("")
                                    .status("FAILED")
                                    .stage("PAHUB-DRUGNAME")
                                    .message("Drug name with pahub instance has been failed")
                                    .build());
                    log.error(aMarker, "error in hitting the file for mentioned request {}", request);
                }
            }

            atomicInteger.set(0);
            log.info(aMarker, "coproProcessor consumer process with output entity {}", parentObj);
            return parentObj;
        }
    }

    @Override
    public boolean executeIf() throws Exception {
        return drugMatch.getCondition();
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DrugMatchInputTable implements CoproProcessor.Entity {

        private String originId;
        private String eocIdentifier;
        private Integer paperNo;
        private String documentId;
        private String drugName;
        private String jCode;

        @Override
        public List<Object> getRowData() {
            return null;
        }
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DrugMatchOutputTable implements CoproProcessor.Entity {

        private String originId;
        private String eocIdentifier;
        private Integer paperNo;
        private String documentId;
        private String drugName;
        private String drugJCode;
        private Timestamp createdOn;
        private String actualValue;
        private String status;
        private String stage;
        private String message;

        @Override
        public List<Object> getRowData() {
            return Stream.of(this.originId, this.eocIdentifier, this.paperNo, this.createdOn, this.documentId,
                    this.drugName, this.drugJCode,
                    this.actualValue, this.status, this.stage, this.message
            ).collect(Collectors.toList());
        }
    }


    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DrugNameRequest {

        private String drugName;
        private String jCode;

    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class drugNameResponse {

        private String drugName;
        private String ndc;
        private String jCode;


    }
}


