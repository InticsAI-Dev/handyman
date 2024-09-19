package in.handyman.raven.lib;

import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.access.ResourceAccess;
import in.handyman.raven.lambda.action.ActionExecution;
import in.handyman.raven.lambda.action.IActionExecution;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.PaperItemizer;
import in.handyman.raven.lib.model.paperitemizer.PaperItemizerConsumerProcess;
import in.handyman.raven.lib.model.paperitemizer.PaperItemizerInputTable;
import in.handyman.raven.lib.model.paperitemizer.PaperItemizerOutputTable;
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
        actionName = "PaperItemizer"
)
public class PaperItemizerAction implements IActionExecution {
    public static final String READ_BATCH_SIZE = "read.batch.size";
    public static final String PAPER_ITEMIZER_CONSUMER_API_COUNT = "paper.itemizer.consumer.API.count";
    public static final String WRITE_BATCH_SIZE = "write.batch.size";
    public static final String INSERT_COLUMNS = "origin_id,group_id,tenant_id,template_id,processed_file_path,paper_no, status,stage,message,created_on,process_id,root_pipeline_id,model_name,model_version,batch_id, last_updated_on";
    private final ActionExecutionAudit action;

    private final Logger log;

    private final PaperItemizer paperItemizer;

    private final Marker aMarker;

    public PaperItemizerAction(final ActionExecutionAudit action, final Logger log,
                               final Object paperItemizer) {
        this.paperItemizer = (PaperItemizer) paperItemizer;
        this.action = action;
        this.log = log;
        this.aMarker = MarkerFactory.getMarker(" PaperItemizer:" + this.paperItemizer.getName());
    }

    @Override
    public void execute() {
        try {
            log.info(aMarker, "paper itemizer Action has been started {}", paperItemizer);

            final Jdbi jdbi = ResourceAccess.rdbmsJDBIConn(paperItemizer.getResourceConn());
            jdbi.getConfig(Arguments.class).setUntypedNullArgument(new NullArgument(Types.NULL));
            final String outputDir = Optional.ofNullable(paperItemizer.getOutputDir()).map(String::valueOf).orElse(null);
            log.info(aMarker, "paper itemizer Action output directory {}", outputDir);
            //5. build insert prepare statement with output table columns
            final String insertQuery = "INSERT INTO " + paperItemizer.getResultTable() +
                    "(" + INSERT_COLUMNS + ") " +
                    " VALUES(?,?, ?,?, ?,?, ?,?,?,? ,?,  ?,?,?,?, ?)";
            log.info(aMarker, "paper itemizer Insert query {}", insertQuery);

            //3. initiate copro processor and copro urls
            final List<URL> urls = Optional.ofNullable(action.getContext().get("copro.paper-itemizer.url")).map(s -> Arrays.stream(s.split(",")).map(s1 -> {
                try {
                            return new URL(s1);
                        } catch (MalformedURLException e) {
                            log.error("Error in processing the URL ", e);
                            throw new HandymanException("Error in processing the URL", e, action);
                        }
                    }).collect(Collectors.toList())).orElse(Collections.emptyList());
            log.info(aMarker, "paper itemizer copro urls {}", urls);

            final CoproProcessor<PaperItemizerInputTable, PaperItemizerOutputTable> coproProcessor =
                    new CoproProcessor<>(new LinkedBlockingQueue<>(),
                            PaperItemizerOutputTable.class,
                            PaperItemizerInputTable.class,
                            jdbi, log,
                            new PaperItemizerInputTable(), urls, action);

            log.info(aMarker, "paper itemizer copro coproProcessor initialization  {}", coproProcessor);

            //4. call the method start producer from coproprocessor
            Integer readBatchSize = Integer.valueOf(action.getContext().get(READ_BATCH_SIZE));
            Integer consumerApiCount = Integer.valueOf(action.getContext().get(PAPER_ITEMIZER_CONSUMER_API_COUNT));
            Integer writeBatchSize = Integer.valueOf(action.getContext().get(WRITE_BATCH_SIZE));

            coproProcessor.startProducer(paperItemizer.getQuerySet(), readBatchSize);
            log.info(aMarker, "paper itemizer copro coproProcessor startProducer called read batch size {}", readBatchSize);
            Thread.sleep(1000);
            coproProcessor.startConsumer(insertQuery, consumerApiCount, writeBatchSize, new PaperItemizerConsumerProcess(log, aMarker, outputDir, action));
            log.info(aMarker, "paper itemizer copro coproProcessor startConsumer called consumer count {} write batch count {} ", consumerApiCount, writeBatchSize);

        } catch (Exception ex) {
            log.error(aMarker, "error in execute method for paper itemizer  ", ex);
            throw new HandymanException("error in execute method for paper itemizer", ex, action);
        }
    }
    @Override
    public boolean executeIf() throws Exception {
        return paperItemizer.getCondition();
    }

}

