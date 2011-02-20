package code.model

import _root_.net.liftweb.mapper._
import _root_.net.liftweb.util._
import _root_.net.liftweb.common._

object User extends User with MetaMegaProtoUser[User] {
  override def dbTableName = "users" // define the DB table name
  override def screenWrap = Full(<lift:surround with="default" at="content"><lift:bind /></lift:surround>)
  // define the order fields will appear in forms and output
  override def fieldOrder = List(id, administrator, firstName, lastName, email, locale, timezone, password)

  // comment this line out to require email validations
  override def skipEmailValidation = true
}

class User extends MegaProtoUser[User] {
  def getSingleton = User
  object administrator extends MappedBoolean(this)
}
