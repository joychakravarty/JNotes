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
package com.jc.jnotes.viewcontroller;

import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.jc.jnotes.helper.AlertHelper;
import com.jc.jnotes.service.ControllerService;

import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextInputDialog;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 *
 * @author Joy C
 * 
 *  To make NotesController less monolithic, this class takes away the functionality 
 *  of Add/Rename/Delete Notebooks from NotesController.
 * 
 *
 */
public class NotebookActions {

    private static final String NOTEBOOK_DELETE_STATUS_NOTIFICATION = "Success: Notebook Deleted.";

    private final ComboBox<String> notebookComboBox;
    private final Stage parentStage;
    private final Text notificationText;
    private final AlertHelper alertHelper;
    private final ControllerService service;

    public NotebookActions(ControllerService service, AlertHelper alertHelper, Stage parentStage, ComboBox<String> notebookComboBox,
            Text notificationText) {
        this.service = service;
        this.parentStage = parentStage;
        this.notebookComboBox = notebookComboBox;
        this.notificationText = notificationText;
        this.alertHelper = alertHelper;
    }

    protected void deleteNotebook() {
        try {
            if (notebookComboBox.getItems().size() == 1) {
                alertHelper.showErrorAlert(parentStage, "Invalid Operation", "Cannot delete the only Notebook");

            } else {
                String notebookToBeDeleted = notebookComboBox.getSelectionModel().getSelectedItem();
                Optional<ButtonType> result = alertHelper.showDefaultConfirmation(parentStage,
                        "Delete Notebook: " + notebookToBeDeleted + "?", "Note: This is will delete all the data within this notebook!");

                if (result.get() == ButtonType.OK) {
                    service.deleteNotebook(notebookToBeDeleted);
                    notebookComboBox.getItems().remove(notebookToBeDeleted);
                    notebookComboBox.getSelectionModel().select(0);
                    notificationText.setText(NOTEBOOK_DELETE_STATUS_NOTIFICATION);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            alertHelper.showAlertWithExceptionDetails(parentStage, ex, "Failed to delete Notebook", "");
        }
    }

    protected void renameNotebook() {
        String notebookToBeRenamed = notebookComboBox.getSelectionModel().getSelectedItem();
        TextInputDialog dialog = new TextInputDialog();
        dialog.setHeaderText("Rename Notebook: " + notebookToBeRenamed);
        dialog.setContentText("New Notebook Name");
        dialog.showAndWait().ifPresent(newNotebookName -> {
            if (StringUtils.isNotBlank(newNotebookName) && notebookComboBox.getItems().indexOf(newNotebookName) == -1) {
                try {
                    service.renameNotebook(notebookToBeRenamed, newNotebookName);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    alertHelper.showAlertWithExceptionDetails(parentStage, ex, "Failed to Rename Notebook", "");
                    return;
                }

                int index = notebookComboBox.getItems().indexOf(notebookToBeRenamed);
                notebookComboBox.getItems().add(index, newNotebookName);
                notebookComboBox.getItems().remove(notebookToBeRenamed);
                notebookComboBox.getSelectionModel().select(index);
                return;
            } else {
                alertHelper.showErrorAlert(parentStage, "Invalid operation", "Please enter a valid Notebook name");
            }
        });
    }

    protected void addNewNotebook() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setHeaderText("Add New Notebook");
        dialog.setContentText("New Notebook Name");
        dialog.showAndWait().ifPresent(newNotebookName -> {
            if (StringUtils.isNotBlank(newNotebookName) && notebookComboBox.getItems().indexOf(newNotebookName) == -1) {
                try {
                    service.addNotebook(newNotebookName);
                } catch (InvalidPathException ex) {
                    ex.printStackTrace();
                    alertHelper.showAlertWithExceptionDetails(parentStage, ex, "Failed to Add Notebook", "");
                    return;
                }
                int index = notebookComboBox.getItems().size() - 1;
                notebookComboBox.getItems().add(index, newNotebookName);
                notebookComboBox.getSelectionModel().select(index);
            } else {
                alertHelper.showErrorAlert(parentStage, "Invalid operation", "Please enter a valid Notebook name");
            }
        });
    }

}