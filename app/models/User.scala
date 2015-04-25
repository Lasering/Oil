package models

case class User(id: Option[Int], age: Int, streetAddress: String, city: String, zipCode: String, country: String, email: String, telephoneNumber: String)