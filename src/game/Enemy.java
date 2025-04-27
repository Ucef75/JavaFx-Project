package game;

public class Enemy extends Entity {
    private int damage;

    public Enemy(int x, int y) {
        super(x, y);
        this.damage = 10;
    }

    public void moveUp() {
        if (y > 0) y--;
    }

    public void moveDown() {
        if (y < 9) y++;
    }

    public void moveLeft() {
        if (x > 0) x--;
    }

    public void moveRight() {
        if (x < 9) x++;
    }

    public int getDamage() {
        return damage;
    }
}