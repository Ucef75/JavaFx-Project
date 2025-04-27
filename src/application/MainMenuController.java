package application;

import javafx.fxml.FXML;

public class MainMenuController {
    private Main mainApp;
    
    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp;
    }
    
    @FXML
    private void launchRogueLike() {
        mainApp.launchGame("RogueLike");
    }
    
    @FXML
    private void launchGame2() {
        // Implement later
    }
    
    // Add methods for other games...
    
    @FXML
    private void handleLogout() {
        mainApp.showLoginView();
    }
}