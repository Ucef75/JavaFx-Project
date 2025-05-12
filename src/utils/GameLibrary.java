package utils;

import model.Game;
import model.Score;
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

                games.add(new Game(
                        rs.getInt("game_id"),
                        rs.getString("game_name"),
                        rs.getString("description"),
                        imagePath,
                        getGameClass(rs.getString("game_name"))
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error loading games: " + e.getMessage());
            e.printStackTrace();
        }

        return games.toArray(new Game[0]);
    }


    private static Class<?> getGameClass(String gameName) {
        return switch (gameName) {
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
    }

    public static List<Score> getAllScores() {
        String sql = """
            SELECT us.user_score_id, us.user_id, us.game_id, us.score, 
                   us.last_updated as date, u.username, u.country, g.game_name
            FROM UserScores us
            JOIN Users u ON us.user_id = u.user_id
            JOIN Games g ON us.game_id = g.game_id
            ORDER BY us.score DESC
            """;

        List<Score> scores = new ArrayList<>();

        try (Connection conn = Database.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                scores.add(new Score(
                        rs.getInt("user_score_id"),
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("country"),
                        new Game(rs.getInt("game_id"), rs.getString("game_name"), ""),
                        rs.getInt("score"),
                        rs.getString("date")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error loading scores: " + e.getMessage());
            e.printStackTrace();
        }

        return scores;
    }

    public static List<Score> getScoresForGame(int gameId) {
        String sql = """
            SELECT us.user_score_id, us.user_id, us.game_id, us.score, 
                   us.last_updated as date, u.username, u.country, g.game_name
            FROM UserScores us
            JOIN Users u ON us.user_id = u.user_id
            JOIN Games g ON us.game_id = g.game_id
            WHERE us.game_id = ?
            ORDER BY us.score DESC
            """;

        List<Score> scores = new ArrayList<>();

        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, gameId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    scores.add(new Score(
                            rs.getInt("user_score_id"),
                            rs.getInt("user_id"),
                            rs.getString("username"),
                            rs.getString("country"),
                            new Game(rs.getInt("game_id"), rs.getString("game_name"), ""),
                            rs.getInt("score"),
                            rs.getString("date")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error loading game scores: " + e.getMessage());
            e.printStackTrace();
        }

        return scores;
    }

    public static boolean saveScore(int userId, int gameId, int score) {
        String sql = """
            INSERT INTO UserScores (user_id, game_id, score)
            VALUES (?, ?, ?)
            ON CONFLICT(user_id, game_id) 
            DO UPDATE SET score = MAX(score, excluded.score), last_updated = CURRENT_TIMESTAMP
            """;

        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, gameId);
            pstmt.setInt(3, score);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error saving score: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static int getUserHighScore(int userId, int gameId) {
        String sql = "SELECT score FROM UserScores WHERE user_id = ? AND game_id = ?";

        try (Connection conn = Database.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, gameId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("score");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting user score: " + e.getMessage());
            e.printStackTrace();
        }

        return 0;
    }
}