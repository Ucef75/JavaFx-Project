package games;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Random;

public class Catch extends Application {
    private static final int WIDTH = 400;
    private static final int HEIGHT = 600;
    private static final int PADDLE_WIDTH = 80;
    private static final int PADDLE_HEIGHT = 10;
    private static final int CIRCLE_RADIUS = 10;

    private Rectangle paddle;
    private Circle fallingCircle;
    private int score = 0;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Pane root = new Pane();

        // Create paddle
        paddle = new Rectangle(WIDTH / 2 - PADDLE_WIDTH / 2, HEIGHT - 40, PADDLE_WIDTH, PADDLE_HEIGHT);
        paddle.setFill(Color.BLUE);

        // Create falling circle
        fallingCircle = new Circle(CIRCLE_RADIUS);
        fallingCircle.setFill(Color.RED);
        resetFallingCircle();

        root.getChildren().addAll(paddle, fallingCircle);

        Scene scene = new Scene(root, WIDTH, HEIGHT);

        // Paddle movement
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.LEFT) {
                paddle.setX(Math.max(0, paddle.getX() - 20));
            } else if (event.getCode() == KeyCode.RIGHT) {
                paddle.setX(Math.min(WIDTH - PADDLE_WIDTH, paddle.getX() + 20));
            }
        });

        // Timeline for animation
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(20), e -> updateGame()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        primaryStage.setTitle("Catch the Falling Objects");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void updateGame() {
        // Move the falling circle down
        fallingCircle.setCenterY(fallingCircle.getCenterY() + 5);

        // Check if the circle hits the paddle
        if (fallingCircle.getBoundsInParent().intersects(paddle.getBoundsInParent())) {
            score++;
            System.out.println("Score: " + score);
            resetFallingCircle();
        }

        // Check if the circle falls off the screen
        if (fallingCircle.getCenterY() > HEIGHT) {
            System.out.println("Game Over! Final Score: " + score);
            System.exit(0);
        }
    }

    private void resetFallingCircle() {
        Random random = new Random();
        double x = random.nextDouble() * (WIDTH - 2 * CIRCLE_RADIUS) + CIRCLE_RADIUS;
        fallingCircle.setCenterX(x);
        fallingCircle.setCenterY(0);
    }
}