package com.buzzinate.lezhi.model

import java.sql.{ Date => SqlDate }
import scala.collection.JavaConverters.mapAsScalaMapConverter
import org.scalaquery.ql.extended.{ ExtendedTable => Table }
import org.scalaquery.ql.extended.MySQLDriver.Implicit._
import org.scalaquery.session.Database.threadLocalSession
import com.buzzinate.lezhi.db.MysqlClient.{ masterDatabase, slaveDatabase }
import com.buzzinate.common.util.DateTimeUtil
import com.buzzinate.lezhi.util.Logging
import java.util.Date
/**
 * @author jeffrey.ji <jeffrey.ji@buzzinate.com>
 * Jul 24, 2013 4:30:10 PM
 *
 */
object LezhiStats extends Logging {
    case class LezhiStats(val statDay: SqlDate = new SqlDate(System.currentTimeMillis),
        var uv: Long = 0,
        var uniqueIp: Long = 0)

    val lezhiStats = new Table[LezhiStats]("lezhi_stats") {
        def statDay = column[SqlDate]("stat_day", O.PrimaryKey)
        def uv = column[Long]("uv")
        def uniqueIp = column[Long]("unique_ip")

        def * = statDay ~ uv ~ uniqueIp <> (LezhiStats, LezhiStats.unapply _)
    }

    val COLUMN_UV = "uv"
    val COLUMN_UNIQUE_IP = "uniqueIp"

    def getStats(startDate: Date, endDate: Date): List[LezhiStats] = {
        try {
            slaveDatabase withSession {
                val query = for {
                    u <- lezhiStats if (u.statDay between (new SqlDate(startDate.getTime), new SqlDate(endDate.getTime)))
                } yield u
                query.list
            }
        } catch {
            case ex: Exception =>
                log.error(ex, "Failed to get lezhiStats, [startDate: " + DateTimeUtil.formatDate(startDate) + "endDate: " + DateTimeUtil.formatDate(endDate) + "]")
                Nil
        }
    }

    def update(day: Date, col: String, count: Long = 1): Boolean = {
        try {
            masterDatabase withSession {
                val query = for {
                    u <- lezhiStats
                    if u.statDay === new SqlDate(day.getTime)
                } yield col match {
                    case COLUMN_UV => u.uv
                    case COLUMN_UNIQUE_IP => u.uniqueIp
                }
                if (query.firstOption.isDefined) {
                    query.update(query.firstOption.get + count)
                } else {
                    val pc = col match {
                        case COLUMN_UV => LezhiStats(new SqlDate(day.getTime), count)
                        case COLUMN_UNIQUE_IP => LezhiStats(new SqlDate(day.getTime), 0L, count)
                    }
                    lezhiStats.insert(pc)
                }
            }
            true
        } catch {
            case ex: Exception =>
                log.error(ex, "Failed to update lezhiStats, [day: " + day + "]")
                false
        }
    }

    def delete(day: Date): Boolean = {
        try {
            masterDatabase withSession {
                lezhiStats.where(uu => uu.statDay === new SqlDate(day.getTime)).delete
            }
            true
        } catch {
            case ex: Exception =>
                log.error(ex, "Failed to delete lezhiStats, [day: " + DateTimeUtil.formatDate(day) + "]")
                false
        }
    }
}