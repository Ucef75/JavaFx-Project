package games;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class Connect4 extends Application {

    private static final int ROWS = 6;
    private static final int COLS = 7;
    private final Circle[][] grid = new Circle[ROWS][COLS];
    private final int[][] board = new int[ROWS][COLS];
    private int currentPlayer = 1; // 1 or 2
    private Stage mainStage;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        this.mainStage = primaryStage;
        showMainMenu();
    }

    private void showMainMenu() {
        VBox menuLayout = new VBox(20);
        menuLayout.setAlignment(Pos.CENTER);

        Text title = new Text("Connect 4");
        title.setFont(Font.font(36));

        Button startButton = new Button("Start Game");
        startButton.setOnAction(e -> showGameScene());

        Button exitButton = new Button("Exit");
        exitButton.setOnAction(e -> mainStage.close());

        menuLayout.getChildren().addAll(title, startButton, exitButton);

        Scene menuScene = new Scene(menuLayout, 600, 600);
        mainStage.setScene(menuScene);
        mainStage.setTitle("Connect 4 Menu");
        mainStage.show();
    }

    private void showGameScene() {
        BorderPane root = new BorderPane();
        GridPane boardPane = new GridPane();
        boardPane.setAlignment(Pos.CENTER);

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                Circle cell = new Circle(40);
                cell.setFill(Color.LIGHTGRAY);
                cell.setStroke(Color.BLACK);
                grid[row][col] = cell;
                int finalCol = col;
                cell.setOnMouseClicked(e -> dropDisc(finalCol));
                boardPane.add(cell, col, row);
            }
        }

        Button backButton = new Button("Back to Menu");
        backButton.setOnAction(e -> showMainMenu());

        VBox bottom = new VBox(10, backButton);
        bottom.setAlignment(Pos.CENTER);

        root.setCenter(boardPane);
        root.setBottom(bottom);

        Scene gameScene = new Scene(root, 800, 700);
        mainStage.setScene(gameScene);
        mainStage.setTitle("Connect 4 Game");
    }

    private void dropDisc(int col) {
        for (int row = ROWS - 1; row >= 0; row--) {
            if (board[row][col] == 0) {
                board[row][col] = currentPlayer;
                grid[row][col].setFill(currentPlayer == 1 ? Color.RED : Color.YELLOW);
                if (checkWin(row, col)) {
                    showWinMessage("Player " + currentPlayer + " wins!");
                } else {
                    currentPlayer = 3 - currentPlayer;
                }
                break;
            }
        }
    }

    private boolean checkWin(int row, int col) {
        int player = board[row][col];
        return count(row, col, 1, 0, player) + count(row, col, -1, 0, player) > 2 ||
                count(row, col, 0, 1, player) + count(row, col, 0, -1, player) > 2 ||
                count(row, col, 1, 1, player) + count(row, col, -1, -1, player) > 2 ||
                count(row, col, 1, -1, player) + count(row, col, -1, 1, player) > 2;
    }

    private int count(int row, int col, int dr, int dc, int player) {
        int cnt = 0;
        for (int r = row + dr, c = col + dc;
             r >= 0 && r < ROWS && c >= 0 && c < COLS && board[r][c] == player;
             r += dr, c += dc) {
            cnt++;
        }
        return cnt;
    }

    private void showWinMessage(String message) {
        VBox layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);

        Text msg = new Text(message);
        msg.setFont(Font.font(24));

        Button backToMenu = new Button("Back to Menu");
        backToMenu.setOnAction(e -> showMainMenu());

        layout.getChildren().addAll(msg, backToMenu);
        mainStage.setScene(new Scene(layout, 600, 400));
    }
}
