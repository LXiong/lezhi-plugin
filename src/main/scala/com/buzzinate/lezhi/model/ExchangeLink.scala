package com.buzzinate.lezhi.model

import java.sql.Timestamp

import org.apache.commons.lang.StringUtils
import org.scalaquery.ql.Query
import org.scalaquery.ql.TypeMapper.StringTypeMapper
import org.scalaquery.ql.extended.{ ExtendedTable => Table }
import org.scalaquery.ql.extended.MySQLDriver.Implicit._
import org.scalaquery.session.Database.threadLocalSession

import com.buzzinate.lezhi.db.MysqlClient.{ masterDatabase, slaveDatabase }
import com.buzzinate.lezhi.util.Logging

/**
 * @author jeffrey.ji <jeffrey.ji@buzzinate.com>
 * May 7, 2013 12:07:37 PM
 *
 */
object ExchangeLink extends Logging {
    case class ExchangeLink(var url: String = "",
        var order: Int = 0,
        var title: String = "",
        val gmtCreated: Timestamp = new Timestamp(System.currentTimeMillis))

    val exchangeLink = new Table[ExchangeLink]("exchange_link") {
        def url = column[String]("url", O.PrimaryKey)
        def order = column[Int]("order")
        def title = column[String]("title")
        def gmtCreated = column[Timestamp]("gmt_created")
        def * = url ~ order ~ title ~ gmtCreated <> (ExchangeLink, ExchangeLink.unapply _)
    }

    def getLinks(): Seq[ExchangeLink] = {
        try {
            slaveDatabase withSession {
                Query(exchangeLink).orderBy(exchangeLink.order).list
            }
        } catch {
            case ex: Exception =>
                log.error(ex, "Failed to get all the exchange links");
                Nil
        }
    }

    def updateExchangeLinks(links: Seq[ExchangeLink]): Boolean = {
        if (links.size <= 0) {
            return false
        }
        try {
            masterDatabase withSession {
                exchangeLink.insertAll(links: _*)
            }
            true
        } catch {
            case ex: Exception =>
                log.error(ex, "Failed to update exchange links:[exchangeLinks: " + links.toString() + "]");
                false
        }
    }

    def delExchangeLink(url: String): Boolean = {
        if (StringUtils.isBlank(url)) {
            return false
        }
        try {
            masterDatabase withSession {
                exchangeLink.where(uu => uu.url is url).delete
            }
            true
        } catch {
            case ex: Exception =>
                log.error(ex, "Failed to delete exchange links from:[url:" + url + "]");
                false
        }
    }
}
