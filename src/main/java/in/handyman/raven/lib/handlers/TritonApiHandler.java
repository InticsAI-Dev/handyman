package in.handyman.raven.lib.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.apache.poi.ss.formula.functions.T;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

public class TritonApiHandler implements ProcessApiHandler{

    @Override
    public String buildInputPayload(Object object, ObjectMapper objectMapper) throws JsonProcessingException {
        String inputPayloadString = objectMapper.writeValueAsString(object);
        return inputPayloadString;
    }

    @Override
    public String buildRequestBodyPayload(Object inputPayload, ObjectMapper objectMapper) throws JsonProcessingException{
        String requestPayloadString = objectMapper.writeValueAsString(inputPayload);
        return requestPayloadString;
    }

    @Override
    public Request buildRequestApiObject(URL url, String jsonRequest, MediaType mediaType, Map<String, String> headers) {
        Request.Builder request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(jsonRequest, mediaType));
        if (headers != null) {
            for (Map.Entry<String, String> header : headers.entrySet()) {
                request.addHeader(header.getKey(), header.getValue());
            }
        }
        return request.build();
    }

    @Override
    public ResponseBody executeRequestApi(Request request, OkHttpClient httpClient) throws IOException {
        ResponseBody responseBody=null;
        try ( Response response= httpClient.newCall(request).execute()) {
            responseBody=response.body();
        }catch (IOException e) {
            responseBody=null;
        }
        return responseBody;
    }

    @Override
    public <T> T processResponseBody(Response responseBody, ObjectMapper objectMapper,Class<T> tClass) throws IOException {
        if (responseBody == null || responseBody.body() == null) {
            throw new IllegalArgumentException("Response body is null");
        }
        T responseBodyClass= objectMapper.readValue(responseBody.body().string(), tClass);
        return responseBodyClass;
    }


    @Override
    public T processOutputPayloadResponse(String responseOutput, ObjectMapper objectMapper, Class<org.apache.poi.ss.formula.functions.T> responseClass) throws JsonProcessingException {
        if (responseOutput == null ) {
            throw new IllegalArgumentException("Response body is null");
        }
        T responseBodyClass= objectMapper.readValue(responseOutput, responseClass);
        return responseBodyClass;
    }

}
