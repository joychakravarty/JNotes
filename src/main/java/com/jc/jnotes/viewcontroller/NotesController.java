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

import static com.jc.jnotes.AppConfig.APP_CONFIG;
import static com.jc.jnotes.JNotesConstants.DATETIME_DISPLAY_FORMAT;
import static com.jc.jnotes.model.NoteEntry.KEY_COL_NAME;

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
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
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
import javafx.scene.image.ImageView;
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
 * This is the main view controller for JNotes.
 * 
 * @author Joy C
 *
 */
public class NotesController {

    private static final String ADD_STATUS_NOTIFICATION = "Success: Note Added.";
    private static final String EDIT_STATUS_NOTIFICATION = "Success: Note Saved.";
    private static final String DELETE_STATUS_NOTIFICATION = "Success: Note(s) Deleted.";
    private static final String MOVE_STATUS_NOTIFICATION = "Success: Note(s) Moved.";

    private static final String DELETE_NOTES_CONFIRMATION_HEADER = "Delete selected notes?";
    private static final String DELETE_NOTES_CONFIRMATION_CONTENT = "%d note(s) will be deleted.";

    private static final String EXPORT_SUCCESS_STATUS_NOTIFICATION = "Exported successfully. File: %s";
    private static final String EXPORT_FAILURE_STATUS_NOTIFICATION = "Export failed.";

    private static final String IMPORT_SUCCESS_STATUS_NOTIFICATION = "Imported Notes count: %d";
    private static final String IMPORT_FAILURE_STATUS_NOTIFICATION = "Import failed.";

    // To Be Set By Caller
    private Stage parentStage;

    private ObservableList<NoteEntry> observableNoteEntryList;

    // Child stages
    private Stage noteEntryStage;
    private Stage syncStage;
    private NoteEntry selectedNoteEntry = null;
    private boolean showingSearchedResults = false;

    private final Comparator<NoteEntry> comparator = Comparator.comparing((noteEntry) -> noteEntry.getLastModifiedTime());
    private NotebookActions notebookActions;

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
    private ComboBox<String> notebookComboBox;
    @FXML
    private MenuButton menuButton;
    @FXML
    private ToggleButton sortToggleButton;
    @FXML
    private ImageView connectionImage;

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

        initializeNotebooks();

        addAccelerators();

        initializeOnlineDataStore();

    }

    private void initializeOnlineDataStore() {
        if (userPreferences.getAutoConnect() && StringUtils.isNotBlank(userPreferences.getUserId()) && userPreferences.isConnected()) {
            try {
                service.connect(false, userPreferences.getUserId(), userPreferences.getUserSecret());
                updateConnectionImageBasedOnFlag(true);
            } catch (ControllerServiceException e) {
                e.printStackTrace();
                // alertHelper.showErrorAlert(parentStage, "Failed to connect to Online Database", null);
                userPreferences.setConnected(false);
                updateConnectionImageBasedOnFlag(false);
            }
        } else {
            userPreferences.setConnected(false);
            updateConnectionImageBasedOnFlag(false);
        }
    }

    private void prepareDependencies() {
        userPreferences = APP_CONFIG.getUserPreferences();
        service = APP_CONFIG.getControllerService();
        alertHelper = APP_CONFIG.getAlertHelper();
        ioHelper = APP_CONFIG.getIOHelper();
        notebookActions = new NotebookActions(service, alertHelper, parentStage, notebookComboBox, notificationText);
    }

    private void initializeNotebooks() {
        notebookComboBox.setTooltip(new Tooltip("Select Notebook"));
        loadNotebooks();
        notebookComboBox.setEditable(false);
        notebookComboBox.getSelectionModel().selectedItemProperty().addListener((obs, prevNotebook, selectedNotebook) -> {
            if (StringUtils.isNotBlank(selectedNotebook)) {
                userPreferences.setCurrentNotebook(selectedNotebook);
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
                    notebookComboBox.requestFocus();
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
                    notebookComboBox.requestFocus();
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

        valueColumn.setCellValueFactory(new PropertyValueFactory<>("displayValue"));
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
            if (selectedNoteEntry != null) { // When the JNotes starts NO NoteEntry is selected. This is to handle that.
                infoField.setText(selectedNoteEntry.getInfo());
                notificationText.setText("Last modified on: " + selectedNoteEntry.getLastModifiedTime().format(DATETIME_DISPLAY_FORMAT));
                initializeContextMenuOnRowSelect();
            } else {
                infoField.clear();
                notesTable.setContextMenu(null);
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
            if (source == null || (source instanceof TableRow && ((TableRow<?>) source).isEmpty())) {
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
                    String textToBeCopied;
                    if (tablePosition.getColumn() == 0) {
                        textToBeCopied = selectedNoteEntry.getKey();
                    } else {
                        textToBeCopied = selectedNoteEntry.getValue();
                    }
                    final ClipboardContent clipboardContent = new ClipboardContent();
                    clipboardContent.putString(textToBeCopied);
                    Clipboard.getSystemClipboard().setContent(clipboardContent);
                }
            }
        });
    }
    
    /*
     * This sets up menu on right click.
     * To be called when at least one note is selected.
     */
    private void initializeContextMenuOnRowSelect() {
        ContextMenu contextMenu = new ContextMenu();
        //Add move menu to ContextMenu
        //Moving to another notebook is only possible when there is more than one notebook
        if (notebookComboBox.getItems().size() > 1) {
            Menu moveMenu = new Menu("Move to");
            String selectedNotebook = notebookComboBox.getSelectionModel().getSelectedItem();
            for (String notebook : notebookComboBox.getItems()) {
                if (!selectedNotebook.equals(notebook)) {
                    MenuItem menuItem = new MenuItem(notebook);
                    menuItem.setOnAction((event) -> {
                        String destinationNotebook = ((MenuItem) event.getSource()).getText();
                        System.out.println("Moving to " + destinationNotebook);
                        List<NoteEntry> noteEntriesToBeMoved = notesTable.getSelectionModel().getSelectedItems();
                        try {
                            service.moveNotes(noteEntriesToBeMoved, selectedNotebook, destinationNotebook);
                            observableNoteEntryList.removeAll(noteEntriesToBeMoved);
                            notificationText.setText(MOVE_STATUS_NOTIFICATION);
                        } catch (IOException ex) {
                            alertHelper.showAlertWithExceptionDetails(parentStage, ex, "Failed to move Notes", "");
                        }
                        notesTable.refresh();
                    });
                    moveMenu.getItems().add(menuItem);
                }
            }
            contextMenu.getItems().add(moveMenu);
        }
        //Add delete notes to context menu
        MenuItem deleteMenuItem = new MenuItem("Delete");
        deleteMenuItem.setOnAction((event) -> {
            this.deleteNoteEntries();
        });
        contextMenu.getItems().add(deleteMenuItem);
        notesTable.setContextMenu(contextMenu);
        
    }

    private void updateConnectionImageBasedOnFlag(boolean isConnectedFLag) {
        InputStream connectionStatusImg;
        if (isConnectedFLag) {
            connectionStatusImg = JNotesApplication.getResourceAsStream("/images/connected.png");
        } else {
            connectionStatusImg = JNotesApplication.getResourceAsStream("/images/disconnected.png");
        }
        connectionImage.setImage(new Image(connectionStatusImg));
    }

    private void loadNotebooks() {
        List<String> directories = ioHelper.getAllNotebooks();
        notebookComboBox.getItems().clear();
        notebookComboBox.getItems().addAll(directories);
        notebookComboBox.getSelectionModel().select(userPreferences.getCurrentNotebook());
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
        NoteEntry newNoteEntry = new NoteEntry(NoteEntry.generateID(), "", "", "", "N");
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(JNotesApplication.getResource("viewcontroller/NoteEntry.fxml"));
            AnchorPane page = (AnchorPane) loader.load();

            noteEntryStage = new Stage();
            noteEntryStage.setTitle("Add");
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

            InputStream iconInputStream = JNotesApplication.getResourceAsStream("/images/add.png");
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
    protected void sync() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(JNotesApplication.getResource("viewcontroller/Sync.fxml"));
            AnchorPane page = (AnchorPane) loader.load();
            syncStage = new Stage();
            syncStage.setTitle("Settings");
            syncStage.initModality(Modality.WINDOW_MODAL);
            syncStage.initOwner(parentStage);
            Scene scene = new Scene(page);
            syncStage.setScene(scene);

            SyncController controller = loader.getController();
            controller.setParentStage(syncStage);
            controller.setRunAfter(() -> {
                notesTable.refresh();
                loadNotebooks();
                if (userPreferences.isConnected()) {
                    updateConnectionImageBasedOnFlag(true);
                } else {
                    updateConnectionImageBasedOnFlag(false);
                }
            });

            InputStream iconInputStream = JNotesApplication.getResourceAsStream("/images/cloudsync.png");
            if (iconInputStream != null) {
                syncStage.getIcons().add(new Image(iconInputStream));
            }

            syncStage.show();
        } catch (IOException ex) {
            ex.printStackTrace();
            alertHelper.showAlertWithExceptionDetails(parentStage, ex, "Failed to open Sync Dialog", "");
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
            noteEntryStage.setTitle("Edit");
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
    protected void deleteNotebook() {
        notebookActions.deleteNotebook();
    }

    @FXML
    protected void renameNotebook() {
        notebookActions.renameNotebook();
    }

    @FXML
    protected void addNewNotebook() {
        notebookActions.addNewNotebook();
    }

    @FXML
    protected void exportNotebook() {
        String exprtFilePath = ioHelper.exportNotebook(observableNoteEntryList);
        if (exprtFilePath != null) {
            notificationText.setText(String.format(EXPORT_SUCCESS_STATUS_NOTIFICATION, exprtFilePath));
        } else {
            notificationText.setText(EXPORT_FAILURE_STATUS_NOTIFICATION);
        }
    }

    @FXML
    protected void importNotebook() {
        FileChooser fileChooser = new FileChooser();
        File selectedFile = fileChooser.showOpenDialog(parentStage);
        fileChooser.setInitialDirectory(ioHelper.getBaseDirectory());
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Text Files", "*.csv"),
                new FileChooser.ExtensionFilter("Properties Files", "*.properties"));
        if (selectedFile == null) {
            return;
        }
        List<NoteEntry> noteEntries = ioHelper.importNotebook(selectedFile);
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