package in.handyman.raven.lib;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.zaxxer.hikari.HikariDataSource;
import in.handyman.raven.connection.ResourceAccess;
import in.handyman.raven.process.Context;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.action.Action;
import in.handyman.raven.action.IActionExecution;
import in.handyman.raven.lib.model.RestApi;
import in.handyman.raven.util.CommonQueryUtil;
import in.handyman.raven.util.ExceptionUtil;
import lombok.extern.log4j.Log4j2;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.logging.log4j.MarkerManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Auto Generated By Raven
 */
@Action(
        actionName = "RestApi"
)
@Log4j2
public class RestApiAction implements IActionExecution {

    private static final String APPLICATION_OCTET_STREAM = "application/octet-stream";
    private static final String APPLICATION_JSON_CHARSET_UTF_8 = "application/json; charset=utf-8";
    private static final String POST = "POST";
    private static final String DELETE = "DELETE";
    private static final String GET = "GET";
    private static final String PUT = "PUT";
    private final Context context;

    private final RestApi restApi;

    private final MarkerManager.Log4jMarker aMarker;

    public RestApiAction(final Context context, final Object restApi) {
        this.restApi = (RestApi) restApi;
        this.context = context;
        this.aMarker = new MarkerManager.Log4jMarker("RestApi");
        this.context.getDetailMap().putPOJO("context", restApi);
    }

    @Override
    public void execute() throws Exception {
        var url = new StringBuilder(restApi.getUrl());
        var source = restApi.getSource();
        var method = restApi.getMethod();
        var name = restApi.getName();
        var payload = restApi.getValue();
        var id = context.getProcessId();
        var header = restApi.getHeaders();
        final HikariDataSource hikariDataSource = ResourceAccess.rdbmsConn(source);
        final ObjectNode detailMap = context.getDetailMap();
        log.info(aMarker, " id#{}, name#{}, url#{}, payload#{}", id, name, url, payload);
        final OkHttpClient client = new OkHttpClient();
        final Request request;
        final JsonNode params = restApi.getParams();
        if (Objects.nonNull(params)) {
            final List<String> paramList = new ArrayList<>();
            if (params.isArray()) {
                params.forEach(jsonNode -> jsonNode.fields()
                        .forEachRemaining(stringJsonNodeEntry -> addParam(hikariDataSource, detailMap, paramList, stringJsonNodeEntry)));
            } else {
                params.fields()
                        .forEachRemaining(stringJsonNodeEntry -> addParam(hikariDataSource, detailMap, paramList, stringJsonNodeEntry));
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
                        .forEachRemaining(stringJsonNodeEntry -> addHeader(hikariDataSource, detailMap, builder, stringJsonNodeEntry)));
            } else {
                header.fields()
                        .forEachRemaining(stringJsonNodeEntry -> addHeader(hikariDataSource, detailMap, builder, stringJsonNodeEntry));
            }
        }

        final RequestBody body;
        if (Objects.equals(Constants.BODY_TYPE_JSON, restApi.getBodyType())) {
            var bodyNode = JsonNodeFactory.instance.objectNode();
            payload.forEach(restPart -> bodyNode.put(restPart.getPartName(), getResult(hikariDataSource, detailMap, restPart.getPartData())));
            body = RequestBody.create(bodyNode.toString(), MediaType.get(APPLICATION_JSON_CHARSET_UTF_8));
        } else if (Objects.equals(Constants.BODY_TYPE_FORM, restApi.getBodyType())) {
            final MultipartBody.Builder formBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM);
            restApi.getValue().forEach(restPart -> {
                if (Objects.equals(restPart.getType(), Constants.PART_TYPE_TEXT)) {
                    formBody.addFormDataPart(restPart.getPartName(), getResult(hikariDataSource, detailMap, restPart.getPartData()));
                } else if (Objects.equals(restPart.getType(), Constants.PART_TYPE_FILE)) {
                    final File multiPart = new File(getResult(hikariDataSource, detailMap, restPart.getPartData()));
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
            context.getDetailMap().put("Exception", ExceptionUtil.toString(e));
        }
    }

    private void addParam(final HikariDataSource hikariDataSource, final ObjectNode detailMap, final List<String> paramList, final Map.Entry<String, JsonNode> stringJsonNodeEntry) {
        final String result = getResult(hikariDataSource, detailMap, stringJsonNodeEntry.getValue().textValue());
        if (Objects.nonNull(result)) {
            paramList.add(String.format("%s=\"%s\"", stringJsonNodeEntry.getKey(), result));
        }
    }

    private void addHeader(final HikariDataSource hikariDataSource, final ObjectNode detailMap, final Request.Builder builder, final Map.Entry<String, JsonNode> stringJsonNodeEntry) {
        final String result = getResult(hikariDataSource, detailMap, stringJsonNodeEntry.getValue().textValue());
        if (Objects.nonNull(result)) {
            builder.header(stringJsonNodeEntry.getKey(), result);
        }
    }

    private String getResult(final HikariDataSource hikariDataSource, final ObjectNode detailMap, final String partData) {
        return CommonQueryUtil.getResult(hikariDataSource, partData, detailMap);
    }

    @Override
    public boolean executeIf() throws Exception {
        return restApi.getCondition();
    }
}
