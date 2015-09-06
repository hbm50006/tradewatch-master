package org.trade.forum

import dispatch.Defaults._
import dispatch._
import org.jsoup.Jsoup
import org.trade.item.ItemParser

import scala.collection.JavaConversions._
import scala.collection.mutable.ArrayBuffer

class ThreadIndex {
    val forumPageUrl = "https://www.pathofexile.com/forum/view-forum/591/page/"
    val threadPageUrl = "https://www.pathofexile.com/forum/view-thread/"
    val http = Http.configure { builder => builder.setFollowRedirects(true) }

    def search() {
        val itemParser = new ItemParser
        val threadIds = ArrayBuffer[Int]()

        for(i <- (1 to 15).par) {
            val req = url(forumPageUrl + i)
            val response = http(req OK as.String).apply()
            val ids = getThreadIds(response)
            threadIds ++= ids
        }
        println("starting thread searches")
        val start = System.currentTimeMillis()

        var threadsTotal = 0
        var threadsParsed = 0

        for(id <- threadIds.par) {
            val req = url(threadPageUrl + id)
            val response = http(req OK as.String).apply()
            //println("thread " + threadPageUrl + id)
            threadsTotal += 1
            val parsed = ForumSearch.parseShopThread(response, id, itemParser)
            if(parsed) threadsParsed += 1
        }
        println("total time: " + (System.currentTimeMillis() - start))
        println("threads " + threadsParsed + "/" + threadsTotal)
    }

    def getThreadIds(html: String): List[Int] = {
        val doc = Jsoup.parse(html)
        val threads = doc.getElementsByClass("thread").toList
        for(t <- threads) yield {
            t.getElementsByClass("setReadButton").first().attr("id").split("_")(2).toInt
        }
    }
}
