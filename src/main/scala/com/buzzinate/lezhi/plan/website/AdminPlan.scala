package com.buzzinate.lezhi.plan.website

import java.util.{ Calendar, Date }

import scala.Option.option2Iterable
import scala.annotation.serializable

import org.apache.commons.lang.StringUtils

import com.buzzinate.common.util.DateTimeUtil
import com.buzzinate.common.util.string.UrlUtil
import com.buzzinate.lezhi.bean.{ AdminData, Page, Paginate }
import com.buzzinate.lezhi.db.JedisClient
import com.buzzinate.lezhi.model.{ AdminCounts, ExchangeLink, PageStatistic, SiteConfig, SiteStatus, StatSite }
import com.buzzinate.lezhi.plan.BasicPlan
import com.buzzinate.lezhi.service.{ SiteStatsService, UuidSiteService }
import com.buzzinate.lezhi.util.{ MetricsUtil, OfcUtil }
import com.yammer.metrics.MetricRegistry

import unfiltered.request._
import unfiltered.request.QParams._
import unfiltered.response._
import unfiltered.scalate.Scalate

/**
 * @author jeffrey created on 2012-9-19 上午11:17:56
 *
 */
class AdminPlan extends BasicPlan {

    private val pvTimer = MetricsUtil.metrics.timer(MetricRegistry.name(classOf[AdminPlan], "getViews"))
    private val adminTimer = MetricsUtil.metrics.timer(MetricRegistry.name(classOf[AdminPlan], "getSites"))
    private val meter = MetricsUtil.metrics.meter(MetricRegistry.name(classOf[AdminPlan], "dashboard", "requests"))

    def intent = {
        case req @ GET(Path("/admin/dashboard") & Params(params)) => {
            if (!isAdmin(req)) {
                Unauthorized ~> Scalate(req, "401.ssp")
            } else {
                Ok ~> Scalate(req, "admin/dashboard.ssp")
            }
        }

        case req @ GET(Path("/admin/stats/dashboard") & Params(params)) =>
            if (!isAdmin(req)) {
                Unauthorized ~> Scalate(req, "401.ssp")
            } else {
                meter.mark
                val calendar = Calendar.getInstance()
                val endDay = calendar.getTime()
                calendar.add(Calendar.DAY_OF_MONTH, -3)
                val startDay = calendar.getTime()

                var admin = JedisClient.getObject("admin").asInstanceOf[Map[String, String]]
                if (admin == null) {
                    val articleNum = SiteStatus.getUrlNumber(SiteStatus.ADMIN_UUID).toString

                    var (pluginUuids, buttonUuids, picUuids, textUuids) = SiteConfig.getAllTypeUuids
                    while ((pluginUuids.size + buttonUuids.size) <= 0 || (picUuids.size + textUuids.size) <= 0) {
                        val (pluginUuidsT, buttonUuidsT, picUuidsT, textUuidsT) = SiteConfig.getAllTypeUuids
                        pluginUuids = pluginUuidsT
                        buttonUuids = buttonUuidsT
                        picUuids = picUuidsT
                        textUuids = textUuidsT
                    }
                    val pluginNum = pluginUuids.size.toString
                    val buttonNum = buttonUuids.size.toString
                    val picNum = picUuids.size.toString
                    val textNum = textUuids.size.toString

                    val activePluginNum = StatSite.getActiveSiteNumber(pluginUuids, startDay, endDay).toString
                    val activeButtonPluginNum = StatSite.getActiveSiteNumber(buttonUuids, startDay, endDay).toString
                    val activePicNum = StatSite.getActiveSiteNumber(picUuids, startDay, endDay).toString
                    val activeTextNum = StatSite.getActiveSiteNumber(textUuids, startDay, endDay).toString

                    val totalUuids = SiteConfig.getAllUuids
                    val totalNum = totalUuids.size.toString
                    val activeTotalNum = StatSite.getActiveSiteNumber(totalUuids, startDay, endDay).toString

                    admin = Map("articleNum" -> articleNum,
                        "pluginNum" -> pluginNum, "buttonNum" -> buttonNum, "picNum" -> picNum, "textNum" -> textNum,
                        "activePluginNum" -> activePluginNum, "activeButtonPluginNum" -> activeButtonPluginNum, "activePicNum" -> activePicNum, "activeTextNum" -> activeTextNum,
                        "totalNum" -> totalNum, "activeTotalNum" -> activeTotalNum)

                    JedisClient.set("admin", 3600, admin)
                }
                calendar.setTime(new Date)
                calendar.add(Calendar.DAY_OF_MONTH, -7)

                val pvContext = pvTimer.time()
                val pv = try {
                    (0 until 7).map { i =>
                        calendar.add(Calendar.DAY_OF_MONTH, 1)
                        calendar.getTime -> AdminCounts.getViews(calendar.getTime)
                    }
                } finally {
                    pvContext.stop();
                }
                calendar.setTime(new Date)
                calendar.add(Calendar.DAY_OF_MONTH, -6)
                val dateStart = calendar.getTime()
                val jsonPv = OfcUtil.getOfc2ChartJsonData(pv, dateStart, endDay, OfcUtil.ChartType.LINE,
                    false, null, null, true)

                calendar.setTime(new Date)
                calendar.add(Calendar.DAY_OF_MONTH, -7)

                val adminContext = adminTimer.time()
                val adminDatas = try {
                    (0 until 7).map { i =>
                        calendar.add(Calendar.DAY_OF_MONTH, 1)
                        val adminCounts = AdminCounts.get(calendar.getTime)
                        if (adminCounts.isDefined) {
                            val admin = adminCounts.get
                            val totalShowup = admin.showupsFixedI + admin.showupsSlideI
                            val totalClick = admin.clicksFixedI + admin.clicksSlideI
                            val totalClickRate = if (totalShowup != 0) { totalClick.toDouble * 100 / totalShowup } else { 0.00 }
                            val pluginClickRate = if (admin.showupsFixedI != 0) { admin.clicksFixedI.toDouble * 100 / admin.showupsFixedI } else { 0.00 }
                            val buttonClickRate = if (admin.showupsSlideI != 0) { admin.clicksSlideI.toDouble * 100 / admin.showupsSlideI } else { 0.00 }
                            val picClickRate = if (admin.showupsPicI != 0) { admin.clicksPicI.toDouble * 100 / admin.showupsPicI } else { 0.00 }
                            val textClickRate = if (admin.showupsNopicI != 0) { admin.clicksNopicI.toDouble * 100 / admin.showupsNopicI } else { 0.00 }

                            val totalShowupT = admin.showupsFixedT + admin.showupsSlideT
                            val totalClickT = admin.clicksFixedT + admin.clicksSlideT
                            val totalClickRateT = if (totalShowupT != 0) { totalClickT.toDouble * 100 / totalShowupT } else { 0.00 }
                            val pluginClickRateT = if (admin.showupsFixedT != 0) { admin.clicksFixedT.toDouble * 100 / admin.showupsFixedT } else { 0.00 }
                            val buttonClickRateT = if (admin.showupsSlideT != 0) { admin.clicksSlideT.toDouble * 100 / admin.showupsSlideT } else { 0.00 }
                            val picClickRateT = if (admin.showupsPicT != 0) { admin.clicksPicT.toDouble * 100 / admin.showupsPicT } else { 0.00 }
                            val textClickRateT = if (admin.showupsNoPicT != 0) { admin.clicksNoPicT.toDouble * 100 / admin.showupsNoPicT } else { 0.00 }

                            new AdminData(DateTimeUtil.formatDate(calendar.getTime), admin, totalShowup, totalClick, totalClickRate, pluginClickRate, buttonClickRate, picClickRate, textClickRate, totalShowupT, totalClickT, totalClickRateT, pluginClickRateT, buttonClickRateT, picClickRateT, textClickRateT)
                        } else {
                            new AdminData(DateTimeUtil.formatDate(calendar.getTime), AdminCounts.AdminCounts(new java.sql.Date(DateTimeUtil.getDateDay(calendar.getTime).getTime)), 0, 0, 0.00, 0.00, 0.00, 0.00, 0.00, 0, 0, 0.00, 0.00, 0.00, 0.00, 0.00)
                        }
                    }.toSeq
                } finally {
                    adminContext.stop();
                }

                Ok ~> Scalate(req, "admin/admin.ssp",
                    ("articleNum", admin("articleNum")),
                    ("totalNum", admin("totalNum")),
                    ("pluginNum", admin("pluginNum")),
                    ("buttonNum", admin("buttonNum")),
                    ("picNum", admin("picNum")),
                    ("textNum", admin("textNum")),
                    ("activeTotalNum", admin("activeTotalNum")),
                    ("activePluginNum", admin("activePluginNum")),
                    ("activeButtonPluginNum", admin("activeButtonPluginNum")),
                    ("activePicNum", admin("activePicNum")),
                    ("activeTextNum", admin("activeTextNum")),
                    ("jsonPv", jsonPv),
                    ("dateStart", DateTimeUtil.formatDate(dateStart)),
                    ("dateEnd", DateTimeUtil.formatDate(endDay)),
                    ("adminDatas", adminDatas),
                    ("isAdmin", isAdmin(req)),
                    ("link", "admin"),
                    ("subLink", "overview"))

            }
        case req @ GET(Path("/admin/stats/sites") & Params(params)) =>
            if (!isAdmin(req)) {
                Unauthorized ~> Scalate(req, "401.ssp")
            } else {
                val expected = for {
                    pagination <- lookup("pagination") is
                        int { "'" + _ + "'' is not an integer" }
                    sortKey <- lookup("sortKey") is trimmed
                    uuidStr <- lookup("uuid") is trimmed
                    site <- lookup("site") is trimmed
                } yield {
                    var endDay = DateTimeUtil.convertDate(getParam(params, "dateEnd"))
                    var startDay = DateTimeUtil.convertDate(getParam(params, "dateStart"))
                    var err = (-1, "")
                    if (endDay == null || startDay == null) {
                        endDay = DateTimeUtil.getCurrentDateDay()
                        startDay = DateTimeUtil.subtractDays(endDay, 3)
                    }
                    val sortType = sortKey.getOrElse("articleNum")
                    var uuids: Seq[String] = Nil
                    //search by uuid
                    if (uuidStr.nonEmpty && StringUtils.isNotEmpty(uuidStr.get.trim)) {
                        try {
                            uuids = Array(uuidStr.get.trim)
                        } catch {
                            case ex: Exception =>
                                err = (1, "UUID 格式不正确")
                        }
                        //search by site name
                    } else if (site.nonEmpty && StringUtils.isNotEmpty(site.get)) {
                        uuids = UuidSiteService.getUuidSiteByName(site.get)
                    } //get all sites
//                    else {
//                        uuids = SiteConfig.getAllUuids
//                    }
                    //get the siteStats order by statType
//                    val siteStats = SiteStatsService.getSiteDatas(pagination.getOrElse(0), PAGE_SIZE, sortType, uuids, startDay, endDay)
                    val siteStats = SiteStatsService.getSiteDatas(uuids, startDay, endDay, new Paginate(pagination.getOrElse(1)), sortType)
                        
                    Ok ~> Scalate(req, "admin/sites.ssp",
                        ("sitestats", siteStats._2),
                        ("pagination", pagination.getOrElse(0)),
                        ("pageSize", PAGE_SIZE),
                        ("totalSize", siteStats._1),
                        ("isAdmin", isAdmin(req)),
                        ("link", "admin"),
                        ("sortKey", sortType),
                        ("site", site.getOrElse("").trim),
                        ("uuidStr", uuidStr.getOrElse("").trim),
                        ("dateStart", DateTimeUtil.formatDate(startDay)),
                        ("dateEnd", DateTimeUtil.formatDate(endDay)),
                        ("subLink", "data"),
                        ("err", err))
                }

                expected(params) orFail { fails =>
                    BadRequest ~> Scalate(req, "400.ssp", ("fails", fails))
                }
            }

        case req @ GET(Path("/admin/stats/pages") & Params(params)) =>
            if (!isAdmin(req)) {
                Unauthorized ~> Scalate(req, "401.ssp")
            } else {
                val expected = for {
                    pagination <- lookup("pagination") is
                        int { "'" + _ + "'' is not an integer" }
                    domain <- lookup("domain") is trimmed
                    date <- lookup("date") is trimmed
                    sortKey <- lookup("sortKey") is trimmed
                } yield {
                    val calendar = Calendar.getInstance()
                    calendar.add(Calendar.DAY_OF_MONTH, -1)
                    val keyType = date.getOrElse(DateTimeUtil.formatDate(calendar.getTime())) + "_" + sortKey.getOrElse("clickToShowup")
                    val start = (pagination.getOrElse(1) - 1) * PAGE_SIZE
                    val end = pagination.getOrElse(1) * PAGE_SIZE

                    var pages: Seq[Page] =
                        if (StringUtils.isNotEmpty(domain.getOrElse(""))) {
                            PageStatistic.getStatistics(keyType).filter(page => UrlUtil.getDomainName(page.url) == domain.get.trim())
                        } else {
                            PageStatistic.getStatistics(keyType)
                        }
                    val cur = if (pages.size == 0) {
                        0
                    } else {
                        pagination.getOrElse(1)
                    }
                    Ok ~> Scalate(req, "admin/pages.ssp",
                        ("isAdmin", isAdmin(req)),
                        ("pages", pages.slice(start, end)),
                        ("cur", cur),
                        ("total", (pages.size.doubleValue / PAGE_SIZE).ceil.toInt),
                        ("date", date.getOrElse(DateTimeUtil.formatDate(calendar.getTime()))),
                        ("sortKey", sortKey.getOrElse("clickToShowup")),
                        ("domain", domain.getOrElse("")),
                        ("link", "admin"),
                        ("subLink", "pageRanking"))
                }
                expected(params) orFail { fails =>
                    BadRequest ~> Scalate(req, "400.ssp", ("fails", fails))
                }
            }

        case req @ GET(Path("/admin/link") & Params(params)) => {
            if (!isAdmin(req)) {
                Unauthorized ~> Scalate(req, "401.ssp")
            } else {
                val links = ExchangeLink.getLinks
                Ok ~> Scalate(req, "admin/link.ssp", ("links", links))
            }
        }

        case req @ GET(Path("/admin/link/update") & Params(params)) => {
            if (!isAdmin(req)) {
                Unauthorized ~> Scalate(req, "401.ssp")
            } else {
                val expected = for {
                    url <- lookup("url") is trimmed is
                        required("missing param") is
                        nonempty("empty param")
                    order <- lookup("order") is trimmed is
                        required("missing param") is
                        nonempty("empty param")
                    title <- lookup("title") is trimmed is
                        required("missing param") is
                        nonempty("empty param")

                } yield {
                    ExchangeLink.updateExchangeLinks(List(com.buzzinate.lezhi.model.ExchangeLink.ExchangeLink(url.get, order.get.toInt, title.get)))
                    val links = ExchangeLink.getLinks
                    Ok ~> Scalate(req, "admin/link.ssp", ("links", links))
                }
                expected(params) orFail { fails =>
                    BadRequest ~> Scalate(req, "400.ssp", ("fails", fails))
                }
            }
        }

        case req @ GET(Path("/admin/link/del") & Params(params)) => {
            if (!isAdmin(req)) {
                Unauthorized ~> Scalate(req, "401.ssp")
            } else {
                val expected = for {
                    url <- lookup("url") is trimmed is
                        required("missing param") is
                        nonempty("empty param")
                } yield {
                    ExchangeLink.delExchangeLink(url.get)
                    val links = ExchangeLink.getLinks
                    Ok ~> Scalate(req, "admin/link.ssp", ("links", links))
                }
                expected(params) orFail { fails =>
                    BadRequest ~> Scalate(req, "400.ssp", ("fails", fails))
                }
            }
        }

        case req @ GET(Path("/admin/redis/flush") & Params(params)) => {
            if (!isAdmin(req)) {
                Unauthorized ~> Scalate(req, "401.ssp")
            } else {
                JedisClient.flush
                Ok ~> ResponseString("flush redis ok!")
            }
        }
    }
}
