package com.buzzinate.lezhi.test

import java.util.Date

import scala.collection.JavaConverters.mapAsJavaMapConverter
import scala.util.matching.Regex

import com.buzzinate.common.util.http.UserAgentUtil
import com.buzzinate.lezhi.util.Logging
import com.buzzinte.common.genome.bean.GenomeInfo
import com.codahale.jerkson.Json

/**
 * @author jeffrey.ji <jeffrey.ji@buzzinate.com>
 * Jul 5, 2013 2:36:48 PM
 *
 */
object Test extends Logging {
    val pattern = new Regex(""""ip":"(-?[1-9]\d*)","vid":"([0-9A-Za-z]*)"""")

    def main(args: Array[String]) = {
        val data = Map("vid" -> "vid", "url" -> "url", "referrer" -> "referrer", "uuid" -> "uuid", "ip" -> UserAgentUtil.ipToInt("192.168.1.132").toString, "ua" -> "ua")
        val genomeInfo = new GenomeInfo("APP", "VIEW", new Date, data.asJava)
        val line = Json.generate(genomeInfo)
        println(line)

        println(pattern)
        val res = pattern.findFirstMatchIn(line)
        println(res)
        if (res.isDefined) {
            val ip = res.get.group(1)
            val vid = res.get.group(2)
            println(ip)
            println(vid)
        }
    }
}