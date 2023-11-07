package in.handyman.raven.lib.model.ftpUpload;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.net.MediaType;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.FtpUpload;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import in.handyman.raven.lib.CoproProcessor;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.ArrayList;
import java.net.URL;


public class FtpUploadConsumerProcess implements CoproProcessor.ConsumerProcess<FtpUploadInputTable, FtpUploadOutputTable> {
    private final ActionExecutionAudit action;

    private final Logger log;

    private final FtpUpload ftpUpload;

    private final Marker aMarker;
    private final ObjectMapper mapper = new ObjectMapper();
    private static final MediaType MEDIA_TYPE_JSON = MediaType
            .parse("application/json; charset=utf-8");


    public FtpUploadConsumerProcess(Logger log, Marker aMarker, ActionExecutionAudit action, final Object ftpDownload) {
        this.ftpUpload = (FtpUpload) ftpDownload;
        this.action = action;
        this.log = log;
        this.aMarker = MarkerFactory.getMarker(" FtpDownload:" + this.ftpUpload.getName());
    }

    @Override
    public List<FtpUploadOutputTable> process(URL endpoint, FtpUploadInputTable entity) throws Exception {
        List<FtpUploadOutputTable> parentObj = new ArrayList<>();
        List<String> UploadFilePaths = new ArrayList<>(); // Store downloaded file paths
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
            final String destDir = action.getContext().get("destinationPath");
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
            System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            log.info(aMarker, "Remote system is {} ", ftpClient.getSystemType());
            String workingDirectory = ftpClient.printWorkingDirectory();
            log.info(aMarker, "Current directory is {}", workingDirectory);
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            ftpClient.changeWorkingDirectory(sourceDir);
            if (uploadTime.equals("null")) {

                File directory = new File(destDir);
                if (directory.exists() && directory.isDirectory()) {
                    File[] filesAndDirs = directory.listFiles();

                    if (filesAndDirs != null) {
                        for (File fileOrDir : filesAndDirs) {
                            if (fileOrDir.isFile()) {
                                System.out.println("File: " + fileOrDir.getName());
                                System.out.println("Directory: " + fileOrDir.getName());
                                uploadDirectory(ftpClient, sourceDir,  destDir,"");
                         }
                        }
                        if (!UploadFilePaths.isEmpty()) {

                            ObjectMapper mapper = new ObjectMapper();
                            String filePaths = mapper.writeValueAsString(UploadFilePaths);

                            FtpUploadOutputTable outputTable = FtpUploadOutputTable.builder()
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
                                    .destinationPath(destDir) // Set the destination path
                                    .filePaths(filePaths)
                                    .version(1)
                                    .build();
                            parentObj.add(outputTable);

                        }
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
                                String savePath = destDir + File.separator + aFile.getName();
                                uploadDirectory(ftpClient, sourceDir, destDir,"");
                            }
                        }
                        if (!UploadFilePaths.isEmpty()) {
                            ObjectMapper mapper = new ObjectMapper();
                            String filePaths = mapper.writeValueAsString(UploadFilePaths);
                            FtpUploadOutputTable outputTable = FtpUploadOutputTable.builder()
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
                                    .destinationPath(destDir)
                                    .filePaths(filePaths)
                                    .version(1)
                                    .build();

                            parentObj.add(outputTable);
                        }
                    }
                }


            } catch(Exception e){
                e.printStackTrace();
            } finally{
                if (ftpClient.isConnected()) {
                    ftpClient.logout();
                    ftpClient.disconnect();
                }
            }
            return parentObj;
        }







    public void uploadDirectory(FTPClient ftpClient, String remoteDirPath, String localParentDir, String remoteParentDir) throws IOException {
        log.info(aMarker, "Listing directory {} ", localParentDir);
        File localDir = new File(localParentDir);
        File[] subFiles = localDir.isFile() ? new File[]{localDir} : localDir.listFiles();
        if (subFiles != null) {
            for (File item : subFiles) {
                String remoteFilePath = remoteDirPath + File.separator + remoteParentDir + File.separator + item.getName();
                if (remoteParentDir.equals("")) {
                    remoteFilePath = remoteDirPath + File.separator + item.getName();
                }
                if (item.isFile()) {
                    singleFileUploadProcessing(ftpClient, item, remoteFilePath);
                } else {
                    directoryUploadProcessing(ftpClient, remoteDirPath, remoteParentDir, item, remoteFilePath);
                }
            }
        }
    }

    private void directoryUploadProcessing(FTPClient ftpClient, String remoteDirPath, String remoteParentDir, File item, String remoteFilePath) throws IOException {
        String localParentDir;
        boolean created = ftpClient.makeDirectory(remoteFilePath);
        if (created) {
            log.info(aMarker, "Created the directory: {} ", remoteFilePath);
        } else {
            log.info(aMarker, "Couldn't create the directory: {} ", remoteFilePath);
        }
        // upload the subdirectory
        String parent = remoteParentDir + File.separator + item.getName();
        if (remoteParentDir.equals("")) {
            parent = item.getName();
        }
        localParentDir = item.getAbsolutePath();
        uploadDirectory(ftpClient, remoteDirPath, localParentDir, parent);
    }

    private void singleFileUploadProcessing(FTPClient ftpClient, File item, String remoteFilePath) throws IOException {
        // upload the file
        String localFilePath = item.getAbsolutePath();
        log.info(aMarker, "About to upload the file: {} ", localFilePath);
        uploadSingleFile(ftpClient, localFilePath, remoteFilePath);
    }

    public void uploadSingleFile(FTPClient ftpClient, String localFilePath, String remoteFilePath) throws IOException {
        File localFile = new File(localFilePath);
        try (InputStream inputStream = new FileInputStream(localFile)) {
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            boolean uploaded = ftpClient.storeFile(remoteFilePath, inputStream);
            if (uploaded) {
                log.info(aMarker, "Uploaded a file to: {}", remoteFilePath);
            } else {
                log.info(aMarker, "COULD NOT upload the file: {} ", localFilePath);
            }
        }
    }
    private LocalDateTime convertTimestampToLocalDateTime(long timestamp) {
        Instant instant = Instant.ofEpochMilli(timestamp);
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }

}

