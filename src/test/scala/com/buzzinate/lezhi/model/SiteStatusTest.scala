package com.buzzinate.lezhi.model

import org.scalatest.BeforeAndAfter
import org.scalatest.FunSuite
import com.buzzinate.lezhi.bean.Paginate

/**
 * @author jeffrey created on 2012-10-23 上午10:00:53
 *
 */
class SiteStatusTest extends FunSuite with BeforeAndAfter {

    private val UUID1 = "3da84e24-20d5-4aa7-ba77-ea716c696e51"
    private val UUID2 = "3da84e24-20d5-4aa7-ba77-ea716c696e52"

    before {
        SiteStatus.addUrlNumber(UUID1, 1001)
        SiteStatus.addUrlNumber(UUID2, 1002)
    }

    after {
    }

    test("Test clean and set url number") {
        SiteStatus.cleanNumber("6d2dfe27-e430-41ef-8cf9-c322fc2f7314")
        SiteStatus.addUrlNumber("6d2dfe27-e430-41ef-8cf9-c322fc2f7314", 100)
        assert(100 == SiteStatus.getUrlNumber("6d2dfe27-e430-41ef-8cf9-c322fc2f7314"))
    }

    test("get article nums by uuids") {
        val siteStatusList = SiteStatus.getArticleNumByUuids(Seq(UUID1, UUID2))
        assert(siteStatusList.size === 2)

        val uuid2SiteStatus = siteStatusList(0)
        val uuid1SiteStatus = siteStatusList(1)
        assert(uuid2SiteStatus.uuid === UUID2)
        assert(uuid1SiteStatus.uuid === UUID1)
        assert(uuid2SiteStatus.urlNumber >= uuid1SiteStatus.urlNumber)
    }

    test("get article nums by paginate") {
        val siteStatusList = SiteStatus.getArticleNumByPaginate(new Paginate)

        assert(siteStatusList.size >= 2)
        assert(siteStatusList(0).urlNumber >= siteStatusList(1).urlNumber)
    }

    test("get uuid count has article num") {
        val count = SiteStatus.getUuidCount
        assert(count >= 2)
    }
}