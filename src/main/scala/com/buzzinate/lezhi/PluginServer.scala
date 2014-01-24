package com.buzzinate.lezhi

import com.buzzinate.lezhi.plan.plugin.{ PluginPlan, TracePlan }
import com.buzzinate.lezhi.util.{ Logging, SimpleHttpClient }
import unfiltered.response.{ NotFound, ResponseString }

/**
 * Embedded jetty server
 */
object PluginServer extends Logging {
    def main(args: Array[String]) {
        Config.initialize("conf/app-plugin.conf")
        unfiltered.netty.Http(Config.getInt("plugin.server.port"))
            .chunked(1048576)
            .plan(new PluginPlan)
            .plan(new TracePlan)
            .plan(unfiltered.netty.cycle.Planify {
                case _ => NotFound ~> ResponseString("404: Page not found.")
            })
            .run(
                { svr => log.info("starting plugin server") },
                { svr =>
                    SimpleHttpClient.close()
                    log.info("shutting down plugin server")
                })
    }
}

