package wire;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import java.sql.*;
import java.text.SimpleDateFormat;

public class AdminDashboardController {

    @FXML private Button viewIncomeBtn;
    @FXML private Button registerUserBtn;
    @FXML private Button viewUsersBtn;
    @FXML private Button viewTransactionsBtn;
    @FXML private Button logoutBtn;
    @FXML private TextArea reportArea;

    @FXML
    public void initialize() {
        setupButtonStyles();
    }

    private void setupButtonStyles() {
        String buttonStyle = "-fx-background-color: #2E8B57; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 15;";
        String hoverStyle = "-fx-background-color: #3CB371; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 15;";
        
        Button[] buttons = {viewIncomeBtn, registerUserBtn, viewUsersBtn, viewTransactionsBtn, logoutBtn};
        
        for (Button button : buttons) {
            button.setStyle(buttonStyle);
            button.setOnMouseEntered(e -> {
                button.setStyle(hoverStyle);
                button.setEffect(new DropShadow(10, Color.GREEN));
            });
            button.setOnMouseExited(e -> {
                button.setStyle(buttonStyle);
                button.setEffect(null);
            });
        }
    }

    @FXML
    private void handleViewIncome() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Total income
            String incomeSql = "SELECT SUM(transaction_fee) as total_income FROM company_income";
            PreparedStatement incomeStmt = conn.prepareStatement(incomeSql);
            ResultSet incomeRs = incomeStmt.executeQuery();
            
            double totalIncome = incomeRs.next() ? incomeRs.getDouble("total_income") : 0;
            
            // Monthly breakdown
            String monthlySql = "SELECT strftime('%Y-%m', timestamp) as month, SUM(transaction_fee) as monthly_income FROM company_income GROUP BY month ORDER BY month DESC";
            PreparedStatement monthlyStmt = conn.prepareStatement(monthlySql);
            ResultSet monthlyRs = monthlyStmt.executeQuery();
            
            StringBuilder report = new StringBuilder();
            report.append("=== COMPANY INCOME REPORT ===\n\n");
            report.append(String.format("Total Income: KSh %.2f\n\n", totalIncome));
            report.append("Monthly Breakdown:\n");
            
            while (monthlyRs.next()) {
                String month = monthlyRs.getString("month");
                double monthlyIncome = monthlyRs.getDouble("monthly_income");
                report.append(String.format("%s: KSh %.2f\n", month, monthlyIncome));
            }
            
            reportArea.setText(report.toString());
            
        } catch (SQLException e) {
            showAlert("Error", "Could not generate income report");
        }
    }

    @FXML
    private void handleRegisterUser() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("fxml/register.fxml"));
            Stage stage = (Stage) registerUserBtn.getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleViewUsers() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT name, id_number, phone_number, balance, created_at FROM users ORDER BY created_at DESC";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            
            StringBuilder usersReport = new StringBuilder();
            usersReport.append("=== REGISTERED USERS ===\n\n");
            
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            int count = 0;
            double totalBalance = 0;
            
            while (rs.next()) {
                count++;
                String name = rs.getString("name");
                String idNumber = rs.getString("id_number");
                String phone = rs.getString("phone_number");
                double balance = rs.getDouble("balance");
                String createdAt = sdf.format(rs.getTimestamp("created_at"));
                
                totalBalance += balance;
                
                usersReport.append(String.format("User %d:\n", count));
                usersReport.append(String.format("  Name: %s\n", name));
                usersReport.append(String.format("  ID: %s\n", idNumber));
                usersReport.append(String.format("  Phone: %s\n", phone));
                usersReport.append(String.format("  Balance: KSh %.2f\n", balance));
                usersReport.append(String.format("  Registered: %s\n\n", createdAt));
            }
            
            usersReport.append(String.format("Total Users: %d\n", count));
            usersReport.append(String.format("Total System Balance: KSh %.2f\n", totalBalance));
            
            reportArea.setText(usersReport.toString());
            
        } catch (SQLException e) {
            showAlert("Error", "Could not retrieve users list");
        }
    }

    @FXML
    private void handleViewTransactions() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = """
                SELECT t.transaction_type, t.amount, t.transaction_fee, t.timestamp, 
                       u.name, u.phone_number, t.recipient_phone, t.till_number
                FROM transactions t
                JOIN users u ON t.user_id = u.id
                ORDER BY t.timestamp DESC
                LIMIT 20
            """;
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            
            StringBuilder transactionsReport = new StringBuilder();
            transactionsReport.append("=== RECENT TRANSACTIONS ===\n\n");
            
            SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm");
            double totalVolume = 0;
            double totalFees = 0;
            
            while (rs.next()) {
                String type = rs.getString("transaction_type");
                double amount = rs.getDouble("amount");
                double fee = rs.getDouble("transaction_fee");
                String timestamp = sdf.format(rs.getTimestamp("timestamp"));
                String name = rs.getString("name");
                String phone = rs.getString("phone_number");
                String recipient = rs.getString("recipient_phone");
                String till = rs.getString("till_number");
                
                totalVolume += amount;
                totalFees += fee;
                
                transactionsReport.append(String.format("[%s] %s: KSh %.2f", timestamp, type, amount));
                transactionsReport.append(String.format(" (User: %s - %s)", name, phone));
                
                if (recipient != null) {
                    transactionsReport.append(String.format(" → %s", recipient));
                }
                if (till != null) {
                    transactionsReport.append(String.format(" [Till: %s]", till));
                }
                if (fee > 0) {
                    transactionsReport.append(String.format(" [Fee: KSh %.2f]", fee));
                }
                
                transactionsReport.append("\n");
            }
            
            transactionsReport.append(String.format("\nTotal Volume: KSh %.2f\n", totalVolume));
            transactionsReport.append(String.format("Total Fees: KSh %.2f\n", totalFees));
            
            reportArea.setText(transactionsReport.toString());
            
        } catch (SQLException e) {
            showAlert("Error", "Could not retrieve transactions");
        }
    }

    @FXML
    private void handleLogout() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("fxml/login.fxml"));
            Stage stage = (Stage) logoutBtn.getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}