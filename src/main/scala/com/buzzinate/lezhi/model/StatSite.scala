package com.buzzinate.lezhi.model

import java.sql.{ Date => SqlDate }
import java.util.{ Date, UUID }
import scala.collection.JavaConverters._

import org.scalaquery.ql.Query
import org.scalaquery.ql.TypeMapper.StringTypeMapper
import org.scalaquery.ql.extended.{ ExtendedTable => Table }
import org.scalaquery.ql.extended.MySQLDriver.Implicit._
import org.scalaquery.session.Database.threadLocalSession

import com.buzzinate.lezhi.db.MysqlClient.{ masterDatabase, slaveDatabase }
import com.buzzinate.common.util.DateTimeUtil
import com.buzzinate.lezhi.util.Logging
import com.buzzinate.lezhi.bean.Paginate

/**
 * @author jeffrey.ji <jeffrey.ji@buzzinate.com>
 * Jul 9, 2013 12:33:24 PM
 *
 */
object StatSite extends Logging {
    case class StatSite(var uuid: String,
        var siteDay: SqlDate = new SqlDate(System.currentTimeMillis),
        var inClicks: Long = 0,
        var outClicks: Long = 0,
        var showups: Long = 0) {
    }

    val statSites = new Table[StatSite]("stat_site") {
        def uuid = column[String]("uuid")
        def siteDay = column[SqlDate]("site_day")
        def inClicks = column[Long]("in_clicks")
        def outClicks = column[Long]("out_clicks")
        def showups = column[Long]("showups")
        def * = uuid ~ siteDay ~ inClicks ~ outClicks ~ showups <> (StatSite, StatSite.unapply _)
        def pk = primaryKey("pk", (uuid, siteDay))
    }

    val COLUMN_IN_CLICKS = "inClicks"
    val COLUMN_OUT_CLICKS = "outClicks"
    val COLUMN_SHOWUPS = "showups"

    def incrSiteInClick(uuid: String, day: Date, count: Long = 1): Boolean = {
        incrCounter(COLUMN_IN_CLICKS, uuid, day, count)
    }
    def incrSiteOutClick(uuid: String, day: Date, count: Long = 1): Boolean = {
        incrCounter(COLUMN_OUT_CLICKS, uuid, day, count)
    }
    def incrSiteShowup(uuid: String, day: Date, count: Long = 1): Boolean = {
        incrCounter(COLUMN_SHOWUPS, uuid, day, count)
    }
    def getSiteInClick(uuid: String, day: Date): Long = {
        getCounter(COLUMN_IN_CLICKS, uuid, day)
    }
    def getSiteInClick(uuid: String, startDay: Date, endDay: Date): Seq[(Date, Long)] = {
        getCounter(COLUMN_IN_CLICKS, uuid, startDay, endDay)
    }
    def getSiteOutClick(uuid: String, day: Date): Long = {
        getCounter(COLUMN_OUT_CLICKS, uuid, day)
    }
    def getSiteOutClick(uuid: String, startDay: Date, endDay: Date): Seq[(Date, Long)] = {
        getCounter(COLUMN_OUT_CLICKS, uuid, startDay, endDay)
    }
    def getSiteShowup(uuid: String, day: Date): Long = {
        getCounter(COLUMN_SHOWUPS, uuid, day)
    }
    def getSiteShowup(uuid: String, startDay: Date, endDay: Date): Seq[(Date, Long)] = {
        getCounter(COLUMN_SHOWUPS, uuid, startDay, endDay)
    }
    def getOutClickOfSites(uuids: Seq[String], startDay: Date, endDay: Date): Seq[(String, Long)] = {
        getSumOfSites(uuids, COLUMN_OUT_CLICKS, startDay, endDay)
    }
    def getShowupOfSites(uuids: Seq[String], startDay: Date, endDay: Date): Seq[(String, Long)] = {
        getSumOfSites(uuids, COLUMN_SHOWUPS, startDay, endDay)
    }

    /**
     * get article num by uuid
     * @param uuids
     * @return
     */
    def getArticleNumOfSites(uuids: Seq[String]): Seq[(String, Long)] = {
        val siteDatas = SiteStatus.getArticleNumByUuids(uuids)
        
        siteDatas.map { siteData =>
            (siteData.uuid, siteData.urlNumber)
        }
    }

    /**
     * get statsite data by uuid list
     * @param uuids
     * @param startDay
     * @param endDay
     * @return
     */
    def getStatSiteByUuids(uuids: Seq[String], startDay: Date, endDay: Date, sortType: String): Seq[StatSite] = {
        if (uuids.isEmpty) {
            Nil
        }
        try {
            slaveDatabase withSession {
                val query = for {
                    u <- statSites
                    sortField = sortType match {
                        case "inClick" => u.inClicks.sum
                        case "outClick" => u.outClicks.sum
                        case _ => u.showups.sum
                    }
                    if ((u.uuid inSet uuids) && (u.siteDay between (new SqlDate(DateTimeUtil.getDateDay(startDay).getTime), new SqlDate(DateTimeUtil.getDateDay(endDay).getTime))))
                    _ <- Query groupBy u.uuid
                    _ <- Query orderBy(sortField desc)
                } yield u.uuid ~ u.inClicks.sum ~ u.outClicks.sum ~ u.showups.sum

                query.list.map {
                    case (uuid, inClicks, outClicks, showUps) =>
                        new StatSite(uuid, null, inClicks.getOrElse(0L), outClicks.getOrElse(0L), showUps.getOrElse(0L))
                }
            }
        } catch {
            case ex: Exception =>
                log.error(ex, "Failed to get stats of sites from " + uuids)
                Nil
        }
    }
    
    /**
     * get stat sites data group by day, sortBy field and painate
     */
    def getStatSiteByPaginate(paginate: Paginate, sortType: String, startDay: Date, endDay: Date): Seq[StatSite] = {
        try {
            slaveDatabase withSession {
                val query = for {
                    u <- statSites
                    sortField = sortType match {
                        case "inClick" => u.inClicks.sum
                        case "outClick" => u.outClicks.sum
                        case _ => u.showups.sum
                    }
                    if (u.siteDay between (new SqlDate(DateTimeUtil.getDateDay(startDay).getTime), new SqlDate(DateTimeUtil.getDateDay(endDay).getTime)))
                    _ <- Query groupBy u.uuid
                    _ <- Query orderBy(sortField desc)
                } yield (u.uuid ~ u.inClicks.sum ~ u.outClicks.sum ~ u.showups.sum)

                query.drop(paginate.startIndex).take(paginate.pageSize).list.map {
                    case (uuid, inClicks, outClicks, showUps) =>
                        StatSite(uuid, null, inClicks.getOrElse(0L), outClicks.getOrElse(0L), showUps.getOrElse(0L))
                }
            }
        } catch {
            case ex: Exception =>
                log.error(ex, "Failed to get stats of sites from paginate" + paginate)
                Nil
        }
    }

    private def getSumOfSites(uuids: Seq[String], col: String, startDay: Date, endDay: Date): Seq[(String, Long)] = {
        if (uuids.isEmpty) {
            Nil
        }
        try {
            slaveDatabase withSession {
                val query = for {
                    u <- statSites
                    if ((u.uuid inSet uuids) && (u.siteDay between (new SqlDate(DateTimeUtil.getDateDay(startDay).getTime), new SqlDate(DateTimeUtil.getDateDay(endDay).getTime))))
                    _ <- Query groupBy u.uuid
                } yield {
                    val colValue = col match {
                        case COLUMN_IN_CLICKS => u.inClicks
                        case COLUMN_OUT_CLICKS => u.outClicks
                        case COLUMN_SHOWUPS => u.showups
                    }
                    u.uuid ~ colValue.sum
                }
                query.list.map {
                    case (uuid, value) =>
                        (uuid, value.getOrElse(0L))
                }
            }
        } catch {
            case ex: Exception =>
                log.error(ex, "Failed to get stats of sites from " + uuids + " on col:" + col)
                Nil
        }
    }

    def batchSite(siteMap: java.util.HashMap[(String, UUID, String), Long]): Boolean = {
        if (siteMap.size == 0) {
            return false
        }
        try {
            siteMap.asScala.foreach {
                case ((col, uuid, date), value) => {
                    incrCounter(col, uuid.toString, DateTimeUtil.convertDate(date), value)
                }
            }
            true
        } catch {
            case ex: Exception =>
                log.error(ex, "Failed to increase[sites: " + siteMap + "]")
                false
        }
    }

    /**
     * batch get the active page
     */
    def getActiveSiteNumber(uuids: Seq[String], startDay: Date, endDay: Date): Int = {
        uuids.map { uuid =>
            val count = getCounter(COLUMN_SHOWUPS, uuid, startDay, endDay)
            uuid -> (0L /: count) { (sum, elem) => sum + elem._2 }
        }.filter(row => row._2 > 0).size
    }

    /**
     * batch get the active page
     */
    def getActiveSiteUuid(uuids: Seq[String], startDay: Date, endDay: Date): Seq[String] = {
        uuids.map { uuid =>
            val count = getCounter(COLUMN_SHOWUPS, uuid, startDay, endDay)
            uuid -> (0L /: count) { (sum, elem) => sum + elem._2 }
        }.filter(row => row._2 > 0).map(row => row._1)
    }

    private def incrCounter(col: String, uuid: String, day: Date, count: Long = 1): Boolean = {
        try {
            masterDatabase withSession {
                val query = for {
                    u <- statSites
                    if (u.uuid === uuid && u.siteDay === new SqlDate(DateTimeUtil.getDateDay(day).getTime))
                } yield col match {
                    case COLUMN_IN_CLICKS => u.inClicks
                    case COLUMN_OUT_CLICKS => u.outClicks
                    case COLUMN_SHOWUPS => u.showups
                }
                if (query.firstOption.isDefined) {
                    query.update(query.firstOption.get + count)
                } else {
                    val pc = col match {
                        case COLUMN_IN_CLICKS => StatSite(uuid, new SqlDate(day.getTime), count, 0, 0)
                        case COLUMN_OUT_CLICKS => StatSite(uuid, new SqlDate(day.getTime), 0, count, 0)
                        case COLUMN_SHOWUPS => StatSite(uuid, new SqlDate(day.getTime), 0, 0, count)
                    }
                    statSites.insert(pc)
                }
            }
            true
        } catch {
            case ex: Exception =>
                log.error(ex, "Failed to increase " + uuid + ",on [col: " + col + ",date:" + DateTimeUtil.formatDate(day) + "]")
                false
        }
    }

    private def getCounter(col: String, uuid: String, day: Date): Long = {
        try {
            slaveDatabase withSession {
                val query = for {
                    u <- statSites
                    if (u.uuid === uuid && u.siteDay === new SqlDate(DateTimeUtil.getDateDay(day).getTime))
                } yield col match {
                    case COLUMN_IN_CLICKS => u.inClicks
                    case COLUMN_OUT_CLICKS => u.outClicks
                    case COLUMN_SHOWUPS => u.showups
                }
                if (query.firstOption.isDefined)
                    query.firstOption.get
                else 0L
            }
        } catch {
            case ex: Exception =>
                log.error(ex, "Failed to get " + col + ",on [uuid: " + uuid + "]")
                0L
        }
    }

    private def getCounter(col: String, uuid: String, startDay: Date, endDay: Date): Seq[(Date, Long)] = {
        try {
            slaveDatabase withSession {
                val query = for {
                    u <- statSites
                    if (u.uuid === uuid && (u.siteDay between (new SqlDate(DateTimeUtil.getDateDay(startDay).getTime), new SqlDate(DateTimeUtil.getDateDay(endDay).getTime))))
                } yield {
                    val colValue = col match {
                        case COLUMN_IN_CLICKS => u.inClicks
                        case COLUMN_OUT_CLICKS => u.outClicks
                        case COLUMN_SHOWUPS => u.showups
                    }
                    u.siteDay ~ colValue
                }
                query.list
            }
        } catch {
            case ex: Exception =>
                log.error(ex, "Failed to get " + col + "on [uuid: " + uuid + ",startDay:" + DateTimeUtil.formatDate(startDay) + ",endDay:" + DateTimeUtil.formatDate(endDay) + "]")
                Nil
        }
    }

    def migrate(res: Seq[StatSite]) = {
        try {
            masterDatabase withSession {
                statSites.insertAll(res: _*)
            }
            true
        } catch {
            case ex: Exception =>
                log.error(ex, "Failed to insert  stat_site:[sites: " + res + "]");
                false
        }
    }

    def migrate(keyType: String, res: scala.collection.mutable.HashMap[(String, SqlDate), Long]) = {
        try {
            masterDatabase withSession {
                res.foreach {
                    case ((uuid, day), value) => {
                        val query = for {
                            u <- statSites
                            if (u.uuid === uuid && u.siteDay === day)
                        } yield keyType match {
                            case COLUMN_IN_CLICKS => u.inClicks
                            case COLUMN_OUT_CLICKS => u.outClicks
                        }
                        if (query.firstOption.isDefined) {
                            query.update(value)
                        } else {
                            val pc = keyType match {
                                case COLUMN_IN_CLICKS => StatSite(uuid, day, value, 0, 0)
                                case COLUMN_OUT_CLICKS => StatSite(uuid, day, 0, value, 0)
                            }
                            statSites.insert(pc)
                        }
                    }
                }
            }
            true
        } catch {
            case ex: Exception =>
                log.error(ex, "Failed to update  stat_site on:" + keyType + ",[sites: " + res + "]");
                false
        }
    }
}