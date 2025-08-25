package model

import scalafx.beans.property.{StringProperty, IntegerProperty, BooleanProperty, ObjectProperty}
import java.time.LocalDate
import scalikejdbc.*
import scala.util.{Try, Failure}
import util.Database

class MealPlan(val userIdI: Int, val recipeIdI: Int, val mealDateD: LocalDate, val mealTypeS: String, val consumedB: Boolean = false):
  var id = IntegerProperty(0)
  val userId = IntegerProperty(userIdI)
  val recipeId = IntegerProperty(recipeIdI)
  var mealDate = ObjectProperty(mealDateD)
  var mealType = new StringProperty(mealTypeS)
  var consumed = BooleanProperty(consumedB)

  /** Save or update meal plan to database */
  def save(): Try[Int] =
    if !isExist then
      Try(DB autoCommit { implicit session =>
        sql"""
          INSERT INTO meal_plan_table (user_id, recipe_id, meal_date, meal_type, consumed)
          VALUES (${userId.value}, ${recipeId.value}, ${mealDate.value}, ${mealType.value}, ${consumed.value})
           """.updateAndReturnGeneratedKey.apply().toInt
      }).map { generatedId =>
        id.value = generatedId
        generatedId
      }
    else
      Try(DB autoCommit { implicit session =>
        sql"""
          UPDATE meal_plan_table
          SET meal_date = ${mealDate.value},
              meal_type = ${mealType.value},
              consumed = ${consumed.value}
          WHERE id = ${id.value}
           """.update.apply()
      })

  /** Update only the meal date */
  def updateDate(newDate: LocalDate): Try[Int] =
    Try(DB autoCommit { implicit session =>
      sql"""
        UPDATE meal_plan_table
        SET meal_date = $newDate
        WHERE id = ${id.value}
      """.update.apply()
    }).map { rows =>
      if rows > 0 then mealDate.value = newDate
      rows
    }

  /** Delete this meal plan */
  def delete(): Try[Int] =
    Try(DB autoCommit { implicit session =>
      sql"DELETE FROM meal_plan_table WHERE id = ${id.value}".update.apply()
    })

  /** Check if meal plan exists by id */
  def isExist: Boolean =
    DB readOnly { implicit session =>
      sql"SELECT id FROM meal_plan_table WHERE id = ${id.value}"
        .map(_.int("id")).single.apply().isDefined
    }

object MealPlan extends Database:
  def apply(userIdI: Int, recipeIdI: Int, mealDateD: LocalDate, mealTypeS: String, consumedB: Boolean = false): MealPlan =
    new MealPlan(userIdI, recipeIdI, mealDateD, mealTypeS, consumedB)

  def initializeTable(): Unit =
    DB autoCommit { implicit session =>
      sql"""
        CREATE TABLE meal_plan_table (
          id INT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
          user_id INT NOT NULL,
          recipe_id INT NOT NULL,
          meal_date DATE NOT NULL,
          meal_type VARCHAR(20),
          consumed BOOLEAN DEFAULT FALSE,
          PRIMARY KEY (id),
          FOREIGN KEY (recipe_id) REFERENCES recipe_table(id)
        )
         """.execute.apply()
    }

  /** Add recipe to user meal plan */
  def addRecipeToUser(userId: Int, recipeId: Int, mealType: String = "Dinner", date: LocalDate = LocalDate.now()): Try[Int] =
    if date.isEqual(LocalDate.now()) then
      Try(DB autoCommit { implicit session =>
        sql"""
          INSERT INTO meal_plan_table (user_id, recipe_id, meal_date, meal_type, consumed)
          VALUES ($userId, $recipeId, $date, $mealType, FALSE)
        """.updateAndReturnGeneratedKey.apply().toInt
      })
    else
      Failure(new IllegalArgumentException("You can only add meals for today."))

  /** Fetch meals for a user by date */
  def getByDate(userId: Int, date: LocalDate): List[MealPlan] =
    DB readOnly { implicit session =>
      sql"SELECT * FROM meal_plan_table WHERE user_id=$userId AND meal_date=$date"
        .map(rs => {
          val mp = new MealPlan(
            rs.int("user_id"),
            rs.int("recipe_id"),
            rs.date("meal_date").toLocalDate,
            rs.string("meal_type"),
            rs.boolean("consumed")
          )
          mp.id.value = rs.int("id")
          mp
        }).list.apply()
    }

  /** Mark a meal as consumed */
  def markConsumed(id: Int): Unit =
    DB autoCommit { implicit session =>
      sql"UPDATE meal_plan_table SET consumed = TRUE WHERE id = $id".update.apply()
    }

