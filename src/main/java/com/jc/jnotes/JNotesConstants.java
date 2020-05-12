package com.jc.jnotes;

import java.time.format.DateTimeFormatter;

public final class JNotesConstants {
    private JNotesConstants () {
        
    }
    
    public static final String APP_NAME = "JNotes";
    public static final String DEFAULT_NOTEBOOK = "default";
    public static final DateTimeFormatter DATETIME_DISPLAY_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
    public static final DateTimeFormatter DATETIME_EXPORT_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    public static final String LINE_SEPARATOR = System.getProperty("line.separator");
    public static final String EXPORT_FILE_SUFFIX = "_export.csv";
    public static final String FIELD_SEPARATOR = ",";
    public static final String EQUALS_FIELD_SEPARATOR = "=";
    public static final String CURRENT_VERSION = JNotesConstants.class.getPackage().getImplementationVersion();
    public static final String USER_HOME_PATH = System.getProperty("user.home");
    public static final String ONLINE_SYNC_CONF_FILE = "jn_sync_DO_NOT_TOUCH.txt";
    public static final String CASSANDRA_CONNECTION_PATH = "/Users/d3m0n/workzone/secure-connect-jnotes.zip";
    public static final String INSTALLER_USERNAME = "jnotes_installer";
    public static final String INSTALLER_PASSWORD = "Installer1234!";
    public static final String CLIENT_USERNAME = "jnotes_user";
    public static final String CLIENT_PASSWORD = "User1234!";
    
}
