package application;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import utils.Database;

public class RegisterController {

    @FXML private ResourceBundle resources;
    @FXML private URL location;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private ComboBox<String> countryComboBox;
    @FXML private DatePicker birthDatePicker;
    @FXML private Button registerButton;
    @FXML private Button loginButton;
    @FXML private Label passwordStrengthLabel;

    @FXML
    void initialize() {
        // Initialize database
        Database.initializeDB();
        
        // Set up country combo box
        initializeCountryComboBox();
        
        // Password strength indicator
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            updatePasswordStrength(newValue);
        });
        
        // Register button action
        registerButton.setOnAction(this::handleRegister);
        
        // Login button action
        loginButton.setOnAction(this::navigateToLogin);
    }
    
    private void initializeCountryComboBox() {
        // Clear any existing items first
        countryComboBox.getItems().clear();
        
        // Add countries to the combo box
        countryComboBox.getItems().addAll(
            "United States", "Canada", "United Kingdom", "France", "Germany",
            "Japan", "Australia", "Brazil", "India", "China", "Tunisia", 
            "Egypt", "Morocco", "Algeria", "South Africa", "Iraq", 
            "Russia", "Spain", "S.Korea", "Turkey", "Italy"
        );
        
        // Set prompt text to guide the user
        countryComboBox.setPromptText("Select a country");
        
        // Ensure the combo box is visible and enabled
        countryComboBox.setVisible(true);
        countryComboBox.setDisable(false);
        
        // Set the combo box to not editable
        countryComboBox.setEditable(false);
    }
    
    private void updatePasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            passwordStrengthLabel.setText("");
            return;
        }
        
        int strength = 0;
        if (password.length() >= 8) strength++;
        if (password.matches(".*[A-Z].*")) strength++;
        if (password.matches(".*[a-z].*")) strength++;
        if (password.matches(".*[0-9].*")) strength++;
        if (password.matches(".*[!@#$%^&*()].*")) strength++;
        
        switch (strength) {
            case 0, 1 -> {
                passwordStrengthLabel.setText("Weak");
                passwordStrengthLabel.setStyle("-fx-text-fill: red;");
            }
            case 2, 3 -> {
                passwordStrengthLabel.setText("Medium");
                passwordStrengthLabel.setStyle("-fx-text-fill: orange;");
            }
            case 4, 5 -> {
                passwordStrengthLabel.setText("Strong");
                passwordStrengthLabel.setStyle("-fx-text-fill: green;");
            }
        }
    }
    
    private void handleRegister(ActionEvent event) {
        try {
            String username = usernameField.getText().trim();
            String password = passwordField.getText();
            String confirmPassword = confirmPasswordField.getText();
            String country = countryComboBox.getValue();
            String birthDate = birthDatePicker.getValue() != null ? 
                birthDatePicker.getValue().format(DateTimeFormatter.ISO_DATE) : null;
            
            // Debug output
            System.out.println("Register button clicked!");
            System.out.println("Username: " + username);
            System.out.println("Password length: " + (password != null ? password.length() : 0));
            System.out.println("Country: " + country);
            System.out.println("Birth date: " + birthDate);
            
            // Validate inputs
            if (username == null || username.isEmpty()) {
                showAlert("Error", "Username is required!");
                return;
            }
            
            if (password == null || password.isEmpty()) {
                showAlert("Error", "Password is required!");
                return;
            }
            
            if (!password.equals(confirmPassword)) {
                showAlert("Error", "Passwords do not match!");
                return;
            }
            
            if (country == null || country.isEmpty()) {
                showAlert("Error", "Please select your country");
                return;
            }
            
            if (birthDate == null) {
                showAlert("Error", "Please select your birth date");
                return;
            }
            
            // Register user
            boolean success = Database.registerUser(username, password, country, birthDate);
            System.out.println("Registration success: " + success); // Debug statement
            
            if (success) {
                showAlert("Success", "Registration successful! You can now login.");
                navigateToLogin(event);
            } else {
                showAlert("Error", "Registration failed. Username may already exist.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "An error occurred during registration: " + e.getMessage());
        }
    }
    
    private void navigateToLogin(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/loginview.fxml"));
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Login to Pixely");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Could not load login page: " + e.getMessage());
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