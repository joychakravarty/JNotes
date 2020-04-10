package com.jc.jnotes.viewcontroller;

import com.jc.jnotes.JNotesApplication;
import com.jc.jnotes.model.NoteEntry;
import javafx.fxml.FXML;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import java.io.IOException;
import java.util.Optional;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import java.util.UUID;
import javafx.scene.control.Button;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.stage.Stage;
import javafx.application.Platform;
import javafx.scene.control.Alert.AlertType;

/**
 *
 * @author Joy C
 *
 */
public class NotesController {

    @FXML
    private TableView<NoteEntry> notesTable;
    @FXML
    private TableColumn<NoteEntry, String> keyColumn;
    @FXML
    private TableColumn<NoteEntry, String> valueColumn;
    @FXML
    private TextArea infoField;
    @FXML
    private Button newButton;
    @FXML
    private Button deleteButton;
    
    private final ObservableList<NoteEntry> noteEntryList = FXCollections.observableArrayList();
    private Stage parentStage;
    private Stage noteEntryStage;

    public void setParentStage(Stage parentStage) {
        this.parentStage = parentStage;
    }
    
    /**
     * @see
     * https://docs.oracle.com/javafx/2/ui_controls/table-view.htm#sthref119
     */
    @FXML
    private void initialize() {
        loadNoteEntries();

        notesTable.setEditable(true);

        keyColumn.setCellValueFactory(new PropertyValueFactory<>("key"));
        keyColumn.setCellFactory((tableCol) -> new NoteEntryEditingTableCell());
        keyColumn.setOnEditCommit(editEvent -> {
            String newKey = editEvent.getNewValue();
            ((NoteEntry) editEvent.getTableView().getItems().get(
                    editEvent.getTablePosition().getRow())).setKey(newKey);
        });

        valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
        valueColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        valueColumn.setCellFactory((tableCol) -> new NoteEntryEditingTableCell());
        valueColumn.setOnEditCommit(editEvent -> {
            String newValue = editEvent.getNewValue();
            ((NoteEntry) editEvent.getTableView().getItems().get(
                    editEvent.getTablePosition().getRow())).setValue(newValue);
        });

        addAccelerators();

    }

    private void addAccelerators() {
        Platform.runLater(() -> {
            newButton.getScene().getAccelerators().put(new KeyCodeCombination(KeyCode.N, KeyCombination.SHORTCUT_DOWN), () -> {
                newButton.fire();
            });
        });

        Platform.runLater(() -> {
            deleteButton.getScene().getAccelerators().put(new KeyCodeCombination(KeyCode.D, KeyCombination.SHORTCUT_DOWN), () -> {
                deleteButton.fire();
            });
        });

    }

    public void loadNoteEntries() {
        NoteEntry entry1 = new NoteEntry(UUID.randomUUID().toString(), "Joy", "Chakravarty", "");

        NoteEntry entry2 = new NoteEntry(UUID.randomUUID().toString(), "Robert", "Pires", "info bbbbb");

        noteEntryList.add(entry1);
        noteEntryList.add(entry2);

        notesTable.setItems(noteEntryList);
    }

    @FXML
    public void selectNoteEntry(MouseEvent event) {
        NoteEntry selectedNoteEntry = notesTable.getSelectionModel().getSelectedItem();
        if (selectedNoteEntry != null) {
            System.out.println(selectedNoteEntry);
            infoField.setText(selectedNoteEntry.getInfo());
        }
    }

    @FXML
    protected void deleteNoteEntry() {
        NoteEntry toBeDeletedNoteEntry = notesTable.getSelectionModel().getSelectedItem();
        System.out.println(toBeDeletedNoteEntry);
        if (toBeDeletedNoteEntry != null) {

            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle("Confirm Delete");
            alert.setHeaderText("Delete this Note Entry?");
            alert.setContentText("Key:"+toBeDeletedNoteEntry.getKey());

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK) {
                noteEntryList.remove(toBeDeletedNoteEntry);
                infoField.setText("");
            }
        }
    }

    @FXML
    protected void addNewNoteEntry() {
        NoteEntry newNoteEntry = new NoteEntry(UUID.randomUUID().toString(), "", "", "");
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(JNotesApplication.class.getResource("viewcontroller/NoteEntry.fxml"));
            AnchorPane page = (AnchorPane) loader.load();

            noteEntryStage = new Stage();
            noteEntryStage.setTitle("Add Note Entry");
            noteEntryStage.initModality(Modality.WINDOW_MODAL);
            noteEntryStage.initOwner(parentStage);
            Scene scene = new Scene(page);
            noteEntryStage.setScene(scene);

            NoteEntryController controller = loader.getController();
            controller.setParentStage(noteEntryStage);
            controller.setNoteEntry(newNoteEntry);
            controller.setNoteEntryList(noteEntryList);

            noteEntryStage.getIcons().add(new Image("file:resources/images/edit.png"));

            noteEntryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void showAbout() {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("About JNote");
        alert.setHeaderText("Author - Joy Chakravarty");

        alert.showAndWait();
    }
    
    @FXML
    private void exitJNote() {
        System.exit(0);
    }

}
