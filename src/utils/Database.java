package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

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

            // Create UserScores Table
            String createUserScoresTable = """
                CREATE TABLE IF NOT EXISTS UserScores (
                    user_score_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    game_id INTEGER NOT NULL,
                    score INTEGER DEFAULT 0,
                    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE,
                    FOREIGN KEY (game_id) REFERENCES Games(game_id) ON DELETE CASCADE,
                    UNIQUE (user_id, game_id)
                );
            """;

            // Execute table creation statements
            stmt.execute(createUsersTable);
            stmt.execute(createGamesTable);
            stmt.execute(createUserScoresTable);

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
            "Catch the treasure", "8024", "Pacman", "Snake",
            "X/O", "Space AIRCRAFT", "Connect 4"
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

    // Additional utility method to check if driver is available
    public static boolean isDriverAvailable() {
        try {
            Class.forName("org.sqlite.JDBC");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}