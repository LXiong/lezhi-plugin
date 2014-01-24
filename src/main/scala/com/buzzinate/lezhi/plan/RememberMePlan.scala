package com.buzzinate.lezhi.plan

import org.apache.commons.lang.StringUtils

import com.buzzinate.lezhi.Config
import com.buzzinate.lezhi.service.UserService
import com.buzzinate.lezhi.util.{Logging, LoginUtil}
import com.buzzinate.user.vo.Role

import javax.servlet.http.HttpServletRequest
import unfiltered.Cookie
import unfiltered.request.{Cookies, HttpRequest}
import unfiltered.response.Pass

class RememberMePlan extends unfiltered.filter.Plan with Logging {

    def intent = {
        case req @ Cookies(cookies) =>
            doRememberMe(cookies, req)
            Pass
    }

    private def doRememberMe(cookies: Map[String, Option[Cookie]], req: HttpRequest[HttpServletRequest]) = {
        val tokens = getCookieTokens(cookies)
        if (tokens.length > 2) {
            val loginId = tokens(0)
            val tokenExpiryTime = tokens(1)
            val signatureValue = tokens(2)
            val user = UserService.getByLoginId(loginId)
            if (user.isDefined) {
                val password = UserService.getPassWordByUserId(user.get.getUserId)
                val expectedSignatureValue = LoginUtil.makeTokenSignature(tokenExpiryTime.toLong, loginId, password)
                if (expectedSignatureValue.equals(signatureValue)) {
                    req.underlying.setAttribute(LoginUtil.USER_ID, user.get.getUserId)
                    req.underlying.setAttribute(LoginUtil.USER_NAME, user.get.getUserBasic.getName)
                    if (user.get.getRoles.contains(new Role(Config.getString("bshare.role.lezhi.admin")))) {
                        req.underlying.setAttribute(LoginUtil.IS_ADMIN, true)
                    }
                    if (user.get.getRoles.contains(new Role(Config.getString("bshare.role.lezhi.publisher")))) {
                        req.underlying.setAttribute(LoginUtil.IS_PUBLISHER, true)
                    }
                    if (user.get.getRoles.contains(new Role(Config.getString("bshare.role.lezhi.premium.publisher")))) {
                        req.underlying.setAttribute(LoginUtil.IS_PREMIUM_PUBLISHER, true)
                    }
                    if (user.get.getRoles.contains(new Role(Config.getString("bshare.role.lezhi.enterprise")))) {
                        req.underlying.setAttribute(LoginUtil.IS_ENTERPRISE, true)
                    }
                }
            }
        }
    }

    private def getCookieTokens(cookies: Map[String, Option[Cookie]]) = {
        cookies(LoginUtil.COOKIE_NAME) match {
            case Some(c: Cookie) =>
                val cookieValue = c.value
                if (StringUtils.isNotEmpty(cookieValue))
                    LoginUtil.decodeCookie(cookieValue)
                else Array[String]()
            case _ => Array[String]()
        }
    }

}