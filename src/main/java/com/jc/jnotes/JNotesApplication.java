package com.jc.jnotes;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.io.IOException;

import com.jc.jnotes.viewcontroller.NotesController;

/**
 * 
 * @author Joy 
 */
public class JNotesApplication extends Application {

    @Override
    public void start(Stage stage) {
        stage.setTitle("JNote");
        
        try(FileInputStream fis = new FileInputStream("src/main/resources/images/spiral-booklet.png")){
            stage.getIcons().add(new Image(fis));
        }catch(IOException ioEx) {
            ioEx.printStackTrace();
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
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Could not load notes");
            alert.setContentText(ex.getMessage());
            alert.showAndWait();
        }
    }
}
