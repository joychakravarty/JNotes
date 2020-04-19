package com.jc.jnotes.viewcontroller;

import static com.jc.jnotes.JNotesPreferences.DEFAULT_APP_NAME;
import static com.jc.jnotes.JNotesPreferences.DEFAULT_DATETIME_DISPLAY_FORMAT;
import static com.jc.jnotes.JNotesPreferences.CURRENT_VERSION;
import static com.jc.jnotes.JNotesPreferences.getBasePath;
import static com.jc.jnotes.JNotesPreferences.getCurrentProfile;
import static com.jc.jnotes.model.NoteEntry.KEY_COL_NAME;
import static com.jc.jnotes.model.NoteEntry.VALUE_COL_NAME;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.index.IndexNotFoundException;

import com.jc.jnotes.JNotesApplication;
import com.jc.jnotes.JNotesPreferences;
import com.jc.jnotes.dao.NoteEntryDaoFactory;
import com.jc.jnotes.helper.AlertHelper;
import com.jc.jnotes.helper.ImportExportHelper;
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
import javafx.scene.control.ComboBox;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
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
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 *
 * @author Joy C
 *
 */
public class NotesController {

    private static final String ADD_STATUS_NOTIFICATION = "Success: Note Added.";
    private static final String EDIT_STATUS_NOTIFICATION = "Success: Note Saved.";
    private static final String DELETE_STATUS_NOTIFICATION = "Success: Note Deleted.";
    private static final String PROFILE_DELETE_STATUS_NOTIFICATION = "Success: Profile Deleted.";

    private static final String EXPORT_SUCCESS_STATUS_NOTIFICATION = "Exported successfully. File: %s";
    private static final String EXPORT_FAILURE_STATUS_NOTIFICATION = "Export failed.";

    private static final String IMPORT_SUCCESS_STATUS_NOTIFICATION = "Imported Notes count: %d";
    private static final String IMPORT_FAILURE_STATUS_NOTIFICATION = "Import failed.";

    private final AlertHelper alertHelper = new AlertHelper();
    private final ImportExportHelper importExportHelper = new ImportExportHelper();

    private ObservableList<NoteEntry> observableNoteEntryList;
    private Stage parentStage;
    private Stage noteEntryStage;
    private NoteEntry selectedNoteEntry = null;
    private boolean showingSearchedResults = false;

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
    private Text notificationText;
    @FXML
    private ComboBox<String> profileComboBox;
    @FXML
    private SplitMenuButton splitMenuButton;

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

        keyColumn.setCellValueFactory(new PropertyValueFactory<>(KEY_COL_NAME));
        keyColumn.setCellFactory((tabCol) -> new NonEditableTableCell());

        valueColumn.setCellValueFactory(new PropertyValueFactory<>(VALUE_COL_NAME));
        valueColumn.setCellFactory((tabCol) -> new NonEditableTableCell());

        // When the selected NoteEntry in notesTable we set its info in the infoField
        notesTable.getSelectionModel().selectedItemProperty().addListener((obs, prevNoteEntry, selectedNoteEntry) -> {
            this.selectedNoteEntry = selectedNoteEntry;
            if (selectedNoteEntry != null) { // When the JNotes start no NoteEntry is selected. This is to handle that
                infoField.setText(selectedNoteEntry.getInfo());
                notificationText
                        .setText("Last modified on: " + selectedNoteEntry.getLastModifiedTime().format(DEFAULT_DATETIME_DISPLAY_FORMAT));
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
                    splitMenuButton.requestFocus();
                } else {
                    notesTable.requestFocus();
                    if (selectedNoteEntry == null) {
                        if (observableNoteEntryList != null && !observableNoteEntryList.isEmpty()) {
                            notesTable.getSelectionModel().select(0);
                        }
                    }
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
                    if (selectedNoteEntry == null) {
                        if (observableNoteEntryList != null && !observableNoteEntryList.isEmpty()) {
                            notesTable.getSelectionModel().select(0);
                        }
                    }
                } else {
                    profileComboBox.requestFocus();
                }
            }
        });

        splitMenuButton.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.TAB) {
                event.consume();
                if (!event.isShiftDown()) {
                    profileComboBox.requestFocus();
                } else {
                    infoField.requestFocus();
                }
            }
        });

        searchAllCheckBox.setTooltip(new Tooltip("Search all fields"));

        profileComboBox.setTooltip(new Tooltip("Select Profile"));
        loadProfiles();
        profileComboBox.setEditable(false);
        profileComboBox.getSelectionModel().selectedItemProperty().addListener((obs, prevProfile, selectedProfile) -> {
            if (StringUtils.isNotBlank(selectedProfile)) {
                JNotesPreferences.setCurrentProfile(selectedProfile);
                loadAllNoteEntries();
            }
        });
    }

    private void loadProfiles() {
        Path directory = Paths.get(getBasePath(), DEFAULT_APP_NAME);
        List<String> directories;
        try {
            directories = Files.walk(directory).filter(Files::isDirectory).map((path) -> path.getFileName()).map(Path::toString)
                    .filter((name) -> !name.equals(DEFAULT_APP_NAME)).collect(Collectors.toList());
        } catch (IOException e) {
            directories = new ArrayList<>();
            directories.add(JNotesPreferences.DEFAULT_PROFILE);
        }
        profileComboBox.getItems().addAll(directories);
        profileComboBox.getSelectionModel().select(getCurrentProfile());

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
        notificationText.setText("Total Notes : " + observableNoteEntryList.size());
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
                    notificationText.setText(DELETE_STATUS_NOTIFICATION);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            alertHelper.showAlertWithExceptionDetails(parentStage, ex, "Failed to delete NoteEntry", "");
        }
    }

    @FXML
    protected void addNewNoteEntry() {
        NoteEntry newNoteEntry = new NoteEntry(NoteEntry.generateID(), "", "", "");
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
                notificationText.setText(ADD_STATUS_NOTIFICATION);
            });

            InputStream iconInputStream = JNotesApplication.class.getResourceAsStream("/images/edit.png");
            if (iconInputStream != null) {
                noteEntryStage.getIcons().add(new Image(iconInputStream));
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
                notificationText.setText(EDIT_STATUS_NOTIFICATION);
                infoField.requestFocus();
            });

            InputStream iconInputStream = JNotesApplication.class.getResourceAsStream("/images/edit.png");
            if (iconInputStream != null) {
                noteEntryStage.getIcons().add(new Image(iconInputStream));
            }

            noteEntryStage.show();
        } catch (IOException ex) {
            ex.printStackTrace();
            alertHelper.showAlertWithExceptionDetails(parentStage, ex, "Failed to open NoteEntry Dialog", "");
        }
    }

    @FXML
    protected void deleteProfile() {
        try {
            if (profileComboBox.getItems().size() == 1) {
                alertHelper.showErrorAlert(parentStage, "Invalid Operation", "Cannot delete the only Profile");

            } else {
                String profileToBeDeleted = profileComboBox.getSelectionModel().getSelectedItem();
                Optional<ButtonType> result = alertHelper.showDefaultConfirmation(parentStage,
                        "Delete profile: " + profileToBeDeleted + "?", "Note: This is will delete all the data within this profile!");

                if (result.get() == ButtonType.OK) {
                    profileComboBox.getItems().remove(profileToBeDeleted);
                    profileComboBox.getSelectionModel().select(0);
                    Path pathToBeDeleted = Paths.get(getBasePath(), DEFAULT_APP_NAME, profileToBeDeleted);
                    FileUtils.deleteDirectory(pathToBeDeleted.toFile());
                    notificationText.setText(PROFILE_DELETE_STATUS_NOTIFICATION);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            alertHelper.showAlertWithExceptionDetails(parentStage, ex, "Failed to delete Profile", "");
        }
    }

    @FXML
    protected void renameProfile() {
        String profileToBeRenamed = profileComboBox.getSelectionModel().getSelectedItem();
        TextInputDialog dialog = new TextInputDialog();
        dialog.setHeaderText("Rename Profile: " + profileToBeRenamed);
        dialog.setContentText("New Profile Name");
        dialog.showAndWait().ifPresent(text -> {
            if (StringUtils.isNotBlank(text) && profileComboBox.getItems().indexOf(text) == -1) {
                try {
                    Path source = Paths.get(getBasePath(), DEFAULT_APP_NAME, profileToBeRenamed);
                    Files.move(source, source.resolveSibling(text));
                } catch (IOException ex) {
                    ex.printStackTrace();
                    alertHelper.showAlertWithExceptionDetails(parentStage, ex, "Failed to Rename Profile", "");
                    return;
                }

                int index = profileComboBox.getItems().indexOf(profileToBeRenamed);
                profileComboBox.getItems().add(index, text);
                profileComboBox.getItems().remove(profileToBeRenamed);
                profileComboBox.getSelectionModel().select(index);
            } else {
                alertHelper.showErrorAlert(parentStage, "Invalid operation", "Please enter a valid Profile name");
            }
        });
    }

    @FXML
    protected void addNewProfile() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setHeaderText("Add New Profile");
        dialog.setContentText("New Profile Name");
        dialog.showAndWait().ifPresent(text -> {
            if (StringUtils.isNotBlank(text) && profileComboBox.getItems().indexOf(text) == -1) {
                try {
                    Paths.get(getBasePath(), DEFAULT_APP_NAME, text);
                } catch (InvalidPathException ex) {
                    ex.printStackTrace();
                    alertHelper.showAlertWithExceptionDetails(parentStage, ex, "Failed to Add Profile", "");
                    return;
                }
                int index = profileComboBox.getItems().size() - 1;
                profileComboBox.getItems().add(index, text);
                profileComboBox.getSelectionModel().select(index);
            } else {
                alertHelper.showErrorAlert(parentStage, "Invalid operation", "Please enter a valid Profile name");
            }
        });
    }

    @FXML
    protected void exportProfile() {
        boolean exprtStatus = importExportHelper.exportProfile(observableNoteEntryList);
        if (exprtStatus) {
            notificationText.setText(String.format(EXPORT_SUCCESS_STATUS_NOTIFICATION, importExportHelper.getExportFilePath().toString()));
        } else {
            notificationText.setText(EXPORT_FAILURE_STATUS_NOTIFICATION);
        }
    }

    @FXML
    protected void importProfile() {
        FileChooser fileChooser = new FileChooser();
        File selectedFile = fileChooser.showOpenDialog(parentStage);
        fileChooser.setInitialDirectory(Paths.get(JNotesPreferences.getBasePath()).toFile());

        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Text Files", "*.csv"),
                new FileChooser.ExtensionFilter("Properties Files", "*.properties"));

        List<NoteEntry> noteEntries = importExportHelper.importProfile(selectedFile);
        if (noteEntries == null) {
            notificationText.setText(IMPORT_FAILURE_STATUS_NOTIFICATION);
        } else {
            try {
                for (NoteEntry nEntry : noteEntries) {
                    NoteEntryDaoFactory.getNoteEntryDao().addNoteEntry(nEntry);
                    observableNoteEntryList.add(nEntry);
                }
                notificationText.setText(String.format(IMPORT_SUCCESS_STATUS_NOTIFICATION, noteEntries.size()));
                notesTable.refresh();
            } catch (IOException ex) {
                ex.printStackTrace();
                notificationText.setText(IMPORT_FAILURE_STATUS_NOTIFICATION);
            }
        }
    }

    @FXML
    private void showAbout() {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("About " + DEFAULT_APP_NAME);
        alert.setHeaderText("Author - Joy Chakravarty");
        if(StringUtils.isNotBlank(CURRENT_VERSION)){
            alert.setContentText("Version: "+CURRENT_VERSION);
        }
        alert.showAndWait();
    }

    @FXML
    private void exitJNote() {
        System.exit(0);
    }

}