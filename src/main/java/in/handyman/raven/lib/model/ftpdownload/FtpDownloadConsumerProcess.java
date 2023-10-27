package in.handyman.raven.lib.model.ftpdownload;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.CoproProcessor;
import in.handyman.raven.lib.model.FtpDownload;
import okhttp3.MediaType;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.io.*;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class FtpDownloadConsumerProcess implements CoproProcessor.ConsumerProcess<FtpDownloadInputTable, FtpDownloadOutputTable> {
    private final ActionExecutionAudit action;

    private final Logger log;

    private final FtpDownload ftpDownload;

    private final Marker aMarker;
    private final ObjectMapper mapper = new ObjectMapper();
    private static final MediaType MEDIA_TYPE_JSON = MediaType
            .parse("application/json; charset=utf-8");


    public FtpDownloadConsumerProcess(Logger log, Marker aMarker, ActionExecutionAudit action, final Object ftpDownload) {
        this.ftpDownload = (FtpDownload) ftpDownload;
        this.action = action;
        this.log = log;
        this.aMarker = MarkerFactory.getMarker(" FtpDownload:" + this.ftpDownload.getName());
    }

    @Override
    public List<FtpDownloadOutputTable> process(URL endpoint, FtpDownloadInputTable entity) throws Exception {
        List<FtpDownloadOutputTable> parentObj = new ArrayList<>();
        List<String> downloadedFilePaths = new ArrayList<>(); // Store downloaded file paths
        log.info("now initializing the ftpclient..");
        FTPClient ftpClient = new FTPClient();
        log.info("ftpclient initialized...");


        try {
            String userName = entity.getUsername();
            String password = entity.getPassword();
            String remoteHost = entity.getServerAddress();
            String sourceDir = entity.getFolderPath();

            final String tenantId = action.getContext().get("tenant_id");
            final Long rootPipelineId = action.getRootPipelineId();

            final String remotePort = action.getContext().get("ftpPort");
            final String DestDir = action.getContext().get("destinationPath");
            final String uploadTime = action.getContext().get("uploadTime");


            int reply;
            ftpClient.connect(remoteHost, Integer.parseInt(remotePort));
            log.info(aMarker, "FTP URL HOST {} and port {} ", remoteHost, ftpClient.getDefaultPort());
            reply = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftpClient.disconnect();
                log.error("Exception in connecting to FTP Server");
            }
            ftpClient.login(userName, password);
            ftpClient.enterLocalPassiveMode();
            ftpClient.setUseEPSVwithIPv4(true);
            System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");

            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            ftpClient.changeWorkingDirectory(sourceDir);
            if (uploadTime.equals("null")) {
                FTPFile[] subFiles = ftpClient.listFiles();
                if (subFiles != null) {
                    for (FTPFile aFile : subFiles) {

                        if (aFile.isFile()) {

                            String remoteFilePath = sourceDir + "/" + aFile.getName();
                            String savePath = DestDir + File.separator + aFile.getName();
                            downloadSingleFile(ftpClient, remoteFilePath, savePath);
                            downloadedFilePaths.add(savePath);// Add downloaded file path to the list
                        }
                    }
                    if (!downloadedFilePaths.isEmpty()) {

                        ObjectMapper mapper = new ObjectMapper();
                        String filePaths = mapper.writeValueAsString(downloadedFilePaths);

                        FtpDownloadOutputTable outputTable = FtpDownloadOutputTable.builder()
                                .tenantId(1L)
                                .rootPipelineId(rootPipelineId) // Adjust as needed
                                .createdDate(LocalDateTime.now())
                                .createdBy(1L) // Set as needed
                                .lastModifiedBy(1L) // Set as needed
                                .lastModifiedDate(LocalDateTime.now())
                                .status("ACTIVE") // Set the status as needed
                                .message("FTP download completed")
                                .type("FTP") // Set as needed
                                .lastProcessedOn(LocalDateTime.now())
                                .ftpFolderPath(sourceDir) // Set the FTP folder path
                                .destinationPath(DestDir) // Set the destination path
                                .filePaths(filePaths)
                                .createdOn(LocalDateTime.now())
                                .createdUserId(1)
                                .lateUpdatedOn(LocalDateTime.now())
                                .lastUpdatedUserId(1)
                                .version(1)
                                .build();
                        parentObj.add(outputTable);

                    }
                }


            } else {

                FTPFile[] subFiles = ftpClient.listFiles();
                if (subFiles != null) {
                    for (FTPFile aFile : subFiles) {

                        LocalDateTime lastModifiedTime = convertTimestampToLocalDateTime(aFile.getTimestamp().getTimeInMillis());
                        LocalDateTime uploadTimeDate = LocalDateTime.parse(uploadTime);

                        if (lastModifiedTime.isAfter(uploadTimeDate)) { // If uploadTime is specified and the file was uploaded after that time, download the file
                            String remoteFilePath = sourceDir + "/" + aFile.getName();
                            String savePath = DestDir + File.separator + aFile.getName();
                            downloadSingleFile(ftpClient, remoteFilePath, savePath);
                        }
                    }
                    if (!downloadedFilePaths.isEmpty()) {
                        ObjectMapper mapper = new ObjectMapper();
                        String filePaths = mapper.writeValueAsString(downloadedFilePaths);
                        FtpDownloadOutputTable outputTable = FtpDownloadOutputTable.builder()
                                .tenantId(1L)
                                .rootPipelineId(1L)
                                .createdDate(LocalDateTime.now())
                                .createdBy(1L)
                                .lastModifiedBy(1L)
                                .lastModifiedDate(LocalDateTime.now())
                                .status("ACTIVE")
                                .message("FTP download completed")
                                .type("FTP")
                                .lastProcessedOn(LocalDateTime.now())
                                .ftpFolderPath(sourceDir)
                                .destinationPath(DestDir)
                                .filePaths(filePaths)
                                .createdOn(LocalDateTime.now())
                                .createdUserId(1)
                                .lateUpdatedOn(LocalDateTime.now())
                                .lastUpdatedUserId(1)
                                .version(1)
                                .build();

                        parentObj.add(outputTable);
                    }
                }
            }


        } catch (Exception e) {
            log.error("Error during FTP download: {}", e.getMessage(), e);
            e.printStackTrace();

        } finally {
            if (ftpClient.isConnected()) {
                ftpClient.logout();
                ftpClient.disconnect();
            } else {
                log.info("Nothing to disconnect for ftpclient ");
            }
        }


        return parentObj;
    }

    public void downloadSingleFile(FTPClient ftpClient, String remoteFilePath, String savePath) throws IOException {
        File downloadFile = new File(savePath);
        File parentDir = downloadFile.getParentFile();
        if (!parentDir.exists()) {
            boolean createDirSuccessful = parentDir.mkdir();
            if (!createDirSuccessful) log.error("Problem in creating the directory {} ", parentDir);


        }
        try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(downloadFile))) {
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            log.info(aMarker, "Download file {} ", savePath);
            boolean success = ftpClient.retrieveFile(remoteFilePath, outputStream);

            if (success) {
                log.info(aMarker, "Download the file {} ", remoteFilePath);
            } else {
                log.error(aMarker, "Couldn't download the file: {} ", remoteFilePath);
            }
        }
    }


    private LocalDateTime convertTimestampToLocalDateTime(long timestamp) {
        Instant instant = Instant.ofEpochMilli(timestamp);
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }

}
