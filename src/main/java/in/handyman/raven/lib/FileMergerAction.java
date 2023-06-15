package in.handyman.raven.lib;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.action.ActionExecution;
import in.handyman.raven.lambda.action.IActionExecution;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.FileMerger;
import in.handyman.raven.util.InstanceUtil;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.util.Objects;

/**
 * Auto Generated By Raven
 */
@ActionExecution(
        actionName = "FileMerger"
)
public class FileMergerAction implements IActionExecution {
    private static final MediaType MediaTypeJSON = MediaType.parse("application/json; charset=utf-8");
    private final ActionExecutionAudit action;
    private final Logger log;
    private final FileMerger fileMerger;
    private final Marker aMarker;
    private final ObjectMapper mapper = new ObjectMapper();
    private final String URI;

    public FileMergerAction(final ActionExecutionAudit action, final Logger log,
                            final Object fileMerger) {
        this.fileMerger = (FileMerger) fileMerger;
        this.action = action;
        this.log = log;
        this.aMarker = MarkerFactory.getMarker(" FileMerger:" + this.fileMerger.getName());
        this.URI = action.getContext().get("copro.file-merger.url");
    }

    @Override
    public void execute() throws Exception {
        final OkHttpClient httpclient = InstanceUtil.createOkHttpClient();
        String requestBody = fileMerger.getRequestBody();
        ObjectNode inputJSON = (ObjectNode) mapper.readTree(requestBody);

        String outputDir = fileMerger.getOutputDir();
        final String mergerProcessName="FILE_MERGER";
        String rootPipelineId= action.getContext().get("gen_id.root_pipeline_id");
        Long actionId=action.getActionId();

        inputJSON.put("outputDir", outputDir);
        inputJSON.put("actionId",actionId);
        inputJSON.put("rootPipelineId",rootPipelineId);
        inputJSON.put("process",mergerProcessName);
        // BUILD A REQUEST
        Request request = new Request.Builder().url(URI)
                .post(RequestBody.create(inputJSON.toString(), MediaTypeJSON)).build();

        if(log.isInfoEnabled()){
            log.info(aMarker, "The request got it successfully the copro url {} ,request body {} and output directory  {}", URI,requestBody,outputDir);
        }
        String name = "file-merger-response";
        try (Response response = httpclient.newCall(request).execute()) {
            String responseBody = Objects.requireNonNull(response.body()).string();
            if (response.isSuccessful()) {
                action.getContext().put(name, mapper.readTree(responseBody).toString());
                action.getContext().put(name.concat(".success"), "true");
                log.info(aMarker, "The Successful Response  {} {}", name, responseBody);
            } else {
                action.getContext().put(name.concat(".error"), "true");
                log.error(aMarker, "The Failure Response  {} {}", name, responseBody);
            }
        } catch (Exception e) {
            action.getContext().put(name.concat(".error"), "true");
            log.error(aMarker, "The Exception occurred ", e);
            throw new HandymanException("Failed to execute", e, action);
        }
    }


    @Override
    public boolean executeIf() throws Exception {
        return fileMerger.getCondition();
    }
}
