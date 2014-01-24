package com.buzzinate.lezhi.elastic

import com.buzzinate.lezhi.model.Metadata

/**
 * @author jeffrey.ji <jeffrey.ji@buzzinate.com>
 * May 22, 2013 3:13:47 PM
 *
 */
trait ElasticIndexClient {

    def searchUrls(sitePrefixes: Seq[String],
        title: String,
        start: Int,
        size: Int): (Int, Seq[Metadata])

    def deleteIndexes(urls: Seq[String])

    def getByUrls(urls: Seq[String]): Seq[Metadata]

    def matchAll(sitePrefixes: Seq[String], start: Int, size: Int): (Int, Seq[Metadata])

    def updateMetadata(metadata: Metadata)

}