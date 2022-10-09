package in.handyman.raven.lib;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.action.ActionExecution;
import in.handyman.raven.lambda.action.IActionExecution;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.CheckboxClassification;
import in.handyman.raven.util.InstanceUtil;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.commons.compress.utils.FileNameUtils;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.nio.file.Paths;
import java.util.Optional;

/**
 * Auto Generated By Raven
 */
@ActionExecution(
        actionName = "CheckboxClassification"
)
public class CheckboxClassificationAction implements IActionExecution {
    private static final MediaType MediaTypeJSON = MediaType
            .parse("application/json; charset=utf-8");
    private final ActionExecutionAudit action;
    private final Logger log;
    private final CheckboxClassification checkboxClassification;
    private final Marker aMarker;
    private final ObjectMapper mapper = new ObjectMapper();
    private final String URI;

    public CheckboxClassificationAction(final ActionExecutionAudit action, final Logger log,
                                        final Object checkboxClassification) {
        this.checkboxClassification = (CheckboxClassification) checkboxClassification;
        this.action = action;
        this.log = log;
        this.aMarker = MarkerFactory.getMarker(" CheckboxClassification:" + this.checkboxClassification.getName());
        this.URI = action.getContext().get("copro.checkbox.url");

    }

    @Override
    public void execute() throws Exception {
        final OkHttpClient httpclient = InstanceUtil.createOkHttpClient();

        final ObjectNode objectNode = mapper.createObjectNode();

        objectNode.put("inputFilePath", checkboxClassification.getFilePath());
        objectNode.put("outputDir", checkboxClassification.getOutputDir());

        log.info(aMarker, " input variables id : {}, name : {}", action.getActionId(), checkboxClassification.getName());
        // build a request
        log.info(aMarker, "Checkbox Classification Action for filename : {}, from filepath : {}", Paths.get(checkboxClassification.getFilePath()).getFileName().toString(), FileNameUtils.getBaseName(checkboxClassification.getFilePath()));

        Request request = new Request.Builder().url(URI)
                .post(RequestBody.create(objectNode.toString(), MediaTypeJSON)).build();
        log.debug(aMarker, "Request has been build with the parameters \n URI : {} \n Input-File-Path : {} \n Output-Directory : {}", URI, checkboxClassification.getFilePath(), checkboxClassification.getOutputDir());
        String name = checkboxClassification.getName() + "_response";
        try (Response response = httpclient.newCall(request).execute()) {
            String responseBody = response.body().string();
            if (response.isSuccessful()) {
                log.info(aMarker, "CheckBox Classification Action has completed its execution");
                String classifyName = checkboxClassification.getName() + "_classification";
                action.getContext().put(name, responseBody);
                action.getContext().put(classifyName, Optional.ofNullable(mapper.readTree(responseBody).get("label")).map(JsonNode::asText).map(String::toLowerCase).orElseThrow());
                action.getContext().put(name.concat(".error"), "false");
                log.info(aMarker, "The Successful Response  {} {}", name, responseBody);
            } else {
                log.info(aMarker, "CheckBox Classification has failed with bad response");
                action.getContext().put(name.concat(".error"), "true");
                action.getContext().put(name.concat(".errorMessage"), responseBody);
                log.info(aMarker, "The Failure Response  {} {}", name, responseBody);
            }
        } catch (Exception e) {
            action.getContext().put(name.concat(".error"), "true");
            action.getContext().put(name.concat(".errorMessage"), e.getMessage());
            log.info(aMarker, "The Exception occurred ", e);
            throw new HandymanException("Failed to execute", e);
        }
    }

    @Override
    public boolean executeIf() throws Exception {
        return checkboxClassification.getCondition();
    }
}

