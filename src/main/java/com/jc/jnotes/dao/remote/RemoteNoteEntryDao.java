package com.jc.jnotes.dao.remote;

import java.io.IOException;

import com.jc.jnotes.dao.NoteEntryDao;

/**
 * Remote version of NoteEntryDao should also provide functionality of backup and restore along with CRUD.
 * 
 * @author Joy C
 *
 */
public interface RemoteNoteEntryDao extends NoteEntryDao {

    long restore() throws IOException;

    long backup() throws IOException;

    void closeConnection();

}