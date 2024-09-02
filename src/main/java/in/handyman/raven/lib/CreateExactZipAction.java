package in.handyman.raven.lib;

import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.action.ActionExecution;
import in.handyman.raven.lambda.action.IActionExecution;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.CreateExactZip;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

/**
 * Auto Generated By Raven
 */
@ActionExecution(
        actionName = "CreateExactZip"
)
public class CreateExactZipAction implements IActionExecution {
    private final ActionExecutionAudit action;

    private final Logger log;

    private final CreateExactZip createExactZip;

    private final Marker aMarker;

    public CreateExactZipAction(final ActionExecutionAudit action, final Logger log,
                                final Object createExactZip) {
        this.createExactZip = (CreateExactZip) createExactZip;
        this.action = action;
        this.log = log;
        this.aMarker = MarkerFactory.getMarker(" CreateExactZip:" + this.createExactZip.getName());
    }

    @Override
    public void execute() throws Exception {
        final String zipFileName = createExactZip.getFileName();
        final String source = createExactZip.getSource();
        final String destFileDir = createExactZip.getDestination();
        if (!Files.exists(Paths.get(source)))
            log.info(aMarker, "{} source Folder not found", source);

        log.info(aMarker, "Folder zip started {}.", destFileDir);
        boolean isCreated = zipNestedDirectory(zipFileName, source, destFileDir);
        if (isCreated) {
            log.info("ZIP file created successfully.{}", source);
        } else {
            log.info("Failed to create ZIP file.");
        }
    }

    public boolean zipNestedDirectory(String zipName, String sourceDir, String targetDir) {
        Path sourcePath = Paths.get(sourceDir);
        Path zipPath = Paths.get(targetDir, zipName);

        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipPath.toFile()))) {
            Files.walk(sourcePath)
                    .forEach(path -> {
                        try {
                            if (Files.isDirectory(path)) {
                                // If it's a directory, add it as an empty directory entry
                                String dirEntryName = sourcePath.relativize(path).toString() + "/";
                                zos.putNextEntry(new ZipEntry(dirEntryName));
                                zos.closeEntry();
                            } else {
                                // If it's a file, add the file's contents
                                ZipEntry zipEntry = new ZipEntry(sourcePath.relativize(path).toString());
                                try (InputStream is = Files.newInputStream(path)) {
                                    zos.putNextEntry(zipEntry);
                                    byte[] buffer = new byte[1024];
                                    int length;
                                    while ((length = is.read(buffer)) >= 0) {
                                        zos.write(buffer, 0, length);
                                    }
                                    zos.closeEntry();
                                }
                            }
                        } catch (IOException e) {
                            log.error("Failed to add file to ZIP: {}", path);
                            HandymanException handymanException = new HandymanException(e);
                            HandymanException.insertException("Failed to add file to ZIP {} :", handymanException, action);

                        }
                    });
            return true;  // Return true if ZIP creation is successful
        } catch (IOException e) {
            log.error("Failed to create ZIP file: {}", targetDir);
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("Failed to add file to ZIP {} :", handymanException, action);
            return false;  // Return false if an error occurs
        }
    }

    public boolean zipDirectory(String zipName, String sourceDir, String targetDir) {
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
                            log.error("Failed to add file to ZIP: {}", path);
                            HandymanException handymanException = new HandymanException(e);
                            HandymanException.insertException("Failed to add file to ZIP {} :", handymanException, action);

                        }
                    });
            return true;  // Return true if ZIP creation is successful
        } catch (IOException e) {
            log.error("Failed to create ZIP file: {}", targetDir);
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("Failed to add file to ZIP {} :", handymanException, action);
            return false;  // Return false if an error occurs
        }
    }

    @Override
    public boolean executeIf() throws Exception {
        return createExactZip.getCondition();
    }
}
