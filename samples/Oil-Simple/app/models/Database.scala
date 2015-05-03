package models

import play.api.db.slick.Config.driver.simple._
import oil.FilterByKey

object Database {
  class UserRow(tag: Tag) extends Table[User](tag, "Users") with FilterByKey {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def age = column[Int]("age")
    def streetAddress = column[String]("street_address")
    def city = column[String]("city")
    def zipCode = column[String]("zip_code")
    def country = column[String]("country")
    def email = column[String]("email")
    def telephoneNumber = column[String]("telephone_number")

    def * = (id.?, age, streetAddress, city, zipCode, country, email, telephoneNumber) <> (User.tupled, User.unapply)

    def filterByKey(key: String): Column[Boolean] = id === key.toInt
  }
  
  //val Users = new TableQuery(tag => new UserRow(tag))
  val Users = TableQuery[UserRow]
}
