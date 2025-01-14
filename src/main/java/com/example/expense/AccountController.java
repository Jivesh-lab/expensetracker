package com.example.expense;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.time.LocalDate;
import java.util.Optional;

public class AccountController extends Database {


    @FXML
    protected TextField name;
    @FXML
    protected TableView<Expenditure> info1; // Specify the type parameter for TableView
    @FXML
    protected TextField amount;
    @FXML
    protected TextField allocate;
    @FXML
    protected TextField income;
    @FXML
    protected ComboBox<String> type;
    @FXML
    protected DatePicker date;
    @FXML
    protected TextArea desc;
    @FXML
    protected Button addbtn;
    @FXML
    private TableColumn<Expenditure, String> transactionTypeCol;
    @FXML
    private TableColumn<Expenditure, String> nameCol;
    @FXML
    private TableColumn<Expenditure, Integer> amountCol;
    @FXML
    private TableColumn<Expenditure, String> descriptionCol;
    @FXML
    private TableColumn<Expenditure, Void> delete; // Column for Delete button

    public boolean validateForm() {
        StringBuilder errors = new StringBuilder();

        // Validate name (must contain only alphabets)
        if (!name.getText().matches("[a-zA-Z]+")) {
            errors.append("Name must contain alphabets only.\n");
        }

        // Validate amount, allocate, and income (must be integers)
        if (!isInteger(amount.getText())) {
            errors.append("Amount must be an integer.\n");
        }
        if (!isInteger(allocate.getText())) {
            errors.append("Allocate must be an integer.\n");
        }

        // Validate ComboBox (must have an option selected)
        if (type.getValue() == null || type.getValue().isEmpty()) {
            errors.append("Please select a type from the ComboBox.\n");
        }

        // Validate DatePicker (must have a date selected)
        if (date.getValue() == null) {
            errors.append("Please select a date.\n");
        }

        // Check if all required fields have values
        if (name.getText().isEmpty() || amount.getText().isEmpty() || allocate.getText().isEmpty()) {
            errors.append("All fields must be filled.\n");
        }

        // If there are errors, show an alert box
        if (errors.length() > 0) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Form Validation Error");
            alert.setHeaderText(null);
            alert.setContentText(errors.toString());
            alert.showAndWait();
            return false; // Validation failed
        } else {
            return true; // Validation passed
        }
    }


    // Helper method to check if a string is an integer
    private boolean isInteger(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // Nested static class for Expenditure
    public static class Expenditure {
        private SimpleStringProperty transactionType;
        private SimpleStringProperty name;
        private SimpleIntegerProperty amount;
        private SimpleStringProperty description;

        public Expenditure(String transactionType, String name, int amount, String description) {
            this.transactionType = new SimpleStringProperty(transactionType);
            this.name = new SimpleStringProperty(name);
            this.amount = new SimpleIntegerProperty(amount);
            this.description = new SimpleStringProperty(description);

        }

        public String getTransactionType() {
            return transactionType.get();
        }

        public String getName() {
            return name.get();
        }

        public int getAmount() {
            return amount.get();
        }

        public String getDescription() {
            return description.get();
        }

    }

    @FXML
    public void initialize() {

        type.getItems().addAll("Food & Dining", "Rent", "Transportation", "Entertainment", "Utilities", "Healthcare", "Groceries",
                "Clothing", "Insurance", "Savings", "Personal Care", "Miscellaneous", "others");
        int userId = Database.getLoginId();

        // Query to fetch the income value for the current user
        String query = "SELECT income FROM information WHERE userid = ?";

        try (PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setInt(1, userId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                int income1 = resultSet.getInt("income");

                // If income is found, disable the text field
                if (income1 > 0) {
                    income.setDisable(true);
                } else {
                    income.setDisable(false);
                }
            } else {
                // If no income record found, enable the text field
                income.setDisable(false);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // Initialize TableColumn cell value factories
        transactionTypeCol.setCellValueFactory(new PropertyValueFactory<>("transactionType"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));

        delete.setCellFactory(col -> new TableCell<Expenditure, Void>() {
            private final Button deleteButton = new Button("Delete");

            {
                deleteButton.setStyle("-fx-text-fill: white; -fx-background-color: #215fd1;");
                deleteButton.setOnAction(event -> {
                    Expenditure expenditure = getTableView().getItems().get(getIndex());
                    executeDelete(expenditure);  // Call the method to delete the expenditure
                });

            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteButton); // Add the button to the cell
                }
            }
        });
        checkIncomeStatus(Database.getLoginId());
        // Load expenses for the logged-in user
        loadExpenses(Database.getLoginId());
        Database.connect();
    }

    private void checkIncomeStatus(int userId) {
        String query = "SELECT Income FROM information WHERE userid = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            // If income already exists for this user, disable the income text field
            if (rs.next() && rs.getInt("Income") > 0) {
                income.setDisable(true);
            } else {
                income.setDisable(false);  // Enable the income field if no income is present
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
@FXML
    protected void add(ActionEvent event) {
        // First validate the form
        if (validateForm()) {
            // Proceed only if the form is validated successfully
            String name = this.name.getText().trim(); // Trim to remove extra whitespace
            Integer amount = parseInteger(this.amount.getText());
            Integer allocate = parseInteger(this.allocate.getText());
            String type = this.type.getValue();
            LocalDate localDate = this.date.getValue();
            Date sqlDate = (localDate != null) ? Date.valueOf(localDate) : null; // Handle null LocalDate
            String desc = this.desc.getText().trim(); // Trim description
            Integer incomeValue = null;

            // Only fetch the income value if the field is not disabled and has input
            if (!income.isDisabled() && !this.income.getText().isEmpty()) {
                incomeValue = parseInteger(this.income.getText());  // Parse income text to Integer if enabled
            }

            boolean values = Database.alter(name, type, amount, allocate, incomeValue, sqlDate, desc);
            if (values) {
                // Clear the form fields after successful insertion
                clearFormFields();
                System.out.println("Form cleared after successful database insertion.");
                if (incomeValue != null) {
                    this.income.setDisable(true);  // Disable the income field
                    System.out.println("Income field disabled after adding income.");
                }

                // Reload expenses to reflect new data in the TableView
                loadExpenses(Database.getLoginId());
            } else {
                System.out.println("Failed to insert data into the database.");
            }
        }
    }

    // Helper method to parse Integer safely
    private Integer parseInteger(String text) {
        try {
            return (text != null && !text.isEmpty()) ? Integer.parseInt(text) : null;
        } catch (NumberFormatException e) {
            System.out.println("Invalid number format: " + text);
            return null; // Return null if parsing fails
        }
    }

    // Method to clear form fields
    private void clearFormFields() {
        this.name.clear();
        this.amount.clear();
        this.allocate.clear();
        this.type.setValue(null);   // Clear ComboBox
        this.date.setValue(null);   // Clear DatePicker
        this.income.clear();
        this.desc.clear();
    }
    private void executeDelete(Expenditure expenditure)
    {
        int user1 = Database.getLoginId();

        // Step 1: Check if income value exists
        String checkIncomeQuery = "SELECT income FROM information WHERE userid = ? AND Amount = ?";

        try (PreparedStatement checkPstmt = conn.prepareStatement(checkIncomeQuery)) {
            checkPstmt.setInt(1, user1);
            checkPstmt.setInt(2, expenditure.getAmount());  // Use setDouble if Amount is decimal

            ResultSet rs = checkPstmt.executeQuery();

            if (rs.next()) {
                double incomeValue = rs.getInt("income");

                // Step 2: If income value exists, show a warning before deleting
                if (incomeValue != 0) {
                    // Show a warning alert
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Warning");
                    alert.setHeaderText("Income associated with this record.");
                    alert.setContentText("Are you sure you want to delete this record? The income value will be lost.");

                    // Add 'Yes' and 'No' buttons to the alert dialog
                    ButtonType yesButton = new ButtonType("Yes", ButtonBar.ButtonData.YES);
                    ButtonType noButton = new ButtonType("No", ButtonBar.ButtonData.NO);
                    alert.getButtonTypes().setAll(yesButton, noButton);

                    // Show the alert and wait for the user's response
                    Optional<ButtonType> result = alert.showAndWait();

                    if (result.isPresent() && result.get() == yesButton) {
                        // If user clicks 'Yes', proceed with deletion
                        deleteExpenditure(expenditure);
                    }
                } else {
                    // No income value, proceed with deletion
                    deleteExpenditure(expenditure);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void deleteExpenditure(Expenditure expenditure) {

        String deleteQuery = "DELETE  FROM information WHERE userid = ? AND Amount = ?";
        int user1 = Database.getLoginId();

        try (PreparedStatement pstmt = conn.prepareStatement(deleteQuery)) {
            pstmt.setInt(1, user1);
            pstmt.setInt(2, expenditure.getAmount());
            pstmt.executeUpdate();
            // Reload expenses to refresh the TableView
            loadExpenses(Database.getLoginId());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadExpenses(int userId) {
        ObservableList<Expenditure> expenses = FXCollections.observableArrayList();
        String query = "SELECT NameofExpense, typeofexpense, Amount, Description FROM information WHERE userid = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String transactionType = rs.getString("typeofexpense");
                String name = rs.getString("NameofExpense");
                int amount = rs.getInt("Amount");
                String description = rs.getString("Description");
                expenses.add(new Expenditure(transactionType, name, amount, description));
            }

            info1.setItems(expenses);  // Bind the ObservableList to the TableView
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
