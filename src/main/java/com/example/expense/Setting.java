package com.example.expense;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import javafx.fxml.FXML;
import javafx.scene.Scene;

import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class Setting  extends Database {
    @FXML
    protected Button btn1;

    @FXML
    protected TextField fullname;
    @FXML
    protected TextField username;
    @FXML
    protected TextField email1;
    @FXML
    protected TextField pass;

    int login = Database.getLoginId();

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean validateForm() {
        String fullNameText = fullname.getText();
        String userNameText = username.getText();
        String emailText = email1.getText();
        String passText = pass.getText();

        // Check if any fields are empty
        if (fullNameText.isEmpty() || userNameText.isEmpty() || emailText.isEmpty() || passText.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please fill in all fields.");
            return false;
        }

        // Check for valid email format
        if (!emailText.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Please enter a valid email address.");
            return false;
        }

        // Check for password length (minimum 6 characters, for example)
        if (!passText.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#])[A-Za-z\\d@$!%*?&#]{8,}$") ) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Password must be at least 8 characters long and contain atleast one upper,lower and a special character");
            return false;
        }

        // If everything is valid
        return true;
    }

    @FXML
    protected void updateDetails() {
        if (!validateForm()) {
            return; // Exit if validation fails
        }
        {
            String enteredEmail = email1.getText(); // Get email from the email1 TextField
            String newFullName = fullname.getText(); // Get new full name from fullname TextField
            String newUsername = username.getText(); // Get new username from username TextField
            String newPassword = pass.getText();
            String query = "SELECT username,cusname,email,password FROM customer WHERE email = ?";

            try ( // Assuming you have a Database connection method
                  PreparedStatement checkEmailStmt = conn.prepareStatement(query)) {

                // Check if the entered email exists in the database
                checkEmailStmt.setString(1, enteredEmail);
                ResultSet resultSet = checkEmailStmt.executeQuery();

                if (resultSet.next()) {
                    // Email exists, now update the user's details
                    String updateQuery = " UPDATE customer SET cusname = ?, username = ?, email = ?, password = ?, confirm = ? WHERE email = ?";

                    try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                        updateStmt.setString(1, newUsername); // cusname is mapped to the username TextField
                        updateStmt.setString(2, newFullName); // username is mapped to the fullname TextField
                        updateStmt.setString(3, enteredEmail); // Update email with the value in email1 TextField
                        updateStmt.setString(4, newPassword);
                        updateStmt.setString(5, newPassword); /// Update password
                        updateStmt.setString(6, enteredEmail); // This is the email to be checked

                        // Execute the update query
                        int rowsUpdated = updateStmt.executeUpdate();

                        if (rowsUpdated > 0) {
                            System.out.println("User details updated successfully.");

                            fullname.clear();
                            username.clear();
                            email1.clear();
                            pass.clear();
                            showAlert(Alert.AlertType.INFORMATION, "Update Successful", "Your details have been updated successfully.");
                        } else {
                            System.out.println("Update failed.");
                            showAlert(Alert.AlertType.WARNING, "Update Fail", "Your Details have not been Changed.");
                        }
                    }
                } else {
                    System.out.println("Email does not exist in the database.");
                    showAlert(Alert.AlertType.INFORMATION, "Email Error", "Email does not exist");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }


    }
}
