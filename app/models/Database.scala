package models

import play.api.db.slick.Config.driver.simple._
import java.net.InetAddress
import controllers.CRUD.FilterByKey

import scala.slick.lifted
import scala.slick.lifted.{ShapedValue, ToShapedValue, MappedProjection, ProvenShape}

object Database {
  class UserRow(tag: Tag) extends Table[User](tag, "userrow__test") with FilterByKey {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def age = column[Int]("age")

    def * = (id, name, age) <> (User.tupled, User.unapply)

    def filterByKey(key: String): Column[Boolean] = id === key.toInt
  }
  
  //val Users = new TableQuery(tag => new UserRow(tag))
  val Users = TableQuery[UserRow]
}
