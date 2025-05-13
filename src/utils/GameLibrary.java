package utils;

import model.Game;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GameLibrary {
    private static final String DEFAULT_IMAGE_PATH = "/pictures/default_game.jpg";

    public static Game[] getAvailableGames() {
        String sql = "SELECT game_id, game_name, description, game_photo FROM Games";
        List<Game> games = new ArrayList<>();

        try (Connection conn = Database.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String imagePath = rs.getString("game_photo");
                if (imagePath == null || imagePath.isEmpty()) {
                    imagePath = DEFAULT_IMAGE_PATH;
                }

                // Create Game object using the database constructor
                Game game = new Game(
                        rs.getInt("game_id"),
                        rs.getString("game_name"),
                        rs.getString("description")
                );

                // Set additional fields
                game.setImagePath(imagePath);
                game.setGameClass(getGameClass(rs.getString("game_name")));

                games.add(game);
            }
        } catch (SQLException e) {
            System.err.println("Error loading games: " + e.getMessage());
            e.printStackTrace();
        }

        return games.toArray(new Game[0]);
    }

    @SuppressWarnings("unchecked")
    private static Class<? extends javafx.application.Application> getGameClass(String gameName) {
        try {
            return (Class<? extends javafx.application.Application>) switch (gameName) {
                case "Bomberman" -> games.Bomberman.class;
                case "BrickBreaker" -> games.BrickBreaker.class;
                case "Catch" -> games.Catch.class;
                case "Dungeon" -> games.MiniDungeonGame.class;
                case "FlappyBird" -> games.FlappyBird.class;
                case "Game2048" -> games.Game2048.class;
                case "Pong" -> games.Pong.class;
                case "Snake" -> games.Snake.class;
                case "The Circles" -> games.TheCircles.class;
                case "Asteroid" -> asteroid.AsteroidApplication.class;
                case "Pacman" -> pacman.PacmanApplication.class;
                default -> null;
            };
        } catch (ClassCastException e) {
            System.err.println("Game class for " + gameName + " is not a JavaFX Application");
            return null;
        }
    }
}