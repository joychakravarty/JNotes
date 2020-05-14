package com.jc.jnotes.dao.remote;

import java.util.List;

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

}