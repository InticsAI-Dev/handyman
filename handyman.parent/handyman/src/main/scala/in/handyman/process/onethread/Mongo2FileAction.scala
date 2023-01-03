package in.handyman.process.onethread

import java.io.BufferedWriter
import java.io.FileWriter
import java.sql.SQLException
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Date

import org.bson.Document
import org.bson.json.JsonWriterSettings
import org.json.JSONArray
import org.json.JSONObject

import com.mongodb.BasicDBObject
import com.mongodb.client.FindIterable
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoCursor
import com.mongodb.client.MongoDatabase
import com.typesafe.scalalogging.LazyLogging

import in.handyman.command.CommandProxy
import in.handyman.config.ConfigurationService
import in.handyman.config.Resource
import in.handyman.util.ExceptionUtil
import in.handyman.util.ParameterisationEngine


class Mongo2FileAction extends in.handyman.command.Action with LazyLogging {
  val detailMap = new java.util.HashMap[String, String]
  var fetchSize: String = ""
  def execute(context: in.handyman.command.Context, action: in.handyman.dsl.Action, actionId: Integer): in.handyman.command.Context = {

    val mongo2FileAsIs: in.handyman.dsl.Mongo2File = action.asInstanceOf[in.handyman.dsl.Mongo2File]
    val mongo2File: in.handyman.dsl.Mongo2File = CommandProxy.createProxy(mongo2FileAsIs, classOf[in.handyman.dsl.Mongo2File], context)

    val configMap = ConfigurationService.getGlobalconfig()
    var rowsProcessed: Int = 1

    val source = mongo2File.getSourceConnStr
    val targetFile = mongo2File.getTo
    val sourceDb = mongo2File.getSourceDb
    val sourceTable = mongo2File.getSourceTable
    val name = mongo2File.getName
    val id = context.getValue("process-id")
    val slimit: String = mongo2File.getLimit
    val limit: Int = slimit.toInt
    val filter: String = mongo2File.getFilter
    fetchSize = mongo2File.getFetchBatchSize

    val connResource: Resource = ConfigurationService.getResourceConfig(source)
    val srcConnStr = connResource.url

    val mongoClient: MongoClient = MongoClients.create(srcConnStr)
    val mongoDatabase: MongoDatabase = mongoClient.getDatabase(sourceDb)
    val coll: MongoCollection[Document] = mongoDatabase.getCollection(sourceTable)
    var mongoCursor: MongoCursor[Document] = findAndFetch(filter, coll)
    
    val fr : FileWriter = new FileWriter(targetFile);
		val br : BufferedWriter = new BufferedWriter(fr);

    try {
      if (mongoCursor != null) {
        var query: String = ""
        while (mongoCursor.hasNext()) {
          var doc: Document = mongoCursor.next();
          
          val settings : JsonWriterSettings = JsonWriterSettings.builder()
                              	    				//.int64Converter((value, writer) => writer.writeNumber(value.toString()))
                              	    				.doubleConverter((value, writer) => writer.writeNumber(java.lang.Long.toString(Math.round(value))))
                              	    				//.objectIdConverter((value, writer) => writer.writeString(value.toHexString()))
                              	    				.build();
          var json : String = doc.toJson(settings);
	    		var prefix : String = "{\"object_id\":\"" + doc.get("_id") + "\", \"document\":";
	    		var suffix : String = "}";
	    		
	    		br.write(prefix+json+suffix);
	    		
	    		if(mongoCursor.hasNext())
	    		  br.newLine();

          rowsProcessed = rowsProcessed + 1
        }
     }
    } catch {
      case ex: SQLException => {
        ex.printStackTrace()
        //throw ex
      }
    } finally {

      detailMap.put("name", name)
      detailMap.put("source", source)
      detailMap.put("targetFile", targetFile)
      detailMap.put("srcDb", sourceDb)
      detailMap.put("srcTable", sourceTable)
      detailMap.put("srcConnStr", srcConnStr)

      if (rowsProcessed > 0) {
        rowsProcessed = rowsProcessed - 1
      }
      detailMap.put("rows-processed", String.valueOf(rowsProcessed))

      context.addValue("rows-processed", String.valueOf(rowsProcessed))

      try {
        if (mongoCursor != null)
          mongoCursor.close

        if (mongoClient != null)
          mongoClient.close

      	if(br != null)
      		br.close();
        
      	if(fr != null)
      		fr.close();
      } catch {
        case ex: Throwable => {
          ex.printStackTrace
          detailMap.put("exception", ExceptionUtil.completeStackTraceex(ex))
        }
      }
    }

    context
  }

  def findAndFetch(filter: String, coll: com.mongodb.client.MongoCollection[org.bson.Document]): com.mongodb.client.MongoCursor[org.bson.Document] = {
    
    val jsonArr: JSONArray = new JSONArray(filter);
    var col: String = "";
    var andobj: ArrayList[BasicDBObject] = new ArrayList[BasicDBObject]();
    var orobj: ArrayList[BasicDBObject] = new ArrayList[BasicDBObject]();
    for (i <- 0 to jsonArr.length() - 1) {
      var filObj: BasicDBObject = null;
      val jsonObj: JSONObject = jsonArr.get(i).asInstanceOf[JSONObject];

      col = String.valueOf(jsonObj.get("column"))
      val colType: String = String.valueOf(jsonObj.get("type"))
      val colFormat: String = String.valueOf(jsonObj.get("format"))
      val logicalOperator: String = String.valueOf(jsonObj.get("logical-operator"))

      val condArr: JSONArray = new JSONArray(String.valueOf(jsonObj.get("condition")));
      for (j <- 0 to condArr.length() - 1) {
        val condObj: JSONObject = condArr.get(j).asInstanceOf[JSONObject];
        val operator: String = String.valueOf(condObj.get("operator"))
        var colVal: Object = condObj.get("value")
        
        var colFormatted: Date = null;
        if (colType.equals("date") && !colFormat.isEmpty() && colVal != null) {
          colFormatted = new SimpleDateFormat(colFormat).parse(String.valueOf(colVal));

          if (filObj == null) {
            filObj = new BasicDBObject(operator, colFormatted);
          } else {
            filObj = filObj.append(operator, colFormatted);
          }
        } else {
          if(colVal.equals(null)){
            if (filObj == null) {
              filObj = new BasicDBObject(operator, null);
            } else {
              filObj = filObj.append(operator, null);
            }
          }else{
            if (filObj == null) {
              filObj = new BasicDBObject(operator, String.valueOf(colVal));
            } else {
              filObj = filObj.append(operator, String.valueOf(colVal));
            }
        }
        }
      }
      if(logicalOperator.equalsIgnoreCase("and"))
        andobj.add(new BasicDBObject(col, filObj));
      else if(logicalOperator.equalsIgnoreCase("or"))
        orobj.add(new BasicDBObject(col, filObj));
    }

    if (!andobj.isEmpty()) {
      var andQuery: BasicDBObject = new BasicDBObject();
      andQuery.put("$and", andobj);
      
      if (!orobj.isEmpty()) {
        andobj.add(new BasicDBObject("$or", orobj))
      }

      val findIterate: FindIterable[Document] = coll.find(andQuery).batchSize(fetchSize.toInt)

      return findIterate.iterator()
    } else {
      return coll.find().batchSize(fetchSize.toInt).iterator()
    }
  }
  
  def executeIf(context: in.handyman.command.Context, action: in.handyman.dsl.Action): Boolean = {
    val mongo2FileAsIs: in.handyman.dsl.Mongo2File = action.asInstanceOf[in.handyman.dsl.Mongo2File]
    val mongo2File: in.handyman.dsl.Mongo2File = CommandProxy.createProxy(mongo2FileAsIs, classOf[in.handyman.dsl.Mongo2File], context)

    val expression = mongo2File.getCondition
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
