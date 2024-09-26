package in.handyman.raven.lib;

import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.access.ResourceAccess;
import in.handyman.raven.lambda.action.ActionExecution;
import in.handyman.raven.lambda.action.IActionExecution;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.IntegratedNoiseModelApi;
import in.handyman.raven.lib.model.noiseModel.NoiseModelConsumerProcess;
import in.handyman.raven.lib.model.noiseModel.NoiseModelInputEntity;
import in.handyman.raven.lib.model.noiseModel.NoiseModelOutputEntity;
import in.handyman.raven.util.ExceptionUtil;
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
        actionName = "IntegratedNoiseModelApi"
)
public class IntegratedNoiseModelApiAction implements IActionExecution {
    public static final String INSERT_COLUMN_NAMES = "origin_id, paper_no, process_id, group_id, tenant_id, input_file_path, " +
            "consolidated_confidence_score, consolidated_class, noise_models_result, hw_noise_detection_output, " +
            "check_noise_detection_output, checkbox_mark_detection_output, speckle_noise_detection_output," +
            " created_on, root_pipeline_id,status,stage,message,model_name,model_version,batch_id, last_updated_on";
    public static final String PREPARED_STATEMENT_PLACEHOLDERS = "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?";
    public static final String READ_BATCH_SIZE = "read.batch.size";
    public static final String NOISE_CONSUMER_API_COUNT = "noise.consumer.API.count";
    public static final String WRITE_BATCH_SIZE = "write.batch.size";
    private final ActionExecutionAudit action;

    private final Logger log;

    private final IntegratedNoiseModelApi integratedNoiseModelApi;
    private static String httpClientTimeout = new String();
    private final Marker aMarker;

    public IntegratedNoiseModelApiAction(final ActionExecutionAudit action, final Logger log,
                                         final Object integratedNoiseModelApi) {
        this.integratedNoiseModelApi = (IntegratedNoiseModelApi) integratedNoiseModelApi;
        this.action = action;
        this.log = log;
        this.aMarker = MarkerFactory.getMarker(" IntegratedNoiseModelApi:" + this.integratedNoiseModelApi.getName());
    }

    @Override
    public void execute() throws Exception {
        try {
            log.info(aMarker, "Integrated noise model Action for {} with group by eoc-id has started", integratedNoiseModelApi.getName());
            final Jdbi jdbi = ResourceAccess.rdbmsJDBIConn(integratedNoiseModelApi.getResourceConn());
            Integer consumerCount = Integer.valueOf(action.getContext().get(NOISE_CONSUMER_API_COUNT));
            Integer writeBatchSize = Integer.valueOf(action.getContext().get(WRITE_BATCH_SIZE));
            Integer readBatchSize = Integer.valueOf(action.getContext().get(READ_BATCH_SIZE));

            jdbi.getConfig(Arguments.class).setUntypedNullArgument(new NullArgument(Types.NULL)); //for handling null values
            //3. initiate Copro processor and Copro urls change the url to integrated noise model
            final List<URL> urls = Optional.ofNullable(integratedNoiseModelApi.getEndPoint()).map(s -> Arrays.stream(s.split(",")).map(s1 -> {
                try {
                    return new URL(s1);
                } catch (MalformedURLException e) {
                    log.error("Error in processing the URL {}", s1, e);
                    throw new HandymanException("Error in processing the URL", e, action);
                }
            }).collect(Collectors.toList())).orElse(Collections.emptyList());
            log.info("Urls for the Integrated noise model :  {}", urls);
            //5. build insert prepare statement with output table columns
//
            final String insertQuery = "INSERT INTO " + integratedNoiseModelApi.getOutputTable() +
                    " ( " + INSERT_COLUMN_NAMES + ")" +
                    "VALUES(" + PREPARED_STATEMENT_PLACEHOLDERS + ")";


            final CoproProcessor<NoiseModelInputEntity, NoiseModelOutputEntity> coproProcessor =
                    new CoproProcessor<>(new LinkedBlockingQueue<>(),
                            NoiseModelOutputEntity.class,
                            NoiseModelInputEntity.class,
                            jdbi, log,
                            new NoiseModelInputEntity(), urls, action);

            coproProcessor.startProducer(integratedNoiseModelApi.getQuerySet(), readBatchSize);
            log.info("start producer method from copro processor ");
            Thread.sleep(1000);

            //8. call the method start consumer from coproprocessor

            coproProcessor.startConsumer(insertQuery, consumerCount, writeBatchSize, new NoiseModelConsumerProcess(log, aMarker, action));
            log.info("start consumer method from copro processor ");


        } catch (Exception e) {
            log.error("Error in the Integrated noise model action {}", ExceptionUtil.toString(e));
            throw new HandymanException("Integrated noise model action failed ", e, action);


        }
    }

    @Override
    public boolean executeIf() throws Exception {
        return integratedNoiseModelApi.getCondition();
    }
}
