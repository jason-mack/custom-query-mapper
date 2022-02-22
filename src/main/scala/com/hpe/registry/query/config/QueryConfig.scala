package com.hpe.registry.query.config

import com.hpe.registry.query.config.QueryConfig.RegistryInputType.IntRIT.{createArg, set}
import io.circe.Decoder
import sangria.marshalling.FromInput
import sangria.schema.{Argument, FloatType, InputType, IntType, ListInputType, OptionInputType, StringType, WithoutInputTypeTags}
import zio.Task

import java.sql.PreparedStatement
//import harmony.pal.core.schema.scalars._
//import harmony.pal.data.model.codecs._
//import harmony.pal.data.schema.data._
import io.circe.parser._
import org.http4s.Headers
import org.http4s.util.CaseInsensitiveString
import sangria.marshalling.circe.circeDecoderFromInput
import sangria.parser.QueryParser
import sangria.schema._

import scala.concurrent.Future

case class QueryConfig(
    name: String,
    description: Option[String],
    inputs: Map[String, QueryConfig.RegistryInputType[_]],
    outputs: Map[String, String],
    paramOrder: List[String],
    shardDescriminator: Option[String],
    query: String,
    runAgainst: String
)

object QueryConfig {

  sealed trait RegistryInputType[T] {
    type Type = T
    def createArg(name: String): Argument[T]
    def it: InputType[T]
    def fi: FromInput[T]
    private[QueryConfig] def set(stm: PreparedStatement, idx: Int, value: T): Task[Unit]

    final def set(stm: PreparedStatement, idx: Int, ctx: Context[Any, Unit], name: String): Task[Unit] =
      set(stm, idx, ctx.arg(createArg(name)))
  }

  object RegistryInputType {
    import WithoutInputTypeTags._

    def fromTypeString(s: String): Option[RegistryInputType[_]] = {
      import cats.implicits.catsSyntaxOptionId
      s match {
        case "String"         => StringRIT.some
        case "Int"            => IntRIT.some
        case "Double"         => DoubleRIT.some
        case "Option[String]" => new OptionRIT(StringRIT).some
        case "Option[Int]"    => new OptionRIT(IntRIT).some
        case "Option[Double]" => new OptionRIT(DoubleRIT).some
        case _                => None
      }
    }

    final class OptionRIT[T](val inner: ScalarRegistryInputType[T]) extends RegistryInputType[Option[T]] {
      private implicit def innerFi                     = inner.fi
      def createArg(name: String): Argument[Option[T]] = Argument(name, it)

      override def it: InputType[Option[T]] = OptionInputType(inner.it)

      override def fi: FromInput[Option[T]] = implicitly

      override def set(stm: PreparedStatement, idx: Int, value: Option[T]): Task[Unit] =
        value.fold(
          inner match {
            case _: IntRIT.type    => Task(stm.setNull(idx, java.sql.Types.INTEGER))
            case _: StringRIT.type => Task(stm.setNull(idx, java.sql.Types.VARCHAR))
            case _: DoubleRIT.type => Task(stm.setNull(idx, java.sql.Types.DOUBLE))
          }
        )(v => Task(inner.set(stm, idx, v)))
    }

    // TODO: resolve dealing with list params in query.
//    final class ListRIT[T](inner: ScalarRegistryInputType[T]) extends RegistryInputType[List[T]] {
//      import inner.{decoder, fi}
//      def createArg(name: String): Argument[List[T]] = Argument(name, it)
//      override def it: InputType[List[T]]            = ListInputType(inner.it)
//
//      override def fi = implicitly
//
//      override def set(stm: PreparedStatement, idx: Int, value: List[T]): Task[Unit] = ???
//    }

    sealed trait ScalarRegistryInputType[T] extends RegistryInputType[T] {
      private[QueryConfig] implicit def decoder: Decoder[T]
    }

    case object IntRIT extends ScalarRegistryInputType[Int] {
      def createArg(name: String): Argument[Int] = Argument(name, it)
      override def it: InputType[Int]            = IntType

      override def fi = implicitly

      override def set(stm: PreparedStatement, idx: Int, value: Int): Task[Unit] =
        Task(stm.setInt(idx, value))

      override private[QueryConfig] implicit def decoder: Decoder[Int] = Decoder.decodeInt
    }

    case object StringRIT extends ScalarRegistryInputType[String] {
      def createArg(name: String): Argument[String] = Argument(name, it)
      override def it: InputType[String]            = StringType
      override def fi                               = implicitly

      override def set(stm: PreparedStatement, idx: Int, value: String): Task[Unit] =
        Task(stm.setString(idx, value))

      override private[QueryConfig] implicit def decoder: Decoder[String] = Decoder.decodeString
    }

    case object DoubleRIT extends ScalarRegistryInputType[Double] {
      def createArg(name: String): Argument[Double] = Argument(name, it)
      override def it: InputType[Double]            = FloatType
      override def fi                               = implicitly

      override def set(stm: PreparedStatement, idx: Int, value: Double): Task[Unit] =
        Task(stm.setDouble(idx, value))

      override private[QueryConfig] implicit def decoder: Decoder[Double] = Decoder.decodeDouble
    }
  }
}
