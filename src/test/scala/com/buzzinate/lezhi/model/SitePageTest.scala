package com.buzzinate.lezhi.model

import com.buzzinate.lezhi.util.Util
import java.util.Date
import java.util.UUID
import org.scalatest.{ FunSuite, BeforeAndAfter }
import com.buzzinate.common.util.DateTimeUtil

class SitePageTest extends FunSuite with BeforeAndAfter {

    private val TEST_UUID = UUID.fromString("f8a4a53f-438a-4ffa-939f-7f313a7e3e79")
    private val TEST_UUID2 = UUID.fromString("48d9c87e-f5d8-4b76-9bb1-73ad0dcf054f")

    private val TEST_PAGE_1 = "http://www.bshare.cn/abcd"
    private val TEST_PAGE_2 = "http://www.bshare.cn/efgh"
    private val TEST_PAGES = List(TEST_PAGE_1, TEST_PAGE_2)

    before {
    }

    after {
        SitePage.delete(TEST_UUID)
        SitePage.delete(TEST_UUID2)
    }

    test("test add and get page in SitePage") {
        SitePage.update(TEST_UUID, TEST_PAGE_1)
        SitePage.update(TEST_UUID, TEST_PAGE_2)

        val res = SitePage.get(TEST_UUID)
        TEST_PAGES.foreach { url =>
            assert(res contains url, "Fail to get correct page in SitePage")
        }
        SitePage.delete(TEST_UUID)
        assert(0 == SitePage.get(TEST_UUID).size)
    }

    test("test batch update") {
        SitePage.batchUpdate(List((TEST_UUID, TEST_PAGE_1), (TEST_UUID, TEST_PAGE_2)))
        assert(SitePage.get(TEST_UUID).size == 2)
        
        SitePage.batchUpdate(List((TEST_UUID, TEST_PAGE_1), (TEST_UUID2, TEST_PAGE_2)))
        assert(SitePage.get(TEST_UUID).size == 1)
        assert(SitePage.get(TEST_UUID2).size == 1)
    }

    test("test get site_page by time") {
        SitePage.batchUpdate(List((TEST_UUID, TEST_PAGE_1), (TEST_UUID, TEST_PAGE_2)))

        val startDate: Date = new Date
        val endDate: Date = new Date

        val result = SitePage.get(TEST_UUID, DateTimeUtil.plusDays(startDate, 1), DateTimeUtil.plusDays(endDate, 2))
        assert(0 == result.size)

        val res = SitePage.get(TEST_UUID, DateTimeUtil.plusDays(startDate, -1), endDate)
        val res2 = SitePage.get(TEST_UUID)
        assert(res == res2)

        val res3 = SitePage.get(TEST_UUID, DateTimeUtil.plusDays(startDate, -2), DateTimeUtil.plusDays(endDate, -1))
        assert(0 == res3.size)

        SitePage.delete(TEST_UUID)
        assert(0 == SitePage.get(TEST_UUID).size)
    }

    test("test delete") {
        SitePage.delete
    }
}