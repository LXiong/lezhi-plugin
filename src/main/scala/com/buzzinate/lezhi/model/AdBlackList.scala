package com.buzzinate.lezhi.model

import java.sql.Timestamp

import org.apache.commons.lang.StringUtils
import org.scalaquery.ql.TypeMapper.StringTypeMapper
import org.scalaquery.ql.extended.{ ExtendedTable => Table }
import org.scalaquery.ql.extended.MySQLDriver.Implicit._
import org.scalaquery.session.Database.threadLocalSession
import org.scalaquery.simple.StaticQuery

import com.buzzinate.lezhi.db.MysqlClient.{ masterDatabase, slaveDatabase }
import com.buzzinate.lezhi.util.{ Logging, Util }

/**
 * @author jeffrey created on 2012-11-28 上午11:22:21
 *
 */
object AdBlackList extends Logging {
    case class AdBlackList(var uuid: String = "", keyword: String, val gmtCreated: Timestamp = new Timestamp(System.currentTimeMillis))

    val adBlackList = new Table[AdBlackList]("ad_black_list") {
        def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
        def uuid = column[String]("uuid")
        def keyword = column[String]("keyword")
        def gmtCreated = column[Timestamp]("gmt_created")
        def * = uuid ~ keyword ~ gmtCreated <> (AdBlackList, AdBlackList.unapply _)
        def key = index("uuid_key", uuid)
    }

    def getKeywords(uuid: String): Seq[String] = {
        if (StringUtils.isBlank(uuid)) {
            return Nil
        }
        try {
            slaveDatabase withSession {
                val query = for {
                    u <- adBlackList if (u.uuid === uuid)
                } yield u.keyword
                query.list
            }
        } catch {
            case ex: Exception =>
                log.error(ex, "Failed to get ad blacklist keywords by uuid:[uuid: " + uuid + "]");
                Nil
        }
    }

    def addKeywords(uuid: String, keywords: Seq[String]): Boolean = {
        if (keywords.isEmpty) {
            return false
        }
        val inserts = keywords.map { keyword =>
            AdBlackList(uuid, keyword)
        }
        try {
            masterDatabase withSession {
                adBlackList.insertAll(inserts: _*)
            }
            true
        } catch {
            case ex: Exception =>
                log.error(ex, "Failed to update ad blacklist keywords:[uuid: " + uuid + ",keywords:" + keywords + "]");
                false
        }
    }

    def delKeywords(uuid: String, keywords: Seq[String]): Boolean = {
        if (keywords.isEmpty) {
            return false
        }
        try {
            masterDatabase withSession {
                keywords.foreach { keyword =>
                    adBlackList.where(uu => (uu.uuid === uuid) && (uu.keyword === keyword)).delete
                }
            }
            true
        } catch {
            case ex: Exception =>
                log.error(ex, "Failed to delete ad keywords from:[uuid: " + uuid + ",keywords:" + keywords + "]");
                false
        }
    }

    def delAllKeywords(uuid: String): Boolean = {
        try {
            masterDatabase withSession {
                adBlackList.where(uu => uu.uuid === uuid).delete
            }
            true
        } catch {
            case ex: Exception =>
                log.error(ex, "Failed to delete all the ad keywords from:[uuid: " + uuid + "]");
                false
        }
    }
}