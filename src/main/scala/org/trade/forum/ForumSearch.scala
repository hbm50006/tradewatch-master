package org.trade.forum

import java.text.SimpleDateFormat
import java.util.Calendar

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.trade.item.{Item, ItemParser}
import org.trade.util.DynamicJson

import scala.collection.JavaConversions._

object ForumSearch {
    val itemsBeginString = "{ (new R("
    val itemsEndString = ")).run();"
    val updateDateFormat = new SimpleDateFormat("MMMMM dd, yyyy hh:mm aaa")

    def main(args: Array[String]) {
        val index = new ThreadIndex
        index.search()
    }

    def parseShopThread(html: String, threadId: Int, parser: ItemParser): Boolean = {
        val doc = Jsoup.parse(html)

        // check the last update time or post date to see if we can skip parsing this thread
        val firstPost = doc.getElementsByClass("content-container").first()
        val lastEdit = firstPost.getElementsByClass("last_edited_by")
        val recentThreshold = Calendar.getInstance()
        recentThreshold.add(Calendar.MINUTE, -30)
        if(lastEdit.size > 0) {
            try{
                val date = updateDateFormat.parse( lastEdit.first.text().split(" on ")(1))
                if(date.before(recentThreshold.getTime)) return false
            } catch {
                case e:Exception => println("error in parsing last edit for https://www.pathofexile.com/forum/view-thread/" + threadId)
            }
        }
        else {
            //if there is no last update time, check if the thread was created recently
            val postDate = updateDateFormat.parse(doc.getElementsByClass("post_date").first().text())
            if(postDate.before(recentThreshold.getTime)) return false
        }

        val itemsHtml = doc.getElementsByTag("script").last().outerHtml()
        val begin = itemsHtml.indexOf(itemsBeginString) + itemsBeginString.length
        val end = itemsHtml.indexOf(itemsEndString)

        if(begin < 0 || end < 0) return false

        val items = DynamicJson.parse(itemsHtml.substring(begin, end))

        val buyouts = parseBuyouts(doc.getElementsByClass("content").first())

        for(i <- items) {
            val list = i.toList
            val id = list.head.asInt
            val buyout = buyouts.getOrElse(id, 0.0)
            val item = new Item(id, threadId, buyout, list(1))

            if(item.verified && buyout > 0.0) parser.scanItem(item)
        }
        true
    }

    // given an html element, return a map of all item ids within it and its children to their buyouts in chaos equivalence
    def parseBuyouts(element: Element, buyout: Double = 0.0, buyouts: Map[Int, Double] = Map()): Map[Int, Double] = {
        if (element.classNames.contains("spoiler")) {
            val thisBuyout = element.getElementsByClass("spoilerTitle").first().getElementsByTag("span").first().text
            parseBuyouts(element.getElementsByClass("spoilerContent").first(), Buyout.parse(thisBuyout.trim).getOrElse(buyout), buyouts)
        }
        else {
            // filter out elements that aren't items
            val nonItems = element.children().filter(!_.classNames.contains("itemContentLayout"))
            val replaced = nonItems.map(_.outerHtml()).fold(element.html)((a, b) => a.replace(b, ""))
            val filteredElement = Jsoup.parse(replaced).body()
            val items = filteredElement.children().filter(_.classNames.contains("itemContentLayout"))
            val html = filteredElement.html()

            // count anything after an item element (and before the next) as potential buyout text
            val buyoutTexts = for (i <- items.indices) yield {
                val start = html.indexOf(items(i).outerHtml()) + items(i).outerHtml().length
                val text =
                    if (i < items.size - 1) html.substring(start, html.indexOf(items(i + 1).outerHtml()))
                    else html.substring(start)
                val itemBuyout: Double = Buyout.parse(text.trim).getOrElse(buyout)
                (items(i).id().split("-")(2).toInt, itemBuyout)
            }
            val subBuyouts = for (e <- element.getElementsByClass("spoiler")) yield parseBuyouts(e, buyout)
            // add the buyouts of items at this level and within child spoilers and return
            buyouts ++ buyoutTexts.toMap ++ subBuyouts.flatten.toMap
        }
    }
}
