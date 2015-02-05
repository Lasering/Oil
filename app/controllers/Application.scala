package controllers

import models.Database
import play.api._
import play.api.mvc._

import scala.slick.lifted.MappedProjection

object Application extends Controller {

 
  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }
  
}
