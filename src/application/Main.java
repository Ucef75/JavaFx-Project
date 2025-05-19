package application;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.util.Random;

public class Main extends Application {
    private static final int SPLASH_WIDTH = 800;
    private static final int SPLASH_HEIGHT = 600;
    private static final String[] LOADING_MESSAGES = {
            "Loading awesome games...",
            "Preparing your adventure...",
            "Initializing game engines...",
            "Charging up controllers...",
            "Summoning digital heroes...",
            "Assembling pixel armies...",
            "Generating random worlds...",
            "Brewing potions of fun...",
            "Sharpening digital swords...",
            "Enabling cheat codes...",
            "Calibrating difficulty...",
            "Polishing high scores..."
    };

    @Override
    public void start(Stage primaryStage) {
        try {
            // First show the splash screen
            showSplashScreen(primaryStage);
        } catch (Exception e) {
            e.printStackTrace();
            // If splash fails, try to launch main interface directly
            loadMainInterface(primaryStage);
        }
    }

    private void showSplashScreen(Stage primaryStage) {
        try {
            // Create splash components
            StackPane splashLayout = new StackPane();
            splashLayout.setStyle("-fx-background-color: linear-gradient(to bottom, #1e3c72, #2a5298);");

            // Logo
            ImageView logo = createLogo();

            // Glowing title
            Text title = new Text("PIXELY");
            title.setFont(Font.font("Impact", FontWeight.BOLD, 80));
            title.setFill(Color.WHITE);

            // Add glow effect
            Glow glow = new Glow();
            glow.setLevel(0.8);
            title.setEffect(glow);

            // Add drop shadow for better visibility
            DropShadow dropShadow = new DropShadow();
            dropShadow.setColor(Color.DEEPSKYBLUE);
            dropShadow.setRadius(20);
            title.setEffect(dropShadow);

            // Tagline
            Label tagline = new Label("Your Gateway to Gaming Adventures");
            tagline.setFont(Font.font("Verdana", FontWeight.LIGHT, 20));
            tagline.setTextFill(Color.LIGHTBLUE);

            // Loading message
            Label loadingLabel = new Label(getRandomLoadingMessage());
            loadingLabel.setFont(Font.font("Verdana", 16));
            loadingLabel.setTextFill(Color.WHITE);

            // Progress bar
            ProgressBar progressBar = new ProgressBar(0);
            progressBar.setPrefWidth(400);
            progressBar.setStyle("-fx-accent: #2ecc71;");

            // VBox for organizing elements
            VBox vbox = new VBox(20);
            vbox.setAlignment(Pos.CENTER);
            vbox.getChildren().addAll(logo, title, tagline, loadingLabel, progressBar);

            splashLayout.getChildren().add(vbox);

            // Create scene and stage
            Scene splashScene = new Scene(splashLayout, SPLASH_WIDTH, SPLASH_HEIGHT);
            Stage splashStage = new Stage();
            splashStage.initStyle(StageStyle.UNDECORATED);
            splashStage.setScene(splashScene);

            // Set icon
            setStageIcon(splashStage);

            // Create animation for progress bar
            Timeline timeline = new Timeline(
                    new KeyFrame(Duration.ZERO, new KeyValue(progressBar.progressProperty(), 0)),
                    new KeyFrame(Duration.seconds(3), new KeyValue(progressBar.progressProperty(), 1))
            );

            // Update loading message every second
            Timeline messageTimeline = new Timeline();
            for (int i = 1; i <= 3; i++) {
                final int index = i;
                messageTimeline.getKeyFrames().add(
                        new KeyFrame(Duration.seconds(i), e ->
                                loadingLabel.setText(getRandomLoadingMessage())
                        )
                );
            }

            // After splash finishes, load main interface
            timeline.setOnFinished(event -> {
                FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.8), splashLayout);
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0.0);
                fadeOut.setOnFinished(e -> {
                    splashStage.close();
                    loadMainInterface(primaryStage);
                });
                fadeOut.play();
            });

            timeline.play();
            messageTimeline.play();

            // Show splash screen
            splashStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            // If splash fails, try to launch main interface directly
            loadMainInterface(primaryStage);
        }
    }

    private ImageView createLogo() {
        ImageView logo = new ImageView();
        try {
            String logoPath = "/pictures/Pixely_logo.jpg";
            if (getClass().getResource(logoPath) != null) {
                Image logoImage = new Image(getClass().getResourceAsStream(logoPath));
                logo.setImage(logoImage);
                logo.setFitHeight(150);
                logo.setFitWidth(150);
                logo.setPreserveRatio(true);
            } else {
                // Create fallback colored shape if logo not found
                logo.setFitHeight(150);
                logo.setFitWidth(150);

                // Try with PNG extension
                logoPath = "/pictures/Pixely_logo.png";
                if (getClass().getResource(logoPath) != null) {
                    Image logoImage = new Image(getClass().getResourceAsStream(logoPath));
                    logo.setImage(logoImage);
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading logo: " + e.getMessage());
        }
        return logo;
    }

    private String getRandomLoadingMessage() {
        Random random = new Random();
        return LOADING_MESSAGES[random.nextInt(LOADING_MESSAGES.length)];
    }

    private void loadMainInterface(Stage primaryStage) {
        try {
            // Load the FXML file
            Parent root = FXMLLoader.load(getClass().getResource("/view/interface.fxml"));

            // Create the scene
            Scene scene = new Scene(root);

            // Set the stage properties
            primaryStage.setScene(scene);
            primaryStage.setTitle("Welcome to Pixely");

            // Add the application icon
            setStageIcon(primaryStage);

            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showErrorStage("Failed to load main interface: " + e.getMessage());
        }
    }

    private void setStageIcon(Stage stage) {
        try {
            // Try jpg first
            String imagePath = "/pictures/Pixely_logo.jpg";
            if (getClass().getResource(imagePath) != null) {
                Image icon = new Image(getClass().getResourceAsStream(imagePath));
                stage.getIcons().add(icon);
                return;
            }

        } catch (Exception e) {
            System.err.println("Error loading window icon: " + e.getMessage());
        }
    }

    private void showErrorStage(String errorMessage) {
        Stage errorStage = new Stage();
        StackPane errorPane = new StackPane();
        errorPane.setStyle("-fx-background-color: #f0f0f0;");

        VBox vbox = new VBox(20);
        vbox.setAlignment(Pos.CENTER);

        Label errorLabel = new Label("Error");
        errorLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
        errorLabel.setTextFill(Color.RED);

        Label messageLabel = new Label(errorMessage);
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(400);

        vbox.getChildren().addAll(errorLabel, messageLabel);
        errorPane.getChildren().add(vbox);

        Scene errorScene = new Scene(errorPane, 500, 200);
        errorStage.setScene(errorScene);
        errorStage.setTitle("Pixely Error");
        errorStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}