package games;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import java.util.Random;

public class Game2048 extends Application {

    private static final int SIZE = 4;
    private static final int TILE_SIZE = 100;
    private int[][] board = new int[SIZE][SIZE];
    private GridPane grid = new GridPane();
    private Random random = new Random();

    @Override
    public void start(Stage primaryStage) {
        initBoard();
        updateBoard();

        Scene scene = new Scene(grid, TILE_SIZE * SIZE, TILE_SIZE * SIZE);
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
                    System.out.println("Game Over!");
                }
            }
        });

        primaryStage.setTitle("2048");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void initBoard() {
        spawn();
        spawn();
    }

    private void spawn() {
        while (true) {
            int x = random.nextInt(SIZE);
            int y = random.nextInt(SIZE);
            if (board[x][y] == 0) {
                board[x][y] = random.nextDouble() < 0.9 ? 2 : 4;
                break;
            }
        }
    }

    private void updateBoard() {
        grid.getChildren().clear();
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                Rectangle rect = new Rectangle(TILE_SIZE, TILE_SIZE);
                rect.setFill(getColor(board[i][j]));
                Text text = new Text(board[i][j] == 0 ? "" : String.valueOf(board[i][j]));
                text.setFont(Font.font(24));
                grid.add(rect, j, i);
                grid.add(text, j, i);
            }
        }
    }

    private Color getColor(int value) {
        switch (value) {
            case 2: return Color.BEIGE;
            case 4: return Color.BISQUE;
            case 8: return Color.ORANGE;
            case 16: return Color.DARKORANGE;
            case 32: return Color.CORAL;
            case 64: return Color.RED;
            case 128: return Color.GOLD;
            case 256: return Color.GOLDENROD;
            case 512: return Color.LIGHTGREEN;
            case 1024: return Color.GREEN;
            case 2048: return Color.DARKGREEN;
            default: return Color.LIGHTGRAY;
        }
    }

    private boolean moveLeft() {
        boolean moved = false;
        for (int i = 0; i < SIZE; i++) {
            int[] newRow = new int[SIZE];
            int pos = 0;
            boolean merged = false;
            for (int j = 0; j < SIZE; j++) {
                if (board[i][j] != 0) {
                    if (pos > 0 && newRow[pos - 1] == board[i][j] && !merged) {
                        newRow[pos - 1] *= 2;
                        merged = true;
                        moved = true;
                    } else {
                        newRow[pos++] = board[i][j];
                        if (j != pos - 1) moved = true;
                        merged = false;
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
