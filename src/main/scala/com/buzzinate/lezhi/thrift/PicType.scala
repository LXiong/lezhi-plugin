package com.buzzinate.lezhi.thrift

import org.apache.thrift.TEnum

object PicType {
  case object Text extends PicType(0, "Text")
  case object Inpage extends PicType(1, "Inpage")
  case object Insite extends PicType(2, "Insite")
  case object Provided extends PicType(3, "Provided")

  def apply(value: Int): PicType = {
    value match {
      case 0 => Text
      case 1 => Inpage
      case 2 => Insite
      case 3 => Provided
      case _ => throw new NoSuchElementException(value.toString)
    }
  }

  def get(value: Int): Option[PicType] = {
    value match {
      case 0 => scala.Some(Text)
      case 1 => scala.Some(Inpage)
      case 2 => scala.Some(Insite)
      case 3 => scala.Some(Provided)
      case _ => scala.None
    }
  }

  def valueOf(name: String): Option[PicType] = {
    name.toLowerCase match {
      case "text" => scala.Some(PicType.Text)
      case "inpage" => scala.Some(PicType.Inpage)
      case "insite" => scala.Some(PicType.Insite)
      case "provided" => scala.Some(PicType.Provided)
      case _ => scala.None
    }
  }
}

abstract class PicType(val value: Int, val name: String) extends TEnum {
  def getValue = value
}
