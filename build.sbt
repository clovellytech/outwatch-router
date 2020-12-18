import xerial.sbt.Sonatype._

cancelable in Global := true

val versions = new {
  val scalatest = "3.2.2"
  val outwatch = "61deece8"
}

val scala213 = "2.13.4"
val scala212 = "2.12.12"

val commonSettings = Seq(
  organization := "com.clovellytech",
  scalacOptions ++= options.scalac,
  scalacOptions in (Compile, console) := options.scalacConsole,
  updateOptions := updateOptions.value.withLatestSnapshots(false),
  crossScalaVersions in ThisBuild := Seq(scala213, scala212)
)

lazy val publishSettings = Seq(
  publishMavenStyle := true,
  publishTo := sonatypePublishTo.value,
  publishArtifact in Test := false,
  homepage := Some(url("https://github.com/clovellytech/outwatch-router")),
  pomIncludeRepository := Function.const(false),
  sonatypeProfileName := "com.clovellytech",
  // License of your choice
  licenses := Seq("MIT" -> url("http://opensource.org/licenses/MIT")),
  // Where is the source code hosted
  sonatypeProjectHosting := Some(
    GitHubHosting("clovellytech", "outwatch-router", "pattersonzak@gmail.com"),
  ),
)

lazy val docs = project
  .in(file("./router-docs"))
  .settings(commonSettings)
  .enablePlugins(MdocPlugin)
  .enablePlugins(MicrositesPlugin)
  .settings(
    name := "outwatch-router-docs",
    description := "A router for outwatch",
    organizationName := "com.clovellytech",
    organizationHomepage := Some(url("https://github.com/clovellytech")),
    homepage := Some(url("https://clovellytech.github.io/outwatch-router")),
    micrositeUrl := "https://clovellytech.github.io/outwatch-router",
    micrositeBaseUrl := "/outwatch-router",
    micrositeName := "Outwatch Router",
    micrositeCompilingDocsTool := WithMdoc,
    micrositeGithubOwner := "clovellytech",
    micrositeGithubRepo := "outwatch-router",
    scalacOptions := options.scalacConsole,
    libraryDependencies ++= Seq(
      "io.monix" %%% "monix-bio" % "1.1.0",
    ),
  )
  .settings(
    mdocVariables := Map(
      "VERSION" -> version.value,
    ),
    skip in publish := true,
  )
  .dependsOn(router)

lazy val copyFastOptJS = TaskKey[Unit]("copyFastOptJS", "Copy javascript files to target directory")

lazy val router = project
  .in(file("./outwatch-router"))
  .settings(name := "outwatch-router")
  .enablePlugins(ScalaJSPlugin)
  .enablePlugins(ScalaJSBundlerPlugin)
  .settings(commonSettings)
  .settings(
    useYarn := true, // makes scalajs-bundler use yarn instead of npm
    requireJsDomEnv in Test := true,
    version in webpack := "4.16.1",
    version in startWebpackDevServer := "3.1.4",
    webpackDevServerExtraArgs := Seq("--progress", "--color"),
    webpackConfigFile in fastOptJS := Some(baseDirectory.value / "webpack.config.dev.js"),
    // https://scalacenter.github.io/scalajs-bundler/cookbook.html#performance
    webpackBundlingMode in fastOptJS := BundlingMode.LibraryOnly(),
    resolvers += "jitpack".at("https://jitpack.io"),
    libraryDependencies ++= Seq(
      "com.github.outwatch.outwatch" %%% "outwatch" % versions.outwatch,
      "com.github.outwatch.outwatch" %%% "outwatch-util" % versions.outwatch,
      "org.scalatest" %%% "scalatest" % versions.scalatest % Test,
    ),
    copyFastOptJS := {
      val inDir = (crossTarget in (Compile, fastOptJS)).value
      val outDir = (crossTarget in (Compile, fastOptJS)).value / "dev"
      val files = Seq(
        "outwatch-router-fastopt-loader.js",
        "outwatch-router-frontend-fastopt.js",
        "outwatch-router-frontend-fastopt.js.map",
      ).map(p => (inDir / p, outDir / p))
      IO.copy(files, overwrite = true, preserveLastModified = true, preserveExecutable = true)
    },
    // hot reloading configuration:
    // https://github.com/scalacenter/scalajs-bundler/issues/180
    addCommandAlias(
      "dev",
      "; compile; fastOptJS::startWebpackDevServer; devwatch; fastOptJS::stopWebpackDevServer",
    ),
    addCommandAlias("devwatch", "~; fastOptJS; copyFastOptJS"),
  )
  .settings(publishSettings)

lazy val root = project
  .in(file("."))
  .settings(name := "outwatch-router-root")
  .settings(commonSettings)
  .settings(
    skip in publish := true,
  )
  .dependsOn(router)
  .aggregate(router)
