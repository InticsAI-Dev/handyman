package in.handyman.raven.lib;

import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.access.ResourceAccess;
import in.handyman.raven.lambda.action.ActionExecution;
import in.handyman.raven.lambda.action.IActionExecution;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.DbDataDart;

import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import in.handyman.raven.util.CommonQueryUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.argument.Arguments;
import org.jdbi.v3.core.argument.NullArgument;
import org.jdbi.v3.core.result.ResultIterable;
import org.jdbi.v3.core.statement.Query;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;
import org.jdbi.v3.core.transaction.TransactionException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

/**
 * Auto Generated By Raven
 */
@ActionExecution(
        actionName = "DbDataDart"
)
public class DbDataDartAction implements IActionExecution {
  private final ActionExecutionAudit action;

  private final Logger log;

  private final DbDataDart dbDataDart;

  private final Marker aMarker;

  public DbDataDartAction(final ActionExecutionAudit action, final Logger log,
                          final Object dbDataDart) {
    this.dbDataDart = (DbDataDart) dbDataDart;
    this.action = action;
    this.log = log;
    this.aMarker = MarkerFactory.getMarker(" DbDataDart:" + this.dbDataDart.getName());
  }

  @Override
  public void execute() throws Exception {
    final Jdbi jdbi = ResourceAccess.rdbmsJDBIConn(dbDataDart.getResourceConn());
    jdbi.getConfig(Arguments.class).setUntypedNullArgument(new NullArgument(Types.NULL));
    log.info(aMarker, "Database truncate action {} has been started ", dbDataDart.getName());

    final List<DataBaseTruncateInputTable> dataBaseTruncateInputTableList = getDataBaseTruncateInputTables(jdbi);

    final List<String> schemaTableNamesList = getSchemaTableNamesList(dataBaseTruncateInputTableList, jdbi);

    // Get a single connection for all truncation operations
    jdbi.useHandle(handle -> {
      final Connection connection = handle.getConnection();
      connection.setAutoCommit(false); // Disable auto-commit to start a transaction

      for (String schemaTableName : schemaTableNamesList) {
        final String[] parts = schemaTableName.split("\\.");
        final String schemaName = parts[0];
        final String truncateQuery = String.format("TRUNCATE TABLE %s CASCADE", schemaTableName);

        try (final Statement stmt = connection.createStatement()) {
          performTruncation(schemaTableName, stmt, truncateQuery, jdbi, dataBaseTruncateInputTableList, schemaName);
        } catch (SQLException e) {
          log.error(aMarker, "Error truncating table {}: {}", schemaTableName, e.getMessage());
          throw new TransactionException(e); // Rethrow the exception to mark the transaction as failed
        }
      }
      // Commit the transaction
      connection.commit();
      // Enable auto-commit
      connection.setAutoCommit(true);
    });
  }

  private void performTruncation(String schemaTableName, Statement stmt, String truncateQuery, Jdbi jdbi, List<DataBaseTruncateInputTable> dataBaseTruncateInputTableList, String schemaName) throws SQLException {
    final String getRowCount = String.format("SELECT count(*) FROM %s", schemaTableName);
    final ResultSet rs = stmt.executeQuery(getRowCount);
    long truncatedRowCount = 0;
    if (rs.next()) {
      truncatedRowCount = rs.getLong(1); // Get the value of the first column in the result set
      log.debug(aMarker, "Row Count: {}", truncatedRowCount); // Logging the row count
    } else {
      log.debug(aMarker, "No rows returned {}", truncatedRowCount); // Logging when no rows are returned
    }
    // Truncate the table
    stmt.executeUpdate(truncateQuery);
    log.debug(aMarker, "Truncated {} rows from table {}", truncatedRowCount, schemaTableName);
    handleResponse(jdbi, dataBaseTruncateInputTableList, truncatedRowCount, schemaName, schemaTableName);
  }


  private @NotNull List<String> getSchemaTableNamesList(List<DataBaseTruncateInputTable> dataBaseTruncateInputTableList, Jdbi jdbi) {
    final List<String> schemaTableNamesList = new ArrayList<>(); // List to store schema.table_name strings

    // Iterate over DataBaseTruncateInputTable objects
    for (DataBaseTruncateInputTable inputTable : dataBaseTruncateInputTableList) {
      final List<String> truncateSchemaList = inputTable.getTruncateSchemaList();
      final List<String> excludeTableWithSchemaList = inputTable.getExcludeTableWithSchemaList();
      for (String schema : truncateSchemaList) {
        String query = "SELECT table_name FROM information_schema.tables " +
                "WHERE table_schema = :schema AND table_type = 'BASE TABLE'";
        List<String> tables = jdbi.withHandle(handle ->
                handle.createQuery(query)
                        .bind("schema", schema)
                        .mapTo(String.class)
                        .list());
        // Add schema.table_name strings to the schemaTableNamesList
        tables.forEach(tableName -> schemaTableNamesList.add(schema + "." + tableName));
      }
      if (!excludeTableWithSchemaList.isEmpty() &&
              Boolean.parseBoolean(action.getContext().get("db.truncate.exclude.table.list"))) {
        schemaTableNamesList.removeAll(excludeTableWithSchemaList);
      }
    }
    return schemaTableNamesList;
  }


  private @NotNull List<DataBaseTruncateInputTable> getDataBaseTruncateInputTables(Jdbi jdbi) {
    final List<DataBaseTruncateInputTable> dataBaseTruncateInputTableList = new ArrayList<>();

    // Execute SQL queries to get backup information
    jdbi.useTransaction(handle -> {
      final List<String> formattedQuery = CommonQueryUtil.getFormattedQuery(dbDataDart.getQuerySet());
      AtomicInteger atomicInteger = new AtomicInteger(0);
      for (String sqlToExecute : formattedQuery) {
        log.info(aMarker, "Executing query {} from index {}", sqlToExecute, atomicInteger.getAndIncrement());
        Query query = handle.createQuery(sqlToExecute);
        ResultIterable<DataBaseTruncateInputTable> dataBaseTruncateInputTables = query.mapToBean(DataBaseTruncateInputTable.class);
        dataBaseTruncateInputTableList.addAll(dataBaseTruncateInputTables.stream().collect(Collectors.toList()));
        log.info(aMarker, "Executed query from index {}", atomicInteger.get());
      }
    });
    return dataBaseTruncateInputTableList;
  }


  private void handleResponse(final Jdbi jdbi, final List<DataBaseTruncateInputTable> dataBaseTruncateInputTable,
                              final Long totalRowCount, final String schemaName,
                              final String tableName) {

    final DataBaseTruncateOutputTable dataBaseTruncateOutputTable = new DataBaseTruncateOutputTable();
    final LocalDateTime localDateTime = LocalDateTime.now();
    final String rowsPresentedStatus = totalRowCount > 0 ? "ROWS_PRESENT" : "ROWS_ABSENT";

    dataBaseTruncateInputTable.forEach(dataBaseTruncateInputTableMap -> {
      dataBaseTruncateOutputTable.setGroupId(dataBaseTruncateInputTableMap.getGroupId());
      dataBaseTruncateOutputTable.setRootPipelineId(dataBaseTruncateInputTableMap.getRootPipelineId());
      dataBaseTruncateOutputTable.setProcessId(dataBaseTruncateInputTableMap.getProcessId());
      dataBaseTruncateOutputTable.setTenantId(dataBaseTruncateInputTableMap.getTenantId());
      dataBaseTruncateOutputTable.setCreatedOn(localDateTime);
      dataBaseTruncateOutputTable.setSchemaName(schemaName);
      dataBaseTruncateOutputTable.setTableName(tableName);
      dataBaseTruncateOutputTable.setRowPresentStatus(rowsPresentedStatus);
      dataBaseTruncateOutputTable.setTotalRowCount(totalRowCount);

      try {
        jdbi.useHandle(handle -> {
          String sql = "INSERT INTO sanitary_hub.db_data_truncate_audit (" +
                  "created_on, schema_name, table_name, row_presented_status, group_id, process_id, " +
                  "root_pipeline_id, tenant_id, total_row_count) " +
                  "VALUES (:createdOn, :schemaName, :tableName, :rowPresentStatus, :groupId, :processId, " +
                  ":rootPipelineId, :tenantId, :totalRowCount)";

          handle.createUpdate(sql)
                  .bindBean(dataBaseTruncateOutputTable)
                  .execute();
        });
      } catch (UnableToExecuteStatementException exception) {
        log.error(aMarker, "Exception occurred in database truncate audit insert: {}", exception.getMessage(), exception);
        HandymanException handymanException = new HandymanException(exception);
        HandymanException.insertException("Exception occurred in database truncate audit insert -  " +
                dataBaseTruncateOutputTable.getTableName(), handymanException, this.action);
      }
    });
  }


  @AllArgsConstructor
  @NoArgsConstructor
  @Data
  public static class DataBaseTruncateInputTable {
    private List<String> truncateSchemaList;
    private List<String> excludeTableWithSchemaList;
    private Integer groupId;
    private Long tenantId;
    private Long processId;
    private Long rootPipelineId;
  }


  @AllArgsConstructor
  @NoArgsConstructor
  @Data
  public static class DataBaseTruncateOutputTable {
    private LocalDateTime createdOn;
    private String schemaName;
    private String tableName;
    private String rowPresentStatus;
    private Long processId;
    private Long rootPipelineId;
    private Long tenantId;
    private Integer groupId;
    private Long totalRowCount;
  }


  @Override
  public boolean executeIf() throws Exception {
    return dbDataDart.getCondition();
  }
}