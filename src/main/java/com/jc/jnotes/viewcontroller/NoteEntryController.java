package com.jc.jnotes.viewcontroller;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import com.jc.jnotes.model.NoteEntry;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;

/**
 * 
 * @author Joy C
 */
public class NoteEntryController implements Initializable {

    private NoteEntry noteEntry;
    private Stage parentStage;
    private ObservableList<NoteEntry> noteEntryList;
    
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
        addAccelerators();
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
    
    public void setParentStage(Stage parentStage){
        this.parentStage = parentStage;
    }
    
    public void setNoteEntry(NoteEntry noteEntry) {
        this.noteEntry = noteEntry;

        keyField.setText(noteEntry.getKey());
        valueField.setText(noteEntry.getValue());
        infoField.setText(noteEntry.getInfo());
    }
    
    public void setNoteEntryList(ObservableList<NoteEntry> noteEntryList){
        this.noteEntryList = noteEntryList;
    }
    
    @FXML
    private void saveNoteEntry() {
        if (isInputValid()) {
            noteEntry.setKey(keyField.getText());
            noteEntry.setValue(valueField.getText());
            noteEntry.setInfo(infoField.getText());
            
            noteEntryList.add(noteEntry);
            parentStage.close();
        }
    }
    
    @FXML
    private void cancel() {
        parentStage.close();
    }

    private boolean isInputValid() {
        if (keyField.getText() == null || keyField.getText().length() == 0) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.initOwner(parentStage);
            alert.setTitle("Invalid NoteEntry");
            alert.setHeaderText("Key is blank");
            //alert.setContentText("Key cannot be empty");

            alert.showAndWait();
            
            return false; 
        }else{
            return true;
        }   
    }
}
