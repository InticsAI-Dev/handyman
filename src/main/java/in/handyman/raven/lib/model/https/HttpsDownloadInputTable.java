package in.handyman.raven.lib.model.https;

import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Data

public class HttpsDownloadInputTable {
    private String url;
}
