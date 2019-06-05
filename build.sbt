import xerial.sbt.Sonatype._

cancelable in Global := true

val compilerPlugins = Seq(
  addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.10.2"),
  addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full)
)

val versions = new {
  val scalatest = "3.0.5"
  val outwatch = "676f94a"
}

val commonSettings = Seq(
  organization := "com.clovellytech",
  version := Version.version,
  scalaVersion := Version.scalaVersion,
  scalacOptions ++= options.scalac,
  scalacOptions in (Compile, console) := options.scalacConsole,
  updateOptions := updateOptions.value.withLatestSnapshots(false)
) ++ compilerPlugins

lazy val publishSettings = Seq(
  useGpg := true,
  publishMavenStyle := true,
  publishTo := sonatypePublishTo.value,
  publishArtifact in Test := false,
  homepage := Some(url("https://github.com/clovellytech/outwatch-router")),
  pomIncludeRepository := Function.const(false),
  sonatypeProfileName := "com.clovellytech",

  // License of your choice
  licenses := Seq("MIT" -> url("http://opensource.org/licenses/MIT")),

  // Where is the source code hosted
  sonatypeProjectHosting := Some(GitHubHosting("clovellytech", "outwatch-router", "pattersonzak@gmail.com"))
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
    scalacOptions := options.scalacConsole
  )
  .settings(
    mdocVariables := Map(
      "VERSION" -> version.value
    )
  )
  .dependsOn(router)

lazy val copyFastOptJS = TaskKey[Unit]("copyFastOptJS", "Copy javascript files to target directory")

lazy val router  = project
  .in(file("./outwatch-router"))
  .settings(name := "outwatch-router")
  .enablePlugins(ScalaJSPlugin)
  .enablePlugins(ScalaJSBundlerPlugin)
  .settings(commonSettings)
  .settings(
    scalaJSModuleKind := ModuleKind.CommonJSModule,
    scalacOptions += "-P:scalajs:sjsDefinedByDefault",
    useYarn := true, // makes scalajs-bundler use yarn instead of npm
    jsEnv in Test := new org.scalajs.jsenv.jsdomnodejs.JSDOMNodeJSEnv,
    scalaJSModuleKind := ModuleKind.CommonJSModule, // configure Scala.js to emit a JavaScript module instead of a top-level script
    version in webpack := "4.16.1",
    version in startWebpackDevServer := "3.1.4",
    webpackDevServerExtraArgs := Seq("--progress", "--color"),
    webpackConfigFile in fastOptJS := Some(baseDirectory.value / "webpack.config.dev.js"),
    // https://scalacenter.github.io/scalajs-bundler/cookbook.html#performance
    webpackBundlingMode in fastOptJS := BundlingMode.LibraryOnly(),
    resolvers += "jitpack" at "https://jitpack.io",
    libraryDependencies ++= Seq(
      "io.github.outwatch" % "outwatch" % versions.outwatch,
      "org.scalatest" %%% "scalatest" % versions.scalatest % Test,
    ),
    copyFastOptJS := {
      val inDir = (crossTarget in (Compile, fastOptJS)).value
      val outDir = (crossTarget in (Compile, fastOptJS)).value / "dev"
      val files = Seq("outwatch-router-fastopt-loader.js", "outwatch-router-frontend-fastopt.js", "outwatch-router-frontend-fastopt.js.map") map { p =>   (inDir / p, outDir / p) }
      IO.copy(files, overwrite = true, preserveLastModified = true, preserveExecutable = true)
    },
    // hot reloading configuration:
    // https://github.com/scalacenter/scalajs-bundler/issues/180
    addCommandAlias("dev", "; compile; fastOptJS::startWebpackDevServer; devwatch; fastOptJS::stopWebpackDevServer"),
    addCommandAlias("devwatch", "~; fastOptJS; copyFastOptJS")
  )
  .settings(publishSettings)

lazy val exampleApp = (project in file("router-example"))
  .settings(name := "outwatch-example")
  .settings(commonSettings)
  .dependsOn(router)

lazy val root = project
  .in(file("."))
  .settings(name := "outwatch-router-root")
  .settings(commonSettings)
  .settings(
    skip in publish := true,
  )
  .dependsOn(router)
  .aggregate(router)


