package in.handyman.raven.lib.model.ftpUpload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FtpUploadInputTable {
    private String username;
    private String password;
    private String serverAddress;
    private String folderPath;
}
