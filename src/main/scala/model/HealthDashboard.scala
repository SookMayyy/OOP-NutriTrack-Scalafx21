package model

import java.time.LocalDate
import scalikejdbc.*

/** Case classes for health dashboard data as these are immutable data strcutures and composition */
case class DailyNutrition(protein: Double, fat: Double, carbs: Double, calories: Double)

case class MealHistoryData(recipeName: String, calories: Double, date: LocalDate):
  /** Format date for display */
  def getFormattedDate: String = {
    date.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy"))
  }

object HealthDashboard:

  /** Get consumed meal plans for a specific user and date */
  def getConsumedMealPlans(userId: Int, date: LocalDate): List[(Int, Int)] =
    DB readOnly { implicit session =>
      sql"""
      SELECT id, recipe_id FROM meal_plan_table
      WHERE user_id = $userId
        AND meal_date = $date
        AND consumed = true
    """.map(rs => (rs.int("id"), rs.int("recipe_id"))).list.apply()
    }

  /** Get daily nutrition totals for a user at a specific date */
  def getDailyNutrition(userId: Int, date: LocalDate): DailyNutrition =
    val plans = getConsumedMealPlans(userId, date)
    var totalProtein, totalFat, totalCarbs, totalCalories = 0.0

    plans.foreach { (planId, recipeId) =>
      Recipe.getRecipeById(recipeId).foreach { recipe =>
        RecipeDetail.getByRecipeId(recipe.id.value).foreach { detail =>
          totalProtein += detail.protein.value
          totalFat += detail.fat.value
          totalCarbs += detail.carbs.value
        }
        totalCalories += recipe.totalCalories.value
      }
    }
    DailyNutrition(totalProtein, totalFat, totalCarbs, totalCalories)

  /** Get all consumed meal history for a user using collection operations */
  def getMealHistory(userId: Int): List[MealHistoryData] =
    val plans = DB readOnly { implicit session =>
      sql"""
        SELECT recipe_id, meal_date FROM meal_plan_table
        WHERE user_id = $userId AND consumed = true
        ORDER BY meal_date DESC
      """.map(rs => (rs.int("recipe_id"), rs.date("meal_date").toLocalDate)).list.apply()
    }

    plans.flatMap { case (recipeId, date) =>
      Recipe.getRecipeById(recipeId).map { recipe =>
        MealHistoryData(
          recipe.title.value,
          recipe.getCalories,
          date
        )
      }
    }
