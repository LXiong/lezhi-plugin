package com.buzzinate.lezhi.recommend

import com.buzzinate.lezhi.thrift.RecommendTypeParam
import com.buzzinate.lezhi.bean.RequestParam

/**
 * Interface for recommendation engin
 */
trait RecommendClient {

    def recommend(userId: String,
        url: String,
        types: List[RecommendTypeParam],
        canonicalUrl: String,
        title: String,
        keywords: String,
        sitePrefix: String,
        customThumbnail: Option[String],
        customTitle: Option[String]): (List[(RequestParam, List[Map[String, Any]])], String)

    def click(userId: String,
        tourl: String,
        fromurl: String,
        typeStr: String,
        sitePrefix: String)

    def recrawl(url: String)

    def correctImg(url: String, imageUrl: String, userAgent: String)
}