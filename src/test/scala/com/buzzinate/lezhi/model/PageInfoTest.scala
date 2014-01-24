package com.buzzinate.lezhi.model

import java.util.UUID

import org.scalatest.FunSuite

class PageInfoTest extends FunSuite {
    private val TEST_URL = "http://www.bshare.cn/abcd"
    private val TEST_URL2 = "http://www.bshare.cn/efgh"

    private val TEST_UUID = "f8a4a53f-438a-4ffa-939f-7f313a7e3e79"
    private val TEST_UUID2 = "f8a4a53f-438a-4ffa-939f-7f313a7e3e89"

    test("Test save and get PageInfo") {
        PageInfo.save(TEST_URL, "bShare分享", TEST_UUID)

        val pMaybe = PageInfo.get(TEST_URL)
        pMaybe match {
            case Some(p) => assert(p.title == "bShare分享", "Got wrong PageInfo")
            case None => fail("Failed to get PageInfo")
        }

        PageInfo.delete(TEST_URL)
        assert(None == PageInfo.get(TEST_URL))
    }

    test("Test update and get PageInfo with UUID") {
        PageInfo.update(TEST_URL2, "bShare分享2", TEST_UUID)

        val pMaybe = PageInfo.get(TEST_URL2)
        pMaybe match {
            case Some(p) => assert(p.siteUuid == TEST_UUID, "Got wrong PageInfo")
            case None => fail("Failed to get PageInfo")
        }

        PageInfo.delete(TEST_URL2)
        assert(None == PageInfo.get(TEST_URL2))
    }

    test("Test get multi PageInfo") {
        PageInfo.save(TEST_URL, "bShare分享", TEST_UUID)
        PageInfo.save(TEST_URL2, "bShare分享2", TEST_UUID2)

        val res = PageInfo.get(List(TEST_URL, TEST_URL2))
        assert(res.length == 2, "Got wrong view of pages")
        res foreach { pi =>
            if (pi.url == TEST_URL) {
                assert(pi.title == "bShare分享", "Got wrong PageInfo")
            } else if (pi.url == TEST_URL2) {
                assert(pi.title == "bShare分享2", "Got wrong PageInfo")
            }
        }

        PageInfo.delete(TEST_URL)
        assert(None == PageInfo.get(TEST_URL))
        PageInfo.delete(TEST_URL2)
        assert(None == PageInfo.get(TEST_URL2))
    }

    test("Test batch update") {
        val test = List((TEST_URL, "bShare分享", None), (TEST_URL2, "bShare分享2", Some(UUID.fromString(TEST_UUID2))))
        PageInfo.batchUpdate(test)
        PageInfo.delete(TEST_URL)
        assert(None == PageInfo.get(TEST_URL))
        PageInfo.delete(TEST_URL2)
        assert(None == PageInfo.get(TEST_URL2))
    }
}