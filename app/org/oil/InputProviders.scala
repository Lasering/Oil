package org.oil

import play.twirl.api.Html

case class InputProvider[T](inputType: String, initialization: Html = Html(""))

object InputProviders {
  implicit val emptyProvider = new InputProvider[Nothing]("")
  implicit val textProvider = new InputProvider[String]("text")
  implicit val intProvider = new InputProvider[Int]("number")
}
