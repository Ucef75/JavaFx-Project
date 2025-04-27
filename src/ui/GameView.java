package ui;

import game.Dungeon;
import game.Player;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

public class GameView {
    private static final int TILE_SIZE = 50;
    private Canvas canvas;
    private Scene scene;
    private Dungeon dungeon;
    private Player player;

    public GameView(Dungeon dungeon, Player player) {
        this.dungeon = dungeon;
        this.player = player;
        this.canvas = new Canvas(dungeon.getWidth() * TILE_SIZE, dungeon.getHeight() * TILE_SIZE + 40);
        this.scene = new Scene(new StackPane(canvas));
    }

    public Scene getScene() {
        return scene;
    }

    public void update() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Draw HUD
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, canvas.getWidth(), 40);
        gc.setFill(Color.WHITE);
        gc.setFont(new Font(16));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText("Health: " + player.getHealth(), 10, 25);
        gc.setTextAlign(TextAlignment.RIGHT);
        gc.fillText("Score: " + player.getScore(), canvas.getWidth() - 10, 25);

        // Draw dungeon
        for (int x = 0; x < dungeon.getWidth(); x++) {
            for (int y = 0; y < dungeon.getHeight(); y++) {
                char tile = dungeon.getTile(x, y);
                switch (tile) {
                    case '#':
                        gc.setFill(Color.GRAY);
                        break;
                    case '.':
                        gc.setFill(Color.BLACK);
                        break;
                    case 'T':
                        gc.setFill(Color.GOLD);
                        break;
                    case 'E':
                        gc.setFill(Color.RED);
                        break;
                }
                gc.fillRect(x * TILE_SIZE, y * TILE_SIZE + 40, TILE_SIZE, TILE_SIZE);
            }
        }

        // Draw player
        gc.setFill(Color.BLUE);
        gc.fillOval(player.getX() * TILE_SIZE, player.getY() * TILE_SIZE + 40, TILE_SIZE, TILE_SIZE);
    }
    public Canvas getCanvas() {
        return canvas;
    }

    public void showGameOver() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.rgb(0, 0, 0, 0.7));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setFill(Color.WHITE);
        gc.setFont(new Font(30));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("GAME OVER", canvas.getWidth() / 2, canvas.getHeight() / 2);
        gc.setFont(new Font(20));
        gc.fillText("Final Score: " + player.getScore(), canvas.getWidth() / 2, canvas.getHeight() / 2 + 40);
    }

    public void showVictory() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.rgb(0, 0, 0, 0.7));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setFill(Color.GOLD);
        gc.setFont(new Font(30));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("VICTORY!", canvas.getWidth() / 2, canvas.getHeight() / 2);
        gc.setFont(new Font(20));
        gc.fillText("Final Score: " + player.getScore(), canvas.getWidth() / 2, canvas.getHeight() / 2 + 40);
    }
}