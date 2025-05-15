package games;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class XOGame extends Application {
    private char currentPlayer = 'X';
    private Button[][] buttons = new Button[3][3];
    private Label statusLabel;
    private Stage primaryStage;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        showMainMenu();
    }

    private void showMainMenu() {
        VBox menuLayout = new VBox(20);
        menuLayout.setAlignment(Pos.CENTER);
        menuLayout.setPadding(new Insets(20));
        menuLayout.setStyle("-fx-background-color: #f0f0f0;");

        Label titleLabel = new Label("X/O Game");
        titleLabel.setFont(Font.font(36));
        titleLabel.setTextFill(Color.DARKBLUE);

        Button playButton = new Button("Play Game");
        playButton.setStyle("-fx-font-size: 18px; -fx-min-width: 150px;");
        playButton.setOnAction(e -> startGame());

        Button exitButton = new Button("Exit");
        exitButton.setStyle("-fx-font-size: 18px; -fx-min-width: 150px;");
        exitButton.setOnAction(e -> primaryStage.close());

        menuLayout.getChildren().addAll(titleLabel, playButton, exitButton);

        Scene menuScene = new Scene(menuLayout, 400, 400);
        primaryStage.setTitle("X/O Game - Main Menu");
        primaryStage.setScene(menuScene);
        primaryStage.show();
    }

    private void startGame() {
        GridPane gameGrid = new GridPane();
        gameGrid.setAlignment(Pos.CENTER);
        gameGrid.setHgap(10);
        gameGrid.setVgap(10);
        gameGrid.setPadding(new Insets(20));

        statusLabel = new Label("Player X's turn");
        statusLabel.setFont(Font.font(20));
        statusLabel.setTextFill(Color.DARKGREEN);

        // Create game board
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                Button button = new Button();
                button.setMinSize(80, 80);
                button.setFont(Font.font(24));
                button.setStyle("-fx-background-color: #ffffff; -fx-border-color: #000000;");

                final int finalRow = row;
                final int finalCol = col;
                button.setOnAction(e -> handleButtonClick(finalRow, finalCol));

                buttons[row][col] = button;
                gameGrid.add(button, col, row);
            }
        }

        Button menuButton = new Button("Back to Menu");
        menuButton.setStyle("-fx-font-size: 14px;");
        menuButton.setOnAction(e -> showMainMenu());

        Button restartButton = new Button("Restart Game");
        restartButton.setStyle("-fx-font-size: 14px;");
        restartButton.setOnAction(e -> resetGame());

        HBox buttonBox = new HBox(10, menuButton, restartButton);
        buttonBox.setAlignment(Pos.CENTER);

        VBox gameLayout = new VBox(20, statusLabel, gameGrid, buttonBox);
        gameLayout.setAlignment(Pos.CENTER);
        gameLayout.setPadding(new Insets(20));
        gameLayout.setStyle("-fx-background-color: #f0f0f0;");

        Scene gameScene = new Scene(gameLayout, 400, 500);
        primaryStage.setTitle("X/O Game");
        primaryStage.setScene(gameScene);
    }

    private void handleButtonClick(int row, int col) {
        Button button = buttons[row][col];

        if (button.getText().isEmpty()) {
            button.setText(String.valueOf(currentPlayer));
            button.setTextFill(currentPlayer == 'X' ? Color.BLUE : Color.RED);

            if (checkForWin()) {
                statusLabel.setText("Player " + currentPlayer + " wins!");
                disableAllButtons();
            } else if (isBoardFull()) {
                statusLabel.setText("Game ended in a draw!");
            } else {
                currentPlayer = (currentPlayer == 'X') ? 'O' : 'X';
                statusLabel.setText("Player " + currentPlayer + "'s turn");
            }
        }
    }

    private boolean checkForWin() {
        // Check rows
        for (int row = 0; row < 3; row++) {
            if (buttons[row][0].getText().equals(String.valueOf(currentPlayer)) &&
                    buttons[row][1].getText().equals(String.valueOf(currentPlayer)) &&
                    buttons[row][2].getText().equals(String.valueOf(currentPlayer))) {
                return true;
            }
        }

        // Check columns
        for (int col = 0; col < 3; col++) {
            if (buttons[0][col].getText().equals(String.valueOf(currentPlayer)) &&
                    buttons[1][col].getText().equals(String.valueOf(currentPlayer)) &&
                    buttons[2][col].getText().equals(String.valueOf(currentPlayer))) {
                return true;
            }
        }

        // Check diagonals
        if (buttons[0][0].getText().equals(String.valueOf(currentPlayer)) &&
                buttons[1][1].getText().equals(String.valueOf(currentPlayer)) &&
                buttons[2][2].getText().equals(String.valueOf(currentPlayer))) {
            return true;
        }

        if (buttons[0][2].getText().equals(String.valueOf(currentPlayer)) &&
                buttons[1][1].getText().equals(String.valueOf(currentPlayer)) &&
                buttons[2][0].getText().equals(String.valueOf(currentPlayer))) {
            return true;
        }

        return false;
    }

    private boolean isBoardFull() {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                if (buttons[row][col].getText().isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    private void disableAllButtons() {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                buttons[row][col].setDisable(true);
            }
        }
    }

    private void resetGame() {
        currentPlayer = 'X';
        statusLabel.setText("Player X's turn");

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                buttons[row][col].setText("");
                buttons[row][col].setDisable(false);
            }
        }
    }
}