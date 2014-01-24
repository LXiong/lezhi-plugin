package com.buzzinate.lezhi.plugin

import java.text.SimpleDateFormat

import com.buzzinate.common.util.kestrel.KestrelClient
import com.buzzinate.lezhi.Config
import com.buzzinate.lezhi.bean.{GenomeViewMessage, GenomeClickMessage}
import com.buzzinte.common.genome.bean.GenomeInfo
import scala.collection.JavaConverters._

import com.buzzinate.lezhi.util.Logging

import akka.actor.Actor
import com.buzzinate.common.util.http.UserAgentUtil
import com.buzzinte.common.genome.GenomeClient

/**
 * @author jeffrey.ji <jeffrey.ji@buzzinate.com>
 * @date Dec 27, 2012 4:11:32 PM
 *
 */
object GenomeUtil extends Logging {
    //constants for genome statistic
    val APP = "lezhiweb"
    val CLICK = "click"
    val VIEW = "view"

    // send to genome server flag
    val genomeEnable = Config.getBool("genome.enable")
    val genomeClientIp = Config.getString("bx.genome.kestrel.ips")
    val genomeClientPool = Config.getInt("bx.genome.kestrel.pool.size")

    var genomeClient: GenomeClient = null
    if (genomeEnable) {
        genomeClient = new GenomeClient(genomeClientIp, genomeClientPool)
    }
}

class GenomeActor extends Actor with Logging {
    import com.buzzinate.lezhi.plugin.GenomeUtil.{genomeClient, genomeEnable, APP, CLICK, VIEW}

    def receive = {

        case GenomeViewMessage(typeStr, vid, url, referrer, uuid, ip, ua, time) => {
            val data = Map("vid" -> vid, "url" -> url, "referrer" -> referrer, "uuid" -> uuid, "ip" -> UserAgentUtil.ipToInt(ip).toString, "ua" -> ua)
            if (genomeEnable && genomeClient != null) {
                val genomeInfo = new GenomeInfo(APP, VIEW, time, data.asJava)
                genomeClient.send(genomeInfo)
            }
        }

        case GenomeClickMessage(typeStr, vid, url, to, uuid, ip, ua, time) => {
            val data = Map("vid" -> vid, "url" -> url, "to" -> to, "uuid" -> uuid, "ip" -> UserAgentUtil.ipToInt(ip).toString, "ua" -> ua)
            if (genomeEnable && genomeClient != null) {
                val genomeInfo = new GenomeInfo(APP, CLICK, time, data.asJava)
                genomeClient.send(genomeInfo)
            }
        }

        case _ =>
            log.debug("Unknownm genome message...")
    }
}