package model;

import javafx.beans.property.*;

public class Score {
    private final IntegerProperty id;
    private final IntegerProperty userId;
    private final StringProperty username;
    private final StringProperty country;
    private final Game game;
    private final IntegerProperty scoreValue;
    private final StringProperty date;

    public Score(int id, int userId, String username, String country,
                 Game game, int scoreValue, String date) {
        this.id = new SimpleIntegerProperty(id);
        this.userId = new SimpleIntegerProperty(userId);
        this.username = new SimpleStringProperty(username);
        this.country = new SimpleStringProperty(country);
        this.game = game;
        this.scoreValue = new SimpleIntegerProperty(scoreValue);
        this.date = new SimpleStringProperty(date);
    }

    // Getters
    public int getId() { return id.get(); }
    public int getUserId() { return userId.get(); }
    public String getUsername() { return username.get(); }
    public String getCountry() { return country.get(); }
    public Game getGame() { return game; }
    public int getScoreValue() { return scoreValue.get(); }
    public String getDate() { return date.get(); }

    // Property getters (for TableView binding)
    public IntegerProperty idProperty() { return id; }
    public IntegerProperty userIdProperty() { return userId; }
    public StringProperty usernameProperty() { return username; }
    public StringProperty countryProperty() { return country; }
    public IntegerProperty scoreValueProperty() { return scoreValue; }
    public StringProperty dateProperty() { return date; }

    // For leaderboard display (simplified version)
    public IntegerProperty rankProperty() { return new SimpleIntegerProperty(0); } // Will be set later
    public StringProperty playerNameProperty() { return username; }
    public StringProperty gameNameProperty() { return new SimpleStringProperty(game.getName()); }
}