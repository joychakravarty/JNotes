package com.jc.jnotes.dao;

import java.io.IOException;
import java.util.List;

import com.jc.jnotes.model.NoteEntry;

/**
 *
 * This is the base interface with the basic CRUD functionality for NoteEntries of a notebook.
 * 
 * @author Joy C
 *
 */
public interface NoteEntryDao {
    
    /**
     * 
     * @return lists all entries with latest modified on top (ls -lt)
     * @throws IOException
     */
    List<NoteEntry> getAll(String notebook) throws IOException;

    long addNoteEntry(String notebook, NoteEntry noteEntry) throws IOException;

    long editNoteEntry(String notebook, NoteEntry noteEntry) throws IOException;

    void deleteNoteEntry(String notebook, NoteEntry noteEntry) throws IOException;

    void deleteNoteEntries(String notebook, List<NoteEntry> noteEntries) throws IOException;

}