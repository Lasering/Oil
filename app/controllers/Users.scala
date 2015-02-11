package controllers

import controllers.CRUD.CRUDController
import models._
import org.oil.Forms._
import org.oil.Fields._

object Users extends CRUDController(Database.Users) {
  val form = (
    "id" -> number.optional,
    "gender" -> text,
    "street address" -> text,
    "city" -> text,
    "zip code" -> text,
    "country" -> text,
    "email" -> text,
    "telephone number" -> text
  ) <> (User.apply, User.unapply)
}