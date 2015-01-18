package controllers

import play.api._
import play.api.mvc._

object
Application extends Controller {

  def index = Action {
    Ok(views.html.index(Seq(("Index", routes.Application.index))))
  }

  def model(name: String) = Action {
    val name = "Person"
    val fields = List(("id", "Int"), ("name", "String"), ("birthday", "DateTime"), ("email", "String"), ("number_pets", "Int"), ("is_studying", "Boolean"))
    val pages = Seq(("Index", routes.Application.index), ("Person", routes.Application.model(name)))
    Ok(views.html.models.view(name, fields, pages))
  }

}
