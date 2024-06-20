package in.handyman.raven.lib;

import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.action.ActionExecution;
import in.handyman.raven.lambda.action.IActionExecution;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.CreateTAR;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;


/**
 * Auto Generated By Raven
 */
@ActionExecution(
        actionName = "CreateTAR"
)
public class CreateTARAction implements IActionExecution {
    private final ActionExecutionAudit actionExecutionAudit;

    private final Logger log;

    private final CreateTAR createTAR;

    private final Marker aMarker;

    public CreateTARAction(final ActionExecutionAudit actionExecutionAudit, final Logger log, final Object createTAR) {
        this.createTAR = (CreateTAR) createTAR;
        this.actionExecutionAudit = actionExecutionAudit;
        this.log = log;
        this.aMarker = MarkerFactory.getMarker("CreateTAR");
    }

    @Override
    public void execute() throws Exception {
        var source = createTAR.getSource();
        if (!Files.exists(Paths.get(source))) {
            log.info(aMarker, "{} source Folder not found", source);
        }
        this.actionExecutionAudit.getContext().put("tar.source.fileName", source);
        this.actionExecutionAudit.getContext().put("tar.source.fileSize", String.valueOf(calculateSize(source)));
        var destination = createTAR.getDestination();
        final Path dPath = Paths.get(destination);
        if (!Files.exists(dPath)) {
            Files.createDirectory(dPath);
        }
        var extension = createTAR.getExtension();
        var sourceFile = new File(source);
        var destinationFile = new File(destination.concat("/" + UUID.randomUUID()));
        log.info(aMarker, "Destination {}", destinationFile);
        final String fileName = destinationFile.getAbsolutePath().concat("." + extension);
        try (var fileOutputStream = new FileOutputStream(fileName)) {
            try (var gzipOutputStream = new GzipCompressorOutputStream(fileOutputStream)) {
                try (var archive = new TarArchiveOutputStream(gzipOutputStream)) {
                    archive.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_POSIX);
                    archive.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
                    addFilesToTarGZ(source, "", archive);
                }
            }
        }
        this.actionExecutionAudit.getContext().put("tar.dest.fileName", fileName);
        this.actionExecutionAudit.getContext().put("tar.dest.fileSize", String.valueOf(calculateSize(fileName)));

    }

    private double calculateSize(final String fileName)  {
        try {
            return BigDecimal.valueOf(Files.size(new File(fileName).toPath()) / (1024.0 * 1024)).setScale(2, RoundingMode.CEILING).doubleValue();
        } catch (IOException e) {
            throw new HandymanException("Error in getting file details", e, actionExecutionAudit);
        }
    }

    private void addFilesToTarGZ(final String filePath, final String parent, final TarArchiveOutputStream tarArchive){
        try {
            log.info(aMarker, " source {} parent {}", filePath, parent);
            final File file = new File(filePath);
            final String baseName = parent + file.getName();
            final TarArchiveEntry archiveEntry = new TarArchiveEntry(file);
            tarArchive.putArchiveEntry(archiveEntry);
            if (file.isFile()) {
                try (var fileInputStream = new FileInputStream(file)) {
                    IOUtils.copy(fileInputStream, tarArchive);
                    tarArchive.closeArchiveEntry();
                }
            } else if (file.isDirectory()) {
                tarArchive.closeArchiveEntry();
                final Optional<File[]> files = Optional.ofNullable(file.listFiles());
                if (files.isPresent()) {
                    for (var childFile : files.get()) {
                        addFilesToTarGZ(childFile.getAbsolutePath(), baseName + File.separator, tarArchive);
                    }
                }
            } else {
                tarArchive.closeArchiveEntry();
            }
        } catch (Exception e) {
            throw new HandymanException("Error in adding files to tar", e, actionExecutionAudit);
        }
    }

    @Override
    public boolean executeIf() throws Exception {
        return createTAR.getCondition();
    }
}
