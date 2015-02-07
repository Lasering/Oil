package controllers

import models.Database
import play.api._
import play.api.mvc._

import scala.slick.lifted.MappedProjection

object Application extends Controller {

  def index = Action {
    Ok(views.html.index())
  }

  def model(name: String) = Action {
    val name = "Person"
    val fields = List(("id", "Int"), ("name", "String"), ("birthday", "DateTime"), ("email", "String"), ("number_pets", "Int"), ("is_studying", "Boolean"))
    Ok(views.html.models.view(name, fields))
  }
}

