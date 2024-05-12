// Generated from java-escape by ANTLR 4.11.1

package in.handyman.raven.compiler;

import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link RavenParser}.
 */
public interface RavenListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link RavenParser#process}.
	 * @param ctx the parse tree
	 */
	void enterProcess(RavenParser.ProcessContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#process}.
	 * @param ctx the parse tree
	 */
	void exitProcess(RavenParser.ProcessContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#tryClause}.
	 * @param ctx the parse tree
	 */
	void enterTryClause(RavenParser.TryClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#tryClause}.
	 * @param ctx the parse tree
	 */
	void exitTryClause(RavenParser.TryClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#finallyClause}.
	 * @param ctx the parse tree
	 */
	void enterFinallyClause(RavenParser.FinallyClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#finallyClause}.
	 * @param ctx the parse tree
	 */
	void exitFinallyClause(RavenParser.FinallyClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#catchClause}.
	 * @param ctx the parse tree
	 */
	void enterCatchClause(RavenParser.CatchClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#catchClause}.
	 * @param ctx the parse tree
	 */
	void exitCatchClause(RavenParser.CatchClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#action}.
	 * @param ctx the parse tree
	 */
	void enterAction(RavenParser.ActionContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#action}.
	 * @param ctx the parse tree
	 */
	void exitAction(RavenParser.ActionContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#multitude}.
	 * @param ctx the parse tree
	 */
	void enterMultitude(RavenParser.MultitudeContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#multitude}.
	 * @param ctx the parse tree
	 */
	void exitMultitude(RavenParser.MultitudeContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#copyData}.
	 * @param ctx the parse tree
	 */
	void enterCopyData(RavenParser.CopyDataContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#copyData}.
	 * @param ctx the parse tree
	 */
	void exitCopyData(RavenParser.CopyDataContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#transform}.
	 * @param ctx the parse tree
	 */
	void enterTransform(RavenParser.TransformContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#transform}.
	 * @param ctx the parse tree
	 */
	void exitTransform(RavenParser.TransformContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#loadCsv}.
	 * @param ctx the parse tree
	 */
	void enterLoadCsv(RavenParser.LoadCsvContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#loadCsv}.
	 * @param ctx the parse tree
	 */
	void exitLoadCsv(RavenParser.LoadCsvContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#abort}.
	 * @param ctx the parse tree
	 */
	void enterAbort(RavenParser.AbortContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#abort}.
	 * @param ctx the parse tree
	 */
	void exitAbort(RavenParser.AbortContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#callProcess}.
	 * @param ctx the parse tree
	 */
	void enterCallProcess(RavenParser.CallProcessContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#callProcess}.
	 * @param ctx the parse tree
	 */
	void exitCallProcess(RavenParser.CallProcessContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#forkProcess}.
	 * @param ctx the parse tree
	 */
	void enterForkProcess(RavenParser.ForkProcessContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#forkProcess}.
	 * @param ctx the parse tree
	 */
	void exitForkProcess(RavenParser.ForkProcessContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#spawnProcess}.
	 * @param ctx the parse tree
	 */
	void enterSpawnProcess(RavenParser.SpawnProcessContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#spawnProcess}.
	 * @param ctx the parse tree
	 */
	void exitSpawnProcess(RavenParser.SpawnProcessContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#dogLeg}.
	 * @param ctx the parse tree
	 */
	void enterDogLeg(RavenParser.DogLegContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#dogLeg}.
	 * @param ctx the parse tree
	 */
	void exitDogLeg(RavenParser.DogLegContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#startProcess}.
	 * @param ctx the parse tree
	 */
	void enterStartProcess(RavenParser.StartProcessContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#startProcess}.
	 * @param ctx the parse tree
	 */
	void exitStartProcess(RavenParser.StartProcessContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#assign}.
	 * @param ctx the parse tree
	 */
	void enterAssign(RavenParser.AssignContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#assign}.
	 * @param ctx the parse tree
	 */
	void exitAssign(RavenParser.AssignContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#dropFile}.
	 * @param ctx the parse tree
	 */
	void enterDropFile(RavenParser.DropFileContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#dropFile}.
	 * @param ctx the parse tree
	 */
	void exitDropFile(RavenParser.DropFileContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#restApi}.
	 * @param ctx the parse tree
	 */
	void enterRestApi(RavenParser.RestApiContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#restApi}.
	 * @param ctx the parse tree
	 */
	void exitRestApi(RavenParser.RestApiContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#restPart}.
	 * @param ctx the parse tree
	 */
	void enterRestPart(RavenParser.RestPartContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#restPart}.
	 * @param ctx the parse tree
	 */
	void exitRestPart(RavenParser.RestPartContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#exportCsv}.
	 * @param ctx the parse tree
	 */
	void enterExportCsv(RavenParser.ExportCsvContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#exportCsv}.
	 * @param ctx the parse tree
	 */
	void exitExportCsv(RavenParser.ExportCsvContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#importCsvToDB}.
	 * @param ctx the parse tree
	 */
	void enterImportCsvToDB(RavenParser.ImportCsvToDBContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#importCsvToDB}.
	 * @param ctx the parse tree
	 */
	void exitImportCsvToDB(RavenParser.ImportCsvToDBContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#extractTAR}.
	 * @param ctx the parse tree
	 */
	void enterExtractTAR(RavenParser.ExtractTARContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#extractTAR}.
	 * @param ctx the parse tree
	 */
	void exitExtractTAR(RavenParser.ExtractTARContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#createTAR}.
	 * @param ctx the parse tree
	 */
	void enterCreateTAR(RavenParser.CreateTARContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#createTAR}.
	 * @param ctx the parse tree
	 */
	void exitCreateTAR(RavenParser.CreateTARContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#createDirectory}.
	 * @param ctx the parse tree
	 */
	void enterCreateDirectory(RavenParser.CreateDirectoryContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#createDirectory}.
	 * @param ctx the parse tree
	 */
	void exitCreateDirectory(RavenParser.CreateDirectoryContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#createFile}.
	 * @param ctx the parse tree
	 */
	void enterCreateFile(RavenParser.CreateFileContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#createFile}.
	 * @param ctx the parse tree
	 */
	void exitCreateFile(RavenParser.CreateFileContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#deleteFileDirectory}.
	 * @param ctx the parse tree
	 */
	void enterDeleteFileDirectory(RavenParser.DeleteFileDirectoryContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#deleteFileDirectory}.
	 * @param ctx the parse tree
	 */
	void exitDeleteFileDirectory(RavenParser.DeleteFileDirectoryContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#transferFileDirectory}.
	 * @param ctx the parse tree
	 */
	void enterTransferFileDirectory(RavenParser.TransferFileDirectoryContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#transferFileDirectory}.
	 * @param ctx the parse tree
	 */
	void exitTransferFileDirectory(RavenParser.TransferFileDirectoryContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#producerConsumerModel}.
	 * @param ctx the parse tree
	 */
	void enterProducerConsumerModel(RavenParser.ProducerConsumerModelContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#producerConsumerModel}.
	 * @param ctx the parse tree
	 */
	void exitProducerConsumerModel(RavenParser.ProducerConsumerModelContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#producer}.
	 * @param ctx the parse tree
	 */
	void enterProducer(RavenParser.ProducerContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#producer}.
	 * @param ctx the parse tree
	 */
	void exitProducer(RavenParser.ProducerContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#consumer}.
	 * @param ctx the parse tree
	 */
	void enterConsumer(RavenParser.ConsumerContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#consumer}.
	 * @param ctx the parse tree
	 */
	void exitConsumer(RavenParser.ConsumerContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#pushJson}.
	 * @param ctx the parse tree
	 */
	void enterPushJson(RavenParser.PushJsonContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#pushJson}.
	 * @param ctx the parse tree
	 */
	void exitPushJson(RavenParser.PushJsonContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#mapJsonContext}.
	 * @param ctx the parse tree
	 */
	void enterMapJsonContext(RavenParser.MapJsonContextContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#mapJsonContext}.
	 * @param ctx the parse tree
	 */
	void exitMapJsonContext(RavenParser.MapJsonContextContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#sharePoint}.
	 * @param ctx the parse tree
	 */
	void enterSharePoint(RavenParser.SharePointContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#sharePoint}.
	 * @param ctx the parse tree
	 */
	void exitSharePoint(RavenParser.SharePointContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterExpression(RavenParser.ExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitExpression(RavenParser.ExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#log}.
	 * @param ctx the parse tree
	 */
	void enterLog(RavenParser.LogContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#log}.
	 * @param ctx the parse tree
	 */
	void exitLog(RavenParser.LogContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#ravenVmException}.
	 * @param ctx the parse tree
	 */
	void enterRavenVmException(RavenParser.RavenVmExceptionContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#ravenVmException}.
	 * @param ctx the parse tree
	 */
	void exitRavenVmException(RavenParser.RavenVmExceptionContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#checksum}.
	 * @param ctx the parse tree
	 */
	void enterChecksum(RavenParser.ChecksumContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#checksum}.
	 * @param ctx the parse tree
	 */
	void exitChecksum(RavenParser.ChecksumContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#fileSize}.
	 * @param ctx the parse tree
	 */
	void enterFileSize(RavenParser.FileSizeContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#fileSize}.
	 * @param ctx the parse tree
	 */
	void exitFileSize(RavenParser.FileSizeContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#downloadAsset}.
	 * @param ctx the parse tree
	 */
	void enterDownloadAsset(RavenParser.DownloadAssetContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#downloadAsset}.
	 * @param ctx the parse tree
	 */
	void exitDownloadAsset(RavenParser.DownloadAssetContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#paperItemization}.
	 * @param ctx the parse tree
	 */
	void enterPaperItemization(RavenParser.PaperItemizationContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#paperItemization}.
	 * @param ctx the parse tree
	 */
	void exitPaperItemization(RavenParser.PaperItemizationContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#autoRotation}.
	 * @param ctx the parse tree
	 */
	void enterAutoRotation(RavenParser.AutoRotationContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#autoRotation}.
	 * @param ctx the parse tree
	 */
	void exitAutoRotation(RavenParser.AutoRotationContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#blankPageRemover}.
	 * @param ctx the parse tree
	 */
	void enterBlankPageRemover(RavenParser.BlankPageRemoverContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#blankPageRemover}.
	 * @param ctx the parse tree
	 */
	void exitBlankPageRemover(RavenParser.BlankPageRemoverContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#qrAttribution}.
	 * @param ctx the parse tree
	 */
	void enterQrAttribution(RavenParser.QrAttributionContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#qrAttribution}.
	 * @param ctx the parse tree
	 */
	void exitQrAttribution(RavenParser.QrAttributionContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#uploadAsset}.
	 * @param ctx the parse tree
	 */
	void enterUploadAsset(RavenParser.UploadAssetContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#uploadAsset}.
	 * @param ctx the parse tree
	 */
	void exitUploadAsset(RavenParser.UploadAssetContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#fileMerger}.
	 * @param ctx the parse tree
	 */
	void enterFileMerger(RavenParser.FileMergerContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#fileMerger}.
	 * @param ctx the parse tree
	 */
	void exitFileMerger(RavenParser.FileMergerContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#ftpsUpload}.
	 * @param ctx the parse tree
	 */
	void enterFtpsUpload(RavenParser.FtpsUploadContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#ftpsUpload}.
	 * @param ctx the parse tree
	 */
	void exitFtpsUpload(RavenParser.FtpsUploadContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#ftpsDownload}.
	 * @param ctx the parse tree
	 */
	void enterFtpsDownload(RavenParser.FtpsDownloadContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#ftpsDownload}.
	 * @param ctx the parse tree
	 */
	void exitFtpsDownload(RavenParser.FtpsDownloadContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#sftpConnector}.
	 * @param ctx the parse tree
	 */
	void enterSftpConnector(RavenParser.SftpConnectorContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#sftpConnector}.
	 * @param ctx the parse tree
	 */
	void exitSftpConnector(RavenParser.SftpConnectorContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#createZip}.
	 * @param ctx the parse tree
	 */
	void enterCreateZip(RavenParser.CreateZipContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#createZip}.
	 * @param ctx the parse tree
	 */
	void exitCreateZip(RavenParser.CreateZipContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#extractZip}.
	 * @param ctx the parse tree
	 */
	void enterExtractZip(RavenParser.ExtractZipContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#extractZip}.
	 * @param ctx the parse tree
	 */
	void exitExtractZip(RavenParser.ExtractZipContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#sorGroupDetails}.
	 * @param ctx the parse tree
	 */
	void enterSorGroupDetails(RavenParser.SorGroupDetailsContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#sorGroupDetails}.
	 * @param ctx the parse tree
	 */
	void exitSorGroupDetails(RavenParser.SorGroupDetailsContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#zeroShotClassifier}.
	 * @param ctx the parse tree
	 */
	void enterZeroShotClassifier(RavenParser.ZeroShotClassifierContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#zeroShotClassifier}.
	 * @param ctx the parse tree
	 */
	void exitZeroShotClassifier(RavenParser.ZeroShotClassifierContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#loadExtractedData}.
	 * @param ctx the parse tree
	 */
	void enterLoadExtractedData(RavenParser.LoadExtractedDataContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#loadExtractedData}.
	 * @param ctx the parse tree
	 */
	void exitLoadExtractedData(RavenParser.LoadExtractedDataContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#absentKeyFilter}.
	 * @param ctx the parse tree
	 */
	void enterAbsentKeyFilter(RavenParser.AbsentKeyFilterContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#absentKeyFilter}.
	 * @param ctx the parse tree
	 */
	void exitAbsentKeyFilter(RavenParser.AbsentKeyFilterContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#sorFilter}.
	 * @param ctx the parse tree
	 */
	void enterSorFilter(RavenParser.SorFilterContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#sorFilter}.
	 * @param ctx the parse tree
	 */
	void exitSorFilter(RavenParser.SorFilterContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#triageAttribution}.
	 * @param ctx the parse tree
	 */
	void enterTriageAttribution(RavenParser.TriageAttributionContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#triageAttribution}.
	 * @param ctx the parse tree
	 */
	void exitTriageAttribution(RavenParser.TriageAttributionContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#docnetAttribution}.
	 * @param ctx the parse tree
	 */
	void enterDocnetAttribution(RavenParser.DocnetAttributionContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#docnetAttribution}.
	 * @param ctx the parse tree
	 */
	void exitDocnetAttribution(RavenParser.DocnetAttributionContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#tqaFilter}.
	 * @param ctx the parse tree
	 */
	void enterTqaFilter(RavenParser.TqaFilterContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#tqaFilter}.
	 * @param ctx the parse tree
	 */
	void exitTqaFilter(RavenParser.TqaFilterContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#textFilter}.
	 * @param ctx the parse tree
	 */
	void enterTextFilter(RavenParser.TextFilterContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#textFilter}.
	 * @param ctx the parse tree
	 */
	void exitTextFilter(RavenParser.TextFilterContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#entityFilter}.
	 * @param ctx the parse tree
	 */
	void enterEntityFilter(RavenParser.EntityFilterContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#entityFilter}.
	 * @param ctx the parse tree
	 */
	void exitEntityFilter(RavenParser.EntityFilterContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#thresholdCheck}.
	 * @param ctx the parse tree
	 */
	void enterThresholdCheck(RavenParser.ThresholdCheckContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#thresholdCheck}.
	 * @param ctx the parse tree
	 */
	void exitThresholdCheck(RavenParser.ThresholdCheckContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#jsonToFile}.
	 * @param ctx the parse tree
	 */
	void enterJsonToFile(RavenParser.JsonToFileContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#jsonToFile}.
	 * @param ctx the parse tree
	 */
	void exitJsonToFile(RavenParser.JsonToFileContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#docnetResult}.
	 * @param ctx the parse tree
	 */
	void enterDocnetResult(RavenParser.DocnetResultContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#docnetResult}.
	 * @param ctx the parse tree
	 */
	void exitDocnetResult(RavenParser.DocnetResultContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#setContextValue}.
	 * @param ctx the parse tree
	 */
	void enterSetContextValue(RavenParser.SetContextValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#setContextValue}.
	 * @param ctx the parse tree
	 */
	void exitSetContextValue(RavenParser.SetContextValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#evalPatientName}.
	 * @param ctx the parse tree
	 */
	void enterEvalPatientName(RavenParser.EvalPatientNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#evalPatientName}.
	 * @param ctx the parse tree
	 */
	void exitEvalPatientName(RavenParser.EvalPatientNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#evalMemberId}.
	 * @param ctx the parse tree
	 */
	void enterEvalMemberId(RavenParser.EvalMemberIdContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#evalMemberId}.
	 * @param ctx the parse tree
	 */
	void exitEvalMemberId(RavenParser.EvalMemberIdContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#evalDateOfBirth}.
	 * @param ctx the parse tree
	 */
	void enterEvalDateOfBirth(RavenParser.EvalDateOfBirthContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#evalDateOfBirth}.
	 * @param ctx the parse tree
	 */
	void exitEvalDateOfBirth(RavenParser.EvalDateOfBirthContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#dirPath}.
	 * @param ctx the parse tree
	 */
	void enterDirPath(RavenParser.DirPathContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#dirPath}.
	 * @param ctx the parse tree
	 */
	void exitDirPath(RavenParser.DirPathContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#fileDetails}.
	 * @param ctx the parse tree
	 */
	void enterFileDetails(RavenParser.FileDetailsContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#fileDetails}.
	 * @param ctx the parse tree
	 */
	void exitFileDetails(RavenParser.FileDetailsContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#wordcount}.
	 * @param ctx the parse tree
	 */
	void enterWordcount(RavenParser.WordcountContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#wordcount}.
	 * @param ctx the parse tree
	 */
	void exitWordcount(RavenParser.WordcountContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#charactercount}.
	 * @param ctx the parse tree
	 */
	void enterCharactercount(RavenParser.CharactercountContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#charactercount}.
	 * @param ctx the parse tree
	 */
	void exitCharactercount(RavenParser.CharactercountContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#datevalidator}.
	 * @param ctx the parse tree
	 */
	void enterDatevalidator(RavenParser.DatevalidatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#datevalidator}.
	 * @param ctx the parse tree
	 */
	void exitDatevalidator(RavenParser.DatevalidatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#alphavalidator}.
	 * @param ctx the parse tree
	 */
	void enterAlphavalidator(RavenParser.AlphavalidatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#alphavalidator}.
	 * @param ctx the parse tree
	 */
	void exitAlphavalidator(RavenParser.AlphavalidatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#alphanumericvalidator}.
	 * @param ctx the parse tree
	 */
	void enterAlphanumericvalidator(RavenParser.AlphanumericvalidatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#alphanumericvalidator}.
	 * @param ctx the parse tree
	 */
	void exitAlphanumericvalidator(RavenParser.AlphanumericvalidatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#numericvalidator}.
	 * @param ctx the parse tree
	 */
	void enterNumericvalidator(RavenParser.NumericvalidatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#numericvalidator}.
	 * @param ctx the parse tree
	 */
	void exitNumericvalidator(RavenParser.NumericvalidatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#nervalidator}.
	 * @param ctx the parse tree
	 */
	void enterNervalidator(RavenParser.NervalidatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#nervalidator}.
	 * @param ctx the parse tree
	 */
	void exitNervalidator(RavenParser.NervalidatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#urgencyTriage}.
	 * @param ctx the parse tree
	 */
	void enterUrgencyTriage(RavenParser.UrgencyTriageContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#urgencyTriage}.
	 * @param ctx the parse tree
	 */
	void exitUrgencyTriage(RavenParser.UrgencyTriageContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#donutDocQa}.
	 * @param ctx the parse tree
	 */
	void enterDonutDocQa(RavenParser.DonutDocQaContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#donutDocQa}.
	 * @param ctx the parse tree
	 */
	void exitDonutDocQa(RavenParser.DonutDocQaContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#scalarAdapter}.
	 * @param ctx the parse tree
	 */
	void enterScalarAdapter(RavenParser.ScalarAdapterContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#scalarAdapter}.
	 * @param ctx the parse tree
	 */
	void exitScalarAdapter(RavenParser.ScalarAdapterContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#phraseMatchPaperFilter}.
	 * @param ctx the parse tree
	 */
	void enterPhraseMatchPaperFilter(RavenParser.PhraseMatchPaperFilterContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#phraseMatchPaperFilter}.
	 * @param ctx the parse tree
	 */
	void exitPhraseMatchPaperFilter(RavenParser.PhraseMatchPaperFilterContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#zeroShotClassifierPaperFilter}.
	 * @param ctx the parse tree
	 */
	void enterZeroShotClassifierPaperFilter(RavenParser.ZeroShotClassifierPaperFilterContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#zeroShotClassifierPaperFilter}.
	 * @param ctx the parse tree
	 */
	void exitZeroShotClassifierPaperFilter(RavenParser.ZeroShotClassifierPaperFilterContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#assetInfo}.
	 * @param ctx the parse tree
	 */
	void enterAssetInfo(RavenParser.AssetInfoContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#assetInfo}.
	 * @param ctx the parse tree
	 */
	void exitAssetInfo(RavenParser.AssetInfoContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#dataExtraction}.
	 * @param ctx the parse tree
	 */
	void enterDataExtraction(RavenParser.DataExtractionContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#dataExtraction}.
	 * @param ctx the parse tree
	 */
	void exitDataExtraction(RavenParser.DataExtractionContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#episodeOfCoverage}.
	 * @param ctx the parse tree
	 */
	void enterEpisodeOfCoverage(RavenParser.EpisodeOfCoverageContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#episodeOfCoverage}.
	 * @param ctx the parse tree
	 */
	void exitEpisodeOfCoverage(RavenParser.EpisodeOfCoverageContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#userRegistration}.
	 * @param ctx the parse tree
	 */
	void enterUserRegistration(RavenParser.UserRegistrationContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#userRegistration}.
	 * @param ctx the parse tree
	 */
	void exitUserRegistration(RavenParser.UserRegistrationContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#authToken}.
	 * @param ctx the parse tree
	 */
	void enterAuthToken(RavenParser.AuthTokenContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#authToken}.
	 * @param ctx the parse tree
	 */
	void exitAuthToken(RavenParser.AuthTokenContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#eocJsonGenerator}.
	 * @param ctx the parse tree
	 */
	void enterEocJsonGenerator(RavenParser.EocJsonGeneratorContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#eocJsonGenerator}.
	 * @param ctx the parse tree
	 */
	void exitEocJsonGenerator(RavenParser.EocJsonGeneratorContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#zipContentList}.
	 * @param ctx the parse tree
	 */
	void enterZipContentList(RavenParser.ZipContentListContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#zipContentList}.
	 * @param ctx the parse tree
	 */
	void exitZipContentList(RavenParser.ZipContentListContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#hwDetection}.
	 * @param ctx the parse tree
	 */
	void enterHwDetection(RavenParser.HwDetectionContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#hwDetection}.
	 * @param ctx the parse tree
	 */
	void exitHwDetection(RavenParser.HwDetectionContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#intellimatch}.
	 * @param ctx the parse tree
	 */
	void enterIntellimatch(RavenParser.IntellimatchContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#intellimatch}.
	 * @param ctx the parse tree
	 */
	void exitIntellimatch(RavenParser.IntellimatchContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#checkboxVqa}.
	 * @param ctx the parse tree
	 */
	void enterCheckboxVqa(RavenParser.CheckboxVqaContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#checkboxVqa}.
	 * @param ctx the parse tree
	 */
	void exitCheckboxVqa(RavenParser.CheckboxVqaContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#pixelClassifierUrgencyTriage}.
	 * @param ctx the parse tree
	 */
	void enterPixelClassifierUrgencyTriage(RavenParser.PixelClassifierUrgencyTriageContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#pixelClassifierUrgencyTriage}.
	 * @param ctx the parse tree
	 */
	void exitPixelClassifierUrgencyTriage(RavenParser.PixelClassifierUrgencyTriageContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#paperItemizer}.
	 * @param ctx the parse tree
	 */
	void enterPaperItemizer(RavenParser.PaperItemizerContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#paperItemizer}.
	 * @param ctx the parse tree
	 */
	void exitPaperItemizer(RavenParser.PaperItemizerContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#nerAdapter}.
	 * @param ctx the parse tree
	 */
	void enterNerAdapter(RavenParser.NerAdapterContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#nerAdapter}.
	 * @param ctx the parse tree
	 */
	void exitNerAdapter(RavenParser.NerAdapterContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#coproStart}.
	 * @param ctx the parse tree
	 */
	void enterCoproStart(RavenParser.CoproStartContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#coproStart}.
	 * @param ctx the parse tree
	 */
	void exitCoproStart(RavenParser.CoproStartContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#coproStop}.
	 * @param ctx the parse tree
	 */
	void enterCoproStop(RavenParser.CoproStopContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#coproStop}.
	 * @param ctx the parse tree
	 */
	void exitCoproStop(RavenParser.CoproStopContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#outboundDeliveryNotify}.
	 * @param ctx the parse tree
	 */
	void enterOutboundDeliveryNotify(RavenParser.OutboundDeliveryNotifyContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#outboundDeliveryNotify}.
	 * @param ctx the parse tree
	 */
	void exitOutboundDeliveryNotify(RavenParser.OutboundDeliveryNotifyContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#masterdataComparison}.
	 * @param ctx the parse tree
	 */
	void enterMasterdataComparison(RavenParser.MasterdataComparisonContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#masterdataComparison}.
	 * @param ctx the parse tree
	 */
	void exitMasterdataComparison(RavenParser.MasterdataComparisonContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#zipBatch}.
	 * @param ctx the parse tree
	 */
	void enterZipBatch(RavenParser.ZipBatchContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#zipBatch}.
	 * @param ctx the parse tree
	 */
	void exitZipBatch(RavenParser.ZipBatchContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#drugMatch}.
	 * @param ctx the parse tree
	 */
	void enterDrugMatch(RavenParser.DrugMatchContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#drugMatch}.
	 * @param ctx the parse tree
	 */
	void exitDrugMatch(RavenParser.DrugMatchContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#urgencyTriageModel}.
	 * @param ctx the parse tree
	 */
	void enterUrgencyTriageModel(RavenParser.UrgencyTriageModelContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#urgencyTriageModel}.
	 * @param ctx the parse tree
	 */
	void exitUrgencyTriageModel(RavenParser.UrgencyTriageModelContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#donutImpiraQa}.
	 * @param ctx the parse tree
	 */
	void enterDonutImpiraQa(RavenParser.DonutImpiraQaContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#donutImpiraQa}.
	 * @param ctx the parse tree
	 */
	void exitDonutImpiraQa(RavenParser.DonutImpiraQaContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#templateDetection}.
	 * @param ctx the parse tree
	 */
	void enterTemplateDetection(RavenParser.TemplateDetectionContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#templateDetection}.
	 * @param ctx the parse tree
	 */
	void exitTemplateDetection(RavenParser.TemplateDetectionContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#trinityModel}.
	 * @param ctx the parse tree
	 */
	void enterTrinityModel(RavenParser.TrinityModelContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#trinityModel}.
	 * @param ctx the parse tree
	 */
	void exitTrinityModel(RavenParser.TrinityModelContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#fileBucketing}.
	 * @param ctx the parse tree
	 */
	void enterFileBucketing(RavenParser.FileBucketingContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#fileBucketing}.
	 * @param ctx the parse tree
	 */
	void exitFileBucketing(RavenParser.FileBucketingContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#alchemyInfo}.
	 * @param ctx the parse tree
	 */
	void enterAlchemyInfo(RavenParser.AlchemyInfoContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#alchemyInfo}.
	 * @param ctx the parse tree
	 */
	void exitAlchemyInfo(RavenParser.AlchemyInfoContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#alchemyAuthToken}.
	 * @param ctx the parse tree
	 */
	void enterAlchemyAuthToken(RavenParser.AlchemyAuthTokenContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#alchemyAuthToken}.
	 * @param ctx the parse tree
	 */
	void exitAlchemyAuthToken(RavenParser.AlchemyAuthTokenContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#alchemyResponse}.
	 * @param ctx the parse tree
	 */
	void enterAlchemyResponse(RavenParser.AlchemyResponseContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#alchemyResponse}.
	 * @param ctx the parse tree
	 */
	void exitAlchemyResponse(RavenParser.AlchemyResponseContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#productResponse}.
	 * @param ctx the parse tree
	 */
	void enterProductResponse(RavenParser.ProductResponseContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#productResponse}.
	 * @param ctx the parse tree
	 */
	void exitProductResponse(RavenParser.ProductResponseContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#tableExtraction}.
	 * @param ctx the parse tree
	 */
	void enterTableExtraction(RavenParser.TableExtractionContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#tableExtraction}.
	 * @param ctx the parse tree
	 */
	void exitTableExtraction(RavenParser.TableExtractionContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#mailServer}.
	 * @param ctx the parse tree
	 */
	void enterMailServer(RavenParser.MailServerContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#mailServer}.
	 * @param ctx the parse tree
	 */
	void exitMailServer(RavenParser.MailServerContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#alchemyKvpResponse}.
	 * @param ctx the parse tree
	 */
	void enterAlchemyKvpResponse(RavenParser.AlchemyKvpResponseContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#alchemyKvpResponse}.
	 * @param ctx the parse tree
	 */
	void exitAlchemyKvpResponse(RavenParser.AlchemyKvpResponseContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#alchemyTableResponse}.
	 * @param ctx the parse tree
	 */
	void enterAlchemyTableResponse(RavenParser.AlchemyTableResponseContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#alchemyTableResponse}.
	 * @param ctx the parse tree
	 */
	void exitAlchemyTableResponse(RavenParser.AlchemyTableResponseContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#productOutboundZipfile}.
	 * @param ctx the parse tree
	 */
	void enterProductOutboundZipfile(RavenParser.ProductOutboundZipfileContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#productOutboundZipfile}.
	 * @param ctx the parse tree
	 */
	void exitProductOutboundZipfile(RavenParser.ProductOutboundZipfileContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#fileMergerPdf}.
	 * @param ctx the parse tree
	 */
	void enterFileMergerPdf(RavenParser.FileMergerPdfContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#fileMergerPdf}.
	 * @param ctx the parse tree
	 */
	void exitFileMergerPdf(RavenParser.FileMergerPdfContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#zipFileCreationOutbound}.
	 * @param ctx the parse tree
	 */
	void enterZipFileCreationOutbound(RavenParser.ZipFileCreationOutboundContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#zipFileCreationOutbound}.
	 * @param ctx the parse tree
	 */
	void exitZipFileCreationOutbound(RavenParser.ZipFileCreationOutboundContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#outboundKvpResponse}.
	 * @param ctx the parse tree
	 */
	void enterOutboundKvpResponse(RavenParser.OutboundKvpResponseContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#outboundKvpResponse}.
	 * @param ctx the parse tree
	 */
	void exitOutboundKvpResponse(RavenParser.OutboundKvpResponseContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#outboundTableResponse}.
	 * @param ctx the parse tree
	 */
	void enterOutboundTableResponse(RavenParser.OutboundTableResponseContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#outboundTableResponse}.
	 * @param ctx the parse tree
	 */
	void exitOutboundTableResponse(RavenParser.OutboundTableResponseContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#integratedNoiseModelApi}.
	 * @param ctx the parse tree
	 */
	void enterIntegratedNoiseModelApi(RavenParser.IntegratedNoiseModelApiContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#integratedNoiseModelApi}.
	 * @param ctx the parse tree
	 */
	void exitIntegratedNoiseModelApi(RavenParser.IntegratedNoiseModelApiContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#loadBalancerQueueUpdate}.
	 * @param ctx the parse tree
	 */
	void enterLoadBalancerQueueUpdate(RavenParser.LoadBalancerQueueUpdateContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#loadBalancerQueueUpdate}.
	 * @param ctx the parse tree
	 */
	void exitLoadBalancerQueueUpdate(RavenParser.LoadBalancerQueueUpdateContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#qrExtraction}.
	 * @param ctx the parse tree
	 */
	void enterQrExtraction(RavenParser.QrExtractionContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#qrExtraction}.
	 * @param ctx the parse tree
	 */
	void exitQrExtraction(RavenParser.QrExtractionContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#multipartUpload}.
	 * @param ctx the parse tree
	 */
	void enterMultipartUpload(RavenParser.MultipartUploadContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#multipartUpload}.
	 * @param ctx the parse tree
	 */
	void exitMultipartUpload(RavenParser.MultipartUploadContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#multipartDownload}.
	 * @param ctx the parse tree
	 */
	void enterMultipartDownload(RavenParser.MultipartDownloadContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#multipartDownload}.
	 * @param ctx the parse tree
	 */
	void exitMultipartDownload(RavenParser.MultipartDownloadContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#systemkeyTable}.
	 * @param ctx the parse tree
	 */
	void enterSystemkeyTable(RavenParser.SystemkeyTableContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#systemkeyTable}.
	 * @param ctx the parse tree
	 */
	void exitSystemkeyTable(RavenParser.SystemkeyTableContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#tritonModelLoadUnload}.
	 * @param ctx the parse tree
	 */
	void enterTritonModelLoadUnload(RavenParser.TritonModelLoadUnloadContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#tritonModelLoadUnload}.
	 * @param ctx the parse tree
	 */
	void exitTritonModelLoadUnload(RavenParser.TritonModelLoadUnloadContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#tableExtractionHeaders}.
	 * @param ctx the parse tree
	 */
	void enterTableExtractionHeaders(RavenParser.TableExtractionHeadersContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#tableExtractionHeaders}.
	 * @param ctx the parse tree
	 */
	void exitTableExtractionHeaders(RavenParser.TableExtractionHeadersContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#currencyDetection}.
	 * @param ctx the parse tree
	 */
	void enterCurrencyDetection(RavenParser.CurrencyDetectionContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#currencyDetection}.
	 * @param ctx the parse tree
	 */
	void exitCurrencyDetection(RavenParser.CurrencyDetectionContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#greyScaleConversion}.
	 * @param ctx the parse tree
	 */
	void enterGreyScaleConversion(RavenParser.GreyScaleConversionContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#greyScaleConversion}.
	 * @param ctx the parse tree
	 */
	void exitGreyScaleConversion(RavenParser.GreyScaleConversionContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#tableExtractionOutbound}.
	 * @param ctx the parse tree
	 */
	void enterTableExtractionOutbound(RavenParser.TableExtractionOutboundContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#tableExtractionOutbound}.
	 * @param ctx the parse tree
	 */
	void exitTableExtractionOutbound(RavenParser.TableExtractionOutboundContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#paragraphExtraction}.
	 * @param ctx the parse tree
	 */
	void enterParagraphExtraction(RavenParser.ParagraphExtractionContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#paragraphExtraction}.
	 * @param ctx the parse tree
	 */
	void exitParagraphExtraction(RavenParser.ParagraphExtractionContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#bulletInExtraction}.
	 * @param ctx the parse tree
	 */
	void enterBulletInExtraction(RavenParser.BulletInExtractionContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#bulletInExtraction}.
	 * @param ctx the parse tree
	 */
	void exitBulletInExtraction(RavenParser.BulletInExtractionContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#p2pNameValidation}.
	 * @param ctx the parse tree
	 */
	void enterP2pNameValidation(RavenParser.P2pNameValidationContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#p2pNameValidation}.
	 * @param ctx the parse tree
	 */
	void exitP2pNameValidation(RavenParser.P2pNameValidationContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#resource}.
	 * @param ctx the parse tree
	 */
	void enterResource(RavenParser.ResourceContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#resource}.
	 * @param ctx the parse tree
	 */
	void exitResource(RavenParser.ResourceContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#json}.
	 * @param ctx the parse tree
	 */
	void enterJson(RavenParser.JsonContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#json}.
	 * @param ctx the parse tree
	 */
	void exitJson(RavenParser.JsonContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#obj}.
	 * @param ctx the parse tree
	 */
	void enterObj(RavenParser.ObjContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#obj}.
	 * @param ctx the parse tree
	 */
	void exitObj(RavenParser.ObjContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#pair}.
	 * @param ctx the parse tree
	 */
	void enterPair(RavenParser.PairContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#pair}.
	 * @param ctx the parse tree
	 */
	void exitPair(RavenParser.PairContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#arr}.
	 * @param ctx the parse tree
	 */
	void enterArr(RavenParser.ArrContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#arr}.
	 * @param ctx the parse tree
	 */
	void exitArr(RavenParser.ArrContext ctx);
	/**
	 * Enter a parse tree produced by {@link RavenParser#jValue}.
	 * @param ctx the parse tree
	 */
	void enterJValue(RavenParser.JValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link RavenParser#jValue}.
	 * @param ctx the parse tree
	 */
	void exitJValue(RavenParser.JValueContext ctx);
}