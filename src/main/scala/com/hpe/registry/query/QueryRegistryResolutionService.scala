package com.hpe.registry.query

import cats.effect.Effect
import cats.implicits._
import com.hpe.registry.query.config.QueryConfig
import com.hpe.registry.query.schema.{InputValidationException, SchemaBuilder}
import com.typesafe.scalalogging.Logger
import io.circe.{Decoder, Json, JsonObject}
import org.http4s.Headers
import sangria.ast.Document
import sangria.execution._
import sangria.marshalling.circe._
import sangria.parser.QueryParser
import sangria.validation.ValueCoercionViolation
import zio.{Task, UIO, ZIO}
import zio.interop.catz._

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

object QueryRegistryResolutionService {

  private implicit lazy val inputParser = Decoder.instance { c =>
    for {
      q <- c.downField("query").as[String]
      f <- c.downField("operationName").as[Option[String]]
      v <- c.downField("variables").as[Option[Json]]
    } yield (q, f, v.getOrElse(Json.fromJsonObject(JsonObject.empty)))
  }

  private val applicationExceptionHandler = ExceptionHandler(
    onUserFacingError = {
      case (m, x: InputValidationException) if x.errors.size > 1  =>
        HandledException.multiple(
          messages = x.errors
            .map(e =>
              (
                "Invalid value",
                Map(
                  "property"  -> m.scalarNode(e.propertyName, "String", Set.empty),
                  "message"   -> m.scalarNode(e.message, "String", Set.empty),
                  "errorCode" -> m.scalarNode(e.errorCode, "String", Set.empty)
                ),
                Nil
              )
            )
            .toList
            .toVector,
          addFieldsInError = true,
          addFieldsInExtensions = false
        )
      case (m, x: InputValidationException) if x.errors.size == 1 =>
        HandledException.single(
          "Invalid value",
          additionalFields = Map(
            "property"  -> m.scalarNode(x.errors.head.propertyName, "String", Set.empty),
            "message"   -> m.scalarNode(x.errors.head.message, "String", Set.empty),
            "errorCode" -> m.scalarNode(x.errors.head.errorCode, "String", Set.empty)
          ),
          addFieldsInError = true,
          addFieldsInExtensions = false
        )
    },
    onViolation = {
      case (_, v: ValueCoercionViolation) =>
        HandledException.single(
          "Violation" + v
        )
    }
  )
}

class QueryRegistryResolutionService(queries: List[QueryConfig]) {
  import QueryRegistryResolutionService._

  private val logger = Logger[this.type]

  def executeQuery(input: Json, headers: Headers): Task[Json] =
    input.as[(String, Option[String], Json)] match {
      case Right((query, operation, variables)) =>
        for {
          doc <- {
            Task
              .fromTry(QueryParser.parse(query))
              .tapError(parseError => UIO(logger.error("unable to parse a query: {}", parseError)))
              .tap(_ => UIO(logger.debug("received a new query: {}", query)))
          }
          res <- execute(doc, operation, variables, headers)
        } yield res
      case Left(e)                              => zio.Task.fail(e)
    }

  private def execute(doc: Document, operation: Option[String], vars: Json, headers: Headers): Task[Json] = {
    Task.fromFuture(implicit ec =>
      Executor
        .execute(
          SchemaBuilder.buildSchema(queries),
          doc,
          new Object(),
          //          queryReducers = QueryReducer.rejectIntrospection[DsccApplicationContext](includeTypeName = false) :: Nil,
          //          middleware = telemetry match {
          //            case Some(value) => new FieldMetrics(restServices, value) :: Nil
          //            case None        => Nil
          //          },
          variables = vars,
          operationName = operation,
          exceptionHandler = applicationExceptionHandler
        )
        .recover {
          case analysis: QueryAnalysisError =>
            analysis.resolveError
        }
    )
  }
}
