// Add delete user account

package controller

import javafx.fxml.FXML
import javafx.scene.control.{Label, TableColumn, TableView}
import javafx.scene.chart.PieChart
import javafx.scene.image.ImageView
import javafx.scene.shape.Circle
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scalafx.stage.Stage
import scalafx.collections.ObservableBuffer
import scalafx.Includes.*
import scalafx.beans.property.StringProperty
import scalafx.scene.control.Alert
import scalafx.scene.control.Alert.AlertType
import model.{User, HealthDashboard}

/** --- Row class for TableView --- */
case class MealHistoryRow(recipeName: String, calories: String, date: String):
  val recipeNameProperty = StringProperty(recipeName)
  val caloriesProperty = StringProperty(calories)
  val dateProperty = StringProperty(date)

class HealthDashboardController extends MenuNavigation:

  /** FXML injections */
  @FXML private var profile: ImageView = null
  @FXML private var lblName: Label = null
  @FXML private var lblEmail: Label = null
  @FXML private var lblTodayDate: Label = null
  @FXML private var lblProtein: Label = null
  @FXML private var lblFat: Label = null
  @FXML private var lblCarbs: Label = null
  @FXML private var lblCalories: Label = null
  @FXML private var nutritionPieChart: PieChart = null
  @FXML private var colRecipeName: TableColumn[MealHistoryRow, String] = null
  @FXML private var colCalories: TableColumn[MealHistoryRow, String] = null
  @FXML private var colDate: TableColumn[MealHistoryRow, String] = null
  @FXML private var mealHistoryTable: TableView[MealHistoryRow] = null

  var dialogStage: Stage = null
  /** Date Formatter */
  private val dateFormatter = DateTimeFormatter.ofPattern("d MMM uuuu")
  private val currentDate: LocalDate = LocalDate.now()

  @FXML
  def initialize(): Unit =
    // Show today's date
    lblTodayDate.setText(currentDate.format(dateFormatter))

    // Make profile picture fully circular
    val circle = new Circle()
    circle.radiusProperty().bind(profile.fitWidthProperty().divide(2))
    circle.centerXProperty().bind(profile.fitWidthProperty().divide(2))
    circle.centerYProperty().bind(profile.fitHeightProperty().divide(2))
    profile.setClip(circle)

    // Set up table columns
    colRecipeName.setCellValueFactory(_.value.recipeNameProperty)
    colCalories.setCellValueFactory(_.value.caloriesProperty)
    colDate.setCellValueFactory(_.value.dateProperty)

  /** Load current user info and dashboard */
  def loadUserData(): Unit =
    if currentUser != null then
      lblName.setText(currentUser.name.value)
      lblEmail.setText(currentUser.email.value)

      loadDailySummary(currentDate)
      loadMealHistory()
    else
      lblName.setText("No user")
      lblEmail.setText("No email")

  /** Load today's protein, fat, carbs, calories & pie chart */
  private def loadDailySummary(date: LocalDate): Unit =
    if currentUser == null then return

    val nutrition = HealthDashboard.getDailyNutrition(currentUser.id.value, currentDate)

    // Update labels
    lblProtein.setText(f"${nutrition.protein}%.1f g")
    lblFat.setText(f"${nutrition.fat}%.1f g")
    lblCarbs.setText(f"${nutrition.carbs}%.1f g")
    lblCalories.setText(f"${nutrition.calories}%.0f kcal")

    // Update pie chart (if no data → empty)
    if (nutrition.protein > 0 || nutrition.fat > 0 || nutrition.carbs > 0) then
      val pieData = ObservableBuffer(
        new PieChart.Data("Protein", nutrition.protein),
        new PieChart.Data("Fat", nutrition.fat),
        new PieChart.Data("Carbs", nutrition.carbs)
      )
      nutritionPieChart.setData(pieData)
    else
      nutritionPieChart.setData(ObservableBuffer.empty)

  /** Load consumed meal history into table */
  private def loadMealHistory(): Unit =
    if currentUser == null then return

      val dataList = HealthDashboard.getMealHistory(currentUser.id.value)

    val rows = dataList.map { data =>
      MealHistoryRow(
        data.recipeName,
        f"${data.calories}%.0f kcal",
        data.getFormattedDate
      )
    }
    mealHistoryTable.setItems(ObservableBuffer(rows: _*))

  /** Show Alert */
  private def showAlert(alertTitle: String, message: String): Unit =
    new Alert(AlertType.Information):
      initOwner(dialogStage)
      this.title = alertTitle
      headerText = None
      contentText = message
    .showAndWait()






