package com.jc.jnotes.viewcontroller;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.index.IndexNotFoundException;

import com.jc.jnotes.JNotesApplication;
import com.jc.jnotes.JNotesPreferences;
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
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
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
    private CheckBox searchAllCheckBox;
    @FXML
    private Text text;

    NoteEntry selectedNoteEntry = null;

    boolean showingSearchedResults = false;

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
        keyColumn.setCellFactory((tabCol) -> new NonEditableTableCell());

        valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
        valueColumn.setCellFactory((tabCol) -> new NonEditableTableCell());

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
        final KeyCodeCombination keyCodeCopy = new KeyCodeCombination(KeyCode.C, KeyCombination.SHORTCUT_DOWN);
        notesTable.setOnKeyPressed(event -> {
            if (keyCodeCopy.match(event) && this.selectedNoteEntry != null) {
                @SuppressWarnings("rawtypes")
                ObservableList<TablePosition> tablePositions = notesTable.getSelectionModel().getSelectedCells();
                if (tablePositions != null && tablePositions.size() > 0) {
                    @SuppressWarnings("unchecked")
                    TablePosition<NoteEntry, ?> tablePosition = tablePositions.get(0);
                    Object cellData = tablePosition.getTableColumn().getCellData(tablePosition.getRow());
                    if (cellData != null) {
                        final ClipboardContent clipboardContent = new ClipboardContent();
                        clipboardContent.putString(cellData.toString());
                        Clipboard.getSystemClipboard().setContent(clipboardContent);
                    }
                }
            }
        });

        infoField.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.TAB) {
                event.consume();
                if (!event.isShiftDown()) {
                    // menuButton.requestFocus();
                } else {
                    notesTable.requestFocus();
                }
            }
        });

        searchField.addEventFilter(KeyEvent.KEY_RELEASED, event -> {
            String searchTxt = searchField.getText();
            if (showingSearchedResults && (event.getCode() == KeyCode.ESCAPE || StringUtils.isBlank(searchTxt))) {
                event.consume();
                loadAllNoteEntries();
                searchField.setText("");
                showingSearchedResults = false;
            } else if (event.getCode() == KeyCode.BACK_SPACE) {
                if (showingSearchedResults && StringUtils.isBlank(searchTxt)) {
                    event.consume();
                    loadAllNoteEntries();
                    showingSearchedResults = false;
                } else if (StringUtils.isBlank(searchTxt)) {
                    // do nothing
                } else {
                    event.consume();
                    loadSearchedNoteEntries(searchTxt);
                    showingSearchedResults = true;
                }
            } else if (StringUtils.isNotBlank(searchTxt)) {
                event.consume();
                loadSearchedNoteEntries(searchTxt);
                showingSearchedResults = true;
            } else {
                // do nothing
            }
        });

        searchField.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.TAB) {
                event.consume();
                if (!event.isShiftDown()) {
                    notesTable.requestFocus();
                } else {
                    // menuButton.requestFocus();
                }
            }
        });

        searchAllCheckBox.setTooltip(new Tooltip("Search all fields"));
    }

    private void addAccelerators() {
        Platform.runLater(() -> {
            searchField.getScene().getAccelerators().put(new KeyCodeCombination(KeyCode.F, KeyCombination.SHORTCUT_DOWN), () -> {
                searchField.requestFocus();
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

    protected void loadSearchedNoteEntries(String searchTxt) {
        List<NoteEntry> noteEntries;
        try {
            boolean searchInfoAlso = searchAllCheckBox.isSelected();
            noteEntries = NoteEntryDaoFactory.getNoteEntryDao().searchNotes(searchTxt, searchInfoAlso);
        } catch (IOException e) {
            e.printStackTrace();
            noteEntries = Collections.emptyList();
        }
        loadNoteEntries(noteEntries);
    }

    private void loadNoteEntries(List<NoteEntry> noteEntries) {
        observableNoteEntryList = FXCollections.observableArrayList();
        observableNoteEntryList.addAll(noteEntries);

        notesTable.setItems(observableNoteEntryList);
        notesTable.refresh();
    }

    @FXML
    protected void deleteNoteEntry() {
        try {
            NoteEntry toBeDeletedNoteEntry = notesTable.getSelectionModel().getSelectedItem();
            System.out.println(toBeDeletedNoteEntry);
            if (toBeDeletedNoteEntry != null) {

                Optional<ButtonType> result = alertHelper.showDefaultConfirmation(parentStage, "Delete selected note entry?",
                        "Key:" + toBeDeletedNoteEntry.getKey());
                if (result.get() == ButtonType.OK) {
                    observableNoteEntryList.remove(toBeDeletedNoteEntry);
                    infoField.setText("");
                    this.selectedNoteEntry = null;
                    NoteEntryDaoFactory.getNoteEntryDao().deleteNoteEntry(toBeDeletedNoteEntry);
                    notesTable.refresh();
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            alertHelper.showAlertWithExceptionDetails(parentStage, ex, "Failed to delete NoteEntry", "");
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
            // controller.setNoteEntryList(observableNoteEntryList);
            controller.setMode(NoteEntryController.MODE_ADD);
            controller.setRunAfter(() -> {
                observableNoteEntryList.add(newNoteEntry);
                notesTable.refresh();
                // infoField.requestFocus();
                // infoField.setText("Note added successfully");
            });

            try (FileInputStream fis = new FileInputStream("src/main/resources/images/edit.png")) {
                noteEntryStage.getIcons().add(new Image(fis));
            } catch (IOException ioEx) {
                ioEx.printStackTrace();
            }

            noteEntryStage.show();
        } catch (IOException ex) {
            ex.printStackTrace();
            alertHelper.showAlertWithExceptionDetails(parentStage, ex, "Failed to open NoteEntry Dialog", "");
        }
    }

    @FXML
    protected void editNoteEntry() {
        if (this.selectedNoteEntry == null) {
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(JNotesApplication.class.getResource("viewcontroller/NoteEntry.fxml"));
            AnchorPane page = (AnchorPane) loader.load();

            noteEntryStage = new Stage();
            noteEntryStage.setTitle("Edit Note Entry");
            noteEntryStage.initModality(Modality.WINDOW_MODAL);
            noteEntryStage.initOwner(parentStage);
            Scene scene = new Scene(page);
            noteEntryStage.setScene(scene);

            NoteEntryController controller = loader.getController();
            controller.setParentStage(noteEntryStage);
            controller.setNoteEntry(selectedNoteEntry);
            // controller.setNoteEntryList(observableNoteEntryList);
            controller.setMode(NoteEntryController.MODE_EDIT);
            controller.setRunAfter(() -> {
                notesTable.refresh();
                infoField.requestFocus();
            });

            try (FileInputStream fis = new FileInputStream("src/main/resources/images/edit.png")) {
                noteEntryStage.getIcons().add(new Image(fis));
            } catch (IOException ioEx) {
                ioEx.printStackTrace();
            }

            noteEntryStage.show();
        } catch (IOException ex) {
            ex.printStackTrace();
            alertHelper.showAlertWithExceptionDetails(parentStage, ex, "Failed to open NoteEntry Dialog", "");
        }
    }

    @FXML
    private void showAbout() {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("About "+JNotesPreferences.getAppName());
        alert.setHeaderText("Author - Joy Chakravarty");

        alert.showAndWait();
    }

    @FXML
    private void exitJNote() {
        System.exit(0);
    }

}