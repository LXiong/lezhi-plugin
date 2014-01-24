create database lezhi_plugin;

use lezhi_plugin;

CREATE table IF NOT EXISTS site_config(
    uuid char(36) PRIMARY KEY,
    source varchar(20) not null  COMMENT '标签类型，常用值为猜你喜欢和最热文章，对应值为insite和trending，还有personalized和itemcf',
    plugin_type varchar(10) not null COMMENT '插件类型，常用值为插件和按钮，对应值为fixed和button',
    position varchar(10),
    site_prefix varchar(128) COMMENT '网站前缀',
    pic boolean COMMENT '插件样式，分为图文和文字',
    default_pic varchar(512) COMMENT '默认图片，当设为图文样式时，推荐内容无匹配图片时，设置该值',
    row integer not null COMMENT '推荐框行数',
    col integer not null COMMENT '推荐框列数',
    htcolor varchar(11) COMMENT 'head的字体颜色',
    rtcolor varchar(11) COMMENT '推荐的文字字体颜色',
    bdcolor varchar(11) COMMENT '插件窗体的框线颜色',
    hvcolor varchar(11) COMMENT '悬浮正文的字体颜色',
    font_size integer not null COMMENT '字体大小',
    promote varchar(8) not null COMMENT '页签显示文字',
    pic_size integer COMMENT '图片大小',
    highlight boolean COMMENT '文章标题和图片加红高亮显示',
    auto_match boolean COMMENT '自动匹配图片',
    redirect_mode varchar(10) COMMENT '跳转模式',
    ad_enabled boolean COMMENT '是否开通广告',
    ad_count integer COMMENT '广告条数',
    width integer not null COMMENT '插件宽度',
    height integer not null COMMENT '插件高度',
    title_bg_color varchar(11) COMMENT '页签的背景颜色',
    title_image varchar(128) COMMENT '页签的背景图片地址',
    title_font_size integer COMMENT '页签的字体大小',
    title_bold boolean COMMENT '页签的字体是否加粗',
    font_bold boolean COMMENT '推荐文章字体是否加粗',
    font_underline boolean COMMENT '推荐文章字体是否加下划线',
    link_underline boolean COMMENT '推荐文章字体鼠标放上时是否加下划线',
    redirect_type varchar(10) COMMENT '推荐文章跳转模式',
    pic_match varchar(10) COMMENT '图片匹配',
    bg_color varchar(11) COMMENT '插件窗体的背景颜色',
    pic_border_radius boolean COMMENT '插件图片的边框是否为圆角',
    line_height integer COMMENT '插件正文字体的行高',
    list_type varchar(10) COMMENT '插件正文的list type',
    position_y varchar(10) COMMENT '插件y轴坐标',
    gmt_created datetime
)ENGINE=InnoDB  DEFAULT CHARSET=utf8;

CREATE table IF NOT EXISTS page_info(
    url_uuid char(36) primary key,
    url  varchar(250) ,
    site_uuid char(36),
    title varchar(512),
    gmt_created datetime
)ENGINE=InnoDB  DEFAULT CHARSET=utf8;

CREATE table IF NOT EXISTS site_page(
    url_uuid char(36) PRIMARY KEY,
    url  varchar(250),
    uuid char(36),
    gmt_created datetime
)ENGINE=InnoDB  DEFAULT CHARSET=utf8;

CREATE table IF NOT EXISTS exchange_link(
    url  varchar(250) PRIMARY KEY,
    `order` integer not null,
    title varchar(20) not null,
    gmt_created datetime
)ENGINE=InnoDB  DEFAULT CHARSET=utf8;

CREATE table IF NOT EXISTS page_statistic(
    id integer primary key auto_increment,
    key_type varchar(30),
    url  varchar(250),
    title varchar(128),
    showup integer,
    in_click integer,
    out_click integer,
    click_to_showup decimal(10,3), 
    gmt_created datetime,
    key key_type(key_type)
)ENGINE=InnoDB  DEFAULT CHARSET=utf8;

CREATE table IF NOT EXISTS ad_close_reason(
    id integer primary key auto_increment,
    uuid char(36) ,
    reason varchar(100),
    gmt_created datetime,
    key uuid_key (uuid)
)ENGINE=InnoDB  DEFAULT CHARSET=utf8;

CREATE table IF NOT EXISTS ad_black_list(
    id integer primary key auto_increment,
    uuid char(36) ,
    keyword varchar(100),
    gmt_created datetime,
    key uuid_key (uuid)
)ENGINE=InnoDB  DEFAULT CHARSET=utf8;

CREATE table IF NOT EXISTS uuid_urls(
    uuid char(36),
    url  varchar(250),
    url_uuid char(36), 
    status varchar(10),
    gmt_created datetime,
    primary key (uuid,url_uuid)
)ENGINE=InnoDB  DEFAULT CHARSET=utf8;

CREATE table IF NOT EXISTS site_status(
    uuid char(36) primary key,
    url_number  integer,
    gmt_created datetime
)ENGINE=InnoDB  DEFAULT CHARSET=utf8;

CREATE table IF NOT EXISTS admin_counts(
    admin_day date primary key,
    views bigint COMMENT 'PV',
    in_clicks bigint COMMENT '内点数',
    out_clicks bigint COMMENT '外点数',
    showups bigint COMMENT '展示数',
    showups_fixed_i bigint COMMENT '插件样式猜你喜欢展示数',
    showups_slide_i bigint COMMENT '按钮样式猜你喜欢展示数',
    showups_pic_i bigint COMMENT '图文样式猜你喜欢展示数',
    showups_nopic_i bigint COMMENT '文本样式猜你喜欢展示数',

    showups_fixed_t bigint COMMENT '插件样式最热文章展示数',
    showups_slide_t bigint COMMENT '按钮样式最热文章展示数',
    showups_pic_t bigint COMMENT '图文样式最热文章展示数',
    showups_nopic_t bigint COMMENT '文本样式最热文章展示数',

    clicks_fixed_i bigint COMMENT '插件样式猜你喜欢点击数',
    clicks_slide_i bigint COMMENT '按钮样式猜你喜欢点击数',
    clicks_pic_i bigint COMMENT '图文样式猜你喜欢点击数',
    clicks_nopic_i bigint COMMENT '文本样式猜你喜欢点击数',

    clicks_fixed_t bigint COMMENT '插件样式最热文章点击数',
    clicks_slide_t bigint COMMENT '按钮样式最热文章点击数',
    clicks_pic_t bigint COMMENT '图文样式最热文章点击数',
    clicks_nopic_t bigint COMMENT '文本样式最热文章点击数'
)ENGINE=InnoDB  DEFAULT CHARSET=utf8;

CREATE table IF NOT EXISTS stat_site(
    uuid char(36),
    site_day date, 
    in_clicks bigint COMMENT '内点数',
    out_clicks bigint COMMENT '外点数',
    showups bigint COMMENT '展示数',
    primary key (uuid,site_day)
)ENGINE=InnoDB  DEFAULT CHARSET=utf8;

CREATE table IF NOT EXISTS stat_page(
	`ID` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
    `URL_HASH` binary(16) NOT NULL COMMENT 'URL HASH',
    `UUID` binary(16) COMMENT 'UUID',
    `PAGE_DAY` date NOT NULL, 
    `SHOWUPS` integer NOT NULL DEFAULT 0 COMMENT '展示数',
    `IN_CLICKS` integer NOT NULL DEFAULT 0 COMMENT '内点数',
    `OUT_CLICKS` integer NOT NULL DEFAULT 0 COMMENT '外点数',
    PRIMARY KEY (`ID`),
    UNIQUE KEY `URL_HASH_IDX` (`URL_HASH`, `PAGE_DAY`),
    KEY `UUID_HASH_IDX` (`UUID`, `PAGE_DAY`)
)ENGINE=InnoDB  DEFAULT CHARSET=utf8;

-- uuid <=> path
CREATE TABLE IF NOT EXISTS `uuid_path` (
	`ID` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
	`UUID` binary(16) NOT NULL COMMENT 'UUID HASH',
	`PATH_HASH` binary(16) NOT NULL COMMENT 'PATH HASH',
	`PATH` varchar(1024) NOT NULL COMMENT 'PATH',
	`STATUS` tinyint(1) NOT NULL DEFAULT '0' COMMENT '状态 0：未验证/1：已验证',
	`UPDATE_TIME` datetime NOT NULL COMMENT '记录更新时间',
	PRIMARY KEY (`ID`),
	KEY `PATH_HASH_IDX` (`PATH_HASH`),
	KEY `UUID_IDX` (`UUID`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8;

-- lezhi analytic
CREATE TABLE IF NOT EXISTS `lezhi_stats` (
    stat_day date primary key,
    uv bigint default 0 COMMENT 'uv',
    unique_ip bigint default 0 COMMENT '独立ip数'
) ENGINE=InnoDB  DEFAULT CHARSET=utf8;

--
alter table site_config add column `is_deleted` boolean default false comment '是否逻辑删除';