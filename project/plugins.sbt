logLevel := Level.Warn

externalResolvers := Seq("artifactory" at "http://lolhens.no-ip.org/artifactory/maven-public/")

//addSbtPlugin("io.get-coursier" % "sbt-coursier" % "1.0.0-RC1") //M15-7

addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.3.0")

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.8.2")

addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "5.0.1")

addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.1.4")

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.3")
