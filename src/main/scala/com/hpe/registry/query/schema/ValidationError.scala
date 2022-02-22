package com.hpe.registry.query.schema

/** Represents a validation error
  */
sealed trait ValidationError extends Product with Serializable {
  val propertyName: String
  val errorCode: String
  def message: String
}

object ValidationError {

  case class EmptyValue(propertyName: String) extends ValidationError {
    override def message: String   = propertyName + " can not be empty"
    override val errorCode: String = "EMPTY_VALUE"
  }

  case class WrongFormat(propertyName: String, error: String) extends ValidationError {
    override def message: String   = "Wrong format in property '" + propertyName + "': " + error
    override val errorCode: String = "WRONG_FORMAT"
  }

}
