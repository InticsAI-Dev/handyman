package in.handyman.raven.lib.model.ftpdownload;

import in.handyman.raven.lib.CoproProcessor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FtpDownloadInputTable implements CoproProcessor.Entity {
    private String username;

    private String password;
    private String serverAddress;
    private String folderPath;


    @Override
    public List<Object> getRowData() {
        return null;
    }
}

