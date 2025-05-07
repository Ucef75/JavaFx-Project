package color_switch.obstacles;

import color_switch.elements.Ball;
import color_switch.obstacles.controllers.TriangleController;

public class Triangle extends Obstacle {
    private TriangleController triangleController;

    public Triangle() {
        // Load the FXML and set 'pane' in Parent
        loadFXMLtoPane("/obstacles/fxml/triangle.fxml");

        triangleController = (TriangleController) controller;
        loadStar(triangleController.star);
    }

    @Override
    public int hasCollided(Ball b) {
        return 0;
    }
}
