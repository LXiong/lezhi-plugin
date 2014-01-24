#/bin/bash

lezhi_pid=`ps -ef |grep "lezhi.WebsiteServer" |grep -v grep |grep java |awk '{print $2}'`
JAVA_OPTS="-Dfile.encoding=UTF-8 -Xms512m -Xmx512m -XX:NewSize=192M -XX:MaxNewSize=192M -XX:PermSize=128m -XX:MaxPermSize=128m"
#JAVA_OPTS=$JAVA_OPTS" -Xloggc:/var/log/lezhi/plugin_gc.log -XX:+PrintGCDetails -XX:+PrintGC -XX:+PrintGCTimeStamps -XX:+PrintHeapAtGC -XX:+PrintTenuringDistribution"
#JAVA_OPTS=$JAVA_OPTS" -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/var/log/lezhi/lezhi_plugin<pid>.hprof"
JAVA_OPTS=$JAVA_OPTS" -Djava.awt.headless=true -XX:+UseConcMarkSweepGC -XX:+CMSClassUnloadingEnabled -XX:+DisableExplicitGC"

start_run () {
    java $JAVA_OPTS -DAPP_NAME=lezhiWebsite -Droot.dir=`dirname $0`/ -cp `dirname $0`/target/lezhi_recomm-assembly-0.1.0-SNAPSHOT.jar com.buzzinate.lezhi.WebsiteServer >> /var/log/lezhi/lezhi_website.log 2>&1 &
 }


case "$1" in
  start)
        start_run
        ;;
   stop)
        if [[ $lezhi_pid == "" ]]; then
            echo "Lezhi was stoped."
          else
            kill -9 $lezhi_pid
        fi
        ;;
restart)
        kill -9 $lezhi_pid
        start_run  
        ;;
 status)
        if [[ $lezhi_pid -gt 1 ]]; then
            echo "Lezhi is running."
          else
            echo "Lezhi is stop."
        fi
        ;;
  *)
        echo $"Usage: $0 {start|stop|restart}"
esac
