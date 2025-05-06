package games;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
    private Text scoreText;
    private Pane root;
    private Timeline enemyMovement;

    @Override
    public void start(Stage primaryStage) {
        root = new Pane();

        // Create the player
        player = new Rectangle(CELL_SIZE, CELL_SIZE);
        player.setFill(Color.BLUE);
        player.setX(CELL_SIZE);
        player.setY(CELL_SIZE);
        root.getChildren().add(player);

        // Create score text
        scoreText = new Text("Score: 0");
        scoreText.setFill(Color.BLACK);
        scoreText.setX(10);
        scoreText.setY(20);
        root.getChildren().add(scoreText);

        // Create barriers
        createBarriers();

        // Create enemies
        createEnemies();

        Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);

        // Handle player movement
        scene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case UP -> movePlayer(0, -CELL_SIZE);
                case DOWN -> movePlayer(0, CELL_SIZE);
                case LEFT -> movePlayer(-CELL_SIZE, 0);
                case RIGHT -> movePlayer(CELL_SIZE, 0);
                case SPACE -> dropBomb();
            }
        });

        // Animate enemies
        enemyMovement = new Timeline(new KeyFrame(Duration.seconds(0.5), e -> moveEnemies()));
        enemyMovement.setCycleCount(Timeline.INDEFINITE);
        enemyMovement.play();

        primaryStage.setTitle("Bomberman Game");
        primaryStage.setScene(scene);
        primaryStage.show();
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
        // Remove the bomb from the screen and the list
        root.getChildren().remove(bomb);
        bombs.remove(bomb);

        // Create an explosion effect (cross shape)
        List<Rectangle> explosions = new ArrayList<>();
        createExplosion(bomb.getX(), bomb.getY(), explosions); // Center
        
        // Left explosion (until hitting a barrier)
        for (int i = 1; i <= 2; i++) {
            if (!createExplosion(bomb.getX() - i * CELL_SIZE, bomb.getY(), explosions)) {
                break;
            }
        }
        
        // Right explosion
        for (int i = 1; i <= 2; i++) {
            if (!createExplosion(bomb.getX() + i * CELL_SIZE, bomb.getY(), explosions)) {
                break;
            }
        }
        
        // Up explosion
        for (int i = 1; i <= 2; i++) {
            if (!createExplosion(bomb.getX(), bomb.getY() - i * CELL_SIZE, explosions)) {
                break;
            }
        }
        
        // Down explosion
        for (int i = 1; i <= 2; i++) {
            if (!createExplosion(bomb.getX(), bomb.getY() + i * CELL_SIZE, explosions)) {
                break;
            }
        }

        // Check for collisions with enemies and barriers
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
        Timeline explosionDuration = new Timeline(new KeyFrame(Duration.seconds(0.5), e -> {
            root.getChildren().removeAll(explosions);
        }));
        explosionDuration.setCycleCount(1);
        explosionDuration.play();
    }

    private boolean createExplosion(double x, double y, List<Rectangle> explosions) {
        if (x >= 0 && x < WINDOW_WIDTH && y >= 0 && y < WINDOW_HEIGHT) {
            // Check if there's an indestructible barrier at this position
            for (Rectangle barrier : barriers) {
                if (barrier.getX() == x && barrier.getY() == y && barrier.getFill() == Color.GRAY) {
                    return false;
                }
            }
            
            Rectangle explosion = new Rectangle(CELL_SIZE, CELL_SIZE);
            explosion.setFill(Color.ORANGE);
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
            Color barrierColor = random.nextBoolean() ? Color.GRAY : Color.BROWN;
            barrier.setFill(barrierColor);
            
            int x = random.nextInt(GRID_WIDTH) * CELL_SIZE;
            int y = random.nextInt(GRID_HEIGHT) * CELL_SIZE;
            
            // Don't place barriers on player's starting position or adjacent cells
            if ((x == CELL_SIZE && y == CELL_SIZE) || 
                (x == 0 && y == CELL_SIZE) || 
                (x == CELL_SIZE && y == 0) ||
                (x == 2*CELL_SIZE && y == CELL_SIZE) ||
                (x == CELL_SIZE && y == 2*CELL_SIZE)) {
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
        
        while (enemiesCreated < 3) {
            Rectangle enemy = new Rectangle(CELL_SIZE, CELL_SIZE);
            enemy.setFill(Color.GREEN);
            
            int x = random.nextInt(GRID_WIDTH) * CELL_SIZE;
            int y = random.nextInt(GRID_HEIGHT) * CELL_SIZE;
            
            // Don't place enemies on player's starting position or adjacent cells
            if ((x == CELL_SIZE && y == CELL_SIZE) || 
                (x == 0 && y == CELL_SIZE) || 
                (x == CELL_SIZE && y == 0) ||
                (x == 2*CELL_SIZE && y == CELL_SIZE) ||
                (x == CELL_SIZE && y == 2*CELL_SIZE)) {
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

            // Ensure enemies stay within the grid and don't collide with barriers or other enemies
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
        enemies.removeIf(enemy -> {
            if (enemy.getBoundsInParent().intersects(explosion.getBoundsInParent())) {
                root.getChildren().remove(enemy);
                score += 10;
                updateScore();
                return true;
            }
            return false;
        });
        
        // Check if all enemies are destroyed
        if (enemies.isEmpty()) {
            gameWon();
        }
    }

    private void destroyBarriers(Rectangle explosion) {
        barriers.removeIf(barrier -> {
            if (barrier.getFill() == Color.BROWN && barrier.getBoundsInParent().intersects(explosion.getBoundsInParent())) {
                root.getChildren().remove(barrier);
                return true;
            }
            return false;
        });
    }

    private void updateScore() {
        scoreText.setText("Score: " + score);
    }
    
    private void gameOver() {
        enemyMovement.stop();
        Text gameOverText = new Text("GAME OVER\nFinal Score: " + score);
        gameOverText.setFill(Color.RED);
        gameOverText.setX(WINDOW_WIDTH / 2 - 80);
        gameOverText.setY(WINDOW_HEIGHT / 2);
        root.getChildren().add(gameOverText);
    }
    
    private void gameWon() {
        enemyMovement.stop();
        Text winText = new Text("YOU WIN!\nFinal Score: " + score);
        winText.setFill(Color.GREEN);
        winText.setX(WINDOW_WIDTH / 2 - 60);
        winText.setY(WINDOW_HEIGHT / 2);
        root.getChildren().add(winText);
    }

    public static void main(String[] args) {
        launch(args);
    }
}