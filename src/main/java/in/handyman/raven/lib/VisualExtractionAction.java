package in.handyman.raven.lib;

import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.access.ResourceAccess;
import in.handyman.raven.lambda.action.ActionExecution;
import in.handyman.raven.lambda.action.IActionExecution;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.VisualExtraction;
import in.handyman.raven.lib.model.visualextraction.coproprocessor.VisualExtractionConsumerProcess;
import in.handyman.raven.lib.model.visualextraction.coproprocessor.VisualExtractionInputTable;
import in.handyman.raven.lib.model.visualextraction.coproprocessor.VisualExtractionOutputTable;
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

import static in.handyman.raven.core.enums.DatabaseConstants.DB_INSERT_WRITE_BATCH_SIZE;
import static in.handyman.raven.core.enums.DatabaseConstants.DB_SELECT_READ_BATCH_SIZE;

@ActionExecution(actionName = "VisualExtraction")
public class VisualExtractionAction implements IActionExecution {
    private final ActionExecutionAudit action;
    private final Logger log;
    private final VisualExtraction visualExtraction;
    private final Marker aMarker;

    public VisualExtractionAction(final ActionExecutionAudit action, final Logger log,
            final Object visualExtraction) {
        this.visualExtraction = (VisualExtraction) visualExtraction;
        this.action = action;
        this.log = log;
        this.aMarker = MarkerFactory.getMarker(" VisualExtraction:" + this.visualExtraction.getName());
    }

    @Override
    public void execute() {
        try {
            log.info(aMarker, "Visual Extraction Action has been started {}", visualExtraction);

            final Jdbi jdbi = ResourceAccess.rdbmsJDBIConn(visualExtraction.getResourceConn());
            jdbi.getConfig(Arguments.class).setUntypedNullArgument(new NullArgument(Types.NULL));

            final String insertQuery = "INSERT INTO " + visualExtraction.getResultTable() +
                    "(origin_id, group_id, root_pipeline_id, tenant_id, paper_no, document_type, stage, status, model_name, "
                    + "error_message, duration_time, batch_id, process_id, response, request, endpoint) " +
                    " VALUES(?,nullif(cast(? as text), '')::bigint,?,?,?,?,?,?,?,?,?,?,?,?,cast(? as json),?)";

            log.info(aMarker, "Visual extraction Insert query {}", insertQuery);

            final List<URL> urls = Optional.ofNullable(visualExtraction.getEndpoint())
                    .map(s -> Arrays.stream(s.split(",")).map(s1 -> {
                        try {
                            return new URL(s1);
                        } catch (MalformedURLException e) {
                            log.error("Error in processing the URL ", e);
                            throw new HandymanException("Error in processing the URL", e, action);
                        }
                    }).collect(Collectors.toList())).orElse(Collections.emptyList());

            log.info(aMarker, "visual extraction copro urls {}", urls);

            final CoproProcessor<VisualExtractionInputTable, VisualExtractionOutputTable> coproProcessor = new CoproProcessor<>(
                    new LinkedBlockingQueue<>(),
                    VisualExtractionOutputTable.class,
                    VisualExtractionInputTable.class,
                    visualExtraction.getResourceConn(), log,
                    new VisualExtractionInputTable(), urls, action);

            log.info(aMarker, "visual extraction copro coproProcessor initialization  {}", coproProcessor);

            coproProcessor.startProducer(visualExtraction.getQuerySet(),
                    Integer.valueOf(action.getContext().get(DB_SELECT_READ_BATCH_SIZE)));
            log.info(aMarker, "visual extraction copro coproProcessor startProducer called read batch size {}",
                    action.getContext().get(DB_SELECT_READ_BATCH_SIZE));

            Thread.sleep(1000);

            coproProcessor.startConsumer(insertQuery,
                    Integer.valueOf(action.getContext().get("visual.extraction.consumer.API.count")),
                    Integer.valueOf(action.getContext().get(DB_INSERT_WRITE_BATCH_SIZE)),
                    new VisualExtractionConsumerProcess(log, aMarker, action));

            log.info(aMarker,
                    "visual extraction copro coproProcessor startConsumer called");

        } catch (Exception ex) {
            log.error(aMarker, "error in execute method for visual extraction  ", ex);
            throw new HandymanException("error in execute method for visual extraction", ex, action);
        }
    }

    @Override
    public boolean executeIf() throws Exception {
        return visualExtraction.getCondition();
    }
}
