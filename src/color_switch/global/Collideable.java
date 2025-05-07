package color_switch.global;

import color_switch.elements.Ball;
import javafx.geometry.Bounds;

public interface Collideable {
    Bounds getBounds();
    int hasCollided(Ball b);
}
