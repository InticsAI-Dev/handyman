package in.handyman.raven.lib;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.access.ResourceAccess;
import in.handyman.raven.lambda.action.ActionExecution;
import in.handyman.raven.lambda.action.IActionExecution;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.ZipFileCreationOutbound;
import in.handyman.raven.lib.model.outbound.OutboundInputTableEntity;
import in.handyman.raven.lib.model.outbound.OutboundOutputTableEntity;
import in.handyman.raven.util.CommonQueryUtil;
import in.handyman.raven.util.ExceptionUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.result.ResultIterable;
import org.jdbi.v3.core.statement.Query;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Auto Generated By Raven
 */
@ActionExecution(
        actionName = "ZipFileCreationOutbound"
)
public class ZipFileCreationOutboundAction implements IActionExecution {
    private final ActionExecutionAudit action;

    private final Logger log;

    private final ZipFileCreationOutbound zipFileCreationOutbound;

    private final Marker aMarker;

    private final String OUTBOUND_FILES = "outbound-files";

    public ZipFileCreationOutboundAction(final ActionExecutionAudit action, final Logger log,
                                         final Object zipFileCreationOutbound) {
        this.zipFileCreationOutbound = (ZipFileCreationOutbound) zipFileCreationOutbound;
        this.action = action;
        this.log = log;
        this.aMarker = MarkerFactory.getMarker(" ZipFileCreationOutbound:" + this.zipFileCreationOutbound.getName());
    }

    @Override
    public void execute() throws Exception {

        log.info(aMarker, "Asset Info Action for {} has been started", zipFileCreationOutbound.getName());

        final Jdbi jdbi = ResourceAccess.rdbmsJDBIConn(zipFileCreationOutbound.getResourceConn());
        final List<OutboundInputTableEntity> tableInfos = new ArrayList<>();

        String sourceOutputFolderPath = zipFileCreationOutbound.getOutputDir();
        jdbi.useTransaction(handle -> {
            final List<String> formattedQuery = CommonQueryUtil.getFormattedQuery(zipFileCreationOutbound.getQuerySet());
            AtomicInteger i = new AtomicInteger(0);
            formattedQuery.forEach(sqlToExecute -> {
                log.info(aMarker, "executing  query {} from index {}", sqlToExecute, i.getAndIncrement());
                Query query = handle.createQuery(sqlToExecute);
                ResultIterable<OutboundInputTableEntity> resultIterable = query.mapToBean(OutboundInputTableEntity.class);
                List<OutboundInputTableEntity> detailList = resultIterable.stream().collect(Collectors.toList());
                tableInfos.addAll(detailList);
                log.info(aMarker, "executed query from index {}", i.get());
            });
        });

        List<OutboundOutputTableEntity> outboundOutputTableEntities = new ArrayList<>();

        tableInfos.forEach(outboundInputTableEntity -> {


            String tenantPathStr = sourceOutputFolderPath + File.separator + outboundInputTableEntity.getTenantId() + File.separator;
            String sourcePdfName = outboundInputTableEntity.getFileName();


            final String originFolderPath = tenantPathStr + OUTBOUND_FILES + File.separator + outboundInputTableEntity.getOriginId() + File.separator + sourcePdfName + File.separator;

            final String originKvpFolderPath = tenantPathStr + File.separator + OUTBOUND_FILES + File.separator + outboundInputTableEntity.getOriginId() + File.separator + sourcePdfName + File.separator + "Kvp" + File.separator;
            final String originTableFolderPath = tenantPathStr + File.separator + OUTBOUND_FILES + File.separator + outboundInputTableEntity.getOriginId() + File.separator + sourcePdfName + File.separator + "Table" + File.separator;
            final String originZipPath = tenantPathStr + File.separator + "zip-files" + File.separator + outboundInputTableEntity.getOriginId() + File.separator + sourcePdfName + File.separator;
            String sourceCleanedPdfPath = outboundInputTableEntity.getCleanedPdfPath();
            String sourceOriginPdfPath = outboundInputTableEntity.getOriginPdfPath();

            String sourceJsonString = outboundInputTableEntity.getProductJson();
            String sourceKvpJsonString = outboundInputTableEntity.getKvpResponse();
            String sourceTableJsonString = outboundInputTableEntity.getTableResponse();
            String fileNameStr = outboundInputTableEntity.getFileName();

            createFolder(originFolderPath);
            createFolder(originKvpFolderPath);
            createFolder(originZipPath);

            createJsonFile(sourceJsonString, originFolderPath, sourcePdfName);
            createJsonFile(sourceKvpJsonString, originKvpFolderPath, sourcePdfName + "_kvp");

            copyFileIntoOrigin(sourceCleanedPdfPath, originFolderPath);
            copyFileIntoOrigin(sourceOriginPdfPath, originFolderPath);

            List<TruthPaperList> truthPaperList = getTruthPaperList(outboundInputTableEntity.getOriginId(), jdbi);
            truthPaperList.stream().filter(Objects::nonNull).forEach(truthPaperList1 -> {
                createFolder(originTableFolderPath);
                String originPaperTablePath = originTableFolderPath + File.separator + truthPaperList1.getPaperNo();
                createFolder(originPaperTablePath);

                createJsonFile(truthPaperList1.getTableResponse(), originPaperTablePath, sourcePdfName + "_" + truthPaperList1.getPaperNo() + "_table");

                copyFileIntoOrigin(truthPaperList1.getFilePath(), originPaperTablePath);
                String processedJsonNodePath = truthPaperList1.getProcessedFilePath();
                String cropppedImagePath = truthPaperList1.getCroppedimage();
                //copy table csv file into the output zip folder
                copyFileIntoOrigin(processedJsonNodePath, originPaperTablePath);
                copyFileIntoOrigin(cropppedImagePath, originPaperTablePath);


            });
            try {
                String outboundZipFilePath = createZipFile(originFolderPath, originZipPath, sourcePdfName);

                OutboundOutputTableEntity outboundOutputTableEntity = OutboundOutputTableEntity.builder()
                        .originId(outboundInputTableEntity.getOriginId())
                        .groupId(outboundInputTableEntity.getGroupId())
                        .rootPipelineId(outboundInputTableEntity.getRootPipelineId())
                        .processId(outboundInputTableEntity.getProcessId())
                        .cleanedPdfPath(outboundInputTableEntity.getCleanedPdfPath())
                        .originPdfPath(outboundInputTableEntity.getOriginPdfPath())
                        .productJson(outboundInputTableEntity.getProductJson())
                        .kvpResponse(outboundInputTableEntity.getKvpResponse())
                        .tableResponse(outboundInputTableEntity.getTableResponse())
                        .tenantId(outboundInputTableEntity.getTenantId())
                        .zipFilePath(outboundZipFilePath)
                        .alchemyOriginId(outboundInputTableEntity.getAlchemyOriginId())
                        .fileName(fileNameStr)
                        .stage("PRODUCT_OUTBOUND")
                        .status("COMPLETED")
                        .message("completed for the outbound zip file creation ")
                        .build();
                outboundOutputTableEntities.add(outboundOutputTableEntity);

            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            consumerBatch(jdbi, outboundOutputTableEntities);

        });


    }


    @Override
    public boolean executeIf() throws Exception {
        return zipFileCreationOutbound.getCondition();
    }

    public void consumerBatch(final Jdbi jdbi, List<OutboundOutputTableEntity> resultQueue) {
        try {
            resultQueue.forEach(insert -> {
                        jdbi.useTransaction(handle -> {
                            try {
                                handle.createUpdate("INSERT INTO " + zipFileCreationOutbound.getResultTable() + "(origin_id, root_pipeline_id,group_id,process_id,cleaned_pdf_path,origin_pdf_path,product_json,kvp_response,table_response,tenant_id,zip_file_path,status,stage,message,file_name,alchemy_origin_id)" +
                                                "VALUES(:originId,:rootPipelineId,:groupId,:processId,:cleanedPdfPath,:originPdfPath,:productJson,:kvpResponse,:tableResponse,:tenantId,:zipFilePath,:status,:stage,:message,:fileName,:alchemyOriginId);")
                                        .bindBean(insert).execute();
                                log.info(aMarker, "inserted {} into outbound zip file details", insert);
                            } catch (Throwable t) {
                                log.error(aMarker, "error inserting result into outbound file details {}", resultQueue, t);
                            }

                        });
                    }
            );
        } catch (Exception e) {
            log.error(aMarker, "error inserting result {}", resultQueue, e);
            HandymanException handymanException = new HandymanException(e);
            HandymanException.insertException("error inserting result" + resultQueue, handymanException, action);
        }
    }


    public void copyFileIntoOrigin(String inputFilePath, String outputDirectory) {
        Path sourcePath = Path.of(inputFilePath);

        File sourceFile = new File(sourcePath.toString());
        // Specify the target directory path
        Path targetDirectory = Path.of(outputDirectory);

        if (!Files.exists(targetDirectory)) {
            createFolder(targetDirectory.toString());
        }

        boolean isFileExists = sourceFile.exists();
        if (isFileExists) {
            try {
                // Use Files.copy() to copy the file to the target directory
                Path targetPath = targetDirectory.resolve(sourcePath.getFileName());
                Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);

                log.info("File copied from {} to {}", inputFilePath, outputDirectory);
            } catch (IOException e) {
                log.error("File copied failed for {} to {}", inputFilePath, outputDirectory);
                HandymanException handymanException = new HandymanException(e);
                HandymanException.insertException("failed in moving the file", handymanException, action);
                throw new HandymanException("Error in execute method for ner adapter", e, action);
            }
        } else {
            log.info("File not found from {} to {}", inputFilePath, outputDirectory);
        }
    }

    public void createFolder(String directoryPath) {

        File directoryFile = new File(directoryPath);

        if (!directoryFile.exists()) {
            if (directoryFile.mkdirs()) {
                log.info("Origin Directory created {}", directoryPath);

            } else {
                log.info("Origin Directory creation failed {}", directoryPath);
            }
        } else {
            log.info("Origin Directory already present {}", directoryPath);
        }

    }

    public String createZipFile(String inputFolder, String outputZipPath, String fileName) throws FileNotFoundException {

        String zipFileAbsolutePath;
        try {

            zipFileAbsolutePath = outputZipPath + File.separator + fileName + ".zip";
            FileOutputStream fos = new FileOutputStream(zipFileAbsolutePath);
            ZipOutputStream zipOut = new ZipOutputStream(fos);
            File fileToZip = new File(inputFolder);
            zipFile(fileToZip, fileToZip.getName(), zipOut);
            zipOut.close();
            fos.close();
        } catch (Exception e) {
            log.error(aMarker, "Error in execute method in create zip action {}", ExceptionUtil.toString(e));
            throw new HandymanException("Error in execute method in create zip action", e, action);
        }
        File file = new File(zipFileAbsolutePath);
        return file.getAbsolutePath();
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

    public void createJsonFile(String jsonString, String jsonPath, String jsonName) {

        String filePath = jsonPath + File.separator + jsonName + ".json";

        Path path = Paths.get(jsonPath);
        if (!Files.exists(path)) {
            createFolder(path.toString());
        }

        try (FileWriter fileWriter = new FileWriter(filePath)) {
            if (jsonString != null) {
                ObjectMapper objectMapper = new ObjectMapper();
                ObjectWriter writer = objectMapper.writerWithDefaultPrettyPrinter();
                String formattedJson = writer.writeValueAsString(objectMapper.readTree(jsonString));
                fileWriter.write(formattedJson);

            } else {
                log.info("No json content present in the response  {}", jsonString);
            }

            log.info("json response coverted to json file {}", jsonString);
        } catch (IOException e) {
            log.info("json file creation failed {}", filePath);

        }

    }

    public List<TruthPaperList> getTruthPaperList(String originId, Jdbi jdbi) {
        String querySet = "select a.file_path,sot.origin_id,sot.paper_no,ter.processed_file_path,atr.table_response ,ter.croppedimage " +
                "from info.source_of_truth sot" +
                " join info.asset a on sot.preprocessed_file_id  =a.file_id " +
                "join alchemy_response.alchemy_table_response atr on atr.pipeline_origin_id=sot.origin_id and atr.paper_no=sot.paper_no " +
                "join table_extraction.table_extraction_result ter on ter.origin_id =sot.origin_id and ter.paper_no =sot.paper_no " +
                "where sot.origin_id='" + originId + "';";
        List<TruthPaperList> tableInfos = new ArrayList<>();
        jdbi.useTransaction(handle -> {
            final List<String> formattedQuery = CommonQueryUtil.getFormattedQuery(querySet);
            AtomicInteger i = new AtomicInteger(0);
            formattedQuery.forEach(sqlToExecute -> {
                log.info(aMarker, "executing  query {} from index {}", sqlToExecute, i.getAndIncrement());
                Query query = handle.createQuery(sqlToExecute);
                ResultIterable<TruthPaperList> resultIterable = query.mapToBean(TruthPaperList.class);
                List<TruthPaperList> detailList = resultIterable.stream().collect(Collectors.toList());
                tableInfos.addAll(detailList);
                log.info(aMarker, "executed query from index {}", i.get());
            });
        });
        return tableInfos;

    }


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class TruthPaperList {

        private String paperNo;
        private String filePath;
        private String originId;
        private String processedFilePath;
        private String tableResponse;
        private String croppedimage;
    }
}
