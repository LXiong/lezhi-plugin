package com.buzzinate.lezhi.model

import scala.collection.mutable.ListBuffer

import org.apache.commons.lang.StringUtils

import com.buzzinate.lezhi.util.{ Logging, Util }
import com.buzzinate.lezhi.thrift.{ Metadata => TMetadata }
import com.buzzinate.lezhi.thrift.StatusEnum

/**
 * @author jeffrey created on 2012-8-20 下午8:15:33
 *
 */
class Metadata(var url: String = "",
    var thumbnail: String = "",
    var title: String = "",
    var keywords: String = "",
    var blackKeywords: String = "default",
    var status: String = "",
    var statusCode: String = "") {

}

object Metadata extends Logging {
    def getMetadata(tMetadata: TMetadata): Metadata = {
        new Metadata(tMetadata._1, tMetadata._2.getOrElse(""), tMetadata._3.getOrElse(""), tMetadata._4.getOrElse(""), tMetadata._5.getOrElse(""), tMetadata._6.get.toString.toLowerCase, "")
    }

    def getTMetadata(metadata: Metadata): TMetadata = {
        val thumbnail = if (StringUtils.isNotEmpty(metadata.thumbnail)) Some(metadata.thumbnail) else None
        val title = if (StringUtils.isNotEmpty(metadata.title)) Some(metadata.title) else None
        val blackKeywords = if (!StringUtils.equals(metadata.blackKeywords, "default")) Some(metadata.blackKeywords) else None
        val status = if (StringUtils.isNotEmpty(metadata.status)) StatusEnum.valueOf(metadata.status) else None
        TMetadata(metadata.url, thumbnail, title, None, blackKeywords, status)
    }

}

