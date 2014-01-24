package com.buzzinate.lezhi.recommend

import com.buzzinate.lezhi.thrift.{ ClickParam, RecommendParam, RecommendResult, RecommendType, RecommendTypeParam }
import com.buzzinate.lezhi.util.{ Logging, UrlFilter }
import com.twitter.util.Future
import com.buzzinate.lezhi.bean.RequestParam
import scala.collection.mutable.ListBuffer

object RecommendClientThrift {
    val client = new RecommendFinagledClient
}

class RecommendClientThrift extends RecommendClient with Logging {
    import RecommendClientThrift.client

    def recommend(userId: String,
        url: String,
        types: List[RecommendTypeParam],
        canonicalUrl: String,
        title: String,
        keywords: String,
        sitePrefix: String,
        customThumbnail: Option[String],
        customTitle: Option[String]): (List[(RequestParam, List[Map[String, Any]])], String) = {
        val recommendFuture = getRecommendFuture(userId, url, types, canonicalUrl, title, keywords, sitePrefix, customThumbnail, customTitle)
        getRecommendResult(recommendFuture.get)
    }

    def getRecommendResult(rs: RecommendResult): (List[(RequestParam, List[Map[String, Any]])], String) = {
        val resultMap = rs.results.map { typeItem =>
            val res = typeItem.items map { item =>
                Map("url" -> item.url,
                    "title" -> item.title,
                    "pic" -> item.pic.getOrElse(""),
                    "hotScore" -> item.hotScore.getOrElse(0))
            }
            val result: ListBuffer[Map[String, Any]] = new ListBuffer[Map[String, Any]]()
            val listRes = res.toList
            val (max, second) = highlight(listRes)
            (0 until listRes.size).foreach { i =>
                var rItem = listRes(i).-("hotScore")
                if (i == max || i == second) {
                    rItem = rItem.+(("mark", true))
                }
                result += rItem
            }
            new RequestParam(typeItem.typeParam.order, "", typeItem.typeParam.recommendType.toString.toLowerCase, typeItem.typeParam.count, 0) -> result.toList
        }.toList
        (resultMap, rs.thumbnail)
    }

    private def highlight(recommendResult: List[Map[String, Any]]): (Int, Int) = {
        //找出hotScore值最大的RecommendItem
        var max = 0
        (1 until recommendResult.size).foreach { i =>
            if (recommendResult(i).get("hotScore").get.asInstanceOf[Double].compareTo(
                recommendResult(max).get("hotScore").get.asInstanceOf[Double]) > 0) {
                max = i
            }
        }
        //找出hotScore值第二大的RecommendItem
        var second = if (max == 0) 1 else 0
        (1 until recommendResult.size).foreach { i =>
            if (i != max && recommendResult(i).get("hotScore").get.asInstanceOf[Double].compareTo(
                recommendResult(second).get("hotScore").get.asInstanceOf[Double]) > 0) {
                second = i
            }
        }
        (max, second)
    }

    def getRecommendFuture(userId: String,
        url: String,
        types: List[RecommendTypeParam],
        canonicalUrl: String,
        title: String,
        keywords: String,
        sitePrefix: String,
        customThumbnail: Option[String],
        customTitle: Option[String]): Future[RecommendResult] = {
        log.debug("Send recommend: [userId=" + userId + "url=" + url + ", title=" + title + ", sitePrefix=" + sitePrefix + ",types=" + types + "]")

        val customThumbnailUrl = if (customThumbnail.isDefined && UrlFilter.isValidImgUrl(customThumbnail.get)) customThumbnail else None
        val param = RecommendParam.apply(url, types, Some(title), Some(sitePrefix), userId, Some(keywords), Some(canonicalUrl), customThumbnail, customTitle)
        client.recommend(param) onFailure {
            case ex: Exception =>
                log.error(ex, "Send recommend: [userId=" + userId + ", url=" + url + ", title=" + title
                    + ", sitePrefix=" + sitePrefix + ",type=" + types + "] but Failed to get recommend from backend.")
        }
    }

    def click(userId: String,
        toUrl: String,
        fromUrl: String,
        typeStr: String,
        sitePrefix: String) {
        log.debug("Send click: [userId=" + userId + "from=" + fromUrl + ", to=" + toUrl + ", type=" + typeStr
            + ", sitePrefix=" + sitePrefix + "]")

        val param = ClickParam.apply(toUrl, fromUrl, RecommendType.valueOf(typeStr), Some(sitePrefix), userId)
        client.click(param) onFailure {
            case ex: Exception =>
                log.error(ex, "Send click: [userId=" + userId + "from=" + fromUrl + ", to=" + toUrl + ", type=" + typeStr
                    + ", sitePrefix=" + sitePrefix + "] but Failed to send click to backend.")
        }
    }

    def recrawl(url: String) {
        log.debug("Send recrawl for the url " + url)
        client.recrawl(url) onFailure {
            case ex: Exception =>
                log.error(ex, "Send url:" + url + "to recrawl but Failed to send recrawl to backend.")
        }
    }

    def correctImg(url: String, imageUrl: String, userAgent: String) {
        log.debug("Send img correct for the url " + url)
        client.correctImg(url, imageUrl, userAgent) onFailure {
            case ex: Exception =>
                log.error(ex, "Send url:" + url + ", imageUrl:" + imageUrl + "to correct but Failed to send correct to backend.")
        }
    }
}