package games;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
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

    private Circle bird;
    private double velocity = 0;
    private double gravity = 0.5;
    private double jumpStrength = -8;

    private List<Rectangle> pipes = new ArrayList<>();
    private Pane root = new Pane();
    private Random random = new Random();
    private boolean gameOver = false;

    @Override
    public void start(Stage primaryStage) {
        bird = new Circle(BIRD_RADIUS);
        bird.setFill(Color.YELLOW);
        bird.setCenterX(WIDTH / 4);
        bird.setCenterY(HEIGHT / 2);
        root.getChildren().add(bird);

        Scene scene = new Scene(root, WIDTH, HEIGHT);

        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.SPACE && !gameOver) {
                velocity = jumpStrength;
            }
        });

        AnimationTimer timer = new AnimationTimer() {
            private long lastPipeTime = 0;

            @Override
            public void handle(long now) {
                if (!gameOver) {
                    updateBird();
                    updatePipes();
                    checkCollisions();

                    if (now - lastPipeTime > 2000000000) { // toutes les 2 secondes
                        spawnPipes();
                        lastPipeTime = now;
                    }
                }
            }
        };
        timer.start();

        primaryStage.setTitle("Flappy Bird");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void updateBird() {
        velocity += gravity;
        bird.setCenterY(bird.getCenterY() + velocity);
    }

    private void spawnPipes() {
        int pipeHeight = random.nextInt(HEIGHT - PIPE_GAP - 100) + 50;
        Rectangle topPipe = new Rectangle(PIPE_WIDTH, pipeHeight);
        topPipe.setX(WIDTH);
        topPipe.setY(0);
        topPipe.setFill(Color.GREEN);

        Rectangle bottomPipe = new Rectangle(PIPE_WIDTH, HEIGHT - pipeHeight - PIPE_GAP);
        bottomPipe.setX(WIDTH);
        bottomPipe.setY(pipeHeight + PIPE_GAP);
        bottomPipe.setFill(Color.GREEN);

        pipes.add(topPipe);
        pipes.add(bottomPipe);
        root.getChildren().addAll(topPipe, bottomPipe);
    }

    private void updatePipes() {
        Iterator<Rectangle> it = pipes.iterator();
        while (it.hasNext()) {
            Rectangle pipe = it.next();
            pipe.setX(pipe.getX() - 3);

            if (pipe.getX() + PIPE_WIDTH < 0) {
                root.getChildren().remove(pipe);
                it.remove();
            }
        }
    }

    private void checkCollisions() {
        // Collision avec sol ou plafond
        if (bird.getCenterY() < 0 || bird.getCenterY() > HEIGHT) {
            endGame();
        }

        // Collision avec tuyaux
        for (Rectangle pipe : pipes) {
            if (bird.getBoundsInParent().intersects(pipe.getBoundsInParent())) {
                endGame();
            }
        }
    }

    private void endGame() {
        System.out.println("Game Over !");
        gameOver = true;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
