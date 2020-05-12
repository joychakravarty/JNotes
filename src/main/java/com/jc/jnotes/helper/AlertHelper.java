package com.jc.jnotes.helper;

import static com.jc.jnotes.JNotesConstants.CURRENT_VERSION;
import static com.jc.jnotes.JNotesConstants.APP_NAME;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

/**
 * 
 * Helper class to display Alert Dialogs
 * 
 * @author Joy C
 *
 */
public final class AlertHelper {
    
    private AlertHelper() {
        
    }

    /**
     * Displays Error Alert Dialog with the exception details.
     * Intended for displaying Exception details to the user. Not intended for user errors, like validation error.
     * 
     * @param parentStage
     * @param ex
     * @param headerText
     * @param contextText
     */
    public static void showAlertWithExceptionDetails(Stage parentStage, Exception ex, String headerText, String contextText) {
        Alert alert = new Alert(AlertType.ERROR);
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
    
    /**
     * 
     * Displays Error Alert Dialog for User errors or Validation Errors not meant to be used in ExceptionHandling.
     * 
     * @param parentStage
     * @param headerText
     * @param contentText
     */
    public static void showErrorAlert(Stage parentStage, String headerText, String contentText) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.initOwner(parentStage);
        alert.setTitle("Exception Dialog");
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
    public static Optional<ButtonType> showDefaultConfirmation(Stage parentStage, String headerText, String contentText) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Confirmation Dialog");
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);

        return alert.showAndWait();
    }
    
    public static void showAboutJNotesDialog() {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("About " + APP_NAME);
        alert.setHeaderText("Author - Joy Chakravarty");
        if (StringUtils.isNotBlank(CURRENT_VERSION)) {
            alert.setContentText("Version: " + CURRENT_VERSION);
        }
        alert.showAndWait();
    }
    
    

}