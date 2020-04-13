package com.jc.jnotes.dao;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.jc.jnotes.JNotesPreferences;

/**
 * 
 * @author Joy C
 *
 */
public final class NoteEntryDaoFactory {
    
    private NoteEntryDaoFactory() {
        
    }

    private final static Map<String, NoteEntryDao> DAO_MAP = new HashMap<>();

    public static NoteEntryDao getNoteEntryDao() throws IOException {
        final String mapKey = JNotesPreferences.getBasePath() + JNotesPreferences.getCurrentProfile();
        System.out.println("mapKey" + mapKey + " and DAO_MAP size" + DAO_MAP.size());

        if (DAO_MAP.get(mapKey) != null) {
            return DAO_MAP.get(mapKey);
        } else {
            NoteEntryDao noteEntryDao = new LuceneNoteEntryDao();
            DAO_MAP.put(mapKey, noteEntryDao);
            return noteEntryDao;
        }
    }

}