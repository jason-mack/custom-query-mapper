import sbt._
import sbtsonar.SonarPlugin.autoImport.sonarProperties
import local._

lazy val FunctionalTest = config("functionalTest") extend Test

scalaVersion := "2.12.10"

name := "query-registry"

organization in Global := "hpe.harmony"
def functionalTestFilter(name: String): Boolean = name endsWith "FunctionalTest"
def unitFilter(name: String): Boolean           = !functionalTestFilter(name)

val commonSettings = Seq(run / fork := false, test / fork := false)

val sonarSettings = Seq(
  sonarProperties ++= Map(
    "sonar.host.url"    -> "http://sonarqube1.dev.nimblestorage.com/",
    "sonar.modules"     -> "core, model, data",
    "sonar.projectName" -> "dscc-pal"
  ),
  sonarScan / aggregate := false
)

libraryDependencies ++= Seq(
  http4s.`blaze-server`,
  http4s.`http4s-circe`,
  http4s.`http4s-dsl`,
  logback.core,
  harmony.model,
  harmony.telemetry,
  harmony.shardclient,
  postgresql.postgres,
  circe.core,
  circe.parser,
  scalacheck.core,
  scalatest.core,
  scalatestplus.core,
  hikari.`connection-pool`,
  shapeless.core,
  caliban.client,
  caliban.zioHttp,
  zio.core,
  zio.catsInterop,
  sttp.client,
  hpe.harmony.sbtPlugin.dependencies._test(shapeless.core),
  http4s.`http4s-dsl`,
  harmony.common,
  cats.effect,
  sangria.core,
  sangria.circe,
  circe.core,
  commons.lang3,
  groupon.uuid,
  harmony.model,
  sealerate,
  _test(local.scalatest.core),
  _test(scalacheck.core),
  cats.effect,
  http4s.`blaze-server`,
  http4s.`http4s-circe`,
  http4s.`http4s-dsl`,
  sangria.core,
  sangria.slowlog,
  sangria.circe,
  sangria.monix,
  circe.core,
  circe.generic,
  sealerate
)

enablePlugins(JavaAppPackaging, com.typesafe.sbt.packager.docker.DockerPlugin)

settings.common

settings.buildInfo

settings.packaging("pal-data-query")

enablePlugins(JavaAgent)

javaAgents += "org.mortbay.jetty.alpn" % "jetty-alpn-agent" % "2.0.9" % "runtime;test"

addCommandAlias("build", value = ";data/docker:publish;writeCIArtifactsInfoFile")
addCommandAlias("build-local", value = ";data/docker:publishLocal;writeCIArtifactsInfoFile")

val remoteDockerRepo = sys.env.get("DOCKER_REPOSITORY")

lazy val promPort = 8090
lazy val promConf = "etc/promconf.yaml"
Universal / mappings += file(baseDirectory.value + s"/$promConf") -> promConf
javaAgents += JavaAgent(prometheus.javaAgent % "dist;runtime;test", arguments = s"$promPort:$promConf")
dockerExposedPorts += promPort

Docker / packageName := "dscc-pal"
dockerRepository := remoteDockerRepo

//Compile / TwirlKeys.compileTemplates / sourceDirectories := "src/main/twirl"

lazy val queryRegistry =
  project
    .in(file("."))
    .settings(sonarSettings)

addCommandAlias(
  "functionalTest",
  value = ";FunctionalTest/test;writeCIArtifactsInfoFile"
)
addCommandAlias(
  "scoverage",
  ";clean;coverage;test;coverageReport;coverageAggregate"
)
addCommandAlias("sonar", ";scoverage;sonarScan")
addCommandAlias(
  "verify",
  ";clean;compile;test;scalafmtCheckAll;scalafmtSbtCheck;FunctionalTest/test;build-local"
)
addCommandAlias("scalafmt", ";scalafmtAll;scalafmtSbt")

ThisBuild / coverageOutputHTML := false
ThisBuild / coverageOutputCobertura := false
ThisBuild / coverageOutputXML := true
