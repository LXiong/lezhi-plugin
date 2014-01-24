package com.buzzinate.lezhi.util

import java.net.{ URLDecoder, URLEncoder }

import org.scalatest.{ BeforeAndAfter, FunSuite }

class UrlFilterTest extends FunSuite with BeforeAndAfter {

    test("Test UrlFilter") {
        assert(UrlFilter.filter("http://bshare.cn/test?b=2&a=1&bsh_bid=123") ==
            "http://bshare.cn/test?a=1&b=2")

        assert(UrlFilter.filter("http://bshare.cn/test?lang=en&b=2&bsh_bid=123&a=1") ==
            "http://bshare.cn/test?a=1&b=2")

        assert(UrlFilter.filter("http://bshare.cn/test?bdclkid=123&b=2&jtss=123&a=1") ==
            "http://bshare.cn/test?a=1&b=2")
    }

    test("Test decoded url") {
        val decodedUrl = "http://www.lezhi.me/chooseType/麻烦-迷/chooseType/麻/迷"
        val url = URLEncoder.encode(decodedUrl, "UTF-8")

        val cPart1 = URLEncoder.encode("麻烦-迷", "UTF-8")
        val cPart2 = URLEncoder.encode("麻", "UTF-8")
        val cPart3 = URLEncoder.encode("迷", "UTF-8")

        val cEncodedUrl = "http://www.lezhi.me/chooseType/" + cPart1 + "/chooseType/" ++ cPart2 + "/" + cPart3
        assert(cEncodedUrl == "http://www.lezhi.me/chooseType/%E9%BA%BB%E7%83%A6-%E8%BF%B7/chooseType/%E9%BA%BB/%E8%BF%B7")

        val chineseEncodeUrl = UrlFilter.getChineseEncodedUrl(decodedUrl)
        assert(chineseEncodeUrl == cEncodedUrl)
    }
}