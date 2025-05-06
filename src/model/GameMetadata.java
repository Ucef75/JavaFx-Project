package model;

public class GameMetadata {
    private String gameId;
    private String title;
    private String description;
    private String iconPath;
    private String launchClass; // Fully qualified class name to launch the game
    private boolean isInstalled;
    private boolean isNew;
    
    // Constructor, getters and setters
    public GameMetadata(String gameId, String title, String description, 
                       String iconPath, String launchClass) {
        this.gameId = gameId;
        this.title = title;
        this.description = description;
        this.iconPath = iconPath;
        this.launchClass = launchClass;
        this.isInstalled = true; // Default to true for locally available games
        this.isNew = false;
    }
    
    // Add all getters and setters here
    // ...
}