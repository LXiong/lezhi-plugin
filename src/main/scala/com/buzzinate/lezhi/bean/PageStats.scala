package com.buzzinate.lezhi.bean

import java.nio.ByteBuffer
import com.buzzinate.lezhi.model.StatPage.StatPage

class PageStats(var url: String = "",
    var title: String = "",
    var viewCounts: Long = 0,
    var showupCounts: Long = 0,
    var inClickCounts: Long = 0,
    var outClickCounts: Long = 0,
    var outClickToShowup: Double = 0) {

    def this(statPage: StatPage) {
        this("", "", 0, statPage.showUps, statPage.inClicks, statPage.outClicks, 0)

        if (statPage.showUps > 0) {
            this.outClickToShowup = (statPage.outClicks.toDouble / statPage.showUps) * 100
        }
    }
}
