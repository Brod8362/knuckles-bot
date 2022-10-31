name := "knuckles"

version := "0.4"

scalaVersion := "2.13.2"

resolvers += "jcenter-bintray" at "https://jcenter.bintray.com"

libraryDependencies += "net.dv8tion" % "JDA" % "5.0.0-alpha.5"

cancelable in Global := true

assemblyMergeStrategy in assembly := {
  case "module-info.class" => MergeStrategy.first
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}