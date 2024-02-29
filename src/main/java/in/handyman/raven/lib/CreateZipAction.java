package in.handyman.raven.lib;

import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.action.ActionExecution;
import in.handyman.raven.lambda.action.IActionExecution;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.CreateZip;
import in.handyman.raven.util.ExceptionUtil;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Auto Generated By Raven
 */
@ActionExecution(
        actionName = "CreateZip"
)
public class CreateZipAction implements IActionExecution {
    private final ActionExecutionAudit action;

    private final Logger log;

    private final CreateZip createZip;

    private final Marker aMarker;

    public CreateZipAction(final ActionExecutionAudit action, final Logger log,
                           final Object createZip) {
        this.createZip = (CreateZip) createZip;
        this.action = action;
        this.log = log;
        this.aMarker = MarkerFactory.getMarker(" CreateZip:" + this.createZip.getName());
    }

    @Override
    public void execute() throws Exception {
        try {
            final String zipFileName = createZip.getFileName();
            final String source = createZip.getSource();
            final String destFileDir = createZip.getDestination();
            if (!Files.exists(Paths.get(source)))
                log.info(aMarker, "{} source Folder not found", source);
//            FileOutputStream fos = new FileOutputStream(destFileDir + File.separator + zipFileName + ".zip");
//            ZipOutputStream zipOut = new ZipOutputStream(fos);
//            File fileToZip = new File(source);
//            zipFile(fileToZip, fileToZip.getName(), zipOut);
//            zipOut.close();
//            fos.close();
            try {
                zipFolderLegacy(source, destFileDir + File.separator + zipFileName + ".zip");
                System.out.println("Folder successfully zipped.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            log.error(aMarker, "Error in execute method in create zip action {}", ExceptionUtil.toString(e));
            throw new HandymanException("Error in execute method in create zip action", e, action);
        }
    }

    private void zipFolderLegacy(String sourceFolderPath, String zipFilePath) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(zipFilePath);
             ZipOutputStream zos = new ZipOutputStream(fos)) {

            File sourceFolder = new File(sourceFolderPath);
            zipFileNew(sourceFolder, sourceFolder.getName(), zos);
        }
    }

    private void zipFileNew(File fileToZip, String fileName, ZipOutputStream zos) throws IOException {
        if (fileToZip.isHidden()) {
            return;
        }

        if (fileToZip.isDirectory()) {
            for (File file : fileToZip.listFiles()) {
                zipFileNew(file, fileName + File.separator + file.getName(), zos);
            }
            return;
        }

        try (FileInputStream fis = new FileInputStream(fileToZip)) {
            ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
            zos.putNextEntry(zipEntry);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) >= 0) {
                zos.write(buffer, 0, length);
            }
        }
    }


    public void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut) {
        try {
            if (fileToZip.isHidden()) {
                return;
            }
            if (fileToZip.isDirectory()) {
                if (fileName.endsWith("/")) {
                    zipOut.putNextEntry(new ZipEntry(fileName));
                    zipOut.closeEntry();
                } else {
                    zipOut.putNextEntry(new ZipEntry(fileName + "/"));
                    zipOut.closeEntry();
                }
                File[] children = fileToZip.listFiles();
                if (children != null && children.length > 0) {
                    for (File childFile : children) {
                        zipFile(childFile, fileName + "/" + childFile.getName(), zipOut);
                    }
                    return;
                }
            }
            FileInputStream fis = new FileInputStream(fileToZip);
            ZipEntry zipEntry = new ZipEntry(fileName);
            zipOut.putNextEntry(zipEntry);
            byte[] bytes = new byte[1024];
            int length;
            while ((length = fis.read(bytes)) >= 0) {
                zipOut.write(bytes, 0, length);
            }
            fis.close();
            log.info(aMarker, "Created zip {} and saved in the {} directory", fileToZip.getName(), fileToZip.getAbsolutePath());
        } catch (Exception e) {
            log.error(aMarker, "Error in zip file generation {}", ExceptionUtil.toString(e));
            throw new HandymanException("Error in zip file generation", e, action);
        }

    }

    @Override
    public boolean executeIf() throws Exception {
        return createZip.getCondition();
    }
}
