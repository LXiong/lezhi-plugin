package com.buzzinate.lezhi.crontab

import java.util.Calendar

import com.buzzinate.common.util.DateTimeUtil
import com.buzzinate.lezhi.model.{ SiteConfig, StatSite }
import com.buzzinate.lezhi.util.Logging
import com.buzzinate.lezhi.util.UUIDSerializer

/**
 * @author jeffrey.ji <jeffrey.ji@buzzinate.com>
 * May 8, 2013 4:30:35 PM
 *
 */
object ActiveUser extends Logging {
    /**
     * first use "cqlsh -2" connect to online cassandra
     * then "use lezhi_plugin"
     * then "select * from admin_counts" and store the result to file "/home/jeffrey/adminCounts.txt"
     */
    def getAdminShoupAndViews() {
        val source = scala.io.Source.fromFile("/home/jeffrey/adminCounts.txt")
        val result = source.getLines
        while (result.hasNext) {
            val line = result.next;
            if (line.contains("showups")) {
                print(line.substring(0, 10))
                val indexOfShowups = line.indexOf("showups") + 7
                print(line.substring(indexOfShowups, line.indexOf("|", indexOfShowups)))
                println(line.substring(line.lastIndexOf(",")))
            }
        }
    }

    def getActiveUser() {
        val calendar = Calendar.getInstance()
        val conDay = calendar.getTime()
        calendar.set(2012, 6, 1)
        var endDay = calendar.getTime()
        calendar.add(Calendar.DAY_OF_MONTH, -3)
        var startDay = calendar.getTime()

        log.info("begin to write file")
        var out = new java.io.FileWriter("/root/active.xls", true)
        out.write("日期,插件，按钮，图文，文本\n")
        out.flush()
        log.info("header is done")

        log.info("begin to iterate the site_config")
        val (pluginUuids, buttonUuids, picUuids, textUuids) = SiteConfig.getAllTypeUuids()
        log.info("done in iterating the site_config")

        while (endDay.before(conDay)) {
            log.info("begin to get active number")
            val activePluginNum = StatSite.getActiveSiteNumber(pluginUuids, startDay, endDay).toString
            val activeButtonPluginNum = StatSite.getActiveSiteNumber(buttonUuids, startDay, endDay).toString
            val activePicNum = StatSite.getActiveSiteNumber(picUuids, startDay, endDay).toString
            val activeTextNum = StatSite.getActiveSiteNumber(textUuids, startDay, endDay).toString
            log.info("done in getting active number")

            val record = DateTimeUtil.formatDate(endDay) + "," + activePluginNum + "," + activeButtonPluginNum + "," + activePicNum + "," + activeTextNum + "\n"
            log.info("get the record and begin to write:" + record)
            out.write(record)
            out.flush()
            log.info("the record is written")

            startDay = DateTimeUtil.plusDays(startDay, 1)
            endDay = DateTimeUtil.plusDays(endDay, 1)
        }
        out.close()
    }

    def main(args: Array[String]) = {
        val calendar = Calendar.getInstance()
        val conDay = calendar.getTime()
        calendar.set(2012, 6, 1)
        var endDay = calendar.getTime()
        calendar.add(Calendar.DAY_OF_MONTH, -3)
        var startDay = calendar.getTime()

        log.info("begin to write file")
        var out = new java.io.FileWriter("/var/log/lezhi/showups.xls", true)
        out.write("日期,展示数\n")
        out.flush()
        log.info("header is done")

        log.info("begin to iterate the site_config")
        val uuids = SiteConfig.getAllUuids
        log.info("done in iterating the site_config")
        while (endDay.before(conDay)) {
            log.info("begin to get active site uuids")
            val activeUuids = StatSite.getActiveSiteUuid(uuids, startDay, endDay)
            log.info("done in getting active site uuids")

            var res: Long = 0L
            activeUuids.foreach { uuid =>
                val siteConfig = SiteConfig.get(uuid)
                if (siteConfig.isDefined) {
                    res += siteConfig.get.row * siteConfig.get.col * StatSite.getSiteShowup(uuid, endDay)
                }
            }
            val record = DateTimeUtil.formatDate(endDay) + "," + res.toString + "\n"
            log.info("get the record and begin to write:" + record)
            out.write(record)
            out.flush()

            startDay = DateTimeUtil.plusDays(startDay, 1)
            endDay = DateTimeUtil.plusDays(endDay, 1)
        }
        out.close()
        log.info("end to write file")
    }
}