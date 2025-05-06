package asteroid;


import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Polygon;



public class Ship extends Character{
    private Polygon character;
    private Point2D point2D;

    public Ship(int x, int y) {
        super(new Polygon(-22, -22, 27, 0, -22,22), x, y);
        this.character = (Polygon) getCharacter(); // Cast the character to Polygon
        setShipBackground();
    }

    private void setShipBackground() {
        // Load the image for the ship's background
    	Image backgroundImage = new Image(getClass().getResourceAsStream("spaceship.png"));
        // Apply the image as a pattern fill for the ship
        character.setFill(new ImagePattern(backgroundImage));
    }
}
