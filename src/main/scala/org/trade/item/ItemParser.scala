package org.trade.item

import org.trade.util.Currency
import org.trade.util.DynamicJson._

import scala.collection.mutable.ArrayBuffer
import scala.io.Source

case class ItemSearch(description: String, rules: List[Item => Boolean]) {
    def itemQualifies(item: Item): Boolean = {
        for(r <- rules) if(!r(item)) return false
        true
    }
}

class ItemParser {
    val json = Source.fromFile("searches.json").mkString
    val searchList = parse(json).searches

    val itemSearches = getSearchRules(searchList).filter(_.rules.nonEmpty)

    val valuePattern = """\d+(\.\d*)?""".r

    /*
     * Produce a list of predicates for checking whether an item meets the given search criteria
     * The resulting list will be run in order for every item checked, so the rules added first should be the
     * ones most likely to disqualify an item with the least amount of work done, for efficiency
     */
    def getSearchRules(searches: DynJson) = for(s <- searches) yield {
        val rules = ArrayBuffer[Item => Boolean]()

        // basic name rule
        if(s.has("name")) rules += { item => item.fullNameContains(s.name) }

        // buyout rule
        if(s.has("buyout_currency")) {
            val buyout = Currency.ratios(s.buyout_currency) * s.buyout_max.asDouble
            rules += { item => item.buyout <= buyout }
        }

        if(s.has("mods")) rules ++= s.mods.map(getModRules)

        if(s.has("sockets_min")) {
            val minSockets = s.sockets_min.asInt
            rules += { item => item.socketCount >= minSockets }
        }

        if(s.has("sockets_max")) {
            val maxSockets = s.sockets_max.asInt
            rules += { item => item.socketCount <= maxSockets }
        }

        if(s.has("type")) {
            val itemType = s.get("type").asText
            rules += { item => item.isType(itemType) }
        }

        if(s.has("q_min")) {
            val quality = s.q_min.asInt
            rules += { item => item.getPropertyValue("Quality") >= quality }
        }

        if(s.has("q_max")) {
            val quality = s.q_max.asInt
            rules += { item => item.getPropertyValue("Quality") <= quality }
        }

        val description = if(s.has("description")) s.description.asText else s.name.asText
        ItemSearch(description, rules.toList)
    }

    def scanItem(i: Item) {
        for(s <- itemSearches; if s.itemQualifies(i)) {
            // found an item that meets a set of search criteria
            println("item found - " + s.description + " - " + i.fullName + " - buyout " + i.buyout + ", "
                + "https://www.pathofexile.com/forum/view-thread/" + i.threadId)
        }
    }

    def getModRules(modJson: DynJson): Item => Boolean = {

        def modRule(item: Item): Boolean = {
            // check to see if we have any mods, and if the mod is even on the item
            if(!item.json.has("explicitMods")) return false
            val mods = item.json.explicitMods.map(_.asText)
            val modOpt = mods.find(valuePattern.replaceAllIn(_, "#").contains(modJson.mods))
            if(modOpt.isEmpty) return false

            val modMax = if(modJson.modmax.asText != "") Some(modJson.modmax.asDouble) else None
            val modMin = if(modJson.modmin.asText != "") Some(modJson.modmin.asDouble) else None
            // if we have no max/min requirement, then all we needed was to have the mod
            if(modMax.isEmpty && modMin.isEmpty) return true

            val mod = modOpt.get
            val modValue = valuePattern.findFirstIn(mod).get.toDouble
            if(modMax.isDefined && modValue > modMax.get) return false
            if(modMin.isDefined && modValue < modMin.get) return false

            true
        }

        modRule
    }
}
