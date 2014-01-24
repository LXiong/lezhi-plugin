
/*
CREATE KEYSPACE lezhi_plugin 
   WITH strategy_class = 'SimpleStrategy'
   AND strategy_options:replication_factor = 1;
*/

CREATE KEYSPACE lezhi_plugin 
   WITH strategy_class = 'NetworkTopologyStrategy' 
   AND strategy_options:DC1 = 3;

USE lezhi_plugin;

CREATE COLUMNFAMILY site_config (
   KEY uuid PRIMARY KEY,
   source text,
   pluginType text,
   position text,
   sitePrefix text,
   promot text,
   pic boolean,
   defaultPic text,
   row int,
   col int,
   htcolor text,
   rtcolor text,
   bdcolor text,
   hvcolor text)
   WITH read_repair_chance = 1.0;


CREATE COLUMNFAMILY page_info (
   KEY uuid PRIMARY KEY,
   url text,
   title text,
   siteUuid uuid,
   createTime timestamp)
   WITH read_repair_chance = 1.0;


CREATE COLUMNFAMILY page_counts (
   KEY uuid PRIMARY KEY,
   views counter,
   inClicks counter,
   outClicks counter)
   WITH default_validation=counter;
  

CREATE COLUMNFAMILY admin_counts (
   KEY timestamp PRIMARY KEY,
   views counter,
   inClicks counter,
   outClicks counter)
   WITH default_validation=counter;
  

CREATE COLUMNFAMILY site_page (KEY uuid PRIMARY KEY)
   WITH comparator=uuid;

CREATE COLUMNFAMILY stat_site_view(KEY uuid PRIMARY KEY)
   WITH comparator=timestamp AND default_validation=counter;

CREATE COLUMNFAMILY stat_site_in_click(KEY uuid PRIMARY KEY)
   WITH comparator=timestamp AND default_validation=counter;

CREATE COLUMNFAMILY stat_site_out_click(KEY uuid PRIMARY KEY)
   WITH comparator=timestamp AND default_validation=counter;


CREATE COLUMNFAMILY stat_page_view (KEY uuid PRIMARY KEY)
   WITH comparator=timestamp AND default_validation=counter;

CREATE COLUMNFAMILY stat_page_in_click (KEY uuid PRIMARY KEY)
   WITH comparator=timestamp AND default_validation=counter;

CREATE COLUMNFAMILY stat_page_out_click (KEY uuid PRIMARY KEY)
   WITH comparator=timestamp AND default_validation=counter;

ALTER COLUMNFAMILY site_config ADD fontSize int;

ALTER COLUMNFAMILY admin_counts ADD displays counter;

ALTER COLUMNFAMILY page_counts ADD displays counter;

CREATE COLUMNFAMILY stat_page_displays (KEY uuid PRIMARY KEY)
   WITH comparator=timestamp AND default_validation=counter;
   
ALTER COLUMNFAMILY site_config ADD promote text;
ALTER COLUMNFAMILY site_config ADD picSize int;


-- Update for 9.11

ALTER TABLE site_config WITH read_repair_chance = 1.0 AND caching = 'all'; 

-- Update for 10.10
use lezhi_plugin;

--add showup
CREATE COLUMNFAMILY stat_page_showup (KEY uuid PRIMARY KEY)
   WITH comparator=timestamp AND default_validation=counter;
CREATE COLUMNFAMILY stat_site_showup (KEY uuid PRIMARY KEY)
   WITH comparator=timestamp AND default_validation=counter;

ALTER COLUMNFAMILY admin_counts ADD showups counter;
ALTER COLUMNFAMILY page_counts ADD showups counter;

ALTER COLUMNFAMILY admin_counts ADD showups_fixed_i counter;
ALTER COLUMNFAMILY admin_counts ADD showups_slide_i counter;
ALTER COLUMNFAMILY admin_counts ADD showups_pic_i counter;
ALTER COLUMNFAMILY admin_counts ADD showups_nopic_i counter;

ALTER COLUMNFAMILY admin_counts ADD showups_fixed_t counter;
ALTER COLUMNFAMILY admin_counts ADD showups_slide_t counter;
ALTER COLUMNFAMILY admin_counts ADD showups_pic_t counter;
ALTER COLUMNFAMILY admin_counts ADD showups_nopic_t counter;

ALTER COLUMNFAMILY admin_counts ADD clicks_fixed_i counter;
ALTER COLUMNFAMILY admin_counts ADD clicks_slide_i counter;
ALTER COLUMNFAMILY admin_counts ADD clicks_pic_i counter;
ALTER COLUMNFAMILY admin_counts ADD clicks_nopic_i counter;

ALTER COLUMNFAMILY admin_counts ADD clicks_fixed_t counter;
ALTER COLUMNFAMILY admin_counts ADD clicks_slide_t counter;
ALTER COLUMNFAMILY admin_counts ADD clicks_pic_t counter;
ALTER COLUMNFAMILY admin_counts ADD clicks_nopic_t counter;

ALTER COLUMNFAMILY site_config ADD autoMatch boolean;
ALTER COLUMNFAMILY site_config ADD highlight boolean;
ALTER COLUMNFAMILY site_config ADD redirectMode text;

-- use cassandra-cli command to execute the following schema
CREATE COLUMN FAMILY page_statistic WITH column_type = 'Super' AND comparator = UTF8Type AND default_validation_class=UTF8Type;
use search;
CREATE COLUMN FAMILY blacklist WITH comparator = UTF8Type AND default_validation_class=UTF8Type;
use crawl;
CREATE COLUMN FAMILY uuid_urls WITH comparator = UTF8Type AND default_validation_class=UTF8Type;
CREATE COLUMN FAMILY site_status WITH comparator = UTF8Type AND default_validation_class=CounterColumnType;

-- use cassandra-cli command to execute the following schema

use lezhi_plugin;
CREATE COLUMN FAMILY site_blacklist WITH comparator = UTF8Type AND default_validation_class=UTF8Type;
CREATE COLUMN FAMILY ad_blacklist WITH comparator = UTF8Type AND default_validation_class=UTF8Type;
CREATE COLUMN FAMILY adsclose_reason WITH comparator = UTF8Type AND default_validation_class=UTF8Type;


ALTER COLUMNFAMILY site_config ADD adEnabled boolean;

ALTER COLUMNFAMILY site_config ADD adCount int;

