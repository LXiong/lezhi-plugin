package com.buzzinate.lezhi.plan.website

import java.awt.image.BufferedImage
import java.io.{ ByteArrayInputStream, ByteArrayOutputStream }
import java.net.URL
import java.text.SimpleDateFormat

import scala.collection.mutable.ListBuffer

import org.apache.commons.lang.StringUtils

import com.buzzinate.common.util.string.UrlUtil
import com.buzzinate.image.util.ImageUtil
import com.buzzinate.lezhi.bean.PageData
import com.buzzinate.lezhi.model.{ Metadata, UuidPath, UuidUrls }
import com.buzzinate.lezhi.plan.BasicPlan
import com.buzzinate.lezhi.plugin.PluginConstant.{ elasticClient, recommendClient }
import com.buzzinate.lezhi.service.UuidSiteService
import com.buzzinate.lezhi.util.{ MetricsUtil, UrlFilter, Util }
import com.buzzinate.lezhi.util.ValidateUtil.escapeContent
import com.buzzinate.up.UpYunClient
import com.codahale.jerkson.Json
import com.yammer.metrics.MetricRegistry

import javax.imageio.ImageIO
import unfiltered.filter.request.{ MultiPart, MultiPartParams }
import unfiltered.request._
import unfiltered.request.QParams._
import unfiltered.response._
import unfiltered.scalate.Scalate

/**
 * @author jeffrey created on 2012-8-21 下午7:00:57
 *
 */
class ContentPlan extends BasicPlan {

    val PIC_MAX_NUM = 10
    val MAX_PIC_SIZE = 1024 * 1024;

    val MSG_NO_CONTENT = "尚无内容"
    val MSG_INVALID_URL = "非法URL，该URL不存在于你的网站"

    val HIDDEN_STATUS = "hidden"
    val PRIOR_STATUS = "prior"
    val NORMAL_STATUS = "normal"

    private val sdf = new SimpleDateFormat("yyyy-MM-dd")
    private val upyun = new UpYunClient("lezhi", "buzzinate", "buzzinate")

    private val timer = MetricsUtil.metrics.timer(MetricRegistry.name(classOf[ContentPlan], "contentList"))
    private val meter = MetricsUtil.metrics.meter(MetricRegistry.name(classOf[ContentPlan], "contentListMeter", "requests"))

    def intent = {
        /**
         * search by status or url or time
         */
        case req @ GET(Path("/user/content") & Params(params)) => {
            if (!isPublisher(req)) {
                Unauthorized ~> Scalate(req, "401.ssp")
            } else {
                val expected = for {
                    uuidStr <- lookup("uuid") is trimmed is
                        required("uuid未指定") is
                        nonempty("uuid不能为空")
                    url <- lookup("url") is trimmed
                    title <- lookup("articleTitle") is trimmed
                    status <- lookup("status") is trimmed
                    pagination <- lookup("pagination") is trimmed is
                        int { "'" + _ + "'' is not an int" }
                } yield {
                    val site = UuidSiteService.getUuidSiteByUuid(uuidStr.get)
                    if (site != null) {
                        val domain = UrlUtil.getDomainName(site.getUrl)
                        val sitePrefixes: Seq[String] = UuidPath.getUuidPathsByStatus(uuidStr.get, 1)
                        var err = (1, MSG_NO_CONTENT)
                        var metadatas: Seq[Metadata] = Nil
                        var totalSize = 1
                        var start = pagination.getOrElse(0) * PAGE_SIZE
                        try {
                            //按照地址查找
                            if (StringUtils.isNotEmpty(url.getOrElse(""))) {
                                val vurl = UrlFilter.getChineseEncodedUrl(url.get).toLowerCase
                                if (UrlUtil.isUrlUnderDomain(vurl, domain)) {
                                    metadatas = elasticClient.getByUrls(List(vurl))
                                    metadatas = filterMetadata(metadatas, sitePrefixes)
                                } else {
                                    err = (1, MSG_INVALID_URL)
                                }
                            } //按照标题模糊搜索
                            else if (StringUtils.isNotEmpty(title.getOrElse(""))) {
                                val result = elasticClient.searchUrls(sitePrefixes, title.get, start, PAGE_SIZE)
                                totalSize = result._1
                                metadatas = result._2
                                metadatas = filterMetadata(metadatas, sitePrefixes)
                            } //按照状态查找
                            else if (StringUtils.equals(status.getOrElse(""), HIDDEN_STATUS) || StringUtils.equals(status.getOrElse(""), PRIOR_STATUS)) {
                                totalSize = UuidUrls.getUrlsNum(uuidStr.get, status.get)
                                val urls = UuidUrls.getPaginationUrls(uuidStr.get, status.get, start, PAGE_SIZE)
                                metadatas = elasticClient.getByUrls(urls)
                                metadatas = filterMetadata(metadatas, sitePrefixes)
                            } //查找全部
                            else {
                                val matchResult = elasticClient.matchAll(sitePrefixes, start, 10)
                                totalSize = matchResult._1
                                metadatas = matchResult._2
                            }
                        } catch {
                            case ex: Exception =>
                                log.error(ex, "Failed to get content from elasticClient");
                        }

                        val pageDatas = metadatas.map { metadata =>
                            val defaultTitle = metadata.title
                            val (imgUrl, imgTitle) = getImgUrlAndTitle(Util.getImgsList(metadata.url, PIC_MAX_NUM), defaultTitle)
                            new PageData(metadata, imgUrl, imgTitle, metadata.keywords)
                        }.sortWith((data1, data2) => data1.metadata.url.compareTo(data2.metadata.url) < 0)
                        Ok ~> Scalate(req, "user/list.ssp",
                            ("pageDatas", pageDatas),
                            ("pagination", pagination.getOrElse(0)),
                            ("pageSize", PAGE_SIZE),
                            ("totalSize", totalSize),
                            ("uuid", uuidStr.get),
                            ("articleTitle", title.getOrElse("")),
                            ("siteName", site.getName),
                            ("isAdmin", isAdmin(req)),
                            ("status", status.getOrElse("")),
                            ("link", "listPages"))
                    } else {
                        Unauthorized ~> Scalate(req, "401.ssp")
                    }
                }
                expected(params) orFail { fails =>
                    BadRequest ~> Scalate(req, "400.ssp", ("fails", fails))
                }
            }

        }

        /**
         * update content status or title
         */
        case req @ POST(Path("/user/delete") & Params(params)) => {
            if (!isPublisher(req)) {
                Unauthorized ~> Scalate(req, "401.ssp")
            } else {
                val expected = for {
                    uuidStr <- lookup("uuid") is trimmed is
                        required("uuid未指定") is
                        nonempty("uuid不能为空")
                    urlStrs <- lookup("urls") is trimmed is
                        required("未指定参数") is
                        nonempty("参数不能为空")
                } yield {
                    var err = (1, MSG_UNKNOWN_ERRPR)
                    val site = UuidSiteService.getUuidSiteByUuid(uuidStr.get)
                    if (site != null) {
                        try {
                            val urls = Json.parse[List[String]](urlStrs.get).map(UrlFilter.getChineseEncodedUrl(_).toLowerCase)
                            UuidUrls.delUrls(uuidStr.get, urls)
                            elasticClient.deleteIndexes(urls)
                            err = (0, "成功删除")
                        } catch {
                            case ex: Exception =>
                                log.error(ex, "Failed to change content status: [uuid: " + uuidStr.get + ",url=" + urlStrs.get + "]");
                        }
                        Ok ~> ResponseString("{\"result\":\"" + err._1 + "\", \"message\":\"" + err._2 + "\"}")
                    } else {
                        Unauthorized ~> Scalate(req, "401.ssp")
                    }
                }
                expected(params) orFail { fails =>
                    BadRequest ~> Scalate(req, "400.ssp", ("fails", fails))
                }
            }
        }

        /**
         * update content status or title
         */
        case req @ POST(Path("/user/editStatus") & Params(params)) => {
            if (!isPublisher(req)) {
                Unauthorized ~> Scalate(req, "401.ssp")
            } else {
                val expected = for {
                    uuidStr <- lookup("uuid") is trimmed is
                        required("uuid未指定") is
                        nonempty("uuid不能为空")
                    url <- lookup("url") is trimmed is required("未指定url") is
                        nonempty("url不能为空")
                    oldStatus <- lookup("oldStatus") is trimmed is required("未指定oldStatus") is
                        nonempty("oldStatus不能为空")
                    newStatus <- lookup("newStatus") is trimmed is required("未指定newStatus") is
                        nonempty("newStatus不能为空")
                } yield {
                    var err = (1, MSG_UNKNOWN_ERRPR)
                    val site = UuidSiteService.getUuidSiteByUuid(uuidStr.get)
                    if (site != null) {
                        try {
                            val oldStatusStr = oldStatus.get
                            val newStatusStr = newStatus.get
                            val metadata = new Metadata
                            metadata.url = url.get
                            metadata.status = newStatusStr

                            elasticClient.updateMetadata(metadata)
                            if (oldStatusStr == NORMAL_STATUS && (newStatusStr == PRIOR_STATUS || newStatusStr == HIDDEN_STATUS)) {
                                UuidUrls.addUrls(uuidStr.get, newStatusStr, List(url.get))
                            } else if (newStatusStr == NORMAL_STATUS && (oldStatusStr == PRIOR_STATUS || oldStatusStr == HIDDEN_STATUS)) {
                                UuidUrls.delUrls(uuidStr.get, List(url.get))
                            } else {
                                UuidUrls.updateStatus(uuidStr.get, url.get, newStatusStr)
                            }
                            err = (0, MSG_SUCCESS_UPDATE)
                        } catch {
                            case ex: Exception =>
                                log.error(ex, "Failed to change content status: [uuid: " + uuidStr.get + "]");
                        }
                        Ok ~> ResponseString("{\"result\":\"" + err._1 + "\", \"message\":\"" + err._2 + "\"}")
                    } else {
                        Unauthorized ~> Scalate(req, "401.ssp")
                    }
                }
                expected(params) orFail { fails =>
                    BadRequest ~> Scalate(req, "400.ssp", ("fails", fails))
                }
            }
        }

        /**
         * update the blacklist
         */
        case req @ POST(Path("/user/editKeywords") & Params(params)) => {
            if (!isPublisher(req)) {
                Unauthorized ~> Scalate(req, "401.ssp")
            } else {
                val expected = for {
                    url <- lookup("url") is trimmed is required("未指定url") is
                        nonempty("url不能为空")
                    rmWords <- lookup("rmWords") is trimmed
                    adWords <- lookup("adWords") is trimmed
                } yield {
                    var err = (0, MSG_SUCCESS_UPDATE)
                    var keywords: Seq[String] = Nil
                    try {
                        val metadata = elasticClient.getByUrls(List(UrlFilter.getChineseEncodedUrl(url.get).toLowerCase))(0)
                        var keywords = metadata.blackKeywords.split(",").toSet

                        val removes = Json.parse[Set[String]](rmWords.get)
                        val adds = Json.parse[Set[String]](adWords.get)
                        if (removes != adds) {
                            adds.foreach(keywords += _)
                            removes.foreach(keywords -= _)
                            val mdata = new Metadata(metadata.url)
                            mdata.blackKeywords = Util.getStringFromList(keywords.toSeq)
                            elasticClient.updateMetadata(mdata)
                        }
                    } catch {
                        case ex: Exception =>
                            log.error(ex, "Failed to edit blacklist: [url: " + url.toString + "]");
                            ex.printStackTrace
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

        case req @ POST(Path("/user/editTitle") & Params(params)) =>
            if (!isPublisher(req)) {
                Unauthorized ~> Scalate(req, "401.ssp")
            } else {
                val expected = for {
                    title <- lookup("title") is trimmed is
                        required("未指定参数") is
                        nonempty("参数不能为空")
                    url <- lookup("url") is trimmed is
                        required("未指定参数") is
                        nonempty("参数不能为空")
                } yield {
                    var err = (0, MSG_PAGE_INFO_SUCCESS_UPDATE)
                    var escapedTitle = ""
                    try {
                        escapedTitle = title.map(escapeContent).get
                        val metadata = new Metadata
                        metadata.url = url.get
                        metadata.title = escapedTitle
                        elasticClient.updateMetadata(metadata)
                    } catch {
                        case ex: Exception =>
                            log.info("Exception: " + ex.getMessage)
                            err = (1, MSG_PAGE_INFO_FAILED_UPDATE)
                    }
                    Ok ~> ResponseString("{\"result\":\"" + err._1 + "\", \"title\":\"" + escapedTitle +
                        "\", \"message\":\"" + err._2 + "\"}")
                }

                expected(params) orFail { fails =>
                    BadRequest ~> Scalate(req, "400.ssp", ("fails", fails))
                }

            }

        case POST(Path("/user/uploadPic") & MultiPart(req)) =>
            if (!isPublisher(req)) {
                Unauthorized ~> Scalate(req, "401.ssp")
            } else {
                //Represents uploaded file and params loaded into memory
                val disk = MultiPartParams.Disk(req)
                var err = (0, MSG_PAGE_IMG_SUCCESS_UPDATE)
                var imgUrl: String = ""
                //get the upload pic file and url param
                (disk.files("Filedata"), disk.params("url")) match {
                    case (Seq(pic, _*), url) =>
                        if (StringUtils.isEmpty(url(0))) {
                            BadRequest ~> BadRequest ~> Scalate(req, "error.ssp", ("message", "no valid article url found"))
                        } else {
                            try {
                                if (pic.bytes.length < MAX_PIC_SIZE) {
                                    //resize the pic and upload to upyun and return the url got from upyun
                                    imgUrl = processUploadFile(ImageIO.read(new ByteArrayInputStream(pic.bytes)))
                                    val metadata = new Metadata
                                    metadata.url = UrlFilter.getChineseEncodedUrl(url(0)).toLowerCase
                                    metadata.thumbnail = imgUrl
                                    elasticClient.updateMetadata(metadata)
                                } else {
                                    //over size
                                    err = (5, MAG_PAGE_IMG_OVER_SIZE)
                                }
                            } catch {
                                case ex: Exception =>
                                    log.info("Exception: " + ex.getMessage)
                                    err = (1, MSG_PAGE_IMG_FAILED_UPDATE)
                            }
                            Ok ~> ResponseString("{\"result\":\"" + err._1 + "\", \"imgUrl\":\"" + imgUrl +
                                "\", \"message\":\"" + err._2 + "\"}")
                        }

                    case _ =>
                        err = (4, MSG_UNKNOWN_ERRPR)
                        Ok ~> ResponseString("{\"result\":\"" + err._1 + "\", \"imgUrl\":\"" + imgUrl +
                            "\", \"message\":\"" + err._2 + "\"}")
                }
            }

        case req @ POST(Path("/user/uploadPic") & Params(params)) =>
            if (!isPublisher(req)) {
                Unauthorized ~> Scalate(req, "401.ssp")
            } else {
                val expected = for {
                    picUrl <- lookup("picUrl") is trimmed is
                        required("未指定参数") is
                        nonempty("参数不能为空")
                    url <- lookup("url") is trimmed is
                        required("未指定参数") is
                        nonempty("参数不能为空")
                } yield {
                    var err = (0, MSG_PAGE_IMG_SUCCESS_UPDATE)
                    var imgUrl: String = ""
                    try {
                        //check whether the pic over size(max 1M)
                        if (new URL(picUrl.get).openConnection().getContentLength() < MAX_PIC_SIZE) {
                            //resize the pic and upload to upyun and return the url got from upyun
                            imgUrl = processUploadFile(ImageIO.read(new URL(picUrl.get)))
                            val metadata = new Metadata
                            metadata.url = url.get
                            metadata.thumbnail = imgUrl
                            elasticClient.updateMetadata(metadata)
                        } else {
                            //over size
                            err = (5, MAG_PAGE_IMG_OVER_SIZE)
                        }
                    } catch {
                        case ex: Exception =>
                            log.info("Exception: " + ex.getMessage)
                            err = (1, MSG_PAGE_IMG_FAILED_UPDATE)
                    }
                    Ok ~> ResponseString("{\"result\":\"" + err._1 + "\", \"imgUrl\":\"" + imgUrl +
                        "\", \"message\":\"" + err._2 + "\"}")
                }
                expected(params) orFail { fails =>
                    BadRequest ~> Scalate(req, "400.ssp", ("fails", fails))
                }
            }

        case req @ POST(Path("/user/recrawl") & Params(params)) =>
            if (!isPublisher(req)) {
                Unauthorized ~> Scalate(req, "401.ssp")
            } else {
                val expected = for {
                    uuidStr <- lookup("uuid") is trimmed is
                        required("uuid未指定") is
                        nonempty("uuid不能为空")
                    url <- lookup("url") is trimmed is
                        required("未指定参数") is
                        nonempty("参数不能为空")
                    status <- lookup("status") is trimmed is
                        required("未指定参数") is
                        nonempty("参数不能为空")
                } yield {
                    var err = (0, MSG_SITE_CONFIG_SUCCESS_UPDATE)

                    try {
                        //reset the status to normal and clear the blacklist keywords
                        val site = UuidSiteService.getUuidSiteByUuid(uuidStr.get)
                        if (site != null) {
                            UuidUrls.delUrls(uuidStr.get, List(url.get))
                            val metadata = new Metadata
                            metadata.url = url.get
                            metadata.blackKeywords = ""
                            metadata.status = "normal"
                            //call the backend
                            recommendClient.recrawl(url.get)
                        }
                    } catch {
                        case ex: Exception =>
                            log.info("Exception: " + ex.getMessage)
                            err = (1, MSG_SITE_CONFIG_FAILED_UPDATE)
                    }
                    Ok ~> ResponseString("{\"result\":\"" + err._1 + "\", \"message\":\"" + err._2 + "\"}")
                }
                expected(params) orFail { fails =>
                    BadRequest ~> Scalate(req, "400.ssp", ("fails", fails))
                }
            }
    }

    def processUploadFile(img: BufferedImage) = {
        val byteStream = new ByteArrayOutputStream()
        try {
            val imgName = System.currentTimeMillis() + ".jpg";
            val resized = ImageUtil.resizeImage(img)
            ImageIO.write(resized, "jpg", byteStream)
            val result = upyun.upload("/" + imgName, byteStream.toByteArray(), true)
            result.url
        } finally {
            byteStream.close()
        }

    }

    def getImgUrlAndTitle(imgs: List[(String, String)], defaultTitle: String): (String, String) = {
        val imgUrls = ListBuffer[String]()
        val imgTitles = ListBuffer[String]()
        imgs.foreach { img =>
            imgUrls.append(img._1)
            if (StringUtils.isNotEmpty(img._2)) imgTitles.append(img._2)
            else imgTitles.append(defaultTitle)
        }
        (Util.getStringFromList(imgUrls.toList), Util.getStringFromList(imgTitles.toList))
    }

    private def filterMetadata(metadatas: Seq[Metadata], sitePrefixes: Seq[String]): Seq[Metadata] = {
        metadatas.filter { metadata => isInSitePrefix(sitePrefixes, metadata.url) }
    }

    private def isInSitePrefix(sitePrefixes: Seq[String], url: String): Boolean = {
        sitePrefixes.foreach { sitePrefix =>
            if (url.startsWith(sitePrefix)) {
                return true
            }
        }
        false
    }

}
