package com.buzzinate.lezhi.plan.plugin

import java.util.UUID

import scala.annotation.serializable

import org.apache.commons.lang.StringUtils

import com.buzzinate.common.util.string.UrlUtil
import com.buzzinate.lezhi.bean.RequestParam
import com.buzzinate.lezhi.model.SiteConfig
import com.buzzinate.lezhi.plugin.PluginConstant.{ adClient, getMatchPic, getRecommendParams, getRequestParams, getTypes, getVCookie, getXID, join, recommendClient }
import com.buzzinate.lezhi.thrift.PicType
import com.buzzinate.lezhi.util.{ Logging, UrlFilter }
import com.codahale.jerkson.Json

import unfiltered.netty._
import unfiltered.request._
import unfiltered.request.QParams._
import unfiltered.response._

/**
 * @author jeffrey.ji <jeffrey.ji@buzzinate.com>
 * May 21, 2013 3:38:23 PM
 *
 */
class TracePlan extends NettyPlan with Logging {

    def intent = {
        case req @ GET(Path("/trace/feeds") & Params(params) & Cookies(cookies)) =>
            val expected = for {
                uuidStr <- lookup("uuid") is trimmed
                urlOrg <- lookup("url") is trimmed is
                    required("missing param") is
                    nonempty("empty param")
                canonicalUrl <- lookup("canonicalUrl") is trimmed
                sitePrefixParam <- lookup("sitePrefix") is trimmed
                callback <- lookup("callback") is trimmed
                title <- lookup("title") is trimmed
                keywords <- lookup("keywords") is trimmed
                referrer <- lookup("referrer") is trimmed
                hash <- lookup("hash") is trimmed
                customThumbnail <- lookup("customThumbnail") is trimmed
                customTitle <- lookup("customTitle") is trimmed

                defaultPicUrl <- lookup("defaultPic") is trimmed
                adCountStr <- lookup("adCount") is trimmed

                typeStrParam <- lookup("type") is trimmed
                countStr <- lookup("count") is trimmed
                typeStrs <- lookup("types") is trimmed
            } yield {
                // filter unwanted params and unify url
                val url = UrlFilter.getChineseEncodedUrl(UrlFilter.filter(urlOrg.get)).toLowerCase

                val uuid = try {
                    Some(UUID.fromString(uuidStr.getOrElse("")))
                } catch {
                    case ex: IllegalArgumentException =>
                        None
                }
                // use params as default; will be overrited if has uuid
                var typeStr = typeStrParam.getOrElse("")
                var sitePrefix = sitePrefixParam.getOrElse("")
                var count = countStr.map(_.toInt).getOrElse(0)
                var adCount = adCountStr.map(_.toInt).getOrElse(0)
                var autoMatch = SiteConfig.DEFAULT_AUTO_MATCH
                var needPic = SiteConfig.DEFAULT_PIC
                var picMatch = SiteConfig.DEFAULT_PIC_MATCH
                var defaultPic = defaultPicUrl.getOrElse(SiteConfig.DEFAULT_DEFAULT_PIC)

                val feedsObj = scala.collection.mutable.Map[String, Any]()
                // get config for this uuid
                if (uuid.isDefined) {
                    SiteConfig.get(uuid.get.toString) match {
                        case Some(c: SiteConfig) =>
                            feedsObj.put("config", SiteConfig.getConfigObj(c))
                            defaultPic = c.defaultPic
                            autoMatch = c.autoMatch
                            picMatch = c.picMatch
                            needPic = c.pic
                            // overrite the params
                            // If type is empty or insite (i.e. default type), then we'll use the config from this 
                            // uuid. Otherwise, do not override (e.g. CNZZ, ChinaNews etc)
                            if (StringUtils.isEmpty(typeStr) || typeStr == "insite") {
                                typeStr = c.source
                            }
                            if (StringUtils.isEmpty(sitePrefix)) {
                                sitePrefix = c.sitePrefix
                            }
                            if (adCount == 0 && c.adEnabled) {
                                adCount = c.adCount
                            }
                            // If not count in param, use col * row as count
                            if (count == 0) {
                                count = c.col * c.row
                            }

                        case _ => log.debug("Cannot get SiteConfig for uuid: " + uuidStr.getOrElse(""))
                    }
                }
                // Still no count, use default 10
                if (count == 0)
                    count = SiteConfig.DEFAULT_COL * SiteConfig.DEFAULT_ROW

                // if no sitePrefix, use host of url
                if (StringUtils.isEmpty(sitePrefix)) {
                    sitePrefix = UrlUtil.getFullDomainNameWithHttpPrefix(url)
                }
                val (cookieId, isNewCreated) = getVCookie(cookies, req)
                val xid = getXID(cookieId)

                var res: (List[(RequestParam, List[Map[String, Any]])], String) = (Nil, "")
                var adRes: List[Map[String, Any]] = Nil

                var typeList = if (StringUtils.isNotBlank(typeStr)) getTypes(typeStr, "insite") else Nil
                var matchPic = getMatchPic(needPic, autoMatch, picMatch)
                if (typeList.contains("personalized")) {
                    matchPic = PicType.Provided
                }
                val requestParams = getRequestParams(adCount, typeStrs, matchPic, count, typeList)
                val (totalAdCount, types) = getRecommendParams(adCount, typeStrs, matchPic, count, typeList)
                if (totalAdCount > 0) {
                    //使用Future.join使广告和推荐的调用并行化
                    val combineResult = join(
                        recommendClient.getRecommendFuture(xid, url, types.toList, canonicalUrl.getOrElse(""), title.getOrElse(url), keywords.getOrElse(""), sitePrefix, customThumbnail, customTitle),
                        adClient.getServeFuture(xid, uuidStr.getOrElse(""), url, UrlFilter.getClientIp(req), title.getOrElse(url), keywords.getOrElse(""), needPic, totalAdCount))
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

                val adParams = Map("url" -> url, "uuid" -> uuidStr.getOrElse(""), "needPic" -> needPic, "adCount" -> totalAdCount)
                feedsObj.put("adParams", adParams)
                feedsObj.put("adResult", adRes)
                feedsObj.put("recommendParams", requestParams.toList.toString)
                val fResult = res._1.map {
                    case (param, result) =>
                        param.recommendType + ":" + param.order -> result
                }
                feedsObj.put("recommendResult", fResult)

                val responseString = if (callback.getOrElse("").isEmpty)
                    Json.generate[scala.collection.mutable.Map[String, Any]](feedsObj)
                else
                    callback.get + "(" + Json.generate[scala.collection.mutable.Map[String, Any]](feedsObj) + ")"

                Ok ~> CacheControl("no-cache") ~> JsonContent ~> ResponseString(responseString)
            }

            expected(params) orFail { fails =>
                CacheControl("no-cache") ~> BadRequest ~> ResponseString("400: "
                    + (fails.map { f => f.name + ": " + f.error } mkString ("; ")))
            }
    }
}