package com.jc.jnotes.dao;

import java.io.IOException;
import java.util.List;

import com.jc.jnotes.model.NoteEntry;

/**
 * Local version of NoteEntryDao should also provide functionality of Search along with CRUD.
 * 
 * @author Joy C
 *
 */
public interface LocalNoteEntryDao  extends NoteEntryDao {

    List<NoteEntry> searchNotes(String searchParam, boolean searchInfo) throws IOException;

}