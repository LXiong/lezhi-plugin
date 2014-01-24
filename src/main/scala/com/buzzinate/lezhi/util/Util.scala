package com.buzzinate.lezhi.util

import java.nio.ByteBuffer
import java.security.MessageDigest
import java.util.{ UUID => JavaUUID }
import org.apache.commons.lang.StringUtils
import com.buzzinate.extract.HtmlExtractor
import com.eaio.uuid.{ UUID => EaioUUID }

object Util {

    /**
     * Hash the given string and return a byte array
     */
    def hash(s: String): Array[Byte] = {
        try {
            val md = MessageDigest.getInstance("MD5")
            md.digest(s.getBytes("UTF-8"))
        } catch {
            case _: Exception => new Array[Byte](0)
        }
    }

    /**
     * Convert to url to ByteBuffer
     */
    def urlToByte(url: String) = {
        val urlUuid = LezhiUUID(hash(url))
        UUIDSerializer.toByteBuffer(urlUuid)
    }

    /**
     * Convert url to Array[Byte]
     */
    def urlToBytes(url: String): Array[Byte] = {
        val urlUuid = urlToUuid(url)
        uuidToByte(urlUuid)
    }

    /**
     * Convert to url to String
     */
    def urlToUuid(url: String): String = {
        LezhiUUID(hash(url)).toString
    }

    /**
     * Convert byte array to uuid string
     */
    def byteToUuid(data: Array[Byte]): String = {
        assert(data.length == 16)
        var msb = 0L
        var lsb = 0L

        (0 until 8).foreach { (i) => msb = (msb << 8) | (data(i) & 0xff) }
        (8 until 16).foreach { (i) => lsb = (lsb << 8) | (data(i) & 0xff) }

        new EaioUUID(msb, lsb).toString
    }

    /**
     * Convert uuid string to byte array
     */
    def uuidToByte(uuid: String) = {
        try {
            assert(uuid.length == 36)

            val u = JavaUUID.fromString(uuid)
            val msb = u.getMostSignificantBits()
            val lsb = u.getLeastSignificantBits()
            val buffer = new Array[Byte](16)

            (0 until 8).foreach { (i) => buffer(i) = (msb >>> 8 * (7 - i)).asInstanceOf[Byte] }
            (8 until 16).foreach { (i) => buffer(i) = (lsb >>> 8 * (7 - i)).asInstanceOf[Byte] }
            buffer
        } catch {
            case _: Error => new Array[Byte](0)
        }
    }

    /**
     * Convert latin string to utf-8 encoding
     * @param source
     * @return source if can't convert chars from latin to utf-8
     */
    def convertLatin2Utf8(source: String): String = {
        if (StringUtils.isEmpty(source)) {
            return ""
        }
        try {
            val sourceBytes = source.getBytes("iso-8859-1")
            new String(sourceBytes, "utf-8")
        } catch {
            case _: Exception => source
        }
    }

    /**
     * Get an empty bytebuffer object
     */
    def getEmptyByte() = {
        ByteBuffer.wrap(new Array[Byte](0))
    }

    /**
     * get the top max pics of an article
     */
    def getImgsList(url: String, max: Int): List[(String, String)] = {
        try {
            val imgs = HtmlExtractor.extractAllImgs(url)
            var sortedImgs = imgs.sortBy(-_._3) map { img => (img._1, img._2) }
            if (sortedImgs.size > max) sortedImgs = sortedImgs.slice(0, max - 1)
            sortedImgs
        } catch {
            case ex: Exception => Nil
        }
    }

    def getStringFromList(params: Seq[String]): String = {
        var res = ""
        params.foreach { param =>
            res += param
            res += ","
        }
        if (res.length() > 0)
            res.substring(0, res.lastIndexOf(","))
        else
            ""
    }

    // calculate paginations and get page of current page
    def getPagination[E](dataPages: Seq[E], page: Int, size: Int): Seq[E] = {
        val totalSize = dataPages.size
        val fromIndex = Math.min(page * size, totalSize)
        val toIndex = Math.min((page + 1) * size, totalSize)
        val pages = dataPages.slice(fromIndex, toIndex)
        pages
    }
}