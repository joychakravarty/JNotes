/*
 * This file is part of JNotes. Copyright (C) 2020  Joy Chakravarty
 * 
 * JNotes is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JNotes is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JNotes.  If not, see <https://www.gnu.org/licenses/>.
 * 
 * 
 */
package com.jc.jnotes.dao.remote.cassandra;

import static com.datastax.oss.driver.api.querybuilder.SchemaBuilder.createTable;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

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
import com.jc.jnotes.dao.remote.RemoteNoteEntryDao;
import com.jc.jnotes.model.NoteEntry;
import com.jc.jnotes.util.EncryptionUtil;

/**
 * 
 * This class uses Cassandra DB as the Remote DataStore.<br>
 * As this desktop application interacts directly with the DB (without a indirection of a web service), the credentials
 * to connect are shipped with JNotes.<br>
 * So it is tricky but vital to ensure data-privacy (Challenge Accepted). The aim is to be as much hacker-proof as
 * possible and I try to achieve that by:<br>
 * 1. data modeling/schema design<br>
 * 2. role permissions<br>
 * 3. allow user to encrypt data using their own secret<br>
 * <br>
 * jnotes_client user cannot query system tables to find all the tables in the keyspace.<br>
 * So a potential hacker will not be able to access any other user's data without knowing their userId.<br>
 * And even if the hacker gets to know of some other userId, the worst action he could take is to delete/drop which
 * user can mitigate by re-backup.<br>
 * Importantly, Hacker will not be able to make sense of the data as he would need userSecret as well for decryption.<br>
 * 
 * 
 * @author Joy C
 *
 */
public class CassandraNoteEntryDao implements RemoteNoteEntryDao {

    private final CassandraSessionManager sessionManager;
    private final String userId;
    private final String userEncryptionKey;

    public CassandraNoteEntryDao(CassandraSessionManager sessionManager, String userId, String userEncKey) {
        System.out.println("Creating CassandraNoteEntryDao : " + userId + "-" + userEncKey);
        this.sessionManager = sessionManager;
        this.userId = userId;
        this.userEncryptionKey = userEncKey;
    }

    private static final String GET_ALL_NOTES_FOR_NOTEBOOK = "SELECT * FROM %s.%s WHERE notebook = ?";
    private static final String GET_ALL_NOTES_FOR_USER = "SELECT * FROM %s.%s";
    private static final String ADD_NOTE_ENTRY = "INSERT INTO %s.%s (notebook, noteid, key, value, info, passwordFlag, lastModifiedTime) VALUES (?,?,?,?,?,?,?)";
    private static final String EDIT_NOTE_ENTRY = "UPDATE %s.%s SET key = ?, value = ?, info = ?, passwordFlag = ?, lastModifiedTime = ? where notebook = ? and noteid = ?";
    private static final String DELETE_NOTE_ENTRIES = "DELETE FROM %s.%s WHERE notebook = ? and noteid IN ?";

    private static final String DELETE_NOTEBOOK = "DELETE FROM %s.%s WHERE notebook = ?";

    private static final String ADD_USERSCRET_VALIDATION_ROW = "INSERT INTO %s.%s (notebook, encrypted_validation_text) VALUES (?, ?)";
    private static final String GET_USERSCRET_VALIDATION_ROW = "SELECT * FROM %s.%s WHERE notebook = ?";

    public static final String VALIDATION_NOTEBOOK = "VALIDATION_NOTEBOOK";
    public static final String VALIDATION_TEXT = "CheckSecret123";

    private PreparedStatement preparedAddNoteEntry;

    /**
     * 1. Create userId table which will store the notes for this user. - a user's userId also acts as his password, without
     * knowing the userId no one can access/modify its content.<br>
     * 2. Store the secret-validation-text in an encrypted form - this is to ensure that user doesn't accidentally connect
     * using a wrong secret. Note: secret itself is not stored!<br>
     * 3. Create prepared statement for Add Note Entry. (TODO: create prepared statement for Edit and Delete as well)<br>
     * 
     * What can go wrong?<br>
     * if Step 1 fails -> no worries, user will not be connected, no table is created, so they can retry as new user<br>
     * if Step 2 fails -> table will be created but the validation row will not be present -> fixes itself in validateUserSecret<br>
     * 
     */
    @Override
    public boolean setupUser(String userId) {
        try {
            CreateTable createUserTable = createTable(sessionManager.getKeyspace(), userId).withPartitionKey("notebook", DataTypes.TEXT)
                    .withClusteringColumn("noteid", DataTypes.TEXT).withColumn("key", DataTypes.TEXT).withColumn("value", DataTypes.TEXT)
                    .withColumn("info", DataTypes.TEXT).withColumn("passwordFlag", DataTypes.TEXT)
                    .withColumn("lastModifiedTime", DataTypes.TIMESTAMP).withStaticColumn("encrypted_validation_text", DataTypes.TEXT);

            sessionManager.getClientSession().execute(createUserTable.build());
            System.out.println("Cassandra: User table created");
            
            insertValidationText();

        } catch (AlreadyExistsException alreadyExistsException) {
            System.err.println(userId + " already exists");
            return false;
        } catch (Exception ex) {
            ex.printStackTrace();
            System.err.println("User should to recreate new user!");
            // TODO : try to drop the table
            return false;
        }

        preparePreparedStatement();
        return true;
    }

    private void insertValidationText() {
        sessionManager.getClientSession()
                .execute(SimpleStatement.builder(String.format(ADD_USERSCRET_VALIDATION_ROW, sessionManager.getKeyspace(), userId))
                        .addPositionalValues(VALIDATION_NOTEBOOK, EncryptionUtil.encrypt(userEncryptionKey, VALIDATION_TEXT)).build());
        System.out.println("Cassandra: Validation Text added to User table");
    }

    private void preparePreparedStatement() {
        System.out.println("Cassandra: Preparing Insert PreparedStatement...");
        String addNoteEntryCQL = String.format(ADD_NOTE_ENTRY, sessionManager.getKeyspace(), userId);
        preparedAddNoteEntry = sessionManager.getClientSession().prepare(addNoteEntryCQL);
        System.out.println("Cassandra: Prepared the PreparedStatement.");
    }

    @Override
    public List<NoteEntry> getAll(String notebook) {

        String cqlStr = String.format(GET_ALL_NOTES_FOR_NOTEBOOK, sessionManager.getKeyspace(), userId);
        ResultSet results;
        try {
            results = sessionManager.getClientSession().execute(SimpleStatement.builder(cqlStr).addPositionalValues(notebook).build());
        } catch (InvalidQueryException ex) {
            System.err.println("Caught InvalidQueryException");
            return null;
        }
        List<Row> rows = results.all();
        if (rows != null && !rows.isEmpty()) {
            return rows.stream().filter(row -> isValidationNotebook(row)).map(row -> toNoteEntry(row)).collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    // Used for filtering out the Validation Row
    private boolean isValidationNotebook(Row row) {
        String notebook = row.getString("notebook");
        if (VALIDATION_NOTEBOOK.equals(notebook)) {
            return false;
        } else {
            return true;
        }
    }

    private NoteEntry toNoteEntry(Row row) {
        NoteEntry noteEntry = new NoteEntry(row.getString("noteid"), EncryptionUtil.decrypt(userEncryptionKey, row.getString("key")),
                EncryptionUtil.decrypt(userEncryptionKey, row.getString("value")),
                EncryptionUtil.decrypt(userEncryptionKey, row.getString("info")), row.getString("passwordFlag"),
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
        if(preparedAddNoteEntry == null) {//Paranoia - ideally the PreparedStatement must be prepared by this stage.
            preparePreparedStatement();
        }
        return preparedAddNoteEntry.bind(notebook, noteEntry.getId(), EncryptionUtil.encrypt(userEncryptionKey, noteEntry.getKey()),
                EncryptionUtil.encrypt(userEncryptionKey, noteEntry.getValue()),
                EncryptionUtil.encrypt(userEncryptionKey, noteEntry.getInfo()), noteEntry.getPasswordFlag(),
                noteEntry.getLastModifiedTime().toInstant(ZoneOffset.UTC));
    }

    @Override
    public long editNoteEntry(String notebook, NoteEntry noteEntry) {
        String cqlStr = String.format(EDIT_NOTE_ENTRY, sessionManager.getKeyspace(), userId);

        sessionManager.getClientSession()
                .execute(SimpleStatement.builder(cqlStr)
                        .addPositionalValues(EncryptionUtil.encrypt(userEncryptionKey, noteEntry.getKey()),
                                EncryptionUtil.encrypt(userEncryptionKey, noteEntry.getValue()),
                                EncryptionUtil.encrypt(userEncryptionKey, noteEntry.getInfo()), noteEntry.getPasswordFlag(),
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
        String cqlStr = String.format(DELETE_NOTE_ENTRIES, sessionManager.getKeyspace(), userId);
        sessionManager.getClientSession().execute(SimpleStatement.builder(cqlStr).addPositionalValues(notebook, noteIds).build());
    }

    @Override
    public void deleteNotebook(String notebookToBeDeleted) {
        String cqlStr = String.format(DELETE_NOTEBOOK, sessionManager.getKeyspace(), userId);
        sessionManager.getClientSession().execute(SimpleStatement.builder(cqlStr).addPositionalValues(notebookToBeDeleted).build());
    }

    @Override
    public boolean backup(String notebook, List<NoteEntry> notes) {
        Iterable<BatchableStatement<?>> statements = notes.stream()
                .map((noteEntry) -> getBoundStatementForAddNoteEntry(notebook, noteEntry)).collect(Collectors.toList());
        BatchStatement batch = new BatchStatementBuilder(BatchType.UNLOGGED).setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM)
                .addStatements(statements).build();
        sessionManager.getClientSession().execute(batch);
        return true;
    }

    @Override
    public Map<String, List<NoteEntry>> restore() {

        String cqlStr = String.format(GET_ALL_NOTES_FOR_USER, sessionManager.getKeyspace(), userId);
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
            rows.stream().filter(row -> isValidationNotebook(row)).map(row -> toNoteEntry(row)).forEach(
                    (noteEntry) -> notebookMap.computeIfAbsent(noteEntry.getNotebook(), (k) -> new ArrayList<NoteEntry>()).add(noteEntry));
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
        try {
            results = sessionManager.getClientSession()
                    .execute(SimpleStatement.builder(String.format(GET_USERSCRET_VALIDATION_ROW, sessionManager.getKeyspace(), userId))
                            .addPositionalValues(VALIDATION_NOTEBOOK).build());
        } catch (Exception ex) {
            System.err.println("Exception in validateUserSecret (can't do much here) : " + ex.getMessage());
            return 1;
        }
        Row row = results.one();
        if (row == null) { // Validation notebook is not found.
            insertValidationText(); // so trying to rectify the setupUser issue here by creating the Validation notebook row.
            return 0;
        } else {
            String encryptedValidationText = row.getString("encrypted_validation_text");
            if (VALIDATION_TEXT.equals(encryptedValidationText) && StringUtils.isNotBlank(userEncryptionKey)) {
                return 2;
            }
            if (VALIDATION_TEXT.equals(EncryptionUtil.decrypt(userEncryptionKey, encryptedValidationText))) { // Secret matches
                String addNoteEntryCQL = String.format(ADD_NOTE_ENTRY, sessionManager.getKeyspace(), userId);
                preparedAddNoteEntry = sessionManager.getClientSession().prepare(addNoteEntryCQL);
                return 0;
            } else { // Secret does not match
                return 2;
            }
        }
    }

}
