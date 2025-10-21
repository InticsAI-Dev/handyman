package in.handyman.raven.lib.model.asset.info;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.access.ResourceAccess;
import in.handyman.raven.lambda.action.ActionExecution;
import in.handyman.raven.lambda.action.IActionExecution;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.CoproProcessor;
import in.handyman.raven.lib.model.AssetInfo;
import in.handyman.raven.lib.model.AssetInfoInputTable;
import in.handyman.raven.lib.model.AssetInfoOutputTable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


public class OptimizedAssetInfoAction  {
//    private final ActionExecutionAudit action;
//    private final Logger log;
//    private final AssetInfo assetInfo;
//    private final Marker aMarker;
//
//    // Configuration constants
//    public static final String DUMMY_URL = "http://localhost:10181/copro/preprocess/autorotation";
//    public static final String ASSET_INFO_CONSUMER_API_COUNT = "asset.info.consumer.API.count";
//    public static final String ENABLE_ASYNC_PROCESSING = "enable.async.processing";
//    public static final String ASYNC_TIMEOUT_MINUTES = "async.timeout.minutes";
//
//    public static final String INSERT_INTO = "INSERT INTO ";
//    public static final String INSERT_INTO_VALUES_UPDATED = "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
//    public static final String INSERT_COLUMNS_UPDATED = "file_id, process_id, tenant_id, root_pipeline_id, file_checksum, file_extension, file_name, decoded_file_name, file_path, file_size, encode, width, height, dpi, batch_id";
//
//    public OptimizedAssetInfoAction(final ActionExecutionAudit action, final Logger log, final Object assetInfo) {
//        this.assetInfo = (AssetInfo) assetInfo;
//        this.action = action;
//        this.log = log;
//        this.aMarker = MarkerFactory.getMarker(" AssetInfo:" + this.assetInfo.getName());
//    }
//
//    @Override
//    public void execute() throws Exception {
//        OptimizedAssetInfoConsumerProcess consumerProcess = null;
//        try {
//            Long tenantId = Long.valueOf(action.getContext().get("tenant_id"));
//            log.info(aMarker, "Optimized Asset Info Action for {} has been started", assetInfo.getName());
//
//            // Parse URLs
//            final List<URL> urls = parseUrls();
//
//            // Get configuration
//            Integer readBatchSize = Integer.valueOf(action.getContext().get(READ_BATCH_SIZE));
//            Integer consumerApiCount = Integer.valueOf(action.getContext().get(ASSET_INFO_CONSUMER_API_COUNT));
//            Integer writeBatchSize = Integer.valueOf(action.getContext().get(DB_INSERT_WRITE_BATCH_SIZE));
//            boolean enableAsync = Boolean.parseBoolean(action.getContext().getOrDefault(ENABLE_ASYNC_PROCESSING, "true"));
//            int asyncTimeoutMinutes = Integer.parseInt(action.getContext().getOrDefault(ASYNC_TIMEOUT_MINUTES, "30"));
//
//            log.info(aMarker, "Configuration - ReadBatch: {}, WriteBatch: {}, ConsumerAPI: {}, AsyncEnabled: {}, Timeout: {}min",
//                    readBatchSize, writeBatchSize, consumerApiCount, enableAsync, asyncTimeoutMinutes);
//
//            // Create appropriate consumer process
//            if (enableAsync) {
//                consumerProcess = new OptimizedAssetInfoConsumerProcess(log, aMarker, action, assetInfo, tenantId);
//                log.info(aMarker, "Using optimized async processing");
//            } else {
//                // Fallback to original implementation
//                log.info(aMarker, "Using original synchronous processing");
//                executeOriginal(tenantId, urls, readBatchSize, consumerApiCount, writeBatchSize);
//                return;
//            }
//
//            // Create and configure copro processor
//            final CoproProcessor<AssetInfoInputTable, AssetInfoOutputTable> coproProcessor =
//                    new CoproProcessor<>(
//                            new LinkedBlockingQueue<>(),
//                            AssetInfoOutputTable.class,
//                            AssetInfoInputTable.class,
//                            assetInfo.getResourceConn(),
//                            log,
//                            new AssetInfoInputTable(),
//                            urls,
//                            action
//                    );
//
//            String insertQuery = INSERT_INTO + assetInfo.getAssetTable() + " ( " + INSERT_COLUMNS_UPDATED + " ) " + INSERT_INTO_VALUES_UPDATED;
//
//            // Execute async processing with proper resource management
//            executeAsyncProcessing(coproProcessor, consumerProcess, insertQuery, readBatchSize,
//                    consumerApiCount, writeBatchSize, asyncTimeoutMinutes);
//
//            log.info(aMarker, "Optimized Asset Info Action completed successfully for {}", assetInfo.getName());
//
//        } catch (Exception e) {
//            action.getContext().put(assetInfo.getName().concat(".error"), "true");
//            log.error(aMarker, "Exception occurred in optimized asset info execute", e);
//            throw new HandymanException("Exception occurred in optimized asset info execute", e, action);
//        } finally {
//            // Cleanup resources
//            if (consumerProcess != null) {
//                try {
//                    consumerProcess.shutdown();
//                    log.info(aMarker, "Consumer process shutdown completed");
//                } catch (Exception e) {
//                    log.warn(aMarker, "Error during consumer process shutdown", e);
//                }
//            }
//        }
//    }
//
//    private void executeAsyncProcessing(CoproProcessor<AssetInfoInputTable, AssetInfoOutputTable> coproProcessor,
//                                        OptimizedAssetInfoConsumerProcess consumerProcess,
//                                        String insertQuery, Integer readBatchSize, Integer consumerApiCount,
//                                        Integer writeBatchSize, int timeoutMinutes) throws Exception {
//
//        // Start producer asynchronously
//        CompletableFuture<Void> producerFuture = CompletableFuture.runAsync(() -> {
//            try {
//                log.info(aMarker, "Starting async producer with batch size: {}", readBatchSize);
//                coproProcessor.startProducer(assetInfo.getValues(), readBatchSize);
//                log.info(aMarker, "Producer completed successfully");
//            } catch (Exception e) {
//                log.error(aMarker, "Producer failed", e);
//                throw new RuntimeException("Producer execution failed", e);
//            }
//        });
//
//        // Small delay to ensure producer starts
//        Thread.sleep(1000);
//
//        // Start consumer asynchronously
//        CompletableFuture<Void> consumerFuture = CompletableFuture.runAsync(() -> {
//            try {
//                log.info(aMarker, "Starting async consumer with API count: {}, write batch: {}",
//                        consumerApiCount, writeBatchSize);
//                coproProcessor.startConsumer(insertQuery, consumerApiCount, writeBatchSize, consumerProcess);
//                log.info(aMarker, "Consumer completed successfully");
//            } catch (Exception e) {
//                log.error(aMarker, "Consumer failed", e);
//                throw new RuntimeException("Consumer execution failed", e);
//            }
//        });
//
//        // Wait for both producer and consumer to complete with timeout
//        try {
//            CompletableFuture<Void> allProcessing = CompletableFuture.allOf(producerFuture, consumerFuture);
//            allProcessing.get(timeoutMinutes, TimeUnit.MINUTES);
//            log.info(aMarker, "All async processing completed successfully");
//        } catch (Exception e) {
//            log.error(aMarker, "Async processing failed or timed out after {} minutes", timeoutMinutes, e);
//
//            // Cancel running futures
//            producerFuture.cancel(true);
//            consumerFuture.cancel(true);
//
//            throw new HandymanException("Async processing failed or timed out", e, action);
//        }
//    }
//
//    private void executeOriginal(Long tenantId, List<URL> urls, Integer readBatchSize,
//                                 Integer consumerApiCount, Integer writeBatchSize) throws Exception {
//        // Original synchronous implementation
//        final CoproProcessor<AssetInfoInputTable, AssetInfoOutputTable> coproProcessor =
//                new CoproProcessor<>(
//                        new LinkedBlockingQueue<>(),
//                        AssetInfoOutputTable.class,
//                        AssetInfoInputTable.class,
//                        assetInfo.getResourceConn(),
//                        log,
//                        new AssetInfoInputTable(),
//                        urls,
//                        action
//                );
//
//        log.info(aMarker, "Consumer API count for Asset Info is {}", consumerApiCount);
//        String insertQuery = INSERT_INTO + assetInfo.getAssetTable() + " ( " + INSERT_COLUMNS_UPDATED + " ) " + INSERT_INTO_VALUES_UPDATED;
//
//        coproProcessor.startProducer(assetInfo.getValues(), readBatchSize);
//        Thread.sleep(1000);
//
//        // Use original consumer process
//        OptimizedAssetInfoConsumerProcess originalConsumerProcess = new OptimizedAssetInfoConsumerProcess(log, aMarker, action, assetInfo, tenantId);
//        coproProcessor.startConsumer(insertQuery, consumerApiCount, writeBatchSize, originalConsumerProcess);
//    }
//
//    private List<URL> parseUrls() {
//        return Optional.of(DUMMY_URL)
//                .map(s -> Arrays.stream(s.split(","))
//                        .map(urlString -> {
//                            try {
//                                return new URL(urlString.trim());
//                            } catch (MalformedURLException e) {
//                                log.error(aMarker, "Error parsing URL: {}", urlString, e);
//                                throw new HandymanException("Error parsing URL: " + urlString, e, action);
//                            }
//                        })
//                        .collect(Collectors.toList()))
//                .orElse(Collections.emptyList());
//    }
//
//    @Override
//    public boolean executeIf() throws Exception {
//        return assetInfo.getCondition();
//    }
//
//    @AllArgsConstructor
//    @NoArgsConstructor
//    @Data
//    @Builder
//    @JsonIgnoreProperties(ignoreUnknown = true)
//    public static class SanitarySummary {
//        private int rowCount;
//        private int correctRowCount;
//        private int errorRowCount;
//        private String comments;
//        private Long tenantId;
//        private String batchId;
//    }
}
