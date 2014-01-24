#!/bin/bash

HOSTNAME="10.40.6.91"
PORT="3306"
USERNAME="root"
PASSWORD=""

DBNAME="lezhi_plugin"
TABLENAME="lezhi_stats"

uniqueIpNum=`cat access_lzplugin.bshare.cn.log  | cut -d " " -f 1 | sort | uniq  | wc -l`
update_sql="insert into ${TABLENAME}(stat_day,unique_ip) values(date_sub(curdate(),interval 1 day),uniqueIpNum) on duplicate key update unique_ip = unique_ip + ${uniqueIpNum}"
mysql -h${HOSTNAME}  -P${PORT}  -u${USERNAME} -p${PASSWORD} -e "${update_sql}"  

uv=`grep -E "userid=[0-9A-Za-z]{20}" -o lezhi-api.log  | uniq | wc -l`
update_sql="update ${TABLENAME} set uv = uv + $uv where stat_day=date_sub(curdate(),interval 1 day)"
mysql -h${HOSTNAME}  -P${PORT}  -u${USERNAME} -p${PASSWORD} -e "${update_sql}"  

