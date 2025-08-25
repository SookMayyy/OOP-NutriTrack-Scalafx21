package controller

import javafx.fxml.FXML
import javafx.event.ActionEvent
import scalafx.stage.Stage

class AboutController():

  var stage: Option[Stage] = None
  var okClicked = false

  @FXML
  def handleClose(action: ActionEvent): Unit =
    okClicked = true
    stage.foreach(_.close())

