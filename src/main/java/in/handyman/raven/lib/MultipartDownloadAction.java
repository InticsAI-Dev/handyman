package in.handyman.raven.lib;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.access.ResourceAccess;
import in.handyman.raven.lambda.action.ActionExecution;
import in.handyman.raven.lambda.action.IActionExecution;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.MultipartDownload;
import in.handyman.raven.util.ExceptionUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.argument.Arguments;
import org.jdbi.v3.core.argument.NullArgument;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


/**
 * Auto Generated By Raven
 */
@ActionExecution(
        actionName = "MultipartDownload"
)
public class MultipartDownloadAction implements IActionExecution {
    private final ActionExecutionAudit action;

    private final Logger log;

    private final MultipartDownload multipartDownload;

    private final Marker aMarker;

    public MultipartDownloadAction(final ActionExecutionAudit action, final Logger log,
                                   final Object multipartDownload) {
        this.multipartDownload = (MultipartDownload) multipartDownload;
        this.action = action;
        this.log = log;
        this.aMarker = MarkerFactory.getMarker(" MultipartDownload:" + this.multipartDownload.getName());
    }

    @Override
    public void execute() throws Exception {
        try {
            final Jdbi jdbi = ResourceAccess.rdbmsJDBIConn(multipartDownload.getResourceConn());
            jdbi.getConfig(Arguments.class).setUntypedNullArgument(new NullArgument(Types.NULL));
            log.info(aMarker, "Download OctetStream File Action for {} has been started", multipartDownload.getName());
            final String insertQuery = "";
            String endPoint = multipartDownload.getEndPoint();
            final List<URL> urls = Optional.ofNullable(endPoint).map(s -> Arrays.stream(s.split(",")).map(s1 -> {
                try {
                    return new URL(s1);
                } catch (MalformedURLException e) {
                    log.error("Error in processing the URL ", e);
                    throw new RuntimeException(e);
                }
            }).collect(Collectors.toList())).orElse(Collections.emptyList());

            final CoproProcessor<MultipartDownloadAction.DownloadOctetStreamFileInputTable, MultipartDownloadAction.MultipartDownloadOutputTable> coproProcessor =
                    new CoproProcessor<>(new LinkedBlockingQueue<>(),
                            MultipartDownloadAction.MultipartDownloadOutputTable.class,
                            MultipartDownloadAction.DownloadOctetStreamFileInputTable.class,
                            jdbi, log,
                            new MultipartDownloadAction.DownloadOctetStreamFileInputTable(), urls, action);
            coproProcessor.startProducer(multipartDownload.getQuerySet(), Integer.valueOf(action.getContext().get("read.batch.size")));
            Thread.sleep(1000);
            coproProcessor.startConsumer(insertQuery, 1, Integer.valueOf(action.getContext().get("write.batch.size")), new MultipartDownloadAction.MultipartDownloadConsumerProcess(log, aMarker, action));
            log.info(aMarker, "Download OctetStream File has been completed {}  ", multipartDownload.getName());
        } catch (Exception t) {
            log.error(aMarker, "Error at Download OctetStream File execute method {}", ExceptionUtil.toString(t));
            throw new HandymanException("Error at Download OctetStream File execute method ", t, action);
        }
    }

    public static class MultipartDownloadConsumerProcess implements CoproProcessor.ConsumerProcess<MultipartDownloadAction.DownloadOctetStreamFileInputTable, MultipartDownloadAction.MultipartDownloadOutputTable> {
        private final Logger log;
        private final Marker aMarker;

        public final ActionExecutionAudit action;

        final OkHttpClient httpclient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.MINUTES)
                .writeTimeout(10, TimeUnit.MINUTES)
                .readTimeout(10, TimeUnit.MINUTES)
                .build();

        public MultipartDownloadConsumerProcess(final Logger log, final Marker aMarker, ActionExecutionAudit action) {
            this.log = log;
            this.aMarker = aMarker;
            this.action = action;
        }

        @Override
        public List<MultipartDownloadAction.MultipartDownloadOutputTable> process(URL endpoint, MultipartDownloadAction.DownloadOctetStreamFileInputTable entity) throws Exception {

            List<MultipartDownloadAction.MultipartDownloadOutputTable> parentObj = new ArrayList<>();
            String inputFilePath = entity.getFilepath();

            MediaType MEDIA_TYPE = MediaType.parse("application/json");

            URL url = new URL(endpoint.toString() + "?filepath=" + inputFilePath);
            Request request = new Request.Builder().url(url)
                    .addHeader("accept", "*/*")
                    .post(RequestBody.create("{}", MEDIA_TYPE))
                    .build();

            if (log.isInfoEnabled()) {
                log.info("Sending request to URL: {}", url);
                log.info("Request headers: {}", request.headers());
                log.info(aMarker, "Request has been build with the parameters {} ,inputFilePath : {}", endpoint, inputFilePath);
            }

            try (Response response = httpclient.newCall(request).execute()) {
                if (response.isSuccessful()) {

                    log.info("Response is successful and Response Details: {}", response);
                    log.info("Response is successful and header Details: {}", response.headers());


                    // Create a new file and save the response body into it
                    File file = new File(inputFilePath);

                    File parentDir = file.getParentFile();
                    if (!parentDir.exists()) {
                        log.info("Directory created: {}", parentDir.mkdir());
                    }
                    try (ResponseBody responseBody = response.body();
                         InputStream inputStream = Objects.requireNonNull(responseBody).byteStream();
                         FileOutputStream outputStream = new FileOutputStream(file, false)) {

                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }

                        // Flush and close the output stream to ensure all data is written
                        outputStream.flush();
                        outputStream.close();

                        log.info("File {} downloaded successfully", file.getName());
                    } catch (Exception e) {
                        log.error("Error writing file: {}", e.getMessage());
                        HandymanException handymanException = new HandymanException(e);
                        HandymanException.insertException("Exception occurred in Writing multipart File for file - " + inputFilePath, handymanException, this.action);
                    }
                }
            } catch (Exception e) {
                log.error(aMarker, "The Exception occurred in Download multipart File for file {} with exception {}", inputFilePath, e.getMessage());
                HandymanException handymanException = new HandymanException(e);
                HandymanException.insertException("Exception occurred in Download multipart File for file - " + inputFilePath, handymanException, this.action);
            }
            return parentObj;
        }
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DownloadOctetStreamFileQueryResult {

        private String filepath;

    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DownloadOctetStreamFileInputTable {

        private String filepath;

    }


    @AllArgsConstructor
    @Data
    @Builder
    public static class MultipartDownloadOutputTable implements CoproProcessor.Entity {

        @Override
        public List<Object> getRowData() {
            return null;
        }
    }


    @Override
    public boolean executeIf() throws Exception {
        return multipartDownload.getCondition();
    }
}
