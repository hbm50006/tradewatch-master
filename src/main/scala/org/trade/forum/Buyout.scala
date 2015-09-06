package org.trade.forum

import org.trade.util.Currency

object Buyout {
    val buyoutFormat = """b/o.*(\d+\.?\d*).*\w+""".r
    val priceFormat = """(\d+\.?\d*).*\w+""".r
    val valueFormat = """\d+\.?\d*""".r
    val currencyTypeFormat = """([a-zA-Z]+)""".r

    val currencyAliases = Map(
        "chrom" -> "chromatic",
        "alt" -> "alteration",
        "jew" -> "jewellers",
        "jewel" -> "jewellers",
        "alch" -> "alchemy",
        "alchy" -> "alchemy",
        "fuse" -> "fusing",
        "fus" -> "fusing",
        "c" -> "chaos",
        "choas" -> "chaos",
        "exalt" -> "exalted",
        "exa" -> "exalted",
        "ex" -> "exalted"
    )

    // given a buyout string, return chaos equivalent value
    def parse(text: String): Option[Double] = {
        buyoutFormat findFirstIn text.toLowerCase match {
            case Some(str) =>
                val price = priceFormat.findFirstIn(str).get
                val buyoutValue = valueFormat.findFirstIn(price).get
                val currencyType = currencyTypeFormat.findFirstIn(price).get
                //println(text, str, price, buyoutValue, currencyType)
                val ratio = getRatio(currencyType)
                if (ratio == 0.0) {
                    println("couldnt parse buyout text: ", text)
                    return None
                }

                val chaosEquivValue = buyoutValue.toDouble * ratio
                //println(chaosEquivValue)
                Some(chaosEquivValue)
            case None => None
        }
    }

    def getRatio(currencyType: String): Double = {
        if (Currency.ratios.contains(currencyType)) Currency.ratios(currencyType)
        else if (currencyAliases.contains(currencyType) && Currency.ratios.contains(currencyAliases(currencyType))) Currency.ratios(currencyAliases(currencyType))
        else 0.0
    }
}
