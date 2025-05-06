package application;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Load the start view FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../view/interface.fxml"));
            Parent root = loader.load();
            
            // Set up the primary stage
            Scene scene = new Scene(root);
            primaryStage.setTitle("Pixely - Game Platform");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false); // Consider making non-resizable for better control
            primaryStage.show();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
            System.out.println("SQLite JDBC driver found successfully!");
            // List all available JDBC drivers
            java.util.Enumeration<Driver> drivers = DriverManager.getDrivers();
            while (drivers.hasMoreElements()) {
                System.out.println("Available driver: " + drivers.nextElement());
            }
        } catch (ClassNotFoundException e) {
            System.err.println("ERROR: SQLite JDBC driver NOT found in classpath");
            e.printStackTrace();
        }
    }
}