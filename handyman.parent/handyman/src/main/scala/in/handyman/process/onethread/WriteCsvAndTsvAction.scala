package in.handyman.process.onethread

import com.typesafe.scalalogging.LazyLogging
import in.handyman.command.Action
import in.handyman.command.Context
import in.handyman.util.ResourceAccess
import in.handyman.DslStandaloneSetup
import in.handyman.server.PathResolver

import in.handyman.command.TryContext
import in.handyman.parser.antlr.DslParser
import java.io.FileReader
import in.handyman.server.ProcessExecutor
import in.handyman.command.CommandProxy
import in.handyman.server.ProcessAST
import in.handyman.util.ParameterisationEngine
import java.io.StringWriter
import java.io.PrintWriter
import in.handyman.util.ExceptionUtil
import java.sql.SQLException
import java.io.Writer
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.io.BufferedOutputStream
import java.io.File
import java.sql.ResultSet

class WriteCsvAndTsvAction extends in.handyman.command.Action with LazyLogging {
  val detailMap = new java.util.HashMap[String, String]
  def execute(context: in.handyman.command.Context, action: in.handyman.dsl.Action, actionId: Integer): in.handyman.command.Context = {

    val writecsvandtsvAsIs: in.handyman.dsl.WriteCsv = action.asInstanceOf[in.handyman.dsl.WriteCsv]
    val writecsvandtsv: in.handyman.dsl.WriteCsv = CommandProxy.createProxy(writecsvandtsvAsIs, classOf[in.handyman.dsl.WriteCsv], context)
    val from = writecsvandtsv.getSource
    val to: File = new File(writecsvandtsv.getTo)
    val name = writecsvandtsv.getName
    val id = context.getValue("process-id")
    val sqlList = writecsvandtsv.getValue.replaceAll("\"", "")
    val delim = writecsvandtsv.getDelim
    logger.info("WriteCsvAndTsv id#{}, name#{}, from#{}, sqlList#{}", id, name, from)

    val conn = ResourceAccess.rdbmsConn(from)
    conn.setAutoCommit(false)

    val stmt = conn.createStatement
    val ars: ResultSet = stmt.executeQuery(sqlList)
    val ncols: Int = ars.getMetaData.getColumnCount
    conn.commit

    val fop: FileOutputStream = new FileOutputStream(to)
    val out: Writer = new OutputStreamWriter(new BufferedOutputStream(fop))
    var column: String = ""
    if (!to.exists()) {
      to.createNewFile()
    }

    for (i <- 1 to ncols) {
      out.append(ars.getMetaData.getColumnName(i))
      if (i < ncols) out.append(delim) else out.append("\r\n")
    }
    while (ars.next()) {
      for (i <- 1 to ncols) {
        column = ars.getString(i)
        if (column != null)
          column = column.replaceAll("[^a-zA-Z0-9-:.]", " ")
        out.append("\"" + column + "\"")
        if (i < ncols) out.append(delim) else out.append("\r\n")
      }
    }

    out.close()
    logger.info("Completed WriteCsvAndTsv id#{}, name#{}, from#{}, sqlList#{}", id, name, from, sqlList)

    stmt.close
    conn.close
    context
  }

  def executeIf(context: in.handyman.command.Context, action: in.handyman.dsl.Action): Boolean = {
    val writecsvandtsvAsIs: in.handyman.dsl.WriteCsv = action.asInstanceOf[in.handyman.dsl.WriteCsv]
    val writecsvandtsv: in.handyman.dsl.WriteCsv = CommandProxy.createProxy(writecsvandtsvAsIs, classOf[in.handyman.dsl.WriteCsv], context)

    val expression = writecsvandtsv.getCondition
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