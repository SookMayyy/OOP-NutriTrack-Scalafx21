package controller

import javafx.fxml.FXML
import javafx.scene.control.{Button, TextField}
import scalafx.scene.control.Alert
import scalafx.scene.control.Alert.AlertType
import scalafx.stage.Stage
import scalafx.Includes.*
import app.MainApp
import model.User

class LoginController:

  // Injected from FXML
  @FXML private var emailField: TextField = null
  @FXML private var passwordField: TextField = null
  @FXML private var loginButton: Button = null

  var dialogStage: Stage = null
  private var __user: User = null

  // Public setter (pre-fill form)
  def user: User = __user
  def user_=(u: User): Unit =
    __user = u
    emailField.text = __user.email.value
    passwordField.text = __user.password.value

  /** Handle login button click */
  @FXML
  private def handleLogin(): Unit =
    if isInputValid() then
      val email = emailField.text.value.trim
      val password = passwordField.text.value.trim

      User.findByEmail(email) match
        case Some(u) if u.password.value == password =>
          showAlert("Login Successful \uD83C\uDF89", s"Welcome back to NutriTrack, ${u.name.value}!", AlertType.Information)
          MainApp.showHomeView(u) // pass user into home
        case Some(_) =>
          showAlert("Login Failed", "Incorrect password.", AlertType.Error)
        case None =>
          showAlert("Login Failed", "No account found with this email. Please sign up first.", AlertType.Error)

  /** Handle goToSignup button */
  @FXML
  def goToSignup(): Unit =
    MainApp.showSignupView()

  /** Input validation */
  private def isInputValid(): Boolean =
    var errorMessage = ""

    if (emailField.getText == null || emailField.getText.trim.isEmpty)
      errorMessage += "Email is required!\n"
    else if !isValidEmail(emailField.getText.trim) then
      errorMessage += "Invalid email format. Example: user@gmail.com\n"

    if (passwordField.getText == null || passwordField.getText.trim.isEmpty)
      errorMessage += "Password is required!\n"
    else if !isValidPassword(passwordField.getText.trim) then
      errorMessage += "Password must be at least 8 characters and contain at least\none digit.\n"

    if errorMessage.isEmpty then
      true
    else
      new Alert(AlertType.Error):
        initOwner(dialogStage)
        title = "Invalid Input"
        headerText = "Please enter the empty fields:"
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
