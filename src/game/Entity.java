package game;


public abstract class Entity {
    protected int x, y;

    public Entity(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public abstract void moveUp();

    public abstract void moveDown();

    public abstract void moveLeft();

    public abstract void moveRight();

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
