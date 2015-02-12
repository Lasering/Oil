package org.oil

sealed trait ValidationResult
case object Valid extends ValidationResult
case class Invalid(errors: FormError*) extends ValidationResult {
  def ++(other: Invalid): Invalid = new Invalid((this.errors ++ other.errors):_*)
}

case class Constraint[T] (constraint: T => ValidationResult) extends (T => ValidationResult) {
  def this(constraint: T => Boolean, error: => FormError) = this(t => if (constraint(t)) Valid else Invalid(error))
  def apply(t: T): ValidationResult = constraint(t)
  def validate(t: T): ValidationResult = constraint(t)
}

object Constraints {
  //This allows a Constraint[T] to be used where a Constraint[Option[T]] is expected
  implicit def toOptionalConstraint[T](constraint: Constraint[T]) = new Constraint[Option[T]](v => constraint(v.get))
  //This is not implicit to avoid creating an "implicit cycle"
  def toConstraint[T](optionalConstraint: Constraint[Option[T]]) = new Constraint[T](v => optionalConstraint(Some(v)))

  /**
   * Defines a ‘required’ constraint for `String` values, i.e. one in which empty strings are invalid.
   *
   * '''name'''[constraint.required]
   * '''error'''[error.required]
   */
  val nonEmpty = new Constraint[String](data => data != null && data.trim.nonEmpty, FormError("error.required"))

  /**
   * Defines an ‘emailAddress’ constraint for `String` values which will validate email addresses.
   *
   * '''name'''[constraint.email]
   * '''error'''[error.email]
   */
  private val emailRegex = """^(?!\.)("([^"\r\\]|\\["\r\\])*"|([-a-zA-Z0-9!#$%&'*+/=?^_`{|}~]|(?<!\.)\.)*)(?<!\.)@[a-zA-Z0-9][\w\.-]*[a-zA-Z0-9]\.[a-zA-Z][a-zA-Z\.]*[a-zA-Z]$""".r
  val emailAddress = new Constraint[String](e => nonEmpty.validate(e) match {
    case invalid@Invalid(_) => invalid
    case Valid => emailRegex.findFirstMatchIn(e).map(_ => Valid).getOrElse(Invalid(FormError("error.email")))
  })

  /**
   * Defines a minimum value for `Ordered` values, by default the value must be greater than or equal to the constraint parameter
   *
   * '''name'''[constraint.min(minValue)]
   * '''error'''[error.min(minValue)] or [error.min.strict(minValue)]
   */
  def min[T](minValue: T, strict: Boolean = false)(implicit ordering: scala.math.Ordering[T]): Constraint[T] = Constraint[T] { o =>
    (ordering.compare(o, minValue).signum, strict) match {
      case (1, _) | (0, false) => Valid
      case (_, false) => Invalid(FormError("error.min", minValue))
      case (_, true) => Invalid(FormError("error.min.strict", minValue))
    }
  }

  /**
   * Defines a maximum value for `Ordered` values, by default the value must be less than or equal to the constraint parameter
   *
   * '''name'''[constraint.max(maxValue)]
   * '''error'''[error.max(maxValue)] or [error.max.strict(maxValue)]
   */
  def max[T](maxValue: T, strict: Boolean = false)(implicit ordering: scala.math.Ordering[T]): Constraint[T] = Constraint[T] { o =>
    (ordering.compare(o, maxValue).signum, strict) match {
      case (-1, _) | (0, false) => Valid
      case (_, false) => Invalid(FormError("error.max", maxValue))
      case (_, true) => Invalid(FormError("error.max.strict", maxValue))
    }
  }

  /**
   * Defines a minimum length constraint for `String` values, i.e. the string’s length must be greater than or equal to the constraint parameter
   *
   * '''name'''[constraint.minLength(length)]
   * '''error'''[error.minLength(length)]
   */
  def minLength(length: Int): Constraint[String] = Constraint[String] { o =>
    require(length >= 0, "string minLength must not be negative")
    if (o == null) Invalid(FormError("error.minLength", length)) else if (o.size >= length) Valid else Invalid(FormError("error.minLength", length))
  }

  /**
   * Defines a maximum length constraint for `String` values, i.e. the string’s length must be less than or equal to the constraint parameter
   *
   * '''name'''[constraint.maxLength(length)]
   * '''error'''[error.maxLength(length)]
   */
  def maxLength(length: Int): Constraint[String] = Constraint[String] { o =>
    require(length >= 0, "string maxLength must not be negative")
    if (o == null) Invalid(FormError("error.maxLength", length)) else if (o.size <= length) Valid else Invalid(FormError("error.maxLength", length))
  }

  /**
   * Defines a regular expression constraint for `String` values, i.e. the string must match the regular expression pattern
   *
   * '''name'''[constraint.pattern(regex)] or defined by the name parameter.
   * '''error'''[error.pattern(regex)] or defined by the error parameter.
   */
  def pattern(regex: => scala.util.matching.Regex, error: String = "error.pattern"): Constraint[String] = Constraint[String] { o =>
    require(regex != null, "regex must not be null")
    require(error != null, "error must not be null")

    if (o == null) {
      Invalid(FormError(error, regex))
    } else {
      regex.unapplySeq(o).map(_ => Valid).getOrElse(Invalid(FormError(error, regex)))
    }
  }
}