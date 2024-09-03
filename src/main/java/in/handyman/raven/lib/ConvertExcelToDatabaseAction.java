package in.handyman.raven.lib;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.access.ResourceAccess;
import in.handyman.raven.lambda.action.ActionExecution;
import in.handyman.raven.lambda.action.IActionExecution;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lambda.doa.audit.ExecutionStatus;
import in.handyman.raven.lib.model.ConvertExcelToDatabase;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import in.handyman.raven.lib.prompt.generation.SanitarySummary;
import in.handyman.raven.util.CommonQueryUtil;
import in.handyman.raven.util.ExceptionUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.result.ResultIterable;
import org.jdbi.v3.core.statement.Query;
import org.jdbi.v3.core.statement.Update;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

/**
 * Auto Generated By Raven
 */
@ActionExecution(
        actionName = "ConvertExcelToDatabase"
)
public class ConvertExcelToDatabaseAction implements IActionExecution {
  private final ActionExecutionAudit action;

  private final Logger log;

  private final ConvertExcelToDatabase convertExcelToDatabase;

  private final Marker aMarker;

  private final String SCHEMA_NAME="meta_bootstraping";

  public ConvertExcelToDatabaseAction(final ActionExecutionAudit action, final Logger log,
                                      final Object convertExcelToDatabase) {
    this.convertExcelToDatabase = (ConvertExcelToDatabase) convertExcelToDatabase;
    this.action = action;
    this.log = log;
    this.aMarker = MarkerFactory.getMarker(" ConvertExcelToDatabase:"+this.convertExcelToDatabase.getName());
  }

  @Override
  public void execute() throws Exception {


    log.info(aMarker, "Convert excel to database Action for {} has been started", convertExcelToDatabase.getName());

    final Jdbi jdbi = ResourceAccess.rdbmsJDBIConn(convertExcelToDatabase.getResourceConn());
    final Jdbi targetJdbi = ResourceAccess.rdbmsJDBIConn(convertExcelToDatabase.getTargetConn());
    List<OutputQuerySet> outputQuerySets=new ArrayList<>();


    final List<QueryResult> tableInfos = new ArrayList<>();

    jdbi.useTransaction(handle -> {
      final List<String> formattedQuery = CommonQueryUtil.getFormattedQuery(convertExcelToDatabase.getQuerySet());
      AtomicInteger i = new AtomicInteger(0);
      formattedQuery.forEach(sqlToExecute -> {
        log.info(aMarker, "executing  query {} from index {}", sqlToExecute, i.getAndIncrement());
        Query query = handle.createQuery(sqlToExecute);
        ResultIterable<QueryResult> resultIterable = query.mapToBean(QueryResult.class);
        List<QueryResult> detailList = resultIterable.stream().collect(Collectors.toList());
        tableInfos.addAll(detailList);
        log.info(aMarker, "executed query from index {}", i.get());
      });
    });

    log.info("number of rows returned from query set {}",tableInfos.size());

    tableInfos.forEach(queryResult -> {
      List<Path> pathList=readExcelFolder(queryResult.getFilePath());
      pathList.forEach(path -> {

        excelToDatabase(targetJdbi,path.toString(),outputQuerySets,path,queryResult);



       // outputQuerySets.addAll(outputQuerySet);
      });
    });

    consumerBatch(targetJdbi,outputQuerySets);
  }


  public List<Path> readExcelFolder(String filePathString){
    List<Path> pathList = new ArrayList<>();

    try (var files = Files.walk(Path.of(filePathString)).filter(Files::isRegularFile)) {
      log.info(aMarker, "Iterating each file in directory {}", files);
      files.forEach(pathList::add);
    } catch (IOException e) {
      log.error(aMarker, "Exception occurred in directory iteration {}", ExceptionUtil.toString(e));
      throw new HandymanException("Exception occurred in directory iteration", e, action);
    }

    return pathList;
  }

  public String generateTableName(File file,String rootPipelineId,String placeholder){
    String fileName= FilenameUtils.removeExtension(file.getName().replace("-","_")) + "_"+placeholder+"_" + rootPipelineId;
    String schemaName =SCHEMA_NAME+"."+fileName;

    return schemaName;
  }
  public List<OutputQuerySet> excelToDatabase(Jdbi targetJdbi,String excelFilePath,List<OutputQuerySet> outputQuerySets,Path path,QueryResult queryResult){

    String tableName1 = generateTableName(path.toFile(), String.valueOf(queryResult.getRootPipelineId()),"question");
    String tableName2 = generateTableName(path.toFile(), String.valueOf(queryResult.getRootPipelineId()),"placeholder");
    try {
      // Load Excel workbook
      FileInputStream fis = new FileInputStream(excelFilePath);
      Workbook workbook = new XSSFWorkbook(fis);
      Sheet sheet1 = workbook.getSheetAt(0); // Assuming data is in the first sheet


      extractSheetWiseData(targetJdbi, tableName1, sheet1);

      Sheet sheet2 = workbook.getSheetAt(1);
      extractSheetWiseData(targetJdbi, tableName2, sheet2);

      outputQuerySets.add(
      OutputQuerySet.builder()
              .filePath(path.toString())
              .tableName(tableName1)
              .createdOn(Timestamp.valueOf(LocalDateTime.now()))
              .lastUpdatedOn(Timestamp.valueOf(LocalDateTime.now()))
              .createdUserId(queryResult.getTenantId())
              .lastUpdatedUserId(queryResult.tenantId)
              .rootPipelineId(queryResult.getRootPipelineId())
              .status(ExecutionStatus.COMPLETED.name())
              .tenantId(queryResult.getTenantId())
              .metaMapping("QUESTION")
              .build());

      outputQuerySets.add(
              OutputQuerySet.builder()
                      .filePath(path.toString())
                      .tableName(tableName2)
                      .createdOn(Timestamp.valueOf(LocalDateTime.now()))
                      .lastUpdatedOn(Timestamp.valueOf(LocalDateTime.now()))
                      .createdUserId(queryResult.getTenantId())
                      .lastUpdatedUserId(queryResult.tenantId)
                      .rootPipelineId(queryResult.getRootPipelineId())
                      .status(ExecutionStatus.COMPLETED.name())
                      .tenantId(queryResult.getTenantId())
                      .metaMapping("PLACEHOLDER")
                      .build());


      return outputQuerySets;

    } catch (IOException e) {
      HandymanException.insertException("Error in file reading for load the excel",new HandymanException(e),action);

    }

      return outputQuerySets;
  }

  private void extractSheetWiseData(Jdbi targetJdbi, String tableName, Sheet sheet) {
    // Database connection details
//      Jdbi targetJdbi =  ResourceAccess.rdbmsJDBIConn(targetDatabase);

    StringBuilder createTableQuery = new StringBuilder("CREATE TABLE IF NOT EXISTS ")
            .append(tableName)
            .append(" (");

    Row headerRow = sheet.getRow(0);
    for (int i = 0; i < headerRow.getLastCellNum(); i++) {
      String columnName = headerRow.getCell(i).getStringCellValue();
      createTableQuery.append(columnName).append(" TEXT");
      if (i < headerRow.getLastCellNum() - 1) {
        createTableQuery.append(", ");
      }
    }
    createTableQuery.append(")");

    targetJdbi.useHandle(handle -> handle.execute(createTableQuery.toString()));

    // Insert data into the database table
    String insertQuery = "INSERT INTO " + tableName + " VALUES (";

    for (Row row : sheet) {
      if (row.getRowNum() == 0) continue; // Skip header row

      StringBuilder values = new StringBuilder();
      for (int i = 0; i < row.getLastCellNum(); i++) {
        Cell cell = row.getCell(i);
        String cellValue = (cell == null) ? "" : cell.toString();
        values.append("'").append(cellValue).append("'");
        if (i < row.getLastCellNum() - 1) {
          values.append(", ");
        }
      }

      String fullInsertQuery = insertQuery + values + ")";
      targetJdbi.useHandle(handle -> {
        int i=handle.execute(fullInsertQuery);

        log.info("handle executor i {}",i);
      });


      log.info("insert into the database insert query {}",fullInsertQuery);
    }

    log.info("Excel data imported into PostgreSQL table successfully.");
  }


  void consumerBatch(final Jdbi jdbi, List<OutputQuerySet> resultQueue) {
    Long tenantId = Long.valueOf(action.getContext().get("tenant_id"));
    try {
      resultQueue.forEach(insert -> {
                jdbi.useTransaction(handle -> {
                  try {
                    handle.createUpdate("INSERT INTO meta_bootstraping.import_excel_output_table(group_id, tenant_id, root_pipeline_id, created_on, created_user_id, file_path, table_name, last_updated_on, last_updated_user_id, status,version,meta_mapping)" +
                                    "VALUES(:groupId, :tenantId, :rootPipelineId, :createdOn, :createdUserId, :filePath, :tableName, :lastUpdatedOn, :lastUpdatedUserId, :status,:version,:metaMapping);")
                            .bindBean(insert).execute();
                    log.info(aMarker, "inserted {} into excel export output table", insert);
                  } catch (Throwable t) {
                    insertSummaryAudit(jdbi, 0, 0, 1, "failed in batch for " + insert.getTableName(), tenantId);
                    log.error(aMarker, "error inserting result {}", resultQueue, t);
                  }

                });
              }
      );
    } catch (Exception e) {
      insertSummaryAudit(jdbi, 0, 0, resultQueue.size(), "failed in batch insert", tenantId);
      log.error(aMarker, "error inserting result {}", resultQueue, e);
      HandymanException handymanException = new HandymanException(e);
      HandymanException.insertException("error inserting result" + resultQueue, handymanException, action);
    }
  }

  void insertSummaryAudit(final Jdbi jdbi, int rowCount, int executeCount, int errorCount, String comments, Long tenantId) {
    try {
      SanitarySummary summary = new SanitarySummary().builder()
              .rowCount(rowCount)
              .correctRowCount(executeCount)
              .errorRowCount(errorCount)
              .comments(comments)
              .tenantId(tenantId)
              .build();
      jdbi.useTransaction(handle -> {
        Update update = handle.createUpdate("  INSERT INTO meta_bootstrapping.final_sanitizer_summary_audit"  +
                " ( row_count, correct_row_count, error_row_count,comments, created_at,tenant_id) " +
                " VALUES(:rowCount, :correctRowCount, :errorRowCount, :comments, NOW(),:tenantId);");
        Update bindBean = update.bindBean(summary);
        bindBean.execute();
      });
    } catch (Exception exception) {
      log.error(aMarker, "error inserting into batch insert audit  {}", ExceptionUtil.toString(exception));
      HandymanException handymanException = new HandymanException(exception);
      HandymanException.insertException("error inserting into batch insert audit", handymanException, action);

    }
  }



  @AllArgsConstructor
  @NoArgsConstructor
  @Data
  @Builder
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class QueryResult {
    private int migrationId;
    private Long tenantId;
    private Long groupId;
    private String filePath;
    private Long rootPipelineId;
  }


  @AllArgsConstructor
  @NoArgsConstructor
  @Data
  @Builder
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class OutputQuerySet {
    private Long groupId;
    private Long tenantId;
    private Long rootPipelineId;
    private Timestamp createdOn;
    private Long createdUserId ;
    private String filePath ;
    private String tableName;
    private Timestamp lastUpdatedOn;
    private Long lastUpdatedUserId ;
    private String status;
    private Long version ;
    private String metaMapping;

  }
  @Override
  public boolean executeIf() throws Exception {
    return convertExcelToDatabase.getCondition();
  }
}
