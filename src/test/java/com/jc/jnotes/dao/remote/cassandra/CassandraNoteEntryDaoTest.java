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

import static com.jc.jnotes.AppConfig.APP_CONFIG;
import static com.jc.jnotes.dao.remote.cassandra.CassandraNoteEntryDao.VALIDATION_NOTEBOOK;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;

import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.jc.jnotes.AppConfig;
import com.jc.jnotes.dao.remote.RemoteNoteEntryDao;
import com.jc.jnotes.model.NoteEntry;

/**
 * 
 * @author Joy C
 *
 */
@TestInstance(Lifecycle.PER_CLASS)
@TestMethodOrder(OrderAnnotation.class)
public class CassandraNoteEntryDaoTest {

    private static final String TEST_USER_ID = "jnotes_testuser";
    private static final String TEST_USER_SECRET = "jnotes_testsecret";

    private CassandraSessionManager sessionManager = AppConfig.APP_CONFIG.getCassandraSessionManager();
    private RemoteNoteEntryDao cassandraNoteEntryDao = APP_CONFIG.getRemoteNoteEntryDao(TEST_USER_ID, TEST_USER_SECRET);

    @BeforeAll
    public void beforeAll() {
        sessionManager.getClientSession().execute(SimpleStatement.builder("DROP TABLE IF EXISTS jnotes_testuser").build());
    }

    @Test
    @Order(1)
    public void testSetupUser_NewUser() {
        boolean returnStatus = cassandraNoteEntryDao.setupUser(TEST_USER_ID);

        assertTrue(returnStatus, "Setup new user should have been successful");

        ResultSet results = sessionManager.getClientSession().execute(SimpleStatement.builder("SELECT * from " + TEST_USER_ID).build());

        assertNotNull(results, TEST_USER_ID + " table should have been created");
        Row row = results.one();
        String notebook = row.getString("notebook");
        assertEquals(VALIDATION_NOTEBOOK, notebook);

        int status = cassandraNoteEntryDao.validateUserSecret();
        assertEquals(0, status, "Secret should have matched");
    }

    @Test
    @Order(2)
    public void testSetupUser_ExistingUser() {
        boolean returnStatus = APP_CONFIG.getRemoteNoteEntryDao(TEST_USER_ID, TEST_USER_SECRET).setupUser(TEST_USER_ID);
        assertFalse(returnStatus, "Setup new user should return false for existing user");
    }

    @Test
    @Order(3)
    public void testAddNoteEntry() throws IOException {
        String testNotebook = "nb1";
        NoteEntry noteEntry = new NoteEntry("ididid", "kkk", "vvv", "iii", "N", LocalDateTime.now());
        cassandraNoteEntryDao.addNoteEntry(testNotebook, noteEntry);

        List<NoteEntry> notes = cassandraNoteEntryDao.getAll(testNotebook);

        assertEquals(1, notes.size());
        assertEquals("kkk", notes.get(0).getKey());
    }

    @Test
    @Order(4)
    public void testEditNoteEntry() throws IOException {
        String testNotebook = "nb1";
        NoteEntry noteEntry = new NoteEntry("ididid", "kkk222", "vvv", "iii", "N", LocalDateTime.now());
        cassandraNoteEntryDao.editNoteEntry(testNotebook, noteEntry);

        List<NoteEntry> notes = cassandraNoteEntryDao.getAll(testNotebook);

        assertEquals(1, notes.size());
        assertEquals("kkk222", notes.get(0).getKey());
    }

    @Test
    @Order(4)
    public void testBackup() throws IOException {
        String testNotebook1 = "nbX";
        String testNotebook2 = "nbY";
        NoteEntry noteEntry1 = new NoteEntry("id1", "kkk111", "vvv", "iii", "N", LocalDateTime.now());
        NoteEntry noteEntry2 = new NoteEntry("id2", "kkk222", "vvv", "iii", "N", LocalDateTime.now());
        List<NoteEntry> list1 = new ArrayList<>();
        list1.add(noteEntry1);
        list1.add(noteEntry2);
        NoteEntry noteEntry3 = new NoteEntry("id3", "kkk333", "vvv", "iii", "N", LocalDateTime.now());
        List<NoteEntry> list2 = new ArrayList<>();
        list2.add(noteEntry3);

        cassandraNoteEntryDao.backup(testNotebook1, list1);
        cassandraNoteEntryDao.backup(testNotebook2, list2);

        List<NoteEntry> notes = cassandraNoteEntryDao.getAll(testNotebook1);
        assertEquals(2, notes.size());

        notes = cassandraNoteEntryDao.getAll(testNotebook2);
        assertEquals(1, notes.size());
        assertEquals("kkk333", notes.get(0).getKey());
    }

    @Test
    @Order(5)
    public void testRestore() throws IOException {
        Map<String, List<NoteEntry>> noteEntries = cassandraNoteEntryDao.restore();
        assertNull(noteEntries.get(VALIDATION_NOTEBOOK));
        assertEquals(noteEntries.get("nbX").size(), 2);
        assertEquals(noteEntries.get("nbY").size(), 1);
    }

}
