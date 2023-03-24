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
 *  along with JNotes.  If not, see <https://www.gnu.org/licenses/>.
 * 
 * 
 */
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

    public static final String EXTENSION_JSON = ".json";
    public static final String EXTENSION_PROPERTIES = ".properties";
    public static final String EXPORT_FILE_SUFFIX = "_export" + EXTENSION_JSON;
    public static final String FIELD_SEPARATOR = ",";
    public static final String EQUALS_FIELD_SEPARATOR = "=";
    public static final String CURRENT_VERSION = JNotesConstants.class.getPackage().getImplementationVersion();
    public static final String USER_HOME_PATH = System.getProperty("user.home");
    public static final String ONLINE_SYNC_CONF_FILE = "jn_sync_DO_NOT_TOUCH.txt";
    public static final String REMOTE_URL = "https://jnotes-api.azurewebsites.net/";
    //public static final String REMOTE_URL = "http://localhost:5000/";
    
}
