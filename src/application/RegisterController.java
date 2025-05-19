package application;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;
import utils.Database;

import java.net.URL;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

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
    @FXML private ProgressBar passwordStrengthBar;
    @FXML private Label usernameErrorLabel;
    @FXML private Label passwordErrorLabel;
    @FXML private Label confirmPasswordErrorLabel;
    @FXML private Label countryErrorLabel;
    @FXML private Label birthdateErrorLabel;

    private static final int MIN_AGE = 13; // Minimum age requirement
    private static final int MIN_USERNAME_LENGTH = 3;
    private static final int MIN_PASSWORD_LENGTH = 8;

    // Username validation pattern (alphanumeric with underscore allowed)
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{" + MIN_USERNAME_LENGTH + ",20}$");

    @FXML
    void initialize() {
        // Initialize database
        Database.initializeDB();

        // Set up country combo box with search functionality
        initializeCountryComboBox();

        // Set up date picker with age validation
        setupDatePicker();

        // Setup real-time validation
        setupFieldValidation();

        // Password strength indicator
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            updatePasswordStrength(newValue);
        });

        // Register button action
        registerButton.setOnAction(this::handleRegister);

        // Login button action
        loginButton.setOnAction(this::navigateToLogin);

        // Set up Enter key handler using Platform.runLater to wait until Scene is available
        javafx.application.Platform.runLater(() -> {
            if (registerButton.getScene() != null) {
                registerButton.getScene().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                    if (event.getCode() == KeyCode.ENTER) {
                        handleRegister(new ActionEvent());
                    }
                });
            }
        });

        // Clear error labels initially
        clearErrorLabels();

        // Set default focus on username field
        Platform.runLater(() -> usernameField.requestFocus());
    }

    private void clearErrorLabels() {
        usernameErrorLabel.setText("");
        passwordErrorLabel.setText("");
        confirmPasswordErrorLabel.setText("");
        countryErrorLabel.setText("");
        birthdateErrorLabel.setText("");
    }

    private void setupFieldValidation() {
        // Username field validation
        usernameField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) { // When focus lost
                validateUsername();
            } else {
                usernameErrorLabel.setText("");
            }
        });

        // Password field validation
        passwordField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) { // When focus lost
                validatePassword();
            } else {
                passwordErrorLabel.setText("");
            }
        });

        // Confirm Password field validation
        confirmPasswordField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) { // When focus lost
                validateConfirmPassword();
            } else {
                confirmPasswordErrorLabel.setText("");
            }
        });

        // Country validation
        countryComboBox.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) { // When focus lost
                validateCountry();
            } else {
                countryErrorLabel.setText("");
            }
        });
    }

    private boolean validateUsername() {
        String username = usernameField.getText().trim();

        if (username.isEmpty()) {
            usernameErrorLabel.setText("Username is required");
            return false;
        } else if (!USERNAME_PATTERN.matcher(username).matches()) {
            usernameErrorLabel.setText("Username must be " + MIN_USERNAME_LENGTH + "-20 characters, alphanumeric with _ allowed");
            return false;
        } else {
            usernameErrorLabel.setText("");
            return true;
        }
    }

    private boolean validatePassword() {
        String password = passwordField.getText();

        if (password.isEmpty()) {
            passwordErrorLabel.setText("Password is required");
            return false;
        } else if (password.length() < MIN_PASSWORD_LENGTH) {
            passwordErrorLabel.setText("Password must be at least " + MIN_PASSWORD_LENGTH + " characters");
            return false;
        } else {
            passwordErrorLabel.setText("");
            return true;
        }
    }

    private boolean validateConfirmPassword() {
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (confirmPassword.isEmpty()) {
            confirmPasswordErrorLabel.setText("Please confirm your password");
            return false;
        } else if (!password.equals(confirmPassword)) {
            confirmPasswordErrorLabel.setText("Passwords do not match");
            return false;
        } else {
            confirmPasswordErrorLabel.setText("");
            return true;
        }
    }

    private boolean validateCountry() {
        String country = countryComboBox.getValue();

        if (country == null || country.isEmpty()) {
            countryErrorLabel.setText("Please select your country");
            return false;
        } else {
            countryErrorLabel.setText("");
            return true;
        }
    }

    private boolean validateBirthDate() {
        LocalDate birthDate = birthDatePicker.getValue();

        if (birthDate == null) {
            birthdateErrorLabel.setText("Please select your birth date");
            return false;
        } else if (Period.between(birthDate, LocalDate.now()).getYears() < MIN_AGE) {
            birthdateErrorLabel.setText("You must be at least " + MIN_AGE + " years old to register");
            return false;
        } else {
            birthdateErrorLabel.setText("");
            return true;
        }
    }

    private void setupDatePicker() {
        // Set max date to today
        birthDatePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);

                LocalDate today = LocalDate.now();
                setDisable(empty || date.isAfter(today));

                // Visually mark dates that would make the user under MIN_AGE
                if (date != null && !empty) {
                    LocalDate minAgeDate = today.minusYears(MIN_AGE);
                    if (date.isAfter(minAgeDate)) {
                        setStyle("-fx-background-color: #FFC0C0;"); // Light red
                    }
                }
            }
        });

        // Add validation when date changes
        birthDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            validateBirthDate();
        });
    }

    private void initializeCountryComboBox() {
        // Clear any existing items first
        countryComboBox.getItems().clear();

        // Sort countries alphabetically for better UX
        String[] countries = {
                "Afghanistan", "Albania", "Algeria", "Andorra", "Angola",
                "Argentina", "Armenia", "Australia", "Austria", "Azerbaijan",
                "Bahamas", "Bahrain", "Bangladesh", "Barbados", "Belarus", "Belgium",
                "Belize", "Benin", "Bhutan", "Bolivia", "Bosnia and Herzegovina",
                "Botswana", "Brazil", "Brunei", "Bulgaria", "Burkina Faso", "Burundi",
                "Cambodia", "Cameroon", "Canada", "Cape Verde", "Central African Republic",
                "Chad", "Chile", "China", "Colombia", "Comoros", "Congo", "Costa Rica",
                "Croatia", "Cuba", "Cyprus", "Czech Republic", "Denmark", "Djibouti", "Dominica",
                "Dominican Republic", "East Timor", "Ecuador", "Egypt", "El Salvador",
                "Equatorial Guinea", "Eritrea", "Estonia", "Ethiopia", "Fiji", "Finland",
                "France", "Gabon", "Gambia", "Georgia", "Germany", "Ghana", "Greece",
                "Grenada", "Guatemala", "Guinea", "Guinea-Bissau", "Guyana", "Haiti",
                "Honduras", "Hungary", "Iceland", "India", "Indonesia", "Iran", "Iraq",
                "Ireland", "Israel", "Italy", "Ivory Coast", "Jamaica", "Japan", "Jordan",
                "Kazakhstan", "Kenya", "Kiribati", "North Korea", "South Korea", "Kuwait",
                "Kyrgyzstan", "Laos", "Latvia", "Lebanon", "Lesotho", "Liberia", "Libya",
                "Liechtenstein", "Lithuania", "Luxembourg", "Macedonia", "Madagascar",
                "Malawi", "Malaysia", "Maldives", "Mali", "Malta", "Marshall Islands",
                "Mauritania", "Mauritius", "Mexico", "Micronesia", "Moldova", "Monaco",
                "Mongolia", "Montenegro", "Morocco", "Mozambique", "Myanmar", "Namibia",
                "Nauru", "Nepal", "Netherlands", "New Zealand", "Nicaragua", "Niger",
                "Nigeria", "Norway", "Oman", "Pakistan", "Palau", "Panama", "Papua New Guinea",
                "Paraguay", "Peru", "Philippines", "Poland", "Portugal", "Qatar", "Romania",
                "Russia", "Rwanda", "Saint Kitts and Nevis", "Saint Lucia", "Saint Vincent and the Grenadines",
                "Samoa", "San Marino", "Sao Tome and Principe", "Saudi Arabia", "Senegal",
                "Serbia", "Seychelles", "Sierra Leone", "Singapore", "Slovakia", "Slovenia",
                "Solomon Islands", "Somalia", "South Africa", "Spain", "Sri Lanka", "Sudan",
                "Suriname", "Swaziland", "Sweden", "Switzerland", "Syria", "Taiwan", "Tajikistan",
                "Tanzania", "Thailand", "Togo", "Tonga", "Trinidad and Tobago", "Tunisia",
                "Turkey", "Turkmenistan", "Tuvalu", "Uganda", "Ukraine", "United Arab Emirates",
                "United Kingdom", "United States", "Uruguay", "Uzbekistan", "Vanuatu", "Vatican City",
                "Venezuela", "Vietnam", "Yemen", "Zambia", "Zimbabwe"
        };

        countryComboBox.getItems().addAll(countries);

        // Set prompt text to guide the user
        countryComboBox.setPromptText("Select a country");

        // Enable search by typing
        countryComboBox.setEditable(true);
        countryComboBox.getEditor().textProperty().addListener((obs, oldText, newText) -> {
            // Filter countries based on input
            if (newText != null && !newText.isEmpty()) {
                String input = newText.toLowerCase();
                countryComboBox.show();
                countryComboBox.getItems().setAll(countries);
                countryComboBox.getItems().removeIf(country ->
                        !country.toLowerCase().contains(input));
            } else {
                countryComboBox.getItems().setAll(countries);
            }
        });
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

    private void handleRegister(ActionEvent event) {
        try {
            // Clear previous error messages
            clearErrorLabels();

            // Validate all fields
            boolean isUsernameValid = validateUsername();
            boolean isPasswordValid = validatePassword();
            boolean isConfirmPasswordValid = validateConfirmPassword();
            boolean isCountryValid = validateCountry();
            boolean isBirthDateValid = validateBirthDate();

            // Check if all validations passed
            if (isUsernameValid && isPasswordValid && isConfirmPasswordValid &&
                    isCountryValid && isBirthDateValid) {

                String username = usernameField.getText().trim();
                String password = passwordField.getText();
                String country = countryComboBox.getValue();
                String birthDate = birthDatePicker.getValue().format(DateTimeFormatter.ISO_DATE);

                // Register user
                boolean success = Database.registerUser(username, password, country, birthDate);

                if (success) {
                    // Show success notification
                    showNotification(
                            "Registration Successful",
                            "Welcome to Pixely, " + username + "!",
                            Notifications.Type.SUCCESS
                    );

                    // Navigate to login
                    navigateToLogin(event);
                } else {
                    // Username likely already exists
                    usernameErrorLabel.setText("This username is already taken");
                    usernameField.requestFocus();
                }
            } else {
                // Focus on the first field with error
                if (!isUsernameValid) {
                    usernameField.requestFocus();
                } else if (!isPasswordValid) {
                    passwordField.requestFocus();
                } else if (!isConfirmPasswordValid) {
                    confirmPasswordField.requestFocus();
                } else if (!isCountryValid) {
                    countryComboBox.requestFocus();
                } else if (!isBirthDateValid) {
                    birthDatePicker.requestFocus();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "An error occurred during registration: " + e.getMessage());
        }
    }

    private void showNotification(String title, String message, Notifications.Type type) {
        Notifications notification = Notifications.create()
                .title(title)
                .text(message)
                .graphic(null)
                .hideAfter(Duration.seconds(5))
                .position(Notifications.Position.TOP_RIGHT);

        switch (type) {
            case SUCCESS -> notification.showInformation();
            case ERROR -> notification.showError();
            case INFO -> notification.showInformation();
            case WARNING -> notification.showWarning();
        }
    }

    @FXML
    public void navigateToLogin(ActionEvent event) {
        try {
            // Load the login view
            Parent root = FXMLLoader.load(getClass().getResource("/view/loginview.fxml"));
            Scene scene = new Scene(root);
            Stage stage = (Stage) loginButton.getScene().getWindow();

            // Set the root node's opacity to 0 initially
            root.setOpacity(0.0);

            // Set the new scene
            stage.setScene(scene);
            stage.setTitle("Login to Pixely");

            // Create and play the fade-in animation
            javafx.animation.FadeTransition fadeIn = new javafx.animation.FadeTransition(Duration.millis(300), root);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
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

    // Inner enum for notification types
    // Inner enum for notification types
    private static class Notifications {
        enum Type {
            SUCCESS, ERROR, INFO, WARNING
        }

        enum Position {
            TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT, CENTER
        }

        static Notifications create() {
            return new Notifications();
        }

        Notifications title(String title) {
            this.title = title;
            return this;
        }

        Notifications text(String text) {
            this.text = text;
            return this;
        }

        Notifications graphic(Object graphic) {
            return this;
        }

        Notifications hideAfter(Duration duration) {
            return this;
        }

        Notifications position(Position position) {
            return this;
        }

        void showInformation() {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(text);
            alert.show();
        }

        void showError() {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(text);
            alert.show();
        }

        void showWarning() {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(text);
            alert.show();
        }

        private String title;
        private String text;
    }
}