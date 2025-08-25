package controller

import javafx.fxml.FXML
import javafx.event.ActionEvent
import app.MainApp

class RootLayoutController():
  @FXML
  def handleClose(action: ActionEvent): Unit =
    System.exit(0)

  @FXML
  def handleAbout(action: ActionEvent): Unit =
    MainApp.showAbout()