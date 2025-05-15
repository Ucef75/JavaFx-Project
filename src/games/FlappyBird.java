package games;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class FlappyBird extends Application {

    private static final int WIDTH = 400;
    private static final int HEIGHT = 600;
    private static final int PIPE_WIDTH = 60;
    private static final int PIPE_GAP = 150;
    private static final int BIRD_RADIUS = 15;
    private static final int GROUND_HEIGHT = 50;

    private Circle bird;
    private double velocity = 0;
    private double gravity = 0.5;
    private double jumpStrength = -8;
    private int score = 0;
    private int highScore = 0;
    private Text scoreText;
    private Text highScoreText;

    private List<Rectangle> pipes = new ArrayList<>();
    private Rectangle ground;
    private Pane root = new Pane();
    private Random random = new Random();
    private boolean gameOver = false;
    private boolean gameStarted = false;

    private enum GameState {
        MENU, PLAYING, GAME_OVER
    }

    private GameState gameState = GameState.MENU;
    private Text titleText;
    private Text startText;
    private Text quitText;
    private Text gameOverText;
    private Text restartText;
    private Text finalScoreText;

    @Override
    public void start(Stage primaryStage) {
        // Setup game elements
        setupGameElements();
        setupMenu();
        setupGameOverScreen();

        Scene scene = new Scene(root, WIDTH, HEIGHT);

        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.SPACE) {
                if (gameState == GameState.PLAYING && !gameOver) {
                    velocity = jumpStrength;
                    // Play flap sound
                } else if (gameState == GameState.MENU) {
                    startGame();
                } else if (gameState == GameState.GAME_OVER) {
                    restartGame();
                }
            }

            if (e.getCode() == KeyCode.ESCAPE) {
                if (gameState == GameState.PLAYING) {
                    returnToMenu();
                } else if (gameState == GameState.GAME_OVER) {
                    returnToMenu();
                }
            }
        });

        AnimationTimer timer = new AnimationTimer() {
            private long lastPipeTime = 0;

            @Override
            public void handle(long now) {
                if (gameState == GameState.PLAYING) {
                    updateBird();
                    updatePipes();
                    checkCollisions();

                    if (now - lastPipeTime > 1_500_000_000) { // 1.5 seconds
                        spawnPipes();
                        lastPipeTime = now;
                    }
                }
            }
        };
        timer.start();

        primaryStage.setTitle("Flappy Bird");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    private void setupGameElements() {
        // Bird
        bird = new Circle(BIRD_RADIUS);
        bird.setFill(Color.YELLOW);
        bird.setStroke(Color.ORANGE);
        bird.setStrokeWidth(2);
        bird.setCenterX(WIDTH / 4);
        bird.setCenterY(HEIGHT / 2);

        // Ground
        ground = new Rectangle(0, HEIGHT - GROUND_HEIGHT, WIDTH, GROUND_HEIGHT);
        ground.setFill(Color.SANDYBROWN);

        // Score display
        scoreText = new Text(20, 40, "Score: 0");
        scoreText.setFont(Font.font(20));
        scoreText.setFill(Color.WHITE);
        scoreText.setVisible(false);

        highScoreText = new Text(WIDTH - 150, 40, "High Score: 0");
        highScoreText.setFont(Font.font(20));
        highScoreText.setFill(Color.WHITE);
        highScoreText.setVisible(false);

        root.getChildren().addAll(ground, bird, scoreText, highScoreText);
    }

    private void setupMenu() {
        titleText = new Text(WIDTH / 2 - 100, HEIGHT / 3, "Flappy Bird");
        titleText.setFont(Font.font(36));
        titleText.setFill(Color.GOLD);

        startText = new Text(WIDTH / 2 - 50, HEIGHT / 2, "Press SPACE to Start");
        startText.setFont(Font.font(20));
        startText.setFill(Color.WHITE);

        quitText = new Text(WIDTH / 2 - 40, HEIGHT / 2 + 40, "ESC to Quit");
        quitText.setFont(Font.font(20));
        quitText.setFill(Color.WHITE);

        root.getChildren().addAll(titleText, startText, quitText);
    }

    private void setupGameOverScreen() {
        gameOverText = new Text(WIDTH / 2 - 80, HEIGHT / 3, "Game Over");
        gameOverText.setFont(Font.font(36));
        gameOverText.setFill(Color.RED);
        gameOverText.setVisible(false);

        finalScoreText = new Text(WIDTH / 2 - 60, HEIGHT / 2, "Score: 0");
        finalScoreText.setFont(Font.font(24));
        finalScoreText.setFill(Color.WHITE);
        finalScoreText.setVisible(false);

        restartText = new Text(WIDTH / 2 - 100, HEIGHT / 2 + 60, "Press SPACE to Restart");
        restartText.setFont(Font.font(20));
        restartText.setFill(Color.WHITE);
        restartText.setVisible(false);

        root.getChildren().addAll(gameOverText, finalScoreText, restartText);
    }

    private void startGame() {
        gameState = GameState.PLAYING;
        gameOver = false;
        score = 0;
        updateScore();

        // Hide menu elements
        titleText.setVisible(false);
        startText.setVisible(false);
        quitText.setVisible(false);

        // Show game elements
        scoreText.setVisible(true);
        highScoreText.setVisible(true);

        // Reset bird position
        bird.setCenterX(WIDTH / 4);
        bird.setCenterY(HEIGHT / 2);
        velocity = 0;

        // Clear existing pipes
        root.getChildren().removeAll(pipes);
        pipes.clear();
    }

    private void returnToMenu() {
        gameState = GameState.MENU;

        // Hide game over elements
        gameOverText.setVisible(false);
        finalScoreText.setVisible(false);
        restartText.setVisible(false);
        scoreText.setVisible(false);

        // Show menu elements
        titleText.setVisible(true);
        startText.setVisible(true);
        quitText.setVisible(true);
    }

    private void showGameOver() {
        gameState = GameState.GAME_OVER;
        gameOverText.setVisible(true);
        finalScoreText.setText("Score: " + score);
        finalScoreText.setVisible(true);
        restartText.setVisible(true);
        scoreText.setVisible(false);

        if (score > highScore) {
            highScore = score;
            highScoreText.setText("High Score: " + highScore);
        }
    }

    private void restartGame() {
        gameState = GameState.PLAYING;
        gameOver = false;
        score = 0;
        updateScore();

        // Hide game over elements
        gameOverText.setVisible(false);
        finalScoreText.setVisible(false);
        restartText.setVisible(false);

        // Show score
        scoreText.setVisible(true);

        // Reset bird position
        bird.setCenterX(WIDTH / 4);
        bird.setCenterY(HEIGHT / 2);
        velocity = 0;

        // Clear existing pipes
        root.getChildren().removeAll(pipes);
        pipes.clear();
    }

    private void updateBird() {
        velocity += gravity;
        bird.setCenterY(bird.getCenterY() + velocity);

        // Rotate bird based on velocity
        bird.setRotate(Math.min(velocity * 5, 90));
    }

    private void spawnPipes() {
        int pipeHeight = random.nextInt(HEIGHT - PIPE_GAP - GROUND_HEIGHT - 100) + 50;

        // Top pipe
        Rectangle topPipe = new Rectangle(PIPE_WIDTH, pipeHeight);
        topPipe.setX(WIDTH);
        topPipe.setY(0);
        topPipe.setFill(Color.LIMEGREEN);
        topPipe.setStroke(Color.DARKGREEN);
        topPipe.setStrokeWidth(2);

        // Bottom pipe
        Rectangle bottomPipe = new Rectangle(PIPE_WIDTH, HEIGHT - pipeHeight - PIPE_GAP - GROUND_HEIGHT);
        bottomPipe.setX(WIDTH);
        bottomPipe.setY(pipeHeight + PIPE_GAP);
        bottomPipe.setFill(Color.LIMEGREEN);
        bottomPipe.setStroke(Color.DARKGREEN);
        bottomPipe.setStrokeWidth(2);

        pipes.add(topPipe);
        pipes.add(bottomPipe);
        root.getChildren().addAll(topPipe, bottomPipe);
    }

    private void updatePipes() {
        Iterator<Rectangle> it = pipes.iterator();
        boolean scored = false;

        while (it.hasNext()) {
            Rectangle pipe = it.next();
            pipe.setX(pipe.getX() - 3);

            // Check if bird passed the pipe
            if (pipe.getX() + PIPE_WIDTH < bird.getCenterX() && !pipe.getProperties().containsKey("scored")) {
                pipe.getProperties().put("scored", true);
                if (pipe.getY() > 0) { // Only score on bottom pipes to avoid double counting
                    scored = true;
                }
            }

            if (pipe.getX() + PIPE_WIDTH < 0) {
                root.getChildren().remove(pipe);
                it.remove();
            }
        }

        if (scored) {
            score++;
            updateScore();
            // Play score sound
        }
    }

    private void updateScore() {
        scoreText.setText("Score: " + score);
    }

    private void checkCollisions() {
        // Collision with ground or ceiling
        if (bird.getCenterY() - BIRD_RADIUS < 0 ||
                bird.getCenterY() + BIRD_RADIUS > HEIGHT - GROUND_HEIGHT) {
            endGame();
        }

        // Collision with pipes
        for (Rectangle pipe : pipes) {
            if (bird.getBoundsInParent().intersects(pipe.getBoundsInParent())) {
                endGame();
            }
        }
    }

    private void endGame() {
        gameOver = true;
        showGameOver();
        // Play crash sound
    }

    public static void main(String[] args) {
        launch(args);
    }
}