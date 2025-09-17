package in.handyman.raven.lib;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.access.ResourceAccess;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.core.encryption.SecurityEngine;
import in.handyman.raven.lib.model.AssetInfo;
import in.handyman.raven.lib.model.AssetInfoInputTable;
import in.handyman.raven.lib.model.AssetInfoOutputTable;
import in.handyman.raven.util.ExceptionUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.Update;
import org.slf4j.Logger;
import org.slf4j.Marker;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static in.handyman.raven.core.encryption.EncryptionConstants.ENCRYPT_TEXT_EXTRACTION_OUTPUT;
import static java.lang.Math.random;

/**
 * Consumer process for AssetInfoAction to handle file metadata extraction and audit logging.
 */
public class AssetInfoConsumerProcess implements CoproProcessor.ConsumerProcess<AssetInfoInputTable, AssetInfoOutputTable> {
    public static final String ASSET_INFO_ACTION_BASE_64_STORE_ACTIVATOR = "asset.info.action.base64.store.activator";
    private final Logger log;
    private final Marker marker;
    private final ActionExecutionAudit action;
    private final AssetInfo assetInfo;
    private final Long tenantId;

    public AssetInfoConsumerProcess(Logger log, Marker marker, ActionExecutionAudit action, AssetInfo assetInfo, Long tenantId) {
        this.log = log;
        this.marker = marker;
        this.action = action;
        this.assetInfo = assetInfo;
        this.tenantId = tenantId;
    }

    public List<AssetInfoOutputTable> process(URL endpoint, AssetInfoInputTable input) throws Exception {
        List<AssetInfoOutputTable> fileInfos = new ArrayList<>();
        try {
            log.info(marker, "Processing file path: {}", input.getFilePath());
            final String filePathString = Optional.ofNullable(input.getFilePath()).map(String::valueOf).orElse("[]");
            final String batchId = Optional.ofNullable(input.getBatchId()).map(String::valueOf).orElse("[]");
            File file = new File(filePathString);
            if (file.isDirectory()) {
                try (var files = Files.walk(Path.of(filePathString)).filter(Files::isRegularFile)) {
                    files.forEach(path -> fileInfos.add(insertQuery(path.toFile(), tenantId, batchId)));
                } catch (IOException e) {
                    log.error(marker, "Exception occurred in directory iteration {}", ExceptionUtil.toString(e));
                    throw new HandymanException("Exception occurred in directory iteration", e, action);
                }
            } else if (file.isFile()) {
                fileInfos.add(insertQuery(file, tenantId, batchId));
            }
        } catch (Exception e) {
            log.error(marker, "Error in file info for {}", input.getFilePath(), e);
            HandymanException handymanException=new HandymanException(e);
            HandymanException.insertException("Error in file info for " + input.getFilePath(), handymanException, action);
        }
        return fileInfos;
    }

    private AssetInfoOutputTable insertQuery(File file, Long tenantId, String batchId) {
        AssetInfoOutputTable fileInfoBuilder = new AssetInfoOutputTable();
        try {
            log.info(marker, "Insert query for file {}", file);
            String sha1Hex;
            try (InputStream is = Files.newInputStream(Path.of(file.getPath()))) {
                sha1Hex = org.apache.commons.codec.digest.DigestUtils.sha1Hex(is);
            } catch (IOException e) {
                log.error("Error in reading input stream {}", ExceptionUtil.toString(e));
                throw new HandymanException("Error in reading input stream", e, action);
            }
            var fileSize = file.length() / 1024;
            String fileExtension = FilenameUtils.getExtension(file.getName());
            String fileAbsolutePath = file.getAbsolutePath();
            String decodedFileName = decodeFileName(file.getName());

            float pageWidth = 0f;
            float pageHeight = 0f;
            int dpi = 0;
            if (fileExtension.equalsIgnoreCase("pdf")) {
                try (PDDocument document = Loader.loadPDF(Files.readAllBytes(file.toPath()))) {
                    PDPage firstPage = document.getPage(0);
                    pageWidth = firstPage.getMediaBox().getWidth();
                    pageHeight = firstPage.getMediaBox().getHeight();
                    float width_inches = pageWidth / 72.0f; // Assuming 75 DPI for PDF
                    dpi = (int) (pageWidth / width_inches);
                    log.info("Page width: {}, height: {}, dpi {}", pageWidth, pageHeight, dpi);
                } catch (IOException e) {
                    log.error("Error in calculating width, height, dpi for pdf file with exception {}", e.getMessage());
                }
            } else {
                BufferedImage image = ImageIO.read(file);
                if (image != null) {
                    pageWidth = image.getWidth();
                    pageHeight = image.getHeight();
                    float width_inches = pageWidth / 72;
                    dpi = (int) (pageWidth / width_inches);
                } else {
                    log.error("Error in calculating width, height, dpi for image");
                }
            }
            String fileId = FilenameUtils.removeExtension(file.getName()) + "_" + ((int) (900000 * random() + 100000));
            log.info("File Name: {}, File Size: {}, File Extension: {}, File ID: {}", file.getName(), fileSize, fileExtension, fileId);
            log.info("File ID generated: {}", fileId);

            String base64EncodeValue = getBase64EncodeValue(fileAbsolutePath, fileExtension);
            fileInfoBuilder = AssetInfoOutputTable.builder()
                    .fileId(fileId)
                    .tenantId(tenantId)
                    .fileChecksum(sha1Hex)
                    .fileExtension(fileExtension)
                    .fileName(FilenameUtils.removeExtension(file.getName()))
                    .decodedFileName(FilenameUtils.removeExtension(decodedFileName))
                    .filePath(fileAbsolutePath)
                    .fileSize(String.valueOf(fileSize))
                    .rootPipelineId(Long.valueOf(action.getContext().get("pipeline-id")))
                    .processId(Long.valueOf(action.getContext().get("process-id")))
                    .encode(base64EncodeValue)
                    .width(pageWidth)
                    .height(pageHeight)
                    .dpi(dpi)
                    .batchId(batchId)
                    .build();
            log.info(marker, "File Info Builder {}", fileInfoBuilder.getFileName());
        } catch (Exception ex) {
            log.error(marker, "Error occurred in builder {}", ExceptionUtil.toString(ex));
            HandymanException handymanException=new HandymanException(ex);
            HandymanException.insertException("Error occurred in builder", handymanException, action);
        }
        return fileInfoBuilder;
    }

    private String getBase64EncodeValue(String fileAbsolutePath, String fileExtension) throws IOException {
        boolean saveBase64Value = Boolean.parseBoolean(action.getContext().getOrDefault(ASSET_INFO_ACTION_BASE_64_STORE_ACTIVATOR, "false"));
        if(saveBase64Value){
            String base64ForPathValue = getBase64ForPath(fileAbsolutePath, fileExtension);
            return encryptRequestResponse(base64ForPathValue);
        }else {
            return "";
        }
    }

    private String encryptRequestResponse(String request) {
        String encryptReqRes = action.getContext().get(ENCRYPT_TEXT_EXTRACTION_OUTPUT);
        String requestStr;
        if ("true".equals(encryptReqRes)) {
            String encryptedRequest = SecurityEngine.getInticsIntegrityMethod(action, log).encrypt(request, "AES256", "BASE64_ENCODED");
            requestStr = encryptedRequest;
        } else {
            requestStr = request;
        }
        return requestStr;
    }

    private String getBase64ForPath(String imagePath, String fileExtension) throws IOException {
        String base64Image = "";
        try {
            if (!Objects.equals(fileExtension, "pdf")) {
                byte[] imageBytes = Files.readAllBytes(Path.of(imagePath));
                base64Image = Base64.getEncoder().encodeToString(imageBytes);
                log.info(marker, "Base64 created for file {}", imagePath);
            }
        } catch (Exception e) {
            log.error(marker, "Error occurred in creating base64 {}", ExceptionUtil.toString(e));
            HandymanException handymanException=new HandymanException(e);
            HandymanException.insertException("Error occurred in creating base64", handymanException, action);
        }
        return base64Image;
    }

    private static String decodeFileName(String fileName) {
        try {
            return URLDecoder.decode(fileName, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            return fileName;
        }
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SanitarySummary {
        private int rowCount;
        private int correctRowCount;
        private int errorRowCount;
        private String comments;
        private Long tenantId;
        private String batchId;
    }
}