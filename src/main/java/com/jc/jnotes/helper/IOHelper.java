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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.jc.jnotes.UserPreferences;
import com.jc.jnotes.model.NoteEntry;
import javafx.collections.ObservableList;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import static com.jc.jnotes.JNotesConstants.*;

/**
 * Does file related operations
 *
 * @author Joy C
 */
public class IOHelper {

    private UserPreferences userPreferences;

    public IOHelper(UserPreferences userPreferences) {
        this.userPreferences = userPreferences;
    }

    private final static String JSON_KEY = "key";
    private final static String JSON_VALUE = "value";
    private final static String JSON_INFO = "info";
    private final static String JSON_PWD_FLAG = "pwd_flag";

    private final static String JSON_ROOT = "jnotes";

    /**
     * @param observableNoteEntryList
     * @return String value of the Path of the exported file. null if export failed.
     */
    public String exportNotebook(final ObservableList<NoteEntry> observableNoteEntryList) {
        final Path exportPath = getExportFilePath();
        String exportPathStr = exportPath.toString();
        try (PrintWriter exportFile = new PrintWriter(exportPath.toString(), "UTF-8")) {
            ObjectWriter ow = getObjectMapper().writer().withDefaultPrettyPrinter();
            String json = ow.writeValueAsString(observableNoteEntryList);
            exportFile.println(json);
        } catch (Exception ex) {
            ex.printStackTrace();
            exportPathStr = null;
        }
        return exportPathStr;
    }

    private Path getExportFilePath() {
        return Paths.get(userPreferences.getBasePath(), APP_NAME,
                userPreferences.getCurrentNotebook() + "_" + LocalDateTime.now().format(DATETIME_EXPORT_FORMAT) + EXPORT_FILE_SUFFIX);
    }

    /**
     * @param importFile
     * @return null -> invalid fileType selected
     */
    public List<NoteEntry> importNotebook(File importFile) {
        List<NoteEntry> noteEntries = null;
        try {
            System.out.println(importFile.getPath());
            Path filePath = importFile.toPath();
            String fileName = filePath.getFileName().toString();
            System.out.println(fileName);
            if (fileName.endsWith(EXTENSION_JSON)) {
                noteEntries = jsonToNoteEntries(importFile);
            } else if (fileName.endsWith(EXTENSION_PROPERTIES)) {
                noteEntries = propertiesToNoteEntries(importFile);
            } else {
                System.err.println("Invalid file extension. Filename " + fileName);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return noteEntries;
    }

    protected List<NoteEntry> propertiesToNoteEntries(File importFile) throws IOException {
        List<NoteEntry> noteEntries = new ArrayList<>();
        try (FileReader fileReader = new FileReader(importFile)) {
            Properties properties = new Properties();
            properties.load(fileReader);
            noteEntries = properties.entrySet().stream()
                    .map(entry -> new NoteEntry(userPreferences.getCurrentNotebook(), NoteEntry.generateID(),
                            entry.getKey() == null ? "" : entry.getKey().toString(),
                            entry.getValue() == null ? "" : entry.getValue().toString(), "", "N"))
                    .collect(Collectors.toList());
        }
        return noteEntries;
    }

    protected ObjectMapper getObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();

        JavaTimeModule module = new JavaTimeModule();
        LocalDateTimeDeserializer localDateTimeDeserializer = new LocalDateTimeDeserializer(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        module.addDeserializer(LocalDateTime.class, localDateTimeDeserializer);

        objectMapper.registerModule(module);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper;
    }

    protected List<NoteEntry> jsonToNoteEntries(File file) throws IOException {

        String importedJson = Files.readString(file.toPath(), StandardCharsets.UTF_8);
        ObjectMapper objectMapper = getObjectMapper();
        List<NoteEntry> noteEntries = objectMapper.readValue(importedJson, new TypeReference<List<NoteEntry>>() {
        });
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
