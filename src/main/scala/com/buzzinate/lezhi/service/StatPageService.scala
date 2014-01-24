package com.buzzinate.lezhi.service

import java.util.Date
import scala.Array.fallbackCanBuildFrom
import scala.Option.option2Iterable
import scala.collection.JavaConverters.{ asScalaBufferConverter, seqAsJavaListConverter }
import org.apache.hadoop.hbase.KeyValue
import org.apache.hadoop.hbase.client.{ Get, HTableInterface, HTablePool }
import org.apache.hadoop.hbase.util.Bytes
import com.buzzinate.common.util.DateTimeUtil
import com.buzzinate.lezhi.db.HBaseClient.{ STAT_PAGE_TABLE, STAT_COLUMN_FAMILY }
import com.buzzinate.lezhi.model.StatPage.StatPage
import com.buzzinate.lezhi.util.{ Logging, Util }
import scala.collection.mutable.ListBuffer
import org.apache.hadoop.hbase.client.Put

/**
 * @author jeffrey.ji <jeffrey.ji@buzzinate.com>
 * Aug 5, 2013 5:23:50 PM
 *
 */
object StatPageService extends Logging {
    val cfBytes = Bytes.toBytes(STAT_COLUMN_FAMILY)

    val COLUMN_UUID = "uuid"
    val COLUMN_IN_CLICKS = "inClicks"
    val COLUMN_OUT_CLICKS = "outClicks"
    val COLUMN_SHOWUPS = "showUps"

    private def use[T](f: HTableInterface => T): Option[T] = {
        //val statPage = htablePool.getTable(STAT_PAGE_TABLE)
        try {
            //Some(f(statPage))
            None
        } catch {
            case e => {
                //TODO: 异常时，怎样返回
                log.error(e, "error happened in interacting with hbase")
                None
            }
        } finally {
            //statPage.close
        }
    }

    /**
     * key is in the format: uuid:yyyy-mm-dd
     */
    def getStatPage(urlUuid: String, pageDay: Date): Option[StatPage] = {
        val key = urlUuid + ":" + DateTimeUtil.formatDate(pageDay)
        val get = new Get(Bytes.toBytes(key))
        get.addFamily(cfBytes)
        get.addColumn(cfBytes, Bytes.toBytes(COLUMN_UUID))
        get.addColumn(cfBytes, Bytes.toBytes(COLUMN_IN_CLICKS))
        get.addColumn(cfBytes, Bytes.toBytes(COLUMN_OUT_CLICKS))
        get.addColumn(cfBytes, Bytes.toBytes(COLUMN_SHOWUPS))

        val result = use { htable => htable.get(get) }
        if (result.isDefined && result.get.list() != null) {
            val statPage = StatPage(0, Util.uuidToByte(urlUuid))
            result.get.list.asScala.foreach { kv: KeyValue =>
                val (col, value) = (Bytes.toString(kv.getQualifier()), Bytes.toString(kv.getValue()))
                col match {
                    case COLUMN_UUID => statPage.uuid = Util.uuidToByte(value)
                    case COLUMN_IN_CLICKS => statPage.inClicks = value.toInt
                    case COLUMN_OUT_CLICKS => statPage.outClicks = value.toInt
                    case COLUMN_SHOWUPS => statPage.showUps = value.toInt
                }
            }
            Some(statPage)
        } else
            None
    }

    /**
     * batch get stat_pages
     */
    def getStatPages(keys: List[(String, Date)]): Seq[StatPage] = {
        val gets = keys map {
            case (urlUuid, pageDay) =>
                val key = urlUuid + ":" + DateTimeUtil.formatDate(pageDay)
                val get = new Get(Bytes.toBytes(key))
                get.addFamily(cfBytes)
                get.addColumn(cfBytes, Bytes.toBytes(COLUMN_UUID))
                get.addColumn(cfBytes, Bytes.toBytes(COLUMN_IN_CLICKS))
                get.addColumn(cfBytes, Bytes.toBytes(COLUMN_OUT_CLICKS))
                get.addColumn(cfBytes, Bytes.toBytes(COLUMN_SHOWUPS))
                get
        }
        val results = use { htable => htable.get(gets.toList.asJava) }
        if (results.isDefined) {
            val res = new ListBuffer[StatPage]()
            results.get.foreach { result =>
                if (result.list != null) {
                    val key = Bytes.toString(result.getRow())
                    val statPage = StatPage(0, Util.uuidToByte(key.substring(0, key.lastIndexOf(":"))))
                    result.list.asScala.foreach { kv: KeyValue =>
                        val (col, value) = (Bytes.toString(kv.getQualifier()), Bytes.toString(kv.getValue()))
                        col match {
                            case COLUMN_UUID => statPage.uuid = Util.uuidToByte(value)
                            case COLUMN_IN_CLICKS => statPage.inClicks = value.toInt
                            case COLUMN_OUT_CLICKS => statPage.outClicks = value.toInt
                            case COLUMN_SHOWUPS => statPage.showUps = value.toInt
                        }
                    }
                    res += statPage
                }
            }
            res.toSeq
        } else
            Nil
    }

    /**
     * batch update stat_page into hbase
     */
    def updateStatPages(statPages: Seq[StatPage]): Unit = {
        val putList = statPages.map {
            case statPage =>
                val put = new Put(Bytes.toBytes(Util.byteToUuid(statPage.urlHash) + ":" + DateTimeUtil.formatDate(new Date(statPage.pageDay.getTime))))
                put.add(cfBytes, Bytes.toBytes(COLUMN_UUID), statPage.uuid)
                put.add(cfBytes, Bytes.toBytes(COLUMN_IN_CLICKS), Bytes.toBytes(statPage.inClicks))
                put.add(cfBytes, Bytes.toBytes(COLUMN_OUT_CLICKS), Bytes.toBytes(statPage.outClicks))
                put.add(cfBytes, Bytes.toBytes(COLUMN_SHOWUPS), Bytes.toBytes(statPage.showUps))
                put
        }.toList
        use { htable => htable.put(putList.asJava) }
    }

    /**
     * update stat_page into hbase
     */
    def updateStatPage(statPage: StatPage): Unit = {
        val put = new Put(Bytes.toBytes(Util.byteToUuid(statPage.urlHash) + ":" + DateTimeUtil.formatDate(new Date(statPage.pageDay.getTime))))
        put.add(cfBytes, Bytes.toBytes(COLUMN_UUID), statPage.uuid)
        put.add(cfBytes, Bytes.toBytes(COLUMN_IN_CLICKS), Bytes.toBytes(statPage.inClicks))
        put.add(cfBytes, Bytes.toBytes(COLUMN_OUT_CLICKS), Bytes.toBytes(statPage.outClicks))
        put.add(cfBytes, Bytes.toBytes(COLUMN_SHOWUPS), Bytes.toBytes(statPage.showUps))

        use { htable => htable.put(put) }
    }
}