package in.handyman.raven.lib;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.access.ResourceAccess;
import in.handyman.raven.lambda.action.ActionExecution;
import in.handyman.raven.lambda.action.IActionExecution;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.DataExtraction;
import in.handyman.raven.lib.model.textextraction.DataExtractionConsumerProcess;
import in.handyman.raven.lib.model.textextraction.DataExtractionInputTable;
import in.handyman.raven.lib.model.textextraction.DataExtractionOutputTable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import okhttp3.MediaType;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.argument.Arguments;
import org.jdbi.v3.core.argument.NullArgument;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

/**
 * Auto Generated By Raven
 */
@ActionExecution(actionName = "DataExtraction")
public class DataExtractionAction implements IActionExecution {
    public static final String OKHTTP_CLIENT_TIMEOUT = "okhttp.client.timeout";
    public static final String INSERT_COLUMNS = "origin_id,group_id,tenant_id,template_id,process_id, file_path, extracted_text,paper_no,file_name, status,stage,message,is_blank_page, created_on ,root_pipeline_id,template_name,model_name,model_version,request,response,endpoint";
    public static final String INSERT_INTO = "INSERT INTO ";
    public static final String INSERT_INTO_VALUES = "VALUES(?,? ,?,?,? ,?,?,?,?, ?,?,?,?,? ,?, ?,?,?,?,?,?)";
  public static final String READ_BATCH_SIZE = "read.batch.size";
  public static final String TEXT_EXTRACTION_CONSUMER_API_COUNT = "text.extraction.consumer.API.count";
  public static final String WRITE_BATCH_SIZE = "write.batch.size";
  private final ActionExecutionAudit action;

    private final Logger log;

    private final DataExtraction dataExtraction;
    private final Marker aMarker;

    public DataExtractionAction(final ActionExecutionAudit action, final Logger log, final Object dataExtraction) {
        this.dataExtraction = (DataExtraction) dataExtraction;
        this.action = action;
        this.log = log;
        this.aMarker = MarkerFactory.getMarker(" DataExtraction:" + this.dataExtraction.getName());
    }

    @Override
    public void execute() throws Exception {
        try {
            final Jdbi jdbi = ResourceAccess.rdbmsJDBIConn(dataExtraction.getResourceConn());

            jdbi.getConfig(Arguments.class).setUntypedNullArgument(new NullArgument(Types.NULL));
            log.info(aMarker, "Data Extraction Action for {} has been started", dataExtraction.getName());

            String outputTableName = dataExtraction.getResultTable();
            final String insertQuery = INSERT_INTO + outputTableName + " ( " + INSERT_COLUMNS + " ) " + INSERT_INTO_VALUES;
            final List<URL> urls = Optional.ofNullable(dataExtraction.getEndPoint()).map(s -> Arrays.stream(s.split(",")).map(s1 -> {
                try {
                    return new URL(s1);
                } catch (MalformedURLException e) {
                    log.error("Error in processing the URL ", e);
                    throw new HandymanException("Error in processing the URL", e, action);
                }
            }).collect(Collectors.toList())).orElse(Collections.emptyList());

            final CoproProcessor<DataExtractionInputTable, DataExtractionOutputTable> coproProcessor = new CoproProcessor<>(new LinkedBlockingQueue<>(), DataExtractionOutputTable.class, DataExtractionInputTable.class, jdbi, log, new DataExtractionInputTable(), urls, action);

            Integer readBatchSize = Integer.valueOf(action.getContext().get(READ_BATCH_SIZE));
            Integer consumerApiCount = Integer.valueOf(action.getContext().get(TEXT_EXTRACTION_CONSUMER_API_COUNT));
            Integer writeBatchSize = Integer.valueOf(action.getContext().get(WRITE_BATCH_SIZE));
            DataExtractionConsumerProcess dataExtractionConsumerProcess = new DataExtractionConsumerProcess(log, aMarker, action);

            coproProcessor.startProducer(dataExtraction.getQuerySet(), readBatchSize);
            Thread.sleep(1000);
            coproProcessor.startConsumer(insertQuery, consumerApiCount, writeBatchSize, dataExtractionConsumerProcess);
            log.info(aMarker, " Data Extraction Action has been completed {}  ", dataExtraction.getName());
        } catch (Exception e) {
            action.getContext().put(dataExtraction.getName() + ".isSuccessful", "false");
            log.error(aMarker, "error in execute method in data extraction", e);
            throw new HandymanException("error in execute method in data extraction", e, action);
        }

    }


    @Override
    public boolean executeIf() throws Exception {
        return dataExtraction.getCondition();
    }

    @Data
    @AllArgsConstructor
    @Builder
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AssetAttributionResponse {
        private String pageContent;
    }

}