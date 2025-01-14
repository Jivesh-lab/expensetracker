package com.example.expense;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;

import javafx.scene.chart.NumberAxis;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import javafx.scene.Scene;

import javafx.scene.control.Button;
import javafx.scene.control.Label;

import javafx.scene.layout.AnchorPane;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.scene.Node;

import java.io.IOException;


public class MainPage extends Database{

    @FXML
 protected BarChart<String, Number> barchart;
    @FXML
     protected Button hbt;
    @FXML
    protected Button budtn;
    @FXML
    protected Button accbtn;
    @FXML
    protected Button rpbtn;
    @FXML
    protected Button setbtn;
    @FXML
    protected Button offbtn;
    @FXML
    protected Label dash;
    @FXML
    protected AnchorPane anch;
    @FXML
    protected TableView<Information> table2;
    @FXML
    protected TableColumn<Information, String> date2;
    @FXML
    protected TableColumn<Information, String> desc2;
    @FXML
    protected TableColumn<Information, String> category;
    @FXML
    protected TableColumn<Information, Integer> amount2;
    @FXML
    protected Label num1;
    @FXML
    protected Label num2;
    @FXML
    protected Label num3;
private int loggedInUserId=Database.getLoginId();

    public void initialize() {

   populateBarChart(barchart);
       updateLabels();

        date2.setCellValueFactory(new PropertyValueFactory<>("Date"));
        desc2.setCellValueFactory(new PropertyValueFactory<>("Description"));
        category.setCellValueFactory(new PropertyValueFactory<>("category"));
        amount2.setCellValueFactory(new PropertyValueFactory<>("amount"));

        updateLabels();
        // Load data into table view based on user ID
    load(Database.getLoginId());
    }
    public void populateBarChart(BarChart<String, Number> barchart) {
        // Create a new series with Number for Y-axis (supports Integer or Double)
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Expenses");

        // Get the current date and calculate the start of the week (Monday) of the previous week
        LocalDate currentDate = LocalDate.now();

        // Check if today's date is early in the week to include the previous week if necessary
        // We will adjust the start date to include days from the previous week if it's the start of the month
        LocalDate startOfWeek = currentDate.with(DayOfWeek.MONDAY);

        // If today is early in the week, we can extend the range to the previous week
        LocalDate endOfWeek = startOfWeek.plusDays(6);  // End of the week (Sunday)

        // Create a formatter for the date to display (e.g., "Mon, 25 Sep")
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE, dd MMM");

        // Fetch expenses from the database for the current user and the current week
        Map<LocalDate, Integer> weeklyExpenses = getExpensesForCurrentWeek(startOfWeek, endOfWeek);
        System.out.println("Weekly Expenses: " + weeklyExpenses);  // Debugging

        // Add data for each day of the current week
        for (int i = 0; i < 7; i++) {
            LocalDate date = startOfWeek.plusDays(i);
            String formattedDate = date.format(formatter);  // Format date for X-axis

            // Get the expense for the day if available, else set to 0
            int expense = weeklyExpenses.getOrDefault(date, 0);
            series.getData().add(new XYChart.Data<>(formattedDate, expense));
        }

        // Clear old data and add the new series
        barchart.getData().clear();
        barchart.getData().add(series);

        // Customize Y-axis settings
        NumberAxis yAxis = (NumberAxis) barchart.getYAxis();  // No casting issues now
        yAxis.setAutoRanging(false);  // Disable auto scaling
        yAxis.setTickUnit(800);       // Set tick interval (adjust as needed)
        yAxis.setLowerBound(0);       // Set lower bound to 0
        yAxis.setUpperBound(10000);   // Set upper bound (adjust based on your data)
    }

    private Map<LocalDate, Integer> getExpensesForCurrentWeek(LocalDate startOfWeek, LocalDate endOfWeek) {
        Map<LocalDate, Integer> weeklyExpenses = new HashMap<>();

        String query = "SELECT date, SUM(Amount) as total_expense FROM information " +
                "WHERE userid = ? AND Date >= ? AND Date <= ? " +
                "GROUP BY Date";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, loggedInUserId);
            stmt.setDate(2, java.sql.Date.valueOf(startOfWeek));
            stmt.setDate(3, java.sql.Date.valueOf(endOfWeek));

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                LocalDate date = rs.getDate("date").toLocalDate();
                Integer totalExpense = rs.getInt("total_expense");
                System.out.println("hii" + date + " " + totalExpense);
                weeklyExpenses.put(date, totalExpense);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return weeklyExpenses;
    }
    public static class Information {
        private SimpleStringProperty date2;
        private SimpleStringProperty category;
        private SimpleIntegerProperty amount;
        private SimpleStringProperty description;

        public Information(String date, String description, String category, int amount) {
            this.date2 = new SimpleStringProperty(date);
            this.description = new SimpleStringProperty(description);
            this.category = new SimpleStringProperty(category);
            this.amount = new SimpleIntegerProperty(amount);
        }

        public String getDate() {
            return date2.get();
        }

        public String getCategory() {
            return category.get();
        }

        public int getAmount() {
            return amount.get();
        }

        public String getDescription() {
            return description.get();
        }

    }
    // Method to load user data
    public void updateLabels() {
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
        num2.setText(String.valueOf(income));   // Set Income Label
        num3.setText(String.valueOf(expense));  // Set Expense Label
        num1.setText(String.valueOf(balance));  // Set Balance Label
    }


    @FXML
    protected void onOff(ActionEvent event) throws IOException {
        // Load the FXML file for the main page
        FXMLLoader fxmlLoader = new FXMLLoader(RegisterController.class.getResource("login.fxml"));

// Create the Scene using the loaded FXML content
        Scene scene = new Scene(fxmlLoader.load());

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

// Set the Scene on the Stage
        stage.setScene(scene);

// Automatically resize the window (Stage) to fit the content in the Scene
        stage.sizeToScene();
// Set the title of the Stage
        stage.setTitle("Expense Tracker");
// Show the Stage
        stage.show();
    }
    @FXML
    protected void onBudget(ActionEvent event) {
        try {
            dash.setText("Budget");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Budget.fxml")); // Replace with your FXML file path
            AnchorPane newContent = loader.load();

            // Clear the current content of the AnchorPane
            anch.getChildren().clear();

            // Add the new content to the AnchorPane
            anch.getChildren().add(newContent);
            //             loadContent("Account.fxml");
        } catch (IOException e) {
            e.printStackTrace();

        }
}
        @FXML
        protected void onhome (ActionEvent event){
            try {
                dash.setText("DashBoard Overview");
                FXMLLoader loader = new FXMLLoader(getClass().getResource("mainpage.fxml")); // Replace with your FXML file path
                BorderPane newContent = loader.load(); // Load as BorderPane

                // Get the specific content you want to load (e.g., the Center of the BorderPane)
                Node centerContent = newContent.getCenter(); // Replace with the appropriate node you want

                // Clear the current content of the AnchorPane
                anch.getChildren().clear();

                // Add the new content (the center part) to the AnchorPane
                anch.getChildren().add(centerContent);

                //             loadContent("Account.fxml");
            } catch (IOException e) {
                e.printStackTrace();
//
            }

        }

        @FXML
        protected void OnAccnt (ActionEvent event){
            try {
                dash.setText("Account");
                FXMLLoader loader = new FXMLLoader(getClass().getResource("account.fxml")); // Replace with your FXML file path
                AnchorPane newContent = loader.load();

                // Clear the current content of the AnchorPane
                anch.getChildren().clear();

                // Add the new content to the AnchorPane
                anch.getChildren().add(newContent);
                //             loadContent("Account.fxml");
            } catch (IOException e) {
                e.printStackTrace();
//
            }
        }
        @FXML
        protected void onReport (ActionEvent event){
            try {
                dash.setText("Report");
                FXMLLoader loader = new FXMLLoader(getClass().getResource("Report.fxml")); // Replace with your FXML file path
                AnchorPane newContent = loader.load();

                // Clear the current content of the AnchorPane
                anch.getChildren().clear();

                // Add the new content to the AnchorPane
                anch.getChildren().add(newContent);
                //             loadContent("Account.fxml");
            } catch (IOException e) {
                e.printStackTrace();
//
            }
        }
        @FXML
        private void onset (ActionEvent event){
            try {
                dash.setText("Settings");
                FXMLLoader loader = new FXMLLoader(getClass().getResource("sett.fxml")); // Replace with your FXML file path
                AnchorPane newContent = loader.load();

                // Clear the current content of the AnchorPane
                anch.getChildren().clear();

                // Add the new content to the AnchorPane
                anch.getChildren().add(newContent);
                //             loadContent("Account.fxml");
            } catch (IOException e) {
                e.printStackTrace();

            }
        }
        private void load(int userId) {
            ObservableList<MainPage.Information> exp= FXCollections.observableArrayList();
            String query ="SELECT date, Description, typeofexpense AS category, Amount  FROM information WHERE userid = ?";

            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, userId);
                ResultSet rs = pstmt.executeQuery();

                while (rs.next()) {
                    String date = rs.getString("date");                // Date
                    String description = rs.getString("Description");  // Description of the expense
                    String category = rs.getString("category");        // Category/Type of the expense
                    int amount = rs.getInt("Amount");                  // Amount of the expense
                    if (description == null || description.trim().isEmpty()) {
                        description = "-";}
                        // Add data to the ObservableList
                    exp.add(new MainPage.Information(date, description, category, amount));
                }

                table2.setItems(exp);  // Bind the ObservableList to the TableView
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }

