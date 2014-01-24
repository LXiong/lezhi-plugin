package com.buzzinate.lezhi.model

import java.util.Date
import java.util.UUID
import org.scalatest.{ FunSuite, BeforeAndAfter }

class SiteConfigTest extends FunSuite with BeforeAndAfter {

    private val TEST_UUID = "9c54915f-4ef0-4a16-9988-2ca87cae9d0a"

    before {
        SiteConfig.delete(TEST_UUID)
    }

    after {
        SiteConfig.delete(TEST_UUID)
    }

    test("Test save and get SiteConfig") {
        val c = new SiteConfig(TEST_UUID)
        c.source = "outsite"
        c.pluginType = "pluginType"
        c.pic = false
        c.position = "position"
        c.sitePrefix = "sitePrefix"
        c.promote = "promote"
        c.fontSize = 16
        c.row = 3
        c.col = 5
        c.htcolor = "htcolor"
        c.rtcolor = "rtcolor"
        c.bdcolor = "bdcolor"
        c.hvcolor = "hvcolor"
        c.highlight = false
        c.redirectMode = "http"
        c.adEnabled = false
        c.adCount = 0

        assert(SiteConfig.update(c), "Failed to save SiteConfig")

        val cMaybe = SiteConfig.get(TEST_UUID)
        cMaybe match {
            case Some(c) => {
                assert(c.source == "outsite", "Got wrong PageInfo")
                assert(c.pluginType == "pluginType", "Got wrong PageInfo")
                assert(c.pic == false, "Got wrong PageInfo")
                assert(c.position == "position", "Got wrong PageInfo")
                assert(c.sitePrefix == "sitePrefix", "Got wrong PageInfo")
                assert(c.promote == "promote", "Got wrong PageInfo")
                assert(c.source == "outsite", "Got wrong PageInfo")
                assert(c.row == 3, "Got wrong PageInfo")
                assert(c.col == 5, "Got wrong PageInfo")
                assert(c.htcolor == "htcolor", "Got wrong PageInfo")
                assert(c.rtcolor == "rtcolor", "Got wrong PageInfo")
                assert(c.bdcolor == "bdcolor", "Got wrong PageInfo")
                assert(c.hvcolor == "hvcolor", "Got wrong PageInfo")
                assert(c.adEnabled == false, "Got wrong adEnabled")
                assert(c.adCount == 0, "Got wrong adCount")
            }
            case None => fail("Failed to get SiteConfig")
        }
    }

    test("Test getAllTypeUuids") {
        val allUuids = SiteConfig.getAllUuids()
        val (pluginUuids, buttonUuids, picUuids, textUuids) = SiteConfig.getAllTypeUuids()
        assert(pluginUuids.size + buttonUuids.size == picUuids.size + textUuids.size)
        assert(allUuids.size == picUuids.size + textUuids.size)
    }
}