package com.buzzinate.lezhi.bean

/**
 * @author jeffrey created on 2012-9-3 下午4:09:33
 *
 */
class Page(var url: String,
    var title: String = "",
    var showup: Int = 0,
    var inClick: Int = 0,
    var outClick: Int = 0,
    var clickToShowup: Double = 0.00,
    var domain: String) {

}