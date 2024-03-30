/*
 * This file is part of JNotes. Copyright (C) 2020  Joy Chakravarty
 * 
 * JNotes is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JNotes is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 *  along with JNotes.  If not, see <https://www.gnu.org/licenses/>.
 * 
 * 
 */
package com.jc.jnotes;

import static com.jc.jnotes.AppConfig.APP_CONFIG;
import static com.jc.jnotes.JNotesConstants.APP_NAME;
import static com.jc.jnotes.JNotesConstants.CURRENT_VERSION;

import java.io.InputStream;
import java.net.URL;

import com.jc.jnotes.viewcontroller.NotesController;

import javafx.application.Application;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;

/**
 * This class launches the JavaFX UI for JNotes
 * 
 * @author Joy
 */
public class JNotesApplication extends Application {

    private static UserPreferences userPreferences = null;

    public static UserPreferences getUserPreferences() {
        return userPreferences;
    }

    private static HostServices hostServices;
    @Override
    public void start(Stage stage) {
        System.out.println("Starting JNotes...");
        String version = StringUtils.isBlank(CURRENT_VERSION) ? "" : (" " +CURRENT_VERSION);
        stage.setTitle(APP_NAME + version);
        InputStream iconInputStream = JNotesApplication.getResourceAsStream("/images/spiral-booklet.png");
        if (iconInputStream != null) {
            stage.getIcons().add(new Image(iconInputStream));
        }
        hostServices = getHostServices();
        loadNotes(stage);
    }

    @Override
    public void stop() {
        System.out.println("Stopping JNotes");
        APP_CONFIG.close();
        Platform.exit();
    }

    /**
     * This main method is NOT the starting point for the jar file. @see JNotesMain
     * 
     * @param args
     */
    public static void main(String[] args) {
        final String basePath;
        if(args != null && args.length > 0) {
            basePath = args[0];
        } else {
            basePath = null; //UserPreferences will pick user home in this case as base path.
        }
        userPreferences = new UserPreferences(basePath);
        System.out.println("UserPreferences is loaded with basePath: " + userPreferences.getBasePath());
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
            APP_CONFIG.getAlertHelper().showErrorAlert(stage, "Could not load notes", ex.getMessage());
        }
    }

    public static InputStream getResourceAsStream(String resouceName) {
        return JNotesApplication.class.getResourceAsStream(resouceName);
    }

    public static URL getResource(String resouceName) {
        return JNotesApplication.class.getResource(resouceName);
    }
    
    public static void openLink(String link) {
        hostServices.showDocument(link);
    }


}
