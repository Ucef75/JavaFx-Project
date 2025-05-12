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
    private Button leaderboardButton;

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

    /**
     * Set the logged-in username (to be called from login controller)
     * @param username The username of the logged-in user
     */
    public void setLoggedInUsername(String username) {
        this.loggedInUsername = username;
        loadCurrentUser();
    }

    @FXML
    public void initialize() {
        // Note: The currentUser will be null until setLoggedInUsername is called

        // Load games from GameLibrary
        loadGames();

        // Display games in the grid
        displayGames();
    }

    private void loadCurrentUser() {
        if (loggedInUsername == null || loggedInUsername.isEmpty()) {
            showError("Error", "No user logged in. Please restart the application.");
            return;
        }

        // Load user from database using the Database utility class
        Map<String, Object> userData = Database.loadUserByUsername(loggedInUsername);

        if (userData == null) {
            showError("Error", "Failed to load user data. Please try logging in again.");
            return;
        }

        // Create User object from the map data
        currentUser = new User(
                (Integer) userData.get("user_id"),
                (String) userData.get("username"),
                "", // Password is not needed in the model
                (String) userData.get("country"),
                (String) userData.get("created_at"),
                (String) userData.get("birth_date")
        );

        // Update UI with user info if needed
        if (welcomeLabel != null) {
            welcomeLabel.setText("Welcome, " + currentUser.getUsername() + "!");
        }
    }

    /**
     * This method loads all available games from the GameLibrary
     */
    private void loadGames() {
        // Clear existing games
        games.clear();

        // Load games from GameLibrary
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

        // Image loading
        ImageView imageView = new ImageView();
        try {
            Image image = new Image(getClass().getResourceAsStream(game.getImagePath()));
            imageView.setImage(image);
        } catch (Exception e) {
            System.err.println("Error loading image for " + game.getName() + ": " + game.getImagePath());
            // Set default image if desired
            imageView.setImage(new Image(getClass().getResourceAsStream("/pictures/default_game.png")));
        }

        imageView.setFitWidth(120);
        imageView.setFitHeight(120);
        imageView.setPreserveRatio(true);

        // Game info
        Label nameLabel = new Label(game.getName());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");

        Label descLabel = new Label(game.getDescription());
        descLabel.setWrapText(true);
        descLabel.setMaxWidth(200);

        // Play button
        Button playButton = new Button("Play");
        playButton.setStyle("-fx-background-color: #2ECC71; -fx-text-fill: white;");
        playButton.setOnAction(e -> launchGame(game));

        gameBox.getChildren().addAll(imageView, nameLabel, descLabel, playButton);
        return gameBox;
    }

    private void launchGame(Game game) {
        // Check if user is logged in
        if (currentUser == null) {
            showError("Error", "Please log in to play games.");
            return;
        }

        try {
            // Launch the game using reflection
            game.launch();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error", "Failed to launch game: " + e.getMessage());
        }
    }

    /**
     * This method is unused and can be removed since we now launch games directly
     * using the launchGame method instead of showing a game detail screen.
     * If needed in the future, ensure the GameDetailController has proper setUser method.
     */
    private void openGameDetail(Game game) {
        // Check if user is loaded
        if (currentUser == null) {
            showError("Error", "Please log in to play games.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/GameDetail.fxml"));
            Scene scene = new Scene(loader.load());

            GameDetailController controller = loader.getController();
            controller.setGame(game);
            // Make sure GameDetailController has a setUser method or use a different approach
            // controller.setUser(currentUser);  // This line causes the compilation error

            // For now, store the user ID in a controller property if that's what's needed
            if (controller instanceof GameDetailController) {
                // Alternative approach - pass user ID directly if that's what the controller needs
                // controller.setUserId(currentUser.getUserId());
            }

            Stage stage = new Stage();
            stage.setTitle(game.getName());
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Error", "Failed to open game: " + e.getMessage());
        }
    }

    @FXML
    public void openSettings() {
        // Check if user is loaded
        if (currentUser == null) {
            showError("Error", "Please log in to access settings.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/PixelySettings.fxml"));
            Scene scene = new Scene(loader.load());

            SettingsController controller = loader.getController();
            controller.setUser(currentUser);

            Stage stage = new Stage();
            stage.setTitle("Settings");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // Refresh user info if changes were made
            loadCurrentUser();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Error", "Failed to open settings: " + e.getMessage());
        }
    }

    @FXML
    public void showLeaderboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/Leaderboard.fxml"));
            Scene scene = new Scene(loader.load());

            LeaderboardController controller = loader.getController();
            if (currentUser != null) {
                controller.setCurrentUser(currentUser);
            }

            Stage stage = new Stage();
            stage.setTitle("Leaderboard");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Error", "Failed to open leaderboard: " + e.getMessage());
        }
    }

    @FXML
    public void showUserProfile() {
        // Check if user is loaded
        if (currentUser == null) {
            showError("Error", "Please log in to view profile.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/UserProfile.fxml"));
            Scene scene = new Scene(loader.load());

            UserProfileController controller = loader.getController();
            controller.setUser(currentUser);

            Stage stage = new Stage();
            stage.setTitle("User Profile");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Error", "Failed to open user profile: " + e.getMessage());
        }
    }

    @FXML
    public void logout() {
        // Clear current user
        currentUser = null;

        // Get the current stage
        Stage currentStage = (Stage) logoutButton.getScene().getWindow();

        try {
            // Load login screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/loginview.fxml"));
            Scene scene = new Scene(loader.load());

            // Set the scene to the stage
            currentStage.setScene(scene);
            currentStage.setTitle("Pixely Login");
            currentStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Error", "Failed to return to login screen: " + e.getMessage());
        }
    }

    @FXML
    public void exit() {
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