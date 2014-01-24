package com.buzzinate.lezhi.crontab

import java.util.{ Calendar, Date }

import scala.annotation.serializable
import scala.collection.mutable.ListBuffer

import com.buzzinate.common.util.DateTimeUtil
import com.buzzinate.lezhi.model.{ PageInfo, PageStatistic }
import com.buzzinate.lezhi.model.{ SiteConfig, SitePage, StatPage }
import com.buzzinate.lezhi.model.PageStatistic.Page
import com.buzzinate.lezhi.util.Logging

/**
 * @author jeffrey created on 2012-9-18 上午10:48:21
 *
 */
object PageStatisticCron extends Logging {

    val SIZE = 500
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_MONTH, -1)
    val prefix = DateTimeUtil.formatDate(calendar.getTime)
    val SORT_KEY_BY_OUT_CLICK = prefix + "_outClick"
    val SORT_KEY_BY_IN_CLICK = prefix + "_inClick"
    val SORT_KEY_BY_SHOWUP = prefix + "_showup"
    val SORT_KEY_BY_CLICKTOSHOWUP = prefix + "_clickToShowup"

    def addStatistics(keyType: String, inclickMap: Map[String, String], outclickMap: Map[String, String], showupMap: Map[String, String]) = {
        val pages = new ListBuffer[Page]()
        inclickMap.foreach { item =>
            val (urlUuid, value) = item
            val pageInfo = PageInfo.getByUrlUuid(urlUuid)
            val outClick = outclickMap.get(urlUuid).getOrElse("0")
            val showup = showupMap.get(urlUuid).getOrElse("0")
            val clickToShowup = if (showupMap.get(urlUuid).isDefined && showupMap.get(urlUuid).get != "0") {
                ((outclickMap.get(urlUuid).getOrElse("0").toDouble / showupMap.get(urlUuid).get.toLong) * 100).toString()
            } else {
                "0"
            }
            if (pageInfo.isDefined)
                pages += Page(keyType, pageInfo.get.url, pageInfo.get.title, showup.toInt, value.toInt, outClick.toInt, clickToShowup.toDouble)
        }
        try {
            PageStatistic.addStatistic(pages)
        } catch {
            case ex: Exception =>
                log.error(ex, "Failed to batch insert page_statistic by [pages:" + pages + "]");
        }

    }

    def addTopInclicks(date: Date, uuids: Seq[String]) = {
        val inclicks = StatPage.getTopInclicks(date, SIZE, uuids)
        val urlUuids = inclicks.map(inclick => inclick._1)
        val inclickMap = inclicks.map(inclick => (inclick._1, inclick._2)).toMap
        val outclickMap = StatPage.getOutClickOfPagesByUrlUuids(urlUuids, date, date).map(outclick => (outclick._1, outclick._2.toString)).toMap
        val showupsMap = StatPage.getShowupOfPagesByUrlUuids(urlUuids, date, date).map(showup => (showup._1, showup._2.toString)).toMap
        addStatistics(SORT_KEY_BY_IN_CLICK, inclickMap, outclickMap, showupsMap)
    }

    def addTopOutclicks(date: Date, uuids: Seq[String]) = {
        val outclicks = StatPage.getTopOutclicks(date, SIZE, uuids)
        val urlUuids = outclicks.map(inclick => inclick._1)
        val outclickMap = outclicks.map(outclick => (outclick._1, outclick._2)).toMap
        val inclickMap = StatPage.getInClickOfPagesByUrlUuids(urlUuids, date, date).map(inclick => (inclick._1, inclick._2.toString)).toMap
        val showupsMap = StatPage.getShowupOfPagesByUrlUuids(urlUuids, date, date).map(showup => (showup._1, showup._2.toString)).toMap
        addStatistics(SORT_KEY_BY_OUT_CLICK, inclickMap, outclickMap, showupsMap)
    }

    def addTopShowups(date: Date, uuids: Seq[String]) = {
        val showups = StatPage.getTopShowups(date, SIZE, uuids)
        val urlUuids = showups.map(showup => showup._1)
        val showupsMap = showups.map(showup => (showup._1, showup._2)).toMap
        val inclickMap = StatPage.getInClickOfPagesByUrlUuids(urlUuids, date, date).map(inclick => (inclick._1, inclick._2.toString)).toMap
        val outclickMap = StatPage.getOutClickOfPagesByUrlUuids(urlUuids, date, date).map(outclick => (outclick._1, outclick._2.toString)).toMap
        addStatistics(SORT_KEY_BY_SHOWUP, inclickMap, outclickMap, showupsMap)
    }

    def addTopRates(date: Date, uuids: Seq[String]) = {
        val rates = StatPage.getTopRates(date, SIZE, uuids)
        val urlUuids = rates.map(rate => rate._1)
        val ratesMap = rates.map(rate => (rate._1, rate._2)).toMap
        val inclickMap = StatPage.getInClickOfPagesByUrlUuids(urlUuids, date, date).map(inclick => (inclick._1, inclick._2.toString)).toMap
        val outclickMap = StatPage.getOutClickOfPagesByUrlUuids(urlUuids, date, date).map(outclick => (outclick._1, outclick._2.toString)).toMap
        val showupsMap = StatPage.getShowupOfPagesByUrlUuids(urlUuids, date, date).map(showup => (showup._1, showup._2.toString)).toMap
        addStatistics(SORT_KEY_BY_CLICKTOSHOWUP, inclickMap, outclickMap, showupsMap)
    }

    def main(args: Array[String]) = {
        try {
            val uuids = SiteConfig.getAllUuids
            log.info("begin to sync data at the time: " + System.nanoTime())
            addTopInclicks(calendar.getTime, uuids)
            log.info("end syncing top inclick data at the time: " + System.nanoTime())

            addTopOutclicks(calendar.getTime, uuids)
            log.info("end syncing top outclick data at the time: " + System.nanoTime())

            addTopShowups(calendar.getTime, uuids)
            log.info("end syncing top showup data at the time: " + System.nanoTime())

            addTopRates(calendar.getTime, uuids)
            log.info("end syncing top rate data at the time: " + System.nanoTime())

            SitePage.delete
            log.info("end remove the url which does exist longer than a week")

            log.info("end syncing data at the time: " + System.nanoTime())
        } catch {
            case ex: Exception =>
                log.error(ex, "Failed to add yesterday's top ranking data");
        }
    }
}