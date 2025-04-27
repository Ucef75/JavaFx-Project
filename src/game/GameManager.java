package game;

import ui.GameView;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.Duration;

public class GameManager {
    private Dungeon dungeon;
    private Player player;
    private GameView gameView;
    private boolean isGameRunning;

    public GameManager() {
        this.dungeon = new Dungeon(10, 10); // Création du donjon de taille 10x10
        this.player = new Player(1, 1, dungeon);      // Position initiale du joueur (1, 1)
        this.gameView = new GameView(dungeon, player); // Vue du jeu
        this.isGameRunning = false;
    }

    public GameView getGameView() {
        return gameView;
    }

    public void startGame() {
        isGameRunning = true;
        gameLoop();  // Lancer la boucle de jeu
    }

    private void gameLoop() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(0.5), e -> updateGame()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        // Gérer les événements de pression des touches
        gameView.getScene().setOnKeyPressed(this::handleKeyPress);
    }

    private void updateGame() {
        if (isGameRunning) {
            dungeon.update(); // Met à jour le donjon
            checkPlayerPosition(); // Vérifie la position du joueur
            gameView.update(); // Met à jour la vue du jeu
            
            // Vérifier si la santé du joueur est inférieure ou égale à 0
            if (player.getHealth() <= 0) {
                isGameRunning = false;
                gameView.showGameOver(); // Afficher Game Over
            }
            
            // Vérifier si tous les trésors ont été collectés
            if (dungeon.getTreasureCount() == 0) {
                isGameRunning = false;
                gameView.showVictory(); // Afficher Victoire
            }
        }
    }

    private void checkPlayerPosition() {
        // No longer needed in GameManager, since Player class handles it now
    }

    private void handleKeyPress(KeyEvent event) {
        if (!isGameRunning) return;  // If the game isn't running, don't process events

        System.out.println("Key pressed: " + event.getCode()); // Debugging line to see which key is pressed

        // Gérer les déplacements avec les touches fléchées ou WASD
        switch (event.getCode()) {
            case W:
            case UP:
                player.moveUp();
                System.out.println("Moving up: " + player.getX() + ", " + player.getY()); // Debugging line
                break;
            case S:
            case DOWN:
                player.moveDown();
                System.out.println("Moving down: " + player.getX() + ", " + player.getY()); // Debugging line
                break;
            case A:
            case LEFT:
                player.moveLeft();
                System.out.println("Moving left: " + player.getX() + ", " + player.getY()); // Debugging line
                break;
            case D:
            case RIGHT:
                player.moveRight();
                System.out.println("Moving right: " + player.getX() + ", " + player.getY()); // Debugging line
                break;
        }

        gameView.update(); // Mettre à jour la vue après chaque déplacement
    }
}
