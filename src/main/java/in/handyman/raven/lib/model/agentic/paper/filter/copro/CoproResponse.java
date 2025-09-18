package in.handyman.raven.lib.model.agentic.paper.filter.copro;

import lombok.Getter;

import java.nio.charset.StandardCharsets;

/**
 * Immutable wrapper for HTTP responses from Copro API calls.
 */
@Getter
public class CoproResponse {

    private final int httpCode;
    private final String message;
    private final byte[] body;

    /**
     * Constructor.
     *
     * @param httpCode HTTP status code of the response
     * @param message  Optional message or description
     * @param body     Response body as bytes
     */
    public CoproResponse(int httpCode, String message, byte[] body) {
        this.httpCode = httpCode;
        this.message = message;
        this.body = body != null ? body.clone() : null; // Defensive copy
    }

    /**
     * Returns the response body as a UTF-8 string.
     *
     * @return body string or null if body is null
     */
    public String getBodyAsString() {
        return body == null ? null : new String(body, StandardCharsets.UTF_8);
    }

    @Override
    public String toString() {
        return "CoproResponse{" +
                "httpCode=" + httpCode +
                ", message='" + message + '\'' +
                ", bodyLength=" + (body != null ? body.length : 0) +
                '}';
    }
}
