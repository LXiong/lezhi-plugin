package com.buzzinate.lezhi.service

import org.scalatest.BeforeAndAfter
import org.scalatest.FunSuite

class UserServiceTest extends FunSuite with BeforeAndAfter {
    test("Get User by UuidService") {
        val loginId = "xinjun91@yahoo.com.cn"
        val user = UserService.getByLoginId(loginId).get
        val userBasic = user.getUserBasic
        assert(loginId == userBasic.getLoginId)

        val userId = user.getUserId
        val uuser = UserService.getByUserId(userId).get
        assert(userId == uuser.getUserId)

        val email = "xinjun91@yahoo.com.cn"
        val euser = UserService.getByEmail(email).get
        assert(userId == euser.getUserId)
    }
}