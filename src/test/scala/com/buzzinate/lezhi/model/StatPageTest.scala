package com.buzzinate.lezhi.model

import java.util.{Date, Calendar, UUID}
import org.scalatest.{BeforeAndAfter, FunSuite}
import com.buzzinate.common.util.DateTimeUtil
import com.buzzinate.common.util.string.StringUtil
import com.buzzinate.lezhi.util.Util
import com.buzzinate.common.util.UuidUtil
import java.sql.{Date => SqlDate}
import com.buzzinate.lezhi.model.StatPage
import java.sql.Date
import com.buzzinate.lezhi.bean.Paginate

class StatPageTest extends FunSuite with BeforeAndAfter {

    private val TEST_URL = "http://www.bshare.cn/abcd"
    private val TEST_URL2 = "http://www.bshare.cn/efgh"
    private val TEST_UUID = UUID.fromString("f8a4a53f-438a-4ffa-939f-7f313a7e3e79")
    private val TEST_UUID2 = UUID.fromString("a8a4a53f-438a-4ffa-939f-7f313a7e3e70")

    private val calendar = Calendar.getInstance();
    calendar.set(2012, 5, 24)
    private val TEST_DAY = calendar.getTime
    private val TEST_START_DAY = DateTimeUtil.getDateDay(TEST_DAY)
    private val TEST_SQL_DAY = new SqlDate(TEST_START_DAY.getTime)
    calendar.set(2012, 5, 25)
    private val TEST_DAY_END = calendar.getTime
    private val TEST_END_DAY = DateTimeUtil.getDateDay(TEST_DAY_END)

	before {
        StatPage.del(TEST_URL, TEST_DAY)
        StatPage.del(TEST_URL2, TEST_DAY)
    }

    after {
        StatPage.del(TEST_URL, TEST_DAY)
        StatPage.del(TEST_URL2, TEST_DAY)
    }

    test("test batch page") {
        var statPage1 = StatPage.getStatPages(TEST_URL, TEST_DAY, TEST_DAY)
        val statPageData1 = if (statPage1.isEmpty) (0, 0, 0) else (statPage1(0).inClicks, statPage1(0).outClicks, statPage1(0).showUps)
        var statPage2 = StatPage.getStatPages(TEST_URL2, TEST_DAY, TEST_DAY)
        val statPageData2 = if (statPage2.isEmpty) (0, 0, 0) else (statPage2(0).inClicks, statPage2(0).outClicks, statPage2(0).showUps)

        val test = new java.util.HashMap[(String, String), StatPage.StatPage]()
        test.put((TEST_URL, DateTimeUtil.formatDate(TEST_DAY)), StatPage.StatPage(0, Util.urlToBytes(TEST_URL), Util.uuidToByte(TEST_UUID.toString), TEST_SQL_DAY, 1, 2, 3))
        test.put((TEST_URL2, DateTimeUtil.formatDate(TEST_DAY)), StatPage.StatPage(0, Util.urlToBytes(TEST_URL2), Util.uuidToByte(TEST_UUID.toString), TEST_SQL_DAY, 11, 12, 13))
                
        (0 to 1).foreach(i => StatPage.batchPage(test))

        statPage1 = StatPage.getStatPages(TEST_URL, TEST_DAY, TEST_DAY)
        val updateStatPageData1 = if (statPage1.isEmpty) (0, 0, 0) else (statPage1(0).inClicks, statPage1(0).outClicks, statPage1(0).showUps)
        statPage2 = StatPage.getStatPages(TEST_URL2, TEST_DAY, TEST_DAY)
        val updateStatPageData2 = if (statPage2.isEmpty) (0, 0, 0) else (statPage2(0).inClicks, statPage2(0).outClicks, statPage2(0).showUps)

        assert(updateStatPageData1 === (statPageData1._1 + 2, statPageData1._2 + 4, statPageData1._3 + 6), "Got wrong StatPage")
        assert(updateStatPageData1 === (statPageData1._1 + 22, statPageData1._2 + 24, statPageData1._3 + 26), "Got wrong StatPage")
    }

    test("test get page stat by uuid") {
        var pageMap = new java.util.HashMap[(String, String), StatPage.StatPage]()
        (1 to 15).foreach{ i =>
            val urlDayKey = (TEST_URL + i, DateTimeUtil.formatDate(TEST_SQL_DAY))
            pageMap.put(urlDayKey, new StatPage.StatPage(0, Util.urlToBytes(TEST_URL + i), Util.uuidToByte(TEST_UUID2.toString), TEST_SQL_DAY, i, i, i))
        }
        StatPage.batchPage(pageMap)

        var statPagesResult = StatPage.getStatPagesByUuid(TEST_UUID2.toString, TEST_START_DAY, TEST_END_DAY, new Paginate, "inClick")
        var statPages = statPagesResult._2
        assert(statPagesResult._1 === 15)
        assert(statPages.size === 10)
        (0 until statPages.size - 1).foreach { i =>
            assert(statPages(i).inClicks >= statPages(i+1).inClicks)
        }

        statPagesResult = StatPage.getStatPagesByUuid(TEST_UUID2.toString, TEST_START_DAY, TEST_END_DAY, new Paginate(2), "outClick")
        statPages = statPagesResult._2
        assert(statPagesResult._1 === 15)
        assert(statPages.size == 5)
        (0 until statPages.size - 1).foreach { i =>
            assert(statPages(i).outClicks >= statPages(i+1).outClicks)
        }

        statPagesResult = StatPage.getStatPagesByUuid(TEST_UUID2.toString, TEST_START_DAY, TEST_END_DAY, new Paginate, "")
        statPages = statPagesResult._2
        assert(statPagesResult._1 === 15)
        assert(statPages.size == 10)
        (0 until statPages.size - 1).foreach { i =>
            assert(statPages(i).showUps >= statPages(i+1).showUps)
        }
    }
    
    test("path hash") {
        println(Util.urlToUuid(TEST_URL))
        println(com.buzzinate.lezhi.util.LezhiUUID(StringUtil.hash(TEST_URL)).toString)

        val urlUuid = UuidUtil.uuidToByteArray(Util.urlToUuid(TEST_URL))
        val urlHash = StringUtil.hash(TEST_URL)
        
        assert(java.util.Arrays.equals(urlUuid, urlHash))

        println(Util.urlToUuid("http://qa.buzzinate.com/news/news/%E9%92%93%E9%B1%BC%E5%B2%9B%E4%BD%95%E9%9C%80%E7%BF%BB%E9%BB%84%E5%8E%86.html"))
        println(Util.urlToUuid("http://qa.buzzinate.com/news/news/%E9%92%93%E9%B1%BC%E5%B2%9B%E4%BD%95%E9%9C%80%E7%BF%BB%E9%BB%84%E5%8E%86.html#blz-insite"))
        println(Util.urlToUuid("http://www.bshare.cn/abcd10"))
    }
}