package com.jc.jnotes.viewcontroller;

import static com.jc.jnotes.JNotesConstants.DATETIME_DISPLAY_FORMAT;
import static com.jc.jnotes.model.NoteEntry.KEY_COL_NAME;
import static com.jc.jnotes.model.NoteEntry.VALUE_COL_NAME;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;

import com.jc.jnotes.JNotesApplication;
import com.jc.jnotes.UserPreferences;
import com.jc.jnotes.helper.AlertHelper;
import com.jc.jnotes.helper.IOHelper;
import com.jc.jnotes.model.NoteEntry;
import com.jc.jnotes.service.ControllerService;
import com.jc.jnotes.service.ControllerServiceException;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.MenuButton;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
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

    private static final String DELETE_NOTES_CONFIRMATION_HEADER = "Delete selected notes?";
    private static final String DELETE_NOTES_CONFIRMATION_CONTENT = "%d note(s) will be deleted.";

    private static final String EXPORT_SUCCESS_STATUS_NOTIFICATION = "Exported successfully. File: %s";
    private static final String EXPORT_FAILURE_STATUS_NOTIFICATION = "Export failed.";

    private static final String IMPORT_SUCCESS_STATUS_NOTIFICATION = "Imported Notes count: %d";
    private static final String IMPORT_FAILURE_STATUS_NOTIFICATION = "Import failed.";

    // To Be Set By Caller
    private Stage parentStage;

    private ObservableList<NoteEntry> observableNoteEntryList;
    private Stage noteEntryStage;
    private NoteEntry selectedNoteEntry = null;
    private boolean showingSearchedResults = false;

    private final Comparator<NoteEntry> comparator = Comparator.comparing((noteEntry) -> noteEntry.getLastModifiedTime());
    private NoteBookActions noteBookActions;

    // Spring Dependencies
    private UserPreferences userPreferences;
    private ControllerService service;
    private AlertHelper alertHelper;
    private IOHelper ioHelper;

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
    private ComboBox<String> noteBookComboBox;
    @FXML
    private MenuButton menuButton;
    @FXML
    private ToggleButton sortToggleButton;

    public void setParentStage(Stage parentStage) {
        this.parentStage = parentStage;
    }

    /**
     * @see https://docs.oracle.com/javafx/2/ui_controls/table-view.htm#sthref119
     */
    @FXML
    private void initialize() {

        prepareDependencies();

        loadAllNoteEntries();

        initializeNotesTable();

        initializeNotesTableColumns();

        initializeInfoField();

        initializeSearchField();

        initilalizeMenuButton();

        initializeNoteBooks();

        addAccelerators();

    }

    private void prepareDependencies() {
        ApplicationContext applicationContext = JNotesApplication.getAppicationContext();
        userPreferences = applicationContext.getBean(UserPreferences.class);
        service = applicationContext.getBean(ControllerService.class);
        alertHelper = applicationContext.getBean(AlertHelper.class);
        ioHelper = applicationContext.getBean(IOHelper.class);
        noteBookActions = new NoteBookActions(alertHelper, ioHelper, parentStage, noteBookComboBox, notificationText);
    }

    private void initializeNoteBooks() {
        noteBookComboBox.setTooltip(new Tooltip("Select NoteBook"));
        loadNoteBooks();
        noteBookComboBox.setEditable(false);
        noteBookComboBox.getSelectionModel().selectedItemProperty().addListener((obs, prevNoteBook, selectedNoteBook) -> {
            if (StringUtils.isNotBlank(selectedNoteBook)) {
                userPreferences.setCurrentNoteBook(selectedNoteBook);
                loadAllNoteEntries();
                infoField.clear();
            }
        });

    }

    private void initilalizeMenuButton() {
        menuButton.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.TAB) {
                event.consume();
                if (!event.isShiftDown()) {
                    noteBookComboBox.requestFocus();
                } else {
                    infoField.requestFocus();
                }
            }
        });
    }

    private void initializeSearchField() {
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
                    noteBookComboBox.requestFocus();
                }
            }
        });

        searchAllCheckBox.setTooltip(new Tooltip("Search all fields"));
    }

    private void initializeInfoField() {
        infoField.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.TAB) {
                event.consume();
                if (!event.isShiftDown()) {
                    menuButton.requestFocus();
                } else {
                    notesTable.requestFocus();
                    if (selectedNoteEntry == null) {
                        if (observableNoteEntryList != null && !observableNoteEntryList.isEmpty()) {
                            notesTable.getSelectionModel().select(0);
                        }
                    }
                }
            } else if (event.getCode() == KeyCode.S && event.isShortcutDown()) {
                event.consume();
                if (selectedNoteEntry != null) {
                    selectedNoteEntry.setInfo(infoField.getText());
                    try {
                        service.editNoteEntry(selectedNoteEntry);
                    } catch (ControllerServiceException ex) {
                        alertHelper.showAlertWithExceptionDetails(parentStage, ex, "Failed to save NoteEntry Dialog", "");
                    }
                    notificationText.setText(EDIT_STATUS_NOTIFICATION);
                    notesTable.refresh();
                }
            }
        });
    }

    private void initializeNotesTableColumns() {
        BiConsumer<String, Integer> saveOnEditBiConsumer = (editedText, colIndex) -> {
            if (colIndex == 0) {
                selectedNoteEntry.setKey(editedText);
            } else {// colIndex == 1
                selectedNoteEntry.setValue(editedText);
            }
            try {
                service.editNoteEntry(selectedNoteEntry);
            } catch (ControllerServiceException ex) {
                alertHelper.showAlertWithExceptionDetails(parentStage, ex, "Failed to save NoteEntry Dialog", "");
            }
            notesTable.getSelectionModel().clearSelection();
            notificationText.setText(EDIT_STATUS_NOTIFICATION);
            notesTable.refresh();
        };

        keyColumn.setCellValueFactory(new PropertyValueFactory<>(KEY_COL_NAME));
        keyColumn.setCellFactory((tabCol) -> new SaveEnabledTableCell(saveOnEditBiConsumer, 0));

        valueColumn.setCellValueFactory(new PropertyValueFactory<>(VALUE_COL_NAME));
        valueColumn.setCellFactory((tabCol) -> new SaveEnabledTableCell(saveOnEditBiConsumer, 1));
    }

    private void initializeNotesTable() {
        notesTable.setEditable(true);
        notesTable.getSelectionModel().cellSelectionEnabledProperty().set(true);
        notesTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // When the selected NoteEntry in notesTable we set its info in the infoField
        notesTable.getSelectionModel().selectedItemProperty().addListener((obs, prevNoteEntry, selectedNoteEntry) -> {
            // CRITICAL: most of the code relies on selectedNoteEntry. On losing focus, it also sets selectedNoteEntry to null.
            this.selectedNoteEntry = selectedNoteEntry;
            // END CRITICAL
            if (selectedNoteEntry != null) { // When the JNotes start no NoteEntry is selected. This is to handle that
                infoField.setText(selectedNoteEntry.getInfo());
                notificationText.setText("Last modified on: " + selectedNoteEntry.getLastModifiedTime().format(DATETIME_DISPLAY_FORMAT));
            } else {
                infoField.clear();
            }
        });

        // For Navigation
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

        // Double click on empty area to open AddNewNoteEntry
        // Single click elsewhere would clear the edit mode of the cell if thats the cell is in edit mode
        notesTable.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
            Node source = event.getPickResult().getIntersectedNode();

            // move up through the node hierarchy until a TableRow or scene root is found
            while (source != null && !(source instanceof TableRow)) {
                source = source.getParent();
            }

            // clear selection on click anywhere but on a filled row
            if (source == null || (source instanceof TableRow && ((TableRow) source).isEmpty())) {
                notesTable.getSelectionModel().clearSelection();
                if (event.getClickCount() == 2 && source != null) {
                    this.addNewNoteEntry();
                }
            }
        });

        // Lets user copy cell value directly without entering edit mode using Ctrl^C
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
    }

    private void loadNoteBooks() {
        List<String> directories = ioHelper.getAllNoteBooks();
        noteBookComboBox.getItems().addAll(directories);
        noteBookComboBox.getSelectionModel().select(userPreferences.getCurrentNoteBook());
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
            allNoteEntries = service.getAll();
        } catch (ControllerServiceException ex) {
            allNoteEntries = new ArrayList<>();
            alertHelper.showAlertWithExceptionDetails(parentStage, ex, "Failed to get all Notes", "");
        }
        loadNoteEntries(allNoteEntries);
    }

    protected void loadSearchedNoteEntries(String searchTxt) {
        List<NoteEntry> noteEntries;
        boolean searchInfoAlso = searchAllCheckBox.isSelected();
        noteEntries = service.searchNotes(searchTxt, searchInfoAlso);
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
    protected void deleteNoteEntries() {
        try {
            // NoteEntry toBeDeletedNoteEntry = notesTable.getSelectionModel().getSelectedItem();
            List<NoteEntry> noteEntriesToBeDeleted = notesTable.getSelectionModel().getSelectedItems();
            // System.out.println(listofEntriesToBeDeleted);
            // System.out.println(toBeDeletedNoteEntry);
            if (noteEntriesToBeDeleted != null) {

                Optional<ButtonType> result = alertHelper.showDefaultConfirmation(parentStage, DELETE_NOTES_CONFIRMATION_HEADER,
                        String.format(DELETE_NOTES_CONFIRMATION_CONTENT, noteEntriesToBeDeleted.size()));
                if (result.get() == ButtonType.OK) {
                    service.deleteNoteEntries(noteEntriesToBeDeleted);
                    observableNoteEntryList.removeAll(noteEntriesToBeDeleted);
                    infoField.clear();
                    this.selectedNoteEntry = null;
                    notesTable.refresh();
                    notificationText.setText(DELETE_STATUS_NOTIFICATION);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            alertHelper.showAlertWithExceptionDetails(parentStage, ex, "Failed to delete Notes", "");
        }
    }

    @FXML
    protected void addNewNoteEntry() {
        NoteEntry newNoteEntry = new NoteEntry(NoteEntry.generateID(), "", "", "");
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(JNotesApplication.getResource("viewcontroller/NoteEntry.fxml"));
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

            InputStream iconInputStream = JNotesApplication.getResourceAsStream("/images/edit.png");
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
            loader.setLocation(JNotesApplication.getResource("viewcontroller/NoteEntry.fxml"));
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
                infoField.setText(notesTable.getSelectionModel().getSelectedItem().getInfo());
                // infoField.requestFocus();
            });

            InputStream iconInputStream = JNotesApplication.getResourceAsStream("/images/edit.png");
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
    protected void sortByModificationDate() {
        boolean isSelected = sortToggleButton.isSelected();
        if (isSelected) {
            Collections.sort(observableNoteEntryList, comparator);
        } else {
            Collections.sort(observableNoteEntryList, comparator.reversed());
        }
        notesTable.refresh();
    }

    @FXML
    protected void deleteNoteBook() {
        noteBookActions.deleteNoteBook();
    }

    @FXML
    protected void renameNoteBook() {
        noteBookActions.renameNoteBook();
    }

    @FXML
    protected void addNewNoteBook() {
        noteBookActions.addNewNoteBook();
    }

    @FXML
    protected void exportNoteBook() {
        String exprtFilePath = ioHelper.exportNoteBook(observableNoteEntryList);
        if (exprtFilePath != null) {
            notificationText.setText(String.format(EXPORT_SUCCESS_STATUS_NOTIFICATION, exprtFilePath));
        } else {
            notificationText.setText(EXPORT_FAILURE_STATUS_NOTIFICATION);
        }
    }

    @FXML
    protected void importNoteBook() {
        FileChooser fileChooser = new FileChooser();
        File selectedFile = fileChooser.showOpenDialog(parentStage);
        fileChooser.setInitialDirectory(ioHelper.getBaseDirectory());
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Text Files", "*.csv"),
                new FileChooser.ExtensionFilter("Properties Files", "*.properties"));
        if (selectedFile == null) {
            return;
        }
        List<NoteEntry> noteEntries = ioHelper.importNoteBook(selectedFile);
        if (noteEntries == null) {
            notificationText.setText(IMPORT_FAILURE_STATUS_NOTIFICATION);
        } else {
            try {
                for (NoteEntry note : noteEntries) {
                    service.addNoteEntry(note);
                    observableNoteEntryList.add(note);
                }
                notificationText.setText(String.format(IMPORT_SUCCESS_STATUS_NOTIFICATION, noteEntries.size()));
                notesTable.refresh();
            } catch (ControllerServiceException ex) {
                notificationText.setText(IMPORT_FAILURE_STATUS_NOTIFICATION);
            }
        }
    }

    @FXML
    private void showAbout() {
        alertHelper.showAboutJNotesDialog();
    }

    @FXML
    private void exitJNote() {
        System.exit(0);
    }

}