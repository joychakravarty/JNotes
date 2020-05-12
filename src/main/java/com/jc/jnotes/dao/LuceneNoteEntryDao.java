package com.jc.jnotes.dao;

import static com.jc.jnotes.JNotesConstants.APP_NAME;
import static com.jc.jnotes.JNotesConstants.DATETIME_DISPLAY_FORMAT;
import static com.jc.jnotes.JNotesPreferences.getBasePath;
import static com.jc.jnotes.JNotesPreferences.getCurrentNoteBook;
import static com.jc.jnotes.model.NoteEntry.ID_COL_NAME;
import static com.jc.jnotes.model.NoteEntry.INFO_COL_NAME;
import static com.jc.jnotes.model.NoteEntry.KEY_COL_NAME;
import static com.jc.jnotes.model.NoteEntry.LAST_MODIFIED_TIME_COL_NAME;
import static com.jc.jnotes.model.NoteEntry.VALUE_COL_NAME;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexNotFoundException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.SortField.Type;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import com.jc.jnotes.model.NoteEntry;

/**
 * 
 * This is a Lucene based implemen
 * 
 * @author Joy C
 *
 */
public class LuceneNoteEntryDao implements LocalNoteEntryDao {

    private final Directory indexDir;
    private final Query getAllQuery = new MatchAllDocsQuery();
    private final StandardAnalyzer analyzer = new StandardAnalyzer();
    // private final MultiFieldQueryParser multiFieldQueryParser = new MultiFieldQueryParser(new String[]{"key", "value",
    // "info"}, analyzer);

    public LuceneNoteEntryDao() throws IOException {
        this(APP_NAME);
    }

    public LuceneNoteEntryDao(String pathAppender) throws IOException {
        Path indexPath = Paths.get(getBasePath(), pathAppender, getCurrentNoteBook());
        File file = indexPath.toFile();
        if (!file.exists()) {
            file.mkdirs();
        }
        indexDir = FSDirectory.open(indexPath);
    }

    @Override
    public List<NoteEntry> getAll() throws IOException {
        List<NoteEntry> noteEntries;
        IndexReader indexReader;
        try {
            indexReader = DirectoryReader.open(indexDir);
        } catch (IndexNotFoundException ex) {
            return Collections.emptyList();
        }
        try {
            IndexSearcher searcher = new IndexSearcher(indexReader);
            Sort sort = new Sort(new SortField[] { new SortField(null, Type.DOC, true) });// Sort based on order of last modified
            TopDocs topDocs = searcher.search(getAllQuery, 10000, sort);
            noteEntries = getNoteEntries(topDocs, searcher);
        } finally {
            indexReader.close();
        }
        return noteEntries;
    }

    @Override
    public long addNoteEntry(NoteEntry noteEntry) throws IOException {
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
        IndexWriter writer = new IndexWriter(indexDir, indexWriterConfig);
        Document document = fromNoteEntry(noteEntry);
        long seqNo = writer.addDocument(document);
        writer.commit();
        writer.close();
        return seqNo;
    }

    @Override
    public long editNoteEntry(NoteEntry noteEntry) throws IOException {
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
        IndexWriter writer = new IndexWriter(indexDir, indexWriterConfig);
        Document document = fromNoteEntry(noteEntry);
        Term idTerm = new Term(ID_COL_NAME, noteEntry.getId());
        long seqNo = writer.updateDocument(idTerm, document);
        writer.commit();
        writer.close();
        return seqNo;
    }

    @Override
    public void deleteNoteEntry(NoteEntry noteEntry) throws IOException {
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
        IndexWriter writer = new IndexWriter(indexDir, indexWriterConfig);
        writer.deleteDocuments(new Term(ID_COL_NAME, noteEntry.getId()));
        writer.flush();
        // writer.forceMergeDeletes();
        writer.commit();
        writer.close();
    }
    
    @Override
    public void deleteNoteEntries(List<NoteEntry> noteEntries) throws IOException {
        if(noteEntries==null) {
            throw new IllegalArgumentException("deleteNoteEntries: Cannot pass null as argument.");
        }
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
        IndexWriter writer = new IndexWriter(indexDir, indexWriterConfig);
//        final int noteEntriesToBeDeleted = noteEntries.size();
//        Term[] terms = new Term[noteEntriesToBeDeleted];
//        for (int i=0; i<noteEntriesToBeDeleted; i++) {
//          Term term = new Term(ID_COL_NAME, noteEntries.get(i).getId());    
//          terms[i] = term;
//        }
        Term[] terms = noteEntries.stream().map((noteEntry)->{
            Term term = new Term(ID_COL_NAME, noteEntry.getId());
            return term;
            }).toArray(Term[]::new);
                
        
        writer.deleteDocuments(terms);
        writer.flush();
        // writer.forceMergeDeletes();
        writer.commit();
        writer.close();
    }

    @Override
    public List<NoteEntry> searchNotes(String searchParam, boolean searchInfo) throws IOException {
        searchParam = searchParam.toLowerCase();
        IndexReader indexReader = DirectoryReader.open(indexDir);
        IndexSearcher searcher = new IndexSearcher(indexReader);
        Set<NoteEntry> searchedEntries = new LinkedHashSet<>();

        Term keyTerm = new Term(KEY_COL_NAME, "*" + searchParam + "*");
        Query keyQuery = new WildcardQuery(keyTerm);
        TopDocs keyTopDocs = searcher.search(keyQuery, 10000);
        searchedEntries.addAll(getNoteEntries(keyTopDocs, searcher));

        Term valueTerm = new Term(VALUE_COL_NAME, "*" + searchParam + "*");
        Query valueQuery = new WildcardQuery(valueTerm);
        TopDocs valueTopDocs = searcher.search(valueQuery, 10000);
        searchedEntries.addAll(getNoteEntries(valueTopDocs, searcher));

        if (searchInfo) {
            Term infoTerm = new Term(INFO_COL_NAME, "*" + searchParam + "*");
            Query infoQuery = new WildcardQuery(infoTerm);
            TopDocs infoTopDocs = searcher.search(infoQuery, 10000);
            searchedEntries.addAll(getNoteEntries(infoTopDocs, searcher));
        }

        List<NoteEntry> noteEntries = new ArrayList<>();
        noteEntries.addAll(searchedEntries);
        indexReader.close();
        return noteEntries;

    }

    protected List<NoteEntry> getNoteEntries(TopDocs topDocs, IndexSearcher searcher) throws IOException {
        List<NoteEntry> noteEntries = new ArrayList<>();
        ScoreDoc[] sDocs = topDocs.scoreDocs;
        for (ScoreDoc scoreDoc : sDocs) {
            Document dd = searcher.doc(scoreDoc.doc);
            NoteEntry noteEntry = new NoteEntry(dd.get(ID_COL_NAME), dd.get(KEY_COL_NAME), dd.get(VALUE_COL_NAME), dd.get(INFO_COL_NAME));
            noteEntry.setLastModifiedTime(LocalDateTime.parse(dd.get(LAST_MODIFIED_TIME_COL_NAME), DATETIME_DISPLAY_FORMAT));
            noteEntries.add(noteEntry);
        }
        return noteEntries;
    }

    private Document fromNoteEntry(NoteEntry noteEntry) {
        Document document = new Document();
        document.add(new StringField(ID_COL_NAME, noteEntry.getId(), Field.Store.YES));// id is not to be tokenized
        document.add(new TextField(KEY_COL_NAME, noteEntry.getKey(), Field.Store.YES));
        document.add(new TextField(VALUE_COL_NAME, noteEntry.getValue(), Field.Store.YES));
        document.add(new TextField(INFO_COL_NAME, noteEntry.getInfo(), Field.Store.YES));
        document.add(new StringField(LAST_MODIFIED_TIME_COL_NAME, noteEntry.getLastModifiedTime().format(DATETIME_DISPLAY_FORMAT),
                Field.Store.YES));
        return document;

    }

}