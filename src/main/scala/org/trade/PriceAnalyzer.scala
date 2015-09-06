package org.trade

/**
 *
 * @author hmcgonig
 */
object PriceAnalyzer {
    def main(args: Array[String]) {
        ProxyFinder.schedule

        while(ProxyFinder.proxyList.length < ProxyFinder.minimumProxies) {
            println("Not enough proxies to continue... Waiting for more. Current amount: " + ProxyFinder.proxyList.length)
            Thread.sleep(60000)
        }

        val tradeList: List[TradeSearch] = TraderMain.extractTradesFromFile("searches.json")
        for(trade <- tradeList) trade.generatePriceCheck()
    }
}
