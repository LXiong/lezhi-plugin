package com.buzzinate.lezhi.bean

/**
 * @author jeffrey.ji <jeffrey.ji@buzzinate.com>
 * May 28, 2013 11:38:51 AM
 *
 */
@serializable
class RequestParam(
    var order: Int = 0,
    var style: String = "",
    var recommendType: String = "",
    var count: Int = 0,
    var adCount: Int = 0) {

    override def toString(): String = {
        "{order:" + order + ",style:" + style + ",recommendType:" + recommendType + ",count:" + count + ",adCount:" + adCount + "}"
    }
}

