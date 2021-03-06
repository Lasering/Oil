package oil

import _root_.views.html.b3
import _root_.views.html.b3.B3FieldConstructor
import play.api.i18n.Lang
import play.twirl.api.Html

object InputProvider {
  //Theses casts WILL result in problems
  //This allows a InputProvider[T] to be used where a InputProvider[Option[T]] is expected
  implicit def toOptionalInputProvider[T](inputProvider: InputProvider[T]): InputProvider[Option[T]] = {
    inputProvider.asInstanceOf[InputProvider[Option[T]]]
  }
  //This is not implicit to avoid creating an "implicit cycle"
  def toInputProvider[T](optionalInputProvider: InputProvider[Option[T]]): InputProvider[T] = {
    optionalInputProvider.asInstanceOf[InputProvider[T]]
  }
}

/**
 * Knows how to render an HTML input for the given type `T`.
 */
trait InputProvider[T]{
  def field: Field[T]
  def inputType: String

  def withField(newField: Field[T]): InputProvider[T]

  /**
   * HTML with the the necessary includes to use this InputProvider, such as
   * stylesheets or javascript libraries.
   */
  def includes: Html = Html("")

  /**
   * HTML with the necessary initializations to use this InputProvider.
   * These are normally javascript code which initializes the included libraries.
   * The code here will run when the HTML document is ready.
   * @return
   */
  def onReady: Html = Html("")

  def render(fieldName: String, args: (Symbol, Any)*)(implicit handler: B3FieldConstructor, lang: Lang): Html = {
    b3.inputType(field.inputProvider.inputType, toPlayField(fieldName, field),
      '_label -> fieldName,
      'placeholder -> "",
      '_help -> "",
      '_success -> true,
      '_showIconValid -> true,
      '_showIconOnError -> true,
      '_error -> "")
  }

  /**
   * Converts an org.oil.Field to a play.api.data.Field.
   * This method should be used very sparingly and very carefully, as it sets
   * most of the play Field fields to empty or `null` values.
   * @param name
   * @param field
   * @return
   */
  protected def toPlayField(name: String, field: Field[_]): play.api.data.Field = {
    import play.api.data.{Field => PlayField}
    import play.api.data.{FormError => PlayFormError}
    PlayField(null, name, Seq.empty[(String, Seq[Any])], None, Seq.empty[PlayFormError], field.data)
  }
}

case class ValidatingInputProvider[T](field: Field[T], inputType: String) extends InputProvider[T] {
  def withField(field: Field[T]): InputProvider[T] = this.copy(field = field)

  override def includes: Html = {
    Html(s"""<script src="${controllers.Assets.at("/public", "lib/jquery-validation/jquery.validate.min.js")}"></script>""")
  }
}

object InputProviders {
  /**
   * An empty input provider does not render its field.
   */
  implicit def emptyProvider[T](field: Field[T]): InputProvider[T] = {
    val _field = field
    new InputProvider[T] {
      def field: Field[T] = _field
      def inputType: String = ""
      def withField(newField: Field[T]): InputProvider[T] = emptyProvider(newField)
      override def render(fieldName: String, args: (Symbol, Any)*)(implicit handler: B3FieldConstructor, lang: Lang): Html = Html("")
    }
  }
  implicit def textProvider(field: Field[String]): InputProvider[String] = new ValidatingInputProvider(field, "text")
  implicit def intProvider(field: Field[Int]): InputProvider[Int] = new ValidatingInputProvider(field, "number")

  /*case class HiddenInputProvider[T](field: Field[T]) extends InputProvider[T] {
    val inputType: String = "hidden"

    def withField(field: Field[T]): InputProvider[T] = this.copy(field = field)

    override def render(fieldName: String, args: (Symbol, Any)*)(implicit handler: B3FieldConstructor, lang: Lang): Html = {
      implicit val handler: B3FieldConstructor = b3.clear.fieldConstructor
      b3.hidden(fieldName, toPlayField(fieldName, field))
    }
  }
  def hiddenProvider[T](field: Field[T]): InputProvider[T] = new HiddenInputProvider[T](field)
  */
  def hiddenProvider[T](field: Field[T]): InputProvider[T] = {
    val _field = field
    new InputProvider[T] {
      def field: Field[T] = _field
      def inputType: String = "hidden"
      def withField(newField: Field[T]): InputProvider[T] = hiddenProvider(newField)

      override def render(fieldName: String, args: (Symbol, Any)*)(implicit handler: B3FieldConstructor, lang: Lang): Html = {
        implicit val handler: B3FieldConstructor = b3.clear.fieldConstructor
        b3.hidden(fieldName, _field.data.getOrElse(""))
      }
    }
  }
}
