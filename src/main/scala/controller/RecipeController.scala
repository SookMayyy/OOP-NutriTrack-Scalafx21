package controller

import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.control.{ScrollPane, TextField, Button, Label}
import javafx.scene.layout.{HBox, VBox}
import java.time.LocalDate
import scalafx.scene.text.Font
import scalafx.scene.text.Text
import scalafx.scene.image.{Image, ImageView}
import scalafx.scene.control.Alert
import scalafx.scene.control.Alert.AlertType
import scalafx.stage.Stage
import scala.util.{Success, Failure}
import model.{Recipe, RecipeDetail, MealPlan, User}

class RecipeController extends MenuNavigation:

  @FXML private var recipeScrollPane: ScrollPane = null
  @FXML private var recipeListContainer: VBox = null
  @FXML private var txtMinCalories: TextField = null
  @FXML private var txtMaxCalories: TextField = null
  @FXML private var btnFilter: Button = null

  @FXML private var btnMainDish: Button = null
  @FXML private var btnBeverage: Button = null
  @FXML private var btnSideDish: Button = null
  @FXML private var btnDesserts: Button = null
  @FXML private var btnSoupsStews: Button = null

  @FXML private var lblPrepTime: Label = null
  @FXML private var lblCookTime: Label = null
  @FXML private var lblNutrition: Label = null
  @FXML private var lblIngredients: Label = null
  @FXML private var lblSteps: Label = null
  @FXML private var btnAddToMealPlan: Button = null

  var dialogStage: Stage = null
  private var currentRecipeId: Option[Int] = None // track current selected recipe

  @FXML
  def initialize(): Unit =
    loadRecipes()
    btnFilter.setOnAction(_ => handleFilterClick())
    btnMainDish.setOnAction(_ => loadRecipesByCategory("Main Dish"))
    btnBeverage.setOnAction(_ => loadRecipesByCategory("Beverage"))
    btnSideDish.setOnAction(_ => loadRecipesByCategory("Side Dish"))
    btnDesserts.setOnAction(_ => loadRecipesByCategory("Desserts"))
    btnSoupsStews.setOnAction(_ => loadRecipesByCategory("Soups & Stews"))
    btnAddToMealPlan.setOnAction(_ => addToMealPlan())

  /** Loads recipes from DB, filtered by calories if given */
  private def loadRecipes(): Unit =
    recipeListContainer.getChildren.clear()
    val recipes = Recipe.getAllRecipes
    if recipes.isEmpty then
      showAlert("No Recipes Found", "No recipes available to be displayed.")
    else
      recipes.foreach(recipe => recipeListContainer.getChildren.add(createRecipeRow(recipe)))

  /** Loads recipes by category */
  private def loadRecipesByCategory(category: String): Unit =
    recipeListContainer.getChildren.clear()
    val recipes = Recipe.getRecipesByCategory(category)
    if recipes.isEmpty then
      showAlert("No Recipes Found", s"No recipes found in $category.")
    else
      recipes.foreach(recipe => recipeListContainer.getChildren.add(createRecipeRow(recipe)))

  /** Called when Filter button clicked */
  private def handleFilterClick(): Unit =
    val minCal = Option(txtMinCalories.getText).filter(_.nonEmpty).map(_.toInt).getOrElse(0)
    val maxCal = Option(txtMaxCalories.getText).filter(_.nonEmpty).map(_.toInt).getOrElse(Int.MaxValue)
    if (minCal > maxCal)
      showAlert("Invalid Range", "Minimum calories cannot be greater than maximum")
    else
      recipeListContainer.getChildren.clear()
      val recipes = Recipe.getByCalories(minCal, maxCal)

      if recipes.isEmpty then
        showAlert("No Recipes Found", s"No recipes match the range $minCal - $maxCal kcal.")
      else
        recipes.foreach(r => recipeListContainer.getChildren.add(createRecipeRow(r)))

  /** Creates an HBox for each recipe */
  private def createRecipeRow(recipe: Recipe): HBox =
    val box = new HBox()
    box.setStyle("-fx-background-color: #f4fbf8; -fx-padding: 10; -fx-spacing: 10;")
    box.setCursor(javafx.scene.Cursor.HAND) // makes all the card cursor a hand
    val imageView = new ImageView(new Image(recipe.imageUrl.value))
    imageView.setFitWidth(100)
    imageView.setFitHeight(90)
    val nameText = new Text(s"${recipe.title.value} (${recipe.totalCalories.value} kcal)")
    nameText.setFont(Font.font("Segoe UI Bold", 14))

    box.getChildren.addAll(imageView, nameText)
    // When user clicks the recipe row, it will show nutrition info
    box.setOnMouseClicked(_ => showRecipeDetails(recipe.id.value))
    box

  /** Generate Nutrition Info */
  private def showRecipeDetails(recipeId: Int): Unit =
    currentRecipeId = Some(recipeId) // Store the currently selected recipe
    (Recipe.getRecipeById(recipeId), RecipeDetail.getByRecipeId(recipeId)) match
      case (Some(recipe), Some(detail)) =>
        lblPrepTime.setText(recipe.prepTime.value)
        lblCookTime.setText(recipe.cookTime.value)
        lblNutrition.setText(recipe.getNutritionInfo)
        lblIngredients.setText(detail.ingredient.value)
        lblSteps.setText(detail.steps.value)
      case (None, _) =>
        showAlert("Error", s"Recipe with ID $recipeId not found")
      case (_, None) =>
        showAlert("Error", "Recipe details could not be found")
      case _ =>
        showAlert("Error", "Recipe details could not be found")

  /** Add current recipe to meal plan */
  private def addToMealPlan(): Unit =
    (currentRecipeId, currentUser) match
      case (Some(recipeId), user: User) =>
        MealPlan.addRecipeToUser(user.id.value, recipeId) match
          case Success(newId) =>
            showAlert("Success", "Recipe added to your meal plan")
          case Failure(ex) =>
            showAlert("Error", s"Failed: ${ex.getMessage}")
      case (None, _) =>
        showAlert("Error", "No recipe selected")
      case (_, null) =>
        showAlert("Error", "Please log in to add recipes to meal plan")

  /** Shows an alert popup */
  private def showAlert(alertTitle: String, message: String): Unit =
    new Alert(AlertType.Information):
      initOwner(dialogStage)
      this.title = alertTitle
      headerText = None
      contentText = message
    .showAndWait()
