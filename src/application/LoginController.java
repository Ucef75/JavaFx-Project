package application;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import utils.Database;

public class LoginController implements Initializable {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Button signupButton;
    @FXML private Label forgotPasswordLabel;
    @FXML private Label errorLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Set up login button action
        loginButton.setOnAction(event -> handleLogin());
        
        // Set up signup button action
        signupButton.setOnAction(event -> switchToSignup());
        
        // Set up forgot password action
        forgotPasswordLabel.setOnMouseClicked(event -> handleForgotPassword());
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        // Validate inputs
        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both username and password");
            return;
        }

        // Authenticate user
        try {
            if (authenticateUser(username, password)) {
                showSuccess("Login successful!");
                // Proceed to main application
                navigateToMainApp();
            } else {
                showError("Invalid username or password");
            }
        } catch (SQLException e) {
            showError("Database error. Please try again.");
            e.printStackTrace();
        }
    }

    private boolean authenticateUser(String username, String password) throws SQLException {
        String sql = "SELECT password FROM Users WHERE username = ?";
        
        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                String storedPassword = rs.getString("password");
                // In a real app, you would compare hashed passwords here
                return password.equals(storedPassword);
            }
            return false;
        }
    }

    private void handleForgotPassword() {
        // Implement forgot password functionality
        showError("Forgot password functionality not implemented yet");
    }

    private void switchToSignup() {
        // Implement navigation to signup screen
        try {
            SceneManager.switchToScene("/view/registerview.fxml");
        } catch (Exception e) {
            showError("Could not load registration screen");
            e.printStackTrace();
        }
    }

    private void navigateToMainApp() {
        // Implement navigation to main application
        try {
            SceneManager.switchToScene("/mainview.fxml");
        } catch (Exception e) {
            showError("Could not load application");
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setStyle("-fx-text-fill: red;");
    }

    private void showSuccess(String message) {
        errorLabel.setText(message);
        errorLabel.setStyle("-fx-text-fill: green;");
    }
}