package com.buzzinate.lezhi.util

import java.nio.ByteBuffer

/**
 * Created with IntelliJ IDEA.
 * User: jeffrey
 * Date: 8/8/13
 * Time: 4:57 PM
 * To change this template use File | Settings | File Templates.
 */
object UUIDSerializer {
    import com.buzzinate.lezhi.util.{ LezhiUUID => UUIDUtils }
    import java.util.UUID

    def fromByteBuffer(bytes: ByteBuffer) = UUIDUtils(bytes)

    def toString(uuid: UUID) = uuid.toString

    def fromString(str: String) = UUID.fromString(str)

    def toByteBuffer(uuid: UUID) = {
        val msb = uuid.getMostSignificantBits()
        val lsb = uuid.getLeastSignificantBits()
        val buffer = new Array[Byte](16)

        (0 until 8).foreach {
            (i) => buffer(i) = (msb >>> 8 * (7 - i)).asInstanceOf[Byte]
        }
        (8 until 16).foreach {
            (i) => buffer(i) = (lsb >>> 8 * (7 - i)).asInstanceOf[Byte]
        }

        ByteBuffer.wrap(buffer)
    }

}

object LezhiUUID {
    import java.util.{ UUID => JavaUUID }
    import _root_.com.eaio.uuid.{ UUID => EaioUUID }

    /**
     * returns a new uuid, can be used as a time uuid
     */
    def apply() = JavaUUID.fromString(new EaioUUID().toString());

    def apply(data: ByteBuffer): JavaUUID = {
        //first get the uuid 16 bytes from ByteBuffer
        val uuid = new Array[Byte](data.remaining())
        //reset the pos, then can process this another time
        val pos = data.position()
        data.get(uuid)
        data.position(pos)
        apply(uuid.array)
    }
    /**
     * returns a new uuid based on the specified string
     */
    def apply(data: Array[Byte]): JavaUUID = {
        var msb = 0L
        var lsb = 0L
        assert(data.length == 16)

        (0 until 8).foreach { (i) => msb = (msb << 8) | (data(i) & 0xff) }
        (8 until 16).foreach { (i) => lsb = (lsb << 8) | (data(i) & 0xff) }

        JavaUUID.fromString(new EaioUUID(msb, lsb).toString)
    }
}
