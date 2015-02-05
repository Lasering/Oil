package org.oil

/**
 * A Formatter knows how to convert a string to a given type T and vice-versa.
 * The conversion from string to T might not be possible, in that case, a FormError is returned in a Left.
 **/
trait Formatter[T] {
  def toType(data: String): Either[FormError, T]
  def toString(value: T): String = value.toString

  protected def parsing[T](convertedValue: => T, formError: => FormError): Either[FormError, T] = {
    scala.util.control.Exception.allCatch[T]
      .either(convertedValue)
      .left.map(e => formError)
  }
}
object Formats {
  implicit val emptyFormat = new Formatter[Nothing] {
    def toType(data: String): Either[FormError, Nothing] = ???
  }

  implicit val stringFormat = new Formatter[String] {
    def toType(data: String): Either[FormError, String] = Right(data)
  }
  implicit val intFormat = new Formatter[Int] {
    def toType(data: String): Either[FormError, Int] = parsing(data.toInt, FormError("error.number"))
  }
}
