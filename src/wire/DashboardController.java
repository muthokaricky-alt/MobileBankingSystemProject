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
import java.util.Optional;

public class DashboardController {

    @FXML private Label welcomeLabel;
    @FXML private Label balanceLabel;
    @FXML private Button depositBtn;
    @FXML private Button withdrawBtn;
    @FXML private Button sendMoneyBtn;
    @FXML private Button payBillBtn;
    @FXML private Button savingsBtn;
    @FXML private Button statementBtn;
    @FXML private Button changePinBtn;
    @FXML private Button logoutBtn;

    private User user;

    public void setUser(User user) {
        this.user = user;
        updateDashboard();
    }

    @FXML
    public void initialize() {
        setupButtonStyles();
    }

    private void setupButtonStyles() {
        String buttonStyle = "-fx-background-color: #2E8B57; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 15;";
        String hoverStyle = "-fx-background-color: #3CB371; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 15;";
        
        Button[] buttons = {depositBtn, withdrawBtn, sendMoneyBtn, payBillBtn, savingsBtn, statementBtn, changePinBtn, logoutBtn};
        
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

    private void updateDashboard() {
        if (user != null) {
            welcomeLabel.setText("Welcome, " + user.getName());
            balanceLabel.setText(String.format("KSh %.2f", user.getBalance()));
        }
    }

    @FXML
    private void handleDeposit() {
        showAmountDialog("Deposit", "Enter amount to deposit:", "DEPOSIT");
    }

    @FXML
    private void handleWithdraw() {
        showAmountDialog("Withdraw", "Enter amount to withdraw:", "WITHDRAWAL");
    }

    @FXML
    private void handleSendMoney() {
        TextInputDialog phoneDialog = new TextInputDialog();
        phoneDialog.setTitle("Send Money");
        phoneDialog.setHeaderText("Enter recipient's phone number:");
        phoneDialog.setContentText("Phone:");

        phoneDialog.showAndWait().ifPresent(phone -> {
            if (phone.length() != 10) {
                showAlert("Error", "Please enter a valid 10-digit phone number");
                return;
            }
            showAmountDialog("Send Money", "Enter amount to send to " + phone + ":", "SEND_MONEY", phone);
        });
    }

    @FXML
    private void handlePayBill() {
        TextInputDialog tillDialog = new TextInputDialog();
        tillDialog.setTitle("Pay Bill");
        tillDialog.setHeaderText("Enter till number:");
        tillDialog.setContentText("Till Number:");

        tillDialog.showAndWait().ifPresent(till -> {
            showAmountDialog("Pay Bill", "Enter amount to pay to till " + till + ":", "PAY_BILL", null, till);
        });
    }

    @FXML
    private void handleSavings() {
        showAmountDialog("Savings", "Enter amount to save:", "SAVINGS");
    }

    @FXML
    private void handleStatement() {
        showStatement();
    }

    @FXML
    private void handleChangePin() {
        // Step 1: Get user's ID number for verification
        TextInputDialog idDialog = new TextInputDialog();
        idDialog.setTitle("Change PIN");
        idDialog.setHeaderText("Enter your ID Number to verify your identity");
        idDialog.setContentText("ID Number:");
        
        Optional<String> idResult = idDialog.showAndWait();
        if (!idResult.isPresent()) return;
        
        String idNumber = idResult.get();
        
        // Step 2: Verify ID number matches the logged-in user
        if (!idNumber.equals(user.getIdNumber())) {
            showAlert("Error", "ID Number does not match our records");
            return;
        }
        
        // Step 3: Get new PIN
        TextInputDialog newPinDialog = new TextInputDialog();
        newPinDialog.setTitle("Change PIN");
        newPinDialog.setHeaderText("Enter new 4-digit PIN");
        newPinDialog.setContentText("New PIN:");
        
        Optional<String> newPinResult = newPinDialog.showAndWait();
        if (!newPinResult.isPresent()) return;
        
        String newPin = newPinResult.get();
        
        // Step 4: Validate new PIN
        if (newPin.length() != 4 || !newPin.matches("\\d+")) {
            showAlert("Error", "PIN must be 4 digits (numbers only)");
            return;
        }
        
        // Step 5: Confirm new PIN
        TextInputDialog confirmPinDialog = new TextInputDialog();
        confirmPinDialog.setTitle("Change PIN");
        confirmPinDialog.setHeaderText("Confirm your new PIN");
        confirmPinDialog.setContentText("Confirm PIN:");
        
        Optional<String> confirmPinResult = confirmPinDialog.showAndWait();
        if (!confirmPinResult.isPresent()) return;
        
        String confirmPin = confirmPinResult.get();
        
        // Step 6: Check if PINs match
        if (!newPin.equals(confirmPin)) {
            showAlert("Error", "PINs do not match");
            return;
        }
        
        // Step 7: Update PIN in database
        updatePinInDatabase(newPin);
    }

    private void updatePinInDatabase(String newPin) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "UPDATE users SET pin = ? WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, hashPin(newPin));
            stmt.setInt(2, user.getId());
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                // Update user object with new PIN
                user.setPin(hashPin(newPin));
                showAlert("Success", "PIN changed successfully!");
            } else {
                showAlert("Error", "Failed to change PIN");
            }
            
        } catch (SQLException e) {
            showAlert("Database Error", "Error changing PIN");
        }
    }

    private String hashPin(String pin) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(pin.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (Exception e) {
            return pin;
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

    private void showAmountDialog(String title, String content, String transactionType) {
        showAmountDialog(title, content, transactionType, null, null);
    }

    private void showAmountDialog(String title, String content, String transactionType, String recipientPhone) {
        showAmountDialog(title, content, transactionType, recipientPhone, null);
    }

    private void showAmountDialog(String title, String content, String transactionType, String recipientPhone, String tillNumber) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(title);
        dialog.setHeaderText(content);
        dialog.setContentText("Amount:");

        dialog.showAndWait().ifPresent(amountStr -> {
            try {
                double amount = Double.parseDouble(amountStr);
                processTransaction(transactionType, amount, recipientPhone, tillNumber);
            } catch (NumberFormatException e) {
                showAlert("Error", "Please enter a valid amount");
            }
        });
    }

    private void processTransaction(String type, double amount, String recipientPhone, String tillNumber) {
        if (amount <= 0) {
            showAlert("Error", "Amount must be positive");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                switch (type) {
                    case "DEPOSIT":
                        processDeposit(conn, amount);
                        break;
                    case "WITHDRAWAL":
                        processWithdrawal(conn, amount);
                        break;
                    case "SEND_MONEY":
                        processSendMoney(conn, amount, recipientPhone);
                        break;
                    case "PAY_BILL":
                        processPayBill(conn, amount, tillNumber);
                        break;
                    case "SAVINGS":
                        processSavings(conn, amount);
                        break;
                }
                conn.commit();
                updateDashboard();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            showAlert("Error", "Transaction failed: " + e.getMessage());
        }
    }

    private void processDeposit(Connection conn, double amount) throws SQLException {
        updateBalance(conn, amount);
        recordTransaction(conn, "DEPOSIT", amount, null, null, 0);
        showAlert("Success", String.format("Deposited KSh %.2f successfully", amount));
    }

    private void processWithdrawal(Connection conn, double amount) throws SQLException {
        if (amount > user.getBalance()) {
            throw new SQLException("Insufficient balance");
        }
        updateBalance(conn, -amount);
        recordTransaction(conn, "WITHDRAWAL", amount, null, null, 0);
        showAlert("Success", String.format("Withdrawn KSh %.2f successfully", amount));
    }

    private void processSendMoney(Connection conn, double amount, String recipientPhone) throws SQLException {
        double fee = amount > 500 ? amount * 0.001 : 0;
        double total = amount + fee;

        if (total > user.getBalance()) {
            throw new SQLException("Insufficient balance including transaction fee");
        }

        updateBalance(conn, -total);
        recordTransaction(conn, "SEND_MONEY", amount, recipientPhone, null, fee);
        
        if (fee > 0) {
            recordCompanyIncome(conn, getLastTransactionId(conn), fee);
        }

        showAlert("Success", String.format("Sent KSh %.2f to %s. Fee: KSh %.2f", amount, recipientPhone, fee));
    }

    private void processPayBill(Connection conn, double amount, String tillNumber) throws SQLException {
        double fee = amount > 500 ? amount * 0.001 : 0;
        double total = amount + fee;

        if (total > user.getBalance()) {
            throw new SQLException("Insufficient balance including transaction fee");
        }

        updateBalance(conn, -total);
        recordTransaction(conn, "PAY_BILL", amount, null, tillNumber, fee);
        
        if (fee > 0) {
            recordCompanyIncome(conn, getLastTransactionId(conn), fee);
        }

        showAlert("Success", String.format("Paid KSh %.2f to till %s. Fee: KSh %.2f", amount, tillNumber, fee));
    }

    private void processSavings(Connection conn, double amount) throws SQLException {
        if (amount > user.getBalance()) {
            throw new SQLException("Insufficient balance");
        }

        updateBalance(conn, -amount);
        recordTransaction(conn, "SAVINGS", amount, null, null, 0);
        
        String savingsSql = "INSERT INTO savings (user_id, amount) VALUES (?, ?)";
        PreparedStatement stmt = conn.prepareStatement(savingsSql);
        stmt.setInt(1, user.getId());
        stmt.setDouble(2, amount);
        stmt.executeUpdate();

        showAlert("Success", String.format("Saved KSh %.2f successfully", amount));
    }

    private void updateBalance(Connection conn, double amount) throws SQLException {
        String sql = "UPDATE users SET balance = balance + ? WHERE id = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setDouble(1, amount);
        stmt.setInt(2, user.getId());
        stmt.executeUpdate();
        
        user.setBalance(user.getBalance() + amount);
    }

    private void recordTransaction(Connection conn, String type, double amount, String recipient, String till, double fee) throws SQLException {
        String sql = "INSERT INTO transactions (user_id, transaction_type, amount, recipient_phone, till_number, transaction_fee) VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, user.getId());
        stmt.setString(2, type);
        stmt.setDouble(3, amount);
        stmt.setString(4, recipient);
        stmt.setString(5, till);
        stmt.setDouble(6, fee);
        stmt.executeUpdate();
    }

    private int getLastTransactionId(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid()");
        return rs.getInt(1);
    }

    private void recordCompanyIncome(Connection conn, int transactionId, double fee) throws SQLException {
        String sql = "INSERT INTO company_income (transaction_id, transaction_fee) VALUES (?, ?)";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, transactionId);
        stmt.setDouble(2, fee);
        stmt.executeUpdate();
    }

    private void showStatement() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM transactions WHERE user_id = ? ORDER BY timestamp DESC LIMIT 10";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, user.getId());
            
            ResultSet rs = stmt.executeQuery();
            
            StringBuilder statement = new StringBuilder();
            statement.append("Recent Transactions:\n\n");
            
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            
            while (rs.next()) {
                String type = rs.getString("transaction_type");
                double amount = rs.getDouble("amount");
                String timestamp = sdf.format(rs.getTimestamp("timestamp"));
                String recipient = rs.getString("recipient_phone");
                String till = rs.getString("till_number");
                double fee = rs.getDouble("transaction_fee");
                
                statement.append(timestamp).append(" - ").append(type).append(": KSh ").append(String.format("%.2f", amount));
                
                if (recipient != null) {
                    statement.append(" to ").append(recipient);
                }
                if (till != null) {
                    statement.append(" (Till: ").append(till).append(")");
                }
                if (fee > 0) {
                    statement.append(" [Fee: KSh ").append(String.format("%.2f", fee)).append("]");
                }
                statement.append("\n");
            }
            
            TextArea textArea = new TextArea(statement.toString());
            textArea.setEditable(false);
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Bank Statement");
            alert.setHeaderText("Your Recent Transactions");
            alert.getDialogPane().setContent(textArea);
            alert.showAndWait();
            
        } catch (SQLException e) {
            showAlert("Error", "Could not retrieve statement");
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