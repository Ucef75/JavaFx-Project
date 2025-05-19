package games;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.*;

public class TheCircles extends Application {

    private static final int WIDTH = 400;
    private static final int HEIGHT = 600;
    private Pane gamePane;
    private List<Circle> circles = new ArrayList<>();
    private Random random = new Random();
    private int score = 0;
    private int highScore = 0;
    private Text scoreText;
    private Text highScoreText;
    private boolean gameOver = false;
    private AnimationTimer timer;

    @Override
    public void start(Stage primaryStage) {
        VBox mainLayout = new VBox();
        mainLayout.setAlignment(Pos.TOP_CENTER);

        scoreText = new Text("Score: 0");
        scoreText.setFont(Font.font(18));
        highScoreText = new Text("High Score: 0");
        highScoreText.setFont(Font.font(18));

        gamePane = new Pane();
        gamePane.setPrefSize(WIDTH, HEIGHT);

        mainLayout.getChildren().addAll(new VBox(5, scoreText, highScoreText), gamePane);

        Scene scene = new Scene(mainLayout, WIDTH, HEIGHT + 40);

        gamePane.setOnMouseClicked(this::handleClick);

        timer = new AnimationTimer() {
            private long lastSpawn = 0;

            @Override
            public void handle(long now) {
                if (!gameOver) {
                    if (now - lastSpawn > 700_000_000) { // spawn faster for more fun
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
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    private void spawnCircle() {
        int radius = random.nextInt(10) + 20; // randomize radius for challenge
        Circle circle = new Circle(radius, Color.color(random.nextDouble(), random.nextDouble(), random.nextDouble()));
        circle.setCenterX(random.nextInt(WIDTH - 2 * radius) + radius);
        circle.setCenterY(0);
        circles.add(circle);
        gamePane.getChildren().add(circle);
    }

    private void updateCircles() {
        Iterator<Circle> it = circles.iterator();
        while (it.hasNext()) {
            Circle circle = it.next();
            circle.setCenterY(circle.getCenterY() + (3 + (circle.getRadius() / 10.0))); // larger circles fall faster

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
                gamePane.getChildren().remove(circle);
                it.remove();
                score++;
                scoreText.setText("Score: " + score);
                break;
            }
        }
    }

    private void endGame() {
        gameOver = true;
        timer.stop();
        if (score > highScore) {
            highScore = score;
        }
        highScoreText.setText("High Score: " + highScore);

        Text gameOverText = new Text(WIDTH / 2.0 - 70, HEIGHT / 2.0, "Game Over!");
        gameOverText.setFill(Color.RED);
        gameOverText.setFont(Font.font(28));
        gamePane.getChildren().add(gameOverText);

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Your score: " + score + "\nHigh Score: " + highScore + "\nPlay Again?", ButtonType.YES, ButtonType.NO);
        alert.setHeaderText("Game Over!");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            resetGame();
        }
    }

    private void resetGame() {
        gamePane.getChildren().clear();
        circles.clear();
        score = 0;
        scoreText.setText("Score: 0");
        gameOver = false;
        timer.start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}