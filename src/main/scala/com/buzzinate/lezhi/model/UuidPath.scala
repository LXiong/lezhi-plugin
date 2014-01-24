package com.buzzinate.lezhi.model

import com.buzzinate.lezhi.util.Logging
import java.sql.Timestamp
import org.apache.commons.lang.StringUtils
import org.scalaquery.ql.TypeMapper.StringTypeMapper
import org.scalaquery.ql.extended.{ ExtendedTable => Table }
import org.scalaquery.ql.extended.MySQLDriver.Implicit._
import org.scalaquery.session.Database.threadLocalSession
import scala.Array
import org.scalaquery.ql.{ Subquery, ColumnOps, Query }
import com.buzzinate.lezhi.bean.Paginate
import com.buzzinate.common.util.string.StringUtil
import scala.collection.JavaConverters._
import com.buzzinate.lezhi.db.MysqlClient.{ masterDatabase, slaveDatabase }
import com.buzzinate.lezhi.util.Util

/**
 * uuid <=> path
 *
 * User: magic
 * Date: 13-7-22 上午11:39
 */

class UuidPath(val id: Int = 0, var uuid: Array[Byte], var pathHash: Array[Byte], var path: String, var status: Int = 0,
    var updateTime: Timestamp = new Timestamp(System.currentTimeMillis)) {
    def getUuid = {
        StringUtil.byteArrayToUuid(uuid)
    }
}

object UuidPath extends Logging {

    val uuidPaths = new Table[UuidPath]("uuid_path") {
        def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

        def uuid = column[Array[Byte]]("uuid")

        def pathHash = column[Array[Byte]]("path_hash")

        def path = column[String]("path")

        def status = column[Int]("status")

        def updateTime = column[Timestamp]("update_time")

        def * = id ~ uuid ~ pathHash ~ path ~ status ~ updateTime <> (UuidPath.apply _, UuidPath.unapply _)

        def pk = primaryKey("pk", id)
    }

    /**
     * get uuid path info by pagination
     */
    def getUuidPathsAsPaginate(paginate: Paginate): Seq[UuidPath] = {
        try {
            slaveDatabase withSession {
                Query(uuidPaths).drop(paginate.startIndex).take(paginate.pageSize).list
            }
        } catch {
            case ex: Exception =>
                log.error(ex, "get uuid path by paginate: " + paginate)
                Nil
        }
    }

    def countAll(): Int = {
        try {
            slaveDatabase withSession {
                Query(uuidPaths.count).first
            }
        } catch {
            case ex: Exception =>
                log.error(ex, "get uuid path by paginate: ")
                0
        }
    }

    /**
     * get uuid path info filter by uuid
     */
    def getUuidPaths(uuid: String): Seq[UuidPath] = {
        if (StringUtils.isBlank(uuid)) {
            Nil
        }

        try {
            slaveDatabase withSession {
                val query = for {
                    u <- uuidPaths if u.uuid === Util.uuidToByte(uuid).bind
                } yield u

                query.list
            }
        } catch {
            case ex: Error =>
                log.error(ex, "get uuid path by uuid : " + uuid)
                Nil
        }
    }

    /**
     * get site prefixs by uuid
     */
    def getSitePrefixs(uuid: String): Seq[String] = {
        if (StringUtils.isBlank(uuid)) {
            Nil
        }
        try {
            slaveDatabase withSession {
                val query = for {
                    u <- uuidPaths if (u.uuid === Util.uuidToByte(uuid).bind)
                } yield u.path

                query.list
            }
        } catch {
            case ex: Exception =>
                log.error(ex, "get site prefixs by uuid : " + uuid)
                Nil
        }
    }

    /**
     * get uuid path info filter by uuid
     */
    def getUuidPathsByStatus(uuid: String, status: Int): Seq[String] = {
        if (StringUtils.isBlank(uuid)) {
            Nil
        }

        try {
            slaveDatabase withSession {
                val query = for {
                    u <- uuidPaths if (u.uuid === Util.uuidToByte(uuid).bind && u.status === status)
                } yield u.path

                query.list
            }
        } catch {
            case ex: Exception =>
                log.error(ex, "get uuid path by uuid : " + uuid)
                Nil
        }
    }

    /**
     * get uuid path info filter by path
     */
    def getUuidPath(path: String): Option[UuidPath] = {
        if (StringUtils.isBlank(path)) {
            None
        }

        try {
            val pathHash = StringUtil.hash(path)
            slaveDatabase withSession {
                val queryByPath = for {
                    u <- uuidPaths if u.pathHash === pathHash.bind
                } yield u

                if (queryByPath.firstOption.isDefined)
                    Some(queryByPath.first)
                else
                    None
            }
        } catch {
            case ex: Exception =>
                log.error(ex, "get uuid path by path : " + path)
                None
        }
    }

    /**
     * addd or update uuid path
     */
    def addOrUpdateUuidPath(path: String, uuid: String): Boolean = {
        try {
            masterDatabase withSession {
                val pathHash = StringUtil.hash(path)
                val uuidHash: Array[Byte] = Util.uuidToByte(uuid)

                val query = for {
                    u <- uuidPaths if u.pathHash === pathHash.bind
                } yield u.uuid ~ u.updateTime

                if (query.firstOption.isDefined)
                    query.update(uuidHash, new Timestamp(System.currentTimeMillis))
                else
                    uuidPaths.insert(new UuidPath(0, uuidHash, pathHash, path, 0))
                true
            }
        } catch {
            case ex: Error =>
                log.error(ex, "get uuid path by uuid : " + uuid)
                false
        }
    }

    /**
     * batch update path <=> uuid match
     */
    def batchUpdatePathUuid(pathUUidMap: java.util.HashMap[String, String]): Boolean = {
        try
            masterDatabase withSession {
                pathUUidMap.asScala.foreach {
                    case (path, uuid) =>
                        val pathHash = StringUtil.hash(path)
                        val uuidHash: Array[Byte] = Util.uuidToByte(uuid)

                        val query = for {
                            u <- uuidPaths if u.pathHash === pathHash.bind
                        } yield u.uuid ~ u.status

                        if (query.firstOption.isDefined) {
                            val (uuid, status) = query.first
                            if (!java.util.Arrays.equals(uuid, uuidHash)) {
                                query.update(uuidHash, 0)
                            }
                        } else {
                            uuidPaths.insert(new UuidPath(0, uuidHash, pathHash, path))
                        }
                }
                true
            }
        catch {
            case ex: Exception =>
                log.error(ex, "batch update uuid path error")
                false
        }
    }

    def updateStatus(id: Int, enable: Boolean): Boolean = {
        try {
            masterDatabase withSession {
                val query = for {
                    u <- uuidPaths if u.id === id
                } yield u.status

                var status = 0
                if (enable) {
                    status = 1
                }
                query.update(status)
                true
            }
        } catch {
            case ex: Exception =>
                log.error(ex, "update status error, id : " + id)
                false
        }
    }

    /**
     * remove uuid path by id
     */
    def deleteUuidPath(id: Int): Boolean = {
        try {
            masterDatabase withSession {
                val query = for {
                    u <- uuidPaths if u.id === id
                } yield u

                query.delete
                true
            }
        } catch {
            case ex: Exception =>
                log.error(ex, "remove uuid path by id : " + id)
                false
        }
    }

    def deleteUuidPath(path: String): Boolean = {
        try {
            masterDatabase withSession {
                val pathHash = StringUtil.hash(path)
                val query = for {
                    u <- uuidPaths if u.pathHash === pathHash.bind
                } yield u

                query.delete
                true
            }
        } catch {
            case ex: Exception =>
                log.error(ex, "remove uuid path by id : " + path)
                false
        }
    }

    def deleteAll(): Boolean = {
        try {
            masterDatabase withSession {
                val query = for {
                    u <- uuidPaths
                } yield u

                query.delete
                true
            }
        } catch {
            case ex: Exception =>
                log.error(ex, "remove all uuid path error")
                false
        }
    }

    def unapply(uuidPath: UuidPath) = Some((uuidPath.id, uuidPath.uuid, uuidPath.pathHash, uuidPath.path, uuidPath.status, uuidPath.updateTime))

    def apply(id: Int, uuid: Array[Byte], pathHash: Array[Byte], path: String, status: Int, updateTime: Timestamp) = new UuidPath(id, uuid, pathHash, path, status, updateTime)

}
