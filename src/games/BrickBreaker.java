package games;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.List;

public class BrickBreaker extends Application {

    private static final int WIDTH = 500;
    private static final int HEIGHT = 600;
    private Rectangle paddle;
    private Circle ball;
    private List<Rectangle> bricks = new ArrayList<>();
    private int ballSpeedX = 3;
    private int ballSpeedY = -3;
    private int score = 0;
    private Text scoreText;

    @Override
    public void start(Stage primaryStage) {
        Pane root = new Pane();
        Scene scene = new Scene(root, WIDTH, HEIGHT);

        paddle = new Rectangle(100, 10, Color.BLUE);
        paddle.setLayoutX(WIDTH / 2 - 50);
        paddle.setLayoutY(HEIGHT - 30);

        ball = new Circle(8, Color.BLACK);
        ball.setLayoutX(WIDTH / 2);
        ball.setLayoutY(HEIGHT - 50);

        scoreText = new Text(10, 20, "Score: 0");

        // Créer des briques
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 8; j++) {
                Rectangle brick = new Rectangle(50, 20, Color.color(Math.random(), Math.random(), Math.random()));
                brick.setLayoutX(j * 60 + 10);
                brick.setLayoutY(i * 30 + 40);
                bricks.add(brick);
                root.getChildren().add(brick);
            }
        }

        root.getChildren().addAll(paddle, ball, scoreText);

        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.LEFT) {
                paddle.setLayoutX(paddle.getLayoutX() - 20);
            } else if (e.getCode() == KeyCode.RIGHT) {
                paddle.setLayoutX(paddle.getLayoutX() + 20);
            }
        });

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                ball.setLayoutX(ball.getLayoutX() + ballSpeedX);
                ball.setLayoutY(ball.getLayoutY() + ballSpeedY);

                // rebond murs
                if (ball.getLayoutX() <= 0 || ball.getLayoutX() >= WIDTH) {
                    ballSpeedX *= -1;
                }
                if (ball.getLayoutY() <= 0) {
                    ballSpeedY *= -1;
                }

                // rebond paddle
                if (ball.getBoundsInParent().intersects(paddle.getBoundsInParent())) {
                    ballSpeedY *= -1;
                }

                // toucher briques
                for (Rectangle brick : new ArrayList<>(bricks)) {
                    if (ball.getBoundsInParent().intersects(brick.getBoundsInParent())) {
                        ballSpeedY *= -1;
                        root.getChildren().remove(brick);
                        bricks.remove(brick);
                        score += 10;
                        scoreText.setText("Score: " + score);
                        break;
                    }
                }

                // perdu
                if (ball.getLayoutY() > HEIGHT) {
                    stop();
                    scoreText.setText("Game Over ! Score: " + score);
                }
            }
        };
        timer.start();

        primaryStage.setTitle("Brick Breaker");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
