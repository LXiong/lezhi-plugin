package com.buzzinate.lezhi.thrift

import com.twitter.scrooge.{ThriftStruct, ThriftStructCodec}
import org.apache.thrift.protocol._
import java.nio.ByteBuffer
import com.twitter.finagle.SourcedException
import scala.collection.mutable
import scala.collection.{Map, Set}

object RecommendResult extends ThriftStructCodec[RecommendResult] {
  val Struct = new TStruct("RecommendResult")
  val ResultsField = new TField("results", TType.LIST, 1)
  val ThumbnailField = new TField("thumbnail", TType.STRING, 2)

  def encode(_item: RecommendResult, _oproto: TProtocol) { _item.write(_oproto) }
  def decode(_iprot: TProtocol) = Immutable.decode(_iprot)

  def apply(_iprot: TProtocol): RecommendResult = decode(_iprot)

  def apply(
    `results`: Seq[RecommendItemList] = Seq[RecommendItemList](),
    `thumbnail`: String
  ): RecommendResult = new Immutable(
    `results`,
    `thumbnail`
  )

  def unapply(_item: RecommendResult): Option[Product2[Seq[RecommendItemList], String]] = Some(_item)

  object Immutable extends ThriftStructCodec[RecommendResult] {
    def encode(_item: RecommendResult, _oproto: TProtocol) { _item.write(_oproto) }
    def decode(_iprot: TProtocol) = {
      var `results`: Seq[RecommendItemList] = Seq[RecommendItemList]()
      var _got_results = false
      var `thumbnail`: String = null
      var _got_thumbnail = false
      var _done = false
      _iprot.readStructBegin()
      while (!_done) {
        val _field = _iprot.readFieldBegin()
        if (_field.`type` == TType.STOP) {
          _done = true
        } else {
          _field.id match {
            case 1 => { /* results */
              _field.`type` match {
                case TType.LIST => {
                  `results` = {
                    val _list = _iprot.readListBegin()
                    val _rv = new mutable.ArrayBuffer[RecommendItemList](_list.size)
                    var _i = 0
                    while (_i < _list.size) {
                      _rv += {
                        RecommendItemList.decode(_iprot)
                      }
                      _i += 1
                    }
                    _iprot.readListEnd()
                    _rv
                  }
                  _got_results = true
                }
                case _ => TProtocolUtil.skip(_iprot, _field.`type`)
              }
            }
            case 2 => { /* thumbnail */
              _field.`type` match {
                case TType.STRING => {
                  `thumbnail` = {
                    _iprot.readString()
                  }
                  _got_thumbnail = true
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
      if (!_got_results) throw new TProtocolException("Required field 'results' was not found in serialized data for struct RecommendResult")
      if (!_got_thumbnail) throw new TProtocolException("Required field 'thumbnail' was not found in serialized data for struct RecommendResult")
      new Immutable(
        `results`,
        `thumbnail`
      )
    }
  }

  /**
   * The default read-only implementation of RecommendResult.  You typically should not need to
   * directly reference this class; instead, use the RecommendResult.apply method to construct
   * new instances.
   */
  class Immutable(
    val `results`: Seq[RecommendItemList] = Seq[RecommendItemList](),
    val `thumbnail`: String
  ) extends RecommendResult

  /**
   * This Proxy trait allows you to extend the RecommendResult trait with additional state or
   * behavior and implement the read-only methods from RecommendResult using an underlying
   * instance.
   */
  trait Proxy extends RecommendResult {
    protected def _underlyingRecommendResult: RecommendResult
    def `results`: Seq[RecommendItemList] = _underlyingRecommendResult.`results`
    def `thumbnail`: String = _underlyingRecommendResult.`thumbnail`
  }
}

trait RecommendResult extends ThriftStruct
  with Product2[Seq[RecommendItemList], String]
  with java.io.Serializable
{
  import RecommendResult._

  def `results`: Seq[RecommendItemList]
  def `thumbnail`: String

  def _1 = `results`
  def _2 = `thumbnail`

  override def write(_oprot: TProtocol) {
    validate()
    _oprot.writeStructBegin(Struct)
    if (true) {
      val `results_item` = `results`
      _oprot.writeFieldBegin(ResultsField)
      _oprot.writeListBegin(new TList(TType.STRUCT, `results_item`.size))
      `results_item`.foreach { `_results_item_element` =>
        `_results_item_element`.write(_oprot)
      }
      _oprot.writeListEnd()
      _oprot.writeFieldEnd()
    }
    if (true) {
      val `thumbnail_item` = `thumbnail`
      _oprot.writeFieldBegin(ThumbnailField)
      _oprot.writeString(`thumbnail_item`)
      _oprot.writeFieldEnd()
    }
    _oprot.writeFieldStop()
    _oprot.writeStructEnd()
  }

  def copy(
    `results`: Seq[RecommendItemList] = this.`results`,
    `thumbnail`: String = this.`thumbnail`
  ): RecommendResult = new Immutable(
    `results`,
    `thumbnail`
  )

  /**
   * Checks that all required fields are non-null.
   */
  def validate() {
    if (`results` == null) throw new TProtocolException("Required field 'results' cannot be null")
    if (`thumbnail` == null) throw new TProtocolException("Required field 'thumbnail' cannot be null")
  }

  def canEqual(other: Any) = other.isInstanceOf[RecommendResult]

  override def equals(other: Any): Boolean = runtime.ScalaRunTime._equals(this, other)

  override def hashCode: Int = runtime.ScalaRunTime._hashCode(this)

  override def toString: String = runtime.ScalaRunTime._toString(this)

  override def productArity = 2

  override def productElement(n: Int): Any = n match {
    case 0 => `results`
    case 1 => `thumbnail`
    case _ => throw new IndexOutOfBoundsException(n.toString)
  }

  override def productPrefix = "RecommendResult"
}