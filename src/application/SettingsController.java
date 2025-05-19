package application;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import model.User;
import globalFunc.Sound_Func;
import java.util.Locale;
import java.util.ResourceBundle;

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

        // Setup volume slider (do not recreate it!)
        volumeSlider.setMin(0);
        volumeSlider.setMax(1);
        volumeSlider.setValue(Sound_Func.getVolume());
        volumeSlider.setShowTickMarks(true);
        volumeSlider.setShowTickLabels(true);
        volumeSlider.setMajorTickUnit(0.25);
        volumeSlider.setBlockIncrement(0.1);

        previousVolume = Sound_Func.getVolume();

        // Initialize music checkbox
        musicCheckBox.setSelected(true);
        musicWasEnabled = true;

        volumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            Sound_Func.setVolume(newValue.doubleValue());
        });

        // Initialize language options
        languageComboBox.getItems().addAll("English", "French", "Spanish", "German");
        languageComboBox.setValue(currentLanguage);

        musicCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                Sound_Func.playBackgroundSong();
            } else {
                Sound_Func.stopBackgroundMusic();
            }
        });

        languageComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.equals(currentLanguage)) {
                currentLanguage = newValue;
                updateLanguage(newValue);
            }
        });

        // Initialize UI language
        updateLanguage(currentLanguage);
    }

    /**
     * Updates the UI language based on the selected language
     * @param language The language to set
     */
    private void updateLanguage(String language) {
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
            resources = ResourceBundle.getBundle("lang.strings", locale);

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
        loadUserSettings();
    }

    /**
     * Loads user settings from the database
     */
    private void loadUserSettings() {
        if (user == null) return;

        try {
            // Example: load actual user settings from your DB here.
            String theme = "Default";
            boolean musicEnabled = true;
            double volume = Sound_Func.getVolume();
            String language = "English";

            themeComboBox.setValue(theme);
            musicCheckBox.setSelected(musicEnabled);
            volumeSlider.setValue(volume);
            languageComboBox.setValue(language);

            musicWasEnabled = musicEnabled;
            previousVolume = volume;

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
        String selectedTheme = themeComboBox.getValue();
        boolean musicEnabled = musicCheckBox.isSelected();
        double volumeLevel = volumeSlider.getValue();
        String selectedLanguage = languageComboBox.getValue();

        if (musicEnabled) {
            Sound_Func.playBackgroundSong();
        } else {
            Sound_Func.stopBackgroundMusic();
        }
        Sound_Func.setVolume(volumeLevel);
        updateLanguage(selectedLanguage);

        musicWasEnabled = musicEnabled;
        previousVolume = volumeLevel;

        saveUserSettings(selectedTheme, musicEnabled, volumeLevel, selectedLanguage);

        ((Button)event.getSource()).getScene().getWindow().hide();
    }

    private void saveUserSettings(String theme, boolean musicEnabled, double volume, String language) {
        if (user == null) return;

        try {
            // Save user settings to database here if needed
            showAlert(Alert.AlertType.INFORMATION, "Success", "Settings saved successfully.");
        } catch (Exception e) {
            System.err.println("Error saving user settings: " + e.getMessage());
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to save user settings.");
        }
    }

    @FXML
    private void cancel(ActionEvent event) {
        Sound_Func.setVolume(previousVolume);
        if (musicWasEnabled) {
            Sound_Func.playBackgroundSong();
        } else {
            Sound_Func.stopBackgroundMusic();
        }
        ((Button)event.getSource()).getScene().getWindow().hide();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.showAndWait();
    }
}