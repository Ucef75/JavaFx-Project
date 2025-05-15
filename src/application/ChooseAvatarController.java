package application;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.TilePane;
import javafx.stage.Stage;
import model.User;

import java.util.ArrayList;
import java.util.List;

public class ChooseAvatarController {

    @FXML
    private TilePane avatarTilePane;

    @FXML
    private Button confirmButton;

    @FXML
    private Button cancelButton;

    private User user;
    private String selectedAvatarPath;

    // Predefined avatar list
    private static final List<String> predefinedAvatars = new ArrayList<>();

    static {
        predefinedAvatars.add("/Avatar-photos/alien.jpg");
        predefinedAvatars.add("/Avatar-photos/astro.jpg");
        predefinedAvatars.add("/Avatar-photos/girl.jpg");
        predefinedAvatars.add("/Avatar-photos/gorilla.jpg");
        predefinedAvatars.add("/Avatar-photos/hipo.jpg");
        predefinedAvatars.add("/Avatar-photos/ninja.jpg");
        predefinedAvatars.add("/Avatar-photos/submarine.jpg");
        predefinedAvatars.add("/Avatar-photos/whitep.jpg");
    }

    @FXML
    public void initialize() {
        loadAvatars();
    }

    public void setUser(User user) {
        this.user = user;
    }

    private void loadAvatars() {
        avatarTilePane.getChildren().clear();

        for (String avatarPath : predefinedAvatars) {
            try {
                ImageView avatarView = new ImageView(new Image(getClass().getResource(avatarPath).toExternalForm()));
                avatarView.setFitWidth(100);
                avatarView.setFitHeight(100);
                avatarView.setPreserveRatio(true);
                avatarView.setOnMouseClicked(event -> selectAvatar(avatarPath));

                avatarTilePane.getChildren().add(avatarView);
            } catch (NullPointerException e) {
                System.err.println("Error: Avatar not found at path: " + avatarPath);
            }
        }
    }

    private void selectAvatar(String avatarPath) {
        selectedAvatarPath = avatarPath;

        // Highlight the selected avatar (optional)
        avatarTilePane.getChildren().forEach(node -> {
            if (node instanceof ImageView) {
                node.setStyle("-fx-border-color: transparent; -fx-border-width: 0;");
            }
        });
        avatarTilePane.getChildren().filtered(node -> {
            if (node instanceof ImageView) {
                ImageView imageView = (ImageView) node;
                return imageView.getImage().getUrl().contains(avatarPath);
            }
            return false;
        }).forEach(node -> node.setStyle("-fx-border-color: #27AE60; -fx-border-width: 3;"));
    }

    @FXML
    public void confirmSelection() {
        if (selectedAvatarPath != null) {
            user.setAvatarPath(selectedAvatarPath);

            // Close the dialog
            closeDialog();
        }
    }

    @FXML
    public void cancelSelection() {
        // Close the dialog without saving
        closeDialog();
    }

    private void closeDialog() {
        Stage stage = (Stage) confirmButton.getScene().getWindow();
        stage.close();
    }
}