package games;

public class Dungeon {
    private int width;
    private int height;
    private char[][] map;

    // Attributs du joueur intégrés ici
    private int playerX;
    private int playerY;
    private int health = 100;
    private int score = 0;

    public Dungeon(int width, int height) {
        this.width = width;
        this.height = height;
        this.map = new char[width][height];
        generateMap();
        this.playerX = 1;
        this.playerY = 1;
    }

    private void generateMap() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (x == 0 || y == 0 || x == width - 1 || y == height - 1) {
                    map[x][y] = '#'; // murs
                } else {
                    map[x][y] = '.'; // sol
                }
            }
        }
        map[3][3] = 'T'; // trésor
        map[4][4] = 'E'; // ennemi
    }

    public char getTile(int x, int y) {
        return map[x][y];
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    // === Joueur ===
    public int getPlayerX() {
        return playerX;
    }

    public int getPlayerY() {
        return playerY;
    }

    public int getHealth() {
        return health;
    }

    public int getScore() {
        return score;
    }

    public void movePlayer(int dx, int dy) {
        int newX = playerX + dx;
        int newY = playerY + dy;

        if (map[newX][newY] != '#') {
            playerX = newX;
            playerY = newY;

            char tile = map[newX][newY];
            if (tile == 'T') {
                score += 10;
                map[newX][newY] = '.';
            } else if (tile == 'E') {
                health -= 20;
                map[newX][newY] = '.';
            }
        }
    }
}
