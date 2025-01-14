package com.example.expense;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BudgetController extends Database{
    @FXML
    protected Label tot1;
    @FXML
    protected Label tot2;
    @FXML
    protected Label tot3;
@FXML
protected PieChart pie;

        @FXML
        protected TableView<Spend> tab1;
        @FXML
        protected TableColumn<Spend, String> Budget;
        @FXML
        protected TableColumn<Spend, String> Spent;
        @FXML
        protected TableColumn<Spend, String> allocate;
    public void initialize() {
        upd();
        Budget.setCellValueFactory(new PropertyValueFactory<>("budget"));
        Spent.setCellValueFactory(new PropertyValueFactory<>("spent"));
        allocate.setCellValueFactory(new PropertyValueFactory<>("allocate"));
        populatePieChart(pie);


        upd();


        // Load data into table view based on user ID
        loading(Database.getLoginId());
    }
    public void populatePieChart(PieChart pieChart) {
        // Clear previous data
        pieChart.getData().clear();

        // Fetch categorized expenses from the database for the current user
        Map<String, Integer> categorizedExpenses = getCategorizedExpenses(Database.getLoginId());

        // Debugging: Print the data to check if values are correctly fetched
        System.out.println("Categorized Expenses: " + categorizedExpenses);

        // Check if the data is empty and handle it
        if (categorizedExpenses.isEmpty()) {
            System.out.println("No expenses found.");
        }

        // Iterate through the categories and add them as slices to the PieChart
        for (Map.Entry<String, Integer> entry : categorizedExpenses.entrySet()) {
            String category = entry.getKey();
            int totalExpense = entry.getValue();

            // Print values to the console for debugging
            System.out.println("Category: " + category + ", Total Expense: " + totalExpense);

            // Create PieChart slice and add it to the PieChart
            PieChart.Data slice = new PieChart.Data(category, totalExpense);
            pieChart.getData().add(slice);
        }
    }

    private Map<String, Integer> getCategorizedExpenses(int userId) {
        Map<String, Integer> categorizedExpenses = new HashMap<>();

        String query = "SELECT typeofexpense, SUM(Amount) as total_expense FROM information " +
                "WHERE userid = ? " +
                "GROUP BY typeofexpense";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);

            ResultSet rs = stmt.executeQuery();

            // Retrieve the total expense for each category
            while (rs.next()) {
                String category = rs.getString("typeofexpense");
                int totalExpense = rs.getInt("total_expense");

                // Print fetched values to the console for debugging
                System.out.println("Category: " + category + ", Total Expense: " + totalExpense);

                categorizedExpenses.put(category, totalExpense);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return categorizedExpenses;
    }


    public static class Spend {
                        private SimpleStringProperty budget;     // Changed variable name to lowercase
                        private SimpleIntegerProperty spent;     // Changed to SimpleIntegerProperty for integers
                        private SimpleIntegerProperty allocate;  // Changed to SimpleIntegerProperty for integers

                        // Constructor
                        public Spend(String budget, int spent, int allocate) {
                                this.budget = new SimpleStringProperty(budget);  // Initialize String property for budget
                                this.spent = new SimpleIntegerProperty(spent);   // Initialize Integer property for spent
                                this.allocate = new SimpleIntegerProperty(allocate);  // Initialize Integer property for allocate
                        }

                        // Getter for budget (String)
                        public String getBudget() {
                                return budget.get();
                        }

                        // Getter for spent (int)
                        public int getSpent() {
                                return spent.get();
                        }

                        // Getter for allocate (int)
                        public int getAllocate() {
                                return allocate.get();
                        }

                }

    // Method to load user data
    public void upd() {
        // Default values if database returns no value
        int income = Database.getIncomeFromDatabase(); // Fetch income from the database
        int expense = Database.getExpens(); // Fetch expense from the database

        // Set default values to 0 if no data is retrieved
        if (income <= 0) {
            income = 0;
        }

        if (expense <= 0) {
            expense = 0;
        }

        // Calculate balance
        int balance = income - expense;

        // If the balance is negative, set it to 0
        if (balance < 0) {
            balance = 0;
        }

        // Set the labels with the correct values
        tot2.setText(String.valueOf(income));   // Set Income Label
        tot3.setText(String.valueOf(expense));  // Set Expense Label
        tot1.setText(String.valueOf(balance));  // Set Balance Label
    }

    private void loading(int userId) {
    ObservableList<BudgetController.Spend> exp= FXCollections.observableArrayList();
    String query ="SELECT typeofexpense,amount,allocate FROM information WHERE userid = ?";
 try{

    PreparedStatement pstmt = conn.prepareStatement(query);
        pstmt.setInt(1, userId);
        ResultSet rs = pstmt.executeQuery();
     while (rs.next()) {
         // Get typeofexpense as a String
         String typeofexpense = rs.getString("typeofexpense");

         // Get amount and allocate as integers
         int amount = rs.getInt("amount");
         int allocate = rs.getInt("allocate");

         // Add data to the ObservableList
         exp.add(new BudgetController.Spend(typeofexpense, amount, allocate));
     }

     // Set the ObservableList to the TableView
     tab1.setItems(exp);
 // Bind the ObservableList to the TableView
    } catch (
    SQLException e) {
        e.printStackTrace();
    }
}
        }





