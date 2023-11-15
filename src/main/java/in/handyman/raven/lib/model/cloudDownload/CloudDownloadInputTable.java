package in.handyman.raven.lib.model.cloudDownload;

import lombok.Data;

@Data
public class CloudDownloadInputTable {
    private String bucketName;
    private String objectKey;
}
