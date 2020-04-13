package com.jc.jnotes.viewcontroller;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.lucene.index.IndexNotFoundException;

import com.jc.jnotes.JNotesApplication;
import com.jc.jnotes.dao.NoteEntryDaoFactory;
import com.jc.jnotes.model.NoteEntry;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.MenuButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 *
 * @author Joy C
 *
 */
public class NotesController {

    private final AlertHelper alertHelper = new AlertHelper();

    private ObservableList<NoteEntry> observableNoteEntryList;
    private Stage parentStage;
    private Stage noteEntryStage;

    @FXML
    private TableView<NoteEntry> notesTable;
    @FXML
    private TableColumn<NoteEntry, String> keyColumn;
    @FXML
    private TableColumn<NoteEntry, String> valueColumn;
    @FXML
    private TextArea infoField;
    @FXML
    private TextField searchField;
    @FXML
    private Button newButton;
    @FXML
    private Button deleteButton;
    @FXML
    private MenuButton menuButton;

    NoteEntry selectedNoteEntry = null;

    public void setParentStage(Stage parentStage) {
        this.parentStage = parentStage;
    }

    /**
     * @see https://docs.oracle.com/javafx/2/ui_controls/table-view.htm#sthref119
     */
    @FXML
    private void initialize() {
        loadAllNoteEntries();
        notesTable.setEditable(true);
        notesTable.getSelectionModel().cellSelectionEnabledProperty().set(true);

        keyColumn.setCellValueFactory(new PropertyValueFactory<>("key"));
        keyColumn.setCellFactory(TextFieldTableCell.forTableColumn());

        valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
        valueColumn.setCellFactory(TextFieldTableCell.forTableColumn());

        // When the selected NoteEntry in notesTable we set its info in the infoField
        notesTable.getSelectionModel().selectedItemProperty().addListener((obs, prevNoteEntry, selectedNoteEntry) -> {
            this.selectedNoteEntry = selectedNoteEntry;
            if (selectedNoteEntry != null) { // When the JNotes start no NoteEntry is selected. This is to handle that
                infoField.setText(selectedNoteEntry.getInfo());
            } else {
                infoField.setText("");
            }
        });

        addAccelerators();

        notesTable.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.TAB) {
                event.consume();
                if (!event.isShiftDown()) {
                    infoField.requestFocus();
                } else {
                    searchField.requestFocus();
                    searchField.end();
                }
            }
        });

        infoField.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.TAB) {
                event.consume();
                if (!event.isShiftDown()) {
                    newButton.requestFocus();
                } else {
                    notesTable.requestFocus();
                }
            }
        });

        searchField.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.TAB) {
                event.consume();
                if (!event.isShiftDown()) {
                    notesTable.requestFocus();
                } else {
                    menuButton.requestFocus();
                }
            }
        });

    }

    private void addAccelerators() {
        Platform.runLater(() -> {
            newButton.getScene().getAccelerators().put(new KeyCodeCombination(KeyCode.N, KeyCombination.SHORTCUT_DOWN), () -> {
                newButton.fire();
            });
            deleteButton.getScene().getAccelerators().put(new KeyCodeCombination(KeyCode.D, KeyCombination.SHORTCUT_DOWN), () -> {
                deleteButton.fire();
            });
        });
    }

    protected void loadAllNoteEntries() {
        List<NoteEntry> allNoteEntries;
        try {
            allNoteEntries = NoteEntryDaoFactory.getNoteEntryDao().getAll();
        } catch (IndexNotFoundException inEx) {
            allNoteEntries = new ArrayList<>();
            System.err.println("Exception in loadAllNoteEntries - IndexNotfound. " + inEx.getMessage());
            // inEx.printStackTrace();
        } catch (IOException ex) {
            allNoteEntries = new ArrayList<>();
            ex.printStackTrace();
            alertHelper.showAlertWithExceptionDetails(parentStage, ex, "Failed to get all Note Entries", "");
        }
        loadNoteEntries(allNoteEntries);
    }

    protected void loadNoteEntries(List<NoteEntry> noteEntries) {
        observableNoteEntryList = FXCollections.observableArrayList();
        observableNoteEntryList.addAll(noteEntries);

        notesTable.setItems(observableNoteEntryList);
    }

    @FXML
    protected void deleteNoteEntry() {
        NoteEntry toBeDeletedNoteEntry = notesTable.getSelectionModel().getSelectedItem();
        System.out.println(toBeDeletedNoteEntry);
        if (toBeDeletedNoteEntry != null) {

            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle("Confirm Delete");
            alert.setHeaderText("Delete this Note Entry?");
            alert.setContentText("Key:" + toBeDeletedNoteEntry.getKey());

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK) {
                observableNoteEntryList.remove(toBeDeletedNoteEntry);
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
            controller.setNoteEntryList(observableNoteEntryList);
            controller.setMode(NoteEntryController.MODE_ADD);

            try (FileInputStream fis = new FileInputStream("src/main/resources/images/edit.png")) {
                noteEntryStage.getIcons().add(new Image(fis));
            } catch (IOException ioEx) {
                ioEx.printStackTrace();
            }

            noteEntryStage.show();
        } catch (IOException ex) {
            ex.printStackTrace();
            alertHelper.showAlertWithExceptionDetails(parentStage, ex, "Failed to open new NoteEntry Dialog", "");
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