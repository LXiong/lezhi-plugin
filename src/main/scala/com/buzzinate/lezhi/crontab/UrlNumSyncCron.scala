package com.buzzinate.lezhi.crontab

import com.buzzinate.lezhi.model.{SiteConfig, SiteStatus, UuidPath}
import com.buzzinate.lezhi.plugin.PluginConstant.elasticClient
import com.buzzinate.lezhi.util.Logging

/**
 * Calculate the url num from uuid_urls and synchronize to url_num
 *
 */
object UrlNumSyncCron extends Logging {

    def main(args: Array[String]) {
        try {
            val uuids = SiteConfig.getAllUuids
            SiteStatus.cleanNumber(SiteStatus.ADMIN_UUID)
            batchCount(uuids).foreach {
                case (uuid, num) =>
                    log.info("Synchronizing uuid " + uuid + ", and the url num is " + num)
                    log.info("Clean the number for uuid " + uuid)
                    SiteStatus.cleanNumber(uuid)
                    log.info("Set the number for uuid " + uuid)
                    SiteStatus.addUrlNumber(uuid, num)
                    log.info("After the synchonization, the url number for uuid: " + uuid + " is " + SiteStatus.getUrlNumber(uuid))
                    SiteStatus.addUrlNumber(SiteStatus.ADMIN_UUID, num)
                    log.info("After the synchonization, the url number for admin uuid: " + SiteStatus.ADMIN_UUID + " is " + SiteStatus.getUrlNumber(SiteStatus.ADMIN_UUID))
            }
            log.info("sync is over")
        } catch {
            case ex: Exception =>
                log.error(ex, "Failed to sync yesterday's url number");
        }
        return
    }

    private def batchCount(uuids: Seq[String]): Seq[(String, Int)] = {
        try {
            uuids.map {
                uuid =>
                    val sitePrefixes = UuidPath.getSitePrefixs(uuid)
                    val (totalSize, records) = elasticClient.matchAll(sitePrefixes, 0, 1)
                    uuid -> totalSize
            }.toSeq
        } catch {
            case ex: Exception =>
                log.error(ex, "Failed to batch get the column number from uuid_urls by [uuids: " + uuids + "]");
                Nil
        }
    }
}
