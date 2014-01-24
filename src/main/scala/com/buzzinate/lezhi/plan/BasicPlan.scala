package com.buzzinate.lezhi.plan

import java.io.File
import java.net.URLEncoder
import java.util.UUID

import scala.annotation.serializable

import org.apache.commons.lang.StringUtils
import org.fusesource.scalate.{ Binding, TemplateEngine }
import org.fusesource.scalate.layout.DefaultLayoutStrategy

import com.buzzinate.lezhi.Config
import com.buzzinate.lezhi.model.SiteConfig
import com.buzzinate.lezhi.util.{ Logging, LoginUtil }
import com.buzzinate.lezhi.service.UuidSiteService
import com.buzzinate.user.vo.UuidSite

import javax.servlet.http.HttpServletRequest
import unfiltered.Cookie
import unfiltered.request._
import unfiltered.response._
import unfiltered.request.Params

trait BasicPlan extends unfiltered.filter.Plan with Logging {

    val PAGE_SIZE = Config.getInt("lezhi.statistics.display.max.num.pages")
    val MAX_PAGE_NUM = Config.getInt("lezhi.statistics.page.display.max.num")

    val MSG_UNKNOWN_ERRPR = "发生错误，请重试！"
    val MSG_SITE_CONFIG_NO_SITE = "无法找到指定的站点设置！"
    val MSG_SITE_CONFIG_FAILED_UPDATE = "更新失败，请重试！"
    val MSG_SITE_CONFIG_INVALID_UUID = "非法的UUID!"
    val MSG_SITE_CONFIG_SUCCESS_LOAD = "成功加载设置!"
    val MSG_SITE_CONFIG_SUCCESS_UPDATE = "成功更新设置!"
    val MSG_PAGE_INFO_FAILED_UPDATE = "文章设置失败， 请重试!"
    val MSG_PAGE_INFO_SUCCESS_UPDATE = "文章成功更新!"
    val MSG_PAGE_IMG_FAILED_UPDATE = "文章图片设置失败， 请重试!"
    val MSG_PAGE_IMG_SUCCESS_UPDATE = "文章图片成功更新!"
    val MSG_CACHE_FAILURE = "请刷新缓存"
    val MAG_PAGE_IMG_OVER_SIZE = "链接图片大小超过1M, 请更换图片"
    val MSG_SUCCESS_UPDATE = "更新成功!"
    val MSG_INALID_UUID_USER = "您不是此UUID的合法用户"
    val MSG_CREATE_UUID_SITE_FAILED = "创建网站失败"
    val MSG_DELETE_UUID_SITE_FAILED = "删除网站失败"
    
    // define template engine for scalate
    val templateDirs = List(new File(Config.getString("root.dir") + "templates/"))
    implicit val engine = new TemplateEngine(templateDirs, Config.getString("scala.mode"))
    engine.layoutStrategy = new DefaultLayoutStrategy(engine, Config.getString("root.dir") +
        "templates/layouts/default.ssp")
    implicit val bindings: List[Binding] = List(
        Binding(name = "staticUrl", className = "String"),
        Binding(name = "rootDir", className = "String"),
        Binding(name = "rootUrl", className = "String"),
        Binding(name = "pluginUrl", className = "String"),
        Binding(name = "bshareAuthServer", className = "String"),
        Binding(name = "bshareAuthClient", className = "String"))
    implicit val additionalAttributes = List(
        ("staticUrl", Config.getString("static.url")),
        ("rootUrl", Config.getString("root.url")),
        ("rootDir", Config.getString("root.dir")),
        ("pluginUrl", Config.getString("plugin.url")),
        ("bshareAuthServer", Config.getString("bshare.auth.server")),
        ("bshareAuthClient", Config.getString("bshare.auth.client")))

    val CAS_VALIDATE_URL = Config.getString("bshare.auth.server") + "/serviceValidate?service=" +
        URLEncoder.encode(Config.getString("bshare.auth.client")) + "/login" + "&ticket="
    val CAS_LOGOUT_URL = Config.getString("bshare.auth.server") + "/logout?service=" +
      URLEncoder.encode(Config.getString("bshare.auth.client"))
    protected def getParam(params: Map[String, Seq[String]], s: String, default: String = "") = {
        params.get(s).flatMap { _.headOption } getOrElse (default)
    }

    protected def getUserId(req: HttpRequest[HttpServletRequest]) = {
        req.underlying.getAttribute(LoginUtil.USER_ID).asInstanceOf[Int]
    }

    protected def isAdmin(req: HttpRequest[HttpServletRequest]): Boolean = {
        req.underlying.getAttribute(LoginUtil.IS_ADMIN) != null
    }

    protected def isPublisher(req: HttpRequest[HttpServletRequest]): Boolean = {
        // admin should be publisher anyway
        req.underlying.getAttribute(LoginUtil.IS_PUBLISHER) != null ||
            req.underlying.getAttribute(LoginUtil.IS_ADMIN) != null ||
            req.underlying.getAttribute(LoginUtil.IS_PREMIUM_PUBLISHER) != null ||
            req.underlying.getAttribute(LoginUtil.IS_ENTERPRISE) != null
    }

    protected def getMaxWebsiteNumCanCreated(req: HttpRequest[HttpServletRequest]): Int = {
        if (req.underlying.getAttribute(LoginUtil.IS_ENTERPRISE) != null) {
            Config.getInt("bshare.website.enterprise")
        } else if (req.underlying.getAttribute(LoginUtil.IS_PREMIUM_PUBLISHER) != null) {
            Config.getInt("bshare.website.premium.publisher")
        } else if (req.underlying.getAttribute(LoginUtil.IS_PUBLISHER) != null) {
            Config.getInt("bshare.website.publisher")
        } else
            0
    }

    protected def isLogIn(req: HttpRequest[HttpServletRequest]) = {
        req.underlying.getAttribute(LoginUtil.USER_ID) != null
    }

    protected def isValidUser(req: HttpRequest[HttpServletRequest], uuid: String): Boolean = {
        val userId = getUserId(req)
        val site = UuidSiteService.getUuidSiteByUuid(uuid)
        site != null && (site.getUserId == userId)
    }
}
