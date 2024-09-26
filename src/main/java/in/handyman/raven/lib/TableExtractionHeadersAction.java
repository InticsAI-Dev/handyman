package in.handyman.raven.lib;

import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.access.ResourceAccess;
import in.handyman.raven.lambda.action.ActionExecution;
import in.handyman.raven.lambda.action.IActionExecution;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.TableExtractionHeaders;
import in.handyman.raven.lib.model.table.extraction.headers.coproprocessor.TableExtractionConsumerProcess;
import in.handyman.raven.lib.model.table.extraction.headers.coproprocessor.TableExtractionInputTable;
import in.handyman.raven.lib.model.table.extraction.headers.coproprocessor.TableExtractionOutputTable;
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
@ActionExecution(
        actionName = "TableExtractionHeaders"
)
public class TableExtractionHeadersAction implements IActionExecution {
    private final ActionExecutionAudit action;

    private final Logger log;

    private final TableExtractionHeaders tableExtractionHeaders;

    private final Marker aMarker;

    public TableExtractionHeadersAction(final ActionExecutionAudit action, final Logger log,
                                        final Object tableExtractionHeaders) {
        this.tableExtractionHeaders = (TableExtractionHeaders) tableExtractionHeaders;
        this.action = action;
        this.log = log;
        this.aMarker = MarkerFactory.getMarker(" TableExtractionHeaders:" + this.tableExtractionHeaders.getName());
    }

    @Override
    public void execute() throws Exception {

        try {
            log.info(aMarker, "Table Extraction Action has been started {}", tableExtractionHeaders);
            final String readBatchSizeStr = action.getContext().get("read.batch.size");
            final String tableExtractionApiConsumerCountStr = action.getContext().get("table.extraction.consumer.API.count");
            final String writeBatchSizeStr = action.getContext().get("write.batch.size");
            final Integer consumerCountInt = Integer.valueOf(tableExtractionApiConsumerCountStr);
            final Integer writeBatchSizeInt = Integer.valueOf(writeBatchSizeStr);
            final Jdbi jdbi = ResourceAccess.rdbmsJDBIConn(tableExtractionHeaders.getResourceConn());
            final String outputDir = Optional.ofNullable(tableExtractionHeaders.getOutputDir()).map(String::valueOf).orElse(null);

            jdbi.getConfig(Arguments.class).setUntypedNullArgument(new NullArgument(Types.NULL));
            log.info(aMarker, "Table Extraction Action output directory {}", outputDir);
            //5. build insert prepare statement with output table columns
            final String insertQuery = "INSERT INTO " + tableExtractionHeaders.getResultTable() +
                    "(origin_id,group_id,tenant_id,processed_file_path,paper_no, status,stage,message,created_on,process_id,root_pipeline_id,table_response, bboxes, croppedImage,column_headers,truth_entity_name, model_name,truth_entity_id,sor_container_id,channel_id, request, response, endpoint) " +
                    " VALUES(?,?, ?, ?,?, ?,?,?,? ,?,  ?, ? , ?, ?,?,?,  ? ,?,  ?,  ?,?,?,?)";
            log.info(aMarker, "table extraction Insert query {}", insertQuery);

            //3. initiate copro processor and copro urls
            final List<URL> urls = Optional.ofNullable(tableExtractionHeaders.getEndpoint()).map(s -> Arrays.stream(s.split(",")).map(s1 -> {
                try {
                    return new URL(s1);
                } catch (MalformedURLException e) {
                    throw new HandymanException("Error in processing the URL", e, action);
                }
            }).collect(Collectors.toList())).orElse(Collections.emptyList());
            log.info(aMarker, "table extraction copro urls {}", urls);

            final CoproProcessor<TableExtractionInputTable, TableExtractionOutputTable> coproProcessor =
                    new CoproProcessor<>(new LinkedBlockingQueue<>(),
                            TableExtractionOutputTable.class,
                            TableExtractionInputTable.class,
                            jdbi, log,
                            new TableExtractionInputTable(), urls, action);

            log.info(aMarker, "table extraction copro coproProcessor initialization  {}", coproProcessor);

            //4. call the method start producer from coproprocessor

            coproProcessor.startProducer(tableExtractionHeaders.getQuerySet(), Integer.valueOf(readBatchSizeStr));
            log.info(aMarker, "table extraction copro coproProcessor startProducer called read batch size {}", readBatchSizeStr);
            Thread.sleep(1000);
            TableExtractionConsumerProcess tableExtractionConsumerProcess = new TableExtractionConsumerProcess(log, aMarker, outputDir, action);
            coproProcessor.startConsumer(insertQuery, consumerCountInt, writeBatchSizeInt, tableExtractionConsumerProcess);
            log.info(aMarker, "table extraction copro coproProcessor startConsumer called consumer count {} write batch count {} ", consumerCountInt, writeBatchSizeInt);


        } catch (Exception ex) {
            throw new HandymanException("error in execute method for table extraction", ex, action);
        }
    }

    @Override
    public boolean executeIf() throws Exception {
        return tableExtractionHeaders.getCondition();
    }
}
