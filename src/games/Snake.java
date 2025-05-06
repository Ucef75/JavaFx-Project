package games;

import ui.MainMenu;  // Import the main menu class
import globalFunc.Sound_Func;  // Import the sound functions
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
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
    private Pane root = new Pane();
    private Timeline timeline;
    private MainMenu mainMenu;
    private boolean gamePaused = false;
    private Timeline timeline;

    @Override
    public void start(Stage primaryStage) {
        initGame();
        Scene gameScene = new Scene(root, WIDTH, HEIGHT);
        
        // Initialize menu with resume callback
        mainMenu = new MainMenu(primaryStage, gameScene, () -> {
            gamePaused = false;
            timeline.play();
            Sound_Func.playBackground(); // If you have background music
        });
        
        // Set up key controls
        gameScene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                toggleGamePause();
            } else if (!gamePaused) {
                // Original movement handlers
                if (e.getCode() == KeyCode.UP && direction != Direction.DOWN) direction = Direction.UP;
                if (e.getCode() == KeyCode.DOWN && direction != Direction.UP) direction = Direction.DOWN;
                if (e.getCode() == KeyCode.LEFT && direction != Direction.RIGHT) direction = Direction.LEFT;
                if (e.getCode() == KeyCode.RIGHT && direction != Direction.LEFT) direction = Direction.RIGHT;
            }
        });
        
        // Game loop
        timeline = new Timeline(new KeyFrame(Duration.millis(150), e -> {
            if (running && !gamePaused) {
                move();
                checkCollision();
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        
        primaryStage.setTitle("Snake Game");
        primaryStage.setScene(gameScene);
        primaryStage.show();
    }

    private void toggleGamePause() {
        gamePaused = !gamePaused;
        if (gamePaused) {
            timeline.pause();
            Sound_Func.playBackground(); // If you have background music
            mainMenu.showMenu();
        } else {
            mainMenu.hideMenu();
        }
    }


    private void initGame() {
        // Remove individual sound loading since we're using Sound_Func
        // Initialize snake
        Rectangle head = new Rectangle(TILE_SIZE, TILE_SIZE);
        head.setFill(Color.GREEN);
        head.setX(WIDTH / 2);
        head.setY(HEIGHT / 2);
        snake.add(head);
        root.getChildren().add(head);

        spawnFood();
    }

    private void spawnFood() {
        food = new Rectangle(TILE_SIZE, TILE_SIZE);
        food.setFill(Color.RED);
        food.setX(random.nextInt(WIDTH / TILE_SIZE) * TILE_SIZE);
        food.setY(random.nextInt(HEIGHT / TILE_SIZE) * TILE_SIZE);
        root.getChildren().add(food);
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
        newHead.setFill(Color.GREEN);
        newHead.setX(newX);
        newHead.setY(newY);

        snake.addFirst(newHead);
        root.getChildren().add(newHead);

        if (newHead.getX() == food.getX() && newHead.getY() == food.getY()) {
            root.getChildren().remove(food);
            spawnFood();
            Sound_Func.playEatingSound();  // Use centralized sound function
        } else {
            Rectangle tail = snake.removeLast();
            root.getChildren().remove(tail);
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
        Sound_Func.playDefeatSound();  // Use centralized sound function
        System.out.println("Game Over! Score: " + (snake.size() - 1));
    }

    public static void main(String[] args) {
        launch(args);
    }
}