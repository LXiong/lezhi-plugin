package com.buzzinate.lezhi.db

import java.sql.Connection
import scala.annotation.implicitNotFound
import org.scalaquery.session.Database
import com.buzzinate.lezhi.Config
import com.buzzinate.lezhi.util.Logging
import com.jolbox.bonecp.BoneCPDataSource
import com.buzzinate.common.util.db.MasterSlaveRoutingDataSource

object MysqlClient extends Logging {
    private val masterDataSource = new BoneCPDataSource()
    masterDataSource.setDriverClass("com.mysql.jdbc.Driver")
    masterDataSource.setJdbcUrl(Config.getString("mysql.lezhi.master.jdbcUrl"))
    masterDataSource.setUsername(Config.getString("mysql.lezhi.master.username"))
    masterDataSource.setPassword(Config.getString("mysql.lezhi.master.password"))
    masterDataSource.setMaxConnectionsPerPartition(Config.getInt("mysql.lezhi.master.max.connections.per.partition"))
    masterDataSource.setMinConnectionsPerPartition(Config.getInt("mysql.lezhi.master.min.connections.per.partition"))
    masterDataSource.setPartitionCount(Config.getInt("mysql.lezhi.master.partition.count"))
    val masterDatabase = Database.forDataSource(masterDataSource)

    private val slaveDataSource = new BoneCPDataSource()
    slaveDataSource.setDriverClass("com.mysql.jdbc.Driver")
    slaveDataSource.setJdbcUrl(Config.getString("mysql.lezhi.slave.jdbcUrl"))
    slaveDataSource.setUsername(Config.getString("mysql.lezhi.slave.username"))
    slaveDataSource.setPassword(Config.getString("mysql.lezhi.slave.password"))
    slaveDataSource.setMaxConnectionsPerPartition(Config.getInt("mysql.lezhi.slave.max.connections.per.partition"))
    slaveDataSource.setMinConnectionsPerPartition(Config.getInt("mysql.lezhi.slave.min.connections.per.partition"))
    slaveDataSource.setPartitionCount(Config.getInt("mysql.lezhi.slave.partition.count"))
    val slaveDatabase = Database.forDataSource(slaveDataSource)

    val buzzadsDataSource = new BoneCPDataSource()
    buzzadsDataSource.setDriverClass("com.mysql.jdbc.Driver")
    buzzadsDataSource.setJdbcUrl(Config.getString("buzzads.jdbcUrl"))
    buzzadsDataSource.setUsername(Config.getString("mysql.buzzads.username"))
    buzzadsDataSource.setPassword(Config.getString("mysql.buzzads.password"))
    buzzadsDataSource.setMaxConnectionsPerPartition(3)
    buzzadsDataSource.setMinConnectionsPerPartition(1)
    buzzadsDataSource.setPartitionCount(2)
    val buzzadsDatabase = Database.forDataSource(buzzadsDataSource)

    def runLezhiQuery(f: Connection => Any) = {
        val conn = slaveDataSource.getConnection()
        try {
            f(conn)
        } catch {
            case ex: Exception =>
                log.error(ex, "Failed to run query.")
                None
        } finally {
            conn.close
        }
    }

    def runLezhiUpdate(f: Connection => Boolean) = {
        val conn = masterDataSource.getConnection()
        try {
            f(conn)
        } catch {
            case ex: Exception =>
                log.error(ex, "Failed to run query.")
                false
        } finally {
            conn.close
        }
    }

    def runBuzzadsQuery[T](f: Connection => Option[T]) = {
        val conn = buzzadsDataSource.getConnection()
        try {
            f(conn)
        } catch {
            case ex: Exception =>
                log.error(ex, "Failed to run query.")
                None
        } finally {
            conn.close
        }
    }

    def runBuzzadsUpdate(f: Connection => Boolean) = {
        val conn = buzzadsDataSource.getConnection()
        try {
            f(conn)
        } catch {
            case ex: Exception =>
                log.error(ex, "Failed to run query.")
                false
        } finally {
            conn.close
        }
    }

}