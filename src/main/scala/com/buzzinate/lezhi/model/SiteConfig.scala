package com.buzzinate.lezhi.model

import java.net.URLEncoder
import java.nio.ByteBuffer
import java.sql.Timestamp
import scala.annotation.serializable
import scala.collection.mutable.ListBuffer
import org.apache.commons.lang.StringUtils
import com.buzzinate.lezhi.db.JedisClient
import com.buzzinate.lezhi.db.MysqlClient.{ runLezhiQuery, runLezhiUpdate }
import com.buzzinate.lezhi.util.{ Logging, Util }
import java.util.UUID

/**
 * @author jeffrey created on 2013-06-06 下午8:15:33
 * for scalaquery doesn't a table more than 22 column,
 * so just use jdbc for site_config
 */
class SiteConfig(var uuid: String = UUID.randomUUID.toString,
    var source: String = "insite",
    var pluginType: String = "fixed",
    var pic: Boolean = true,
    var position: String = "right",
    var sitePrefix: String = "",
    var defaultPic: String = "http://lzstatic.bshare.cn/plugin/img/default.gif",
    var fontSize: Int = 12,
    var row: Int = 2,
    var col: Int = 5,
    var htcolor: String = "#333",
    var rtcolor: String = "#333",
    var bdcolor: String = "#dadada",
    var hvcolor: String = "#f5f4de",
    var promote: String = "您可能也喜欢",
    var picSize: Int = 88,
    var autoMatch: Boolean = false,
    var highlight: Boolean = false,
    var redirectMode: String = "js",
    var adEnabled: Boolean = false,
    var adCount: Int = 2,
    var width: Int = 0,
    var height: Int = 0,
    var titleBgColor: String = "#fff",
    var titleImage: String = "",
    var titleFontSize: Int = 12,
    var titleBold: Boolean = false,
    var fontBold: Boolean = false,
    var fontUnderline: Boolean = false,
    var linkUnderline: Boolean = false,
    var redirectType: String = "_blank",
    var picMatch: String = "provided",
    var bgColor: String = "#fff",
    var picBorderRadius: Boolean = true,
    var lineHeight: Int = 15,
    var listType: String = "disc",
    var positionY: String = "bottom",
    val gmtCreated: Timestamp = new Timestamp(System.currentTimeMillis)) extends Logging with Serializable {
    if (pic) hvcolor = SiteConfig.DEFAULT_HVCOLOR_PIC

    override def toString(): String = {
        val sb = new StringBuilder
        sb.append("uuid:" + uuid)
        sb.append(",source:" + source)
        sb.append(",pluginType:" + pluginType)
        sb.append(",pic:" + pic)
        sb.append(",position:" + position)
        sb.append(",sitePrefix:" + sitePrefix)
        sb.append(",defaultPic:" + defaultPic)
        sb.append(",fontSize:" + fontSize)
        sb.append(",row:" + row)
        sb.append(",col:" + col)
        sb.append(",htcolor:" + htcolor)
        sb.append(",rtcolor:" + rtcolor)
        sb.append(",bdcolor:" + bdcolor)
        sb.append(",hvcolor:" + hvcolor)
        sb.append(",promote:" + promote)
        sb.append(",picSize:" + picSize)
        sb.append(",autoMatch:" + autoMatch)
        sb.append(",highlight:" + highlight)
        sb.append(",redirectMode:" + redirectMode)
        sb.append(",adEnabled:" + adEnabled)
        sb.append(",adCount:" + adCount)
        sb.append(",width:" + width)
        sb.append(",height:" + height)
        sb.append(",titleBgColor:" + titleBgColor)
        sb.append(",titleImage:" + titleImage)
        sb.append(",titleFontSize:" + titleFontSize)
        sb.append(",titleBold:" + titleBold)
        sb.append(",fontBold:" + fontBold)
        sb.append(",fontUnderline:" + fontUnderline)
        sb.append(",linkUnderline:" + linkUnderline)
        sb.append(",redirectType:" + redirectType)
        sb.append(",picMatch:" + picMatch)
        sb.append(",bgColor:" + bgColor)
        sb.append(",picBorderRadius:" + picBorderRadius)
        sb.append(",lineHeight:" + lineHeight)
        sb.append(",listType:" + listType)
        sb.append(",positionY:" + positionY)
        sb.toString
    }
}

@serializable
object SiteConfig extends Logging {
    val DEFAULT_HVCOLOR_PIC = "#f5f4de"
    val DEFAULT_HVCOLOR_TEXT = "#333"
    val DEFAULT_AUTO_MATCH = false
    val DEFAULT_AD_COUNT = 2
    val DEFAULT_PIC = true
    val DEFAULT_PIC_MATCH = "provided"
    val DEFAULT_DEFAULT_PIC = "http://lzstatic.bshare.cn/plugin/img/default.gif"
    val DEFAULT_ROW = 2
    val DEFAULT_COL = 5
    val DEFAULT_REDIRECT_MODE = "js"
    val DEFAULT_AD_ENABLED = false
    val DEFAULT_POSITION = "right"
    val DEFAULT_TITLEBGCOLOR = "#fff"
    val DEFAULT_LIST_TYPE = "disc"
    val DEFAULT_POSITION_Y = "bottom"
    val DEFAULT_REDIRECT_TYPE = "_blank"
    val CACHE_PREFIX = "SC:"

    def update(sc: SiteConfig): Boolean = {
        try {
            if (sc.pic && sc.hvcolor == "#333") {
                sc.hvcolor = "#f5f4de"
            }
            val sql = """insert into site_config(uuid,source,plugin_type,position,site_prefix,pic,default_pic,row,col,htcolor,rtcolor,bdcolor,hvcolor,
                                                font_size,promote,pic_size,highlight,auto_match,redirect_mode,ad_enabled,ad_count,width,height,
                                                title_bg_color,title_image,title_font_size,title_bold,font_bold,font_underline,link_underline,
                                                redirect_type,pic_match,bg_color,pic_border_radius,line_height,list_type,position_y,gmt_created) 
                         values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) 
                         on duplicate key update
                            source=?,
                            plugin_type=?,
                            position=?,
                            site_prefix=?,
                            pic=?,
                            default_pic=?,
                            row=?,
                            col=?,
                            htcolor=?,
                            rtcolor=?,
                            bdcolor=?,
                            hvcolor=?,
                            font_size=?,
                            promote=?,
                            pic_size=?,
                            highlight=?,
                            auto_match=?,
                            redirect_mode=?,
                            ad_enabled=?,
                            ad_count=?,
                            width=?,
                            height=?,
                            title_bg_color=?,
                            title_image=?,
                            title_font_size=?,
                            title_bold=?,
                            font_bold=?,
                            font_underline=?,
                            link_underline=?,
                            redirect_type=?,
                            pic_match=?,
                            bg_color=?,
                            pic_border_radius=?,
                            line_height=?,
                            list_type=?,
                            position_y=?,
                            gmt_created=?"""

            runLezhiUpdate { conn =>
                val statement = conn.prepareStatement(sql)
                statement.setString(1, sc.uuid)
                statement.setString(2, sc.source)
                statement.setString(3, sc.pluginType)
                statement.setString(4, sc.position)
                statement.setString(5, sc.sitePrefix)
                statement.setBoolean(6, sc.pic)
                statement.setString(7, sc.defaultPic)
                statement.setInt(8, sc.row)
                statement.setInt(9, sc.col)
                statement.setString(10, sc.htcolor)
                statement.setString(11, sc.rtcolor)
                statement.setString(12, sc.bdcolor)
                statement.setString(13, sc.hvcolor)
                statement.setInt(14, sc.fontSize)
                statement.setString(15, sc.promote)
                statement.setInt(16, sc.picSize)
                statement.setBoolean(17, sc.highlight)
                statement.setBoolean(18, sc.autoMatch)
                statement.setString(19, sc.redirectMode)
                statement.setBoolean(20, sc.adEnabled)
                statement.setInt(21, sc.adCount)
                statement.setInt(22, sc.width)
                statement.setInt(23, sc.height)
                statement.setString(24, sc.titleBgColor)
                statement.setString(25, sc.titleImage)
                statement.setInt(26, sc.titleFontSize)
                statement.setBoolean(27, sc.titleBold)
                statement.setBoolean(28, sc.fontBold)
                statement.setBoolean(29, sc.fontUnderline)
                statement.setBoolean(30, sc.linkUnderline)
                statement.setString(31, sc.redirectType)
                statement.setString(32, sc.picMatch)
                statement.setString(33, sc.bgColor)
                statement.setBoolean(34, sc.picBorderRadius)
                statement.setInt(35, sc.lineHeight)
                statement.setString(36, sc.listType)
                statement.setString(37, sc.positionY)
                statement.setTimestamp(38, sc.gmtCreated)

                statement.setString(39, sc.source)
                statement.setString(40, sc.pluginType)
                statement.setString(41, sc.position)
                statement.setString(42, sc.sitePrefix)
                statement.setBoolean(43, sc.pic)
                statement.setString(44, sc.defaultPic)
                statement.setInt(45, sc.row)
                statement.setInt(46, sc.col)
                statement.setString(47, sc.htcolor)
                statement.setString(48, sc.rtcolor)
                statement.setString(49, sc.bdcolor)
                statement.setString(50, sc.hvcolor)
                statement.setInt(51, sc.fontSize)
                statement.setString(52, sc.promote)
                statement.setInt(53, sc.picSize)
                statement.setBoolean(54, sc.highlight)
                statement.setBoolean(55, sc.autoMatch)
                statement.setString(56, sc.redirectMode)
                statement.setBoolean(57, sc.adEnabled)
                statement.setInt(58, sc.adCount)
                statement.setInt(59, sc.width)
                statement.setInt(60, sc.height)
                statement.setString(61, sc.titleBgColor)
                statement.setString(62, sc.titleImage)
                statement.setInt(63, sc.titleFontSize)
                statement.setBoolean(64, sc.titleBold)
                statement.setBoolean(65, sc.fontBold)
                statement.setBoolean(66, sc.fontUnderline)
                statement.setBoolean(67, sc.linkUnderline)
                statement.setString(68, sc.redirectType)
                statement.setString(69, sc.picMatch)
                statement.setString(70, sc.bgColor)
                statement.setBoolean(71, sc.picBorderRadius)
                statement.setInt(72, sc.lineHeight)
                statement.setString(73, sc.listType)
                statement.setString(74, sc.positionY)
                statement.setTimestamp(75, sc.gmtCreated)

                statement.executeUpdate
                JedisClient.set(CACHE_PREFIX + sc.uuid, 3600, Some(sc))
                true
            }
        } catch {
            case ex: Exception =>
                log.error(ex, "Failed to save SiteConfig, [SiteConfig: " + sc + "]");
                false
        }
    }

    def getSiteConfigs(uuids: Seq[String]): scala.collection.mutable.Map[String, SiteConfig] = {
        var siteConfigs = scala.collection.mutable.Map[String, SiteConfig]()
        uuids.foreach { uuid => 
            val siteConfig = get(uuid)
            siteConfigs(uuid) = siteConfig.getOrElse(null)
        }
        siteConfigs
    }
    
    def get(uuid: String): Option[SiteConfig] = {
        try {
            val key = CACHE_PREFIX + uuid
            var res = JedisClient.getObject(key).asInstanceOf[Option[SiteConfig]]
            if (res == null) {
                res = doGet(uuid)
                JedisClient.set(key, 3600, res)
            }
            res
        } catch {
            case ex: Exception =>
                log.error(ex, "Failed to get site_config, [uuid: " + uuid.toString + "]");
                None
        }
    }

    def doGet(uuid: String): Option[SiteConfig] = {
        var sc: Option[SiteConfig] = None
        runLezhiQuery { conn =>
            val sql = "select * from site_config where uuid = ? and is_deleted = false"
            val statement = conn.prepareStatement(sql)
            statement.setString(1, uuid)
            val resultSet = statement.executeQuery
            sc = if (resultSet.next) {
                val siteConfig = new SiteConfig(uuid)
                siteConfig.source = resultSet.getString("source")
                siteConfig.pluginType = resultSet.getString("plugin_type")
                siteConfig.position = resultSet.getString("position")
                siteConfig.sitePrefix = resultSet.getString("site_prefix")
                siteConfig.pic = resultSet.getBoolean("pic")
                siteConfig.defaultPic = resultSet.getString("default_pic")
                siteConfig.row = resultSet.getInt("row")
                siteConfig.col = resultSet.getInt("col")
                siteConfig.htcolor = resultSet.getString("htcolor")
                siteConfig.rtcolor = resultSet.getString("rtcolor")
                siteConfig.bdcolor = resultSet.getString("bdcolor")
                siteConfig.hvcolor = resultSet.getString("hvcolor")
                siteConfig.fontSize = resultSet.getInt("font_size")
                siteConfig.promote = resultSet.getString("promote")
                siteConfig.picSize = resultSet.getInt("pic_size")
                siteConfig.highlight = resultSet.getBoolean("highlight")
                siteConfig.autoMatch = resultSet.getBoolean("auto_match")
                siteConfig.redirectMode = resultSet.getString("redirect_mode")
                siteConfig.adEnabled = resultSet.getBoolean("ad_enabled")
                siteConfig.adCount = resultSet.getInt("ad_count")
                siteConfig.width = resultSet.getInt("width")
                siteConfig.height = resultSet.getInt("height")
                siteConfig.titleBgColor = resultSet.getString("title_bg_color")
                siteConfig.titleImage = resultSet.getString("title_image")
                siteConfig.titleFontSize = resultSet.getInt("title_font_size")
                siteConfig.titleBold = resultSet.getBoolean("title_bold")
                siteConfig.fontBold = resultSet.getBoolean("font_bold")
                siteConfig.fontUnderline = resultSet.getBoolean("font_underline")
                siteConfig.linkUnderline = resultSet.getBoolean("link_underline")
                siteConfig.redirectType = resultSet.getString("redirect_type")
                siteConfig.picMatch = resultSet.getString("pic_match")
                siteConfig.bgColor = resultSet.getString("bg_color")
                siteConfig.picBorderRadius = resultSet.getBoolean("pic_border_radius")
                siteConfig.lineHeight = resultSet.getInt("line_height")
                siteConfig.listType = resultSet.getString("list_type")
                siteConfig.positionY = resultSet.getString("position_y")
                Some(siteConfig)
            } else {
                None
            }
        }
        sc
    }

    def getAllUuids(): Seq[String] = {
        val res = new ListBuffer[String]()
        try {
            var sql = "select uuid from site_config where is_deleted = false"
            runLezhiQuery { conn =>
                val statement = conn.prepareStatement(sql)
                val resultSet = statement.executeQuery
                while (resultSet.next) {
                    res += (resultSet.getString("uuid"))
                }
            }
            res
        } catch {
            case ex: Exception =>
                log.error(ex, "Failed to get all the uuids");
                Nil
        }
    }

    def getAllTypeUuids(): (Seq[String], Seq[String], Seq[String], Seq[String]) = {
        var pluginUuids = ListBuffer[String]()
        var buttonUuids = ListBuffer[String]()
        var picUuids = ListBuffer[String]()
        var textUuids = ListBuffer[String]()
        try {
            var sql = "select uuid,pic,plugin_type from site_config where is_deleted = false"
            runLezhiQuery { conn =>
                val statement = conn.prepareStatement(sql)
                val resultSet = statement.executeQuery
                while (resultSet.next) {
                    val uuid = resultSet.getString("uuid")
                    val pic = resultSet.getBoolean("pic")
                    val pluginType = resultSet.getString("plugin_type")
                    if (pluginType == "fixed")
                        pluginUuids += uuid
                    else
                        buttonUuids += uuid
                    if (pic)
                        picUuids += uuid
                    else
                        textUuids += uuid
                }
            }
        } catch {
            case ex: Exception =>
                log.error(ex, "Failed to get all type uuid")
        }
        (pluginUuids, buttonUuids, picUuids, textUuids)
    }

    def count(): Int = {
        var res: Int = 0
        try {
            val sql = "select count(uuid) as num from site_config where is_deleted = false"
            runLezhiQuery { conn =>
                val statement = conn.prepareStatement(sql)
                val resultSet = statement.executeQuery
                if (resultSet.next) {
                    res = resultSet.getInt("num")
                }
            }
            res
        } catch {
            case ex: Exception =>
                log.error(ex, "Failed to get the count of sites");
                0
        }
    }

    def delete(uuid: String): Boolean = {
        try {
            val sql = "update site_config set is_deleted = true where uuid=?"
            runLezhiUpdate { conn =>
                val statement = conn.prepareStatement(sql)
                statement.setString(1, uuid)
                statement.executeUpdate
                JedisClient.delete(CACHE_PREFIX + uuid.toString)
                true
            }
        } catch {
            case ex: Exception =>
                log.error(ex, "Failed to get site_config, [uuid: " + uuid.toString + "]");
                false
        }
    }

    def getConfigObj(c: SiteConfig) = {
        val configDocs = scala.collection.mutable.Map[String, Any](
            "uuid" -> c.uuid,
            "source" -> c.source,
            "type" -> c.pluginType,
            "pic" -> c.pic,
            "sitePrefix" -> c.sitePrefix,
            "fontSize" -> c.fontSize,
            "defaultPic" -> c.defaultPic,
            "col" -> c.col,
            "row" -> c.row,
            "htcolor" -> c.htcolor,
            "rtcolor" -> c.rtcolor,
            "bdcolor" -> c.bdcolor,
            "hvcolor" -> c.hvcolor,
            "picSize" -> c.picSize,
            "position" -> c.position,
            "promote" -> c.promote,
            "highlight" -> c.highlight,
            "redirectMode" -> c.redirectMode,
            "width" -> c.width,
            "height" -> c.height,
            "titleBgColor" -> c.titleBgColor,
            "titleImage" -> c.titleImage,
            "titleFontSize" -> c.titleFontSize,
            "titleBold" -> c.titleBold,
            "fontBold" -> c.fontBold,
            "fontUnderline" -> c.fontUnderline,
            "linkUnderline" -> c.linkUnderline,
            "redirectType" -> c.redirectType,
            "picMatch" -> c.picMatch,
            "bgcolor" -> c.bgColor,
            "picBorderRadius" -> c.picBorderRadius,
            "lineHeight" -> c.lineHeight,
            "listType" -> c.listType,
            "positionY" -> c.positionY,
            "adEnabled" -> c.adEnabled)
        configDocs
    }

    /**
     * Convert the configurations to list of tuples
     */
    def toParamTuples(sc: SiteConfig) = {
        getParams(sc) ++ getColor(sc)
    }

    def toEncodedParamTuples(sc: SiteConfig) = {
        getParams(sc) ++ getEncodeColor(sc)
    }

    def getParams(sc: SiteConfig) = {
        ("pic", sc.pic) ::
            ("pluginType", sc.pluginType) ::
            ("source", sc.source) ::
            ("position", sc.position) ::
            ("defaultPic", sc.defaultPic) ::
            ("fontSize", sc.fontSize) ::
            ("row", sc.row) ::
            ("col", sc.col) ::
            ("picSize", sc.picSize) ::
            ("autoMatch", sc.autoMatch) ::
            ("highlight", sc.highlight) ::
            ("redirectMode", sc.redirectMode) ::
            ("adEnabled", sc.adEnabled) ::
            ("adCount", if (sc.adEnabled) sc.adCount else 0) ::
            ("width", sc.width) ::
            ("height", sc.height) ::
            ("titleImage", sc.titleImage) ::
            ("titleFontSize", sc.titleFontSize) ::
            ("titleBold", sc.titleBold) ::
            ("fontBold", sc.fontBold) ::
            ("fontUnderline", sc.fontUnderline) ::
            ("linkUnderline", sc.linkUnderline) ::
            ("redirectType", sc.redirectType) ::
            ("picMatch", sc.picMatch) ::
            ("picBorderRadius", sc.picBorderRadius) ::
            ("lineHeight", sc.lineHeight) ::
            ("listType", sc.listType) ::
            ("positionY", sc.positionY) :: Nil
    }

    private def getColor(sc: SiteConfig) = {
        ("promote", sc.promote) ::
            ("htcolor", sc.htcolor) ::
            ("rtcolor", sc.rtcolor) ::
            ("bdcolor", sc.bdcolor) ::
            ("hvcolor", sc.hvcolor) ::
            ("titleBgColor", sc.titleBgColor) ::
            ("bgcolor", sc.bgColor) :: Nil
    }

    private def getEncodeColor(sc: SiteConfig) = {
        ("promote", URLEncoder.encode(sc.promote, "UTF-8")) ::
            ("htcolor", URLEncoder.encode(sc.htcolor, "UTF-8")) ::
            ("rtcolor", URLEncoder.encode(sc.rtcolor, "UTF-8")) ::
            ("bdcolor", URLEncoder.encode(sc.bdcolor, "UTF-8")) ::
            ("hvcolor", URLEncoder.encode(sc.hvcolor, "UTF-8")) ::
            ("titleBgColor", URLEncoder.encode(sc.titleBgColor, "UTF-8")) ::
            ("bgcolor", URLEncoder.encode(sc.bgColor, "UTF-8")) :: Nil
    }

    def getSiteConfig(uuidStr: Option[String]): SiteConfig = {
        if (StringUtils.isNotBlank(uuidStr.get) && uuidStr.get != "new" && uuidStr.get != "nologin") {
            try {
                val config = SiteConfig.get(uuidStr.get)
                if (config.isDefined) {
                    config.get
                } else {
                    val sf = new SiteConfig
                    sf.uuid = uuidStr.get
                    sf
                }
            } catch {
                case ex: IllegalArgumentException => new SiteConfig
            }
        } else {
            new SiteConfig
        }
    }

    import com.buzzinate.lezhi.util.ValidateUtil._

    def getSiteConfig(siteConfig: SiteConfig, pluginType: Option[String], row: Option[Int], col: Option[Int], pic: Option[String],
        fontSize: Option[Int], source: Option[String], defaultPic: Option[String], position: Option[String],
        htcolor: Option[String], rtcolor: Option[String], bdcolor: Option[String], hvcolor: Option[String],
        promote: Option[String], picSize: Option[Int], autoMatch: Option[String], highlight: Option[String], redirectMode: Option[String],
        width: Option[Int], height: Option[Int], titleBgColor: Option[String], titleImage: Option[String], titleFontSize: Option[Int],
        titleBold: Option[String], fontBold: Option[String], fontUnderline: Option[String], linkUnderline: Option[String], redirectType: Option[String], picMatch: Option[String],
        bgcolor: Option[String], picBorderRadius: Option[String], lineHeight: Option[Int], listType: Option[String], positionY: Option[String]): SiteConfig = {

        if (pluginType.isDefined)
            siteConfig.pluginType = pluginType.map(escapeContent).get
        if (row.isDefined)
            siteConfig.row = row.get
        if (col.isDefined)
            siteConfig.col = col.get
        if (pic.isDefined)
            siteConfig.pic = pic.map(_.toBoolean).get
        if (fontSize.isDefined)
            siteConfig.fontSize = fontSize.get
        if (source.isDefined)
            siteConfig.source = source.map(escapeContent).get
        if (defaultPic.isDefined)
            siteConfig.defaultPic = defaultPic.get
        if (position.isDefined)
            siteConfig.position = position.map(escapeContent).get
        if (htcolor.isDefined)
            siteConfig.htcolor = htcolor.get
        if (rtcolor.isDefined)
            siteConfig.rtcolor = rtcolor.get
        if (bdcolor.isDefined)
            siteConfig.bdcolor = bdcolor.get
        if (hvcolor.isDefined)
            siteConfig.hvcolor = hvcolor.get
        if (promote.isDefined)
            siteConfig.promote = if (promote.map(escapeContent).get.length > 8) promote.map(escapeContent).get.substring(0, 8) else promote.map(escapeContent).get
        if (picSize.isDefined)
            siteConfig.picSize = picSize.get
        if (autoMatch.isDefined)
            siteConfig.autoMatch = autoMatch.map(_.toBoolean).get
        if (highlight.isDefined)
            siteConfig.highlight = highlight.map(_.toBoolean).get
        if (redirectMode.isDefined)
            siteConfig.redirectMode = redirectMode.get

        if (width.isDefined)
            siteConfig.width = width.get
        if (height.isDefined)
            siteConfig.height = height.get
        if (titleBgColor.isDefined)
            siteConfig.titleBgColor = titleBgColor.get
        if (titleImage.isDefined)
            siteConfig.titleImage = titleImage.get
        if (titleFontSize.isDefined)
            siteConfig.titleFontSize = titleFontSize.get
        if (titleBold.isDefined)
            siteConfig.titleBold = titleBold.map(_.toBoolean).get
        if (fontBold.isDefined)
            siteConfig.fontBold = fontBold.map(_.toBoolean).get
        if (fontUnderline.isDefined)
            siteConfig.fontUnderline = fontUnderline.map(_.toBoolean).get
        if (linkUnderline.isDefined)
            siteConfig.linkUnderline = linkUnderline.map(_.toBoolean).get
        if (redirectType.isDefined)
            siteConfig.redirectType = redirectType.get
        if (picMatch.isDefined)
            siteConfig.picMatch = picMatch.get
        if (bgcolor.isDefined)
            siteConfig.bgColor = bgcolor.get
        if (picBorderRadius.isDefined)
            siteConfig.picBorderRadius = picBorderRadius.map(_.toBoolean).get
        if (lineHeight.isDefined)
            siteConfig.lineHeight = lineHeight.get
        if (listType.isDefined)
            siteConfig.listType = listType.get
        if (positionY.isDefined)
            siteConfig.positionY = positionY.get

        siteConfig
    }
}

