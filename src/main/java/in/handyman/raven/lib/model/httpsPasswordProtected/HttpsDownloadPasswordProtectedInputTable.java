package in.handyman.raven.lib.model.httpsPasswordProtected;

import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Data

public class HttpsDownloadPasswordProtectedInputTable {

        private String url;
        private String userName;
        private String password;
    }


