package in.handyman.raven.lib;

import in.handyman.raven.lambda.access.ResourceAccess;
import in.handyman.raven.lambda.action.ActionExecution;
import in.handyman.raven.lambda.action.IActionExecution;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.FtpConnectionCheck;
import in.handyman.raven.lib.model.FtpConnectionCheckTable;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.argument.Arguments;
import org.jdbi.v3.core.argument.NullArgument;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Types;
import java.time.LocalDateTime;

/**
 * Auto Generated By Raven
 */
@ActionExecution(
        actionName = "FtpConnectionCheck"
)
public class FtpConnectionCheckAction implements IActionExecution {
    private final ActionExecutionAudit action;

    private final Logger log;

    private final FtpConnectionCheck ftpConnectionCheck;

    private final Marker aMarker;

    public FtpConnectionCheckAction(final ActionExecutionAudit action, final Logger log,
                                    final Object ftpConnectionCheck) {
        this.ftpConnectionCheck = (FtpConnectionCheck) ftpConnectionCheck;
        this.action = action;
        this.log = log;
        this.aMarker = MarkerFactory.getMarker(" FtpConnectionCheck:" + this.ftpConnectionCheck.getName());
    }

    @Override
    public void execute() throws Exception {

        final Jdbi jdbi = ResourceAccess.rdbmsJDBIConn(ftpConnectionCheck.getResourceConn());
        jdbi.getConfig(Arguments.class).setUntypedNullArgument(new NullArgument(Types.NULL));
        final String insertQuery = "INSERT INTO " + ftpConnectionCheck.getResultTable() +
                "(tenant_id, root_pipeline_id, created_user_id, last_updated_on, created_on, last_updated_user_id, status, message, type, info, last_processed_on, is_ftp_connected, version) " +
                "VALUES (:tenantId, :rootPipelineId, :createdUserId, :lastUpdatedOn, :createdOn, :lastUpdatedUserId, :status, :message, :type, :info, :lastProcessedOn, :ftpConnected, :version)";

        log.info(aMarker, "ftpConnectionCheck Insert query {}", insertQuery);

        final String tenantId = action.getContext().get("tenant_id");
        final String remoteHost = action.getContext().get("ftpHost");
        final Long rootPipelineId = action.getRootPipelineId();
        final String userName = action.getContext().get("ftpUserName");
        final String password = action.getContext().get("ftpPassword");
        final String remotePort = action.getContext().get("ftpPort");
        final String destDir = action.getContext().get("ftpSourceDir");

        FTPClient ftpClient = new FTPClient();
        // Check the FTP connection using credentials (username and password)
        boolean isFtpConnected = checkFtpConnection(userName, password, remoteHost, remotePort, ftpClient);
        boolean isDirectoryAccessOk = checkDirectoryAccess(destDir, ftpClient);
        String ftpConnectionMessage = isFtpConnected ? "FTP connection successful" : "FTP connection failed";
        String directoryAccessMessage = isDirectoryAccessOk ? "Directory access successful" : "Directory access failed";


        FtpConnectionCheckTable ftpConnectionCheckTable = new FtpConnectionCheckTable();
        ftpConnectionCheckTable.setTenantId(Long.valueOf(tenantId));
        ftpConnectionCheckTable.setRootPipelineId(rootPipelineId);
        ftpConnectionCheckTable.setCreatedOn(LocalDateTime.now());
        ftpConnectionCheckTable.setCreatedUserId(Long.valueOf(tenantId));
        ftpConnectionCheckTable.setLastUpdatedUserId(Long.valueOf(tenantId));
        ftpConnectionCheckTable.setLastUpdatedOn(LocalDateTime.now());
        ftpConnectionCheckTable.setStatus("ACTIVE");
        ftpConnectionCheckTable.setMessage((isFtpConnected && isDirectoryAccessOk) ? "FTP connected and directory access successful" :
                "FTP connection or directory access failed. " + ftpConnectionMessage + ". " + directoryAccessMessage); // Matches 'message' in the database
        ftpConnectionCheckTable.setType("FTP");
        ftpConnectionCheckTable.setInfo("FTP INFO");
        ftpConnectionCheckTable.setLastProcessedOn(LocalDateTime.now());
        ftpConnectionCheckTable.setFtpConnected(isFtpConnected && isDirectoryAccessOk);

        try (Handle handle = jdbi.open()) {
            handle.createUpdate(insertQuery)
                    .bindBean(ftpConnectionCheckTable)
                    .execute();
            log.info(aMarker, "Data inserted successfully");
        } catch (Exception e) {
            log.error("Error inserting data: " + e.getMessage());
        }
    }


    private boolean checkDirectoryAccess(String destDir, FTPClient ftpClient) throws IOException {
        ftpClient.enterLocalPassiveMode();
        try {
            return ftpClient.changeWorkingDirectory(destDir);
        }
        catch (Exception e) {
            return false;
        }
        finally {
            ftpClient.disconnect();
        }
    }


    private boolean checkFtpConnection(String userName, String password, String remoteHost, String remotePort, FTPClient ftpClient) {

        try {
            // Connect to the FTP server
            ftpClient.connect(remoteHost);
            int reply = ftpClient.getReplyCode();

            if (!FTPReply.isPositiveCompletion(reply)) {
                // Connection failed
                log.error("FTP connection failed: FTP server reply code is not positive.");
                return false;
            }

            // Login with the provided username and password
            if (ftpClient.login(userName, password)) {
                // Successfully logged in
                return true;
            }

            // Login failed
            log.error("FTP login failed: Invalid username or password.");
            return false;
        } catch (Exception e) {
            // Handle any exceptions (e.g., connection timeout)
            log.error("Error during FTP connection: " + e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean executeIf() throws Exception {
        return ftpConnectionCheck.getCondition();
    }
}
