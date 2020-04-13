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

    List<NoteEntry> getAll() throws IOException;

    List<NoteEntry> searchNotes(String searchParam, boolean searchInfo) throws IOException;

    long addNoteEntry(NoteEntry noteEntry) throws IOException;

}