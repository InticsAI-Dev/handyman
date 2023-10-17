package in.handyman.raven.lib.ftpdownload;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.CoproProcessor;
import in.handyman.raven.lib.model.FtpDownload;
import okhttp3.MediaType;
import org.apache.commons.net.PrintCommandListener;
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
        this.aMarker = MarkerFactory.getMarker(" FtpDownload:" + this.ftpDownload.getName());}

    @Override
    public List<FtpDownloadOutputTable> process(URL endpoint, FtpDownloadInputTable entity) throws Exception {
        List<FtpDownloadOutputTable> parentObj = new ArrayList<>();
        FTPClient ftpClient = new FTPClient();
        try {

            String username = entity.getUsername();
            String password = entity.getPassword();
            String serverAddress = entity.getServerAddress();
            String filePath = entity.getFolderPath();
            final String tenantId = action.getContext().get("tenantId");
            final String destDir = action.getContext().get("destinationPath");
            final String uploadTime = action.getContext().get("uploadPath");

            ftpClient.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));
            int reply;
            ftpClient.connect(serverAddress, 21);
            log.info(aMarker, "FTP URL is: {} ", ftpClient.getDefaultPort());
            reply = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftpClient.disconnect();
                log.error("Exception in connecting to FTP Server");
            }
            ftpClient.login(username, password);
            ftpClient.enterLocalPassiveMode();
            ftpClient.setUseEPSVwithIPv4(true);
            System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");

            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            String workingDirectory = ftpClient.printWorkingDirectory();
            log.info(aMarker, "Current directory is {}", workingDirectory);

            FTPFile[] subFiles = ftpClient.listFiles(workingDirectory);
            if (subFiles != null) {
                for (FTPFile aFile : subFiles) {

                    if (aFile.isFile()) {
                        if (uploadTime == null) {
                            String remoteFilePath = workingDirectory + "/" + aFile.getName();
                            String savePath = filePath + File.separator + aFile.getName();
                            downloadSingleFile(ftpClient, remoteFilePath, savePath);


                            FtpDownloadOutputTable outputTable = FtpDownloadOutputTable.builder()
                                    .tenantId(1L)
                                    .rootPipelineId(1L) // Adjust as needed
                                    .createdDate(LocalDateTime.now())
                                    .createdBy(1L) // Set as needed
                                    .lastModifiedBy(1L) // Set as needed
                                    .lastModifiedDate(LocalDateTime.now())
                                    .status("Completed") // Set the status as needed
                                    .message("FTP download completed")
                                    .type("FTP") // Set as needed
                                    .lastProcessedOn(LocalDateTime.now())
                                    .ftpFolderPath(workingDirectory) // Set the FTP folder path
                                    .destinationPath(filePath) // Set the destination path
                                    .filePath(savePath) // Set the downloaded file path
                                    .build();

                            parentObj.add(outputTable);

                        }
                        else {
                            LocalDateTime lastModifiedTime = convertTimestampToLocalDateTime(aFile.getTimestamp().getTimeInMillis());
                            LocalDateTime uploadTimeDate = LocalDateTime.parse(uploadTime);

                            if (lastModifiedTime.isAfter(uploadTimeDate)) { // If uploadTime is specified and the file was uploaded after that time, download the file
                                String remoteFilePath = workingDirectory + "/" + aFile.getName();
                                String savePath = filePath + File.separator + aFile.getName();
                                downloadSingleFile(ftpClient, remoteFilePath, savePath);

                                FtpDownloadOutputTable outputTable = FtpDownloadOutputTable.builder()
                                        .tenantId(1L)
                                        .rootPipelineId(1L)
                                        .createdDate(LocalDateTime.now())
                                        .createdBy(1L)
                                        .lastModifiedBy(1L)
                                        .lastModifiedDate(LocalDateTime.now())
                                        .status("Completed")
                                        .message("FTP download completed")
                                        .type("FTP")
                                        .lastProcessedOn(LocalDateTime.now())
                                        .ftpFolderPath(workingDirectory)
                                        .destinationPath(filePath)
                                        .filePath(savePath)
                                        .build();

                                parentObj.add(outputTable);
                            }
                        }
                    }



        }
    }

        } catch (Exception e) {
            log.error("Error during FTP download: {}", e.getMessage(), e);

        } finally {
            if (ftpClient.isConnected()) {
                ftpClient.logout();
                ftpClient.disconnect();
            }
        }

        return parentObj;
    }
//    public void ftpDownloadDirectory(FTPClient ftpClient, String srcFile, String saveDir) throws IOException {
//        FTPFile[] subFiles = ftpClient.listFiles(srcFile);
//        if (subFiles != null) {
//            for (FTPFile aFile : subFiles) {
//
//                if (aFile.isFile()) {
//                    String remoteFilePath = srcFile + "/" + aFile.getName();
//                    String savePath = saveDir + File.separator + aFile.getName();
//                    downloadSingleFile(ftpClient, remoteFilePath, savePath);
//                }
//            }
//        }

//    }
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
