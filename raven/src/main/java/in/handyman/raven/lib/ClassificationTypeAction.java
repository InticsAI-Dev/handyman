package in.handyman.raven.lib;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.action.ActionExecution;
import in.handyman.raven.lambda.action.IActionExecution;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.ClassificationType;

import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

/**
 * Auto Generated By Raven
 */
@ActionExecution(
        actionName = "ClassificationType"
)
public class ClassificationTypeAction implements IActionExecution {
    private final ActionExecutionAudit action;

    private final Logger log;

    private final ClassificationType classificationType;

    private final Marker aMarker;

    private final ObjectMapper mapper = new ObjectMapper();

    private static final MediaType MediaTypeJSON = MediaType
            .parse("application/json; charset=utf-8");

    private final String URI;

    public ClassificationTypeAction(final ActionExecutionAudit action, final Logger log,
                                    final Object classificationType) {
        this.classificationType = (ClassificationType) classificationType;
        this.action = action;
        this.log = log;
        this.aMarker = MarkerFactory.getMarker(" ClassificationType:" + this.classificationType.getName());
        this.URI = action.getContext().get("copro.classifier-type.url");
    }

    @Override
    public void execute() throws Exception {
        log.info(aMarker, "<-------Pixel Classifier Action for {} has been started------->" + classificationType.getName());
        final OkHttpClient httpclient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.MINUTES)
                .writeTimeout(10, TimeUnit.MINUTES)
                .readTimeout(10, TimeUnit.MINUTES)
                .build();

        final ObjectNode objectNode = mapper.createObjectNode();

        objectNode.put("inputFilePath", classificationType.getInputFilePath());
        objectNode.put("outputDir", classificationType.getOutputDir());
        objectNode.put("modelFilePath", classificationType.getModelFilePath());
        objectNode.set("labels", mapper.readTree(classificationType.getLabels()));

        log.info(aMarker, " Input variables id : {}", action.getActionId());
        Request request = new Request.Builder().url(URI)
                .post(RequestBody.create(objectNode.toString(), MediaTypeJSON)).build();

        log.debug(aMarker, "Request has been build with the parameters \n URI : {} \n Input-File-Path : {} \n Output-Directory : {} \n Model-Path : {} \n Path-Labels : {}", URI, classificationType.getInputFilePath(), classificationType.getOutputDir(), classificationType.getModelFilePath(), classificationType.getLabels());
        String name = classificationType.getName() + "_response";
        log.debug(aMarker, "The Request Details: {}", request);
        try (Response response = httpclient.newCall(request).execute()) {
            String responseBody = response.body().string();
//            String labelName = classificationType.getName() + "_label";
            if (response.isSuccessful()) {
                action.getContext().put(name, responseBody);
//                action.getContext().put(labelName, Optional.ofNullable(mapper.readTree(responseBody).get("label")).map(JsonNode::asText).map(String::toLowerCase).orElseThrow());
                action.getContext().put(name.concat(".error"), "false");
                log.info(aMarker, "The Successful Response for {} --> {}", name, responseBody);
            } else {
                action.getContext().put(name.concat(".error"), "true");
                action.getContext().put(name.concat(".errorMessage"), responseBody);
                log.info(aMarker, "The Failure Response {} --> {}", name, responseBody);
            }
            log.info(aMarker, "<-------Pixel Classifier Action for {} has been completed------->" + classificationType.getName());
        } catch (Exception e) {
            action.getContext().put(name.concat(".error"), "true");
            action.getContext().put(name.concat(".errorMessage"), e.getMessage());
            log.info(aMarker, "The Exception occurred ", e);
            throw new HandymanException("Failed to execute", e);
        }
    }

    @Override
    public boolean executeIf() throws Exception {
        return classificationType.getCondition();
    }
}
