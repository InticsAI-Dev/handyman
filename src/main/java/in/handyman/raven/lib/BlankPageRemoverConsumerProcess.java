package in.handyman.raven.lib;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.util.ExceptionUtil;
import okhttp3.*;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

//6. write consumer process class which implements CoproProcessor.ConsumerProcess
public class BlankPageRemoverConsumerProcess implements CoproProcessor.ConsumerProcess<BlankPageRemoverInputTable, BlankPageRemoverOutputTable> {
    private final Logger log;
    private final Marker aMarker;
    private final ObjectMapper mapper = new ObjectMapper();
    private static final MediaType MediaTypeJSON = MediaType
            .parse("application/json; charset=utf-8");
    private final String outputDir;

    public final ActionExecutionAudit action;
    final OkHttpClient httpclient = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.MINUTES)
            .writeTimeout(10, TimeUnit.MINUTES)
            .readTimeout(10, TimeUnit.MINUTES)
            .build();

    public BlankPageRemoverConsumerProcess(final Logger log, final Marker aMarker, ActionExecutionAudit action, String outputDir) {
        this.log = log;
        this.aMarker = aMarker;
        this.action = action;
        this.outputDir = outputDir;
    }

    //7. overwrite the method process in coproprocessor, write copro api logic inside this method
    @Override
    public List<BlankPageRemoverOutputTable> process(URL endpoint, BlankPageRemoverInputTable entity) throws JsonProcessingException {
        List<BlankPageRemoverOutputTable> parentObj = new ArrayList<>();
        final ObjectNode objectNode = mapper.createObjectNode();
        objectNode.put("inputFilePath", entity.getFilePath());
        objectNode.put("outputDir", outputDir);
        log.info(aMarker, " Input variables id : {}", action.getActionId());
        Request request = new Request.Builder().url(endpoint)
                .post(RequestBody.create(objectNode.toString(), MediaTypeJSON)).build();
        log.debug(aMarker, "Request has been build with the parameters \n URI : {} ", request.body());
        log.debug(aMarker, "The Request Details: {}", request);
        Integer groupId = entity.getGroupId();
        try (Response response = httpclient.newCall(request).execute()) {
            String responseBody = Objects.requireNonNull(response.body()).string();
            if (response.isSuccessful()) {
                JSONObject parentResponseObject = new JSONObject(responseBody);
                parentObj.add(
                        BlankPageRemoverOutputTable
                                .builder()
                                .processedFilePath(Optional.ofNullable(parentResponseObject.get("processedFilePath")).map(String::valueOf).orElse(null))
                                .originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null))
                                .groupId(groupId)
                                .status("COMPLETED")
                                .stage("BLANK_PAGE_REMOVAL")
                                .message("Blankpage removal finished")
                                .response(responseBody)
                                .request(String.valueOf(request))
                                .endpoint(String.valueOf(endpoint))
                                .build());
            } else {
                parentObj.add(
                        BlankPageRemoverOutputTable
                                .builder()
                                .originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null))
                                .groupId(groupId)
                                .status("FAILED")
                                .stage("BLANK_PAGE_REMOVAL")
                                .message(response.message())
                                .response(response.message())
                                .request(String.valueOf(request))
                                .endpoint(String.valueOf(endpoint))
                                .build());
                log.info(aMarker, "The Exception occurred in blank page remover ");
            }
        } catch (Exception e) {
            parentObj.add(
                    BlankPageRemoverOutputTable
                            .builder()
                            .originId(Optional.ofNullable(entity.getOriginId()).map(String::valueOf).orElse(null))
                            .groupId(groupId)
                            .status("FAILED")
                            .stage("BLANK_PAGE_REMOVAL")
                            .message(ExceptionUtil.toString(e))
                            .response("Error in getting response")
                            .request(String.valueOf(request))
                            .endpoint(String.valueOf(endpoint))
                            .build());
            log.info(aMarker, "The Exception occurred in blank page remover ", e);
            //TODO  insert query for error queue
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("AutoRotation consumer failed for batch/group "+groupId,
                    handymanException,
                    this.action);
        }
        return parentObj;
    }

}
