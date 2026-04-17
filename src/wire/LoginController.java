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

public class LoginController {

    @FXML private TextField phoneField;
    @FXML private PasswordField pinField;
    @FXML private Button loginBtn;
    @FXML private Button registerBtn;
    @FXML private Button adminLoginBtn;

    @FXML
    public void initialize() {
        setupButtonStyles();
    }

    private void setupButtonStyles() {
        String buttonStyle = "-fx-background-color: #2E8B57; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 15;";
        String hoverStyle = "-fx-background-color: #3CB371; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 15;";
        
        Button[] buttons = {loginBtn, registerBtn, adminLoginBtn};
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
    private void handleLogin() {
        String phone = phoneField.getText();
        String pin = pinField.getText();

        if (phone.isEmpty() || pin.isEmpty()) {
            showAlert("Error", "Please enter both phone number and PIN");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM users WHERE phone_number = ? AND pin = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, phone);
            stmt.setString(2, hashPin(pin));
            
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                User user = new User(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("id_number"),
                    rs.getString("phone_number"),
                    rs.getString("pin"),
                    rs.getDouble("balance")
                );
                openDashboard(user);
            } else {
                showAlert("Error", "Invalid phone number or PIN");
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Error connecting to database");
        }
    }

    @FXML
    private void handleRegister() {
        loadScreen("register.fxml");
    }

    @FXML
    private void handleAdminLogin() {
        loadScreen("admin_login.fxml");
    }

    private void openDashboard(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("fxml/dashboard.fxml"));
            Parent root = loader.load();
            DashboardController controller = loader.getController();
            controller.setUser(user);
            Stage stage = (Stage) loginBtn.getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadScreen(String fxmlFile) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("fxml/" + fxmlFile));
            Stage stage = (Stage) loginBtn.getScene().getWindow();
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
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}