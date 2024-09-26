package in.handyman.raven.lib;


import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.access.ResourceAccess;
import in.handyman.raven.lambda.action.ActionExecution;
import in.handyman.raven.lambda.action.IActionExecution;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.FileMergerPdf;
import in.handyman.raven.lib.model.filemergerpdf.FileMergerPdfConsumerProcess;
import in.handyman.raven.lib.model.filemergerpdf.FileMergerpdfInputEntity;
import in.handyman.raven.lib.model.filemergerpdf.FileMergerpdfOutputEntity;
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
        actionName = "FileMergerPdf"

)


public class FileMergerPdfAction implements IActionExecution {
    public static final String FILE_MERGER_CONSUMER_API_COUNT = "file.merger.consumer.API.count";
    private final ActionExecutionAudit action;
    private final Logger log;
    private final FileMergerPdf fileMergerPdf;
    private final Marker aMarker;

    public FileMergerPdfAction(final ActionExecutionAudit action, final Logger log,
                               final Object fileMergerPdf) {
        this.fileMergerPdf = (FileMergerPdf) fileMergerPdf;
        this.action = action;
        this.log = log;
        this.aMarker = MarkerFactory.getMarker(" FileMerger:" + this.fileMergerPdf.getName());
    }


    @Override
    public void execute() {
        try {
            log.info(aMarker, "file mergerAction has been started {}", fileMergerPdf);

            final Jdbi jdbi = ResourceAccess.rdbmsJDBIConn(fileMergerPdf.getResourceConn());
            jdbi.getConfig(Arguments.class).setUntypedNullArgument(new NullArgument(Types.NULL));
            Integer consumerApiCount = Integer.valueOf(action.getContext().get(FILE_MERGER_CONSUMER_API_COUNT));
            Integer writeBatchSize1 = Integer.valueOf(action.getContext().get("write.batch.size"));
            Integer readBatchSize = Integer.valueOf(action.getContext().get("read.batch.size"));
            FileMergerPdfConsumerProcess fileMergerPdfConsumerProcess = new FileMergerPdfConsumerProcess(log, aMarker, action, fileMergerPdf);

            final String outputDir = fileMergerPdf.getOutputDir();
            log.info(aMarker, "file mergerAction output directory {}", outputDir);
            //5. build insert prepare statement with output table columns
            final String insertQuery = "INSERT INTO " + fileMergerPdf.getOutputTable() + "(origin_id,tenant_id,group_id,processed_file_path,created_on,last_updated_on,root_pipeline_id,status,stage,message,model_name,model_version,file_name,process_id, width, height, dpi, request, response, endpoint) " +
                    " VALUES(?,?,?,?,?::timestamp,?::timestamp,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

            log.info(aMarker, "file mergerInsert query {}", insertQuery);

            //3. initiate copro processor and copro urls
            final List<URL> urls = Optional.ofNullable(fileMergerPdf.getEndPoint()).map(s -> Arrays.stream(s.split(",")).map(s1 -> {
                try {
                    return new URL(s1);
                } catch (MalformedURLException e) {
                    log.error("Error in processing the URL ", e);
                    throw new HandymanException("Error in processing the URL", e, action);
                }
            }).collect(Collectors.toList())).orElse(Collections.emptyList());
            log.info(aMarker, "file mergercopro urls {}", urls);

            final CoproProcessor<FileMergerpdfInputEntity, FileMergerpdfOutputEntity> coproProcessor =
                    new CoproProcessor<>(new LinkedBlockingQueue<>(),
                            FileMergerpdfOutputEntity.class,
                            FileMergerpdfInputEntity.class,
                            jdbi, log,
                            new FileMergerpdfInputEntity(), urls, action);

            log.info(aMarker, "file mergercopro coproProcessor initialization  {}", coproProcessor);

            //4. call the method start producer from coproprocessor

            coproProcessor.startProducer(fileMergerPdf.getQuerySet(), readBatchSize);
            log.info(aMarker, "file mergercopro coproProcessor startProducer called read batch size {}", readBatchSize);
            Thread.sleep(1000);
            coproProcessor.startConsumer(insertQuery, consumerApiCount, writeBatchSize1, fileMergerPdfConsumerProcess);
            log.info(aMarker, "file mergercopro coproProcessor startConsumer called consumer count {} write batch count {} ", consumerApiCount, writeBatchSize1);

        } catch (Exception ex) {
            log.error(aMarker, "error in execute method for file merger ", ex);
            throw new HandymanException("error in execute method for file merger", ex, action);
        }

    }

    @Override
    public boolean executeIf() throws Exception {
        return fileMergerPdf.getCondition();
    }

}