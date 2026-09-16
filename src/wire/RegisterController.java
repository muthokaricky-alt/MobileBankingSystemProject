package wire;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.sql.*;

public class RegisterController {

    @FXML private TextField nameField;
    @FXML private TextField idNumberField;
    @FXML private TextField phoneField;
    @FXML private PasswordField pinField;
    @FXML private PasswordField confirmPinField;
    @FXML private Button registerBtn;
    @FXML private Button backBtn;

    @FXML
    public void initialize() {
        setupButtonStyles();
    }

    private void setupButtonStyles() {
        String buttonStyle = "-fx-background-color: #2E8B57; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 15;";
        registerBtn.setStyle(buttonStyle);
        backBtn.setStyle(buttonStyle);
    }

    @FXML
    private void handleRegister() {
        String name = nameField.getText();
        String idNumber = idNumberField.getText();
        String phone = phoneField.getText();
        String pin = pinField.getText();
        String confirmPin = confirmPinField.getText();

        if (name.isEmpty() || idNumber.isEmpty() || phone.isEmpty() || pin.isEmpty()) {
            showAlert("Error", "Please fill in all fields");
            return;
        }

        if (!pin.equals(confirmPin)) {
            showAlert("Error", "PINs do not match");
            return;
        }

        if (pin.length() != 4) {
            showAlert("Error", "PIN must be 4 digits");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Check if user already exists
            String checkSql = "SELECT * FROM users WHERE id_number = ? OR phone_number = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, idNumber);
            checkStmt.setString(2, phone);
            
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                showAlert("Error", "ID Number or Phone Number already registered");
                return;
            }

            // Register new user with name
            String insertSql = "INSERT INTO users (name, id_number, phone_number, pin) VALUES (?, ?, ?, ?)";
            PreparedStatement insertStmt = conn.prepareStatement(insertSql);
            insertStmt.setString(1, name);
            insertStmt.setString(2, idNumber);
            insertStmt.setString(3, phone);
            insertStmt.setString(4, hashPin(pin));
            
            insertStmt.executeUpdate();
            
            showAlert("Success", "Registration successful! Please login.");
            handleBack();
            
        } catch (SQLException e) {
            showAlert("Database Error", "Error registering user");
        }
    }

    @FXML
    private void handleBack() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("fxml/login.fxml"));
            Stage stage = (Stage) backBtn.getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
        } catch (Exception e) {
            e.printStackTrace();
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

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}