inThisBuild(Seq(
  name         := "Rorqual",
  organization := "com.dafttech",
  version      := "0.1.0-SNAPSHOT",
  scalaVersion := "3.3.7",
  licenses     := Seq("Apache-2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0")),
  scalacOptions ++= Seq(
    "-deprecation",
    "-feature",
    "-unchecked",
    "-explain",
    "-Wunused:all",
    "-Wvalue-discard",
    "-Wnonunit-statement"
  )
))

val V = new {
  val catsEffect       = "3.5.7"
  val fs2              = "3.11.0"
  val scodecBits       = "1.2.1"
  val scodecCore       = "2.3.2"
  val munit            = "1.0.4"
  val munitCatsEffect  = "2.0.0"
  val logback          = "1.5.18"
  val slf4j            = "2.0.17"
  val commonsLang3     = "3.17.0"
  val jnrFuse          = "0.5.7"
}

lazy val commonSettings = Seq(
  libraryDependencies ++= Seq(
    "org.scalameta" %% "munit"               % V.munit           % Test,
    "org.typelevel" %% "munit-cats-effect"   % V.munitCatsEffect % Test
  ),
  testFrameworks += new TestFramework("munit.Framework")
)

lazy val root = project.in(file("."))
  .settings(
    name := (ThisBuild / name).value,
    publish / skip := true
  )
  .aggregate(core, os, fileBackend, deviceBackend, partition, fuse, iscsi)

lazy val core = project.in(file("core"))
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-effect" % V.catsEffect,
      "co.fs2"        %% "fs2-core"    % V.fs2,
      "co.fs2"        %% "fs2-io"      % V.fs2,
      "org.scodec"    %% "scodec-bits" % V.scodecBits,
      "org.slf4j"     %  "slf4j-api"   % V.slf4j,
      "ch.qos.logback" % "logback-classic" % V.logback
    )
  )

lazy val partition = project.in(file("partition"))
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "org.scodec" %% "scodec-bits" % V.scodecBits,
      "org.scodec" %% "scodec-core" % V.scodecCore
    )
  )
  .dependsOn(core)

lazy val os = project.in(file("os"))
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "org.apache.commons" % "commons-lang3" % V.commonsLang3
    )
  )
  .dependsOn(core)

lazy val fileBackend = project.in(file("file"))
  .settings(commonSettings)
  .dependsOn(core)

lazy val deviceBackend = project.in(file("device"))
  .settings(commonSettings)
  .dependsOn(fileBackend, os)

lazy val fuse = project.in(file("fuse"))
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "com.github.serceman" % "jnr-fuse" % V.jnrFuse
    )
  )
  .dependsOn(core)

lazy val iscsi = project.in(file("iscsi"))
  .settings(commonSettings)
  .dependsOn(core)
