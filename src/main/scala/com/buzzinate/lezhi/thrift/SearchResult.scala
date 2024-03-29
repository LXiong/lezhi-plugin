package com.buzzinate.lezhi.thrift

import com.twitter.scrooge.{ThriftStruct, ThriftStructCodec}
import org.apache.thrift.protocol._
import java.nio.ByteBuffer
import com.twitter.finagle.SourcedException
import scala.collection.mutable
import scala.collection.{Map, Set}

object SearchResult extends ThriftStructCodec[SearchResult] {
  val Struct = new TStruct("SearchResult")
  val TotalHitField = new TField("totalHit", TType.I32, 1)
  val DocsField = new TField("docs", TType.LIST, 2)

  def encode(_item: SearchResult, _oproto: TProtocol) { _item.write(_oproto) }
  def decode(_iprot: TProtocol) = Immutable.decode(_iprot)

  def apply(_iprot: TProtocol): SearchResult = decode(_iprot)

  def apply(
    `totalHit`: Int,
    `docs`: Seq[Metadata] = Seq[Metadata]()
  ): SearchResult = new Immutable(
    `totalHit`,
    `docs`
  )

  def unapply(_item: SearchResult): Option[Product2[Int, Seq[Metadata]]] = Some(_item)

  object Immutable extends ThriftStructCodec[SearchResult] {
    def encode(_item: SearchResult, _oproto: TProtocol) { _item.write(_oproto) }
    def decode(_iprot: TProtocol) = {
      var `totalHit`: Int = 0
      var _got_totalHit = false
      var `docs`: Seq[Metadata] = Seq[Metadata]()
      var _got_docs = false
      var _done = false
      _iprot.readStructBegin()
      while (!_done) {
        val _field = _iprot.readFieldBegin()
        if (_field.`type` == TType.STOP) {
          _done = true
        } else {
          _field.id match {
            case 1 => { /* totalHit */
              _field.`type` match {
                case TType.I32 => {
                  `totalHit` = {
                    _iprot.readI32()
                  }
                  _got_totalHit = true
                }
                case _ => TProtocolUtil.skip(_iprot, _field.`type`)
              }
            }
            case 2 => { /* docs */
              _field.`type` match {
                case TType.LIST => {
                  `docs` = {
                    val _list = _iprot.readListBegin()
                    val _rv = new mutable.ArrayBuffer[Metadata](_list.size)
                    var _i = 0
                    while (_i < _list.size) {
                      _rv += {
                        Metadata.decode(_iprot)
                      }
                      _i += 1
                    }
                    _iprot.readListEnd()
                    _rv
                  }
                  _got_docs = true
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
      if (!_got_totalHit) throw new TProtocolException("Required field 'totalHit' was not found in serialized data for struct SearchResult")
      if (!_got_docs) throw new TProtocolException("Required field 'docs' was not found in serialized data for struct SearchResult")
      new Immutable(
        `totalHit`,
        `docs`
      )
    }
  }

  /**
   * The default read-only implementation of SearchResult.  You typically should not need to
   * directly reference this class; instead, use the SearchResult.apply method to construct
   * new instances.
   */
  class Immutable(
    val `totalHit`: Int,
    val `docs`: Seq[Metadata] = Seq[Metadata]()
  ) extends SearchResult

  /**
   * This Proxy trait allows you to extend the SearchResult trait with additional state or
   * behavior and implement the read-only methods from SearchResult using an underlying
   * instance.
   */
  trait Proxy extends SearchResult {
    protected def _underlyingSearchResult: SearchResult
    def `totalHit`: Int = _underlyingSearchResult.`totalHit`
    def `docs`: Seq[Metadata] = _underlyingSearchResult.`docs`
  }
}

trait SearchResult extends ThriftStruct
  with Product2[Int, Seq[Metadata]]
  with java.io.Serializable
{
  import SearchResult._

  def `totalHit`: Int
  def `docs`: Seq[Metadata]

  def _1 = `totalHit`
  def _2 = `docs`

  override def write(_oprot: TProtocol) {
    validate()
    _oprot.writeStructBegin(Struct)
    if (true) {
      val `totalHit_item` = `totalHit`
      _oprot.writeFieldBegin(TotalHitField)
      _oprot.writeI32(`totalHit_item`)
      _oprot.writeFieldEnd()
    }
    if (true) {
      val `docs_item` = `docs`
      _oprot.writeFieldBegin(DocsField)
      _oprot.writeListBegin(new TList(TType.STRUCT, `docs_item`.size))
      `docs_item`.foreach { `_docs_item_element` =>
        `_docs_item_element`.write(_oprot)
      }
      _oprot.writeListEnd()
      _oprot.writeFieldEnd()
    }
    _oprot.writeFieldStop()
    _oprot.writeStructEnd()
  }

  def copy(
    `totalHit`: Int = this.`totalHit`,
    `docs`: Seq[Metadata] = this.`docs`
  ): SearchResult = new Immutable(
    `totalHit`,
    `docs`
  )

  /**
   * Checks that all required fields are non-null.
   */
  def validate() {
    if (`docs` == null) throw new TProtocolException("Required field 'docs' cannot be null")
  }

  def canEqual(other: Any) = other.isInstanceOf[SearchResult]

  override def equals(other: Any): Boolean = runtime.ScalaRunTime._equals(this, other)

  override def hashCode: Int = runtime.ScalaRunTime._hashCode(this)

  override def toString: String = runtime.ScalaRunTime._toString(this)

  override def productArity = 2

  override def productElement(n: Int): Any = n match {
    case 0 => `totalHit`
    case 1 => `docs`
    case _ => throw new IndexOutOfBoundsException(n.toString)
  }

  override def productPrefix = "SearchResult"
}