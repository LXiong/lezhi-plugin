package com.buzzinate.lezhi.util

import com.buzzinate.lezhi.Config
import javax.servlet.http.HttpServletRequest
import org.apache.commons.codec.binary.Base64
import unfiltered.request.HttpRequest
import unfiltered.Cookie

object LoginUtil {

    val COOKIE_EXPIRE_REMEMBER = 2147483647
    val COOKIE_EXPIRE_NORMAL = 1800

    val COOKIE_KEY = Config.getString("bshare.security.cookie.key")
    val COOKIE_NAME = Config.getString("bshare.security.cookie.name")
    val COOKIE_DOMAIN = Config.getString("bshare.security.cookie.domain")

    val USER_NAME = "USER_NAME"
    val USER_ID = "USER_ID"
    val UUID = "UUID"
    val IS_ADMIN = "IS_ADMIN"
    val IS_PUBLISHER = "IS_PUBLISHER"
    val IS_PREMIUM_PUBLISHER = "IS_PREMIUM_PUBLISHER"
    val IS_ENTERPRISE = "IS_ENTERPRISE"
    val hex = Array('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f')

    /**
     * Hex data encoder. Converts byte arrays (such as those obtained from message digests)
     * into hexadecimal string representation.
     */
    def encode(data: Array[Byte]): Array[Char] = {
        val result = new Array[Char](2 * data.length)
        var j: Int = 0
        for (i <- 0 until data.length) {
            // Char for top 4 bits
            result(j) = hex((0xF0 & data(i)) >>> 4)
            j += 1
            // Bottom 4
            result(j) = hex((0x0F & data(i)))
            j += 1
        }
        return result
    }

    /**
     * Inverse operation of decodeCookie.
     *
     * @param cookieTokens the tokens to be encoded.
     * @return base64 encoding of the tokens concatenated with the ":" delimiter.
     */
    def encodeCookie(data: Array[String]) = {
        var sb = new StringBuilder();
        for (i <- 0 until data.length) {
            sb.append(data(i));
            if (i < data.length - 1) {
                sb.append(":");
            }
        }
        var value = sb.toString();

        sb = new StringBuilder(new String(Base64.encodeBase64(value.getBytes())))
        while (sb.charAt(sb.length() - 1) == '=') {
            sb.deleteCharAt(sb.length() - 1);
        }
        sb.toString()
    }

    /**
     * Calculates the digital signature to be put in the cookie. Default value is
     * MD5 ("username:tokenExpiryTime:password:key")
     */
    def makeTokenSignature(tokenExpiryTime: Long, name: String, password: String): String = {
        var data = name + ":" + tokenExpiryTime + ":" + password + ":" + COOKIE_KEY
        var signatureValue = Util.hash(data)
        return new String(encode(signatureValue))
    }

    /**
     * Decodes the cookie and splits it into a set of token strings using the ":" delimiter.
     *
     * @param cookieValue the value obtained from the submitted cookie
     * @return the array of tokens.
     * @throws InvalidCookieException if the cookie was not base64 encoded.
     */
    def decodeCookie(data: String) = {
        var cookieValue = data
        for (i <- 0 until cookieValue.length() % 4) {
            cookieValue = cookieValue + "="
        }
        if (!Base64.isBase64(cookieValue.getBytes())) {
            throw new Exception("Cookie token was not Base64 encoded; value was '" + cookieValue + "'");
        }

        val cookieAsPlainText = new String(Base64.decodeBase64(cookieValue.getBytes()))
        cookieAsPlainText.split(":")
    }

    def setCookieCode(name: String, password: String, remember: Boolean = true): unfiltered.Cookie = {
        var expiryTime = System.currentTimeMillis();
        var tokenLifetime =
            if (remember) COOKIE_EXPIRE_REMEMBER
            else COOKIE_EXPIRE_NORMAL

        expiryTime += 1000L * tokenLifetime
        var signatureValue = makeTokenSignature(expiryTime, name, password);
        val cookieValue = encodeCookie(Array(name, expiryTime.toString(), signatureValue))

        return unfiltered.Cookie(COOKIE_NAME, cookieValue,
            Some(COOKIE_DOMAIN), Some("/"), Some(tokenLifetime))
    }

}