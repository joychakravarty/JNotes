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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.prefs.Preferences;

import org.apache.commons.lang3.StringUtils;

import static com.jc.jnotes.JNotesConstants.*;

/**
 * 
 * This class stores the user preferences.
 * 
 * @author Joy C
 *
 */
public final class UserPreferences {

    private static final String KEY_CURRENT_NOTEBOOK = "currentNotebook";
    private static final String JNOTES_USER_ID = "jnotes_userid";
    private static final String JNOTES_USER_SECRET = "jnotes_usersecret";
    private static final String JNOTES_IS_CONNECTED = "jnotes_isconnected";
    private static final String JNOTES_AUTOCONNECT = "jnotes_autoconnect";

    private static final Preferences PREFERENCES = Preferences.userNodeForPackage(UserPreferences.class);
    private final String basePath;

    public UserPreferences(String basePath) {
        if(StringUtils.isNotEmpty(basePath)) {
            this.basePath = basePath;
        } else {
            this.basePath = USER_HOME_PATH;
        }
        try {
            this.getOnlineSyncConfFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void clearUserIdAndUserSecret() {
        PREFERENCES.remove(JNOTES_USER_ID);
        PREFERENCES.remove(JNOTES_USER_SECRET);
    }

    public String getBasePath() {
        return this.basePath;
    }

    public String getCurrentNotebook() {
        return PREFERENCES.get(KEY_CURRENT_NOTEBOOK, DEFAULT_NOTEBOOK);
    }

    public void setCurrentNotebook(String currentNotebook) {
        PREFERENCES.put(KEY_CURRENT_NOTEBOOK, currentNotebook);
    }

    public void setUserIdAndSecretForOnlineSync(String userId, String secret) {
        if (StringUtils.isBlank(userId)) {
            throw new IllegalArgumentException("UserId cannot be null or blank");
        }
        if (secret == null) {
            return;
        }
        PREFERENCES.put(JNOTES_USER_ID, userId);
        PREFERENCES.put(JNOTES_USER_SECRET, secret);

        String jnSyncStr = userId + "|" + secret;

        Path path = Paths.get(getBasePath(), LOCAL_STORE_NAME, ONLINE_SYNC_CONF_FILE);
        byte[] strToBytes = jnSyncStr.getBytes();

        try {
            Files.write(path, strToBytes);
        } catch (IOException e) {
            e.printStackTrace(); // We proceed with relying only on Preferences.
        }
    }

    public String getUserId() {
        String userName = PREFERENCES.get(JNOTES_USER_ID, null);
        if (StringUtils.isBlank(userName)) {// attempt to get userName from sync_conf file
            try {
                Path path = getOnlineSyncConfFile();
                List<String> allLines = Files.readAllLines(path);
                if (allLines != null && !allLines.isEmpty()) {
                    String jnSyncStr = Files.readAllLines(path).get(0);
                    if (jnSyncStr != null && jnSyncStr.contains("|")) {
                        userName = (jnSyncStr.split("\\|"))[0];
                        if (userName != null) {
                            PREFERENCES.put(JNOTES_USER_ID, userName);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace(); // We proceed assuming user has not provided online sync details yet.
            }
        }
        return userName;
    }

    private Path getOnlineSyncConfFile() throws IOException {
        Files.createDirectories(Paths.get(this.getBasePath(), LOCAL_STORE_NAME));
        Path path = Paths.get(getBasePath(), LOCAL_STORE_NAME, ONLINE_SYNC_CONF_FILE);
        File onlineSyncConfFile = path.toFile();
        onlineSyncConfFile.createNewFile();
        return path;
    }

    public String getUserSecret() {
        String userSecret = PREFERENCES.get(JNOTES_USER_SECRET, null);
        if (userSecret == null) {// attempt to get userSecret from sync_conf file
            try {
                Path path = getOnlineSyncConfFile();
                List<String> allLines = Files.readAllLines(path);
                if (allLines != null && !allLines.isEmpty()) {
                    String jnSyncStr = Files.readAllLines(path).get(0);
                    if (jnSyncStr != null && jnSyncStr.contains("|")) {
                        userSecret = (jnSyncStr.split("\\|"))[1];
                        if (userSecret != null) {
                            PREFERENCES.put(JNOTES_USER_SECRET, userSecret);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace(); // We proceed assuming user has not provided online sync details yet.
            }
        }
        return userSecret;
    }

    public void setConnected(boolean isConnected) {
        PREFERENCES.put(JNOTES_IS_CONNECTED, String.valueOf(isConnected));
    }

    public boolean isConnected() {
        String isConnectedStr = PREFERENCES.get(JNOTES_IS_CONNECTED, String.valueOf(false));
        return Boolean.valueOf(isConnectedStr);
    }

    public void setAutoConnect(boolean autoConnect) {
        PREFERENCES.put(JNOTES_AUTOCONNECT, String.valueOf(autoConnect));
    }

    public boolean getAutoConnect() {
        String autoConnectStr = PREFERENCES.get(JNOTES_AUTOCONNECT, String.valueOf(false));
        return Boolean.valueOf(autoConnectStr);
    }

}
