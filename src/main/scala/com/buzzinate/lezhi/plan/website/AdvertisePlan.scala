package com.buzzinate.lezhi.plan.website

import java.util.UUID

import scala.Option.option2Iterable
import scala.annotation.serializable

import org.apache.commons.lang.StringUtils

import com.buzzinate.buzzads.domain.PublisherContactInfo
import com.buzzinate.buzzads.enums.{ PublisherContactRevMethod, PublisherContactStausEnum }
import com.buzzinate.lezhi.buzzads.AdvertiseDataServiceClient
import com.buzzinate.lezhi.model.{ AdBlackList, AdCloseReason, SiteConfig }
import com.buzzinate.lezhi.plan.BasicPlan
import com.buzzinate.lezhi.service.{ UserService, UuidSiteService }
import com.buzzinate.lezhi.util.Util
import com.codahale.jerkson.Json

import unfiltered.request._
import unfiltered.request.QParams._
import unfiltered.response._
import unfiltered.scalate.Scalate

/**
 * @author jeffrey created on 2012-11-23 下午4:08:52
 *
 */
object AdvertisePlan {
    val adDataServiceClient = new AdvertiseDataServiceClient
}

class AdvertisePlan extends BasicPlan {
    import AdvertisePlan._

    val MSG_SITE_DOESNOT_EXIST = "该站点不存在"
    val MSG_SITE_DOESNOT_BELONG = "该站点非你所有"
    val MSG_SITE_ADSOPEN_SUCCESS = "广告开启成功，点击设置进入广告投放设置"
    val MSG_SITE_ADSOPEN_FAILED = "广告开启失败, 请重试"
    val MSG_SITE_ADSCLOSE_SUCCESS = "广告关闭成功"
    val MSG_SITE_ADSCLOSE_FAILED = "广告关闭失败, 请重试"
    val MSG_CONTACTINFO_ADD_FAILED = "联系信息添加失败, 请重试"
    val MSG_CONTACTINFO_UPDATE_FAILED = "联系信息更新失败, 请重试"

    def intent = {
        case req @ GET(Path("/user/ads/setting/edit") & Params(params)) => {
            if (!isPublisher(req)) {
                Unauthorized ~> Scalate(req, "401.ssp")
            } else {
                val expected = for {
                    uuidStr <- lookup("uuid") is trimmed is
                        required("未指定参数") is
                        nonempty("参数不能为空")
                } yield {
                    var url = ""
                    var adCount = SiteConfig.DEFAULT_AD_COUNT
                    var row = SiteConfig.DEFAULT_ROW
                    var col = SiteConfig.DEFAULT_COL
                    var count = SiteConfig.DEFAULT_COL * SiteConfig.DEFAULT_ROW
                    var pic = SiteConfig.DEFAULT_PIC
                    var hvcolor = SiteConfig.DEFAULT_HVCOLOR_TEXT
                    var adEnabled = SiteConfig.DEFAULT_AD_ENABLED
                    var adBlackList: Seq[String] = Nil
                    var err = (0, MSG_SITE_CONFIG_SUCCESS_UPDATE)
                    val site = UuidSiteService.getUuidSiteByUuid(uuidStr.get)
                    if (site == null) {
                        err = (1, MSG_SITE_DOESNOT_EXIST)
                    } else {
                        if (getUserId(req) != site.getUserId) {
                            err = (1, MSG_SITE_DOESNOT_BELONG)
                        } else
                            try {
                                val uuid = UUID.fromString(uuidStr.get)
                                val siteConfig = SiteConfig.get(uuid.toString)
                                if (siteConfig.isDefined) {
                                    adCount = siteConfig.get.adCount
                                    count = siteConfig.get.col * siteConfig.get.row
                                    row = siteConfig.get.row
                                    col = siteConfig.get.col
                                    pic = siteConfig.get.pic
                                    hvcolor = siteConfig.get.hvcolor
                                    adEnabled = siteConfig.get.adEnabled
                                } else {
                                    val sf = new SiteConfig
                                    sf.uuid = uuid.toString
                                    if (!SiteConfig.update(sf)) {
                                        err = (1, MSG_SITE_ADSOPEN_FAILED)
                                    }
                                }
                                url = site.getUrl
                                adBlackList = AdBlackList.getKeywords(uuidStr.get)
                            } catch {
                                case ex: IllegalArgumentException => err = (2, MSG_SITE_CONFIG_INVALID_UUID)
                                case ex: Exception =>
                                    log.error(ex, "Exception when setting the advertise with uuid: " + uuidStr.get)
                                    err = (3, MSG_UNKNOWN_ERRPR)
                            }
                    }
                    Ok ~> Scalate(req, "ads/setting.ssp", ("err", err),
                        ("adCount", adCount),
                        ("count", count),
                        ("row", row),
                        ("col", col),
                        ("pic", pic),
                        ("hvcolor", hvcolor),
                        ("adEnabled", adEnabled),
                        ("uuid", uuidStr.get),
                        ("url", url),
                        ("adBlackList", Util.getStringFromList(adBlackList)),
                        ("link", "edit"))
                }
                expected(params) orFail { fails =>
                    BadRequest ~> Scalate(req, "400.ssp", ("fails", fails))
                }
            }
        }

        case req @ GET(Path("/user/ads/setting") & Params(params)) =>
            if (!isPublisher(req)) {
                Unauthorized ~> Scalate(req, "401.ssp")
            } else {
                val expected = for {
                    uuidStr <- lookup("uuid") is trimmed is
                        required("未指定参数") is
                        nonempty("参数不能为空")
                    count <- lookup("count") is
                        int { "'" + _ + "'' is not an integer" }
                } yield {
                    var err = (0, MSG_SITE_CONFIG_SUCCESS_UPDATE)
                    try {
                        val siteConfig = SiteConfig.get(uuidStr.get).get
                        siteConfig.adCount = count.get
                        siteConfig.adEnabled = true
                        if (!SiteConfig.update(siteConfig)) {
                            err = (1, MSG_SITE_CONFIG_FAILED_UPDATE)
                        }
                    } catch {
                        // invalide uuid
                        case ex: IllegalArgumentException => err = (2, MSG_SITE_CONFIG_INVALID_UUID)
                        case ex: Exception =>
                            log.error(ex, "Exception when setting the advertise with uuid: " + uuidStr.get)
                            err = (3, MSG_UNKNOWN_ERRPR)
                    }
                    Found ~> Location("/user/manage")
                }
                expected(params) orFail { fails =>
                    BadRequest ~> Scalate(req, "400.ssp", ("fails", fails))
                }
            }

        /**
         * update the blacklist
         */
        case req @ POST(Path("/user/ads/keywords/edit") & Params(params)) => {
            if (!isPublisher(req)) {
                Unauthorized ~> Scalate(req, "401.ssp")
            } else {
                val expected = for {
                    page <- lookup("page") is
                        int { "'" + _ + "'' 非正整数" }
                    uuid <- lookup("uuid") is trimmed
                    rmWords <- lookup("rmWords") is trimmed
                    adWords <- lookup("adWords") is trimmed
                } yield {
                    var err = (0, MSG_SUCCESS_UPDATE)
                    var keywords: Seq[String] = Nil
                    try {
                        val removes = Json.parse[List[String]](rmWords.get)
                        val adds = Json.parse[List[String]](adWords.get)
                        if (removes != adds) {
                            AdBlackList.delKeywords(uuid.get, removes)
                            AdBlackList.addKeywords(uuid.get, adds)
                        }
                        keywords = AdBlackList.getKeywords(uuid.get)
                    } catch {
                        case ex: Exception =>
                            log.error(ex, "Failed to edit ad blacklist: [uuid: " + uuid.toString + "]");
                            err = (1, MSG_UNKNOWN_ERRPR)
                    }
                    Ok ~> ResponseString("{\"result\":\"" + err._1 + "\", \"keywords\":\"" +
                        Util.getStringFromList(keywords) + "\", \"message\":\"" + err._2 + "\"}")
                }
                expected(params) orFail { fails =>
                    BadRequest ~> Scalate(req, "400.ssp", ("fails", fails))
                }
            }
        }

        case req @ POST(Path("/user/ads/contact") & Params(params)) => {
            if (!isPublisher(req)) {
                Unauthorized ~> Scalate(req, "401.ssp")
            } else {
                val expected = for {
                    name <- lookup("name") is trimmed is
                        required("未指定参数") is
                        nonempty("参数不能为空")
                    mobile <- lookup("mobile") is trimmed is
                        required("未指定参数") is
                        nonempty("参数不能为空")
                    qq <- lookup("qq") is trimmed is
                        required("未指定参数") is
                        nonempty("参数不能为空")
                    rcvMethod <- lookup("rcvMethod") is
                        int { "'" + _ + "'' 非正整数" }
                    rcvAccount <- lookup("rcvAccount") is trimmed is
                        required("未指定参数") is
                        nonempty("参数不能为空")
                    rcvName <- lookup("rcvName") is trimmed is
                        required("未指定参数") is
                        nonempty("参数不能为空")
                    rcvBank <- lookup("rcvBank") is trimmed
                } yield {
                    var err = (-1, "")
                    val validateRes = validateContactInfo(mobile.get, qq.get, rcvAccount.get)
                    var contactInfo = new PublisherContactInfo
                    contactInfo.setStatus(PublisherContactStausEnum.NORMAL)
                    if (validateRes._1) {
                        val userId = getUserId(req)
                        val user = UserService.getByUserId(userId)
                        try {
                            val info = adDataServiceClient.getPublisherContact(userId)
                            if (info.isDefined) {
                                contactInfo = info.get
                            }
                        } catch {
                            case ex: Exception =>
                                log.error(ex, MSG_UNKNOWN_ERRPR)
                                Found ~> Location("/user/ads/contact")
                        }

                        contactInfo.setUserId(userId)
                        contactInfo.setName(name.get)
                        contactInfo.setMobile(mobile.get)
                        if (user.nonEmpty) {
                            contactInfo.setEmail(user.get.getUserBasic.getName)
                        }
                        contactInfo.setQq(qq.get)
                        contactInfo.setReceiveMethod(PublisherContactRevMethod.findByValue(rcvMethod.get))
                        contactInfo.setReceiveName(rcvName.get)
                        contactInfo.setReceiveAccount(rcvAccount.get)
                        contactInfo.setReceiveBank(rcvBank.getOrElse(""))

                        try {
                            adDataServiceClient.saveOrUpdatePublisherContact(contactInfo)
                        } catch {
                            case ex: Exception =>
                                log.error(ex, "Exception when modify contactInfo. ")
                                err = (4, MSG_UNKNOWN_ERRPR)
                        }
                    } else {
                        err = (3, validateRes._2)
                    }

                    Found ~> Location("/user/ads/contact")
                }

                expected(params) orFail { fails =>
                    BadRequest ~> Scalate(req, "400.ssp", ("fails", fails))
                }
            }
        }

        case req @ GET(Path("/user/ads/contact/edit") & Params(params)) => {
            if (!isPublisher(req)) {
                Unauthorized ~> Scalate(req, "401.ssp")
            } else {
                val userId = getUserId(req)
                try {
                    val contactInfo = adDataServiceClient.getPublisherContact(userId)
                    Ok ~> Scalate(req, "ads/edit.ssp", ("contactInfo", contactInfo.getOrElse(new PublisherContactInfo)), ("link", "edit"))
                } catch {
                    case ex: Exception =>
                        log.error(ex, MSG_UNKNOWN_ERRPR)
                        Found ~> Location("/user/ads/contact")
                }
            }
        }

        case req @ GET(Path("/user/ads/enable") & Params(params)) => {
            if (!isPublisher(req)) {
                Unauthorized ~> Scalate(req, "401.ssp")
            } else {
                val userId = getUserId(req)
                try {
                    if (adDataServiceClient.getPublisherContact(userId).nonEmpty) {
                        Found ~> Location("/user/manage")
                    } else {
                        Ok ~> Scalate(req, "ads/edit.ssp", ("link", "enable"))
                    }
                } catch {
                    case ex: Exception =>
                        log.error(ex, MSG_UNKNOWN_ERRPR)
                        Found ~> Location("/user/manage")
                }
            }
        }

        case req @ GET(Path("/user/ads/contact") & Params(params)) => {
            if (!isPublisher(req)) {
                Unauthorized ~> Scalate(req, "401.ssp")
            } else {
                var err = (0, "")
                val userId = getUserId(req)
                var contactInfo: Option[PublisherContactInfo] = None
                try {
                    contactInfo = adDataServiceClient.getPublisherContact(userId)
                } catch {
                    case ex: Exception =>
                        err = (1, "对不起， 系统发生了异常, 请稍后重试")
                        log.error(ex, MSG_UNKNOWN_ERRPR)
                }
                Ok ~> Scalate(req, "ads/contact.ssp", ("contactInfo", contactInfo.getOrElse(new PublisherContactInfo)), ("link", "contact"), ("err", err))
            }
        }

        case req @ GET(Path("/user/ads/manage") & Params(params)) => {
            if (!isPublisher(req)) {
                Unauthorized ~> Scalate(req, "401.ssp")
            } else {
                Found ~> Location("/user/manage")

                /*val userId = getUserId(req)
                var contactInfo: Option[PublisherContactInfo] = None
                try {
                    contactInfo = adDataServiceClient.getPublisherContact(userId)
                } catch {
                    case ex: Exception =>
                        log.error(ex, MSG_UNKNOWN_ERRPR)
                        Found ~> Location("/user/ads/contact")
                }
                if (contactInfo.nonEmpty) {
                    val status = contactInfo.get.getStatus().getCode()
                    val userUuids = Site.getUuidInfo(userId)
                    val siteAdsListBuffer = ListBuffer[SiteAds]()
                    userUuids.foreach(
                        uuidInfo => {
                            val siteConfig = SiteConfig.get(UUID.fromString(uuidInfo._2))
                            if (siteConfig.nonEmpty) {
                                val siteAds = new SiteAds(uuidInfo._1, uuidInfo._2, UrlUtil.getFullDomainNameWithHttpPrefix(uuidInfo._3),
                                    siteConfig.get.adCount.getOrElse(SiteConfig.DEFAULT_AD_COUNT),
                                    siteConfig.get.adEnabled.getOrElse(SiteConfig.DEFAULT_AD_ENABLED))
                                siteAdsListBuffer.append(siteAds)
                                //if it's already frozen, close the ads
                                if (status == PublisherContactStausEnum.FROZEN.getCode() &&
                                    siteConfig.get.adEnabled.nonEmpty && siteConfig.get.adEnabled.get) {
                                    siteConfig.get.adEnabled = Some(false)
                                    siteConfig.get.update
                                }
                            }
                        })
                    Ok ~> Scalate(req, "ads/manage.ssp", ("siteAdsList", siteAdsListBuffer.toList), ("status", status), ("link", "manage"))
                } else {
                    Found ~> Location("/user/ads/enable")
                }*/
            }
        }

        case req @ GET(Path("/user/ads/open") & Params(params)) => {
            if (!isPublisher(req)) {
                Unauthorized ~> Scalate(req, "401.ssp")
            } else {
                var err = (-1, MSG_SITE_ADSOPEN_SUCCESS)
                val expected = for {
                    uuidStr <- lookup("uuid") is trimmed is
                        required("未指定参数") is
                        nonempty("参数不能为空")
                } yield {
                    if (isValidUser(req, uuidStr.get)) {
                        try {
                            val siteConfig = SiteConfig.get(uuidStr.get)
                            if (siteConfig.isDefined) {
                                siteConfig.get.adEnabled = true
                                if (!SiteConfig.update(siteConfig.get)) {
                                    (0, MSG_SITE_ADSOPEN_FAILED)
                                }
                            } else {
                                err = (1, MSG_SITE_DOESNOT_EXIST)
                            }
                        } catch {
                            case ex: IllegalArgumentException => err = (2, MSG_SITE_CONFIG_INVALID_UUID)
                            case ex: Exception =>
                                err = (4, MSG_UNKNOWN_ERRPR)
                                log.error(ex, "Failed to open ads of site " + uuidStr.get)
                        }
                    } else {
                        err = (2, MSG_INALID_UUID_USER)
                    }
                    Ok ~> ResponseString("{\"result\":\"" + err._1 + "\", \"message\":\"" + err._2 + "\"}")
                }
                expected(params) orFail { fails =>
                    BadRequest ~> Scalate(req, "400.ssp", ("fails", fails))
                }
            }
        }

        case req @ POST(Path("/user/ads/close") & Params(params)) => {
            if (!isPublisher(req)) {
                Unauthorized ~> Scalate(req, "401.ssp")
            } else {
                var err = (-1, MSG_SITE_ADSCLOSE_SUCCESS)
                val expected = for {
                    uuidStr <- lookup("uuid") is trimmed is
                        required("未指定参数") is
                        nonempty("参数不能为空")
                    closeReason <- lookup("reason") is trimmed
                } yield {
                    if (isValidUser(req, uuidStr.get)) {
                        try {
                            val siteConfig = SiteConfig.get(uuidStr.get)
                            if (siteConfig.isDefined) {
                                siteConfig.get.adEnabled = false
                                siteConfig.get.adCount = 0
                                if (!SiteConfig.update(siteConfig.get)) {
                                    (1, MSG_SITE_ADSCLOSE_FAILED)
                                }
                                if (closeReason.nonEmpty && StringUtils.isNotEmpty(closeReason.get)) {
                                    var closeReasonStr = closeReason.get
                                    //limit the close reason less than 100 characters 
                                    closeReasonStr = closeReasonStr.substring(0, if (closeReasonStr.length() > 100) 100 else closeReasonStr.length())
                                    AdCloseReason.add(uuidStr.get, closeReasonStr)
                                }
                            } else {
                                err = (2, MSG_SITE_DOESNOT_EXIST)
                            }
                        } catch {
                            case ex: IllegalArgumentException => err = (3, MSG_SITE_CONFIG_INVALID_UUID)
                            case ex: Exception =>
                                err = (4, MSG_UNKNOWN_ERRPR)
                                log.error(ex, "Failed to close ads of site " + uuidStr.get)
                        }
                    } else {
                        err = (2, MSG_INALID_UUID_USER)
                    }
                    Ok ~> ResponseString("{\"result\":\"" + err._1 + "\", \"message\":\"" + err._2 + "\"}")
                }
                expected(params) orFail { fails =>
                    BadRequest ~> Scalate(req, "400.ssp", ("fails", fails))
                }
            }
        }
    }

    //simple validation for the publisher contact info
    private def validateContactInfo(mobile: String, qq: String, account: String): (Boolean, String) = {
        //simple regex check for mobile
        //only character length check for qq and account
        var errMsg: String = ""
        val mobileRegex = "^((1[3-9]{1})+\\d{9})$"
        val maxLength = 30

        if (!mobile.matches(mobileRegex))
            errMsg += "请输入正确的手机号/n"
        if (qq.length() > maxLength)
            errMsg += "请填写正确位数的QQ号/n"
        if (account.length() > maxLength)
            errMsg += "请填写正确的银行账号"

        if (StringUtils.isEmpty(errMsg)) {
            (true, "")
        } else {
            (false, errMsg)
        }
    }

}
