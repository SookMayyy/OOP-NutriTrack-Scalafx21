package app

import javafx.fxml.FXMLLoader
import scalafx.application.JFXApp3
import scalafx.application.JFXApp3.PrimaryStage
import scalafx.scene.image.Image
import scalafx.scene.Scene
import scalafx.stage.Stage
import scalafx.Includes.*
import javafx.scene.Parent
import util.Database
import controller.{AboutController, HealthDashboardController, HomeController, LoginController, MealPlanController, RecipeController, SignupController}
import model.User
import scalafx.stage.Modality.ApplicationModal

object MainApp extends JFXApp3:

  var rootLayout: Option[javafx.scene.layout.BorderPane] = None
  var stageRef: PrimaryStage = _

  override def start(): Unit =
    Database.setupDB()

    // Load RootLayout.fxml
    val rootResource = getClass.getResource("/view/RootLayout.fxml")
    val loader = new FXMLLoader(rootResource)
    loader.load()
    rootLayout = Option(loader.getRoot[javafx.scene.layout.BorderPane])

    stageRef = new PrimaryStage():
      title = "NutriTrack"
      icons += new Image(getClass.getResource("/images/nutriTrack_logo.png").toExternalForm)
      resizable = false
      scene = new Scene():
        root = rootLayout.get

    // Start at login screen
    showLoginView()

  def showAbout(): Unit =
    val resource = getClass.getResource("/view/About.fxml")
    val loader = new FXMLLoader(resource)
    loader.load()
    val pane = loader.getRoot[javafx.scene.layout.AnchorPane]
    val mywindow = new Stage():
      initOwner(stageRef)
      initModality(ApplicationModal)
      title = "About"
      scene = new Scene():
        root = pane

    val ctrl = loader.getController[AboutController]()
    ctrl.stage = Option(mywindow)
    mywindow.showAndWait()
    ctrl.okClicked

  def showLoginView(): Unit =
    val resource = getClass.getResource("/view/LoginView.fxml")
    val loader = new FXMLLoader(resource)
    loader.load()
    val loginRoot = loader.getRoot[javafx.scene.layout.Pane]
    rootLayout.foreach(_.setCenter(loginRoot))

  def showSignupView(): Unit =
    val resource = getClass.getResource("/view/SignupView.fxml")
    val loader = new FXMLLoader(resource)
    loader.load()
    val signupRoot = loader.getRoot[javafx.scene.layout.Pane]
    rootLayout.foreach(_.setCenter(signupRoot))

  def showHomeView(user: User): Unit =
    val resource = getClass.getResource("/view/HomeView.fxml")
    val loader = new FXMLLoader(resource)
    loader.load()
    val homeRoot = loader.getRoot[javafx.scene.control.ScrollPane]
    rootLayout.foreach(_.setCenter(homeRoot))
    val ctrl = loader.getController[HomeController]()
    ctrl.setCurrentUser(user) // pass the user

  def showRecipeView(user: User): Unit =
    val resource = getClass.getResource("/view/RecipeView.fxml")
    val loader = new FXMLLoader(resource)
    loader.load()
    val recipeRoot = loader.getRoot[javafx.scene.layout.BorderPane]
    rootLayout.foreach(_.setCenter(recipeRoot))
    val ctrl = loader.getController[RecipeController]()
    ctrl.setCurrentUser(user) // pass the user

  def showMealPlanView(user: User): Unit =
    val resource = getClass.getResource("/view/MealPlanView.fxml")
    val loader = new FXMLLoader(resource)
    loader.load()
    val mealPlanRoot = loader.getRoot[javafx.scene.layout.AnchorPane]
    rootLayout.foreach(_.setCenter(mealPlanRoot))
    val ctrl = loader.getController[MealPlanController]()
    ctrl.setCurrentUser(user) // pass the user
    ctrl.loadUserData()

  def showHealthDashboardView(user: User): Unit =
    val resource = getClass.getResource("/view/HealthDashboardView.fxml")
    val loader = new FXMLLoader(resource)
    loader.load()
    val dashboardRoot = loader.getRoot[javafx.scene.control.ScrollPane]
    rootLayout.foreach(_.setCenter(dashboardRoot))
    val ctrl = loader.getController[HealthDashboardController]()
    ctrl.setCurrentUser(user) // pass the user
    ctrl.loadUserData()