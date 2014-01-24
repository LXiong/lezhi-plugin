package com.buzzinate.lezhi.buzzads

import com.buzzinate.lezhi.Config
import com.buzzinate.lezhi.util.Logging
import com.codahale.jerkson.Json

/**
 * @author jeffrey created on 2012-11-28 下午3:14:30
 *
 */
class AdvertiseClientMock extends AdvertiseClient with Logging {

    def serve(userId: String,
        uuid: String,
        url: String,
        ip: String,
        title: String,
        keywords: String,
        needPic: Boolean,
        count: Int): List[Map[String, Any]] = {

        log.debug("Send mock advertise: " + url + ", adCount: " + count)
        try {
            val source = scala.io.Source.fromFile(Config.getString("root.dir") + "www/advertise.json")
            val resultJson = source.mkString
            source.close()

            Json.parse[List[Map[String, Any]]](resultJson).slice(0,count)
        } catch {
            case ex: Exception =>
                log.error("Exception: " + ex.getMessage)
                Nil
        }
    }
}