package controllers

import models._
import org.oil.{Constraints, CRUDController}
import org.oil.Forms._
import org.oil.Fields._

object Users extends CRUDController(Database.Users) {
  val form = (
    "id" -> number.optional.hidden,
    "age" -> number.verifying(Constraints.min(5)),
    "street_address" -> text,
    "city" -> text,
    "zip_code" -> text,
    "country" -> text,
    "email" -> text,
    "telephone_number" -> text
  ) <> (User.apply, User.unapply)
}