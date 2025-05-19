package application;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.User;
import utils.Database;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

public class UserProfileController {

    @FXML private Label usernameLabel;
    @FXML private ImageView userAvatar;
    @FXML private ImageView editUserAvatar;
    @FXML private Label userFullName;
    @FXML private Label joinDateLabel;
    @FXML private Label countryLabel;
    @FXML private Label gamesPlayedLabel;
    @FXML private Label achievementsLabel;

    @FXML private Button editProfileButton;
    @FXML private Button saveProfileButton;
    @FXML private Button cancelEditButton;
    @FXML private Button chooseAvatarButton;
    @FXML private Button uploadAvatarButton;
    @FXML private Button chooseAvatarEditButton;
    @FXML private Button uploadAvatarEditButton;

    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private ProgressBar passwordStrengthBar;
    @FXML private Label passwordStrengthLabel;
    @FXML private Label currentPasswordError;
    @FXML private Label newPasswordError;
    @FXML private Label confirmPasswordError;

    @FXML private VBox editProfilePane;
    @FXML private VBox viewProfilePane;
    @FXML private TabPane profileTabPane;

    private User user;
    private boolean isEditMode = false;
    private String newAvatarPath;
    private static final int MIN_PASSWORD_LENGTH = 8;

    @FXML
    public void initialize() {
        // Hide edit controls initially
        if (editProfilePane != null) {
            editProfilePane.setVisible(false);
            editProfilePane.setManaged(false);
        }

        // Setup password strength indicator
        if (newPasswordField != null) {
            newPasswordField.textProperty().addListener((observable, oldValue, newValue) -> {
                updatePasswordStrength(newValue);
                validateNewPassword();
            });
        }

        // Add validation listeners
        if (currentPasswordField != null) {
            currentPasswordField.focusedProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal && currentPasswordField.getText().isEmpty()) {
                    currentPasswordError.setText("Current password is required");
                } else {
                    currentPasswordError.setText("");
                }
            });
        }

        if (confirmPasswordField != null) {
            confirmPasswordField.focusedProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal) validateConfirmPassword();
            });
        }

        // Add visual effects
        if (userAvatar != null) {
            userAvatar.setEffect(new DropShadow(10, Color.rgb(0, 0, 0, 0.5)));
        }

        if (editUserAvatar != null) {
            editUserAvatar.setEffect(new DropShadow(10, Color.rgb(0, 0, 0, 0.5)));
        }

        // Disable save button unless password fields are valid
        if (saveProfileButton != null && currentPasswordField != null && newPasswordField != null && confirmPasswordField != null) {
            saveProfileButton.disableProperty().bind(
                    Bindings.createBooleanBinding(() ->
                                    currentPasswordField.getText().isEmpty() ||
                                            (!newPasswordField.getText().isEmpty() && newPasswordField.getText().length() < MIN_PASSWORD_LENGTH) ||
                                            (!confirmPasswordField.getText().isEmpty() && !confirmPasswordField.getText().equals(newPasswordField.getText())),
                            currentPasswordField.textProperty(),
                            newPasswordField.textProperty(),
                            confirmPasswordField.textProperty()
                    )
            );
        }
    }

    public void setUser(User user) {
        this.user = user;
        updateUserDisplay();

        // Load default avatar to edit pane
        if (editUserAvatar != null && user.getAvatarPath() != null) {
            editUserAvatar.setImage(userAvatar.getImage());
        }
    }

    private void updateUserDisplay() {
        if (user == null) return;

        usernameLabel.setText(user.getUsername() + "'s Profile");
        userFullName.setText(user.getUsername());
        joinDateLabel.setText("Member since: " + user.getCreated());
        countryLabel.setText("From: " + user.getCountry());

        // Set sample stats (replace with real data when available)
        if (gamesPlayedLabel != null) {
            gamesPlayedLabel.setText("0");
        }

        if (achievementsLabel != null) {
            achievementsLabel.setText("0");
        }

        // Load avatar with error handling
        loadAvatar(userAvatar, user.getAvatarPath());
    }

    private void loadAvatar(ImageView imageView, String avatarPath) {
        try {
            if (avatarPath != null && !avatarPath.isEmpty()) {
                Image avatar;

                // Handle file:/ protocol for uploaded files
                if (avatarPath.startsWith("file:/")) {
                    avatar = new Image(avatarPath);
                } else {
                    // For resource paths
                    InputStream imageStream = getClass().getResourceAsStream(avatarPath);
                    if (imageStream != null) {
                        avatar = new Image(imageStream);
                    } else {
                        avatar = new Image("/Avatar-photos/default-avatar.jpg");
                    }
                }

                imageView.setImage(avatar);
            } else {
                imageView.setImage(new Image("/Avatar-photos/default-avatar.jpg"));
            }
        } catch (Exception e) {
            System.err.println("Failed to load avatar: " + e.getMessage());
            imageView.setImage(new Image("/Avatar-photos/default-avatar.jpg"));
        }
    }

    @FXML
    public void editProfile() {
        // Apply transition effect
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), viewProfilePane);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        fadeOut.setOnFinished(event -> {
            // Toggle edit mode
            isEditMode = true;
            viewProfilePane.setVisible(false);
            viewProfilePane.setManaged(false);
            editProfilePane.setVisible(true);
            editProfilePane.setManaged(true);

            // Copy avatar to edit view
            editUserAvatar.setImage(userAvatar.getImage());

            // Clear password fields for security
            currentPasswordField.clear();
            newPasswordField.clear();
            confirmPasswordField.clear();
            passwordStrengthBar.setProgress(0);
            passwordStrengthLabel.setText("");

            // Clear error messages
            currentPasswordError.setText("");
            newPasswordError.setText("");
            confirmPasswordError.setText("");

            // Fade in edit pane
            FadeTransition fadeIn = new FadeTransition(Duration.millis(200), editProfilePane);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });

        fadeOut.play();
    }

    @FXML
    public void saveProfile() {
        String currentPassword = currentPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Validate current password
        if (!currentPassword.equals(user.getPassword())) {
            currentPasswordError.setText("Current password is incorrect");
            showAlert(Alert.AlertType.ERROR, "Password Error", "Current password is incorrect.");
            return;
        }

        // Validate new password if provided
        if (!newPassword.isEmpty()) {
            if (!newPassword.equals(confirmPassword)) {
                confirmPasswordError.setText("Passwords don't match");
                showAlert(Alert.AlertType.ERROR, "Password Error", "New passwords don't match.");
                return;
            }

            if (newPassword.length() < MIN_PASSWORD_LENGTH) {
                newPasswordError.setText("Password must be at least 8 characters");
                showAlert(Alert.AlertType.ERROR, "Password Error", "Password must be at least 8 characters.");
                return;
            }

            // Update password in database
            boolean passwordUpdateSuccess = Database.updateUserPassword(user.getUsername(), newPassword);
            if (passwordUpdateSuccess) {
                user.setPassword(newPassword);
            } else {
                showAlert(Alert.AlertType.ERROR, "Update Error", "Failed to update password.");
                return;
            }
        }

        // Update avatar if changed
        if (newAvatarPath != null && !newAvatarPath.equals(user.getAvatarPath())) {
            boolean avatarUpdateSuccess = Database.updateAvatar(user.getUsername(), newAvatarPath);
            if (avatarUpdateSuccess) {
                user.setAvatarPath(newAvatarPath);
            } else {
                showAlert(Alert.AlertType.ERROR, "Update Error", "Failed to update avatar.");
                return;
            }
        }

        // Apply transition effect
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), editProfilePane);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        fadeOut.setOnFinished(event -> {
            // Exit edit mode
            isEditMode = false;
            viewProfilePane.setVisible(true);
            viewProfilePane.setManaged(true);
            editProfilePane.setVisible(false);
            editProfilePane.setManaged(false);

            // Update the display
            updateUserDisplay();

            // Fade in view pane
            FadeTransition fadeIn = new FadeTransition(Duration.millis(200), viewProfilePane);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();

            // Show success message
            showSuccessNotification("Profile Updated", "Your profile has been updated successfully.");
        });

        fadeOut.play();
    }

    @FXML
    public void cancelEdit() {
        // Apply transition effect
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), editProfilePane);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        fadeOut.setOnFinished(event -> {
            // Exit edit mode without saving
            isEditMode = false;
            viewProfilePane.setVisible(true);
            viewProfilePane.setManaged(true);
            editProfilePane.setVisible(false);
            editProfilePane.setManaged(false);

            // Reset the new avatar path
            newAvatarPath = null;

            // Fade in view pane
            FadeTransition fadeIn = new FadeTransition(Duration.millis(200), viewProfilePane);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });

        fadeOut.play();
    }

    @FXML
    public void closeProfile() {
        // Apply fade out effect when closing
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), usernameLabel.getScene().getRoot());
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        fadeOut.setOnFinished(event -> {
            Stage stage = (Stage) usernameLabel.getScene().getWindow();
            stage.close();
        });

        fadeOut.play();
    }

    @FXML
    public void chooseAvatar() {
        // Open a dialog to let the user choose from predefined avatars
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/ChooseAvatarDialog.fxml"));
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/css/avatar-selector.css").toExternalForm());

            ChooseAvatarController controller = loader.getController();
            controller.setUser(user);

            Stage stage = new Stage();
            stage.setTitle("Choose Avatar");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);

            // When the dialog is closed, check if avatar was updated
            stage.setOnHidden(e -> {
                if (isEditMode) {
                    // We're in edit mode, so update the edit avatar
                    if (user.getAvatarPath() != null) {
                        newAvatarPath = user.getAvatarPath();
                        loadAvatar(editUserAvatar, newAvatarPath);
                    }
                } else {
                    // We're in view mode, refresh the display
                    updateUserDisplay();
                }
            });

            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not open avatar selection dialog.");
        }
    }

    @FXML
    public void uploadAvatar() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Upload Avatar");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        File file = fileChooser.showOpenDialog(usernameLabel.getScene().getWindow());
        if (file != null) {
            try {
                // Create directory if it doesn't exist
                Path uploadDir = Paths.get("uploads", "avatars");
                if (!Files.exists(uploadDir)) {
                    Files.createDirectories(uploadDir);
                }

                // Generate unique filename
                String filename = "avatar_" + user.getUsername() + "_" + System.currentTimeMillis() +
                        file.getName().substring(file.getName().lastIndexOf('.'));

                // Copy file to uploads directory
                Path destination = uploadDir.resolve(filename);
                Files.copy(file.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);

                // Set the new avatar path
                String avatarPath = "file:/" + destination.toString().replace('\\', '/');

                if (isEditMode) {
                    // Just update the preview in edit mode
                    newAvatarPath = avatarPath;
                    editUserAvatar.setImage(new Image(avatarPath));
                } else {
                    // In view mode, update immediately
                    boolean success = Database.updateAvatar(user.getUsername(), avatarPath);
                    if (success) {
                        user.setAvatarPath(avatarPath);
                        updateUserDisplay();
                        showSuccessNotification("Avatar Updated", "Your avatar has been updated successfully.");
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Upload Error", "Failed to update avatar in the database.");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Upload Error", "Failed to upload avatar: " + e.getMessage());
            }
        }
    }

    private void updatePasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            passwordStrengthLabel.setText("");
            passwordStrengthBar.setProgress(0);
            return;
        }

        int strength = 0;
        double progressValue = 0;

        // Length check (scale from 0 to 0.2 based on length up to 12 chars)
        progressValue += Math.min(password.length() / 60.0, 0.2);
        if (password.length() >= MIN_PASSWORD_LENGTH) strength++;

        // Character type checks
        if (password.matches(".*[A-Z].*")) {
            strength++;
            progressValue += 0.2;
        }

        if (password.matches(".*[a-z].*")) {
            strength++;
            progressValue += 0.2;
        }

        if (password.matches(".*[0-9].*")) {
            strength++;
            progressValue += 0.2;
        }

        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
            strength++;
            progressValue += 0.2;
        }

        // Update UI
        passwordStrengthBar.setProgress(progressValue);

        switch (strength) {
            case 0, 1 -> {
                passwordStrengthLabel.setText("Weak");
                passwordStrengthLabel.getStyleClass().setAll("password-strength", "strength-weak");
                passwordStrengthBar.getStyleClass().setAll("progress-bar", "strength-bar-weak");
            }
            case 2, 3 -> {
                passwordStrengthLabel.setText("Medium");
                passwordStrengthLabel.getStyleClass().setAll("password-strength", "strength-medium");
                passwordStrengthBar.getStyleClass().setAll("progress-bar", "strength-bar-medium");
            }
            case 4, 5 -> {
                passwordStrengthLabel.setText("Strong");
                passwordStrengthLabel.getStyleClass().setAll("password-strength", "strength-strong");
                passwordStrengthBar.getStyleClass().setAll("progress-bar", "strength-bar-strong");
            }
        }
    }

    private boolean validateNewPassword() {
        String password = newPasswordField.getText();

        if (password.isEmpty()) {
            newPasswordError.setText("");
            return true;
        }

        if (password.length() < MIN_PASSWORD_LENGTH) {
            newPasswordError.setText("Password must be at least " + MIN_PASSWORD_LENGTH + " characters");
            return false;
        }

        newPasswordError.setText("");
        return true;
    }

    private boolean validateConfirmPassword() {
        String password = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (confirmPassword.isEmpty()) {
            confirmPasswordError.setText("");
            return true;
        }

        if (!password.equals(confirmPassword)) {
            confirmPasswordError.setText("Passwords don't match");
            return false;
        }

        confirmPasswordError.setText("");
        return true;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccessNotification(String title, String message) {
        // Create a custom notification that fades in and out
        StackPane notificationPane = new StackPane();
        notificationPane.setStyle("-fx-background-color: #43a047; -fx-background-radius: 5px; -fx-padding: 15px;");
        notificationPane.setMaxWidth(300);
        notificationPane.setMaxHeight(100);

        VBox content = new VBox(5);
        content.setAlignment(Pos.CENTER);

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px;");

        Label messageLabel = new Label(message);
        messageLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
        messageLabel.setWrapText(true);

        content.getChildren().addAll(titleLabel, messageLabel);
        notificationPane.getChildren().add(content);

        // Apply drop shadow
        notificationPane.setEffect(new DropShadow(10, Color.rgb(0, 0, 0, 0.5)));

        // Add to scene but initially invisible
        Scene scene = usernameLabel.getScene();
        StackPane rootPane = new StackPane();
        rootPane.getChildren().add(notificationPane);
        rootPane.setAlignment(Pos.TOP_RIGHT);
        rootPane.setPrefSize(scene.getWidth(), scene.getHeight());
        rootPane.setPickOnBounds(false);

        // Position off screen initially
        notificationPane.setTranslateX(scene.getWidth());
        notificationPane.setOpacity(0);

        // Add to scene
        ((StackPane)scene.getRoot()).getChildren().add(rootPane);

        // Slide in animation
        TranslateTransition slideIn = new TranslateTransition(Duration.millis(500), notificationPane);
        slideIn.setToX(-20);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(500), notificationPane);
        fadeIn.setToValue(1.0);

        // Slide out and fade out animation (after delay)
        TranslateTransition slideOut = new TranslateTransition(Duration.millis(500), notificationPane);
        slideOut.setDelay(Duration.seconds(3));
        slideOut.setToX(scene.getWidth());

        FadeTransition fadeOut = new FadeTransition(Duration.millis(500), notificationPane);
        fadeOut.setDelay(Duration.seconds(3));
        fadeOut.setToValue(0.0);

        // Remove from scene after animation completes
        fadeOut.setOnFinished(e -> ((StackPane)scene.getRoot()).getChildren().remove(rootPane));

        // Play animations
        slideIn.play();
        fadeIn.play();
        slideOut.play();
        fadeOut.play();
    }
}