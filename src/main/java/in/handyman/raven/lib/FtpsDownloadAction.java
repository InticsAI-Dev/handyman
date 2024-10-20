package in.handyman.raven.lib;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.lambda.action.ActionExecution;
import in.handyman.raven.lambda.action.IActionExecution;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.FtpsDownload;
import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPSClient;
import org.apache.commons.net.util.TrustManagerUtils;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

/**
 * Auto Generated By Raven
 */
@ActionExecution(
        actionName = "FtpsDownload"
)
public class FtpsDownloadAction implements IActionExecution {
    private final ActionExecutionAudit action;

    private final Logger log;

    private final FtpsDownload ftpsDownload;

    private final Marker aMarker;
    private final ObjectMapper mapper = new ObjectMapper();


    public FtpsDownloadAction(final ActionExecutionAudit action, final Logger log,
                              final Object ftpsDownload) {
        this.ftpsDownload = (FtpsDownload) ftpsDownload;
        this.action = action;
        this.log = log;
        this.aMarker = MarkerFactory.getMarker(" FtpsDownload:" + this.ftpsDownload.getName());
    }

    @Override
    public void execute() throws Exception {
        final String remoteHost = ftpsDownload.getHost();
        final String userName = ftpsDownload.getUserName();
        final String password = ftpsDownload.getPassword();
        final int remotePort = Integer.parseInt(ftpsDownload.getPort());
        final int sessionTimeout = Integer.parseInt(ftpsDownload.getSessionTimeOut());
        final String destDir = ftpsDownload.getDestDir();
        final String remoteFile = ftpsDownload.getSourceFile();
        final String check = ftpsDownload.getUploadCheck();
        if (Boolean.parseBoolean(check)) {
            log.info(aMarker, "Got the sftp details for the host {} and user {}", remoteHost, userName);
            FTPSClient ftpClient = new FTPSClient();
            try {
                ftpClient.setEndpointCheckingEnabled(false);
                ftpClient.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));
                int reply;
                ftpClient.setConnectTimeout(sessionTimeout);
                ftpClient.connect(remoteHost, remotePort);
                log.info(aMarker, "FTP URL is: {} ", ftpClient.getDefaultPort());
                reply = ftpClient.getReplyCode();
                if (!FTPReply.isPositiveCompletion(reply)) {
                    ftpClient.disconnect();
                    log.error("Exception in connecting to FTP Server");
                }
                ftpClient.login(userName, password);
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                ftpClient.enterLocalPassiveMode();
                ftpClient.setUseEPSVwithIPv4(true);
                ftpClient.execPBSZ(0);
                ftpClient.execPROT("P");
                System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");
                ftpClient.setTrustManager(TrustManagerUtils.getAcceptAllTrustManager());
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                log.info(aMarker, "Remote system is  {}", (Object) ftpClient.getEnabledCipherSuites());
                log.info(aMarker, "SSL: {}", ftpClient.getEnableSessionCreation());
                log.info(aMarker, "Remote system is {} ", ftpClient.getSystemType());
                String workingDirectory = ftpClient.printWorkingDirectory();
                log.info(aMarker, "Current directory is {}", workingDirectory);
                ftpDownloadDirectory(ftpClient, remoteFile, destDir);
                String name = "ftps-file-download-connector-response";
                action.getContext().put(name, destDir);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (ftpClient.isConnected()) {
                    ftpClient.logout();
                    ftpClient.disconnect();
                }
            }
        } else
            log.info(aMarker, "Download flag is set to {}", check);

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
//
//    private void fileIteration(FTPClient ftpClient, String saveDir, FTPFile[] subFiles) throws IOException {
//        if (subFiles != null && subFiles.length > 0) {
//            subFilesProcessing(ftpClient, saveDir,  subFiles);
//        }
//    }
//
//    private void subFilesProcessing(FTPClient ftpClient, String parentDir, String currentDir, String saveDir, String dirToList, FTPFile[] subFiles) throws IOException {
//        for (FTPFile aFile : subFiles) {
//            String currentFileName = aFile.getName();
//            if (currentFileName.equals(".") || currentFileName.equals("..")) continue;
//            String filePath = parentDir + File.separator + currentDir + File.separator + currentFileName;
//            String newDirPath = saveDir + parentDir + File.separator + currentDir + File.separator + currentFileName;
//            if (currentDir.equals("")){
//                filePath = parentDir + File.separator + currentFileName;
//                newDirPath = saveDir + parentDir + File.separator + currentFileName;
//            }
//            if(aFile.isFile()){
//                filePath = parentDir;
//                newDirPath = saveDir + parentDir;
//            }
//            if (aFile.isDirectory())
//                createDirectoryForSubFilesAndIterate(ftpClient, saveDir, dirToList, currentFileName, newDirPath);
//            else downloadSingleFile(ftpClient, filePath, newDirPath);
//        }
//    }

    private void createDirectoryForSubFilesAndIterate(FTPClient ftpClient, String saveDir, String dirToList, String currentFileName, String newDirPath) throws IOException {
        File newDir = new File(newDirPath);
        boolean created = newDir.mkdirs();
        if (created) log.info(aMarker, "Created the directory: {} ", newDirPath);
        else log.info(aMarker, "Couldn't create the directory: {} ", newDirPath);
//        ftpDownloadDirectory(ftpClient, dirToList, currentFileName, saveDir);
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


    @Override
    public boolean executeIf() throws Exception {
        return ftpsDownload.getCondition();
    }
}
