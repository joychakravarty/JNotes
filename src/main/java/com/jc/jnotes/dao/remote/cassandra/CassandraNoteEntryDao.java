package com.jc.jnotes.dao.remote.cassandra;

import static com.datastax.oss.driver.api.querybuilder.SchemaBuilder.createTable;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.core.servererrors.AlreadyExistsException;
import com.datastax.oss.driver.api.core.servererrors.InvalidQueryException;
import com.datastax.oss.driver.api.core.type.DataTypes;
import com.datastax.oss.driver.api.querybuilder.schema.CreateTable;
import com.jc.jnotes.dao.remote.RemoteNoteEntryDao;
import com.jc.jnotes.model.NoteEntry;
import com.jc.jnotes.util.EncryptionUtil;

/**
 * 
 * @author Joy C
 *
 */
@Component
public class CassandraNoteEntryDao implements RemoteNoteEntryDao {

    @Autowired
    private CassandraSessionManager cassandraSessionManager;

    private String userId;
    private String userEncryptionKey;

    private static final String GET_ALL = "SELECT * FROM %s.%s WHERE notebook = ?";
    private static final String ADD_NOTE_ENTRY = "INSERT INTO %s.%s (notebook, noteid, key, value, info, lastModifiedTime) VALUES (?,?,?,?,?,?)";
    private static final String EDIT_NOTE_ENTRY = "UPDATE %s.%s SET key = ?, value = ?, info = ?, lastModifiedTime = ? where notebook = ? and noteid = ?";
    private static final String DELETE_NOTE_ENTRIES = "DELETE FROM %s.%s WHERE notebook = ? and noteid IN ?";

    public CassandraNoteEntryDao(String userId, String userEncKey) {
        System.out.println("Creating CassandraNoteEntryDao : " + userId + "-" + userEncKey);
        this.userId = userId;
        this.userEncryptionKey = userEncKey;
    }

    @Override
    public boolean setupUser(String userId) {
        boolean returnStatus = true;
        try {
            CreateTable create = createTable(cassandraSessionManager.getKeyspace(), userId).withPartitionKey("notebook", DataTypes.TEXT)
                    .withClusteringColumn("noteid", DataTypes.TEXT).withColumn("key", DataTypes.TEXT).withColumn("value", DataTypes.TEXT)
                    .withColumn("info", DataTypes.TEXT).withColumn("lastModifiedTime", DataTypes.TIMESTAMP);

            cassandraSessionManager.getClientSession().execute(create.build());
        } catch (AlreadyExistsException ex) {
            System.err.println(userId + "already exists");
            returnStatus = false;
        }
        return returnStatus;
    }

    @Override
    public List<NoteEntry> getAll(String notebook) {

        String cqlStr = String.format(GET_ALL, cassandraSessionManager.getKeyspace(), userId);
        ResultSet results;
        try {
            results = cassandraSessionManager.getClientSession()
                    .execute(SimpleStatement.builder(cqlStr).addPositionalValues(notebook).build());
        } catch (InvalidQueryException ex) {
            System.err.println("Caught InvalidQueryException");
            return null;
        }
        List<Row> rows = results.all();
        if (rows != null && !rows.isEmpty()) {
            return rows.stream().map(row -> toNoteEntry(row)).collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }

    }

    private NoteEntry toNoteEntry(Row row) {
        NoteEntry noteEntry = new NoteEntry(row.getString("noteid"), EncryptionUtil.decrypt(userEncryptionKey, row.getString("key")),
                EncryptionUtil.decrypt(userEncryptionKey, row.getString("value")),
                EncryptionUtil.decrypt(userEncryptionKey, row.getString("info")),
                LocalDateTime.ofInstant(row.getInstant("lastModifiedTime"), ZoneOffset.UTC));
        return noteEntry;
    }

    @Override
    public long addNoteEntry(String notebook, NoteEntry noteEntry) {

        String cqlStr = String.format(ADD_NOTE_ENTRY, cassandraSessionManager.getKeyspace(), userId);

        cassandraSessionManager.getClientSession()
                .execute(SimpleStatement.builder(cqlStr)
                        .addPositionalValues(notebook, noteEntry.getId(), EncryptionUtil.encrypt(userEncryptionKey, noteEntry.getKey()),
                                EncryptionUtil.encrypt(userEncryptionKey, noteEntry.getValue()),
                                EncryptionUtil.encrypt(userEncryptionKey, noteEntry.getInfo()),
                                noteEntry.getLastModifiedTime().toInstant(ZoneOffset.UTC))
                        .build());
        return 0;
    }

    @Override
    public long editNoteEntry(String notebook, NoteEntry noteEntry) {
        String cqlStr = String.format(EDIT_NOTE_ENTRY, cassandraSessionManager.getKeyspace(), userId);

        cassandraSessionManager.getClientSession()
                .execute(SimpleStatement.builder(cqlStr)
                        .addPositionalValues(EncryptionUtil.encrypt(userEncryptionKey, noteEntry.getKey()),
                                EncryptionUtil.encrypt(userEncryptionKey, noteEntry.getValue()),
                                EncryptionUtil.encrypt(userEncryptionKey, noteEntry.getInfo()),
                                noteEntry.getLastModifiedTime().toInstant(ZoneOffset.UTC), notebook, noteEntry.getId())
                        .build());
        return 0;
    }

    @Override
    public void deleteNoteEntry(String notebook, NoteEntry noteEntry) {
        List<NoteEntry> noteEntries = new ArrayList<>();
        noteEntries.add(noteEntry);
        deleteNoteEntries(notebook, noteEntries);

    }

    @Override
    public void deleteNoteEntries(String notebook, List<NoteEntry> noteEntries) {
        List<String> noteIds = noteEntries.stream().map((noteEntry) -> noteEntry.getId()).collect(Collectors.toList());
        String cqlStr = String.format(DELETE_NOTE_ENTRIES, cassandraSessionManager.getKeyspace(), userId);
        cassandraSessionManager.getClientSession().execute(SimpleStatement.builder(cqlStr).addPositionalValues(notebook, noteIds).build());
    }

    @Override
    public boolean backup(String notebook, List<NoteEntry> notes) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void disconnect() {
        cassandraSessionManager.closeClientSession();
    }

}
