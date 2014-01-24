package com.buzzinate.lezhi.bean

import java.util.Date
import java.util.UUID
import scala.collection.mutable

import unfiltered.request.HttpRequest
import com.buzzinate.lezhi.model.StatPage

/**
 * @author jeffrey created on 2012-11-28 下午10:42:18
 *
 */
object StatsType extends Enumeration {
    type StatsType = Value
    val View, InClick, OutClick, Showup, Flush = Value
}

import StatsType._

sealed trait StatMessage

case class AnalyticMessage

case class ViewMessage(
    uuid: Option[UUID],
    url: String,
    date: Date) extends StatMessage

case class InClickMessage(
    uuid: Option[UUID],
    url: String,
    title: String,
    date: Date) extends StatMessage

case class OutClickMessage(
    uuid: Option[UUID],
    url: String,
    title: String,
    ref: String,
    pluginType: String,
    pic: String,
    date: Date) extends StatMessage

case class ShowupMessage(
    uuid: Option[UUID],
    url: String,
    ref: String,
    pluginType: String,
    pic: String,
    date: Date) extends StatMessage

case class UpdateMessage(
    siteMap: java.util.HashMap[(String, UUID, String), Long],
    sitePage: java.util.HashSet[(UUID, String)],
    pageInfo: java.util.HashSet[(String, String, Option[UUID])],
    pageMap: java.util.HashMap[(String, String), StatPage.StatPage],
    adminMap: java.util.HashMap[(String, String), Long],
    pathUuidMap: java.util.HashMap[String, String])

case class ClickMessage(
    userId: String,
    toUrl: String,
    fromUrl: String,
    typeStr: String,
    sitePrefix: String)

case class GenomeClickMessage(
    typeStr: String,
    vid: String,
    url: String,
    to: String,
    uuid: String,
    ip: String,
    ua: String,
    time: Date)

case class GenomeViewMessage(
    typeStr: String,
    vid: String,
    url: String,
    referrer: String,
    uuid: String,
    ip: String,
    ua: String,
    time: Date)

case class AdvertisePageViewMessage(
    url: String,
    uuid: String,
    xid: String,
    ip: String,
    ua: String)

case class AdvertiseViewMessage(
    adEntryIds: String,
    url: String,
    uuid: String,
    xid: String,
    ip: String,
    ua: String)

case class AdvertiseClickMessage(
    adEntryIds: String,
    url: String,
    uuid: String,
    xid: String,
    ip: String,
    ua: String)