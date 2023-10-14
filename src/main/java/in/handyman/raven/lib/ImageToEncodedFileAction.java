package in.handyman.raven.lib;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import in.handyman.raven.lambda.access.ResourceAccess;
import in.handyman.raven.lambda.action.ActionExecution;
import in.handyman.raven.lambda.action.IActionExecution;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.ImageToEncodedFile;
import in.handyman.raven.lib.model.ImageToBaseInputTable;
import in.handyman.raven.lib.model.ImageToBaseOutputTable;
import in.handyman.raven.util.ExceptionUtil;
import okhttp3.*;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.argument.Arguments;
import org.jdbi.v3.core.argument.NullArgument;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Types;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

@ActionExecution(actionName = "ImageToEncodedFile")
public class ImageToEncodedFileAction implements IActionExecution {
    private final ActionExecutionAudit action;
    private final Logger log;
    private final ImageToEncodedFile imageToEncodedFile;

    private final Marker aMarker;

    public ImageToEncodedFileAction(final ActionExecutionAudit action, final Logger log,
                                    final Object imageToEncodedFile) {
        this.imageToEncodedFile = (ImageToEncodedFile) imageToEncodedFile;
        this.action = action;
        this.log = log;

        this.aMarker = MarkerFactory.getMarker("ImageToEncodedFile:" + this.imageToEncodedFile.getName());
    }

    @Override
    public  void execute() throws Exception {
        try {
            log.info(aMarker, "ImageToEncodedFile Action has been started {}", imageToEncodedFile.getName());

            final Jdbi jdbi = ResourceAccess.rdbmsJDBIConn(imageToEncodedFile.getResourceConn());
            jdbi.getConfig(Arguments.class).setUntypedNullArgument(new NullArgument(Types.NULL));

            final String selectQuery = "SELECT originId as 1, paperNo as 1, preprocessedFilePath as /home/christopher.paulraj@zucisystems.com/Pictures/Screenshot from 2023-09-24 07-47-34.png, tenantId as 1";

            final List<URL> urls = ofNullable(action.getContext().get(""))
                    .map(s -> Arrays.stream(s.split(","))
                            .map(s1 -> {
                                try {
                                    return new URL(s1);
                                } catch (MalformedURLException e) {
                                    log.error("Error in processing the URL ", e);
                                    throw new RuntimeException(e);
                                }
                            }).collect(Collectors.toList())).orElse(Collections.emptyList());

            // 5. build insert prepare statement with output table columns
            final String insertQuery = "INSERT INTO " + imageToEncodedFile.getOutputTable() +
                    "(id, process_id, group_id, tenant_id, origin_id, paper_no, processed_file_path, status, stage, message, root_pipeline_id, encode, created_on) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, now())";
            log.info(aMarker, "Insert query: {}", insertQuery);

            final CoproProcessor<ImageToBaseInputTable, ImageToBaseOutputTable> coproProcessor =
                    new CoproProcessor<>(new LinkedBlockingQueue<>(),
                            ImageToBaseOutputTable.class,
                            ImageToBaseInputTable.class,
                            jdbi, log,
                            new ImageToBaseInputTable(), urls, action);

            // 4. call the method start producer from coproprocessor
            coproProcessor.startProducer(imageToEncodedFile.getQuerySet(), Integer.valueOf(action.getContext().get("read.batch.size")));
            Thread.sleep(1000);

            try {
                // Process image paths and convert to Base64
                List<String> imagePaths = Arrays.asList(
                        "/path/to/image1.jpg",
                        "/path/to/image2.png",
                        "/path/to/image3.bmp"
                        // Add more image file paths as needed
                );
                List<ImageToBaseOutputTable> outputTables = new ArrayList<>();

                for (String imagePath : imagePaths) {
                    try {
                        String base64Image = Base64.getEncoder().encodeToString(Files.readAllBytes(Paths.get(imagePath)));
                        coproProcessor.startConsumer(insertQuery, Integer.valueOf(action.getContext().get("consumer.API.count")),
                                Integer.valueOf(action.getContext().get("write.batch.size")), new imageToEncodedFileConsumerProcess(log, aMarker, base64Image, action));

                        log.info(aMarker, "ImageToEncodedFile Action has been completed {}", imageToEncodedFile.getName());
                    } catch (IOException e) {
                        log.error("Error converting image to Base64: " + e.getMessage());
                    }
                }
            } catch (Throwable t) {
                // Handle exceptions
            }
        } catch (Throwable t) {
            action.getContext().put(imageToEncodedFile.getName() + ".isSuccessful", "false");
            log.error(aMarker, "Error in ImageToEncodedFile execute method", t);
        }
    }

    public static class imageToEncodedFileConsumerProcess implements CoproProcessor.ConsumerProcess<ImageToBaseInputTable, ImageToBaseOutputTable> {
        private final Logger log;
        private final Marker aMarker;
        private final ObjectMapper mapper = new ObjectMapper();
        private static final MediaType MediaTypeJSON = MediaType.parse("application/json; charset=utf-8");
        private final String outputDir;
        public final ActionExecutionAudit action;
        final OkHttpClient httpclient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.MINUTES)
                .writeTimeout(10, TimeUnit.MINUTES)
                .readTimeout(10, TimeUnit.MINUTES)
                .build();



        public imageToEncodedFileConsumerProcess(Logger log, Marker aMarker, String outputDir, ActionExecutionAudit action) {
            this.log = log;
            this.aMarker = aMarker;
            this.outputDir = outputDir;
            this.action = action;
        }




        @Override
        public List<ImageToBaseOutputTable> process(URL endpoint, ImageToBaseInputTable entity) throws JsonProcessingException {
            List<ImageToBaseOutputTable> parentObj = new ArrayList<>();
            final ObjectNode objectNode = mapper.createObjectNode();
            objectNode.put("outputDir", outputDir);
            log.info(aMarker, "Input variables id : {}", action.getActionId());
            Request request = new Request.Builder().url(endpoint)
                    .post(RequestBody.create(objectNode.toString(), MediaTypeJSON)).build();
            log.debug(aMarker, "Request has been built with the parameters \n URI : {} ");
            log.debug(aMarker, "The Request Details: {}", request);
            try (Response response = httpclient.newCall(request).execute()) {
                String responseBody = Objects.requireNonNull(response.body()).string();
                if (response.isSuccessful()) {
                    JSONObject parentResponseObject = new JSONObject(responseBody);
                    parentObj.add(
                            ImageToBaseOutputTable.builder()
                                    .processId(1L)
                                    .groupId(entity.getGroupId())
                                    .tenantId(entity.getTenantId())
                                    .originId(ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null))
                                    .paperNo(entity.getPaperNo())
                                    .processedFilePath(ofNullable(parentResponseObject.get("processedFilePath")).map(String::valueOf).orElse(null))
                                    .status("COMPLETED")
                                    .stage("ImageToBase64")
                                    .message("ImageToBase64 finished")
                                    .build()
                    );
                } else {
                    parentObj.add(
                            ImageToBaseOutputTable.builder()
                                    .originId(ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null))
                                    .groupId(entity.getGroupId())
                                    .status("FAILED")
                                    .stage("ImageToBase64")
                                    .message(response.message())
                                    .processId(1L)
                                    .rootPipelineId(1L)
                                    .paperNo(1L)
                                    .build()
                    );
                    log.info(aMarker, "The Exception occurred in blank page remover");
                }
            } catch (Exception e) {
                parentObj.add(
                        ImageToBaseOutputTable.builder()
                                .originId(ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null))
                                .groupId(entity.getGroupId())
                                .status("FAILED")
                                .stage("ImageToBase64")
                                .message(ExceptionUtil.toString(e))
                                .build()
                );
                log.error(aMarker, "The Exception occurred in blank page remover", e);

                // TODO: Insert query for error queue

            }
            return parentObj;
        }
    }

    @Override
    public boolean executeIf() throws Exception {
        return imageToEncodedFile.getCondition();
    }
}
