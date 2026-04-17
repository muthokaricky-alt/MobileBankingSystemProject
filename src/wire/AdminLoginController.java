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

public class AdminLoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginBtn;
    @FXML private Button backBtn;

    @FXML
    public void initialize() {
        setupButtonStyles();
    }

    private void setupButtonStyles() {
        String buttonStyle = "-fx-background-color: #2E8B57; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 15;";
        String hoverStyle = "-fx-background-color: #3CB371; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 15;";
        
        loginBtn.setStyle(buttonStyle);
        backBtn.setStyle(buttonStyle);
        
        loginBtn.setOnMouseEntered(e -> {
            loginBtn.setStyle(hoverStyle);
            loginBtn.setEffect(new DropShadow(10, Color.GREEN));
        });
        loginBtn.setOnMouseExited(e -> {
            loginBtn.setStyle(buttonStyle);
            loginBtn.setEffect(null);
        });
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Please enter both username and password");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM admins WHERE username = ? AND password = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, password);
            
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                openAdminDashboard();
            } else {
                showAlert("Error", "Invalid admin credentials");
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Error connecting to database");
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

    private void openAdminDashboard() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("fxml/admin_dashboard.fxml"));
            Stage stage = (Stage) loginBtn.getScene().getWindow();
            stage.setScene(new Scene(root, 800, 600));
        } catch (Exception e) {
            e.printStackTrace();
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