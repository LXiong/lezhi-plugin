package com.buzzinate.lezhi.util

import com.html.escape.util.HtmlUtils

object ValidateUtil {
    val URL_REX = "http://[a-zA-Z0-9\\.\\-]+\\.([a-zA-Z]{2,4})(:\\d+)?(/[a-zA-Z0-9\\.\\-~!@#$%^&*+?:_/=<>]*)?";
    val COLOR_REX = "#([0-9a-fA-f]{3}|[0-9a-fA-f]{6})$"
    def isValidUrl(p: String) = {
        if (p.nonEmpty) p.matches(URL_REX)
        else true
    }

    def isValidColor(p: String) = {
        if (p.nonEmpty) p == "transparent" || p.matches(COLOR_REX)
        else true
    }

    def escapeContent(p: String) = {
        if (p.nonEmpty) {
            HtmlUtils.htmlEscape(p)
        } else p
    }

    // def main(args: Array[String]) {
    //     println(isValidColor("transparent"))
    // }

}