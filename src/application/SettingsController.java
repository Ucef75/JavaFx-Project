package application;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import model.User;

public class SettingsController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private TextField userIdField;

    @FXML
    private TextField countryField;

    @FXML
    private DatePicker birthDatePicker;

    @FXML
    private TextField createdField;

    @FXML
    private ComboBox<String> themeComboBox;

    @FXML
    private CheckBox notificationsCheckBox;

    @FXML
    private Button saveButton;

    @FXML
    private Button cancelButton;

    private User user;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @FXML
    public void initialize() {
        // Initialize theme options
        themeComboBox.getItems().addAll("Default", "Dark", "Light", "High Contrast");
        themeComboBox.setValue("Default");

        // Set default for notifications
        notificationsCheckBox.setSelected(true);
    }

    public void setUser(User user) {
        this.user = user;

        // Populate fields with user data
        usernameField.setText(user.getUsername());
        passwordField.setText(user.getPassword());
        confirmPasswordField.setText(user.getPassword());
        userIdField.setText(String.valueOf(user.getUserId()));
        countryField.setText(user.getCountry());

        try {
            LocalDate birthDate = LocalDate.parse(user.getBirthDate(), formatter);
            birthDatePicker.setValue(birthDate);
        } catch (Exception e) {
            System.err.println("Error parsing birth date: " + e.getMessage());
        }

        createdField.setText(user.getCreated());
    }

    @FXML
    public void saveChanges() {
        // Validate input
        if (!validateInput()) {
            return;
        }

        // Update user object
        user.setUsername(usernameField.getText());
        user.setPassword(passwordField.getText());

        // In a real application, you would save changes to a database
        System.out.println("Changes saved for user: " + user.getUsername());

        // Close the window
        closeWindow();
    }

    private boolean validateInput() {
        // Check if username is empty
        if (usernameField.getText().trim().isEmpty()) {
            showAlert("Username cannot be empty");
            return false;
        }

        // Check if passwords match
        if (!passwordField.getText().equals(confirmPasswordField.getText())) {
            showAlert("Passwords do not match");
            return false;
        }

        // Check if password is too short
        if (passwordField.getText().length() < 6) {
            showAlert("Password must be at least 6 characters long");
            return false;
        }

        return true;
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Validation Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void cancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}