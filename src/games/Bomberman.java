package games;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.*;

public class Bomberman extends Application {
    private static final int CELL_SIZE = 40;
    private static final int GRID_WIDTH = 10;
    private static final int GRID_HEIGHT = 10;
    private static final int WINDOW_WIDTH = CELL_SIZE * GRID_WIDTH;
    private static final int WINDOW_HEIGHT = CELL_SIZE * GRID_HEIGHT;

    private Rectangle player;
    private List<Rectangle> bombs = new ArrayList<>();
    private List<Rectangle> enemies = new ArrayList<>();
    private List<Rectangle> barriers = new ArrayList<>();
    private int score = 0;
    private int highScore = 0;
    private Text scoreText;
    private Text highScoreText;
    private Pane root;
    private Timeline enemyMovement;
    private boolean gameActive = true;

    @Override
    public void start(Stage primaryStage) {
        VBox mainLayout = new VBox();
        mainLayout.setAlignment(Pos.TOP_CENTER);

        root = new Pane();
        root.setPrefSize(WINDOW_WIDTH, WINDOW_HEIGHT);

        scoreText = new Text("Score: 0");
        scoreText.setFont(Font.font(18));
        scoreText.setFill(Color.DARKBLUE);
        highScoreText = new Text("High Score: 0");
        highScoreText.setFont(Font.font(18));
        highScoreText.setFill(Color.DARKBLUE);

        VBox scoreBox = new VBox(5, scoreText, highScoreText);
        scoreBox.setAlignment(Pos.CENTER);

        mainLayout.getChildren().addAll(scoreBox, root);

        Scene scene = new Scene(mainLayout, WINDOW_WIDTH, WINDOW_HEIGHT + 40);

        setupGame();

        // Handle player movement
        scene.setOnKeyPressed(event -> {
            if (!gameActive) return;
            switch (event.getCode()) {
                case UP -> movePlayer(0, -CELL_SIZE);
                case DOWN -> movePlayer(0, CELL_SIZE);
                case LEFT -> movePlayer(-CELL_SIZE, 0);
                case RIGHT -> movePlayer(CELL_SIZE, 0);
                case SPACE -> dropBomb();
                case R -> {
                    if (!gameActive) resetGame();
                }
            }
        });

        primaryStage.setTitle("Bomberman Game");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    private void setupGame() {
        gameActive = true;
        root.getChildren().clear();
        bombs.clear();
        enemies.clear();
        barriers.clear();
        score = 0;
        updateScore();
        updateHighScore();

        // Create the player
        player = new Rectangle(CELL_SIZE, CELL_SIZE);
        player.setFill(Color.BLUE);
        player.setX(CELL_SIZE);
        player.setY(CELL_SIZE);
        root.getChildren().add(player);

        // Create barriers
        createBarriers();

        // Create enemies
        createEnemies();

        // Animate enemies
        if (enemyMovement != null) enemyMovement.stop();
        enemyMovement = new Timeline(new KeyFrame(Duration.seconds(0.4), e -> moveEnemies()));
        enemyMovement.setCycleCount(Timeline.INDEFINITE);
        enemyMovement.play();
    }

    private void movePlayer(int dx, int dy) {
        double newX = player.getX() + dx;
        double newY = player.getY() + dy;

        // Ensure the player stays within the grid and doesn't collide with barriers or bombs
        if (newX >= 0 && newX < WINDOW_WIDTH && newY >= 0 && newY < WINDOW_HEIGHT &&
                !collidesWithBarrier(newX, newY) && !collidesWithBomb(newX, newY)) {
            player.setX(newX);
            player.setY(newY);
        }
    }

    private boolean collidesWithBomb(double x, double y) {
        for (Rectangle bomb : bombs) {
            if (bomb.getX() == x && bomb.getY() == y) {
                return true;
            }
        }
        return false;
    }

    private void dropBomb() {
        if (!gameActive) return;
        // Check if there's already a bomb at this position
        if (collidesWithBomb(player.getX(), player.getY())) {
            return;
        }

        // Create a bomb at the player's current position
        Rectangle bomb = new Rectangle(CELL_SIZE, CELL_SIZE);
        bomb.setFill(Color.RED);
        bomb.setX(player.getX());
        bomb.setY(player.getY());
        bombs.add(bomb);
        root.getChildren().add(bomb);

        // Set a timer for the bomb to explode
        Timeline explosionTimer = new Timeline(new KeyFrame(Duration.seconds(2), e -> explodeBomb(bomb)));
        explosionTimer.setCycleCount(1);
        explosionTimer.play();
    }

    private void explodeBomb(Rectangle bomb) {
        if (!gameActive) return;
        root.getChildren().remove(bomb);
        bombs.remove(bomb);

        // Create an explosion effect (cross shape)
        List<Rectangle> explosions = new ArrayList<>();
        createExplosion(bomb.getX(), bomb.getY(), explosions); // Center

        // Left explosion up to two cells
        for (int i = 1; i <= 2; i++) {
            if (!createExplosion(bomb.getX() - i * CELL_SIZE, bomb.getY(), explosions)) break;
        }
        // Right explosion
        for (int i = 1; i <= 2; i++) {
            if (!createExplosion(bomb.getX() + i * CELL_SIZE, bomb.getY(), explosions)) break;
        }
        // Up explosion
        for (int i = 1; i <= 2; i++) {
            if (!createExplosion(bomb.getX(), bomb.getY() - i * CELL_SIZE, explosions)) break;
        }
        // Down explosion
        for (int i = 1; i <= 2; i++) {
            if (!createExplosion(bomb.getX(), bomb.getY() + i * CELL_SIZE, explosions)) break;
        }

        // Check for collisions with enemies/barriers/player
        for (Rectangle explosion : explosions) {
            destroyEnemies(explosion);
            destroyBarriers(explosion);

            // Check if player is hit by explosion
            if (player.getBoundsInParent().intersects(explosion.getBoundsInParent())) {
                gameOver();
                return;
            }
        }

        // Remove the explosion effect after a short delay
        Timeline explosionDuration = new Timeline(new KeyFrame(Duration.seconds(0.4), e -> {
            root.getChildren().removeAll(explosions);
        }));
        explosionDuration.setCycleCount(1);
        explosionDuration.play();
    }

    private boolean createExplosion(double x, double y, List<Rectangle> explosions) {
        if (x >= 0 && x < WINDOW_WIDTH && y >= 0 && y < WINDOW_HEIGHT) {
            // Check if there's an indestructible barrier at this position
            for (Rectangle barrier : barriers) {
                if (barrier.getX() == x && barrier.getY() == y && barrier.getFill().equals(Color.GRAY)) {
                    return false;
                }
            }
            Rectangle explosion = new Rectangle(CELL_SIZE, CELL_SIZE);
            explosion.setFill(Color.GOLD);
            explosion.setOpacity(0.8);
            explosion.setX(x);
            explosion.setY(y);
            explosions.add(explosion);
            root.getChildren().add(explosion);
            return true;
        }
        return false;
    }

    private void createBarriers() {
        Random random = new Random();
        int barriersCreated = 0;

        // Make sure player starting position is clear
        while (barriersCreated < 15) {
            Rectangle barrier = new Rectangle(CELL_SIZE, CELL_SIZE);
            Color barrierColor = random.nextBoolean() ? Color.GRAY : Color.SADDLEBROWN;
            barrier.setFill(barrierColor);

            int x = random.nextInt(GRID_WIDTH) * CELL_SIZE;
            int y = random.nextInt(GRID_HEIGHT) * CELL_SIZE;

            // Don't place barriers on player's starting position or adjacent cells
            if ((x == CELL_SIZE && y == CELL_SIZE) ||
                    (x == 0 && y == CELL_SIZE) ||
                    (x == CELL_SIZE && y == 0) ||
                    (x == 2 * CELL_SIZE && y == CELL_SIZE) ||
                    (x == CELL_SIZE && y == 2 * CELL_SIZE)) {
                continue;
            }

            barrier.setX(x);
            barrier.setY(y);

            // Check if this position is already occupied
            boolean positionOccupied = false;
            for (Rectangle existing : barriers) {
                if (existing.getX() == x && existing.getY() == y) {
                    positionOccupied = true;
                    break;
                }
            }

            if (!positionOccupied) {
                barriers.add(barrier);
                root.getChildren().add(barrier);
                barriersCreated++;
            }
        }
    }

    private void createEnemies() {
        Random random = new Random();
        int enemiesCreated = 0;

        while (enemiesCreated < 4) {
            Rectangle enemy = new Rectangle(CELL_SIZE, CELL_SIZE);
            enemy.setFill(Color.DARKGREEN);

            int x = random.nextInt(GRID_WIDTH) * CELL_SIZE;
            int y = random.nextInt(GRID_HEIGHT) * CELL_SIZE;

            // Don't place enemies on player's starting position or adjacent cells
            if ((x == CELL_SIZE && y == CELL_SIZE) ||
                    (x == 0 && y == CELL_SIZE) ||
                    (x == CELL_SIZE && y == 0) ||
                    (x == 2 * CELL_SIZE && y == CELL_SIZE) ||
                    (x == CELL_SIZE && y == 2 * CELL_SIZE)) {
                continue;
            }

            // Don't place enemies on barriers
            boolean onBarrier = false;
            for (Rectangle barrier : barriers) {
                if (barrier.getX() == x && barrier.getY() == y) {
                    onBarrier = true;
                    break;
                }
            }

            if (!onBarrier) {
                enemy.setX(x);
                enemy.setY(y);
                enemies.add(enemy);
                root.getChildren().add(enemy);
                enemiesCreated++;
            }
        }
    }

    private void moveEnemies() {
        if (!gameActive) return;
        Random random = new Random();
        List<Rectangle> enemiesToCheck = new ArrayList<>(enemies);

        for (Rectangle enemy : enemiesToCheck) {
            int dx = 0, dy = 0;
            switch (random.nextInt(4)) {
                case 0 -> dy = -CELL_SIZE; // Up
                case 1 -> dy = CELL_SIZE;  // Down
                case 2 -> dx = -CELL_SIZE; // Left
                case 3 -> dx = CELL_SIZE;  // Right
            }
            double newX = enemy.getX() + dx;
            double newY = enemy.getY() + dy;

            // Ensure enemies stay within the grid and don't collide with barriers, bombs, or other enemies
            if (newX >= 0 && newX < WINDOW_WIDTH && newY >= 0 && newY < WINDOW_HEIGHT &&
                    !collidesWithBarrier(newX, newY) && !collidesWithOtherEnemy(enemy, newX, newY) &&
                    !collidesWithBomb(newX, newY)) {
                enemy.setX(newX);
                enemy.setY(newY);
            }

            // Check for collision with the player (Game Over)
            if (enemy.getBoundsInParent().intersects(player.getBoundsInParent())) {
                gameOver();
                return;
            }
        }
    }

    private boolean collidesWithOtherEnemy(Rectangle currentEnemy, double x, double y) {
        for (Rectangle enemy : enemies) {
            if (enemy != currentEnemy && enemy.getX() == x && enemy.getY() == y) {
                return true;
            }
        }
        return false;
    }

    private boolean collidesWithBarrier(double x, double y) {
        for (Rectangle barrier : barriers) {
            if (barrier.getX() == x && barrier.getY() == y) {
                return true;
            }
        }
        return false;
    }

    private void destroyEnemies(Rectangle explosion) {
        Iterator<Rectangle> enemyIt = enemies.iterator();
        boolean enemyKilled = false;
        while (enemyIt.hasNext()) {
            Rectangle enemy = enemyIt.next();
            if (enemy.getBoundsInParent().intersects(explosion.getBoundsInParent())) {
                root.getChildren().remove(enemy);
                score += 15;
                updateScore();
                enemyIt.remove();
                enemyKilled = true;
            }
        }
        if (enemyKilled) {
            updateScore();
        }

        // Check if all enemies are destroyed
        if (enemies.isEmpty() && gameActive) {
            gameWon();
        }
    }

    private void destroyBarriers(Rectangle explosion) {
        Iterator<Rectangle> barrierIt = barriers.iterator();
        while (barrierIt.hasNext()) {
            Rectangle barrier = barrierIt.next();
            if (barrier.getFill().equals(Color.SADDLEBROWN) &&
                    barrier.getBoundsInParent().intersects(explosion.getBoundsInParent())) {
                root.getChildren().remove(barrier);
                barrierIt.remove();
            }
        }
    }

    private void updateScore() {
        scoreText.setText("Score: " + score);
    }

    private void updateHighScore() {
        if (score > highScore) highScore = score;
        highScoreText.setText("High Score: " + highScore);
    }

    private void gameOver() {
        if (!gameActive) return;
        gameActive = false;
        enemyMovement.stop();
        updateHighScore();
        Text gameOverText = new Text("GAME OVER\nFinal Score: " + score + "\nPress 'R' to Restart");
        gameOverText.setFont(Font.font(22));
        gameOverText.setFill(Color.RED);
        gameOverText.setX(WINDOW_WIDTH / 2.0 - 100);
        gameOverText.setY(WINDOW_HEIGHT / 2.0);
        root.getChildren().add(gameOverText);

        showRestartAlert("Game Over! Your Score: " + score + "\nHigh Score: " + highScore + "\nPlay again?");
    }

    private void gameWon() {
        if (!gameActive) return;
        gameActive = false;
        enemyMovement.stop();
        updateHighScore();
        Text winText = new Text("YOU WIN!\nFinal Score: " + score + "\nPress 'R' to Restart");
        winText.setFont(Font.font(22));
        winText.setFill(Color.GREEN);
        winText.setX(WINDOW_WIDTH / 2.0 - 80);
        winText.setY(WINDOW_HEIGHT / 2.0);
        root.getChildren().add(winText);

        showRestartAlert("You Win! Your Score: " + score + "\nHigh Score: " + highScore + "\nPlay again?");
    }

    private void showRestartAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, msg, ButtonType.YES, ButtonType.NO);
        alert.setHeaderText("Restart Game?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            resetGame();
        }
    }

    private void resetGame() {
        setupGame();
    }

    public static void main(String[] args) {
        launch(args);
    }
}