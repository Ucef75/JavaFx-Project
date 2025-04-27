package model;

public class Game {
    private String name;
    private String description;
    
    public Game(String name, String description) {
        this.name = name;
        this.description = description;
    }
    
    // Getters
    public String getName() { return name; }
    public String getDescription() { return description; }
}