package pacman;

import globalFunc.Sound_Func;
import javafx.fxml.FXML;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.application.Platform;
import java.util.Timer;
import java.util.TimerTask;

public class Controller implements EventHandler<KeyEvent> {
    final private static double FRAMES_PER_SECOND = 5.0;

    @FXML private Label scoreLabel;
    @FXML private Label levelLabel;
    @FXML private Label gameOverLabel;
    @FXML private PacManView pacManView;
    private PacManModel pacManModel;
    private static final String[] levelFiles = {
        "/pacman/levels/level1.txt", 
        "/pacman/levels/level2.txt", 
        "/pacman/levels/level3.txt"
    };

    private Timer timer;
    private static int ghostEatingModeCounter;
    private boolean paused;
    private boolean musicPlaying = false;

    public Controller() {
        this.paused = false;
    }

    public void initialize() {
        String file = this.getLevelFile(0);
        this.pacManModel = new PacManModel();
        this.update(PacManModel.Direction.NONE);
        ghostEatingModeCounter = 25;
        this.startTimer();
        startBackgroundMusic();
    }

    private void startBackgroundMusic() {
        if (!musicPlaying) {
            Sound_Func.playPacmanSong();
            musicPlaying = true;
        }
    }

    private void stopBackgroundMusic() {
        if (musicPlaying) {
            Sound_Func.stopBackgroundMusic();
            musicPlaying = false;
        }
    }

    private void startTimer() {
        this.timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            public void run() {
                Platform.runLater(() -> update(pacManModel.getCurrentDirection()));
            }
        };

        long frameTimeInMilliseconds = (long)(1000.0 / FRAMES_PER_SECOND);
        this.timer.schedule(timerTask, 0, frameTimeInMilliseconds);
    }

    private void update(PacManModel.Direction direction) {
        this.pacManModel.step(direction);
        this.pacManView.update(pacManModel);
        this.scoreLabel.setText(String.format("Score: %d", this.pacManModel.getScore()));
        this.levelLabel.setText(String.format("Level: %d", this.pacManModel.getLevel()));
        
        if (pacManModel.isGameOver()) {
            this.gameOverLabel.setText("GAME OVER");
            pause();
            stopBackgroundMusic();
            Sound_Func.playDefeatSound();
        }
        if (pacManModel.isYouWon()) {
            this.gameOverLabel.setText("YOU WON!");
            Sound_Func.playVictorySound();
        }
        
        if (pacManModel.isGhostEatingMode()) {
            ghostEatingModeCounter--;
        }
        if (ghostEatingModeCounter == 0 && pacManModel.isGhostEatingMode()) {
            pacManModel.setGhostEatingMode(false);
        }
    }

    @Override
    public void handle(KeyEvent keyEvent) {
        boolean keyRecognized = true;
        KeyCode code = keyEvent.getCode();
        PacManModel.Direction direction = PacManModel.Direction.NONE;
        
        switch (code) {
            case LEFT -> direction = PacManModel.Direction.LEFT;
            case RIGHT -> direction = PacManModel.Direction.RIGHT;
            case UP -> direction = PacManModel.Direction.UP;
            case DOWN -> direction = PacManModel.Direction.DOWN;
            case G -> {
                pause();
                stopBackgroundMusic();
                this.pacManModel.startNewGame();
                this.gameOverLabel.setText("");
                paused = false;
                this.startTimer();
                startBackgroundMusic();
            }
            case ESCAPE -> {
                // When returning to main menu
                pause();
                stopBackgroundMusic();
                // Add code here to return to main menu if needed
            }
            default -> keyRecognized = false;
        }
        
        if (keyRecognized) {
            keyEvent.consume();
            pacManModel.setCurrentDirection(direction);
        }
    }

    public void pause() {
        if (timer != null) {
            this.timer.cancel();
            this.paused = true;
        }
    }

    public double getBoardWidth() {
        return PacManView.CELL_WIDTH * this.pacManView.getColumnCount();
    }

    public double getBoardHeight() {
        return PacManView.CELL_WIDTH * this.pacManView.getRowCount();
    }

    public static void setGhostEatingModeCounter() {
        ghostEatingModeCounter = 25;
    }

    public static int getGhostEatingModeCounter() {
        return ghostEatingModeCounter;
    }

    public static String getLevelFile(int index) {
        if (index >= 0 && index < levelFiles.length) {
            return levelFiles[index];
        }
        throw new IllegalArgumentException("Invalid level index: " + index);
    }

    public boolean getPaused() {
        return paused;
    }
    
}