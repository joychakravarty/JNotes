package com.jc.jnotes.helper;

import static com.jc.jnotes.JNotesPreferences.DEFAULT_APP_NAME;
import static com.jc.jnotes.JNotesPreferences.DEFAULT_EXPORT_FILE_SUFFIX;
import static com.jc.jnotes.JNotesPreferences.DEFAULT_FIELD_SEPARATOR;
import static com.jc.jnotes.JNotesPreferences.DEFAULT_LINE_SEPARATOR;
import static com.jc.jnotes.JNotesPreferences.getBasePath;
import static com.jc.jnotes.JNotesPreferences.getCurrentProfile;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;

import com.jc.jnotes.model.NoteEntry;

import javafx.collections.ObservableList;

/**
 * 
 * @author Joy C
 *
 */
public class ImportExportHelper {

    public Path getExportFilePath() {
        return Paths.get(getBasePath(), DEFAULT_APP_NAME, getCurrentProfile() + DEFAULT_EXPORT_FILE_SUFFIX);
    }

    public boolean exportProfile(final ObservableList<NoteEntry> observableNoteEntryList) {
        boolean exportStatus = true;
        final Path exportPath = getExportFilePath();
        final File csvOutputFile = exportPath.toFile();
        FileUtils.deleteQuietly(csvOutputFile); // If file already exists delete it
        try {
            observableNoteEntryList.stream()
                    .map((noteEntry) -> String.join(DEFAULT_FIELD_SEPARATOR, escapeSpecialCharacters(noteEntry.getKey()),
                            escapeSpecialCharacters(noteEntry.getValue()), escapeSpecialCharacters(noteEntry.getInfo())))
                    .forEach((str) -> {
                        try {
                            FileUtils.writeStringToFile(csvOutputFile, str + DEFAULT_LINE_SEPARATOR, Charset.defaultCharset(), true);
                        } catch (IOException e) {
                            e.printStackTrace();
                            throw new RuntimeException(e);
                        }
                    });
        } catch (RuntimeException re) {
            exportStatus = false;
        }
        return exportStatus;
    }

    private String escapeSpecialCharacters(String str) {
        if (str.contains(DEFAULT_FIELD_SEPARATOR) || str.contains("\"") || str.contains("'") || str.contains(DEFAULT_LINE_SEPARATOR)) {
            str = str.replace("\"", "\"\"");
            str = "\"" + str + "\"";
        }
        return str;
    }

    /**
     * 
     * @param importFile
     * @return null -> invalid fileType selected
     * 
     */
    public List<NoteEntry> importProfile(File importFile) {
        System.out.println(importFile.getPath());
        List<NoteEntry> noteEntries;
        Path filePath = importFile.toPath();
        String fileName = filePath.getFileName().toString();
        System.out.println(fileName);
        if (fileName.endsWith(".csv")) {
            noteEntries = csvToNoteEntries(importFile);
        } else if (fileName.endsWith(".properties")) {

            noteEntries = propertiesToNoteEntries(importFile);
        } else {
            noteEntries = null;
        }
        return noteEntries;
    }

    private List<NoteEntry> propertiesToNoteEntries(File importFile) {
        List<NoteEntry> noteEntries = new ArrayList<>();
        try {
            Properties properties = new Properties();
            properties.load(new FileReader(importFile));
            noteEntries = properties.entrySet().stream()
                    .map(entry -> new NoteEntry(NoteEntry.generateID(), entry.getKey() == null ? "" : entry.getKey().toString(),
                            entry.getValue() == null ? "" : entry.getValue().toString(), ""))
                    .collect(Collectors.toList());
        } catch (IOException ex) {
            ex.printStackTrace();
            noteEntries = null;
        }
        return noteEntries;
    }

    private List<NoteEntry> csvToNoteEntries(File file) {
        List<NoteEntry> noteEntries = new ArrayList<>();
        try {
            Reader in = new FileReader(file);

            Iterable<CSVRecord> records = CSVFormat.DEFAULT.parse(in);
            for (CSVRecord record : records) {
                String key = record.get(0);
                String value = record.get(1);
                String info = record.get(2);
                NoteEntry noteEntry = new NoteEntry(NoteEntry.generateID(), key, value, info);
                noteEntries.add(noteEntry);

            }
        } catch (IOException ex) {
            ex.printStackTrace();
            noteEntries = null;
        }
        return noteEntries;

    }
}
