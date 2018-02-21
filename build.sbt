inThisBuild(Seq(
  name := "Rorqual",
  organization := "com.dafttech",
  version := "0.0.0",

  scalaVersion := "2.12.4",

  resolvers ++= Seq(
    "lolhens-maven" at "http://artifactory.lolhens.de/artifactory/maven-public/",
    Resolver.url("lolhens-ivy", url("http://artifactory.lolhens.de/artifactory/ivy-public/"))(Resolver.ivyStylePatterns)
  ),

  scalacOptions ++= Seq("-Xmax-classfile-name", "127"),

  mainClass in Compile := None
))

name := (name in ThisBuild).value

lazy val settings = Seq(
  addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full),
  addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.6")
)

lazy val root = project.in(file("."))
  .settings(publishArtifact := false)
  .aggregate(core, iscsi, fileBackend, deviceBackend, fuseFrontend, partition)

lazy val core = project.in(file("core"))
  .enablePlugins(
    JavaAppPackaging,
    UniversalPlugin)
  .settings(
    libraryDependencies ++= Seq(
      "org.slf4j" % "slf4j-api" % "1.7.25",
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2",
      "org.typelevel" %% "cats-core" % "1.0.1",
      "io.monix" %% "monix" % "3.0.0-M3",
      "com.typesafe.akka" %% "akka-actor" % "2.5.9",
      "org.scodec" %% "scodec-bits" % "1.1.5",
      "eu.timepit" %% "refined" % "0.8.7",
      "org.lolhens" %% "ifoption" % "0.2.1",
      "org.xerial.larray" %% "larray" % "0.4.0"
    )
  )
  .settings(settings: _*)

lazy val iscsi = project.in(file("iscsi"))
  .enablePlugins(
    JavaAppPackaging,
    UniversalPlugin)
  .settings(settings: _*)
  .settings(libraryDependencies ++= Seq(
  ))
  .dependsOn(core)

lazy val fileBackend = project.in(file("file"))
  .settings(settings: _*)
  .dependsOn(core)

lazy val os = project.in(file("os"))
  .settings(
    libraryDependencies ++= Seq(
      "io.monix" %% "monix" % "3.0.0-M3",
      "org.lolhens" %% "ifoption" % "0.1.2"
    )
  )
  .settings(settings: _*)
  .settings(
    libraryDependencies ++= Seq("org.apache.commons" % "commons-lang3" % "3.5")
  )
  .dependsOn(core)

lazy val deviceBackend = project.in(file("device"))
  .settings(settings: _*)
  .dependsOn(fileBackend, os)

lazy val fuseFrontend = project.in(file("fuse"))
  .enablePlugins(
    JavaAppPackaging,
    UniversalPlugin)
  .settings(settings: _*)
  .dependsOn(core)

lazy val partition = project.in(file("partition"))
  .settings(settings: _*)
  .dependsOn(core, deviceBackend)
