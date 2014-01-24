package com.buzzinate.lezhi.model

import java.sql.{ Date => SqlDate }
import java.util.Date
import java.util.UUID

import scala.collection.JavaConverters.mapAsScalaMapConverter
import scala.collection.mutable.ListBuffer

import org.scalaquery.ql.Query
import org.scalaquery.ql.extended.{ ExtendedTable => Table }
import org.scalaquery.ql.extended.MySQLDriver.Implicit._
import org.scalaquery.session.Database.threadLocalSession
import org.scalaquery.ql.Ordering.Desc

import com.buzzinate.common.util.DateTimeUtil
import com.buzzinate.lezhi.crontab.PageStatisticCron
import com.buzzinate.lezhi.db.MysqlClient.masterDatabase
import com.buzzinate.lezhi.db.MysqlClient.slaveDatabase
import com.buzzinate.lezhi.util.Logging
import com.buzzinate.lezhi.util.Util
import com.buzzinate.lezhi.bean.Paginate

/**
 * @author jeffrey.ji <jeffrey.ji@buzzinate.com>
 *         Jul 9, 2013 12:33:15 PM
 *
 */
object StatPage extends Logging {

    case class StatPage(var id: Int,
        val urlHash: Array[Byte],
        var uuid: Array[Byte] = new Array[Byte](0),
        var pageDay: SqlDate = new SqlDate(System.currentTimeMillis),
        var inClicks: Int = 0,
        var outClicks: Int = 0,
        var showUps: Int = 0) {
    }

    val statPages = new Table[StatPage]("stat_page") {
        def id = column[Int]("id")

        def urlHash = column[Array[Byte]]("url_hash")

        def uuid = column[Array[Byte]]("uuid")

        def pageDay = column[SqlDate]("page_day")

        def inClicks = column[Int]("in_clicks")

        def outClicks = column[Int]("out_clicks")

        def showUps = column[Int]("showups")

        def * = id ~ urlHash ~ uuid ~ pageDay ~ inClicks ~ outClicks ~ showUps <> (StatPage, StatPage.unapply _)

        def pk = primaryKey("pk", (id))
    }

    val COLUMN_IN_CLICKS = "inClicks"
    val COLUMN_OUT_CLICKS = "outClicks"
    val COLUMN_SHOWUPS = "showups"

    def getInClickOfPagesByUrlUuids(urlUuids: Seq[String], startDay: Date, endDay: Date): Seq[(String, Int)] = {
        val urlHashs = urlUuids.map {
            urlUuid =>
                Util.uuidToByte(urlUuid)
        }
        getSumOfPagesByUrlUuids(COLUMN_IN_CLICKS, urlHashs, startDay, endDay)
    }

    def getOutClickOfPagesByUrlUuids(urlUuids: Seq[String], startDay: Date, endDay: Date): Seq[(String, Int)] = {
        val urlHashs = urlUuids.map {
            urlUuid =>
                Util.uuidToByte(urlUuid)
        }
        getSumOfPagesByUrlUuids(COLUMN_OUT_CLICKS, urlHashs, startDay, endDay)
    }

    def getShowupOfPagesByUrlUuids(urlUuids: Seq[String], startDay: Date, endDay: Date): Seq[(String, Int)] = {
        val urlHashs = urlUuids.map {
            urlUuid =>
                Util.uuidToByte(urlUuid)
        }
        getSumOfPagesByUrlUuids(COLUMN_SHOWUPS, urlHashs, startDay, endDay)
    }

    private def getSumOfPagesByUrlUuids(col: String, urlHashs: Seq[Array[Byte]], startDay: Date, endDay: Date): Seq[(String, Int)] = {
        if (urlHashs.isEmpty) {
            return Nil
        }
        try {
            slaveDatabase withSession {
                val query = for {
                    u <- statPages
                    if ((u.urlHash inSetBind urlHashs) && (u.pageDay between (new SqlDate(DateTimeUtil.getDateDay(startDay).getTime), new SqlDate(DateTimeUtil.getDateDay(endDay).getTime))))
                    _ <- Query groupBy u.urlHash
                } yield {
                    val colValue = col match {
                        case COLUMN_IN_CLICKS => u.inClicks
                        case COLUMN_OUT_CLICKS => u.outClicks
                        case COLUMN_SHOWUPS => u.showUps
                    }
                    u.urlHash ~ colValue.sum
                }
                query.list.map {
                    case (urlHash, value) =>
                        (Util.byteToUuid(urlHash), value.getOrElse(0))
                }
            }
        } catch {
            case ex: Exception =>
                log.error(ex, "Failed to get " + col + "on [urlHashs: " + urlHashs.size + ",startDay:" + DateTimeUtil.formatDate(startDay) + ",endDay:" + DateTimeUtil.formatDate(endDay) + "]")
                Nil
        }
    }

    def batchPage(pageMap: java.util.HashMap[(String, String), StatPage]): Boolean = {
        if (pageMap.size == 0) {
            return false
        }
        try {
            masterDatabase withSession {
                pageMap.asScala.foreach {
                    case ((url, date), value) => {
                        val urlHash = Util.urlToBytes(url)
                        val day = new SqlDate(DateTimeUtil.getDateDay(DateTimeUtil.convertDate(date)).getTime)
                        val query = for {
                            u <- statPages
                            if (u.urlHash === urlHash.bind && u.pageDay === day)
                        } yield u

                        if (query.firstOption.isDefined) {
                            val statPage = query.firstOption.get

                            val queryUpdata= for {
                                u <- statPages if u.id === statPage.id
                            } yield u.inClicks ~ u.outClicks ~ u.showUps
                            queryUpdata.update(statPage.inClicks, statPage.outClicks, statPage.showUps)
                        } else {
                            statPages.insert(value)
                        }
                        log.info("update to mysql by the url_uuid:" + Util.byteToUuid(urlHash) + ",value:" + value)
                    }
                }
            }
        } catch {
            case ex: Exception =>
                log.error(ex, "Failed to increase[pages: " + pageMap + "]")
        }
        true
    }

    private def getTops(col: String, date: Date, size: Int, keyType: String, uuids: Seq[String]): Seq[(String, String)] = {
        val res = new ListBuffer[(String, String)]
        try {
            uuids.foreach {
                uuid =>
                    val urls = SitePage.get(UUID.fromString(uuid))
                    if (urls.size != 0) {
                        getBatchCounter(col, urls, date).foreach {
                            case (urlUuid, value) =>
                                if (keyType != PageStatisticCron.SORT_KEY_BY_CLICKTOSHOWUP)
                                    res += ((urlUuid, value.toString))
                                if (keyType == PageStatisticCron.SORT_KEY_BY_CLICKTOSHOWUP)
                                    res += ((urlUuid, (if (getUrlUuidCounter(COLUMN_SHOWUPS, urlUuid, date) == 0) "0"
                                    else value.toDouble / getUrlUuidCounter(COLUMN_SHOWUPS, urlUuid, date)).toString()))
                        }
                    }
            }
            res.sortWith((t1, t2) => t1._2.toDouble.compareTo(t2._2.toDouble) > 0).slice(0, size)
        } catch {
            case ex: Exception =>
                log.error(ex, "Failed to get top:" + keyType);
                Nil
        }
    }

    private def getBatchCounter(col: String, urls: Seq[String], day: Date): Seq[(String, Int)] = {
        if (urls.isEmpty) {
            return Nil
        }
        val urlHashs = urls.map {
            url =>
                Util.urlToBytes(url)
        }
        try {
            slaveDatabase withSession {
                val query = for {
                    u <- statPages
                    if ((u.urlHash inSetBind urlHashs) && (u.pageDay === new SqlDate(DateTimeUtil.getDateDay(day).getTime)))
                } yield {
                    val colValue = col match {
                        case COLUMN_IN_CLICKS => u.inClicks
                        case COLUMN_OUT_CLICKS => u.outClicks
                        case COLUMN_SHOWUPS => u.showUps
                    }
                    u.urlHash ~ colValue
                }
                query.list.map {
                    case (urlHash, value) =>
                        (Util.byteToUuid(urlHash), value)
                }
            }
        } catch {
            case ex: Exception =>
                log.error(ex, "Failed to get " + col + "on [urls: " + urls + ",day:" + DateTimeUtil.formatDate(day) + "]")
                Nil
        }
    }

    def getTopInclicks(date: Date, size: Int, uuids: Seq[String]): Seq[(String, String)] = {
        getTops(COLUMN_IN_CLICKS, date, size, PageStatisticCron.SORT_KEY_BY_IN_CLICK, uuids)
    }

    def getTopOutclicks(date: Date, size: Int, uuids: Seq[String]): Seq[(String, String)] = {
        getTops(COLUMN_OUT_CLICKS, date, size, PageStatisticCron.SORT_KEY_BY_OUT_CLICK, uuids)
    }

    def getTopShowups(date: Date, size: Int, uuids: Seq[String]): Seq[(String, String)] = {
        getTops(COLUMN_SHOWUPS, date, size, PageStatisticCron.SORT_KEY_BY_SHOWUP, uuids)
    }

    def getTopRates(date: Date, size: Int, uuids: Seq[String]): Seq[(String, String)] = {
        getTops(COLUMN_SHOWUPS, date, size, PageStatisticCron.SORT_KEY_BY_CLICKTOSHOWUP, uuids)
    }

    private def getUrlUuidCounter(col: String, urlUuid: String, day: Date): Int = {
        try {
            slaveDatabase withSession {
                val query = for {
                    u <- statPages
                    if (u.urlHash === Util.uuidToByte(urlUuid).bind && u.pageDay === new SqlDate(DateTimeUtil.getDateDay(day).getTime))
                } yield col match {
                    case COLUMN_IN_CLICKS => u.inClicks
                    case COLUMN_OUT_CLICKS => u.outClicks
                    case COLUMN_SHOWUPS => u.showUps
                }
                if (query.firstOption.isDefined)
                    query.firstOption.get
                else 0
            }
        } catch {
            case ex: Exception =>
                log.error(ex, "Failed to get " + col + "on [url_uuid: " + urlUuid + ",day:" + DateTimeUtil.formatDate(day) + "]")
                0
        }
    }

    def getStatPages(url: String, startDay: Date, endDay: Date): Seq[StatPage] = {
        try {
            slaveDatabase withSession {
                val query = for {
                    u <- statPages
                    if (u.urlHash === Util.urlToBytes(url).bind && (u.pageDay between (new SqlDate(DateTimeUtil.getDateDay(startDay).getTime), new SqlDate(DateTimeUtil.getDateDay(endDay).getTime))))
                } yield u
                query.list
            }
        } catch {
            case ex: Exception =>
                log.error(ex, "Failed to get on [url: " + url + ",startDay:" + DateTimeUtil.formatDate(startDay) + ",endDay:" + DateTimeUtil.formatDate(endDay) + "]")
                Nil
        }
    }

    def getStatPagesByUuid(uuid: String, startDay: Date, endDay: Date, paginate: Paginate, sortType: String): (Int, Seq[StatPage]) = {
        try {
            slaveDatabase withSession {
                val queryCount = for {
                    u <- statPages
                    if (u.uuid === Util.uuidToByte(uuid).bind && (u.pageDay between (new SqlDate(DateTimeUtil.getDateDay(startDay).getTime), new SqlDate(DateTimeUtil.getDateDay(endDay).getTime))))
                    _ <- Query groupBy(u.urlHash)
                } yield u.urlHash


                val query = for {
                    u <- statPages
                    sortField = sortType match {
                        case "inClick" => u.inClicks
                        case "outClick" => u.outClicks
                        case "clickToShowup" => u.showUps
                        case _ => u.showUps
                    }
                    if (u.uuid === Util.uuidToByte(uuid).bind && (u.pageDay between (new SqlDate(DateTimeUtil.getDateDay(startDay).getTime), new SqlDate(DateTimeUtil.getDateDay(endDay).getTime))))
                    _ <- Query orderBy(sortField desc)
                    _ <- Query groupBy(u.urlHash)
                } yield  u.urlHash ~ u.inClicks.sum ~ u.outClicks.sum ~ u.showUps.sum

                val statPageDatas = query.drop(paginate.startIndex).take(paginate.pageSize).list.map {
                    case (urlHash, inClicks, outClicks, showUps) =>
                        StatPage(0, urlHash, null, null, inClicks.getOrElse(0), outClicks.getOrElse(0), showUps.getOrElse(0))
                }
                (Query(queryCount.count).first, statPageDatas)
            }
        } catch {
            case ex: Exception =>
                log.error(ex, "Failed to get on [uuid: " + uuid + ",startDay:" + DateTimeUtil.formatDate(startDay) + ",endDay:" + DateTimeUtil.formatDate(endDay) + "]")
                (0, Nil)
        }
    }

    def del(url: String, day: Date): Boolean = {
        try {
            masterDatabase withSession {
                statPages.where(uu => uu.urlHash === Util.urlToBytes(url).bind && uu.pageDay === new SqlDate(day.getTime)).delete
            }
            true
        } catch {
            case ex: Exception =>
                log.error(ex, "Failed to del " + url + ", [day:" + DateTimeUtil.formatDate(day) + "]")
                false
        }
    }
}