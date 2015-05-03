package oil

import _root_.views.html.b3.B3FieldConstructor
import play.api.i18n.Lang
import play.twirl.api.Html

trait Field[T] {
  def constraints: Seq[Constraint[T]]
  def data: Option[String]
  def formatter: Formatter[T]
  def inputProvider: InputProvider[T]

  def hasErrors: Boolean
  /**
   * Returns the list of errors associated with this field.
   * The list will be empty if this field has no errors.
   * @return the list of errors.
   */
  def errors: Seq[FormError]

  def isValid: Boolean
  /**
   * Returns this field value if `data` was successfully formatted and all `constraints` are valid.
   */
  def value: Option[T]

  def render(fieldName: String, args: (Symbol,Any)*)(implicit handler: B3FieldConstructor, lang: Lang): Html = {
    inputProvider.render(fieldName)(handler, lang)
  }

  /**
   * Returns a copy of this Field but with the given `data`.
   * @param data the new data.
   */
  def withData(data: Option[String]): Field[T]
  /**
   * Returns a copy of this Field but with the given `value`.
   *
   * Note that calling `.value` on the returned field might not return `Some(value)`, since `value`
   * may not obey all the constraints.
   * @param value the value.
   * @return the new Field.
   */
  def withValue(value: T): Field[T]
  /**
   * Returns a copy of this Field but with the given `Formatter`.
   * @param newFormatter the new Formatter.
   */
  def withFormatter(newFormatter: Formatter[T]): Field[T]
  /**
   * Returns a copy of this Field but with the given `InputProvider`.
   * @param newInputProvider the new InputProvider.
   */
  def withInputProvider(newInputProvider: InputProvider[T]): Field[T]
  /**
   * Returns a copy of this Field but with an HiddenInputProvider.
   * @return the new field.
   */
  def hidden: Field[T] = withInputProvider(InputProviders.hiddenProvider(this))
  /**
   * Returns a copy of this Field with the new constraints added.
   * @param constraints the constraints to add.
   */
  def verifying(constraints: Constraint[T]*): Field[T]

  /**
   * @return the list of all distinct constraints of this field including the subconstraints of the constraints.
   */
  val allDistinctConstraints: Seq[Constraint[T]] = constraints.flatMap(_.allSubConstraints).distinct
}

case class RequiredField[T](constraints: Seq[Constraint[T]] = Seq(Constraints.requiredAny), data: Option[String] = None)
                           (implicit val formatter: Formatter[T], val inputProviderCreator: Field[T] => InputProvider[T]) extends Field[T] {
  /**
   * _value will be:
   * · None - if a None was received in `data`.
   * · Some[Either[Seq[FormError], T]] - otherwise
   *  · Right - if `data` was successfully formatted and all `constraints` are valid.
   *  . Left - otherwise
   */
  private val _value: Option[Either[Seq[FormError], T]] = data.map{ data =>
    formatter.toType(data).fold(
      error => Left(Seq(error)),
      t => {
        val constraintErrors: Seq[FormError] = constraints.map(_.validate(t)).collect {
          case Invalid(error) => error
        }
        Either.cond(constraintErrors.isEmpty, t, constraintErrors)
      }
    )
  }

  def hasErrors: Boolean = _value.exists(_.isLeft)
  def errors: Seq[FormError] = _value.flatMap(_.left.toOption).getOrElse(Seq())

  def isValid: Boolean = hasErrors == false
  def value: Option[T] = _value.flatMap(_.right.toOption)

  val inputProvider: InputProvider[T] = inputProviderCreator(this)
  def optional: Field[Option[T]] = new OptionalField[T](this)

  def withData(data: Option[String]): Field[T] = this.copy(data = data)
  def withValue(value: T): Field[T] = this.copy(data = Some(formatter.toString(value)))
  def withFormatter(newFormatter: Formatter[T]): Field[T] = this.copy()(newFormatter, inputProviderCreator)
  def withInputProvider(newInputProvider: InputProvider[T]): Field[T] = this.copy()(formatter, (f: Field[T]) => newInputProvider.withField(f))
  def verifying(constraints: Constraint[T]*): Field[T] = this.copy(constraints = this.constraints ++ constraints)
}

case class OptionalField[T](innerField: Field[T]) extends Field[Option[T]] {
  lazy val constraints: Seq[Constraint[Option[T]]] = innerField.constraints.map(c => Constraint.toOptionalConstraint(c))
  def data: Option[String] = innerField.data
  lazy val formatter: Formatter[Option[T]] = Formatter.toOptionalFormatter(innerField.formatter)
  lazy val inputProvider: InputProvider[Option[T]] = InputProvider.toOptionalInputProvider(innerField.inputProvider)

  def hasErrors: Boolean = innerField.hasErrors
  def errors: Seq[FormError] = innerField.errors

  def isValid: Boolean = innerField.isValid
  def value: Option[Option[T]] = Some(innerField.value)

  def withData(data: Option[String]): Field[Option[T]] = this.copy(innerField.withData(data))
  def withValue(value: Option[T]): Field[Option[T]] = value.fold(this)(v => this.copy(innerField.withValue(v)))
  def withFormatter(newFormatter: Formatter[Option[T]]): Field[Option[T]] = {
    this.copy(innerField.withFormatter(Formatter.toFormatter(newFormatter)))
  }
  def withInputProvider(newInputProvider: InputProvider[Option[T]]): Field[Option[T]] = {
    this.copy(innerField.withInputProvider(InputProvider.toInputProvider(newInputProvider)))
  }
  def verifying(constraints: Constraint[Option[T]]*): Field[Option[T]] = {
    this.copy(innerField.verifying(constraints.map(c => Constraint.toConstraint(c)):_*))
  }
}

object Fields {
  import Constraints._
  import Formats._
  import InputProviders._

  implicit val empty = new RequiredField[Nothing]()(emptyFormat, emptyProvider)
  implicit val text = new RequiredField[String]()(stringFormat, textProvider)
  implicit val number = new RequiredField[Int]()(intFormat, intProvider)
  //TODO: add more fields
}
