package com.jc.jnotes.dao.remote.cassandra;

import static com.datastax.oss.driver.api.querybuilder.QueryBuilder.literal;
import static com.datastax.oss.driver.api.querybuilder.QueryBuilder.update;
import static com.datastax.oss.driver.api.querybuilder.SchemaBuilder.createTable;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.datastax.oss.driver.api.core.ConsistencyLevel;
import com.datastax.oss.driver.api.core.cql.BatchStatement;
import com.datastax.oss.driver.api.core.cql.BatchStatementBuilder;
import com.datastax.oss.driver.api.core.cql.BatchType;
import com.datastax.oss.driver.api.core.cql.BatchableStatement;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.core.servererrors.AlreadyExistsException;
import com.datastax.oss.driver.api.core.servererrors.InvalidQueryException;
import com.datastax.oss.driver.api.core.type.DataTypes;
import com.datastax.oss.driver.api.querybuilder.schema.CreateTable;
import com.datastax.oss.driver.api.querybuilder.update.Update;
import com.jc.jnotes.dao.remote.RemoteNoteEntryDao;
import com.jc.jnotes.model.NoteEntry;
import com.jc.jnotes.util.EncryptionUtil;

/**
 * 
 * This class uses (DataStax-Astra) CassandraDB as the Remote DataStore.<br>
 * As this desktop application interacts directly with the DB (without a indirection of a web service), the credentials
 * to connect are shipped with JNotes.<br>
 * So it is tricky but vital to ensure data-privacy (Challenge Accepted). The aim is to be as much hacker-proof as
 * possible and I try to achieve that by:<br>
 * 1. data modeling/schema design<br>
 * 2. role permissions<br>
 *
 * 
 * @author Joy C
 *
 */
@Component
public class CassandraNoteEntryDao implements RemoteNoteEntryDao {

    @Autowired
    private CassandraSessionManager sessionManager;

    private String userId;
    private String userEncryptionKey;

    private static final String GET_ALL_NOTES_FOR_NOTEBOOK = "SELECT * FROM %s WHERE notebook = ?";
    private static final String GET_ALL_NOTES_FOR_USER = "SELECT * FROM %s";
    private static final String ADD_NOTE_ENTRY = "INSERT INTO %s (notebook, noteid, key, value, info, lastModifiedTime) VALUES (?,?,?,?,?,?)";
    private static final String EDIT_NOTE_ENTRY = "UPDATE %s SET key = ?, value = ?, info = ?, lastModifiedTime = ? where notebook = ? and noteid = ?";
    private static final String DELETE_NOTE_ENTRIES = "DELETE FROM %s WHERE notebook = ? and noteid IN ?";

    private static final String ADD_USERSCRET_VALIDATION_ROW = "INSERT INTO %s_secret_validation (userid, encrypted_validation_text) VALUES (?, ?)";
    private static final String GET_USERSCRET_VALIDATION_ROW = "SELECT * FROM %s_secret_validation WHERE userid = ?";

    private static final String VALIDATION_TEXT = "CheckSecret123";

    private PreparedStatement preparedAddNoteEntry;

    public CassandraNoteEntryDao(String userId, String userEncKey) {
        System.out.println("Creating CassandraNoteEntryDao : " + userId + "-" + userEncKey);
        this.userId = userId;
        this.userEncryptionKey = userEncKey;
    }

    /**
     * 1. Create userId table which will store the notes for this user. - a user's userId also acts as his password, without
     * knowing the userId no one can access/modify its content. 2. Create userId_secret_validation table which will store
     * the validation text in an encrypted form using user's secret-key - this is to ensure that user doesn't accidentally
     * connect using a wrong secret. Note: secret itself is not stored! 3. Store the secret-validation-text in an encrypted
     * form. 4. Create prepared statement for Add Note Entry. (TODO: create prepared statement for Edit and Delete as well)
     */
    @Override
    public boolean setupUser(String userId) {
        try {
            CreateTable createUserTable = createTable(userId).withPartitionKey("notebook", DataTypes.TEXT)
                    .withClusteringColumn("noteid", DataTypes.TEXT).withColumn("key", DataTypes.TEXT).withColumn("value", DataTypes.TEXT)
                    .withColumn("info", DataTypes.TEXT).withColumn("lastModifiedTime", DataTypes.TIMESTAMP);

            sessionManager.getClientSession().execute(createUserTable.build());

            CreateTable createUserSecretValidationTable = createTable(userId + "_secret_validation")
                    .withPartitionKey("userid", DataTypes.TEXT).withColumn("encrypted_validation_text", DataTypes.TEXT);

            sessionManager.getClientSession().execute(createUserSecretValidationTable.build());

        } catch (AlreadyExistsException ex) {
            System.err.println(userId + " already exists");
            return false;
        }

        sessionManager.getClientSession().execute(SimpleStatement.builder(String.format(ADD_USERSCRET_VALIDATION_ROW, userId))
                .addPositionalValues(userId, EncryptionUtil.encrypt(userEncryptionKey, VALIDATION_TEXT)).build());

        String addNoteEntryCQL = String.format(ADD_NOTE_ENTRY, userId);
        preparedAddNoteEntry = sessionManager.getClientSession().prepare(addNoteEntryCQL);

        return true;
    }

    @Override
    public List<NoteEntry> getAll(String notebook) {

        String cqlStr = String.format(GET_ALL_NOTES_FOR_NOTEBOOK, userId);
        ResultSet results;
        try {
            results = sessionManager.getClientSession().execute(SimpleStatement.builder(cqlStr).addPositionalValues(notebook).build());
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
        noteEntry.setNotebook(row.getString("notebook"));
        return noteEntry;
    }

    @Override
    public long addNoteEntry(String notebook, NoteEntry noteEntry) {
        sessionManager.getClientSession().execute(getBoundStatementForAddNoteEntry(notebook, noteEntry));

        // String cqlStr = String.format(ADD_NOTE_ENTRY, userId);
        //
        // cassandraSessionManager.getClientSession()
        // .execute(SimpleStatement.builder(cqlStr)
        // .addPositionalValues(notebook, noteEntry.getId(), EncryptionUtil.encrypt(userEncryptionKey, noteEntry.getKey()),
        // EncryptionUtil.encrypt(userEncryptionKey, noteEntry.getValue()),
        // EncryptionUtil.encrypt(userEncryptionKey, noteEntry.getInfo()),
        // noteEntry.getLastModifiedTime().toInstant(ZoneOffset.UTC))
        // .build());
        return 0;
    }

    private BoundStatement getBoundStatementForAddNoteEntry(String notebook, NoteEntry noteEntry) {
        return preparedAddNoteEntry.bind(notebook, noteEntry.getId(), EncryptionUtil.encrypt(userEncryptionKey, noteEntry.getKey()),
                EncryptionUtil.encrypt(userEncryptionKey, noteEntry.getValue()),
                EncryptionUtil.encrypt(userEncryptionKey, noteEntry.getInfo()), noteEntry.getLastModifiedTime().toInstant(ZoneOffset.UTC));
    }

    @Override
    public long editNoteEntry(String notebook, NoteEntry noteEntry) {
        String cqlStr = String.format(EDIT_NOTE_ENTRY, userId);

        sessionManager.getClientSession()
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
        String cqlStr = String.format(DELETE_NOTE_ENTRIES, userId);
        sessionManager.getClientSession().execute(SimpleStatement.builder(cqlStr).addPositionalValues(notebook, noteIds).build());
    }

    @Override
    public boolean backup(String notebook, List<NoteEntry> notes) {
        Iterable<BatchableStatement<?>> statements = notes.stream()
                .map((noteEntry) -> getBoundStatementForAddNoteEntry(notebook, noteEntry)).collect(Collectors.toList());
        BatchStatement batch = new BatchStatementBuilder(BatchType.UNLOGGED).setKeyspace(sessionManager.getKeyspace())
                .setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM).addStatements(statements).build();
        sessionManager.getClientSession().execute(batch);
        return true;
    }

    @Override
    public Map<String, List<NoteEntry>> restore() {

        String cqlStr = String.format(GET_ALL_NOTES_FOR_USER, userId);
        ResultSet results;
        try {
            results = sessionManager.getClientSession().execute(SimpleStatement.builder(cqlStr).build());
        } catch (InvalidQueryException ex) {
            System.err.println("Caught InvalidQueryException");
            return null;
        }
        List<Row> rows = results.all();
        Map<String, List<NoteEntry>> notebookMap = new HashMap<>();
        if (rows != null && !rows.isEmpty()) {
            rows.stream()
            .map(row -> toNoteEntry(row))
            .forEach((noteEntry) -> notebookMap.computeIfAbsent(noteEntry.getNotebook(), (k) -> new ArrayList<NoteEntry>()).add(noteEntry));
        }
        return notebookMap;
    }

    @Override
    public void disconnect() {
        sessionManager.closeClientSession();
    }

    @Override
    public int validateUserSecret() {
        ResultSet results;
        results = sessionManager.getClientSession()
                .execute(SimpleStatement.builder(String.format(GET_USERSCRET_VALIDATION_ROW, userId)).addPositionalValues(userId).build());
        Row row = results.one();
        if (row == null) {
            return 1;
        } else {
            String encryptedValidationText = row.getString("encrypted_validation_text");
            if (VALIDATION_TEXT.equals(EncryptionUtil.decrypt(userEncryptionKey, encryptedValidationText))) {
                String addNoteEntryCQL = String.format(ADD_NOTE_ENTRY, userId);
                preparedAddNoteEntry = sessionManager.getClientSession().prepare(addNoteEntryCQL);
                return 0;
            } else {
                return 2;
            }
        }
    }

}
