package com.buzzinate.lezhi.thrift

import com.twitter.scrooge.{ThriftStruct, ThriftStructCodec}
import org.apache.thrift.protocol._
import java.nio.ByteBuffer
import com.twitter.finagle.SourcedException
import scala.collection.mutable
import scala.collection.{Map, Set}

object Metadata extends ThriftStructCodec[Metadata] {
  val Struct = new TStruct("Metadata")
  val UrlField = new TField("url", TType.STRING, 1)
  val ThumbnailField = new TField("thumbnail", TType.STRING, 2)
  val TitleField = new TField("title", TType.STRING, 3)
  val KeywordsField = new TField("keywords", TType.STRING, 4)
  val BlackKeywordsField = new TField("blackKeywords", TType.STRING, 5)
  val StatusField = new TField("status", TType.I32, 6)

  def encode(_item: Metadata, _oproto: TProtocol) { _item.write(_oproto) }
  def decode(_iprot: TProtocol) = Immutable.decode(_iprot)

  def apply(_iprot: TProtocol): Metadata = decode(_iprot)

  def apply(
    `url`: String,
    `thumbnail`: Option[String] = None,
    `title`: Option[String] = None,
    `keywords`: Option[String] = None,
    `blackKeywords`: Option[String] = None,
    `status`: Option[StatusEnum] = None
  ): Metadata = new Immutable(
    `url`,
    `thumbnail`,
    `title`,
    `keywords`,
    `blackKeywords`,
    `status`
  )

  def unapply(_item: Metadata): Option[Product6[String, Option[String], Option[String], Option[String], Option[String], Option[StatusEnum]]] = Some(_item)

  object Immutable extends ThriftStructCodec[Metadata] {
    def encode(_item: Metadata, _oproto: TProtocol) { _item.write(_oproto) }
    def decode(_iprot: TProtocol) = {
      var `url`: String = null
      var _got_url = false
      var `thumbnail`: String = null
      var _got_thumbnail = false
      var `title`: String = null
      var _got_title = false
      var `keywords`: String = null
      var _got_keywords = false
      var `blackKeywords`: String = null
      var _got_blackKeywords = false
      var `status`: StatusEnum = null
      var _got_status = false
      var _done = false
      _iprot.readStructBegin()
      while (!_done) {
        val _field = _iprot.readFieldBegin()
        if (_field.`type` == TType.STOP) {
          _done = true
        } else {
          _field.id match {
            case 1 => { /* url */
              _field.`type` match {
                case TType.STRING => {
                  `url` = {
                    _iprot.readString()
                  }
                  _got_url = true
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
            case 3 => { /* title */
              _field.`type` match {
                case TType.STRING => {
                  `title` = {
                    _iprot.readString()
                  }
                  _got_title = true
                }
                case _ => TProtocolUtil.skip(_iprot, _field.`type`)
              }
            }
            case 4 => { /* keywords */
              _field.`type` match {
                case TType.STRING => {
                  `keywords` = {
                    _iprot.readString()
                  }
                  _got_keywords = true
                }
                case _ => TProtocolUtil.skip(_iprot, _field.`type`)
              }
            }
            case 5 => { /* blackKeywords */
              _field.`type` match {
                case TType.STRING => {
                  `blackKeywords` = {
                    _iprot.readString()
                  }
                  _got_blackKeywords = true
                }
                case _ => TProtocolUtil.skip(_iprot, _field.`type`)
              }
            }
            case 6 => { /* status */
              _field.`type` match {
                case TType.I32 => {
                  `status` = {
                    StatusEnum(_iprot.readI32())
                  }
                  _got_status = true
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
      if (!_got_url) throw new TProtocolException("Required field 'url' was not found in serialized data for struct Metadata")
      new Immutable(
        `url`,
        if (_got_thumbnail) Some(`thumbnail`) else None,
        if (_got_title) Some(`title`) else None,
        if (_got_keywords) Some(`keywords`) else None,
        if (_got_blackKeywords) Some(`blackKeywords`) else None,
        if (_got_status) Some(`status`) else None
      )
    }
  }

  /**
   * The default read-only implementation of Metadata.  You typically should not need to
   * directly reference this class; instead, use the Metadata.apply method to construct
   * new instances.
   */
  class Immutable(
    val `url`: String,
    val `thumbnail`: Option[String] = None,
    val `title`: Option[String] = None,
    val `keywords`: Option[String] = None,
    val `blackKeywords`: Option[String] = None,
    val `status`: Option[StatusEnum] = None
  ) extends Metadata

  /**
   * This Proxy trait allows you to extend the Metadata trait with additional state or
   * behavior and implement the read-only methods from Metadata using an underlying
   * instance.
   */
  trait Proxy extends Metadata {
    protected def _underlyingMetadata: Metadata
    def `url`: String = _underlyingMetadata.`url`
    def `thumbnail`: Option[String] = _underlyingMetadata.`thumbnail`
    def `title`: Option[String] = _underlyingMetadata.`title`
    def `keywords`: Option[String] = _underlyingMetadata.`keywords`
    def `blackKeywords`: Option[String] = _underlyingMetadata.`blackKeywords`
    def `status`: Option[StatusEnum] = _underlyingMetadata.`status`
  }
}

trait Metadata extends ThriftStruct
  with Product6[String, Option[String], Option[String], Option[String], Option[String], Option[StatusEnum]]
  with java.io.Serializable
{
  import Metadata._

  def `url`: String
  def `thumbnail`: Option[String]
  def `title`: Option[String]
  def `keywords`: Option[String]
  def `blackKeywords`: Option[String]
  def `status`: Option[StatusEnum]

  def _1 = `url`
  def _2 = `thumbnail`
  def _3 = `title`
  def _4 = `keywords`
  def _5 = `blackKeywords`
  def _6 = `status`

  override def write(_oprot: TProtocol) {
    validate()
    _oprot.writeStructBegin(Struct)
    if (true) {
      val `url_item` = `url`
      _oprot.writeFieldBegin(UrlField)
      _oprot.writeString(`url_item`)
      _oprot.writeFieldEnd()
    }
    if (`thumbnail`.isDefined) {
      val `thumbnail_item` = `thumbnail`.get
      _oprot.writeFieldBegin(ThumbnailField)
      _oprot.writeString(`thumbnail_item`)
      _oprot.writeFieldEnd()
    }
    if (`title`.isDefined) {
      val `title_item` = `title`.get
      _oprot.writeFieldBegin(TitleField)
      _oprot.writeString(`title_item`)
      _oprot.writeFieldEnd()
    }
    if (`keywords`.isDefined) {
      val `keywords_item` = `keywords`.get
      _oprot.writeFieldBegin(KeywordsField)
      _oprot.writeString(`keywords_item`)
      _oprot.writeFieldEnd()
    }
    if (`blackKeywords`.isDefined) {
      val `blackKeywords_item` = `blackKeywords`.get
      _oprot.writeFieldBegin(BlackKeywordsField)
      _oprot.writeString(`blackKeywords_item`)
      _oprot.writeFieldEnd()
    }
    if (`status`.isDefined) {
      val `status_item` = `status`.get
      _oprot.writeFieldBegin(StatusField)
      _oprot.writeI32(`status_item`.value)
      _oprot.writeFieldEnd()
    }
    _oprot.writeFieldStop()
    _oprot.writeStructEnd()
  }

  def copy(
    `url`: String = this.`url`,
    `thumbnail`: Option[String] = this.`thumbnail`,
    `title`: Option[String] = this.`title`,
    `keywords`: Option[String] = this.`keywords`,
    `blackKeywords`: Option[String] = this.`blackKeywords`,
    `status`: Option[StatusEnum] = this.`status`
  ): Metadata = new Immutable(
    `url`,
    `thumbnail`,
    `title`,
    `keywords`,
    `blackKeywords`,
    `status`
  )

  /**
   * Checks that all required fields are non-null.
   */
  def validate() {
    if (`url` == null) throw new TProtocolException("Required field 'url' cannot be null")
  }

  def canEqual(other: Any) = other.isInstanceOf[Metadata]

  override def equals(other: Any): Boolean = runtime.ScalaRunTime._equals(this, other)

  override def hashCode: Int = runtime.ScalaRunTime._hashCode(this)

  override def toString: String = runtime.ScalaRunTime._toString(this)

  override def productArity = 6

  override def productElement(n: Int): Any = n match {
    case 0 => `url`
    case 1 => `thumbnail`
    case 2 => `title`
    case 3 => `keywords`
    case 4 => `blackKeywords`
    case 5 => `status`
    case _ => throw new IndexOutOfBoundsException(n.toString)
  }

  override def productPrefix = "Metadata"
}