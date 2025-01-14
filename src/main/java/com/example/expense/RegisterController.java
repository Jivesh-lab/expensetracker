package com.example.expense;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.io.IOException;
public class RegisterController extends Database{
  @FXML
  protected Button btn1;
  @FXML
  protected TextField txt1;
    @FXML
    protected TextField txt2;
    @FXML
    protected TextField txt3;
    @FXML
    protected TextField txt4;


           @FXML
    protected boolean validateInput() {
               boolean isValid = true;
               StringBuilder errorMessage = new StringBuilder("Please correct the following errors:\n");

// Validation for txt1: Name (only letters)
               if (!txt1.getText().trim().matches("[a-zA-Z]+")) {
                   txt1.setStyle("-fx-border-color: red;");
                   errorMessage.append("- Name must contain only letters.\n"); // Specific error message
                   isValid = false;
               } else {
                   txt1.setStyle(null); // Reset border color if valid
               }

// Validation for txt2: Email
               if (!txt2.getText().trim().matches("^[A-Za-z0-9+_.-]+@[a-zA-Z0-9.-]+$")) {
                   txt2.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
                   errorMessage.append("- Email format is invalid.\n"); // Specific error message
                   isValid = false;
               } else {
                   txt2.setStyle(null); // Reset style if valid
               }

// Validation for txt3: Password
               if (!txt3.getText().trim().matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#])[A-Za-z\\d@$!%*?&#]{8,}$")) {
                   txt3.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
                   errorMessage.append("- Password must be at least 8 characters long and include at least one uppercase letter, one lowercase letter, one number, and one special character.\n"); // Specific error message
                   isValid = false;
               } else {
                   txt3.setStyle(null);
               }

// Validation for txt4: Confirm Password
               if (!txt4.getText().trim().equals(txt3.getText())) {
                   txt4.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
                   errorMessage.append("- Confirm Password does not match Password.\n"); // Specific error message
                   isValid = false;
               } else {
                   txt4.setStyle(null);
               }

// If any validation failed, show an alert
               if (!isValid) {
                   Alert alert = new Alert(Alert.AlertType.WARNING, errorMessage.toString(), ButtonType.OK);
                   alert.setTitle("Validation Error");
                   alert.setHeaderText("Invalid Input");
                   alert.showAndWait();
               }
               else {
                   Alert alert = new Alert(Alert.AlertType.INFORMATION);
                   alert.setTitle("Register  ");
                   alert.setHeaderText("Register successfully");
                   alert.showAndWait();
               }


               return isValid;
           }
 // protected Button btn2;
       @FXML
       protected void onOpen(ActionEvent event) {


           if (validateInput()) {
               String user=txt1.getText().trim();
               String password=txt2.getText().trim();
               String mail=txt3.getText().trim();
               String confirm=txt4.getText().trim();
               Database.insert(user,password,mail,confirm);
               try {

                   FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));

                   Parent root = loader.load();

                   Scene newScene = new Scene(root);

                   Stage stage = (Stage) btn1.getScene().getWindow();

                   stage.setScene(newScene);


               } catch (IOException e) {

                   e.printStackTrace();

               }
           }

       }





}
