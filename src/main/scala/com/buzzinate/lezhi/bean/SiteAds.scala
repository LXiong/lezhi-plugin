package com.buzzinate.lezhi.bean

class SiteAds(var siteName: String = "",
    var uuid: String = "",
    var domain: String = "",
    var adsCount: Int = 0,
    var adsStatus: Boolean = false,
    var getCode: Boolean = false) {
    var pluginType: String = "fixed"
    var pic: Boolean = true
}