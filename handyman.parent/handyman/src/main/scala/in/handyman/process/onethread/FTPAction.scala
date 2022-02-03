package in.handyman.process.onethread

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.security.MessageDigest
import java.util.ArrayList

import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPFile

import com.typesafe.scalalogging.LazyLogging

import in.handyman.command.CommandProxy
import in.handyman.config.ConfigurationService
import in.handyman.util.ParameterisationEngine

/**
 * TODO - Still need to add more rich ness to audit trail with respect to statement warnings
 * "put", "get", "del", "list", "chmod", "mkdir", "rmdir"
 */
class FTPAction extends in.handyman.command.Action with LazyLogging {

  val detailMap = new java.util.HashMap[String, String]
  val client = new FTPClient

  def execute(context: in.handyman.command.Context, action: in.handyman.dsl.Action, actionId: Integer): in.handyman.command.Context = {

    //Setting up the proxy for retrieving configuration for the macro
    val ftpAsIs: in.handyman.dsl.FTP = action.asInstanceOf[in.handyman.dsl.FTP]
    val ftp: in.handyman.dsl.FTP = CommandProxy.createProxy(ftpAsIs, classOf[in.handyman.dsl.FTP], context)

    //Retrieving the global config map for default value
    val configMap = ConfigurationService.getGlobalconfig()

    val instanceId = context.getValue("process-id")

    val name = ftp.getName
    var localDir = ftp.getLocalDir
    val localFile = ftp.getLocalFile
    val remoteDir = ftp.getRemoteDir
    val ftpAction = ftp.getAction
    val userName = ftp.getUsername
    val password = ftp.getPassword
    val host = ftp.getHost
    val port: Int = Integer.valueOf(ftp.getPort)
    val remoteFile = ftp.getRemoteFile
    val remote = remoteDir.concat(remoteFile)
    val local = localDir.concat(localFile)

    detailMap.put("name", name)
    detailMap.put("localDir", localDir)
    detailMap.put("localFile", localFile)
    detailMap.put("remoteDir", remoteDir)
    detailMap.put("ftpAction", ftpAction)
    detailMap.put("userName", userName)
    detailMap.put("password", password)
    detailMap.put("host", host)
    detailMap.put("port", ftp.getPort)
    detailMap.put("remoteFile", remoteFile)

    try {
      connect(host)
      login(userName, password)

      ftpAction match {
        case "listFiles" => {
          cd(remoteDir)
          listFiles
        }
        case "listFileNames" => {
          cd(remoteDir)
        }
        case "downloadFile" => {
          if (remoteFile != null && !remoteFile.isEmpty()) {
            downloadFile(remote, localDir,localFile)

            var fileList: Array[String] = new Array[String](1);
            fileList(0) = remoteFile
          } else {
            cd(remoteDir)
            downloadAllFiles(localDir)
          }
        }
        case "deleteFile" => deleteFile(remote)
        case "removeDir" => removeDir(remote)
        case "cd" => cd(remoteDir)
        case "md" => md(remoteDir)
        case "uploadFile" => {
          cd(remoteDir)

          if (localFile != null && !localFile.isEmpty()) {
            uploadFile(local)

            var fileList: Array[String] = new Array[String](1);
            fileList(0) = localFile
          } else {
            val localDirFile: File = new File(localDir);
            val localFiles: Array[File] = localDirFile.listFiles();
            var localFileNames: Array[String] = new Array[String](localFiles.size);

            var i: Int = 0;
            localFiles.foreach(lFile => {
              if (lFile.isFile())
                uploadFile(localDir.concat(lFile.getName))
              localFileNames(i) = lFile.getName;
              i = i + 1
            })
          }
        }
      }

    } finally {
      client.disconnect()
    }
    context
  }

  def login(username: String, password: String) = {
    client.login(username, password)
  }

  def connect(host: String) = {
    client.connect(host)
  }

  def connected: Boolean = client.isConnected

  def disconnect(): Unit = {
    if (connected) {
      client.logout()
      client.disconnect()
    }
  }

  def canConnect(host: String): Boolean = {
    client.connect(host)
    val connectionWasEstablished = connected
    client.disconnect()
    connectionWasEstablished
  }

  def listFiles(): List[FTPFile] =
    client.listFiles.toList

  def listFileNames(): Array[String] = {
    val ftpFiles: List[FTPFile] = listFiles()
    val ftpFileNames: Array[String] = new Array[String](ftpFiles.size);

    var i: Int = 0;
    for (fFile <- ftpFiles) {
      if (fFile.isFile()) {
        ftpFileNames(i) = fFile.getName;
        i = i + 1;
      }
    }

    ftpFileNames
  }

  def cd(path: String): Boolean =
    client.changeWorkingDirectory(path)

  def md(path: String): Boolean =
    client.makeDirectory(path)

  def filesInCurrentDirectory: Seq[String] =
    listFiles().map(_.getName)

  def downloadFileStream(remote: String): InputStream = {
    val stream = client.retrieveFileStream(remote)
    client.completePendingCommand()
    stream
  }

  def findChecksum(digest: MessageDigest, remote: String): String = {
    val stream = client.retrieveFileStream(remote)
    val byteArray: Array[Byte] = new Array[Byte](1024);
    var bytesCount: Int = 0;

    //Read file data and update in message digest
    bytesCount = stream.read(byteArray);
    while (bytesCount > 0) {
      digest.update(byteArray, 0, bytesCount)
      bytesCount = stream.read(byteArray);
    }

    //close the stream; We don't need it now.
    stream.close();

    //Get the hash's bytes
    val bytes: Array[Byte] = digest.digest();

    //This bytes[] has bytes in decimal format;
    //Convert it to hexadecimal format
    var sb: StringBuilder = new StringBuilder();
    bytes.foreach(byt => sb.append(Integer.toString((byt & 0xff) + 0x100, 16).substring(1)))

    //return complete hash
    sb.toString();
  }

  def downloadAllFiles(localDir: String) = {
    val ftpFiles: List[FTPFile] = listFiles()
    val ftpFileNames: ArrayList[String] = new ArrayList[String];

    for (fFile <- ftpFiles) {
      if (fFile.isFile()) {
        val outfile: File = new File(localDir + "/" + fFile.getName());
        outfile.createNewFile();

        val output: FileOutputStream = new FileOutputStream(outfile);

        client.retrieveFile(fFile.getName(), output);
        output.close();

        logger.info(s"File " + fFile.getName() + " Download Successfull");
      }
    }
  }

  def deleteFile(remote: String): Boolean = {
    client.deleteFile(remote)
  }

  def removeDir(remote: String): Boolean = {
    client.removeDirectory(remote)
  }

  def downloadFile(remote: String, localdir: String, localFile: String) {

    val f: File = new File(localdir)
    val local = localdir.concat(localFile)
    if (!f.exists()) {
      logger.info(s"Directory does not exist");
      f.mkdir();
    }

    val os = new FileOutputStream(new File(local))

    if (client.retrieveFile(remote, os)) {
      logger.info(s"File download Successfull");
    }

    os.close()
  }

  def uploadFile(localFileFullName: String) = {
    var localFile: File = new File(localFileFullName);
    var input: FileInputStream = new FileInputStream(localFile);

    if (client.storeFile(localFile.getName(), input)) {
      logger.info(s"File Upload Successfull");
    }

    input.close();
  }

  def executeIf(context: in.handyman.command.Context, action: in.handyman.dsl.Action): Boolean =
    {
      val ftpAsIs: in.handyman.dsl.FTP = action.asInstanceOf[in.handyman.dsl.FTP]
      val ftp: in.handyman.dsl.FTP = CommandProxy.createProxy(ftpAsIs, classOf[in.handyman.dsl.FTP], context)

      val expression = ftp.getCondition
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