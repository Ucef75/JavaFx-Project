package application;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.application.Platform;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import model.Game;
import model.User;
import utils.Database;
import utils.GameLibrary;
import globalFunc.Sound_Func;

public class PixelyController {

    @FXML
    private GridPane gamesGrid;
    @FXML
    private Button usersDirectoryButton;
    @FXML
    private Button settingsButton;
    @FXML
    private Button userButton;
    @FXML
    private Button logoutButton;
    @FXML
    private Button exitButton;
    @FXML
    private Label welcomeLabel;

    private List<Game> games = new ArrayList<>();
    private User currentUser;
    private String loggedInUsername;

    public void setLoggedInUsername(String username) {
        this.loggedInUsername = username;
        loadCurrentUser();
        updateWelcomeMessage();
    }

    @FXML
    public void initialize() {
        setupButtonActions();
        loadGames();
        displayGames();

        // Start background music
        Sound_Func.playBackgroundSong();

        // Listen for theme/language changes
        SettingsManager.getInstance().themeProperty().addListener((obs, oldVal, newVal) -> {
            Platform.runLater(() -> ThemeUtil.applyTheme(usersDirectoryButton.getScene()));
        });
        SettingsManager.getInstance().localeProperty().addListener((obs, oldVal, newVal) -> {
            Platform.runLater(this::updateTextsFromLocale);
        });

        // Apply current theme and language
        Platform.runLater(() -> {
            if (usersDirectoryButton.getScene() != null) {
                ThemeUtil.applyTheme(usersDirectoryButton.getScene());
                updateTextsFromLocale();
            }
        });
    }

    private void updateTextsFromLocale() {
        ResourceBundle bundle = ResourceBundle.getBundle("lang.strings", SettingsManager.getInstance().getLocale());
        // Update UI texts here using bundle
        if (welcomeLabel != null && currentUser != null) {
            welcomeLabel.setText(bundle.getString("main.welcome") + ", " + currentUser.getUsername() + "!");
        }
        usersDirectoryButton.setText(bundle.getString("main.usersDirectory"));
        settingsButton.setText(bundle.getString("main.settings"));
        userButton.setText(bundle.getString("main.profile"));
        logoutButton.setText(bundle.getString("main.logout"));
        exitButton.setText(bundle.getString("main.exit"));
    }

    private void setupButtonActions() {
        usersDirectoryButton.setOnAction(e -> showUsersDirectory());
        settingsButton.setOnAction(e -> openSettings());
        userButton.setOnAction(e -> showUserProfile());
        logoutButton.setOnAction(e -> logout());
        exitButton.setOnAction(e -> exit());
    }

    private void updateWelcomeMessage() {
        if (currentUser != null && welcomeLabel != null) {
            ResourceBundle bundle = ResourceBundle.getBundle("lang.strings", SettingsManager.getInstance().getLocale());
            welcomeLabel.setText(bundle.getString("main.welcome") + ", " + currentUser.getUsername() + "!");
        }
    }

    private void loadCurrentUser() {
        if (loggedInUsername == null || loggedInUsername.isEmpty()) {
            showError("Error", "No user logged in.");
            return;
        }

        Map<String, Object> userData = Database.loadUserByUsername(loggedInUsername);
        if (userData == null) {
            showError("Error", "Failed to load user data.");
            return;
        }

        currentUser = new User(
                (Integer) userData.get("user_id"),
                (String) userData.get("username"),
                "", // Password is not needed here
                (String) userData.get("country"),
                (String) userData.get("created_at"),
                (String) userData.get("birth_date")
        );
    }

    private void loadGames() {
        games.clear();
        games.addAll(Arrays.asList(GameLibrary.getAvailableGames()));
    }

    private void displayGames() {
        gamesGrid.getChildren().clear();
        int col = 0;
        int row = 0;

        for (Game game : games) {
            VBox gameBox = createGameBox(game);
            gamesGrid.add(gameBox, col, row);

            col++;
            if (col == 4) {
                col = 0;
                row++;
            }
        }
    }

    private VBox createGameBox(Game game) {
        VBox gameBox = new VBox(10);
        gameBox.getStyleClass().add("game-box");
        gameBox.setStyle("-fx-border-color: #3498DB; -fx-border-width: 2; -fx-padding: 10; -fx-background-color: #ECF0F1;");

        ImageView imageView = new ImageView();
        imageView.setFitWidth(150);
        imageView.setFitHeight(150);
        imageView.setPreserveRatio(true);

        try {
            String imagePath = game.getImagePath();
            if (imagePath != null && !imagePath.isEmpty()) {
                InputStream imageStream = getClass().getResourceAsStream(imagePath);
                if (imageStream != null) {
                    imageView.setImage(new Image(imageStream));
                } else {
                    String fallbackPath = "/pictures/" + game.getName().replace(" ", "_") + "_game.jpg";
                    InputStream fallbackStream = getClass().getResourceAsStream(fallbackPath);
                    if (fallbackStream != null) {
                        imageView.setImage(new Image(fallbackStream));
                    } else {
                        fallbackPath = "/pictures/" + game.getName().replace(" ", "_") + "_game.png";
                        fallbackStream = getClass().getResourceAsStream(fallbackPath);
                        if (fallbackStream != null) {
                            imageView.setImage(new Image(fallbackStream));
                        } else {
                            System.err.println("Game-specific image not found for: " + game.getName());
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading image for " + game.getName() + ": " + e.getMessage());
        }

        imageView.setFitWidth(120);
        imageView.setFitHeight(120);
        imageView.setPreserveRatio(true);

        Label nameLabel = new Label(game.getName());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");

        Label descLabel = new Label(game.getDescription());
        descLabel.setWrapText(true);
        descLabel.setMaxWidth(200);

        Button playButton = new Button("Play");
        playButton.setStyle("-fx-background-color: #2ECC71; -fx-text-fill: white;");
        playButton.setOnAction(e -> launchGame(game));

        gameBox.getChildren().addAll(imageView, nameLabel, descLabel, playButton);
        return gameBox;
    }

    private void launchGame(Game game) {
        if (currentUser == null) {
            showError("Error", "Please log in to play games.");
            return;
        }

        try {
            Class<? extends javafx.application.Application> gameClass = game.getGameClass();
            if (gameClass != null) {
                Thread gameThread = new Thread(() -> {
                    try {
                        javafx.application.Application gameApp = gameClass.getDeclaredConstructor().newInstance();
                        Platform.runLater(() -> {
                            try {
                                Stage gameStage = new Stage();
                                gameStage.setTitle(game.getName());
                                gameApp.start(gameStage);
                                gameStage.show();
                                // Apply theme
                                ThemeUtil.applyTheme(gameStage.getScene());
                            } catch (Exception ex) {
                                Platform.runLater(() -> showError("Error", "Failed to launch game: " + ex.getMessage()));
                            }
                        });
                    } catch (Exception ex) {
                        Platform.runLater(() -> showError("Error", "Failed to initialize game: " + ex.getMessage()));
                    }
                });
                gameThread.setDaemon(true);
                gameThread.start();
            } else {
                throw new Exception("Game class not found for " + game.getName());
            }
        } catch (Exception e) {
            showError("Error", "Failed to launch game: " + e.getMessage());
        }
    }

    @FXML
    private void showUsersDirectory() {
        if (currentUser == null) {
            showError("Error", "Please log in to view users directory.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/UserDirectory.fxml"));
            loader.setResources(ResourceBundle.getBundle("lang.strings", SettingsManager.getInstance().getLocale()));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Users Directory");
            Scene scene = new Scene(root);
            ThemeUtil.applyTheme(scene);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showError("Error", "Failed to open users directory: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void openSettings() {
        if (currentUser == null) {
            showError("Error", "Please log in to access settings.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/PixelySettings.fxml"));
            loader.setResources(ResourceBundle.getBundle("lang.strings", SettingsManager.getInstance().getLocale()));
            Parent root = loader.load();
            Stage stage = new Stage();
            Scene scene = new Scene(root);
            ThemeUtil.applyTheme(scene);
            stage.setScene(scene);
            stage.setTitle("Settings");
            stage.initModality(Modality.APPLICATION_MODAL);

            SettingsController controller = loader.getController();
            controller.setUser(currentUser);
            stage.showAndWait();

            loadCurrentUser();
            updateWelcomeMessage();
        } catch (IOException e) {
            showError("Error", "Failed to open settings: " + e.getMessage());
        }
    }

    @FXML
    public void showUserProfile() {
        if (currentUser == null) {
            showError("Error", "Please log in to view profile.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/UserProfile.fxml"));
            loader.setResources(ResourceBundle.getBundle("lang.strings", SettingsManager.getInstance().getLocale()));
            Parent root = loader.load();
            Stage stage = new Stage();
            Scene scene = new Scene(root);
            ThemeUtil.applyTheme(scene);
            stage.setScene(scene);
            stage.setTitle("User Profile");
            stage.initModality(Modality.APPLICATION_MODAL);

            UserProfileController controller = loader.getController();
            controller.setUser(currentUser);
            stage.showAndWait();

            Sound_Func.playProfileOpenedSound();
        } catch (IOException e) {
            showError("Error", "Failed to open profile: " + e.getMessage());
        }
    }

    @FXML
    public void logout() {
        Sound_Func.stopBackgroundMusic();
        currentUser = null;
        try {
            Stage currentStage = (Stage) logoutButton.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/loginview.fxml"));
            loader.setResources(ResourceBundle.getBundle("lang.strings", SettingsManager.getInstance().getLocale()));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            ThemeUtil.applyTheme(scene);
            currentStage.setScene(scene);
            currentStage.setTitle("Pixely Login");
        } catch (IOException e) {
            showError("Error", "Failed to logout: " + e.getMessage());
        }
    }

    @FXML
    public void exit() {
        Sound_Func.stopBackgroundMusic();
        Platform.exit();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.showAndWait();
    }
}