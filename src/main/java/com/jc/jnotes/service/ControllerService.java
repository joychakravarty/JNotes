package com.jc.jnotes.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.lucene.index.IndexNotFoundException;

import com.jc.jnotes.dao.NoteEntryDaoFactory;
import com.jc.jnotes.model.NoteEntry;

/**
 * 
 * 
 * @author Joy C
 *
 */
public class ControllerService {

    public void addNoteEntry(NoteEntry noteEntry) throws ControllerServiceException {
        try {
            NoteEntryDaoFactory.getLocalNoteEntryDao().addNoteEntry(noteEntry);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ControllerServiceException("Exception while creating new Note", ex);
        }

    }

    public void editNoteEntry(NoteEntry noteEntry) throws ControllerServiceException {
        try {
            NoteEntryDaoFactory.getLocalNoteEntryDao().editNoteEntry(noteEntry);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ControllerServiceException("Exception while updating Note", ex);
        }
    }

    public List<NoteEntry> searchNotes(String searchTxt, boolean searchInfoAlso) {
        List<NoteEntry> notes;
        try {
            notes = NoteEntryDaoFactory.getLocalNoteEntryDao().searchNotes(searchTxt, searchInfoAlso);
        } catch (Exception ex) {
            ex.printStackTrace();
            notes = Collections.emptyList();
        }
        return notes;
    }

    public List<NoteEntry> getAll() throws ControllerServiceException {
        List<NoteEntry> allNoteEntries;
        try {
            allNoteEntries = NoteEntryDaoFactory.getLocalNoteEntryDao().getAll();
        } catch (IndexNotFoundException indexNotFoundEx) {
            allNoteEntries = new ArrayList<>();
            System.err.println("Exception in loadAllNoteEntries - IndexNotfound. " + indexNotFoundEx.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ControllerServiceException("Exception while getting all Notes", ex);
        }
        return allNoteEntries;
    }

    public void deleteNoteEntries(List<NoteEntry> noteEntriesToBeDeleted) throws IOException {
        NoteEntryDaoFactory.getLocalNoteEntryDao().deleteNoteEntries(noteEntriesToBeDeleted);
    }

}
