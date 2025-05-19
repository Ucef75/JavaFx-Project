package games;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class Catch extends Application {
    // Game constants
    private static final int WIDTH = 800;
    private static final int HEIGHT = 700;
    private static final int PADDLE_WIDTH = 100;
    private static final int PADDLE_HEIGHT = 15;
    private static final int BASE_CIRCLE_RADIUS = 15;

    // Game state
    private Rectangle paddle;
    private List<FallingObject> fallingObjects = new ArrayList<>();
    private List<PowerUp> activePowerUps = new ArrayList<>();
    private int score = 0;
    private int lives = 3;
    private int level = 1;
    private int objectsCaught = 0;
    private int objectsRequired;
    private double gameSpeed = 1.0;
    private boolean isPaused = false;
    private boolean gameOver = false;
    private boolean gameWon = false;
    private Timeline gameLoop;
    private Random random = new Random();
    private double mouseX;
    private long lastObjectTime = 0;
    private boolean paddleGrowthActive = false;
    private boolean slowMotionActive = false;
    private long powerUpEndTime = 0;

    // Stats
    private int totalObjectsCaught = 0;
    private int totalObjectsMissed = 0;
    private int highScore = 0;

    // Visual effects
    private List<Particle> particles = new ArrayList<>();
    private List<ScorePopup> scorePopups = new ArrayList<>();
    private double backgroundHue = 0;

    // UI elements
    private Pane gamePane;
    private Text scoreText;
    private Text livesText;
    private Text levelText;
    private Text messageText;
    private ProgressBar levelProgress;
    private StackPane mainPane;
    private Pane gameArea;

    // Background
    private Canvas backgroundCanvas;
    private GraphicsContext bgContext;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // Create main layout
        mainPane = new StackPane();

        // Create menus
        Pane mainMenu = createMainMenu();
        Pane gameOverMenu = createGameOverMenu();
        Pane pauseMenu = createPauseMenu();

        // Create game pane
        gamePane = createGamePane();

        // Add all layers
        mainPane.getChildren().addAll(gamePane, mainMenu, gameOverMenu, pauseMenu);

        // Show only main menu initially
        gamePane.setVisible(false);
        mainMenu.setVisible(true);
        gameOverMenu.setVisible(false);
        pauseMenu.setVisible(false);

        // Create scene
        Scene scene = new Scene(mainPane, WIDTH, HEIGHT);

        // Set up input handlers
        setupInputHandlers(scene);

        // Set up window
        primaryStage.setTitle("Catch the Falling Objects");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    private Pane createGamePane() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: black;");

        // Create background animation
        backgroundCanvas = new Canvas(WIDTH, HEIGHT);
        bgContext = backgroundCanvas.getGraphicsContext2D();

        // Create game area
        gameArea = new Pane();

        // Create paddle
        paddle = new Rectangle(WIDTH / 2 - PADDLE_WIDTH / 2, HEIGHT - 60, PADDLE_WIDTH, PADDLE_HEIGHT);
        paddle.setArcWidth(15);
        paddle.setArcHeight(15);

        // Apply gradient and effect to paddle
        LinearGradient paddleGradient = new LinearGradient(
                0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.AQUA),
                new Stop(0.5, Color.BLUE),
                new Stop(1, Color.DARKBLUE)
        );
        paddle.setFill(paddleGradient);

        DropShadow paddleShadow = new DropShadow();
        paddleShadow.setColor(Color.AQUA);
        paddleShadow.setRadius(10);
        paddle.setEffect(paddleShadow);

        // Create UI elements
        scoreText = new Text("Score: 0");
        scoreText.setFill(Color.WHITE);
        scoreText.setFont(Font.font("Arial", FontWeight.BOLD, 20));

        livesText = new Text("Lives: 3");
        livesText.setFill(Color.WHITE);
        livesText.setFont(Font.font("Arial", FontWeight.BOLD, 20));

        levelText = new Text("Level: 1");
        levelText.setFill(Color.WHITE);
        levelText.setFont(Font.font("Arial", FontWeight.BOLD, 20));

        levelProgress = new ProgressBar(0, 200);

        messageText = new Text("");
        messageText.setFill(Color.GOLD);
        messageText.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        messageText.setTextAlignment(TextAlignment.CENTER);
        messageText.setTranslateY(HEIGHT / 3);

        // Create top bar
        HBox topBar = new HBox(20);
        topBar.setPadding(new Insets(10));
        topBar.setAlignment(Pos.CENTER);
        topBar.getChildren().addAll(scoreText, levelProgress, livesText, levelText);

        // Add components to root
        root.getChildren().add(backgroundCanvas);
        root.setTop(topBar);
        root.setCenter(gameArea);
        gameArea.getChildren().add(paddle);
        gameArea.getChildren().add(messageText);
        return root;
    }

    private Pane createMainMenu() {
        StackPane menuPane = new StackPane();
        menuPane.setStyle("-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #000428, #004e92);");

        VBox menuBox = new VBox(20);
        menuBox.setAlignment(Pos.CENTER);
        menuBox.setPadding(new Insets(20));

        Text title = new Text("CATCH");
        title.setFont(Font.font("Impact", FontWeight.BOLD, 72));
        title.setFill(Color.WHITE);

        DropShadow titleGlow = new DropShadow();
        titleGlow.setColor(Color.AQUA);
        titleGlow.setRadius(20);
        title.setEffect(titleGlow);

        Text subtitle = new Text("The Ultimate Falling Object Challenge");
        subtitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        subtitle.setFill(Color.LIGHTGRAY);

        Button playButton = createMenuButton("Play Game", 200, 50);
        playButton.setOnAction(e -> startNewGame());

        Button instructionsButton = createMenuButton("Instructions", 200, 50);
        instructionsButton.setOnAction(e -> showInstructions());

        Button exitButton = createMenuButton("Exit Game", 200, 50);
        exitButton.setOnAction(e -> System.exit(0));

        menuBox.getChildren().addAll(title, subtitle, playButton, instructionsButton, exitButton);
        menuPane.getChildren().add(menuBox);

        return menuPane;
    }

    private Pane createGameOverMenu() {
        StackPane menuPane = new StackPane();
        menuPane.setVisible(false);
        menuPane.setStyle("-fx-background-color: rgba(0, 0, 0, 0.8);");

        VBox menuBox = new VBox(20);
        menuBox.setAlignment(Pos.CENTER);
        menuBox.setPadding(new Insets(20));

        Text title = new Text("GAME OVER");
        title.setFont(Font.font("Impact", FontWeight.BOLD, 64));
        title.setFill(Color.RED);

        Text finalScoreText = new Text("Score: 0");
        finalScoreText.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        finalScoreText.setFill(Color.WHITE);

        Text statsText = new Text("Objects Caught: 0\nObjects Missed: 0");
        statsText.setFont(Font.font("Arial", 18));
        statsText.setFill(Color.LIGHTGRAY);
        statsText.setTextAlignment(TextAlignment.CENTER);

        Button tryAgainButton = createMenuButton("Try Again", 200, 50);
        tryAgainButton.setOnAction(e -> startNewGame());

        Button mainMenuButton = createMenuButton("Main Menu", 200, 50);
        mainMenuButton.setOnAction(e -> showMainMenu());

        menuBox.getChildren().addAll(title, finalScoreText, statsText, tryAgainButton, mainMenuButton);
        menuPane.getChildren().add(menuBox);

        return menuPane;
    }

    private Pane createPauseMenu() {
        StackPane menuPane = new StackPane();
        menuPane.setVisible(false);
        menuPane.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");

        VBox menuBox = new VBox(20);
        menuBox.setAlignment(Pos.CENTER);
        menuBox.setPadding(new Insets(20));

        Text title = new Text("GAME PAUSED");
        title.setFont(Font.font("Impact", FontWeight.BOLD, 48));
        title.setFill(Color.YELLOW);

        Button resumeButton = createMenuButton("Resume", 200, 50);
        resumeButton.setOnAction(e -> resumeGame());

        Button restartButton = createMenuButton("Restart", 200, 50);
        restartButton.setOnAction(e -> startNewGame());

        Button mainMenuButton = createMenuButton("Main Menu", 200, 50);
        mainMenuButton.setOnAction(e -> showMainMenu());

        menuBox.getChildren().addAll(title, resumeButton, restartButton, mainMenuButton);
        menuPane.getChildren().add(menuBox);

        return menuPane;
    }

    private Button createMenuButton(String text, double width, double height) {
        Button button = new Button(text);
        button.setPrefSize(width, height);
        button.setStyle("-fx-background-color: linear-gradient(#0099ff, #0066cc); " +
                "-fx-background-radius: 30; " +
                "-fx-text-fill: white; " +
                "-fx-font-size: 18px; " +
                "-fx-font-weight: bold;");

        // Button hover effect
        button.setOnMouseEntered(e -> {
            button.setStyle("-fx-background-color: linear-gradient(#00ccff, #0099ff); " +
                    "-fx-background-radius: 30; " +
                    "-fx-text-fill: white; " +
                    "-fx-font-size: 18px; " +
                    "-fx-font-weight: bold; " +
                    "-fx-effect: dropshadow(three-pass-box, rgba(0,200,255,0.8), 10, 0, 0, 0);");
        });

        button.setOnMouseExited(e -> {
            button.setStyle("-fx-background-color: linear-gradient(#0099ff, #0066cc); " +
                    "-fx-background-radius: 30; " +
                    "-fx-text-fill: white; " +
                    "-fx-font-size: 18px; " +
                    "-fx-font-weight: bold;");
        });

        return button;
    }

    private void setupInputHandlers(Scene scene) {
        // Keyboard controls
        scene.setOnKeyPressed(event -> {
            if (gameOver || !gamePane.isVisible()) return;

            if (event.getCode() == KeyCode.LEFT || event.getCode() == KeyCode.A) {
                movePaddle(-20);
            } else if (event.getCode() == KeyCode.RIGHT || event.getCode() == KeyCode.D) {
                movePaddle(20);
            } else if (event.getCode() == KeyCode.SPACE) {
                activateSpecialAbility();
            } else if (event.getCode() == KeyCode.P) {
                togglePause();
            } else if (event.getCode() == KeyCode.ESCAPE) {
                togglePause();
            }
        });

        // Mouse control for paddle
        scene.setOnMouseMoved(event -> {
            if (!gameOver && gamePane.isVisible() && !isPaused) {
                mouseX = event.getX();
                if (mouseX >= PADDLE_WIDTH / 2 && mouseX <= WIDTH - PADDLE_WIDTH / 2) {
                    paddle.setX(mouseX - PADDLE_WIDTH / 2);
                }
            }
        });

        // Click to start game from main menu
        scene.setOnMouseClicked(event -> {
            if (!gamePane.isVisible() && !((StackPane)mainPane.getChildren().get(2)).isVisible()) {
                startNewGame();
            }
        });
    }

    private void startNewGame() {
        // Reset game state
        score = 0;
        lives = 3;
        level = 1;
        objectsCaught = 0;
        objectsRequired = 10;
        gameSpeed = 1.0;
        isPaused = false;
        gameOver = false;
        gameWon = false;
        totalObjectsCaught = 0;
        totalObjectsMissed = 0;
        paddleGrowthActive = false;
        slowMotionActive = false;

        // Reset paddle
        paddle.setWidth(PADDLE_WIDTH);
        paddle.setX(WIDTH / 2 - PADDLE_WIDTH / 2);

        // Clear objects
        fallingObjects.clear();
        particles.clear();
        scorePopups.clear();
        activePowerUps.clear();

        // Update UI
        updateUI();

        // Hide menus and show game
        mainPane.getChildren().get(1).setVisible(false); // Main menu
        mainPane.getChildren().get(2).setVisible(false); // Game over menu
        mainPane.getChildren().get(3).setVisible(false); // Pause menu
        gamePane.setVisible(true);

        // Start game loop
        if (gameLoop != null) {
            gameLoop.stop();
        }

        gameLoop = new Timeline(new KeyFrame(Duration.millis(16), e -> updateGame()));
        gameLoop.setCycleCount(Timeline.INDEFINITE);
        gameLoop.play();

        // Show level start message
        showMessage("Level 1", 2000);
    }

    private void updateGame() {
        if (isPaused || gameOver) return;

        // Update background
        updateBackground();

        // Check for power-up expiration
        checkPowerUpExpiration();

        // Generate new falling objects
        generateFallingObjects();

        // Update falling objects
        updateFallingObjects();

        // Render all objects in gameArea
        renderFallingObjects();

        // Update particles
        updateParticles();

        // Update score popups
        updateScorePopups();

        // Update active power-ups
        updatePowerUps();

        // Update UI
        updateUI();
    }

    private void renderFallingObjects() {
        // Clear any previous object shapes (except paddle and message text)
        for (int i = gameArea.getChildren().size() - 1; i >= 0; i--) {
            Node node = gameArea.getChildren().get(i);
            if (node != paddle && node != messageText && !(node instanceof Text)) {
                gameArea.getChildren().remove(i);
            }
        }

        // Render each falling object
        for (FallingObject obj : fallingObjects) {
            Circle circle;

            if (obj instanceof PowerUp) {
                PowerUp powerUp = (PowerUp) obj;

                // Create a special glow effect for power-ups
                circle = new Circle(obj.getX(), obj.getY(), obj.getRadius() * 1.2);
                circle.setFill(obj.getColor());

                Glow glow = new Glow(0.8);
                circle.setEffect(glow);

                // Add a small white circle in center
                Circle innerCircle = new Circle(obj.getX(), obj.getY(), obj.getRadius() * 0.4, Color.WHITE);
                gameArea.getChildren().add(innerCircle);
            } else if (obj instanceof Bomb) {
                // Create a bomb shape (black circle with details)
                circle = new Circle(obj.getX(), obj.getY(), obj.getRadius());
                circle.setFill(Color.BLACK);

                // Add a fuse line
                Line fuse = new Line(
                        obj.getX(), obj.getY() - obj.getRadius(),
                        obj.getX() + 5, obj.getY() - obj.getRadius() - 10
                );
                fuse.setStroke(Color.ORANGE);
                fuse.setStrokeWidth(2);
                gameArea.getChildren().add(fuse);

                // Add a spark at the end of the fuse
                Circle spark = new Circle(
                        obj.getX() + 5, obj.getY() - obj.getRadius() - 10,
                        2, Color.RED
                );
                gameArea.getChildren().add(spark);
            } else if (obj instanceof MovingObject) {
                // Create moving object with a trail effect
                circle = new Circle(obj.getX(), obj.getY(), obj.getRadius());
                circle.setFill(obj.getColor());

                // Add a trail
                Circle trail = new Circle(obj.getX(), obj.getY() - 10, obj.getRadius() * 0.7);
                trail.setFill(Color.color(
                        obj.getColor().getRed(),
                        obj.getColor().getGreen(),
                        obj.getColor().getBlue(),
                        0.3)
                );
                gameArea.getChildren().add(trail);
            } else {
                // Standard object
                circle = new Circle(obj.getX(), obj.getY(), obj.getRadius());
                circle.setFill(obj.getColor());
            }

            gameArea.getChildren().add(circle);
        }
    }

    private void updateBackground() {
        // Create a gradient background that slowly shifts colors
        backgroundHue = (backgroundHue + 0.1) % 360;
        Color color1 = Color.hsb(backgroundHue, 0.7, 0.2);
        Color color2 = Color.hsb((backgroundHue + 180) % 360, 0.5, 0.1);

        // Fill background with gradient
        bgContext.clearRect(0, 0, WIDTH, HEIGHT);
        bgContext.setFill(new LinearGradient(0, 0, 0, HEIGHT, false, CycleMethod.NO_CYCLE,
                new Stop(0, color1),
                new Stop(1, color2)));
        bgContext.fillRect(0, 0, WIDTH, HEIGHT);

        // Draw stars
        bgContext.setFill(Color.WHITE);
        for (int i = 0; i < 50; i++) {
            double x = random.nextDouble() * WIDTH;
            double y = random.nextDouble() * HEIGHT;
            double size = random.nextDouble() * 2 + 1;
            bgContext.fillOval(x, y, size, size);
        }
    }

    private void generateFallingObjects() {
        long currentTime = System.currentTimeMillis();

        // Generate objects based on level and time
        double spawnChance = 0.05 * gameSpeed;
        if (currentTime - lastObjectTime > 300 / gameSpeed) {
            if (random.nextDouble() < spawnChance) {
                createFallingObject();
                lastObjectTime = currentTime;
            }
        }
    }

    private void createFallingObject() {
        double x = random.nextDouble() * (WIDTH - 2 * BASE_CIRCLE_RADIUS) + BASE_CIRCLE_RADIUS;

        // Create different types of objects with varying probabilities
        double typeProbability = random.nextDouble();

        if (typeProbability < 0.05) {
            // Create a power-up (rare)
            PowerUpType type = PowerUpType.values()[random.nextInt(PowerUpType.values().length)];
            fallingObjects.add(new PowerUp(x, -BASE_CIRCLE_RADIUS, type));
        } else if (typeProbability < 0.15) {
            // Create a bomb (somewhat rare)
            fallingObjects.add(new Bomb(x, -BASE_CIRCLE_RADIUS, BASE_CIRCLE_RADIUS));
        } else if (typeProbability < 0.30 && level >= 3) {
            // Create a moving object (medium rare, only in higher levels)
            fallingObjects.add(new MovingObject(x, -BASE_CIRCLE_RADIUS, BASE_CIRCLE_RADIUS));
        } else {
            // Create a standard object
            int objectType = random.nextInt(3); // 0=regular, 1=bonus, 2=large
            int points = 0;
            double radius = BASE_CIRCLE_RADIUS;
            Color color = Color.RED;

            switch (objectType) {
                case 0: // Regular object
                    points = 10;
                    color = Color.RED;
                    break;
                case 1: // Bonus object
                    points = 20;
                    radius = BASE_CIRCLE_RADIUS * 0.8;
                    color = Color.GOLD;
                    break;
                case 2: // Large object
                    points = 5;
                    radius = BASE_CIRCLE_RADIUS * 1.5;
                    color = Color.ORANGERED;
                    break;
            }

            fallingObjects.add(new StandardObject(x, -radius, radius, points, color));
        }
    }

    private void updateFallingObjects() {
        Iterator<FallingObject> iterator = fallingObjects.iterator();
        while (iterator.hasNext()) {
            FallingObject obj = iterator.next();

            // Move the object
            obj.update(gameSpeed * (slowMotionActive ? 0.5 : 1.0));

            // Check if the object hits the paddle
            if (obj.intersects(paddle)) {
                handleObjectCaught(obj);
                iterator.remove();
                continue;
            }

            // Check if the object falls off the screen
            if (obj.getY() > HEIGHT) {
                if (obj.isCatchable() && !(obj instanceof Bomb)) {
                    handleObjectMissed();
                }
                iterator.remove();
            }
        }
    }

    private void updateParticles() {
        Iterator<Particle> iterator = particles.iterator();
        while (iterator.hasNext()) {
            Particle p = iterator.next();
            p.update();

            if (p.isDead()) {
                // Remove the particle's shape from the game area
                gameArea.getChildren().remove(p.circle);
                iterator.remove();
            } else {
                // Add particle to the game area if it's not already there
                if (!gameArea.getChildren().contains(p.circle)) {
                    gameArea.getChildren().add(p.circle);
                }
            }
        }
    }

    private void updateScorePopups() {
        Iterator<ScorePopup> iterator = scorePopups.iterator();
        while (iterator.hasNext()) {
            ScorePopup popup = iterator.next();
            popup.update();

            if (popup.isDead()) {
                gameArea.getChildren().remove(popup.text);
                iterator.remove();
            } else {
                // Add score popup to the game area if it's not already there
                if (!gameArea.getChildren().contains(popup.text)) {
                    gameArea.getChildren().add(popup.text);
                }
            }
        }
    }

    private void updatePowerUps() {
        Iterator<PowerUp> iterator = activePowerUps.iterator();
        while (iterator.hasNext()) {
            PowerUp powerUp = iterator.next();
            powerUp.updateEffect();

            if (powerUp.isExpired()) {
                deactivatePowerUp(powerUp.getType());
                iterator.remove();
            }
        }
    }

    private void checkPowerUpExpiration() {
        long currentTime = System.currentTimeMillis();
        if (currentTime > powerUpEndTime) {
            if (paddleGrowthActive) {
                paddle.setWidth(PADDLE_WIDTH);
                paddleGrowthActive = false;
            }

            if (slowMotionActive) {
                slowMotionActive = false;
            }
        }
    }

    private void handleObjectCaught(FallingObject obj) {
        if (obj instanceof Bomb) {
            // Caught a bomb - lose a life
            spawnExplosion(obj.getX(), obj.getY(), Color.ORANGE);
            lives--;

            if (lives <= 0) {
                endGame(false);
            } else {
                showMessage("-1 Life!", 1000);
            }
        } else if (obj instanceof PowerUp) {
            // Caught a power-up
            PowerUp powerUp = (PowerUp) obj;
            activatePowerUp(powerUp.getType());
            spawnParticles(obj.getX(), obj.getY(), 20, powerUp.getColor());

        } else {
            // Caught a normal object
            int points = obj.getPoints();
            score += points;
            objectsCaught++;
            totalObjectsCaught++;

            // Create score popup
            scorePopups.add(new ScorePopup("+" + points, obj.getX(), obj.getY()));

            // Create particle effect
            spawnParticles(obj.getX(), obj.getY(), 10, obj.getColor());

            // Check for level completion
            if (objectsCaught >= objectsRequired) {
                levelUp();
            }
        }
    }

    private void handleObjectMissed() {
        totalObjectsMissed++;

        if (!(slowMotionActive)) { // Don't lose lives during slow motion
            lives--;

            if (lives <= 0) {
                endGame(false);
            }
        }
    }

    private void levelUp() {
        level++;
        objectsCaught = 0;
        objectsRequired = 10 + level * 2;
        gameSpeed += 0.2;

        if (level > 10) {
            endGame(true);
            return;
        }

        // Bonus points for completing level
        score += level * 50;

        showMessage("Level " + level + "!", 2000);
    }

    private void activatePowerUp(PowerUpType type) {
        PowerUp powerUp = new PowerUp(0, 0, type);
        powerUp.setActivationTime(System.currentTimeMillis());
        activePowerUps.add(powerUp);

        switch (type) {
            case EXTRA_LIFE:
                lives = Math.min(lives + 1, 5);
                showMessage("+1 Life!", 1500);
                break;

            case SCORE_MULTIPLIER:
                score += 100;
                showMessage("+100 Points!", 1500);
                break;

            case PADDLE_GROWTH:
                paddle.setWidth(PADDLE_WIDTH * 2);
                paddleGrowthActive = true;
                powerUpEndTime = System.currentTimeMillis() + 5000; // 5 seconds
                showMessage("Paddle Growth! (5s)", 1500);
                break;

            case SLOW_MOTION:
                slowMotionActive = true;
                powerUpEndTime = System.currentTimeMillis() + 7000; // 7 seconds
                showMessage("Slow Motion! (7s)", 1500);
                break;

            case CLEAR_SCREEN:
                // Collect all catchable objects
                Iterator<FallingObject> iterator = fallingObjects.iterator();
                while (iterator.hasNext()) {
                    FallingObject obj = iterator.next();
                    if (obj.isCatchable() && !(obj instanceof Bomb)) {
                        score += obj.getPoints();
                        spawnParticles(obj.getX(), obj.getY(), 5, obj.getColor());
                        iterator.remove();
                    }
                }
                showMessage("Screen Cleared!", 1500);
                break;
        }
    }

    private void deactivatePowerUp(PowerUpType type) {
        switch (type) {
            case PADDLE_GROWTH:
                paddle.setWidth(PADDLE_WIDTH);
                paddleGrowthActive = false;
                break;

            case SLOW_MOTION:
                slowMotionActive = false;
                break;

            default:
                break;
        }
    }

    private void activateSpecialAbility() {
        // Only allow special ability when there are objects to catch
        if (!fallingObjects.isEmpty()) {
            // Attract all objects toward paddle
            for (FallingObject obj : fallingObjects) {
                if (obj instanceof Bomb) continue; // Don't attract bombs

                double moveSpeed = 10;
                double targetX = paddle.getX() + PADDLE_WIDTH / 2;
                double dx = targetX - obj.getX();
                double dy = (paddle.getY() - 30) - obj.getY();
                double distance = Math.sqrt(dx * dx + dy * dy);

                if (distance > 0) {
                    obj.setX(obj.getX() + dx / distance * moveSpeed);
                    obj.setY(obj.getY() + dy / distance * moveSpeed);
                }
            }

            // Visual effect
            spawnParticles(paddle.getX() + PADDLE_WIDTH / 2, paddle.getY(), 20, Color.CYAN);
        }
    }

    private void spawnParticles(double x, double y, int count, Color baseColor) {
        for (int i = 0; i < count; i++) {
            particles.add(new Particle(x, y, baseColor));
        }
    }

    private void spawnExplosion(double x, double y, Color color) {
        for (int i = 0; i < 50; i++) {
            particles.add(new Particle(x, y, color));
        }
    }

    private void updateUI() {
        scoreText.setText("Score: " + score);
        livesText.setText("Lives: " + lives);
        levelText.setText("Level: " + level);

        // Update level progress bar
        levelProgress.setProgress((double) objectsCaught / objectsRequired);
    }

    private void movePaddle(double dx) {
        double newX = paddle.getX() + dx;
        if (newX < 0) {
            paddle.setX(0);
        } else if (newX > WIDTH - paddle.getWidth()) {
            paddle.setX(WIDTH - paddle.getWidth());
        } else {
            paddle.setX(newX);
        }
    }

    private void showMessage(String message, long duration) {
        messageText.setText(message);
        messageText.setVisible(true);

        // Set up a timer to hide the message
        new Thread(() -> {
            try {
                Thread.sleep(duration);
                javafx.application.Platform.runLater(() -> messageText.setVisible(false));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void endGame(boolean victory) {
        gameOver = true;
        gameWon = victory;
        gameLoop.stop();

        if (victory) {
            showGameWonScreen();
        } else {
            showGameOverScreen();
        }

        // Update high score
        if (score > highScore) {
            highScore = score;
        }
    }

    private void showGameOverScreen() {
        // Get game over menu and update its texts
        StackPane gameOverMenu = (StackPane) mainPane.getChildren().get(2);
        VBox menuBox = (VBox) gameOverMenu.getChildren().get(0);

        Text finalScoreText = (Text) menuBox.getChildren().get(1);
        finalScoreText.setText("Final Score: " + score + "\nHigh Score: " + highScore);

        Text statsText = (Text) menuBox.getChildren().get(2);
        statsText.setText(
                "Level Reached: " + level + "\n" +
                        "Objects Caught: " + totalObjectsCaught + "\n" +
                        "Objects Missed: " + totalObjectsMissed
        );

        // Show the menu
        gameOverMenu.setVisible(true);
    }

    private void showGameWonScreen() {
        // Create a victory screen similar to game over
        StackPane victoryPane = new StackPane();
        victoryPane.setStyle("-fx-background-color: rgba(0, 0, 0, 0.8);");

        VBox menuBox = new VBox(20);
        menuBox.setAlignment(Pos.CENTER);
        menuBox.setPadding(new Insets(20));

        Text title = new Text("VICTORY!");
        title.setFont(Font.font("Impact", FontWeight.BOLD, 64));
        title.setFill(Color.GOLD);

        DropShadow glow = new DropShadow();
        glow.setColor(Color.YELLOW);
        glow.setRadius(20);
        title.setEffect(glow);

        Text scoreText = new Text("Final Score: " + score + "\nHigh Score: " + highScore);
        scoreText.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        scoreText.setFill(Color.WHITE);
        scoreText.setTextAlignment(TextAlignment.CENTER);

        Text statsText = new Text(
                "All 10 Levels Completed!\n" +
                        "Objects Caught: " + totalObjectsCaught + "\n" +
                        "Objects Missed: " + totalObjectsMissed
        );
        statsText.setFont(Font.font("Arial", 18));
        statsText.setFill(Color.LIGHTGRAY);
        statsText.setTextAlignment(TextAlignment.CENTER);

        Button playAgainButton = createMenuButton("Play Again", 200, 50);
        playAgainButton.setOnAction(e -> startNewGame());

        Button mainMenuButton = createMenuButton("Main Menu", 200, 50);
        mainMenuButton.setOnAction(e -> showMainMenu());

        menuBox.getChildren().addAll(title, scoreText, statsText, playAgainButton, mainMenuButton);
        victoryPane.getChildren().add(menuBox);

        // Add the victory screen to main pane and show it
        mainPane.getChildren().add(victoryPane);
    }

    private void togglePause() {
        isPaused = !isPaused;

        if (isPaused) {
            gameLoop.pause();
            mainPane.getChildren().get(3).setVisible(true); // Show pause menu
        } else {
            resumeGame();
        }
    }

    private void resumeGame() {
        if (isPaused) {
            isPaused = false;
            gameLoop.play();
            mainPane.getChildren().get(3).setVisible(false); // Hide pause menu
        }
    }

    private void showInstructions() {
        // Create instructions popup
        StackPane instructionsPane = new StackPane();
        instructionsPane.setStyle("-fx-background-color: rgba(0, 0, 0, 0.9);");

        VBox content = new VBox(15);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(20));

        Text title = new Text("HOW TO PLAY");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        title.setFill(Color.WHITE);

        Text instructions = new Text(
                "• Move the paddle with your MOUSE or LEFT/RIGHT ARROW KEYS\n" +
                        "• Catch falling objects to earn points\n" +
                        "• Avoid bombs (they cost you a life)\n" +
                        "• Collect power-ups for special abilities\n" +
                        "• Press SPACE to activate your special ability\n" +
                        "• Press P or ESC to pause the game\n\n" +
                        "POWER-UPS:\n" +
                        "• BLUE - Extra Life\n" +
                        "• GOLD - Score Bonus\n" +
                        "• GREEN - Larger Paddle\n" +
                        "• PURPLE - Slow Motion\n" +
                        "• WHITE - Clear Screen\n\n" +
                        "Complete all 10 levels to win!"
        );
        instructions.setFont(Font.font("Arial", 18));
        instructions.setFill(Color.LIGHTGRAY);
        instructions.setTextAlignment(TextAlignment.CENTER);

        Button backButton = createMenuButton("Back to Menu", 200, 50);
        backButton.setOnAction(e -> {
            mainPane.getChildren().remove(instructionsPane);
        });

        content.getChildren().addAll(title, instructions, backButton);
        instructionsPane.getChildren().add(content);

        mainPane.getChildren().add(instructionsPane);
    }

    private void showMainMenu() {
        // Stop the game loop if running
        if (gameLoop != null) {
            gameLoop.stop();
        }

        // Show main menu, hide others
        gamePane.setVisible(false);
        mainPane.getChildren().get(1).setVisible(true); // Main menu
        mainPane.getChildren().get(2).setVisible(false); // Game over menu
        mainPane.getChildren().get(3).setVisible(false); // Pause menu

        // Remove any victory screen if it exists
        if (mainPane.getChildren().size() > 4) {
            mainPane.getChildren().remove(4);
        }
    }

    // -------- INNER CLASSES --------

    private enum PowerUpType {
        EXTRA_LIFE,
        SCORE_MULTIPLIER,
        PADDLE_GROWTH,
        SLOW_MOTION,
        CLEAR_SCREEN
    }

    private abstract class FallingObject {
        protected double x, y;
        protected double radius;
        protected Color color;
        protected int points;

        public FallingObject(double x, double y, double radius) {
            this.x = x;
            this.y = y;
            this.radius = radius;
        }

        public abstract void update(double gameSpeed);
        public abstract void draw(GraphicsContext gc);

        public double getX() { return x; }
        public double getY() { return y; }
        public double getRadius() { return radius; }
        public Color getColor() { return color; }
        public int getPoints() { return points; }

        public void setX(double x) { this.x = x; }
        public void setY(double y) { this.y = y; }

        public boolean isCatchable() { return true; }

        public boolean intersects(Rectangle paddle) {
            // Check if circle intersects with rectangle
            double closestX = Math.max(paddle.getX(), Math.min(x, paddle.getX() + paddle.getWidth()));
            double closestY = Math.max(paddle.getY(), Math.min(y, paddle.getY() + paddle.getHeight()));

            double distanceX = x - closestX;
            double distanceY = y - closestY;

            return (distanceX * distanceX + distanceY * distanceY) < (radius * radius);
        }
    }

    private class StandardObject extends FallingObject {
        public StandardObject(double x, double y, double radius, int points, Color color) {
            super(x, y, radius);
            this.points = points;
            this.color = color;
        }

        @Override
        public void update(double gameSpeed) {
            y += 3 * gameSpeed;
        }

        @Override
        public void draw(GraphicsContext gc) {
            gc.setFill(color);
            gc.fillOval(x - radius, y - radius, radius * 2, radius * 2);
        }
    }

    private class MovingObject extends FallingObject {
        private double speedX;

        public MovingObject(double x, double y, double radius) {
            super(x, y, radius);
            this.points = 30; // Higher points for more difficulty
            this.color = Color.LIGHTGREEN;
            this.speedX = (random.nextDouble() * 4 - 2); // Random horizontal speed
        }

        @Override
        public void update(double gameSpeed) {
            y += 4 * gameSpeed;
            x += speedX * gameSpeed;

            // Bounce off walls
            if (x - radius < 0 || x + radius > WIDTH) {
                speedX = -speedX;
            }
        }

        @Override
        public void draw(GraphicsContext gc) {
            gc.setFill(color);
            gc.fillOval(x - radius, y - radius, radius * 2, radius * 2);

            // Add a trail effect
            gc.setGlobalAlpha(0.3);
            gc.fillOval(x - radius * 0.8, y - radius * 0.8 - 10, radius * 1.6, radius * 1.6);
            gc.setGlobalAlpha(1.0);
        }
    }

    private class Bomb extends FallingObject {
        private double rotation = 0;

        public Bomb(double x, double y, double radius) {
            super(x, y, radius);
            this.color = Color.BLACK;
            this.points = -1; // Negative points as penalty
        }

        @Override
        public void update(double gameSpeed) {
            y += 5 * gameSpeed; // Bombs fall faster
            rotation += 5;
        }

        @Override
        public void draw(GraphicsContext gc) {
            // Draw bomb body
            gc.setFill(Color.BLACK);
            gc.fillOval(x - radius, y - radius, radius * 2, radius * 2);

            // Draw fuse
            gc.save();
            gc.translate(x, y - radius);
            gc.rotate(rotation);
            gc.setStroke(Color.ORANGE);
            gc.setLineWidth(2);
            gc.strokeLine(0, -5, 5, -10);
            gc.setFill(Color.RED);
            gc.fillOval(5 - 2, -10 - 2, 4, 4);
            gc.restore();
        }
    }

    private class PowerUp extends FallingObject {
        private PowerUpType type;
        private long activationTime;
        private static final long DURATION = 5000; // 5 seconds duration for power-ups

        public PowerUp(double x, double y, PowerUpType type) {
            super(x, y, BASE_CIRCLE_RADIUS);
            this.type = type;
            this.points = 50;

            // Set color based on power-up type
            switch (type) {
                case EXTRA_LIFE:
                    this.color = Color.BLUE;
                    break;
                case SCORE_MULTIPLIER:
                    this.color = Color.GOLD;
                    break;
                case PADDLE_GROWTH:
                    this.color = Color.GREEN;
                    break;
                case SLOW_MOTION:
                    this.color = Color.PURPLE;
                    break;
                case CLEAR_SCREEN:
                    this.color = Color.WHITE;
                    break;
            }
        }

        @Override
        public void update(double gameSpeed) {
            y += 2 * gameSpeed; // Power-ups fall slower
        }

        @Override
        public void draw(GraphicsContext gc) {
            // Draw power-up with glowing effect
            RadialGradient gradient = new RadialGradient(
                    0, 0, x, y, radius * 1.5, false, CycleMethod.NO_CYCLE,
                    new Stop(0, color),
                    new Stop(0.8, color),
                    new Stop(1, Color.TRANSPARENT)
            );

            gc.setFill(gradient);
            gc.fillOval(x - radius * 1.5, y - radius * 1.5, radius * 3, radius * 3);

            // Draw inner circle
            gc.setFill(Color.WHITE);
            gc.fillOval(x - radius * 0.5, y - radius * 0.5, radius, radius);
        }

        public void setActivationTime(long time) {
            this.activationTime = time;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > activationTime + DURATION;
        }

        public void updateEffect() {
            // Power-up specific ongoing effects could be implemented here
        }

        public PowerUpType getType() {
            return type;
        }
    }

    private class Particle {
        private double x, y;
        private double speedX, speedY;
        private double size;
        private int lifetime;
        private Color color;
        public Circle circle;

        public Particle(double x, double y, Color baseColor) {
            this.x = x;
            this.y = y;

            // Random direction and speed
            double angle = random.nextDouble() * Math.PI * 2;
            double speed = random.nextDouble() * 5 + 1;

            this.speedX = Math.cos(angle) * speed;
            this.speedY = Math.sin(angle) * speed;

            // Random size
            this.size = random.nextDouble() * 4 + 2;

            // Random lifetime between 20-60 frames
            this.lifetime = random.nextInt(40) + 20;

            // Slightly randomize color
            double hue = randomizeColor(baseColor.getHue(), 20);
            double saturation = randomizeColor(baseColor.getSaturation(), 0.2);
            double brightness = randomizeColor(baseColor.getBrightness(), 0.2);
            this.color = Color.hsb(hue, saturation, brightness);

            // Create circle
            this.circle = new Circle(x, y, size, color);

            // Add glow effect
            Glow glow = new Glow();
            glow.setLevel(0.5);
            this.circle.setEffect(glow);
        }

        private double randomizeColor(double value, double range) {
            double min = Math.max(0, value - range);
            double max = Math.min(1, value + range);
            return min + random.nextDouble() * (max - min);
        }

        public void update() {
            // Update position
            x += speedX;
            y += speedY;

            // Apply gravity and drag
            speedY += 0.1;
            speedX *= 0.95;
            speedY *= 0.95;

            // Update circle position
            circle.setCenterX(x);
            circle.setCenterY(y);

            // Shrink over time
            if (lifetime < 10) {
                circle.setRadius(size * lifetime / 10);
            }

            // Update opacity based on lifetime
            circle.setOpacity(Math.min(1.0, lifetime / 20.0));

            // Decrease lifetime
            lifetime--;
        }

        public boolean isDead() {
            return lifetime <= 0;
        }
    }

    private class ScorePopup {
        private Text text;
        private double x, y;
        private int lifetime;

        public ScorePopup(String score, double x, double y) {
            this.x = x;
            this.y = y;
            this.lifetime = 50; // About 0.8 seconds at 60fps

            this.text = new Text(score);
            this.text.setFont(Font.font("Arial", FontWeight.BOLD, 18));
            this.text.setFill(Color.WHITE);

            // Center the text at the x,y position
            this.text.setLayoutX(x - this.text.getBoundsInLocal().getWidth() / 2);
            this.text.setLayoutY(y);

            // Add a drop shadow for better visibility
            DropShadow shadow = new DropShadow();
            shadow.setColor(Color.BLACK);
            shadow.setRadius(2);
            this.text.setEffect(shadow);
        }

        public void update() {
            // Move up slowly
            y -= 1;
            text.setLayoutY(y);

            // Fade out near the end of its lifetime
            if (lifetime < 20) {
                text.setOpacity(lifetime / 20.0);
            }

            lifetime--;
        }

        public boolean isDead() {
            return lifetime <= 0;
        }

        public Text getText() {
            return text;
        }
    }

    private class ProgressBar extends HBox {
        private Rectangle bar;
        private Rectangle background;
        private double maxWidth;

        public ProgressBar(double progress, double width) {
            this.maxWidth = width;
            this.setAlignment(Pos.CENTER_LEFT);

            background = new Rectangle(width, 10);
            background.setFill(Color.GRAY);
            background.setArcWidth(10);
            background.setArcHeight(10);

            bar = new Rectangle(progress * width, 10);
            bar.setFill(Color.LIMEGREEN);
            bar.setArcWidth(10);
            bar.setArcHeight(10);

            this.getChildren().addAll(background, bar);

            // Position the bar over the background
            bar.setTranslateX(-width);
        }

        public void setProgress(double progress) {
            bar.setWidth(Math.min(1, Math.max(0, progress)) * maxWidth);
        }
    }
}