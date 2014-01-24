package com.buzzinate.lezhi.plan.plugin

import java.util.{ Date, UUID }

import scala.Option.option2Iterable
import scala.annotation.serializable

import org.apache.commons.lang.{ NumberUtils, StringUtils }

import com.buzzinate.common.util.serialize.HessianSerializer
import com.buzzinate.common.util.string.UrlUtil
import com.buzzinate.lezhi.Config
import com.buzzinate.lezhi.bean.{ AdvertiseClickMessage, AdvertisePageViewMessage, AdvertiseViewMessage, ClickMessage, GenomeClickMessage, GenomeViewMessage, InClickMessage, OutClickMessage, ShowupMessage, ViewMessage }
import com.buzzinate.lezhi.model.SiteConfig
import com.buzzinate.lezhi.plugin.PluginConstant.{ COOKIE_EXPIRE_TIME, INCLICK_HASH_PREFIX, LEZHI, MOCK_COOKIE_NAME, P3P, PLUGIN_COOKIE_DOMAIN, adsActor, clickRouter, genomeActor, getVCookie, getXID, isBxCookie, kestrelClient, recommendClient }
import com.buzzinate.lezhi.service.RecommendService
import com.buzzinate.lezhi.util.{ Logging, MetricsUtil, UrlFilter }
import com.codahale.jerkson.Json
import com.yammer.metrics.MetricRegistry

import akka.actor.actorRef2Scala
import unfiltered.Cookie
import unfiltered.netty._
import unfiltered.request._
import unfiltered.request.QParams._
import unfiltered.response._

trait NettyPlan extends cycle.Plan with cycle.ThreadPool with ServerErrorResponse

class PluginPlan extends NettyPlan with Logging {
    private val timer = MetricsUtil.metrics.timer(MetricRegistry.name(classOf[PluginPlan], "feeds"))
    private val meter = MetricsUtil.metrics.meter(MetricRegistry.name(classOf[PluginPlan], "feedsMeter", "requests"))
    def intent = {
        case req @ GET(Path("/plugin/feeds") & Params(params) & Cookies(cookies)) =>
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
                mock <- lookup("mock") is trimmed
                customThumbnail <- lookup("customThumbnail") is trimmed
                customTitle <- lookup("customTitle") is trimmed

                defaultPicUrl <- lookup("defaultPic") is trimmed
                adCountStr <- lookup("adCount") is trimmed

                typeStrParam <- lookup("type") is trimmed
                countStr <- lookup("count") is trimmed
                typeStrs <- lookup("types") is trimmed
            } yield {
                meter.mark
                val context = timer.time
                try {
                    // filter unwanted params and unify url
                    val url = UrlFilter.getChineseEncodedUrl(UrlFilter.filter(urlOrg.get)).toLowerCase

                    val uuid = try {
                        Some(UUID.fromString(uuidStr.getOrElse("")))
                    } catch {
                        case ex: IllegalArgumentException =>
                            None
                    }

                    // save the stats asynchronously
                    kestrelClient.put(LEZHI, HessianSerializer.serialize(ViewMessage(uuid, url, new Date)))
                    if (StringUtils.isNotEmpty(hash.getOrElse("")) && hash.get.startsWith(INCLICK_HASH_PREFIX)) {
                        kestrelClient.put(LEZHI, HessianSerializer.serialize(InClickMessage(uuid, url, title.getOrElse(url), new Date)))
                    }
                    // use params as default; will be overwrited if has uuid
                    var typeStr = typeStrParam.getOrElse("")
                    var sitePrefix = sitePrefixParam.getOrElse("")
                    var count = countStr.map(_.toInt).getOrElse(0)
                    var adCount = adCountStr.map(_.toInt).getOrElse(0)
                    var defaultPic = defaultPicUrl.getOrElse(SiteConfig.DEFAULT_DEFAULT_PIC)
                    var autoMatch = SiteConfig.DEFAULT_AUTO_MATCH
                    var needPic = SiteConfig.DEFAULT_PIC
                    var picMatch = SiteConfig.DEFAULT_PIC_MATCH

                    var feedsObj = scala.collection.mutable.Map[String, Any]()
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
                    val ip = UrlFilter.getClientIp(req)
                    val (cookieId, isNewCreated) = getVCookie(cookies, req)
                    val xid = getXID(cookieId)
                    val (feeds, thumbnail) = RecommendService.recommend(url, xid, needPic, autoMatch, picMatch, typeStr, uuidStr, canonicalUrl, title, keywords,
                        customThumbnail, customTitle, sitePrefix, defaultPic, ip, adCount, count, typeStrs, isMock(mock, cookies))
                    feedsObj ++= feeds
                    //put into kestrel for genome statistics
                    genomeActor ! GenomeViewMessage(typeStr, xid, url, referrer.getOrElse(""), uuidStr.getOrElse(""), UrlFilter.getClientIp(req), getUserAgent(req), new Date)

                    val bx = isBxCookie(isNewCreated, cookies)
                    feedsObj.put("bx", bx)
                    feedsObj.put("vid", xid)
                    feedsObj.put("thumbnail", thumbnail)

                    val responseString = if (callback.getOrElse("").isEmpty)
                        Json.generate[scala.collection.mutable.Map[String, Any]](feedsObj)
                    else
                        callback.get + "(" + Json.generate[scala.collection.mutable.Map[String, Any]](feedsObj) + ")"

                    val responder = if (bx) {
                        Ok ~> SetCookies(Cookie("t", System.currentTimeMillis.toString,
                            Option(PLUGIN_COOKIE_DOMAIN), Option("/"),
                            Option(COOKIE_EXPIRE_TIME)))
                    } else Ok

                    responder ~>
                        SetCookies(Cookie("v", cookieId, Option(PLUGIN_COOKIE_DOMAIN),
                            Option("/"), Option(COOKIE_EXPIRE_TIME))) ~>
                        ResponseHeader("P3P",
                            List("CP=\"IDC DSP COR ADM DEVi TAIi PSA PSD IVAi IVDi CONi HIS OUR IND CNT\"")) ~>
                            CacheControl("no-cache") ~> JsonContent ~> ResponseString(responseString)
                } finally {
                    context.stop();
                }
            }

            expected(params) orFail { fails =>
                CacheControl("no-cache") ~> BadRequest ~> ResponseString("400: "
                    + (fails.map { f => f.name + ": " + f.error } mkString ("; ")))
            }

        case req @ GET(Path("/plugin/redirect") & Params(params) & Cookies(cookies)) =>
            val expected = for {
                uuidStr <- lookup("uuid") is trimmed
                title <- lookup("title") is trimmed
                orgToUrl <- lookup("to") is trimmed is
                    required("missing param") is
                    nonempty("empty param")
                orgFromUrl <- lookup("from") is trimmed is
                    required("missing param") is
                    nonempty("empty param")
                typeStr <- lookup("ref") is trimmed is
                    required("missing param") is
                    nonempty("empty param")
                pluginType <- lookup("type") is trimmed is
                    required("missing param") is
                    nonempty("empty param")
                pic <- lookup("pic") is trimmed is
                    required("missing param") is
                    nonempty("empty param")
                sitePrefix <- lookup("sitePrefix") is trimmed
                adEntryId <- lookup("adEntryId") is trimmed
            } yield {
                meter.mark
                val context = timer.time()
                try {
                    // filter unwanted params and unify url
                    val toUrl = UrlFilter.getChineseEncodedUrl(UrlFilter.filter(orgToUrl.get)).toLowerCase
                    val fromUrl = UrlFilter.getChineseEncodedUrl(UrlFilter.filter(orgFromUrl.get)).toLowerCase
                    val uuid = try {
                        Some(UUID.fromString(uuidStr.getOrElse("")))
                    } catch {
                        case ex: IllegalArgumentException => None
                    }

                    // save the stats asynchronously
                    kestrelClient.put(LEZHI, HessianSerializer.serialize(OutClickMessage(uuid, fromUrl, title.getOrElse(fromUrl), typeStr.get, pluginType.get, pic.get, new Date)))
                    // if sitePrefix is null or empty, use host of url
                    val theSitePrefix = if (StringUtils.isNotEmpty(sitePrefix.getOrElse(""))) {
                        sitePrefix.get
                    } else
                        UrlUtil.getFullDomainNameWithHttpPrefix(fromUrl)
                    val xid = getXID(getVCookie(cookies, req)._1)
                    clickRouter ! ClickMessage(xid, toUrl, fromUrl, typeStr.get, theSitePrefix)

                    //put into kestrel for genome statistics
                    genomeActor ! GenomeClickMessage(typeStr.get, xid, fromUrl, toUrl, uuidStr.getOrElse(""), UrlFilter.getClientIp(req), getUserAgent(req), new Date)

                    //put into kestrel for ad click statistics 
                    if (adEntryId.nonEmpty && NumberUtils.isDigits(adEntryId.get)) {
                        adsActor ! AdvertiseClickMessage(adEntryId.get, fromUrl, uuidStr.getOrElse(""), xid, UrlFilter.getClientIp(req), getUserAgent(req))
                    }

                    // add a hash tag so we can get in clicks when user views the page
                    val redirectUrl = toUrl + "#" + INCLICK_HASH_PREFIX + typeStr.get
                    var redirectMode = SiteConfig.DEFAULT_REDIRECT_MODE
                    if (uuid.nonEmpty && SiteConfig.get(uuid.get.toString).nonEmpty) {
                        redirectMode = SiteConfig.get(uuid.get.toString).get.redirectMode
                    }
                    if (redirectMode == SiteConfig.DEFAULT_REDIRECT_MODE) {
                        val location = "'" + redirectUrl + "'"
                        val html = <res><html><head><script>window.location.href={ scala.xml.Unparsed(location) }</script><noscript><meta http-equiv="refresh" content="0"/></noscript></head></html></res>
                        Ok ~> CacheControl("no-cache") ~> JsonContent ~> Html(html \ "html")
                    } else {
                        CacheControl("no-cache") ~> JsonContent ~> Found ~> Location(redirectUrl)
                    }
                } finally {
                    context.stop();
                }
            }
            expected(params) orFail { fails =>
                BadRequest ~> ResponseString("400: "
                    + (fails.map { f => f.name + ": " + f.error } mkString ("; ")))
            }

        case req @ GET(Path("/plugin/showup") & Params(params) & Cookies(cookies)) =>
            val expected = for {
                uuidStr <- lookup("uuid") is trimmed
                urlStr <- lookup("url") is trimmed is
                    required("missing param") is
                    nonempty("empty param")
                typeStr <- lookup("ref") is trimmed is
                    required("missing param") is
                    nonempty("empty param")
                pluginType <- lookup("type") is trimmed is
                    required("missing param") is
                    nonempty("empty param")
                pic <- lookup("pic") is trimmed is
                    required("missing param") is
                    nonempty("empty param")
            } yield {
                meter.mark
                val context = timer.time()
                try {
                    val url = UrlFilter.getChineseEncodedUrl(UrlFilter.filter(urlStr.get)).toLowerCase
                    val uuid = try {
                        Some(UUID.fromString(uuidStr.getOrElse("")))
                    } catch {
                        case ex: IllegalArgumentException => None
                    }
                    log.debug("Get showup from: " + url)
                    // save the showups
                    kestrelClient.put(LEZHI, HessianSerializer.serialize(ShowupMessage(uuid, url, typeStr.get, pluginType.get, pic.get, new Date)))
                    Ok
                } finally {
                    context.stop();
                }
            }

            expected(params) orFail { fails =>
                BadRequest ~> ResponseString("400: "
                    + (fails.map { f => f.name + ": " + f.error } mkString ("; ")))
            }

        case req @ GET(Path("/plugin/adShowup") & Params(params) & Cookies(cookies)) =>
            val expected = for {
                uuidStr <- lookup("uuid") is trimmed
                urlStr <- lookup("url") is trimmed is
                    required("missing param") is
                    nonempty("empty param")
                adEntryIds <- lookup("adEntryIds") is trimmed
            } yield {
                meter.mark
                val context = timer.time()
                try {
                    val url = UrlFilter.getChineseEncodedUrl(UrlFilter.filter(urlStr.get)).toLowerCase
                    //put into kestrel for ad showup statistics 
                    if (adEntryIds.nonEmpty) {
                        adsActor ! AdvertisePageViewMessage(url, uuidStr.getOrElse(""), getXID(getVCookie(cookies, req)._1), UrlFilter.getClientIp(req), getUserAgent(req))
                        adsActor ! AdvertiseViewMessage(adEntryIds.get, url, uuidStr.getOrElse(""), getXID(getVCookie(cookies, req)._1), UrlFilter.getClientIp(req), getUserAgent(req))
                    }
                    Ok
                } finally {
                    context.stop();
                }
            }

            expected(params) orFail { fails =>
                BadRequest ~> ResponseString("400: "
                    + (fails.map { f => f.name + ": " + f.error } mkString ("; ")))
            }

        case req @ GET(Path("/plugin/correct") & Params(params) & Cookies(cookies)) =>
            val expected = for {
                urlStr <- lookup("url") is trimmed is
                    required("missing param") is
                    nonempty("empty param")
                thumbnail <- lookup("thumbnail") is trimmed is
                    required("missing param") is
                    nonempty("empty param")
            } yield {
                recommendClient.correctImg(UrlFilter.getChineseEncodedUrl(UrlFilter.filter(urlStr.get).toLowerCase), thumbnail.get, getUserAgent(req))
                Ok
            }

            expected(params) orFail { fails =>
                BadRequest ~> ResponseString("400: "
                    + (fails.map { f => f.name + ": " + f.error } mkString ("; ")))
            }

        case GET(Path("/plugin/monitor")) =>
            Ok ~> CacheControl("no-cache") ~> ResponseString("Response!")

        case GET(Path("/mock/set")) =>
            P3P.apply("CP=\"IDC DSP COR ADM DEVi TAIi PSA PSD IVAi IVDi CONi HIS OUR IND CNT\"") ~>
                SetCookies(unfiltered.Cookie(MOCK_COOKIE_NAME, "1", Some(PLUGIN_COOKIE_DOMAIN), Some("/"), Some(COOKIE_EXPIRE_TIME))) ~>
                CacheControl("no-cache") ~>
                Ok ~> ResponseString("set mock ok!")

        case GET(Path("/mock/clear")) =>
            SetCookies(unfiltered.Cookie(MOCK_COOKIE_NAME, "", Some(PLUGIN_COOKIE_DOMAIN), Some("/"), Some(0))) ~>
                Ok ~> ResponseString("clear mock ok!")
    }

    private def getUserAgent(req: HttpRequest[ReceivedMessage]): String = {
        if (req.headers("User-Agent").length != 0)
            req.headers("User-Agent").next
        else ""
    }

    /**
     * recommend/ads mock or not
     *
     * @param mock
     * @param cookies
     * @return if mock param = true or (mock cookie exist && backend.mock = true), return true; else return false
     */
    private def isMock(mock: Option[String], cookies: Map[String, Option[Cookie]]): Boolean = {
        (mock.getOrElse("") == "true") || (Config.getBool("backend.mock") && getMockCookie(cookies))
    }

    private def getMockCookie(cookies: Map[String, Option[Cookie]]): Boolean = {
        cookies(MOCK_COOKIE_NAME) match {
            case Some(c: Cookie) => true
            case _ => false
        }
    }
}
