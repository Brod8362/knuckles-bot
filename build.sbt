name := "knuckles"

version := "0.5"

scalaVersion := "2.13.2"

resolvers += "jcenter-bintray" at "https://jcenter.bintray.com"

libraryDependencies ++= Seq(
  "net.dv8tion" % "JDA" % "5.0.0-alpha.22",
  "com.lihaoyi" %% "requests" % "0.7.1",
  "com.lihaoyi" %% "upickle" % "2.0.0"
)

cancelable in Global := true

assemblyMergeStrategy in assembly := {
  case "module-info.class" => MergeStrategy.first
  case "META-INF/versions/9/module-info.class" => MergeStrategy.first
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}