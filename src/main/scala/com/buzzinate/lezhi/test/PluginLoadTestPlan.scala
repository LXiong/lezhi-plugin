package com.buzzinate.lezhi.test

import com.buzzinate.lezhi.Config
import com.buzzinate.lezhi.util._
import com.buzzinate.lezhi.plan.plugin.NettyPlan

import java.util.Date
import scala.actors.Actor._

import org.apache.commons.lang.StringUtils

import akka.actor._
import java.util.concurrent.Executors
import akka.dispatch.{Future, ExecutionContext}

import org.jboss.netty.handler.codec.http.{HttpResponse => JHttpResponse}
import unfiltered.Async.Responder
import unfiltered.request._
import unfiltered.response._
import unfiltered.request.QParams._
import unfiltered.netty._


class PluginLoadTestPlan extends cycle.Plan with cycle.ThreadPool with ServerErrorResponse {

    def intent = {
        case req @ GET(Path("/test/netty_cycle") & Params(params)) => 

            // sleep 200 ms to imulate backend
            Thread.sleep(500)
    
            Ok ~> CacheControl("no-cache") ~> ResponseString("Response!")
    }
}




class PluginLoadTestPlanDefer extends cycle.Plan with cycle.DeferralExecutor with cycle.DeferredIntent 
        with ServerErrorResponse {

    override def underlying = MyExecutor.underlying

    def intent = {
        case req @ GET(Path("/test/netty_defer") & Params(params)) => 

            // sleep 200 ms to imulate backend
            Thread.sleep(500)
    
            Ok ~> CacheControl("no-cache") ~> ResponseString("Response!")
    }

}

object MyExecutor {
    import org.jboss.netty.handler.execution._
    lazy val underlying = new MemoryAwareThreadPoolExecutor(
        16, 65536, 1048576)
}


class PluginLoadTestPlanAsync extends async.Plan with ServerErrorResponse  {

    def intent = {
        case req @ GET(Path("/test/netty_async") & Params(params)) => 

            // sleep 200 ms to imulate backend
            Thread.sleep(500)
    
            req.respond(Ok ~> CacheControl("no-cache") ~> ResponseString("Response!"))
    }

}


class PluginLoadTestPlanFuture extends async.Plan with ServerErrorResponse {

    val pool = Executors.newCachedThreadPool()
    implicit val ec = ExecutionContext.fromExecutorService(pool)

    type Request = HttpRequest[ReceivedMessage] with Responder[JHttpResponse]

    def intent = {
        case req @ GET(Path("/test/netty_future")) => get(req)
    }

    def get(request: Request): Unit = {

        val future = Future {
            Thread.sleep(500)  
        }

        future.onComplete {
            case Right(result) =>
                request.respond(Ok ~> CacheControl("no-cache") ~> ResponseString("Response!"))
            case Left(e) =>
                request.respond(InternalServerError ~> ResponseString(e.getMessage))
        }
    }
}