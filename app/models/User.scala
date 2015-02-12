package models

case class User(id: Option[Int], gender: String, streetAddress: String, city: String, zipCode: String, country: String, email: String, telephoneNumber: String)