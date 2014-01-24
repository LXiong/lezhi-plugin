package com.buzzinate.lezhi.util

import java.util.{ Calendar, UUID => JavaUUID }
import scala.util.Random
import org.scalatest.{ BeforeAndAfter, FunSuite }
import com.buzzinate.lezhi.model.{ PageInfo, SitePage, StatPage, SiteConfig }
import com.buzzinate.common.util.DateTimeUtil
import java.sql.{ Date => SqlDate }

/**
 * @author jeffrey created on 2012-8-29 上午10:44:49
 *
 */
class DataTest extends FunSuite with BeforeAndAfter {

    test("Generated analytic data for page") {
        val uuid = "01f0006f-d7f4-fbbc-c3a7-35e631336e87"
        val title = "test"
        val url = "http://www.test.com"
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_MONTH, -1)
        val date = calendar.getTime()
        val urls = (0 until 1000).map { i =>
            url + i
        }
        val pages = urls.map { url =>
            (JavaUUID.fromString(uuid), url)
        }.toList

        val sc = new SiteConfig
        sc.uuid = uuid
        SiteConfig.update(sc)
        SitePage.batchUpdate(pages)
        var pis = urls.map { url =>
            (url, title, Some(JavaUUID.fromString(uuid)))
        }.toList
        PageInfo.batchUpdate(pis)

        val pageMap = new java.util.HashMap[(String, String), StatPage.StatPage]()
        val day = DateTimeUtil.formatDate(date)
        val sqlDay = new SqlDate(DateTimeUtil.getDateDay(date).getTime)
        urls.foreach { url =>
            pageMap.put((url, day), StatPage.StatPage(0, Util.urlToBytes(url), Util.uuidToByte(uuid), sqlDay, Math.abs(Random.nextInt(100))))
            pageMap.put((url, day), StatPage.StatPage(0, Util.urlToBytes(url), Util.uuidToByte(uuid), sqlDay, 0, Math.abs(Random.nextInt(100))))
            pageMap.put((url, day), StatPage.StatPage(0, Util.urlToBytes(url), Util.uuidToByte(uuid), sqlDay, 0, 0, Math.abs(Random.nextInt(100))))

        }
        StatPage.batchPage(pageMap)
    }
}