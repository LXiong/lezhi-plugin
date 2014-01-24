package com.buzzinate.lezhi.model

import java.util.Date
import java.sql.{ Date => SqlDate }
import scala.collection.JavaConverters.mapAsScalaMapConverter
import org.scalaquery.ql.extended.{ ExtendedTable => Table }
import org.scalaquery.ql.extended.MySQLDriver.Implicit._
import org.scalaquery.session.Database.threadLocalSession
import com.buzzinate.lezhi.db.MysqlClient.{ masterDatabase, slaveDatabase }
import com.buzzinate.common.util.DateTimeUtil
import com.buzzinate.lezhi.util.Logging

object AdminCounts extends Logging {
    case class AdminCounts(val adminDay: SqlDate = new SqlDate(System.currentTimeMillis),
        var views: Long = 0,
        var inClicks: Long = 0,
        var outClicks: Long = 0,
        var showups: Long = 0,
        var showupsFixedI: Long = 0,
        var showupsSlideI: Long = 0,
        var showupsPicI: Long = 0,
        var showupsNopicI: Long = 0,
        var showupsFixedT: Long = 0,
        var showupsSlideT: Long = 0,
        var showupsPicT: Long = 0,
        var showupsNoPicT: Long = 0,
        var clicksFixedI: Long = 0,
        var clicksSlideI: Long = 0,
        var clicksPicI: Long = 0,
        var clicksNopicI: Long = 0,
        var clicksFixedT: Long = 0,
        var clicksSlideT: Long = 0,
        var clicksPicT: Long = 0,
        var clicksNoPicT: Long = 0)

    val adminCounts = new Table[AdminCounts]("admin_counts") {
        def adminDay = column[SqlDate]("admin_day", O.PrimaryKey)
        def views = column[Long](COLUMN_VIEWS)
        def inClicks = column[Long]("in_clicks")
        def outClicks = column[Long]("out_clicks")
        def showups = column[Long](COLUMN_SHOWUPS)
        def showupsFixedI = column[Long]("showups_fixed_i")
        def showupsSlideI = column[Long]("showups_slide_i")
        def showupsPicI = column[Long]("showups_pic_i")
        def showupsNopicI = column[Long]("showups_nopic_i")
        def showupsFixedT = column[Long]("showups_fixed_t")
        def showupsSlideT = column[Long]("showups_slide_t")
        def showupsPicT = column[Long]("showups_pic_t")
        def showupsNoPicT = column[Long]("showups_nopic_t")
        def clicksFixedI = column[Long]("clicks_fixed_i")
        def clicksSlideI = column[Long]("clicks_slide_i")
        def clicksPicI = column[Long]("clicks_pic_i")
        def clicksNopicI = column[Long]("clicks_nopic_i")
        def clicksFixedT = column[Long]("clicks_fixed_t")
        def clicksSlideT = column[Long]("clicks_slide_t")
        def clicksPicT = column[Long]("clicks_pic_t")
        def clicksNoPicT = column[Long]("clicks_nopic_t")

        def * = adminDay ~ views ~ inClicks ~ outClicks ~ showups ~ showupsFixedI ~ showupsSlideI ~ showupsPicI ~ showupsNopicI ~ showupsFixedT ~ showupsSlideT ~ showupsPicT ~ showupsNoPicT ~ clicksFixedI ~ clicksSlideI ~ clicksPicI ~ clicksNopicI ~ clicksFixedT ~ clicksSlideT ~ clicksPicT ~ clicksNoPicT <> (AdminCounts, AdminCounts.unapply _)
    }

    val COLUMN_VIEWS = "views"
    val COLUMN_IN_CLICKS = "inClicks"
    val COLUMN_OUT_CLICKS = "outClicks"
    val COLUMN_SHOWUPS = "showups"
    val COLUMN_SHOWUPS_FIXED_I = "showupsFixedI"
    val COLUMN_SHOWUPS_SLIDE_I = "showupsSlideI"
    val COLUMN_SHOWUPS_PIC_I = "showupsPicI"
    val COLUMN_SHOWUPS_NOPIC_I = "showupsNopicI"
    val COLUMN_SHOWUPS_FIXED_T = "showupsFixedT"
    val COLUMN_SHOWUPS_SLIDE_T = "showupsSlideT"
    val COLUMN_SHOWUPS_PIC_T = "showupsPicT"
    val COLUMN_SHOWUPS_NOPIC_T = "showupsNoPicT"
    val COLUMN_CLICKS_FIXED_I = "clicksFixedI"
    val COLUMN_CLICKS_SLIDE_I = "clicksSlideI"
    val COLUMN_CLICKS_PIC_I = "clicksPicI"
    val COLUMN_CLICKS_NOPIC_I = "clicksNopicI"
    val COLUMN_CLICKS_FIXED_T = "clicksFixedT"
    val COLUMN_CLICKS_SLIDE_T = "clicksSlideT"
    val COLUMN_CLICKS_PIC_T = "clicksPicT"
    val COLUMN_CLICKS_NOPIC_T = "clicksNoPicT"

    def incrFromMap(adminMap: java.util.Map[(String, String), Long]) {
        if (adminMap.size == 0) {
            return
        }
        try {
            adminMap.asScala.foreach {
                case ((keyType, date), value) =>
                    val day = DateTimeUtil.convertDate(date)
                    incrCounter(day, keyType, value)
            }
        } catch {
            case ex: Exception =>
                log.error(ex, "Failed to update AdminCounts, [map: " + adminMap + "]");
        }
    }

    def getViews(day: Date): Long = {
        getCounter(day, COLUMN_VIEWS)
    }

    def get(day: Date): Option[AdminCounts] = {
        val d = DateTimeUtil.getDateDay(day)
        try {
            slaveDatabase withSession {
                val query = for {
                    u <- adminCounts if (u.adminDay === new SqlDate(d.getTime))
                } yield u
                if (query.firstOption.isDefined) {
                    Some(query.first)
                } else
                    None
            }
        } catch {
            case ex: Exception =>
                log.error(ex, "Failed to get AdminCounts, [day: " + day + "]");
                None
        }
    }

    private def incrCounter(day: Date, col: String, count: Long = 1): Boolean = {
        val d = DateTimeUtil.getDateDay(day)
        try {
            masterDatabase withSession {
                val query = for {
                    u <- adminCounts
                    if u.adminDay === new SqlDate(d.getTime)
                } yield col match {
                    case COLUMN_VIEWS => u.views
                    case COLUMN_IN_CLICKS => u.inClicks
                    case COLUMN_OUT_CLICKS => u.outClicks
                    case COLUMN_SHOWUPS => u.showups
                    case COLUMN_SHOWUPS_FIXED_I => u.showupsFixedI
                    case COLUMN_SHOWUPS_SLIDE_I => u.showupsSlideI
                    case COLUMN_SHOWUPS_PIC_I => u.showupsPicI
                    case COLUMN_SHOWUPS_NOPIC_I => u.showupsNopicI
                    case COLUMN_SHOWUPS_FIXED_T => u.showupsFixedT
                    case COLUMN_SHOWUPS_SLIDE_T => u.showupsSlideT
                    case COLUMN_SHOWUPS_PIC_T => u.showupsPicT
                    case COLUMN_SHOWUPS_NOPIC_T => u.showupsNoPicT
                    case COLUMN_CLICKS_FIXED_I => u.clicksFixedI
                    case COLUMN_CLICKS_SLIDE_I => u.clicksSlideI
                    case COLUMN_CLICKS_PIC_I => u.clicksPicI
                    case COLUMN_CLICKS_NOPIC_I => u.clicksNopicI
                    case COLUMN_CLICKS_FIXED_T => u.clicksFixedT
                    case COLUMN_CLICKS_SLIDE_T => u.clicksSlideT
                    case COLUMN_CLICKS_PIC_T => u.clicksPicT
                    case COLUMN_CLICKS_NOPIC_T => u.clicksNoPicT
                }

                if (query.firstOption.isDefined) {
                    query.update(query.firstOption.get + count)
                } else {
                    val pc = col match {
                        case COLUMN_VIEWS => AdminCounts(new SqlDate(day.getTime), count)
                        case COLUMN_IN_CLICKS => AdminCounts(new SqlDate(day.getTime), 0, count)
                        case COLUMN_OUT_CLICKS => AdminCounts(new SqlDate(day.getTime), 0, 0, count)
                        case COLUMN_SHOWUPS => AdminCounts(new SqlDate(day.getTime), 0, 0, 0, count)
                        case COLUMN_SHOWUPS_FIXED_I => AdminCounts(new SqlDate(day.getTime), 0, 0, 0, 0, count)
                        case COLUMN_SHOWUPS_SLIDE_I => AdminCounts(new SqlDate(day.getTime), 0, 0, 0, 0, 0, count)
                        case COLUMN_SHOWUPS_PIC_I => AdminCounts(new SqlDate(day.getTime), 0, 0, 0, 0, 0, 0, count)
                        case COLUMN_SHOWUPS_NOPIC_I => AdminCounts(new SqlDate(day.getTime), 0, 0, 0, 0, 0, 0, 0, count)
                        case COLUMN_SHOWUPS_FIXED_T => AdminCounts(new SqlDate(day.getTime), 0, 0, 0, 0, 0, 0, 0, 0, count)
                        case COLUMN_SHOWUPS_SLIDE_T => AdminCounts(new SqlDate(day.getTime), 0, 0, 0, 0, 0, 0, 0, 0, 0, count)
                        case COLUMN_SHOWUPS_PIC_T => AdminCounts(new SqlDate(day.getTime), 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, count)
                        case COLUMN_SHOWUPS_NOPIC_T => AdminCounts(new SqlDate(day.getTime), 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, count)
                        case COLUMN_CLICKS_FIXED_I => AdminCounts(new SqlDate(day.getTime), 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, count)
                        case COLUMN_CLICKS_SLIDE_I => AdminCounts(new SqlDate(day.getTime), 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, count)
                        case COLUMN_CLICKS_PIC_I => AdminCounts(new SqlDate(day.getTime), 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, count)
                        case COLUMN_CLICKS_NOPIC_I => AdminCounts(new SqlDate(day.getTime), 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, count)
                        case COLUMN_CLICKS_FIXED_T => AdminCounts(new SqlDate(day.getTime), 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, count)
                        case COLUMN_CLICKS_SLIDE_T => AdminCounts(new SqlDate(day.getTime), 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, count)
                        case COLUMN_CLICKS_PIC_T => AdminCounts(new SqlDate(day.getTime), 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, count)
                        case COLUMN_CLICKS_NOPIC_T => AdminCounts(new SqlDate(day.getTime), 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, count)
                    }
                    adminCounts.insert(pc)
                }
            }
            true
        } catch {
            case ex: Exception =>
                log.error(ex, "Failed to update AdminCounts, [day: " + day + "]")
                false
        }
    }

    private def getCounter(day: Date, col: String): Long = {
        try {
            slaveDatabase withSession {
                val query = for {
                    u <- adminCounts if (u.adminDay === new SqlDate(day.getTime))
                } yield col match {
                    case COLUMN_VIEWS => u.views
                    case COLUMN_IN_CLICKS => u.inClicks
                    case COLUMN_OUT_CLICKS => u.outClicks
                    case COLUMN_SHOWUPS => u.showups
                    case COLUMN_SHOWUPS_FIXED_I => u.showupsFixedI
                    case COLUMN_SHOWUPS_SLIDE_I => u.showupsSlideI
                    case COLUMN_SHOWUPS_PIC_I => u.showupsPicI
                    case COLUMN_SHOWUPS_NOPIC_I => u.showupsNopicI
                    case COLUMN_SHOWUPS_FIXED_T => u.showupsFixedT
                    case COLUMN_SHOWUPS_SLIDE_T => u.showupsSlideT
                    case COLUMN_SHOWUPS_PIC_T => u.showupsPicT
                    case COLUMN_SHOWUPS_NOPIC_T => u.showupsNoPicT
                    case COLUMN_CLICKS_FIXED_I => u.clicksFixedI
                    case COLUMN_CLICKS_SLIDE_I => u.clicksSlideI
                    case COLUMN_CLICKS_PIC_I => u.clicksPicI
                    case COLUMN_CLICKS_NOPIC_I => u.clicksNopicI
                    case COLUMN_CLICKS_FIXED_T => u.clicksFixedT
                    case COLUMN_CLICKS_SLIDE_T => u.clicksSlideT
                    case COLUMN_CLICKS_PIC_T => u.clicksPicT
                    case COLUMN_CLICKS_NOPIC_T => u.clicksNoPicT
                }
                if (query.firstOption.isDefined)
                    query.firstOption.get
                else 0L
            }
        } catch {
            case ex: Exception =>
                log.error(ex, "Failed to get AdminCounts, [day: " + day + "]")
                0L
        }
    }

    def migrate(res: Seq[AdminCounts]) = {
        val today = new SqlDate(DateTimeUtil.getDateDay(new Date).getTime)
        val beforeTodayAdmins = res.filter(admin => admin.adminDay != today)
        val todayAdmin = res.filter(admin => admin.adminDay == today)
        log.info("todayAdmin:" + todayAdmin)
        println(todayAdmin)
        try {
            masterDatabase withSession {
                adminCounts.insertAll(beforeTodayAdmins: _*)
            }
            true
        } catch {
            case ex: Exception =>
                log.error(ex, "Failed to batch insert AdminCounts, [res: " + res + "]")
                false
        }
    }
}
