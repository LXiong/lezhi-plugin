package com.buzzinate.lezhi.thrift

import org.apache.thrift.TEnum

object StatusEnum {
  case object Normal extends StatusEnum(0, "Normal")
  case object Prior extends StatusEnum(1, "Prior")
  case object Hidden extends StatusEnum(2, "Hidden")

  def apply(value: Int): StatusEnum = {
    value match {
      case 0 => Normal
      case 1 => Prior
      case 2 => Hidden
      case _ => throw new NoSuchElementException(value.toString)
    }
  }

  def get(value: Int): Option[StatusEnum] = {
    value match {
      case 0 => scala.Some(Normal)
      case 1 => scala.Some(Prior)
      case 2 => scala.Some(Hidden)
      case _ => scala.None
    }
  }

  def valueOf(name: String): Option[StatusEnum] = {
    name.toLowerCase match {
      case "normal" => scala.Some(StatusEnum.Normal)
      case "prior" => scala.Some(StatusEnum.Prior)
      case "hidden" => scala.Some(StatusEnum.Hidden)
      case _ => scala.None
    }
  }
}

abstract class StatusEnum(val value: Int, val name: String) extends TEnum {
  def getValue = value
}
