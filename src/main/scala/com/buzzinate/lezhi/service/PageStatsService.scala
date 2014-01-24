package com.buzzinate.lezhi.service

import scala.collection.mutable
import java.util.Date
import com.buzzinate.lezhi.util.Util
import org.apache.commons.lang.StringUtils
import com.buzzinate.lezhi.bean.Paginate
import java.net.URLDecoder
import com.buzzinate.lezhi.util.Logging
import scala._
import com.buzzinate.lezhi.model.PageInfo
import com.buzzinate.lezhi.model.StatPage
import com.buzzinate.lezhi.bean.PageStats
import scala.collection.mutable.ListBuffer

object PageStatsService extends Logging {

    /**
     * get page stat data by uuid
     *
     * @param uuid
     * @param startDay
     * @param endDay
     * @param paginate
     * @param sortType
     * @return
     */
    def getPageDatasByUuid(uuid: String, startDay: Date, endDay: Date, paginate: Paginate, sortType: String): (Int, List[PageStats]) = {
        val statPags = StatPage.getStatPagesByUuid(uuid, startDay, endDay, paginate, sortType)
        (statPags._1, getPageStats(statPags._2))
    }

    private def getPageStats(statPages: Seq[StatPage.StatPage]): List[PageStats] = {
        var urlUuids = statPages.map { statPage => Util.byteToUuid(statPage.urlHash).toString }

        val pageInfos = PageInfo.getByUrlUuids(urlUuids)
        val pageInfoMaps = mutable.HashMap[String, PageInfo.PageInfo]()
        pageInfos.foreach { pageInfo =>
            pageInfoMaps(pageInfo.urlUuid) = pageInfo
        }

        var pageStats = ListBuffer[PageStats]()
        statPages.map { statPage =>
            val pageInfo = pageInfoMaps.get(Util.byteToUuid(statPage.urlHash))

            if (pageInfo.isDefined) {
                val pageStat = new PageStats(statPage)
                pageStat.title = pageInfo.get.title
                pageStat.url = URLDecoder.decode(pageInfo.get.url)

                pageStats += (pageStat)
            }
        }
        pageStats.toList
    }

}