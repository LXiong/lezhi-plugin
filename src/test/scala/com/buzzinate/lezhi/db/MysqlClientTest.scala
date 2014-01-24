package com.buzzinate.lezhi.db

import org.scalaquery.ql.Query
import org.scalaquery.ql.TypeMapper.{ IntTypeMapper, StringTypeMapper }
import org.scalaquery.ql.extended.{ ExtendedTable => Table }
import org.scalaquery.ql.extended.MySQLDriver.Implicit._
import org.scalaquery.session.Database.threadLocalSession
import org.scalaquery.session.Session

import com.buzzinate.lezhi.db.MysqlClient.{ masterDatabase, slaveDatabase }
import com.buzzinate.lezhi.util.Logging

object MysqlClientTest extends Logging {

    case class Test(id: Int, test: String)

    val test = new Table[Test]("test") {
        def id = column[Int]("id", O.PrimaryKey, O.AutoInc) // This is the primary key column
        def test = column[String]("test")
        def * = id ~ test <> (Test, Test.unapply _)
    }

    import org.scalaquery.meta.{ MTable }

    def checkAndCreateTable() = {
        try {
            masterDatabase withSession { session: Session =>
                val tableList = MTable.getTables.list()(session)
                val tableMap = tableList.map {
                    t => (t.name.name, t)
                }.toMap

                if (!tableMap.contains("tableName")) {
                    test.ddl.create(session)
                }
            }
        } catch {
            case ex: Exception =>
                log.error(ex, "Failed to get ]")
        }
    }

    def add(testStr: String): Boolean = {
        val inserts = Set(Test(1, testStr), Test(1, testStr), Test(2, testStr))
        try {
            masterDatabase withSession {
                test.insertAll(inserts.toList: _*)
            }
            true
        } catch {
            case ex: Exception =>
                log.error(ex, "Failed to save test, [test: " + inserts + "]");
                false
        }
    }

    def get() = {
        try {
            slaveDatabase withSession {
                Query(test) foreach { test =>
                    println(test)
                }
            }
        } catch {
            case ex: Exception =>
        }
    }

    def main(args: Array[String]) {
        checkAndCreateTable
        add("fuck")
        get
    }
}