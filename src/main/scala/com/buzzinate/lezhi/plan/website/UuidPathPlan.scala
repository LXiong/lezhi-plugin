package com.buzzinate.lezhi.plan.website

import org.apache.commons.lang.StringUtils

import com.buzzinate.lezhi.bean.Paginate
import com.buzzinate.lezhi.model.UuidPath
import com.buzzinate.lezhi.plan.BasicPlan

import unfiltered.request._
import unfiltered.request.QParams._
import unfiltered.response._
import unfiltered.scalate.Scalate

/**
 * uuid path admin manager plan
 *
 * User: magic
 * Date: 13-7-23
 * Time: 上午11:03
 */
class UuidPathPlan extends BasicPlan {

    def intent = {
        case req @ GET(Path("/admin/uuidpaths") & Params(params)) =>
            if (!isAdmin(req)) {
                Unauthorized ~> Scalate(req, "401.ssp")
            } else {
                val expected = for {
                    pagination <- lookup("pagination") is
                        int { "'" + _ + "'' is not an integer" }
                    keywordStr <- lookup("keyword") is trimmed
                } yield {
                    var uuidPathList: Seq[UuidPath] = List[UuidPath]()
                    var err = (-1, "")
                    val page = new Paginate(pagination.getOrElse(1))
                    var totalSize = 0

                    if (keywordStr.isDefined && StringUtils.isNotEmpty(keywordStr.get)) {
                        val keyword = keywordStr.get
                        if (keyword.startsWith("http")) {
                            val uuidPath = UuidPath.getUuidPath(keyword)
                            if (uuidPath.isDefined) {
                                uuidPathList = uuidPath.get :: Nil
                            } else {
                                err = (1, keyword + "对应的网站不存在")
                            }
                        } else {
                            uuidPathList = UuidPath.getUuidPaths(keyword)
                            if (uuidPathList.isEmpty) {
                                err = (1, keyword + "对应的网站不存在")
                            }
                        }
                        totalSize = uuidPathList.size
                    } else {
                        uuidPathList = UuidPath.getUuidPathsAsPaginate(page)
                        totalSize = UuidPath.countAll
                    }

                    Ok ~> Scalate(req, "admin/uuidpath/uuidpaths.ssp",
                        ("keyword", keywordStr.getOrElse("")),
                        ("uuidPathList", uuidPathList),
                        ("pagination", page.pageNum),
                        ("pageSize", page.pageSize),
                        ("totalSize", totalSize),
                        ("err", err))
                }
                expected(params) orFail { fails =>
                    BadRequest ~> Scalate(req, "400.ssp", ("fails", fails))
                }
            }

        case req @ GET(Path("/admin/uuidpaths/verifyModify") & Params(params)) =>
            if (!isAdmin(req)) {
                Unauthorized ~> Scalate(req, "401.ssp")
            } else {
                val expected = for {
                    id <- lookup("id") is int { "'id" + _ + "' 不是正整数" }
                    status <- lookup("status") is trimmed is
                        required("missing param") is
                        nonempty("empty param")
                } yield {
                    val err = (0, "状态更改成功")
                    if (status.get == "true") {
                        UuidPath.updateStatus(id.get, true)
                    } else {
                        UuidPath.updateStatus(id.get, false)
                    }

                    Ok ~> ResponseString("{\"result\":\"" + err._1 + "\", \"message\":\"" + err._2 + "\"}")
                }
                expected(params) orFail { fails =>
                    BadRequest ~> Scalate(req, "400.ssp", ("fails", fails))
                }
            }

        case req @ POST(Path("/admin/uuidpaths/add") & Params(params)) =>
            if (!isAdmin(req)) {
                Unauthorized ~> Scalate(req, "401.ssp")
            } else {
                val expected = for {
                    uuidStr <- lookup("uuid") is trimmed is
                        required("missing param") is
                        nonempty("empty param")
                    path <- lookup("path") is trimmed is
                        required("missing param") is
                        nonempty("empty param")
                } yield {
                    val err = (0, "记录成功添加")
                    val uuidPath = UuidPath.getUuidPath(path.get)

                    if (!path.get.startsWith("http")) {
                        BadRequest ~> Scalate(req, "400.ssp", ("fails", List(Fail("path", path.get + "必须以http开头"))))
                    } else if (uuidPath.isDefined) {
                        BadRequest ~> Scalate(req, "400.ssp", ("fails", List(Fail("path", path.get + "的记录已存在"))))
                    } else if (UuidPath.addOrUpdateUuidPath(path.get, uuidStr.get)) {
                        Found ~> Location("/admin/uuidpaths")
                    } else {
                        BadRequest ~> Scalate(req, "400.ssp")
                    }
                }
                expected(params) orFail { fails =>
                    BadRequest ~> Scalate(req, "400.ssp", ("fails", fails))
                }
            }

        case req @ GET(Path("/admin/uuidpaths/delete") & Params(params)) =>
            if (!isAdmin(req)) {
                Unauthorized ~> Scalate(req, "401.ssp")
            } else {
                val expected = for {
                    id <- lookup("id") is int { "'id" + _ + "' 不是正整数" }
                } yield {
                    val err = (0, "记录成功删除")
                    UuidPath.deleteUuidPath(id.get)
                    Ok ~> ResponseString("{\"result\":\"" + err._1 + "\", \"message\":\"" + err._2 + "\"}")
                }
                expected(params) orFail { fails =>
                    BadRequest ~> Scalate(req, "400.ssp", ("fails", fails))
                }
            }
    }
}
