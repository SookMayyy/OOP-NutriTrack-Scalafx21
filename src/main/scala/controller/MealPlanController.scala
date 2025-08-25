package controller


import javafx.fxml.FXML
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javafx.scene.layout.{VBox, HBox, Region, Priority}
import javafx.scene.image.{Image, ImageView}
import javafx.scene.control.{DatePicker, Label, Button, ComboBox}
import scalafx.stage.Stage
import scalafx.scene.text.Font
import scalafx.Includes.*
import scalafx.scene.control.Alert
import scalafx.scene.control.Alert.AlertType
import scala.util.{Success, Failure}
import model.{MealPlan, Recipe, RecipeDetail, User}

class MealPlanController extends MenuNavigation:
  @FXML private var datePicker: DatePicker = null
  @FXML private var lblDate: Label = null
  @FXML private var mealListContainer: VBox = null
  @FXML private var lblTitle: Label = null
  @FXML private var lblCalories: Label = null
  @FXML private var lblNutrition: Label = null
  @FXML private var lblIngredients: Label = null
  @FXML private var lblSteps: Label = null
  @FXML private var imgRecipe: ImageView = null
  @FXML private var btnConsume: Button = null

  var dialogStage: Stage = null
  private val dateFormatter = DateTimeFormatter.ofPattern("d MMM uuuu")

  @FXML
  def initialize(): Unit =
    val today = LocalDate.now()
    datePicker.setValue(today)

    datePicker.valueProperty.addListener((_, _, newDate) => {
      if newDate != null then
        lblDate.setText(newDate.format(dateFormatter))
        loadMealPlans(newDate)
    })

  /** Load user data */
  def loadUserData(): Unit =
    val today = LocalDate.now()
    lblDate.setText(today.format(dateFormatter))
    refresh()

    // Re-attach listener for loading meals when date changes
    datePicker.valueProperty.addListener((_, _, newDate) => {
      if newDate != null then
        lblDate.setText(newDate.format(dateFormatter))
        loadMealPlans(newDate)
    })

  /** Refresh meal list */
  def refresh(): Unit =
    val date = datePicker.getValue
    if date != null then
      lblDate.setText(date.format(dateFormatter))
      loadMealPlans(date)

  /** Load meal plans for the selected date */
  private def loadMealPlans(date: LocalDate): Unit =
    mealListContainer.getChildren.clear()
    if currentUser != null then
      val plans = MealPlan.getByDate(currentUser.id.value, date)
        .filterNot(_.consumed.value) // exclude consumed meals

      if plans.isEmpty then
        val emptyLabel = new Label("🍽️ Feed Me Some Recipe!")
        emptyLabel.setStyle("-fx-text-fill: #595656; -fx-font-size: 20px; -fx-font-style: italic;")
        mealListContainer.getChildren.add(emptyLabel)

      else
        plans.foreach(plan => {
          Recipe.getRecipeById(plan.recipeId.value).foreach(recipe =>
            mealListContainer.getChildren.add(createMealCard(recipe, plan))
          )
        })
    else
      showAlert("Error", "No user logged in")

  /** Create UI card for a meal plan */
  private def createMealCard(recipe: Recipe, plan: MealPlan): HBox =
    val box = new HBox(15)
    box.setStyle("-fx-background-color: #f4fbf8; -fx-padding: 10; -fx-spacing: 10; -fx-alignment: center-left; -fx-background-radius: 8; -fx-border-color: #c8e6c9; -fx-border-radius: 8;")
    box.setMinHeight(100)
    box.setCursor(javafx.scene.Cursor.HAND)

    val image = new ImageView(new Image(recipe.imageUrl.value))
    image.setFitWidth(100)
    image.setFitHeight(90)

    val infoBox = new VBox(3)
    val name = new Label(recipe.title.value)
    name.setFont(Font.font("Segoe UI Bold", 12))
    val calories = new Label(s"${recipe.totalCalories.value} kcal")
    calories.setFont(Font.font("Segoe UI Italic", 12))
    infoBox.getChildren.addAll(name, calories)

    val spacer = new Region()
    HBox.setHgrow(spacer, Priority.ALWAYS)

    val combo = new ComboBox[String]()
    combo.getItems.addAll("Breakfast", "Lunch", "Dinner")
    combo.setValue(plan.mealType.value)
    combo.setOnAction(_ => {
      plan.mealType.value = combo.getValue
      plan.save()
    })

    val btnEditDate = new Button("\uD83D\uDD8D")
    btnEditDate.setStyle("-fx-background-color: transparent; -fx-text-fill: #4CAF50; -fx-font-size: 16px; -fx-cursor: hand;")
    btnEditDate.setMinSize(32, 32)
    btnEditDate.setOnAction(_ => editMealDate(plan, recipe))

    val btnDelete = new Button("\uD83D\uDDD1")
    btnDelete.setStyle("-fx-background-color: transparent; -fx-text-fill: red; -fx-font-size: 18px; -fx-cursor: hand;")
    btnDelete.setMinSize(32, 32)
    btnDelete.setOnAction(_ => {
      plan.delete()
      loadMealPlans(datePicker.getValue)
    })

    box.setOnMouseClicked(_ => showRecipeDetails(recipe, plan))
    box.getChildren.addAll(image, infoBox, spacer, combo, btnEditDate, btnDelete)
    box

  /** Edit meal date */
  private def editMealDate(plan: MealPlan, recipe: Recipe): Unit =
    val datePickerDialog = new DatePicker()
    datePickerDialog.setValue(plan.mealDate.value)

    val alert = new Alert(AlertType.Confirmation):
      initOwner(dialogStage)
      title = "Edit Meal Date"
      headerText = s"Update date for ${recipe.title.value}"
      dialogPane().setContent(datePickerDialog)
    val result = alert.showAndWait()

    if result.isDefined && result.get == scalafx.scene.control.ButtonType.OK then
      val newDate = datePickerDialog.getValue
      if newDate != null && newDate.isAfter(LocalDate.now()) then
        plan.updateDate(newDate) match
          case Success(_) =>
            showAlert("Updated", s"Date updated to $newDate")
            loadMealPlans(datePicker.getValue)
          case Failure(ex) =>
            showAlert("Error", s"Could not update: ${ex.getMessage}")
      else
        showAlert("Invalid Date", "Please select a future date.")

  /** Show recipe details */
  private def showRecipeDetails(recipe: Recipe, plan: MealPlan): Unit =
    RecipeDetail.getByRecipeId(recipe.id.value) match
      case Some(detail) =>
        lblTitle.setText(recipe.title.value)
        lblCalories.setText(s"${recipe.totalCalories.value} kcal")
        lblNutrition.setText(f"Protein ${detail.protein.value}%.1f g, Fat ${detail.fat.value}%.1f g, Carbs ${detail.carbs.value}%.1f g")
        lblIngredients.setText(detail.ingredient.value)
        lblSteps.setText(detail.steps.value)
        imgRecipe.setImage(new Image(recipe.imageUrl.value))
        lblDate.setText(plan.mealDate.value.toString)

        val isToday = plan.mealDate.value.isEqual(LocalDate.now())
        btnConsume.setDisable(!isToday)
        btnConsume.setCursor(javafx.scene.Cursor.HAND)

        if isToday then
          btnConsume.setOnAction(_ => {
            MealPlan.markConsumed(plan.id.value)
            showAlert("Consumed", s"You have consumed ${recipe.title.value}")
            loadMealPlans(datePicker.getValue)
          })
        else
          btnConsume.setOnAction(_ => {
            showAlert("Not Allowed", "You can only mark meals as consumed for today")
          })

      case _ =>
        lblTitle.setText("Select A Meal")
        lblCalories.setText("Select A Meal")
        lblNutrition.setText("Select A Meal")
        lblIngredients.setText("Select A Meal")
        lblSteps.setText("Select A Meal")
        lblDate.setText("Select A Meal")

  /** Add recipe to today's meal plan */
  def addRecipeToMealPlan(recipeId: Int, mealType: String = "Dinner"): Unit =
    if currentUser == null then
      showAlert("Error", "No user logged in")
    else
      // Use MealPlan model method
      MealPlan.addRecipeToUser(currentUser.id.value, recipeId, mealType, LocalDate.now()) match
        case scala.util.Success(_) =>
          showAlert("Success", "Recipe added to today's meal plan!")
          loadMealPlans(LocalDate.now()) // refresh UI
        case scala.util.Failure(ex) =>
          showAlert("Error", ex.getMessage)

  /** Show alert popup */
  private def showAlert(alertTitle: String, message: String): Unit =
    new Alert(AlertType.Information):
      initOwner(dialogStage)
      this.title = alertTitle
      headerText = None
      contentText = message
    .showAndWait()
