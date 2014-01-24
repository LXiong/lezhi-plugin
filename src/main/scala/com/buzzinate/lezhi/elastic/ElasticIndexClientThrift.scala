package com.buzzinate.lezhi.elastic

import com.buzzinate.lezhi.util.Logging
import com.buzzinate.lezhi.thrift.SearchResult
import com.buzzinate.lezhi.thrift.{ Metadata => TMetadata }
import com.buzzinate.lezhi.model.Metadata

/**
 * @author jeffrey.ji <jeffrey.ji@buzzinate.com>
 * May 22, 2013 3:14:09 PM
 *
 */
object ElasticIndexClientThrift {
    val client = new ElasticIndexFinagledClient
}

class ElasticIndexClientThrift extends ElasticIndexClient with Logging {
    import ElasticIndexClientThrift.client

    def searchUrls(sitePrefixes: Seq[String],
        title: String,
        start: Int,
        size: Int): (Int, Seq[Metadata]) = {
        val result = client.searchUrls(sitePrefixes, title, start, size).get
        (result._1, result._2.map { Metadata.getMetadata(_) })
    }

    def deleteIndexes(urls: Seq[String]) = {
        client.deleteIndexes(urls)
    }

    def getByUrls(urls: Seq[String]): Seq[Metadata] = {
        client.getByUrls(urls).get.map { Metadata.getMetadata(_) }
    }

    def matchAll(sitePrefixes: Seq[String], start: Int, size: Int): (Int, Seq[Metadata]) = {
        val result = client.matchAll(sitePrefixes, start, size).get
        (result._1, result._2.map { Metadata.getMetadata(_) })
    }

    def updateMetadata(metadata: Metadata) = {
        client.updateMetadata(Metadata.getTMetadata(metadata))
    }

}
