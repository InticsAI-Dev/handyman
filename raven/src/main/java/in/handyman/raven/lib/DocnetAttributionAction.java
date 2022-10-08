package in.handyman.raven.lib;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.action.ActionExecution;
import in.handyman.raven.lambda.action.IActionExecution;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.DocnetAttribution;

import java.io.File;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.util.concurrent.TimeUnit;

import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

/**
 * Auto Generated By Raven
 */
@ActionExecution(
    actionName = "DocnetAttribution"
)
public class DocnetAttributionAction implements IActionExecution {
  private final ActionExecutionAudit action;

  private final Logger log;

  private final DocnetAttribution docnetAttribution;
  private static final MediaType MediaTypeJSON = MediaType.parse("application/json; charset=utf-8");

  private final Marker aMarker;
  private final String URI;
  private final ObjectMapper mapper = new ObjectMapper();

  public DocnetAttributionAction(final ActionExecutionAudit action, final Logger log,
      final Object docnetAttribution) {
    this.docnetAttribution = (DocnetAttribution) docnetAttribution;
    this.action = action;
    this.log = log;
    this.aMarker = MarkerFactory.getMarker(" DocnetAttribution: "+this.docnetAttribution.getName());
    this.URI=action.getContext().get("copro.docnet-attribution.url");
  }

  @Override
  public void execute() throws Exception {
    final OkHttpClient httpclient = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.MINUTES)
            .writeTimeout(10, TimeUnit.MINUTES)
            .readTimeout(10, TimeUnit.MINUTES).build();


    log.info(aMarker, "The request got it successfully for Asset ID and Attribution List {} {}",
            docnetAttribution.getName(),docnetAttribution.getAttributes());

    final ObjectNode objectNode = mapper.createObjectNode();

    objectNode.put("inputFilePath",docnetAttribution.getInputFilePath());
    objectNode.set("attributes",mapper.readTree(docnetAttribution.getAttributes()));

    Request request = new Request.Builder().url(URI)
            .post(RequestBody.create(objectNode.toString(),MediaTypeJSON)).build();

    try (Response response = httpclient.newCall(request).execute()) {
      String responseBody = response.body().string();
      String name = docnetAttribution.getName() + "_response";
      log.info(aMarker, "The response got it successfully for Asset ID and Attribution List {}",
              responseBody);
      if (response.isSuccessful()) {
        action.getContext().put(name, responseBody);
        action.getContext().put(name.concat(".error"), "false");
        log.info(aMarker, "The Successful Response  {} {}", name, responseBody);
      }else {
        action.getContext().put(name.concat(".error"), "true");
        action.getContext().put(name.concat(".errorMessage"), responseBody);
        log.info(aMarker, "The Failure Response  {} {}", name, responseBody);
      }
    }catch (Exception e){
      log.info(aMarker, "The Exception occurred ",e);
      throw new HandymanException("Failed to execute", e);
    }
  }

  @Override
  public boolean executeIf() throws Exception {
    return docnetAttribution.getCondition();
  }
}