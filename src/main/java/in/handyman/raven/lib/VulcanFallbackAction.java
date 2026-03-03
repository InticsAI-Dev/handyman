package in.handyman.raven.lib;

import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.action.ActionExecution;
import in.handyman.raven.lambda.action.IActionExecution;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.VulcanFallback;
import in.handyman.raven.lib.model.fallback.processor.VulcanFallbackConsumerProcess;
import in.handyman.raven.lib.model.fallback.processor.VulcanFallbackInputTable;
import in.handyman.raven.lib.model.fallback.processor.VulcanFallbackOutputTable;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

/**
 * Handyman Action to proactively trigger Vulcan Fallback for missing SOR items
 * using CoproProcessor.
 */
@ActionExecution(actionName = "VulcanFallback")
public class VulcanFallbackAction implements IActionExecution {

    private final ActionExecutionAudit action;
    private final Logger log;
    private final VulcanFallback vulcanFallback;
    private final Marker aMarker;

    public VulcanFallbackAction(final ActionExecutionAudit action, final Logger log, final Object vulcanFallback) {
        this.action = action;
        this.log = log;
        this.vulcanFallback = (VulcanFallback) vulcanFallback;
        this.aMarker = MarkerFactory.getMarker("VulcanFallback:" + this.vulcanFallback.getName());
    }

    @Override
    public void execute() throws Exception {
        try {
            log.info(aMarker, "Starting Vulcan Proactive Fallback action for {} using CoproProcessor",
                    vulcanFallback.getName());

            final List<URL> urls = Optional.ofNullable(vulcanFallback.getEndpoint())
                    .map(s -> Arrays.stream(s.split(",")).map(urlItem -> {
                        try {
                            return new URL(urlItem);
                        } catch (MalformedURLException e) {
                            log.error("Error in processing the URL {}", urlItem, e);
                            throw new HandymanException("Error in processing the URL", e, action);
                        }
                    }).collect(Collectors.toList())).orElse(Collections.emptyList());

            int consumerApiCount = Integer
                    .parseInt(action.getContext().getOrDefault("vulcan.fallback.consumer.count", "3"));
            int readBatchSize = Integer
                    .parseInt(action.getContext().getOrDefault("vulcan.fallback.read.batch.size", "10"));
            int writeBatchSize = Integer
                    .parseInt(action.getContext().getOrDefault("vulcan.fallback.write.batch.size", "10"));

            final CoproProcessor<VulcanFallbackInputTable, VulcanFallbackOutputTable> coproProcessor = getTableCoproProcessor(
                    urls, readBatchSize);

            final VulcanFallbackConsumerProcess consumerProcess = new VulcanFallbackConsumerProcess(log, aMarker,
                    action);

            // The output table here is mainly for pipeline-level auditing.
            // Persistence to vqa_transaction is handled by the Vulcan service itself.
            String insertQuery = "SELECT 1";

            coproProcessor.startConsumer(insertQuery, consumerApiCount, writeBatchSize, consumerProcess);

            log.info(aMarker, "Completed Vulcan Proactive Fallback action for {}", vulcanFallback.getName());
        } catch (Exception e) {
            action.getContext().put(vulcanFallback.getName() + ".isSuccessful", "false");
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("Error in execute method for Vulcan Fallback action", handymanException,
                    action);
        }
    }

    private @NotNull CoproProcessor<VulcanFallbackInputTable, VulcanFallbackOutputTable> getTableCoproProcessor(
            List<URL> urls, int readBatchSize) {
        VulcanFallbackInputTable stoppingSeed = new VulcanFallbackInputTable();
        final CoproProcessor<VulcanFallbackInputTable, VulcanFallbackOutputTable> coproProcessor = new CoproProcessor<>(
                new LinkedBlockingQueue<>(),
                VulcanFallbackOutputTable.class,
                VulcanFallbackInputTable.class,
                vulcanFallback.getResourceConn(), log,
                stoppingSeed, urls, action);

        coproProcessor.startProducer(vulcanFallback.getQuerySet(), readBatchSize);
        return coproProcessor;
    }

    @Override
    public boolean executeIf() throws Exception {
        return vulcanFallback.getCondition();
    }
}
