package games;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MiniDungeonGame extends Application {
    private Dungeon dungeon;
    private Player player;
    private GameView gameView;
    private boolean isGameRunning = false;

    @Override
    public void start(Stage primaryStage) {
        dungeon = new Dungeon(10, 10);
        player = new Player(1, 1, dungeon);
        gameView = new GameView(dungeon, player);

        Scene scene = new Scene(gameView, 500, 500);
        primaryStage.setTitle("Mini Dungeon Game");
        primaryStage.setScene(scene);
        primaryStage.show();

        startGame(scene);
    }

    private void startGame(Scene scene) {
        isGameRunning = true;
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(0.5), e -> updateGame()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        scene.setOnKeyPressed(this::handleKeyPress);
    }

    private void updateGame() {
        if (isGameRunning) {
            dungeon.update();
            gameView.update();

            if (player.getHealth() <= 0) {
                isGameRunning = false;
                gameView.showGameOver();
            }
            if (dungeon.getTreasureCount() == 0) {
                isGameRunning = false;
                gameView.showVictory();
            }
        }
    }

    private void handleKeyPress(KeyEvent event) {
        if (!isGameRunning) return;
        KeyCode code = event.getCode();

        switch (code) {
            case W, UP -> player.moveUp();
            case S, DOWN -> player.moveDown();
            case A, LEFT -> player.moveLeft();
            case D, RIGHT -> player.moveRight();
        }

        gameView.update();
    }

    // -------- CLASSES INTERNES --------

    private class Dungeon {
        private int width, height;
        private char[][] grid;
        private List<Enemy> enemies;
        private int treasureCount;
        private Random random = new Random();

        public Dungeon(int width, int height) {
            this.width = width;
            this.height = height;
            grid = new char[width][height];
            enemies = new ArrayList<>();
            generateDungeon();
        }

        private void generateDungeon() {
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    grid[i][j] = (i == 0 || j == 0 || i == width - 1 || j == height - 1) ? '#' : '.';
                }
            }

            treasureCount = 5;
            for (int i = 0; i < treasureCount; i++) {
                int x = random.nextInt(width - 2) + 1;
                int y = random.nextInt(height - 2) + 1;
                grid[x][y] = 'T';
            }

            for (int i = 0; i < 3; i++) {
                int x = random.nextInt(width - 2) + 1;
                int y = random.nextInt(height - 2) + 1;
                enemies.add(new Enemy(x, y));
                grid[x][y] = 'E';
            }
        }

        public void update() {
            for (Enemy enemy : enemies) {
                grid[enemy.getX()][enemy.getY()] = '.';
                int dir = random.nextInt(4);
                switch (dir) {
                    case 0 -> enemy.moveUp();
                    case 1 -> enemy.moveDown();
                    case 2 -> enemy.moveLeft();
                    case 3 -> enemy.moveRight();
                }
                grid[enemy.getX()][enemy.getY()] = 'E';
            }
        }

        public char getTile(int x, int y) { return grid[x][y]; }
        public void setTile(int x, int y, char tile) { grid[x][y] = tile; }
        public int getWidth() { return width; }
        public int getHeight() { return height; }
        public int getTreasureCount() { return treasureCount; }
        public void collectTreasure() { treasureCount--; }
    }

    private abstract class Entity {
        protected int x, y;
        public Entity(int x, int y) { this.x = x; this.y = y; }
        public int getX() { return x; }
        public int getY() { return y; }
        public abstract void moveUp();
        public abstract void moveDown();
        public abstract void moveLeft();
        public abstract void moveRight();
    }

    private class Enemy extends Entity {
        private int damage = 10;

        public Enemy(int x, int y) { super(x, y); }

        public void moveUp() { if (y > 0) y--; }
        public void moveDown() { if (y < 9) y++; }
        public void moveLeft() { if (x > 0) x--; }
        public void moveRight() { if (x < 9) x++; }

        public int getDamage() { return damage; }
    }

    private class Player {
        private int x, y, health = 100, score = 0;
        private Dungeon dungeon;

        public Player(int startX, int startY, Dungeon dungeon) {
            this.x = startX;
            this.y = startY;
            this.dungeon = dungeon;
        }

        public int getX() { return x; }
        public int getY() { return y; }
        public int getHealth() { return health; }
        public int getScore() { return score; }

        public void moveUp() { if (y > 0) { y--; interactWithTile(); } }
        public void moveDown() { if (y < dungeon.getHeight() - 1) { y++; interactWithTile(); } }
        public void moveLeft() { if (x > 0) { x--; interactWithTile(); } }
        public void moveRight() { if (x < dungeon.getWidth() - 1) { x++; interactWithTile(); } }

        private void interactWithTile() {
            char tile = dungeon.getTile(x, y);
            if (tile == 'T') {
                score += 10;
                dungeon.setTile(x, y, '.');
                dungeon.collectTreasure();
            } else if (tile == 'E') {
                health -= 10;
                moveAwayFromEnemy();
            }
        }

        private void moveAwayFromEnemy() {
            if (x > 0) x--; else if (x < dungeon.getWidth() - 1) x++;
            if (y > 0) y--; else if (y < dungeon.getHeight() - 1) y++;
        }
    }

    private class GameView extends StackPane {
        private Dungeon dungeon;
        private Player player;
        private Canvas canvas;

        public GameView(Dungeon dungeon, Player player) {
            this.dungeon = dungeon;
            this.player = player;
            canvas = new Canvas(500, 500);
            getChildren().add(canvas);
            update();
        }

        public void update() {
            GraphicsContext gc = canvas.getGraphicsContext2D();
            gc.setFill(Color.BLACK);
            gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

            double tileSize = 500.0 / dungeon.getWidth();

            for (int x = 0; x < dungeon.getWidth(); x++) {
                for (int y = 0; y < dungeon.getHeight(); y++) {
                    char tile = dungeon.getTile(x, y);
                    switch (tile) {
                        case '#' -> gc.setFill(Color.GRAY);
                        case '.' -> gc.setFill(Color.DARKSLATEGRAY);
                        case 'T' -> gc.setFill(Color.GOLD);
                        case 'E' -> gc.setFill(Color.RED);
                    }
                    gc.fillRect(x * tileSize, y * tileSize, tileSize, tileSize);
                }
            }

            gc.setFill(Color.BLUE);
            gc.fillOval(player.getX() * tileSize, player.getY() * tileSize, tileSize, tileSize);
        }

        public void showGameOver() {
            System.out.println("Game Over!");
        }

        public void showVictory() {
            System.out.println("Victory!");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
