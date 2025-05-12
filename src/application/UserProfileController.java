package application;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.User;

import java.io.IOException;

public class UserProfileController {

    @FXML
    private Label usernameLabel;

    @FXML
    private ImageView userAvatar;

    @FXML
    private Label userFullName;

    @FXML
    private Label joinDateLabel;

    @FXML
    private Label countryLabel;

    private User user;

    @FXML
    public void initialize() {
        // Plus rien à initialiser
    }

    public void setUser(User user) {
        this.user = user;

        usernameLabel.setText(user.getUsername() + "'s Profile");
        userFullName.setText(user.getUsername());
        joinDateLabel.setText(user.getCreated());
        countryLabel.setText(user.getCountry());

        // Avatar (à activer si tu as une image)
        // userAvatar.setImage(new Image("path/to/avatar.png"));
    }

    @FXML
    public void editProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/PixelySettings.fxml"));
            Scene scene = new Scene(loader.load());

            SettingsController controller = loader.getController();
            controller.setUser(user);

            Stage stage = new Stage();
            stage.setTitle("Edit Profile");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            setUser(user); // Rechargement des infos utilisateur
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void closeProfile() {
        Stage stage = (Stage) usernameLabel.getScene().getWindow();
        stage.close();
    }
}
