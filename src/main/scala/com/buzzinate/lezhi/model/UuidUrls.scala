package com.buzzinate.lezhi.model

import java.sql.Timestamp
import org.scalaquery.ql.Query
import org.scalaquery.ql.TypeMapper.StringTypeMapper
import org.scalaquery.ql.extended.{ ExtendedTable => Table }
import org.scalaquery.ql.extended.MySQLDriver.Implicit._
import org.scalaquery.session.Database.threadLocalSession
import com.buzzinate.lezhi.db.MysqlClient.{ masterDatabase, slaveDatabase }
import com.buzzinate.lezhi.util.Logging
import com.buzzinate.lezhi.util.Util
/**
 * @author jeffrey created on 2012-8-21 下午7:46:04
 *
 */
object UuidUrls extends Logging {
    case class UuidUrls(var uuid: String,
        var url: String,
        var urlUuid: String,
        var status: String,
        val gmtCreated: Timestamp = new Timestamp(System.currentTimeMillis))

    val uuidUrls = new Table[UuidUrls]("uuid_urls") {
        def uuid = column[String]("uuid")
        def url = column[String]("url")
        def urlUuid = column[String]("url_uuid")
        def status = column[String]("status")
        def gmtCreated = column[Timestamp]("gmt_created")
        def * = uuid ~ url ~ urlUuid ~ status ~ gmtCreated <> (UuidUrls, UuidUrls.unapply _)
        def pk = primaryKey("pk", (uuid, urlUuid))
    }

    def getUrls(uuid: String): Seq[String] = {
        try {
            slaveDatabase withSession {
                val query = for {
                    u <- uuidUrls if (u.uuid === uuid)
                } yield u.url
                query.list
            }
        } catch {
            case ex: Exception =>
                log.error(ex, "Failed to get pagination urls from uuid_urls by [uuid: " + uuid + "]");
                Nil
        }
    }

    def getPaginationUrls(uuid: String, status: String, start: Int, size: Int): Seq[String] = {
        try {
            slaveDatabase withSession {
                val query = Query(uuidUrls).where(uu => uu.uuid === uuid && uu.status === status).drop(start).take(size)
                query.list.map { uuidUrl =>
                    uuidUrl.url
                }
            }
        } catch {
            case ex: Exception =>
                log.error(ex, "Failed to get pagination urls from uuid_urls by [uuid: " + uuid + "]");
                Nil
        }
    }

    def isUrlExisted(uuid: String, url: String): Boolean = {
        try {
            slaveDatabase withSession {
                val query = for {
                    u <- uuidUrls if (u.uuid === uuid && u.urlUuid === Util.urlToUuid(url))
                } yield u.urlUuid
                query.list.size > 0
            }
        } catch {
            case ex: Exception =>
                log.error(ex, "Failed to get url from uuid_urls by [uuid: " + uuid + ",url:" + url + "]");
                false
        }
    }

    /**
     * initialize normal status urls
     */
    def addUrls(uuid: String, status: String, urls: Seq[String]): Boolean = {
        if (urls.isEmpty) {
            return false
        }
        var inserts = urls.map { url =>
            UuidUrls(uuid, url, Util.urlToUuid(url), status)
        }
        val urlUuids = urls.map { url =>
            Util.urlToUuid(url)
        }
        try {
            masterDatabase withSession {
                val query = for {
                    u <- uuidUrls if (u.urlUuid inSet urlUuids)
                } yield u.url
                inserts = inserts.filter(uuidUrl => !query.list.contains(uuidUrl.url))
                uuidUrls.insertAll(inserts: _*)
            }
            true
        } catch {
            case ex: Exception =>
                log.error(ex, "Failed to update uuid_urls by  [uuid: " + uuid + ",urls is:" + urls + "]");
                false
        }
    }

    /**
     * delete the urls of status
     */
    def delUrls(uuid: String, urls: List[String]): Boolean = {
        val urlUuids = urls.map { url =>
            Util.urlToUuid(url)
        }
        try {
            masterDatabase withSession {
                uuidUrls.where(uu => (uu.uuid === uuid) && (uu.urlUuid inSet urlUuids)).delete
            }
            true
        } catch {
            case ex: Exception =>
                log.error(ex, "Failed to delete uuid_urls by  [uuid: " + uuid + ",urls is:" + urls + "]");
                false
        }
    }

    /**
     * update status
     */
    def updateStatus(uuid: String, url: String, newStatus: String): Boolean = {
        try {
            masterDatabase withSession {
                val query = for {
                    u <- uuidUrls if (u.uuid === uuid && u.urlUuid === Util.urlToUuid(url))
                } yield u.status
                if (query.firstOption.isDefined) {
                    query.update(newStatus)
                } else
                    uuidUrls.insert(UuidUrls(uuid, url, Util.urlToUuid(url), newStatus))
            }
            true
        } catch {
            case ex: Exception =>
                log.error("Failed to change the [uuid:" + uuid + ",url:" + url + "] to new status:[ " + newStatus + "]");
                false
        }
    }

    def getUrlsNum(uuid: String, status: String): Int = {
        try {
            slaveDatabase withSession {
                val query = for (u <- uuidUrls if (u.uuid === uuid) && (u.status === status)) yield u.urlUuid
                Query(query.count).first 
            }
        } catch {
            case ex: Exception =>
                log.error(ex, "Failed to get urls count from uuid_urls by [uuid: " + uuid + "]");
                0
        }
    }
}