package application;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class UserActivity {
    private final SimpleStringProperty date;
    private final SimpleStringProperty gameName;
    private final SimpleIntegerProperty score;
    private final SimpleIntegerProperty rank;

    public UserActivity(String date, String gameName, int score, int rank) {
        this.date = new SimpleStringProperty(date);
        this.gameName = new SimpleStringProperty(gameName);
        this.score = new SimpleIntegerProperty(score);
        this.rank = new SimpleIntegerProperty(rank);
    }

    public SimpleStringProperty dateProperty() {
        return date;
    }

    public SimpleStringProperty gameNameProperty() {
        return gameName;
    }

    public SimpleIntegerProperty scoreProperty() {
        return score;
    }

    public SimpleIntegerProperty rankProperty() {
        return rank;
    }

    public String getDate() {
        return date.get();
    }

    public String getGameName() {
        return gameName.get();
    }

    public int getScore() {
        return score.get();
    }

    public int getRank() {
        return rank.get();
    }
}