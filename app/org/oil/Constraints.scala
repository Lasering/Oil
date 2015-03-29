package org.oil

import play.twirl.api.Html

object ValidationResult {
  def apply(result: Boolean, formError: => FormError): ValidationResult = if (result) Valid else Invalid(formError)
}
sealed trait ValidationResult
case object Valid extends ValidationResult
//TODO: review if Invalid should keep a list of FormErrors
case class Invalid(error: FormError) extends ValidationResult

object Constraint {
  //This allows a Constraint[T] to be used where a Constraint[Option[T]] is expected
  implicit def toOptionalConstraint[T](constraint: Constraint[T]): Constraint[Option[T]] = new Constraint[Option[T]](constraint.name) {
    override def validateInternal(value: Option[T]): ValidationResult = constraint.validate(value.get)
  }
  //This is not implicit to avoid creating an "implicit cycle"
  def toConstraint[T](optionalConstraint: Constraint[Option[T]]): Constraint[T] = new Constraint[T](optionalConstraint.name){
    override def validateInternal(value: T): ValidationResult = optionalConstraint.validate(Some(value))
  }

  // def apply[T](name: String, constraint: T => Boolean): Constraint[T] = apply(name, constraint, FormError(s"error.$name"))
  def apply[T](name: String, constraint: T => Boolean, error: => FormError) = new Constraint[T](name) {
    override def validateInternal(value: T): ValidationResult = ValidationResult(constraint(value), error)
  }
}
abstract class Constraint[-T] (val name: String, val subConstraints: Seq[Constraint[T]] = Seq.empty) {
  protected def validateInternal(value: T): ValidationResult

  val allSubConstraints: Seq[Constraint[T]] = subConstraints.flatMap(_.allSubConstraints).distinct

  final def validate(value: T): ValidationResult = {
    (allSubConstraints :+ this).foldLeft[ValidationResult](Valid) { (accumulator, constraint) =>
      accumulator match {
        case Valid => constraint.validateInternal(value)
        //By doing this we only return the first error we encounter. And also we terminate the execution as soon as we find this error.
        //TODO: Maybe it would be interesting to return all the errors.
        case invalid: Invalid => return invalid
      }
    }
  }
  
  def canEqual(other: Any): Boolean = other.isInstanceOf[Constraint[_]]

  override def equals(other: Any): Boolean = other match {
    case that: Constraint[_] =>
      (that canEqual this) &&
        name == that.name &&
        subConstraints == that.subConstraints
    case _ => false
  }

  override def hashCode(): Int = Seq(name, subConstraints).map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
}

object ClientSideConstraint {
  def apply[T](name: String, parameters: String, subConstraints: Seq[Constraint[T]], constraint: T => Boolean, error: FormError): ClientSideConstraint[T] = new ClientSideConstraint[T](name, parameters, subConstraints) {
    override def validateInternal(value: T): ValidationResult = ValidationResult(constraint(value), error)
  }
}
/**
 * If used in conjunction with a ValidatingInputProvider this type of Constraint will also perform client-side form
 * validation using jQuery Validation (http://jqueryvalidation.org/).
 * This has the added benefit of uniforming server-side and client-side validation.
 * @param name the name of a built-in validation method from jQuery Validation. Or the name used in the validationMethod.
 *             Must be a valid Javascript identifier.
 * @param parameters the parameters to the validation method.
 * @tparam T
 */
abstract class ClientSideConstraint[-T](name: String, val parameters: String, subConstraints: Seq[Constraint[T]] = Seq.empty) extends Constraint[T](name, subConstraints) {
  /**
   * The client-side validation method. It should use `name` and message should be the same returned in
   * FormError of the Invalid ValidationResult.
   * @example {{{
   * Query.validator.addMethod("math", function(value, element, params) {
   *   return this.optional(element) || value == params[0] + params[1];
   * }, jQuery.validator.format("Please enter the correct value for {0} + {1}"));
   * }}}
   * @return
   */
  def validationMethod: Option[Html] = None

  override def canEqual(other: Any): Boolean = other.isInstanceOf[ClientSideConstraint[_]]

  override def equals(other: Any): Boolean = other match {
    case that: ClientSideConstraint[_] =>
      (that canEqual this) &&
        parameters == that.parameters &&
        super.equals(that)
    case _ => false
  }

  override def hashCode(): Int = Seq(super.hashCode(), parameters.hashCode).foldLeft(0)((a, b) => 31 * a + b)
}

object Constraints {
  /**
   * @see http://jqueryvalidation.org/required-method
   */
  val requiredAny: ClientSideConstraint[Any] = {
    ClientSideConstraint[Any]("required", "true", Seq.empty, data => data != null, FormError("error.required"))
  }

  val required: ClientSideConstraint[String] = {
    ClientSideConstraint[String]("required", "true", Seq(requiredAny), data => data.trim.nonEmpty, FormError("error.required"))
  }

  /**
   * Defines a minimum length constraint for `String` values, i.e. the string’s length must be
   * greater than or equal to `length`.
   *
   * '''name'''[minlength]
   * '''error'''[error.minLength(length)]
   * @see http://jqueryvalidation.org/minlength-method
   */
  def minLength(length: Int) = {
    require(length >= 0, "length must not be negative")
    ClientSideConstraint[String]("minlength", length.toString, Seq(required),
      value => value.size >= length, FormError("error.minLength", length))
  }
  /**
   * Defines a maximum length constraint for `String` values, i.e. the string’s length must be
   * less than or equal to `length`.
   *
   * '''name'''[maxLength]
   * '''error'''[maxLength(length)]
   * @see http://jqueryvalidation.org/maxlength-method
   */
  def maxLength(length: Int) = {
    require(length >= 0, "length must not be negative")
    ClientSideConstraint[String]("maxlength", length.toString, Seq(required),
      value => value.size <= length, FormError("error.maxLength", length))
  }
  /**
   * Defines a range length constraint for `String` values, i.e. the string’s length must be
   * between `minLength` and `maxLength`.
   *
   * '''name'''[rangelength]
   * '''error'''[rangeLength(minLength, maxLength)]
   * @see http://jqueryvalidation.org/rangelength-method
   */
  def rangeLength(minLength: Int, maxLength: Int) = {
    require(minLength <= maxLength, "minLength must be less than or equal to maxLength")
    ClientSideConstraint[String]("rangelength", s"[$minLength, $maxLength]", Seq(required),
      data => data.trim.length >= minLength && data.trim.length <= maxLength, FormError("error.rangeLength", minLength, maxLength))
  }

  import scala.math.Ordering
  /**
   * Defines a minimum value for `Ordered` values, i.e. the value must be greater than or equal to `minValue`.
   *
   * '''name'''[min]
   * '''error'''[error.min(minValue)]
   * @see http://jqueryvalidation.org/min-method
   */
  def min[T](minValue: T)(implicit ordering: Ordering[T]) = ClientSideConstraint[T]("min", minValue.toString, Seq.empty,
    value => ordering.gteq(value, minValue), FormError("error.min", minValue))
  /**
   * Defines a maximum value for `Ordered` values, i.e. the value must be less than or equal to `maxValue`.
   *
   * '''name'''[max]
   * '''error'''[error.max(maxValue)]
   * @see http://jqueryvalidation.org/max-method
   */
  def max[T](maxValue: T)(implicit ordering: Ordering[T]) = ClientSideConstraint[T]("max", maxValue.toString, Seq.empty,
    value => ordering.lteq(value, maxValue), FormError("error.max", maxValue))
  /**
   * Defines a range value for `Ordered` values, i.e. the value must be between `minValue` and `maxValue`.
   *
   * '''name'''[range]
   * '''error'''[error.range(minValue, maxValue)]
   * @see http://jqueryvalidation.org/range-method
   */
  def range[T](minValue: T, maxValue: T)(implicit ordering: Ordering[T]) = {
    require(ordering.lteq(minValue, maxValue), "minValue must be less than or equal to maxValue")
    ClientSideConstraint[T]("range", s"[$minValue, $maxValue]", Seq.empty,
      //We only have three values if value ends up in the second position then minValue <= value <= maxValue
      value => Seq(minValue, value, maxValue).sorted(ordering)(1) == value,
      FormError("error.range", minValue, maxValue))
  }

  import scala.util.matching.Regex
  /**
   * Defines a regular expression constraint for `String` values, i.e. the string must match the regular expression pattern.
   *
   * @param regex Must simultaneously be a regex accepted by Scala and javascript.
   * @param error
   * '''name'''[pattern].
   * '''error'''[error.pattern(regex)] or defined by the error parameter.
   */
  def pattern(regex: Regex, error: String = "error.pattern") = new ClientSideConstraint[String]("regex", regex.regex) {
    require(regex != null, "regex must not be null")
    require(error != null, "error must not be null")

    def validateInternal(value: String): ValidationResult = ValidationResult(regex.unapplySeq(value).isDefined, FormError(error, regex))

    //TODO: add a jqueryValidation method to validate regexes
    //override def validationMethod: Option[Html] = Some(Html())
  }

  /*List of jQuery validation built-in validation methods:
  remote – Requests a resource to check the element for validity

  email – Makes the element require a valid email
  url – Makes the element require a valid url
  dateISO – Makes the element require an ISO date.
  number – Makes the element require a decimal number.
  digits – Makes the element require digits only.

  date – Makes the element require a date.
  equalTo – Requires the element to be the same as another one*/
}
