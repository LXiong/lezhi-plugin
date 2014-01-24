package com.buzzinate.lezhi.model

import java.sql.Timestamp
import org.scalaquery.ql.Query
import org.scalaquery.ql.TypeMapper.StringTypeMapper
import org.scalaquery.ql.extended.{ ExtendedTable => Table }
import org.scalaquery.ql.extended.MySQLDriver.Implicit._
import org.scalaquery.session.Database.threadLocalSession
import com.buzzinate.lezhi.db.MysqlClient.{ masterDatabase, slaveDatabase }
import com.buzzinate.lezhi.util.Logging
import com.buzzinate.common.util.string.UrlUtil

/**
 * @author jeffrey created on 2012-9-17 下午5:22:36
 *
 */
object PageStatistic extends Logging {
    case class Page(
        var keyType: String = "",
        var url: String,
        var title: String = "",
        var showup: Int = 0,
        var inClick: Int = 0,
        var outClick: Int = 0,
        var clickToShowup: Double = 0.00,
        val gmtCreated: Timestamp = new Timestamp(System.currentTimeMillis)) {

        override def toString(): String = {
            "[keyType:" + keyType + ",url:" + url + ",title:" + title + ",showup:" + showup + ",inClick:" + inClick + ",outClick:" + outClick + ",clickToShowup:" + clickToShowup + "]"
        }
    }

    val pageStatistic = new Table[Page]("page_statistic") {
        def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
        def keyType = column[String]("key_type")
        def url = column[String]("url")
        def title = column[String]("title")
        def showup = column[Int]("showup")
        def inClick = column[Int]("in_click")
        def outClick = column[Int]("out_click")
        def clickToShowup = column[Double]("click_to_showup")
        def gmtCreated = column[Timestamp]("gmt_created")
        def * = keyType ~ url ~ title ~ showup ~ inClick ~ outClick ~ clickToShowup ~ gmtCreated <> (Page, Page.unapply _)
        def key = index("key_type", keyType)
    }

    /**
     * get statistics by the key in the format:yyyy-MM-dd_xxxx
     */
    def getStatistics(key: String): Seq[com.buzzinate.lezhi.bean.Page] = {
        val orderBy = key.substring(11) match {
            case "showup" => pageStatistic.showup
            case "inClick" => pageStatistic.inClick
            case "outClick" => pageStatistic.outClick
            case "clickToShowup" => pageStatistic.clickToShowup
            case _ => pageStatistic.clickToShowup
        }
        try {
            slaveDatabase withSession {
                val query = Query(pageStatistic).where(uu => uu.keyType === key).orderBy(orderBy desc)
                query.list.map { page =>
                    new com.buzzinate.lezhi.bean.Page(page.url, page.title, page.showup, page.inClick, page.outClick, page.clickToShowup, UrlUtil.getDomainName(page.url))
                }
            }
        } catch {
            case ex: Exception =>
                log.error(ex, "Failed to get page_statistic by [key:" + key + "]");
                Nil
        }
    }

    def addStatistic(pages: Seq[Page]) = {
        try {
            masterDatabase withSession {
                pageStatistic.insertAll(pages: _*)
            }
            true
        } catch {
            case ex: Exception =>
                log.error(ex, "Failed to batch insert page_statistic[pages:" + pages + "]");
        }
    }

    def delStatistic(keyType: String) = {
        try {
            masterDatabase withSession {
                pageStatistic.where(uu => uu.keyType === keyType).delete
            }
            true
        } catch {
            case ex: Exception =>
                log.error(ex, "Failed to delete all the page statistic records from:[keyType: " + keyType + "]");
                false
        }
    }
}