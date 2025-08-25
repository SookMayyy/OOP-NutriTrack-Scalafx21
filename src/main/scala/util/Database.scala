package util

/** References: AddressApp Tutorial Part 5: Database Setup */
import scalikejdbc.*
import model.{User, Recipe, RecipeDetail, MealPlan}

trait Database:
  // Derby setup
  private val derbyDriverClassname = "org.apache.derby.jdbc.EmbeddedDriver"
  private val dbURL = "jdbc:derby:nutritrackDB;create=true;" // db named "nutritrackDB"

  // Initialize JDBC driver and connection pool
  Class.forName(derbyDriverClassname)
  ConnectionPool.singleton(dbURL, "APP", "pass")

  // ad-hoc session provider
  given AutoSession = AutoSession

object Database extends Database:
  /** Set up the database */
  def setupDB(): Unit =
    // Create table if missing
    if (!hasDBInitialized("USER_TABLE")) then User.initializeTable()
    if (!hasDBInitialized("RECIPE_TABLE")) then Recipe.initializeTable()
    if (!hasDBInitialized("RECIPE_DETAIL_TABLE")) then RecipeDetail.initializeTable()
    if (!hasDBInitialized("MEAL_PLAN_TABLE")) then MealPlan.initializeTable()

    // Seed recipes if empty
    if (Recipe.getAllRecipes.isEmpty)
      Recipe.seed()

  /** Check if all the database table exist */
  private def hasDBInitialized(tableName: String): Boolean =
    DB getTable tableName match
      case Some(_) => true
      case None    => false
    
