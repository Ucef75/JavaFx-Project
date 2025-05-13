package application;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SceneManager {
    public static void switchToScene(String fxmlPath, Stage currentStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fxmlPath));
        Parent root = loader.load();
        Stage newStage = new Stage();
        newStage.setScene(new Scene(root));
        newStage.show();

        // Close current window if provided
        if (currentStage != null) {
            currentStage.close();
        }
    }

    public static void switchToScene(String fxmlPath) throws Exception {
        switchToScene(fxmlPath, null);
    }
}