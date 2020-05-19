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
package com.jc.jnotes;

import static com.jc.jnotes.JNotesConstants.APP_NAME;
import static com.jc.jnotes.JNotesConstants.DEFAULT_NOTEBOOK;
import static com.jc.jnotes.JNotesConstants.ONLINE_SYNC_CONF_FILE;
import static com.jc.jnotes.JNotesConstants.USER_HOME_PATH;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.prefs.Preferences;

/**
 * 
 * This class stores the user preferences.
 * 
 * @author Joy C
 *
 */
public class UserPreferences {

    private static final String KEY_BASEPATH = "basePath";
    private static final String KEY_CURRENT_NOTEBOOK = "currentNotebook";
    private static final String JNOTES_USER_ID = "jnotes_userid";
    private static final String JNOTES_USER_SECRET = "jnotes_usersecret";
    private static final String JNOTES_IS_CONNECTED = "jnotes_isconnected";
    private static final String JNOTES_AUTOCONNECT = "jnotes_autoconnect";

    private final Preferences userPreferences = Preferences.userNodeForPackage(UserPreferences.class);

    public String getBasePath() {
        return userPreferences.get(KEY_BASEPATH, USER_HOME_PATH);
    }

    public void setBasePath(String basePath) {
        userPreferences.put(KEY_BASEPATH, basePath);
    }

    public String getCurrentNotebook() {
        return userPreferences.get(KEY_CURRENT_NOTEBOOK, DEFAULT_NOTEBOOK);
    }

    public void setCurrentNotebook(String currentNotebook) {
        userPreferences.put(KEY_CURRENT_NOTEBOOK, currentNotebook);
    }

    public void setUserIdAndSecretForOnlineSync(String userId, String secret) {
        userPreferences.put(JNOTES_USER_ID, userId);
        userPreferences.put(JNOTES_USER_SECRET, secret);

        String jnSyncStr = userId + "|" + secret;

        Path path = Paths.get(getBasePath(), APP_NAME, ONLINE_SYNC_CONF_FILE);
        byte[] strToBytes = jnSyncStr.getBytes();

        try {
            Files.write(path, strToBytes);
        } catch (IOException e) {
            e.printStackTrace(); // We proceed with relying only on Preferences.
        }
    }

    public String getUserId() {
        String userName = userPreferences.get(JNOTES_USER_ID, null);
        if (userName == null) {// attempt to get userName from sync_conf file
            try {
                Path path = Paths.get(getBasePath(), APP_NAME, ONLINE_SYNC_CONF_FILE);
                String jnSyncStr = Files.readAllLines(path).get(0);

                if (jnSyncStr != null && jnSyncStr.contains("|")) {
                    userName = (jnSyncStr.split("\\|"))[0];
                    if (userName != null) {
                        userPreferences.put(JNOTES_USER_ID, userName);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace(); // We proceed assuming user has not provided online sync details yet.
            }
        }
        return userName;
    }

    public String getUserSecret() {
        String userSecret = userPreferences.get(JNOTES_USER_SECRET, null);
        if (userSecret == null) {// attempt to get userSecret from sync_conf file
            try {
                Path path = Paths.get(getBasePath(), APP_NAME, ONLINE_SYNC_CONF_FILE);
                String jnSyncStr = Files.readAllLines(path).get(0);

                if (jnSyncStr != null && jnSyncStr.contains("|")) {
                    userSecret = (jnSyncStr.split("\\|"))[1];
                    if (userSecret != null) {
                        userPreferences.put(JNOTES_USER_SECRET, userSecret);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace(); // We proceed assuming user has not provided online sync details yet.
            }
        }
        return userSecret;
    }

    public void setConnected(boolean isConnected) {
        userPreferences.put(JNOTES_IS_CONNECTED, String.valueOf(isConnected));
    }

    public boolean isConnected() {
        String isConnectedStr = userPreferences.get(JNOTES_IS_CONNECTED, String.valueOf(false));
        return Boolean.valueOf(isConnectedStr);
    }

    public void setAutoConnect(boolean autoConnect) {
        userPreferences.put(JNOTES_AUTOCONNECT, String.valueOf(autoConnect));
    }

    public boolean getAutoConnect() {
        String autoConnectStr = userPreferences.get(JNOTES_AUTOCONNECT, String.valueOf(false));
        return Boolean.valueOf(autoConnectStr);
    }

}
