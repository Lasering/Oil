package oil

import play.api.mvc.{Action, AnyContent, Call, Controller}

/**
 * Knows about every registered CRUDController in the application.
 * It routes the request to the corresponding CRUDController or to a error page stating
 * that the received model name does not exist or it wasn't registered.
 */
object MainController extends Controller {
  var modelControllers = Map.empty[String, CRUDController[_]]

  val baseCrumb: Seq[(String, Call)] = Seq(("CRUD", routes.MainController.index))

  private def executeOperation(model: String, f: CRUDController[_] => Action[AnyContent]): Action[AnyContent] = modelControllers.get(model) match {
    case Some(modelController) => f(modelController)
    case None => Action {
      NotFound(oil.views.html.index(modelControllers, baseCrumb,
        Some(s"""<strong>Error!</strong> Unknown model named "$model". """ +
          "Maybe you forgot to register its CRUDController.")
      ))
    }
  }

  /**
   * Lists all the models.
   */
  def index = Action {
    Ok(oil.views.html.index(modelControllers, baseCrumb))
  }

  /**
   * Shows the page to create a new model.
   */
  def createForm(model: String) = executeOperation(model, _.createForm)
  /**
   * Actually creates the model.
   */
  def create(model: String) = executeOperation(model, _.create)

  /**
   * Lists all the entries.
   */
  def listRedirect(model: String) = executeOperation(model, controller => Action {
    Redirect(routes.MainController.list(model, page = 1))
  })
  /**
   * Lists all the entries.
   */
  def list(model: String, page: Int) = executeOperation(model, _.list(page))

  /**
   * Shows the page to update an entry.
   */
  def updateForm(model: String, key: String) = executeOperation(model, _.updateForm(key))
  /**
   * Actually updates the entry with the given key.
   */
  def update(model: String, key: String) = executeOperation(model, _.update(key))

  /**
   * Deletes the entry with the given key.
   */
  def delete(model: String, key: String) = executeOperation(model, _.delete(key))
}