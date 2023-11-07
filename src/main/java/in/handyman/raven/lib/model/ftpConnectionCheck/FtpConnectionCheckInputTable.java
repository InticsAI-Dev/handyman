package in.handyman.raven.lib.model.ftpConnectionCheck;

import lombok.*;

@Getter
@Setter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class FtpConnectionCheckInputTable {

    private String username;
    private String password;
    private String serverAddress;
    private String FolderPath;
}
