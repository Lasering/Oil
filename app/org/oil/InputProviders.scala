package org.oil

import play.api.i18n.Lang
import play.api.templates.PlayMagic
import play.twirl.api.Html
import views.html.b3
import views.html.b3.B3FieldConstructor

/*trait InputProvider[T] {
  def inputType: String
  def initialization: Html
  def render(fieldName: String, field: Field[T], args: (Symbol, Any)*)(implicit handler: B3FieldConstructor, lang: Lang): Html
}*/

//The type parameter only exists to guide implicits
case class InputProvider[T](inputType: String) {
  def initialization: Html = Html("")

  def render(fieldName: String, field: Field[T], args: (Symbol, Any)*)(implicit handler: B3FieldConstructor, lang: Lang): Html = {
    println("Inside render")
    b3.inputType(field.inputProvider.inputType, field.toPlayField(fieldName), '_label -> fieldName, 'placeholder -> "")
  }
}

object InputProviders {
  //This allows a InputProvider[T] to be used where a InputProvider[Option[T]] is expected
  implicit def toOptionalInputProvider[T](inputProvider: InputProvider[T]): InputProvider[Option[T]] = {
    new InputProvider[Option[T]](inputProvider.inputType) {
      override def initialization: Html = inputProvider.initialization
      override def render(fieldName: String, field: Field[Option[T]], args: (Symbol, Any)*)(implicit handler: B3FieldConstructor, lang: Lang): Html = {
        //This cast is a time bomb waiting to explode
        inputProvider.render(fieldName, field.asInstanceOf[OptionalField[T]].innerField, args:_*)(handler, lang)
      }
    }
  }
  //This is not implicit to avoid creating an "implicit cycle"
  def toInputProvider[T](optionalInputProvider: InputProvider[Option[T]]): InputProvider[T] = {
    new InputProvider[T](optionalInputProvider.inputType) {
      override def initialization: Html = optionalInputProvider.initialization
      override def render(fieldName: String, field: Field[T], args: (Symbol, Any)*)(implicit handler: B3FieldConstructor, lang: Lang): Html = {
        //This "cast" is a time bomb waiting to explode
        optionalInputProvider.render(fieldName, new OptionalField[T](field), args:_*)(handler, lang)
      }
    }
  }

  implicit val emptyProvider = new InputProvider[Nothing]("")
  implicit val textProvider = new InputProvider[String]("text")
  implicit val intProvider = new InputProvider[Int]("number")
  class HiddenProvider extends InputProvider[Any]("hidden") {
    override def render(fieldName: String, field: Field[Any], args: (Symbol,Any)*)(implicit handler: B3FieldConstructor, lang: Lang): Html = {
      println("Inside override render")
      implicit val handler: B3FieldConstructor = b3.clear.fieldConstructor
      b3.hidden(fieldName, field.toPlayField(fieldName))
    }
  }
  val hiddenProvider = new HiddenProvider
}
