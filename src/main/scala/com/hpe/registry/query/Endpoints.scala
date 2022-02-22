package com.hpe.registry.query

import cats.effect.{Blocker, ContextShift}
import cats.implicits._
import com.typesafe.scalalogging.Logger
import io.circe.Json
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.{HttpRoutes, StaticFile}
import zio.Task
import zio.interop.catz._

import scala.concurrent.ExecutionContext

class Endpoints(resService: QueryRegistryResolutionService)(implicit ec: ExecutionContext) extends Http4sDsl[ClockIO] {
  private val logger                = Logger[this.type]
  val endpoint: HttpRoutes[ClockIO] = HttpRoutes.of {

    case req @ POST -> Root / "graphql" =>
      (
        for {
          _   <- ContextShift[ClockIO].shift
          p   <- req.as[Json]
          _   <- Task(logger.debug(s"Headers : ${req.headers}"))
          o   <- {
            resService
              .executeQuery(p, req.headers)
              .fold(
                e =>
                  InternalServerError(
                    Option(e.getMessage).getOrElse("Unexpected error during the execution of graphql command")
                  ),
                v => Ok(v)
              )
          }
          res <- o
        } yield res
      )

    case req @ GET -> Root => StaticFile.fromResource("/graphiql.html", Blocker.liftExecutionContext(ec), req.some).getOrElseF(NotFound())
  }
}
