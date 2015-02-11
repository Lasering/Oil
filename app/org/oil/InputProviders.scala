package org.oil

import play.twirl.api.Html

case class InputProvider[T](inputType: String, initialization: Html = Html(""))

object InputProviders {
  //This allows a InputProvider[T] to be used where a InputProvider[Option[T]] is expected
  implicit def toOptionalInputProvider[T](inputProvider: InputProvider[T]): InputProvider[Option[T]] = {
    new InputProvider[Option[T]](inputProvider.inputType, inputProvider.initialization)
  }
  //This is not implicit to avoid creating an "implicit cycle"
  def toInputProvider[T](optionalInputProvider: InputProvider[Option[T]]): InputProvider[T] = {
    new InputProvider[T](optionalInputProvider.inputType, optionalInputProvider.initialization)
  }

  implicit val emptyProvider = new InputProvider[Nothing]("")
  implicit val textProvider = new InputProvider[String]("text")
  implicit val intProvider = new InputProvider[Int]("number")
}
