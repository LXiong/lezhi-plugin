package com.buzzinate.lezhi.model

import java.nio.ByteBuffer
import java.sql.Timestamp

import org.scalaquery.ql.Query
import org.scalaquery.ql.TypeMapper.StringTypeMapper
import org.scalaquery.ql.extended.{ ExtendedTable => Table }
import org.scalaquery.ql.extended.MySQLDriver.Implicit._
import org.scalaquery.session.Database.threadLocalSession

import com.buzzinate.lezhi.db.MysqlClient.{ masterDatabase, slaveDatabase }
import com.buzzinate.lezhi.util.{ Util, Logging }

object PageInfo extends Logging {
    case class PageInfo(var url: String,
        var urlUuid: String,
        var title: String,
        var siteUuid: String = "",
        var gmtCreated: Timestamp = new Timestamp(System.currentTimeMillis))

    val pageInfo = new Table[PageInfo]("page_info") {
        def url = column[String]("url")
        def urlUuid = column[String]("url_uuid")
        def title = column[String]("title")
        def siteUuid = column[String]("site_uuid")
        def gmtCreated = column[Timestamp]("gmt_created")
        def * = url ~ urlUuid ~ title ~ siteUuid ~ gmtCreated <> (PageInfo, PageInfo.unapply _)
        def pk = primaryKey("pk", urlUuid)
    }

    def save(url: String, title: String, siteUuid: String): Boolean = {
        try {
            masterDatabase withSession {
                pageInfo.insert(PageInfo(url, Util.urlToUuid(url), title, siteUuid))
            }
            true
        } catch {
            case ex: Exception =>
                log.error(ex, "Failed to update PageInfo, [url: " + url + "]")
                false
        }
    }

    def get(url: String): Option[PageInfo] = {
        getByUrlUuid(Util.urlToUuid(url))
    }

    def getByUrlUuid(urlUuid: String): Option[PageInfo] = {
        try {
            slaveDatabase withSession {
                val query = for {
                    u <- pageInfo if (u.urlUuid === urlUuid)
                } yield u
                if (query.firstOption.isDefined)
                    Some(query.first)
                else
                    None
            }
        } catch {
            case ex: Exception =>
                log.error(ex, "Failed to get page by url_uuid:" + urlUuid)
                None
        }
    }

    def getUrlByUrlUuid(urlUuid: String): Option[String] = {
        try {
            slaveDatabase withSession {
                val query = for {
                    u <- pageInfo if (u.urlUuid === urlUuid)
                } yield u.url
                if (query.firstOption.isDefined)
                    Some(query.first)
                else
                    None
            }
        } catch {
            case ex: Exception =>
                log.error(ex, "Failed to get page by url_uuid:" + urlUuid)
                None
        }
    }

    def get(urls: Seq[String]): Seq[PageInfo] = {
        if (urls.isEmpty) {
            return Nil
        }
        val urlUuids = urls.map { url =>
            Util.urlToUuid(url)
        }
        getByUrlUuids(urlUuids)
    }

    def getByUrlUuids(urlUuids: Seq[String]): Seq[PageInfo] = {
      if (urlUuids.isEmpty) {
        return Nil
      }
      try {
        slaveDatabase withSession {
          val query = for {
            u <- pageInfo if (u.urlUuid inSet urlUuids)
          } yield u
          query.list
        }
      } catch {
        case ex: Exception =>
          log.error(ex, "Failed to get out clicks of pages")
          Nil
      }
    }

    def update(url: String, title: String, siteUuid: String) = {
        try {
            masterDatabase withSession {
                val query = for {
                    u <- pageInfo if (u.siteUuid === siteUuid && u.urlUuid === Util.urlToUuid(url))
                } yield u.title ~ u.gmtCreated
                if (query.firstOption.isDefined)
                    query.update(title, new Timestamp(System.currentTimeMillis))
                else
                    pageInfo.insert(PageInfo(url, Util.urlToUuid(url), title, siteUuid))
            }
            true
        } catch {
            case ex: Exception =>
                log.error(ex, "Failed to update PageInfo, [siteUuid: " + siteUuid + ",url:" + url + ",title:" + title + "]");
                false
        }
    }

    def batchUpdate(params: List[(String, String, Option[java.util.UUID])]) = {
        try {
            var inserts = params.map {
                case (url, title, uuid) =>
                    PageInfo(url, Util.urlToUuid(url), title, uuid.getOrElse("").toString)
            }
            val existedUuidUrls = params.map {
                case (url, title, uuid) =>
                    Util.urlToUuid(url)
            }
            masterDatabase withSession {
                val query = for {
                    u <- pageInfo if (u.urlUuid inSet existedUuidUrls)
                } yield u.urlUuid
                inserts = inserts.filter(pageInfo => !query.list.contains(pageInfo.urlUuid))
                pageInfo.insertAll(inserts: _*)
            }
            true
        } catch {
            case ex: Exception =>
                log.error(ex, "Failed to update PageInfo, [params: " + params + "]");
                false
        }
    }

    def migrateUpdate(params: List[(String, String, Option[java.util.UUID])]) = {
        try {
            var inserts = params.map {
                case (url, title, uuid) =>
                    PageInfo(url, Util.urlToUuid(url), title, uuid.getOrElse("").toString)
            }
            masterDatabase withSession {
                pageInfo.insertAll(inserts: _*)
            }
            true
        } catch {
            case ex: Exception =>
                log.error(ex, "Failed to update PageInfo, [params: " + params + "]");
                false
        }
    }

    def delete(url: String): Boolean = {
        try {
            masterDatabase withSession {
                pageInfo.where(uu => uu.urlUuid === Util.urlToUuid(url)).delete
            }
            true
        } catch {
            case ex: Exception =>
                log.error(ex, "Failed to get PageInfo, [url: " + url + "]");
                false
        }
    }
}