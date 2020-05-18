package com.jc.jnotes.dao.remote;

import java.util.List;
import java.util.Map;

import com.jc.jnotes.dao.NoteEntryDao;
import com.jc.jnotes.model.NoteEntry;

/**
 * Remote version of NoteEntryDao should also provide functionality of backup and restore(getAll)
 * along with CRUD.
 * 
 * @author Joy C
 *
 */
public interface RemoteNoteEntryDao extends NoteEntryDao {
    
    boolean setupUser(String userId);

    boolean backup(String notebook, List<NoteEntry> notes);

    void disconnect();

    /**
     * 
     * @return 0 - success
     *  <br/>1 - userId does not exist 
     *  <br/>2 - userSecret is not correct
     */
    int validateUserSecret();

    Map<String, List<NoteEntry>> restore();

}