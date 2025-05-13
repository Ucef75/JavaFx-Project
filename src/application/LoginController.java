package application;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import utils.Database;

public class LoginController implements Initializable {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Button signupButton;
    @FXML private Label forgotPasswordLabel;
    @FXML private Label errorLabel;

    private String loggedInUsername; // Add this field

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loginButton.setOnAction(event -> handleLogin());
        signupButton.setOnAction(event -> switchToSignup());
        forgotPasswordLabel.setOnMouseClicked(event -> handleForgotPassword());
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both username and password");
            return;
        }

        try {
            if (authenticateUser(username, password)) {
                showSuccess("Login successful!");
                navigateToMainApp(username);
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
                return password.equals(storedPassword);
            }
            return false;
        }
    }

    private void handleForgotPassword() {
        showError("Forgot password functionality not implemented yet");
    }

    private void switchToSignup() {
        try {
            Stage currentStage = (Stage) signupButton.getScene().getWindow();
            SceneManager.switchToScene("/view/registerview.fxml", currentStage);
        } catch (Exception e) {
            showError("Could not load registration screen");
            e.printStackTrace();
        }
    }

    private void navigateToMainApp(String username) {
        try {
            Stage currentStage = (Stage) loginButton.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/PixelyMain.fxml"));
            Parent root = loader.load();

            // Pass the username to the main controller
            PixelyController controller = loader.getController();
            controller.setLoggedInUsername(username);

            Stage newStage = new Stage();
            newStage.setScene(new Scene(root));
            newStage.show();

            currentStage.close();
        } catch (Exception e) {
            showError("Could not load application");
            e.printStackTrace();
        }
    }

    private void loadCurrentUser() {
        // This method would be implemented if you need to load user data
        // Currently just storing the username
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