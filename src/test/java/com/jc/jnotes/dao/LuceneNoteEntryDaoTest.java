package com.jc.jnotes.dao;

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

import com.jc.jnotes.JNotesPreferences;
import com.jc.jnotes.model.NoteEntry;

public class LuceneNoteEntryDaoTest {
    
    private static final String TEST_INDX_DIR = "JNotesTest";
    private NoteEntryDao dao;
    
    @BeforeEach
    void clearIndexes() throws IOException {
        deleteIndexDirectory();
        dao = new LuceneNoteEntryDao(TEST_INDX_DIR);
    }
    
    @AfterAll
    static void clearIndexesAfterTests() throws IOException {
        deleteIndexDirectory();
    }
    
    private static void deleteIndexDirectory() throws IOException {
        Path pathToBeDeleted = Paths.get(JNotesPreferences.getBasePath(), TEST_INDX_DIR);
        FileUtils.deleteDirectory(pathToBeDeleted.toFile());
    }
    
    @Test
    void addEntryNoteTest() throws IOException {
        String id1 = UUID.randomUUID().toString();
        String id2 = UUID.randomUUID().toString();
        NoteEntry noteEntry1 = new NoteEntry(id1, "key1", "value1", "info1");
        NoteEntry noteEntry2 = new NoteEntry(id2, "key2", "value2", "info2");
        dao.addNoteEntry(noteEntry1);
        dao.addNoteEntry(noteEntry2);
        
        dao.getAll().stream().forEach((ne) -> System.out.println("addEntrNoteTest: "+ne));
        
        List<NoteEntry> noteEntries = dao.getAll();
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
        NoteEntry noteEntry1 = new NoteEntry(id1, "key1", "value1", "info1");
        NoteEntry noteEntry2 = new NoteEntry(id2, "key2", "value2", "info2");
        dao.addNoteEntry(noteEntry1);
        dao.addNoteEntry(noteEntry2);
        
        dao.getAll().stream().forEach((ne) -> System.out.println("deleteNoteEntryTest before: "+ne));
        
        dao.deleteNoteEntry(noteEntry1);
        
        dao.getAll().stream().forEach((ne) -> System.out.println("deleteNoteEntryTest after: "+ne));
        
        List<NoteEntry> noteEntries = dao.getAll();
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
        NoteEntry noteEntry1 = new NoteEntry(id1, "key1", "value1", "info1");
        NoteEntry noteEntry2 = new NoteEntry(id2, "key2", "value2", "info2");
        dao.addNoteEntry(noteEntry1);
        dao.addNoteEntry(noteEntry2);
        
        dao.getAll().stream().forEach((ne) -> System.out.println("editEntryNoteTest before: "+ne));
        
        noteEntry2.setKey("key3");
        dao.editNoteEntry(noteEntry2);
        
        dao.getAll().stream().forEach((ne) -> System.out.println("editEntryNoteTest after: "+ne));
        
        List<NoteEntry> noteEntries = dao.getAll();
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
        NoteEntry noteEntry1 = new NoteEntry(id1, "key1", "value1", "infoy3");//has - e1, y3-info
        NoteEntry noteEntry2 = new NoteEntry(id2, "key2", "value2", "infoe1");//has - e1-info
        NoteEntry noteEntry3 = new NoteEntry(id3, "key3", "value3", "info3");//has - y3
        dao.addNoteEntry(noteEntry1);
        dao.addNoteEntry(noteEntry2);
        dao.addNoteEntry(noteEntry3);
        
        List<NoteEntry> noteEntries = dao.getAll();
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
