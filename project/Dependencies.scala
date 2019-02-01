import sbt._
import sbt.librarymanagement.DependencyBuilders
import org.portablescala.sbtplatformdeps._

object dependencies {
  val addResolvers = Seq(
    "52north for postgis" at "http://52north.org/maven/repo/releases/",
    Resolver.sonatypeRepo("releases"),
    Resolver.sonatypeRepo("snapshots")
  )
  
  val compilerPlugins = Seq(
    addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.9"),
    addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full)
  )

  val bcrypt = "3.1"
  val cats = "1.4.0"
  val catsMtl = "0.4.0"
  val catsEffect = "1.2.0"
  val circe = "0.11.0"
  val circeConfig = "0.6.1"
  val doobie = "0.6.0"
  val flyway = "5.2.4"
  val fs2 = "1.0.2"
  val h4sm = "0.0.17"
  val http4s = "0.20.0-M5"
  val logback = "1.2.3"
  val monocle = "1.5.0"
  val postgis = "1.3.3"
  val postgres = "42.2.5"
  val scalaCheck = "1.14.0"
  val scalaTest = "3.0.5"
  val simulacrum = "0.14.0"

  val httpDeps = Seq(
    "http4s-blaze-server",
    "http4s-blaze-client",
    "http4s-circe",
    "http4s-dsl"
  ).map("org.http4s" %% _ % http4s) ++ Seq(
    "circe-core",
    "circe-generic",
    "circe-parser",
    "circe-java8"
  ).map("io.circe" %% _ % circe)

  val testDeps = Seq(
    "org.scalatest" %% "scalatest" % scalaTest,
    "org.tpolecat" %% "doobie-scalatest" % doobie,
    "org.scalacheck" %% "scalacheck" % scalaCheck,
    "com.clovellytech" %% "h4sm-dbtesting" % h4sm
  )

  val testDepsInTestOnly = testDeps.map(_ % "test")

  val dbDeps = Seq(
    "org.flywaydb" % "flyway-core" % flyway,
    "org.postgresql" % "postgresql" % postgres,
    "org.postgis" % "postgis-jdbc" % postgis
  ) ++ Seq(
    "doobie-core",
    "doobie-postgres",
    "doobie-hikari"
  ).map("org.tpolecat" %% _ % doobie)

  val commonDeps = Seq(
    "io.circe" %% "circe-config" % circeConfig,
    "ch.qos.logback" %  "logback-classic" % logback,
    "com.github.mpilquist" %% "simulacrum" % simulacrum
  ) ++ Seq(
    "h4sm-auth",
    "h4sm-files",
    "h4sm-permissions"
  ).map("com.clovellytech" %% _ % h4sm) ++ Seq(
    "monocle-core",
    "monocle-generic",
    "monocle-macro",
    "monocle-state",
    "monocle-refined"
  ).map("com.github.julien-truffaut" %% _ % monocle)

  val allDeps = httpDeps ++ dbDeps ++ commonDeps ++ testDepsInTestOnly
}
