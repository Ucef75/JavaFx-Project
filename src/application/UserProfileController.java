package application;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.User;

import java.io.File;
import java.io.IOException;

public class UserProfileController {

    @FXML
    private Label usernameLabel;

    @FXML
    private ImageView userAvatar;

    @FXML
    private Label userFullName;

    @FXML
    private Label joinDateLabel;

    @FXML
    private Label countryLabel;

    @FXML
    private Button editProfileButton;

    @FXML
    private Button saveProfileButton;

    @FXML
    private Button cancelEditButton;

    @FXML
    private PasswordField currentPasswordField;

    @FXML
    private PasswordField newPasswordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private VBox editProfilePane;

    @FXML
    private VBox viewProfilePane;

    private User user;
    private boolean isEditMode = false;

    @FXML
    public void initialize() {
        // Hide edit controls initially
        if (editProfilePane != null) {
            editProfilePane.setVisible(false);
            editProfilePane.setManaged(false);
        }
    }

    public void setUser(User user) {
        this.user = user;
        updateUserDisplay();
    }

    private void updateUserDisplay() {
        usernameLabel.setText(user.getUsername() + "'s Profile");
        userFullName.setText(user.getUsername());
        joinDateLabel.setText(user.getCreated());
        countryLabel.setText(user.getCountry());

        // Load avatar
        if (user.getAvatarPath() != null && !user.getAvatarPath().isEmpty()) {
            userAvatar.setImage(new Image(user.getAvatarPath()));
        } else {
            userAvatar.setImage(new Image("/Avatar-photos/default-avatar.jpg")); // Default avatar
        }
    }

    @FXML
    public void editProfile() {
        // Toggle edit mode
        isEditMode = true;
        viewProfilePane.setVisible(false);
        viewProfilePane.setManaged(false);
        editProfilePane.setVisible(true);
        editProfilePane.setManaged(true);

        // Clear password fields for security
        currentPasswordField.clear();
        newPasswordField.clear();
        confirmPasswordField.clear();
    }

    @FXML
    public void saveProfile() {
        String currentPassword = currentPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Validate current password
        if (!currentPassword.equals(user.getPassword())) {
            showAlert(Alert.AlertType.ERROR, "Password Error", "Current password is incorrect.");
            return;
        }

        // Validate new password
        if (!newPassword.isEmpty()) {
            if (!newPassword.equals(confirmPassword)) {
                showAlert(Alert.AlertType.ERROR, "Password Error", "New passwords don't match.");
                return;
            }

            // Update password
            user.setPassword(newPassword);
        }

        // Exit edit mode
        isEditMode = false;
        viewProfilePane.setVisible(true);
        viewProfilePane.setManaged(true);
        editProfilePane.setVisible(false);
        editProfilePane.setManaged(false);

        // Show success message
        showAlert(Alert.AlertType.INFORMATION, "Success", "Profile updated successfully.");
    }

    @FXML
    public void cancelEdit() {
        // Exit edit mode without saving
        isEditMode = false;
        viewProfilePane.setVisible(true);
        viewProfilePane.setManaged(true);
        editProfilePane.setVisible(false);
        editProfilePane.setManaged(false);
    }

    @FXML
    public void closeProfile() {
        Stage stage = (Stage) usernameLabel.getScene().getWindow();
        stage.close();
    }

    @FXML
    public void chooseAvatar() {
        // Open a dialog to let the user choose from predefined avatars
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/ChooseAvatarDialog.fxml"));
            Scene scene = new Scene(loader.load());

            ChooseAvatarController controller = loader.getController();
            controller.setUser(user);

            Stage stage = new Stage();
            stage.setTitle("Choose Avatar");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            updateUserDisplay(); // Reload user info
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void uploadAvatar() {
        // Let the user upload a custom avatar
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Upload Avatar");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        File file = fileChooser.showOpenDialog(usernameLabel.getScene().getWindow());
        if (file != null) {
            user.setAvatarPath(file.toURI().toString());
            updateUserDisplay();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}