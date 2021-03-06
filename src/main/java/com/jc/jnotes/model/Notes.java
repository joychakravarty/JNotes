package com.jc.jnotes.model;

import java.util.Collections;
import java.util.List;

public final class Notes {
    
    public Notes() {
    }

    public Notes(List<NoteEntry> noteEntries) {
        this.setNoteEntries(Collections.unmodifiableList(noteEntries));
    }

    public List<NoteEntry> getNoteEntries() {
        
        return noteEntries;
    }

    public void setNoteEntries(List<NoteEntry> noteEntries) {
        this.noteEntries = noteEntries;
    }

    private List<NoteEntry> noteEntries;

}
