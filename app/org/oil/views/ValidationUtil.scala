package org.oil.views

import org.oil.{ClientSideConstraint, Constraint}
import play.twirl.api.Html
import org.oil.Form

class ValidationUtil(val form: Form[_, _]) {
  private var _allInputProvidersIncludes = Set.empty[Html]
  private var _allInputProvidersOnReady = Set.empty[Html]
  private var _allConstraintsCustomValidationMethods = Set.empty[Html]
  private var _clientSideConstraintsByField = Map.empty[String, Seq[ClientSideConstraint[_]]]

  form.fields.foreach{ case (name, field) =>
    val inputProvider = field.inputProvider
    _allInputProvidersIncludes += inputProvider.includes
    _allInputProvidersOnReady += inputProvider.onReady
    println(s"$name: A${inputProvider.onReady}B")


    val clientSideConstraints: Seq[ClientSideConstraint[_]] = field.allDistinctConstraints.map {
      case clientSideConstraint: ClientSideConstraint[_] => clientSideConstraint
    }
    _allConstraintsCustomValidationMethods ++= clientSideConstraints.map(_.validationMethod).flatten[Html]
    if (clientSideConstraints.nonEmpty) {
      _clientSideConstraintsByField += name -> clientSideConstraints
    }
  }

  val allInputProvidersIncludes: Set[Html] = ensureUniqueness(_allInputProvidersIncludes)
  val allInputProvidersOnReady: Set[Html] = ensureUniqueness(_allInputProvidersOnReady)
  val allConstraintsCustomValidationMethods: Set[Html] = ensureUniqueness(_allConstraintsCustomValidationMethods)
  val clientSideConstraintsByField: Map[String, Seq[ClientSideConstraint[_]]] = _clientSideConstraintsByField

  //This method should not be necessary but in the current version of Twirl the class HTML does not implement
  //equals and hashCode. So we resorted to this approach to ensure there are not repeated HTML in the set.
  //TODO: remove this line when the new version of Twirl is used
  private def ensureUniqueness(set: Set[Html]): Set[Html] = set.map(_.toString()).map(new Html(_))
}
