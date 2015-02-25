package org.oil

import play.twirl.api.Html

object ValidationResult {
  def apply(result: Boolean, formError: => FormError): ValidationResult = if (result) Valid else Invalid(formError)
}
sealed trait ValidationResult
case object Valid extends ValidationResult
case class Invalid(error: FormError) extends ValidationResult

object Constraint {
  //This allows a Constraint[T] to be used where a Constraint[Option[T]] is expected
  implicit def toOptionalConstraint[T](constraint: Constraint[T]): Constraint[Option[T]] = new Constraint[Option[T]](constraint.name) {
    override def validate(value: Option[T]): ValidationResult = constraint.validate(value.get)
  }
  //This is not implicit to avoid creating an "implicit cycle"
  def toConstraint[T](optionalConstraint: Constraint[Option[T]]): Constraint[T] = new Constraint[T](optionalConstraint.name){
    override def validate(value: T): ValidationResult = optionalConstraint.validate(Some(value))
  }

  def apply[T](name: String, constraint: T => Boolean, error: => FormError) = new Constraint[T](name) {
    override def validate(value: T): ValidationResult = ValidationResult(constraint(value), error)
  }
}
abstract class Constraint[T] (val name: String) {
  def validate(value: T): ValidationResult
}

object ClientSideConstraint {
  def apply[T](name: String, parameters: String, constraint: T => Boolean, error: => FormError) = new ClientSideConstraint[T](name, Html(parameters)) {
    override def validate(value: T): ValidationResult = ValidationResult(constraint(value), error)
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
abstract class ClientSideConstraint[T](name: String, parameters: Html = Html("")) extends Constraint[T](name) {
  def this(name: String, parameters: String) = this(name, Html(parameters))

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
}

object Constraints {
  //jQueryValidation required method works with text inputs, selects, checkboxes and radio buttons.
  //TODO: does this constraint have a relation to the RequiredField?
  /**
   * @see http://jqueryvalidation.org/required-method
   */
  val required: ClientSideConstraint[String] = {
    ClientSideConstraint[String]("required", "true", data => data != null && data.trim.nonEmpty, FormError("error.required"))
  }

  //jQueryValidation required method works with text inputs, selects and checkboxes.
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
    ClientSideConstraint[String]("minlength", length.toString,
      value => value != null && value.size >= length, FormError("error.minLength", length))
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
    ClientSideConstraint[String]("maxlength", length.toString,
      value => value != null && value.size <= length, FormError("error.maxLength", length))
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
    ClientSideConstraint[String]("rangelength", s"[$minLength, $maxLength]",
      data => data != null && data.trim.length >= minLength && data.trim.length <= maxLength, FormError("error.rangeLength", minLength, maxLength))
  }

  import scala.math.Ordering
  //jQueryValidation required method works with text inputs.
  /**
   * Defines a minimum value for `Ordered` values, i.e. the value must be greater than or equal to `minValue`.
   *
   * '''name'''[min]
   * '''error'''[error.min(minValue)]
   * @see http://jqueryvalidation.org/min-method
   */
  def min[T](minValue: T)(implicit ordering: Ordering[T]) = ClientSideConstraint[T]("min", minValue.toString,
    value => ordering.gteq(value, minValue), FormError("error.min", minValue))
  /**
   * Defines a maximum value for `Ordered` values, i.e. the value must be less than or equal to `maxValue`.
   *
   * '''name'''[max]
   * '''error'''[error.max(maxValue)]
   * @see http://jqueryvalidation.org/max-method
   */
  def max[T](maxValue: T)(implicit ordering: Ordering[T]) = ClientSideConstraint[T]("max", maxValue.toString,
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
    ClientSideConstraint[T]("range", s"[$minValue, $maxValue]",
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
  def pattern(regex: Regex, error: String = "error.pattern") = new ClientSideConstraint[String]("regex") {
    require(regex != null, "regex must not be null")
    require(error != null, "error must not be null")

    override def validate(value: String): ValidationResult = ValidationResult(regex.unapplySeq(value).isDefined, FormError(error, regex))

    //TODO: add a jqueryValidation method to validate regexes
    //override def validationMethod: Option[Html] = Some(Html())
  }

  /**
   * Defines an ‘emailAddress’ constraint for `String` values which will validate email addresses.
   *
   * '''name'''[constraint.email]
   * '''error'''[error.email]
   */
  /*private val emailRegex = """^(?!\.)("([^"\r\\]|\\["\r\\])*"|([-a-zA-Z0-9!#$%&'*+/=?^_`{|}~]|(?<!\.)\.)*)(?<!\.)@[a-zA-Z0-9][\w\.-]*[a-zA-Z0-9]\.[a-zA-Z][a-zA-Z\.]*[a-zA-Z]$""".r
  val emailAddress = new Constraint[String](e => nonEmpty.validate(e) match {
    case invalid@Invalid(_) => invalid
    case Valid => emailRegex.findFirstMatchIn(e).map(_ => Valid).getOrElse(Invalid(FormError("error.email")))
  })*/

  /*List of jQuery validation built-in validation methods:
  required – Makes the element required.
  remote – Requests a resource to check the element for validity

  email – Makes the element require a valid email
  url – Makes the element require a valid url
  dateISO – Makes the element require an ISO date.
  number – Makes the element require a decimal number.
  digits – Makes the element require digits only.

  date – Makes the element require a date.
  equalTo – Requires the element to be the same as another one*/
}