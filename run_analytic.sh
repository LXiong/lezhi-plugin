#/bin/bash

analytic_pid=`ps -ef |grep "AnalyticServer" |grep -v grep |grep java |awk '{print $2}'`
JAVA_OPTS="-Dfile.encoding=UTF-8 -Xms1024m -Xmx1024m -XX:NewSize=256M -XX:MaxNewSize=256M -XX:PermSize=128m -XX:MaxPermSize=128m"
#JAVA_OPTS=$JAVA_OPTS" -Xloggc:/var/log/analytic/analytics_gc.log -XX:+PrintGCDetails -XX:+PrintGC -XX:+PrintGCTimeStamps -XX:+PrintHeapAtGC -XX:+PrintTenuringDistribution"
#JAVA_OPTS=$JAVA_OPTS" -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/var/log/analytic/analytics<pid>.hprof"
#JAVA_OPTS=$JAVA_OPTS" -Dlogback.configurationFile=logging.xml"
JAVA_OPTS=$JAVA_OPTS" -Djava.awt.headless=true -XX:+UseConcMarkSweepGC -XX:+CMSClassUnloadingEnabled -XX:+DisableExplicitGC"


start_run () {
    java $JAVA_OPTS -DAPP_NAME=lezhiAnalytics -Droot.dir=`dirname $0`/ -cp `dirname $0`/target/lezhi_recomm-assembly-0.1.0-SNAPSHOT.jar com.buzzinate.lezhi.AnalyticServer >> /var/log/lezhi/lezhi_analytics.log 2>&1 &
}


case "$1" in
  start)
        start_run
        ;;
   stop)
        if [[ $analytic_pid == "" ]]; then
            echo "Analytic was stoped."
          else
            kill -9 $analytic_pid
        fi
        ;;
restart)
        kill -9 $analytic_pid
        start_run
        ;;
 status)
        if [[ $analytic_pid -gt 1 ]]; then
            echo "Analytic is running."
          else
            echo "Analytic is stop."
        fi
        ;;
  *)
        echo $"Usage: $0 {start|stop|restart}"
esac

