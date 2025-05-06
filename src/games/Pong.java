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

public class Pong extends Application {

    private static final int WIDTH = 600;
    private static final int HEIGHT = 400;
    private Rectangle player;
    private Rectangle ai;
    private Circle ball;
    private int ballSpeedX = 3;
    private int ballSpeedY = 3;
    private int score = 0;
    private Text scoreText;

    @Override
    public void start(Stage primaryStage) {
        Pane root = new Pane();
        Scene scene = new Scene(root, WIDTH, HEIGHT);

        player = new Rectangle(10, 80, Color.BLUE);
        player.setLayoutX(20);
        player.setLayoutY(HEIGHT / 2 - 40);

        ai = new Rectangle(10, 80, Color.RED);
        ai.setLayoutX(WIDTH - 30);
        ai.setLayoutY(HEIGHT / 2 - 40);

        ball = new Circle(10, Color.BLACK);
        ball.setLayoutX(WIDTH / 2);
        ball.setLayoutY(HEIGHT / 2);

        scoreText = new Text(10, 20, "Score: 0");

        root.getChildren().addAll(player, ai, ball, scoreText);

        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.UP) {
                player.setLayoutY(player.getLayoutY() - 20);
            } else if (e.getCode() == KeyCode.DOWN) {
                player.setLayoutY(player.getLayoutY() + 20);
            }
        });

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                ball.setLayoutX(ball.getLayoutX() + ballSpeedX);
                ball.setLayoutY(ball.getLayoutY() + ballSpeedY);

                // rebond sur haut/bas
                if (ball.getLayoutY() <= 0 || ball.getLayoutY() >= HEIGHT) {
                    ballSpeedY *= -1;
                }

                // IA suit la balle
                if (ball.getLayoutY() > ai.getLayoutY() + 40) {
                    ai.setLayoutY(ai.getLayoutY() + 3);
                } else if (ball.getLayoutY() < ai.getLayoutY() + 40) {
                    ai.setLayoutY(ai.getLayoutY() - 3);
                }

                // collision joueur
                if (ball.getBoundsInParent().intersects(player.getBoundsInParent())) {
                    ballSpeedX *= -1;
                    score++;
                    scoreText.setText("Score: " + score);
                }

                // collision IA
                if (ball.getBoundsInParent().intersects(ai.getBoundsInParent())) {
                    ballSpeedX *= -1;
                }

                // perdu
                if (ball.getLayoutX() < 0) {
                    stop();
                    scoreText.setText("Game Over ! Score: " + score);
                }
            }
        };
        timer.start();

        primaryStage.setTitle("Pong Game");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
