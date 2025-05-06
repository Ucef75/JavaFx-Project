package ui;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.layout.VBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class MainMenu {
    private Stage stage;
    private Scene gameScene;
    private boolean menuVisible = false;
    private VBox menu;
    private Runnable onResume;
    
    public MainMenu(Stage stage, Scene gameScene, Runnable onResume) {
        this.stage = stage;
        this.gameScene = gameScene;
        this.onResume = onResume;
        initializeMenu();
    }

    private void initializeMenu() {
        menu = new VBox(20);
        menu.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7); -fx-padding: 20px; -fx-alignment: center;");

        Text menuTitle = new Text("Main Menu");
        menuTitle.setFill(Color.WHITE);

        Button exitButton = new Button("Exit Game");
        exitButton.setOnAction(e -> System.exit(0));

        Button resumeButton = new Button("Resume Game");
        resumeButton.setOnAction(e -> toggleMenu());

        menu.getChildren().addAll(menuTitle, resumeButton, exitButton);
        menu.setVisible(false);
    }

    public void toggleMenu() {
        menuVisible = !menuVisible;
        if (menuVisible) {
            stage.setScene(new Scene(new StackPane(gameScene.getRoot(), menu), 
                                  gameScene.getWidth(), gameScene.getHeight()));
        } else {
            stage.setScene(gameScene);
            if (onResume != null) {
                onResume.run();
            }
        }
    }

    public void showMenu() {
        if (!menuVisible) toggleMenu();
    }

    public void hideMenu() {
        if (menuVisible) toggleMenu();
    }
}