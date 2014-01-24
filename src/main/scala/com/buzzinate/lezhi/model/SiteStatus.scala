package com.buzzinate.lezhi.model

import java.sql.Timestamp

import org.scalaquery.ql.TypeMapper.StringTypeMapper
import org.scalaquery.ql.extended.{ExtendedTable => Table}
import org.scalaquery.ql.extended.MySQLDriver.Implicit._
import org.scalaquery.session.Database.threadLocalSession

import com.buzzinate.lezhi.db.MysqlClient.{masterDatabase, slaveDatabase}
import com.buzzinate.lezhi.util.Logging
import com.buzzinate.lezhi.bean.Paginate
import org.scalaquery.ql.Query

/**
 * @author jeffrey created on 2012-10-23 上午9:37:02
 *
 */
object SiteStatus extends Logging {
    //uuid online in bshare 
    val ADMIN_UUID = "d2ca89b2-3cb3-45e8-8a23-791496fbc418"

    case class SiteStatus(var uuid: String = "", var urlNumber: Long = 0L, var gmtCreated: Timestamp = new Timestamp(System.currentTimeMillis))

    val siteStatus = new Table[SiteStatus]("site_status") {
        def uuid = column[String]("uuid", O.PrimaryKey)

        def urlNumber = column[Long]("url_number")

        def gmtCreated = column[Timestamp]("gmt_created")

        def * = uuid ~ urlNumber ~ gmtCreated <>(SiteStatus, SiteStatus.unapply _)
    }

    def getUrlNumber(uuid: String): Long = {
        try {
            slaveDatabase withSession {
                val query = for {
                    u <- siteStatus if u.uuid === uuid
                } yield u.urlNumber
                if (query.firstOption.isDefined)
                    query.firstOption.get
                else 0L
            }
        } catch {
            case ex: Exception =>
                log.error(ex, "Failed to get the url number by uuid:[uuid: " + uuid + "]")
                0L
        }
    }

    /**
     * get uuid count which has article
     */
    def getUuidCount(): Int = {
        try {
            slaveDatabase withSession {
                val query = for {
                    u <- siteStatus if u.urlNumber > 0L
                } yield u.uuid.count

                query.first
            }
        } catch {
            case ex: Exception =>
                log.error(ex, "Failed to get uuid has article count")
                0
        }
    }

    def getArticleNumByPaginate(paginate: Paginate): Seq[SiteStatus] = {
        try {
            slaveDatabase withSession {
                Query(siteStatus).where(s => s.urlNumber > 0L).where(s => s.uuid =!= ADMIN_UUID).orderBy(siteStatus.urlNumber desc).drop(paginate.startIndex).take(paginate.pageSize).list
            }
        } catch {
            case ex: Exception =>
                log.error(ex, "Failed to get the url number by paginate:" + paginate)
                Nil
        }
    }

    def getArticleNumByUuids(uuids: Seq[String]): Seq[SiteStatus] = {
        try {
            slaveDatabase withSession {
                Query(siteStatus).where(s => s.uuid inSet uuids).orderBy(siteStatus.urlNumber desc).list
            }
        } catch {
            case ex: Exception =>
                log.error(ex, "Failed to get the article number by uuids:" + uuids)
                Nil
        }
    }

    def addUrlNumber(uuid: String, urlNum: Long) = {
        try {
            masterDatabase withSession {
                val query = for {
                    u <- siteStatus
                    if u.uuid === uuid
                } yield u.urlNumber ~ u.gmtCreated
                if (query.firstOption.isDefined) {
                    query.update(query.firstOption.get._1 + urlNum, new Timestamp(System.currentTimeMillis))
                } else
                    siteStatus.insert(SiteStatus(uuid, urlNum))
            }
        } catch {
            case ex: Exception =>
                log.error(ex, "Failed to set the url number for uuid:[uuid: " + uuid + "]")
        }
    }

    def cleanNumber(uuid: String) = {
        try {
            masterDatabase withSession {
                val query = for {
                    u <- siteStatus if u.uuid === uuid
                } yield u.urlNumber ~ u.gmtCreated
                if (query.firstOption.isDefined)
                    query.update(0L, new Timestamp(System.currentTimeMillis))
            }
        } catch {
            case ex: Exception =>
                log.error(ex, "Failed to clean the url number of uuid:[uuid: " + uuid + "]")
        }
    }
}
