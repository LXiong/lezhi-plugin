package com.buzzinate.lezhi.db

import org.apache.hadoop.hbase.HBaseConfiguration
import org.apache.hadoop.hbase.client.{ HTableInterface, HTablePool }
import com.buzzinate.lezhi.util.Logging
import com.buzzinate.lezhi.Config

object HBaseClient extends Logging {
    val hbaseZookeeperQuorum = Config.getString("hbase.zookeeper.quorum")
    val poolSize=Config.getInt("htable.pool.size")
    val STAT_PAGE_TABLE = "lezhi_stat_page"
    val STAT_COLUMN_FAMILY = "stat_page"

    //val htablePool = HBaseClient.createHTablePool(hbaseZookeeperQuorum, poolSize)

    private def createHTablePool(hbaseZookeeperQuorum: String, hTableReference: Int): HTablePool = {
        val conf = HBaseConfiguration.create()
        conf.set("hbase.zookeeper.quorum", hbaseZookeeperQuorum)
        new HTablePool(conf, hTableReference)
    }
}