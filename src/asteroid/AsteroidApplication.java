package asteroid;

import globalFunc.Sound_Func;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class AsteroidApplication extends Application {
    public static int WIDTH = 800;
    public static int HEIGHT = 800;
    private boolean gameOver = false;

    @Override
    public void start(Stage stage) throws Exception {
        // Start background music (will loop indefinitely)
        Sound_Func.playMainMusic();
        
        Pane pane = new Pane();
        pane.setPrefSize(WIDTH, HEIGHT);
        
        // Load background image
        Image backgroundImage = new Image(getClass().getResourceAsStream("bg.jpg"));     
        if (backgroundImage.isError()) {
            System.out.println("Error loading background image.");
        } else {
            System.out.println("Background image loaded successfully.");
        }

        ImageView backgroundImageView = new ImageView(backgroundImage);
        backgroundImageView.setFitWidth(WIDTH);
        backgroundImageView.setFitHeight(HEIGHT);
        pane.getChildren().add(backgroundImageView);

        // Score display
        Text text = new Text(WIDTH / 2.2, 50, "Score: 0");
        text.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 30));
        text.setFill(javafx.scene.paint.Color.YELLOW);
        text.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        AtomicInteger points = new AtomicInteger();
        pane.getChildren().add(text);

        // Create ship and asteroids
        Ship triangleShip = new Ship(WIDTH / 2, HEIGHT / 2);
        List<Asteroid> asteroids = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            Random rnd = new Random();
            Asteroid asteroid = new Asteroid(rnd.nextInt(WIDTH / 3), rnd.nextInt(HEIGHT));
            asteroids.add(asteroid);
        }

        asteroids.forEach(asteroid -> pane.getChildren().add(asteroid.getCharacter()));
        pane.getChildren().add(triangleShip.getCharacter());

        ArrayList<Projectiles> projectiles = new ArrayList<>();
        Scene scene = new Scene(pane);

        // Handle window close event to stop music
        stage.setOnCloseRequest(event -> {
            Sound_Func.stopBackgroundMusic();
        });

        // Keyboard input handling
        HashMap<KeyCode, Boolean> keypressed = new HashMap<>();
        scene.setOnKeyPressed(event -> keypressed.put(event.getCode(), Boolean.TRUE));
        scene.setOnKeyReleased(event -> keypressed.put(event.getCode(), Boolean.FALSE));

        // Main game loop
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (gameOver) return;

                // Spawn new asteroids randomly
                if (Math.random() < 0.005) {
                    Asteroid asteroid = new Asteroid(WIDTH, HEIGHT);
                    if (!asteroid.collide(triangleShip)) {
                        asteroids.add(asteroid);
                        pane.getChildren().add(asteroid.getCharacter());
                    }
                }

                // Handle ship controls
                if (keypressed.getOrDefault(KeyCode.LEFT, false)) {
                    triangleShip.turnLeft();
                }
                if (keypressed.getOrDefault(KeyCode.RIGHT, false)) {
                    triangleShip.turnRight();
                }
                if (keypressed.getOrDefault(KeyCode.UP, false)) {
                    triangleShip.accelerate();
                }
                if (keypressed.getOrDefault(KeyCode.DOWN, false)) {
                    triangleShip.deaccelerate();
                }

                // Shooting projectiles
                if (keypressed.getOrDefault(KeyCode.SPACE, false) && projectiles.size() < 3) {
                    Projectiles projectile = new Projectiles(
                        (int) triangleShip.getCharacter().getTranslateX(), 
                        (int) triangleShip.getCharacter().getTranslateY()
                    );
                    projectile.getCharacter().setRotate(triangleShip.getCharacter().getRotate());
                    projectiles.add(projectile);
                    projectile.accelerate();
                    projectile.setMovement(projectile.getMovement().normalize().multiply(3));
                    pane.getChildren().add(projectile.getCharacter());
                }

                // Move all game objects
                triangleShip.move();
                asteroids.forEach(asteroid -> asteroid.move());
                projectiles.forEach(projectile -> projectile.move());

                // Check for collisions
                asteroids.forEach(asteroid -> {
                    if (triangleShip.collide(asteroid)) {
                        gameOver = true;
                        Sound_Func.stopBackgroundMusic();
                        Sound_Func.playDefeatSound();
                        stop();
                    }
                });

                // Check projectile hits
                projectiles.forEach(projectile -> {
                    asteroids.forEach(asteroid -> {
                        if (projectile.collide(asteroid)) {
                            projectile.setAlive(false);
                            asteroid.setAlive(false);
                        }
                    });
                    if (!projectile.isAlive()) {
                        text.setText("Points: " + points.addAndGet(1));
                    }
                });

                // Clean up dead projectiles and asteroids
                projectiles.stream()
                    .filter(projectile -> !projectile.isAlive())
                    .forEach(projectile -> pane.getChildren().remove(projectile.getCharacter()));
                projectiles.removeAll(projectiles.stream()
                    .filter(projectile -> !projectile.isAlive())
                    .collect(Collectors.toList()));

                asteroids.stream()
                    .filter(asteroid -> !asteroid.isAlive())
                    .forEach(asteroid -> pane.getChildren().remove(asteroid.getCharacter()));
                asteroids.removeAll(asteroids.stream()
                    .filter(asteroid -> !asteroid.isAlive())
                    .collect(Collectors.toList()));
            }
        }.start();

        stage.setTitle("Asteroid Game");
        stage.setScene(scene);
        stage.show();
    }
}