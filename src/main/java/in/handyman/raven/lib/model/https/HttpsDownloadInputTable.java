package in.handyman.raven.lib.model.https;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class HttpsDownloadInputTable {
    private String url;
}
