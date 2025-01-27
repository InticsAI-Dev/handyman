package in.handyman.raven.lib.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.apache.poi.ss.formula.functions.T;

import java.io.IOException;
import java.net.URL;
import java.net.http.HttpClient;
import java.util.Map;

public interface ProcessApiHandler {
    public String buildInputPayload(Object object, ObjectMapper objectMapper) throws JsonProcessingException;
    public String buildRequestBodyPayload(Object inputPayload, ObjectMapper objectMapper) throws JsonProcessingException;
    public Request buildRequestApiObject(URL url, String jsonRequest, MediaType mediaType, Map<String, String> headers);
    public ResponseBody executeRequestApi(Request request, OkHttpClient httpClient) throws IOException;
    public <T> T processResponseBody(Response responseBody, ObjectMapper objectMapper,Class<T> responseClass)throws IOException;
    public T processOutputPayloadResponse(String responseOutput, ObjectMapper objectMapper,Class<T> responseClass)throws JsonProcessingException;

}
