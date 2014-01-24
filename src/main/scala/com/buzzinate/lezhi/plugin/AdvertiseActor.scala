package com.buzzinate.lezhi.plugin

import java.util.{ArrayList, Date}
import org.apache.commons.lang.NumberUtils
import com.buzzinate.buzzads.analytics.{AdClick, AdShowUps, AdPageView}
import com.buzzinate.common.util.kestrel.KestrelClient
import com.buzzinate.common.util.serialize.HessianSerializer
import com.buzzinate.lezhi.Config
import com.buzzinate.lezhi.util.Logging
import com.buzzinate.lezhi.bean.{AdvertisePageViewMessage, AdvertiseViewMessage, AdvertiseClickMessage}
import akka.actor.Actor
import com.buzzinate.buzzads.enums.AdNetworkEnum

/**
 * @author jeffrey.ji <jeffrey.ji@buzzinate.com>
 * @date Jan 5, 2013 9:02:35 PM
 *
 */
object AdvertiseUtil extends Logging {
    //constants for buzzads statistic
    val ADCLICK_QUEUE = "ad_click"
    val ADSHOWUP_QUEUE = "ad_showup"
    val ADPAGE_QUEUE = "ad_pageview"
    lazy val adKestrelClient = new KestrelClient(Config.getString("ad.kestrel.ips"), Config.getInt("ad.kestrel.pool.size"))
}

class AdvertiseActor extends Actor with Logging {

    import com.buzzinate.lezhi.plugin.AdvertiseUtil.{ADCLICK_QUEUE, ADSHOWUP_QUEUE, ADPAGE_QUEUE, adKestrelClient}

    def receive = {
        case AdvertisePageViewMessage(url, uuid, xid, ip, ua) => {
            val adPageView = getAdPageView(url, uuid, xid, ip, ua)
            adKestrelClient.put(ADPAGE_QUEUE, HessianSerializer.serialize(adPageView))
        }

        case AdvertiseViewMessage(adEntryIds, url, uuid, xid, ip, ua) => {
            val ids = adEntryIds.split(",")
            val idList = new ArrayList[java.lang.Long]
            ids.foreach(id => {
                if (NumberUtils.isDigits(id))
                    idList.add(java.lang.Long.parseLong(id))
            })
            if (idList.size() > 0) {
                val adShowups = getAdShowups(idList, url, uuid, xid, ip, ua)
                adKestrelClient.put(ADSHOWUP_QUEUE, HessianSerializer.serialize(adShowups))
            }
        }

        case AdvertiseClickMessage(adEntryIds, url, uuid, xid, ip, ua) => {
            val adClick = getAdClick(adEntryIds, url, uuid, xid, ip, ua)
            adKestrelClient.put(ADCLICK_QUEUE, HessianSerializer.serialize(adClick))
        }

        case _ =>
            log.debug("Unknownm advertise statistic message...")
    }

    private def getAdClick(
                            adEntryId: String,
                            sourceUrl: String,
                            publisherUuid: String,
                            xid: String,
                            ip: String,
                            ua: String) = {
        val adClick = new AdClick
        adClick.setAdEntryId(adEntryId.toLong)
        adClick.setCookieId(xid)
        adClick.setIp(ip)
        adClick.setPublisherUuid(publisherUuid)
        adClick.setSourceUrl(sourceUrl)
        adClick.setCreateAt(new Date)
        adClick.setNetwork(AdNetworkEnum.LEZHI);
        adClick.setUa(ua);
        adClick
    }

    private def getAdShowups(
                              adEntryIds: java.util.List[java.lang.Long],
                              sourceUrl: String,
                              publisherUuid: String,
                              xid: String,
                              ip: String,
                              ua: String) = {
        val adShowups = new AdShowUps
        adShowups.setAdEntryIds(adEntryIds)
        adShowups.setCookieId(xid)
        adShowups.setIp(ip)
        adShowups.setPublisherUuid(publisherUuid)
        adShowups.setSourceUrl(sourceUrl)
        adShowups.setCreateAt(new Date)
        adShowups.setNetwork(AdNetworkEnum.LEZHI);
        adShowups.setUa(ua);
        adShowups
    }

    private def getAdPageView(
                               url: String,
                               publisherUuid: String,
                               xid: String,
                               ip: String,
                               ua: String) = {
        val adPageView = new AdPageView
        adPageView.setCookieId(xid)
        adPageView.setIp(ip)
        adPageView.setPublisherUuid(publisherUuid)
        adPageView.setSourceUrl(url)
        adPageView.setCreateAt(new Date)
        adPageView.setNetwork(AdNetworkEnum.LEZHI);
        adPageView.setUa(ua);
        adPageView
    }
}