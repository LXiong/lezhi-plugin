package com.buzzinate.lezhi.buzzads

import scala.collection.JavaConverters.asScalaBufferConverter
import com.buzzinate.buzzads.thrift.{ AdItem, AdNetworkEnum, AdParam }
import com.buzzinate.lezhi.util.Logging
import com.twitter.util.Future
import com.buzzinate.buzzads.thrift.AdEntryTypeEnum

/**
 * @author jeffrey created on 2012-11-28 下午3:14:42
 *
 */
object AdvertiseClientThrift {
  val client = new AdvertiseFinagledClient
}

class AdvertiseClientThrift extends AdvertiseClient with Logging {

  import AdvertiseClientThrift.client

  def serve(userId: String,
    uuid: String,
    url: String,
    ip: String,
    title: String,
    keywords: String,
    needPic: Boolean,
    count: Int): List[Map[String, Any]] = {
    val resultFuture = getServeFuture(userId, uuid, url, ip, title, keywords, needPic, count)
    getServeResult(resultFuture.get)
  }


  def getServeFuture(userId: String,
    uuid: String,
    url: String,
    ip: String,
    title: String,
    keywords: String,
    needPic: Boolean,
    count: Int): Future[java.util.List[AdItem]] = {
    log.debug("Send advertise serve: [userId=" + userId + "url=" + url + ", ip=" + ip + ", title=" + title
      + ", needPic=" + needPic + ",count=" + count + "]")
    val param = new AdParam.Builder()
      .userid(userId)
      .uuid(uuid)
      .url(url)
      .ip(ip)
      .title(title)
      .keywords(keywords)
      .resourceType( if (needPic) AdEntryTypeEnum.IMAGE else AdEntryTypeEnum.TEXT)
      .count(count).netWork(AdNetworkEnum.LEZHI).build()

    client.serve(param) onFailure {
      case ex: Exception =>
        log.error(ex, "Send advertise serve: [userId=" + userId + "url=" + url + ", ip=" + ip + ", title=" + title
          + ", needPic=" + needPic + ",count=" + count + "] but Failed to get advertise from backend.")
    }
  }

  def getServeResult(rs: java.util.List[AdItem]): List[Map[String, Any]] = {
    rs.asScala.toList.map { item =>
      Map("url" -> item.getUrl,
        "title" -> item.getTitle,
        "pic" -> (if (item.getPicOption().isDefined()) item.getPic else ""),
        "adEntryId" -> (if (item.getAdEntryIdOption().isDefined()) item.getAdEntryId() else 0),
        "ad" -> true)
    }
  }
}