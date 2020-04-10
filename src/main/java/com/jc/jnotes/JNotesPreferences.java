package com.jc.jnotes;

import java.util.prefs.Preferences;

public class JNotesPreferences {

	private static final Preferences USER_PREFERENCES  = Preferences.userNodeForPackage(JNotesPreferences.class);
	
	public static String getBasePath() {
        return USER_PREFERENCES.get("basePath", System.getProperty("user.home"));
	}
	
	public static void setBasePath(String basePath) {
        USER_PREFERENCES.put("basePath", basePath);
	}
	
	public static String getCurrentProfile() { 
		return USER_PREFERENCES.get("currentProfile", "default");
	}
	
	public static void setCurrentProfile(String currentProfile) { 
		USER_PREFERENCES.put("currentProfile", currentProfile);
	}
	
	
}
