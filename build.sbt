inThisBuild(Seq(
  name := "Rorqual",
  organization := "com.dafttech",
  version := "0.0.0",

  scalaVersion := "2.12.2",

  externalResolvers := Seq("artifactory" at "http://lolhens.no-ip.org/artifactory/maven-public/"),

  dependencyUpdatesExclusions := moduleFilter(organization = "org.scala-lang"),

  scalacOptions ++= Seq("-Xmax-classfile-name", "254")
))

lazy val settings = Seq(
  addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full),
  addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.3"),

  mainClass in Compile := None
)

lazy val root = project.in(file("."))
  .settings(publishArtifact := false)
  .aggregate(core, iscsi, fileBackend, deviceBackend, fuseFrontend)

lazy val core = project.in(file("core"))
  .enablePlugins(
    JavaAppPackaging,
    UniversalPlugin)
  .settings(
    libraryDependencies ++= Seq(
      //"org.scala-lang" % "scala-reflect" % "2.12.2",
      //"org.scala-lang.modules" %% "scala-java8-compat" % "0.8.0",
      "org.slf4j" % "slf4j-api" % "1.7.25",
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
      "org.typelevel" %% "cats" % "0.9.0",
      //"com.chuusai" %% "shapeless" % "2.3.2",
      //"com.github.mpilquist" %% "simulacrum" % "0.10.0",
      "io.monix" %% "monix" % "2.2.4",
      "io.monix" %% "monix-cats" % "2.2.4",
      //"org.atnos" %% "eff" % "4.3.0",
      "com.typesafe.akka" %% "akka-actor" % "2.5.0",
      //"com.typesafe.akka" %% "akka-remote" % "2.5.0",
      //"com.typesafe.akka" %% "akka-stream" % "2.5.0",
      //"io.spray" %% "spray-json" % "1.3.3",
      //"com.github.fommil" %% "spray-json-shapeless" % "1.3.0",
      "org.scodec" %% "scodec-bits" % "1.1.4",
      //"com.github.julien-truffaut" %% "monocle-core" % "1.4.0",
      //"com.github.julien-truffaut" %% "monocle-macro" % "1.4.0",
      //"com.github.melrief" %% "pureconfig" % "0.7.0",
      //"eu.timepit" %% "refined" % "0.8.0",
      //"eu.timepit" %% "refined-pureconfig" % "0.8.0",
      "org.lolhens" %% "ifoption" % "0.1.1"
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
      "io.monix" %% "monix" % "2.2.4",
      "org.lolhens" %% "ifoption" % "0.1.0"
    )
  )
  .settings(settings: _*)
  .settings(
    libraryDependencies ++= Seq("org.apache.commons" % "commons-lang3" % "3.5")
  )

lazy val deviceBackend = project.in(file("device"))
  .settings(settings: _*)
  .dependsOn(fileBackend, os)

lazy val fuseFrontend = project.in(file("fuse"))
  .enablePlugins(
    JavaAppPackaging,
    UniversalPlugin)
  .settings(settings: _*)
  .dependsOn(core)
