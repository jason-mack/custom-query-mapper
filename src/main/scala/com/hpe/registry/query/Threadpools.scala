package com.hpe.registry.query

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext

trait Threadpools {
  lazy val blockingThreadPool: ExecutionContext = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())
}
