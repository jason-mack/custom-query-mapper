package com.hpe.registry.query.schema

import com.hpe.registry.query.QueryRunner
import com.hpe.registry.query.config.QueryConfig
import sangria.marshalling.FromInput
import sangria.schema._
import sangria.schema

object SchemaBuilder {
  def buildSchema(queries: List[QueryConfig]): Schema[Any, Unit] = {
    Schema(
      ObjectType(
        "Query",
        fields[Any, Unit](
          queries.map(buildField(_)): _*
        )
      )
    )
  }

  private def buildField(query: QueryConfig): Field[Any, Unit] = {
    val arguments = query.inputs.map { case (name, tpe) => tpe.createArg(name) }.toList
    Field[Any, Unit](
      name = query.name,
      fieldType = ObjectType(
        "Result",
        query.outputs.map {
          case (name, tpe) =>
            Field[Any, Unit](
              name = name,
              fieldType =
                // TODO: Expand supported types. Lookup types from Prisma1?
                toSangriaOutputType(tpe),
              description = None,
              arguments = Nil,
              resolve = _ => schema.Value(()),
              deprecationReason = None,
              tags = Nil,
              complexity = None,
              manualPossibleTypes = () => Nil,
              astDirectives = Vector.empty,
              astNodes = Vector.empty
            )
        }.toList
      ),
      description = query.description,
      arguments = arguments,
      resolve = c => {
        QueryRunner.run(query, c)
      },
      deprecationReason = None,
      tags = Nil,
      complexity = None,
      manualPossibleTypes = () => Nil,
      astDirectives = Vector.empty,
      astNodes = Vector.empty
    )
  }

  private def toSangriaOutputType(tpe: String): OutputType[_] = {
    tpe.toUpperCase match {
      case "STRING"         => StringType
      case "INT"            => IntType
      case "DOUBLE"         => FloatType
      case "LONG"           => LongType
      case "OPTION[STRING]" => OptionType(StringType)
      case "OPTION[INT]"    => OptionType(IntType)
      case "OPTION[DOUBLE]" => OptionType(FloatType)
      case "OPTION[LONG]"   => OptionType(LongType)
      case _                => sys.error("Unknown type for query")
    }
  }
}
