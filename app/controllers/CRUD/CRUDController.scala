package controllers.CRUD

import org.oil.Form
import play.api.db.slick.DB
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick.DBAction
import play.api.db.slick.DBSessionRequest
import scala.reflect.{ClassTag, classTag}
import scala.slick.lifted.TableQuery
import play.api.Play.current
import play.api.mvc._
import play.twirl.api.Html

trait FilterByKey {
  import scala.slick.lifted.Column
  //The key is always a string since it is received via URL (see the CRUD.routes)
  def filterByKey(key: String): Column[Boolean]
}

//M = model Type
abstract class CRUDController[M: ClassTag](val tableQuery: TableQuery[_ <: Table[M] with FilterByKey]) extends Controller {
  //It would be nice not using the ClassTag to get this
  val modelName = classTag[M].runtimeClass.getSimpleName

  def registerWithMainController: Unit = MainController.modelControllers += modelName -> this
  
  val form: Form[M, _ <: Product]
  val toIndex: Call = routes.MainController.index
  
  val pageSize: Int = 20
  val maxPageSize: Int = 20
  
  def renderList(list: List[M]): Html = {
    val names: List[String] = form.fields.keys.toList

    val fields: List[List[String]] = list.map { element =>
      form.fill(element).fields.values.map(_.data.getOrElse("")).toList
    }
    views.html.CRUD.model(modelName, count, names, fields)

    //views.html.CRUD.model(modelName, form.fields.keys, list)
    //return render(templateForList(), with(Page.class, p));
    //views.html.CRUD.index("renderList")
  }

  def renderForm(form: Form[M, _], key: Option[String] = None): Html = {
    //return render(templateForForm(), with(keyClass, key).and(Form.class, form));
    views.html.CRUD.form(modelName, form)
  }
  def renderShow(model: M): Html = {
    //return render(templateForShow(), with(modelClass, model));
    //views.html.CRUD.index("renderShow")
    ???
  }
  def renderNotFound(key: String): Html = {
    //views.html.CRUD.index(s"There is no such record key=$key")
    ???
  }

  final def ActionWithModel(key: String)(f: (M, DBSessionRequest[_]) => Result) = DBAction {implicit rs =>
    tableQuery.filter(_.filterByKey(key)).firstOption(rs.dbSession) match {
      case None => NotFound(renderNotFound(key))
      case Some(model) => f(model, rs)
    }
  }

  //Lists all the entries for this model.
  def list(page: Int) = DBAction { implicit rs =>
    val entriesPerPage = rs.request.session.get("entriesPerPage")
      .map(s => Math.min(s.toInt, maxPageSize))
      .getOrElse(pageSize)

    val offset = entriesPerPage * (page - 1)
    val entriesForCurrentPage: List[M] = tableQuery.drop(offset).take(entriesPerPage).list(rs.dbSession)

    Ok(renderList(entriesForCurrentPage))
  }

  //Shows the page to create a new model.
  def createForm = Action {
    Ok(renderForm(form))
  }
  //Actually creates the model.
  def create = DBAction { implicit rs =>
    form.bindFromRequest.fold(
      formWithErrors => BadRequest(renderForm(formWithErrors)),
      model => {
        tableQuery.insert(model)(rs.dbSession)
        Redirect(toIndex)
      })
  }

  //Shows (reads) the model entry with the given key.
  def show(key: String) = ActionWithModel(key) { (model, rs) =>
    Ok(renderShow(model))
  }

  //Edit form to edit the entry with the given key.
  def updateForm(key: String) = ActionWithModel(key) { (model, rs) =>
    Ok(renderForm(form.fill(model), Some(key)))
  }
  //Actually updates the entry with the given key.
  def update(key: String) = ActionWithModel(key) { (model, rs) =>
    form.fill(model).bindFromRequest()(rs.request).fold(
      formWithErrors => BadRequest(renderForm(formWithErrors, Some(key))),
      model => {
        tableQuery.update(model)(rs.dbSession)
        Redirect(toIndex)
      }
    )
  }

  //Deletes the entry with the given key.
  def delete(key: String) = DBAction { implicit rs =>
    implicit val session = rs.dbSession
    val row = tableQuery.filter(_.filterByKey(key))
    if (row.list.size == 0) {
      NotFound(renderNotFound(key))
    } else {
      row.delete
      Redirect(toIndex)
    }
  }

  //Returns the number of entries in the database
  def count: Long = DB.withSession { implicit session =>
    tableQuery.length.run
  }
}
