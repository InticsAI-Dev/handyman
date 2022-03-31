package in.handyman.process.onethread

import java.io.BufferedReader
import java.io.FileReader
import java.sql.SQLException
import java.util.LinkedHashMap

import com.opencsv.CSVReader
import com.typesafe.scalalogging.LazyLogging

import in.handyman.command.CommandProxy
import in.handyman.command.Context
import in.handyman.util.ParameterisationEngine
import in.handyman.util.ResourceAccess


class LoadCsvIntoTableAction extends in.handyman.command.Action with LazyLogging {
  val detailMap = new java.util.HashMap[String, String]
  var dbConn : java.sql.Connection = null
  var stmt : java.sql.Statement = null
  var lineReader : BufferedReader = null
  
  def execute(context: Context, action: in.handyman.dsl.Action, actionId: Integer): Context = {
    val loadCsvIntoTableAsIs = action.asInstanceOf[in.handyman.dsl.LoadCsvIntoTable]
    val loadCsvIntoTable: in.handyman.dsl.LoadCsvIntoTable = CommandProxy.createProxy(loadCsvIntoTableAsIs, 
        classOf[in.handyman.dsl.LoadCsvIntoTable], context)

    try{
        val csvFile = loadCsvIntoTable.getCsvFile
        val targetDb = loadCsvIntoTable.getTargetDb
        val name = loadCsvIntoTable.getName
        val delim = loadCsvIntoTable.getDelim
        var limit = loadCsvIntoTable.getLimit.toInt
        val targetTable = loadCsvIntoTable.getTargetTable
        val loadWithCsvSchema = loadCsvIntoTable.getLoadWithCsvSchema
        
        dbConn = ResourceAccess.rdbmsConn(targetDb)
        dbConn.setAutoCommit(false)
        stmt = dbConn.createStatement
        val tableSchema : LinkedHashMap[String, String] = getTableSchema(targetTable)
        
        val reader: CSVReader = new CSVReader(new FileReader(csvFile))
        
        var nextLine: Array[String] = null
        val headerLine: Array[String] = reader.readNext()
        val csvSchema : LinkedHashMap[String, Int] = getCsvSchema(headerLine)
        
        var lineText : String = null;
        var rowCount : Int = 0;
        while ({ nextLine = reader.readNext(); nextLine != null }) {
          if(nextLine.size > 1) {
            var insertStmt : String = ""
            if(loadWithCsvSchema.equalsIgnoreCase("true"))
              insertStmt = getInsertStmtWithCsvSchema(targetTable, tableSchema, csvSchema, nextLine) 
            else 
              insertStmt = getInsertStmt(targetTable, tableSchema, nextLine) 
            
            stmt.addBatch(insertStmt)
            
            rowCount += 1
            if (rowCount % limit == 0) {
              stmt.executeBatch()
              stmt.clearBatch()
            }
          }
        }    
        
        // execute the remaining queries
        stmt.executeBatch();
        dbConn.commit();
    }catch {
      case ex: SQLException => {
        logger.error(s"LoadCSVIntoTable: SQL Exception", ex)
      }
      case ex: Throwable => {
        logger.error(s"LoadCSVIntoTable: throwable exception", ex)
      }
      case e: Exception => e.printStackTrace()
    } finally {
      if (lineReader != null) {
        lineReader.close()
      }
      if (stmt != null) {
        stmt.close()
      }
      if (dbConn != null) {
        dbConn.close()
      }
    }

    context
  }
  
  def getInsertStmt(tableName : String, tableSchema : LinkedHashMap[String, String], values : Array[String]) : String = {
    var insertStmt: StringBuilder = new StringBuilder()
    insertStmt.append("INSERT INTO ").append(tableName).append(Constants.INSERT_STMT_VALUE_START)
    
    var colList : StringBuilder = new StringBuilder()
    var colValList : StringBuilder = new StringBuilder()
    
    var colPosition : Int = 0;
    tableSchema.keySet().forEach(colName => {
      colList.append(colName).append(Constants.FIELD_SEPARATOR)
      
      var colVal : String = values.apply(colPosition).replace("'", "''");
      tableSchema.get(colName).toLowerCase match {
        case Constants.STRING_DATATYPE => colValList.append(Constants.STRING_ENCLOSER).
          append(colVal).append(Constants.STRING_ENCLOSER)
        case "datetime" => colValList.append(Constants.STRING_ENCLOSER).
          append(colVal).append(Constants.STRING_ENCLOSER)
        case "date" => colValList.append(Constants.STRING_ENCLOSER).
          append(colVal).append(Constants.STRING_ENCLOSER)
        case "timestamp" => colValList.append(Constants.STRING_ENCLOSER).
          append(colVal).append(Constants.STRING_ENCLOSER)
        case _ => colValList.append(colVal)
      }
      
      colValList.append(Constants.FIELD_SEPARATOR)
      
      colPosition += 1
    })
    
    insertStmt.append(colList.substring(0, colList.length-1)).append(") VALUES").append(Constants.INSERT_STMT_VALUE_START)
    insertStmt.append(colValList.substring(0, colValList.length-1)).append(Constants.INSERT_STMT_VALUE_END)

    return insertStmt.toString()
  }
  
  def getInsertStmtWithCsvSchema(tableName : String, tableSchema : LinkedHashMap[String, String], csvSchema : LinkedHashMap[String, Int], 
      values : Array[String]) : String = {
    var insertStmt: StringBuilder = new StringBuilder()
    insertStmt.append("INSERT INTO ").append(tableName).append(Constants.INSERT_STMT_VALUE_START)
    
    var colList : StringBuilder = new StringBuilder()
    var colValList : StringBuilder = new StringBuilder()
    
    tableSchema.keySet().forEach(colName => {
      val colPos = csvSchema.get(colName)
      
      colList.append(colName).append(Constants.FIELD_SEPARATOR)
      
      var colVal : String = values.apply(colPos).replace("'", "''");
      tableSchema.get(colName).toLowerCase match {
        case Constants.STRING_DATATYPE => colValList.append(Constants.STRING_ENCLOSER).
          append(colVal).append(Constants.STRING_ENCLOSER)
        case "datetime" => colValList.append(Constants.STRING_ENCLOSER).
          append(colVal).append(Constants.STRING_ENCLOSER)
        case "date" => colValList.append(Constants.STRING_ENCLOSER).
          append(colVal).append(Constants.STRING_ENCLOSER)
        case "timestamp" => colValList.append(Constants.STRING_ENCLOSER).
          append(colVal).append(Constants.STRING_ENCLOSER)
        case _ => colValList.append(colVal)
      }
      
      colValList.append(Constants.FIELD_SEPARATOR)
    })
    
    insertStmt.append(colList.substring(0, colList.length-1)).append(") VALUES").append(Constants.INSERT_STMT_VALUE_START)
    insertStmt.append(colValList.substring(0, colValList.length-1)).append(Constants.INSERT_STMT_VALUE_END)

    return insertStmt.toString()
  }
  
  def getTableSchema(tableName : String): LinkedHashMap[String, String] = {
    var tableSchema : LinkedHashMap[String, String] = new LinkedHashMap[String, String]();
    val schemaSQL : String = "SELECT COLUMN_NAME, DATA_TYPE FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = '"+ tableName + 
                              "' ORDER BY ORDINAL_POSITION;"
    val rs : java.sql.ResultSet = stmt.executeQuery(schemaSQL)
    
    if(rs != null) {
      while(rs.next()){
        tableSchema.put(rs.getString("COLUMN_NAME"), rs.getString("DATA_TYPE"))
      }
    }
    
    return tableSchema 
  }
  
  def getCsvSchema(headerLine : Array[String]): LinkedHashMap[String, Int] = {
    var csvSchema : LinkedHashMap[String, Int] = new LinkedHashMap[String, Int]();
    
    if(headerLine != null) {
      var colPosition : Int = 0;
      headerLine.foreach(headerCol => {
        csvSchema.put(headerCol, colPosition)
        colPosition = colPosition + 1
      })
    }
    
    return csvSchema 
  }
  
  def executeIf(context: Context, action: in.handyman.dsl.Action): Boolean = {
    val loadCsvIntoTableAsIs = action.asInstanceOf[in.handyman.dsl.LoadCsvIntoTable]
    val loadCsvIntoTable: in.handyman.dsl.LoadCsvIntoTable = CommandProxy.createProxy(loadCsvIntoTableAsIs, 
        classOf[in.handyman.dsl.LoadCsvIntoTable], context)

    val expression = loadCsvIntoTable.getCondition
    try {
      val output = ParameterisationEngine.doYieldtoTrue(expression)
      detailMap.putIfAbsent("condition-output", output.toString())
      output
    } finally {
      if (expression != null)
        detailMap.putIfAbsent("condition", "LHS=" + expression.getLhs + ", Operator=" + expression.getOperator + ", RHS=" + expression.getRhs)
    }

  }

  def generateAudit(): java.util.Map[String, String] = {
    detailMap
  }
}