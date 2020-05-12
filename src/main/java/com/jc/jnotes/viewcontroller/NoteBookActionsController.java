package com.jc.jnotes.viewcontroller;

import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.jc.jnotes.helper.AlertHelper;
import com.jc.jnotes.helper.IOHelper;

import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextInputDialog;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 *
 * @author Joy C
 * 
 * To make NotesController less monolithic, this class takes away the 
 * functionality of Add/Rename/Delete NoteBooks from NotesController.
 * 
 *
 */
public class NoteBookActionsController {
    
    private static final String NOTEBOOK_DELETE_STATUS_NOTIFICATION = "Success: NoteBook Deleted.";
 
    private final ComboBox<String> noteBookComboBox;
    private final Stage parentStage;
    private final Text notificationText;
    
    public NoteBookActionsController(Stage parentStage, ComboBox<String> noteBookComboBox, Text notificationText) {
        this.parentStage = parentStage;
        this.noteBookComboBox = noteBookComboBox;
        this.notificationText = notificationText;
    }

    
    protected void deleteNoteBook() {
        try {
            if (noteBookComboBox.getItems().size() == 1) {
                AlertHelper.showErrorAlert(parentStage, "Invalid Operation", "Cannot delete the only NoteBook");

            } else {
                String noteBookToBeDeleted = noteBookComboBox.getSelectionModel().getSelectedItem();
                Optional<ButtonType> result = AlertHelper.showDefaultConfirmation(parentStage,
                        "Delete NoteBook: " + noteBookToBeDeleted + "?", "Note: This is will delete all the data within this noteBook!");

                if (result.get() == ButtonType.OK) {
                    noteBookComboBox.getItems().remove(noteBookToBeDeleted);
                    noteBookComboBox.getSelectionModel().select(0);
                    IOHelper.deleteNoteBook(noteBookToBeDeleted);
                    notificationText.setText(NOTEBOOK_DELETE_STATUS_NOTIFICATION);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            AlertHelper.showAlertWithExceptionDetails(parentStage, ex, "Failed to delete NoteBook", "");
        }
    }

    protected void renameNoteBook() {
        String noteBookToBeRenamed = noteBookComboBox.getSelectionModel().getSelectedItem();
        TextInputDialog dialog = new TextInputDialog();
        dialog.setHeaderText("Rename NoteBook: " + noteBookToBeRenamed);
        dialog.setContentText("New NoteBook Name");
        dialog.showAndWait().ifPresent(newNoteBookName -> {
            if (StringUtils.isNotBlank(newNoteBookName) && noteBookComboBox.getItems().indexOf(newNoteBookName) == -1) {
                try {
                    IOHelper.moveNoteBook(noteBookToBeRenamed, newNoteBookName);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    AlertHelper.showAlertWithExceptionDetails(parentStage, ex, "Failed to Rename NoteBook", "");
                    return;
                }

                int index = noteBookComboBox.getItems().indexOf(noteBookToBeRenamed);
                noteBookComboBox.getItems().add(index, newNoteBookName);
                noteBookComboBox.getItems().remove(noteBookToBeRenamed);
                noteBookComboBox.getSelectionModel().select(index);
            } else {
                AlertHelper.showErrorAlert(parentStage, "Invalid operation", "Please enter a valid NoteBook name");
            }
        });
    }

    protected void addNewNoteBook() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setHeaderText("Add New NoteBook");
        dialog.setContentText("New NoteBook Name");
        dialog.showAndWait().ifPresent(newNoteBookName -> {
            if (StringUtils.isNotBlank(newNoteBookName) && noteBookComboBox.getItems().indexOf(newNoteBookName) == -1) {
                try {
                    IOHelper.addNoteBook(newNoteBookName);
                } catch (InvalidPathException ex) {
                    ex.printStackTrace();
                    AlertHelper.showAlertWithExceptionDetails(parentStage, ex, "Failed to Add NoteBook", "");
                    return;
                }
                int index = noteBookComboBox.getItems().size() - 1;
                noteBookComboBox.getItems().add(index, newNoteBookName);
                noteBookComboBox.getSelectionModel().select(index);
            } else {
                AlertHelper.showErrorAlert(parentStage, "Invalid operation", "Please enter a valid NoteBook name");
            }
        });
    }

}