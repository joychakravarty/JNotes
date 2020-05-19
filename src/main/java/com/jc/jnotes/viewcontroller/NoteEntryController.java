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

import java.net.URL;
import java.util.ResourceBundle;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;

import com.jc.jnotes.JNotesApplication;
import com.jc.jnotes.helper.AlertHelper;
import com.jc.jnotes.model.NoteEntry;
import com.jc.jnotes.service.ControllerService;
import com.jc.jnotes.service.ControllerServiceException;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

/**
 * 
 * @author Joy C
 */
public class NoteEntryController implements Initializable {

    public static final String MODE_ADD = "MODE_ADD";
    public static final String MODE_EDIT = "MODE_EDIT";

    private NoteEntry noteEntry;
    // private ObservableList<NoteEntry> noteEntryList;
    private String mode;

    // To Be Set By Caller
    private Stage parentStage;

    // Spring Dependencies
    private ControllerService service;
    private AlertHelper alertHelper;

    private Runnable runAfter;

    @FXML
    private TextField keyField;
    @FXML
    private TextField valueField;
    @FXML
    private TextArea infoField;

    @FXML
    private Button saveButton;

    @FXML
    private Button cancelButton;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        prepareDependencies();
        addAccelerators();
    }

    private void prepareDependencies() {
        ApplicationContext applicationContext = JNotesApplication.getAppicationContext();
        service = applicationContext.getBean(ControllerService.class);
        alertHelper = applicationContext.getBean(AlertHelper.class);
    }

    private void addAccelerators() {
        Platform.runLater(() -> {
            saveButton.getScene().getAccelerators().put(new KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN), () -> {
                saveButton.fire();
            });
        });

        Platform.runLater(() -> {
            Scene scene = cancelButton.getScene();
            scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
                public void handle(KeyEvent ke) {
                    if (ke.getCode() == KeyCode.ESCAPE) {
                        System.out.println("Key Pressed: " + ke.getCode());
                        parentStage.close();
                    }
                }
            });
        });

    }

    public void setParentStage(Stage parentStage) {
        this.parentStage = parentStage;
    }

    public void setRunAfter(Runnable runAfter) {
        this.runAfter = runAfter;
    }

    public void setNoteEntry(NoteEntry noteEntry) {
        this.noteEntry = noteEntry;

        keyField.setText(noteEntry.getKey());
        valueField.setText(noteEntry.getValue());
        infoField.setText(noteEntry.getInfo());
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    @FXML
    private void saveNoteEntry() {
        if (isInputValid()) {
            noteEntry.setKey(keyField.getText());
            noteEntry.setValue(valueField.getText());
            noteEntry.setInfo(infoField.getText());
            try {
                if (MODE_ADD.equals(mode)) {
                    service.addNoteEntry(noteEntry);
                } else {
                    service.editNoteEntry(noteEntry);
                }
                runAfter.run();
            } catch (ControllerServiceException ex) {
                alertHelper.showAlertWithExceptionDetails(parentStage, ex, "Failed to save", "");
            }
            parentStage.close();
        }
    }

    @FXML
    private void cancel() {
        parentStage.close();
    }

    private boolean isInputValid() {
        if (StringUtils.isBlank(keyField.getText())) {
            alertHelper.showErrorAlert(parentStage, null, "Key cannot be blank");
            return false;
        } else {
            return true;
        }
    }

}