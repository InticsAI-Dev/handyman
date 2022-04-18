package in.handyman.process.onethread

import java.io.FileReader
import java.sql.SQLException
import java.sql.Statement
import java.util.Date
import java.util.logging.Level
import java.util.logging.Logger

import org.slf4j.MarkerFactory

import com.opencsv.CSVReader
import com.typesafe.scalalogging.LazyLogging

import in.handyman.command.CommandProxy
import in.handyman.command.Context
import in.handyman.util.ParameterisationEngine
import in.handyman.util.ResourceAccess

class LoadCsvIntoDbAction extends in.handyman.command.Action with LazyLogging {
  val detailMap = new java.util.HashMap[String, String]
  val auditMarker = "DROPBOX";
  val aMarker = MarkerFactory.getMarker(auditMarker);

  def execute(context: Context, action: in.handyman.dsl.Action, actionId: Integer): Context = {
    val loadcsvAsIs = action.asInstanceOf[in.handyman.dsl.LoadCsv]
    val loadcsv: in.handyman.dsl.LoadCsv = CommandProxy.createProxy(loadcsvAsIs, classOf[in.handyman.dsl.LoadCsv], context)

    val date = new Date
    val pid = loadcsv.getPid
    val csvFile = loadcsv.getSource
    val db = loadcsv.getTo
    val name = loadcsv.getName
    val id = context.getValue("process-id")
    val sqlList = loadcsv.getValue.replaceAll("\"", "")
    val delim = loadcsv.getDelim
    var limit = loadcsv.getLimit.toInt
    var fileName = ""
    if (csvFile.contains("\\")) {
      val counter: Int = csvFile.length - csvFile.replaceAll("\\\\", "").length
      val file: Array[String] = csvFile.split("\\\\", counter + 1)
      fileName = file(counter)
    }else {
      val counter: Int = csvFile.length - csvFile.replaceAll("\\/", "").length
      val file: Array[String] = csvFile.split("/", counter + 1)
      fileName = file(counter)
    }

    var count: Int = 0
    var st: Statement = null
    var totalcount: Int = 0
    var values: String = null
    var cquery: String = ""
    var iquery: String = ""
    var dquery: String = ""
    var ct: String = ""

    logger.info("LoadCsv id#{}, name#{}, from#{}, sqlList#{}", id, name, db)

    val con = ResourceAccess.rdbmsConn(db)
    st = con.createStatement()
    con.setAutoCommit(false)

    try {
      val reader: CSVReader = new CSVReader(new FileReader(csvFile))
      var nextLine: Array[String] = null
      val firstLine: Array[String] = reader.readNext()
      var atos: String = firstLine.mkString("")
      atos = atos.replace("\t", ",")
      var stoa: Array[String] = atos.split(",")
      if (fileName.contains(".csv")) {
        val column: String = convertArrayToInsertLine(firstLine, "`,`")
        ct = convertArrayToInsertLine(firstLine, "`VARCHAR(50),`")
        dquery = "drop table if exists `" + pid + "_" + fileName.replace(".csv", "") + "`;"
        logger.info("LoadCsv id#{}, name#{}, from#{}, sqlList#{}", id, name, db, dquery)
        st.execute(dquery)
        cquery = "CREATE TABLE `" + pid + "_" + fileName.replace(".csv", "") + "` (" + "`" + ct + ");"
        logger.info("LoadCsv id#{}, name#{}, from#{}, sqlList#{}", id, name, db, cquery)
        st.execute(cquery)

        while ({ nextLine = reader.readNext(); nextLine != null }) {
          values = convertArrayToInsertLine(nextLine, "','")
          count += 1
          totalcount += 1
          values = values.replace("''", "'NULL'").replace("00:00:00.0", "")
          iquery = iquery + "('" + values + "),"

          if (count % limit == 0) {
            iquery = "INSERT IGNORE INTO  `" + pid + "_" + fileName.replace(".csv", "") + "`  (" + "`" + column + ")" + "VALUES " + iquery
            iquery = iquery.substring(0, iquery.length() - 1) + ";"
            logger.info("LoadCsv id#{}, name#{}, from#{}, sqlList#{}", id, name, db, iquery)
            st.execute(iquery)
            iquery = ""
            con.commit()
          }
        }
        iquery = "INSERT IGNORE INTO  `" + pid + "_" + fileName.replace(".csv", "") + "`  (" + "`" + column + ")" + "VALUES " + iquery
        iquery = iquery.substring(0, iquery.length() - 1) + ";"
        logger.info("LoadCsv id#{}, name#{}, from#{}, sqlList#{}", id, name, db, iquery)
        st.execute(iquery)
        con.commit()
      } else if (fileName.contains(".tsv")) {
        val column: String = convertArrayToInsertLine(stoa, "`,`")
        ct = convertArrayToInsertLine(stoa, "`VARCHAR(344),`")
        dquery = "drop table if exists " + pid + "_" + fileName.replace(".tsv", "") + ";"
        logger.info("LoadCsv id#{}, name#{}, from#{}, sqlList#{}", id, name, db, dquery)
        st.execute(dquery)
        cquery = "CREATE TABLE `" + pid + "_" + fileName.replace(".tsv", "") + "` (" + "`" + ct + ");"
        logger.info("LoadCsv id#{}, name#{}, from#{}, sqlList#{}", id, name, db, cquery)
        st.execute(cquery)

        while ({ nextLine = reader.readNext(); nextLine != null }) {
          var row: String = nextLine.mkString("")
          row = row.replace("\t", "~ ").replace("\"\"", "\\\"")
          val rowa: Array[String] = row.split("~")
          values = convertArrayToInsertLine(rowa, "','")
          count += 1
          totalcount += 1
          values = values.replace("''", "'NULL'").replace("00:00:00.0", "")
          iquery = iquery + "('" + values + "),"
          if (count % limit == 0) {
            iquery = "INSERT IGNORE INTO  `" + pid + "_" + fileName.replace(".tsv", "") + "`  (" + "`" + column + ")" + "VALUES " + iquery
            iquery = iquery.substring(0, iquery.length() - 1) + ";"
            logger.info("LoadCsv id#{}, name#{}, from#{}, sqlList#{}", id, name, db, iquery)
            st.execute(iquery)
            iquery = ""
            con.commit()
          }
        }
        iquery = "INSERT IGNORE INTO  `" + pid + "_" + fileName.replace(".tsv", "") + "`  (" + "`" + column + ")" + "VALUES " + iquery
        iquery = iquery.substring(0, iquery.length() - 1) + ";"
        logger.info("LoadCsv id#{}, name#{}, from#{}, sqlList#{}", id, name, db, iquery)
        st.execute(iquery)
        con.commit()
      } else {
        logger.info("File format is invalid")
      }
    } catch {
      case ex: SQLException => {
        val lgr: Logger = Logger.getLogger(classOf[App].getName)
        lgr.log(Level.SEVERE, ex.getMessage, ex)
      }
      case e: Exception => e.printStackTrace()
    } finally try {
      if (st != null) {
        st.close()
      }
      if (con != null) {
        con.close()
      }
    } catch {
      case ex: SQLException => {
        val lgr: Logger = Logger.getLogger(classOf[App].getName)
        lgr.log(Level.WARNING, ex.getMessage, ex)
      }
    }
    println(totalcount)
    context
  }

  def convertArrayToInsertLine(
    firstLine: Array[String],
    delimiter: String): String = {
    val sb: StringBuilder = new StringBuilder()
    if (firstLine != null)
      for (str <- firstLine)
        sb.append(str.replace(" ", "")).append(delimiter)
    sb.substring(0, sb.length - 2)
  }
  def executeIf(context: Context, action: in.handyman.dsl.Action): Boolean = {
    val loadcsvAsIs = action.asInstanceOf[in.handyman.dsl.LoadCsv]
    val loadcsv: in.handyman.dsl.LoadCsv = CommandProxy.createProxy(loadcsvAsIs, classOf[in.handyman.dsl.LoadCsv], context)

    val expression = loadcsv.getCondition
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
  //target : String , source : String , auth : String , id :String, name: String, dropboxStmtfrom : Statement

}