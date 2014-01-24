package com.buzzinate.lezhi.db

import org.scalatest.BeforeAndAfter
import org.scalatest.FunSuite
import com.buzzinate.lezhi.model.SiteConfig
import com.buzzinate.lezhi.service.UserService
import com.buzzinate.user.vo.User

class JedisClientTest extends FunSuite with BeforeAndAfter {
    test("Test set and get string") {
        JedisClient.set("test", 3600, "This is a test")
        assert(JedisClient.get("test") == "This is a test")
    }

    test("Test set and get object") {
        val siteConfig = new SiteConfig
        siteConfig.col = 6
        siteConfig.bdcolor = "#222222"
        JedisClient.set("siteConfig", 3600, siteConfig)
        assert(JedisClient.getObject("siteConfig").asInstanceOf[SiteConfig].bdcolor == "#222222")
        assert(JedisClient.getObject("siteConfig").asInstanceOf[SiteConfig].col == 6)

        val user = UserService.getByLoginId("xinjun91@yahoo.com.cn").get
        JedisClient.set("user", 3600, user)
        assert(JedisClient.getObject("user").asInstanceOf[Option[User]].get.getUserBasic.getName == "xinjun91@yahoo.com.cn")
    }

    test("Test delete") {
        JedisClient.set("test", 3600, "This is a test")
        assert(JedisClient.get("test") == "This is a test")
        JedisClient.delete("test")
        assert(JedisClient.get("test") == null)

        val siteConfig = new SiteConfig
        siteConfig.col = 6
        siteConfig.bdcolor = "#222222"
        JedisClient.set("siteConfig", 3600, siteConfig)
        assert(JedisClient.getObject("siteConfig").asInstanceOf[SiteConfig].bdcolor == "#222222")
        JedisClient.delete("siteConfig")
        assert(JedisClient.getObject("siteConfig") == null)
    }

    test("Test flush") {
        //        JedisClient.set("test", 3600, "This is a test")
        //        assert(JedisClient.get("test") == "This is a test")
        //        
        //        val siteConfig = new SiteConfig
        //        siteConfig.col = Option(6)
        //        siteConfig.bdcolor = Option("#222222")
        //        JedisClient.set("siteConfig", 3600, siteConfig)
        //        
        //        JedisClient.flush()
        //        assert(JedisClient.get("test") == null)
        //        assert(JedisClient.getObject("siteConfig") == null)
    }

}