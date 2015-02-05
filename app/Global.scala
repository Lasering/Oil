import controllers.CRUD.MainController
import controllers.Users
import play.api._
import controllers._

object Global extends GlobalSettings {
  override def onStart(app: Application) {
    Seq(Users).map(_.registerWithMainController)
  }
}