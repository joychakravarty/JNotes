package com.jc.jnotes.dao.remote;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jc.jnotes.JNotesConstants;
import com.jc.jnotes.dao.DaoRuntimeException;
import com.jc.jnotes.model.NewUserRequest;
import com.jc.jnotes.model.NoteEntry;
import com.jc.jnotes.model.NotebookRequest;
import com.jc.jnotes.model.Notes;

public class DefaultRemoteNoteEntryDao implements RemoteNoteEntryDao {

    private final String userId;
    private final String userSecret;
    private final HttpHeaders httpHeaders;
    private final RestTemplate restTemplate = new RestTemplate();

    private static final int DAO_TYPE = DaoRuntimeException.REMOTE;

    public DefaultRemoteNoteEntryDao(String userId, String userSecret) {
        this.userId = userId;
        this.userSecret = userSecret;
        this.httpHeaders = createHeaders(userId, userSecret);
        
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.registerModule(new JavaTimeModule());
        //objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        MappingJackson2HttpMessageConverter messageConverter = new MappingJackson2HttpMessageConverter();
        messageConverter.setObjectMapper(objectMapper);
       // messageConverter.setSupportedMediaTypes(Collections.singletonList(MediaTypes.HAL_JSON));

        this.restTemplate.setMessageConverters(Collections.singletonList(messageConverter));
    }

    private HttpHeaders createHeaders(String userId, String userSecret) {
        return new HttpHeaders() {
            private static final long serialVersionUID = 1L;
            {
                String auth = userId + ":" + userSecret;
                byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.UTF_8));
                String authHeader = "JNOTES " + new String(encodedAuth);
                set("Authorization", authHeader);
            }
        };
    }

    @Override
    public List<NoteEntry> getAll(String notebook) {
        List<NoteEntry> noteEntries;
        try {
            ResponseEntity<Notes> response = restTemplate.exchange(JNotesConstants.REMOTE_URL + "/getUserNotes?notebook=" + notebook,
                    HttpMethod.GET, new HttpEntity<>(httpHeaders), Notes.class);
            Notes notes = response.getBody();
            if (notes != null) {
                noteEntries = notes.getNoteEntries();
            } else {
                noteEntries = Collections.emptyList();
            }
        } catch (Exception ex) {
            throw new DaoRuntimeException(DAO_TYPE, "Failed to getAll for notebook:" + notebook, ex);
        }
        return noteEntries;
    }

    @Override
    public void addNoteEntry(NoteEntry noteEntry) {
        try {
            restTemplate.exchange(JNotesConstants.REMOTE_URL + "/addNote", HttpMethod.POST,
                    new HttpEntity<NoteEntry>(noteEntry, httpHeaders), String.class);
        } catch (Exception ex) {
            throw new DaoRuntimeException(DAO_TYPE, "Failed to add note", ex);
        }
    }

    @Override
    public void editNoteEntry(NoteEntry noteEntry) {
        try {
            restTemplate.exchange(JNotesConstants.REMOTE_URL + "/editNote", HttpMethod.POST,
                    new HttpEntity<NoteEntry>(noteEntry, httpHeaders), String.class);
        } catch (Exception ex) {
            throw new DaoRuntimeException(DAO_TYPE, "Failed to edit note", ex);
        }
    }

    @Override
    public void deleteNoteEntry(NoteEntry noteEntry) {
        List<NoteEntry> noteEntries = new ArrayList<>();
        noteEntries.add(noteEntry);
        this.deleteNoteEntries(noteEntries);
    }

    @Override
    public void deleteNoteEntries(List<NoteEntry> noteEntries) {
        Notes notes = new Notes(noteEntries);
        try {
            restTemplate.exchange(JNotesConstants.REMOTE_URL + "/deleteNotes", HttpMethod.POST, new HttpEntity<Notes>(notes, httpHeaders),
                    String.class);

        } catch (Exception ex) {
            throw new DaoRuntimeException(DAO_TYPE, "Failed to delete notes", ex);
        }

    }

    @Override
    public boolean setupUser(String userId) {
        NewUserRequest newUserRequest = new NewUserRequest(userId, this.userSecret);
        try {
            restTemplate.exchange(JNotesConstants.REMOTE_URL + "/setupUser", HttpMethod.POST,
                    new HttpEntity<NewUserRequest>(newUserRequest), String.class);
            return true;
        } catch (HttpClientErrorException ex) {
            if (HttpStatus.CONFLICT.equals(ex.getStatusCode())) {
                return false;
            } else {
                throw new DaoRuntimeException(DAO_TYPE, "Failed to setup user: " + userId, ex);
            }
        } catch (Exception ex) {
            throw new DaoRuntimeException(DAO_TYPE, "Failed to setup user: " + userId, ex);
        }
    }

    @Override
    public void backup(List<NoteEntry> noteEntries) {
        
        Notes notes = new Notes(noteEntries);
        try {
            restTemplate.exchange(JNotesConstants.REMOTE_URL + "/backupNotes", HttpMethod.POST, new HttpEntity<Notes>(notes, httpHeaders),
                    String.class);
        } catch (Exception ex) {
            throw new DaoRuntimeException(DAO_TYPE, "Failed to backup", ex);
        }
    }

    @Override
    public void deleteNotebook(String notebookToBeDeleted) {
        NotebookRequest notebookRequest = new NotebookRequest(notebookToBeDeleted);
        try {
            restTemplate.exchange(JNotesConstants.REMOTE_URL + "/deleteNotebook", HttpMethod.POST,
                    new HttpEntity<NotebookRequest>(notebookRequest, httpHeaders), String.class);
        } catch (Exception ex) {
            throw new DaoRuntimeException(DAO_TYPE, "Failed to delete book:" + notebookToBeDeleted, ex);
        }
    }

    @Override
    public void renameNotebook(String notebookToBeRenamed, String notebookNewName) {
        NotebookRequest notebookRequest = new NotebookRequest(notebookToBeRenamed, notebookNewName);
        try {
            restTemplate.exchange(JNotesConstants.REMOTE_URL + "/renameNotebook", HttpMethod.POST,
                    new HttpEntity<NotebookRequest>(notebookRequest, httpHeaders), String.class);
        } catch (Exception ex) {
            throw new DaoRuntimeException(DAO_TYPE, "Failed to rename book:" + notebookToBeRenamed, ex);
        }
    }

    @Override
    public int validateUserSecret() {
        try {
            restTemplate.exchange(JNotesConstants.REMOTE_URL + "/authenticateUser", HttpMethod.GET, new HttpEntity<>(httpHeaders),
                    String.class);
            return 0;
        } catch (HttpClientErrorException ex) {
            if (HttpStatus.NOT_FOUND.equals(ex.getStatusCode())) {
                return 1;
            } else if (HttpStatus.UNAUTHORIZED.equals(ex.getStatusCode())) {
                return 2;
            } else {
                throw new DaoRuntimeException(DAO_TYPE, "Failed to validateUserSecret : " + userId, ex);
            }
        } catch (Exception ex) {
            throw new DaoRuntimeException(DAO_TYPE, "Failed to validateUserSecret : " + userId, ex);
        }
    }

    @Override
    public Map<String, List<NoteEntry>> restore() {
        Map<String, List<NoteEntry>> notebookMap = new HashMap<>();
        try {
            ResponseEntity<Notes> response = restTemplate.exchange(JNotesConstants.REMOTE_URL + "/getUserNotes", HttpMethod.GET,
                    new HttpEntity<>(httpHeaders), Notes.class);
            if (HttpStatus.OK.equals(response.getStatusCode())) {
                Notes notes = response.getBody();
                if (notes != null && notes.getNoteEntries() != null && !notes.getNoteEntries().isEmpty()) {
                    notes.getNoteEntries().stream().forEach((noteEntry) -> notebookMap
                            .computeIfAbsent(noteEntry.getNotebook(), (k) -> new ArrayList<NoteEntry>()).add(noteEntry));
                }
            }
        } catch (Exception ex) {
            throw new DaoRuntimeException(DAO_TYPE, "Failed to restore", ex);
        }
        return notebookMap;
    }

}
