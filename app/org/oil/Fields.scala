package org.oil

//TODO: maybe it would be nice to have the field name
case class Field[T](constraints: Seq[Constraint[T]] = Seq.empty, data: Option[String] = None)(implicit formatter: Formatter[T], inputProvider: InputProvider[T]) {
  /**
   * The _value of this field. Which will be:
   * · None - if a None was received in {@code data}.
   * · Some[Either[Seq[FormError], T]] - otherwise
   *  · Right - if {@code data} was successfully formatted and all {@code constraints} are valid.
   *  . Left - otherwise
   */
  private val _value: Option[Either[Seq[FormError], T]] = data.map{ data =>
    formatter.toType(data).fold(
      error => Left(Seq(error)),
      t => {
        val constraintErrors = constraints.map(_.validate(t)).collect {
          case Invalid(errors @ _*) => errors
        }.flatten
        if (constraintErrors.nonEmpty) Left(constraintErrors) else Right(t)
      }
    )
  }

  def hasErrors: Boolean = _value.map(_.isLeft).getOrElse(false)

  /**
   * Returns the list of errors associated with this field.
   * The list will be empty if this field has no errors.
   * @return the list of errors
   */
  def errors: Seq[FormError] = _value.flatMap(_.left.toOption).getOrElse(Seq())

  def isValid: Boolean = hasErrors == false

  /**
   * Returns this field value if {@code data} was successfully formatted and all {@code constraints} are valid.
   * @return the value
   */
  def value: Option[T] = _value.flatMap(_.right.toOption)


  /**
   * Constructs a new Field based on this one, but with the given {@code data}.
   * @param data the new data
   * @return the new field
   */
  def withData(data: String) = this.copy(data = Some(data))

  /**
   * Constructs a new Field based on this one but with the given {@code _value}.
   *
   * Note however that calling ._value on the new field might not return {@code Some(Right(_value))}.
   * This is due to the fact that the {@code _value} is converted by the formatter to String which is
   * then used to construct the new Field.
   * @param _value the _value
   * @return the new Field
   */
  def withValue(value: T) = this.copy(data = Some(formatter.toString(value)))

  /**
   * Constructs a new Field based on this one, but with the given {@code Formatter}.
   * @param newFormatter the new Formatter
   * @return the new Field
   */
  def withFormatter(newFormatter: Formatter[T]) = this.copy()(newFormatter, inputProvider)

  /**
   * Constructs a new Field based on this one, but with the given {@code InputProvider}.
   * @param newInputProvider the new InputProvider
   * @return the new Field
   */
  def withInputProvider(newInputProvider: InputProvider[T]) = this.copy()(formatter, newInputProvider)

  /**
   * Constructs a new Field based on this one, by adding new constraints.
   *
   * @param constraints the constraints to add
   * @return the new Field
   */
  def verifying(constraints: Constraint[T]*): Field[T] = this.copy(constraints = this.constraints ++ constraints)
}

object Fields {
  import Constraints._
  import Formats._
  import InputProviders._

  implicit val empty = new Field[Nothing]()(emptyFormat, emptyProvider)
  implicit val text = new Field[String]()(stringFormat, textProvider)
  //will val test = new Field[String]() be equal to text?
  implicit val number = new Field[Int]()(intFormat, intProvider)

}
