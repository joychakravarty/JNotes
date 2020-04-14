package com.jc.jnotes;

import java.util.prefs.Preferences;

public class JNotesPreferences {

	private static final Preferences USER_PREFERENCES  = Preferences.userNodeForPackage(JNotesPreferences.class);
	private static final String APP_NAME = "JNotes";
	private static final String USER_PREF_BASEPATH = "basePath";
	private static final String USER_HOME = "user.home";
	private static final String USER_PREF_CURRENT_PROFILE = "currentProfile";
	private static final String DEFAULT_PROFILE = "default";
	
	
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
	
	public static String getAppName() {
	    return APP_NAME;
	}
	
	
}
