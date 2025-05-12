package application;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import utils.GameLibrary;
import model.Game;
import javafx.beans.value.ObservableValue;
import java.util.List;
import model.Score;

public class LeaderboardController {
    @FXML private ComboBox<String> gameFilterComboBox;
    @FXML private ComboBox<String> timePeriodComboBox;
    @FXML private TableView<LeaderboardEntry> leaderboardTable;
    @FXML private TableColumn<LeaderboardEntry, Integer> rankColumn;
    @FXML private TableColumn<LeaderboardEntry, String> playerColumn;
    @FXML private TableColumn<LeaderboardEntry, String> gameColumn;
    @FXML private TableColumn<LeaderboardEntry, Integer> scoreColumn;
    @FXML private TableColumn<LeaderboardEntry, String> dateColumn;
    @FXML private TableColumn<LeaderboardEntry, String> countryColumn;

    @FXML
    public void initialize() {
        // Set up table columns
        rankColumn.setCellValueFactory(cellData -> cellData.getValue().rankProperty().asObject());
        playerColumn.setCellValueFactory(cellData -> cellData.getValue().playerNameProperty());
        gameColumn.setCellValueFactory(cellData -> cellData.getValue().gameNameProperty());
        scoreColumn.setCellValueFactory(cellData -> cellData.getValue().scoreProperty().asObject());
        dateColumn.setCellValueFactory(cellData -> cellData.getValue().dateProperty());
        countryColumn.setCellValueFactory(cellData -> cellData.getValue().countryProperty());

        initializeFilters();
        loadLeaderboardData("All Games", "All Time");
    }

    private void initializeFilters() {
        // Game filter
        ObservableList<String> gameOptions = FXCollections.observableArrayList("All Games");
        for (Game game : GameLibrary.getAvailableGames()) {
            gameOptions.add(game.getName());
        }
        gameFilterComboBox.setItems(gameOptions);
        gameFilterComboBox.setValue("All Games");

        // Time period filter
        ObservableList<String> timeOptions = FXCollections.observableArrayList(
                "All Time", "This Month", "This Week", "Today"
        );
        timePeriodComboBox.setItems(timeOptions);
        timePeriodComboBox.setValue("All Time");
    }

    private void loadLeaderboardData(String gameFilter, String timeFilter) {
        ObservableList<LeaderboardEntry> leaderboardData = FXCollections.observableArrayList();
        List<Score> scores = GameLibrary.getAllScores();

        int rank = 1;
        for (Score score : scores) {
            if ("All Games".equals(gameFilter) || score.getGame().getName().equals(gameFilter)) {
                leaderboardData.add(new LeaderboardEntry(
                        rank++,
                        score.getUsername(),
                        score.getGame().getName(),
                        score.getScore(),
                        score.getDate(),
                        score.getCountry()
                ));
            }
        }

        leaderboardTable.setItems(leaderboardData);
    }

    @FXML
    public void filterLeaderboard() {
        loadLeaderboardData(gameFilterComboBox.getValue(), timePeriodComboBox.getValue());
    }

    @FXML
    public void refreshLeaderboard() {
        filterLeaderboard();
    }

    @FXML
    public void closeLeaderboard() {
        ((Stage) leaderboardTable.getScene().getWindow()).close();
    }
}