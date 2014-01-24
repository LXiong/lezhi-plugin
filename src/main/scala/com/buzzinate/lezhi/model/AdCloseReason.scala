package com.buzzinate.lezhi.model

import org.scalaquery.ql.TypeMapper.{ DateTypeMapper, StringTypeMapper }
import org.scalaquery.ql.extended.{ ExtendedTable => Table }
import org.scalaquery.ql.extended.MySQLDriver.Implicit._
import org.scalaquery.session.Database.threadLocalSession
import com.buzzinate.lezhi.db.MysqlClient.{ masterDatabase, slaveDatabase }
import com.buzzinate.lezhi.util.Logging
import java.sql.Timestamp

object AdCloseReason extends Logging {
    case class AdCloseReason(var uuid: String, var closeReason: String, val gmtCreated: Timestamp = new Timestamp(System.currentTimeMillis))

    val reason = new Table[AdCloseReason]("ad_close_reason") {
        def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
        def uuid = column[String]("uuid")
        def closeReason = column[String]("reason")
        def gmtCreated = column[Timestamp]("gmt_created")
        def * = uuid ~ closeReason ~ gmtCreated <> (AdCloseReason, AdCloseReason.unapply _)
        def key = index("uuid_key", uuid)
    }

    def add(uuid: String, closeReason: String): Boolean = {
        try {
            masterDatabase withSession {
                reason.insert(AdCloseReason(uuid, closeReason))
            }
            true
        } catch {
            case ex: Exception =>
                log.error(ex, "Failed to add the ads close reason")
                false
        }
    }

    def del(uuid: String) = {
        try {
            masterDatabase withSession {
                reason.where(uu => uu.uuid === uuid).delete
            }
            true
        } catch {
            case ex: Exception =>
                log.error(ex, "Failed to delete the ads close reason by uuid:" + uuid)
                false
        }
    }

    def get(uuid: String): List[AdCloseReason] = {
        try {
            slaveDatabase withSession {
                val query = for {
                    u <- reason if u.uuid === uuid
                } yield u
                query.list
            }
        } catch {
            case ex: Exception =>
                log.error(ex, "Failed to get the ads close reason by uuid:" + uuid)
                Nil
        }
    }
}

