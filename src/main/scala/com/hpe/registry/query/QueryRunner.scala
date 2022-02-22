package com.hpe.registry.query

import com.hpe.registry.query.config.QueryConfig
import com.hpe.registry.query.config.QueryConfig.RegistryInputType
import io.circe.Json
import sangria.marshalling.{FromInput, circe}
import sangria.schema.Context
import zio.Task

import java.sql.Connection

object QueryRunner {
  def run(query: QueryConfig, ctx: Context[Any, Unit]): Task[Json] = {
    val conn: Connection = ???
    val stm              = conn.prepareStatement(query.query)
    for {
      stm <- Task(conn.prepareStatement(query.query))
      _   <- {
        query.paramOrder.zipWithIndex.foldLeft(Task(())) {
          case (task, (paramName, idx)) =>
            task *> query.inputs(paramName).set(stm, idx + 1, ctx, paramName)
        }
      }
      rs  <- Task(stm.executeQuery())
    } yield ???

  }
}
