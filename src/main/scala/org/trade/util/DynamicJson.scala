package org.trade.util

import com.fasterxml.jackson.databind.JsonNode
import play.libs.Json

import scala.language.{dynamics, implicitConversions}

/*
    This class is a wrapper around the play-json library using scala's Dynamic feature to
    make the json code more readable. Instead of needing to write 'json.get("thing")' to
    get the field called "thing", we can just write 'json.thing'.
 */
object DynamicJson {
    /*
     Some implicit methods to allow DynJson objects to be implicitly converted to
     its wrapped type (play.libs.Json) and then to String/Int for convenience
     */
    implicit def getJsonNode(d:DynJson):JsonNode = d.json
    implicit def jsonToString(json:DynJson):String = json.asText
    implicit def jsonToInt(json:DynJson):Int = json.asInt

    def parse(s:String) = DynJson(Json.parse(s))

    case class DynJson(json:JsonNode) extends Dynamic with Traversable[DynJson] {
        /*
          The selectDynamic method is part of the Dynamics feature, and will turn a call
          to a nonexistant method such as json.field into a call to json.selectDynamic("field")
         */
        def selectDynamic(s:String) = if(json.get(s) != null) DynJson(json.get(s)) else DynJson(Json.parse("{}"))
        def foreach[U](f:DynJson => U) {iterator foreach f}
        def iterator() = new Iterator[DynJson] {
            val iter = json.iterator()
            def next() = DynJson(iter.next())
            def hasNext = iter.hasNext
        }
    }
}
