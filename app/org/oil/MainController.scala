package org.oil

import play.api.mvc.{Action, AnyContent, Call, Controller}

object MainController extends Controller {
  var modelControllers = Map.empty[String, CRUDController[_]]

  private def executeOperation(model: String, f: CRUDController[_] => Action[AnyContent]): Action[AnyContent] = modelControllers.get(model) match {
    case Some(modelController) => f(modelController)
    case None => Action {
      //TODO: render a template
      NotFound(s"There is no model named $model")
    }
  }

  /**
   * GET		/						controllers.CRUDController.index()
   *
   * Lists all the models.
   */
  def index = Action {
    val models = for ((key, controller) <- modelControllers) yield (key, controller.count)

    val indexCrumb = List[(String, Boolean, Call)](("CRUD", true, routes.MainController.index()))

    Ok(views.html.index(models, indexCrumb))
  }

  /**
   * GET     /:model/new         	controllers.CRUDController.newForm(model: String)
   *
   * Shows the page to create a new model.
   */
  def createForm(model: String) = executeOperation(model, _.createForm)
  /**
   * POST	/:model/create			controllers.CRUDController.create(model: String)
   *
   * Actually creates the model.
   */
  def create(model: String) = executeOperation(model, _.create)

  /**
   * GET		/:model/list			controllers.CRUDController.listRedirect(model: String)
   *
   * Lists all the entries for a given model.
   */
  def listRedirect(model: String) = Action {
    Redirect(routes.MainController.list(model, page = 1))
  }
  /**
   * GET		/:model/list			controllers.CRUDController.list(model: String)
   *
   * Lists all the entries for a given model.
   */
  def list(model: String, page: Int) = executeOperation(model, _.list(page))

  /**
   * GET		/:model/:key/edit		controllers.CRUDController.editForm(model: String, key: String)
   *
   * Edit form to edit the entry with the given key for the given model.
   */
  def updateForm(model: String, key: String) = executeOperation(model, _.updateForm(key))
  /**
   * POST	/:model/:key/update		controllers.CRUDController.update(model: String, key: String)
   *
   * Actually edits (updates) the entry with the given key for the given model.
   */
  def update(model: String, key: String) = executeOperation(model, _.update(key))

  /**
   * GET		/:model/:key/delete		controllers.CRUDController.delete(model: String, key: String)
   *
   * Deletes the entry with the given key for the given model.
   */
  def delete(model: String, key: String) = executeOperation(model, _.delete(key))
}