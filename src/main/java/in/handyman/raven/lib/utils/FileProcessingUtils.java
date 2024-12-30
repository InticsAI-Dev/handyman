package in.handyman.raven.lib.utils;

import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.util.ExceptionUtil;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

public class FileProcessingUtils {

    private final Logger log;
    private final Marker aMarker;
    public final ActionExecutionAudit action;

    public FileProcessingUtils(Logger log, Marker aMarker, ActionExecutionAudit action) {
        this.log = log;
        this.aMarker = aMarker;
        this.action = action;
    }

    public String convertFileToBase64(String imagePath) throws IOException {
        String base64Image = new String();
        try {

            // Read the image file into a byte array
            byte[] imageBytes = Files.readAllBytes(Path.of(imagePath));

            // Encode the byte array to Base64
            base64Image = Base64.getEncoder().encodeToString(imageBytes);

            // Print the Base64 encoded string
            log.info(aMarker, "base 64 created for this file {}", imagePath);

        } catch (Exception e) {
            log.error(aMarker, "error occurred in creating base 64 {}", ExceptionUtil.toString(e));
            throw new HandymanException("error occurred in creating base 64 {} ", e, action);
        }

        return base64Image;
    }

    public String convertBase64ToFile(String base64Image, String outputFilePath) throws IOException {
        try {
            // Check if the Base64 string is valid
            if (base64Image == null || base64Image.isEmpty() || base64Image.isBlank()) {
                return outputFilePath; // Return the path if the input is invalid
            }

            // Create a File object for the output file path
            File outputFile = new File(outputFilePath);

            // Get the parent directory of the file
            File parentDir = outputFile.getParentFile();

            // If the directory doesn't exist, create it
            if (parentDir != null && !parentDir.exists()) {
                if (parentDir.mkdirs()) {
                    log.info(aMarker, "Directory successfully created at {}", parentDir.getAbsolutePath());
                } else {
                    log.warn(aMarker, "Failed to create directory at {}", parentDir.getAbsolutePath());
                    throw new IOException("Unable to create directory: " + parentDir.getAbsolutePath());
                }
            }

            // Decode the Base64 string into bytes
            byte[] fileBytes = Base64.getDecoder().decode(base64Image);

            // Write the bytes to the specified file
            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                fos.write(fileBytes);
            }
            if (outputFile.exists()) {
                log.info(aMarker, "File successfully created at {}", outputFilePath);
            }
            return outputFilePath;

        } catch (Exception e) {
            // Log and rethrow the exception for further handling
            log.error(aMarker, "Error occurred while converting Base64 to file: {}", ExceptionUtil.toString(e));
            throw new HandymanException("Error occurred while converting Base64 to file", e, action);
        }
    }
}
