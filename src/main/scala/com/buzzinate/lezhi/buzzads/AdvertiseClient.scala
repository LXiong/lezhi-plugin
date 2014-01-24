package com.buzzinate.lezhi.buzzads

/**
 * @author jeffrey created on 2012-11-28 下午3:11:41
 *
 */
trait AdvertiseClient {
    def serve(userId: String,
        uuid: String,
        url: String,
        ip: String,
        title: String,
        keywords: String,
        needPic: Boolean,
        count: Int): List[Map[String, Any]]
}