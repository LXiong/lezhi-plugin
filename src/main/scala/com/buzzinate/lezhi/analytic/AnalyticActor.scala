package com.buzzinate.lezhi.analytic

import com.buzzinate.common.util.serialize.HessianSerializer
import com.buzzinate.lezhi.AnalyticServer.{ LEZHI, collectRouter, kestrelClient }
import com.buzzinate.lezhi.Config
import com.buzzinate.lezhi.bean.AnalyticMessage
import com.buzzinate.lezhi.util.Logging

import akka.actor.{ Actor, actorRef2Scala }
/**
 * @author jeffrey.ji <jeffrey.ji@buzzinate.com>
 * @date Jan 16, 2013 3:06:02 PM
 *
 */
class AnalyticActor extends Actor with Logging {
    private val KESTREL_SLEEP_INTERVAL = Config.getInt("analytics.thread.interval").toLong

    def receive = {
        /**
         * 接受到一条消息后即进入无限循环去队列取消息
         */
        case AnalyticMessage => {
            while (true) {
                try {
                    val message = HessianSerializer.deserialize(kestrelClient.get(LEZHI).asInstanceOf[Array[Byte]])
                    if (message == null) {
                        Thread.sleep(KESTREL_SLEEP_INTERVAL)
                    } else {
                        collectRouter ! message
                    }
                } catch {
                    case ex: Exception => log.error(ex, "lezhi analytic data exception: ")
                }
            }
        }
    }
}