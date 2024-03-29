package com.buzzinate.lezhi.thrift

import com.twitter.scrooge.{ThriftStruct, ThriftStructCodec}
import org.apache.thrift.protocol._
import java.nio.ByteBuffer
import com.twitter.finagle.SourcedException
import scala.collection.mutable
import scala.collection.{Map, Set}

object RecommendItemList extends ThriftStructCodec[RecommendItemList] {
  val Struct = new TStruct("RecommendItemList")
  val TypeParamField = new TField("typeParam", TType.STRUCT, 1)
  val ItemsField = new TField("items", TType.LIST, 2)

  def encode(_item: RecommendItemList, _oproto: TProtocol) { _item.write(_oproto) }
  def decode(_iprot: TProtocol) = Immutable.decode(_iprot)

  def apply(_iprot: TProtocol): RecommendItemList = decode(_iprot)

  def apply(
    `typeParam`: RecommendTypeParam,
    `items`: Seq[RecommendItem] = Seq[RecommendItem]()
  ): RecommendItemList = new Immutable(
    `typeParam`,
    `items`
  )

  def unapply(_item: RecommendItemList): Option[Product2[RecommendTypeParam, Seq[RecommendItem]]] = Some(_item)

  object Immutable extends ThriftStructCodec[RecommendItemList] {
    def encode(_item: RecommendItemList, _oproto: TProtocol) { _item.write(_oproto) }
    def decode(_iprot: TProtocol) = {
      var `typeParam`: RecommendTypeParam = null
      var _got_typeParam = false
      var `items`: Seq[RecommendItem] = Seq[RecommendItem]()
      var _got_items = false
      var _done = false
      _iprot.readStructBegin()
      while (!_done) {
        val _field = _iprot.readFieldBegin()
        if (_field.`type` == TType.STOP) {
          _done = true
        } else {
          _field.id match {
            case 1 => { /* typeParam */
              _field.`type` match {
                case TType.STRUCT => {
                  `typeParam` = {
                    RecommendTypeParam.decode(_iprot)
                  }
                  _got_typeParam = true
                }
                case _ => TProtocolUtil.skip(_iprot, _field.`type`)
              }
            }
            case 2 => { /* items */
              _field.`type` match {
                case TType.LIST => {
                  `items` = {
                    val _list = _iprot.readListBegin()
                    val _rv = new mutable.ArrayBuffer[RecommendItem](_list.size)
                    var _i = 0
                    while (_i < _list.size) {
                      _rv += {
                        RecommendItem.decode(_iprot)
                      }
                      _i += 1
                    }
                    _iprot.readListEnd()
                    _rv
                  }
                  _got_items = true
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
      if (!_got_typeParam) throw new TProtocolException("Required field 'typeParam' was not found in serialized data for struct RecommendItemList")
      if (!_got_items) throw new TProtocolException("Required field 'items' was not found in serialized data for struct RecommendItemList")
      new Immutable(
        `typeParam`,
        `items`
      )
    }
  }

  /**
   * The default read-only implementation of RecommendItemList.  You typically should not need to
   * directly reference this class; instead, use the RecommendItemList.apply method to construct
   * new instances.
   */
  class Immutable(
    val `typeParam`: RecommendTypeParam,
    val `items`: Seq[RecommendItem] = Seq[RecommendItem]()
  ) extends RecommendItemList

  /**
   * This Proxy trait allows you to extend the RecommendItemList trait with additional state or
   * behavior and implement the read-only methods from RecommendItemList using an underlying
   * instance.
   */
  trait Proxy extends RecommendItemList {
    protected def _underlyingRecommendItemList: RecommendItemList
    def `typeParam`: RecommendTypeParam = _underlyingRecommendItemList.`typeParam`
    def `items`: Seq[RecommendItem] = _underlyingRecommendItemList.`items`
  }
}

trait RecommendItemList extends ThriftStruct
  with Product2[RecommendTypeParam, Seq[RecommendItem]]
  with java.io.Serializable
{
  import RecommendItemList._

  def `typeParam`: RecommendTypeParam
  def `items`: Seq[RecommendItem]

  def _1 = `typeParam`
  def _2 = `items`

  override def write(_oprot: TProtocol) {
    validate()
    _oprot.writeStructBegin(Struct)
    if (true) {
      val `typeParam_item` = `typeParam`
      _oprot.writeFieldBegin(TypeParamField)
      `typeParam_item`.write(_oprot)
      _oprot.writeFieldEnd()
    }
    if (true) {
      val `items_item` = `items`
      _oprot.writeFieldBegin(ItemsField)
      _oprot.writeListBegin(new TList(TType.STRUCT, `items_item`.size))
      `items_item`.foreach { `_items_item_element` =>
        `_items_item_element`.write(_oprot)
      }
      _oprot.writeListEnd()
      _oprot.writeFieldEnd()
    }
    _oprot.writeFieldStop()
    _oprot.writeStructEnd()
  }

  def copy(
    `typeParam`: RecommendTypeParam = this.`typeParam`,
    `items`: Seq[RecommendItem] = this.`items`
  ): RecommendItemList = new Immutable(
    `typeParam`,
    `items`
  )

  /**
   * Checks that all required fields are non-null.
   */
  def validate() {
    if (`typeParam` == null) throw new TProtocolException("Required field 'typeParam' cannot be null")
    if (`items` == null) throw new TProtocolException("Required field 'items' cannot be null")
  }

  def canEqual(other: Any) = other.isInstanceOf[RecommendItemList]

  override def equals(other: Any): Boolean = runtime.ScalaRunTime._equals(this, other)

  override def hashCode: Int = runtime.ScalaRunTime._hashCode(this)

  override def toString: String = runtime.ScalaRunTime._toString(this)

  override def productArity = 2

  override def productElement(n: Int): Any = n match {
    case 0 => `typeParam`
    case 1 => `items`
    case _ => throw new IndexOutOfBoundsException(n.toString)
  }

  override def productPrefix = "RecommendItemList"
}