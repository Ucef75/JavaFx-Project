package application;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;

public class LeaderboardEntry {
    private final SimpleIntegerProperty rank;
    private final SimpleStringProperty playerName;
    private final SimpleStringProperty gameName;
    private final SimpleIntegerProperty score;
    private final SimpleStringProperty date;
    private final SimpleStringProperty country;

    public LeaderboardEntry(int rank, String playerName, String gameName, int score, String date, String country) {
        this.rank = new SimpleIntegerProperty(rank);
        this.playerName = new SimpleStringProperty(playerName);
        this.gameName = new SimpleStringProperty(gameName);
        this.score = new SimpleIntegerProperty(score);
        this.date = new SimpleStringProperty(date);
        this.country = new SimpleStringProperty(country);
    }

    // Property getters for TableView
    public ObservableValue<Integer> rankProperty() {
        return rank.asObject();
    }

    public ObservableValue<String> playerNameProperty() {
        return playerName;
    }

    public ObservableValue<String> gameNameProperty() {
        return gameName;
    }

    public ObservableValue<Integer> scoreProperty() {
        return score.asObject();
    }

    public ObservableValue<String> dateProperty() {
        return date;
    }

    public ObservableValue<String> countryProperty() {
        return country;
    }

    // Standard getters
    public int getRank() {
        return rank.get();
    }

    public String getPlayerName() {
        return playerName.get();
    }

    public String getGameName() {
        return gameName.get();
    }

    public int getScore() {
        return score.get();
    }

    public String getDate() {
        return date.get();
    }

    public String getCountry() {
        return country.get();
    }
}