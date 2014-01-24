/**
 * @author jeffrey.ji <jeffrey.ji@buzzinate.com>
 * @date Jan 14, 2013 5:12:53 PM
 *
 */
package com.buzzinate.lezhi

import com.buzzinate.common.util.kestrel.KestrelClient
import com.buzzinate.lezhi.analytic.{ AnalyticActor, CollectActor, UpdateActor }
import com.buzzinate.lezhi.bean.AnalyticMessage
import com.buzzinate.lezhi.util.Logging
import com.typesafe.config.ConfigFactory

import akka.actor.{ ActorSystem, Props, actorRef2Scala }
import akka.routing.RoundRobinRouter

object AnalyticServer extends Logging {
    val LEZHI = "lezhi"
    val kestrelClient = new KestrelClient(
        Config.getString("lezhi.kestrel.ips"),
        Config.getInt("lezhi.kestrel.pool.size"))

    val system = ActorSystem("Analytic", ConfigFactory.load(Thread.currentThread.getContextClassLoader, "analytic.conf"))

    val MAX_COLLECTOR = Config.getInt("lz.analytic.akka.collector")
    val bgCollectors = for (i <- 0 until MAX_COLLECTOR) yield {
        system.actorOf(Props(new CollectActor).withDispatcher("collector-dispatcher"), "CollectActor-" + i)
    }
    val collectRouter = system.actorOf(Props[CollectActor].withRouter(RoundRobinRouter(routees = bgCollectors)), "collect_router")

    val MAX_UPDATER = Config.getInt("lz.analytic.akka.updater")
    val bgUpdaters = for (i <- 0 until MAX_UPDATER) yield {
        system.actorOf(Props(new UpdateActor).withDispatcher("updater-dispatcher"), "UpdateActor-" + i)
    }
    val updateRouter = system.actorOf(Props[UpdateActor].withRouter(RoundRobinRouter(routees = bgUpdaters)), "update_router")

    val MAX_ANALYTIC = Config.getInt("lz.analytic.akka.analytic")
    val bgAnalytics = for (i <- 0 until MAX_ANALYTIC) yield {
        system.actorOf(Props(new AnalyticActor).withDispatcher("analytic-dispatcher"), "AnalyticActor-" + i)
    }
    val analyticRouter = system.actorOf(Props[AnalyticActor].withRouter(RoundRobinRouter(routees = bgAnalytics)), "analytic_router")

    def main(args: Array[String]) {
        log.info("starting analytic server")
        dequeue
    }

    def dequeue() {
        for (i <- 0 until MAX_ANALYTIC) {
            analyticRouter ! AnalyticMessage
        }
    }
}