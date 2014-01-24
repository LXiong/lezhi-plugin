package com.buzzinate.lezhi.model

import java.sql.Timestamp
import java.util.UUID
import org.scalaquery.ql.TypeMapper.StringTypeMapper
import org.scalaquery.ql.extended.{ ExtendedTable => Table }
import org.scalaquery.ql.extended.MySQLDriver.Implicit._
import org.scalaquery.session.Database.threadLocalSession
import com.buzzinate.lezhi.db.MysqlClient.{ masterDatabase, slaveDatabase }
import com.buzzinate.lezhi.util.{ Logging, Util }
import java.util.Date
import com.buzzinate.common.util.DateTimeUtil
import com.buzzinate.lezhi.Config

/**
 * pages in site model
 *
 * uuid <=> page
 */
object SitePage extends Logging {

    private val expireInDays = Config.getInt("site.page.data.exipre.day")

    case class SitePage(var uuid: String = "",
        var url: String,
        var urlUuid: String,
        var gmtCreated: Timestamp = new Timestamp(System.currentTimeMillis))

    val sitePages = new Table[SitePage]("site_page") {
        def uuid = column[String]("uuid")
        def url = column[String]("url")
        def urlUuid = column[String]("url_uuid")
        def gmtCreated = column[Timestamp]("gmt_created")
        def * = uuid ~ url ~ urlUuid ~ gmtCreated <> (SitePage, SitePage.unapply _)
        def pk = primaryKey("pk", urlUuid)
    }

    def update(uuid: UUID, url: String): Boolean = {
        try {
            masterDatabase withSession {
                val query = for {
                    u <- sitePages if (u.urlUuid === Util.urlToUuid(url) && u.uuid === uuid.toString)
                } yield u
                if (query.firstOption.isDefined)
                    query.update(SitePage(uuid.toString, url, Util.urlToUuid(url)))
                else
                    sitePages.insert(SitePage(uuid.toString, url, Util.urlToUuid(url)))
            }
            true
        } catch {
            case ex: Exception =>
                log.error(ex, "Failed to update SitePage: [uuid: " + uuid.toString + ", url: " + url + "]");
                false
        }
    }

    def batchUpdate(params: List[(UUID, String)]): Boolean = {
        var sitePageList = params.map {
            case (uuid, url) =>
                SitePage(uuid.toString, url, Util.urlToUuid(url))
        }
        val existedUuidUrls = params.map {
            case (uuid, url) =>
                Util.urlToUuid(url)
        }
        try {
            masterDatabase withSession {
                sitePageList.foreach { sitePage =>
                    val query = queryByUrlUuid(sitePage.urlUuid)
                    if (query.firstOption.isDefined) {
                        val st = query.first
                        if (st.uuid != sitePage.uuid) {
                            st.uuid = sitePage.uuid
                            st.gmtCreated = new Timestamp(System.currentTimeMillis)
                            query.update(st)
                        }
                    } else {
                        sitePages.insert(sitePage)
                    }
                }
            }
            true
        } catch {
            case ex: Exception =>
                log.error(ex, "Failed to update SitePage: [params: " + params + "]");
                false
        }
    }

    def get(uuid: UUID): Seq[String] = {
        try {
            slaveDatabase withSession {
                val query = for {
                    u <- sitePages if (u.uuid === uuid.toString)
                } yield u.url
                query.list
            }
        } catch {
            case ex: Exception =>
                log.error(ex, "Failed to get SitePage: [uuid: " + uuid.toString + "]");
                Nil
        }
    }

    def get(uuid: UUID, startDate: Date, endDate: Date): Seq[String] = {
        try {
            slaveDatabase withSession {
                val query = for {
                    u <- sitePages if ((u.uuid === uuid.toString) && (u.gmtCreated between (new Timestamp(startDate.getTime()), new Timestamp(endDate.getTime()))))
                } yield u.url
                query.list
            }
        } catch {
            case ex: Exception =>
                log.error(ex, "Failed to get SitePage: [uuid: " + uuid.toString + ",startDate:" + DateTimeUtil.formatDate(startDate) + ",endDate:" + DateTimeUtil.formatDate(endDate) ++ "]");
                Nil
        }
    }

    def delete(uuid: UUID): Boolean = {
        try {
            masterDatabase withSession {
                sitePages.where(uu => uu.uuid === uuid.toString).delete
            }
            true
        } catch {
            case ex: Exception =>
                log.error(ex, "Failed to get SitePage, [uuid: " + uuid + "]");
                false
        }
    }

    def delete(): Boolean = {
        val curTime = new Timestamp(System.currentTimeMillis)
        curTime.setDate(curTime.getDate() - expireInDays)
        try {
            masterDatabase withSession {
                sitePages.where(uu => uu.gmtCreated < curTime).delete
            }
            true
        } catch {
            case ex: Exception =>
                log.error(ex, "Failed to delete the SitePage which exists longer than " + expireInDays + " days.");
                false
        }
    }

    private def queryByUrlUuid(urlUuid: String) = for {
        u <- sitePages if (u.urlUuid === urlUuid.bind)
    } yield u

}