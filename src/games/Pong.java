package games;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class Pong extends Application {

    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int PADDLE_WIDTH = 15;
    private static final int PADDLE_HEIGHT = 100;
    private static final int BALL_SIZE = 15;
    private static final int PADDLE_SPEED = 10;
    private static final int AI_SPEED = 5;

    private Rectangle player;
    private Rectangle ai;
    private Circle ball;
    private int ballSpeedX = 4;
    private int ballSpeedY = 4;
    private int playerScore = 0;
    private int aiScore = 0;
    private Text playerScoreText;
    private Text aiScoreText;
    private Text gameOverText;
    private Text startText;
    private boolean gameRunning = false;
    private boolean gameStarted = false;

    @Override
    public void start(Stage primaryStage) {
        Pane root = new Pane();
        Scene scene = new Scene(root, WIDTH, HEIGHT, Color.BLACK);

        // Create paddles
        player = new Rectangle(PADDLE_WIDTH, PADDLE_HEIGHT, Color.DODGERBLUE);
        player.setLayoutX(50);
        player.setLayoutY(HEIGHT / 2 - PADDLE_HEIGHT / 2);
        player.setArcWidth(10);
        player.setArcHeight(10);

        ai = new Rectangle(PADDLE_WIDTH, PADDLE_HEIGHT, Color.ORANGERED);
        ai.setLayoutX(WIDTH - 50 - PADDLE_WIDTH);
        ai.setLayoutY(HEIGHT / 2 - PADDLE_HEIGHT / 2);
        ai.setArcWidth(10);
        ai.setArcHeight(10);

        // Create ball
        ball = new Circle(BALL_SIZE, Color.WHITE);
        resetBall();

        // Create score displays
        playerScoreText = new Text(WIDTH / 4, 50, "Player: 0");
        playerScoreText.setFont(Font.font(24));
        playerScoreText.setFill(Color.WHITE);

        aiScoreText = new Text(3 * WIDTH / 4 - 50, 50, "AI: 0");
        aiScoreText.setFont(Font.font(24));
        aiScoreText.setFill(Color.WHITE);

        // Create game over text
        gameOverText = new Text(WIDTH / 2 - 100, HEIGHT / 2, "");
        gameOverText.setFont(Font.font(30));
        gameOverText.setFill(Color.WHITE);
        gameOverText.setVisible(false);

        // Create start text
        startText = new Text(WIDTH / 2 - 150, HEIGHT / 2, "Press SPACE to Start");
        startText.setFont(Font.font(30));
        startText.setFill(Color.WHITE);

        root.getChildren().addAll(player, ai, ball, playerScoreText, aiScoreText, gameOverText, startText);

        // Keyboard controls
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.SPACE && !gameRunning) {
                if (!gameStarted) {
                    startGame();
                } else {
                    resetGame();
                }
            }

            if (gameRunning) {
                if (e.getCode() == KeyCode.UP) {
                    player.setLayoutY(Math.max(0, player.getLayoutY() - PADDLE_SPEED));
                } else if (e.getCode() == KeyCode.DOWN) {
                    player.setLayoutY(Math.min(HEIGHT - PADDLE_HEIGHT, player.getLayoutY() + PADDLE_SPEED));
                }
            }
        });

        // Game loop
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (!gameRunning) return;

                // Move ball
                ball.setLayoutX(ball.getLayoutX() + ballSpeedX);
                ball.setLayoutY(ball.getLayoutY() + ballSpeedY);

                // Ball collision with top and bottom
                if (ball.getLayoutY() <= BALL_SIZE || ball.getLayoutY() >= HEIGHT - BALL_SIZE) {
                    ballSpeedY *= -1;
                    playSound("bounce");
                }

                // AI paddle movement (with some imperfection for fairness)
                double aiTargetY = ball.getLayoutY() - PADDLE_HEIGHT / 2;
                double aiCurrentY = ai.getLayoutY();

                if (Math.abs(aiCurrentY - aiTargetY) > AI_SPEED) {
                    if (aiCurrentY < aiTargetY) {
                        ai.setLayoutY(Math.min(HEIGHT - PADDLE_HEIGHT, aiCurrentY + AI_SPEED));
                    } else {
                        ai.setLayoutY(Math.max(0, aiCurrentY - AI_SPEED));
                    }
                }

                // Ball collision with player paddle
                if (ball.getBoundsInParent().intersects(player.getBoundsInParent())) {
                    ballSpeedX = Math.abs(ballSpeedX); // Ensure ball moves right
                    // Add angle based on where ball hits paddle
                    double hitPosition = (ball.getLayoutY() - player.getLayoutY()) / PADDLE_HEIGHT;
                    ballSpeedY = (int)(10 * (hitPosition - 0.5));
                    playSound("paddle");
                }

                // Ball collision with AI paddle
                if (ball.getBoundsInParent().intersects(ai.getBoundsInParent())) {
                    ballSpeedX = -Math.abs(ballSpeedX); // Ensure ball moves left
                    // Add angle based on where ball hits paddle
                    double hitPosition = (ball.getLayoutY() - ai.getLayoutY()) / PADDLE_HEIGHT;
                    ballSpeedY = (int)(10 * (hitPosition - 0.5));
                    playSound("paddle");
                }

                // Score points
                if (ball.getLayoutX() < 0) {
                    aiScore++;
                    aiScoreText.setText("AI: " + aiScore);
                    scorePoint(false);
                } else if (ball.getLayoutX() > WIDTH) {
                    playerScore++;
                    playerScoreText.setText("Player: " + playerScore);
                    scorePoint(true);
                }
            }
        };
        timer.start();

        primaryStage.setTitle("Enhanced Pong Game");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    private void startGame() {
        gameStarted = true;
        gameRunning = true;
        startText.setVisible(false);
        gameOverText.setVisible(false);
        playerScore = 0;
        aiScore = 0;
        playerScoreText.setText("Player: 0");
        aiScoreText.setText("AI: 0");
        resetBall();
    }

    private void resetGame() {
        gameRunning = true;
        gameOverText.setVisible(false);
        resetBall();
    }

    private void resetBall() {
        ball.setLayoutX(WIDTH / 2);
        ball.setLayoutY(HEIGHT / 2);
        ballSpeedX = (Math.random() > 0.5 ? 1 : -1) * 4;
        ballSpeedY = (Math.random() > 0.5 ? 1 : -1) * 4;
    }

    private void scorePoint(boolean playerScored) {
        gameRunning = false;

        if (playerScore >= 5 || aiScore >= 5) {
            String winner = playerScore >= 5 ? "Player Wins!" : "AI Wins!";
            gameOverText.setText(winner + " Score: " + playerScore + "-" + aiScore + "\nPress SPACE to Play Again");
            gameOverText.setVisible(true);
            startText.setVisible(false);
            gameStarted = false;
        } else {
            startText.setText("Press SPACE to Continue");
            startText.setVisible(true);
            resetBall();
        }
    }

    private void playSound(String sound) {
        // In a real implementation, you would play actual sound effects here
        // For simplicity, we'll just print to console
        System.out.println("Playing sound: " + sound);
    }

    public static void main(String[] args) {
        launch(args);
    }
}