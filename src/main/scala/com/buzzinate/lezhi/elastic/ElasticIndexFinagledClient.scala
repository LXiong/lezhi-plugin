package com.buzzinate.lezhi.elastic

import org.apache.thrift.protocol.TBinaryProtocol
import com.buzzinate.lezhi.Config
import com.buzzinate.lezhi.thrift.ElasticIndexService
import com.buzzinate.lezhi.util.MetricsUtil
import com.twitter.finagle.builder.ClientBuilder
import com.twitter.finagle.thrift.ThriftClientFramedCodec
import com.twitter.util.Duration
import com.twitter.util.Future
import com.buzzinate.lezhi.thrift.SearchResult
import com.buzzinate.lezhi.thrift.Metadata

/**
 * @author jeffrey.ji <jeffrey.ji@buzzinate.com>
 * May 22, 2013 3:14:29 PM
 *
 */
class ElasticIndexFinagledClient {
    val service = ClientBuilder.safeBuild(
        ClientBuilder().failFast(false).
            hosts(Config.getString("elastic.thrift.host")).
            codec(ThriftClientFramedCodec()).
            // Retry number, only applies to recoverable errors
            retries(3).
            requestTimeout(Duration.fromMilliseconds(Config.getInt("elastic.thrift.timeout").toLong)).
            // Connection pool core size
            hostConnectionCoresize(Config.getInt("elastic.thrift.pool.coreSize")).
            // Connection pool max size
            hostConnectionLimit(Config.getInt("elastic.thrift.pool.maxActive")).
            // The amount of idle time for which a connection is cached in pool.
            hostConnectionIdleTime(Duration.fromSeconds(Config.getInt("elastic.thrift.pool.maxIdle"))).
            // The maximum number of connection requests that are queued when the 
            // connection concurrency exceeds the connection limit.
            hostConnectionMaxWaiters(Config.getInt("elastic.thrift.pool.maxWait")).
            reportTo(MetricsUtil.elasticStatsReceiver))

    val client = new ElasticIndexService.FinagledClient(service, new TBinaryProtocol.Factory, "ElasticIndexService", MetricsUtil.elasticStatsReceiver)

    def searchUrls(sitePrefix: Seq[String],
        title: String,
        start: Int,
        size: Int): Future[SearchResult] = {
        client.searchUrls(sitePrefix, title, start, size)
    }

    def deleteIndexes(urls: Seq[String]) = {
        client.deleteIndexes(urls)
    }

    def getByUrls(urls: Seq[String]): Future[Seq[Metadata]] = {
        client.getByUrls(urls)
    }

    def matchAll(sitePrefixes: Seq[String], start: Int, size: Int): Future[SearchResult] = {
        client.matchAll(sitePrefixes, start, size)
    }

    def updateMetadata(metadata: Metadata) = {
        client.updateMetadata(metadata)
    }
}