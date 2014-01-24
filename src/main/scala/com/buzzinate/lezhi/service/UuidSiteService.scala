package com.buzzinate.lezhi.service

import scala.collection.JavaConverters.asScalaBufferConverter

import com.buzzinate.lezhi.Config
import com.buzzinate.lezhi.util.Logging
import com.buzzinate.user.service.UserFinagledService
import com.buzzinate.user.vo.UuidSite

/**
 * @author jeffrey.ji <jeffrey.ji@buzzinate.com>
 * Apr 23, 2013 5:44:00 PM
 *
 */
object UuidSiteService extends Logging {
    private val userFinagledService = new UserFinagledService(Config.getString("buzz.user.server.host"))
    userFinagledService.setHostConnectionCoresize(Config.getInt("buzz.user.connection.num"))
    userFinagledService.setHostConnectionLimit(Config.getInt("buzz.user.connection.limit"))
    userFinagledService.setHostConnectionIdleTime(Config.getInt("buzz.user.connection.idle"))
    userFinagledService.setHostConnectionMaxWaiters(Config.getInt("buzz.user.connection.maxwaiter"))

    val uuidSiteService = new com.buzzinate.user.service.UuidSiteService
    uuidSiteService.setUserFinagledService(userFinagledService)

    def getUuidSiteByUserId(userId: Int): List[UuidSite] = {
        try {
            uuidSiteService.getUuidSiteByUserId(userId).asScala.toList
        } catch {
            case ex: Exception =>
                log.error(ex, "Exception when get uuid site by user id:" + userId)
                Nil
        }
    }

    def getUuidSiteByUuid(uuid: String): UuidSite = {
        try {
            uuidSiteService.getUuidSiteByUuid(uuid)
        } catch {
            case ex: Exception =>
                log.error(ex, "Exception when get uuid site by uuid:" + uuid)
                null
        }
    }

    def getUuidSiteByName(name: String): Seq[String] = {
        try {
            uuidSiteService.getUuidSitesByName(name).asScala
        } catch {
            case ex: Exception =>
                log.error(ex, "Exception when get uuid site by name:" + name)
                Nil
        }
    }

    def getUuidSiteCount(userId: Int): Int = {
        try {
            uuidSiteService.getUuidSiteCount(userId)
        } catch {
            case ex: Exception =>
                log.error(ex, "Exception when get uuid site count by user id:" + userId)
                0
        }
    }

    def createUuidSite(site: UuidSite) = {
        uuidSiteService.createUuidSite(site)
    }

    def updateUuidSite(site: UuidSite) = {
        try {
            uuidSiteService.updateUuidSite(site)
        } catch {
            case ex: Exception =>
                log.error(ex, "Exception when update uuid site:" + site)
        }
    }

    def deleteUuidSite(userId: Int, uuid: String) = {
        uuidSiteService.deleteUuidSite(userId, uuid)
    }
}