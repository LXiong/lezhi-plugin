package com.buzzinate.lezhi.util

import java.net.URLDecoder
import java.text.SimpleDateFormat

import com.alibaba.fastjson.JSON

object GnomeUtil {
    case class Gnome(vid: String, url: String, app: String, ip: String, ua: String, time: Long, keyword: String, title: String)

    lazy val dateFormat = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss")
    lazy val dateFormat1 = new SimpleDateFormat("yyyy-MM-ddkk:mm:ss")

    lazy val BX_DELIMITER = "||"

    def parseJson(text: String): Gnome = {
        val json = JSON.parseObject(text)
        val app = json.getString("app")
        val datas = json.getString("data")
        val timeStr = json.getString("time")
        var time = 0l
        try {
            time = dateFormat.parse(timeStr.trim).getTime
        } catch {
            case e: Exception => {
                time = dateFormat1.parse(timeStr.trim).getTime
            }
        }
        val kvs = JSON.parseObject(datas)
        val url = kvs.getString("url")
        val vid = kvs.getString("vid")
        val ip = kvs.getString("ip")
        val ua = kvs.getString("ua")
        //如果keyword或title为空的话，保持一个字符长度的原始数据
        var keyword = " "
        var title = " "
        try {
            keyword = kvs.getString("keyword")
            title = kvs.getString("title")
            if (!keyword.isEmpty) {
                keyword = URLDecoder.decode(keyword, "UTF-8")
            }
            if (!title.isEmpty) {
                title = URLDecoder.decode(title, "UTF-8")
            }
        } catch {
            case e: Exception => {
                time = dateFormat1.parse(timeStr.trim).getTime
            }
        }

        Gnome(vid, url, app, ip, ua, time, keyword, title)
    }

    def main(args: Array[String]): Unit = {
        val testText = """
    {"app":"bshare","data":{"ip":"-633459206","keyword":"%E9%9A%8F%E4%BE%BF","title":"%EF%BF%BD%EF%BF%BD%D0%A1%CA%B1%EF%BF%BD%EF%BF%BD%C5%AE%EF%BF%BD%EF%BF%BD%EF%BF%BD%EF%BF%BD%C4%A8%EF%BF%BD%EF%BF%BD%C8%B9PK-%EF%BF%BD%EF%BF%BD%EF%BF%BD%EF%BF%BD-%EF%BF%BD%EF%BF%BD%EF%BF%BD%EF%BF%BD%EF%BF%BD%EF%BF%BD%7Crayli.com.cn","ua":"Mozilla/4.0(compatible;MSIE6.0;WindowsNT5.1;SV1;.NETCLR2.0.50727)","url":"http://fashion.rayli.com.cn/star/2013-05-30/L0002003004_1067815.html","uuid":"11bd5233-d037-4683-a6e3-7fff25922d3d","vid":"1IEGgTx9Do2jXcNkbCWi"},"time":"2013-06-0404:00:40","type":"view"}
	    """
        println(parseJson(testText).keyword)
    }
}