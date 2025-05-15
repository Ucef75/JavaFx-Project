package application;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import java.util.Locale;
import java.util.ResourceBundle;
import model.User;
import utils.Database;
import globalFunc.Sound_Func;

public class SettingsController {
    @FXML
    private ComboBox<String> themeComboBox;
    @FXML
    private CheckBox musicCheckBox;
    @FXML
    private Slider volumeSlider;
    @FXML
    private ComboBox<String> languageComboBox;
    @FXML
    private Button saveButton;
    @FXML
    private Button cancelButton;
    @FXML
    private Label volumeLabel;
    @FXML
    private Label themeLabel;
    @FXML
    private Label languageLabel;
    @FXML
    private Label musicLabel;
    @FXML
    private Label settingsTitle;

    private User user;
    private ResourceBundle resources;
    private String currentLanguage = "English";
    private boolean musicWasEnabled = true;
    private double previousVolume = 0.5;

    @FXML
    public void initialize() {
        // Initialize theme options
        themeComboBox.getItems().addAll("Default", "Dark", "Light", "High Contrast");
        themeComboBox.setValue("Default");

        // Initialize volume slider with current volume from Sound_Func
        volumeSlider = new Slider(0, 1, Sound_Func.getVolume());
        volumeSlider.setShowTickMarks(true);
        volumeSlider.setShowTickLabels(true);
        volumeSlider.setMajorTickUnit(0.25);
        volumeSlider.setBlockIncrement(0.1);

        // Store initial state to allow canceling changes
        previousVolume = Sound_Func.getVolume();

        // Initialize music checkbox (we'll assume music is enabled by default)
        // We don't have a direct way to check if music is enabled in the existing Sound_Func
        musicCheckBox.setSelected(true);
        musicWasEnabled = true;

        // Add volume slider change listener
        volumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            Sound_Func.setVolume(newValue.doubleValue());
        });

        // Initialize language options
        languageComboBox.getItems().addAll("English", "French", "Spanish", "German");
        languageComboBox.setValue("English");

        // Add listener for music checkbox
        musicCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                // If music was checked, start background music
                Sound_Func.playBackgroundSong();
            } else {
                // If music was unchecked, stop background music
                Sound_Func.stopBackgroundMusic();
            }
        });

        // Add listener for language changes
        languageComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.equals(currentLanguage)) {
                currentLanguage = newValue;
                updateLanguage(newValue);
            }
        });
    }

    /**
     * Updates the UI language based on the selected language
     * @param language The language to set
     */
    private void updateLanguage(String language) {
        // Set locale based on selected language
        Locale locale;
        switch (language) {
            case "French":
                locale = Locale.FRENCH;
                break;
            case "Spanish":
                locale = new Locale("es", "ES");
                break;
            case "German":
                locale = Locale.GERMAN;
                break;
            default:
                locale = Locale.ENGLISH;
                break;
        }

        try {
            // Load resource bundle for selected language
            resources = ResourceBundle.getBundle("application.resources.strings", locale);

            // Update UI text
            settingsTitle.setText(resources.getString("settings.title"));
            themeLabel.setText(resources.getString("settings.theme"));
            musicLabel.setText(resources.getString("settings.music"));
            volumeLabel.setText(resources.getString("settings.volume"));
            languageLabel.setText(resources.getString("settings.language"));
            saveButton.setText(resources.getString("settings.save"));
            cancelButton.setText(resources.getString("settings.cancel"));

        } catch (Exception e) {
            System.err.println("Error loading language resources: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void setUser(User user) {
        this.user = user;
        System.out.println("User data received: " + user.getUsername());

        // Load user settings from database and apply them
        loadUserSettings();
    }

    /**
     * Loads user settings from the database
     */
    private void loadUserSettings() {
        if (user == null) return;

        try {
            // Here you would query your database for user settings
            // This is just a placeholder - implement with your actual database logic
            // For example:
            // Map<String, Object> settings = Database.loadUserSettings(user.getUserId());

            // For now, we'll use dummy values
            String theme = "Default"; // From database
            boolean musicEnabled = true; // From database
            double volume = Sound_Func.getVolume(); // Use current volume as default
            String language = "English"; // From database

            // Apply settings
            themeComboBox.setValue(theme);
            musicCheckBox.setSelected(musicEnabled);
            volumeSlider.setValue(volume);
            languageComboBox.setValue(language);

            // Store initial state to allow canceling changes
            musicWasEnabled = musicEnabled;
            previousVolume = volume;

            // Apply settings to the system
            if (musicEnabled) {
                Sound_Func.playBackgroundSong();
            } else {
                Sound_Func.stopBackgroundMusic();
            }
            Sound_Func.setVolume(volume);
            updateLanguage(language);

        } catch (Exception e) {
            System.err.println("Error loading user settings: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load user settings.");
        }
    }

    @FXML
    private void saveChanges(ActionEvent event) {
        // Get selected values
        String selectedTheme = themeComboBox.getValue();
        boolean musicEnabled = musicCheckBox.isSelected();
        double volumeLevel = volumeSlider.getValue();
        String selectedLanguage = languageComboBox.getValue();

        // Save settings
        System.out.println("Settings saved:");
        System.out.println("Theme: " + selectedTheme);
        System.out.println("Music enabled: " + musicEnabled);
        System.out.println("Volume: " + volumeLevel);
        System.out.println("Language: " + selectedLanguage);

        // Apply settings
        if (musicEnabled) {
            Sound_Func.playBackgroundSong();
        } else {
            Sound_Func.stopBackgroundMusic();
        }
        Sound_Func.setVolume(volumeLevel);
        updateLanguage(selectedLanguage);

        // Update stored values for cancel operation
        musicWasEnabled = musicEnabled;
        previousVolume = volumeLevel;

        // Save to database (you would implement your actual saving logic here)
        saveUserSettings(selectedTheme, musicEnabled, volumeLevel, selectedLanguage);

        // Close the settings window
        ((Button)event.getSource()).getScene().getWindow().hide();
    }

    /**
     * Saves user settings to the database
     */
    private void saveUserSettings(String theme, boolean musicEnabled, double volume, String language) {
        if (user == null) return;

        try {
            // Here you would update your database with user settings
            // For example:
            // Database.saveUserSettings(user.getUserId(), theme, musicEnabled, volume, language);

            showAlert(Alert.AlertType.INFORMATION, "Success", "Settings saved successfully.");

        } catch (Exception e) {
            System.err.println("Error saving user settings: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to save user settings.");
        }
    }

    @FXML
    private void cancel(ActionEvent event) {
        // Revert any temporary changes
        Sound_Func.setVolume(previousVolume);
        if (musicWasEnabled) {
            Sound_Func.playBackgroundSong();
        } else {
            Sound_Func.stopBackgroundMusic();
        }

        // Close the settings window without saving
        System.out.println("Settings changes cancelled");
        ((Button)event.getSource()).getScene().getWindow().hide();
    }

    /**
     * Shows an alert dialog
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.showAndWait();
    }
}