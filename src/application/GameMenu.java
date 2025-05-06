package application;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class GameMenu {
    
    // Singleton instance
    private static GameMenu instance;
    
    // Audio settings
    private boolean musicEnabled = true;
    private boolean soundFxEnabled = true;
    
    // Media players
    private MediaPlayer backgroundMusicPlayer;
    private Map<String, Media> soundEffects = new HashMap<>();
    
    // Current game stage reference
    private Stage currentGameStage;
    
    // Private constructor for singleton
    private GameMenu() {
        // Load sound effects
    }
    
    // Singleton instance getter
    public static GameMenu getInstance() {
        if (instance == null) {
            instance = new GameMenu();
        }
        return instance;
    }
    
    
    // Play a sound effect

    
    // Start background music
    // Set current game stage
    public void setCurrentGameStage(Stage stage) {
        this.currentGameStage = stage;
        
        // Add ESC key handler to show menu
        Scene scene = stage.getScene();
        if (scene != null) {
            scene.setOnKeyPressed(event -> {
                switch (event.getCode()) {
                    case ESCAPE:
                        showGameMenu();
                        break;
                    default:
                        break;
                }
            });
        }
    }
    
    // Show in-game menu
    public void showGameMenu() {
        if (currentGameStage == null) return;
        
        // Create menu stage
        Stage menuStage = new Stage();
        menuStage.initOwner(currentGameStage);
        menuStage.initModality(Modality.APPLICATION_MODAL);
        menuStage.initStyle(StageStyle.UTILITY);
        menuStage.setTitle("Game Menu");
        
        // Create menu components
        Label titleLabel = new Label("Game Menu");
        titleLabel.setStyle("-fx-font-size: 20; -fx-font-weight: bold;");
        
        CheckBox musicCheckBox = new CheckBox("Enable Music");
        musicCheckBox.setSelected(musicEnabled);
        musicCheckBox.setOnAction(e -> {
        });
        
        CheckBox soundFxCheckBox = new CheckBox("Enable Sound FX");
        soundFxCheckBox.setSelected(soundFxEnabled);
        soundFxCheckBox.setOnAction(e -> {
            soundFxEnabled = soundFxCheckBox.isSelected();
        });
        
        Button resumeButton = new Button("Resume Game");
        resumeButton.setMinWidth(150);
        resumeButton.setOnAction(e -> menuStage.close());
        
        Button restartButton = new Button("Restart Game");
        restartButton.setMinWidth(150);
        restartButton.setOnAction(e -> {
            menuStage.close();
            // This is a placeholder - you'll need to implement restart in each game
        });
        
        Button quitButton = new Button("Quit to Main Menu");
        quitButton.setMinWidth(150);
        quitButton.setOnAction(e -> {
            menuStage.close();
            currentGameStage.close();
        });
        
        // Layout
        VBox menuLayout = new VBox(10);
        menuLayout.setAlignment(Pos.CENTER);
        menuLayout.getChildren().addAll(
            titleLabel,
            musicCheckBox,
            soundFxCheckBox,
            resumeButton,
            restartButton,
            quitButton
        );
        menuLayout.setStyle("-fx-padding: 20; -fx-background-color: #f0f0f0;");
        
        BorderPane root = new BorderPane();
        root.setCenter(menuLayout);
        
        // Scene and show
        Scene scene = new Scene(root, 250, 300);
        menuStage.setScene(scene);
        menuStage.showAndWait();
    }
}