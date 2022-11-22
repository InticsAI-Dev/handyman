package in.handyman.process.onethread

import java.io.File
import java.sql.SQLException

import org.slf4j.MarkerFactory

import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.model.PutObjectRequest
import com.typesafe.scalalogging.LazyLogging

import in.handyman.command.CommandProxy
import in.handyman.command.Context
import in.handyman.util.ParameterisationEngine
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.auth.AWSStaticCredentialsProvider

class S3Action extends in.handyman.command.Action with LazyLogging {
  val detailMap = new java.util.HashMap[String, String]
  val auditMarker = "S3";
  val aMarker = MarkerFactory.getMarker(auditMarker);

  def execute(context: Context, action: in.handyman.dsl.Action, actionId: Integer): Context = {
    val s3AsIs: in.handyman.dsl.S3 = action.asInstanceOf[in.handyman.dsl.S3]
    val s3: in.handyman.dsl.S3 = CommandProxy.createProxy(s3AsIs, classOf[in.handyman.dsl.S3], context)

    val name = s3.getName
    val id = context.getValue("process-id")
    val operation = s3.getOperation
    val accesskey = s3.getAccesskey
    val secretkey = s3.getSecretkey
    val bucket = s3.getBucket
    val contentType = s3.getContentType
    val filePath = s3.getFilePath
    val keyName = s3.getKeyName
    val region = s3.getRegion
    var s3Client : AmazonS3 = null;
    try {
        val awsCreds : BasicAWSCredentials = new BasicAWSCredentials(accesskey, secretkey)
			  s3Client = AmazonS3ClientBuilder.standard().withRegion(region).withCredentials(new AWSStaticCredentialsProvider(awsCreds)).build()
        
        if(operation.equals("upload"))
            putObject(s3Client, bucket, keyName, filePath, contentType)
    } 
    catch {
      case ex: SQLException => {
        ex.printStackTrace()
        //throw ex
      }
    }
    finally 
    {
      detailMap.put("name", name)
      detailMap.put("operation", operation)
      detailMap.put("bucket", bucket)
      detailMap.put("contentType", contentType)
      detailMap.put("filePath",filePath)
      
      if(s3Client != null)
        s3Client.shutdown()
    }
    context

  }
  
  def putObject(s3Client: AmazonS3, bucket: String, keyName: String, filePath: String, contentType: String) = {
      val file : File = new File(filePath)
      val request : PutObjectRequest = new PutObjectRequest(bucket, keyName+file.getName, file)
      val metadata : ObjectMetadata = new ObjectMetadata()
      metadata.setContentType(contentType)
      request.setMetadata(metadata)
      s3Client.putObject(request)
  }

  def executeIf(context: Context, action: in.handyman.dsl.Action): Boolean = {
    val s3AsIs: in.handyman.dsl.S3 = action.asInstanceOf[in.handyman.dsl.S3]
    val s3: in.handyman.dsl.S3 = CommandProxy.createProxy(s3AsIs, classOf[in.handyman.dsl.S3], context)

    val expression = s3.getCondition
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