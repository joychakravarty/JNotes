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
package com.jc.jnotes.helper;

import static com.jc.jnotes.JNotesConstants.APP_NAME;
import static com.jc.jnotes.JNotesConstants.DATETIME_EXPORT_FORMAT;
import static com.jc.jnotes.JNotesConstants.DEFAULT_NOTEBOOK;
import static com.jc.jnotes.JNotesConstants.EXPORT_FILE_SUFFIX;
import static com.jc.jnotes.JNotesConstants.FIELD_SEPARATOR;
import static com.jc.jnotes.JNotesConstants.LINE_SEPARATOR;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;

import com.jc.jnotes.UserPreferences;
import com.jc.jnotes.model.NoteEntry;

import javafx.collections.ObservableList;

/**
 * 
 * Does file related operations
 * 
 * @author Joy C
 *
 */
public class IOHelper {

    private UserPreferences userPreferences;

    public IOHelper(UserPreferences userPreferences) {
        this.userPreferences = userPreferences;
    }

    /**
     * 
     * @param observableNoteEntryList
     * @return String value of the Path of the exported file. null if export failed.
     */
    public String exportNotebook(final ObservableList<NoteEntry> observableNoteEntryList) {
        final Path exportPath = getExportFilePath();
        String exportPathStr = exportPath.toString();
        final File csvOutputFile = exportPath.toFile();
        FileUtils.deleteQuietly(csvOutputFile); // If file already exists delete it
        try {
            observableNoteEntryList.stream().map((noteEntry) -> String.join(FIELD_SEPARATOR, escapeSpecialCharacters(noteEntry.getKey()),
                    escapeSpecialCharacters(noteEntry.getValue()), escapeSpecialCharacters(noteEntry.getInfo()), noteEntry.getPasswordFlag())).forEach((str) -> {
                        try {
                            FileUtils.writeStringToFile(csvOutputFile, str + LINE_SEPARATOR, StandardCharsets.UTF_8, true);
                        } catch (IOException e) {
                            e.printStackTrace();
                            throw new RuntimeException(e);
                        }
                    });
        } catch (RuntimeException re) {
            exportPathStr = null;
        }
        return exportPathStr;
    }

    private Path getExportFilePath() {
        return Paths.get(userPreferences.getBasePath(), APP_NAME,
                userPreferences.getCurrentNotebook() + "_" + LocalDateTime.now().format(DATETIME_EXPORT_FORMAT) + EXPORT_FILE_SUFFIX);
    }

    private String escapeSpecialCharacters(String str) {
        if (str.contains(FIELD_SEPARATOR) || str.contains("\"") || str.contains("'") || str.contains(LINE_SEPARATOR)) {
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
    public List<NoteEntry> importNotebook(File importFile) {
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
                    .map(entry -> new NoteEntry(userPreferences.getCurrentNotebook(), NoteEntry.generateID(),
                            entry.getKey() == null ? "" : entry.getKey().toString(),
                            entry.getValue() == null ? "" : entry.getValue().toString(), "", "N"))
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
                String passwordFlag;
                if(record.size()>3) {
                    passwordFlag = record.get(3);
                }else {
                    passwordFlag = "N";
                }
                NoteEntry noteEntry = new NoteEntry(userPreferences.getCurrentNotebook(), NoteEntry.generateID(), key, value, info,
                        passwordFlag);
                noteEntries.add(noteEntry);

            }
        } catch (IOException ex) {
            ex.printStackTrace();
            noteEntries = null;
        }
        return noteEntries;

    }

    public void moveNotebook(String notebookToBeRenamed, String newNotebookName) throws IOException {
        Path source = Paths.get(userPreferences.getBasePath(), APP_NAME, notebookToBeRenamed);
        Files.move(source, source.resolveSibling(newNotebookName));
    }

    public void deleteNotebook(String notebookToBeDeleted) throws IOException {
        Path pathToBeDeleted = Paths.get(userPreferences.getBasePath(), APP_NAME, notebookToBeDeleted);
        FileUtils.deleteDirectory(pathToBeDeleted.toFile());
    }

    public void addNotebook(String newNotebookName) {
        Paths.get(userPreferences.getBasePath(), APP_NAME, newNotebookName);
    }

    public List<String> getAllNotebooks() {
        Path directory = Paths.get(userPreferences.getBasePath(), APP_NAME);
        List<String> directories;
        try {
            directories = Files.walk(directory).filter(Files::isDirectory).map((path) -> path.getFileName()).map(Path::toString)
                    .filter((name) -> !name.equals(APP_NAME)).collect(Collectors.toList());
        } catch (IOException e) {
            directories = new ArrayList<>();
            directories.add(DEFAULT_NOTEBOOK);
        }
        return directories;
    }

    public File getBaseDirectory() {
        return Paths.get(userPreferences.getBasePath()).toFile();
    }
}
