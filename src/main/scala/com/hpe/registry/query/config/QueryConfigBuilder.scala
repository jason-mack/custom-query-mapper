package com.hpe.registry.query.config

import com.typesafe.config.ConfigFactory

import java.io.{File, FileFilter}
import scala.collection.JavaConverters._

object QueryConfigBuilder {
  private def registry = "registry"

  def buildQueries: List[QueryConfig]                                              = {
    val isDir  = new FileFilter {
      override def accept(pathname: File): Boolean = pathname.isDirectory
    }
    val isFile = new FileFilter {
      override def accept(pathname: File): Boolean = pathname.isFile
    }

    val reg = new File(s"$registry")
    (
      for {
        bu    <- reg.listFiles(isDir)
        db    <- bu.listFiles(isDir)
        query <- db.listFiles(isFile)
      } yield buildQuery(bu.getName, db.getName, query.getName)
    ).toList
  }

  // TODO: Deal with files not existing? Deal with files not of correct format?
  private def buildQuery(bu: String, database: String, query: String): QueryConfig = {
    val queryFile = new File(s"registry/$bu/$database/$query")

    //    val path  = Paths.get(filePath).toAbsolutePath
    //    val bytes = Files.readAllBytes(path)
    //    val s     = new String(bytes)
    val conf = ConfigFactory.parseFile(queryFile)
    //    val confS = ConfigFactory.parseString(s)

    def opt[T](s: String, f: String => T): Option[T] =
      if (conf.hasPath(s)) Some(f(s))
      else None

    QueryConfig(
      queryFile.getName.split("\\.")(0),
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
}
