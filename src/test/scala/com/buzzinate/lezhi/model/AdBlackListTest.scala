package com.buzzinate.lezhi.model

import org.scalatest.{BeforeAndAfter, FunSuite}

/**
 * @author jeffrey created on 2012-11-28 下午2:35:46
 *
 */
class AdBlackListTest extends FunSuite with BeforeAndAfter {
    private val TEST_URL = "f8a4a53f-438a-4ffa-939f-7f313a7e3e79"
    private val TEST_URL2 = "f8a4a53f-438a-4ffa-939f-7f313a7e3e71"
    private val KEYWORDS = List("test", "test2")

    test("Test save and get list ad blacklist keywords") {
        AdBlackList.addKeywords(TEST_URL, KEYWORDS)
        AdBlackList.addKeywords(TEST_URL2, KEYWORDS)

        val test1 = AdBlackList.getKeywords(TEST_URL)
        val test2 = AdBlackList.getKeywords(TEST_URL2)
        assert(test1 == test2)

        AdBlackList.delKeywords(TEST_URL, KEYWORDS)
        AdBlackList.delAllKeywords(TEST_URL2)
        assert(0 == AdBlackList.getKeywords(TEST_URL).size)
        assert(0 == AdBlackList.getKeywords(TEST_URL2).size)
    }
}