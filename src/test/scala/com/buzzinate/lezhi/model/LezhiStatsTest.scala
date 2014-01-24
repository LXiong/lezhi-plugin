package com.buzzinate.lezhi.model

import org.scalatest.BeforeAndAfter
import org.scalatest.FunSuite
import java.util.Calendar

/**
 * @author jeffrey.ji <jeffrey.ji@buzzinate.com>
 * Jul 24, 2013 4:57:24 PM
 *
 */
class LezhiStatsTest extends FunSuite with BeforeAndAfter {
    private val calendar = Calendar.getInstance();
    calendar.set(2010, 1, 1)
    private val TEST_DAY = calendar.getTime
    calendar.set(2010, 1, 2)
    private val TEST_DAY2 = calendar.getTime

    before {
        LezhiStats.delete(TEST_DAY)
        LezhiStats.delete(TEST_DAY2)
    }
    after {
        LezhiStats.delete(TEST_DAY)
        LezhiStats.delete(TEST_DAY2)
    }

    test("Test save and get lezhi_stats") {
        val stats = LezhiStats.getStats(TEST_DAY, TEST_DAY2)
        assert(stats.size == 0)

        LezhiStats.update(TEST_DAY, LezhiStats.COLUMN_UV, 10L)
        LezhiStats.update(TEST_DAY, LezhiStats.COLUMN_UV, 10L)
        LezhiStats.update(TEST_DAY, LezhiStats.COLUMN_UNIQUE_IP, 11L)
        LezhiStats.update(TEST_DAY2, LezhiStats.COLUMN_UV, 11L)
        LezhiStats.update(TEST_DAY2, LezhiStats.COLUMN_UV, 11L)
        LezhiStats.update(TEST_DAY2, LezhiStats.COLUMN_UNIQUE_IP, 11L)

        val stats2 = LezhiStats.getStats(TEST_DAY, TEST_DAY2)
        assert(stats2.size == 2)
        stats.foreach { stat =>
            if (stat.statDay == TEST_DAY) {
                assert(stat.uv == 20)
                assert(stat.uniqueIp == 11)
            }
            if (stat.statDay == TEST_DAY2) {
                assert(stat.uv == 22)
                assert(stat.uniqueIp == 11)
            }
        }
    }
}