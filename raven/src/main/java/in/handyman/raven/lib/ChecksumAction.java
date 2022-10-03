package in.handyman.raven.lib;

import in.handyman.raven.lambda.action.ActionExecution;
import in.handyman.raven.lambda.action.IActionExecution;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.Checksum;

import java.io.InputStream;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.jena.base.Sys;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import javax.sound.midi.SysexMessage;

/**
 * Auto Generated By Raven
 */
@ActionExecution(
        actionName = "Checksum"
)
public class ChecksumAction implements IActionExecution {
    private final ActionExecutionAudit action;

    private final Logger log;

    private final Checksum checksum;

    private final Marker aMarker;

    public ChecksumAction(final ActionExecutionAudit action, final Logger log,
                          final Object checksum) {
        this.checksum = (Checksum) checksum;
        this.action = action;
        this.log = log;
        this.aMarker = MarkerFactory.getMarker(" Checksum:" + this.checksum.getName());
    }

    @Override
    public void execute() throws Exception {

        String fileName = checksum.getFileName();
        String filePath = checksum.getFilePath();
        String fullyGeneratedPath = filePath+"/"+fileName;
        try (InputStream is = Files.newInputStream(Paths.get(fullyGeneratedPath))) {
            String md5 = org.apache.commons.codec.digest.DigestUtils.md5Hex(is);
            log.info("MD5 CHECKSUM : "+md5);
        }
    }

    @Override
    public boolean executeIf() throws Exception {
        return checksum.getCondition();
    }
}
