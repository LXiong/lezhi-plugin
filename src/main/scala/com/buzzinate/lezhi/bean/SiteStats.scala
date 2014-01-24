package com.buzzinate.lezhi.bean

import com.buzzinate.lezhi.model.{ StatSite, SiteStatus }
import com.buzzinate.lezhi.model.SiteConfig

class SiteStats(var name: String = "",
    var url: String = "",
    var pluginType: String = "",
    var pic: Boolean = false,
    var articleNum: Long = 0,
    var showupCounts: Long = 0,
    var outClickCounts: Long = 0,
    var clickToShowup: Double = 0,
    var adEnabled: Boolean = false) {
    
    def this(siteStatus: SiteStatus.SiteStatus, statSite: StatSite.StatSite) {
        this()
        
        if (siteStatus != null) {
            this.articleNum = siteStatus.urlNumber
        }
        
        if (statSite != null) {
            this.showupCounts = statSite.showups
            this.outClickCounts = statSite.outClicks

            if (statSite.showups > 0) {
                this.clickToShowup = (statSite.outClicks.toDouble / statSite.showups) * 100
            }
        }
    }
}

