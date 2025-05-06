package application;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SceneManager {
    public static void switchToScene(String fxmlFile) throws Exception {
        Parent root = FXMLLoader.load(SceneManager.class.getResource(fxmlFile));
        Stage stage = (Stage) SceneManager.getCurrentStage();
        stage.setScene(new Scene(root));
        stage.show();
    }

    private static Object getCurrentStage() {
        // You'll need to implement this based on your application structure
        // This is a placeholder - in a real app you might track the current stage
        return null;
    }
}