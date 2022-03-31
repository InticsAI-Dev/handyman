package in.handyman.process.onethread

import java.io.IOException
import java.net.MalformedURLException

import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClients
import org.slf4j.MarkerFactory

import com.typesafe.scalalogging.LazyLogging

import in.handyman.audit.AuditService
import in.handyman.command.CommandProxy
import in.handyman.command.Context
import in.handyman.dsl.Action
import in.handyman.util.ExceptionUtil
import in.handyman.util.ParameterisationEngine

class RestApiAction extends in.handyman.command.Action with LazyLogging {
  val detailMap = new java.util.HashMap[String, String]
  val auditMarker = "RestApi";
  val aMarker = MarkerFactory.getMarker(auditMarker);

  def execute(context: Context, action: Action, actionId: Integer): Context = {
    val restApiAsIs = action.asInstanceOf[in.handyman.dsl.RestApi]
    val restApi: in.handyman.dsl.RestApi = CommandProxy.createProxy(restApiAsIs, classOf[in.handyman.dsl.RestApi], context)

    val url = restApi.getUrl
    val method = restApi.getMethod
    val body = restApi.getBody
    val name = restApi.getName
    val urlParam = restApi.getUrlParam
    val id = context.getValue("process-id")
    val responseLogTable = restApi.getResponseLogTable
    val responseLogDb = restApi.getResponseLogDb
    val sql = restApi.getValue.replaceAll("\"", "")
    logger.info(aMarker, "Rest API id#{}, name#{}, url#{}, sqlList#{}", id, name, url)
    detailMap.put("url", url)
    detailMap.put("method", method)
    detailMap.put("body", body)
    detailMap.put("urlParam.", urlParam)
    detailMap.put("responseLogTable", responseLogTable)
    detailMap.put("responseLogDb", responseLogDb)

    method match {
      case "GET" => {
        GetRequest(context, restApi)
      }
      case "POST" => {
        PostRequest(context, restApi)
      }
    }

    context
  }

  def GetRequest(context: Context, restApi: in.handyman.dsl.RestApi) {
    try {
      val client = HttpClients.createDefault()
      val urlParam = restApi.getUrlParam
      var url = restApi.getUrl
      
      if(!urlParam.isEmpty()){
        val urlParams = urlParam.split("=")
        url = url.replaceAll(urlParams(0), urlParams(1))    
      }
      
      val getRequest = new HttpGet(url)
      
      val headers = restApi.getHeaders
      if(!headers.isEmpty()){
        val headersArr = headers.split(";")
        headersArr.foreach(header => {
          val headerArr = header.split("=")
          getRequest.addHeader(headerArr(0), headerArr(1))
        })
      }
      
      val response = client.execute(getRequest)
      logger.info("Rest Api Response: " + response + " for URL: " + restApi.getUrl)

      val entity = response.getEntity()
      var content = ""
      if (entity != null) {
        val inputStream = entity.getContent()
        content = scala.io.Source.fromInputStream(inputStream).getLines.mkString
        inputStream.close
      }
      logger.info("Rest Api Response Content: " + content + " for URL: " + restApi.getUrl)
      AuditService.insertRestApiResponse(content, Integer.valueOf(context.getValue("process-id")), restApi)
      client.close()

    } catch {
      case ex: MalformedURLException => {
        logger.error(aMarker, "Stopping execution, {}", ex)
        detailMap.put("Exception", ExceptionUtil.completeStackTraceex(ex))
        throw ex
      }

      case ex: IOException => {
        logger.error(aMarker, "Stopping execution, {} ", ex)
        detailMap.put("Exception", ExceptionUtil.completeStackTraceex(ex))
        throw ex
      }

      case ex: Throwable => {
        logger.error(aMarker, "Stopping execution, {}", ex)
        detailMap.put("Exception", ExceptionUtil.completeStackTraceex(ex))
        throw ex
      }
    }
  }

  def PostRequest(context: Context, restApi: in.handyman.dsl.RestApi) {
    try {
      val client = HttpClients.createDefault()
      val postRequest = new HttpPost(restApi.getUrl)
      postRequest.setEntity(new StringEntity(restApi.getBody))

      val headers = restApi.getHeaders
      val headersArr = headers.split(";")
      headersArr.foreach(header => {
        val headerArr = header.split("=")
        postRequest.addHeader(headerArr(0), headerArr(1))
      })

      val response = client.execute(postRequest)
      logger.info("Rest Api Response: " + response + " for URL: " + restApi.getUrl)

      val entity = response.getEntity()
      var content = ""
      if (entity != null) {
        val inputStream = entity.getContent()
        content = scala.io.Source.fromInputStream(inputStream).getLines.mkString
        inputStream.close
      }
      logger.info("Rest Api Response Content: " + content + " for URL: " + restApi.getUrl)
      AuditService.insertRestApiResponse(content, Integer.valueOf(context.getValue("process-id")), restApi)
      client.close()

    } catch {
      case ex: MalformedURLException => {
        logger.error(aMarker, "Stopping execution, {}", ex)
        detailMap.put("Exception", ExceptionUtil.completeStackTraceex(ex))
        throw ex
      }

      case ex: IOException => {
        logger.error(aMarker, "Stopping execution, {} ", ex)
        detailMap.put("Exception", ExceptionUtil.completeStackTraceex(ex))
        throw ex
      }

      case ex: Throwable => {
        logger.error(aMarker, "Stopping execution, {}", ex)
        detailMap.put("Exception", ExceptionUtil.completeStackTraceex(ex))
        throw ex
      }
    }
  }
  
  def executeIf(context: Context, action: Action): Boolean = {
    val restApiAsIs = action.asInstanceOf[in.handyman.dsl.RestApi]
    val restApi: in.handyman.dsl.RestApi = CommandProxy.createProxy(restApiAsIs, classOf[in.handyman.dsl.RestApi], context)
    val name = restApi.getName
    val id = context.getValue("process-id")
    val expression = restApi.getCondition
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