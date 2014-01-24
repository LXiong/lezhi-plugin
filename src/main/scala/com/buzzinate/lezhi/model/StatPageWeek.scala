package com.buzzinate.lezhi.model

import java.sql.{ Date => SqlDate }
import java.util.{ Date, UUID }
import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer
import org.scalaquery.ql.Query
import org.scalaquery.ql.TypeMapper.StringTypeMapper
import org.scalaquery.ql.extended.{ ExtendedTable => Table }
import org.scalaquery.ql.extended.MySQLDriver.Implicit._
import org.scalaquery.session.Database.threadLocalSession
import com.buzzinate.lezhi.db.MysqlClient.{ masterDatabase, slaveDatabase }
import com.buzzinate.common.util.DateTimeUtil
import com.buzzinate.lezhi.crontab.PageStatisticCron
import com.buzzinate.lezhi.util.{ Logging, Util }
import org.scalaquery.session.Session

/**
 * @author jeffrey.ji <jeffrey.ji@buzzinate.com>
 * Jul 11, 2013 2:08:31 PM
 *
 */
class StatPageWeek(week: String) extends Logging {

    val statPageWeek = new Table[com.buzzinate.lezhi.model.StatPage.StatPage]("stat_page_" + week) {
        def id = column[Int]("id")
        def urlHash = column[Array[Byte]]("url_hash")
        def uuid = column[Array[Byte]]("uuid")
        def pageDay = column[SqlDate]("page_day")
        def inClicks = column[Int]("in_clicks")
        def outClicks = column[Int]("out_clicks")
        def showups = column[Int]("showups")
        def * = id ~ urlHash ~ uuid ~ pageDay ~ inClicks ~ outClicks ~ showups <> (com.buzzinate.lezhi.model.StatPage.StatPage, com.buzzinate.lezhi.model.StatPage.StatPage.unapply _)
        def pk = primaryKey("pk", id)
    }

    import org.scalaquery.meta.{ MTable }

    def checkAndCreateTable() = {
        try {
            masterDatabase withSession { session: Session =>
                val tableList = MTable.getTables.list()(session)
                val tableMap = tableList.map {
                    t => (t.name.name, t)
                }.toMap

                if (!tableMap.contains("tableName")) {
                    statPageWeek.ddl.create(session)
                }
            }
        } catch {
            case ex: Exception =>
                log.error(ex, "Failed to get ]")
        }
    }

    val COLUMN_IN_CLICKS = "inClicks"
    val COLUMN_OUT_CLICKS = "outClicks"
    val COLUMN_SHOWUPS = "showups"

    private def getCounter(col: String, url: String, startDay: Date, endDay: Date): Seq[(Date, Int)] = {
        try {
            slaveDatabase withSession {
                val query = for {
                    u <- statPageWeek
                    if (u.urlHash === Util.urlToBytes(url).bind && (u.pageDay between (new SqlDate(DateTimeUtil.getDateDay(startDay).getTime), new SqlDate(DateTimeUtil.getDateDay(endDay).getTime))))
                } yield {
                    val colValue = col match {
                        case COLUMN_IN_CLICKS => u.inClicks
                        case COLUMN_OUT_CLICKS => u.outClicks
                        case COLUMN_SHOWUPS => u.showups
                    }
                    u.pageDay ~ colValue
                }
                query.list
            }
        } catch {
            case ex: Exception =>
                log.error(ex, "Failed to get " + col + "on [url: " + url + ",startDay:" + DateTimeUtil.formatDate(startDay) + ",endDay:" + DateTimeUtil.formatDate(endDay) + "]")
                Nil
        }
    }
}