package in.handyman.process.onethread

import java.lang.reflect.Method
import java.sql.SQLException
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Date
import java.util.HashMap
import java.util.HashSet

import org.bson.Document
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
import in.handyman.util.ResourceAccess


class Mongo2DbAction extends in.handyman.command.Action with LazyLogging {
  val detailMap = new java.util.HashMap[String, String]
  var fetchSize: String = ""
  def execute(context: in.handyman.command.Context, action: in.handyman.dsl.Action, actionId: Integer): in.handyman.command.Context = {

    val mongo2DbAsIs: in.handyman.dsl.Mongo2Db = action.asInstanceOf[in.handyman.dsl.Mongo2Db]
    val mongo2Db: in.handyman.dsl.Mongo2Db = CommandProxy.createProxy(mongo2DbAsIs, classOf[in.handyman.dsl.Mongo2Db], context)

    val configMap = ConfigurationService.getGlobalconfig()
    var rowsProcessed: Int = 1

    val source = mongo2Db.getSourceConnStr
    val destination = mongo2Db.getTo
    val sourceDb = mongo2Db.getSourceDb
    val targetDb = mongo2Db.getTargetDb
    val name = mongo2Db.getName
    val ddlSql: String = mongo2Db.getValue.replaceAll("\"", "")
    val id = context.getValue("process-id")
    val table = mongo2Db.getTargetTable
    val slimit: String = mongo2Db.getLimit
    val limit: Int = slimit.toInt
    val filter: String = mongo2Db.getFilter
    val findAttr: String = mongo2Db.getFindAttr
    val manipFunc: String = mongo2Db.getApplyManipulation
    val onUpdateKey: String = mongo2Db.getOnUpdateKey
    fetchSize = mongo2Db.getFetchBatchSize
    val writeSize: String = mongo2Db.getWriteBatchSize

    val connResource: Resource = ConfigurationService.getResourceConfig(source)
    val srcConnStr = connResource.url

    var sqlSelect: String = ddlSql.substring(ddlSql.toUpperCase().indexOf("SELECT") + 7, ddlSql.toUpperCase().indexOf("FROM"))
    var sqlFrom: String = ddlSql.substring(ddlSql.toUpperCase().indexOf("FROM") + 5, ddlSql.length())

    var (insFieldsMap, insAndSelFieldsMap, projectFields) = getInsertAndSelectFields(sqlSelect)
    var (updateStr, insertStr, inFieldArr, insertFields) = prepareInsertUpdateFields(insFieldsMap, insAndSelFieldsMap)

    var onUpdateStr: String = ""
    if (onUpdateKey != null && !onUpdateKey.isEmpty()) {
      onUpdateStr = onUpdateStr + "target." + onUpdateKey + " = source." + onUpdateKey
    }

    val mongoClient: MongoClient = MongoClients.create(srcConnStr)
    val mongoDatabase: MongoDatabase = mongoClient.getDatabase(sourceDb)
    val coll: MongoCollection[Document] = mongoDatabase.getCollection(sqlFrom)
    var mongoCursor: MongoCursor[Document] = findAndFetch(filter, coll, projectFields)

    var mongo2DbDbConnto : java.sql.Connection = null;
    var mongo2DbStmtto : java.sql.Statement = null;
    try {
      if (mongoCursor != null) {
        mongo2DbDbConnto = ResourceAccess.rdbmsConn(destination)
        mongo2DbStmtto = mongo2DbDbConnto.createStatement
        mongo2DbDbConnto.setAutoCommit(false)
    
        val methodMap: HashMap[String, Method] = new HashMap[String, Method]()
        var javaBso: Any = null;
        if (manipFunc != null) {
          val clazz = this.getClass.getClassLoader.loadClass(manipFunc)
          val decMethods: Array[Method] = clazz.getDeclaredMethods()
          for (method <- decMethods) {
            methodMap.put(method.getName, method)
          }
    
          javaBso = clazz.newInstance()
        }

        var query: String = ""
        while (mongoCursor.hasNext()) {
          var doc: Document = mongoCursor.next();

          query = query + "("
          for (col <- inFieldArr) {
            val colStr: String = col.toString().trim()
            var dbCol = insAndSelFieldsMap.get(colStr);

            var colVal: Object = getColumn(colStr, dbCol, doc, javaBso, methodMap)

            query = appendColumn(colVal, query)
          }

          query = query.substring(0, query.length() - 1) + "),"

          if (rowsProcessed % writeSize.toInt == 0) {

            query = query.replace("\"null\"", "null")
            logger.info("Mongodbsql (Nqne) id#{}, name#{}, from#{}, rows#{}", id, name, source, rowsProcessed.toString)

            var insert: String = ""
            if (onUpdateKey != null && !onUpdateKey.isEmpty()) {
              insert = "MERGE INTO " + targetDb + ".dbo." + table + " AS target USING (VALUES " + query.substring(0, query.length() - 1) +
                ") AS source (" + insertFields + ") ON (" + onUpdateStr + ") WHEN MATCHED THEN UPDATE SET " +
                updateStr + " WHEN NOT MATCHED THEN INSERT (" + insertFields + ") VALUES (" + insertStr + ");";
            } else {
              insert = "insert into " + table + " (" + insertFields + ")" + " values " + query.substring(0, query.length() - 1) + ";"
            }
            logger.info("Mongodbsql (Nqne) id#{}, name#{}, from#{}, rows#{}", id, name, source, rowsProcessed.toString)

            //System.out.println(insert)
            mongo2DbStmtto.execute(insert)

            mongo2DbDbConnto.commit()
            logger.info("Mongodbsql Commited (Nqne) id#{}, name#{}, from#{}, rows#{}", id, name, source, rowsProcessed.toString)
            insert = ""
            query = ""
          }
          rowsProcessed = rowsProcessed + 1

        }

        if (!query.isEmpty()) {
          query = query.replace("\"null\"", "null")
          var insert: String = ""
          if (onUpdateKey != null && !onUpdateKey.isEmpty()) {
            insert = "MERGE INTO " + targetDb + ".dbo." + table + " AS target USING (VALUES " + query.substring(0, query.length() - 1) +
              ") AS source (" + insertFields + ") ON (" + onUpdateStr + ") WHEN MATCHED THEN UPDATE SET " +
              updateStr + " WHEN NOT MATCHED THEN INSERT (" + insertFields + ") VALUES (" + insertStr + ");";
          } else {
            insert = "insert into " + table + " (" + insertFields + ")" + " values " + query.substring(0, query.length() - 1) + ";"
          }

          mongo2DbStmtto.execute(insert)
          mongo2DbDbConnto.commit()
          logger.info("Mongodbsql Commited (Qne)  id#{}, name#{}, from#{}, rows#{}", id, name, source, rowsProcessed.toString)
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
      detailMap.put("destination", destination)
      detailMap.put("ddlSql", ddlSql)
      detailMap.put("filter", filter)
      detailMap.put("srcDb", sourceDb)
      detailMap.put("srcConnStr", srcConnStr)
      detailMap.put("targetTable", table)

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

        if (mongo2DbStmtto != null && !mongo2DbStmtto.isClosed())
          mongo2DbStmtto.close
        
        if (mongo2DbDbConnto != null && !mongo2DbDbConnto.isClosed())
          mongo2DbDbConnto.close
      } catch {
        case ex: Throwable => {
          ex.printStackTrace
          detailMap.put("exception", ExceptionUtil.completeStackTraceex(ex))
        }
      }
    }

    context
  }

  def getInsertAndSelectFields(sqlSelect: String) = {
    val projectFields: BasicDBObject = new BasicDBObject()
    val selectFields: Array[String] = sqlSelect.split(",");
    val insFieldsMap: HashSet[String] = new HashSet[String]
    val insAndSelFieldsMap: HashMap[String, String] = new HashMap[String, String]
    for (col <- selectFields) {
      val colStr: String = col.trim().toString()
      var aliasCol: String = null;
      var dbCol: String = null;
      if (colStr.contains(" as ")) {
        val colWithAlias: Array[String] = colStr.split(" as ")
        dbCol = colWithAlias(0).trim()
        aliasCol = colWithAlias(1).trim()
      } else {
        dbCol = colStr
        aliasCol = colStr
      }

      projectFields.append(dbCol, 1)
      insFieldsMap.add(aliasCol.trim());
      insAndSelFieldsMap.put(aliasCol.trim(), dbCol.trim())
    }

    (insFieldsMap, insAndSelFieldsMap, projectFields)
  }

  def prepareInsertUpdateFields(insFieldsMap: java.util.HashSet[String], insAndSelFieldsMap: java.util.HashMap[String, String]) = {
    val inFieldArr = insFieldsMap.toArray()
    var updateStr: String = "";
    var insertStr: String = "";
    var insertFields: String = "";
    for (inField <- inFieldArr) {
      val dbCol = insAndSelFieldsMap.get(inField)

      if (dbCol.contains("[")) {
        insertFields = insertFields + inField + ","
        updateStr = updateStr + "target." + inField + " = source." + inField + ","
        insertStr = insertStr + "source." + inField + ","

        val colStr: Array[String] = dbCol.substring(dbCol.indexOf("[") + 1, dbCol.indexOf("]")).split(":")
        var colss: String = ""
        for (colS <- colStr) {
          colss = colS
          if (colS.contains(" alias ")) {
            val colWithAlias: Array[String] = colS.split(" alias ")
            colss = colWithAlias(1).trim()
          }
          insertFields = insertFields + colss + ","
          updateStr = updateStr + "target." + colss + " = source." + colss + ","
          insertStr = insertStr + "source." + colss + ","
        }
      } else {
        insertFields = insertFields + inField + ","

        if (!inField.equals("application_id")) {
          updateStr = updateStr + "target." + inField + " = source." + inField + ","
        }
        insertStr = insertStr + "source." + inField + ","
      }
    }

    insertFields = insertFields.substring(0, insertFields.length() - 1)
    updateStr = updateStr.substring(0, updateStr.length() - 1)
    insertStr = insertStr.substring(0, insertStr.length() - 1)

    (updateStr, insertStr, inFieldArr, insertFields)
  }

  def appendColumn(colVal: Object, query: String): String = {
    var queryAppend = query

    if (colVal != null) {
      var colValStr = colVal.toString()

      if ((colVal.isInstanceOf[Integer] || colVal.isInstanceOf[Double])) {
        if (!colValStr.equals(""))
          queryAppend = queryAppend + colValStr + ","
        else
          queryAppend = queryAppend + null + ","
      } else if ((colVal.isInstanceOf[java.util.ArrayList[_]])) {
        val colValObj = colVal.asInstanceOf[java.util.ArrayList[_]]
        if (colValObj != null && !colValObj.isEmpty()) {
          /*val gson : Gson = new GsonBuilder().create();
          val myCustomArray : JsonArray = gson.toJsonTree(colValObj).getAsJsonArray();
          System.out.println("array element4....."+ myCustomArray.toString());
          System.out.println("array element5....."+ gson.toJson(colValObj));
          System.out.println("array element6....."+ gson.toJson(colValObj, new TypeToken[ArrayList[_]]() {}.getType()))*/

          //val jsonArray : JSONArray = new JSONArray(colValObj);
         
          /*logger.info("array element1....."+jsonArray.toString());
          System.out.println("array element2....."+(new JSONArray(colValObj.toArray())).toString());
          System.out.println("array element3....."+(new JSONArray(colValObj.toArray())).toString());*/
          /*colValStr = ""
          colValObj.forEach(colValO => colValStr = colValStr + colValO.toString() + "|")
          queryAppend = queryAppend + "\'" + colValStr.substring(0, colValStr.length()-1) + "\'" + ","*/
          
          val jsonArr : ArrayList[String]  = new java.util.ArrayList[String];
	    		val obj1 : ArrayList[_] = colValObj.asInstanceOf[ArrayList[_]];
	    		val iter : java.util.Iterator[_] = obj1.iterator();
	    		while(iter.hasNext()) {
	    			val dc = iter.next();
	    			if(dc != null){
  	    			if(dc.isInstanceOf[Document])
  	    			  jsonArr.add(dc.asInstanceOf[Document].toJson());
  	    			else 
  	    			  jsonArr.add(dc.toString());	    			  
	    			}
	    		}
		    		
          queryAppend = queryAppend + "\'" + jsonArr.toString() + "\'" + ","
        } else {
          queryAppend = queryAppend + null + ","
        }
      } else if (colVal.isInstanceOf[Date]) {
        colValStr = formatDate(colVal);
        if (colValStr != null && !colValStr.isEmpty()) {
          queryAppend = queryAppend + colValStr + ","
        } else {
          queryAppend = queryAppend + "''" + ","
        }
      } else {
        if (colValStr.contains("'"))
          colValStr = colValStr.replaceAll("'", "''")

        queryAppend = queryAppend + "\'" + colValStr + "\'" + ","
      }
    } else
      queryAppend = queryAppend + null + ","

    return queryAppend
  }

  def formatDate(date: Object): String = {
    val df: DateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS ZZZ");
    if (date != null) {
      val dfStr: String = df.format(date);
      var str: String = "CAST('" + dfStr.substring(0, dfStr.length() - 2) + ":" + dfStr.substring(dfStr.length() - 2, dfStr.length()) + "' AS DateTime2)"

      return str
    }

    return null;
  }

  def getColNestedDocIfExists(docObj: Document, dbCol: String): Object = {
    if (docObj.isInstanceOf[Document] && !dbCol.isEmpty() && dbCol.contains(".")) {
      val dbColForm = dbCol.substring(0, dbCol.indexOf("."))
      val dbColFunc = dbCol.substring(dbCol.indexOf(".") + 1, dbCol.length())
      val obje: Object = docObj.get(dbColForm)
      if (obje.isInstanceOf[java.util.ArrayList[Document]]) {
        val obje1 = obje.asInstanceOf[java.util.ArrayList[Document]]
        if(obje1!=null && !obje1.isEmpty()){
          /*val gson : Gson = new Gson();
          return gson.toJson(obje1);*/
          
          val docc : Document = obje1.get(0);
          
          println("dbColFunc....." + dbColFunc + ".....dbcol array size..... " + (dbColFunc.split(".")).length)
          return obje1.get(0);
          if(dbColFunc.split(".").length > 1){
            getColNestedDocIfExists(docc, dbColFunc)
          }else{
            return obje1.get(0);
          }
        }
        else
          return null;
      } else if (obje.isInstanceOf[java.util.ArrayList[_]]) {
        return obje
      } else {
        val doc1: Document = docObj.get(dbColForm, classOf[Document])

        getColNestedDocIfExists(doc1, dbColFunc)
      }
    } else
      return docObj
  }

  def getColumn(colStr: String, dbCol: String, doc: org.bson.Document, javaBso: Any,
    methodMap: HashMap[String, Method]): Object = {
    var manipFunc: Method = methodMap.get(colStr);
    if (manipFunc != null) { // apply manipulation
      val retObj: Object = manipFunc.invoke(javaBso, doc, dbCol, colStr)
      if (retObj != null) {
        //logger.info("Column:" + aliasCol + ", Before Manipulation:" + colVal + ", After Manipulation:" + retObj.toString())
        return retObj
      }
    } else {
      val docNew = getColNestedDocIfExists(doc, dbCol)
      if (docNew!=null && docNew.isInstanceOf[Document]) {
        val docNew1 = docNew.asInstanceOf[Document]
        if (docNew1 != null) {
          if (dbCol.contains(".")) {
            val colValObj = docNew1.get(dbCol.substring(dbCol.lastIndexOf(".") + 1, dbCol.length()));
            if (colValObj != null)
              return colValObj
          } else {
            val obj: Object = docNew1.get(dbCol);
            if (obj != null)
              return obj
          }
        }
      } else {
        return docNew;
      }
    }

    return null
  }

  // filter format {'column':'updatedat', 'type':'date', 'format':'yyyy-MM-dd HH:mm:ss.SSS ZZZ', 'operator':'$gt', 'value':'2020-06-25 09:28:04.041 UTC'}
  def findAndFetch(filter: String, coll: com.mongodb.client.MongoCollection[org.bson.Document],
    projectFields: BasicDBObject): com.mongodb.client.MongoCursor[org.bson.Document] = {
    
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

      val findIterate: FindIterable[Document] = coll.find(andQuery).batchSize(fetchSize.toInt).projection(projectFields)

      return findIterate.iterator()
    } else {
      return coll.find().batchSize(fetchSize.toInt).projection(projectFields).iterator()
    }
  }

  // find attribute format {'column':'updatedat', 'type':'date', 'format':'yyyy-MM-dd HH:mm:ss.SSS ZZZ', 'function':'max', 'context_column':'max_updatedat'}
  def findAttribute(findAttr: String, context: in.handyman.command.Context, iterCol: String, iterColVal: String) = {
    val obj: JSONObject = new JSONObject(findAttr);
    val col: String = String.valueOf(obj.get("column"))
    val function: String = String.valueOf(obj.get("function"))
    val colType: String = String.valueOf(obj.get("type"))
    val colFormat: String = String.valueOf(obj.get("format"))
    val contextCol: String = String.valueOf(obj.get("context_column"))

    if (col.equalsIgnoreCase(iterCol)) {
      function match {
        case "max" => max(context, contextCol, colType, colFormat, iterColVal)
      }
    }
  }

  def max(context: in.handyman.command.Context, contextCol: String, colType: String, colFormat: String, iterColVal: String) = {
    val contextColVal = context.getValue(contextCol);

    if (contextColVal != null && !contextColVal.isEmpty()) {
      var contextColFormatted: Date = null;
      var iterColValFormatted: Date = null;
      if (colType.equals("date") && !colFormat.isEmpty()) {
        contextColFormatted = new SimpleDateFormat(colFormat).parse(contextColVal);
        iterColValFormatted = new SimpleDateFormat(colFormat).parse(iterColVal);

        if (iterColValFormatted.after(contextColFormatted))
          context.addValue(contextCol, iterColVal);
      }
    } else {
      context.addValue(contextCol, iterColVal);
    }
  }

  def executeIf(context: in.handyman.command.Context, action: in.handyman.dsl.Action): Boolean = {
    val mongo2DbAsIs: in.handyman.dsl.Mongo2Db = action.asInstanceOf[in.handyman.dsl.Mongo2Db]
    val mongo2Db: in.handyman.dsl.Mongo2Db = CommandProxy.createProxy(mongo2DbAsIs, classOf[in.handyman.dsl.Mongo2Db], context)

    val expression = mongo2Db.getCondition
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
