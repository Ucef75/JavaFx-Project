package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class Database {
    // Database file path - will be created in the working directory
    private static final String URL = "jdbc:sqlite:database.db";

    // Static block to ensure driver is loaded at class loading time
    static {
        try {
            Class.forName("org.sqlite.JDBC");
            System.out.println("SQLite JDBC driver loaded successfully");
        } catch (ClassNotFoundException e) {
            System.err.println("ERROR: Failed to load SQLite JDBC driver");
            e.printStackTrace();
            throw new ExceptionInInitializerError("SQLite JDBC driver not found");
        }
    }

    public static void initializeDB() {
        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {

            // Enable foreign key constraints
            stmt.execute("PRAGMA foreign_keys = ON");

            // Create Users Table
            String createUsersTable = """
                CREATE TABLE IF NOT EXISTS Users (
                    user_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL UNIQUE,
                    password TEXT NOT NULL,
                    country TEXT NOT NULL,
                    birth_date TEXT NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );
            """;

            // Create Games Table
            String createGamesTable = """
                CREATE TABLE IF NOT EXISTS Games (
                    game_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    game_name TEXT NOT NULL UNIQUE,
                    game_photo TEXT,
                    description TEXT
                );
            """;

            // Execute table creation statements
            stmt.execute(createUsersTable);
            stmt.execute(createGamesTable);
            // Insert default games
            insertDefaultGames();

            System.out.println("Database initialized successfully.");
        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    private static void insertDefaultGames() {
        String[] games = {
                "Bomberman","BrickBreaker","Catch","Dungeon","FlappyBird","Game2048","Pong","Snake","The Circles","Asteroid","Pacman"
        };

        String sql = "INSERT OR IGNORE INTO Games (game_name) VALUES (?)";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            for (String game : games) {
                pstmt.setString(1, game);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("Error inserting default games: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to insert default games", e);
        }
    }

    public static Connection connect() throws SQLException {
        Connection conn = DriverManager.getConnection(URL);
        // Set some practical connection settings
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA journal_mode = WAL");
            stmt.execute("PRAGMA synchronous = NORMAL");
        }
        return conn;
    }

    public static boolean registerUser(String username, String password, String country, String birthDate) {
        String sql = "INSERT INTO Users (username, password, country, birth_date) VALUES (?, ?, ?, ?)";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, country);
            pstmt.setString(4, birthDate);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Registration Error: " + e.getMessage());
            return false;
        }
    }

    public static Map<String, Object> loadUserByUsername(String username) {
        String sql = "SELECT user_id, username, country, birth_date, created_at FROM Users WHERE username = ?";
        Map<String, Object> userData = new HashMap<>();

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                userData.put("user_id", rs.getInt("user_id"));
                userData.put("username", rs.getString("username"));
                userData.put("country", rs.getString("country"));
                userData.put("birth_date", rs.getString("birth_date"));
                userData.put("created_at", rs.getString("created_at"));
            }
        } catch (SQLException e) {
            System.err.println("Error loading user: " + e.getMessage());
            e.printStackTrace();
        }

        return userData.isEmpty() ? null : userData;
    }

    public static boolean isDriverAvailable() {
        try {
            Class.forName("org.sqlite.JDBC");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}