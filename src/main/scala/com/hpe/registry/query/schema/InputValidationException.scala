package com.hpe.registry.query.schema

import cats.data.NonEmptyChain
import sangria.execution.UserFacingError

case class InputValidationException(message: String, errors: NonEmptyChain[ValidationError]) extends Exception with UserFacingError {
  override def getMessage: String = message
}
