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
import java.nio.charset.Charset;
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
import org.springframework.beans.factory.annotation.Autowired;

import com.jc.jnotes.UserPreferences;
import com.jc.jnotes.model.NoteEntry;

import javafx.collections.ObservableList;

/**
 * 
 * @author Joy C
 *
 */
public class IOHelper {

    @Autowired
    private UserPreferences userPreferences;

    /**
     * 
     * @param observableNoteEntryList
     * @return String value of the Path of the exported file. null if export failed.
     */
    public String exportNoteBook(final ObservableList<NoteEntry> observableNoteEntryList) {
        final Path exportPath = getExportFilePath();
        String exportPathStr = exportPath.toString();
        final File csvOutputFile = exportPath.toFile();
        FileUtils.deleteQuietly(csvOutputFile); // If file already exists delete it
        try {
            observableNoteEntryList.stream().map((noteEntry) -> String.join(FIELD_SEPARATOR, escapeSpecialCharacters(noteEntry.getKey()),
                    escapeSpecialCharacters(noteEntry.getValue()), escapeSpecialCharacters(noteEntry.getInfo()))).forEach((str) -> {
                        try {
                            FileUtils.writeStringToFile(csvOutputFile, str + LINE_SEPARATOR, Charset.defaultCharset(), true);
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
                userPreferences.getCurrentNoteBook() + "_" + LocalDateTime.now().format(DATETIME_EXPORT_FORMAT) + EXPORT_FILE_SUFFIX);
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
    public List<NoteEntry> importNoteBook(File importFile) {
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

    public void moveNoteBook(String noteBookToBeRenamed, String newNoteBookName) throws IOException {
        Path source = Paths.get(userPreferences.getBasePath(), APP_NAME, noteBookToBeRenamed);
        Files.move(source, source.resolveSibling(newNoteBookName));
    }

    public void deleteNoteBook(String noteBookToBeDeleted) throws IOException {
        Path pathToBeDeleted = Paths.get(userPreferences.getBasePath(), APP_NAME, noteBookToBeDeleted);
        FileUtils.deleteDirectory(pathToBeDeleted.toFile());
    }

    public void addNoteBook(String newNoteBookName) {
        Paths.get(userPreferences.getBasePath(), APP_NAME, newNoteBookName);
    }

    public List<String> getAllNoteBooks() {
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
