package application;

import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import game.GameManager;
import javafx.fxml.FXMLLoader;

public class GameLauncher {
    private Stage primaryStage;
    private StackPane rootLayout;
    
    public GameLauncher(Stage primaryStage) {
        this.primaryStage = primaryStage;
        initialize();
    }
    
    private void initialize() {
        rootLayout = new StackPane();
        Scene scene = new Scene(rootLayout, 800, 600);
        scene.getStylesheets().add(getClass().getResource("/view/styles.css").toExternalForm());
        primaryStage.setScene(scene);
        showLogin();
        primaryStage.show();
    }
    
    public void showLogin() {
        loadView("/view/LoginView.fxml", controller -> {
            ((LoginController)controller).setGameLauncher(this);
        });
    }
    
    public void showMainMenu() {
        loadView("/view/MainMenuView.fxml", controller -> {
            ((MainMenuController)controller).setGameLauncher(this);
        });
    }
    
    public void launchGame(String gameName) {
        switch(gameName) {
            case "RogueLike":
                GameManager gameManager = new GameManager();
                rootLayout.getChildren().setAll(gameManager.getGameView().getCanvas());
                gameManager.startGame();
                break;
            default:
                showComingSoon();
        }
    }
    
    public void showComingSoon() {
        // Implement a "Coming Soon" view
    }
    
    private void loadView(String fxmlPath, java.util.function.Consumer<Object> controllerConfig) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource(fxmlPath));
            rootLayout.getChildren().setAll(loader.load());
            controllerConfig.accept(loader.getController());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}