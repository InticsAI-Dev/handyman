package in.handyman.process.onethread

import in.handyman.command.Action

object CommandFactory {

  def create(name: String): Action = {

    name.toLowerCase match {
      case "abort" => new AbortAction
      case "googlecal" => new GoogleCalendarAction
      case "transform" => new TransformAction
      case "assign" => new FetchVariableAction
      case "callprocess" => new CallProcessAction
      case "forkprocess" => new ForkProcessAction
      case "doozle" => new DoozleAction
      case "fbclead" => new FBLeadCreateAction
      case "fbformdownload" => new FBFormDownloadAction
      case "googlecontactput" => new GContactCreateAction
      case "googlecalput" => new GoogleCalendarAction
      case "fetch" => new FetchVariableAction
      case "smsleadsms" => new SmsLeadsAction
      case "sendmail" => new MailAction
      case "firebasedatabaseput" => new FirebaseDbUpdateAction
      case "firebasereactivenotification" => new FirebaseMessagingAction
      case "execjava" => new JavaAction
      case "dropbox" => new DropboxAction
      case "s3" => new S3Action
      case "extract" => new ExtractAction
      case "checksum" => new ChecksumAction
      case "dropbox" => new DropboxAction
      case "s3" => new S3Action
      case "extract" => new ExtractAction
      case "checksum" => new ChecksumAction
      case "loadcsv" => new LoadCsvIntoDbAction
      case "deletefolder" => new DeleteFolderAction
      case "mail" => new EMailAction
      case "targeneration" => new TarGenerationAction
      case "modelgeneration" => new ModelGenerationAction
      case "mahout" => new ModelTrainingAction
      case "copydata" => new CopydataAction
      case "terminal" => new TerminalAction
      case "writecsv" => new WriteCsvAndTsvAction
      case "nlptextpreprocessing" => new NLPTextPreprocessingAction
      case "dropboxcleanup" => new DropboxCleanupAction
      case "deletefolder" => new DeleteFolderAction
      case "mail" => new EMailAction
      case "targeneration" => new TarGenerationAction
      case "modelgeneration" => new ModelGenerationAction
      case "mahout" => new ModelTrainingAction
      case "terminal" => new TerminalAction
      case "writecsv" => new WriteCsvAndTsvAction
      case "nlptextpreprocessing" => new NLPTextPreprocessingAction
      case "dropboxcleanup" => new DropboxCleanupAction
      case "mongo2db" => new Mongo2DbAction
      case "sharepoint" => new SharePointAction
      case "ftp" => new FTPAction
      case "sftp" => new SFTPAction
    }
  }
}