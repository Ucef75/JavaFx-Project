package asteroid;

import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Polygon;


public class Projectiles extends Character{

    public Projectiles(int x, int y) {
        super(new Polygon(2, -2, 2, 2, -2, 2, -2, -2), x, y);

        Stop[] stops = new Stop[] {
                new Stop(0, Color.YELLOW),
                new Stop(0.5, Color.GREEN),
                new Stop(1, Color.WHITE)
        };
        LinearGradient gradient = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE, stops);

        // Apply the gradient as the fill for the asteroid
        super.getCharacter().setFill(gradient);

    }
}
