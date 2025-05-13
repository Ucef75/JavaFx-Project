package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        try {
            // Load the FXML file - better to use absolute path
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
        }
    }

    private void setStageIcon(Stage stage) {
        try {
            // Get the resource URL first to verify existence
            String imagePath = "/pictures/Pixely_logo.jpg"; // Changed to .png
            if (getClass().getResource(imagePath) == null) {
                throw new RuntimeException("Icon file not found at: " + imagePath);
            }

            Image icon = new Image(getClass().getResourceAsStream(imagePath));
            stage.getIcons().add(icon);
        } catch (Exception e) {
            System.err.println("Error loading window icon: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // First verify the icon exists before launching
        try {
            if (Main.class.getResource("/pictures/Pixely_logo.png") == null) {
                System.err.println("Warning: Application icon not found at /pictures/Pixely_logo.jpg");
            }
        } catch (Exception e) {
            System.err.println("Error checking icon path: " + e.getMessage());
        }

        launch(args);
    }
}