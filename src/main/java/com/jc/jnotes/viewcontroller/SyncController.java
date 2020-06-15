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

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.jc.jnotes.JNotesApplication;
import com.jc.jnotes.JNotesConstants;
import com.jc.jnotes.UserPreferences;
import com.jc.jnotes.helper.AlertHelper;
import com.jc.jnotes.service.ControllerService;
import com.jc.jnotes.service.ControllerServiceException;
import com.sun.javafx.application.HostServicesDelegate;

import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * 
 * @author Joy C
 */
public class SyncController implements Initializable {

    // To Be Set By Caller
    private Stage parentStage;

    private Stage progressStage;

    // Spring Dependencies
    private ControllerService service;
    private AlertHelper alertHelper;
    private UserPreferences userPreferences;

    private Runnable runAfter;

    @FXML
    private ToggleGroup toggleGroup;
    @FXML
    private RadioButton newUserRadioButton;
    @FXML
    private RadioButton existingUserRadioButton;

    @FXML
    private CheckBox autoConnectCheckBox;

    @FXML
    private TextField userIdTextField;
    @FXML
    private TextField userSecretTextField;
    @FXML
    private Button connectButton;
    @FXML
    private Button backupButton;
    @FXML
    private Button restoreButton;
    @FXML
    private Hyperlink hyperlink;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        prepareDependencies();
        addAccelerators();
        initializeControls();
    }

    private void initializeControls() {
        String userId = userPreferences.getUserId();
        String userSecret = userPreferences.getUserSecret();
        if (StringUtils.isNotBlank(userId)) {
            userIdTextField.setText(userId);
            userSecretTextField.setText(userSecret);
            existingUserRadioButton.setSelected(true);
            newUserRadioButton.setSelected(false);
        } else {
            existingUserRadioButton.setSelected(false);
            newUserRadioButton.setSelected(true);
        }

        boolean isConnected = userPreferences.isConnected();
        if (isConnected) {
            connectButton.setText("Disconnect");
            backupButton.setDisable(false);
            restoreButton.setDisable(false);
        } else {
            connectButton.setText("Connect");
            backupButton.setDisable(true);
            restoreButton.setDisable(true);
        }

        if (userPreferences.getAutoConnect()) {
            autoConnectCheckBox.setSelected(true);
        } else {
            autoConnectCheckBox.setSelected(false);
        }
    }

    private void prepareDependencies() {
        service = APP_CONFIG.getControllerService();
        alertHelper = APP_CONFIG.getAlertHelper();
        userPreferences = APP_CONFIG.getUserPreferences();
    }

    private void addAccelerators() {

        Platform.runLater(() -> {
            Scene scene = userIdTextField.getScene();
            scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
                public void handle(KeyEvent ke) {
                    if (ke.getCode() == KeyCode.ESCAPE) {
                        System.out.println("Key Pressed: " + ke.getCode());
                        runAfter.run();
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

    @FXML
    public void connectDisconnect() {
        if (autoConnectCheckBox.isSelected()) {
            userPreferences.setAutoConnect(true);
        } else {
            userPreferences.setAutoConnect(false);
        }
        if (userPreferences.isConnected()) {
            disconnect();
        } else {
            connect();
        }
    }

    @FXML
    public void backup() {
        Task<Void> task = new Task<Void>() {
            @Override
            public Void call() throws ControllerServiceException {
                Consumer<Long> progressConsumer = (workDone) -> {
                    updateProgress(workDone, 100L);
                };
                progressConsumer.accept(0L);
                service.backup(progressConsumer);
                updateProgress(100L, 100L);
                return null;
            }
        };
        prepareProgressBarForTask(task);
        task.setOnRunning((e) -> progressStage.show());
        task.setOnSucceeded((e) -> {
            progressStage.hide();
            alertHelper.showInfoDialog(parentStage, null, "Backup successful!");
            runAfter.run();
        });
        task.setOnFailed((e) -> {
            task.getException().printStackTrace();
            progressStage.hide();
            alertHelper.showAlertWithExceptionDetails(parentStage, task.getException(), "Failed to backup.", "");
        });
        new Thread(task).start();
    }

    @FXML
    public void restore() {
        Task<Void> task = new Task<Void>() {
            @Override
            public Void call() throws ControllerServiceException {
                Consumer<Long> progressConsumer = (workDone) -> {
                    updateProgress(workDone, 100L);
                };
                progressConsumer.accept(0L);
                service.restore(progressConsumer);
                updateProgress(100L, 100L);
                return null;
            }
        };
        prepareProgressBarForTask(task);
        task.setOnRunning((e) -> progressStage.show());
        task.setOnSucceeded((e) -> {
            progressStage.hide();
            alertHelper.showInfoDialog(parentStage, null, "Restore successful!");
            runAfter.run();
        });
        task.setOnFailed((e) -> {
            task.getException().printStackTrace();
            progressStage.hide();
            alertHelper.showAlertWithExceptionDetails(parentStage, task.getException(), "Failed to restore.", "");
        });
        new Thread(task).start();
    }
    
    @FXML
    public void openJNotesWeb() {
        JNotesApplication.openLink(JNotesConstants.REMOTE_URL);
    }

    public void disconnect() {
        userPreferences.setConnected(false);
        connectButton.setText("Connect");
        backupButton.setDisable(true);
        restoreButton.setDisable(true);
    }

    private void prepareProgressBarForTask(Task<?> task) {
        ProgressBar progressBar = new ProgressBar();
        progressBar.setMinWidth(200);
        progressBar.progressProperty().bind(task.progressProperty());
        progressStage = new Stage();
        progressStage.setTitle("Please wait..");
        progressStage.setScene(new Scene(new StackPane(progressBar), 250, 80));
        progressStage.setAlwaysOnTop(true);
    }

    public void connect() {
        String userId = userIdTextField.getText();
        String userSecret = userSecretTextField.getText();
        boolean isNewUser = newUserRadioButton.isSelected();

        if (isNewUser && StringUtils.isNotBlank(userPreferences.getUserId())) {
            Optional<ButtonType> result = alertHelper.showDefaultConfirmation(parentStage,
                    "You already have a UserId, are you sure you wish to connect using a new UserId?", null);
            if (!(result.get() == ButtonType.OK)) {
                return;
            }
        }

        if (isInputValid(userId, userSecret)) {
            Task<String> task = new Task<String>() {
                @Override
                public String call() throws ControllerServiceException {
                    return service.connect(isNewUser, userId, userSecret);
                }
            };
            prepareProgressBarForTask(task);
            task.setOnRunning((e) -> progressStage.show()/* connectButton.setText("Connecting..") */);
            task.setOnSucceeded((e) -> {
                try {
                    String message = task.get();
                    if (message != null) {
                        connectButton.setText("Connect");
                        progressStage.hide();
                        alertHelper.showErrorAlert(parentStage, null, message);
                    } else {
                        progressStage.hide();
                        connectButton.setText("Disconnect");
                        // alertHelper.showInfoDialog(parentStage, null, "Connection successful!");
                        userPreferences.setUserIdAndSecretForOnlineSync(userId, userSecret);
                        userPreferences.setConnected(true);
                        backupButton.setDisable(false);
                        restoreButton.setDisable(false);
                        runAfter.run();
                    }
                } catch (InterruptedException | ExecutionException e1) {
                    e1.printStackTrace();
                    progressStage.hide();
                    connectButton.setText("Connect");
                }
            });
            task.setOnFailed((e) -> {
                task.getException().printStackTrace();
                progressStage.hide();
                connectButton.setText("Connect");
                alertHelper.showAlertWithExceptionDetails(parentStage, task.getException(), "Failed to connect", "");
                userPreferences.setConnected(false);
            });
            new Thread(task).start();
        }
    }

    private boolean isInputValid(String userId, String userSecret) {
        if (StringUtils.isBlank(userId)) {
            alertHelper.showErrorAlert(parentStage, null, "UserId cannot be blank");
            return false;
        }
        String userIdWarningTxt = "UserId should be between 7-15 characters long. \nUserId should be alpha-numeric and contain atleast 1 number.";
        if (userId.length() < 7 || userId.length() > 15 || !StringUtils.isAlphanumeric(userId)
                || !Pattern.compile("[0-9]").matcher(userId).find()) {
            alertHelper.showErrorAlert(parentStage, null, userIdWarningTxt);
            return false;
        }

        if (StringUtils.isBlank(userSecret)) {
            alertHelper.showWarningDialog(parentStage, "Secret cannot be blank", null);
            return false;
        }
        if (!StringUtils.isAlphanumeric(userSecret)) {
            alertHelper.showErrorAlert(parentStage, null, "Please use an alpha-numeric Secret.");
            return false;
        }
        if (userSecret.length() > 15) {
            alertHelper.showErrorAlert(parentStage, null, "Secret cannot be more than 15 characters.");
            return false;
        }
        return true;
    }

}