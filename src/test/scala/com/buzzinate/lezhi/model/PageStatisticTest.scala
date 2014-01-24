package com.buzzinate.lezhi.model

import org.scalatest.{ BeforeAndAfter, FunSuite }

import com.buzzinate.lezhi.model.PageStatistic.Page

/**
 * @author jeffrey created on 2012-9-17 下午9:11:16
 *
 */
class PageStatisticTest extends FunSuite with BeforeAndAfter {
    private val keyType = "2011-09-17_showup"

    before {
        PageStatistic.delStatistic(keyType)
    }

    after {
        PageStatistic.delStatistic(keyType)
    }

    test("Test save and get list page_statistic") {

        val pages = List(Page(keyType, "http://www.test.com", "test", 11, 12, 23, 0.03), Page(keyType, "http://www.test.com2", "test2", 112, 12, 23, 0.02))
        PageStatistic.addStatistic(pages)

        val test = PageStatistic.getStatistics("2011-09-17_showup")
        assert(test.size == 2)
        assert("http://www.test.com2" == test(1).url)
        assert("test2" == test(1).title)
        assert(112 == test(1).showup)
        assert(12 == test(1).inClick)
        assert(23 == test(1).outClick)
        assert(0.02 == test(1).clickToShowup)
    }
}