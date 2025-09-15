package ee.vikk.kanban;

import ee.vikk.kanban.database.DatabaseConnection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Main JavaFX Application class
 */
public class KanbanApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        // Initialize database
        try {
            DatabaseConnection.initializeDatabase();
        } catch (SQLException e) {
            System.err.println("Failed to initialize database: " + e.getMessage());
            return;
        }

        FXMLLoader fxmlLoader = new FXMLLoader(KanbanApplication.class.getResource("/fxml/main.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
        
        stage.setTitle("KanBan Board");
        stage.setScene(scene);
        stage.setMinWidth(800);
        stage.setMinHeight(600);
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        DatabaseConnection.closeConnection();
    }

    public static void main(String[] args) {
        launch();
    }
}
