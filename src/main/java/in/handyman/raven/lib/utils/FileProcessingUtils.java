package in.handyman.raven.lib.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.util.ExceptionUtil;
import org.slf4j.Logger;
import org.slf4j.Marker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

public class FileProcessingUtils {

    private final Logger log;
    private final Marker aMarker;
    public final ActionExecutionAudit action;
    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private static final ObjectMapper objectMapper = new ObjectMapper();


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

    public String callCropImageApi(String base64Image) throws IOException, InterruptedException {
        String jsonPayload = String.format("{\"base64Img\": \"%s\"}", base64Image);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8000/crop-image"))  // 🔁 Update URL as needed
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        // Parse JSON response
        JsonNode jsonNode = objectMapper.readTree(response.body());

        if (jsonNode.has("croppedBase64Img")) {
            String croppedBase64 = jsonNode.get("croppedBase64Img").asText();
            log.info(aMarker, "Cropped image received.");
            return croppedBase64;
        } else {
            throw new HandymanException("Cropped base64 image not found in response: " + response.body(),new HandymanException("Error in calling copro api for cropped image"),action);
        }
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
