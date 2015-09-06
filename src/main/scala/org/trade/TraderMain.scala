package org.trade

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.concurrent.Executors

import akka.actor.ActorSystem
import org.pircbotx.{Configuration, PircBotX}
import org.trade.util.Logger
import org.trade.util.DynamicJson._

import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.io.Source

/**
 *
 * @author hbm5006
 */
object TraderMain {
    val threadPool = Executors.newFixedThreadPool(30)
    var tradeList: List[TradeSearch] = null
    var accountBlacklist = Source.fromFile("accounts.blacklist").getLines.toList
    var bot:PircBotX = null
    var debug = false
    var run = false
    var ircChannel = "#tradewatch"

    def main(args: Array[String]) {
        // attach to IRC if irc param is specified
        if(args.contains("--irc")){
            bot = new PircBotX(new Configuration.Builder()
                .setName("tradewatch")
                .setNickservPassword(args(args.indexOf("--irc") + 1))
                .setServerHostname("irc.broke-it.com")
                .setServerPort(6660)
                .addListener(new TradeIRCListener)
                .buildConfiguration())
            new Thread(new Runnable { def run() { bot.startBot() }}).start()
            Thread.sleep(5000)
            bot.sendRaw().rawLine("JOIN " + ircChannel + " " + args(args.indexOf("--irc") + 2) + "\n")
        } else run = true

        tradeList = extractTradesFromFile("searches.json")

        // start up the proxyfinder. this populates a proxy list.
        ProxyFinder.minimumProxies = (tradeList.length/2) + 1
        ProxyFinder.maximumProxies = ProxyFinder.minimumProxies + 30
        Logger.log("Configuring ProxyFinder with min: " + ProxyFinder.minimumProxies + " max: " + ProxyFinder.maximumProxies)
        ProxyFinder.schedule

        ActorSystem("tradermain").scheduler.schedule(0 seconds, 30 seconds)({
            if(run && ProxyFinder.proxyList.length >= ProxyFinder.minimumProxies) {
                val minuteCheck = new SimpleDateFormat("mm").format(Calendar.getInstance().getTime).takeRight(1)
                if(minuteCheck == "9" || minuteCheck == "0" || minuteCheck == "1") {
                    Logger.log(Calendar.getInstance().getTime + " | Running searches...")
                    lookForTrades(tradeList)
                }
            } else if (ProxyFinder.proxyList.length < ProxyFinder.minimumProxies) {
                Logger.log("Still waiting for more proxies... Current amount: " + ProxyFinder.proxyList.length + " Required: " + ProxyFinder.minimumProxies)
            }
        })
    }

    def lookForTrades(tradeList: List[TradeSearch]) {
        for(trade <- tradeList) threadPool.execute(new Runnable { override def run() = trade.search() })
    }

    def extractTradesFromFile(inputFile: String): List[TradeSearch] = {
        // populate the list of trade searches from the search json file
        val shoppingListJsonString = Source.fromFile("searches.json").mkString
        val searchList = parse(shoppingListJsonString)
        var tradeList: List[TradeSearch] = List()
        for (search <- searchList.searches) {
            // dont include the mods list or the description
            val params = for (param <- search.fieldNames().filter(_ != "mods").filter(_ != "description"))
                yield param -> search.selectDynamic(param).asText
            val mods = for (mod <- search.mods; field <- mod.fieldNames()) yield field -> mod.selectDynamic(field).asText
            tradeList = new TradeSearch(searchList.league, search.description, params.toMap, mods.toList) :: tradeList
        }
        tradeList
    }
}
