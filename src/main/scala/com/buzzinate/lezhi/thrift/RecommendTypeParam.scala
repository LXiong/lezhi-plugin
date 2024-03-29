package com.buzzinate.lezhi.thrift

import com.twitter.scrooge.{ThriftStruct, ThriftStructCodec}
import org.apache.thrift.protocol._
import java.nio.ByteBuffer
import com.twitter.finagle.SourcedException
import scala.collection.mutable
import scala.collection.{Map, Set}

object RecommendTypeParam extends ThriftStructCodec[RecommendTypeParam] {
  val Struct = new TStruct("RecommendTypeParam")
  val OrderField = new TField("order", TType.I32, 1)
  val RecommendTypeField = new TField("recommendType", TType.I32, 2)
  val MatchPicField = new TField("matchPic", TType.I32, 3)
  val CountField = new TField("count", TType.I32, 4)

  def encode(_item: RecommendTypeParam, _oproto: TProtocol) { _item.write(_oproto) }
  def decode(_iprot: TProtocol) = Immutable.decode(_iprot)

  def apply(_iprot: TProtocol): RecommendTypeParam = decode(_iprot)

  def apply(
    `order`: Int,
    `recommendType`: RecommendType,
    `matchPic`: PicType,
    `count`: Int = 5
  ): RecommendTypeParam = new Immutable(
    `order`,
    `recommendType`,
    `matchPic`,
    `count`
  )

  def unapply(_item: RecommendTypeParam): Option[Product4[Int, RecommendType, PicType, Int]] = Some(_item)

  object Immutable extends ThriftStructCodec[RecommendTypeParam] {
    def encode(_item: RecommendTypeParam, _oproto: TProtocol) { _item.write(_oproto) }
    def decode(_iprot: TProtocol) = {
      var `order`: Int = 0
      var _got_order = false
      var `recommendType`: RecommendType = null
      var _got_recommendType = false
      var `matchPic`: PicType = null
      var _got_matchPic = false
      var `count`: Int = 5
      var _got_count = false
      var _done = false
      _iprot.readStructBegin()
      while (!_done) {
        val _field = _iprot.readFieldBegin()
        if (_field.`type` == TType.STOP) {
          _done = true
        } else {
          _field.id match {
            case 1 => { /* order */
              _field.`type` match {
                case TType.I32 => {
                  `order` = {
                    _iprot.readI32()
                  }
                  _got_order = true
                }
                case _ => TProtocolUtil.skip(_iprot, _field.`type`)
              }
            }
            case 2 => { /* recommendType */
              _field.`type` match {
                case TType.I32 => {
                  `recommendType` = {
                    RecommendType(_iprot.readI32())
                  }
                  _got_recommendType = true
                }
                case _ => TProtocolUtil.skip(_iprot, _field.`type`)
              }
            }
            case 3 => { /* matchPic */
              _field.`type` match {
                case TType.I32 => {
                  `matchPic` = {
                    PicType(_iprot.readI32())
                  }
                  _got_matchPic = true
                }
                case _ => TProtocolUtil.skip(_iprot, _field.`type`)
              }
            }
            case 4 => { /* count */
              _field.`type` match {
                case TType.I32 => {
                  `count` = {
                    _iprot.readI32()
                  }
                  _got_count = true
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
      if (!_got_order) throw new TProtocolException("Required field 'order' was not found in serialized data for struct RecommendTypeParam")
      if (!_got_recommendType) throw new TProtocolException("Required field 'recommendType' was not found in serialized data for struct RecommendTypeParam")
      if (!_got_matchPic) throw new TProtocolException("Required field 'matchPic' was not found in serialized data for struct RecommendTypeParam")
      if (!_got_count) throw new TProtocolException("Required field 'count' was not found in serialized data for struct RecommendTypeParam")
      new Immutable(
        `order`,
        `recommendType`,
        `matchPic`,
        `count`
      )
    }
  }

  /**
   * The default read-only implementation of RecommendTypeParam.  You typically should not need to
   * directly reference this class; instead, use the RecommendTypeParam.apply method to construct
   * new instances.
   */
  class Immutable(
    val `order`: Int,
    val `recommendType`: RecommendType,
    val `matchPic`: PicType,
    val `count`: Int = 5
  ) extends RecommendTypeParam

  /**
   * This Proxy trait allows you to extend the RecommendTypeParam trait with additional state or
   * behavior and implement the read-only methods from RecommendTypeParam using an underlying
   * instance.
   */
  trait Proxy extends RecommendTypeParam {
    protected def _underlyingRecommendTypeParam: RecommendTypeParam
    def `order`: Int = _underlyingRecommendTypeParam.`order`
    def `recommendType`: RecommendType = _underlyingRecommendTypeParam.`recommendType`
    def `matchPic`: PicType = _underlyingRecommendTypeParam.`matchPic`
    def `count`: Int = _underlyingRecommendTypeParam.`count`
  }
}

trait RecommendTypeParam extends ThriftStruct
  with Product4[Int, RecommendType, PicType, Int]
  with java.io.Serializable
{
  import RecommendTypeParam._

  def `order`: Int
  def `recommendType`: RecommendType
  def `matchPic`: PicType
  def `count`: Int

  def _1 = `order`
  def _2 = `recommendType`
  def _3 = `matchPic`
  def _4 = `count`

  override def write(_oprot: TProtocol) {
    validate()
    _oprot.writeStructBegin(Struct)
    if (true) {
      val `order_item` = `order`
      _oprot.writeFieldBegin(OrderField)
      _oprot.writeI32(`order_item`)
      _oprot.writeFieldEnd()
    }
    if (true) {
      val `recommendType_item` = `recommendType`
      _oprot.writeFieldBegin(RecommendTypeField)
      _oprot.writeI32(`recommendType_item`.value)
      _oprot.writeFieldEnd()
    }
    if (true) {
      val `matchPic_item` = `matchPic`
      _oprot.writeFieldBegin(MatchPicField)
      _oprot.writeI32(`matchPic_item`.value)
      _oprot.writeFieldEnd()
    }
    if (true) {
      val `count_item` = `count`
      _oprot.writeFieldBegin(CountField)
      _oprot.writeI32(`count_item`)
      _oprot.writeFieldEnd()
    }
    _oprot.writeFieldStop()
    _oprot.writeStructEnd()
  }

  def copy(
    `order`: Int = this.`order`,
    `recommendType`: RecommendType = this.`recommendType`,
    `matchPic`: PicType = this.`matchPic`,
    `count`: Int = this.`count`
  ): RecommendTypeParam = new Immutable(
    `order`,
    `recommendType`,
    `matchPic`,
    `count`
  )

  /**
   * Checks that all required fields are non-null.
   */
  def validate() {
    if (`recommendType` == null) throw new TProtocolException("Required field 'recommendType' cannot be null")
    if (`matchPic` == null) throw new TProtocolException("Required field 'matchPic' cannot be null")
  }

  def canEqual(other: Any) = other.isInstanceOf[RecommendTypeParam]

  override def equals(other: Any): Boolean = runtime.ScalaRunTime._equals(this, other)

  override def hashCode: Int = runtime.ScalaRunTime._hashCode(this)

  override def toString: String = runtime.ScalaRunTime._toString(this)

  override def productArity = 4

  override def productElement(n: Int): Any = n match {
    case 0 => `order`
    case 1 => `recommendType`
    case 2 => `matchPic`
    case 3 => `count`
    case _ => throw new IndexOutOfBoundsException(n.toString)
  }

  override def productPrefix = "RecommendTypeParam"
}