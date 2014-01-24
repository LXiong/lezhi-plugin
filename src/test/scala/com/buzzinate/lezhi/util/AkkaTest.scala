package com.buzzinate.lezhi.util

import java.util.{ Date, UUID => JavaUUID }
import com.buzzinate.common.util.serialize.HessianSerializer
import com.buzzinate.lezhi.bean.InClickMessage
import com.buzzinate.lezhi.plugin.PluginConstant.{ LEZHI, kestrelClient }
import com.typesafe.config.ConfigFactory
import akka.actor.{ Actor, ActorSystem, Props, actorRef2Scala }
import akka.routing.RoundRobinRouter
import com.buzzinate.lezhi.Config

object AkkaTest {
    val uuid = Some(JavaUUID.fromString("9c54915f-4ef0-4a16-9988-2ca87cae9d0a"))
    val url = "http://www.test.com"
    val title = "test"

    def main(args: Array[String]) {
        (0 until 500).foreach { i =>
            kestrelClient.put(LEZHI, HessianSerializer.serialize(InClickMessage(uuid, url + i, title + i, new Date)))
            println(i)
        }
        println(Config.getInt("lz.analytic.flush.time"))
        Thread.sleep(Config.getInt("lz.analytic.flush.time").toLong)
        kestrelClient.put(LEZHI, HessianSerializer.serialize(InClickMessage(uuid, url, title, new Date)))
    }

    def testRobbin() {
        val system = ActorSystem("test", ConfigFactory.load(Thread.currentThread.getContextClassLoader, "plugin.conf"))
        val roundRobinRouter = system.actorOf(Props[PrintlnActor].withRouter(RoundRobinRouter(5)), "analytic_collector")
        1 to 100000 foreach {
            i ⇒ roundRobinRouter ! i
        }
    }
}

class PrintlnActor extends Actor {
    def receive = {
        case msg ⇒
            println("Received message '%s' in actor %s".format(msg, self.path.name))
    }
}