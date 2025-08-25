package model

import scalafx.beans.property.{StringProperty, IntegerProperty, DoubleProperty}
import util.{Database, RecipeDataList}
import scalikejdbc.*
import scala.util.{Try, Success, Failure}

class Recipe(val titleS: String, val categoryS: String, val prepTimeS: String, val cookTimeS: String, val totalCaloriesD: Double, val imageUrlS: String) extends FoodItem:
  def this() = this(null, null, null, null, 0.0, null)

  var id = IntegerProperty(0)
  var title = new StringProperty(titleS)
  var category = new StringProperty(categoryS)
  var prepTime = new StringProperty(prepTimeS)
  var cookTime = new StringProperty(cookTimeS)
  var totalCalories = DoubleProperty(totalCaloriesD)
  var imageUrl = new StringProperty(imageUrlS)

  override def tableName: String = "recipe_table"
  override def idValue: Int = id.value

  /** For recipes, calories are stored directly */
  override def getCalories: Double = totalCalories.value

  /** Provides recipe -specific nutrition information format */
  override def getNutritionInfo: String = {
    RecipeDetail.getByRecipeId(id.value) match {
      case Some(detail) =>
        f"Protein: ${detail.protein.value}%.1f g, Fat: ${detail.fat.value}%.1f g, Carbs: ${detail.carbs.value}%.1f g"
      case None => "Nutrition details not available"
    }
  }

  /** INSERT recipe operation */
  override def insertSql(implicit session: DBSession): Int =
    sql"""
        INSERT INTO recipe_table (title, category, prep_time, cook_time, total_calories, image_url)
        VALUES (${title.value}, ${category.value}, ${prepTime.value}, ${cookTime.value}, ${totalCalories.value}, ${imageUrl.value})
      """.updateAndReturnGeneratedKey.apply().toInt

  /** UPDATE recipe operation */
  override def updateSql(implicit session: DBSession): Int =
    sql"""
      UPDATE recipe_table
      SET title = ${title.value},
          category = ${category.value},
          prep_time = ${prepTime.value},
          cook_time = ${cookTime.value},
          total_calories = ${totalCalories.value},
          image_url = ${imageUrl.value}
      WHERE id = ${id.value}
    """.update.apply()

  /** Delete recipe */
  override def delete(): Try[Int] =
    if isExist then
      Try(DB autoCommit { implicit session =>
        sql"""
          DELETE FROM recipe_table
          WHERE id = ${id.value}
        """.update.apply()
      })
    else
      throw new Exception("Recipe does not exist")
  end delete

  /** Check if recipe exists by id */
  override def isExist: Boolean =
    DB readOnly { implicit session =>
      sql"""
        SELECT id FROM recipe_table
        WHERE id = ${id.value}
      """.map(_.int("id")).single.apply()
    } match {
      case Some(_) => true
      case None    => false
    }
  end isExist
end Recipe

object Recipe extends Database:
  def apply(titleS: String, categoryS: String, prepTimeS: String, cookTimeS: String, totalCaloriesD: Double, imageUrlS: String): Recipe =
    new Recipe(titleS, categoryS, prepTimeS, cookTimeS, totalCaloriesD, imageUrlS)

  /** Create recipe table if not exists*/
  def initializeTable(): Unit =
    DB autoCommit { implicit session =>
      sql"""
        CREATE TABLE recipe_table (
          id INT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
          title VARCHAR(200) NOT NULL,
          category VARCHAR(50),
          prep_time VARCHAR(50),
          cook_time VARCHAR(50),
          total_calories DOUBLE,
          image_url VARCHAR(500),
          PRIMARY KEY (id)
        )
      """.execute.apply()
    }

  /** Fetch all recipes */
  def getAllRecipes: List[Recipe] =
    DB readOnly { implicit session =>
      sql"SELECT * FROM recipe_table".map ( rs =>
        val r = new Recipe(
          rs.string("title"),
          rs.string("category"),
          rs.string("prep_time"),
          rs.string("cook_time"),
          rs.double("total_calories"),
          rs.string("image_url")
        )
        r.id.value = rs.int("id")
        r // return recipe
      ).list.apply()
    }

  /** Find recipe by category */
  def getRecipesByCategory(category: String): List[Recipe] =
    DB readOnly { implicit session =>
      sql"SELECT * FROM recipe_table WHERE category = $category"
        .map(rs => {
          val r = new Recipe(
            rs.string("title"),
            rs.string("category"),
            rs.string("prep_time"),
            rs.string("cook_time"),
            rs.double("total_calories"),
            rs.string("image_url")
          )
          r.id.value = rs.int("id")
          r
        }).list.apply()
    }
  end getRecipesByCategory

  /** Get recipe by id */
  def getRecipeById(recipeId: Int): Option[Recipe] =
    DB readOnly { implicit session =>
      sql"""
        SELECT * FROM recipe_table WHERE id = $recipeId
        """.map(rs => {
        val r = new Recipe(
          rs.string("title"),
          rs.string("category"),
          rs.string("prep_time"),
          rs.string("cook_time"),
          rs.double("total_calories"),
          rs.string("image_url")
        )
        r.id.value = rs.int("id")
        r
      }).single.apply()
    }
  end getRecipeById

  /** Get recipe by calories (filter) */
  def getByCalories(min: Double, max: Double): List[Recipe] =
    DB readOnly { implicit session =>
      sql"""
         SELECT * FROM recipe_table
         WHERE total_calories BETWEEN $min AND $max
         """.map (rs => {
        val r = new Recipe(
          rs.string("title"),
          rs.string("category"),
          rs.string("prep_time"),
          rs.string("cook_time"),
          rs.double("total_calories"),
          rs.string("image_url")
        )
        r.id.value = rs.int("id")
        r
      }).list.apply()
    }

  /** Seed sample recipe data (util.RecipeData.RecipeDataList) into database */
  def seed(): Unit =
    RecipeDataList.recipes.foreach { data =>
      val recipe = new Recipe(
        data.title,
        data.category,
        data.prepTime,
        data.cookTime,
        data.totalCalories,
        data.imageUrl
      )
      recipe.save() match {
        case Success(newId) =>
          // create recipe details
          RecipeDetail.insert(newId, data.ingredients, data.protein, data.fat, data.carbs, data.steps)

        case Failure(ex) =>
          throw ex
      }
    }

end Recipe