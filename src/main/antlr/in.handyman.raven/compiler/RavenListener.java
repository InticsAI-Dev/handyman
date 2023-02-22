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