package in.handyman.raven.lib.model.ftpdownload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FtpDownloadInputTable {

    private String username;
    private String password;
    private String serverAddress;
    private String folderPath;

}

