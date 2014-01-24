package com.buzzinate.lezhi.model

import com.buzzinate.common.util.UuidUtil
import org.scalatest.{ BeforeAndAfter, FunSuite }
import com.buzzinate.lezhi.bean.Paginate
import java.util

/**
 * uuid path test
 *
 * User: magic
 * Date: 13-7-22
 * Time: 下午1:39
 */
class UuidPathTest extends FunSuite with BeforeAndAfter {

    private val uuid1 = "6ee942be-d897-4f60-9a70-7acc2035c7a0"
    private val path1 = "http://www.test1.com"
    private val uuid2 = "6ee942be-d897-4f60-9a70-7acc2035c7a1"
    private val path2 = "http://www.test2.com"
    private val uuid3 = "6ee942be-d897-4f60-9a70-7acc2035c7a2"
    private val path3 = "http://www.test3.com"

    before {
        (1 to 3).foreach(i => UuidPath.addOrUpdateUuidPath(path1 + i, uuid1))
        UuidPath.addOrUpdateUuidPath(path2, uuid2)
    }

    after {
        UuidPath.deleteAll
    }

    test("countall") {
        println(UuidPath.countAll)
        assert(UuidPath.countAll == 4)
    }

    test("test get site prefixs") {
        val sitePrefixs = UuidPath.getSitePrefixs(uuid1)
        assert(sitePrefixs.size == 3)

        val sitePrefixs2 = UuidPath.getSitePrefixs(uuid2)
        assert(sitePrefixs2.size == 1)
    }

    test("get uuid paths by paginte") {
        var uuidpaths = UuidPath.getUuidPathsAsPaginate(new Paginate(1))
        assert(uuidpaths.size == 4)

        uuidpaths = UuidPath.getUuidPathsAsPaginate(new Paginate(2))
        assert(uuidpaths.size == 0)

        uuidpaths = UuidPath.getUuidPaths(uuid1)
        assert(uuidpaths.size == 3)
    }

    test("get or update uuid paths") {
        val uuidpathOption = UuidPath.getUuidPath(path2)
        val uuidpath = uuidpathOption.get
        val uuid = UuidUtil.byteArrayToUuid(uuidpath.uuid)
        assert(uuid == uuid2)
        assert(0 == uuidpath.status)

        // update status
        UuidPath.updateStatus(uuidpath.id, true)
        assert(1 == UuidPath.getUuidPath(path2).get.status)

        // update uuid/path
        UuidPath.addOrUpdateUuidPath(path2, uuid1)
        assert(UuidPath.getUuidPaths(uuid1).size == 4)

        // batch update
        val pathUUidMap = new java.util.HashMap[String, String]
        UuidPath.addOrUpdateUuidPath(path2, uuid2)
        UuidPath.addOrUpdateUuidPath(path3, uuid3)
        UuidPath.batchUpdatePathUuid(pathUUidMap)

        var uuidpaths = UuidPath.getUuidPaths(uuid3)
        assert(uuidpaths.size == 1)
        assert(uuidpaths(0).path == path3)
        assert(uuidpaths(0).status == 0)

        uuidpaths = UuidPath.getUuidPaths(uuid2)
        assert(uuidpaths.size == 1)
        assert(uuidpaths(0).path == path2)
    }

    test("test delete") {
        val uuidpathOption = UuidPath.getUuidPath(path2)
        assert(uuidpathOption != None)
        val uuidpath = uuidpathOption.get

        UuidPath.deleteUuidPath(uuidpath.id)
        assert(UuidPath.getUuidPath(path2) == None)

        UuidPath.deleteUuidPath(path1 + 1)
        assert(UuidPath.getUuidPath(path1 + 1) == None)

        UuidPath.deleteAll
        assert(UuidPath.getUuidPaths(uuid1).size == 0)
    }
}
