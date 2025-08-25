package model

import scalafx.beans.property.{StringProperty, IntegerProperty, DoubleProperty}
import util.{Database}
import scalikejdbc.*
import scala.util.Try

class RecipeDetail(val recipeIdI: Int, val ingredientS: String, val proteinD: Double, val fatD: Double, val carbsD: Double, val stepsS: String) extends FoodItem:
  def this() = this(0, null, 0, 0, 0, null)

  var id = IntegerProperty(0)
  var recipeId = IntegerProperty(recipeIdI)
  var ingredient = new StringProperty(ingredientS)
  var protein = DoubleProperty(proteinD)
  var fat = DoubleProperty(fatD)
  var carbs = DoubleProperty(carbsD)
  var steps = new StringProperty(stepsS)

  override def tableName: String = "recipe_detail_table"
  override def idValue: Int = id.value

  /** Calories calculation */
  override def getCalories: Double = protein.value * 4 + carbs.value * 4 + fat.value * 9

  /** Provides detailed macronutrient breakdown */
  override def getNutritionInfo: String =
    f"Protein: ${protein.value}%.1f g (${protein.value * 4}%.0f kcal), " +
      f"Fat: ${fat.value}%.1f g (${fat.value * 9}%.0f kcal), " +
      f"Carbs: ${carbs.value}%.1f g (${carbs.value * 4}%.0f kcal)"

  /** INSERT recipe detail operation */
  override def insertSql(implicit session: DBSession): Int =
    sql"""
       INSERT INTO recipe_detail_table (recipe_id, ingredient, protein, fat, carbs, steps)
       VALUES (${recipeId.value}, ${ingredient.value}, ${protein.value}, ${fat.value}, ${carbs.value}, ${steps.value})
     """.updateAndReturnGeneratedKey.apply().toInt

  /** UPDATE recipe detail operation */
  override def updateSql(implicit session: DBSession): Int =
    sql"""
       UPDATE recipe_detail_table
       SET recipe_id = ${recipeId.value},
           ingredient = ${ingredient.value},
           protein = ${protein.value},
           fat = ${fat.value},
           carbs = ${carbs.value},
           steps = ${steps.value}
       WHERE id = ${id.value}
     """.update.apply()

  /** Check if recipe details exists by id */
  override def isExist: Boolean =
    DB readOnly { implicit session =>
      sql"""
        SELECT id FROM recipe_detail_table
        WHERE id = ${id.value}
      """.map(_.int("id")).single.apply()
    } match {
      case Some(_) => true
      case None    => false
    }
  end isExist

object RecipeDetail extends Database:
  def apply(recipeIdI: Int, ingredientS: String, proteinD: Double, fatD: Double, carbsD: Double, stepsS: String): RecipeDetail =
    new RecipeDetail(recipeIdI, ingredientS, proteinD, fatD, carbsD, stepsS)

  /** Create recipe_detail table if not exists */
  def initializeTable(): Unit =
    DB autoCommit { implicit session =>
      sql"""
        CREATE TABLE recipe_detail_table (
          id INT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
          recipe_id INT,
          ingredient VARCHAR(1000),
          protein DOUBLE,
          fat DOUBLE,
          carbs DOUBLE,
          steps CLOB,
          PRIMARY KEY (id),
          FOREIGN KEY (recipe_id) REFERENCES recipe_table(id)
        )
      """.execute.apply()
    }

  /** Insert a new recipe detail record */
  def insert(recipeId: Int, ingredient: String, protein: Double, fat: Double, carbs: Double, steps: String): Unit =
    DB autoCommit { implicit session =>
      sql"""
        INSERT INTO recipe_detail_table (recipe_id, ingredient, protein, fat, carbs, steps)
        VALUES ($recipeId, $ingredient, $protein, $fat, $carbs, $steps)
      """.update.apply()
    }

  /** Fetch detail for a recipe bt recipe ID */
  def getByRecipeId(recipeId: Int): Option[RecipeDetail] =
    DB readOnly { implicit session =>
      sql"SELECT * FROM recipe_detail_table WHERE recipe_id = $recipeId"
        .map(rs => {
          val detail = new RecipeDetail(
            rs.int("recipe_id"),
            rs.string("ingredient"),
            rs.double("protein"),
            rs.double("fat"),
            rs.double("carbs"),
            rs.string("steps")
          )
          detail.id.value = rs.int("id")
          detail
        }).single.apply()
    }
