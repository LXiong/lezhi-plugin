package com.buzzinate.lezhi.plugin

import org.scalatest.{BeforeAndAfter, FunSuite}
import com.buzzinate.lezhi.bean.GenomeViewMessage
import java.util.Date
import com.buzzinate.lezhi.plugin.PluginConstant.genomeActor

/**
 * Created with IntelliJ IDEA.
 * User: magic
 * Date: 13-7-15
 * Time: 下午7:01
 * To change this template use File | Settings | File Templates.
 */
class GenomeActorTest extends FunSuite with BeforeAndAfter {
    // private val system = ActorSystem("test", ConfigFactory.load(Thread.currentThread.getContextClassLoader, "plugin.conf"))
    // private val genomeActor = system.actorOf(Props(new GenomeActor).withDispatcher("genome-dispatcher"), "GenomeActor")

    test("test send view message to genome server") {
        val vid = "1CHyKjHTDWiu7vrp3kNf"
        val url = "www.bshare.cn"
        val referrer = "www.baidu.com"
        val uuid = "1aaa0c58-26ca-4f89-baec-890fdc7df285"
        val ip = "127.0.0.1"
        val agent = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/28.0.1500.71 Safari/537.36"
        genomeActor ! GenomeViewMessage("insite", vid, url, referrer, uuid, ip, agent, new Date)

    }
}

