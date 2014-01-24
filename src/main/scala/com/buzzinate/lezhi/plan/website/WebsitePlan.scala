package com.buzzinate.lezhi.plan.website

import java.util.UUID

import scala.Option.option2Iterable
import scala.annotation.serializable

import org.apache.commons.lang.StringUtils
import org.jsoup.Jsoup

import com.buzzinate.common.util.string.UrlUtil
import com.buzzinate.lezhi.model._
import com.buzzinate.lezhi.plan.BasicPlan
import com.buzzinate.lezhi.service.{UserService, UuidSiteService}
import com.buzzinate.lezhi.util._
import com.buzzinate.lezhi.util.ValidateUtil._
import com.buzzinate.user.vo.UuidSite

import unfiltered.request._
import unfiltered.request.QParams._
import unfiltered.response._
import unfiltered.scalate.Scalate

class WebsitePlan extends BasicPlan {

    def intent = {
        case req @ GET(Path("/")) => {
            val links = ExchangeLink.getLinks
            Ok ~> Scalate(req, "index.ssp", ("links", links))
        }

        case req @ GET(Path("/help")) =>
            Ok ~> Scalate(req, "help.ssp")

        case req @ GET(Path("/userAgreement")) =>
            Ok ~> Scalate(req, "userAgreement.ssp")

        case req @ GET(Path("/error")) =>
            Ok ~> Scalate(req, "error.ssp", ("message", "未知错误"))

        case req @ GET(Path("/get") & Params(params)) => {
            val expected = for {
                t <- lookup("t") is
                    int { "'" + _ + "'' is not an integer" }
                uuidStr <- lookup("uuid") is trimmed
            } yield {
                Ok ~> Scalate(req, "chooseType.ssp", ("uuid", uuidStr.getOrElse("nologin")))
            }
            expected(params) orFail { fails =>
                Ok ~> Scalate(req, "chooseType.ssp", ("uuid", "nologin"))
            }
        }

        case req @ GET(Path("/get/static/pic") & Params(params)) => {
            val expected = for {
                t <- lookup("t") is
                    int { "'" + _ + "'' is not an integer" }
                uuidStr <- lookup("uuid") is trimmed
            } yield {
                Ok ~> Scalate(req, "choosePicStyle.ssp", ("uuid", uuidStr.getOrElse("nologin")))
            }
            expected(params) orFail { fails =>
                Ok ~> Scalate(req, "choosePicStyle.ssp", ("uuid", "nologin"))
            }
        }
        case req @ GET(Path("/get/float/pic") & Params(params)) => {
            val expected = for {
                t <- lookup("t") is
                    int { "'" + _ + "'' is not an integer" }
                uuidStr <- lookup("uuid") is trimmed
            } yield {
                Ok ~> Scalate(req, "choosePicStyleSlide.ssp", ("uuid", uuidStr.getOrElse("nologin")))
            }
            expected(params) orFail { fails =>
                Ok ~> Scalate(req, "choosePicStyleSlide.ssp", ("uuid", "nologin"))
            }
        }
        case req @ GET(Path("/get/static/text") & Params(params)) => {
            val expected = for {
                t <- lookup("t") is
                    int { "'" + _ + "'' is not an integer" }
                uuidStr <- lookup("uuid") is trimmed
            } yield {
                Ok ~> Scalate(req, "chooseStyle.ssp", ("uuid", uuidStr.getOrElse("nologin")))
            }
            expected(params) orFail { fails =>
                Ok ~> Scalate(req, "chooseStyle.ssp", ("uuid", "nologin"))
            }
        }
        case req @ GET(Path("/get/float/text") & Params(params)) => {
            val expected = for {
                t <- lookup("t") is
                    int { "'" + _ + "'' is not an integer" }
                uuidStr <- lookup("uuid") is trimmed
            } yield {
                Ok ~> Scalate(req, "chooseStyleSlide.ssp", ("uuid", uuidStr.getOrElse("nologin")))
            }
            expected(params) orFail { fails =>
                Ok ~> Scalate(req, "chooseStyleSlide.ssp", ("uuid", "nologin"))
            }
        }
        case req @ GET(Path("/get/customize") & Params(params)) =>
            val expected = for {
                row <- lookup("row") is
                    int { "'行数" + _ + "' 不是正整数" }
                col <- lookup("col") is
                    int { "'列数" + _ + "' 不是正整数" }
                plugintype <- lookup("type") is trimmed is
                    required("未指定参数") is
                    nonempty("参数不能为空")
                pic <- lookup("pic") is trimmed is
                    required("未指定参数") is
                    nonempty("参数不能为空")
                uuidStr <- lookup("uuid") is trimmed is
                    required("uuid未指定") is
                    nonempty("uuid不能为空")
            } yield {
                var site: UuidSite = null
                var sites: List[UuidSite] = null
                var hasSite: Boolean = false
                if (isLogIn(req)) {
                    hasSite = UuidSiteService.getUuidSiteCount(getUserId(req)) > 0
                    if (uuidStr.get == "nologin") {
                        sites = UuidSiteService.getUuidSiteByUserId(getUserId(req))
                    }
                    if (uuidStr.get != "new" && uuidStr.get != "nologin") {
                        site = UuidSiteService.getUuidSiteByUuid(uuidStr.get)
                    }
                }
                val siteConfig = SiteConfig.getSiteConfig(uuidStr)
                siteConfig.col = col.get
                siteConfig.row = row.get
                siteConfig.pluginType = plugintype.get
                siteConfig.pic = pic.map(_.toBoolean).getOrElse(false)
                val p = SiteConfig.toParamTuples(siteConfig)
                Ok ~> Scalate(req, "installGeneral.ssp",
                    ("count", col.getOrElse(SiteConfig.DEFAULT_COL) * row.getOrElse(SiteConfig.DEFAULT_ROW))
                        :: ("site", site)
                        :: ("sites", sites)
                        :: ("hasSite", hasSite)
                        :: ("uuid", uuidStr.get)
                        :: p: _*)
            }

            expected(params) orFail { fails =>
                BadRequest ~> Scalate(req, "400.ssp", ("fails", fails))
            }

        case req @ POST(Path("/get/code") & Params(params)) =>
            val expected = for {
                uuidStr <- lookup("uuid") is trimmed
                pic <- lookup("pic") is trimmed
                pluginType <- lookup("pluginType") is trimmed
                source <- lookup("source") is trimmed
                position <- lookup("position") is trimmed
                promote <- lookup("promote") is trimmed
                autoMatch <- lookup("autoMatch") is trimmed
                highlight <- lookup("highlight") is trimmed
                redirectMode <- lookup("redirectMode") is trimmed

                titleBold <- lookup("titleBold") is trimmed
                titleImage <- lookup("titleImage") is trimmed
                fontBold <- lookup("fontBold") is trimmed
                fontUnderline <- lookup("fontUnderline") is trimmed
                linkUnderline <- lookup("linkUnderline") is trimmed
                redirectType <- lookup("redirectType") is trimmed
                picMatch <- lookup("picMatch") is trimmed
                picBorderRadius <- lookup("picBorderRadius") is trimmed
                listType <- lookup("listType") is trimmed
                positionY <- lookup("positionY") is trimmed
                adEnabled <- lookup("adEnabled") is trimmed

                adCount <- lookup("adCount") is
                    int { "'开通广告条数" + _ + "' 不是正整数" }
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
                titleFontSize <- lookup("titleFontSize") is trimmed is
                    int { "'标题字号大小" + _ + "' 不是正整数" }
                bgcolor <- lookup("bgcolor") is trimmed is
                    pred(isValidColor, p => { "'背景颜色" + p + "' 值无效" })
                lineHeight <- lookup("lineHeight") is trimmed is
                    int { "'行距" + _ + "' 不是正整数" }

                siteName <- lookup("siteName") is trimmed
                siteUrl <- lookup("siteUrl") is trimmed
            } yield {
                var err = (0, MSG_SITE_CONFIG_SUCCESS_UPDATE)
                var siteConfig = SiteConfig.getSiteConfig(uuidStr)
                siteConfig = SiteConfig.getSiteConfig(siteConfig, pluginType, row, col, pic,
                    fontSize, source, defaultPic, position,
                    htcolor, rtcolor, bdcolor, hvcolor,
                    promote, picSize, autoMatch, highlight, redirectMode,
                    width, height, titleBgColor, titleImage, titleFontSize,
                    titleBold, fontBold, fontUnderline, linkUnderline, redirectType, picMatch,
                    bgcolor, picBorderRadius, lineHeight, listType, positionY)
                if (adEnabled.isDefined)
                    siteConfig.adEnabled = adEnabled.map(_.toBoolean).get
                if (siteConfig.adEnabled)
                    siteConfig.adCount = adCount.get

                var site: UuidSite = null
                if (StringUtils.isNotEmpty(uuidStr.get) && uuidStr.get != "nologin") {
                    if (uuidStr.get != "new") {
                        try {
                            val uuid = UUID.fromString(uuidStr.get)
                            site = UuidSiteService.getUuidSiteByUuid(uuid.toString)
                            // check if current is the owner of the site
                            if (site != null) {
                                log.debug("Saving SiteConfig, [uuid=" + uuidStr.get + "]")
                                if (!SiteConfig.update(siteConfig)) {
                                    err = (1, MSG_SITE_CONFIG_FAILED_UPDATE)
                                }
                            } else {
                                // no authorization for this site
                                err = (2, MSG_SITE_CONFIG_NO_SITE)
                            }
                        } catch {
                            case ex: IllegalArgumentException => err = (3, MSG_SITE_CONFIG_INVALID_UUID)
                            case ex: Exception =>
                                log.error(ex, "Exception when modify style. ")
                                err = (4, MSG_UNKNOWN_ERRPR)
                        }
                    } else {
                        try {
                            val uuidSite = new UuidSite()
                            uuidSite.setUserId(getUserId(req));
                            uuidSite.setName(siteName.get)
                            uuidSite.setUrl(siteUrl.get)
                            UuidSiteService.createUuidSite(uuidSite)
                            siteConfig.uuid = uuidSite.getUuid
                            if (!SiteConfig.update(siteConfig)) {
                                err = (1, MSG_SITE_CONFIG_FAILED_UPDATE)
                            }
                        } catch {
                            case ex: Exception =>
                                log.error(ex, "Exception when create site. ")
                                err = (4, MSG_CREATE_UUID_SITE_FAILED)
                        }
                    }
                }
                var isUserCustomize = getParam(params, "isUserCustomize", "false");
                if (isUserCustomize == "true") {
                    Found ~> Location("/user/code?uuid=" + uuidStr.get)
                } else {
                    val (target, p) = if (err._1 > 0) {
                        ("installGeneral.ssp", SiteConfig.toParamTuples(siteConfig))
                    } else {
                        ("install.ssp", SiteConfig.toEncodedParamTuples(siteConfig))
                    }
                    val uuid = if (StringUtils.isNotEmpty(uuidStr.get) && uuidStr.get != "nologin") siteConfig.uuid else uuidStr.get
                    Ok ~> Scalate(req, target,
                        ("err", err) ::
                            ("count", siteConfig.col * siteConfig.row)
                            :: ("site", site)
                            :: ("hasSite", UuidSiteService.getUuidSiteCount(getUserId(req)) > 0)
                            :: ("uuid", uuid)
                            :: p: _*)
                }
            }

            expected(params) orFail { fails =>
                BadRequest ~> Scalate(req, "400.ssp", ("fails", fails))
            }

        case req @ GET(Path("/get/code") & Params(params)) =>
            val expected = for {
                uuidStr <- lookup("uuid") is trimmed is
                    required("uuid未指定") is
                    nonempty("uuid不能为空")
            } yield {
                var err = (-1, MSG_SITE_CONFIG_SUCCESS_LOAD)
                var siteConfig = new SiteConfig
                var site: UuidSite = null
                var sites: List[UuidSite] = null
                if (StringUtils.isNotEmpty(uuidStr.get) && uuidStr.get != "nologin") {
                    try {
                        val uuid = UUID.fromString(uuidStr.get)
                        site = UuidSiteService.getUuidSiteByUuid(uuid.toString)
                        // check if current is the owner of the site
                        if (site != null) {
                            SiteConfig.get(uuidStr.get) match {
                                case Some(sc) => siteConfig = sc
                                case _ => Pass
                            }
                        } else {
                            // no authorization for this site
                            err = (2, MSG_SITE_CONFIG_NO_SITE)
                        }
                    } catch {
                        case ex: IllegalArgumentException => err = (3, MSG_SITE_CONFIG_INVALID_UUID)
                        case ex: Exception =>
                            log.error(ex, "Exception when modify style. ")
                            err = (4, MSG_UNKNOWN_ERRPR)
                    }
                }
                if (isLogIn(req) && uuidStr.get == "nologin") {
                    sites = UuidSiteService.getUuidSiteByUserId(getUserId(req))
                }
                val p = SiteConfig.toParamTuples(siteConfig)
                Ok ~> Scalate(req, "/installGeneral.ssp",
                    ("err", err)
                        :: ("count", siteConfig.col * siteConfig.row)
                        :: ("site", site)
                        :: ("sites", sites)
                        :: ("hasSite", UuidSiteService.getUuidSiteCount(getUserId(req)) > 0)
                        :: ("uuid", uuidStr.get)
                        :: p: _*)
            }
            expected(params) orFail { fails =>
                BadRequest ~> Scalate(req, "400.ssp", ("fails", fails))
            }

        case req @ GET(Path("/siteConfig") & Params(params)) => {
            val expected = for {
                uuidStr <- lookup("uuid") is trimmed is
                    required("uuid未指定") is
                    nonempty("uuid不能为空")
            } yield {
                val siteConfig = SiteConfig.get(uuidStr.get)
                val p = if (siteConfig.isDefined) {
                    SiteConfig.toParamTuples(siteConfig.get)
                } else
                    Nil
                val isExisted = p.size > 0
                Ok ~> Scalate(req, "/siteConfig.ssp", ("isExisted", isExisted) :: ("uuid", uuidStr.get) :: p: _*)
            }
            expected(params) orFail { fails =>
                BadRequest ~> ResponseString("400: "
                    + (fails.map { f => f.name + ": " + f.error } mkString ("; ")))
            }
        }

        case req @ GET(Path("/loginCallback")) =>
            Ok ~> Scalate(req, "loginCallback.ssp")

        case req @ GET(Path("/registerCallback")) =>
            Ok ~> Scalate(req, "registerCallback.ssp")

        case req @ GET(Path("/login") & Params(params)) =>
            val expected = for {
                ticket <- lookup("ticket") is trimmed is
                    required("未指定参数") is
                    nonempty("参数不能为空")
                targetUrl <- lookup("targetUrl") is trimmed is
                    required("未指定参数") is
                    nonempty("参数不能为空")
                rememberMe <- lookup("rememberMe") is trimmed
            } yield {
                val isRemember = (rememberMe.getOrElse("true") == "true")
                log.info("Login now, [ticket=" + ticket.get + ", rememberMe=" + isRemember + "]")

                val loginId = validateTicket(ticket.get)
                if (!StringUtils.isEmpty(loginId)) {
                    val user = UserService.getByLoginId(loginId)
                    if (user.nonEmpty) {
                        val password = UserService.getPassWordByUserId(user.get.getUserId)
                        P3P.apply("CP=\"IDC DSP COR ADM DEVi TAIi PSA PSD IVAi IVDi CONi HIS OUR IND CNT\"") ~>
                            SetCookies(LoginUtil.setCookieCode(loginId, password, isRemember)) ~>
                            CacheControl("no-cache") ~> Found ~> Location(targetUrl.get)
                    } else {
                        log.error("LoginID is" + loginId + ",user is empty. Login failed.")
                        CacheControl("no-cache") ~> Found ~> Location(targetUrl.get)
                    }
                } else {
                    CacheControl("no-cache") ~> Found ~> Location(targetUrl.get)
                }
            }

            expected(params) orFail { fails =>
                BadRequest ~> Scalate(req, "400.ssp", ("fails", fails))
            }

        case req @ GET(Path("/logout")) =>
            unfiltered.response.SetCookies(unfiltered.Cookie(
                LoginUtil.COOKIE_NAME, "",
                Option(LoginUtil.COOKIE_DOMAIN),
                Option("/"),
                Option(0))) ~>
                CacheControl("no-cache") ~>
                Found ~>
                Location(CAS_LOGOUT_URL)

        case req @ GET(Path("/clear")) =>
            P3P.apply("CP=\"IDC DSP COR ADM DEVi TAIi PSA PSD IVAi IVDi CONi HIS OUR IND CNT\"") ~>
                SetCookies(unfiltered.Cookie(
                    LoginUtil.COOKIE_NAME, "",
                    Option(LoginUtil.COOKIE_DOMAIN),
                    Option("/"),
                    Option(0))) ~>
                Ok

        case req @ GET(Path("/test/demopage") & Params(params)) =>
            val url = getParam(params, "url")
            val title = if (StringUtils.isNotEmpty(url)) {
                try {
                    val doc = Jsoup.connect(url).timeout(10000).get();
                    doc.title
                } catch {
                    case ex: Exception => ""
                }
            } else ""
            var sitePrefix = getParam(params, "sitePrefix")
            if (StringUtils.isEmpty(sitePrefix)) {
                sitePrefix = UrlUtil.getFullDomainNameWithHttpPrefix(url)
            }

            Ok ~> Scalate(req, "test/demo.ssp",
                ("url", url), ("title", title), ("sitePrefix", sitePrefix))

        case req @ _ => NotFound ~> Scalate(req, "404.ssp")

    }

    private def validateTicket(ticket: String): String = {
        val url = CAS_VALIDATE_URL + ticket
        val content = SimpleHttpClient.getContent(url)
        if (content.contains("authenticationSuccess")) {
            val userName = content.substring(content.indexOf("<cas:user>") + 10, content.indexOf("</cas:user>"))
            return userName
        } else {
            log.error("ticket validation failed, ticket is" + ticket)
            return ""
        }
    }

    object P3P extends HeaderName("P3P")

}
