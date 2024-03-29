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
 * along with JNotes.  If not, see <https://www.gnu.org/licenses/>.
 *
 *
 */
package com.jc.jnotes.helper;

import com.jc.jnotes.JNotesApplication;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.jc.jnotes.JNotesConstants.APP_NAME;
import static com.jc.jnotes.JNotesConstants.CURRENT_VERSION;

/**
 * Helper class to display Alert Dialogs
 *
 * @author Joy C
 */
public class AlertHelper {

    private static final String INFO_HEADER = "JNotes %s - your quick-access lookup notes.\n%s - %s";

    /**
     * Displays Error Alert Dialog with the exception details. Intended for displaying Exception details to the user. Not
     * intended for user errors, like validation error.
     *
     * @param parentStage
     * @param ex
     * @param headerText
     * @param contextText
     */
    public void showAlertWithExceptionDetails(Stage parentStage, Throwable ex, String headerText, String contextText) {
        Alert alert = new Alert(AlertType.ERROR);
        setAlertFont(alert);
        alert.setTitle("Exception Dialog");
        alert.setHeaderText(headerText);
        alert.setContentText(contextText);
        alert.initOwner(parentStage);

        // Create expandable Exception.
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        String exceptionText = sw.toString();

        Label label = new Label("The exception stacktrace was:");

        TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(label, 0, 0);
        expContent.add(textArea, 0, 1);

        // Set expandable Exception into the dialog pane.
        alert.getDialogPane().setExpandableContent(expContent);

        alert.showAndWait();
    }

    private void setAlertFont(Alert alert) {
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(
                JNotesApplication.getResource("viewcontroller/notes.css").toExternalForm());
        dialogPane.getStyleClass().add("root");
    }

    /**
     * Displays Error Alert Dialog for User errors or Validation Errors not meant to be used in ExceptionHandling.
     *
     * @param parentStage
     * @param headerText
     * @param contentText
     */
    public void showErrorAlert(Stage parentStage, String headerText, String contentText) {
        Alert alert = new Alert(AlertType.ERROR);
        setAlertFont(alert);
        // alert.initOwner(parentStage);
        alert.setTitle("Error Dialog");
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);

        alert.showAndWait();
    }

    /**
     * Displays confirmation type alert dialog
     *
     * @param parentStage
     * @param headerText
     * @param contentText
     * @return
     */
    public Optional<ButtonType> showDefaultConfirmation(Stage parentStage, String headerText, String contentText) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        setAlertFont(alert);
        alert.setTitle("Confirmation Dialog");
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);

        return alert.showAndWait();
    }

    public void showInfoDialog(Stage parentStage, String header, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        setAlertFont(alert);
        // alert.initOwner(parentStage);
        alert.setTitle("Information Dialog");
        alert.setHeaderText(header);
        alert.setContentText(message);

        alert.showAndWait();
    }

    public void showWarningDialog(Stage parentStage, String header, String message) {
        Alert alert = new Alert(AlertType.WARNING);
        setAlertFont(alert);
        // alert.initOwner(parentStage);
        alert.setTitle("Warning Dialog");
        alert.setHeaderText(header);
        alert.setContentText(message);

        alert.showAndWait();
    }

    public void showAboutJNotesDialog() {
        Alert alert = new Alert(AlertType.INFORMATION);
        setAlertFont(alert);
        alert.setTitle("About " + APP_NAME);
        alert.getDialogPane().setMinWidth(500);

        alert.setWidth(500);

        String javaVersion = System.getProperty("java.version");
        String runtimeName = System.getProperty("java.runtime.name");
        String version = StringUtils.isBlank(CURRENT_VERSION) ? "" : CURRENT_VERSION;

        String headerText = String.format(INFO_HEADER, version, runtimeName, javaVersion);
        alert.setHeaderText(headerText);

        String contentText = new BufferedReader(new InputStreamReader(JNotesApplication.getResourceAsStream("/About.txt"))).lines()
                .collect(Collectors.joining("\n"));

        Text text = new Text(contentText);
        text.setFont(new Font("Verdana", 11));
        TextFlow textFlow = new TextFlow();

        textFlow.getChildren().addAll(text);

        alert.getDialogPane().setContent(textFlow);

        alert.showAndWait();
    }

}