package org.oil.views

import org.oil.{ClientSideConstraint, Constraint}
import play.twirl.api.Html
import org.oil.Form

class ValidationUtil(val form: Form[_, _]) {
  private var _allInputProvidersIncludes = Set.empty[Html]
  private var _allInputProvidersOnReady = Set.empty[Html]
  private var _allConstraintsCustomValidationMethods = Set.empty[Html]

  form.fields.values.foreach{ case field =>
    val inputProvider = field.inputProvider
    _allInputProvidersIncludes += inputProvider.includes
    _allInputProvidersOnReady += inputProvider.onReady

    _allConstraintsCustomValidationMethods ++= field.allDistinctConstraints.map {
      case clientSideConstraint: ClientSideConstraint[_] => clientSideConstraint.validationMethod
      case _ => None
    }.flatten[Html]
  }


  val allInputProvidersIncludes: Set[Html] = ensureUniqueness(_allInputProvidersIncludes)
  val allInputProvidersOnReady: Set[Html] = ensureUniqueness(_allInputProvidersOnReady)
  val allConstraintsCustomValidationMethods: Set[Html] = ensureUniqueness(_allConstraintsCustomValidationMethods)

  //This method should not be necessary but in the current version of Twirl the class HTML does not implement
  //equals and hashCode. So we resorted to this approach to ensure no repeated includes are, well, included.
  //TODO: remove this line when the new version of Twirl is used
  private def ensureUniqueness(set: Set[Html]): Set[Html] = set.map(_.toString()).map(new Html(_))
}
