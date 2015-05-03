package oil

import play.api.Play.current
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick.{DB, DBAction, DBSessionRequest}
import play.api.mvc._
import play.twirl.api.Html

import scala.reflect.{ClassTag, classTag}
import scala.slick.jdbc.JdbcBackend
import scala.slick.lifted.TableQuery

trait FilterByKey {
  import scala.slick.lifted.Column
  //The key is always a string since it is received via URL (see the oil.routes)
  //For a given key only one entry from the DB should be returned
  def filterByKey(key: String): Column[Boolean]
}

/**
 * Performs CRUD operations for a specific domain class `M`.
 * @param tableQuery the table query to which DB operations will be performed.
 * @tparam M the type of the domain class this controller handles.
 */
abstract class CRUDController[M: ClassTag](val tableQuery: TableQuery[_ <: Table[M] with FilterByKey]) extends Controller {
  //FIXME: It would be nice not using the ClassTag to get this
  //FIXME: We could also make it this.getClass.getSimpleName
  val modelName = classTag[M].runtimeClass.getSimpleName

  final def registerWithMainController: Unit = MainController.modelControllers += modelName -> this

  //TODO: we have to guarantee that somehow the form contains a field for the key (for FilterByKey)
  //TODO: right now we make the assumption it is the first field
  val form: Form[M, _ <: Product]

  //The view to which this redirect leads to must use the flash scope to read the feedback messages.
  //There cannot exist any intermediate redirects as the flash scope would be lost.
  val redirectTo = routes.MainController.list(modelName, 1)

  val maxEntriesPerPage: Int = 100
  val defaultEntriesPerPage: Int = 20

  val crumbsWithModel: Seq[(String, Call)] = {
    MainController.baseCrumb :+ (modelName, routes.MainController.listRedirect(modelName))
  }

  def renderForm(form: Form[M, _], key: Option[String] = None): Html = {
    val crumbs = crumbsWithModel :+ ("Create", routes.MainController.createForm(modelName))

    oil.views.html.form(modelName, form, crumbs)
  }
  //We need to have the request implicit so that we can access the flash scope
  def renderList(entries: Seq[M])(implicit request: Request[_]): Html = {
    val fields: Seq[Seq[String]] = entries.map { entry =>
      form.fill(entry).fields.values.map(_.data.getOrElse("")).toSeq
    }

    oil.views.html.list(modelName, count, form.fields.keys.toSeq, fields, crumbsWithModel)
  }

  def redirectWithFeedback(feedbackType: String, feedbackMessage: String): Result = {
    Redirect(redirectTo).flashing(
      "feedbackType" -> feedbackType,
      //TODO: change this to a messageKey so that we can internationalize it
      "feedbackMessage" -> feedbackMessage
    )
  }

  /**
   * Returns the result of applying the given function `f` on the entry returned by filtering using `key`.
   *
   * If no entry can be found by filtering with `key`, or if more than one entry can be found an error is
   * shown via [[oil.CRUDController.redirectWithFeedback]].
   *
   * @param key the key to use in the filter.
   * @param f the function to invoke.
   * @return the action with the result from `f`.
   */
  final def actionWithModel(key: String)(f: (M, DBSessionRequest[_]) => Result): Action[AnyContent] = DBAction { implicit rs =>
    implicit val session = rs.dbSession
    val row = tableQuery.filter(_.filterByKey(key))
    row.size.run match {
      //TODO: change this to a messageKey so that we can internationalize it
      case 0 => redirectWithFeedback("danger", s"<strong>Error!</strong> There is no entry with key = $key")
      case 1 =>
        f(row.first, rs)
      case _ =>
        //This should not happen as FilterByKey should always return just one entry
        //But the programmer might have implemented the FilterByKey wrongly so this serves as a safeguard.
        //TODO: change this to a messageKey so that we can internationalize it
        redirectWithFeedback("danger", s"More than one $modelName with key = $key exists. " +
          "FilterByKey should return only ONE entry for a given key. " +
          "Did nothing!")
    }
  }

  /**
   * Shows the page to create a new entry.
   */
  def createForm = Action {
    Ok(renderForm(form))
  }
  /**
   * Actually creates a new entry. Or redirects back to the form page if the form contained errors.
   * If the update is successful it will redirect to the list page. Override [[oil.CRUDController.redirectTo]]
   * if you want to redirect somewhere else.
   */
  def create = DBAction { implicit rs =>
    form.bindFromRequest.fold(
      formWithErrors => BadRequest(renderForm(formWithErrors)),
      model => {
        //TODO: we must handle the case when the insert fails
        tableQuery.insert(model)(rs.dbSession)
        //TODO: change this to a messageKey so that we can internationalize it
        redirectWithFeedback("success", s"<strong>Success:</strong> created new $modelName.")
      }
    )
  }

  /**
   * Lists all the entries in `page`.
   *
   * If `page` is <= 0 then a redirect to page 1 will be sent.
   * If `page` is > MaxPageNumber then a redirect to page MaxPageNumber will be sent.
   *
   * The number of entries per page is calculated from the `entriesPerPage` request session attribute.
   * This number will be capped to be at most `maxEntriesPerPage` (which defaults to 100) to prevent denial of
   * service attacks. You can always override it to something bigger.
   * If no value for `entriesPerPage` is defined then the value `defaultEntriesPerPage` (which defaults to 20)
   * will be used. You can always override it to something bigger.
   *
   * @param page the page number
   */
  def list(page: Int) = DBAction { implicit rs =>
    val entriesPerPage = rs.request.session.get("entriesPerPage")
      .map(s => Math.min(s.toInt, maxEntriesPerPage))
      .getOrElse(defaultEntriesPerPage)

    val maxPageNumber = Math.ceil(count() / entriesPerPage.toDouble).toInt

    //This ensures 1 <= page <= MaxPageNumber
    val normalizedPageNumber = Math.max(1, Math.min(page, maxPageNumber))

    if (normalizedPageNumber != page) {
      Redirect(routes.MainController.list(modelName, normalizedPageNumber))
    } else {
      val offset = entriesPerPage * (normalizedPageNumber - 1)
      val entries = tableQuery.drop(offset).take(entriesPerPage).list(rs.dbSession)

      Ok(renderList(entries))
    }
  }

  /**
   * Edit form to edit the entry with the given key.
   * If no entry exists for the given `key` an error will be shown.
   * @param key the key to use in the filter
   */
  def updateForm(key: String) = actionWithModel(key) { (model, rs) =>
    Ok(renderForm(form.fill(model), Some(key)))
  }
  /**
   * Actually updates the entry with the given key. Or redirects back to the form page if the form contained errors.
   * If no entry exists for the given `key` an error will be shown.
   * If the update is successful it will redirect back to the list page. Override [[oil.CRUDController.redirectTo]]
   * if you want to redirect somewhere else.
   * @param key the key to use in the filter
   */
  def update(key: String) = actionWithModel(key) { (model, rs) =>
    form.fill(model).bindFromRequest()(rs.request).fold(
      formWithErrors => BadRequest(renderForm(formWithErrors, Some(key))),
      model => {
        //TODO: we must handle the case when the update fails
        tableQuery.update(model)(rs.dbSession)
        //TODO: change this to a messageKey so that we can internationalize it
        redirectWithFeedback("success", s"<strong>Success:</strong> edited $modelName with key = $key.")
      }
    )
  }

  /**
   * Deletes the entry with the given key. If no model exists for the given `key` an error will be shown.
   * If the delete is successful it will redirect back to the list page. Override [[oil.CRUDController.redirectTo]]
   * if you want to redirect somewhere else.
   * @param key the key to use in the filter
   */
  def delete(key: String) = actionWithModel(key) { (model, rs) =>
    //We can safely delete because ActionWithModel already ensures only one entry exists for key
    tableQuery.filter(_.filterByKey(key)).delete(rs.dbSession)
    //TODO: change this to a messageKey so that we can internationalize it
    redirectWithFeedback("success", s"<strong>Success:</strong> deleted $modelName with key = $key.")
  }

  /**
   * @return the number of entries in the database
   */
  def count(): Long = DB.withSession { implicit session =>
    tableQuery.length.run
  }
}
