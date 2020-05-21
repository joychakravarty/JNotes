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
package com.jc.jnotes.dao.local.lucene;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.jc.jnotes.UserPreferences;
import com.jc.jnotes.dao.local.LocalNoteEntryDao;
import com.jc.jnotes.model.NoteEntry;

public class LuceneNoteEntryDaoTest {
    
    private static final String TEST_INDX_DIR = "JNotesTest";
    private LocalNoteEntryDao dao;
    
    private static UserPreferences userPreferences = new UserPreferences();
    
    @BeforeEach
    void clearIndexes() throws IOException {
        deleteIndexDirectory();
        dao = new LuceneNoteEntryDao(userPreferences.getBasePath(), TEST_INDX_DIR, userPreferences.getCurrentNotebook());
    }
    
    @AfterAll
    static void clearIndexesAfterTests() throws IOException {
        deleteIndexDirectory();
    }
    
    private static void deleteIndexDirectory() throws IOException {
        Path pathToBeDeleted = Paths.get(userPreferences.getBasePath(), TEST_INDX_DIR);
        FileUtils.deleteDirectory(pathToBeDeleted.toFile());
    }
    
    @Test
    void addEntryNoteTest() throws IOException {
        String id1 = UUID.randomUUID().toString();
        String id2 = UUID.randomUUID().toString();
        NoteEntry noteEntry1 = new NoteEntry(id1, "key1", "value1", "info1", "N");
        NoteEntry noteEntry2 = new NoteEntry(id2, "key2", "value2", "info2", "Y");
        dao.addNoteEntry(userPreferences.getCurrentNotebook(), noteEntry1);
        dao.addNoteEntry(userPreferences.getCurrentNotebook(), noteEntry2);
        
        dao.getAll(userPreferences.getCurrentNotebook()).stream().forEach((ne) -> System.out.println("addEntrNoteTest: "+ne));
        
        List<NoteEntry> noteEntries = dao.getAll(userPreferences.getCurrentNotebook());
        assertEquals(2, noteEntries.size());
        
        assertAll("noteEntries",
                () -> assertEquals(id2, noteEntries.get(0).getId()),
                () -> assertEquals("key2", noteEntries.get(0).getKey()),
                () -> assertEquals("value2", noteEntries.get(0).getValue()),
                () -> assertEquals("info1", noteEntries.get(1).getInfo()),
                () -> assertEquals("key1", noteEntries.get(1).getKey())
            );
    }
    
    @Test
    void deleteNoteEntryTest() throws IOException {
        String id1 = UUID.randomUUID().toString();
        String id2 = UUID.randomUUID().toString();
        NoteEntry noteEntry1 = new NoteEntry(id1, "key1", "value1", "info1", "N");
        NoteEntry noteEntry2 = new NoteEntry(id2, "key2", "value2", "info2", "Y");
        dao.addNoteEntry(userPreferences.getCurrentNotebook(), noteEntry1);
        dao.addNoteEntry(userPreferences.getCurrentNotebook(), noteEntry2);
        
        dao.getAll(userPreferences.getCurrentNotebook()).stream().forEach((ne) -> System.out.println("deleteNoteEntryTest before: "+ne));
        
        dao.deleteNoteEntry(userPreferences.getCurrentNotebook(), noteEntry1);
        
        dao.getAll(userPreferences.getCurrentNotebook()).stream().forEach((ne) -> System.out.println("deleteNoteEntryTest after: "+ne));
        
        List<NoteEntry> noteEntries = dao.getAll(userPreferences.getCurrentNotebook());
        assertEquals(1, noteEntries.size());
        
        assertAll("noteEntries",
                () -> assertEquals(id2, noteEntries.get(0).getId()),
                () -> assertEquals("key2", noteEntries.get(0).getKey()),
                () -> assertEquals("value2", noteEntries.get(0).getValue()),
                () -> assertEquals("info2", noteEntries.get(0).getInfo())
            );
    }
    
    @Test
    void editEntryNoteTest() throws IOException {
        String id1 = UUID.randomUUID().toString();
        String id2 = UUID.randomUUID().toString();
        NoteEntry noteEntry1 = new NoteEntry(id1, "key1", "value1", "info1", "N");
        NoteEntry noteEntry2 = new NoteEntry(id2, "key2", "value2", "info2", "Y");
        dao.addNoteEntry(userPreferences.getCurrentNotebook(), noteEntry1);
        dao.addNoteEntry(userPreferences.getCurrentNotebook(), noteEntry2);
        
        dao.getAll(userPreferences.getCurrentNotebook()).stream().forEach((ne) -> System.out.println("editEntryNoteTest before: "+ne));
        
        noteEntry2.setKey("key3");
        dao.editNoteEntry(userPreferences.getCurrentNotebook(), noteEntry2);
        
        dao.getAll(userPreferences.getCurrentNotebook()).stream().forEach((ne) -> System.out.println("editEntryNoteTest after: "+ne));
        
        List<NoteEntry> noteEntries = dao.getAll(userPreferences.getCurrentNotebook());
        assertEquals(2, noteEntries.size());
        
        assertAll("noteEntries",
                () -> assertEquals(id2, noteEntries.get(0).getId()),
                () -> assertEquals("key3", noteEntries.get(0).getKey()),/*Verify the edit*/
                () -> assertEquals("value2", noteEntries.get(0).getValue()),
                () -> assertEquals(id1, noteEntries.get(1).getId()),
                () -> assertEquals("info1", noteEntries.get(1).getInfo()),
                () -> assertEquals("key1", noteEntries.get(1).getKey())
            );
        
    }
    
    @Test
    void searchNotesTest() throws IOException {
        String id1 = UUID.randomUUID().toString();
        String id2 = UUID.randomUUID().toString();
        String id3 = UUID.randomUUID().toString();
        NoteEntry noteEntry1 = new NoteEntry(id1, "key1", "value1", "infoy3", "N");//has - e1, y3-info
        NoteEntry noteEntry2 = new NoteEntry(id2, "key2", "value2", "infoe1", "Y");//has - e1-info
        NoteEntry noteEntry3 = new NoteEntry(id3, "key3", "value3", "info3", "N");//has - y3
        dao.addNoteEntry(userPreferences.getCurrentNotebook(), noteEntry1);
        dao.addNoteEntry(userPreferences.getCurrentNotebook(), noteEntry2);
        dao.addNoteEntry(userPreferences.getCurrentNotebook(), noteEntry3);
        
        List<NoteEntry> noteEntries = dao.getAll(userPreferences.getCurrentNotebook());
        assertEquals(3, noteEntries.size());
        
        noteEntries = dao.searchNotes("e1", false);
        assertEquals(1, noteEntries.size());
        assertEquals("key1", noteEntries.get(0).getKey());
        
        noteEntries = dao.searchNotes("y3", true);
        assertEquals(2, noteEntries.size());
        assertEquals("key3", noteEntries.get(0).getKey());
        assertEquals("key1", noteEntries.get(1).getKey());
    }

}
