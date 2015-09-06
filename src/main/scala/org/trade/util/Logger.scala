package org.trade.util

import org.trade.TraderMain


/**
 * @author hmcgonig
 */
object Logger {
    def log(message:String): Unit ={
        if(TraderMain.bot != null && TraderMain.debug) {
            TraderMain.bot.sendIRC().message(TraderMain.ircChannel, message)
        } else {
            println(message)
        }
    }
}
