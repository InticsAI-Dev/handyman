package in.handyman.raven.lib;


import in.handyman.raven.exception.HandymanException;
import in.handyman.raven.lambda.access.ResourceAccess;
import in.handyman.raven.lambda.action.ActionExecution;
import in.handyman.raven.lambda.action.IActionExecution;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.model.SystemkeyTable;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

import org.jdbi.v3.core.Jdbi;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;



/**
 * Auto Generated By Raven
 */
@ActionExecution(
    actionName = "SystemkeyTable"
)
public class SystemkeyTableAction implements IActionExecution {
  private final ActionExecutionAudit action;

  private final Logger log;

  private final SystemkeyTable systemkeyTable;

  private final Marker aMarker;

  public SystemkeyTableAction(final ActionExecutionAudit action, final Logger log,
      final Object systemkeyTable) {
    this.systemkeyTable = (SystemkeyTable) systemkeyTable;
    this.action = action;
    this.log = log;
    this.aMarker = MarkerFactory.getMarker(" SystemkeyTable:"+this.systemkeyTable.getName());
  }

  @Override
  public void execute() throws Exception {
    try{
      log.info(aMarker, "System_key table generation has been started", systemkeyTable.getName());
      final Jdbi jdbi = ResourceAccess.rdbmsJDBIConn(systemkeyTable.getResourceConn());
      final String query = systemkeyTable.getQuerySet();

      jdbi.withHandle(handle -> {
        handle.execute("CREATE SCHEMA IF NOT EXISTS md_lookup_systemkey");
        handle.createQuery(query)
                .mapToMap()
                .list()
                .forEach(row -> {
                    String tableName = (String)row.get("sor_item_name");
                    String systemKeys = (String) row.get("all_system_keys");

                    // Convert the comma-separated string to a list of system keys
                    List<String> systemKeysList = Arrays.asList(systemKeys.split("\\s*,\\s*"));

                    int length = systemKeysList.size();

                    try {
                        createTable(handle, tableName, systemKeysList);
                    } catch (SQLException e) {
                        throw new HandymanException("Error in system key table creation action", e, action);
                    }
                });
          return null;
      });

    }
    catch (Exception e) {
        log.error(aMarker, "Error in systemkey Table Generation", e);
        throw new RuntimeException("Error in systemkey Table Generation",e);
    }
  }

  private void createTable(org.jdbi.v3.core.Handle handle,String tableName, List<String> systemKeysList) throws SQLException{
      StringBuilder columns = new StringBuilder();
      for (String systemKey : systemKeysList){
          //Appending each system key as a column
          columns.append("sk_").append(systemKey).append(" VARCHAR NULL, ");
      }
      String createTableSQL = "CREATE TABLE IF NOT EXISTS md_lookup_systemkey." +tableName +
              "(origin_id VARCHAR NULL," +
              "paper_no INT4 NULL," +
              "eoc_identifier VARCHAR NULL," +
              "group_id INT4 NULL," +
              "batch_id varchar NULL," +
              tableName + " VARCHAR NULL," +
              columns +
              "root_pipeline_id INT8 NULL," +
              "tenant_id INT NULL);\n" +
              "\nCREATE TABLE IF NOT EXISTS md_lookup_systemKey."+tableName +"_match" +
              "(origin_id VARCHAR NULL," +
              "paper_no INT4 NULL," +
              "eoc_identifier VARCHAR NULL," +
              "created_on timestamp NULL," +
              "extracted_value varchar NULL," +
              "actual_value varchar NULL," +
              "intelli_match float4 NULL," +
              "status varchar null," +
              "stage varchar null," +
              "message varchar null," +
              "root_pipeline_id int8 null," +
              "model_name varchar NULL," +
              "model_version varchar NULL," +
              "batch_id varchar NULL," +
              "tenant_id int8 NULL);" ;
      handle.execute(createTableSQL);
      System.out.println("Table '"+ tableName+ "' has been created.");
      System.out.println("Table '" + tableName + "_match' has been created");
  }


  @Override
  public boolean executeIf() throws Exception {
    return systemkeyTable.getCondition();
  }
}
