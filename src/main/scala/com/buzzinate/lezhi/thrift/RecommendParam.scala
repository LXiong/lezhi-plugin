package com.buzzinate.lezhi.thrift

import com.twitter.scrooge.{ThriftStruct, ThriftStructCodec}
import org.apache.thrift.protocol._
import java.nio.ByteBuffer
import com.twitter.finagle.SourcedException
import scala.collection.mutable
import scala.collection.{Map, Set}

object RecommendParam extends ThriftStructCodec[RecommendParam] {
  val Struct = new TStruct("RecommendParam")
  val UrlField = new TField("url", TType.STRING, 1)
  val TypesField = new TField("types", TType.LIST, 2)
  val TitleField = new TField("title", TType.STRING, 3)
  val SiteprefixField = new TField("siteprefix", TType.STRING, 4)
  val UseridField = new TField("userid", TType.STRING, 5)
  val KeywordsField = new TField("keywords", TType.STRING, 6)
  val CanonicalUrlField = new TField("canonicalUrl", TType.STRING, 7)
  val CustomThumbnailField = new TField("customThumbnail", TType.STRING, 8)
  val CustomTitleField = new TField("customTitle", TType.STRING, 9)

  def encode(_item: RecommendParam, _oproto: TProtocol) { _item.write(_oproto) }
  def decode(_iprot: TProtocol) = Immutable.decode(_iprot)

  def apply(_iprot: TProtocol): RecommendParam = decode(_iprot)

  def apply(
    `url`: String,
    `types`: Seq[RecommendTypeParam] = Seq[RecommendTypeParam](),
    `title`: Option[String] = None,
    `siteprefix`: Option[String] = None,
    `userid`: String = "1",
    `keywords`: Option[String] = None,
    `canonicalUrl`: Option[String] = None,
    `customThumbnail`: Option[String] = None,
    `customTitle`: Option[String] = None
  ): RecommendParam = new Immutable(
    `url`,
    `types`,
    `title`,
    `siteprefix`,
    `userid`,
    `keywords`,
    `canonicalUrl`,
    `customThumbnail`,
    `customTitle`
  )

  def unapply(_item: RecommendParam): Option[Product9[String, Seq[RecommendTypeParam], Option[String], Option[String], String, Option[String], Option[String], Option[String], Option[String]]] = Some(_item)

  object Immutable extends ThriftStructCodec[RecommendParam] {
    def encode(_item: RecommendParam, _oproto: TProtocol) { _item.write(_oproto) }
    def decode(_iprot: TProtocol) = {
      var `url`: String = null
      var _got_url = false
      var `types`: Seq[RecommendTypeParam] = Seq[RecommendTypeParam]()
      var _got_types = false
      var `title`: String = null
      var _got_title = false
      var `siteprefix`: String = null
      var _got_siteprefix = false
      var `userid`: String = "1"
      var _got_userid = false
      var `keywords`: String = null
      var _got_keywords = false
      var `canonicalUrl`: String = null
      var _got_canonicalUrl = false
      var `customThumbnail`: String = null
      var _got_customThumbnail = false
      var `customTitle`: String = null
      var _got_customTitle = false
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
            case 2 => { /* types */
              _field.`type` match {
                case TType.LIST => {
                  `types` = {
                    val _list = _iprot.readListBegin()
                    val _rv = new mutable.ArrayBuffer[RecommendTypeParam](_list.size)
                    var _i = 0
                    while (_i < _list.size) {
                      _rv += {
                        RecommendTypeParam.decode(_iprot)
                      }
                      _i += 1
                    }
                    _iprot.readListEnd()
                    _rv
                  }
                  _got_types = true
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
            case 6 => { /* keywords */
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
            case 7 => { /* canonicalUrl */
              _field.`type` match {
                case TType.STRING => {
                  `canonicalUrl` = {
                    _iprot.readString()
                  }
                  _got_canonicalUrl = true
                }
                case _ => TProtocolUtil.skip(_iprot, _field.`type`)
              }
            }
            case 8 => { /* customThumbnail */
              _field.`type` match {
                case TType.STRING => {
                  `customThumbnail` = {
                    _iprot.readString()
                  }
                  _got_customThumbnail = true
                }
                case _ => TProtocolUtil.skip(_iprot, _field.`type`)
              }
            }
            case 9 => { /* customTitle */
              _field.`type` match {
                case TType.STRING => {
                  `customTitle` = {
                    _iprot.readString()
                  }
                  _got_customTitle = true
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
      if (!_got_url) throw new TProtocolException("Required field 'url' was not found in serialized data for struct RecommendParam")
      if (!_got_types) throw new TProtocolException("Required field 'types' was not found in serialized data for struct RecommendParam")
      new Immutable(
        `url`,
        `types`,
        if (_got_title) Some(`title`) else None,
        if (_got_siteprefix) Some(`siteprefix`) else None,
        `userid`,
        if (_got_keywords) Some(`keywords`) else None,
        if (_got_canonicalUrl) Some(`canonicalUrl`) else None,
        if (_got_customThumbnail) Some(`customThumbnail`) else None,
        if (_got_customTitle) Some(`customTitle`) else None
      )
    }
  }

  /**
   * The default read-only implementation of RecommendParam.  You typically should not need to
   * directly reference this class; instead, use the RecommendParam.apply method to construct
   * new instances.
   */
  class Immutable(
    val `url`: String,
    val `types`: Seq[RecommendTypeParam] = Seq[RecommendTypeParam](),
    val `title`: Option[String] = None,
    val `siteprefix`: Option[String] = None,
    val `userid`: String = "1",
    val `keywords`: Option[String] = None,
    val `canonicalUrl`: Option[String] = None,
    val `customThumbnail`: Option[String] = None,
    val `customTitle`: Option[String] = None
  ) extends RecommendParam

  /**
   * This Proxy trait allows you to extend the RecommendParam trait with additional state or
   * behavior and implement the read-only methods from RecommendParam using an underlying
   * instance.
   */
  trait Proxy extends RecommendParam {
    protected def _underlyingRecommendParam: RecommendParam
    def `url`: String = _underlyingRecommendParam.`url`
    def `types`: Seq[RecommendTypeParam] = _underlyingRecommendParam.`types`
    def `title`: Option[String] = _underlyingRecommendParam.`title`
    def `siteprefix`: Option[String] = _underlyingRecommendParam.`siteprefix`
    def `userid`: String = _underlyingRecommendParam.`userid`
    def `keywords`: Option[String] = _underlyingRecommendParam.`keywords`
    def `canonicalUrl`: Option[String] = _underlyingRecommendParam.`canonicalUrl`
    def `customThumbnail`: Option[String] = _underlyingRecommendParam.`customThumbnail`
    def `customTitle`: Option[String] = _underlyingRecommendParam.`customTitle`
  }
}

trait RecommendParam extends ThriftStruct
  with Product9[String, Seq[RecommendTypeParam], Option[String], Option[String], String, Option[String], Option[String], Option[String], Option[String]]
  with java.io.Serializable
{
  import RecommendParam._

  def `url`: String
  def `types`: Seq[RecommendTypeParam]
  def `title`: Option[String]
  def `siteprefix`: Option[String]
  def `userid`: String
  def `keywords`: Option[String]
  def `canonicalUrl`: Option[String]
  def `customThumbnail`: Option[String]
  def `customTitle`: Option[String]

  def _1 = `url`
  def _2 = `types`
  def _3 = `title`
  def _4 = `siteprefix`
  def _5 = `userid`
  def _6 = `keywords`
  def _7 = `canonicalUrl`
  def _8 = `customThumbnail`
  def _9 = `customTitle`

  override def write(_oprot: TProtocol) {
    validate()
    _oprot.writeStructBegin(Struct)
    if (true) {
      val `url_item` = `url`
      _oprot.writeFieldBegin(UrlField)
      _oprot.writeString(`url_item`)
      _oprot.writeFieldEnd()
    }
    if (true) {
      val `types_item` = `types`
      _oprot.writeFieldBegin(TypesField)
      _oprot.writeListBegin(new TList(TType.STRUCT, `types_item`.size))
      `types_item`.foreach { `_types_item_element` =>
        `_types_item_element`.write(_oprot)
      }
      _oprot.writeListEnd()
      _oprot.writeFieldEnd()
    }
    if (`title`.isDefined) {
      val `title_item` = `title`.get
      _oprot.writeFieldBegin(TitleField)
      _oprot.writeString(`title_item`)
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
    if (`keywords`.isDefined) {
      val `keywords_item` = `keywords`.get
      _oprot.writeFieldBegin(KeywordsField)
      _oprot.writeString(`keywords_item`)
      _oprot.writeFieldEnd()
    }
    if (`canonicalUrl`.isDefined) {
      val `canonicalUrl_item` = `canonicalUrl`.get
      _oprot.writeFieldBegin(CanonicalUrlField)
      _oprot.writeString(`canonicalUrl_item`)
      _oprot.writeFieldEnd()
    }
    if (`customThumbnail`.isDefined) {
      val `customThumbnail_item` = `customThumbnail`.get
      _oprot.writeFieldBegin(CustomThumbnailField)
      _oprot.writeString(`customThumbnail_item`)
      _oprot.writeFieldEnd()
    }
    if (`customTitle`.isDefined) {
      val `customTitle_item` = `customTitle`.get
      _oprot.writeFieldBegin(CustomTitleField)
      _oprot.writeString(`customTitle_item`)
      _oprot.writeFieldEnd()
    }
    _oprot.writeFieldStop()
    _oprot.writeStructEnd()
  }

  def copy(
    `url`: String = this.`url`,
    `types`: Seq[RecommendTypeParam] = this.`types`,
    `title`: Option[String] = this.`title`,
    `siteprefix`: Option[String] = this.`siteprefix`,
    `userid`: String = this.`userid`,
    `keywords`: Option[String] = this.`keywords`,
    `canonicalUrl`: Option[String] = this.`canonicalUrl`,
    `customThumbnail`: Option[String] = this.`customThumbnail`,
    `customTitle`: Option[String] = this.`customTitle`
  ): RecommendParam = new Immutable(
    `url`,
    `types`,
    `title`,
    `siteprefix`,
    `userid`,
    `keywords`,
    `canonicalUrl`,
    `customThumbnail`,
    `customTitle`
  )

  /**
   * Checks that all required fields are non-null.
   */
  def validate() {
    if (`url` == null) throw new TProtocolException("Required field 'url' cannot be null")
    if (`types` == null) throw new TProtocolException("Required field 'types' cannot be null")
  }

  def canEqual(other: Any) = other.isInstanceOf[RecommendParam]

  override def equals(other: Any): Boolean = runtime.ScalaRunTime._equals(this, other)

  override def hashCode: Int = runtime.ScalaRunTime._hashCode(this)

  override def toString: String = runtime.ScalaRunTime._toString(this)

  override def productArity = 9

  override def productElement(n: Int): Any = n match {
    case 0 => `url`
    case 1 => `types`
    case 2 => `title`
    case 3 => `siteprefix`
    case 4 => `userid`
    case 5 => `keywords`
    case 6 => `canonicalUrl`
    case 7 => `customThumbnail`
    case 8 => `customTitle`
    case _ => throw new IndexOutOfBoundsException(n.toString)
  }

  override def productPrefix = "RecommendParam"
}