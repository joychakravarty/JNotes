package com.jc.jnotes;

import java.time.format.DateTimeFormatter;
import java.util.prefs.Preferences;

public final class JNotesPreferences {
    
    private JNotesPreferences() {
        
    }

    public static final String DEFAULT_APP_NAME = "JNotes";
    public static final String DEFAULT_PROFILE = "default";
    public static final DateTimeFormatter DEFAULT_DATETIME_DISPLAY_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
    public static final String DEFAULT_LINE_SEPARATOR = System.getProperty("line.separator");
    public static final String DEFAULT_EXPORT_FILE_SUFFIX = "_export.csv";
    public static final String DEFAULT_FIELD_SEPARATOR = ",";
    public static final String EQUALS_FIELD_SEPARATOR = "=";

	private static final Preferences USER_PREFERENCES  = Preferences.userNodeForPackage(JNotesPreferences.class);
	private static final String USER_PREF_BASEPATH = "basePath";
	private static final String USER_HOME = "user.home";
	private static final String USER_PREF_CURRENT_PROFILE = "currentProfile";
	
	
	public static String getBasePath() {
        return USER_PREFERENCES.get(USER_PREF_BASEPATH, System.getProperty(USER_HOME));
	}
	
	public static void setBasePath(String basePath) {
        USER_PREFERENCES.put(USER_PREF_BASEPATH, basePath);
	}
	
	public static String getCurrentProfile() { 
		return USER_PREFERENCES.get(USER_PREF_CURRENT_PROFILE, DEFAULT_PROFILE);
	}
	
	public static void setCurrentProfile(String currentProfile) { 
		USER_PREFERENCES.put(USER_PREF_CURRENT_PROFILE, currentProfile);
	}
	
}
