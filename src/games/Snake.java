package games;

import ui.MainMenu;  // Import the main menu class
import globalFunc.Sound_Func;  // Import the sound functions
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.util.LinkedList;
import java.util.Random;

public class Snake extends Application {

    private static final int WIDTH = 600;
    private static final int HEIGHT = 400;
    private static final int TILE_SIZE = 20;

    private LinkedList<Rectangle> snake = new LinkedList<>();
    private Direction direction = Direction.RIGHT;
    private boolean running = true;
    private Rectangle food;
    private Random random = new Random();
    private Pane gamePane = new Pane();
    private Timeline timeline;
    private MainMenu mainMenu;
    private boolean gamePaused = false;

    private int score = 0;
    private int highScore = 0;
    private Text scoreText = new Text();
    private Text highScoreText = new Text();

    private enum Direction {
        UP, DOWN, LEFT, RIGHT
    }

    @Override
    public void start(Stage primaryStage) {
        VBox mainLayout = new VBox(0);
        mainLayout.setAlignment(Pos.TOP_CENTER);

        HBox scoreBox = new HBox(30, scoreText, highScoreText);
        scoreBox.setAlignment(Pos.CENTER);
        scoreText.setFont(Font.font(20));
        highScoreText.setFont(Font.font(20));
        updateScore(0);

        gamePane.setPrefSize(WIDTH, HEIGHT);
        mainLayout.getChildren().addAll(scoreBox, gamePane);

        Scene gameScene = new Scene(mainLayout);

        // Initialize menu with resume callback
        mainMenu = new MainMenu(primaryStage, gameScene, () -> {
            gamePaused = false;
            timeline.play();
            Sound_Func.playBackground();
        });

        // Key controls
        gameScene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                toggleGamePause();
            } else if (!gamePaused) {
                switch (e.getCode()) {
                    case UP:
                        if (direction != Direction.DOWN) direction = Direction.UP;
                        break;
                    case DOWN:
                        if (direction != Direction.UP) direction = Direction.DOWN;
                        break;
                    case LEFT:
                        if (direction != Direction.RIGHT) direction = Direction.LEFT;
                        break;
                    case RIGHT:
                        if (direction != Direction.LEFT) direction = Direction.RIGHT;
                        break;
                }
            }
        });

        // Game loop
        timeline = new Timeline(new KeyFrame(Duration.millis(100), e -> {
            if (running && !gamePaused) {
                move();
                checkCollision();
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);

        resetGame();
        timeline.play();

        primaryStage.setTitle("Snake Game");
        primaryStage.setScene(gameScene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    private void toggleGamePause() {
        gamePaused = !gamePaused;
        if (gamePaused) {
            timeline.pause();
            Sound_Func.playBackground();
            mainMenu.showMenu();
        } else {
            mainMenu.hideMenu();
            timeline.play();
        }
    }

    private void resetGame() {
        gamePane.getChildren().clear();
        snake.clear();
        direction = Direction.RIGHT;
        running = true;
        score = 0;
        updateScore(0);

        // Initialize snake
        Rectangle head = new Rectangle(TILE_SIZE, TILE_SIZE);
        head.setArcWidth(10);
        head.setArcHeight(10);
        head.setFill(Color.LIMEGREEN);
        head.setX(WIDTH / 2);
        head.setY(HEIGHT / 2);
        snake.add(head);
        gamePane.getChildren().add(head);

        spawnFood();
    }

    private void spawnFood() {
        if (food != null) gamePane.getChildren().remove(food);
        food = new Rectangle(TILE_SIZE, TILE_SIZE);
        food.setArcWidth(10);
        food.setArcHeight(10);
        food.setFill(Color.RED);

        // Only spawn food on empty positions
        int tries = 0;
        do {
            food.setX(random.nextInt(WIDTH / TILE_SIZE) * TILE_SIZE);
            food.setY(random.nextInt(HEIGHT / TILE_SIZE) * TILE_SIZE);
            tries++;
        } while (isFoodOnSnake() && tries < 1000);

        gamePane.getChildren().add(food);
    }

    private boolean isFoodOnSnake() {
        for (Rectangle part : snake) {
            if (part.getX() == food.getX() && part.getY() == food.getY()) {
                return true;
            }
        }
        return false;
    }

    private void move() {
        Rectangle head = snake.getFirst();
        double newX = head.getX();
        double newY = head.getY();

        switch (direction) {
            case UP: newY -= TILE_SIZE; break;
            case DOWN: newY += TILE_SIZE; break;
            case LEFT: newX -= TILE_SIZE; break;
            case RIGHT: newX += TILE_SIZE; break;
        }

        Rectangle newHead = new Rectangle(TILE_SIZE, TILE_SIZE);
        newHead.setArcWidth(10);
        newHead.setArcHeight(10);
        newHead.setFill(Color.LIMEGREEN);
        newHead.setX(newX);
        newHead.setY(newY);

        snake.addFirst(newHead);
        gamePane.getChildren().add(newHead);

        if (newHead.getX() == food.getX() && newHead.getY() == food.getY()) {
            score++;
            updateScore(1);
            Sound_Func.playEatingSound();
            spawnFood();
        } else {
            Rectangle tail = snake.removeLast();
            gamePane.getChildren().remove(tail);
        }
    }

    private void checkCollision() {
        Rectangle head = snake.getFirst();

        // Wall collision
        if (head.getX() < 0 || head.getY() < 0 || head.getX() >= WIDTH || head.getY() >= HEIGHT) {
            gameOver();
        }

        // Self collision
        for (int i = 1; i < snake.size(); i++) {
            Rectangle bodyPart = snake.get(i);
            if (head.getX() == bodyPart.getX() && head.getY() == bodyPart.getY()) {
                gameOver();
            }
        }
    }

    private void gameOver() {
        running = false;
        timeline.pause();
        Sound_Func.playDefeatSound();
        if (score > highScore) {
            highScore = score;
        }
        updateScore(0);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Game Over");
        alert.setHeaderText("Game Over! Your Score: " + score);
        alert.setContentText("High Score: " + highScore + "\nPress OK to play again.");
        alert.showAndWait();
        resetGame();
        timeline.play();
    }

    private void updateScore(int add) {
        scoreText.setText("Score: " + score);
        highScoreText.setText("High Score: " + highScore);
    }

    public static void main(String[] args) {
        launch(args);
    }
}