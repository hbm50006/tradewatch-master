package org.trade

import akka.actor.ActorSystem
import com.ning.http.client.ProxyServer
import dispatch.Defaults._
import dispatch._

import scala.concurrent.duration._
import scala.util.Random

/**
 *
 * @author hbm50006
 */
case class Proxy(ip:String, port:Int)
object ProxyFinder {

    val rand = new Random()
    val http = Http.configure{builder =>
        builder.setFollowRedirects(true).setRequestTimeoutInMs(5000).setConnectionTimeoutInMs(5000)
    }

    var minimumProxies = 15
    var maximumProxies = minimumProxies + 30
    var proxyList: List[Proxy] = List[Proxy]()

    def invalidateProxy(proxy: Proxy) {
        println("Invalidating proxy.", proxy.ip, proxy.port)
        proxyList = proxyList.filter(_ != proxy)
    }

    def findMoreProxies() {
        val req = url("https://proxy-list.org/english/search.php?search=usa-and-canada&country=usa-and-canada&type=any&port=any&ssl=any")
        var proxy:Proxy = null
        if(proxyList.nonEmpty) {
            // if we have proxies, use one when requesting the list of proxies.
            // better safe than sorry :^)
            proxy = proxyList(rand.nextInt(proxyList.length))
            req.setProxyServer(new ProxyServer(proxy.ip, proxy.port))
        }

        println("Finding more proxies...")
        val responseFuture = http(req OK as.String)
        responseFuture.onSuccess {
            case response =>
                val rawHtml = "<li class=\"proxy\">.*</li>".r.findAllIn(response).toArray

                var newProxyList: Array[Proxy] = Array[Proxy]()
                for (html <- rawHtml) {
                    val rawProxy = html.replace("<li class=\"proxy\">", "").replace("</li>", "")

                    if (rawProxy.indexOf(":") != -1 && rawProxy.indexOf("*") == -1) {
                        val proxyArr = rawProxy.split(":")
                        val proxy = Proxy(proxyArr(0), proxyArr(1).toInt)
                        if(!proxyList.contains(proxy)) newProxyList = newProxyList :+ proxy
                    }
                }

                testProxies(newProxyList)

                println("Done adding new proxies.")
        }
        responseFuture.onFailure {
            case e =>
                println("Couldn't fetch the proxy list site.")
                if(proxy != null) {
                    invalidateProxy(proxy)
                    findMoreProxies()
                }
        }
    }

    def testProxies(newProxyList: Array[Proxy]) = {
        println("Testing new proxies.")

        for(proxy <- newProxyList) {
            val req: Req = createRequestObject.setProxyServer(new ProxyServer(proxy.ip, proxy.port))

            val responseFuture = http(req OK as.String)
            responseFuture.onSuccess{
                case response =>
                    // check if we got a proxy filter screen
                    if(response.toLowerCase.indexOf("path of exile shops indexer") != -1) {
                        proxyList = proxyList :+ proxy
                        println("Adding proxy", proxy.ip, proxy.port)
                    }
            }
        }
    }

    def createRequestObject: Req = {
        val fields = Map("name" -> "Perandus Signet Paua Ring", "buyout_currency" -> "chaos", "buyout" -> "30")
        val params = List("league" -> "Warbands", "online" -> "x", "capquality" -> "x") ++ fields
        val req = url("http://poe.trade/search") <<? params
        req
    }

    def schedule = {
        ActorSystem("proxyfinder").scheduler.schedule(0 seconds, 30 seconds)({
            if(proxyList.length < maximumProxies) findMoreProxies()
        })
    }

    def main(args: Array[String]) {
        schedule
    }
}
