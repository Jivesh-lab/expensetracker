package com.example.expense;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class LoginController extends Database {
    @FXML
    protected Button loginbtn;
    @FXML
    protected Label errorlabel1;
    @FXML
    protected Button registerbtn;
    @FXML
    protected TextField use;
    @FXML
    protected PasswordField pass;

//    private static final String URL = "jdbc:mysql://localhost:3306/expense";
//    private static final String USER = "root";
//    private static final String PASSWORD = "mynameisjp";
    @FXML
    protected void onRegist(ActionEvent event) {
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("register.fxml"));

            Parent root = loader.load();

            Scene newScene = new Scene(root);

            Stage stage = (Stage) registerbtn.getScene().getWindow();

            stage.setScene(newScene);

        } catch (IOException e) {

            e.printStackTrace();

        }
    }

    @FXML
    protected void onLogin(ActionEvent event) {
        try {
            String user = use.getText().trim();
            String password = pass.getText().trim();
            Database.check(user, password);
            boolean isAuthenticated = Database.check(user, password);
            if (isAuthenticated) {


                FXMLLoader loader = new FXMLLoader(getClass().getResource("mainpage.fxml"));

                Parent root = loader.load();

                Scene newScene = new Scene(root);

                Stage stage = (Stage) loginbtn.getScene().getWindow();

                stage.setScene(newScene);
            } else {
                // If login fails, highlight the incorrect fields and show error message
                errorlabel1.setText("Invalid username or password");


                // Apply a red border to both fields, assuming either could be wrong
                use.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
                pass.setStyle("-fx-border-color: red; -fx-border-width: 2px;");

            }
        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
        }
    }
}





