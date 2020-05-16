package com.jc.jnotes.viewcontroller;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;

import com.jc.jnotes.JNotesApplication;
import com.jc.jnotes.UserPreferences;
import com.jc.jnotes.helper.AlertHelper;
import com.jc.jnotes.service.ControllerService;
import com.jc.jnotes.service.ControllerServiceException;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
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
        
        if(userPreferences.getAutoConnect()) {
            autoConnectCheckBox.setSelected(true);
        } else {
            autoConnectCheckBox.setSelected(false);
        }
    }

    private void prepareDependencies() {
        ApplicationContext applicationContext = JNotesApplication.getAppicationContext();
        service = applicationContext.getBean(ControllerService.class);
        alertHelper = applicationContext.getBean(AlertHelper.class);
        userPreferences = applicationContext.getBean(UserPreferences.class);
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
        if(autoConnectCheckBox.isSelected()) {
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

    public void disconnect() {
        service.disconnect();
        userPreferences.setConnected(false);
        connectButton.setText("Connect");
        backupButton.setDisable(true);
        restoreButton.setDisable(true);
        runAfter.run();
    }

    public void connect() {
        String userId = userIdTextField.getText();
        String userSecret = userSecretTextField.getText();
        boolean isNewUser = newUserRadioButton.isSelected();

        if (isInputValid(userId, userSecret)) {
            Task<String> task = new Task<String>() {
                @Override
                public String call() throws ControllerServiceException {
                    return service.connect(isNewUser, userId, userSecret);
                }
            };

            task.setOnRunning((e) -> connectButton.setText("Connecting.."));
            task.setOnSucceeded((e) -> {
                try {
                    String message = task.get();
                    if (message != null) {
                        connectButton.setText("Connect");
                        alertHelper.showErrorAlert(parentStage, null, message);
                    } else {
                        connectButton.setText("Disconnect");
                        alertHelper.showInfoDialog(parentStage, null, "Connection successful!");
                        userPreferences.setUserIdAndSecretForOnlineSync(userId, userSecret);
                        userPreferences.setConnected(true);
                        backupButton.setDisable(false);
                        restoreButton.setDisable(false);
                        runAfter.run();
                    }
                } catch (InterruptedException | ExecutionException e1) {
                    e1.printStackTrace();
                    connectButton.setText("Connect");
                }
            });
            task.setOnFailed((e) -> {
                task.getException().printStackTrace();
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
        if (!StringUtils.isAlpha(userId)) {
            alertHelper.showErrorAlert(parentStage, null, "UserId can only contain alphabets.");
            return false;
        }
        if (userId.length() > 15) {
            alertHelper.showErrorAlert(parentStage, null, "UserId cannot be more than 15 characters.");
            return false;
        }
        
        if (StringUtils.isBlank(userSecret)) {
            Optional<ButtonType> result = alertHelper.showDefaultConfirmation(parentStage, "Your data will not be encrypted. Is it ok?", null);
            if (result.get() == ButtonType.OK) {
                return true;
            }
            //alertHelper.showErrorAlert(parentStage, null, "Secret cannot be blank.");
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