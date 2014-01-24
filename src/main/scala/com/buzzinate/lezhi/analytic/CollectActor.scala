package com.buzzinate.lezhi.analytic

import java.util.Date
import java.sql.{ Date => SqlDate }
import java.util.UUID
import scala.Option.option2Iterable
import org.apache.commons.lang.StringUtils
import com.buzzinate.common.util.DateTimeUtil
import com.buzzinate.common.util.string.UrlUtil
import com.buzzinate.lezhi.AnalyticServer.updateRouter
import com.buzzinate.lezhi.Config
import com.buzzinate.lezhi.bean.InClickMessage
import com.buzzinate.lezhi.bean.OutClickMessage
import com.buzzinate.lezhi.bean.ShowupMessage
import com.buzzinate.lezhi.bean.StatsType
import com.buzzinate.lezhi.bean.StatsType.StatsType
import com.buzzinate.lezhi.bean.ViewMessage
import com.buzzinate.lezhi.model.AdminCounts
import com.buzzinate.lezhi.model.{ StatPage, StatSite }
import com.buzzinate.lezhi.util.Logging
import akka.actor.Actor
import akka.actor.actorRef2Scala
import com.buzzinate.lezhi.bean.UpdateMessage
import com.buzzinate.lezhi.util.Util

/**
 * @author jeffrey created on 2012-11-27 下午11:44:08
 *
 */
class CollectActor extends Actor with Logging {
    private val flushInterval = Config.getInt("lz.analytic.flush.time").toLong
    private var lastFlushed = System.currentTimeMillis
    private val pathSubLimit = Config.getInt("uuidpath.sub.limit")

    //存放admin统计数据
    private var adminMap = new java.util.HashMap[(String, String), Long]
    //存放sitepage统计数据
    private var sitePage = new java.util.HashSet[(UUID, String)]()
    //存放pageInfo统计数据
    private var pageInfo = new java.util.HashSet[(String, String, Option[UUID])]()
    //存放site inclick,outclick and showup
    private var siteMap = new java.util.HashMap[(String, UUID, String), Long]()
    //存放page inclick,outclick and showup, key=(url, date) 
    private var pageMap = new java.util.HashMap[(String, String), StatPage.StatPage]()
    // uuid <=> path
    private var pathUuidMap = new java.util.HashMap[String, String]

    def receive = {
        case ViewMessage(uuid, url, date) => {
            log.debug("Processing view: [url=" + url + ", uuid=" + uuid.map(_.toString).getOrElse("") + "]")
            incrToMap(StatsType.View, DateTimeUtil.formatDate(date), "", "", "")
        }

        case InClickMessage(uuid, url, title, date) => {
            log.debug("Processing inclick: [url=" + url + ", uuid=" + uuid.map(_.toString).getOrElse("") + "]")
            val day = DateTimeUtil.formatDate(date)
            if (uuid.nonEmpty) {
                incrSite(StatSite.COLUMN_IN_CLICKS, uuid.get, day)
                sitePage.add((uuid.get, url))
                pageInfo.add((url, title, uuid))
            }
            incrPage(StatPage.COLUMN_IN_CLICKS, url, day, uuid.map(_.toString).getOrElse(""))
            incrToMap(StatsType.InClick, day, "", "", "")

            checkFlush
        }

        case OutClickMessage(uuid, url, title, ref, pluginType, pic, date) => {
            log.debug("Processing outclick: [url=" + url + ", uuid=" + uuid.map(_.toString).getOrElse("") + "]")
            val day = DateTimeUtil.formatDate(date)
            if (uuid.nonEmpty) {
                incrSite(StatSite.COLUMN_OUT_CLICKS, uuid.get, day)
                sitePage.add((uuid.get, url))
                pageInfo.add((url, title, uuid))
            }
            incrPage(StatPage.COLUMN_OUT_CLICKS, url, day, uuid.map(_.toString).getOrElse(""))
            incrToMap(StatsType.OutClick, day, ref, pluginType, pic)

            checkFlush
        }

        case ShowupMessage(uuid, url, ref, pluginType, pic, date) => {
            log.debug("Processing showups: [url=" + url + ", uuid=" + uuid.map(_.toString).getOrElse("") + "]")
            val day = DateTimeUtil.formatDate(date)
            if (ref == "insite") {
                if (uuid.nonEmpty) {
                    incrSite(StatSite.COLUMN_SHOWUPS, uuid.get, day)
                }
                incrPage(StatPage.COLUMN_SHOWUPS, url, day, uuid.map(_.toString).getOrElse(""))
            }
            incrToMap(StatsType.Showup, day, ref, pluginType, pic)

            mapUuidPath(uuid, url)
            checkFlush
        }
    }

    private def checkFlush() {
        if (System.currentTimeMillis >= lastFlushed + flushInterval) {
            log.info("check flush publisher data to mysql at: " + DateTimeUtil.formatDate(new Date, DateTimeUtil.FMT_DATE_YYYY_MM_DD_HH_MM_SS))
            updateRouter ! UpdateMessage(siteMap, sitePage, pageInfo, pageMap, adminMap, pathUuidMap)
            siteMap = new java.util.HashMap[(String, UUID, String), Long]()
            sitePage = new java.util.HashSet[(UUID, String)]()
            pageInfo = new java.util.HashSet[(String, String, Option[UUID])]()
            pageMap = new java.util.HashMap[(String, String), StatPage.StatPage]()
            adminMap = new java.util.HashMap[(String, String), Long]()
            pathUuidMap = new java.util.HashMap[String, String]()

            lastFlushed = System.currentTimeMillis
        }
    }

    private def incrSite(statistic: String, uuid: UUID, date: String) {
        val value = siteMap.get((statistic, uuid, date))
        siteMap.put((statistic, uuid, date), value + 1)
    }

    private def incrPage(statistic: String, url: String, date: String, uuid: String) {
        var statPage = pageMap.get((url, date))
        if (statPage == null) {
            val day = new SqlDate(DateTimeUtil.getDateDay(DateTimeUtil.convertDate(date)).getTime)
            statPage = StatPage.StatPage(0, Util.urlToBytes(url), Util.uuidToByte(uuid.toString), day)
        }
        if (StatPage.COLUMN_IN_CLICKS == statistic) {
            statPage.inClicks += 1
        } else if (StatPage.COLUMN_OUT_CLICKS == statistic) {
            statPage.outClicks += 1
        } else {
            statPage.showUps += 1
        }
        pageMap.put((url, date), statPage)
    }

    private def incrAdminCount(statisticName: String, date: String) {
        val value = adminMap.get((statisticName, date))
        adminMap.put((statisticName, date), value + 1L)
    }

    private def incrToMap(addType: StatsType, date: String, ref: String, pluginType: String, pic: String) = {
        addType match {
            case StatsType.View => incrAdminCount(AdminCounts.COLUMN_VIEWS, date)
            case StatsType.InClick => incrAdminCount(AdminCounts.COLUMN_IN_CLICKS, date)

            case StatsType.OutClick => {
                incrAdminCount(AdminCounts.COLUMN_OUT_CLICKS, date)
                ref match {
                    case "insite" =>
                        pluginType match {
                            case "fixed" => incrAdminCount(AdminCounts.COLUMN_CLICKS_FIXED_I, date)
                            case "slide" => incrAdminCount(AdminCounts.COLUMN_CLICKS_SLIDE_I, date)
                        }
                        pic match {
                            case "true" => incrAdminCount(AdminCounts.COLUMN_CLICKS_PIC_I, date)
                            case "false" => incrAdminCount(AdminCounts.COLUMN_CLICKS_NOPIC_I, date)
                        }
                    case "trending" =>
                        pluginType match {
                            case "fixed" => incrAdminCount(AdminCounts.COLUMN_CLICKS_FIXED_T, date)
                            case "slide" => incrAdminCount(AdminCounts.COLUMN_CLICKS_SLIDE_T, date)

                        }
                        pic match {
                            case "true" => incrAdminCount(AdminCounts.COLUMN_CLICKS_PIC_T, date)
                            case "false" => incrAdminCount(AdminCounts.COLUMN_CLICKS_NOPIC_T, date)
                        }
                    case _ => log.info("Unknown outclick ref")
                }
            }

            case StatsType.Showup => {
                incrAdminCount(AdminCounts.COLUMN_SHOWUPS, date)
                ref match {
                    case "insite" =>
                        pluginType match {
                            case "fixed" => incrAdminCount(AdminCounts.COLUMN_SHOWUPS_FIXED_I, date)
                            case "slide" => incrAdminCount(AdminCounts.COLUMN_SHOWUPS_SLIDE_I, date)
                        }
                        pic match {
                            case "true" => incrAdminCount(AdminCounts.COLUMN_SHOWUPS_PIC_I, date)
                            case "false" => incrAdminCount(AdminCounts.COLUMN_SHOWUPS_NOPIC_I, date)
                        }
                    case "trending" =>
                        pluginType match {
                            case "fixed" => incrAdminCount(AdminCounts.COLUMN_SHOWUPS_FIXED_T, date)
                            case "slide" => incrAdminCount(AdminCounts.COLUMN_SHOWUPS_SLIDE_T, date)
                        }
                        pic match {
                            case "true" => incrAdminCount(AdminCounts.COLUMN_SHOWUPS_PIC_T, date)
                            case "false" => incrAdminCount(AdminCounts.COLUMN_SHOWUPS_NOPIC_T, date)
                        }
                    case _ => log.info("Unknown showup ref")
                }
            }

            case _ =>
                log.info("Unknown stats type")
        }
    }

    /**
     * uuid <=> path
     *
     *
     */
    private def mapUuidPath(uuid: Option[UUID], url: String) = {
        if (uuid.firstOption.isDefined) {
            val path = getPath(url)
            if (path.firstOption.isDefined)
                pathUuidMap.put(path.get, uuid.first.toString())
        }
    }

    /**
     * http://www.baidu.com/111/222/333/444?a=a&b=b
     *
     * http://www.baidu.com/111/222/
     */
    private def getPath(url: String): Option[String] = {
        if (StringUtils.isNotBlank(url)) {
            val paths = UrlUtil.getPaths(url, pathSubLimit)
            if (!paths.isEmpty()) {
                val lastPath = paths.get(paths.size() - 1)
                if (UrlUtil.isUrlHttp(url)) {
                    return Some("http://" + lastPath)
                } else {
                    return Some("https://" + lastPath)
                }
            }
        }
        None
    }
}
