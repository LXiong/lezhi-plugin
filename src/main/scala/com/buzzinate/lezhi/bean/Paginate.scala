package com.buzzinate.lezhi.bean

/**
 * pagination
 *
 * User: magic
 * Date: 13-7-22
 */
class Paginate(val pageNum: Int = 1, val pageSize: Int = 10) {

    def startIndex = {
        var start = pageSize * (pageNum - 1)
        if (start < 0) start = 0
        start
    }

    override def toString() = "pageSize=" + pageSize + ", pageNum=" + pageNum
}
