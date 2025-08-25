package model

import scalafx.beans.property.{StringProperty, IntegerProperty}
import scalikejdbc.*
import scala.util.{Try, Success, Failure}
import util.Database

class User(val idI: Int, val emailS: String, val nameS: String, val passwordS: String) extends Database:
  def this() = this(0, null, null, null)
  def this(emailS: String, nameS: String, passwordS: String) = this(0, emailS, nameS, passwordS)

  val id = IntegerProperty(idI)
  var email = new StringProperty(emailS)
  var name = new StringProperty(nameS)
  var password = new StringProperty(passwordS)

  /** Save user: insert if not exists, otherwise update */
  def save(): Try[Int] =
    if !isExist then
      Try(DB autoCommit { implicit session =>
        sql"""
            INSERT INTO user_table (email, name, password)
            VALUES (${email.value}, ${name.value}, ${password.value})
          """.updateAndReturnGeneratedKey.apply().toInt
      }).map { generatedId =>
        id.value = generatedId
        generatedId
      }
    else
      Try(DB autoCommit { implicit session =>
        sql"""
            UPDATE user_table
            SET name = ${name.value}, password = ${password.value}
            WHERE email = ${email.value}
          """.update.apply()
      })
    end if
  end save

  /** Delete user */
  def delete(): Try[Int] =
    if isExist then
      Try(DB autoCommit { implicit session =>
        sql"""
           DELETE FROM user_table
           WHERE email = ${email.value}
         """.update.apply()
      })
    else
      Failure(new Exception("User does not exist"))
    end if
  end delete    

  /** Check if user exists by email */
  def isExist: Boolean =
    DB.readOnly { implicit session =>
      sql"""
        SELECT email FROM user_table
        WHERE email = ${email.value}
      """.map(rs => rs.string("email")).single.apply()
    } match 
      case Some(_) => true
      case None    => false
  end isExist
end User

object User extends Database:
  def apply (idI: Int, emailS: String, nameS:String, passwordS: String): User =
    new User(idI, emailS, nameS, passwordS)
  
  /** Create table if not exists */
  def initializeTable(): Unit =
    DB autoCommit { implicit session =>
      sql"""
          CREATE TABLE user_table (
            id INT NOT NULL GENERATED ALWAYS AS IDENTITY,
            email VARCHAR(100) PRIMARY KEY,
            name VARCHAR(100),
            password VARCHAR(100)
          )
        """.execute.apply()
    }

  /** find user by email */
  def findByEmail(email: String): Option[User] =
    DB readOnly { implicit session =>
      sql"""
         SELECT * FROM user_table
         WHERE email = $email
         """.map(rs =>
          User(
              rs.int("id"),
              rs.string("email"),
              rs.string("name"),
              rs.string("password")
          )
      ).single.apply()
    }

  