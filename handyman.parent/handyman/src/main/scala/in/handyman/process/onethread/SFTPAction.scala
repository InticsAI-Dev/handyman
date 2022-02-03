package in.handyman.process.onethread

import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import com.typesafe.scalalogging.LazyLogging

import in.handyman.command.CommandProxy
import in.handyman.config.ConfigurationService
import in.handyman.util.ParameterisationEngine

class SFTPAction extends in.handyman.command.Action with LazyLogging {

  val detailMap = new java.util.HashMap[String, String]
  var channelSftp : ChannelSftp = null
  def execute(context: in.handyman.command.Context, action: in.handyman.dsl.Action, actionId: Integer): in.handyman.command.Context = {

    //Setting up the proxy for retrieving configuration for the macro
    val sftpAsIs: in.handyman.dsl.SFTP = action.asInstanceOf[in.handyman.dsl.SFTP]
    val sftp: in.handyman.dsl.SFTP = CommandProxy.createProxy(sftpAsIs, classOf[in.handyman.dsl.SFTP], context)

    //Retrieving the global config map for default value
    val configMap = ConfigurationService.getGlobalconfig()

    val instanceId = context.getValue("process-id")

    val name = sftp.getName
    var localDir = sftp.getLocalDir
    val localFile = sftp.getLocalFile
    val remoteDir = sftp.getRemoteDir
    val sftpAction = sftp.getAction
    val userName = sftp.getUsername
    val password = sftp.getPassword
    val host = sftp.getHost
    val port: Int = Integer.valueOf(sftp.getPort)
    val remoteFile = sftp.getRemoteFile
    val remote = remoteDir.concat(remoteFile)
    val local = localDir.concat(localFile)
    val knownHosts = sftp.getKnownHosts

    detailMap.put("name", name)
    detailMap.put("localDir", localDir)
    detailMap.put("localFile", localFile)
    detailMap.put("remoteDir", remoteDir)
    detailMap.put("sftpAction", sftpAction)
    detailMap.put("userName", userName)
    detailMap.put("password", password)
    detailMap.put("host", host)
    detailMap.put("port", sftp.getPort)
    detailMap.put("remoteFile", remoteFile)

    try {
      channelSftp = setupJsch(userName, password, host, knownHosts)
      channelSftp.connect();

      sftpAction match {
        case "downloadFile" => {
            downloadFile(remote, local)
        }
        case "uploadFile" => {
            uploadFile(local, remote)
        }
      }

    } finally {
      if(channelSftp != null) {
        channelSftp.disconnect()
      }
    }
    System.out.println("ih")
    context
  }

  def setupJsch(username : String, password : String, remoteHost : String, knownHosts : String) : ChannelSftp = {
    var jsch : JSch = new JSch();
    jsch.setKnownHosts(knownHosts);
    var jschSession : Session = jsch.getSession(username, remoteHost);
    jschSession.setPassword(password);
    jschSession.connect();
    return jschSession.openChannel("sftp").asInstanceOf[ChannelSftp];
  }

  def downloadFile(remote: String, local: String) {
    channelSftp.get(remote, local);

    channelSftp.exit()
    logger.info(s"SFTP file ${remote} downloaded successfully in local ${local}");
  }

  def uploadFile(local: String, remote : String) = {
    channelSftp.put(local, remote);
 
    channelSftp.exit()
    logger.info(s"Local file ${local} uploaded successfully in SFTP ${remote}");
  }

  def executeIf(context: in.handyman.command.Context, action: in.handyman.dsl.Action): Boolean =
    {
      val sftpAsIs: in.handyman.dsl.SFTP = action.asInstanceOf[in.handyman.dsl.SFTP]
      val sftp: in.handyman.dsl.SFTP = CommandProxy.createProxy(sftpAsIs, classOf[in.handyman.dsl.SFTP], context)

      val expression = sftp.getCondition
      try {
        val output = ParameterisationEngine.doYieldtoTrue(expression)
        detailMap.putIfAbsent("condition-output", output.toString())
        output
      } finally {
        if (expression != null)
          detailMap.putIfAbsent("condition", "LHS=" + expression.getLhs + ", Operator=" + expression.getOperator + ", RHS=" + expression.getRhs)
      }

    }

  def generateAudit(): java.util.Map[String, String] = {
    detailMap
  }

}