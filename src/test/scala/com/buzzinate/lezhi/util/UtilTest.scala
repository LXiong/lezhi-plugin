package com.buzzinate.lezhi.util

/**
 * @author jeffrey.ji <jeffrey.ji@buzzinate.com>
 * Aug 2, 2013 1:25:33 PM
 *
 */
class UtilTest {

    ("test util class")
    def testUtil() {
        val url = "http://qa.buzzinate.com/news/news/%E9%92%93%E9%B1%BC%E5%B2%9B%E4%BD%95%E9%9C%80%E7%BF%BB%E9%BB%84%E5%8E%86.html#blz-insite"
        val urlUuid = Util.urlToUuid(url)
        val hash = Util.byteToUuid(Util.urlToBytes(url))

        val hash2 = Util.byteToUuid(Util.uuidToByte(urlUuid))
        assert(urlUuid == hash)
        assert(urlUuid == hash2)
    }
}