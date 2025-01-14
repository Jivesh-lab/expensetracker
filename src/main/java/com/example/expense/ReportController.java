package com.example.expense;

import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.TextInputDialog;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javafx.stage.FileChooser;
import jxl.Workbook;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import javax.xml.crypto.Data;

public class ReportController extends Database {
    @FXML
    protected Label lb1;
    @FXML
    protected Label lb2;
    @FXML
    protected Label lb3;
    @FXML
    private AnchorPane barcontain;
    @FXML
    private BarChart barc;
    @FXML
    protected LineChart<String, Number> line;
    @FXML
    protected Button exp1;

    @FXML
    private NumberAxis xAxis;

    @FXML
    private CategoryAxis yAxis;

    @FXML
    public void initialize() {
        loadMonthlyBalance();
        loadMonthlyExpenses();


        updateLabels();
    }
    @FXML
protected void onPrint()
    { FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Excel File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xls"));

        // Show the save dialog
        File file = fileChooser.showSaveDialog(null);

        // If the user did not select a file, exit the method
        if (file == null) {
            System.out.println("File selection cancelled.");
            return;
        }

        // Ensure the file has the correct extension
        if (!file.getName().toLowerCase().endsWith(".xls")) {
            file = new File(file.getAbsolutePath() + ".xls");
        }
        try {

            // Query to fetch data from the "information" table
            String query = "SELECT Nameofexpense, typeofexpense, Amount, date FROM information WHERE userid = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, Database.getLoginId()); // Use the logged-in user ID
            ResultSet rs = stmt.executeQuery();

            // Create Excel file at the user-specified location
            WritableWorkbook workbook = Workbook.createWorkbook(file);
            WritableSheet sheet = workbook.createSheet("Expenses", 0);

            // Create header row
            sheet.addCell(new jxl.write.Label(0, 0, "Name of Expense"));
            sheet.addCell(new jxl.write.Label(1, 0, "Type of Expense"));
            sheet.addCell(new jxl.write.Label(2, 0, "Amount"));
            sheet.addCell(new jxl.write.Label(3, 0, "Date"));

            // Add data from database to Excel sheet
            int row = 1; // Start from row 1, as row 0 is the header
            while (rs.next()) {
                sheet.addCell(new jxl.write.Label(0, row, rs.getString("Nameofexpense")));
                sheet.addCell(new jxl.write.Label(1, row, rs.getString("typeofexpense")));
                sheet.addCell(new jxl.write.Number(2, row, rs.getDouble("Amount")));
                sheet.addCell(new jxl.write.Label(3, row, rs.getString("date")));
                row++;
            }

            // Write to workbook and close resources
            workbook.write();
            workbook.close();
            stmt.close();


            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("File Download");
            alert.setHeaderText(null);
            alert.setContentText("Excel file has been downloaded successfully!");
            alert.showAndWait();
            System.out.println("Excel file created successfully with database data.");
        } catch (Exception e) {
            e.printStackTrace();
        }
        // D

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
        lb2.setText(String.valueOf(income));   // Set Income Label
        lb3.setText(String.valueOf(expense));  // Set Expense Label
        lb1.setText(String.valueOf(balance));  // Set Balance Label
    }

    public void loadMonthlyExpenses() {
        String query = "SELECT SUM(Amount), DATE_FORMAT(Date, '%M') AS month FROM information WHERE userid = ? GROUP BY month";

        try {
            // Prepare the SQL query to fetch monthly expenses
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setInt(1, Database.getLoginId()); // Use the logged-in user's ID

            ResultSet rs = ps.executeQuery();

            // Create a series for the BarChart
            XYChart.Series<Number, String> series = new XYChart.Series<>();
            series.setName("Monthly Expenses");

            // A map to store total expenses for each month
            Map<String, Integer> monthlyExpenses = new HashMap<>();

            // Iterate through the result set to fetch the total expenses per month
            while (rs.next()) {
                int totalExpense = rs.getInt(1);
                String month = rs.getString(2);  // Fetch the month (e.g., "September")

                // Add the expenses to the map
                monthlyExpenses.put(month, totalExpense);
            }

            /// Define all months (so that empty months are also included in the Y-axis)
            String[] allMonths = {"January", "February", "March", "April", "May", "June",
                    "July", "August", "September", "October", "November", "December"};

            // Populate the BarChart with data for all months, using 0 for months with no data
            for (String month : allMonths) {
                int totalExpense = monthlyExpenses.getOrDefault(month, 0);  // Use 0 if no data for that month

                // Add each data point to the series (totalExpense on X-axis, month on Y-axis)
                series.getData().add(new XYChart.Data<>(totalExpense, month));
            }


            // Clear existing data and add the new series to the BarChart
            barc.getData().clear();
            barc.getData().add(series);

            // Configure the X-axis to have tick units of 6000
            xAxis.setTickUnit(6000);  // Set the difference between X-axis values
            xAxis.setAutoRanging(false);  // Disable auto-ranging so we can manually control the axis range

            // Set the range manually (you can adjust the upper bound as necessary)
            xAxis.setLowerBound(0);
            xAxis.setUpperBound(40000);  // Set upper bound to 40000
            xAxis.setTickUnit(6000);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void loadMonthlyBalance() {
        // Fetch income for the current user; it will be 0 if no income is found

            // Fetch income for the current user; it will be 0 if no income is found
            int income = getIncomeFromDatabase();

            String queryExpenses = "SELECT SUM(Amount), DATE_FORMAT(Date, '%M') AS month FROM information WHERE userId = ? GROUP BY month";

            try {
                // Prepare the SQL query to fetch monthly expenses
                PreparedStatement ps = conn.prepareStatement(queryExpenses);
                ps.setInt(1, Database.getLoginId()); // Use the logged-in user's ID

                ResultSet rs = ps.executeQuery();

                // Create a series for the LineChart
                XYChart.Series<String, Number> lineSeries = new XYChart.Series<>();
                lineSeries.setName("Monthly Balance");

                // Initialize a map to store expenses by month
                Map<String, Integer> monthlyExpenses = new HashMap<>();

                // Iterate through the result set to fetch the total expenses per month
                while (rs.next()) {
                    int totalExpense = rs.getInt(1);
                    String month = rs.getString(2);  // Fetch the month (e.g., "January")

                    // Store the total expenses in the map
                    monthlyExpenses.put(month, totalExpense);
                }

                // List of all months to ensure they appear on the x-axis
                String[] allMonths = {"January", "February", "March", "April", "May", "June",
                        "July", "August", "September", "October", "November", "December"};

                // Calculate balance for each month and add to the lineSeries


                // Calculate balance for each month and add to the lineSeries
                for (String month : allMonths) {
                    int totalExpense = monthlyExpenses.getOrDefault(month, 0);
                    int balance;

                    // Set the balance based on the income and expenses
                    if (totalExpense == 0) {
                        // If there are no expenses, set balance to 0
                        balance = 0;
                    } else {
                        // Calculate balance normally
                        balance = income - totalExpense;

                        // If balance is negative, set it to 0
                        if (balance < 0) {
                            balance = 0;
                        }
                    }

                    // Add data point for the month
                    lineSeries.getData().add(new XYChart.Data<>(month, balance));
                }

                // Clear existing data and add the new series to the LineChart
                line.getData().clear();
                line.getData().add(lineSeries);

                // Set up the Y-axis
                NumberAxis yAxis = (NumberAxis) line.getYAxis();
                yAxis.setLowerBound(0); // Set the lower bound to 0
                yAxis.setTickUnit(2000); // Set the tick unit
                yAxis.setUpperBound(15000); // Set the upper bound

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }



