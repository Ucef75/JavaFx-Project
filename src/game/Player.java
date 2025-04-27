package game;

public class Player {
    private int x, y;
    private int health;
    private int score;
    private Dungeon dungeon;

    public Player(int startX, int startY, Dungeon dungeon) {
        this.x = startX;
        this.y = startY;
        this.health = 100; // Default health
        this.score = 0;
        this.dungeon = dungeon;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getHealth() {
        return health;
    }

    public int getScore() {
        return score;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void addScore(int score) {
        this.score += score;
    }

    public void takeDamage(int damage) {
        this.health -= damage;
    }

    public void moveUp() {
        if (y > 0) {  // Ensure player doesn't go out of bounds
            y--;
            interactWithTile();
        }
    }

    public void moveDown() {
        if (y < dungeon.getHeight() - 1) {  // Ensure player doesn't go out of bounds
            y++;
            interactWithTile();
        }
    }

    public void moveLeft() {
        if (x > 0) {  // Ensure player doesn't go out of bounds
            x--;
            interactWithTile();
        }
    }

    public void moveRight() {
        if (x < dungeon.getWidth() - 1) {  // Ensure player doesn't go out of bounds
            x++;
            interactWithTile();
        }
    }

    private void interactWithTile() {
        char tile = dungeon.getTile(x, y);
        
        if (tile == 'T') {  // If the player is on a treasure
            addScore(10);
            dungeon.setTile(x, y, '.');  // Replace treasure with empty space
            dungeon.collectTreasure();
        } else if (tile == 'E') {  // If the player is on an enemy
            takeDamage(10);
            moveAwayFromEnemy();  // Move player away from enemy to prevent continuous damage
        }
    }

    private void moveAwayFromEnemy() {
        // Try to move away from the enemy by moving the player in the opposite direction
        // You could add more logic here to randomly move away from the enemy if needed.
        if (x > 0) {
            x--;
        } else if (x < dungeon.getWidth() - 1) {
            x++;
        }

        if (y > 0) {
            y--;
        } else if (y < dungeon.getHeight() - 1) {
            y++;
        }
    }
}
