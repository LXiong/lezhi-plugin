package com.buzzinate.lezhi.service

import org.scalatest.BeforeAndAfter
import org.scalatest.FunSuite
import java.util.Date
import java.util.UUID

/**
 * @author jeffrey.ji <jeffrey.ji@buzzinate.com>
 * Aug 5, 2013 7:18:22 PM
 *
 */
class UuidSiteServiceTest extends FunSuite with BeforeAndAfter {
    private val uuid = "9c54915f-4ef0-4a16-9988-2ca87cae9d0a"

    private val site = new com.buzzinate.user.vo.UuidSite
    site.setCategoryId(1)
    site.setCreateTime(new Date)
    site.setName("test")
    site.setUrl("htpp://www.test.com")
    site.setUserId(1)
    site.setUuid(uuid)

    before {
        UuidSiteService.deleteUuidSite(1, uuid)
    }
    after {
        UuidSiteService.deleteUuidSite(1, uuid)
    }

    test("test uuid site server") {
        UuidSiteService.createUuidSite(site)

        val test = UuidSiteService.getUuidSiteByUuid(uuid)
        assert(test != null)
        assert("test" == test.getName)
        assert("htpp://www.test.com" == test.getUrl)
        assert(1 == test.getUserId)
        assert(uuid == test.getUuid)

    }
}