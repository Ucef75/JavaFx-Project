package utils;

import model.Game;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import asteroid.AsteroidApplication;
import games.Bomberman;
import games.BrickBreaker;
import games.Catch;
import games.FlappyBird;
import games.Game2048;
import games.MiniDungeonGame;
import games.Pong;
import games.Snake;
import games.TheCircles;
import pacman.PacmanApplication;
import games.XOGame;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.scene.text.Font;

public class GameLibrary {
    private static final String DEFAULT_IMAGE_PATH = "/pictures/default_game.png"; // Changed to .png
    private static final Map<String, String> GAME_DESCRIPTIONS = new HashMap<>();

    static {
        // Initialize game descriptions
        GAME_DESCRIPTIONS.put("Bomberman", "Navigate through a maze, placing bombs to destroy obstacles and defeat enemies while avoiding getting caught in your own explosions.");
        GAME_DESCRIPTIONS.put("BrickBreaker", "Control a paddle to bounce a ball and break all the bricks at the top of the screen. Don't let the ball fall!");
        GAME_DESCRIPTIONS.put("Catch", "Test your reflexes by catching falling objects before they hit the ground. The pace increases as you progress!");
        GAME_DESCRIPTIONS.put("Dungeon", "Explore a dangerous dungeon filled with monsters, treasures, and traps. Fight your way through increasingly difficult levels.");
        GAME_DESCRIPTIONS.put("FlappyBird", "Guide a bird through a series of pipes by tapping to make it flap its wings. One touch on the pipes and it's game over!");
        GAME_DESCRIPTIONS.put("Game2048", "Combine matching number tiles to create the elusive 2048 tile. Use strategy to maximize your score in this addictive puzzle game.");
        GAME_DESCRIPTIONS.put("Pong", "The classic arcade game! Control a paddle and bounce the ball back and forth against an AI opponent.");
        GAME_DESCRIPTIONS.put("Snake", "Control a growing snake, eat food to get longer, but don't crash into the walls or yourself!");
        GAME_DESCRIPTIONS.put("The Circles", "A unique puzzle game where you must connect colored circles in the right sequence while solving increasingly complex patterns.");
        GAME_DESCRIPTIONS.put("Asteroid", "Pilot a spaceship through an asteroid field, shooting rocks and avoiding collisions in this classic arcade space shooter.");
        GAME_DESCRIPTIONS.put("Pacman", "Navigate through a maze eating dots while avoiding ghosts. Eat power pellets to turn the tables on the ghosts!");
        GAME_DESCRIPTIONS.put("Catch the treasure", "Race against time to collect as many treasures as possible while avoiding dangerous obstacles and traps.");
        GAME_DESCRIPTIONS.put("8024", "A challenging number puzzle where you must combine powers of 2 to reach higher numbers and clear the board.");
        GAME_DESCRIPTIONS.put("X/O", "The classic game of Tic-Tac-Toe. Get three of your marks in a row, column, or diagonal to win!");
        GAME_DESCRIPTIONS.put("Space AIRCRAFT", "Command a powerful spacecraft through enemy territory, dodging attacks and firing back in this exciting space shooter.");
        GAME_DESCRIPTIONS.put("Connect 4", "Take turns dropping colored discs into a vertical grid. Be the first to connect four of your discs in a row to win!");
    }

    public static Game[] getAvailableGames() {
        String sql = "SELECT game_id, game_name, description, game_photo FROM Games";
        List<Game> games = new ArrayList<>();

        try (Connection conn = Database.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String gameName = rs.getString("game_name");
                String imagePath = getImagePathForGame(gameName); // Use the new method

                // Get description from database or use our predefined one if database description is empty
                String description = rs.getString("description");
                if (description == null || description.trim().isEmpty()) {
                    description = getGameDescription(gameName);
                }

                // Create Game object using the database constructor
                Game game = new Game(
                        rs.getInt("game_id"),
                        gameName,
                        description
                );

                // Set additional fields
                game.setImagePath(imagePath);
                game.setGameClass(getGameClass(gameName));

                games.add(game);
            }
        } catch (SQLException e) {
            System.err.println("Error loading games: " + e.getMessage());
            e.printStackTrace();
        }

        return games.toArray(new Game[0]);
    }

    private static String getImagePathForGame(String gameName) {
        return switch (gameName) {
            case "Bomberman" -> "/pictures/Bomberman_game.jpg";
            case "BrickBreaker" -> "/pictures/Brick_game.jpg";
            case "Catch" -> "/pictures/Catch_game.png";
            case "Dungeon" -> "/pictures/Dungeon_game.jpg";
            case "FlappyBird" -> "/pictures/Flappy_game.jpg";
            case "Game2048" -> "/pictures/2048_game.jpg";
            case "Pong" -> "/pictures/Pong_game.png";
            case "Snake" -> "/pictures/Snake_game.jpg";
            case "The Circles" -> "/pictures/Circle_game.png";
            case "Asteroid" -> "/pictures/Asteroid_game.png";
            case "Pacman" -> "/pictures/Pac_game.jpg";
            case "Catch the treasure" -> "/pictures/treasure_game.png"; // Assuming same image as Catch
            case "8024" -> "/pictures/making_game.jpg"; // Assuming same as 2048
            case "X/O" -> "/pictures/x_game.jpg"; // Need to add an image for this
            case "Space AIRCRAFT" -> "/pictures/space_game.jpg"; // Assuming same as Asteroid
            case "Connect 4" -> "/pictures/connect_game.jpg"; // Need to add an image for this
            default -> DEFAULT_IMAGE_PATH;
        };
    }

    private static String getGameDescription(String gameName) {
        return GAME_DESCRIPTIONS.getOrDefault(gameName, "A fun and exciting game to play! Try it now!");
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
                case "X/O" -> games.XOGame.class;
                case "Connect 4" -> games.Connect4.class;
                default -> null;
            };
        } catch (ClassCastException e) {
            System.err.println("Game class for " + gameName + " is not a JavaFX Application");
            return null;
        }
    }
}