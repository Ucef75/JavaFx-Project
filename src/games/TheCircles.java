package games;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class TheCircles extends Application {

    private static final int WIDTH = 400;
    private static final int HEIGHT = 600;
    private Pane root;
    private List<Circle> circles = new ArrayList<>();
    private Random random = new Random();
    private int score = 0;
    private Text scoreText;
    private boolean gameOver = false;

    @Override
    public void start(Stage primaryStage) {
        root = new Pane();
        Scene scene = new Scene(root, WIDTH, HEIGHT);

        scoreText = new Text(10, 20, "Score: 0");
        root.getChildren().add(scoreText);

        scene.setOnMouseClicked(this::handleClick);

        AnimationTimer timer = new AnimationTimer() {
            private long lastSpawn = 0;

            @Override
            public void handle(long now) {
                if (!gameOver) {
                    if (now - lastSpawn > 800_000_000) { // toutes les 0.8 sec
                        spawnCircle();
                        lastSpawn = now;
                    }
                    updateCircles();
                }
            }
        };
        timer.start();

        primaryStage.setTitle("Catch The Circles");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void spawnCircle() {
        Circle circle = new Circle(20, Color.color(random.nextDouble(), random.nextDouble(), random.nextDouble()));
        circle.setCenterX(random.nextInt(WIDTH - 40) + 20);
        circle.setCenterY(0);
        circles.add(circle);
        root.getChildren().add(circle);
    }

    private void updateCircles() {
        Iterator<Circle> it = circles.iterator();
        while (it.hasNext()) {
            Circle circle = it.next();
            circle.setCenterY(circle.getCenterY() + 3);

            if (circle.getCenterY() > HEIGHT) {
                endGame();
                return;
            }
        }
    }

    private void handleClick(MouseEvent event) {
        Iterator<Circle> it = circles.iterator();
        while (it.hasNext()) {
            Circle circle = it.next();
            if (circle.contains(event.getX(), event.getY())) {
                root.getChildren().remove(circle);
                it.remove();
                score++;
                scoreText.setText("Score: " + score);
                break;
            }
        }
    }

    private void endGame() {
        gameOver = true;
        Text gameOverText = new Text(WIDTH/2 - 50, HEIGHT/2, "Game Over !");
        gameOverText.setFill(Color.RED);
        gameOverText.setStyle("-fx-font-size: 24;");
        root.getChildren().add(gameOverText);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
