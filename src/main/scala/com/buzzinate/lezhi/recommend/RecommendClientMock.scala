package com.buzzinate.lezhi.recommend

import scala.collection.mutable.ListBuffer

import com.buzzinate.lezhi.Config
import com.buzzinate.lezhi.bean.RequestParam
import com.buzzinate.lezhi.thrift.RecommendTypeParam
import com.buzzinate.lezhi.util.Logging
import com.codahale.jerkson.Json

class RecommendClientMock extends RecommendClient with Logging {

    def recommend(userId: String,
        url: String,
        types: List[RecommendTypeParam],
        canonicalUrl: String,
        title: String,
        keywords: String,
        sitePrefix: String,
        customThumbnail: Option[String],
        customTitle: Option[String]): (List[(RequestParam, List[Map[String, Any]])], String) = {

        log.debug("Send mock recommend: " + url + ", sitePrefix: " + sitePrefix + ", types: " + types)
        try {
            val source = scala.io.Source.fromFile(Config.getString("root.dir") + "www/demo.json")
            val resultJson = source.mkString
            source.close()

            val resultMap = Json.parse[Map[String, List[Map[String, Any]]]](resultJson)
            var res = ListBuffer[(RequestParam, List[Map[String, Any]])]()
            types.foreach { tp =>
                res += ((new RequestParam(tp.order, "", tp.recommendType.toString.toLowerCase, tp.count, 0), resultMap.get(tp.recommendType.name.toLowerCase).get.slice(0, tp.count)))
            }

            (res.toList, "")
        } catch {
            case ex: Exception =>
                log.error("Exception: " + ex.getMessage)
                (Nil, "")
        }
    }

    def click(userId: String, toUrl: String,
        fromUrl: String,
        typeStr: String,
        sitePrefix: String) {
    }

    def recrawl(url: String) {

    }

    def correctImg(url: String, imageUrl: String, userAgent: String) {

    }
}

