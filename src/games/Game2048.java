package games;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;

import java.util.Optional;
import java.util.Random;

public class Game2048 extends Application {

    private static final int SIZE = 4;
    private static final int TILE_SIZE = 100;
    private int[][] board = new int[SIZE][SIZE];
    private GridPane grid = new GridPane();
    private Random random = new Random();
    private int score = 0;
    private Text scoreText = new Text();

    @Override
    public void start(Stage primaryStage) {
        grid.setAlignment(Pos.CENTER);
        scoreText.setFont(Font.font(24));
        updateScore(0);
        VBox mainLayout = new VBox(10, scoreText, grid);
        mainLayout.setAlignment(Pos.CENTER);

        initBoard();
        updateBoard();

        Scene scene = new Scene(mainLayout, TILE_SIZE * SIZE, TILE_SIZE * SIZE + 40);
        scene.setOnKeyPressed(e -> {
            boolean moved = false;
            if (e.getCode() == KeyCode.LEFT) moved = moveLeft();
            if (e.getCode() == KeyCode.RIGHT) moved = moveRight();
            if (e.getCode() == KeyCode.UP) moved = moveUp();
            if (e.getCode() == KeyCode.DOWN) moved = moveDown();

            if (moved) {
                spawn();
                updateBoard();
                if (isGameOver()) {
                    showGameOver(primaryStage);
                }
            }
        });

        primaryStage.setTitle("2048");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    private void showGameOver(Stage primaryStage) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Game Over");
        alert.setHeaderText("Game Over! Your score: " + score);
        alert.setContentText("Do you want to play again?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            resetGame();
        } else {
            primaryStage.close();
        }
    }

    private void resetGame() {
        board = new int[SIZE][SIZE];
        score = 0;
        updateScore(0);
        initBoard();
        updateBoard();
    }

    private void updateScore(int add) {
        score += add;
        scoreText.setText("Score: " + score);
    }

    private void initBoard() {
        spawn();
        spawn();
    }

    private void spawn() {
        int emptyCount = 0;
        for (int[] row : board)
            for (int val : row)
                if (val == 0)
                    emptyCount++;
        if (emptyCount == 0) return;

        int pos = random.nextInt(emptyCount);
        for (int i = 0; i < SIZE; i++)
            for (int j = 0; j < SIZE; j++)
                if (board[i][j] == 0 && (pos-- == 0)) {
                    board[i][j] = random.nextDouble() < 0.9 ? 2 : 4;
                    return;
                }
    }

    private void updateBoard() {
        grid.getChildren().clear();
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                StackPane cell = new StackPane();
                Rectangle rect = new Rectangle(TILE_SIZE, TILE_SIZE);
                rect.setArcWidth(20);
                rect.setArcHeight(20);
                rect.setFill(getColor(board[i][j]));
                rect.setStroke(Color.rgb(187, 173, 160));
                rect.setStrokeWidth(3);

                Text text = new Text(board[i][j] == 0 ? "" : String.valueOf(board[i][j]));
                text.setFont(Font.font(28));
                text.setFill(board[i][j] <= 4 ? Color.rgb(119, 110, 101) : Color.WHITE);

                cell.getChildren().addAll(rect, text);
                grid.add(cell, j, i);
            }
        }
    }

    private Color getColor(int value) {
        switch (value) {
            case 2: return Color.web("#eee4da");
            case 4: return Color.web("#ede0c8");
            case 8: return Color.web("#f2b179");
            case 16: return Color.web("#f59563");
            case 32: return Color.web("#f67c5f");
            case 64: return Color.web("#f65e3b");
            case 128: return Color.web("#edcf72");
            case 256: return Color.web("#edcc61");
            case 512: return Color.web("#edc850");
            case 1024: return Color.web("#edc53f");
            case 2048: return Color.web("#edc22e");
            default: return Color.web("#cdc1b4");
        }
    }

    private boolean moveLeft() {
        boolean moved = false;
        for (int i = 0; i < SIZE; i++) {
            int[] newRow = new int[SIZE];
            int pos = 0;
            boolean mergedLast = false;
            for (int j = 0; j < SIZE; j++) {
                if (board[i][j] != 0) {
                    if (pos > 0 && newRow[pos - 1] == board[i][j] && !mergedLast) {
                        newRow[pos - 1] *= 2;
                        updateScore(newRow[pos - 1]);
                        mergedLast = true;
                        moved = true;
                    } else {
                        newRow[pos++] = board[i][j];
                        if (j != pos - 1) moved = true;
                        mergedLast = false;
                    }
                }
            }
            board[i] = newRow;
        }
        return moved;
    }

    private boolean moveRight() {
        rotateBoard();
        rotateBoard();
        boolean moved = moveLeft();
        rotateBoard();
        rotateBoard();
        return moved;
    }

    private boolean moveUp() {
        rotateBoard();
        rotateBoard();
        rotateBoard();
        boolean moved = moveLeft();
        rotateBoard();
        return moved;
    }

    private boolean moveDown() {
        rotateBoard();
        boolean moved = moveLeft();
        rotateBoard();
        rotateBoard();
        rotateBoard();
        return moved;
    }

    private void rotateBoard() {
        int[][] newBoard = new int[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++)
            for (int j = 0; j < SIZE; j++)
                newBoard[i][j] = board[SIZE - j - 1][i];
        board = newBoard;
    }

    private boolean isGameOver() {
        for (int i = 0; i < SIZE; i++)
            for (int j = 0; j < SIZE; j++) {
                if (board[i][j] == 0) return false;
                if (j < SIZE - 1 && board[i][j] == board[i][j + 1]) return false;
                if (i < SIZE - 1 && board[i][j] == board[i + 1][j]) return false;
            }
        return true;
    }

    public static void main(String[] args) {
        launch(args);
    }
}