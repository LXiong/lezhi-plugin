package com.buzzinate.lezhi.model

import org.scalatest.BeforeAndAfter
import org.scalatest.FunSuite

/**
 * @author jeffrey.ji <jeffrey.ji@buzzinate.com>
 * May 7, 2013 12:33:18 PM
 *
 */
class ExchangeLinkTest extends FunSuite with BeforeAndAfter {
    private val nv = List(com.buzzinate.lezhi.model.ExchangeLink.ExchangeLink("www.baidu.com", 1, "test"), com.buzzinate.lezhi.model.ExchangeLink.ExchangeLink("www.bshare.com", 2, "test2"))

    test("Test save and get list exchange link") {
        ExchangeLink.getLinks.foreach {
            link => ExchangeLink.delExchangeLink(link.url)
        }
        ExchangeLink.updateExchangeLinks(nv)
        ExchangeLink.getLinks.foreach {
            link => println("url:" + link.url + ",order:" + link.order + ",title:" + link.title)
        }
    }
}