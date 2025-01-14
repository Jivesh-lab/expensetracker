package com.example.expense;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.Date;
public class Database {
    private static final String URL = "jdbc:mysql://localhost:3306/expense";
    private static final String USER = "root";
    private static final String PASSWORD = "mynameisjp";
    public static Connection conn = null;
    private static Integer loggedInUserId;

    protected static int getLoginId()
    {
        return loggedInUserId;
    }

    public static void connect() {
        try {
            // Load the MySQL JDBC Driver
            Class.forName("com.mysql.cj.jdbc.Driver"); // Ensure the driver is available

            // Establish the connection
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connection established successfully.");

            // You can perform operations here...


        } catch (ClassNotFoundException e) {
            System.out.println("MySQL JDBC Driver not found.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("Connection failed.");
            e.printStackTrace();
        }

    }

    public static void insert(String user, String mail, String pass, String conf) {
        String query = "INSERT INTO customer (cusname, email, password,confirm) VALUES (?, ?, ?,?)";

        try {
            // Load the MySQL JDBC Driver


            PreparedStatement pstmt = conn.prepareStatement(query);
            // Set the values for the query
            pstmt.setString(1, user);  // Replace first '?' with 'username'
            pstmt.setString(2, mail);     // Replace second '?' with 'email'
            pstmt.setString(3, pass);
            pstmt.setString(4, conf);  // Replace third '?' with 'password'

            // Execute the query (perform the actual insert into the database)
            pstmt.executeUpdate();

            System.out.println("Data inserted successfully!");

        } catch (SQLException e) {
            System.out.println("Exception is :" + e);
            e.printStackTrace();
        }

    }

    protected static boolean check(String user, String password) {
        String user2 = user;
        String password2 = password;
        String query = "SELECT * FROM customer WHERE cusname = ?  And password = ? ";
        try {
            // Prepare the SQL statement with placeholders for parameters
            PreparedStatement pstmt = conn.prepareStatement(query);

            // Set the values for the query
            pstmt.setString(1, user2);      // Set the first placeholder for username
            pstmt.setString(2, password2);  // Set the second placeholder for password

            // Execute the query and get the result
            ResultSet rs = pstmt.executeQuery();

            // Check if a record exists (i.e., user credentials match)
            if (rs.next()) {
                // User found, return true for a successful login
                loggedInUserId = rs.getInt("cusid");
                System.out.println("Login successful! Welcome, " + user2);
                return true;
            } else {
                // No matching user found, return false for login failure
                System.out.println("Login failed. Invalid username or password.");
                return false;
            }

        } catch (SQLException e) {
            System.out.println("SQL Exception: " + e.getMessage());
            e.printStackTrace();
            return false;  // Return false if an exception occurs
        }

    }

    protected static boolean alter(String naexp, String typexp, int amount, int allocate, Integer income, Date date, String Descr) {
        Integer userId = loggedInUserId;

        // SQL INSERT statement
        String query = "INSERT INTO  information (NameofExpense,typeofexpense,Amount,Allocate,Date,income,Description,userid) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            // Prepare the SQL statement with placeholders
            PreparedStatement pstmt = conn.prepareStatement(query);

            // Set the values for the query
            pstmt.setString(1, naexp);
            pstmt.setString(2, typexp);
            pstmt.setInt(3,amount);
            pstmt.setInt(4, allocate);
            pstmt.setDate(5, new java.sql.Date(date.getTime()));

            // Check if income is null and set it accordingly
            if (income != null) {
                pstmt.setInt(6, income);  // Set income if it's not null
            } else {
                pstmt.setNull(6, java.sql.Types.INTEGER); // Set to NULL if income is null
            }
            pstmt.setObject(6,income,java.sql.Types.INTEGER);
            pstmt.setString(7, Descr);
            pstmt.setInt(8, userId);

            // Execute the insert query
            int rowsAffected = pstmt.executeUpdate();

            // Check if the insertion was successful
            return rowsAffected > 0;  // Return true if at least one row was inserted

        } catch (SQLException e) {
            System.out.println("SQL Exception: " + e.getMessage());
            e.printStackTrace();
            return false;  // Return false if an exception occurs
        }
    }
    protected static int getIncomeFromDatabase() {
        int income = 0;
        int userId = getLoginId();  // Get the logged-in user ID

        String sql = "SELECT income AS total_income FROM information WHERE userId = ?  AND income IS NOT NULL LIMIT 1";

        try (
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Set the userId parameter in the SQL query
            pstmt.setInt(1, userId);

            // Execute the query and fetch the result
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    income = rs.getInt("total_income");  // Get the single income value
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return income;
    }
    protected static int getExpenseFromDatabase() {
        int expense = 0;
        int userId = getLoginId();  // Get the logged-in user ID

        String sql = "SELECT SUM(amount) AS total_expense FROM information WHERE userId = ? AND amount IS NOT NULL";

        try (             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Set the userId parameter in the SQL query
            pstmt.setInt(1, userId);

            // Execute the query and fetch the result
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    expense = rs.getInt("total_expense");
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return expense;
    }


    protected static int getExpens() {
        int expense = 0;
        int userId = getLoginId();  // Get the logged-in user ID

        // Get the current month and year
        LocalDate currentDate = LocalDate.now();
        int currentMonth = currentDate.getMonthValue(); // 1-12
        int currentYear = currentDate.getYear(); // e.g., 2024

        // Adjust the SQL query to filter expenses for the current month and year
        String sql = "SELECT SUM(amount) AS total_expense FROM information WHERE userId = ? AND MONTH(date) = ? AND YEAR(date) = ? AND amount IS NOT NULL";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // Set the userId and current month/year parameters in the SQL query
            pstmt.setInt(1, userId);
            pstmt.setInt(2, currentMonth);
            pstmt.setInt(3, currentYear);

            // Execute the query and fetch the result
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    expense = rs.getInt("total_expense");
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return expense;
    }



}











