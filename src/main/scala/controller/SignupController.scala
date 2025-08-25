package controller

import javafx.fxml.FXML
import javafx.scene.control.TextField
import scalafx.scene.control.Alert
import scalafx.scene.control.Alert.AlertType
import scalafx.stage.Stage
import scalafx.Includes.*
import scala.util.{Failure, Success}
import app.MainApp
import model.User


class SignupController:

  // Injected from FXML
  @FXML private var nameField: TextField = null
  @FXML private var emailField: TextField = null
  @FXML private var passwordField: TextField = null

  var dialogStage: Stage = null
  private var __user: User = null

  // Public setter (pre-fill form)
  def user: User = __user
  def user_=(u: User): Unit =
    __user = u
    nameField.text = __user.name.value
    emailField.text = __user.email.value
    passwordField.text = __user.password.value

  /** Handle signup button click */
  @FXML
  def handleSignup(): Unit =
    if isInputValid() then
      val newUser = new User(
        emailField.getText.trim,
        nameField.getText.trim,
        passwordField.getText.trim
      )

      newUser.save() match
        case Success(_) =>
          showAlert("Signup Success", "Account created successfully.", AlertType.Information)
          MainApp.showLoginView() // go back to Login page
        case Failure(e) =>
          showAlert("Signup Failed", s"Failed to create account: ${e.getMessage}", AlertType.Error)

  /** Handle goToLogin button */
  @FXML
  def goToLogin(): Unit =
    MainApp.showLoginView()

  /** Input validation */
  private def isInputValid(): Boolean =
    var errorMessage = ""

    if (nameField.getText == null || nameField.getText.trim.isEmpty)
      errorMessage += "Name is required!\n"

    if (emailField.getText == null || emailField.getText.trim.isEmpty)
      errorMessage += "Email is required!\n"
    else if !isValidEmail(emailField.getText.trim) then
      errorMessage += "Invalid email format! Example: user@gmail.com\n"

    if (passwordField.getText == null || passwordField.getText.trim.isEmpty)
      errorMessage += "Password is required!\n"
    else if !isValidPassword(passwordField.getText.trim) then
      errorMessage += "Password must be at least 8 characters and contain at least\none digit.\n"

    if errorMessage.isEmpty then
      true
    else
      new Alert(AlertType.Error):
        initOwner(dialogStage)
        title = "Invalid Fields"
        headerText = "Please correct invalid fields."
        contentText = errorMessage
      .showAndWait()
      false

  private def isValidEmail(email: String): Boolean =
    val emailRegex = "^[\\w.-]+@[\\w.-]+\\.\\w+$".r
    emailRegex.matches(email)

  private def isValidPassword(password: String): Boolean =
    password.length >= 8 && password.exists(_.isDigit)

  /** Show Alert Window */
  private def showAlert(alertTitle: String, message: String, alertType: AlertType): Unit =
    new Alert(alertType):
      initOwner(dialogStage)
      title = alertTitle
      headerText = None
      contentText = message
    .showAndWait()
