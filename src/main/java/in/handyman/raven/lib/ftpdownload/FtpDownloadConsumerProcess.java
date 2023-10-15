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
import java.time.LocalDateTime;
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
            final String tenantId = action.getContext().get("tenant");
            final String destDir = action.getContext().get("destinationPath");
            //ftpClient.setEndpointCheckingEnabled(false);
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
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            ftpClient.enterLocalPassiveMode();
            ftpClient.setUseEPSVwithIPv4(true);
            //ftpClient.execPBSZ(0);
            //ftpClient.execPROT("P");
            System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");


//            ftpClient.connect(serverAddress);
//            ftpClient.login(username, password);
//            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            String workingDirectory = ftpClient.printWorkingDirectory();
            log.info(aMarker, "Current directory is {}", workingDirectory);
            ftpDownloadDirectory(ftpClient, filePath, destDir);
            FTPFile[] files = ftpClient.listFiles(filePath);
            for (FTPFile file : files) {
                if (file.isFile()) {
                    String remoteFilePath = filePath + "/" + file.getName();
                    String savePath = destDir + File.separator + file.getName();
                    downloadSingleFile(ftpClient, remoteFilePath, savePath);

                    FtpDownloadOutputTable outputTable = FtpDownloadOutputTable.builder()
                            .tenantId(Long.parseLong(tenantId))
                            .rootPipelineId(Long.parseLong(action.getContext().get("rootPipelineId"))) // Adjust as needed
                            .createdDate(LocalDateTime.now())
                            .createdBy(1L) // Set as needed
                            .lastModifiedBy(1L) // Set as needed
                            .lastModifiedDate(LocalDateTime.now())
                            .status("Completed") // Set the status as needed
                            .message("FTP download completed")
                            .type("FTP") // Set as needed
                            .lastProcessedOn(LocalDateTime.now())
                            .ftpFolderPath(filePath) // Set the FTP folder path
                            .destinationPath(destDir) // Set the destination path
                            .filePath("path_to_downloaded_file") // Set the downloaded file path
                            .build();

                    parentObj.add(outputTable);
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
    public void ftpDownloadDirectory(FTPClient ftpClient, String srcFile, String saveDir) throws IOException {
        FTPFile[] subFiles = ftpClient.listFiles(srcFile);
        if (subFiles != null) {
            for (FTPFile aFile : subFiles) {

                String currentFileName = aFile.getName();
                if (currentFileName.equals(".") || currentFileName.equals("..")) continue;
                String newDirPath = saveDir + File.separator + currentFileName;
                if (aFile.isFile())
                    downloadSingleFile(ftpClient, srcFile, newDirPath);
            }
        }
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
            if (success) log.info(aMarker, "Download the file {} ", remoteFilePath);
            else log.info(aMarker, "Couldn't download the file: {} ", remoteFilePath);
        }
    }

}
