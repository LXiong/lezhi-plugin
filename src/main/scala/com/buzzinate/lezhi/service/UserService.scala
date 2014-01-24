package com.buzzinate.lezhi.service

import com.buzzinate.lezhi.util.Logging
import com.buzzinate.user.service.UserFinagledService
import com.buzzinate.lezhi.db.JedisClient
import com.buzzinate.user.vo.{ EmailCredential, User }
import com.buzzinate.lezhi.Config

/**
 * @author jeffrey.ji <jeffrey.ji@buzzinate.com>
 * Jul 8, 2013 1:40:10 PM
 *
 */
object UserService extends Logging {
    private val userFinagledService = new UserFinagledService(Config.getString("buzz.user.server.host"))
    userFinagledService.setHostConnectionCoresize(Config.getInt("buzz.user.connection.num"))
    userFinagledService.setHostConnectionLimit(Config.getInt("buzz.user.connection.limit"))
    userFinagledService.setHostConnectionIdleTime(Config.getInt("buzz.user.connection.idle"))
    userFinagledService.setHostConnectionMaxWaiters(Config.getInt("buzz.user.connection.maxwaiter"))

    private val userService = new com.buzzinate.user.service.UserService
    userService.setUserFinagledService(userFinagledService)

    private val emailCredService = new com.buzzinate.user.service.EmailCredService
    emailCredService.setUserFinagledService(userFinagledService)

    private val CACHE_PREFIX = "USR:"
    private val CACHE_PASSWORD = "PASSWORD:"

    def getByEmail(email: String): Option[User] = {
        try {
            val key = CACHE_PREFIX + email
            val res = JedisClient.getObject(key).asInstanceOf[Option[User]]
            if (res == null) {
                val user = userService.getUserByEmail(email)
                if (user != null) {
                    JedisClient.set(key, 3600, Some(user))
                    return Some(user)
                } else
                    return None
            }
            res
        } catch {
            case ex: Exception =>
                log.error(ex, "Failed to get user: [email=" + email + "]")
                None
        }
    }

    def getByUserId(userId: Int): Option[User] = {
        try {
            val key = CACHE_PREFIX + userId
            val res = JedisClient.getObject(key).asInstanceOf[Option[User]]
            if (res == null) {
                val user = userService.getUserById(userId)
                if (user != null) {
                    JedisClient.set(key, 3600, Some(user))
                    return Some(user)
                } else
                    return None
            }
            res
        } catch {
            case ex: Exception =>
                log.error(ex, "Failed to get user: [userId=" + userId + "]")
                None
        }
    }

    def getByLoginId(loginId: String): Option[User] = {
        try {
            val key = CACHE_PREFIX + loginId
            val res = JedisClient.getObject(key).asInstanceOf[Option[User]]
            if (res == null) {
                val user = userService.getUserByLoginId(loginId)
                val test = Some(user)
                JedisClient.set(key, 3600, test)
                if (user != null) {
                    return Some(user)
                } else
                    return None
            }
            res
        } catch {
            case ex: Exception =>
                log.error(ex, "Failed to get user: [loginId=" + loginId + "]")
                None
        }
    }

    def getPassWordByUserId(userId: Int): String = {
        var password = ""
        try {
            val key = CACHE_PASSWORD + userId
            password = JedisClient.get(key).asInstanceOf[String]
            if (password == null) {
                val cred = emailCredService.getEmailCredByUserId(userId)
                if (cred != null) {
                    password = cred.getPassword
                    JedisClient.set(key, 3600, password)
                }
            }
            password
        } catch {
            case ex: Exception =>
              ex.printStackTrace
                log.error(ex, "Failed to get email credential: [userId=" + userId + "]")
                password
        }
    }
}