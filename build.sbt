import play.sbt.PlayImport.PlayKeys.devSettings

name := "jam"

lazy val commonSettings = Seq(
  version := "0.1.0-SNAPSHOT",
  scalaVersion := "2.13.5",
  scalacOptions ++= List(
    "-deprecation",
    "-feature",
    "-unchecked",
    "-language:implicitConversions",
    "-Yrangepos",
    "-Ymacro-annotations",
    "-Ywarn-unused",
    "-Xlint",
    "-Xfatal-warnings"
  ),
  // scalafix
  addCompilerPlugin(scalafixSemanticdb),
  semanticdbEnabled := true,
  semanticdbVersion := scalafixSemanticdb.revision
)

lazy val playCommonSettings = Seq(
  scalacOptions += s"-Wconf:src=${target.value}/.*:s",
  PlayKeys.playRunHooks ++= Seq(
    DockerComposeRunHook(baseDirectory.value.getParentFile)
  ),
  PlayKeys.fileWatchService := {
    lazy val isMac = System.getProperties.get("os.name") == "Mac OS X"
    val logger     = play.sbt.run.toLoggerProxy(sLog.value)
    if (System.getProperties.get("os.arch") == "aarch64") {
      // For Apple M1
      play.dev.filewatch.FileWatchService.jdk7(logger)
    } else
      play.dev.filewatch.FileWatchService.default(logger, isMac)
  }
)

lazy val messaging = (project in file("jam-messaging"))
  .enablePlugins(PlayScala)
  .settings(commonSettings)
  .settings(playCommonSettings)
  .settings(
    libraryDependencies ++= Seq(
      guice,
      jdbc,
      "net.debasishg"          %% "redisclient"        % "3.30",
      "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % Test
    )
  )
  .settings(
    devSettings := Map("play.server.http.port" -> "9001").toSeq
  )

lazy val root = (project in file("."))
  .settings(commonSettings)
  .aggregate(messaging)

addCommandAlias("fixAll", "scalafixAll; scalafmtAll; scalafmtSbt")
addCommandAlias("checkAll", "scalafixAll --check; scalafmtCheckAll; scalafmtSbtCheck")

ThisBuild / scalafixDependencies += "com.github.liancheng" %% "organize-imports" % "0.5.0"

Global / onChangedBuildSource := ReloadOnSourceChanges
