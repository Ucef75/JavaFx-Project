package game;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Dungeon {
    private int width, height;
    private char[][] grid;
    private List<Enemy> enemies;
    private int treasureCount;
    private Random random;

    public Dungeon(int width, int height) {
        this.width = width;
        this.height = height;
        this.grid = new char[width][height];
        this.enemies = new ArrayList<>();
        this.random = new Random();
        generateDungeon();
    }

    private void generateDungeon() {
        // Generate walls and empty space
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (i == 0 || j == 0 || i == width - 1 || j == height - 1) {
                    grid[i][j] = '#';  // Wall
                } else {
                    grid[i][j] = '.';  // Floor
                }
            }
        }

        // Place some treasures (T)
        treasureCount = 5;
        for (int i = 0; i < treasureCount; i++) {
            int x = random.nextInt(width - 2) + 1;
            int y = random.nextInt(height - 2) + 1;
            grid[x][y] = 'T';
        }

        // Place some enemies (E)
        for (int i = 0; i < 3; i++) {
            int x = random.nextInt(width - 2) + 1;
            int y = random.nextInt(height - 2) + 1;
            enemies.add(new Enemy(x, y));
            grid[x][y] = 'E';
        }
    }

    public void update() {
        // Move enemies randomly
        for (Enemy enemy : enemies) {
            grid[enemy.getX()][enemy.getY()] = '.';
            int direction = random.nextInt(4);
            switch (direction) {
                case 0: enemy.moveUp(); break;
                case 1: enemy.moveDown(); break;
                case 2: enemy.moveLeft(); break;
                case 3: enemy.moveRight(); break;
            }
            grid[enemy.getX()][enemy.getY()] = 'E';
        }
    }

    public char getTile(int x, int y) {
        return grid[x][y];
    }

    public void setTile(int x, int y, char tile) {
        grid[x][y] = tile;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public List<Enemy> getEnemies() {
        return enemies;
    }

    public int getTreasureCount() {
        return treasureCount;
    }

    public void collectTreasure() {
        treasureCount--;
    }
}