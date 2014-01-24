package com.buzzinate.lezhi.plugin

import com.buzzinate.lezhi.plugin.PluginConstant.recommendClient
import com.buzzinate.lezhi.util.Logging
import com.buzzinate.lezhi.bean.ClickMessage

import akka.actor.Actor

/**
 * @author jeffrey created on 2012-11-28 下午11:44:08
 *
 */
class ClickActor extends Actor with Logging {

    def receive = {

        case ClickMessage(userId, toUrl, fromUrl, typeStr, sitePrefix) =>
            recommendClient.click(userId, toUrl, fromUrl, typeStr, sitePrefix)

        case _ =>
            log.debug("Unknownm click message...")
    }
}