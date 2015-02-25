package org.oil

object Formatter {
  //This allows a Formatter[T] to be used where a Formatter[Option[T]] is expected
  implicit def toOptionalFormatter[T](formatter: Formatter[T]): Formatter[Option[T]] = new Formatter[Option[T]] {
    //This formatter should only be used when in fact there is a value (a Some)
    override def toType(data: String): Either[FormError, Option[T]] = formatter.toType(data).right.map(t => Some(t))
    override def toString(value: Option[T]): String = value.fold[String](???)(v => formatter.toString(v))
  }
  //This is not implicit to avoid creating an "implicit cycle"
  def toFormatter[T](optionalFormatter: Formatter[Option[T]]): Formatter[T] = new Formatter[T] {
    override def toType(data: String): Either[FormError, T] = optionalFormatter.toType(data).right.map(_.get)
    override def toString(value: T): String = optionalFormatter.toString(Some(value))
  }
}
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
