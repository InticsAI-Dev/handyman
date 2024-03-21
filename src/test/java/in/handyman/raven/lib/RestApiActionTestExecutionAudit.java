package in.handyman.raven.lib;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import in.handyman.raven.lambda.process.HRequestResolver;
import in.handyman.raven.lambda.process.LContext;
import in.handyman.raven.lambda.process.LambdaEngine;
import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
class RestApiActionTestExecutionAudit {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void execute() {
        ObjectNode objectNode = objectMapper.createObjectNode();

        ObjectNode node = objectMapper.createObjectNode();
        ObjectNode node2 = objectMapper.createObjectNode();

        node.put("faxReferenceId", "AGA-REF-ID-001");
        node.put("callBackUrl", "AGA-REF-ID-001");
        node.put("faxChecksum", "bccf7e4800b32ed0e272acd436bfa32c3f3ec393");
        node.put("faxFileUri", "https://www.tutorialspoint.com/java/pdf/java_interfaces.pdf");

        node2.put("faxReferenceId", "AGA-REF-ID-002");
        node2.put("callBackUrl", "AGA-REF-ID-001");
        node2.put("faxChecksum", "72acd436bfa32c3f3ec393");
        node2.put("faxFileUri", "https://www.tutorialspoint.com/java/pdf/java_interfaces.pdf");

        objectNode.putPOJO("faxDocument", List.of(node, node2));

        LContext request = LContext.builder()
                .pipelineName("fax.processing.producer")
                .processLoadType(HRequestResolver.LoadType.FILE.name())
                .inheritedContext(Map.of("request", objectNode.toString()))
                .build();

        log.info(request.toString());
        LambdaEngine.start(request);
    }


    @Test
    public void testTruthAttributionDDL() {
        LContext request = LContext.builder()
                .pipelineName("db.test")
                .processLoadType(HRequestResolver.LoadType.FILE.name())
                .build();
        log.info(request.toString());
        LambdaEngine.start(request);
    }


    @Test
    public void denoiseMainCaller() {
        LContext request = LContext.builder()
                .pipelineName("master.data.caller")
                .processLoadType(HRequestResolver.LoadType.FILE.name())
                .build();
        log.info(request.toString());
        LambdaEngine.start(request);
    }

    @Test
    public void
    performanceOpt() {
        LContext request = LContext.builder()
                .pipelineName("root.processor#1")
                .processLoadType(HRequestResolver.LoadType.FILE.name())
                .inheritedContext(Map.ofEntries(Map.entry("created_user_id", "-1"),
                        Map.entry("batch_id", "TMP-AGD-001"),
                        Map.entry("tenant_id", "TND-001"),
                        Map.entry("document_id", "TMP-AGD-001"),
                        Map.entry("last_updated_user_id", "-1"),
                        Map.entry("dir_path", "/home/balasoundarya.thanga@zucisystems.com/workspace/gitdev/input/1.pdf/"),
                        Map.entry("target_directory_path", "/home/balasoundarya.thanga@zucisystems.com/workspace/gitdev/output")))
                .build();
        log.info(request.toString());
        LambdaEngine.start(request);
    }

    @Test
    public void
    performanceOptUi() {
        LContext request = LContext.builder()
                .pipelineName("root.processor#6")
                .processLoadType(HRequestResolver.LoadType.FILE.name())
                .inheritedContext(Map.ofEntries(Map.entry("created_user_id", "-1"),
                        Map.entry("batch_id", "TMP-AGD-001"),
                        Map.entry("tenant_id", "1"),
                        Map.entry("origin_type", "TRANSACTION"),
                        Map.entry("workspace_id", "-1"),
                        Map.entry("transaction_id", "TND-001"),
                        Map.entry("document_type", "HEALTH_CARE"),
                        Map.entry("document_id", "TMP-AGD-001"),
                        Map.entry("last_updated_user_id", "421"),
                        Map.entry("gen_group_id.group_id", "421"),
//                        Map.entry("group_id", "421"),
                        Map.entry("dir_path", "/home/anandh.andrews@zucisystems.com/intics-workspace/pipeline-ui/testing/input/SYNT_166838894.pdf"),
                        Map.entry("target_directory_path", "/home/anandh.andrews@zucisystems.com/intics-workspace/pipeline-ui/testing/output")))
                .build();
        log.info(request.toString());
        LambdaEngine.start(request);
    }


    @Test
    public void executethepr1(){
        List<String> list1 = Arrays.asList("FileDetailsAction",
                "ProductResponseAction",
                "AlphanumericvalidatorAction",
                "IncidentManagementAction",
                "AlchemyResponseAction",
                "DocnetAttributionAction",
                "MultitudeAction",
                "CreateZipAction",
                "RestApiAction",
                "EvalDateOfBirthAction",
                "DeleteFileDirectoryAction",
                "MultipartUploadAction",
                "TemplateDetectionAction",
                "AssignAction",
                "ProducerAction",
                "PixelClassifierUrgencyTriageAction",
                "EpisodeOfCoverageAction",
                "LoadCsvAction",
                "FileMergerAction",
                "PaperItemizerAction",
                "FileBucketingAction",
                "TableExtractionAction",
                "TritonModelLoadUnloadAction",
                "MailServerAction",
                "AbortAction",
                "ProducerConsumerModelAction",
                "AlphavalidatorAction",
                "ZipContentListAction",
                "UrgencyTriageAction",
                "RavenVmExceptionAction",
                "ZeroShotClassifierAction",
                "ClassificationTypeAction",
                "NerAdapterAction",
                "CoproStartAction",
                "TransferFileDirectoryAction",
                "CharactercountAction",
                "EntityFilterAction",
                "TrinityModelAction",
                "PushJsonAction",
                "CreateDirectoryAction",
                "UploadAssetAction",
                "UserRegistrationAction",
                "DonutDocQaAction",
                "NervalidatorAction",
                "MapJsonActionAction",
                "MultipartDownloadAction",
                "CreateFileAction",
                "QrAttributionAction",
                "DataExtractionAction",
                "AssetInfoAction",
                "CheckboxVqaAction",
                "TqaFilterAction",
                "ZipBatchAction",
                "SetActionValueAction",
                "TransformAction",
                "ProductOutboundZipfileAction",
                "MasterdataComparisonAction",
                "CoproStopAction",
                "DocClassifierAction",
                "DocnetResultAction",
                "FileMergerPdfAction",
                "FileSizeAction",
                "DrugMatchAction",
                "TextFilterAction",
                "CheckboxClassificationAction",
                "AlchemyKvpResponseAction",
                "SorGroupDetailsAction",
                "CreateTARAction",
                "DropFileAction",
                "TableExtractionOutboundAction",
                "AlchemyTableResponseAction",
                "TriageAttributionAction",
                "DonutImpiraQaAction",
                "ConsumerAction",
                "ForkProcessAction",
                "AlchemyInfoAction",
                "DatevalidatorAction",
                "AlchemyAuthTokenAction",
                "LoadExtractedDataAction",
                "ImportCsvToDBAction",
                "ChecksumAction",
                "WordcountAction",
                "ThresholdCheckAction",
                "CopyFileAction",
                "UrgencyTriageModelAction",
                "SorFilterAction",
                "DownloadAssetAction",
                "ZipFileCreationOutboundAction",
                "AuthTokenAction",
                "OutboundDeliveryNotifyAction",
                "SpawnProcessAction",
                "OutboundKvpResponseAction",
                "ExportCsvAction",
                "AutoRotationAction",
                "EvalMemberIdAction",
                "PhraseMatchPaperFilterAction",
                "CallProcessAction",
                "SharePointAction",
                "QrExtractionAction",
                "FtpsUploadAction",
                "AbsentKeyFilterAction",
                "PaperItemizationAction",
                "PixelClassifierAction",
                "JsonToFileAction",
                "ExtractTARAction",
                "SftpConnectorAction",
                "ScalarAdapterAction",
                "NumericvalidatorAction",
                "DogLegAction",
                "ZeroShotClassifierPaperFilterAction",
                "CopyDataAction",
                "LoadBalancerQueueUpdateAction",
                "ExtractZipAction",
                "TableExtractionHeadersAction",
                "EocJsonGeneratorAction",
                "DocumentClassificationAction",
                "BlankPageRemoverAction",
                "DirPathAction",
                "EvalPatientNameAction",
                "FtpsDownloadAction",
                "OutboundTableResponseAction",
                "HwDetectionAction",
                "IntegratedNoiseModelApiAction");
        List<String> list2 = Arrays.asList("TransformAction",
                "TableExtractionOutboundAction",
                "ExportCsvAction",
                "TransferFileDirectoryAction",
                "CoproStopAction",
                "PaperItemizationAction",
                "EntityFilterAction",
                "ZipContentListAction",
                "JsonToFileAction",
                "FtpsUploadAction",
                "PushJsonAction",
                "DonutDocQaAction",
                "UrgencyTriageModelAction",
                "AssignAction",
                "AbsentKeyFilterAction",
                "ZeroShotClassifierPaperFilterAction",
                "DocnetResultAction",
                "DocumentClassificationAction",
                "TritonModelLoadUnloadAction",
                "DownloadAssetAction",
                "AssetInfoAction",
                "NerAdapterAction",
                "OutboundTableResponseAction",
                "DocnetAttributionAction",
                "TemplateDetectionAction",
                "EocJsonGeneratorAction",
                "DatevalidatorAction",
                "DonutImpiraQaAction",
                "FileSizeAction",
                "FileMergerAction",
                "LoadExtractedDataAction",
                "ChecksumAction",
                "DeleteFileDirectoryAction",
                "ZeroShotClassifierAction",
                "TriageAttributionAction",
                "DogLegAction",
                "AutoRotationAction",
                "QrExtractionAction",
                "AuthTokenAction",
                "CheckboxClassificationAction",
                "ForkProcessAction",
                "SftpConnectorAction",
                "SorFilterAction",
                "CreateDirectoryAction",
                "IncidentManagementAction",
                "DocClassifierAction",
                "TableExtractionAction",
                "QrAttributionAction",
                "FileDetailsAction",
                "OutboundKvpResponseAction",
                "UserRegistrationAction",
                "TextFilterAction",
                "IntegratedNoiseModelApiAction",
                "ProducerConsumerModelAction",
                "NumericvalidatorAction",
                "AbortAction",
                "HwDetectionAction",
                "CreateFileAction",
                "EvalPatientNameAction",
                "AlchemyAuthTokenAction",
                "ZipBatchAction",
                "SetValueAction",
                "PhraseMatchPaperFilterAction",
                "ScalarAdapterAction",
                "FileMergerPdfAction",
                "EvalMemberIdAction",
                "UploadAssetAction",
                "CopyDataAction",
                "ProducerAction",
                "PaperItemizerAction",
                "MultipartDownloadAction",
                "MasterdataComparisonAction",
                "AlphanumericvalidatorAction",
                "ClassificationTypeAction",
                "EpisodeOfCoverageAction",
                "ZipFileCreationOutboundAction",
                "MultitudeAction",
                "ExtractTARAction",
                "ProductResponseAction",
                "BlankPageRemoverAction",
                "TqaFilterAction",
                "FileBucketingAction",
                "LoadBalancerQueueUpdateAction",
                "CharactercountAction",
                "ImportCsvToDBAction",
                "NervalidatorAction",
                "LoadCsvAction",
                "MultipartUploadAction",
                "MailServerAction",
                "DirPathAction",
                "CheckboxVqaAction",
                "WordcountAction",
                "SorGroupDetailsAction",
                "OutboundDeliveryNotifyAction",
                "ProductOutboundZipFileAction",
                "PixelClassifierAction",
                "ProductOutboundZipfileAction",
                "CoproStartAction",
                "AlchemyTableResponseAction",
                "CallProcessAction",
                "RavenVmExceptionAction",
                "ConsumerAction",
                "AlchemyKvpResponseAction",
                "FtpsDownloadAction",
                "DrugMatchAction",
                "MapJsonAction",
                "UrgencyTriageAction",
                "CreateTARAction",
                "SpawnProcessAction",
                "DataExtractionAction",
                "DropFileAction",
                "TableExtractionHeadersAction",
                "PixelClassifierUrgencyTriageAction",
                "ExtractZipAction",
                "AlchemyResponseAction",
                "CreateZipAction",
                "CopyFileAction",
                "AlchemyInfoAction",
                "SharePointAction",
                "AlphavalidatorAction",
                "RestApiAction",
                "ThresholdCheckAction",
                "EvalDateOfBirthAction",
                "TrinityModelAction");

//
//        for (String value : list1) {
//            if (!list2.contains(value)) {
//                System.out.println(value);
//            }
//        }



        for (String value : list2) {
            if (!list1.contains(value)) {
                System.out.println(value);
            }
        }

    }
    @Test
    public void qrcode() {
        LContext request = LContext.builder()
                .pipelineName("qr.extraction.processor")
                .processLoadType(HRequestResolver.LoadType.FILE.name())
                .inheritedContext(Map.ofEntries(Map.entry("created_user_id", "-1"),
                        Map.entry("batch_id", "TMP-AGD-001"),
                        Map.entry("tenant_id", "TND-001"),
                        Map.entry("document_id", "TMP-AGD-001"),
                        Map.entry("last_updated_user_id", "-1"),
                        Map.entry("dir_path", "/home/anandh.andrews@zucisystems.com/W-space/pr1-lambdas/agadia/input/"),
                        Map.entry("target_directory_path", "/home/anandh.andrews@zucisystems.com/W-space/pr1-lambdas/agadia/agadia_output")))
                .build();
        log.info(request.toString());
        LambdaEngine.start(request);
    }

    @Test
    public void testTruthAttributionSummoning() {
        LContext request = LContext.builder()
                .pipelineName("truth.attribution.summoning")
                .processLoadType(HRequestResolver.LoadType.FILE.name())
                .build();
        log.info(request.toString());
        LambdaEngine.start(request);
    }


    @Test
    void executeQR() {
        LambdaEngine.start(LContext.builder()
                .pipelineName("root.processor")
                .processLoadType(HRequestResolver.LoadType.FILE.name())
                .inheritedContext(Map.of("group_id", "32"))
                .build());
    }


    @Test
    public void shellScript() throws IOException, InterruptedException {

        String homeDir = System.getProperty("user.home");
        System.out.println("home Directory " + homeDir);
        boolean IS_WINDOWS = System.getProperty("os.name")
                .toLowerCase().startsWith("windows");
        System.out.println("Is windows " + IS_WINDOWS);
        Process process;
        if (IS_WINDOWS) {
            process = Runtime.getRuntime().exec(String.format("cmd.exe /c dir %s", homeDir));
            System.out.println("Executed for windows " + process);
        } else {
            process = Runtime.getRuntime().exec(String.format("/bin/sh -c ls %s", homeDir));
            System.out.println("Executed for linux " + process);
        }
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()));
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line).append("\n");
            System.out.println(stringBuilder);
        }

        int exitCode = process.waitFor();
        assert exitCode == 0;

    }


    @Test
    public void practiceTrying()
    {
        LContext request = LContext.builder()
                .pipelineName("master.data.caller")
                .processLoadType(HRequestResolver.LoadType.FILE.name())
                .inheritedContext(Map.ofEntries(
                        Map.entry("tenant_id","1"),
                        Map.entry("copro.intelli-match.url","http://192.168.10.240:9200/v2/models/cos-service/versions/1/infer"),
                        Map.entry("consumer.masterdata.API.count","1"),
                        Map.entry("read.batch.size","5"),
                        Map.entry("triton.request.activator","false"),
                        Map.entry("actionId","36555"),
                        Map.entry("group_id","16"),
                        Map.entry("gen_id.root_pipeline_id","25384"),
                        Map.entry("gen_group_id.group_id", "16"),
                        Map.entry("init_process_id.process_id", "12345"),
                        Map.entry("write.batch.size","5")))
                .build();
        log.info(request.toString());
        LambdaEngine.start(request);
    }

    @Test
    public void performanceOptUi1()
    {
        LContext request = LContext.builder()
                .pipelineName("root.processor#8")
                .processLoadType(HRequestResolver.LoadType.FILE.name())
                .inheritedContext(Map.ofEntries(Map.entry("created_user_id", "-1"),
                        Map.entry("batch_id","TMP-AGD-001"),
                        Map.entry("tenant_id","1"),
                        Map.entry("origin_type","TRANSACTION"),
                        Map.entry("workspace_id","-1"),
                        Map.entry("transaction_id","TND-001"),
                        Map.entry("document_type","HEALTH_CARE"),
                        Map.entry("document_id","TMP-AGD-001"),
                        Map.entry("consumer.masterdata.API.count","1"),
                        Map.entry("read.batch.size","5"),
                        Map.entry("triton.request.activator","true"),
                        Map.entry("actionId","1"),
                        Map.entry("write.batch.size","5"),
//                        Map.entry("gen_group_id.group_id", "1245"),
                        Map.entry("last_updated_user_id","-1"),
                        Map.entry("copro.intelli-match.url","http://192.168.10.240:9200/v2/models/cos-service/versions/1/infer"),
                        Map.entry("dir_path","/home/iswerya.justin@zucisystems.com/workspace/Dev/Deliverable_May/PAGE-1/1-Page/Elixir/Synthetic_Data/SYNT_166884608.pdf"),
                        Map.entry("target_directory_path","/home/iswerya.justin@zucisystems.com/workspace/Dev/main_/UT/output")))
                .build();
        log.info(request.toString());
        LambdaEngine.start(request);
    }


    @Test
    public void modelIdTest(){
        LContext request = LContext.builder()
                .pipelineName("sor.transaction.main")
                .processLoadType(HRequestResolver.LoadType.FILE.name())
                .inheritedContext(Map.ofEntries(
                        Map.entry("tenant_id", "1"),
                        Map.entry("gen_group_id.group_id", "2"),
                        Map.entry("created_user_id", "-11"),
                        Map.entry("batch_id","TMP-AGD-011"),
                        Map.entry("document_id","TMP-AGD-111"),
                        Map.entry("last_updated_user_id","111"),
                        Map.entry("origin_type","TRANSACTION"),
                        Map.entry("transaction_id","TRZ-111"),
                        Map.entry("init_process_id.process_id", "15"))
                )
                .build();
        log.info(request.toString());
        LambdaEngine.start(request);
    }
}
