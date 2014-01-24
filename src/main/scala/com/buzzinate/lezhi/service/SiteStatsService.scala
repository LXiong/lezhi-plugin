package com.buzzinate.lezhi.service

import java.util.Date

import scala.Option.option2Iterable
import scala.annotation.serializable
import scala.collection.mutable

import org.apache.commons.lang.StringUtils

import com.buzzinate.common.util.string.UrlUtil
import com.buzzinate.lezhi.bean.Paginate
import com.buzzinate.lezhi.bean.SiteStats
import com.buzzinate.lezhi.model.SiteConfig
import com.buzzinate.lezhi.model.SiteStatus
import com.buzzinate.lezhi.model.StatSite
import com.buzzinate.lezhi.util.Util
import scala.collection.mutable.ListBuffer

object SiteStatsService {

    /**
     * get site datas
     */
    def getSiteDatas(uuids: Seq[String], startDay: Date, endDay: Date, paginate: Paginate, sortType: String): (Int, List[SiteStats]) = {
        var siteStatResult = List[(String, SiteStats)]()
        var totalSize = 0
        
        // file siteStatus and statSite
        if (uuids == null || uuids.isEmpty) {
            sortType match {
                case "articleNum" =>
                	val siteStatusDatas = SiteStatus.getArticleNumByPaginate(paginate)
                	val uuids = siteStatusDatas.map(siteStatus => siteStatus.uuid)
                	val statSites = StatSite.getStatSiteByUuids(uuids, startDay, endDay, sortType)
                	siteStatResult = fillSiteDatas(siteStatusDatas, statSites, sortType)
                case _ =>
                    val statSites = StatSite.getStatSiteByPaginate(paginate, sortType, startDay, endDay)
                    val uuids = statSites.map(statSite => statSite.uuid)
                    val siteStatusDatas = SiteStatus.getArticleNumByUuids(uuids)
                	siteStatResult = fillSiteDatas(siteStatusDatas, statSites, sortType)
            }
            totalSize = SiteStatus.getUuidCount
        } else {
        	val siteStatusDatas = SiteStatus.getArticleNumByUuids(uuids)
        	val statSites = StatSite.getStatSiteByUuids(uuids, startDay, endDay, sortType)
        	siteStatResult = fillSiteDatas(siteStatusDatas, statSites, sortType)
            totalSize = uuids.size
        }
        
        // fill site info
        fillSiteInfos(siteStatResult)
        (totalSize, siteStatResult.map(ele => ele._2))
    }
    
    private def fillSiteDatas(siteStatusDatas: Seq[SiteStatus.SiteStatus], statSites: Seq[StatSite.StatSite], sortType: String): List[(String, SiteStats)] = {
        var siteDatas = ListBuffer[(String, SiteStats)]()
        sortType match {
            case "articleNum" => 
                siteStatusDatas.foreach { siteStatusData =>
                    val statSite = statSites.find(statSite => statSite.uuid == siteStatusData.uuid)
                    siteDatas += ((siteStatusData.uuid, new SiteStats(siteStatusData, statSite.getOrElse(null))))
                }
            case _ => 
                statSites.foreach { statSite => 
                    val siteStatusData = siteStatusDatas.find(siteStatusData => siteStatusData.uuid == statSite.uuid)
                    siteDatas += ((statSite.uuid, new SiteStats(siteStatusData.getOrElse(null), statSite)))
                }
        }
        siteDatas.toList
    }

    private def fillSiteInfos(infos: List[(String, SiteStats)]) = {
        for (info <- infos) {
            //get the specific uuid bytes from the buffer and convert to uuid
            val uuid = info._1
            val site = UuidSiteService.getUuidSiteByUuid(uuid)
            val siteConfig = SiteConfig.get(uuid)
            if (site != null && siteConfig.nonEmpty) {
                val siteStat = info._2
                siteStat.name = site.getName
                siteStat.url = UrlUtil.getFullDomainNameWithHttpPrefix(site.getUrl)
                siteStat.pluginType = siteConfig.get.pluginType
                siteStat.pic = siteConfig.get.pic
                siteStat.adEnabled = siteConfig.get.adEnabled
            }
        }
    }
}
