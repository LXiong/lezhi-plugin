package com.buzzinate.lezhi.model

import java.util.{ Calendar, Date }

import scala.collection.JavaConversions._

import org.scalatest.{ BeforeAndAfter, FunSuite }

import com.buzzinate.common.util.DateTimeUtil

class AdminCountsTest extends FunSuite with BeforeAndAfter {
    private val calendar = Calendar.getInstance();
    calendar.set(2010, 1, 1)
    private val TEST_DAY = calendar.getTime
    private val TEST_DAY_STR = DateTimeUtil.formatDate(TEST_DAY)

    test("Test save and get AdminCounts") {
        val currentViews = AdminCounts.getViews(TEST_DAY)
        val viewMap = new java.util.HashMap[(String, String), Long]
        viewMap.put((AdminCounts.COLUMN_VIEWS, TEST_DAY_STR), 11L)
        AdminCounts.incrFromMap(viewMap)
        val newViews = AdminCounts.getViews(TEST_DAY)
        assert(newViews == currentViews + 11, "Got wrong Views in AdminCounts")

        val pcMaybe = AdminCounts.get(TEST_DAY)
        pcMaybe match {
            case Some(pc) =>
                assert(pc.views == currentViews + 11, "Got wrong Views in AdminCounts")
            case None => fail("Failed to get AdminCounts")
        }

    }
}

