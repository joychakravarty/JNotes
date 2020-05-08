package com.jc.jnotes.dao;

import java.io.IOException;
import java.util.List;

import com.jc.jnotes.model.NoteEntry;

/**
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
    List<NoteEntry> getAll() throws IOException;

    List<NoteEntry> searchNotes(String searchParam, boolean searchInfo) throws IOException;

    long addNoteEntry(NoteEntry noteEntry) throws IOException;

    long editNoteEntry(NoteEntry noteEntry) throws IOException;

    void deleteNoteEntry(NoteEntry noteEntry) throws IOException;

    void deleteNoteEntries(List<NoteEntry> noteEntries) throws IOException;

}