package com.hpe.registry.query

import com.hpe.registry.query.config.QueryConfig

import scala.concurrent.ExecutionContext

class Application(queries: List[QueryConfig])(implicit ec: ExecutionContext) {
  def endpoint = new Endpoints(new QueryRegistryResolutionService(queries)).endpoint
}
