package org.trade.item

import org.trade.forum.Buyout
import org.trade.util.DynamicJson.DynJson

class Item(val id: Int, val threadId: Int, val buyout: Double, val json: DynJson) {
    def sanitizeName(name: String) = name.replace("<<set:MS>>", "").replace("<<set:M>>", "").replace("<<set:S>>", "")
    def fullName = (sanitizeName(json.name.asText) + " " + sanitizeName(json.typeLine.asText)).trim

    def nameContains(s: String) = json.name.contains(s)
    def typeContains(s: String) = json.typeLine.contains(s)
    def fullNameContains(s: String) = fullName.contains(s)

    def verified = json.verified.asBoolean

    def socketCount = if(!json.has("sockets")) 0 else json.sockets.size

    val urlTypeBegin = "2DItems"
    val urlTypeEnd = ".png?"
    // look in the item image URL for an indication of the item type. this will have to do for now
    def isType(itemType: String): Boolean = {
        val icon = json.icon.asText
        if(icon.startsWith("/")) return false
        val startIndex = icon.indexOf(urlTypeBegin)
        val endIndex = icon.indexOf(urlTypeEnd)
        if(startIndex < 0 || endIndex < 0) {
            println("couldnt parse url for type info: " + icon)
            return false
        }
        val typeString = icon.substring(startIndex + 7, endIndex)
        typeString.contains(itemType)
    }

    // properties are built in stats on the item, like quality, mana cost, crit chance, etc
    def getPropertyValue(prop: String): Double = {
        if(!json.has("properties")) return 0
        val quality = json.properties.find(_.name.asText == prop)
        if(quality.isEmpty) return 0
        val qString = quality.get.values.head.head
        Buyout.valueFormat.findFirstIn(qString).get.toDouble
    }
}
