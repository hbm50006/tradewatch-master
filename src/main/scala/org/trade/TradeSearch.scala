package org.trade

import java.io.{File, FileInputStream, FileOutputStream}
import java.util.Calendar
import javax.swing.{JOptionPane, JScrollPane, JTextArea}

import com.esotericsoftware.kryo.io.{Input, Output}
import com.ning.http.client.ProxyServer
import com.twitter.chill.ScalaKryoInstantiator
import dispatch.Defaults._
import dispatch._
import org.trade.util.{Currency, Logger}

import scala.util.Random

/**
 *
 * @author hbm5006
 */
case class Listing(itemName:String, account:String, ign:String, price:String, searchUrl:String) {
    def getPriceInChaos: Double = {
        if(price.isEmpty) return 0
        val priceArr = price.split(" ", 2)
        priceArr(0).toDouble * Currency.ratios(priceArr(1))
    }
}
class TradeSearch(league:String, description:String, fields: Map[String, String], mods: List[(String, String)]) {

    val http = Http.configure{builder => builder.setFollowRedirects(true)}
    val kryo = new ScalaKryoInstantiator().newKryo()
    kryo.register(classOf[Listing])
    kryo.register(classOf[Array[Listing]])
    val rand = new Random()

    val tradeDescriptor = if(description.isEmpty) fields("name") else description
    var state = "NOT_STARTED"
    var error = ""
    var previousListings = Array[Listing]()
    var searchUrl = ""
    var retry = 0

    def savePreviousListings() {
        val output = new Output(new FileOutputStream("listings" + java.io.File.separator + searchUrl.split("/").last + ".search"))
        kryo.writeObject(output, previousListings)
        output.close()
    }

    def loadPreviousListings(url: String): Array[Listing] = {
        val fileName = "listings" + java.io.File.separator + url.split("/").last + ".search"
        if(!new File(fileName).exists()) return Array[Listing]()
        val input = new Input(new FileInputStream(fileName))
        val listings = kryo.readObject(input, classOf[Array[Listing]])
        input.close()
        listings
    }

    def notify(listing:Listing) {
        if(TraderMain.bot != null) {
            TraderMain.bot.sendIRC().message(TraderMain.ircChannel, listing.itemName + " | " + listing.account + " | " +
                listing.price + " | " + tradeDescriptor + " | " + listing.searchUrl + " | @" + listing.ign +
                " Hey, I'll buy your " + listing.itemName + " for your buyout of " + listing.price + " on " + league)
        } else {
            val textArea = new JTextArea(7, 30)
            textArea.setText("Item name: " + listing.itemName + "\nType: " + tradeDescriptor + "\nAccount: " + listing.account + "\nPrice: " + listing.price +
                "\nSearch URL: " + listing.searchUrl + "\nMessage: " + "@" + listing.ign + " Hey, I'll buy your " +
                listing.itemName + " for your buyout of " + listing.price + " on " + league)

            textArea.setWrapStyleWord(true)
            textArea.setLineWrap(true)
            textArea.setCaretPosition(0)
            textArea.setEditable(false)
            JOptionPane.showMessageDialog(null, new JScrollPane(textArea),"Found a new trade for you:", JOptionPane.INFORMATION_MESSAGE)
        }
    }

    def getTagAttr(attrName:String, text:String) = {
        val regex = ("(" + attrName + "=\".*?\")").r
        "(\".*?\")".r.findFirstIn(regex.findFirstIn(text).get).get.replace("\"", "")
    }

    def generatePriceCheck() = {
        val noBuyoutFields = fields.filter(_._1 != "buyout_currency").filter(_._1 != "buyout_max")
        val params = List("league" -> league, "online" -> "x", "capquality" -> "x") ++ noBuyoutFields ++ mods

        val proxy = ProxyFinder.proxyList(rand.nextInt(ProxyFinder.proxyList.length))

        val req = url("http://poe.trade/search") <<? params setProxyServer new ProxyServer(proxy.ip, proxy.port)
        http(req).either.map {
            case Right(response) =>
                Logger.log("=======> Listings for " + tradeDescriptor + ", Return code: " + response.getStatusCode + " <=======")
                if (response.getStatusCode == 200) {
                    val sortedReq = url(response.getUri.toString).POST << "sort=price_in_chaos&bare=true" <:< List("Content-Type" -> "application/x-www-form-urlencoded")
                    http(sortedReq).either.map {
                        case Right(sortedResponse) => {
                            if(sortedResponse.getStatusCode == 200) {
                                val rawListings = "<tbody.*>".r.findAllIn(sortedResponse.getResponseBody).toArray
                                val listings = for (l <- rawListings) yield {
                                    val name = getTagAttr("data-name", l).replace("&#39;", "'")
                                    val price = getTagAttr("data-buyout", l)
                                    val account = getTagAttr("data-seller", l)
                                    val ign = getTagAttr("data-ign", l)

                                    // convert price to chaos
                                    val priceArr = price.split(" ", 2)

                                    var priceInChaos = 0.0
                                    if(priceArr.length > 1) {
                                        priceInChaos = priceArr(0).toDouble * Currency.ratios(priceArr(1))
                                    }

                                    Listing(name, account, ign, price, response.getUri.toString)
                                }

                                val prices = for(listing <- listings) yield { listing.getPriceInChaos }
                                val count = listings.length

                                Logger.log("Found " + count + " results.")
                                Logger.log("Lowest 5 buyouts")
                                for(lowPrice <- prices.filter(_ != 0.0).take(5)) Logger.log(lowPrice + " chaos or " + lowPrice/Currency.ratios("exalted") + " exalts")
                            }
                        }
                        case Left(err) => throw err
                    }
                }
                else {
                    ProxyFinder.invalidateProxy(proxy)
                    if(ProxyFinder.proxyList.length > ProxyFinder.minimumProxies) { // we have proxies to burn, re run
                        fetchItemList()
                    }
                }
            case Left(err) => throw err
        }
    }

    def fetchItemList() {
        val params = List("league" -> league, "online" -> "x", "capquality" -> "x") ++ fields ++ mods
        val proxy = ProxyFinder.proxyList(rand.nextInt(ProxyFinder.proxyList.length))
        val req = url("http://poe.trade/search") <<? params setProxyServer new ProxyServer(proxy.ip, proxy.port)

        http(req).either.map {
            case Right(response) =>
                Logger.log("DEBUG - " + Calendar.getInstance().getTime + " - Search for " + tradeDescriptor + " complete! Proxy: " + proxy.toString + " Return code: " + response.getStatusCode)

                if(response.getStatusCode == 200) {
                    val rawListings = "<tbody.*>".r.findAllIn(response.getResponseBody).toArray
                    if(searchUrl.isEmpty) previousListings = loadPreviousListings(response.getUri.toString)
                    searchUrl = response.getUri.toString
                    previousListings = for(l <- rawListings) yield {
                        val name = getTagAttr("data-name", l).replace("&#39;", "'")
                        val price = getTagAttr("data-buyout", l)
                        val account = getTagAttr("data-seller", l)
                        val ign = getTagAttr("data-ign", l)
                        val listing = Listing(name, account, ign, price, response.getUri.toString)
                        if(!previousListings.contains(listing) &&
                            (listing.price.isEmpty || listing.getPriceInChaos != 0) &&
                            !TraderMain.accountBlacklist.contains(account)) notify(listing)
                        listing
                    }

                    retry = 0
                } else if(response.getStatusCode == 502 && retry < 5) { // retry logic to handle 502s
                    retry += 1
                    fetchItemList()
                } else { // kill the proxy
                    ProxyFinder.invalidateProxy(proxy)
                    if(ProxyFinder.proxyList.length > ProxyFinder.minimumProxies) { // we have proxies to burn, re run
                        fetchItemList()
                    }
                }
            case Left(err) =>
                ProxyFinder.invalidateProxy(proxy)
                if(ProxyFinder.proxyList.length > ProxyFinder.minimumProxies) { // we have proxies to burn, re run
                    fetchItemList()
                }
        }
    }

    def search() = {
        try {
            fetchItemList()
            state = "RUNNING"
            error = ""
        } catch {
            case e:Exception =>
                state = "ERROR"
                error = e.getMessage
                val desc = if(description.isEmpty) fields("name") else description
                Logger.log("DEBUG - " + Calendar.getInstance().getTime + " | Something went wrong when fetching from poe.trade for: " + desc + " Exception Message: " + e.getMessage)
        }
    }
}
