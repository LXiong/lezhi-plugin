package com.buzzinate.lezhi.util

import org.apache.http.client.methods.HttpGet
import org.apache.http.params.BasicHttpParams
import org.apache.http.impl.client.{DefaultHttpClient, StandardHttpRequestRetryHandler}
import org.apache.http.conn.scheme.{Scheme, SchemeRegistry, PlainSocketFactory}
import org.apache.http.impl.conn.PoolingClientConnectionManager



object SimpleHttpClient extends Logging {

    private val schemeRegistry = new SchemeRegistry()
    schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));
    private val connectionManager = new PoolingClientConnectionManager(schemeRegistry);
    // increase max per route since we only call bshare or recommendation backend
    connectionManager.setDefaultMaxPerRoute(50);
    connectionManager.setMaxTotal(100);
    private val httpParams = new BasicHttpParams()
    httpParams.setParameter("http.socket.timeout", 2000)
    httpParams.setParameter("http.connection.timeout", 5000)

    private val httpClient = new DefaultHttpClient(connectionManager, httpParams)
    // set retry count to 3
    httpClient.setHttpRequestRetryHandler(new StandardHttpRequestRetryHandler(3, true))


    /**
     * Returns the text content from a REST URL. Returns a blank String if there
     * is a problem.
     */
    def getContent(url: String): String = {
        var content = ""
        try {
            val httpResponse = httpClient.execute(new HttpGet(url))
            val entity = httpResponse.getEntity()
            if (entity != null) {
                val inputStream = entity.getContent()
                content = scala.io.Source.fromInputStream(inputStream)("UTF-8").getLines.mkString
                inputStream.close
            }
            return content
        } catch {
            case ex: Exception => log.error(ex, "Failed to get url: " + url)
        } 
        return content
    }

    /**
     * Visit the url withour returning result
     */
    def get(url: String): Boolean = {
        try {
            httpClient.execute(new HttpGet(url))
            true
        } catch {
            case ex: Exception => log.error(ex, "Failed to get url: " + url)
            false
        }
    }


    def close() {
        httpClient.getConnectionManager().shutdown()
    }
}