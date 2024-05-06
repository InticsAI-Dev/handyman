package in.handyman.raven.lib;

import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.access.ResourceAccess;
import in.handyman.raven.lambda.action.ActionExecution;
import in.handyman.raven.lambda.action.IActionExecution;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.ZeroShotClassifierPaperFilter;
import in.handyman.raven.lib.model.zeroshotclassifier.ZeroShotClassifierInputTable;
import in.handyman.raven.lib.model.zeroshotclassifier.ZeroShotClassifierOutputTable;
import in.handyman.raven.lib.model.zeroshotclassifier.ZeroShotConsumerProcess;
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
        actionName = "ZeroShotClassifierPaperFilter"
)
public class ZeroShotClassifierPaperFilterAction implements IActionExecution {
    public static final String SCHEMA_NAME = "paper";
    public static final String OUTPUT_TABLE_NAME = "zero_shot_classifier_filtering_result_";
    public static final String INSERT_INTO_COLUMNS = "origin_id,group_id,paper_no,synonym,confidence_score,truth_entity,status,stage,message, created_on, root_pipeline_id,model_name,model_version,tenant_id, request, response, endpoint";
    public static final String INSERT_INTO = "INSERT INTO";
    public static final String INSERT_INTO_VALUES = "?,?,?,?,?,?,?,?,?,now() ,?,?,?,?,?,?,?";
    public final ActionExecutionAudit action;

    public final Logger log;
    public final ZeroShotClassifierPaperFilter zeroShotClassifierPaperFilter;

    public final Marker aMarker;

    public ZeroShotClassifierPaperFilterAction(final ActionExecutionAudit action, final Logger log,
                                               final Object zeroShotClassifierPaperFilter) {
        this.zeroShotClassifierPaperFilter = (ZeroShotClassifierPaperFilter) zeroShotClassifierPaperFilter;
        this.action = action;
        this.log = log;
        this.aMarker = MarkerFactory.getMarker(" ZeroShotClassifierPaperFilter:" + this.zeroShotClassifierPaperFilter.getName());
    }

    @Override
    public void execute() throws Exception {
        try {
           final Jdbi jdbi = ResourceAccess.rdbmsJDBIConn(zeroShotClassifierPaperFilter.getResourceConn());
            jdbi.getConfig(Arguments.class).setUntypedNullArgument(new NullArgument(Types.NULL));
            log.info(aMarker, "Phrase match paper filter Action for {} has been started", zeroShotClassifierPaperFilter.getName());
            final String processId = Optional.ofNullable(zeroShotClassifierPaperFilter.getProcessID()).map(String::valueOf).orElse(null);
            final String insertQuery = INSERT_INTO + " " + SCHEMA_NAME + "." + OUTPUT_TABLE_NAME + processId + "(" + INSERT_INTO_COLUMNS + ") " +
                    " VALUES(" + INSERT_INTO_VALUES + ")";
            final List<URL> urls = Optional.ofNullable(zeroShotClassifierPaperFilter.getEndPoint()).map(s -> Arrays.stream(s.split(",")).map(s1 -> {
                try {
                    return new URL(s1);
                } catch (MalformedURLException e) {
                    log.error("Error in processing the URL {}", s1, e);
                    throw new HandymanException("Error in processing the URL", e, action);
                }
            }).collect(Collectors.toList())).orElse(Collections.emptyList());

            final CoproProcessor<ZeroShotClassifierInputTable, ZeroShotClassifierOutputTable> coproProcessor =
                    new CoproProcessor<>(new LinkedBlockingQueue<>(),
                            ZeroShotClassifierOutputTable.class,
                            ZeroShotClassifierInputTable.class,
                            jdbi, log,
                            new ZeroShotClassifierInputTable(), urls, action);
            coproProcessor.startProducer(zeroShotClassifierPaperFilter.getQuerySet(), Integer.parseInt(zeroShotClassifierPaperFilter.getReadBatchSize()));
            Thread.sleep(1000);
            coproProcessor.startConsumer(insertQuery, Integer.parseInt(zeroShotClassifierPaperFilter.getThreadCount()),
                    Integer.parseInt(zeroShotClassifierPaperFilter.getWriteBatchSize()),
                    new ZeroShotConsumerProcess(log, aMarker, action));
            log.info(aMarker, " Zero shot classifier has been completed {}  ", zeroShotClassifierPaperFilter.getName());

        } catch (Exception e) {
            log.error(aMarker, "Error in zero shot paper filter action", e);
            throw new HandymanException("Error in zero shot paper filter action", e, action);
        }
    }

    @Override
    public boolean executeIf() throws Exception {
        return zeroShotClassifierPaperFilter.getCondition();
    }
}