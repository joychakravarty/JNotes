package com.jc.jnotes;

import static com.jc.jnotes.JNotesConstants.APP_NAME;

import java.io.InputStream;
import java.net.URL;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.jc.jnotes.helper.AlertHelper;
import com.jc.jnotes.viewcontroller.NotesController;

import javafx.application.Application;
import javafx.application.Platform;
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

    private static AnnotationConfigApplicationContext applicationContext = null;

    private AlertHelper alertHelper = null;

    @Override
    public void start(Stage stage) {
        System.out.println("Starting JNotes...");
        applicationContext = new AnnotationConfigApplicationContext();
        applicationContext.register(AppConfig.class);
        applicationContext.refresh();
   
        alertHelper = applicationContext.getBean(AlertHelper.class);
        stage.setTitle(APP_NAME);

        InputStream iconInputStream = JNotesApplication.getResourceAsStream("/images/spiral-booklet.png");
        if (iconInputStream != null) {
            stage.getIcons().add(new Image(iconInputStream));
        }

        loadNotes(stage);
    }

    @Override
    public void stop() {
        System.out.println("Stopping JNotes");
        applicationContext.close();
        Platform.exit();
    }

    /**
     * This main method is NOT the starting point for the jar file. @see JNotesMain
     * 
     * @param args
     */
    public static void main(String[] args) {
        launch(args);
    }

    public void loadNotes(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(JNotesApplication.getResource("viewcontroller/Notes.fxml"));
            BorderPane rootPane = (BorderPane) loader.load();
            NotesController notesController = loader.getController();
            notesController.setParentStage(stage);
            Scene scene = new Scene(rootPane);
            stage.setScene(scene);
            stage.show();
        } catch (Exception ex) {
            ex.printStackTrace();
            alertHelper.showErrorAlert(stage, "Could not load notes", ex.getMessage());
        }
    }

    public static InputStream getResourceAsStream(String resouceName) {
        System.out.println("Loading: " + resouceName);
        return JNotesApplication.class.getResourceAsStream(resouceName);
    }

    public static URL getResource(String resouceName) {
        System.out.println("Loading: " + resouceName);
        return JNotesApplication.class.getResource(resouceName);
    }

    public static ApplicationContext getAppicationContext() {
        return applicationContext;
    }

}
