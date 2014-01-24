import AssemblyKeys._ 

assemblySettings

test in assembly := {}

net.virtualvoid.sbt.graph.Plugin.graphSettings

resolvers ++= Seq(
  "java m2" at "http://download.java.net/maven/2",
  "TypeSafe Repo" at "http://repo.typesafe.com/typesafe/releases",
  "alibaba" at "http://code.alibabatech.com/mvn/releases",
  Resolver.url("sbt-plugin-releases", 
  	new URL("http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases/"))(Resolver.ivyStylePatterns)
)

organization := "com.buzzinate"

EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Resource

EclipseKeys.withSource := true

name := "lezhi_recomm"

version := "0.1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  //////////////////////////////////////////////////////////////////////
  // Web server
  "io.netty" % "netty" % "3.6.6.Final",
  "net.databinder" % "unfiltered-filter_2.9.1" % "0.6.2",
  "net.databinder" % "unfiltered-jetty_2.9.1" % "0.6.2",
  "net.databinder" % "unfiltered-netty-server_2.9.1" % "0.6.2",
  "net.databinder" % "dispatch-nio_2.9.1" % "0.8.8",
  "net.databinder" % "dispatch-http_2.9.1" % "0.8.8",
  "net.databinder" % "unfiltered-uploads_2.9.1" % "0.6.2",
  "net.databinder" % "unfiltered-filter-uploads_2.9.1" % "0.6.2",
  "org.fusesource.scalate" % "scalate-core" % "1.5.3",
  //////////////////////////////////////////////////////////////////////
  // Utils
  "org.apache.httpcomponents" % "httpcore" % "4.2",
  "org.apache.httpcomponents" % "httpclient" % "4.2",
  "commons-lang" % "commons-lang" % "2.5",
  "commons-pool" % "commons-pool" % "1.6",
  "commons-codec" % "commons-codec" % "1.6",
  "commons-beanutils" % "commons-beanutils" % "1.8.3",
  "org.safehaus.jug" % "jug" % "2.0.0" classifier "lgpl",
  "com.jolbox" % "bonecp" % "0.8.0-rc1",
  "com.typesafe.akka" % "akka-actor" % "2.0.5",
  "org.jsoup" % "jsoup" % "1.7.2",
  "com.googlecode.juniversalchardet" % "juniversalchardet" % "1.0.3",
  "com.codahale" % "jerkson_2.9.1" % "0.5.0",
  "com.alibaba" % "fastjson" % "1.1.35",
  //////////////////////////////////////////////////////////////////////
  // Logging
  "org.slf4j" % "slf4j-api" % "1.7.5",
  "ch.qos.logback" % "logback-core" % "1.0.13",
  "ch.qos.logback" % "logback-classic" % "1.0.13",
  //////////////////////////////////////////////////////////////////////
  // Clients
  "redis.clients" % "jedis" % "2.1.0",
  "com.googlecode.xmemcached" % "xmemcached" % "1.4.2",
  "mysql" % "mysql-connector-java" % "5.1.25",
  "org.scalaquery" % "scalaquery_2.9.1-1" % "0.10.0-M1",
  //////////////////////////////////////////////////////////////////////
  // Test
  "org.scalatest" % "scalatest_2.9.3" % "1.9.1" % "test",
  "com.yammer.metrics" % "metrics-core" % "3.0.0-BETA1",
  "com.yammer.metrics" % "metrics-servlets" % "3.0.0-BETA1"
 )

excludedJars in assembly <<= (fullClasspath in assembly) map { cp => 
  cp filter { item => 
    item.data.getName == "servlet-api-2.5-20081211.jar"  ||
    item.data.getName == "uuid-3.2.0.jar" ||
    item.data.getName == "uuid-3.3.jar" ||
    item.data.getName == "spring-aop-3.0.7.RELEASE.jar" ||
    item.data.getName == "spring-context-3.0.7.RELEASE.jar" ||
    item.data.getName == "spring-jdbc-3.0.7.RELEASE.jar" ||
    item.data.getName == "spring-tx-3.0.7.RELEASE.jar" ||
    item.data.getName == "spring-core-3.0.7.RELEASE.jar" ||
    item.data.getName == "spring-orm-3.0.7.RELEASE.jar" ||
    item.data.getName == "spring-asm-3.0.7.RELEASE.jar" ||
    item.data.getName == "spring-expression-3.0.7.RELEASE.jar" ||
    item.data.getName == "hibernate-jpa-2.0-api-1.0.0.Final.jar" ||
    item.data.getName == "hibernate-commons-annotations-3.2.0.Final.jar" ||
    item.data.getName == "hibernate-core-3.6.3.Final.jar" ||
    item.data.getName == "jta-1.1.jar" ||
    item.data.getName == "netty-3.2.7.Final.jar" ||
    item.data.getName == "commons-beanutils-core-1.7.0.jar" ||
    item.data.getName == "commons-logging-api-1.0.4.jar" ||
    item.data.getName == "commons-digester-1.6.jar" ||
    item.data.getName == "slf4j-log4j12-1.6.1.jar" ||
    item.data.getName == "slf4j-log4j12-1.5.8.jar" ||
    item.data.getName == "log4j-1.2.16.jar" ||
    item.data.getName == "servlet-api-2.5-6.1.14.jar" ||
    item.data.getName == "jsp-2.1-6.1.14.jar" ||
    item.data.getName == "jruby-complete-1.6.5.jar" ||
    item.data.getName == "mockito-all-1.8.5.jar" ||
    item.data.getName == "commons-beanutils-core-1.8.0.jar" ||
    item.data.getName == "core-3.1.1.jar" ||
    item.data.getName == "zookeeper-3.4.5-cdh4.3.0.jar" ||
    item.data.getName == "mongo-java-driver-2.8.0.jar" ||
    item.data.getName == "jetty-6.1.26.cloudera.2.jar" ||
    item.data.getName == "jetty-util-6.1.26.cloudera.2.jar" ||
    item.data.getName == "jersey-test-framework-grizzly2-1.8.jar" ||
    item.data.getName == "jasper-compiler-5.5.23.jar" ||
    item.data.getName == "jasper-runtime-5.5.23.jar"
  }
}

mergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) =>
  {
    case PathList("org", "apache", "commons", "collections", xs @ _*) => MergeStrategy.first
    case x => old(x)
  }
}

scalaVersion := "2.9.3"
