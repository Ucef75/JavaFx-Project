package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import java.io.IOException;

public class Main extends Application {
    
    private Stage primaryStage;
    private StackPane rootLayout;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Gamer Corner");
        
        initRootLayout();
        showLoginView();
    }
    
    private void initRootLayout() {
        rootLayout = new StackPane();
        Scene scene = new Scene(rootLayout, 800, 600);
        
        // Load CSS - make sure the path is correct
        scene.getStylesheets().add(getClass().getResource("/view/styles.css").toExternalForm());
        
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    private void showLoginView() {
        try {
            FXMLLoader loader = new FXMLLoader();
            // Make sure this path matches your actual FXML location
            loader.setLocation(getClass().getResource("/view/LoginView.fxml"));
            StackPane loginView = loader.load();
            
            LoginController controller = loader.getController();
            controller.setMainApp(this);
            
            rootLayout.getChildren().setAll(loginView);
        } catch (IOException e) {
            e.printStackTrace();
            // Add proper error handling
            System.err.println("Failed to load LoginView.fxml");
            System.err.println("Make sure the file exists at: /view/LoginView.fxml");
        }
    }
    
    public void showMainMenu() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/view/MainMenuView.fxml"));
            StackPane mainMenuView = loader.load();
            
            MainMenuController controller = loader.getController();
            controller.setMainApp(this);
            
            rootLayout.getChildren().setAll(mainMenuView);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load MainMenuView.fxml");
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}