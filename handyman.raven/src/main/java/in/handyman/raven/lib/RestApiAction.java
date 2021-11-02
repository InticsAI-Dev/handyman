package in.handyman.raven.lib;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.zaxxer.hikari.HikariDataSource;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.access.ResourceAccess;
import in.handyman.raven.lambda.action.ActionExecution;
import in.handyman.raven.lambda.action.IActionExecution;
import in.handyman.raven.lambda.doa.Action;
import in.handyman.raven.lib.model.RestApi;
import in.handyman.raven.util.CommonQueryUtil;
import in.handyman.raven.util.ExceptionUtil;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Auto Generated By Raven
 */
@ActionExecution(
        actionName = RestApiAction.REST_API
)
public class RestApiAction implements IActionExecution {

    protected static final String REST_API = "RestApi";
    private static final String APPLICATION_OCTET_STREAM = "application/octet-stream";
    private static final String APPLICATION_JSON_CHARSET_UTF_8 = "application/json; charset=utf-8";
    private static final String POST = "POST";
    private static final String DELETE = "DELETE";
    private static final String GET = "GET";
    private static final String PUT = "PUT";
    private final Action action;
    private final Logger log;
    private final RestApi restApi;

    private final Marker aMarker;

    public RestApiAction(final Action action, final Logger log, final Object restApi) {
        this.restApi = (RestApi) restApi;
        this.action = action;
        this.log = log;
        this.aMarker = MarkerFactory.getMarker(REST_API);
    }

    @Override
    public void execute() throws Exception {
        var url = new StringBuilder(restApi.getUrl());
        var source = restApi.getSource();
        var method = restApi.getMethod();
        var name = restApi.getName();
        var payload = restApi.getValue();
        var id = action.getActionId();
        var header = restApi.getHeaders();
        final HikariDataSource hikariDataSource = ResourceAccess.rdbmsConn(source);
        log.info(aMarker, " id#{}, name#{}, url#{}, payload#{}", id, name, url, payload);
        final OkHttpClient client = new OkHttpClient();
        final Request request;
        final JsonNode params = restApi.getParams();
        if (Objects.nonNull(params)) {
            final List<String> paramList = new ArrayList<>();
            if (params.isArray()) {
                params.forEach(jsonNode -> jsonNode.fields()
                        .forEachRemaining(stringJsonNodeEntry -> addParam(hikariDataSource, paramList, stringJsonNodeEntry)));
            } else {
                params.fields()
                        .forEachRemaining(stringJsonNodeEntry -> addParam(hikariDataSource, paramList, stringJsonNodeEntry));
            }
            if (!paramList.isEmpty()) {
                url.append("?");
                url.append(String.join("&", paramList));
            }
        }

        final Request.Builder builder = new Request.Builder().url(url.toString());
        if (Objects.nonNull(header)) {
            if (header.isArray()) {
                header.forEach(jsonNode -> jsonNode.fields()
                        .forEachRemaining(stringJsonNodeEntry -> addHeader(hikariDataSource, builder, stringJsonNodeEntry)));
            } else {
                header.fields()
                        .forEachRemaining(stringJsonNodeEntry -> addHeader(hikariDataSource, builder, stringJsonNodeEntry));
            }
        }

        final RequestBody body;
        if (Objects.equals(Constants.BODY_TYPE_JSON, restApi.getBodyType())) {
            var bodyNode = JsonNodeFactory.instance.objectNode();
            payload.forEach(restPart -> bodyNode.put(restPart.getPartName(), getResult(hikariDataSource, restPart.getPartData())));
            body = RequestBody.create(bodyNode.toString(), MediaType.get(APPLICATION_JSON_CHARSET_UTF_8));
        } else if (Objects.equals(Constants.BODY_TYPE_FORM, restApi.getBodyType())) {
            final MultipartBody.Builder formBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM);
            restApi.getValue().forEach(restPart -> {
                if (Objects.equals(restPart.getType(), Constants.PART_TYPE_TEXT)) {
                    formBody.addFormDataPart(restPart.getPartName(), getResult(hikariDataSource, restPart.getPartData()));
                } else if (Objects.equals(restPart.getType(), Constants.PART_TYPE_FILE)) {
                    final File multiPart = new File(getResult(hikariDataSource, restPart.getPartData()));
                    final String multiPartName = multiPart.getName();
                    if (!multiPart.exists()) {
                        throw new HandymanException(String.format("File %s not found ", multiPartName));
                    }
                    formBody.addFormDataPart(restPart.getPartName(), multiPartName,
                            RequestBody.create(multiPart,
                                    MediaType.parse(APPLICATION_OCTET_STREAM)));
                }
            });
            body = formBody.build();
        } else if (Objects.equals(Constants.BODY_TYPE_NONE, restApi.getBodyType())) {
            body = RequestBody.create(new byte[0], null);
        } else {
            throw new HandymanException("Unknown Body type");
        }

        if (Objects.equals(GET, method)) {
            request = builder.get().build();
        } else if (Objects.equals(DELETE, method)) {
            request = builder.delete().build();
        } else if (Objects.equals(POST, method)) {
            request = builder.post(body).build();
        } else if (Objects.equals(PUT, method)) {
            request = builder.put(body).build();
        } else {
            throw new HandymanException("Unknown HTTP method");
        }

        try {
            final Response execute = client.newCall(request).execute();
            log.info("Rest Api Response Content: " + execute.body() + " for URL: " + url);
        } catch (Exception e) {
            log.error(aMarker, "Stopping execution, {}", url, e);
            log.error("Exception {}", ExceptionUtil.toString(e));
        }
    }

    private void addParam(final HikariDataSource hikariDataSource, final List<String> paramList, final Map.Entry<String, JsonNode> stringJsonNodeEntry) {
        final String result = getResult(hikariDataSource, stringJsonNodeEntry.getValue().textValue());
        if (Objects.nonNull(result)) {
            paramList.add(String.format("%s=\"%s\"", stringJsonNodeEntry.getKey(), result));
        }
    }

    private void addHeader(final HikariDataSource hikariDataSource, final Request.Builder builder, final Map.Entry<String, JsonNode> stringJsonNodeEntry) {
        final String result = getResult(hikariDataSource, stringJsonNodeEntry.getValue().textValue());
        if (Objects.nonNull(result)) {
            builder.header(stringJsonNodeEntry.getKey(), result);
        }
    }

    private String getResult(final HikariDataSource hikariDataSource, final String partData) {
        return CommonQueryUtil.getResult(hikariDataSource, partData, log);
    }

    @Override
    public boolean executeIf() throws Exception {
        return restApi.getCondition();
    }
}
