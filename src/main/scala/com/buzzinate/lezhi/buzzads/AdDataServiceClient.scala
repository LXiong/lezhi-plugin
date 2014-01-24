package com.buzzinate.lezhi.buzzads

import org.apache.thrift.protocol.TBinaryProtocol

import com.buzzinate.buzzads.data.converter.PublisherContactConverter
import com.buzzinate.buzzads.data.thrift.{ PublisherContactNotFoundException, TAdDataAccessServices }
import com.buzzinate.buzzads.domain.PublisherContactInfo
import com.buzzinate.lezhi.Config
import com.buzzinate.lezhi.util.Logging
import com.buzzinate.lezhi.util.MetricsUtil

import com.twitter.finagle.builder.ClientBuilder
import com.twitter.finagle.thrift.ThriftClientFramedCodec
import com.twitter.util.Duration

class AdvertiseDataServiceClient extends Logging {
    val service = ClientBuilder.safeBuild(
        ClientBuilder().
            hosts(Config.getString("data.access.server.host")).
            codec(ThriftClientFramedCodec.get()).
            // Retry number, only applies to recoverable errors
            retries(3).
            requestTimeout(Duration.fromSeconds(5)).
            // Connection pool core size
            hostConnectionCoresize(Config.getInt("data.access.connection.num")).
            // Connection pool max size
            hostConnectionLimit(Config.getInt("data.access.connection.limit")).
            // The amount of idle time for which a connection is cached in pool.
            hostConnectionIdleTime(Duration.fromSeconds(Config.getInt("data.access.connection.idle"))).
            // The maximum number of connection requests that are queued when the 
            // connection concurrency exceeds the connection limit.
            hostConnectionMaxWaiters(Config.getInt("data.access.connection.maxwaiter")).
            reportTo(MetricsUtil.adDataStatsReceiver))

    val client = new TAdDataAccessServices.FinagledClient(service, new TBinaryProtocol.Factory, "AdDataAccessServices", MetricsUtil.adDataStatsReceiver)

    def getPublisherContact(userId: Int): Option[PublisherContactInfo] = {
        try {
            val tInfoFuture = client.findPublisherContact(userId)
            val info = PublisherContactConverter.fromThrift(tInfoFuture.get())
            Some(info)
        } catch {
            case ex: PublisherContactNotFoundException =>
                log.error(ex, "failed to get publisher contact info for userId = " + userId)
                None
            case ex: Exception =>
                log.error(ex, "Unknown exception, thrift service may has an exception")
                throw ex
        }
    }

    def saveOrUpdatePublisherContact(info: PublisherContactInfo): Boolean = {
        val tPublisherContactInfo = PublisherContactConverter.toThrift(info)
        try {
            client.saveOrUpdatePublisherContact(tPublisherContactInfo).get()
        } catch {
            case ex: Exception =>
                log.error(ex, "failed to save or update publisher contact info for userId = " + info.getUserId())
                false
        }
        true
    }
}
