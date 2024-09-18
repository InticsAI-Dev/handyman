package in.handyman.raven.lib;

import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.access.ResourceAccess;
import in.handyman.raven.lambda.action.ActionExecution;
import in.handyman.raven.lambda.action.IActionExecution;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.PhraseMatchPaperFilter;
import in.handyman.raven.lib.model.pharsematch.*;
import in.handyman.raven.util.ExceptionUtil;
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
import java.util.stream.Collectors;

/**
 * Auto Generated By Raven
 */
@ActionExecution(
        actionName = "PhraseMatchPaperFilter"
)
public class PhraseMatchPaperFilterAction implements IActionExecution {
    public static final String SCHEMA_NAME = "paper";
    public static final String OUTPUT_TABLE_NAME = "phrase_match_filtering_result_";
    public static final String INSERT_INTO_COLUMNS = "origin_id,group_id,paper_no,truth_entity, synonym, is_key_present,status,stage,message, created_on,root_pipeline_id,model_name,model_version,tenant_id,batch_id, last_updated_on";
    public static final String INSERT_INTO_VALUES = "?,?,?,?,?,?,?,?,?,?, ?, ?,?,?,?, ?";
    private final ActionExecutionAudit action;
    private final Logger log;
    private final PhraseMatchPaperFilter phraseMatchPaperFilter;
    private final Marker aMarker;

    public PhraseMatchPaperFilterAction(final ActionExecutionAudit action, final Logger log,
                                        final Object phraseMatchPaperFilter) {
        this.phraseMatchPaperFilter = (PhraseMatchPaperFilter) phraseMatchPaperFilter;
        this.action = action;
        this.log = log;
        this.aMarker = MarkerFactory.getMarker(" PhraseMatchPaperFilter:" + this.phraseMatchPaperFilter.getName());
    }

    @Override
    public void execute() throws Exception {
        try {
            String endPoint = phraseMatchPaperFilter.getEndPoint();

            final Jdbi jdbi = ResourceAccess.rdbmsJDBIConn(phraseMatchPaperFilter.getResourceConn());
            jdbi.getConfig(Arguments.class).setUntypedNullArgument(new NullArgument(Types.NULL));
            log.info(aMarker, "<-------Phrase match paper filter Action for {} has been started------->", phraseMatchPaperFilter.getName());
            final String processId = Optional.ofNullable(phraseMatchPaperFilter.getProcessID()).map(String::valueOf).orElse(null);
            final String insertQuery = "INSERT INTO " + SCHEMA_NAME + "." + OUTPUT_TABLE_NAME + processId + "(" + INSERT_INTO_COLUMNS + ") " +
                    " VALUES(" + INSERT_INTO_VALUES + ")";

            final List<URL> urls = Optional.ofNullable(endPoint).map(s -> Arrays.stream(s.split(",")).map(url -> {
                try {
                    return new URL(url);
                } catch (MalformedURLException e) {
                    log.error("Error in processing the URL {} ", url, e);
                    throw new HandymanException("Error in processing the URL", e, action);
                }
            }).collect(Collectors.toList())).orElse(Collections.emptyList());

            final CoproProcessor<PhraseMatchInputTable, PhraseMatchOutputTable> coproProcessor =
                    new CoproProcessor<>(new LinkedBlockingQueue<>(),
                            PhraseMatchOutputTable.class,
                            PhraseMatchInputTable.class,
                            jdbi, log,
                            new PhraseMatchInputTable(), urls, action);
            coproProcessor.startProducer(phraseMatchPaperFilter.getQuerySet(), Integer.parseInt(phraseMatchPaperFilter.getReadBatchSize()));
            Thread.sleep(1000);
            coproProcessor.startConsumer(insertQuery, Integer.parseInt(phraseMatchPaperFilter.getThreadCount()),
                    Integer.parseInt(phraseMatchPaperFilter.getWriteBatchSize()),
                    new PhraseMatchConsumerProcess(log, aMarker, action));
            log.info(aMarker, " Zero shot classifier has been completed {}  ", phraseMatchPaperFilter.getName());

        } catch (Exception e) {
            log.error(aMarker, "Error in phrase match with exception {}", ExceptionUtil.toString(e));
            throw new HandymanException("Error in phrase match action", e, action);
        }

    }

    @NotNull
    private CoproProcessor<PhraseMatchInputTable, PhraseMatchOutputTable> getPhraseMatchOutputTableCoproProcessor(Jdbi jdbi, List<URL> urls) {
        final CoproProcessor<PhraseMatchInputTable, PhraseMatchOutputTable> coproProcessor =
                new CoproProcessor<>(new LinkedBlockingQueue<>(),
                        PhraseMatchOutputTable.class,
                        PhraseMatchInputTable.class,
                        jdbi, log,
                        new PhraseMatchInputTable(), urls, action);
        coproProcessor.startProducer(phraseMatchPaperFilter.getQuerySet(), Integer.parseInt(phraseMatchPaperFilter.getReadBatchSize()));
        return coproProcessor;
    }

    @Override
    public boolean executeIf() throws Exception {
        return phraseMatchPaperFilter.getCondition();
    }


}
