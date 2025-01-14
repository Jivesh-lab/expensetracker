package com.example.expense;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;

public class MainExpense extends Application {
    public void start(Stage stage) throws IOException {
           Database. connect();
        // Load the FXML file for the main page
        FXMLLoader fxmlLoader = new FXMLLoader(RegisterController.class.getResource("login.fxml"));

// Create the Scene using the loaded FXML content
        Scene scene = new Scene(fxmlLoader.load());
// Set the Scene on the Stage
        stage.setScene(scene);

// Automatically resize the window (Stage) to fit the content in the Scene
        stage.sizeToScene();
// Set the title of the Stage
        stage.setTitle("Expense Tracker");
        // Center the window on the screen
//        centerOnScreen(stage);
// Show the Stage
        stage.show();

    }

    private void centerOnScreen(Stage stage) {
        // Get the dimensions of the screen
        double screenWidth = Screen.getPrimary().getVisualBounds().getWidth();
        double screenHeight = Screen.getPrimary().getVisualBounds().getHeight();

        // Get the dimensions of the window
        double stageWidth = stage.getWidth();
        double stageHeight = stage.getHeight();

        // Calculate the new position for the window
        stage.setX((screenWidth - stageWidth) / 2);
        stage.setY((screenHeight - stageHeight) / 2);
    }
    public static void main(String[] args) {
        launch();
    }
}


