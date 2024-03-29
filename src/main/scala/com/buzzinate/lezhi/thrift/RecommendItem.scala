package com.buzzinate.lezhi.thrift

import com.twitter.scrooge.{ThriftStruct, ThriftStructCodec}
import org.apache.thrift.protocol._
import java.nio.ByteBuffer
import com.twitter.finagle.SourcedException
import scala.collection.mutable
import scala.collection.{Map, Set}

object RecommendItem extends ThriftStructCodec[RecommendItem] {
  val Struct = new TStruct("RecommendItem")
  val UrlField = new TField("url", TType.STRING, 1)
  val TitleField = new TField("title", TType.STRING, 2)
  val PicField = new TField("pic", TType.STRING, 3)
  val ScoreField = new TField("score", TType.DOUBLE, 4)
  val HotScoreField = new TField("hotScore", TType.DOUBLE, 5)

  def encode(_item: RecommendItem, _oproto: TProtocol) { _item.write(_oproto) }
  def decode(_iprot: TProtocol) = Immutable.decode(_iprot)

  def apply(_iprot: TProtocol): RecommendItem = decode(_iprot)

  def apply(
    `url`: String,
    `title`: String,
    `pic`: Option[String] = None,
    `score`: Option[Double] = None,
    `hotScore`: Option[Double] = None
  ): RecommendItem = new Immutable(
    `url`,
    `title`,
    `pic`,
    `score`,
    `hotScore`
  )

  def unapply(_item: RecommendItem): Option[Product5[String, String, Option[String], Option[Double], Option[Double]]] = Some(_item)

  object Immutable extends ThriftStructCodec[RecommendItem] {
    def encode(_item: RecommendItem, _oproto: TProtocol) { _item.write(_oproto) }
    def decode(_iprot: TProtocol) = {
      var `url`: String = null
      var _got_url = false
      var `title`: String = null
      var _got_title = false
      var `pic`: String = null
      var _got_pic = false
      var `score`: Double = 0.0
      var _got_score = false
      var `hotScore`: Double = 0.0
      var _got_hotScore = false
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
            case 2 => { /* title */
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
            case 3 => { /* pic */
              _field.`type` match {
                case TType.STRING => {
                  `pic` = {
                    _iprot.readString()
                  }
                  _got_pic = true
                }
                case _ => TProtocolUtil.skip(_iprot, _field.`type`)
              }
            }
            case 4 => { /* score */
              _field.`type` match {
                case TType.DOUBLE => {
                  `score` = {
                    _iprot.readDouble()
                  }
                  _got_score = true
                }
                case _ => TProtocolUtil.skip(_iprot, _field.`type`)
              }
            }
            case 5 => { /* hotScore */
              _field.`type` match {
                case TType.DOUBLE => {
                  `hotScore` = {
                    _iprot.readDouble()
                  }
                  _got_hotScore = true
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
      if (!_got_url) throw new TProtocolException("Required field 'url' was not found in serialized data for struct RecommendItem")
      if (!_got_title) throw new TProtocolException("Required field 'title' was not found in serialized data for struct RecommendItem")
      new Immutable(
        `url`,
        `title`,
        if (_got_pic) Some(`pic`) else None,
        if (_got_score) Some(`score`) else None,
        if (_got_hotScore) Some(`hotScore`) else None
      )
    }
  }

  /**
   * The default read-only implementation of RecommendItem.  You typically should not need to
   * directly reference this class; instead, use the RecommendItem.apply method to construct
   * new instances.
   */
  class Immutable(
    val `url`: String,
    val `title`: String,
    val `pic`: Option[String] = None,
    val `score`: Option[Double] = None,
    val `hotScore`: Option[Double] = None
  ) extends RecommendItem

  /**
   * This Proxy trait allows you to extend the RecommendItem trait with additional state or
   * behavior and implement the read-only methods from RecommendItem using an underlying
   * instance.
   */
  trait Proxy extends RecommendItem {
    protected def _underlyingRecommendItem: RecommendItem
    def `url`: String = _underlyingRecommendItem.`url`
    def `title`: String = _underlyingRecommendItem.`title`
    def `pic`: Option[String] = _underlyingRecommendItem.`pic`
    def `score`: Option[Double] = _underlyingRecommendItem.`score`
    def `hotScore`: Option[Double] = _underlyingRecommendItem.`hotScore`
  }
}

trait RecommendItem extends ThriftStruct
  with Product5[String, String, Option[String], Option[Double], Option[Double]]
  with java.io.Serializable
{
  import RecommendItem._

  def `url`: String
  def `title`: String
  def `pic`: Option[String]
  def `score`: Option[Double]
  def `hotScore`: Option[Double]

  def _1 = `url`
  def _2 = `title`
  def _3 = `pic`
  def _4 = `score`
  def _5 = `hotScore`

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
      val `title_item` = `title`
      _oprot.writeFieldBegin(TitleField)
      _oprot.writeString(`title_item`)
      _oprot.writeFieldEnd()
    }
    if (`pic`.isDefined) {
      val `pic_item` = `pic`.get
      _oprot.writeFieldBegin(PicField)
      _oprot.writeString(`pic_item`)
      _oprot.writeFieldEnd()
    }
    if (`score`.isDefined) {
      val `score_item` = `score`.get
      _oprot.writeFieldBegin(ScoreField)
      _oprot.writeDouble(`score_item`)
      _oprot.writeFieldEnd()
    }
    if (`hotScore`.isDefined) {
      val `hotScore_item` = `hotScore`.get
      _oprot.writeFieldBegin(HotScoreField)
      _oprot.writeDouble(`hotScore_item`)
      _oprot.writeFieldEnd()
    }
    _oprot.writeFieldStop()
    _oprot.writeStructEnd()
  }

  def copy(
    `url`: String = this.`url`,
    `title`: String = this.`title`,
    `pic`: Option[String] = this.`pic`,
    `score`: Option[Double] = this.`score`,
    `hotScore`: Option[Double] = this.`hotScore`
  ): RecommendItem = new Immutable(
    `url`,
    `title`,
    `pic`,
    `score`,
    `hotScore`
  )

  /**
   * Checks that all required fields are non-null.
   */
  def validate() {
    if (`url` == null) throw new TProtocolException("Required field 'url' cannot be null")
    if (`title` == null) throw new TProtocolException("Required field 'title' cannot be null")
  }

  def canEqual(other: Any) = other.isInstanceOf[RecommendItem]

  override def equals(other: Any): Boolean = runtime.ScalaRunTime._equals(this, other)

  override def hashCode: Int = runtime.ScalaRunTime._hashCode(this)

  override def toString: String = runtime.ScalaRunTime._toString(this)

  override def productArity = 5

  override def productElement(n: Int): Any = n match {
    case 0 => `url`
    case 1 => `title`
    case 2 => `pic`
    case 3 => `score`
    case 4 => `hotScore`
    case _ => throw new IndexOutOfBoundsException(n.toString)
  }

  override def productPrefix = "RecommendItem"
}