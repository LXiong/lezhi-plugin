package com.buzzinate.lezhi.model

import org.scalatest.BeforeAndAfter
import org.scalatest.FunSuite
import java.util.Date
import java.nio.ByteBuffer

/**
 * @author jeffrey created on 2012-9-13 下午3:24:18
 *
 */
class UuidUrlsTest extends FunSuite with BeforeAndAfter {
    private val UUID = "3da84e24-20d5-4aa7-ba77-ea716c696e57"
    private val TEST_UUID = "ef227334-c1a6-4a11-a7cc-b3d790ba9de4"
    private val url = "http://www.test1.com"
    private val url2 = "http://www.test2.com"

    test("Test save and get list urls and remove") {
        UuidUrls.addUrls(UUID, "normal", List(url))
        assert(true == UuidUrls.isUrlExisted(UUID, url))
        UuidUrls.addUrls(TEST_UUID, "normal", List(url2))
        assert(true == UuidUrls.isUrlExisted(TEST_UUID, url2))

        assert(UuidUrls.getUrls(UUID).size == 1)
        assert(UuidUrls.getUrlsNum(UUID, "normal") == 1)
        assert(UuidUrls.getUrls(UUID) == List(url))
        assert(UuidUrls.getUrls(TEST_UUID) == List(url2))

        UuidUrls.delUrls(UUID, List(url))
        assert(false == UuidUrls.isUrlExisted(UUID, url))
        UuidUrls.delUrls(TEST_UUID, List(url2))
        assert(false == UuidUrls.isUrlExisted(TEST_UUID, url))

        assert(UuidUrls.getUrls(UUID).size == 0)
        assert(UuidUrls.getUrls(UUID).size == UuidUrls.getUrls(TEST_UUID).size)
    }

    test("Test update status") {
        UuidUrls.addUrls(TEST_UUID, "normal", List(url))
        assert(true == UuidUrls.isUrlExisted(TEST_UUID, url))

        val urls = UuidUrls.getUrls(TEST_UUID)
        assert(urls(0) == url)
        assert(urls.size == 1)

        UuidUrls.updateStatus(TEST_UUID, url, "hidden")
        val urls1 = UuidUrls.getPaginationUrls(TEST_UUID, "hidden", 0, 10)
        assert(urls1(0) == url)
        assert(urls1.size == 1)

        UuidUrls.delUrls(TEST_UUID, List(url))
        assert(false == UuidUrls.isUrlExisted(TEST_UUID, url))
    }
}