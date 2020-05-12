package com.jc.jnotes.dao;

import java.io.IOException;
import java.util.List;

import com.jc.jnotes.model.NoteEntry;

/**
 * Remote version of NoteEntryDao should also provide functionality of backup and restore along with CRUD.
 * 
 * @author Joy C
 *
 */
public interface RemoteNoteEntryDao extends NoteEntryDao {

    long restore() throws IOException;

    long backup() throws IOException;

}