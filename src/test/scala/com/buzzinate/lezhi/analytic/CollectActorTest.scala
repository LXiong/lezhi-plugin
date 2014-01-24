package com.buzzinate.lezhi.analytic

import java.util.UUID

import org.scalatest.{BeforeAndAfter, FunSuite}

import com.buzzinate.common.util.DateTimeUtil
import com.buzzinate.lezhi.AnalyticServer.collectRouter
import com.buzzinate.lezhi.bean.{InClickMessage, OutClickMessage, ShowupMessage}
import com.buzzinate.lezhi.model.{StatPage, UuidPath}
import com.buzzinate.lezhi.util.Util

import akka.actor.actorRef2Scala

/**
 * collect actore test
 *
 * User: magic
 * Date: 13-7-25
 * Time: 上午10:26
 */
class CollectActorTest extends FunSuite with BeforeAndAfter {

    private val uuid = "ca720443-9e43-4408-9cee-b1b0dca57ab1"
    private val path = "http://www.test.com/111"
    private val url = "http://www.test.com/111/222/333?a=a&b=b"
    private val title = "我是测试页面"
    private val date = DateTimeUtil.convertDate("2013-05-25")
    private val ref = "insite"
    private val pluginType = "fixed"
    private val pic = "true"

    val showup = ShowupMessage(Some(UUID.fromString(uuid)), url, ref, pluginType, pic, date)
    val inClick = InClickMessage(Some(UUID.fromString(uuid)), url, title, date)
    val outClick = OutClickMessage(Some(UUID.fromString(uuid)), url, title, ref, pluginType, pic, date)

    before {

    }

    after {
        UuidPath.deleteUuidPath(path)
    }

//    test("test uuid path") {
//        collectRouter ! showup
//        Thread.sleep(1000)
//        assert(UuidPath.getUuidPath(path).size == 1)
//    }

    test("test stat page") {
        val beforeStatPages = StatPage.getStatPages(url, date, date)
        var beforeStatPage = new StatPage.StatPage(0, Util.urlToBytes(url), Util.uuidToByte(uuid))
        if (beforeStatPages.size > 0) {
            beforeStatPage = beforeStatPages(0)
        }
        
        collectRouter ! showup
        collectRouter ! inClick
        collectRouter ! outClick
        
        Thread.sleep(2000)
        
        val statPages = StatPage.getStatPages(url, date, date)
        assert(statPages.size == 1)
        val statPage = statPages(0)
        
        println(beforeStatPage)
        println(statPage)
//        assert(statPage.inClicks == beforeStatPage.inClicks + 1)
//        assert(statPage.outClicks == beforeStatPage.outClicks + 1)
//        assert(statPage.showups == beforeStatPage.showups + 1)
    }
}
