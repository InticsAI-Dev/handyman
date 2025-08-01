package in.handyman.raven.lib.model.documentEyeCue;

import java.util.HashMap;
import java.util.Map;

public class StoreContentResponseDto {
    private int status;
    private String contentID;
    private String message;
    private Map<String, String> responseHeaders = new HashMap<>();

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public String getContentID() { return contentID; }
    public void setContentID(String contentID) { this.contentID = contentID; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getResponseHeader(String key) { return responseHeaders.get(key); }
    public void setResponseHeader(String key, String value) { this.responseHeaders.put(key, value); }
}
