package com.jc.jnotes;

import static com.jc.jnotes.JNotesConstants.APP_NAME;
import static com.jc.jnotes.JNotesConstants.DEFAULT_NOTEBOOK;
import static com.jc.jnotes.JNotesConstants.USER_HOME_PATH;
import static com.jc.jnotes.JNotesConstants.ONLINE_SYNC_CONF_FILE;

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
public final class JNotesPreferences {

    private JNotesPreferences() {

    }

    private static final Preferences USER_PREFERENCES = Preferences.userNodeForPackage(JNotesPreferences.class);
    private static final String USER_PREF_BASEPATH = "basePath";
    private static final String USER_PREF_CURRENT_NOTEBOOK = "currentNoteBook";
    private static final String JNOTES_USER_NAME = "jnotes_username";
    private static final String JNOTES_USER_SECRET = "jnotes_usersecret";

    public static String getBasePath() {
        return USER_PREFERENCES.get(USER_PREF_BASEPATH, USER_HOME_PATH);
    }

    public static void setBasePath(String basePath) {
        USER_PREFERENCES.put(USER_PREF_BASEPATH, basePath);
    }

    public static String getCurrentNoteBook() {
        return USER_PREFERENCES.get(USER_PREF_CURRENT_NOTEBOOK, DEFAULT_NOTEBOOK);
    }

    public static void setCurrentNoteBook(String currentNoteBook) {
        USER_PREFERENCES.put(USER_PREF_CURRENT_NOTEBOOK, currentNoteBook);
    }

    public static void setUserNameAndSecretForOnlineSync(String userName, String secret) {
        USER_PREFERENCES.put(JNOTES_USER_NAME, userName);
        USER_PREFERENCES.put(JNOTES_USER_SECRET, secret);

        String jnSyncStr = userName + "|" + secret;

        Path path = Paths.get(getBasePath(), APP_NAME, ONLINE_SYNC_CONF_FILE);
        byte[] strToBytes = jnSyncStr.getBytes();

        try {
            Files.write(path, strToBytes);
        } catch (IOException e) {
            e.printStackTrace(); // We proceed with relying only on Preferences.
        }
    }

    public static String getUserName() {
        String userName = USER_PREFERENCES.get(JNOTES_USER_NAME, null);
        if (userName == null) {// attempt to get userName from sync_conf file
            try {
                Path path = Paths.get(getBasePath(), APP_NAME, ONLINE_SYNC_CONF_FILE);
                String jnSyncStr = Files.readAllLines(path).get(0);

                if (jnSyncStr != null && jnSyncStr.contains("|")) {
                    userName = (jnSyncStr.split("\\|"))[0];
                    if(userName!=null) {
                        USER_PREFERENCES.put(JNOTES_USER_NAME, userName);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace(); // We proceed assuming user has not provided online sync details yet.
            }
        }
        return userName;
    }
    
    public static String getUserSecret() {
        String userSecret = USER_PREFERENCES.get(JNOTES_USER_SECRET, null);
        if (userSecret == null) {// attempt to get userSecret from sync_conf file
            try {
                Path path = Paths.get(getBasePath(), APP_NAME, ONLINE_SYNC_CONF_FILE);
                String jnSyncStr = Files.readAllLines(path).get(0);

                if (jnSyncStr != null && jnSyncStr.contains("|")) {
                    userSecret = (jnSyncStr.split("\\|"))[1];
                    if(userSecret!=null) {
                        USER_PREFERENCES.put(JNOTES_USER_SECRET, userSecret);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace(); // We proceed assuming user has not provided online sync details yet.
            }
        }
        return userSecret;
    }

}
