package com.buzzinate.lezhi.model

import org.scalatest.BeforeAndAfter
import org.scalatest.FunSuite

/**
 * @author jeffrey.ji <jeffrey.ji@buzzinate.com>
 * Jun 3, 2013 2:24:59 PM
 *
 */
class AdCloseReasonTest extends FunSuite with BeforeAndAfter {
    private val TEST_UUID = "f8a4a53f-438a-4ffa-939f-7f313a7e3e79"

    before {
        AdCloseReason.del(TEST_UUID)

    }

    after {
        AdCloseReason.del(TEST_UUID)
    }

    test("Test save and get list ad blacklist keywords") {
        AdCloseReason.add(TEST_UUID, TEST_UUID)
        AdCloseReason.add(TEST_UUID, "广告太烂")
        AdCloseReason.get(TEST_UUID).foreach { reason =>
            println(reason)
        }
    }
}