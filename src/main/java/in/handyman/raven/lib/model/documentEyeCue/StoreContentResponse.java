package in.handyman.raven.lib.model.documentEyeCue;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class StoreContentResponse {

    private String status;
    private String message;
    private String dcn;

    public StoreContentResponse() {}

    public StoreContentResponse(String status, String message, String dcn) {
        this.status = status;
        this.message = message;
        this.dcn = dcn;
    }

    @Override
    public String toString() {
        return "StoreContentResponse{" +
                "status='" + status + '\'' +
                ", message='" + message + '\'' +
                ", dcn='" + dcn + '\'' +
                '}';
    }
}
