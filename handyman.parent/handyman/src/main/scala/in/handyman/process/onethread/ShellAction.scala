package in.handyman.process.onethread



import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

import org.slf4j.MarkerFactory

import com.typesafe.scalalogging.LazyLogging

import in.handyman.command.CommandProxy
import in.handyman.command.Context
import in.handyman.dsl.Action
import in.handyman.util.ParameterisationEngine

class ShellAction extends in.handyman.command.Action with LazyLogging {
  val detailMap = new java.util.HashMap[String, String]
  val auditMarker = "Shell";
  val aMarker = MarkerFactory.getMarker(auditMarker);

  def execute(context: Context, action: Action, actionId: Integer): Context = {
    val shellAsIs = action.asInstanceOf[in.handyman.dsl.Shell]
    val shell: in.handyman.dsl.Shell = CommandProxy.createProxy(shellAsIs, classOf[in.handyman.dsl.Shell], context)

    var command : String = shell.getCommand
    val name = shell.getName
    val _timeout : Long = shell.getTimeout.toLong
    val id = context.getValue("process-id")
    val sql = shell.getValue.replaceAll("\"", "")
    logger.info(aMarker, "Shell id#{}, name#{}, url#{}, sqlList#{}", id, name, command)
    
    detailMap.put("command", command)
    
    command = command.replace("\\", "").replace("\"", "")
    logger.info("Command to execute: " + command);
    
    val cmds : Array[String] = command.split(" ")
		var commandArr : java.util.List[String] = new java.util.ArrayList[String]
		cmds.foreach(arg => {
		  commandArr.add(arg)
		})
    val process : ProcessBuilder = new ProcessBuilder(commandArr); 
    try
    {
        var p : Process = process.start();
        if (!p.waitFor(_timeout, TimeUnit.MILLISECONDS)) {
          p.destroy()
          p.waitFor(100, TimeUnit.MILLISECONDS);
          if (p.isAlive()) {
              p.destroyForcibly();
          }
          throw new InterruptedException("Process has been interrupted because of timeout (" + _timeout + "ms). ");
        }
        
        val reader : BufferedReader=  new BufferedReader(new InputStreamReader(p.getInputStream()));
        val builder : StringBuilder = new StringBuilder();
        var line : String = null;
        var linesCount : Int = 0;

        while ({line = reader.readLine; line != null}) {
          linesCount = linesCount + 1;
          builder.append(line);
          builder.append(System.getProperty("line.separator"));
        }
        
        val stderr : BufferedReader = new BufferedReader(new InputStreamReader(p.getErrorStream()))
        if (linesCount == 0) {
          builder.append("ERROR::: ")
  
          while ({line = stderr.readLine; line != null}) {
              builder.append(line).append(System.getProperty("line.separator"))
          }
        }
        
        var output : String = builder.toString();
        logger.info("Shell Action Output : " + output);
    }
    catch {
      case ex: Exception => {
        ex.printStackTrace()
        //throw ex
      }
    }

    context
  }

  def executeIf(context: Context, action: Action): Boolean = {
    val shellAsIs = action.asInstanceOf[in.handyman.dsl.Shell]
    val shell: in.handyman.dsl.Shell = CommandProxy.createProxy(shellAsIs, classOf[in.handyman.dsl.Shell], context)
    val name = shell.getName
    val id = context.getValue("process-id")
    val expression = shell.getCondition
    try {
      val output = ParameterisationEngine.doYieldtoTrue(expression)
      logger.info(aMarker, "Completed evaluation to execute id#{}, name#{}, dbSrc#{}, expression#{}", id, name, expression)
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