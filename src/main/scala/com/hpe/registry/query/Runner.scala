package com.hpe.registry.query

import cats.effect._
import com.codahale.metrics.jvm.{GarbageCollectorMetricSet, MemoryUsageGaugeSet}
import com.hpe.registry.query.config._
import fs2.Stream
import harmony.config.Conf
import harmony.syntax.config._
import harmony.telemetry.Telemetry
import org.http4s.HttpApp
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.syntax.kleisli._
import zio.{ExitCode, RIO, Task, URIO, ZEnv, ZIO}
import zio.clock.Clock
import zio.interop.catz._

object Runner extends zio.App with Threadpools {

  private val telemetry = Telemetry.defaultRegistry

  private implicit val _ec                           = blockingThreadPool
  private implicit val ce: ConcurrentEffect[ClockIO] = taskEffectInstance(zio.Runtime.default)
  private implicit val clock                         = zio.clock.Clock.Service.live
  private type ClockIO[T] = RIO[Clock, T]
  private implicit val timer: cats.effect.Timer[ClockIO] = zioTimer

//  override protected implicit def contextShift: ContextShift[IO] = IO.contextShift(blockingThreadPool)

  def run(args: List[String]): URIO[ZEnv, ExitCode] = {
    telemetry.register("garbage_collector", new GarbageCollectorMetricSet())
    telemetry.register("memory_usage", new MemoryUsageGaugeSet())

    Telemetry.defaultBuilder

    start.compile.drain.fold(_ => ExitCode.failure, _ => ExitCode.success)
  }

  private def start: Stream[ClockIO, ExitCode] =
    for {
      b      <- Stream.resource(Blocker[ClockIO])
//      bu = businessUnit
      c       = configuration
//      cl = tsdbClusters
//      s  = shardClient
//      tl = tsdbShardConfigs
      queries = QueryConfigBuilder.buildQueries
      a       = new Application(queries)
      e      <- BlazeServerBuilder[ClockIO](blockingThreadPool).bindHttp(port = c.port, host = c.host).withHttpApp(context(a)).serve
    } yield e match {
      case cats.effect.ExitCode.Success => ExitCode.success
      case _                            => ExitCode.failure
    }

  private def context(app: Application): HttpApp[ClockIO] =
    Router(
      "/" -> app.endpoint
    ).orNotFound.mapF(_.flatMap(res => ZIO.environment[Clock].map(_ => res)))

  private val businessUnit: String =
    readers.BU =<< Conf.application("harmony")

  private val shardClient: HostConf =
    readers.ShardClient =<< Conf.application("harmony.pal.data.db.shard.client")

  private def configuration: HostConf =
    readers.Server =<< Conf.application("harmony.pal.data.server") // < READER Monad

  private val tsdbClusters: List[String] =
    readers.TsdbClusters =<< Conf.application("harmony.pal.data.db.timeseries")

  private val tsdbShardConfigs: List[DbConf] = {
    val hostList = readers.TsdbHosts =<< Conf.application("harmony.pal.data.db.timeseries")

    hostList.zipWithIndex
      .map(l => readers.TsdbShard(l._1, l._2) =<< Conf.application("harmony.pal.data.db.timeseries"))
  }

}
