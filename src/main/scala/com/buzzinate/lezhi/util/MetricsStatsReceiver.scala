package com.buzzinate.lezhi.util

import java.util.concurrent.{ConcurrentHashMap, TimeUnit}

import com.buzzinate.lezhi.buzzads.{AdvertiseDataServiceClient, AdvertiseFinagledClient}
import com.buzzinate.lezhi.elastic.ElasticIndexFinagledClient
import com.buzzinate.lezhi.recommend.RecommendFinagledClient
import com.twitter.finagle.stats.{Counter, Gauge, Stat, StatsReceiver}
import com.yammer.metrics.{ConsoleReporter, Counter => MCounter, Gauge => MGauge, Histogram => MHistogram, MetricRegistry}

object MetricsUtil {

    val metrics = new MetricRegistry("lezhi");

    val recommendStatsReceiver = new MetricsStatsReceiver(classOf[RecommendFinagledClient])
    val advertiseStatsReceiver = new MetricsStatsReceiver(classOf[AdvertiseFinagledClient])
    val adDataStatsReceiver = new MetricsStatsReceiver(classOf[AdvertiseDataServiceClient])

    val elasticStatsReceiver = new MetricsStatsReceiver(classOf[ElasticIndexFinagledClient])
    val reporter = ConsoleReporter.forRegistry(metrics)
            .convertRatesTo(TimeUnit.SECONDS)
            .convertDurationsTo(TimeUnit.MILLISECONDS)
            .build();
    reporter.start(30, TimeUnit.MINUTES);

    // val reporter = CsvReporter.forRegistry(metrics)
    //         .formatFor(Locale.US)
    //         .convertRatesTo(TimeUnit.SECONDS)
    //         .convertDurationsTo(TimeUnit.MILLISECONDS)
    //         .build(new File(Config.getString("metrics.csv.path")));
    // reporter.start(1, TimeUnit.SECONDS);

}

/** Finagle StatsReceiver implemented using Metrics */
class MetricsStatsReceiver[T](t: Class[T]) extends StatsReceiver {

    import MetricsUtil.metrics

    val repr = this

    val counters = new ConcurrentHashMap[String, MCounter]
    val stats = new ConcurrentHashMap[String, MHistogram]
    val gauges = new ConcurrentHashMap[String, MGauge[Float]]

    def counter(name: String*): Counter = {
        val key = name.mkString(".")
        new Counter {
            def incr(delta: Int) {
                var c = counters.get(key)
                if (c == null) {
                    counters.putIfAbsent(key, metrics.counter(MetricRegistry.name(t, key)))
                    c = counters.get(key)
                }

                if (c != null) {
                    c.inc(delta)
                } else {
                    throw new IllegalStateException()
                }
            }
        }
    }

    def stat(name: String*): Stat = {
        val key = name.mkString(".")
        new Stat {
            def add(value: Float) {
                var h = stats.get(key)
                if (h == null) {
                    stats.putIfAbsent(key, metrics.histogram(MetricRegistry.name(t, key)))
                    h = stats.get(key)
                }

                if (h != null) {
                    h.update(value.toLong)
                } else {
                    throw new IllegalStateException()
                }
            }
        }
    }

    def addGauge(name: String*)(f: => Float): Gauge = {
        val key = name.mkString(".")

        if (!gauges.contains(key)) {
            val g = new MGauge[Float] {
                def getValue(): Float = {
                    return f;
                }
            }
            val pre = gauges.putIfAbsent(key, g)
            if (pre == null) {
                // to prevent register the gauge with same key
                metrics.register(MetricRegistry.name(t, key), g)
            }
        }

        new Gauge {
            def remove() {
                metrics.remove(MetricRegistry.name(t, key))
            }
        }
    }

}