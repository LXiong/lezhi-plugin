package com.buzzinate.lezhi.buzzads

import org.apache.thrift.protocol.TBinaryProtocol

import com.buzzinate.buzzads.thrift.{AdItem, AdParam, AdServices}
import com.buzzinate.lezhi.Config
import com.buzzinate.lezhi.util.MetricsUtil
import com.twitter.finagle.builder.ClientBuilder
import com.twitter.finagle.thrift.ThriftClientFramedCodec
import com.twitter.util.{Duration, Future}

/**
 * @author jeffrey created on 2012-11-28 下午3:00:53
 *
 */
class AdvertiseFinagledClient {
    val service = ClientBuilder.safeBuild(
        ClientBuilder().failFast(false).
            hosts(Config.getString("buzzads.thrift.host")).
            codec(ThriftClientFramedCodec()).
            // Retry number, only applies to recoverable errors
            retries(3).
            requestTimeout(Duration.fromMilliseconds(Config.getInt("buzzads.thrift.timeout").toLong)).
            // Connection pool core size
            hostConnectionCoresize(Config.getInt("buzzads.thrift.pool.coreSize")).
            // Connection pool max size
            hostConnectionLimit(Config.getInt("buzzads.thrift.pool.maxActive")).
            // The amount of idle time for which a connection is cached in pool.
            hostConnectionIdleTime(Duration.fromSeconds(Config.getInt("buzzads.thrift.pool.maxIdle"))).
            // The maximum number of connection requests that are queued when the 
            // connection concurrency exceeds the connection limit.
            hostConnectionMaxWaiters(Config.getInt("buzzads.thrift.pool.maxWait")).
            reportTo(MetricsUtil.advertiseStatsReceiver))

    val client = new AdServices.FinagledClient(service, new TBinaryProtocol.Factory, "AdvertisServices", MetricsUtil.advertiseStatsReceiver)

    def serve(param: AdParam): Future[java.util.List[AdItem]] = {
        client.serve(param)
    }
}
