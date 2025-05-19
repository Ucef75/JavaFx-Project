package application;

import javafx.scene.Scene;

public class ThemeUtil {
    public static void applyTheme(Scene scene) {
        scene.getStylesheets().clear();
        String theme = SettingsManager.getInstance().getTheme();
        switch (theme) {
            case "Dark":
                scene.getStylesheets().add(ThemeUtil.class.getResource("/css/dark-theme.css").toExternalForm());
                break;
            case "Light":
                scene.getStylesheets().add(ThemeUtil.class.getResource("/css/light-theme.css").toExternalForm());
                break;
            case "High Contrast":
                scene.getStylesheets().add(ThemeUtil.class.getResource("/css/high-contrast-theme.css").toExternalForm());
                break;
            default:
                scene.getStylesheets().add(ThemeUtil.class.getResource("/css/default-theme.css").toExternalForm());
                break;
        }
    }
}