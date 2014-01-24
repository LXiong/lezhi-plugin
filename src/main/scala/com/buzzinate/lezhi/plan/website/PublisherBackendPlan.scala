package com.buzzinate.lezhi.plan.website

import java.util.{ Date, UUID }

import scala.collection.mutable.{ HashMap, ListBuffer }

import com.buzzinate.common.util.{ ApiUtil, DateTimeUtil }
import com.buzzinate.common.util.string.UrlUtil
import com.buzzinate.lezhi.bean.{ Paginate, SiteAds }
import com.buzzinate.lezhi.model.{ SiteConfig, SitePage, SiteStatus, StatSite }
import com.buzzinate.lezhi.plan.BasicPlan
import com.buzzinate.lezhi.service.{ PageStatsService, UuidSiteService }
import com.buzzinate.lezhi.util.{ Logging, _ }
import com.buzzinate.lezhi.util.ValidateUtil._
import com.buzzinate.user.vo.UuidSite

import unfiltered.scalate.Scalate
import unfiltered.request._
import unfiltered.response._
import unfiltered.request.QParams._
import unfiltered.request.Params

class PublisherBackendPlan extends BasicPlan {
    val iframeExpire = 15 * 60 * 1000
    def intent = {
        case req @ GET(Path("/user/manage") & Params(params)) =>
            if (!isPublisher(req)) {
                Unauthorized ~> Scalate(req, "401.ssp")
            } else {
                var err = (-1, MSG_SITE_CONFIG_SUCCESS_LOAD)
                val userId = getUserId(req)
                val remainingWebsiteNum = getMaxWebsiteNumCanCreated(req) - UuidSiteService.getUuidSiteCount(userId)
                val userSites = UuidSiteService.getUuidSiteByUserId(userId)
                val siteAdsListBuffer = ListBuffer[SiteAds]()
                var adEnabled: Boolean = false
                //val status = contactInfo.get.getStatus().getCode()

                userSites.foreach(
                    site => {
                        val siteConfig = SiteConfig.get(site.getUuid)
                        if (siteConfig.nonEmpty) {
                            val siteAds = new SiteAds(site.getName, site.getUuid, site.getUrl, siteConfig.get.adCount, siteConfig.get.adEnabled, true)
                            siteAds.pluginType = siteConfig.get.pluginType
                            siteAds.pic = siteConfig.get.pic
                            siteAdsListBuffer.append(siteAds)
                            if (siteConfig.get.adEnabled) {
                                adEnabled = true
                            }
                        } else {
                            val siteAds = new SiteAds(site.getName, site.getUuid, site.getUrl, SiteConfig.DEFAULT_AD_COUNT, SiteConfig.DEFAULT_AD_ENABLED, false)
                            siteAds.pluginType = "fixed"
                            siteAds.pic = true
                            siteAdsListBuffer.append(siteAds)
                        }
                    })
                Ok ~> Scalate(req, "user/manage.ssp", ("adEnabled", adEnabled), ("remainingWebsiteNum", remainingWebsiteNum), ("err", err), ("userSites", siteAdsListBuffer.toList), ("link", "manage"))
            }

        case req @ GET(Path("/user/website/update") & Params(params)) => {
            if (!isPublisher(req)) {
                Unauthorized ~> Scalate(req, "401.ssp")
            } else {
                val expected = for {
                    uuid <- lookup("uuid") is trimmed is
                        required("未指定参数") is
                        nonempty("参数不能为空")
                    name <- lookup("name") is trimmed is
                        required("未指定参数") is
                        nonempty("参数不能为空")
                    url <- lookup("url") is trimmed is
                        required("未指定参数") is
                        nonempty("参数不能为空")
                } yield {
                    var err = (0, MSG_SITE_CONFIG_SUCCESS_UPDATE)
                    val uuidSite = UuidSiteService.getUuidSiteByUuid(uuid.get)
                    if (uuidSite != null) {
                        uuidSite.setName(name.get)
                        uuidSite.setUrl(url.get)
                        UuidSiteService.updateUuidSite(uuidSite)
                    } else {
                        err = (1, MSG_SITE_CONFIG_INVALID_UUID)
                    }
                    Ok ~> ResponseString("{\"result\":\"" + err._1 + "\", \"message\":\"" + err._2 + "\"}")
                }
                expected(params) orFail { fails =>
                    BadRequest ~> Scalate(req, "400.ssp", ("fails", fails))
                }
            }
        }

        case req @ GET(Path("/user/website/delete") & Params(params)) => {
            if (!isPublisher(req)) {
                Unauthorized ~> Scalate(req, "401.ssp")
            } else {
                val expected = for {
                    uuidStr <- lookup("uuid") is trimmed is
                        required("未指定参数") is
                        nonempty("参数不能为空")
                } yield {
                    var err = (0, MSG_SITE_CONFIG_SUCCESS_UPDATE)
                    try {
                        UuidSiteService.deleteUuidSite(getUserId(req), uuidStr.get)
                        SiteConfig.delete(uuidStr.get)
                    } catch {
                        case ex: Exception =>
                            log.error(ex, "Exception when delete uuid site by userId:" + getUserId(req) + ",and uuid:" + uuidStr.get)
                            err = (1, MSG_DELETE_UUID_SITE_FAILED)
                    }
                    Ok ~> ResponseString("{\"result\":\"" + err._1 + "\", \"message\":\"" + err._2 + "\"}")
                }
                expected(params) orFail { fails =>
                    BadRequest ~> Scalate(req, "400.ssp", ("fails", fails))
                }
            }
        }

        case req @ GET(Path("/user/code") & Params(params)) =>
            if (!isPublisher(req)) {
                Unauthorized ~> Scalate(req, "401.ssp")
            } else {
                val expected = for {
                    uuidStr <- lookup("uuid") is trimmed is
                        required("未指定参数") is
                        nonempty("参数不能为空")
                } yield {
                    var err = (-1, MSG_SITE_CONFIG_SUCCESS_LOAD)
                    // check if current is the owner of the site
                    try {
                        val uuid = UUID.fromString(uuidStr.get)
                        if (!SiteConfig.get(uuid.toString).isDefined) {
                            val siteConfig = new SiteConfig
                            siteConfig.uuid = uuid.toString
                            SiteConfig.update(siteConfig)
                        }
                    } catch {
                        case ex: IllegalArgumentException => err = (3, MSG_SITE_CONFIG_INVALID_UUID)
                        case ex: Exception =>
                            log.error(ex, "Exception when modify style. ")
                            err = (4, MSG_UNKNOWN_ERRPR)
                    }

                    Ok ~> Scalate(req, "user/code.ssp",
                        ("err", err), ("hasSite", UuidSiteService.getUuidSiteCount(getUserId(req)) > 0), ("uuid", uuidStr.get),
                        ("link", "code"))
                }
                expected(params) orFail { fails =>
                    BadRequest ~> Scalate(req, "400.ssp", ("fails", fails))
                }
            }

        case req @ GET(Path("/user/stats/pages") & Params(params)) =>
            if (!isPublisher(req)) {
                Unauthorized ~> Scalate(req, "401.ssp")
            } else {
                val expected = for {
                    uuidStr <- lookup("uuid") is trimmed is
                        required("未指定参数") is
                        nonempty("参数不能为空")
                    pagination <- lookup("pagination") is
                        int { "'" + _ + "'' is not an integer" }
                    interval <- lookup("interval") is
                        int { "'" + _ + "'' is not an integer" }
                    sortKey <- lookup("sortKey") is trimmed
                } yield {
                    val site = UuidSiteService.getUuidSiteByUuid(uuidStr.get)
                    if (site != null) {
                        var endDay = DateTimeUtil.getCurrentDateDay()
                        var startDay = DateTimeUtil.subtractDays(endDay, interval.getOrElse(0))
                        if (interval.getOrElse(0) > 0) {
                            endDay = DateTimeUtil.subtractDays(endDay, 1)
                            startDay = DateTimeUtil.subtractDays(endDay, interval.getOrElse(0) - 1)
                        }
                        val sortType = sortKey.getOrElse("clickToShowup")
                        val allSitePages = SitePage.get(UUID.fromString(uuidStr.get), startDay, DateTimeUtil.plusDays(endDay, 1))

                        //val allSitePages = UuidUrls.getUrls(userUuid) map {url => Util.urlToByte(url)}

                        //get the pageStats order by statType
                        //val pageStats = PageStatsService.getPageDatas(pagination.getOrElse(0), PAGE_SIZE, sortType, allSitePages, startDay, endDay)
                        val pageStats = PageStatsService.getPageDatasByUuid(uuidStr.get, startDay, endDay, new Paginate(pagination.getOrElse(1)), sortType)

                        Ok ~> Scalate(req, "user/stats/pages.ssp",
                            ("uuid", uuidStr.get),
                            ("siteName", site.getName),
                            ("pagestats", pageStats._2),
                            ("pagination", pagination.getOrElse(0)),
                            ("pageSize", PAGE_SIZE),
                            ("totalSize", pageStats._1),
                            ("sortKey", sortType),
                            ("link", "pages"))
                    } else {
                        Unauthorized ~> Scalate(req, "401.ssp")
                    }
                }

                expected(params) orFail { fails =>
                    BadRequest ~> Scalate(req, "400.ssp", ("fails", fails))
                }
            }

        case req @ GET(Path("/user/stats/dashboard") & Params(params)) =>
            if (!isPublisher(req)) {
                Unauthorized ~> Scalate(req, "401.ssp")
            } else {
                val expected = for {
                    uuidStr <- lookup("uuid") is trimmed is
                        required("未指定参数") is
                        nonempty("参数不能为空")
                } yield {
                    val site = UuidSiteService.getUuidSiteByUuid(uuidStr.get)
                    if (site != null) {
                        var endDay = DateTimeUtil.convertDate(getParam(params, "dateEnd"))
                        var startDay = DateTimeUtil.convertDate(getParam(params, "dateStart"))
                        if (endDay == null || startDay == null) {
                            endDay = DateTimeUtil.getCurrentDateDay()
                            startDay = DateTimeUtil.subtractDays(endDay, 3)
                        }
                        val urlCounts = SiteStatus.getUrlNumber(uuidStr.get)

                        val showups = StatSite.getSiteShowup(uuidStr.get, startDay, endDay)
                        val jsonDataShowups = OfcUtil.getOfc2ChartJsonData(showups, startDay, endDay, OfcUtil.ChartType.LINE,
                            false, null, null, true)
                        val clicks = StatSite.getSiteOutClick(uuidStr.get, startDay, endDay)
                        val jsonDataClicks = OfcUtil.getOfc2ChartJsonData(clicks, startDay, endDay, OfcUtil.ChartType.LINE,
                            false, null, null, true)

                        val clickToShowups = new HashMap[Date, Double]
                        for (showup <- showups) {
                            if (StatSite.getSiteOutClick(uuidStr.get, showup._1) > 0) {
                                clickToShowups.put(showup._1, showup._2)
                            }
                        }
                        for (click <- clicks) {
                            val date = click._1
                            val clickCount = click._2
                            val showupCount = clickToShowups.get(date)
                            if (showupCount.nonEmpty) {
                                if (clickCount == 0 || showupCount == 0) clickToShowups.put(date, 0)
                                else clickToShowups.put(date, (clickCount * 100 / showupCount.get))
                            }
                        }
                        val jsonDataClickToShowups = OfcUtil.getOfc2ChartJsonData(clickToShowups.toList.sortBy(_._1), startDay, endDay, OfcUtil.ChartType.LINE,
                            false, null, null, true)

                        Ok ~> Scalate(req, "user/stats/overall.ssp",
                            ("uuid", uuidStr.get),
                            ("siteName", site.getName),
                            ("urlCounts", urlCounts),
                            ("jsonDataShowups", jsonDataShowups),
                            ("jsonDataClicks", jsonDataClicks),
                            ("jsonDataClickToShowups", jsonDataClickToShowups),
                            ("dateStart", DateTimeUtil.formatDate(startDay)),
                            ("dateEnd", DateTimeUtil.formatDate(endDay)),
                            ("link", "dashboard"))
                    } else {
                        // no authorization for this site
                        Unauthorized ~> Scalate(req, "401.ssp")
                    }
                }
                expected(params) orFail { fails =>
                    BadRequest ~> Scalate(req, "400.ssp", ("fails", fails))
                }
            }

        case req @ GET(Path("/user/customize") & Params(params)) =>
            if (!isPublisher(req)) {
                Unauthorized ~> Scalate(req, "401.ssp")
            } else {
                val expected = for {
                    uuidStr <- lookup("uuid") is trimmed is
                        required("未指定参数") is
                        nonempty("参数不能为空")
                } yield {
                    var err = (-1, MSG_SITE_CONFIG_SUCCESS_LOAD)
                    var siteConfig = new SiteConfig
                    var site: UuidSite = null
                    try {
                        val uuid = UUID.fromString(uuidStr.get)
                        site = UuidSiteService.getUuidSiteByUuid(uuid.toString)
                        // check if current is the owner of the site
                        if (site != null) {
                            SiteConfig.get(uuid.toString) match {
                                case Some(sc) => siteConfig = sc
                                case _ => Pass
                            }
                        } else {
                            // no authorization for this site
                            err = (2, MSG_SITE_CONFIG_NO_SITE)
                        }
                    } catch {
                        // invalide uuid
                        case ex: IllegalArgumentException => err = (3, MSG_SITE_CONFIG_INVALID_UUID)
                        case ex: Exception =>
                            log.error(ex, "Exception when modify style. ")
                            err = (4, MSG_UNKNOWN_ERRPR)
                    }

                    // if the user requests different type or pic, change the current saved config for the front end
                    if (!getParam(params, "type").isEmpty()) {
                        siteConfig.pluginType = getParam(params, "type")
                    }
                    if (!getParam(params, "pic").isEmpty()) {
                        siteConfig.pic = (getParam(params, "pic") == "true")
                    }

                    val p = SiteConfig.toParamTuples(siteConfig)

                    Ok ~> Scalate(req, "installGeneral.ssp",
                        ("link", "userCustomize") :: ("count", siteConfig.col * siteConfig.row) :: ("err", err) :: ("site", site) :: ("hasSite", UuidSiteService.getUuidSiteCount(getUserId(req)) > 0) :: ("uuid", uuidStr.get) :: p: _*)
                }
                expected(params) orFail { fails =>
                    BadRequest ~> Scalate(req, "400.ssp", ("fails", fails))
                }
            }

        case req @ GET(Path("/user/customizeIframe") & Params(params)) =>
            val expected = for {
                uuidStr <- lookup("uuid") is trimmed is
                    required("未指定参数") is
                    nonempty("参数不能为空")
                ts <- lookup("ts") is
                    long { "'" + _ + "' 非长整型" }
                sig <- lookup("sig") is trimmed is
                    required("未指定参数") is
                    nonempty("参数不能为空")
            } yield {
                var err = (-1, MSG_SITE_CONFIG_SUCCESS_LOAD)
                val uuid = uuidStr.get
                val uuidSite = UuidSiteService.getUuidSiteByUuid(uuid)
                var siteConfig = new SiteConfig

                if (uuidSite == null) {
                    err = (1, MSG_SITE_CONFIG_NO_SITE)
                } else {
                    if (ApiUtil.isSigExpired(ts.get, iframeExpire)) {
                        err = (2, "ts: 调用的时间戳已经过期。")
                    } else {
                        val qs = req.underlying.getQueryString()
                        val hash = ApiUtil.encryptForApi(UrlUtil.getQueryMap(qs), uuidSite.getSecret)
                        if (hash != sig.get) {
                            err = (3, "sig: 无效的签名。")
                        } else {
                            try {
                                SiteConfig.get(uuid) match {
                                    case Some(sc) => siteConfig = sc
                                    case _ => Pass
                                }
                            } catch {
                                case ex: IllegalArgumentException => err = (3, MSG_SITE_CONFIG_INVALID_UUID)
                                case ex: Exception =>
                                    log.error(ex, "Exception when modify style. ")
                                    err = (4, MSG_UNKNOWN_ERRPR)
                            }
                        }
                    }
                }
                val p = SiteConfig.toParamTuples(siteConfig)
                Ok ~> Scalate(req, "common/setParmsIframe.ssp",
                    ("err", err) :: ("ts", ts.get.toString) :: ("sig", sig.get) :: ("uuid", uuid) :: p: _*)
            }
            expected(params) orFail { fails =>
                BadRequest ~> Scalate(req, "400.ssp", ("fails", fails))
            }

        case req @ POST(Path("/user/installIframe") & Params(params)) =>
            val expected = for {
                uuidStr <- lookup("uuid") is trimmed is
                    required("未指定参数") is
                    nonempty("参数不能为空")
                ts <- lookup("ts") is
                    long { "'" + _ + "'' is not an Long" }
                sig <- lookup("sig") is trimmed is
                    required("未指定参数") is
                    nonempty("参数不能为空")
                pic <- lookup("pic") is trimmed
                pluginType <- lookup("pluginType") is trimmed
                source <- lookup("source") is trimmed
                position <- lookup("position") is trimmed
                promote <- lookup("promote") is trimmed
                autoMatch <- lookup("autoMatch") is trimmed
                highlight <- lookup("highlight") is trimmed
                redirectMode <- lookup("redirectMode") is trimmed

                titleBold <- lookup("titleBold") is trimmed
                fontBold <- lookup("fontBold") is trimmed
                fontUnderline <- lookup("fontUnderline") is trimmed
                linkUnderline <- lookup("linkUnderline") is trimmed
                redirectType <- lookup("redirectType") is trimmed
                picMatch <- lookup("picMatch") is trimmed
                picBorderRadius <- lookup("picBorderRadius") is trimmed
                listType <- lookup("listType") is trimmed
                positionY <- lookup("positionY") is trimmed

                fontSize <- lookup("fontSize") is
                    int { "'插件文字字号" + _ + "' 不是正整数" }
                row <- lookup("row") is
                    int { "'行数" + _ + "' 不是正整数" }
                col <- lookup("col") is
                    int { "'列数" + _ + "' 不是正整数" }
                defaultPic <- lookup("defaultPic") is trimmed is
                    pred(isValidUrl, p => { "'默认图片" + p + "' url无效" })
                htcolor <- lookup("htcolor") is trimmed is
                    pred(isValidColor, p => { "'head字体颜色" + p + "' 值无效" })
                rtcolor <- lookup("rtcolor") is trimmed is
                    pred(isValidColor, p => { "'推荐文字字体颜色" + p + "' 值无效" })
                hvcolor <- lookup("hvcolor") is trimmed is
                    pred(isValidColor, p => { "'悬浮正文字体颜色" + p + "' 值无效" })
                picSize <- lookup("picSize") is
                    int { "'图片尺寸" + _ + "' 不是正整数" }
                bdcolor <- lookup("bdcolor") is trimmed is
                    pred(isValidColor, p => { "'插件窗体框线颜色" + p + "' 值无效" })

                width <- lookup("width") is trimmed is
                    int { "'宽度" + _ + "' 不是正整数" }
                height <- lookup("height") is trimmed is
                    int { "'高度" + _ + "' 不是正整数" }
                titleBgColor <- lookup("titleBgColor") is trimmed is
                    pred(isValidColor, p => { "'标题背景颜色" + p + "' 值无效" })
                titleImage <- lookup("titleImage") is trimmed
                titleFontSize <- lookup("titleFontSize") is trimmed is
                    int { "'标题字号大小" + _ + "' 不是正整数" }
                bgcolor <- lookup("bgcolor") is trimmed is
                    pred(isValidColor, p => { "'背景颜色" + p + "' 值无效" })
                lineHeight <- lookup("lineHeight") is trimmed is
                    int { "'行距" + _ + "' 不是正整数" }
            } yield {
                var err = (0, MSG_SITE_CONFIG_SUCCESS_UPDATE)

                val uuid = uuidStr.get
                val uuidSite = UuidSiteService.getUuidSiteByUuid(uuid)
                var siteConfig = SiteConfig.getSiteConfig(uuidStr)

                if (uuidSite == null) {
                    err = (1, MSG_SITE_CONFIG_NO_SITE)
                } else {
                    if (ApiUtil.isSigExpired(ts.get, iframeExpire)) {
                        err = (2, "ts: 调用的时间戳已经过期。")
                    } else {
                        val parameterMap = new java.util.HashMap[String, String]()
                        parameterMap.put("uuid", uuidStr.get)
                        parameterMap.put("ts", ts.get.toString)
                        val hash = ApiUtil.encryptForApi(parameterMap, uuidSite.getSecret)
                        if (hash != sig.get) {
                            err = (3, "sig: 无效的签名。")
                        } else {
                            siteConfig = SiteConfig.getSiteConfig(siteConfig, pluginType, row, col, pic,
                                fontSize, source, defaultPic, position,
                                htcolor, rtcolor, bdcolor, hvcolor,
                                promote, picSize, autoMatch, highlight, redirectMode,
                                width, height, titleBgColor, titleImage, titleFontSize,
                                titleBold, fontBold, fontUnderline, linkUnderline, redirectType, picMatch,
                                bgcolor, picBorderRadius, lineHeight, listType, positionY)
                            try {
                                if (!SiteConfig.update(siteConfig)) {
                                    err = (5, MSG_SITE_CONFIG_FAILED_UPDATE)
                                }
                            } catch {
                                // invalide uuid
                                case ex: IllegalArgumentException => err = (3, MSG_SITE_CONFIG_INVALID_UUID)
                                case ex: Exception =>
                                    log.error(ex, "Exception when modify style. ")
                                    err = (4, MSG_UNKNOWN_ERRPR)
                            }
                        }
                    }
                }
                val p = SiteConfig.toParamTuples(siteConfig)

                Ok ~> Scalate(req, "common/installIframe.ssp", ("err", err) :: ("uuid", uuid) :: p: _*)
            }

            expected(params) orFail { fails =>
                BadRequest ~> Scalate(req, "400.ssp", ("fails", fails))
            }
    }
}
