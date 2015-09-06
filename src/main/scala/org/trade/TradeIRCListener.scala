package org.trade

import java.io.File
import java.lang.management.ManagementFactory

import org.pircbotx.PircBotX
import org.pircbotx.hooks.ListenerAdapter
import org.pircbotx.hooks.types.GenericMessageEvent

import scala.sys.process._

class TradeIRCListener extends ListenerAdapter[PircBotX] {
    override def onGenericMessage(event: GenericMessageEvent[PircBotX]) {

        if(event.getUser.getNick == "mirrorbot" && event.getMessage.startsWith("!"))
            event.respond("!go find something better to do")

        /*
         * Command explanation
         */
        if(event.getMessage.startsWith("!commands")) {
            event.respond("Available commands are: " +
                " | !start - start the bot" +
                " | !stop - stop the bot" +
                " | !status - get the current status of the bot" +
                " | !force - force the bot to look for trades regardless of its current status" +
                " | !blacklist - show the current blacklisted accounts" +
                " | !log - the url for the trade log"
            )
        }

        /*
         * State commands
         */
        else if(event.getMessage.startsWith("!start")) {
            TraderMain.run = true
            event.respond("Tradebot started.")
        }

        else if(event.getMessage.startsWith("!stop")) {
            TraderMain.run = false
            event.respond("Tradebot stopped.")
        }

        else if(event.getMessage.startsWith("!status")) {
            if(TraderMain.run) event.respond("Current tradebot status: started")
            else event.respond("Current tradebot status: stopped")

            var errors = 0
            for(trade <- TraderMain.tradeList) if(trade.state == "ERROR") errors += 1
            event.respond("Trades: " + TraderMain.tradeList.length + " Trades in an error state: " + errors)

            for(trade <- TraderMain.tradeList) {
                if(trade.state == "ERROR") {
                    event.respond(trade.tradeDescriptor + ": " + trade.state)
                    event.respond(trade.error)
                }
            }
        }

        else if(event.getMessage.startsWith("!force")) {
            event.respond("Looking for trades.")
            TraderMain.lookForTrades(TraderMain.tradeList)
        }

        else if(event.getMessage.startsWith("!log")) {
            event.respond("tinyurl.com/mirrorhuntinglog")
        }

        else if(event.getMessage.startsWith("!blacklist")) {
            if(TraderMain.accountBlacklist.isEmpty) event.respond("There are no entries in the list.")
            else event.respond(TraderMain.accountBlacklist.mkString(", "))
        }

        /*
         * Updating and debugging
         */
        else if(event.getMessage.startsWith("!update")) {
            event.respond("Serializing previous searches...")
            for(trade <- TraderMain.tradeList) trade.savePreviousListings()

            event.respond("Pulling new source from git...")
            "git pull".!!
            "./start.sh".!!

            // hard kill it
            event.respond("Going down for restart (Don't forget to start the bot with !start)...")
            val pid = ManagementFactory.getRuntimeMXBean.getName.split("@")(0)
            ("kill -9 " + pid).!!
        }

        else if(event.getMessage.startsWith("!proxylist")) {
            var output = "Proxy List(" + ProxyFinder.proxyList.length + " total, Required: " + ProxyFinder.minimumProxies + ")"
            for(proxy <- ProxyFinder.proxyList) { output = output + " | " + proxy.ip + ":" + proxy.port }
            event.respond(output)
        }

        else if(event.getMessage.startsWith("!debug")) {
            if(!event.getMessage.split(" ").equals("true") && event.getMessage.split(" ").equals("false"))
                event.respond("Valid values are: true | false")
            else {
                TraderMain.debug = event.getMessage.split(" ").last.toBoolean
                event.respond("Setting debug mode to: " + TraderMain.debug)
            }
        }

        else if(event.getMessage.startsWith("!clearcache")) {
            for {
                files <- Option(new File("./listings/").listFiles)
                file <- files if file.getName.endsWith(".search")
            } file.delete()
            for(trade <- TraderMain.tradeList) trade.previousListings = Array[Listing]()
            event.respond("Item cache cleared.")
        }
    }
}
