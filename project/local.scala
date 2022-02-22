import sbt._
import hpe.harmony.sbtPlugin.{dependencies => harmonyDeps}

object local {

  def _test(module: ModuleID): ModuleID = module % "test"

  private object versions {
    val kafka         = "2.3.0"
    val postgresql    = "42.2.23"
    val hikaripool    = "3.4.2"
    val scalatest     = "3.1.0"
    val scalaTestPlus = "3.1.0.0"
    val prometheus    = "0.14.0"
    val caliban       = "0.10.0"

    object harmony {
      val core  = "1.4.0"
      val model = "0.0.1-9-844cb18-SNAPSHOT"
    }

    object sangria {
      val core    = "1.4.2"
      val circe   = "1.2.1"
      val slowlog = "0.1.8"
    }

    object zio {
      val core        = "1.0.4-2"
      val catsInterop = "2.5.1.0"
    }
  }

  val cats = harmonyDeps.cats

  val commons = harmonyDeps.commons

  val groupon = harmonyDeps.groupon

  val logback = harmonyDeps.logback

  object scalatest {
    val core = "org.scalatest" %% "scalatest" % versions.scalatest
  }

  object kafka {
    val clients = "org.apache.kafka" % "kafka-clients" % versions.kafka
  }

  object harmony {
    val common      = "hpe.harmony" %% "harmony-core_common"    % versions.harmony.core
    val model       = "hpe.harmony" %% "symphony-model"         % versions.harmony.model
    val avro        = "hpe.harmony" %% "harmony-core_avro"      % versions.harmony.core
    val kafka       = "hpe.harmony" %% "harmony-core_kafka"     % versions.harmony.core
    val telemetry   = "hpe.harmony" %% "harmony-core_telemetry" % versions.harmony.core
    val shardclient = "hpe.harmony" %% "dbsharding-client"      % versions.harmony.core
  }

  val http4s = harmonyDeps.http4s

  object fs2 {
    val `reactive-streams` = "co.fs2" %% "fs2-reactive-streams" % "2.0.0"
  }

  object monix {
    val core = "io.monix" %% "monix" % "3.1.0"
  }

  object sangria {
    val core    = "org.sangria-graphql" %% "sangria"         % versions.sangria.core
    val circe   = "org.sangria-graphql" %% "sangria-circe"   % versions.sangria.circe
    val slowlog = "org.sangria-graphql" %% "sangria-slowlog" % versions.sangria.slowlog
    val monix   = "org.sangria-graphql" %% "sangria-monix"   % "1.0.0"
  }

  val circe = harmonyDeps.circe

  object postgresql {
    val postgres = "org.postgresql" % "postgresql" % versions.postgresql
  }

  val sealerate = "ca.mrvisser" %% "sealerate" % "0.0.6"

  object hikari {
    val `connection-pool` = "com.zaxxer" % "HikariCP" % versions.hikaripool
  }

  val scalacheck = harmonyDeps.scalacheck

  object scalatestplus {
    val core = "org.scalatestplus" %% "scalacheck-1-14" % versions.scalaTestPlus
  }

  object prometheus {
    val javaAgent = "io.prometheus.jmx" % "jmx_prometheus_javaagent" % versions.prometheus
  }

  object zio {
    val core        = "dev.zio" %% "zio"              % versions.zio.core
    val catsInterop = "dev.zio" %% "zio-interop-cats" % versions.zio.catsInterop
  }

  object caliban {
    val zioHttp = "com.github.ghostdogpr" %% "caliban-zio-http" % versions.caliban
    val client  = "com.github.ghostdogpr" %% "caliban-client"   % versions.caliban
  }

  object sttp {
    val client = "com.softwaremill.sttp.client3" %% "async-http-client-backend-zio" % "3.3.5"
  }

  val shapeless = harmonyDeps.shapeless
}
