package com.buzzinate.lezhi

import org.eclipse.jetty.servlet.ServletHolder

import com.buzzinate.lezhi.plan.{ BasicPlan, RememberMePlan }
import com.buzzinate.lezhi.plan.website.{ AdminPlan, AdvertisePlan, ContentPlan, PublisherBackendPlan, UuidPathPlan, WebsitePlan }
import com.buzzinate.lezhi.util.{ Logging, SimpleHttpClient }

import unfiltered.request._
import unfiltered.request.QParams._
import unfiltered.response._
import unfiltered.scalate.Scalate

class ErrorPlan extends BasicPlan {
    def intent = {
        case Path("/metrics/metrics") => Pass

        case req @ _ => NotFound ~> Scalate(req, "404.ssp")
    }
}

/**
 * Embedded jetty server
 */
object WebsiteServer extends Logging {

    def main(args: Array[String]) {
        val http = unfiltered.jetty.Http(Config.getInt("server.port"), "0.0.0.0")

        http.context("/assets") {
            _.resources(
                //new java.net.URL(getClass().getResource("/www/"), ".")
                new java.net.URL("file://" + Config.getString("root.dir") + "www/"))
        }
            .context("/favicon.ico") {
                _.resources(
                    new java.net.URL("file://" + Config.getString("root.dir") + "www/favicon.ico"))
            }

        http.current.addServlet(new ServletHolder(
            classOf[com.yammer.metrics.servlets.MetricsServlet]), "/metrics/metrics")
        // http.current.addServlet(new ServletHolder(
        //     classOf[com.yammer.metrics.servlets.HealthCheckServlet]), "/metrics/healthcheck")
        // http.current.addServlet(new ServletHolder(
        //     classOf[com.yammer.metrics.servlets.ThreadDumpServlet]), "/metrics/threads")
        // http.current.addServlet(new ServletHolder(
        //     classOf[com.yammer.metrics.servlets.PingServlet]), "/metrics/ping")
        // http.current.addServlet(new ServletHolder(
        //     classOf[com.yammer.metrics.servlets.AdminServlet]), "/metrics")

        http.filter(new RememberMePlan)
            .filter(new PublisherBackendPlan)
            .filter(new ContentPlan)
            .filter(new AdminPlan)
            .filter(new AdvertisePlan)
            .filter(new UuidPathPlan)
            .filter(new WebsitePlan)
            .filter(new ErrorPlan)

        http.run(
            { svr => log.info("starting website server") },
            { svr =>
                SimpleHttpClient.close()
                log.info("shutting down website server")
            })
    }
}

