package com.buzzinate.lezhi.service

import java.util.Date
import org.scalatest.{ BeforeAndAfter, FunSuite }
import com.buzzinate.lezhi.model.StatPage.StatPage
import com.buzzinate.lezhi.util.Util
import java.util.Calendar

/**
 * @author jeffrey.ji <jeffrey.ji@buzzinate.com>
 * Aug 5, 2013 7:15:35 PM
 *
 */
class StatPageServiceTest extends FunSuite with BeforeAndAfter {
    val urlUuid = "f8a4a53f-438a-4ffa-939f-7f313a7e3e79"
    val siteUuid = "9c54915f-4ef0-4a16-9988-2ca87cae9d0a"

    private val calendar = Calendar.getInstance();
    calendar.set(2012, 5, 24)
    private val pageDay = new java.sql.Date(calendar.getTime.getTime)
    calendar.set(2012, 5, 25)
    private val pageDay2 = new java.sql.Date(calendar.getTime.getTime)

    val statPage1 = StatPage(0, Util.uuidToByte(urlUuid), Util.uuidToByte(siteUuid), pageDay, 1, 22, 333)
    val statPage2 = StatPage(0, Util.uuidToByte(urlUuid), Util.uuidToByte(siteUuid), pageDay2, 11, 2, 3)

    test("test stat page service test") {
//        StatPageService.updateStatPage(statPage1)
//        StatPageService.updateStatPage(statPage2)
//
//        val statPage = StatPageService.getStatPage(urlUuid, pageDay).get
    }
}
