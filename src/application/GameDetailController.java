package application;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.Score;
import model.Game;
import java.sql.*;
import javafx.application.Application;

public class GameDetailController {

    @FXML
    private Label gameTitle;

    @FXML
    private ImageView gameImage;

    @FXML
    private Label gameDescription;

    @FXML
    private Label gameInstructions;

    @FXML
    private TableView<Score> highScoresTable;

    @FXML
    private TableColumn<Score, Number> rankColumn;

    @FXML
    private TableColumn<Score, String> playerColumn;

    @FXML
    private TableColumn<Score, Number> scoreColumn;

    @FXML
    private TableColumn<Score, String> dateColumn;

    private Game game;

    @FXML
    public void initialize() {
        rankColumn.setCellValueFactory(cellData -> cellData.getValue().rankProperty());
        playerColumn.setCellValueFactory(cellData -> cellData.getValue().playerNameProperty());
        scoreColumn.setCellValueFactory(cellData -> cellData.getValue().scoreProperty());
        dateColumn.setCellValueFactory(cellData -> cellData.getValue().dateProperty());
    }

    public void setGame(Game game) {
        this.game = game;
        gameTitle.setText(game.getName());

        try {
            gameImage.setImage(new Image(getClass().getResourceAsStream(game.getImagePath())));
        } catch (Exception e) {
            System.err.println("Failed to load image: " + game.getImagePath());
        }

        gameDescription.setText(game.getDescription());
        gameInstructions.setText("Use arrow keys to navigate and spacebar to shoot/select.");

        loadHighScores();
    }

    private void loadHighScores() {
        ObservableList<Score> scores = FXCollections.observableArrayList();

        String query = """
            SELECT u.username, us.score, us.last_update
            FROM UserScores us
            JOIN Users u ON u.user_id = us.user_id
            JOIN Games g ON g.game_id = us.game_id
            WHERE g.name = ?
            ORDER BY us.score DESC
            LIMIT 10
        """;

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:your_database_file.db");
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, game.getName());

            ResultSet rs = stmt.executeQuery();
            int rank = 1;
            while (rs.next()) {
                String player = rs.getString("username");
                int score = rs.getInt("score");
                String date = rs.getString("last_update");
                scores.add(new Score(rank++, player, score, date));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        highScoresTable.setItems(scores);
    }

    @FXML
    public void playGame() {
        try {
            Application.launch(game.getGameClass()); // Doit hériter de javafx.application.Application
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Erreur de lancement");
            alert.setHeaderText("Erreur lors du lancement du jeu");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    public void backToGames() {
        Stage stage = (Stage) gameTitle.getScene().getWindow();
        stage.close();
    }
}
