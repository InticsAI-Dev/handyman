

package in.handyman.command

import in.handyman.util.ParameterisationEngine
import com.fasterxml.jackson.databind.ObjectMapper

class TryContext(var config: Map[String, String], val instanceName: String) extends Context {

  def getValue(variable: String): String = {
      val output = if(config.contains(variable)) {
        val value = config.get(variable).get
        println("varaibel:::" + variable);
        println("value:::" + value);
        //if(!variable.equals("search_export_id"))
        val valoutp = ParameterisationEngine.resolve(value, this)
        println("sasasa:::"+valoutp)
        valoutp
        //else
          //value
      }
      else
      {
        ""
      }      
      output
  }

  def getMe(): java.util.Map[String, String] = {
    val configMap: java.util.Map[String, String] = new java.util.HashMap[String, String]
    config.foreach(t => {
      configMap.put(t._1, t._2)
    })
    configMap
  }

  def getMeAsIs(): Map[String, String] = {
    config
  }

  def addValue(key: String, value: String): Unit = {
    config += ((key, value))
  }
  
  def getJson(): String = {
    val jsonSerializer = new ObjectMapper
    jsonSerializer.writeValueAsString(getMe())
  }
}