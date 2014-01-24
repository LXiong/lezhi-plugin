package com.buzzinate.lezhi.thrift

import com.twitter.conversions.time._
import com.twitter.finagle.SourcedException
import com.twitter.scrooge.{ThriftStruct, ThriftStructCodec}
import com.twitter.util.Future
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import org.apache.thrift.protocol._
import org.apache.thrift.TApplicationException
import scala.collection.mutable
import scala.collection.{Map, Set}
import com.twitter.finagle.{Service => FinagleService}
import com.twitter.finagle.stats.{NullStatsReceiver, StatsReceiver}
import com.twitter.finagle.thrift.ThriftClientRequest
import com.twitter.finagle.{Service => FinagleService}
import java.util.Arrays
import org.apache.thrift.transport.{TMemoryBuffer, TMemoryInputTransport, TTransport}
import com.twitter.finagle.builder.{Server, ServerBuilder}
import com.twitter.finagle.stats.{StatsReceiver, OstrichStatsReceiver}
import com.twitter.finagle.thrift.ThriftServerFramedCodec
import com.twitter.finagle.tracing.{NullTracer, Tracer}
import com.twitter.logging.Logger
import com.twitter.ostrich.admin.Service
import com.twitter.util.Duration
import java.util.concurrent.atomic.AtomicReference


object RecommendServices {
  trait Iface {
    def recommend(`param`: RecommendParam): RecommendResult
    def click(`param`: ClickParam): Unit
    def recrawl(`url`: String): Unit
    def correctImg(`url`: String, `rightImg`: String, `userAgent`: Option[String]): Unit
  }

  trait FutureIface {
    def recommend(`param`: RecommendParam): Future[RecommendResult]
    def click(`param`: ClickParam): Future[Unit]
    def recrawl(`url`: String): Future[Unit]
    def correctImg(`url`: String, `rightImg`: String, `userAgent`: Option[String]): Future[Unit]
  }

  object recommend_args extends ThriftStructCodec[recommend_args] {
    val Struct = new TStruct("recommend_args")
    val ParamField = new TField("param", TType.STRUCT, 1)
  
    def encode(_item: recommend_args, _oproto: TProtocol) { _item.write(_oproto) }
    def decode(_iprot: TProtocol) = Immutable.decode(_iprot)
  
    def apply(_iprot: TProtocol): recommend_args = decode(_iprot)
  
    def apply(
      `param`: RecommendParam
    ): recommend_args = new Immutable(
      `param`
    )
  
    def unapply(_item: recommend_args): Option[RecommendParam] = Some(_item.param)
  
    object Immutable extends ThriftStructCodec[recommend_args] {
      def encode(_item: recommend_args, _oproto: TProtocol) { _item.write(_oproto) }
      def decode(_iprot: TProtocol) = {
        var `param`: RecommendParam = null
        var _got_param = false
        var _done = false
        _iprot.readStructBegin()
        while (!_done) {
          val _field = _iprot.readFieldBegin()
          if (_field.`type` == TType.STOP) {
            _done = true
          } else {
            _field.id match {
              case 1 => { /* param */
                _field.`type` match {
                  case TType.STRUCT => {
                    `param` = {
                      RecommendParam.decode(_iprot)
                    }
                    _got_param = true
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
        new Immutable(
          `param`
        )
      }
    }
  
    /**
     * The default read-only implementation of recommend_args.  You typically should not need to
     * directly reference this class; instead, use the recommend_args.apply method to construct
     * new instances.
     */
    class Immutable(
      val `param`: RecommendParam
    ) extends recommend_args
  
  }
  
  trait recommend_args extends ThriftStruct
    with Product1[RecommendParam]
    with java.io.Serializable
  {
    import recommend_args._
  
    def `param`: RecommendParam
  
    def _1 = `param`
  
    override def write(_oprot: TProtocol) {
      validate()
      _oprot.writeStructBegin(Struct)
      if (true) {
        val `param_item` = `param`
        _oprot.writeFieldBegin(ParamField)
        `param_item`.write(_oprot)
        _oprot.writeFieldEnd()
      }
      _oprot.writeFieldStop()
      _oprot.writeStructEnd()
    }
  
    def copy(
      `param`: RecommendParam = this.`param`
    ): recommend_args = new Immutable(
      `param`
    )
  
    /**
     * Checks that all required fields are non-null.
     */
    def validate() {
    }
  
    def canEqual(other: Any) = other.isInstanceOf[recommend_args]
  
    override def equals(other: Any): Boolean = runtime.ScalaRunTime._equals(this, other)
  
    override def hashCode: Int = runtime.ScalaRunTime._hashCode(this)
  
    override def toString: String = runtime.ScalaRunTime._toString(this)
  
    override def productArity = 1
  
    override def productElement(n: Int): Any = n match {
      case 0 => `param`
      case _ => throw new IndexOutOfBoundsException(n.toString)
    }
  
    override def productPrefix = "recommend_args"
  }
  object recommend_result extends ThriftStructCodec[recommend_result] {
    val Struct = new TStruct("recommend_result")
    val SuccessField = new TField("success", TType.STRUCT, 0)
  
    def encode(_item: recommend_result, _oproto: TProtocol) { _item.write(_oproto) }
    def decode(_iprot: TProtocol) = Immutable.decode(_iprot)
  
    def apply(_iprot: TProtocol): recommend_result = decode(_iprot)
  
    def apply(
      `success`: Option[RecommendResult] = None
    ): recommend_result = new Immutable(
      `success`
    )
  
    def unapply(_item: recommend_result): Option[Option[RecommendResult]] = Some(_item.success)
  
    object Immutable extends ThriftStructCodec[recommend_result] {
      def encode(_item: recommend_result, _oproto: TProtocol) { _item.write(_oproto) }
      def decode(_iprot: TProtocol) = {
        var `success`: RecommendResult = null
        var _got_success = false
        var _done = false
        _iprot.readStructBegin()
        while (!_done) {
          val _field = _iprot.readFieldBegin()
          if (_field.`type` == TType.STOP) {
            _done = true
          } else {
            _field.id match {
              case 0 => { /* success */
                _field.`type` match {
                  case TType.STRUCT => {
                    `success` = {
                      RecommendResult.decode(_iprot)
                    }
                    _got_success = true
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
        new Immutable(
          if (_got_success) Some(`success`) else None
        )
      }
    }
  
    /**
     * The default read-only implementation of recommend_result.  You typically should not need to
     * directly reference this class; instead, use the recommend_result.apply method to construct
     * new instances.
     */
    class Immutable(
      val `success`: Option[RecommendResult] = None
    ) extends recommend_result
  
  }
  
  trait recommend_result extends ThriftStruct
    with Product1[Option[RecommendResult]]
    with java.io.Serializable
  {
    import recommend_result._
  
    def `success`: Option[RecommendResult]
  
    def _1 = `success`
  
    override def write(_oprot: TProtocol) {
      validate()
      _oprot.writeStructBegin(Struct)
      if (`success`.isDefined) {
        val `success_item` = `success`.get
        _oprot.writeFieldBegin(SuccessField)
        `success_item`.write(_oprot)
        _oprot.writeFieldEnd()
      }
      _oprot.writeFieldStop()
      _oprot.writeStructEnd()
    }
  
    def copy(
      `success`: Option[RecommendResult] = this.`success`
    ): recommend_result = new Immutable(
      `success`
    )
  
    /**
     * Checks that all required fields are non-null.
     */
    def validate() {
    }
  
    def canEqual(other: Any) = other.isInstanceOf[recommend_result]
  
    override def equals(other: Any): Boolean = runtime.ScalaRunTime._equals(this, other)
  
    override def hashCode: Int = runtime.ScalaRunTime._hashCode(this)
  
    override def toString: String = runtime.ScalaRunTime._toString(this)
  
    override def productArity = 1
  
    override def productElement(n: Int): Any = n match {
      case 0 => `success`
      case _ => throw new IndexOutOfBoundsException(n.toString)
    }
  
    override def productPrefix = "recommend_result"
  }
  object click_args extends ThriftStructCodec[click_args] {
    val Struct = new TStruct("click_args")
    val ParamField = new TField("param", TType.STRUCT, 1)
  
    def encode(_item: click_args, _oproto: TProtocol) { _item.write(_oproto) }
    def decode(_iprot: TProtocol) = Immutable.decode(_iprot)
  
    def apply(_iprot: TProtocol): click_args = decode(_iprot)
  
    def apply(
      `param`: ClickParam
    ): click_args = new Immutable(
      `param`
    )
  
    def unapply(_item: click_args): Option[ClickParam] = Some(_item.param)
  
    object Immutable extends ThriftStructCodec[click_args] {
      def encode(_item: click_args, _oproto: TProtocol) { _item.write(_oproto) }
      def decode(_iprot: TProtocol) = {
        var `param`: ClickParam = null
        var _got_param = false
        var _done = false
        _iprot.readStructBegin()
        while (!_done) {
          val _field = _iprot.readFieldBegin()
          if (_field.`type` == TType.STOP) {
            _done = true
          } else {
            _field.id match {
              case 1 => { /* param */
                _field.`type` match {
                  case TType.STRUCT => {
                    `param` = {
                      ClickParam.decode(_iprot)
                    }
                    _got_param = true
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
        new Immutable(
          `param`
        )
      }
    }
  
    /**
     * The default read-only implementation of click_args.  You typically should not need to
     * directly reference this class; instead, use the click_args.apply method to construct
     * new instances.
     */
    class Immutable(
      val `param`: ClickParam
    ) extends click_args
  
  }
  
  trait click_args extends ThriftStruct
    with Product1[ClickParam]
    with java.io.Serializable
  {
    import click_args._
  
    def `param`: ClickParam
  
    def _1 = `param`
  
    override def write(_oprot: TProtocol) {
      validate()
      _oprot.writeStructBegin(Struct)
      if (true) {
        val `param_item` = `param`
        _oprot.writeFieldBegin(ParamField)
        `param_item`.write(_oprot)
        _oprot.writeFieldEnd()
      }
      _oprot.writeFieldStop()
      _oprot.writeStructEnd()
    }
  
    def copy(
      `param`: ClickParam = this.`param`
    ): click_args = new Immutable(
      `param`
    )
  
    /**
     * Checks that all required fields are non-null.
     */
    def validate() {
    }
  
    def canEqual(other: Any) = other.isInstanceOf[click_args]
  
    override def equals(other: Any): Boolean = runtime.ScalaRunTime._equals(this, other)
  
    override def hashCode: Int = runtime.ScalaRunTime._hashCode(this)
  
    override def toString: String = runtime.ScalaRunTime._toString(this)
  
    override def productArity = 1
  
    override def productElement(n: Int): Any = n match {
      case 0 => `param`
      case _ => throw new IndexOutOfBoundsException(n.toString)
    }
  
    override def productPrefix = "click_args"
  }
  object click_result extends ThriftStructCodec[click_result] {
    val Struct = new TStruct("click_result")
  
    def encode(_item: click_result, _oproto: TProtocol) { _item.write(_oproto) }
    def decode(_iprot: TProtocol) = Immutable.decode(_iprot)
  
    def apply(_iprot: TProtocol): click_result = decode(_iprot)
  
    def apply(
    ): click_result = new Immutable(
    )
  
    def unapply(_item: click_result): Boolean = true
  
    object Immutable extends ThriftStructCodec[click_result] {
      def encode(_item: click_result, _oproto: TProtocol) { _item.write(_oproto) }
      def decode(_iprot: TProtocol) = {
        var _done = false
        _iprot.readStructBegin()
        while (!_done) {
          val _field = _iprot.readFieldBegin()
          if (_field.`type` == TType.STOP) {
            _done = true
          } else {
            _field.id match {
              case _ => TProtocolUtil.skip(_iprot, _field.`type`)
            }
            _iprot.readFieldEnd()
          }
        }
        _iprot.readStructEnd()
        new Immutable(
        )
      }
    }
  
    /**
     * The default read-only implementation of click_result.  You typically should not need to
     * directly reference this class; instead, use the click_result.apply method to construct
     * new instances.
     */
    class Immutable(
    ) extends click_result
  
  }
  
  trait click_result extends ThriftStruct
    with Product
    with java.io.Serializable
  {
    import click_result._
  
  
  
    override def write(_oprot: TProtocol) {
      validate()
      _oprot.writeStructBegin(Struct)
      _oprot.writeFieldStop()
      _oprot.writeStructEnd()
    }
  
    def copy(
    ): click_result = new Immutable(
    )
  
    /**
     * Checks that all required fields are non-null.
     */
    def validate() {
    }
  
    def canEqual(other: Any) = other.isInstanceOf[click_result]
  
    override def equals(other: Any): Boolean = runtime.ScalaRunTime._equals(this, other)
  
    override def hashCode: Int = runtime.ScalaRunTime._hashCode(this)
  
    override def toString: String = runtime.ScalaRunTime._toString(this)
  
    override def productArity = 0
  
    override def productElement(n: Int): Any = n match {
      case _ => throw new IndexOutOfBoundsException(n.toString)
    }
  
    override def productPrefix = "click_result"
  }
  object recrawl_args extends ThriftStructCodec[recrawl_args] {
    val Struct = new TStruct("recrawl_args")
    val UrlField = new TField("url", TType.STRING, 1)
  
    def encode(_item: recrawl_args, _oproto: TProtocol) { _item.write(_oproto) }
    def decode(_iprot: TProtocol) = Immutable.decode(_iprot)
  
    def apply(_iprot: TProtocol): recrawl_args = decode(_iprot)
  
    def apply(
      `url`: String
    ): recrawl_args = new Immutable(
      `url`
    )
  
    def unapply(_item: recrawl_args): Option[String] = Some(_item.url)
  
    object Immutable extends ThriftStructCodec[recrawl_args] {
      def encode(_item: recrawl_args, _oproto: TProtocol) { _item.write(_oproto) }
      def decode(_iprot: TProtocol) = {
        var `url`: String = null
        var _got_url = false
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
              case _ => TProtocolUtil.skip(_iprot, _field.`type`)
            }
            _iprot.readFieldEnd()
          }
        }
        _iprot.readStructEnd()
        if (!_got_url) throw new TProtocolException("Required field 'url' was not found in serialized data for struct recrawl_args")
        new Immutable(
          `url`
        )
      }
    }
  
    /**
     * The default read-only implementation of recrawl_args.  You typically should not need to
     * directly reference this class; instead, use the recrawl_args.apply method to construct
     * new instances.
     */
    class Immutable(
      val `url`: String
    ) extends recrawl_args
  
  }
  
  trait recrawl_args extends ThriftStruct
    with Product1[String]
    with java.io.Serializable
  {
    import recrawl_args._
  
    def `url`: String
  
    def _1 = `url`
  
    override def write(_oprot: TProtocol) {
      validate()
      _oprot.writeStructBegin(Struct)
      if (true) {
        val `url_item` = `url`
        _oprot.writeFieldBegin(UrlField)
        _oprot.writeString(`url_item`)
        _oprot.writeFieldEnd()
      }
      _oprot.writeFieldStop()
      _oprot.writeStructEnd()
    }
  
    def copy(
      `url`: String = this.`url`
    ): recrawl_args = new Immutable(
      `url`
    )
  
    /**
     * Checks that all required fields are non-null.
     */
    def validate() {
      if (`url` == null) throw new TProtocolException("Required field 'url' cannot be null")
    }
  
    def canEqual(other: Any) = other.isInstanceOf[recrawl_args]
  
    override def equals(other: Any): Boolean = runtime.ScalaRunTime._equals(this, other)
  
    override def hashCode: Int = runtime.ScalaRunTime._hashCode(this)
  
    override def toString: String = runtime.ScalaRunTime._toString(this)
  
    override def productArity = 1
  
    override def productElement(n: Int): Any = n match {
      case 0 => `url`
      case _ => throw new IndexOutOfBoundsException(n.toString)
    }
  
    override def productPrefix = "recrawl_args"
  }
  object recrawl_result extends ThriftStructCodec[recrawl_result] {
    val Struct = new TStruct("recrawl_result")
  
    def encode(_item: recrawl_result, _oproto: TProtocol) { _item.write(_oproto) }
    def decode(_iprot: TProtocol) = Immutable.decode(_iprot)
  
    def apply(_iprot: TProtocol): recrawl_result = decode(_iprot)
  
    def apply(
    ): recrawl_result = new Immutable(
    )
  
    def unapply(_item: recrawl_result): Boolean = true
  
    object Immutable extends ThriftStructCodec[recrawl_result] {
      def encode(_item: recrawl_result, _oproto: TProtocol) { _item.write(_oproto) }
      def decode(_iprot: TProtocol) = {
        var _done = false
        _iprot.readStructBegin()
        while (!_done) {
          val _field = _iprot.readFieldBegin()
          if (_field.`type` == TType.STOP) {
            _done = true
          } else {
            _field.id match {
              case _ => TProtocolUtil.skip(_iprot, _field.`type`)
            }
            _iprot.readFieldEnd()
          }
        }
        _iprot.readStructEnd()
        new Immutable(
        )
      }
    }
  
    /**
     * The default read-only implementation of recrawl_result.  You typically should not need to
     * directly reference this class; instead, use the recrawl_result.apply method to construct
     * new instances.
     */
    class Immutable(
    ) extends recrawl_result
  
  }
  
  trait recrawl_result extends ThriftStruct
    with Product
    with java.io.Serializable
  {
    import recrawl_result._
  
  
  
    override def write(_oprot: TProtocol) {
      validate()
      _oprot.writeStructBegin(Struct)
      _oprot.writeFieldStop()
      _oprot.writeStructEnd()
    }
  
    def copy(
    ): recrawl_result = new Immutable(
    )
  
    /**
     * Checks that all required fields are non-null.
     */
    def validate() {
    }
  
    def canEqual(other: Any) = other.isInstanceOf[recrawl_result]
  
    override def equals(other: Any): Boolean = runtime.ScalaRunTime._equals(this, other)
  
    override def hashCode: Int = runtime.ScalaRunTime._hashCode(this)
  
    override def toString: String = runtime.ScalaRunTime._toString(this)
  
    override def productArity = 0
  
    override def productElement(n: Int): Any = n match {
      case _ => throw new IndexOutOfBoundsException(n.toString)
    }
  
    override def productPrefix = "recrawl_result"
  }
  object correctImg_args extends ThriftStructCodec[correctImg_args] {
    val Struct = new TStruct("correctImg_args")
    val UrlField = new TField("url", TType.STRING, 1)
    val RightImgField = new TField("rightImg", TType.STRING, 2)
    val UserAgentField = new TField("userAgent", TType.STRING, 3)
  
    def encode(_item: correctImg_args, _oproto: TProtocol) { _item.write(_oproto) }
    def decode(_iprot: TProtocol) = Immutable.decode(_iprot)
  
    def apply(_iprot: TProtocol): correctImg_args = decode(_iprot)
  
    def apply(
      `url`: String,
      `rightImg`: String,
      `userAgent`: Option[String] = None
    ): correctImg_args = new Immutable(
      `url`,
      `rightImg`,
      `userAgent`
    )
  
    def unapply(_item: correctImg_args): Option[Product3[String, String, Option[String]]] = Some(_item)
  
    object Immutable extends ThriftStructCodec[correctImg_args] {
      def encode(_item: correctImg_args, _oproto: TProtocol) { _item.write(_oproto) }
      def decode(_iprot: TProtocol) = {
        var `url`: String = null
        var _got_url = false
        var `rightImg`: String = null
        var _got_rightImg = false
        var `userAgent`: String = null
        var _got_userAgent = false
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
              case 2 => { /* rightImg */
                _field.`type` match {
                  case TType.STRING => {
                    `rightImg` = {
                      _iprot.readString()
                    }
                    _got_rightImg = true
                  }
                  case _ => TProtocolUtil.skip(_iprot, _field.`type`)
                }
              }
              case 3 => { /* userAgent */
                _field.`type` match {
                  case TType.STRING => {
                    `userAgent` = {
                      _iprot.readString()
                    }
                    _got_userAgent = true
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
        if (!_got_url) throw new TProtocolException("Required field 'url' was not found in serialized data for struct correctImg_args")
        if (!_got_rightImg) throw new TProtocolException("Required field 'rightImg' was not found in serialized data for struct correctImg_args")
        new Immutable(
          `url`,
          `rightImg`,
          if (_got_userAgent) Some(`userAgent`) else None
        )
      }
    }
  
    /**
     * The default read-only implementation of correctImg_args.  You typically should not need to
     * directly reference this class; instead, use the correctImg_args.apply method to construct
     * new instances.
     */
    class Immutable(
      val `url`: String,
      val `rightImg`: String,
      val `userAgent`: Option[String] = None
    ) extends correctImg_args
  
  }
  
  trait correctImg_args extends ThriftStruct
    with Product3[String, String, Option[String]]
    with java.io.Serializable
  {
    import correctImg_args._
  
    def `url`: String
    def `rightImg`: String
    def `userAgent`: Option[String]
  
    def _1 = `url`
    def _2 = `rightImg`
    def _3 = `userAgent`
  
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
        val `rightImg_item` = `rightImg`
        _oprot.writeFieldBegin(RightImgField)
        _oprot.writeString(`rightImg_item`)
        _oprot.writeFieldEnd()
      }
      if (`userAgent`.isDefined) {
        val `userAgent_item` = `userAgent`.get
        _oprot.writeFieldBegin(UserAgentField)
        _oprot.writeString(`userAgent_item`)
        _oprot.writeFieldEnd()
      }
      _oprot.writeFieldStop()
      _oprot.writeStructEnd()
    }
  
    def copy(
      `url`: String = this.`url`,
      `rightImg`: String = this.`rightImg`,
      `userAgent`: Option[String] = this.`userAgent`
    ): correctImg_args = new Immutable(
      `url`,
      `rightImg`,
      `userAgent`
    )
  
    /**
     * Checks that all required fields are non-null.
     */
    def validate() {
      if (`url` == null) throw new TProtocolException("Required field 'url' cannot be null")
      if (`rightImg` == null) throw new TProtocolException("Required field 'rightImg' cannot be null")
    }
  
    def canEqual(other: Any) = other.isInstanceOf[correctImg_args]
  
    override def equals(other: Any): Boolean = runtime.ScalaRunTime._equals(this, other)
  
    override def hashCode: Int = runtime.ScalaRunTime._hashCode(this)
  
    override def toString: String = runtime.ScalaRunTime._toString(this)
  
    override def productArity = 3
  
    override def productElement(n: Int): Any = n match {
      case 0 => `url`
      case 1 => `rightImg`
      case 2 => `userAgent`
      case _ => throw new IndexOutOfBoundsException(n.toString)
    }
  
    override def productPrefix = "correctImg_args"
  }
  object correctImg_result extends ThriftStructCodec[correctImg_result] {
    val Struct = new TStruct("correctImg_result")
  
    def encode(_item: correctImg_result, _oproto: TProtocol) { _item.write(_oproto) }
    def decode(_iprot: TProtocol) = Immutable.decode(_iprot)
  
    def apply(_iprot: TProtocol): correctImg_result = decode(_iprot)
  
    def apply(
    ): correctImg_result = new Immutable(
    )
  
    def unapply(_item: correctImg_result): Boolean = true
  
    object Immutable extends ThriftStructCodec[correctImg_result] {
      def encode(_item: correctImg_result, _oproto: TProtocol) { _item.write(_oproto) }
      def decode(_iprot: TProtocol) = {
        var _done = false
        _iprot.readStructBegin()
        while (!_done) {
          val _field = _iprot.readFieldBegin()
          if (_field.`type` == TType.STOP) {
            _done = true
          } else {
            _field.id match {
              case _ => TProtocolUtil.skip(_iprot, _field.`type`)
            }
            _iprot.readFieldEnd()
          }
        }
        _iprot.readStructEnd()
        new Immutable(
        )
      }
    }
  
    /**
     * The default read-only implementation of correctImg_result.  You typically should not need to
     * directly reference this class; instead, use the correctImg_result.apply method to construct
     * new instances.
     */
    class Immutable(
    ) extends correctImg_result
  
  }
  
  trait correctImg_result extends ThriftStruct
    with Product
    with java.io.Serializable
  {
    import correctImg_result._
  
  
  
    override def write(_oprot: TProtocol) {
      validate()
      _oprot.writeStructBegin(Struct)
      _oprot.writeFieldStop()
      _oprot.writeStructEnd()
    }
  
    def copy(
    ): correctImg_result = new Immutable(
    )
  
    /**
     * Checks that all required fields are non-null.
     */
    def validate() {
    }
  
    def canEqual(other: Any) = other.isInstanceOf[correctImg_result]
  
    override def equals(other: Any): Boolean = runtime.ScalaRunTime._equals(this, other)
  
    override def hashCode: Int = runtime.ScalaRunTime._hashCode(this)
  
    override def toString: String = runtime.ScalaRunTime._toString(this)
  
    override def productArity = 0
  
    override def productElement(n: Int): Any = n match {
      case _ => throw new IndexOutOfBoundsException(n.toString)
    }
  
    override def productPrefix = "correctImg_result"
  }
  class FinagledClient(
    service: FinagleService[ThriftClientRequest, Array[Byte]],
    protocolFactory: TProtocolFactory = new TBinaryProtocol.Factory,
    serviceName: String = "",
    stats: StatsReceiver = NullStatsReceiver
  ) extends FutureIface {
    // ----- boilerplate that should eventually be moved into finagle:
  
    protected def encodeRequest(name: String, args: ThriftStruct) = {
      val buf = new TMemoryBuffer(512)
      val oprot = protocolFactory.getProtocol(buf)
  
      oprot.writeMessageBegin(new TMessage(name, TMessageType.CALL, 0))
      args.write(oprot)
      oprot.writeMessageEnd()
  
      val bytes = Arrays.copyOfRange(buf.getArray, 0, buf.length)
      new ThriftClientRequest(bytes, false)
    }
  
    protected def decodeResponse[T <: ThriftStruct](resBytes: Array[Byte], codec: ThriftStructCodec[T]) = {
      val iprot = protocolFactory.getProtocol(new TMemoryInputTransport(resBytes))
      val msg = iprot.readMessageBegin()
      try {
        if (msg.`type` == TMessageType.EXCEPTION) {
          val exception = TApplicationException.read(iprot) match {
            case sourced: SourcedException =>
              if (serviceName != "") sourced.serviceName = serviceName
              sourced
            case e => e
          }
          throw exception
        } else {
          codec.decode(iprot)
        }
      } finally {
        iprot.readMessageEnd()
      }
    }
  
    protected def missingResult(name: String) = {
      new TApplicationException(
        TApplicationException.MISSING_RESULT,
        "`" + name + "` failed: unknown result"
      )
    }
  
    // ----- end boilerplate.
  
    private[this] val scopedStats = if (serviceName != "") stats.scope(serviceName) else stats
    private[this] object __stats_recommend {
      val RequestsCounter = scopedStats.scope("recommend").counter("requests")
      val SuccessCounter = scopedStats.scope("recommend").counter("success")
      val FailuresCounter = scopedStats.scope("recommend").counter("failures")
      val FailuresScope = scopedStats.scope("recommend").scope("failures")
    }
  
    def recommend(`param`: RecommendParam): Future[RecommendResult] = {
      __stats_recommend.RequestsCounter.incr()
      this.service(encodeRequest("recommend", recommend_args(param))) flatMap { response =>
        val result = decodeResponse(response, recommend_result)
        val exception =
          None
        exception.orElse(result.success.map(Future.value)).getOrElse(Future.exception(missingResult("recommend")))
      } rescue {
        case ex: SourcedException => {
          if (this.serviceName != "") { ex.serviceName = this.serviceName }
          Future.exception(ex)
        }
      } onSuccess { _ =>
        __stats_recommend.SuccessCounter.incr()
      } onFailure { ex =>
        __stats_recommend.FailuresCounter.incr()
        __stats_recommend.FailuresScope.counter(ex.getClass.getName).incr()
      }
    }
    private[this] object __stats_click {
      val RequestsCounter = scopedStats.scope("click").counter("requests")
      val SuccessCounter = scopedStats.scope("click").counter("success")
      val FailuresCounter = scopedStats.scope("click").counter("failures")
      val FailuresScope = scopedStats.scope("click").scope("failures")
    }
  
    def click(`param`: ClickParam): Future[Unit] = {
      __stats_click.RequestsCounter.incr()
      this.service(encodeRequest("click", click_args(param))) flatMap { response =>
        val result = decodeResponse(response, click_result)
        val exception =
          None
        Future.Done
      } rescue {
        case ex: SourcedException => {
          if (this.serviceName != "") { ex.serviceName = this.serviceName }
          Future.exception(ex)
        }
      } onSuccess { _ =>
        __stats_click.SuccessCounter.incr()
      } onFailure { ex =>
        __stats_click.FailuresCounter.incr()
        __stats_click.FailuresScope.counter(ex.getClass.getName).incr()
      }
    }
    private[this] object __stats_recrawl {
      val RequestsCounter = scopedStats.scope("recrawl").counter("requests")
      val SuccessCounter = scopedStats.scope("recrawl").counter("success")
      val FailuresCounter = scopedStats.scope("recrawl").counter("failures")
      val FailuresScope = scopedStats.scope("recrawl").scope("failures")
    }
  
    def recrawl(`url`: String): Future[Unit] = {
      __stats_recrawl.RequestsCounter.incr()
      this.service(encodeRequest("recrawl", recrawl_args(url))) flatMap { response =>
        val result = decodeResponse(response, recrawl_result)
        val exception =
          None
        Future.Done
      } rescue {
        case ex: SourcedException => {
          if (this.serviceName != "") { ex.serviceName = this.serviceName }
          Future.exception(ex)
        }
      } onSuccess { _ =>
        __stats_recrawl.SuccessCounter.incr()
      } onFailure { ex =>
        __stats_recrawl.FailuresCounter.incr()
        __stats_recrawl.FailuresScope.counter(ex.getClass.getName).incr()
      }
    }
    private[this] object __stats_correctImg {
      val RequestsCounter = scopedStats.scope("correctImg").counter("requests")
      val SuccessCounter = scopedStats.scope("correctImg").counter("success")
      val FailuresCounter = scopedStats.scope("correctImg").counter("failures")
      val FailuresScope = scopedStats.scope("correctImg").scope("failures")
    }
  
    def correctImg(`url`: String, `rightImg`: String, `userAgent`: Option[String]): Future[Unit] = {
      __stats_correctImg.RequestsCounter.incr()
      this.service(encodeRequest("correctImg", correctImg_args(url, rightImg, userAgent))) flatMap { response =>
        val result = decodeResponse(response, correctImg_result)
        val exception =
          None
        Future.Done
      } rescue {
        case ex: SourcedException => {
          if (this.serviceName != "") { ex.serviceName = this.serviceName }
          Future.exception(ex)
        }
      } onSuccess { _ =>
        __stats_correctImg.SuccessCounter.incr()
      } onFailure { ex =>
        __stats_correctImg.FailuresCounter.incr()
        __stats_correctImg.FailuresScope.counter(ex.getClass.getName).incr()
      }
    }
  }
  class FinagledService(
    iface: FutureIface,
    protocolFactory: TProtocolFactory
  ) extends FinagleService[Array[Byte], Array[Byte]] {
    // ----- boilerplate that should eventually be moved into finagle:
  
    protected val functionMap = new mutable.HashMap[String, (TProtocol, Int) => Future[Array[Byte]]]()
  
    protected def addFunction(name: String, f: (TProtocol, Int) => Future[Array[Byte]]) {
      functionMap(name) = f
    }
  
    protected def exception(name: String, seqid: Int, code: Int, message: String): Future[Array[Byte]] = {
      try {
        val x = new TApplicationException(code, message)
        val memoryBuffer = new TMemoryBuffer(512)
        val oprot = protocolFactory.getProtocol(memoryBuffer)
  
        oprot.writeMessageBegin(new TMessage(name, TMessageType.EXCEPTION, seqid))
        x.write(oprot)
        oprot.writeMessageEnd()
        oprot.getTransport().flush()
        Future.value(Arrays.copyOfRange(memoryBuffer.getArray(), 0, memoryBuffer.length()))
      } catch {
        case e: Exception => Future.exception(e)
      }
    }
  
    protected def reply(name: String, seqid: Int, result: ThriftStruct): Future[Array[Byte]] = {
      try {
        val memoryBuffer = new TMemoryBuffer(512)
        val oprot = protocolFactory.getProtocol(memoryBuffer)
  
        oprot.writeMessageBegin(new TMessage(name, TMessageType.REPLY, seqid))
        result.write(oprot)
        oprot.writeMessageEnd()
  
        Future.value(Arrays.copyOfRange(memoryBuffer.getArray(), 0, memoryBuffer.length()))
      } catch {
        case e: Exception => Future.exception(e)
      }
    }
  
    final def apply(request: Array[Byte]): Future[Array[Byte]] = {
      val inputTransport = new TMemoryInputTransport(request)
      val iprot = protocolFactory.getProtocol(inputTransport)
  
      try {
        val msg = iprot.readMessageBegin()
        functionMap.get(msg.name) map { _.apply(iprot, msg.seqid) } getOrElse {
          TProtocolUtil.skip(iprot, TType.STRUCT)
          exception(msg.name, msg.seqid, TApplicationException.UNKNOWN_METHOD,
            "Invalid method name: '" + msg.name + "'")
        }
      } catch {
        case e: Exception => Future.exception(e)
      }
    }
  
    // ---- end boilerplate.
  
    addFunction("recommend", { (iprot: TProtocol, seqid: Int) =>
      try {
        val args = recommend_args.decode(iprot)
        iprot.readMessageEnd()
        (try {
          iface.recommend(args.param)
        } catch {
          case e: Exception => Future.exception(e)
        }) flatMap { value: RecommendResult =>
          reply("recommend", seqid, recommend_result(success = Some(value)))
        } rescue {
          case e => Future.exception(e)
        }
      } catch {
        case e: TProtocolException => {
          iprot.readMessageEnd()
          exception("recommend", seqid, TApplicationException.PROTOCOL_ERROR, e.getMessage)
        }
        case e: Exception => Future.exception(e)
      }
    })
    addFunction("click", { (iprot: TProtocol, seqid: Int) =>
      try {
        val args = click_args.decode(iprot)
        iprot.readMessageEnd()
        (try {
          iface.click(args.param)
        } catch {
          case e: Exception => Future.exception(e)
        }) flatMap { value: Unit =>
          reply("click", seqid, click_result())
        } rescue {
          case e => Future.exception(e)
        }
      } catch {
        case e: TProtocolException => {
          iprot.readMessageEnd()
          exception("click", seqid, TApplicationException.PROTOCOL_ERROR, e.getMessage)
        }
        case e: Exception => Future.exception(e)
      }
    })
    addFunction("recrawl", { (iprot: TProtocol, seqid: Int) =>
      try {
        val args = recrawl_args.decode(iprot)
        iprot.readMessageEnd()
        (try {
          iface.recrawl(args.url)
        } catch {
          case e: Exception => Future.exception(e)
        }) flatMap { value: Unit =>
          reply("recrawl", seqid, recrawl_result())
        } rescue {
          case e => Future.exception(e)
        }
      } catch {
        case e: TProtocolException => {
          iprot.readMessageEnd()
          exception("recrawl", seqid, TApplicationException.PROTOCOL_ERROR, e.getMessage)
        }
        case e: Exception => Future.exception(e)
      }
    })
    addFunction("correctImg", { (iprot: TProtocol, seqid: Int) =>
      try {
        val args = correctImg_args.decode(iprot)
        iprot.readMessageEnd()
        (try {
          iface.correctImg(args.url, args.rightImg, args.userAgent)
        } catch {
          case e: Exception => Future.exception(e)
        }) flatMap { value: Unit =>
          reply("correctImg", seqid, correctImg_result())
        } rescue {
          case e => Future.exception(e)
        }
      } catch {
        case e: TProtocolException => {
          iprot.readMessageEnd()
          exception("correctImg", seqid, TApplicationException.PROTOCOL_ERROR, e.getMessage)
        }
        case e: Exception => Future.exception(e)
      }
    })
  }
}