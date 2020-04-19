package com.jc.jnotes;

import java.io.InputStream;

import com.jc.jnotes.helper.AlertHelper;
import com.jc.jnotes.viewcontroller.NotesController;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * 
 * @author Joy
 */
public class JNotesApplication extends Application {

    private final AlertHelper alertHelper = new AlertHelper();

    @Override
    public void start(Stage stage) {
        stage.setTitle(JNotesPreferences.DEFAULT_APP_NAME);

        InputStream iconInputStream = JNotesApplication.class.getResourceAsStream("/images/spiral-booklet.png");
        if (iconInputStream != null) {
            stage.getIcons().add(new Image(iconInputStream));
        }

        loadNotes(stage);
    }

    public static void main(String[] args) {
        launch(args);
    }

    public void loadNotes(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(JNotesApplication.class.getResource("viewcontroller/Notes.fxml"));
            BorderPane rootPane = (BorderPane) loader.load();
            Scene scene = new Scene(rootPane);
            stage.setScene(scene);
            stage.show();
            NotesController notesController = loader.getController();
            notesController.setParentStage(stage);
        } catch (Exception ex) {
            ex.printStackTrace();
            alertHelper.showErrorAlert(stage, "Could not load notes", ex.getMessage());
        }
    }
}
