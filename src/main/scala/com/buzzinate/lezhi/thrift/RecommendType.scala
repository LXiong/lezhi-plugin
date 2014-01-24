package com.buzzinate.lezhi.thrift

import org.apache.thrift.TEnum

object RecommendType {
  case object Insite extends RecommendType(0, "Insite")
  case object Outsite extends RecommendType(1, "Outsite")
  case object Trending extends RecommendType(2, "Trending")
  case object Personalized extends RecommendType(3, "Personalized")
  case object Itemcf extends RecommendType(4, "Itemcf")

  def apply(value: Int): RecommendType = {
    value match {
      case 0 => Insite
      case 1 => Outsite
      case 2 => Trending
      case 3 => Personalized
      case 4 => Itemcf
      case _ => throw new NoSuchElementException(value.toString)
    }
  }

  def get(value: Int): Option[RecommendType] = {
    value match {
      case 0 => scala.Some(Insite)
      case 1 => scala.Some(Outsite)
      case 2 => scala.Some(Trending)
      case 3 => scala.Some(Personalized)
      case 4 => scala.Some(Itemcf)
      case _ => scala.None
    }
  }

  def valueOf(name: String): Option[RecommendType] = {
    name.toLowerCase match {
      case "insite" => scala.Some(RecommendType.Insite)
      case "outsite" => scala.Some(RecommendType.Outsite)
      case "trending" => scala.Some(RecommendType.Trending)
      case "personalized" => scala.Some(RecommendType.Personalized)
      case "itemcf" => scala.Some(RecommendType.Itemcf)
      case _ => scala.None
    }
  }
}

abstract class RecommendType(val value: Int, val name: String) extends TEnum {
  def getValue = value
}
