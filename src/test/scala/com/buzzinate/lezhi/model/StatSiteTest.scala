package com.buzzinate.lezhi.model

import java.util.{Date, Calendar, UUID}

import org.scalatest.{BeforeAndAfter, FunSuite}

import com.buzzinate.common.util.DateTimeUtil
import com.buzzinate.lezhi.bean.Paginate

/**
 * @author jeffrey.ji <jeffrey.ji@buzzinate.com>
 * Jul 9, 2013 3:45:57 PM
 *
 */
class StatSiteTest extends FunSuite with BeforeAndAfter {
    private val TEST_UUID = "f8a4a53f-438a-4ffa-939f-7f313a7e3e79"

    private val calendar = Calendar.getInstance();
    calendar.set(2012, 5, 24)
    private val TEST_DAY = calendar.getTime
    calendar.set(2012, 5, 25)
    private val TEST_DAY_END = calendar.getTime

    test("Test save and get site StatSite") {
        val inclick = StatSite.getSiteInClick(TEST_UUID, TEST_DAY)
        StatSite.incrSiteInClick(TEST_UUID, TEST_DAY, 11)
        val newInclick = StatSite.getSiteInClick(TEST_UUID, TEST_DAY)
        assert(newInclick == inclick + 11, "Got wrong StatSiteInClick")

        StatSite.getSiteInClick(TEST_UUID, TEST_DAY, TEST_DAY_END) foreach {
            _ match {
                case (TEST_DAY, c) => assert(c == inclick + 11, "Got wrong StatSiteInClick")
                case _ => ;
            }
        }

        val outclick = StatSite.getSiteOutClick(TEST_UUID, TEST_DAY)
        StatSite.incrSiteOutClick(TEST_UUID, TEST_DAY, 11)
        val newOutclick = StatSite.getSiteOutClick(TEST_UUID, TEST_DAY)
        assert(newOutclick == outclick + 11, "Got wrong StatSiteOutClick")

        StatSite.getSiteOutClick(TEST_UUID, TEST_DAY, TEST_DAY_END) foreach {
            _ match {
                case (TEST_DAY, c) => assert(c == outclick + 11, "Got wrong StatSiteOutClick")
                case _ => ;
            }
        }

        val showup = StatSite.getSiteShowup(TEST_UUID, TEST_DAY)
        StatSite.incrSiteShowup(TEST_UUID, TEST_DAY, 11)
        val newShowup = StatSite.getSiteShowup(TEST_UUID, TEST_DAY)
        assert(newShowup == showup + 11, "Got wrong StatSiteShowup")

        StatSite.getSiteShowup(TEST_UUID, TEST_DAY, TEST_DAY_END) foreach {
            _ match {
                case (TEST_DAY, c) => assert(c == showup + 11, "Got wrong StatSiteShowup")
                case _ => ;
            }
        }
    }

    test("test batch site ") {
        val inclick = StatSite.getSiteInClick(TEST_UUID, TEST_DAY)
        val test = new java.util.HashMap[(String, UUID, String), Long]()
        test.put((StatSite.COLUMN_IN_CLICKS, UUID.fromString(TEST_UUID), DateTimeUtil.formatDate(TEST_DAY)), 10L)
        StatSite.batchSite(test)
        assert(StatSite.getSiteInClick(TEST_UUID, TEST_DAY) == inclick + 10, "Got wrong StatSiteInClick")
    }


    private val COLUMN_IN_CLICKS = "inClicks"
    private val COLUMN_OUT_CLICKS = "outClicks"
    private val COLUMN_SHOWUPS = "showups"
    private val PAGINATE_DATE = "2011-11-11"
    private val PAGINATE_UUID = "0f66bca0-6eaa-436c-b204-590a48edbd6"

    before {
        val siteMap = new java.util.HashMap[(String, UUID, String), Long]()
        (1 to 15).foreach { i =>
            siteMap.put((COLUMN_IN_CLICKS, UUID.fromString(PAGINATE_UUID + i), PAGINATE_DATE), i)
            siteMap.put((COLUMN_OUT_CLICKS, UUID.fromString(PAGINATE_UUID + i), PAGINATE_DATE), i)
            siteMap.put((COLUMN_SHOWUPS, UUID.fromString(PAGINATE_UUID + i), PAGINATE_DATE), i)
        }
        StatSite.batchSite(siteMap)
    }

    test("get stat site data by paginate") {
        var statSites = StatSite.getStatSiteByPaginate(new Paginate, "showup", DateTimeUtil.convertDate(PAGINATE_DATE), DateTimeUtil.convertDate(PAGINATE_DATE))
        assert(statSites.size === 10)
        (0 until statSites.size - 1).foreach { i =>
            assert(statSites(i).showups >= statSites(i+1).showups)
        }

        statSites = StatSite.getStatSiteByPaginate(new Paginate, "inClick", DateTimeUtil.convertDate(PAGINATE_DATE), DateTimeUtil.convertDate(PAGINATE_DATE))
        assert(statSites.size === 10)
        (0 until statSites.size - 1).foreach { i =>
            assert(statSites(i).inClicks >= statSites(i+1).inClicks)
        }

        statSites = StatSite.getStatSiteByPaginate(new Paginate, "outClick", DateTimeUtil.convertDate(PAGINATE_DATE), DateTimeUtil.convertDate(PAGINATE_DATE))
        assert(statSites.size === 10)
        (0 until statSites.size - 1).foreach { i =>
            assert(statSites(i).outClicks >= statSites(i+1).outClicks)
        }

        statSites = StatSite.getStatSiteByPaginate(new Paginate(2), "outClick", DateTimeUtil.convertDate(PAGINATE_DATE), DateTimeUtil.convertDate(PAGINATE_DATE))
        assert(statSites.size === 5)
        (0 until statSites.size - 1).foreach { i =>
            assert(statSites(i).outClicks >= statSites(i+1).outClicks)
        }

    }
}