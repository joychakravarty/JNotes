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

import static com.jc.jnotes.JNotesConstants.DATETIME_DISPLAY_FORMAT;
import static com.jc.jnotes.model.NoteEntry.ID_COL_NAME;
import static com.jc.jnotes.model.NoteEntry.INFO_COL_NAME;
import static com.jc.jnotes.model.NoteEntry.KEY_COL_NAME;
import static com.jc.jnotes.model.NoteEntry.LAST_MODIFIED_TIME_COL_NAME;
import static com.jc.jnotes.model.NoteEntry.PASSWORD_FLAG_COL_NAME;
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

import com.jc.jnotes.dao.DaoRuntimeException;
import com.jc.jnotes.dao.local.LocalNoteEntryDao;
import com.jc.jnotes.model.NoteEntry;

/**
 * 
 * This is a Lucene based implementation of Local store of Notes
 * 
 * @author Joy C
 *
 */
public class LuceneNoteEntryDao implements LocalNoteEntryDao {

    private final Directory indexDir;
    private final Query getAllQuery = new MatchAllDocsQuery();
    private final StandardAnalyzer analyzer = new StandardAnalyzer();
    private final String notebook;
    private static final int DAO_TYPE = DaoRuntimeException.LOCAL;
    // private final MultiFieldQueryParser multiFieldQueryParser = new MultiFieldQueryParser(new String[]{"key", "value",
    // "info"}, analyzer);

    public LuceneNoteEntryDao(String basePath, String pathAppender, String notebook) throws IOException {
        System.out.println("Creating LuceneNoteEntryDao : notebook :" + notebook);
        this.notebook = notebook;
        Path indexPath = Paths.get(basePath, pathAppender, notebook);
        File file = indexPath.toFile();
        if (!file.exists()) {
            file.mkdirs();
        }
        indexDir = FSDirectory.open(indexPath);
    }

    @Override
    public List<NoteEntry> getAll(String notebook) {
        List<NoteEntry> noteEntries;
        IndexReader indexReader;
        try {
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
        } catch (Exception ex) {
            throw new DaoRuntimeException(DAO_TYPE, ex);
        }
        return noteEntries;
    }

    @Override
    public void addNoteEntry(NoteEntry noteEntry) {
        try {
            IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
            IndexWriter writer = new IndexWriter(indexDir, indexWriterConfig);
            Document document = fromNoteEntry(noteEntry);
            writer.addDocument(document);
            writer.commit();
            writer.close();
        } catch (Exception ex) {
            throw new DaoRuntimeException(DAO_TYPE, ex);
        }
    }

    @Override
    public void editNoteEntry(NoteEntry noteEntry) {
        try {
            IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
            IndexWriter writer = new IndexWriter(indexDir, indexWriterConfig);
            Document document = fromNoteEntry(noteEntry);
            Term idTerm = new Term(ID_COL_NAME, noteEntry.getId());
            writer.updateDocument(idTerm, document);
            writer.commit();
            writer.close();
        } catch (Exception ex) {
            throw new DaoRuntimeException(DAO_TYPE, ex);
        }
    }

    @Override
    public void deleteNoteEntry(NoteEntry noteEntry) {
        try {
            IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
            IndexWriter writer = new IndexWriter(indexDir, indexWriterConfig);
            writer.deleteDocuments(new Term(ID_COL_NAME, noteEntry.getId()));
            writer.flush();
            // writer.forceMergeDeletes();
            writer.commit();
            writer.close();
        } catch (Exception ex) {
            throw new DaoRuntimeException(DAO_TYPE, ex);
        }
    }

    @Override
    public void deleteNoteEntries(List<NoteEntry> noteEntries) {
        if (noteEntries == null) {
            throw new IllegalArgumentException("deleteNoteEntries: Cannot pass null as argument.");
        }
        try {
            IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
            IndexWriter writer = new IndexWriter(indexDir, indexWriterConfig);
            // final int noteEntriesToBeDeleted = noteEntries.size();
            // Term[] terms = new Term[noteEntriesToBeDeleted];
            // for (int i=0; i<noteEntriesToBeDeleted; i++) {
            // Term term = new Term(ID_COL_NAME, noteEntries.get(i).getId());
            // terms[i] = term;
            // }
            Term[] terms = noteEntries.stream().map((noteEntry) -> {
                Term term = new Term(ID_COL_NAME, noteEntry.getId());
                return term;
            }).toArray(Term[]::new);

            writer.deleteDocuments(terms);
            writer.flush();
            // writer.forceMergeDeletes();
            writer.commit();
            writer.close();
        } catch (Exception ex) {
            throw new DaoRuntimeException(DAO_TYPE, ex);
        }
    }

    @Override
    public List<NoteEntry> searchNotes(String searchParam, boolean searchInfo) {
        List<NoteEntry> noteEntries = new ArrayList<>();
        try {
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
            noteEntries.addAll(searchedEntries);
            indexReader.close();
        } catch (Exception ex) {
            throw new DaoRuntimeException(DAO_TYPE, ex);
        }
        return noteEntries;

    }

    protected List<NoteEntry> getNoteEntries(TopDocs topDocs, IndexSearcher searcher) throws IOException {
        List<NoteEntry> noteEntries = new ArrayList<>();
        ScoreDoc[] sDocs = topDocs.scoreDocs;
        for (ScoreDoc scoreDoc : sDocs) {
            Document dd = searcher.doc(scoreDoc.doc);
            NoteEntry noteEntry = new NoteEntry(notebook, dd.get(ID_COL_NAME), dd.get(KEY_COL_NAME), dd.get(VALUE_COL_NAME),
                    dd.get(INFO_COL_NAME), dd.get(PASSWORD_FLAG_COL_NAME));
            noteEntry.setLastModifiedTime(LocalDateTime.parse(dd.get(LAST_MODIFIED_TIME_COL_NAME), DATETIME_DISPLAY_FORMAT));
            noteEntries.add(noteEntry);
        }
        return noteEntries;
    }

    private Document fromNoteEntry(NoteEntry noteEntry) {
        Document document = new Document();
        document.add(new StringField(ID_COL_NAME, noteEntry.getId(), Field.Store.YES));// id is not to be tokenized
        document.add(new StringField(KEY_COL_NAME, noteEntry.getKey(), Field.Store.YES));
        document.add(new TextField(VALUE_COL_NAME, noteEntry.getValue(), Field.Store.YES));
        document.add(new TextField(INFO_COL_NAME, noteEntry.getInfo(), Field.Store.YES));
        document.add(new TextField(PASSWORD_FLAG_COL_NAME, noteEntry.getPasswordFlag(), Field.Store.YES));
        document.add(new StringField(LAST_MODIFIED_TIME_COL_NAME, noteEntry.getLastModifiedTime().format(DATETIME_DISPLAY_FORMAT),
                Field.Store.YES));
        return document;

    }

}