package model;

import javafx.application.Application;

public class Game {
    private int id;
    private String name;
    private String description;
    private String imagePath;
    private Class<? extends Application> gameClass;

    // Constructor for database-loaded games
    public Game(int id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.imagePath = "";
        this.gameClass = null;
    }

    // Constructor for game library
    public Game(String name, String description, String imagePath, Class<? extends Application> gameClass) {
        this.id = -1; // Default ID for non-database games
        this.name = name;
        this.description = description;
        this.imagePath = imagePath;
        this.gameClass = gameClass;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getImagePath() {
        return imagePath;
    }

    public Class<? extends Application> getGameClass() {
        return gameClass;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public void setGameClass(Class<? extends Application> gameClass) {
        this.gameClass = gameClass;
    }

    @Override
    public String toString() {
        return name;
    }
}