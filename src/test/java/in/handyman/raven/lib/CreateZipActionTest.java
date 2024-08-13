package in.handyman.raven.lib;

import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.CreateZip;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Map;

import java.io.*;
import java.nio.file.*;
import java.util.zip.*;



@Slf4j
class CreateZipActionTest {


    @Test
    void execute() throws Exception {
        CreateZip createZipAction = CreateZip.builder()
                .name("test")
                .fileName("pdf_to_image")
                .destination("/home/anandh.andrews@zucisystems.com/intics-workspace/testing/database-backup")
                .source("/home/anandh.andrews@zucisystems.com/intics-workspace/testing/pdf_to_image")
                .build();

        ActionExecutionAudit actionExecutionAudit = new ActionExecutionAudit();

        actionExecutionAudit.getContext().putAll(Map.ofEntries(
                Map.entry("read.batch.size", "5"),
                Map.entry("gen_group_id.group_id", "5"),
                Map.entry("consumer.API.count", "1"),
                Map.entry("paper.itemizer.multipart.upload.url", "http://localhost:8002/multipart-download"),
                Map.entry("write.batch.size", "5")));

        CreateZipAction createZipAction1 = new CreateZipAction(actionExecutionAudit, log, createZipAction);
        createZipAction1.execute();

    }

    @Test
     void zipDirectory() throws IOException {
        String zipName = "pdf_to_image.zip";
        String targetDir = "/home/anandh.andrews@zucisystems.com/intics-workspace/testing/database-backup";
        String sourceDir = "/home/anandh.andrews@zucisystems.com/intics-workspace/testing/pdf_to_image";

        Path sourcePath = Paths.get(sourceDir);
        Path zipPath = Paths.get(targetDir, zipName);

        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipPath.toFile()))) {
            Files.walk(sourcePath)
                    .filter(path -> !Files.isDirectory(path))
                    .forEach(path -> {
                        ZipEntry zipEntry = new ZipEntry(sourcePath.relativize(path).toString());
                        try (InputStream is = Files.newInputStream(path)) {
                            zos.putNextEntry(zipEntry);
                            byte[] buffer = new byte[1024];
                            int length;
                            while ((length = is.read(buffer)) >= 0) {
                                zos.write(buffer, 0, length);
                            }
                            zos.closeEntry();
                        } catch (IOException e) {
                            System.err.println("Failed to add file to ZIP: " + path);
                            e.printStackTrace();
                        }
                    });
        }
    }
}