package com.hpe.registry.query

import com.hpe.registry.query.config.QueryConfig
import com.typesafe.config.ConfigFactory
import scala.collection.JavaConverters._

import java.io.File
import java.nio.file.{Files, Paths}

object Main extends App {

  // TODO: how to name databases
  case class Database(
  )

  def buildQueryConfig(filePath: String): QueryConfig = {

//    val path  = Paths.get(filePath).toAbsolutePath
//    val bytes = Files.readAllBytes(path)
//    val s     = new String(bytes)
    val file = new File(filePath)
    val conf = ConfigFactory.parseFile(file)
//    val confS = ConfigFactory.parseString(s)

    def opt[T](s: String, f: String => T): Option[T] =
      if (conf.hasPath(s)) Some(f(s))
      else None

    QueryConfig(
      file.getName.split("\\.")(0),
      opt("description", conf.getString(_)),
      conf
        .getObject("inputs")
        .unwrapped()
        .asScala
        .flatMap { case (k, v) => QueryConfig.RegistryInputType.fromTypeString(v.toString).map(k -> _) }
        .toMap,
      conf.getObject("outputs").unwrapped().asScala.mapValues(_.toString).toMap,
      conf.getStringList("param_ordering").asScala.toList,
      opt("shardingDiscriminator", conf.getString(_)),
      conf.getString("query"),
      "appinsights-timescale"
    )
  }

  val qc = buildQueryConfig("registry/appinsights/timescale/databaselist.conf")

  println(qc)
}
