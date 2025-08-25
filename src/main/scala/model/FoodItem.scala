package model
import scala.util.Try
import scalikejdbc._

/** Abstract base class for all food items in the nutrition tracking system. */
abstract class FoodItem:

  // Abstract properties that must be implemented by subclasses
  def tableName: String
  def idColumn: String = "id"
  def idValue: Int

  // Abstract methods for polymorphic behavior
  def insertSql(implicit session: DBSession): Int
  def updateSql(implicit session: DBSession): Int

  /** Each food item type calculates calories differently */
  def getCalories: Double

  /** Allows different food items to provide their nutrition data in different formats */
  def getNutritionInfo: String

  /** Save data operation */
  def save(): Try[Int] =
    if (!isExist)
      Try(DB autoCommit { implicit session =>
        val generatedId = insertSql(session)
        generatedId
      })
    else
      Try(DB autoCommit { implicit session =>
        val rowsAffected = updateSql(session)
        rowsAffected
      })

  /**
   * Delete data operation
   */
  def delete(): Try[Int] =
    if (isExist) {
      Try(DB autoCommit { implicit session =>
        val table = SQLSyntax.createUnsafely(tableName)
        val idCol = SQLSyntax.createUnsafely(idColumn)
        val rowsDeleted = sql"DELETE FROM $table WHERE $idCol = $idValue".update.apply()
        rowsDeleted
      })
    } else {
      Try(throw new Exception(s"$tableName row does not exist"))
    }

  /** Common method to check existence across all food items */
  def isExist: Boolean =
    DB readOnly { implicit session =>
      val table = SQLSyntax.createUnsafely(tableName)
      val idCol = SQLSyntax.createUnsafely(idColumn)
      sql"SELECT $idCol FROM $table WHERE $idCol = $idValue"
        .map(_.int(idColumn)).single.apply()
    } match {
      case Some(_) => true
      case None => false
    }



