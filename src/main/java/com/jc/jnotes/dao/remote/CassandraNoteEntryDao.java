package com.jc.jnotes.dao.remote;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.jc.jnotes.model.NoteEntry;

/**
 * 
 * @author Joy C
 *
 */
public class CassandraNoteEntryDao implements RemoteNoteEntryDao {
    
    private CassandraSessionManager cassandraSessionManager;
    private String userId;
    private String userEncryptionKey;
    
    private static final String GET_ALL = "SELECT * FROM %s.%s WHERE notebook = ?";
    private static final String ADD_NOTE_ENTRY = "INSERT INTO %s.%s (notebook, noteid, key, value, info, lastModifiedTime) VALUES (?,?,?,?,?,?)";
    
    public CassandraNoteEntryDao(CassandraSessionManager cassandraSessionManager, String userId, String userEncKey) {
        this.cassandraSessionManager = cassandraSessionManager;
        this.userId = userId;
        this.userEncryptionKey = userEncKey;
    }

    @Override
    public List<NoteEntry> getAll(String notebook) throws IOException {
        
        String cqlStr = String.format(GET_ALL, cassandraSessionManager.getKeyspace(), userId);
        
        ResultSet results = cassandraSessionManager.getClientSession().execute(
                SimpleStatement.builder( cqlStr)
                        .addPositionalValues(notebook)
                        .build());
            Row row = results.one();
            System.out.println("***************************************************************************************");
            if (row == null) {
                System.out.println("No row selected");
            } else {
                System.out.format("%s %s %s\n",
                    row.getString("key"),
                    row.getString("value"),
                    row.getUuid("info"));
            }
        
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long addNoteEntry(String notebook, NoteEntry noteEntry) throws IOException {
        
        String cqlStr = String.format(ADD_NOTE_ENTRY, cassandraSessionManager.getKeyspace(), userId);
        
        cassandraSessionManager.getClientSession().execute(
                SimpleStatement.builder( cqlStr)
                        .addPositionalValues(notebook, noteEntry.getId(), noteEntry.getKey(), noteEntry.getValue(), noteEntry.getInfo(), noteEntry.getLastModifiedTime())
                        .build());
        return 0;
    }

    @Override
    public long editNoteEntry(String notebook, NoteEntry noteEntry) throws IOException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void deleteNoteEntry(String notebook, NoteEntry noteEntry) throws IOException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void deleteNoteEntries(String notebook, List<NoteEntry> noteEntries) throws IOException {
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
