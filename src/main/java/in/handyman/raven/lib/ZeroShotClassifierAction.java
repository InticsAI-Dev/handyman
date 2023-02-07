package in.handyman.raven.lib;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.action.ActionExecution;
import in.handyman.raven.lambda.action.IActionExecution;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.ZeroShotClassifier;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Auto Generated By Raven
 */
@ActionExecution(
        actionName = "ZeroShotClassifier"
)
public class ZeroShotClassifierAction implements IActionExecution {
    private static final MediaType MediaTypeJSON = MediaType
            .parse("application/json; charset=utf-8");
    private final ActionExecutionAudit action;
    private final Logger log;
    private final ZeroShotClassifier zeroShotClassifier;
    private final Marker aMarker;
    private final ObjectMapper mapper = new ObjectMapper();
    private final String URI;

    public ZeroShotClassifierAction(final ActionExecutionAudit action, final Logger log,
                                    final Object zeroShotClassifier) {
        this.zeroShotClassifier = (ZeroShotClassifier) zeroShotClassifier;
        this.action = action;
        this.log = log;
        this.aMarker = MarkerFactory.getMarker(" ZeroShotClassifier:" + this.zeroShotClassifier.getName());
        this.URI = action.getContext().get("copro.zero-shot-classifier.url");

    }

    @Override
    public void execute() throws Exception {
        log.info(aMarker, "<-------Zero Short Classifier Action for {} has been started------->" + zeroShotClassifier.getName());
        final OkHttpClient httpclient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.MINUTES)
                .writeTimeout(10, TimeUnit.MINUTES)
                .readTimeout(10, TimeUnit.MINUTES)
                .build();

        final ObjectNode objectNode = mapper.createObjectNode();

        objectNode.put("content", zeroShotClassifier.getContent());
        objectNode.set("labels", mapper.readTree(zeroShotClassifier.getCandidateLabels()));

        log.info(aMarker, " Input variables id : {}", action.getActionId());
        Request request = new Request.Builder().url(URI)
                .post(RequestBody.create(objectNode.toString(), MediaTypeJSON)).build();

        log.debug(aMarker, "Request has been build with the parameters \n URI : {} \n content : {} \n labels : {} ", URI, zeroShotClassifier.getContent(), zeroShotClassifier.getCandidateLabels());
        String name = zeroShotClassifier.getName() + "_response";
        log.debug(aMarker, "The Request Details: {} ", request);
        try (Response response = httpclient.newCall(request).execute()) {
            String responseBody = response.body().string();
            String labelName = zeroShotClassifier.getName() + "_label";
            if (response.isSuccessful()) {
                action.getContext().put(name, responseBody);
                action.getContext().put(labelName, Optional.ofNullable(mapper.readTree(responseBody).get("attributionValue")).map(JsonNode::asText).map(String::toLowerCase).orElseThrow());
                action.getContext().put(name.concat(".error"), "false");
                log.info(aMarker, "The Successful Response for {} --> {}", name, responseBody);
            } else {
                action.getContext().put(name.concat(".error"), "true");
                action.getContext().put(name.concat(".errorMessage"), responseBody);
                log.info(aMarker, "The Failure Response {} --> {}", name, responseBody);
            }
            log.info(aMarker, "<-------Zero Short Classifier Action for {} has been completed------->" + zeroShotClassifier.getName());
        } catch (Exception e) {
            action.getContext().put(name.concat(".error"), "true");
            action.getContext().put(name.concat(".errorMessage"), e.getMessage());
            log.info(aMarker, "The Exception occurred ", e);
            throw new HandymanException("Failed to execute", e);
        }
    }

    @Override
    public boolean executeIf() throws Exception {
        return zeroShotClassifier.getCondition();
    }

}
