package application;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.User;
import utils.Database;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

public class UserDirectoryController {

    @FXML
    private ScrollPane usersScrollPane;
    @FXML
    private VBox usersContainer;
    @FXML
    private Label titleLabel;

    private List<User> users = new ArrayList<>();

    @FXML
    public void initialize() {
        loadUsers();
        displayUsers();
    }

    private void loadUsers() {
        users.clear();
        List<Map<String, String>> usersData = Database.loadAllUsersBasicInfo();

        if (usersData != null) {
            for (Map<String, String> userData : usersData) {
                User user = new User(
                        0,
                        userData.get("username"),
                        "",
                        userData.get("country"),
                        "",
                        ""
                );
                user.setAvatarPath(userData.get("avatar_path")); // <- Fix applied here
                users.add(user);
            }
        }
    }

    private void displayUsers() {
        usersContainer.getChildren().clear();

        if (users.isEmpty()) {
            Label noUsersLabel = new Label("No users found");
            noUsersLabel.setStyle("-fx-font-size: 16; -fx-text-fill: #7F8C8D;");
            usersContainer.getChildren().add(noUsersLabel);
            return;
        }

        for (User user : users) {
            HBox userBox = createUserBox(user);
            usersContainer.getChildren().add(userBox);
        }
    }

    private HBox createUserBox(User user) {
        HBox userBox = new HBox(15);
        userBox.setAlignment(Pos.CENTER_LEFT);
        userBox.setPadding(new Insets(10));
        userBox.setStyle("-fx-background-color: #ECF0F1; -fx-border-color: #BDC3C7; -fx-border-width: 0 0 1 0;");

        // Create avatar ImageView
        ImageView avatarView = new ImageView();
        avatarView.setFitWidth(40);
        avatarView.setFitHeight(40);
        avatarView.setPreserveRatio(true);

        // Default avatar
        String defaultAvatarPath = "/pictures/default_avatar.png";

        try {
            String avatarPath = user.getAvatarPath();
            if (avatarPath != null && !avatarPath.isEmpty()) {
                InputStream imageStream = getClass().getResourceAsStream(avatarPath);
                if (imageStream != null) {
                    avatarView.setImage(new Image(imageStream));
                } else {
                    // Load default avatar
                    InputStream defaultStream = getClass().getResourceAsStream(defaultAvatarPath);
                    if (defaultStream != null) {
                        avatarView.setImage(new Image(defaultStream));
                    }
                }
            } else {
                // Load default avatar
                InputStream defaultStream = getClass().getResourceAsStream(defaultAvatarPath);
                if (defaultStream != null) {
                    avatarView.setImage(new Image(defaultStream));
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading avatar for " + user.getUsername() + ": " + e.getMessage());
            // Try to load default avatar on error
            try {
                InputStream defaultStream = getClass().getResourceAsStream(defaultAvatarPath);
                if (defaultStream != null) {
                    avatarView.setImage(new Image(defaultStream));
                }
            } catch (Exception ex) {
                System.err.println("Error loading default avatar: " + ex.getMessage());
            }
        }

        // Create user info VBox
        VBox userInfo = new VBox(5);

        Label usernameLabel = new Label(user.getUsername());
        usernameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");

        Label countryLabel = new Label("Country: " + user.getCountry());

        userInfo.getChildren().addAll(usernameLabel, countryLabel);

        userBox.getChildren().addAll(avatarView, userInfo);
        return userBox;
    }

    public void closeWindow() {
        Stage stage = (Stage) usersScrollPane.getScene().getWindow();
        stage.close();
    }
}