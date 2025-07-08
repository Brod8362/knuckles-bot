name := "knuckles"

version := "0.6"

scalaVersion := "2.13.2"

resolvers += "jcenter-bintray" at "https://jcenter.bintray.com"

libraryDependencies ++= Seq(
  "net.dv8tion" % "JDA" % "5.6.1",
  "com.lihaoyi" %% "requests" % "0.7.1",
  "com.lihaoyi" %% "upickle" % "2.0.0",
  "org.influxdb" % "influxdb-java" % "2.23",
  "org.scalatest" %% "scalatest" % "3.2.18" % "test"
)

cancelable in Global := true

assemblyMergeStrategy in assembly := {
  case "module-info.class" => MergeStrategy.first
  case "META-INF/versions/9/module-info.class" => MergeStrategy.first
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}