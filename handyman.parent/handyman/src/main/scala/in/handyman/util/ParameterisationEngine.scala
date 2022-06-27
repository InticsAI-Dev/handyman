package in.handyman.util

import org.apache.commons.text.StrSubstitutor

import in.handyman.command.Context

object ParameterisationEngine {

  def resolve(inputString: String, context: Context): String = {
    val configMap = context.getMe
    val paramEngine = new StrSubstitutor(configMap)
    if (inputString != null) {
      val temp_output = paramEngine.replace(inputString)
      if (!temp_output.contains("_60_")){
        val output = temp_output.replace("0_", configMap.getOrDefault("process-id", "0_") + "_")
        output
      }else
        temp_output
    } else
      ""
  }
  
  def resolve_without_processidrep(inputString: String, context: Context): String = {
    val configMap = context.getMe
    val paramEngine = new StrSubstitutor(configMap)
    if (inputString != null) {
      val temp_output = paramEngine.replace(inputString)
      temp_output
    } else
      ""
  }

  def doYieldtoTrue(expression: in.handyman.dsl.Expression) = {
    expression match {
      case null => {
        true
      }
      case _ => {
        val lhs = expression.getLhs
        val rhs = expression.getRhs
        val oper = expression.getOperator
        oper match {
          case "<" => {
            val lhsInt = Integer.parseInt(lhs)
            val rhsInt = Integer.parseInt(rhs)

            lhsInt < rhsInt
          }
          case ">" => {
            val lhsInt = Integer.parseInt(lhs)
            val rhsInt = Integer.parseInt(rhs)

            lhsInt > rhsInt

          }
          case "==" => {
            lhs.equals(rhs)
          }
          case "contains" => {
            lhs.contains(rhs)
          }
        }
      }
    }

  }
}