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

case class InputProvider[T](field: Field[T], inputType: String) {
  /**
   * HTML with the the necessary includes to use this InputProvider, such as
   * stylesheets or javascript libraries
   * @return
   */
  def includes: Html = Html("")

  /**
   * HTML with the necessary initializations to use this InputProvider.
   * These are normally javascript code which initializes the included libraries.
   * The code here will run when the HTML document is ready.
   * @return
   */
  def onReady: Html = Html("")

  /**
   * Converts an org.oil.Field to a play.api.data.Field.
   * This method should be used very sparingly and very carefully, as it sets
   * most of the play Field fields to empty values.
   * @param name
   * @param field
   * @return
   */
  protected def toPlayField(name: String, field: Field[_]): play.api.data.Field = {
    import play.api.data.{Field => PlayField}
    import play.api.data.{FormError => PlayFormError}
    PlayField(null, name, Seq.empty[(String, Seq[Any])], None, Seq.empty[PlayFormError], field.data)
  }

  def render(fieldName: String, args: (Symbol, Any)*)(implicit handler: B3FieldConstructor, lang: Lang): Html = {
    println(s"Inside render $fieldName")
    b3.inputType(field.inputProvider.inputType, toPlayField(fieldName, field),
      '_label -> fieldName,
      'placeholder -> "")


      //'value
  }
}

object InputProviders {
  //Theses casts WILL result in problems
  //This allows a InputProvider[T] to be used where a InputProvider[Option[T]] is expected
  implicit def toOptionalInputProvider[T](inputProvider: InputProvider[T]): InputProvider[Option[T]] = {
    inputProvider.asInstanceOf[InputProvider[Option[T]]]
    /*new InputProvider[Option[T]](inputProvider.inputType) {
      override def initialization: Html = inputProvider.initialization
      override def render(fieldName: String, field: Field[Option[T]], args: (Symbol, Any)*)(implicit handler: B3FieldConstructor, lang: Lang): Html = {
        //This cast is a time bomb waiting to explode
        inputProvider.render(fieldName, field.asInstanceOf[OptionalField[T]].innerField, args:_*)(handler, lang)
      }
    }*/
  }
  //This is not implicit to avoid creating an "implicit cycle"
  def toInputProvider[T](optionalInputProvider: InputProvider[Option[T]]): InputProvider[T] = {
    optionalInputProvider.asInstanceOf[InputProvider[T]]
    /*new InputProvider[T](optionalInputProvider.inputType) {
      override def initialization: Html = optionalInputProvider.initialization
      override def render(fieldName: String, field: Field[T], args: (Symbol, Any)*)(implicit handler: B3FieldConstructor, lang: Lang): Html = {
        //This "cast" is a time bomb waiting to explode
        optionalInputProvider.render(fieldName, new OptionalField[T](field), args:_*)(handler, lang)
      }
    }*/
  }

  implicit def emptyProvider[T](field: Field[T]): InputProvider[T] = new InputProvider(field, "")
  implicit def textProvider(field: Field[String]): InputProvider[String] = new InputProvider(field, "text")
  implicit def intProvider(field: Field[Int]): InputProvider[Int] = new InputProvider(field, "number")

  //Its any so it can be used in any type of field
  def hiddenProvider[T](field: Field[T]): InputProvider[T] = new InputProvider[T](field, "hidden") {
    println("Inside hidden provider")
    override def render(fieldName: String, args: (Symbol,Any)*)(implicit handler: B3FieldConstructor, lang: Lang): Html = {
      println(s"Inside overridden render $fieldName")
      implicit val handler: B3FieldConstructor = b3.clear.fieldConstructor
      b3.hidden(fieldName, toPlayField(fieldName, field))
    }
  }
}
