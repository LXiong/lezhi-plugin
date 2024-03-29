package com.buzzinate.lezhi.thrift

import com.twitter.scrooge.{ThriftStruct, ThriftStructCodec}
import org.apache.thrift.protocol._
import java.nio.ByteBuffer
import com.twitter.finagle.SourcedException
import scala.collection.mutable
import scala.collection.{Map, Set}

object ClickParam extends ThriftStructCodec[ClickParam] {
  val Struct = new TStruct("ClickParam")
  val TourlField = new TField("tourl", TType.STRING, 1)
  val FromurlField = new TField("fromurl", TType.STRING, 2)
  val TypeField = new TField("type", TType.I32, 3)
  val SiteprefixField = new TField("siteprefix", TType.STRING, 4)
  val UseridField = new TField("userid", TType.STRING, 5)

  def encode(_item: ClickParam, _oproto: TProtocol) { _item.write(_oproto) }
  def decode(_iprot: TProtocol) = Immutable.decode(_iprot)

  def apply(_iprot: TProtocol): ClickParam = decode(_iprot)

  def apply(
    `tourl`: String,
    `fromurl`: String,
    `type`: Option[RecommendType] = None,
    `siteprefix`: Option[String] = None,
    `userid`: String = "1"
  ): ClickParam = new Immutable(
    `tourl`,
    `fromurl`,
    `type`,
    `siteprefix`,
    `userid`
  )

  def unapply(_item: ClickParam): Option[Product5[String, String, Option[RecommendType], Option[String], String]] = Some(_item)

  object Immutable extends ThriftStructCodec[ClickParam] {
    def encode(_item: ClickParam, _oproto: TProtocol) { _item.write(_oproto) }
    def decode(_iprot: TProtocol) = {
      var `tourl`: String = null
      var _got_tourl = false
      var `fromurl`: String = null
      var _got_fromurl = false
      var `type`: RecommendType = null
      var _got_type = false
      var `siteprefix`: String = null
      var _got_siteprefix = false
      var `userid`: String = "1"
      var _got_userid = false
      var _done = false
      _iprot.readStructBegin()
      while (!_done) {
        val _field = _iprot.readFieldBegin()
        if (_field.`type` == TType.STOP) {
          _done = true
        } else {
          _field.id match {
            case 1 => { /* tourl */
              _field.`type` match {
                case TType.STRING => {
                  `tourl` = {
                    _iprot.readString()
                  }
                  _got_tourl = true
                }
                case _ => TProtocolUtil.skip(_iprot, _field.`type`)
              }
            }
            case 2 => { /* fromurl */
              _field.`type` match {
                case TType.STRING => {
                  `fromurl` = {
                    _iprot.readString()
                  }
                  _got_fromurl = true
                }
                case _ => TProtocolUtil.skip(_iprot, _field.`type`)
              }
            }
            case 3 => { /* type */
              _field.`type` match {
                case TType.I32 => {
                  `type` = {
                    RecommendType(_iprot.readI32())
                  }
                  _got_type = true
                }
                case _ => TProtocolUtil.skip(_iprot, _field.`type`)
              }
            }
            case 4 => { /* siteprefix */
              _field.`type` match {
                case TType.STRING => {
                  `siteprefix` = {
                    _iprot.readString()
                  }
                  _got_siteprefix = true
                }
                case _ => TProtocolUtil.skip(_iprot, _field.`type`)
              }
            }
            case 5 => { /* userid */
              _field.`type` match {
                case TType.STRING => {
                  `userid` = {
                    _iprot.readString()
                  }
                  _got_userid = true
                }
                case _ => TProtocolUtil.skip(_iprot, _field.`type`)
              }
            }
            case _ => TProtocolUtil.skip(_iprot, _field.`type`)
          }
          _iprot.readFieldEnd()
        }
      }
      _iprot.readStructEnd()
      if (!_got_tourl) throw new TProtocolException("Required field 'tourl' was not found in serialized data for struct ClickParam")
      if (!_got_fromurl) throw new TProtocolException("Required field 'fromurl' was not found in serialized data for struct ClickParam")
      new Immutable(
        `tourl`,
        `fromurl`,
        if (_got_type) Some(`type`) else None,
        if (_got_siteprefix) Some(`siteprefix`) else None,
        `userid`
      )
    }
  }

  /**
   * The default read-only implementation of ClickParam.  You typically should not need to
   * directly reference this class; instead, use the ClickParam.apply method to construct
   * new instances.
   */
  class Immutable(
    val `tourl`: String,
    val `fromurl`: String,
    val `type`: Option[RecommendType] = None,
    val `siteprefix`: Option[String] = None,
    val `userid`: String = "1"
  ) extends ClickParam

  /**
   * This Proxy trait allows you to extend the ClickParam trait with additional state or
   * behavior and implement the read-only methods from ClickParam using an underlying
   * instance.
   */
  trait Proxy extends ClickParam {
    protected def _underlyingClickParam: ClickParam
    def `tourl`: String = _underlyingClickParam.`tourl`
    def `fromurl`: String = _underlyingClickParam.`fromurl`
    def `type`: Option[RecommendType] = _underlyingClickParam.`type`
    def `siteprefix`: Option[String] = _underlyingClickParam.`siteprefix`
    def `userid`: String = _underlyingClickParam.`userid`
  }
}

trait ClickParam extends ThriftStruct
  with Product5[String, String, Option[RecommendType], Option[String], String]
  with java.io.Serializable
{
  import ClickParam._

  def `tourl`: String
  def `fromurl`: String
  def `type`: Option[RecommendType]
  def `siteprefix`: Option[String]
  def `userid`: String

  def _1 = `tourl`
  def _2 = `fromurl`
  def _3 = `type`
  def _4 = `siteprefix`
  def _5 = `userid`

  override def write(_oprot: TProtocol) {
    validate()
    _oprot.writeStructBegin(Struct)
    if (true) {
      val `tourl_item` = `tourl`
      _oprot.writeFieldBegin(TourlField)
      _oprot.writeString(`tourl_item`)
      _oprot.writeFieldEnd()
    }
    if (true) {
      val `fromurl_item` = `fromurl`
      _oprot.writeFieldBegin(FromurlField)
      _oprot.writeString(`fromurl_item`)
      _oprot.writeFieldEnd()
    }
    if (`type`.isDefined) {
      val `type_item` = `type`.get
      _oprot.writeFieldBegin(TypeField)
      _oprot.writeI32(`type_item`.value)
      _oprot.writeFieldEnd()
    }
    if (`siteprefix`.isDefined) {
      val `siteprefix_item` = `siteprefix`.get
      _oprot.writeFieldBegin(SiteprefixField)
      _oprot.writeString(`siteprefix_item`)
      _oprot.writeFieldEnd()
    }
    if (true) {
      val `userid_item` = `userid`
      _oprot.writeFieldBegin(UseridField)
      _oprot.writeString(`userid_item`)
      _oprot.writeFieldEnd()
    }
    _oprot.writeFieldStop()
    _oprot.writeStructEnd()
  }

  def copy(
    `tourl`: String = this.`tourl`,
    `fromurl`: String = this.`fromurl`,
    `type`: Option[RecommendType] = this.`type`,
    `siteprefix`: Option[String] = this.`siteprefix`,
    `userid`: String = this.`userid`
  ): ClickParam = new Immutable(
    `tourl`,
    `fromurl`,
    `type`,
    `siteprefix`,
    `userid`
  )

  /**
   * Checks that all required fields are non-null.
   */
  def validate() {
    if (`tourl` == null) throw new TProtocolException("Required field 'tourl' cannot be null")
    if (`fromurl` == null) throw new TProtocolException("Required field 'fromurl' cannot be null")
  }

  def canEqual(other: Any) = other.isInstanceOf[ClickParam]

  override def equals(other: Any): Boolean = runtime.ScalaRunTime._equals(this, other)

  override def hashCode: Int = runtime.ScalaRunTime._hashCode(this)

  override def toString: String = runtime.ScalaRunTime._toString(this)

  override def productArity = 5

  override def productElement(n: Int): Any = n match {
    case 0 => `tourl`
    case 1 => `fromurl`
    case 2 => `type`
    case 3 => `siteprefix`
    case 4 => `userid`
    case _ => throw new IndexOutOfBoundsException(n.toString)
  }

  override def productPrefix = "ClickParam"
}