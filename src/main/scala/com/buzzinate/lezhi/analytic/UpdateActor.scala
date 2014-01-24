package com.buzzinate.lezhi.analytic

import java.util.Date
import scala.collection.JavaConverters._
import com.buzzinate.common.util.DateTimeUtil
import com.buzzinate.lezhi.bean.UpdateMessage
import com.buzzinate.lezhi.model.{ AdminCounts, PageInfo, SitePage, StatPage, StatSite }
import com.buzzinate.lezhi.util.Logging
import akka.actor.Actor
import com.buzzinate.lezhi.model.UuidPath

class UpdateActor extends Actor with Logging {
    def receive = {
        case UpdateMessage(siteMap, sitePage, pageInfo, pageMap, adminMap, pathUuidMap) => {
            log.info("flush analytic data[site size: " + siteMap.size + ", site page size=" + sitePage.size +
                ", page info size=" + pageInfo.size + ",page size: " + pageMap.size + ", admin map size=" + adminMap.size + ", path uuid map size=" + pathUuidMap.size +
                "] to mysql at: " + DateTimeUtil.formatDate(new Date, DateTimeUtil.FMT_DATE_YYYY_MM_DD_HH_MM_SS))

            if (sitePage.size != 0)
                SitePage.batchUpdate(sitePage.asScala.toList)
            if (pageInfo.size != 0)
                PageInfo.batchUpdate(pageInfo.asScala.toList)
            if (siteMap.size != 0)
                StatSite.batchSite(siteMap)
            if (pageMap.size != 0)
                StatPage.batchPage(pageMap)

            if (pathUuidMap.size != 0)
                UuidPath.batchUpdatePathUuid(pathUuidMap)

            log.info("flush admin data: " + adminMap + " to mysql at: " + DateTimeUtil.formatDate(new Date, DateTimeUtil.FMT_DATE_YYYY_MM_DD_HH_MM_SS))
            AdminCounts.incrFromMap(adminMap)
        }
    }
}