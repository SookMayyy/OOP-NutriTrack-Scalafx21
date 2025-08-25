package controller

import javafx.fxml.FXML
import javafx.event.ActionEvent // Reference: AddressApp Tutorial Part 3: Event-driven Programming
import scalafx.stage.Stage
import scalafx.scene.control.Alert
import scalafx.scene.control.Alert.AlertType
import app.MainApp
import model.User

trait MenuNavigation:
  protected var currentUser: User = _ // store logged-in user

  def setCurrentUser(user: User): Unit =
    this.currentUser = user

  // Navigation bar buttons
  @FXML def goToHome(event: ActionEvent): Unit = MainApp.showHomeView(currentUser)
  @FXML def goToRecipe(event: ActionEvent): Unit = MainApp.showRecipeView(currentUser)
  @FXML def goToMealPlan(event: ActionEvent): Unit = MainApp.showMealPlanView(currentUser)
  @FXML def goToHealthDashboard(event: ActionEvent): Unit = MainApp.showHealthDashboardView(currentUser)

  /** Logout */
  @FXML
  def handleLogout(event: ActionEvent): Unit =
    currentUser = null // Clear current user

    // Show Logout Alert
    new Alert(AlertType.Information):
      initOwner(null)
      title = "Logged Out Successfully"
      headerText = "You have been logged out successfully"
      contentText = "We hope to see you soon!"
    .showAndWait()

    MainApp.showLoginView() // Redirect to Login Page


class HomeController extends MenuNavigation:

  /** Home Section Get Started */
  @FXML def getStarted(event:ActionEvent): Unit = MainApp.showRecipeView(currentUser)
  
  /** Explore Button */
  @FXML def handleExploreRecipe(event: ActionEvent): Unit = MainApp.showRecipeView(currentUser)
  @FXML def handleExploreMealPlan(event: ActionEvent): Unit = MainApp.showMealPlanView(currentUser)
  @FXML def handleExploreHealthDashboard(event: ActionEvent): Unit = MainApp.showHealthDashboardView(currentUser)



