package com.jc.jnotes.dao;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import com.jc.jnotes.JNotesPreferences;
import com.jc.jnotes.model.NoteEntry;

/**
 * 
 * @author Joy C
 *
 */
public class LuceneNoteEntryDao implements NoteEntryDao {
    
    private final Directory indexDir;
    private IndexSearcher searcher = null;
    private final Query getAllQuery = new MatchAllDocsQuery();
    private final StandardAnalyzer analyzer = new StandardAnalyzer();
    MultiFieldQueryParser multiFieldQueryParser = new MultiFieldQueryParser(new String[]{"key", "value", "info"}, analyzer);
    
    public LuceneNoteEntryDao() throws IOException {
        Path indexPath = Paths.get(JNotesPreferences.getBasePath(),"JNotes", JNotesPreferences.getCurrentProfile()); 
        indexDir = FSDirectory.open(indexPath);
    }
    
    private void initializeSearcher() throws IOException{
        if(searcher == null) {
            IndexReader indexReader = DirectoryReader.open(indexDir);
            searcher = new IndexSearcher(indexReader);
        }
    }

    @Override
    public List<NoteEntry> getAll() throws IOException {
        initializeSearcher();
        TopDocs topDocs = searcher.search(getAllQuery, 10000);
        return getNoteEntries(topDocs);
    }
    
    @Override
    public long addNoteEntry(NoteEntry noteEntry) throws IOException {
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
        IndexWriter writer = new IndexWriter(indexDir, indexWriterConfig);
        Document document = new Document();
        document.add(new TextField("id", noteEntry.getId(), Field.Store.YES));
        document.add(new TextField("key", noteEntry.getKey(), Field.Store.YES));
        document.add(new TextField("value", noteEntry.getValue(), Field.Store.YES));
        document.add(new TextField("info", noteEntry.getInfo(), Field.Store.YES));
         
        long docId = writer.addDocument(document);
        writer.close();
        return docId;
    }
    
    @Override
    public long editNoteEntry(NoteEntry noteEntry) throws IOException {
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
        IndexWriter writer = new IndexWriter(indexDir, indexWriterConfig);
        Document document = new Document();
        document.add(new TextField("id", noteEntry.getId(), Field.Store.YES));
        document.add(new TextField("key", noteEntry.getKey(), Field.Store.YES));
        document.add(new TextField("value", noteEntry.getValue(), Field.Store.YES));
        document.add(new TextField("info", noteEntry.getInfo(), Field.Store.YES));
         
        long docId = writer.updateDocument(new Term("id", noteEntry.getId()), document);
        writer.commit();
        writer.close();
        return docId;
    }
    
    @Override
    public void deleteNoteEntry(NoteEntry noteEntry) throws IOException {
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
        IndexWriter writer = new IndexWriter(indexDir, indexWriterConfig);
        writer.deleteDocuments(new Term("id", noteEntry.getId()));
        writer.forceMergeDeletes();
        writer.commit();
        writer.close();
    }

    
    @Override
    public List<NoteEntry> searchNotes(String searchParam, boolean searchInfo) throws IOException {
        initializeSearcher();
        Set<NoteEntry> searchedEntries = new LinkedHashSet<>(); 
        
        Term keyTerm = new Term("key", "*"+searchParam+"*");
        Query keyQuery = new WildcardQuery(keyTerm);
        TopDocs keyTopDocs = searcher.search(keyQuery, 10000);
        searchedEntries.addAll(getNoteEntries(keyTopDocs));
        
        Term valueTerm = new Term("value", "*"+searchParam+"*");
        Query valueQuery = new WildcardQuery(valueTerm);
        TopDocs valueTopDocs = searcher.search(valueQuery, 10000);
        searchedEntries.addAll(getNoteEntries(valueTopDocs));
        
        if(searchInfo) {
            Term infoTerm = new Term("info", "*"+searchParam+"*");
            Query infoQuery = new WildcardQuery(infoTerm);
            TopDocs infoTopDocs = searcher.search(infoQuery, 10000);
            searchedEntries.addAll(getNoteEntries(infoTopDocs));
        }
        
        List<NoteEntry> noteEntries = new ArrayList<>();
        noteEntries.addAll(searchedEntries);
        return noteEntries;
        
    }
    
    protected List<NoteEntry> getNoteEntries(TopDocs topDocs) throws IOException {
        List<NoteEntry> noteEntries = new ArrayList<>();
        ScoreDoc[] sDocs = topDocs.scoreDocs;
        for (ScoreDoc scoreDoc : sDocs) {
            Document dd = searcher.doc(scoreDoc.doc);
            NoteEntry noteEntry = new NoteEntry(dd.get("id"), dd.get("key"), dd.get("value"), dd.get("info"));
            System.out.println(noteEntry);
            noteEntries.add(noteEntry);
        }
        return noteEntries;
        
    }

}