package com.buzzinate.lezhi.recommend

import org.apache.thrift.protocol.TBinaryProtocol

import com.buzzinate.lezhi.Config
import com.buzzinate.lezhi.thrift.{ClickParam, RecommendParam, RecommendResult, RecommendServices}
import com.buzzinate.lezhi.util.MetricsUtil
import com.twitter.finagle.builder.ClientBuilder
import com.twitter.finagle.thrift.ThriftClientFramedCodec
import com.twitter.util.{Duration, Future}

class RecommendFinagledClient {
    lazy val service = ClientBuilder.safeBuild(
        ClientBuilder().failFast(false).
            hosts(Config.getString("backend.thrift.host")).
            codec(ThriftClientFramedCodec()).
            // Retry number, only applies to recoverable errors
            retries(3).
            requestTimeout(Duration.fromMilliseconds(Config.getInt("buzzads.thrift.timeout").toLong)).
            // Connection pool core size
            hostConnectionCoresize(Config.getInt("backend.thrift.pool.coreSize")).
            // Connection pool max size
            hostConnectionLimit(Config.getInt("backend.thrift.pool.maxActive")).
            // The amount of idle time for which a connection is cached in pool.
            hostConnectionIdleTime(Duration.fromSeconds(Config.getInt("backend.thrift.pool.maxIdle"))).
            // The maximum number of connection requests that are queued when the 
            // connection concurrency exceeds the connection limit.
            hostConnectionMaxWaiters(Config.getInt("backend.thrift.pool.maxWait")).
            reportTo(MetricsUtil.recommendStatsReceiver))

    val client = new RecommendServices.FinagledClient(service, new TBinaryProtocol.Factory, "RecommendServices", MetricsUtil.recommendStatsReceiver)

    def recommend(param: RecommendParam): Future[RecommendResult] = {
        client.recommend(param)
    }

    def click(param: ClickParam) = {
        client.click(param)
    }

    def recrawl(url: String) = {
        client.recrawl(url)
    }

    def correctImg(url: String, imageUrl: String, userAgent: String) = {
        client.correctImg(url, imageUrl, Some(userAgent))
    }
}
