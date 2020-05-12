package com.jc.jnotes;

import static com.jc.jnotes.JNotesConstants.*;
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
 * This class launches the JavaFX UI for JNotes 
 * 
 * @author Joy
 */
public class JNotesApplication extends Application {

    @Override
    public void start(Stage stage) {
        stage.setTitle(APP_NAME);

        InputStream iconInputStream = JNotesApplication.class.getResourceAsStream("/images/spiral-booklet.png");
        if (iconInputStream != null) {
            stage.getIcons().add(new Image(iconInputStream));
        }

        loadNotes(stage);
    }

    /**
     * This main method is NOT the starting point for the jar file. @see JNotesMain
     * @param args
     */
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
            AlertHelper.showErrorAlert(stage, "Could not load notes", ex.getMessage());
        }
    }
}
