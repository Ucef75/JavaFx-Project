package application;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.application.Platform;
import java.io.IOException;
import java.io.InputStream;  // Added this missing import
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import model.Game;
import model.User;
import utils.Database;
import utils.GameLibrary;

public class PixelyController {

    @FXML
    private GridPane gamesGrid;
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
        // Initialize UI elements
        setupButtonActions();
        loadGames();
        displayGames();
    }

    private void setupButtonActions() {
        settingsButton.setOnAction(e -> openSettings());
        userButton.setOnAction(e -> showUserProfile());
        logoutButton.setOnAction(e -> logout());
        exitButton.setOnAction(e -> exit());
    }

    private void updateWelcomeMessage() {
        if (currentUser != null && welcomeLabel != null) {
            welcomeLabel.setText("Welcome, " + currentUser.getUsername() + "!");
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
        try {
            // First try to load the game-specific image
            String imagePath = game.getImagePath();
            if (imagePath != null && !imagePath.isEmpty()) {
                InputStream imageStream = getClass().getResourceAsStream(imagePath);
                if (imageStream != null) {
                    imageView.setImage(new Image(imageStream));
                } else {
                    throw new IOException("Image not found at: " + imagePath);
                }
            } else {
                // Fall back to default image
                InputStream defaultStream = getClass().getResourceAsStream("/pictures/default_game.png");
                if (defaultStream != null) {
                    imageView.setImage(new Image(defaultStream));
                } else {
                    System.err.println("Default game image not found");
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading image for " + game.getName() + ": " + e.getMessage());
            try {
                // Final fallback if both images fail
                InputStream defaultStream = getClass().getResourceAsStream("/pictures/default_game.png");
                if (defaultStream != null) {
                    imageView.setImage(new Image(defaultStream));
                }
            } catch (Exception ex) {
                System.err.println("Could not load any image for game: " + game.getName());
            }
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
            // Fix for launching the game properly using JavaFX Application methods
            Class<? extends javafx.application.Application> gameClass = game.getGameClass();
            if (gameClass != null) {
                String[] args = new String[0];
                javafx.application.Application.launch(gameClass, args);
            } else {
                throw new Exception("Game class not found for " + game.getName());
            }
        } catch (Exception e) {
            showError("Error", "Failed to launch game: " + e.getMessage());
        }
    }

    @FXML
    private void openSettings() {
        if (currentUser == null) {
            showError("Error", "Please log in to access settings.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/PixelySettings.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Settings");
            stage.initModality(Modality.APPLICATION_MODAL);

            SettingsController controller = loader.getController();
            controller.setUser(currentUser);
            stage.showAndWait();

            loadCurrentUser();
            updateWelcomeMessage();
        } catch (IOException e) {
            showError("Error", "Failed to open settings.");
        }
    }

    @FXML
    private void showUserProfile() {
        if (currentUser == null) {
            showError("Error", "Please log in to view profile.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/UserProfile.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("User Profile");
            stage.initModality(Modality.APPLICATION_MODAL);

            UserProfileController controller = loader.getController();
            controller.setUser(currentUser);
            stage.showAndWait();
        } catch (IOException e) {
            showError("Error", "Failed to open profile.");
        }
    }

    @FXML
    private void logout() {
        currentUser = null;
        try {
            Stage currentStage = (Stage) logoutButton.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/loginview.fxml"));
            currentStage.setScene(new Scene(loader.load()));
            currentStage.setTitle("Pixely Login");
        } catch (IOException e) {
            showError("Error", "Failed to logout.");
        }
    }

    @FXML
    private void exit() {
        Platform.exit();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}