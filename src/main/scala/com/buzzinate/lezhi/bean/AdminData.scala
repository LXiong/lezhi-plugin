package com.buzzinate.lezhi.bean

import com.buzzinate.lezhi.model.AdminCounts.AdminCounts
import java.util.Date

/**
 * @author jeffrey created on 2012-9-21 上午10:32:30
 *
 */
class AdminData(var date: String,
    var adminCount: AdminCounts,
    var totalShowup: Long = 0L,
    var totalClick: Long = 0L,
    var totalClickRate: Double = 0.00,
    var pluginClickRate: Double = 0.00,
    var buttonClickRate: Double = 0.00,
    var picClickRate: Double = 0.00,
    var textClickRate: Double = 0.00,
    var totalShowupT: Long = 0L,
    var totalClickT: Long = 0L,
    var totalClickRateT: Double = 0.00,
    var pluginClickRateT: Double = 0.00,
    var buttonClickRateT: Double = 0.00,
    var picClickRateT: Double = 0.00,
    var textClickRateT: Double = 0.00) {

}
