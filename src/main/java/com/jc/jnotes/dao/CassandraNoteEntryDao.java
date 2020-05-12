package com.jc.jnotes.dao;

import java.io.IOException;
import java.util.List;

import com.jc.jnotes.model.NoteEntry;

/**
 * 
 * @author Joy C
 *
 */
public class CassandraNoteEntryDao implements RemoteNoteEntryDao {

    @Override
    public List<NoteEntry> getAll() throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long addNoteEntry(NoteEntry noteEntry) throws IOException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public long editNoteEntry(NoteEntry noteEntry) throws IOException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void deleteNoteEntry(NoteEntry noteEntry) throws IOException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void deleteNoteEntries(List<NoteEntry> noteEntries) throws IOException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public long restore() throws IOException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public long backup() throws IOException {
        // TODO Auto-generated method stub
        return 0;
    }

}
