package com.buzzinate.lezhi.service

import scala.annotation.serializable
import scala.collection.mutable.ListBuffer

import org.apache.commons.lang.StringUtils

import com.buzzinate.lezhi.bean.RequestParam
import com.buzzinate.lezhi.db.JedisClient
import com.buzzinate.lezhi.plugin.PluginConstant.{ adClient, adClientMock, getTypes, join, recommendClient, recommendClientMock, getRequestParams, getRecommendParams, getMatchPic, getAdCount }
import com.buzzinate.lezhi.thrift.{ PicType, RecommendType, RecommendTypeParam }
import com.buzzinate.lezhi.util.{ Logging, Util }
import com.buzzinate.lezhi.Config

/**
 * @author jeffrey.ji <jeffrey.ji@buzzinate.com>
 * Jul 26, 2013 5:27:57 PM
 *
 */
object RecommendService extends Logging {
    private val CACHE_PREFIX = "PLUGIN:"
    private val RECOMMEND_EXPIRE_TIME = Config.getInt("recommend.cache.time")

    def recommend(url: String, xid: String, needPic: Boolean, autoMatch: Boolean,
        picMatch: String, typeStr: String, uuidStr: Option[String], canonicalUrl: Option[String], title: Option[String], keywords: Option[String],
        customThumbnail: Option[String], customTitle: Option[String],
        sitePrefix: String, defaultPic: String, ip: String,
        adCountTmp: Int, count: Int, typeStrs: Option[String], mock: Boolean): (scala.collection.mutable.Map[String, Any], String) = {

        var typeList = if (StringUtils.isNotBlank(typeStr)) getTypes(typeStr, "insite") else Nil
        var matchPic = getMatchPic(needPic, autoMatch, picMatch)
        if (typeList.contains("personalized")) {
            matchPic = PicType.Provided
        }
        val requestParams = getRequestParams(adCountTmp, typeStrs, matchPic, count, typeList)
        val cacheKey = getCacheKey(url, uuidStr, canonicalUrl, title, keywords, customThumbnail, customTitle, defaultPic, needPic, autoMatch, picMatch, count, typeStr, typeStrs, mock)

        var res: (List[(RequestParam, List[Map[String, Any]])], String) = (Nil, "")
        var adRes: List[Map[String, Any]] = Nil
        res = JedisClient.getObject(cacheKey).asInstanceOf[(List[(RequestParam, List[Map[String, Any]])], String)]
        if (res == null) {
            val (adCount, types) = getRecommendParams(adCountTmp, typeStrs, matchPic, count, typeList)
            if (mock) {
                res = recommendClientMock.recommend(xid, url, types.toList, canonicalUrl.getOrElse(""), title.getOrElse(url), keywords.getOrElse(""), sitePrefix, customThumbnail, customTitle)
                if (adCount > 0) {
                    adRes = adClientMock.serve(xid, uuidStr.getOrElse(""), url, ip, title.getOrElse(url), keywords.getOrElse(""), needPic, adCount)
                }
            } else {
                if (adCount > 0) {
                    //使用Future.join使广告和推荐的调用并行化
                    val combineResult = join(
                        recommendClient.getRecommendFuture(xid, url, types.toList, canonicalUrl.getOrElse(""), title.getOrElse(url), keywords.getOrElse(""), sitePrefix, customThumbnail, customTitle),
                        adClient.getServeFuture(xid, uuidStr.getOrElse(""), url, ip, title.getOrElse(url), keywords.getOrElse(""), needPic, adCount))
                    try {
                        val (tmpRes, tmpAdRes) = combineResult.get
                        res = recommendClient.getRecommendResult(tmpRes)
                        adRes = adClient.getServeResult(tmpAdRes)
                    } catch {
                        case ex: Exception =>
                            log.error(ex, "Failed to get recommend or ads result from finagle client")
                    }
                } else {
                    try {
                        res = recommendClient.recommend(xid, url, types.toList, canonicalUrl.getOrElse(""), title.getOrElse(url), keywords.getOrElse(""),
                            sitePrefix, customThumbnail, customTitle)
                    } catch {
                        case ex: Exception =>
                            log.error(ex, "Failed to get recommend result from finagle client")
                    }
                }
            }
            if (isNeedCached(typeList, res, mock)) {
                JedisClient.set(cacheKey, RECOMMEND_EXPIRE_TIME, res)
            }
        } else {
            val adCount = getAdCount(adCountTmp, typeStrs, typeList)
            if (adCount > 0) {
                try {
                    adRes = adClient.serve(xid, uuidStr.getOrElse(""), url, ip, title.getOrElse(url), keywords.getOrElse(""), needPic, adCount)
                } catch {
                    case ex: Exception =>
                        log.error(ex, "Failed to get advertise result from finagle client")
                }
            }
        }

        val rAdRes = adRes.map { item =>
            val u = item.get("url").getOrElse("").toString
            val adEntryId = item.get("adEntryId").getOrElse(0).toString
            item.+(("url", u.replace("{uuid}", uuidStr.getOrElse("")).replace("{product}", "LEZHI").replace("{adEntryId}", adEntryId)))
        }.toList
        val (result, thumbnail) = res

        val feedsObj = processFeeds(requestParams, result, rAdRes, autoMatch, defaultPic)

        return (feedsObj, thumbnail)
    }

    private def processFeeds(requestParams: List[RequestParam], res: List[(RequestParam, List[Map[String, Any]])],
        adRes: List[Map[String, Any]], autoMatch: Boolean, defaultPic: String): scala.collection.mutable.Map[String, Any] = {
        val feedsObj = scala.collection.mutable.Map[String, Any]()

        var start: Int = 0;
        var rParams = requestParams
        var rRes = res
        rParams.foreach { param =>
            rRes.foreach {
                case (requestParam, recommendResult) => {
                    if (requestParam.order == param.order && requestParam.recommendType == param.recommendType && requestParam.count == param.count) {
                        val fResult = new ListBuffer[Map[String, Any]]()
                        if (recommendResult.size > 0) {
                            val result = recommendResult.map { item =>
                                var rItem = item
                                if (!autoMatch && rItem.get("pic").get == "" && defaultPic != "") {
                                    rItem = rItem.+(("pic", defaultPic))
                                }
                                rItem
                            }
                            if (param.adCount > 0 && start < adRes.size) {
                                val adResResult = adRes.slice(start, start + param.adCount)
                                fResult ++= result.slice(0, param.count - adResResult.size) ++ adResResult
                                start += param.adCount
                            } else {
                                fResult ++= result
                            }
                        }
                        if (param.recommendType == "personalized") {
                            if (fResult.size < param.count && rRes.size > requestParam.order) {
                                fResult ++= rRes(requestParam.order)._2.slice(0, param.count - fResult.size)
                            }
                            rParams = rParams.filterNot { rParam => rParam.order == requestParam.order + 1 && rParam.recommendType == "trending" }
                            rRes = rRes.filterNot { rResult => rResult._1.order == requestParam.order + 1 && rResult._1.recommendType == "trending" }
                        }
                        val key = if (param.style == "old") param.recommendType else param.recommendType + ":" + param.order
                        feedsObj.put(key, fResult.toList)
                        rParams = rParams.-(param)
                        rRes = rRes.-((requestParam, recommendResult))
                    }
                }
            }
        }
        return feedsObj
    }

    private def getCacheKey(url: String, uuidStr: Option[String], canonicalUrl: Option[String], title: Option[String], keywords: Option[String],
        customThumbnail: Option[String], customTitle: Option[String], defaultPic: String, needPic: Boolean, autoMatch: Boolean,
        picMatch: String, count: Int, typeStr: String, typeStrs: Option[String], mock: Boolean): String = {
        val key = new StringBuilder("url:" + url)
        key.append(",uuidStr:" + uuidStr)
        key.append(",canonicalUrl:" + canonicalUrl)
        key.append(",title:" + title)
        key.append(",keywords:" + keywords)
        key.append(",customThumbnail:" + customThumbnail)
        key.append(",customTitle:" + customTitle)
        key.append(",defaultPic:" + defaultPic)
        key.append(",needPic:" + needPic)
        key.append(",autoMatch:" + autoMatch)
        key.append(",type:" + typeStr)
        key.append(",picMatch:" + picMatch)
        key.append(",count:" + count)
        key.append(",typeStrs:" + typeStrs)

        return CACHE_PREFIX + Util.urlToUuid(key.toString);
    }

    /**
     * 1. recommend result is not blank
     * 2. recommend type has no personalized
     * 3. is not mock
     */
    private def isNeedCached(typeList: List[String], res: (List[(com.buzzinate.lezhi.bean.RequestParam, List[Map[String, Any]])], String), mock: Boolean): Boolean = {
        if (!typeList.contains("personalized") && !mock && res != null) {
            res._1.foreach {
                case (param, result) =>
                    if (result.size > 0) return true
            }
        }
        false
    }
}