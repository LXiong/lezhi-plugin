package com.buzzinate.lezhi.util

import java.net.{ URLDecoder, URLEncoder }
import java.util.regex._

import scala.util.matching.Regex

import org.apache.commons.lang.StringUtils

import unfiltered.netty.ReceivedMessage
import unfiltered.request.HttpRequest

object UrlFilter {

    val FILTER_PARAMS = Array(
        Pattern.compile("^blz-.*$"), // lezhi track
        Pattern.compile("^bsh_.*$"), // bshare track
        Pattern.compile("^utm_.*$"), // google track
        Pattern.compile("^bdclkid$"), // baidu track
        Pattern.compile("^jtss.*$"), // jiathis track
        Pattern.compile("^request_locale$"),
        Pattern.compile("^lang$"),
        Pattern.compile("^lange$"),
        Pattern.compile("^langpair$"))

    val pattern = new Regex("[\u4E00-\u9FA5]|[（）！？。，《》{}【】“”·、：；‘’……]")
    val imgPattern = new Regex("(http|https)://[A-Za-z0-9\\./=\\?%_~@#:;\\+\\-]+")
    
    def filter(orgUrl: String): String = {
        val hashPos = orgUrl.indexOf('#')
        val url =
            if (hashPos > 0) orgUrl.substring(0, hashPos)
            else orgUrl

        val quesPos = url.indexOf('?')
        if (quesPos < 0) return url

        val mainUrl = url.substring(0, quesPos)
        val params = url.substring(quesPos + 1).split("&")
        val filteredParams = params.filter { p =>
            val pname = p.split('=')(0)
            FILTER_PARAMS.forall { fp =>
                val m = fp.matcher(pname)
                !m.matches
            }
        }

        var resUrl: StringBuilder = new StringBuilder(mainUrl)
        if (!filteredParams.isEmpty) resUrl.append("?")
        filteredParams.sorted.foreach { p =>
            resUrl.append(p + "&")
        }
        if (resUrl.last == '&')
            resUrl.deleteCharAt(resUrl.size - 1)
        resUrl.toString
    }

    def getClientIp(request: HttpRequest[ReceivedMessage]): String = {
        // Http cache will mask the client ip, try X-Forwarded-For first
        var ip = if (request.headers("x-forwarded-for").isEmpty) "" else request.headers("x-forwarded-for").next
        if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = if (request.headers("Proxy-Client-IP").isEmpty) "" else request.headers("Proxy-Client-IP").next
        }
        if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = if (request.headers("WL-Proxy-Client-IP").isEmpty) "" else request.headers("WL-Proxy-Client-IP").next
        }
        if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.remoteAddr
        }
        if (StringUtils.isEmpty(ip) || "unknown".equalsIgnoreCase(ip)) {
            return ""
        } else if (ip.contains(",")) {
            // Multiple ips, left-most is the client ip address but sometimes it might be unknown
            val ips = ip.split(",")
            for (
                val s <- ips if s.indexOf('.') > 0
            ) ip = s
        }

        return ip.trim()
    }

    /**
     * if the url contains chinese, encode it with utf8
     */
    def getChineseEncodedUrl(decodedUrl: String): String = {
        val matchedRes = pattern.findAllIn(decodedUrl).toList
        if (matchedRes != null && matchedRes.size > 0) {
            val sb: StringBuilder = new StringBuilder()
            var index: Int = 0
            matchedRes.foreach { matchStr =>
                sb.append(decodedUrl.substring(index, decodedUrl.indexOf(matchStr, index)))
                sb.append(URLEncoder.encode(matchStr, "UTF-8"))
                index = decodedUrl.indexOf(matchStr, index) + matchStr.length()
            }
            sb.append(decodedUrl.substring(index))
            return sb.toString
        }
        return decodedUrl
    }

    def isValidImgUrl(url: String): Boolean = {
        imgPattern.findFirstMatchIn(url).isDefined
    }
}