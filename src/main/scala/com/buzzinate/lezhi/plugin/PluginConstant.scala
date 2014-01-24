package com.buzzinate.lezhi.plugin

import scala.Array.canBuildFrom
import scala.annotation.serializable
import scala.collection.mutable.ListBuffer

import org.apache.commons.lang.StringUtils
import org.apache.commons.lang.math.RandomUtils

import com.buzzinate.buzzads.thrift.AdItem
import com.buzzinate.common.util.kestrel.KestrelClient
import com.buzzinate.common.util.string.CookieIdUtil
import com.buzzinate.lezhi.Config
import com.buzzinate.lezhi.bean.RequestParam
import com.buzzinate.lezhi.buzzads.{AdvertiseClientMock, AdvertiseClientThrift}
import com.buzzinate.lezhi.elastic.ElasticIndexClientThrift
import com.buzzinate.lezhi.recommend.{RecommendClientMock, RecommendClientThrift}
import com.buzzinate.lezhi.thrift.{PicType, RecommendResult, RecommendType, RecommendTypeParam}
import com.buzzinate.lezhi.util.{Logging, UrlFilter}
import com.buzzinte.common.genome.GenomeClient
import com.codahale.jerkson.Json
import com.twitter.util.{Future, Promise, Return, Throw}
import com.typesafe.config.ConfigFactory

import akka.actor.{ActorSystem, Props}
import akka.routing.RoundRobinRouter
import unfiltered.Cookie
import unfiltered.netty.ReceivedMessage
import unfiltered.request.HttpRequest
import unfiltered.response.HeaderName

/**
 * @author jeffrey.ji <jeffrey.ji@buzzinate.com>
 * @date Dec 27, 2012 4:51:19 PM
 *
 */
object PluginConstant extends Logging {
    lazy val INCLICK_HASH_PREFIX = "blz-"
    lazy val COOKIE_EXPIRE_TIME = 60 * 60 * 24 * 365
    lazy val PLUGIN_COOKIE_DOMAIN = Config.getString("bshare.security.plugin.cookie.domain")
    lazy val BX_INTERVAL = Config.getInt("lz.bx.hit.interval") * 60 * 60 * 24 * 1000L
    lazy val BX_FACTOR = Config.getInt("lz.bx.hit.factor")

    lazy val MOCK_COOKIE_NAME = "lz-mock"

    object P3P extends HeaderName("P3P")

    lazy val recommendClient = new RecommendClientThrift
    lazy val recommendClientMock = new RecommendClientMock

    lazy val adClient = new AdvertiseClientThrift
    lazy val adClientMock = new AdvertiseClientMock

    lazy val elasticClient = new ElasticIndexClientThrift

    lazy val system = ActorSystem("Lezhi", ConfigFactory.load(Thread.currentThread.getContextClassLoader, "plugin.conf"))
    lazy val MAX_CLICKER = Config.getInt("lz.analytic.akka.clicker")
    lazy val bgClickers = for (i <- 0 until MAX_CLICKER) yield {
        system.actorOf(Props(new ClickActor).withDispatcher("clicker-dispatcher"), "ClickActor-" + i)
    }
    lazy val clickRouter = system.actorOf(Props[ClickActor].withRouter(RoundRobinRouter(routees = bgClickers)), "click_router")

    lazy val genomeActor = system.actorOf(Props(new GenomeActor).withDispatcher("genome-dispatcher"), "GenomeActor")

    lazy val adsActor = system.actorOf(Props(new AdvertiseActor).withDispatcher("advertise-dispatcher"), "AdvertiseActor")

    val LEZHI = "lezhi"
    lazy val kestrelClient = new KestrelClient(
        Config.getString("lezhi.kestrel.ips"),
        Config.getInt("lezhi.kestrel.pool.size"))

    private val PREDEFINED_TYPES = Set("insite", "outsite", "trending", "bshare", "personalized", "itemcf")

    def getXID(cookieId: String): String = {
        if (StringUtils.isNotBlank(cookieId)) {
            // bShare might put | in cookieId
            if (cookieId.contains("|"))
                cookieId.split("\\|")(0)
            else cookieId
        } else ""
    }

    /**
     * 获取v cookie并进行验证，若非法则生成新cookie
     * 最后返回cookieId和布尔值(表明是否是新生成的cookie)
     */
    def getVCookie(cookies: Map[String, Option[Cookie]],
                   req: HttpRequest[ReceivedMessage]): (String, Boolean) = {
        if (cookies.get("v").isDefined && cookies.get("v").get.isDefined) {
            val cookie = cookies.get("v").get.get.value
            val vCookie = if (cookie.startsWith("\"")) cookie.substring(1, cookie.length - 1) else cookie
            val xid = if (vCookie.contains("|")) vCookie.split("\\|")(0) else vCookie
            if (!CookieIdUtil.isValidCookieId(xid)) {
                (generateCookie(req), true)
            } else {
                (vCookie, false)
            }
        } else {
            (generateCookie(req), true)
        }

    }

    /**
     * 获取t cookie,判断该时间戳是否在bx_interval内
     */
    def verifyTCookie(cookies: Map[String, Option[Cookie]]): Boolean = {
        if (cookies.get("t").isDefined && cookies.get("t").get.isDefined) {
            val cookie = cookies.get("t").get.get.value
            System.currentTimeMillis - cookie.toLong > BX_INTERVAL
        } else {
            true
        }
    }

    def generateCookie(req: HttpRequest[ReceivedMessage]): String = {
        if (req.headers("User-Agent").length != 0) {
            CookieIdUtil.generateCookieId(req.headers("User-Agent").next,
                UrlFilter.getClientIp(req)) + "|::"
        } else
            ""
    }

    /**
     * bx cookie映射
     */
    def isBxCookie(isNewCreated: Boolean, cookies: Map[String, Option[Cookie]]): Boolean = {
        if (isNewCreated || verifyTCookie(cookies)) {
            // 使用随机因子控制bX映射的次数
            RandomUtils.nextInt(100) < BX_FACTOR
        } else {
            false
        }
    }

    def getTypes(typeStr: String, default: String = ""): List[String] = {
        val typeArr: Array[String] = for {
            t: String <- typeStr.split(",");
            if PREDEFINED_TYPES.contains(t)
        } yield t

        if (typeArr.isEmpty) {
            List(default)
        } else {
            typeArr.toList
        }
    }

    def join(recommedRs: Future[RecommendResult], adItems: Future[java.util.List[AdItem]]): Future[(RecommendResult, java.util.List[AdItem])] = {
        val p = Promise.interrupts[(RecommendResult, java.util.List[AdItem])](recommedRs, adItems)
        recommedRs.respond {
            case Throw(t) => p() = Throw(t)
            case Return(a) => adItems respond {
                case Throw(t) => p() = Return((a, new java.util.ArrayList[AdItem]))
                case Return(b) => p() = Return((a, b))
            }
        }
        p
    }

    def getRequestParams(adCountTmp: Int, typeStrs: Option[String], matchPic: PicType, count: Int, typeList: List[String]): List[RequestParam] = {
        val requestParams = new ListBuffer[RequestParam]()
        var paramOrder: Int = 1
        var adCount: Int = adCountTmp
        if (StringUtils.isNotBlank(typeStrs.getOrElse(""))) {
            val typeRes = Json.parse[List[Map[String, String]]](typeStrs.get)
            paramOrder = typeRes.size + 1
            adCount += (0 /: typeRes) {
                (sum, param) => sum + param("adCount").toInt
            }
            typeRes.foreach {
                param =>
                    requestParams += new RequestParam(param("order").toInt, "new", param("recommendType"), if (param("count").toInt == 0) count else param("count").toInt, param("adCount").toInt)
            }
        }
        //如果是中新网,直接添加insite和trending类型
        if (typeList.contains("personalized")) {
            requestParams += new RequestParam(paramOrder, "old", "personalized", count, adCount)
            requestParams += new RequestParam(paramOrder + 1, "old", "trending", count, adCount)
            paramOrder += 2
        }
        (0 until typeList.size).foreach {
            i =>
                requestParams += new RequestParam(paramOrder + i, "old", typeList(i), count, adCount)
        }

        requestParams.toList
    }

    def getRecommendParams(adCountTmp: Int, typeStrs: Option[String], matchPic: PicType, count: Int, typeListTmp: List[String]): (Int, List[RecommendTypeParam]) = {
        val types = new ListBuffer[RecommendTypeParam]()
        var paramOrder: Int = 1
        var adCount: Int = adCountTmp
        var typeList = typeListTmp
        if (StringUtils.isNotBlank(typeStrs.getOrElse(""))) {
            val typeRes = Json.parse[List[Map[String, String]]](typeStrs.get)
            paramOrder = typeRes.size + 1
            adCount += (0 /: typeRes) {
                (sum, param) => sum + param("adCount").toInt
            }
            typeRes.foreach {
                param =>
                    types += RecommendTypeParam.apply(param("order").toInt, RecommendType.valueOf(param("recommendType")).get, matchPic, if (param("count").toInt == 0) count else param("count").toInt)
            }
        }
        //如果是中新网,直接添加insite和trending类型
        if (typeList.contains("personalized")) {
            types += RecommendTypeParam.apply(paramOrder, RecommendType.valueOf("personalized").get, matchPic, count)
            types += RecommendTypeParam.apply(paramOrder + 1, RecommendType.valueOf("trending").get, matchPic, count)
            typeList = typeList.-("personalized")
            paramOrder += 2
        }
        (0 until typeList.size).foreach {
            i =>
                types += RecommendTypeParam.apply(paramOrder + i, RecommendType.valueOf(typeList(i)).get, matchPic, count)
        }

        return (adCount, types.toList)
    }

    def getAdCount(adCountTmp: Int, typeStrs: Option[String], typeListTmp: List[String]): Int = {
        var adCount: Int = adCountTmp
        var typeList = typeListTmp
        if (StringUtils.isNotBlank(typeStrs.getOrElse(""))) {
            val typeRes = Json.parse[List[Map[String, String]]](typeStrs.get)
            adCount += (0 /: typeRes) {
                (sum, param) => sum + param("adCount").toInt
            }
        }

        adCount
    }

    def getMatchPic(needPic: Boolean, autoMatch: Boolean, picMatch: String): PicType = {
        if (needPic == true) {
            if (autoMatch == true) {
                if (picMatch == "insite")
                    PicType.Insite
                else
                    PicType.Provided
            } else {
                PicType.Inpage
            }
        } else {
            PicType.Text
        }
    }
}