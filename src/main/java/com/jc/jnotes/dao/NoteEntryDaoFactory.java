package com.jc.jnotes.dao;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.jc.jnotes.JNotesPreferences;
import com.jc.jnotes.dao.LocalNoteEntryDao;
import com.jc.jnotes.dao.RemoteNoteEntryDao;
import com.jc.jnotes.dao.LuceneNoteEntryDao;

/**
 * 
 * @author Joy C
 *
 */
public final class NoteEntryDaoFactory {
    
    private NoteEntryDaoFactory() {
        
    }

    private final static Map<String, LocalNoteEntryDao> LOCAL_DAO_MAP = new HashMap<>();

    public static LocalNoteEntryDao getLocalNoteEntryDao() throws IOException {
        final String mapKey = JNotesPreferences.getBasePath() + JNotesPreferences.getCurrentNoteBook();

        if (LOCAL_DAO_MAP.get(mapKey) != null) {
            return LOCAL_DAO_MAP.get(mapKey);
        } else {
            LocalNoteEntryDao noteEntryDao = new LuceneNoteEntryDao();
            LOCAL_DAO_MAP.put(mapKey, noteEntryDao);
            return noteEntryDao;
        }
    }
    
    public static RemoteNoteEntryDao getRemoteNoteEntryDao()  {
        return null;
    }

}