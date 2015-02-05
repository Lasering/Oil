package controllers

import controllers.CRUD.CRUDController
import models._
import org.oil.Forms._
import org.oil.Fields._

object Users extends CRUDController(Database.Users) {
  val form = (
    "id" -> number,
    "name" -> text,
    "age" -> number
  ) <> (User.apply, User.unapply)
}