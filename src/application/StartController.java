package application;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class StartController {
    @FXML
    private ImageView logoImage;

    @FXML
    private Button loginButton;

    @FXML
    private Button signupButton;

    @FXML
    private Button exitButton;

    @FXML
    private void handleLoginButtonAction() {
        try {
            // Load the login view
            Parent root = FXMLLoader.load(getClass().getResource("/view/loginview.fxml"));
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Login to Pixely");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSignupButtonAction() {
        try {
            // Load the register view
            Parent root = FXMLLoader.load(getClass().getResource("/view/registerview.fxml"));
            Stage stage = (Stage) signupButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Register for Pixely");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleExitButtonAction() {
        Stage stage = (Stage) exitButton.getScene().getWindow();
        stage.close();
    }
}