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


object ElasticIndexService {
  trait Iface {
    def searchUrls(`sitePrefixes`: Seq[String] = Seq[String](), `title`: String, `start`: Int, `size`: Int): SearchResult
    def getByUrls(`urls`: Seq[String] = Seq[String]()): Seq[Metadata]
    def matchAll(`sitePrefixes`: Seq[String] = Seq[String](), `start`: Int, `size`: Int): SearchResult
    def deleteIndexes(`urls`: Seq[String] = Seq[String]()): Unit
    def updateMetadata(`metadata`: Metadata): Unit
  }

  trait FutureIface {
    def searchUrls(`sitePrefixes`: Seq[String] = Seq[String](), `title`: String, `start`: Int, `size`: Int): Future[SearchResult]
    def getByUrls(`urls`: Seq[String] = Seq[String]()): Future[Seq[Metadata]]
    def matchAll(`sitePrefixes`: Seq[String] = Seq[String](), `start`: Int, `size`: Int): Future[SearchResult]
    def deleteIndexes(`urls`: Seq[String] = Seq[String]()): Future[Unit]
    def updateMetadata(`metadata`: Metadata): Future[Unit]
  }

  object searchUrls_args extends ThriftStructCodec[searchUrls_args] {
    val Struct = new TStruct("searchUrls_args")
    val SitePrefixesField = new TField("sitePrefixes", TType.LIST, 1)
    val TitleField = new TField("title", TType.STRING, 2)
    val StartField = new TField("start", TType.I32, 3)
    val SizeField = new TField("size", TType.I32, 4)
  
    def encode(_item: searchUrls_args, _oproto: TProtocol) { _item.write(_oproto) }
    def decode(_iprot: TProtocol) = Immutable.decode(_iprot)
  
    def apply(_iprot: TProtocol): searchUrls_args = decode(_iprot)
  
    def apply(
      `sitePrefixes`: Seq[String] = Seq[String](),
      `title`: String,
      `start`: Int,
      `size`: Int
    ): searchUrls_args = new Immutable(
      `sitePrefixes`,
      `title`,
      `start`,
      `size`
    )
  
    def unapply(_item: searchUrls_args): Option[Product4[Seq[String], String, Int, Int]] = Some(_item)
  
    object Immutable extends ThriftStructCodec[searchUrls_args] {
      def encode(_item: searchUrls_args, _oproto: TProtocol) { _item.write(_oproto) }
      def decode(_iprot: TProtocol) = {
        var `sitePrefixes`: Seq[String] = Seq[String]()
        var _got_sitePrefixes = false
        var `title`: String = null
        var _got_title = false
        var `start`: Int = 0
        var _got_start = false
        var `size`: Int = 0
        var _got_size = false
        var _done = false
        _iprot.readStructBegin()
        while (!_done) {
          val _field = _iprot.readFieldBegin()
          if (_field.`type` == TType.STOP) {
            _done = true
          } else {
            _field.id match {
              case 1 => { /* sitePrefixes */
                _field.`type` match {
                  case TType.LIST => {
                    `sitePrefixes` = {
                      val _list = _iprot.readListBegin()
                      val _rv = new mutable.ArrayBuffer[String](_list.size)
                      var _i = 0
                      while (_i < _list.size) {
                        _rv += {
                          _iprot.readString()
                        }
                        _i += 1
                      }
                      _iprot.readListEnd()
                      _rv
                    }
                    _got_sitePrefixes = true
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
              case 3 => { /* start */
                _field.`type` match {
                  case TType.I32 => {
                    `start` = {
                      _iprot.readI32()
                    }
                    _got_start = true
                  }
                  case _ => TProtocolUtil.skip(_iprot, _field.`type`)
                }
              }
              case 4 => { /* size */
                _field.`type` match {
                  case TType.I32 => {
                    `size` = {
                      _iprot.readI32()
                    }
                    _got_size = true
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
        if (!_got_sitePrefixes) throw new TProtocolException("Required field 'sitePrefixes' was not found in serialized data for struct searchUrls_args")
        if (!_got_title) throw new TProtocolException("Required field 'title' was not found in serialized data for struct searchUrls_args")
        if (!_got_start) throw new TProtocolException("Required field 'start' was not found in serialized data for struct searchUrls_args")
        if (!_got_size) throw new TProtocolException("Required field 'size' was not found in serialized data for struct searchUrls_args")
        new Immutable(
          `sitePrefixes`,
          `title`,
          `start`,
          `size`
        )
      }
    }
  
    /**
     * The default read-only implementation of searchUrls_args.  You typically should not need to
     * directly reference this class; instead, use the searchUrls_args.apply method to construct
     * new instances.
     */
    class Immutable(
      val `sitePrefixes`: Seq[String] = Seq[String](),
      val `title`: String,
      val `start`: Int,
      val `size`: Int
    ) extends searchUrls_args
  
  }
  
  trait searchUrls_args extends ThriftStruct
    with Product4[Seq[String], String, Int, Int]
    with java.io.Serializable
  {
    import searchUrls_args._
  
    def `sitePrefixes`: Seq[String]
    def `title`: String
    def `start`: Int
    def `size`: Int
  
    def _1 = `sitePrefixes`
    def _2 = `title`
    def _3 = `start`
    def _4 = `size`
  
    override def write(_oprot: TProtocol) {
      validate()
      _oprot.writeStructBegin(Struct)
      if (true) {
        val `sitePrefixes_item` = `sitePrefixes`
        _oprot.writeFieldBegin(SitePrefixesField)
        _oprot.writeListBegin(new TList(TType.STRING, `sitePrefixes_item`.size))
        `sitePrefixes_item`.foreach { `_sitePrefixes_item_element` =>
          _oprot.writeString(`_sitePrefixes_item_element`)
        }
        _oprot.writeListEnd()
        _oprot.writeFieldEnd()
      }
      if (true) {
        val `title_item` = `title`
        _oprot.writeFieldBegin(TitleField)
        _oprot.writeString(`title_item`)
        _oprot.writeFieldEnd()
      }
      if (true) {
        val `start_item` = `start`
        _oprot.writeFieldBegin(StartField)
        _oprot.writeI32(`start_item`)
        _oprot.writeFieldEnd()
      }
      if (true) {
        val `size_item` = `size`
        _oprot.writeFieldBegin(SizeField)
        _oprot.writeI32(`size_item`)
        _oprot.writeFieldEnd()
      }
      _oprot.writeFieldStop()
      _oprot.writeStructEnd()
    }
  
    def copy(
      `sitePrefixes`: Seq[String] = this.`sitePrefixes`,
      `title`: String = this.`title`,
      `start`: Int = this.`start`,
      `size`: Int = this.`size`
    ): searchUrls_args = new Immutable(
      `sitePrefixes`,
      `title`,
      `start`,
      `size`
    )
  
    /**
     * Checks that all required fields are non-null.
     */
    def validate() {
      if (`sitePrefixes` == null) throw new TProtocolException("Required field 'sitePrefixes' cannot be null")
      if (`title` == null) throw new TProtocolException("Required field 'title' cannot be null")
    }
  
    def canEqual(other: Any) = other.isInstanceOf[searchUrls_args]
  
    override def equals(other: Any): Boolean = runtime.ScalaRunTime._equals(this, other)
  
    override def hashCode: Int = runtime.ScalaRunTime._hashCode(this)
  
    override def toString: String = runtime.ScalaRunTime._toString(this)
  
    override def productArity = 4
  
    override def productElement(n: Int): Any = n match {
      case 0 => `sitePrefixes`
      case 1 => `title`
      case 2 => `start`
      case 3 => `size`
      case _ => throw new IndexOutOfBoundsException(n.toString)
    }
  
    override def productPrefix = "searchUrls_args"
  }
  object searchUrls_result extends ThriftStructCodec[searchUrls_result] {
    val Struct = new TStruct("searchUrls_result")
    val SuccessField = new TField("success", TType.STRUCT, 0)
  
    def encode(_item: searchUrls_result, _oproto: TProtocol) { _item.write(_oproto) }
    def decode(_iprot: TProtocol) = Immutable.decode(_iprot)
  
    def apply(_iprot: TProtocol): searchUrls_result = decode(_iprot)
  
    def apply(
      `success`: Option[SearchResult] = None
    ): searchUrls_result = new Immutable(
      `success`
    )
  
    def unapply(_item: searchUrls_result): Option[Option[SearchResult]] = Some(_item.success)
  
    object Immutable extends ThriftStructCodec[searchUrls_result] {
      def encode(_item: searchUrls_result, _oproto: TProtocol) { _item.write(_oproto) }
      def decode(_iprot: TProtocol) = {
        var `success`: SearchResult = null
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
                      SearchResult.decode(_iprot)
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
     * The default read-only implementation of searchUrls_result.  You typically should not need to
     * directly reference this class; instead, use the searchUrls_result.apply method to construct
     * new instances.
     */
    class Immutable(
      val `success`: Option[SearchResult] = None
    ) extends searchUrls_result
  
  }
  
  trait searchUrls_result extends ThriftStruct
    with Product1[Option[SearchResult]]
    with java.io.Serializable
  {
    import searchUrls_result._
  
    def `success`: Option[SearchResult]
  
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
      `success`: Option[SearchResult] = this.`success`
    ): searchUrls_result = new Immutable(
      `success`
    )
  
    /**
     * Checks that all required fields are non-null.
     */
    def validate() {
    }
  
    def canEqual(other: Any) = other.isInstanceOf[searchUrls_result]
  
    override def equals(other: Any): Boolean = runtime.ScalaRunTime._equals(this, other)
  
    override def hashCode: Int = runtime.ScalaRunTime._hashCode(this)
  
    override def toString: String = runtime.ScalaRunTime._toString(this)
  
    override def productArity = 1
  
    override def productElement(n: Int): Any = n match {
      case 0 => `success`
      case _ => throw new IndexOutOfBoundsException(n.toString)
    }
  
    override def productPrefix = "searchUrls_result"
  }
  object getByUrls_args extends ThriftStructCodec[getByUrls_args] {
    val Struct = new TStruct("getByUrls_args")
    val UrlsField = new TField("urls", TType.LIST, 1)
  
    def encode(_item: getByUrls_args, _oproto: TProtocol) { _item.write(_oproto) }
    def decode(_iprot: TProtocol) = Immutable.decode(_iprot)
  
    def apply(_iprot: TProtocol): getByUrls_args = decode(_iprot)
  
    def apply(
      `urls`: Seq[String] = Seq[String]()
    ): getByUrls_args = new Immutable(
      `urls`
    )
  
    def unapply(_item: getByUrls_args): Option[Seq[String]] = Some(_item.urls)
  
    object Immutable extends ThriftStructCodec[getByUrls_args] {
      def encode(_item: getByUrls_args, _oproto: TProtocol) { _item.write(_oproto) }
      def decode(_iprot: TProtocol) = {
        var `urls`: Seq[String] = Seq[String]()
        var _got_urls = false
        var _done = false
        _iprot.readStructBegin()
        while (!_done) {
          val _field = _iprot.readFieldBegin()
          if (_field.`type` == TType.STOP) {
            _done = true
          } else {
            _field.id match {
              case 1 => { /* urls */
                _field.`type` match {
                  case TType.LIST => {
                    `urls` = {
                      val _list = _iprot.readListBegin()
                      val _rv = new mutable.ArrayBuffer[String](_list.size)
                      var _i = 0
                      while (_i < _list.size) {
                        _rv += {
                          _iprot.readString()
                        }
                        _i += 1
                      }
                      _iprot.readListEnd()
                      _rv
                    }
                    _got_urls = true
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
        if (!_got_urls) throw new TProtocolException("Required field 'urls' was not found in serialized data for struct getByUrls_args")
        new Immutable(
          `urls`
        )
      }
    }
  
    /**
     * The default read-only implementation of getByUrls_args.  You typically should not need to
     * directly reference this class; instead, use the getByUrls_args.apply method to construct
     * new instances.
     */
    class Immutable(
      val `urls`: Seq[String] = Seq[String]()
    ) extends getByUrls_args
  
  }
  
  trait getByUrls_args extends ThriftStruct
    with Product1[Seq[String]]
    with java.io.Serializable
  {
    import getByUrls_args._
  
    def `urls`: Seq[String]
  
    def _1 = `urls`
  
    override def write(_oprot: TProtocol) {
      validate()
      _oprot.writeStructBegin(Struct)
      if (true) {
        val `urls_item` = `urls`
        _oprot.writeFieldBegin(UrlsField)
        _oprot.writeListBegin(new TList(TType.STRING, `urls_item`.size))
        `urls_item`.foreach { `_urls_item_element` =>
          _oprot.writeString(`_urls_item_element`)
        }
        _oprot.writeListEnd()
        _oprot.writeFieldEnd()
      }
      _oprot.writeFieldStop()
      _oprot.writeStructEnd()
    }
  
    def copy(
      `urls`: Seq[String] = this.`urls`
    ): getByUrls_args = new Immutable(
      `urls`
    )
  
    /**
     * Checks that all required fields are non-null.
     */
    def validate() {
      if (`urls` == null) throw new TProtocolException("Required field 'urls' cannot be null")
    }
  
    def canEqual(other: Any) = other.isInstanceOf[getByUrls_args]
  
    override def equals(other: Any): Boolean = runtime.ScalaRunTime._equals(this, other)
  
    override def hashCode: Int = runtime.ScalaRunTime._hashCode(this)
  
    override def toString: String = runtime.ScalaRunTime._toString(this)
  
    override def productArity = 1
  
    override def productElement(n: Int): Any = n match {
      case 0 => `urls`
      case _ => throw new IndexOutOfBoundsException(n.toString)
    }
  
    override def productPrefix = "getByUrls_args"
  }
  object getByUrls_result extends ThriftStructCodec[getByUrls_result] {
    val Struct = new TStruct("getByUrls_result")
    val SuccessField = new TField("success", TType.LIST, 0)
  
    def encode(_item: getByUrls_result, _oproto: TProtocol) { _item.write(_oproto) }
    def decode(_iprot: TProtocol) = Immutable.decode(_iprot)
  
    def apply(_iprot: TProtocol): getByUrls_result = decode(_iprot)
  
    def apply(
      `success`: Option[Seq[Metadata]] = None
    ): getByUrls_result = new Immutable(
      `success`
    )
  
    def unapply(_item: getByUrls_result): Option[Option[Seq[Metadata]]] = Some(_item.success)
  
    object Immutable extends ThriftStructCodec[getByUrls_result] {
      def encode(_item: getByUrls_result, _oproto: TProtocol) { _item.write(_oproto) }
      def decode(_iprot: TProtocol) = {
        var `success`: Seq[Metadata] = Seq[Metadata]()
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
                  case TType.LIST => {
                    `success` = {
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
     * The default read-only implementation of getByUrls_result.  You typically should not need to
     * directly reference this class; instead, use the getByUrls_result.apply method to construct
     * new instances.
     */
    class Immutable(
      val `success`: Option[Seq[Metadata]] = None
    ) extends getByUrls_result
  
  }
  
  trait getByUrls_result extends ThriftStruct
    with Product1[Option[Seq[Metadata]]]
    with java.io.Serializable
  {
    import getByUrls_result._
  
    def `success`: Option[Seq[Metadata]]
  
    def _1 = `success`
  
    override def write(_oprot: TProtocol) {
      validate()
      _oprot.writeStructBegin(Struct)
      if (`success`.isDefined) {
        val `success_item` = `success`.get
        _oprot.writeFieldBegin(SuccessField)
        _oprot.writeListBegin(new TList(TType.STRUCT, `success_item`.size))
        `success_item`.foreach { `_success_item_element` =>
          `_success_item_element`.write(_oprot)
        }
        _oprot.writeListEnd()
        _oprot.writeFieldEnd()
      }
      _oprot.writeFieldStop()
      _oprot.writeStructEnd()
    }
  
    def copy(
      `success`: Option[Seq[Metadata]] = this.`success`
    ): getByUrls_result = new Immutable(
      `success`
    )
  
    /**
     * Checks that all required fields are non-null.
     */
    def validate() {
    }
  
    def canEqual(other: Any) = other.isInstanceOf[getByUrls_result]
  
    override def equals(other: Any): Boolean = runtime.ScalaRunTime._equals(this, other)
  
    override def hashCode: Int = runtime.ScalaRunTime._hashCode(this)
  
    override def toString: String = runtime.ScalaRunTime._toString(this)
  
    override def productArity = 1
  
    override def productElement(n: Int): Any = n match {
      case 0 => `success`
      case _ => throw new IndexOutOfBoundsException(n.toString)
    }
  
    override def productPrefix = "getByUrls_result"
  }
  object matchAll_args extends ThriftStructCodec[matchAll_args] {
    val Struct = new TStruct("matchAll_args")
    val SitePrefixesField = new TField("sitePrefixes", TType.LIST, 1)
    val StartField = new TField("start", TType.I32, 2)
    val SizeField = new TField("size", TType.I32, 3)
  
    def encode(_item: matchAll_args, _oproto: TProtocol) { _item.write(_oproto) }
    def decode(_iprot: TProtocol) = Immutable.decode(_iprot)
  
    def apply(_iprot: TProtocol): matchAll_args = decode(_iprot)
  
    def apply(
      `sitePrefixes`: Seq[String] = Seq[String](),
      `start`: Int,
      `size`: Int
    ): matchAll_args = new Immutable(
      `sitePrefixes`,
      `start`,
      `size`
    )
  
    def unapply(_item: matchAll_args): Option[Product3[Seq[String], Int, Int]] = Some(_item)
  
    object Immutable extends ThriftStructCodec[matchAll_args] {
      def encode(_item: matchAll_args, _oproto: TProtocol) { _item.write(_oproto) }
      def decode(_iprot: TProtocol) = {
        var `sitePrefixes`: Seq[String] = Seq[String]()
        var _got_sitePrefixes = false
        var `start`: Int = 0
        var _got_start = false
        var `size`: Int = 0
        var _got_size = false
        var _done = false
        _iprot.readStructBegin()
        while (!_done) {
          val _field = _iprot.readFieldBegin()
          if (_field.`type` == TType.STOP) {
            _done = true
          } else {
            _field.id match {
              case 1 => { /* sitePrefixes */
                _field.`type` match {
                  case TType.LIST => {
                    `sitePrefixes` = {
                      val _list = _iprot.readListBegin()
                      val _rv = new mutable.ArrayBuffer[String](_list.size)
                      var _i = 0
                      while (_i < _list.size) {
                        _rv += {
                          _iprot.readString()
                        }
                        _i += 1
                      }
                      _iprot.readListEnd()
                      _rv
                    }
                    _got_sitePrefixes = true
                  }
                  case _ => TProtocolUtil.skip(_iprot, _field.`type`)
                }
              }
              case 2 => { /* start */
                _field.`type` match {
                  case TType.I32 => {
                    `start` = {
                      _iprot.readI32()
                    }
                    _got_start = true
                  }
                  case _ => TProtocolUtil.skip(_iprot, _field.`type`)
                }
              }
              case 3 => { /* size */
                _field.`type` match {
                  case TType.I32 => {
                    `size` = {
                      _iprot.readI32()
                    }
                    _got_size = true
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
        if (!_got_sitePrefixes) throw new TProtocolException("Required field 'sitePrefixes' was not found in serialized data for struct matchAll_args")
        if (!_got_start) throw new TProtocolException("Required field 'start' was not found in serialized data for struct matchAll_args")
        if (!_got_size) throw new TProtocolException("Required field 'size' was not found in serialized data for struct matchAll_args")
        new Immutable(
          `sitePrefixes`,
          `start`,
          `size`
        )
      }
    }
  
    /**
     * The default read-only implementation of matchAll_args.  You typically should not need to
     * directly reference this class; instead, use the matchAll_args.apply method to construct
     * new instances.
     */
    class Immutable(
      val `sitePrefixes`: Seq[String] = Seq[String](),
      val `start`: Int,
      val `size`: Int
    ) extends matchAll_args
  
  }
  
  trait matchAll_args extends ThriftStruct
    with Product3[Seq[String], Int, Int]
    with java.io.Serializable
  {
    import matchAll_args._
  
    def `sitePrefixes`: Seq[String]
    def `start`: Int
    def `size`: Int
  
    def _1 = `sitePrefixes`
    def _2 = `start`
    def _3 = `size`
  
    override def write(_oprot: TProtocol) {
      validate()
      _oprot.writeStructBegin(Struct)
      if (true) {
        val `sitePrefixes_item` = `sitePrefixes`
        _oprot.writeFieldBegin(SitePrefixesField)
        _oprot.writeListBegin(new TList(TType.STRING, `sitePrefixes_item`.size))
        `sitePrefixes_item`.foreach { `_sitePrefixes_item_element` =>
          _oprot.writeString(`_sitePrefixes_item_element`)
        }
        _oprot.writeListEnd()
        _oprot.writeFieldEnd()
      }
      if (true) {
        val `start_item` = `start`
        _oprot.writeFieldBegin(StartField)
        _oprot.writeI32(`start_item`)
        _oprot.writeFieldEnd()
      }
      if (true) {
        val `size_item` = `size`
        _oprot.writeFieldBegin(SizeField)
        _oprot.writeI32(`size_item`)
        _oprot.writeFieldEnd()
      }
      _oprot.writeFieldStop()
      _oprot.writeStructEnd()
    }
  
    def copy(
      `sitePrefixes`: Seq[String] = this.`sitePrefixes`,
      `start`: Int = this.`start`,
      `size`: Int = this.`size`
    ): matchAll_args = new Immutable(
      `sitePrefixes`,
      `start`,
      `size`
    )
  
    /**
     * Checks that all required fields are non-null.
     */
    def validate() {
      if (`sitePrefixes` == null) throw new TProtocolException("Required field 'sitePrefixes' cannot be null")
    }
  
    def canEqual(other: Any) = other.isInstanceOf[matchAll_args]
  
    override def equals(other: Any): Boolean = runtime.ScalaRunTime._equals(this, other)
  
    override def hashCode: Int = runtime.ScalaRunTime._hashCode(this)
  
    override def toString: String = runtime.ScalaRunTime._toString(this)
  
    override def productArity = 3
  
    override def productElement(n: Int): Any = n match {
      case 0 => `sitePrefixes`
      case 1 => `start`
      case 2 => `size`
      case _ => throw new IndexOutOfBoundsException(n.toString)
    }
  
    override def productPrefix = "matchAll_args"
  }
  object matchAll_result extends ThriftStructCodec[matchAll_result] {
    val Struct = new TStruct("matchAll_result")
    val SuccessField = new TField("success", TType.STRUCT, 0)
  
    def encode(_item: matchAll_result, _oproto: TProtocol) { _item.write(_oproto) }
    def decode(_iprot: TProtocol) = Immutable.decode(_iprot)
  
    def apply(_iprot: TProtocol): matchAll_result = decode(_iprot)
  
    def apply(
      `success`: Option[SearchResult] = None
    ): matchAll_result = new Immutable(
      `success`
    )
  
    def unapply(_item: matchAll_result): Option[Option[SearchResult]] = Some(_item.success)
  
    object Immutable extends ThriftStructCodec[matchAll_result] {
      def encode(_item: matchAll_result, _oproto: TProtocol) { _item.write(_oproto) }
      def decode(_iprot: TProtocol) = {
        var `success`: SearchResult = null
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
                      SearchResult.decode(_iprot)
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
     * The default read-only implementation of matchAll_result.  You typically should not need to
     * directly reference this class; instead, use the matchAll_result.apply method to construct
     * new instances.
     */
    class Immutable(
      val `success`: Option[SearchResult] = None
    ) extends matchAll_result
  
  }
  
  trait matchAll_result extends ThriftStruct
    with Product1[Option[SearchResult]]
    with java.io.Serializable
  {
    import matchAll_result._
  
    def `success`: Option[SearchResult]
  
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
      `success`: Option[SearchResult] = this.`success`
    ): matchAll_result = new Immutable(
      `success`
    )
  
    /**
     * Checks that all required fields are non-null.
     */
    def validate() {
    }
  
    def canEqual(other: Any) = other.isInstanceOf[matchAll_result]
  
    override def equals(other: Any): Boolean = runtime.ScalaRunTime._equals(this, other)
  
    override def hashCode: Int = runtime.ScalaRunTime._hashCode(this)
  
    override def toString: String = runtime.ScalaRunTime._toString(this)
  
    override def productArity = 1
  
    override def productElement(n: Int): Any = n match {
      case 0 => `success`
      case _ => throw new IndexOutOfBoundsException(n.toString)
    }
  
    override def productPrefix = "matchAll_result"
  }
  object deleteIndexes_args extends ThriftStructCodec[deleteIndexes_args] {
    val Struct = new TStruct("deleteIndexes_args")
    val UrlsField = new TField("urls", TType.LIST, 1)
  
    def encode(_item: deleteIndexes_args, _oproto: TProtocol) { _item.write(_oproto) }
    def decode(_iprot: TProtocol) = Immutable.decode(_iprot)
  
    def apply(_iprot: TProtocol): deleteIndexes_args = decode(_iprot)
  
    def apply(
      `urls`: Seq[String] = Seq[String]()
    ): deleteIndexes_args = new Immutable(
      `urls`
    )
  
    def unapply(_item: deleteIndexes_args): Option[Seq[String]] = Some(_item.urls)
  
    object Immutable extends ThriftStructCodec[deleteIndexes_args] {
      def encode(_item: deleteIndexes_args, _oproto: TProtocol) { _item.write(_oproto) }
      def decode(_iprot: TProtocol) = {
        var `urls`: Seq[String] = Seq[String]()
        var _got_urls = false
        var _done = false
        _iprot.readStructBegin()
        while (!_done) {
          val _field = _iprot.readFieldBegin()
          if (_field.`type` == TType.STOP) {
            _done = true
          } else {
            _field.id match {
              case 1 => { /* urls */
                _field.`type` match {
                  case TType.LIST => {
                    `urls` = {
                      val _list = _iprot.readListBegin()
                      val _rv = new mutable.ArrayBuffer[String](_list.size)
                      var _i = 0
                      while (_i < _list.size) {
                        _rv += {
                          _iprot.readString()
                        }
                        _i += 1
                      }
                      _iprot.readListEnd()
                      _rv
                    }
                    _got_urls = true
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
        if (!_got_urls) throw new TProtocolException("Required field 'urls' was not found in serialized data for struct deleteIndexes_args")
        new Immutable(
          `urls`
        )
      }
    }
  
    /**
     * The default read-only implementation of deleteIndexes_args.  You typically should not need to
     * directly reference this class; instead, use the deleteIndexes_args.apply method to construct
     * new instances.
     */
    class Immutable(
      val `urls`: Seq[String] = Seq[String]()
    ) extends deleteIndexes_args
  
  }
  
  trait deleteIndexes_args extends ThriftStruct
    with Product1[Seq[String]]
    with java.io.Serializable
  {
    import deleteIndexes_args._
  
    def `urls`: Seq[String]
  
    def _1 = `urls`
  
    override def write(_oprot: TProtocol) {
      validate()
      _oprot.writeStructBegin(Struct)
      if (true) {
        val `urls_item` = `urls`
        _oprot.writeFieldBegin(UrlsField)
        _oprot.writeListBegin(new TList(TType.STRING, `urls_item`.size))
        `urls_item`.foreach { `_urls_item_element` =>
          _oprot.writeString(`_urls_item_element`)
        }
        _oprot.writeListEnd()
        _oprot.writeFieldEnd()
      }
      _oprot.writeFieldStop()
      _oprot.writeStructEnd()
    }
  
    def copy(
      `urls`: Seq[String] = this.`urls`
    ): deleteIndexes_args = new Immutable(
      `urls`
    )
  
    /**
     * Checks that all required fields are non-null.
     */
    def validate() {
      if (`urls` == null) throw new TProtocolException("Required field 'urls' cannot be null")
    }
  
    def canEqual(other: Any) = other.isInstanceOf[deleteIndexes_args]
  
    override def equals(other: Any): Boolean = runtime.ScalaRunTime._equals(this, other)
  
    override def hashCode: Int = runtime.ScalaRunTime._hashCode(this)
  
    override def toString: String = runtime.ScalaRunTime._toString(this)
  
    override def productArity = 1
  
    override def productElement(n: Int): Any = n match {
      case 0 => `urls`
      case _ => throw new IndexOutOfBoundsException(n.toString)
    }
  
    override def productPrefix = "deleteIndexes_args"
  }
  object deleteIndexes_result extends ThriftStructCodec[deleteIndexes_result] {
    val Struct = new TStruct("deleteIndexes_result")
  
    def encode(_item: deleteIndexes_result, _oproto: TProtocol) { _item.write(_oproto) }
    def decode(_iprot: TProtocol) = Immutable.decode(_iprot)
  
    def apply(_iprot: TProtocol): deleteIndexes_result = decode(_iprot)
  
    def apply(
    ): deleteIndexes_result = new Immutable(
    )
  
    def unapply(_item: deleteIndexes_result): Boolean = true
  
    object Immutable extends ThriftStructCodec[deleteIndexes_result] {
      def encode(_item: deleteIndexes_result, _oproto: TProtocol) { _item.write(_oproto) }
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
     * The default read-only implementation of deleteIndexes_result.  You typically should not need to
     * directly reference this class; instead, use the deleteIndexes_result.apply method to construct
     * new instances.
     */
    class Immutable(
    ) extends deleteIndexes_result
  
  }
  
  trait deleteIndexes_result extends ThriftStruct
    with Product
    with java.io.Serializable
  {
    import deleteIndexes_result._
  
  
  
    override def write(_oprot: TProtocol) {
      validate()
      _oprot.writeStructBegin(Struct)
      _oprot.writeFieldStop()
      _oprot.writeStructEnd()
    }
  
    def copy(
    ): deleteIndexes_result = new Immutable(
    )
  
    /**
     * Checks that all required fields are non-null.
     */
    def validate() {
    }
  
    def canEqual(other: Any) = other.isInstanceOf[deleteIndexes_result]
  
    override def equals(other: Any): Boolean = runtime.ScalaRunTime._equals(this, other)
  
    override def hashCode: Int = runtime.ScalaRunTime._hashCode(this)
  
    override def toString: String = runtime.ScalaRunTime._toString(this)
  
    override def productArity = 0
  
    override def productElement(n: Int): Any = n match {
      case _ => throw new IndexOutOfBoundsException(n.toString)
    }
  
    override def productPrefix = "deleteIndexes_result"
  }
  object updateMetadata_args extends ThriftStructCodec[updateMetadata_args] {
    val Struct = new TStruct("updateMetadata_args")
    val MetadataField = new TField("metadata", TType.STRUCT, 1)
  
    def encode(_item: updateMetadata_args, _oproto: TProtocol) { _item.write(_oproto) }
    def decode(_iprot: TProtocol) = Immutable.decode(_iprot)
  
    def apply(_iprot: TProtocol): updateMetadata_args = decode(_iprot)
  
    def apply(
      `metadata`: Metadata
    ): updateMetadata_args = new Immutable(
      `metadata`
    )
  
    def unapply(_item: updateMetadata_args): Option[Metadata] = Some(_item.metadata)
  
    object Immutable extends ThriftStructCodec[updateMetadata_args] {
      def encode(_item: updateMetadata_args, _oproto: TProtocol) { _item.write(_oproto) }
      def decode(_iprot: TProtocol) = {
        var `metadata`: Metadata = null
        var _got_metadata = false
        var _done = false
        _iprot.readStructBegin()
        while (!_done) {
          val _field = _iprot.readFieldBegin()
          if (_field.`type` == TType.STOP) {
            _done = true
          } else {
            _field.id match {
              case 1 => { /* metadata */
                _field.`type` match {
                  case TType.STRUCT => {
                    `metadata` = {
                      Metadata.decode(_iprot)
                    }
                    _got_metadata = true
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
        if (!_got_metadata) throw new TProtocolException("Required field 'metadata' was not found in serialized data for struct updateMetadata_args")
        new Immutable(
          `metadata`
        )
      }
    }
  
    /**
     * The default read-only implementation of updateMetadata_args.  You typically should not need to
     * directly reference this class; instead, use the updateMetadata_args.apply method to construct
     * new instances.
     */
    class Immutable(
      val `metadata`: Metadata
    ) extends updateMetadata_args
  
  }
  
  trait updateMetadata_args extends ThriftStruct
    with Product1[Metadata]
    with java.io.Serializable
  {
    import updateMetadata_args._
  
    def `metadata`: Metadata
  
    def _1 = `metadata`
  
    override def write(_oprot: TProtocol) {
      validate()
      _oprot.writeStructBegin(Struct)
      if (true) {
        val `metadata_item` = `metadata`
        _oprot.writeFieldBegin(MetadataField)
        `metadata_item`.write(_oprot)
        _oprot.writeFieldEnd()
      }
      _oprot.writeFieldStop()
      _oprot.writeStructEnd()
    }
  
    def copy(
      `metadata`: Metadata = this.`metadata`
    ): updateMetadata_args = new Immutable(
      `metadata`
    )
  
    /**
     * Checks that all required fields are non-null.
     */
    def validate() {
      if (`metadata` == null) throw new TProtocolException("Required field 'metadata' cannot be null")
    }
  
    def canEqual(other: Any) = other.isInstanceOf[updateMetadata_args]
  
    override def equals(other: Any): Boolean = runtime.ScalaRunTime._equals(this, other)
  
    override def hashCode: Int = runtime.ScalaRunTime._hashCode(this)
  
    override def toString: String = runtime.ScalaRunTime._toString(this)
  
    override def productArity = 1
  
    override def productElement(n: Int): Any = n match {
      case 0 => `metadata`
      case _ => throw new IndexOutOfBoundsException(n.toString)
    }
  
    override def productPrefix = "updateMetadata_args"
  }
  object updateMetadata_result extends ThriftStructCodec[updateMetadata_result] {
    val Struct = new TStruct("updateMetadata_result")
  
    def encode(_item: updateMetadata_result, _oproto: TProtocol) { _item.write(_oproto) }
    def decode(_iprot: TProtocol) = Immutable.decode(_iprot)
  
    def apply(_iprot: TProtocol): updateMetadata_result = decode(_iprot)
  
    def apply(
    ): updateMetadata_result = new Immutable(
    )
  
    def unapply(_item: updateMetadata_result): Boolean = true
  
    object Immutable extends ThriftStructCodec[updateMetadata_result] {
      def encode(_item: updateMetadata_result, _oproto: TProtocol) { _item.write(_oproto) }
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
     * The default read-only implementation of updateMetadata_result.  You typically should not need to
     * directly reference this class; instead, use the updateMetadata_result.apply method to construct
     * new instances.
     */
    class Immutable(
    ) extends updateMetadata_result
  
  }
  
  trait updateMetadata_result extends ThriftStruct
    with Product
    with java.io.Serializable
  {
    import updateMetadata_result._
  
  
  
    override def write(_oprot: TProtocol) {
      validate()
      _oprot.writeStructBegin(Struct)
      _oprot.writeFieldStop()
      _oprot.writeStructEnd()
    }
  
    def copy(
    ): updateMetadata_result = new Immutable(
    )
  
    /**
     * Checks that all required fields are non-null.
     */
    def validate() {
    }
  
    def canEqual(other: Any) = other.isInstanceOf[updateMetadata_result]
  
    override def equals(other: Any): Boolean = runtime.ScalaRunTime._equals(this, other)
  
    override def hashCode: Int = runtime.ScalaRunTime._hashCode(this)
  
    override def toString: String = runtime.ScalaRunTime._toString(this)
  
    override def productArity = 0
  
    override def productElement(n: Int): Any = n match {
      case _ => throw new IndexOutOfBoundsException(n.toString)
    }
  
    override def productPrefix = "updateMetadata_result"
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
    private[this] object __stats_searchUrls {
      val RequestsCounter = scopedStats.scope("searchUrls").counter("requests")
      val SuccessCounter = scopedStats.scope("searchUrls").counter("success")
      val FailuresCounter = scopedStats.scope("searchUrls").counter("failures")
      val FailuresScope = scopedStats.scope("searchUrls").scope("failures")
    }
  
    def searchUrls(`sitePrefixes`: Seq[String] = Seq[String](), `title`: String, `start`: Int, `size`: Int): Future[SearchResult] = {
      __stats_searchUrls.RequestsCounter.incr()
      this.service(encodeRequest("searchUrls", searchUrls_args(sitePrefixes, title, start, size))) flatMap { response =>
        val result = decodeResponse(response, searchUrls_result)
        val exception =
          None
        exception.orElse(result.success.map(Future.value)).getOrElse(Future.exception(missingResult("searchUrls")))
      } rescue {
        case ex: SourcedException => {
          if (this.serviceName != "") { ex.serviceName = this.serviceName }
          Future.exception(ex)
        }
      } onSuccess { _ =>
        __stats_searchUrls.SuccessCounter.incr()
      } onFailure { ex =>
        __stats_searchUrls.FailuresCounter.incr()
        __stats_searchUrls.FailuresScope.counter(ex.getClass.getName).incr()
      }
    }
    private[this] object __stats_getByUrls {
      val RequestsCounter = scopedStats.scope("getByUrls").counter("requests")
      val SuccessCounter = scopedStats.scope("getByUrls").counter("success")
      val FailuresCounter = scopedStats.scope("getByUrls").counter("failures")
      val FailuresScope = scopedStats.scope("getByUrls").scope("failures")
    }
  
    def getByUrls(`urls`: Seq[String] = Seq[String]()): Future[Seq[Metadata]] = {
      __stats_getByUrls.RequestsCounter.incr()
      this.service(encodeRequest("getByUrls", getByUrls_args(urls))) flatMap { response =>
        val result = decodeResponse(response, getByUrls_result)
        val exception =
          None
        exception.orElse(result.success.map(Future.value)).getOrElse(Future.exception(missingResult("getByUrls")))
      } rescue {
        case ex: SourcedException => {
          if (this.serviceName != "") { ex.serviceName = this.serviceName }
          Future.exception(ex)
        }
      } onSuccess { _ =>
        __stats_getByUrls.SuccessCounter.incr()
      } onFailure { ex =>
        __stats_getByUrls.FailuresCounter.incr()
        __stats_getByUrls.FailuresScope.counter(ex.getClass.getName).incr()
      }
    }
    private[this] object __stats_matchAll {
      val RequestsCounter = scopedStats.scope("matchAll").counter("requests")
      val SuccessCounter = scopedStats.scope("matchAll").counter("success")
      val FailuresCounter = scopedStats.scope("matchAll").counter("failures")
      val FailuresScope = scopedStats.scope("matchAll").scope("failures")
    }
  
    def matchAll(`sitePrefixes`: Seq[String] = Seq[String](), `start`: Int, `size`: Int): Future[SearchResult] = {
      __stats_matchAll.RequestsCounter.incr()
      this.service(encodeRequest("matchAll", matchAll_args(sitePrefixes, start, size))) flatMap { response =>
        val result = decodeResponse(response, matchAll_result)
        val exception =
          None
        exception.orElse(result.success.map(Future.value)).getOrElse(Future.exception(missingResult("matchAll")))
      } rescue {
        case ex: SourcedException => {
          if (this.serviceName != "") { ex.serviceName = this.serviceName }
          Future.exception(ex)
        }
      } onSuccess { _ =>
        __stats_matchAll.SuccessCounter.incr()
      } onFailure { ex =>
        __stats_matchAll.FailuresCounter.incr()
        __stats_matchAll.FailuresScope.counter(ex.getClass.getName).incr()
      }
    }
    private[this] object __stats_deleteIndexes {
      val RequestsCounter = scopedStats.scope("deleteIndexes").counter("requests")
      val SuccessCounter = scopedStats.scope("deleteIndexes").counter("success")
      val FailuresCounter = scopedStats.scope("deleteIndexes").counter("failures")
      val FailuresScope = scopedStats.scope("deleteIndexes").scope("failures")
    }
  
    def deleteIndexes(`urls`: Seq[String] = Seq[String]()): Future[Unit] = {
      __stats_deleteIndexes.RequestsCounter.incr()
      this.service(encodeRequest("deleteIndexes", deleteIndexes_args(urls))) flatMap { response =>
        val result = decodeResponse(response, deleteIndexes_result)
        val exception =
          None
        Future.Done
      } rescue {
        case ex: SourcedException => {
          if (this.serviceName != "") { ex.serviceName = this.serviceName }
          Future.exception(ex)
        }
      } onSuccess { _ =>
        __stats_deleteIndexes.SuccessCounter.incr()
      } onFailure { ex =>
        __stats_deleteIndexes.FailuresCounter.incr()
        __stats_deleteIndexes.FailuresScope.counter(ex.getClass.getName).incr()
      }
    }
    private[this] object __stats_updateMetadata {
      val RequestsCounter = scopedStats.scope("updateMetadata").counter("requests")
      val SuccessCounter = scopedStats.scope("updateMetadata").counter("success")
      val FailuresCounter = scopedStats.scope("updateMetadata").counter("failures")
      val FailuresScope = scopedStats.scope("updateMetadata").scope("failures")
    }
  
    def updateMetadata(`metadata`: Metadata): Future[Unit] = {
      __stats_updateMetadata.RequestsCounter.incr()
      this.service(encodeRequest("updateMetadata", updateMetadata_args(metadata))) flatMap { response =>
        val result = decodeResponse(response, updateMetadata_result)
        val exception =
          None
        Future.Done
      } rescue {
        case ex: SourcedException => {
          if (this.serviceName != "") { ex.serviceName = this.serviceName }
          Future.exception(ex)
        }
      } onSuccess { _ =>
        __stats_updateMetadata.SuccessCounter.incr()
      } onFailure { ex =>
        __stats_updateMetadata.FailuresCounter.incr()
        __stats_updateMetadata.FailuresScope.counter(ex.getClass.getName).incr()
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
  
    addFunction("searchUrls", { (iprot: TProtocol, seqid: Int) =>
      try {
        val args = searchUrls_args.decode(iprot)
        iprot.readMessageEnd()
        (try {
          iface.searchUrls(args.sitePrefixes, args.title, args.start, args.size)
        } catch {
          case e: Exception => Future.exception(e)
        }) flatMap { value: SearchResult =>
          reply("searchUrls", seqid, searchUrls_result(success = Some(value)))
        } rescue {
          case e => Future.exception(e)
        }
      } catch {
        case e: TProtocolException => {
          iprot.readMessageEnd()
          exception("searchUrls", seqid, TApplicationException.PROTOCOL_ERROR, e.getMessage)
        }
        case e: Exception => Future.exception(e)
      }
    })
    addFunction("getByUrls", { (iprot: TProtocol, seqid: Int) =>
      try {
        val args = getByUrls_args.decode(iprot)
        iprot.readMessageEnd()
        (try {
          iface.getByUrls(args.urls)
        } catch {
          case e: Exception => Future.exception(e)
        }) flatMap { value: Seq[Metadata] =>
          reply("getByUrls", seqid, getByUrls_result(success = Some(value)))
        } rescue {
          case e => Future.exception(e)
        }
      } catch {
        case e: TProtocolException => {
          iprot.readMessageEnd()
          exception("getByUrls", seqid, TApplicationException.PROTOCOL_ERROR, e.getMessage)
        }
        case e: Exception => Future.exception(e)
      }
    })
    addFunction("matchAll", { (iprot: TProtocol, seqid: Int) =>
      try {
        val args = matchAll_args.decode(iprot)
        iprot.readMessageEnd()
        (try {
          iface.matchAll(args.sitePrefixes, args.start, args.size)
        } catch {
          case e: Exception => Future.exception(e)
        }) flatMap { value: SearchResult =>
          reply("matchAll", seqid, matchAll_result(success = Some(value)))
        } rescue {
          case e => Future.exception(e)
        }
      } catch {
        case e: TProtocolException => {
          iprot.readMessageEnd()
          exception("matchAll", seqid, TApplicationException.PROTOCOL_ERROR, e.getMessage)
        }
        case e: Exception => Future.exception(e)
      }
    })
    addFunction("deleteIndexes", { (iprot: TProtocol, seqid: Int) =>
      try {
        val args = deleteIndexes_args.decode(iprot)
        iprot.readMessageEnd()
        (try {
          iface.deleteIndexes(args.urls)
        } catch {
          case e: Exception => Future.exception(e)
        }) flatMap { value: Unit =>
          reply("deleteIndexes", seqid, deleteIndexes_result())
        } rescue {
          case e => Future.exception(e)
        }
      } catch {
        case e: TProtocolException => {
          iprot.readMessageEnd()
          exception("deleteIndexes", seqid, TApplicationException.PROTOCOL_ERROR, e.getMessage)
        }
        case e: Exception => Future.exception(e)
      }
    })
    addFunction("updateMetadata", { (iprot: TProtocol, seqid: Int) =>
      try {
        val args = updateMetadata_args.decode(iprot)
        iprot.readMessageEnd()
        (try {
          iface.updateMetadata(args.metadata)
        } catch {
          case e: Exception => Future.exception(e)
        }) flatMap { value: Unit =>
          reply("updateMetadata", seqid, updateMetadata_result())
        } rescue {
          case e => Future.exception(e)
        }
      } catch {
        case e: TProtocolException => {
          iprot.readMessageEnd()
          exception("updateMetadata", seqid, TApplicationException.PROTOCOL_ERROR, e.getMessage)
        }
        case e: Exception => Future.exception(e)
      }
    })
  }
}