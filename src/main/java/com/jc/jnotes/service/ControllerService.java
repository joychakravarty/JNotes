package com.jc.jnotes.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.lucene.index.IndexNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.jc.jnotes.UserPreferences;
import com.jc.jnotes.dao.DaoConfig;
import com.jc.jnotes.dao.local.LocalNoteEntryDao;
import com.jc.jnotes.dao.remote.RemoteNoteEntryDao;
import com.jc.jnotes.helper.IOHelper;
import com.jc.jnotes.model.NoteEntry;

/**
 * 
 * Controllers interact with DAO layer via this class.
 * 
 * @author Joy C
 *
 */
@Component
public class ControllerService {

    @Autowired
    private UserPreferences userPreferences;

    @Autowired
    @Qualifier("localNoteEntryDaoFactory")
    private BiFunction<String, String, LocalNoteEntryDao> localNoteEntryDaoFactory;

    @Autowired
    private IOHelper ioHelper;

    public LocalNoteEntryDao getLocalNoteEntryDao(String notebook) {
        return localNoteEntryDaoFactory.apply(userPreferences.getBasePath(), notebook);
    }

    @Autowired
    @Qualifier("remoteNoteEntryDaoFactory")
    private BiFunction<String, String, RemoteNoteEntryDao> remoteNoteEntryDaoFactory;

    public RemoteNoteEntryDao getRemoteNoteEntryDao(String userId, String userSecret) {
        return remoteNoteEntryDaoFactory.apply(userId, userSecret);
    }

    public void addNoteEntry(NoteEntry noteEntry) throws ControllerServiceException {
        try {
            this.getLocalNoteEntryDao(userPreferences.getCurrentNoteBook()).addNoteEntry(userPreferences.getCurrentNoteBook(), noteEntry);
            if (userPreferences.isConnected()) {
                this.getRemoteNoteEntryDao(userPreferences.getUserId(), userPreferences.getUserSecret())
                        .addNoteEntry(userPreferences.getCurrentNoteBook(), noteEntry);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ControllerServiceException("Exception while creating new Note", ex);
        }
    }

    public void editNoteEntry(NoteEntry noteEntry) throws ControllerServiceException {
        try {
            this.getLocalNoteEntryDao(userPreferences.getCurrentNoteBook()).editNoteEntry(userPreferences.getCurrentNoteBook(), noteEntry);
            if (userPreferences.isConnected()) {
                this.getRemoteNoteEntryDao(userPreferences.getUserId(), userPreferences.getUserSecret())
                        .editNoteEntry(userPreferences.getCurrentNoteBook(), noteEntry);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ControllerServiceException("Exception while updating Note", ex);
        }
    }

    public List<NoteEntry> searchNotes(String searchTxt, boolean searchInfoAlso) {
        List<NoteEntry> notes;
        try {
            notes = this.getLocalNoteEntryDao(userPreferences.getCurrentNoteBook()).searchNotes(searchTxt, searchInfoAlso);
        } catch (Exception ex) {
            ex.printStackTrace();
            notes = Collections.emptyList();
        }
        return notes;
    }

    public List<NoteEntry> getAll() throws ControllerServiceException {
        List<NoteEntry> allNoteEntries;
        try {
            allNoteEntries = this.getLocalNoteEntryDao(userPreferences.getCurrentNoteBook()).getAll(userPreferences.getCurrentNoteBook());
        } catch (IndexNotFoundException indexNotFoundEx) {
            allNoteEntries = new ArrayList<>();
            System.err.println("Exception in loadAllNoteEntries - IndexNotfound. " + indexNotFoundEx.getMessage());
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ControllerServiceException("Exception while getting all Notes", ex);
        }
        return allNoteEntries;
    }

    public void deleteNoteEntries(List<NoteEntry> noteEntriesToBeDeleted) throws IOException {
        this.getLocalNoteEntryDao(userPreferences.getCurrentNoteBook()).deleteNoteEntries(userPreferences.getCurrentNoteBook(),
                noteEntriesToBeDeleted);
        if (userPreferences.isConnected()) {
            this.getRemoteNoteEntryDao(userPreferences.getUserId(), userPreferences.getUserSecret())
                    .deleteNoteEntries(userPreferences.getCurrentNoteBook(), noteEntriesToBeDeleted);
        }
    }

    // Do not use UserPreference inside this method as it will be updated after the connection is made/verified.
    public String connect(boolean isNewUser, String userId, String userSecret) throws ControllerServiceException {
        System.out.println("Connecting to Cloud Datastore!");
        String returnString = null;
        try {
            if (isNewUser) {
                boolean status = this.getRemoteNoteEntryDao(userId, userSecret).setupUser(userId);
                if (!status) {
                    returnString = "UserId already exists";
                }

            } else {
                int outcome = this.getRemoteNoteEntryDao(userId, userSecret).validateUserSecret();
                if (outcome == 1) {
                    returnString = "UserId does not exist";
                } else if (outcome == 2) {
                    returnString = "Secret could not decrypt your data correctly. Please enter correct secret.";
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ControllerServiceException("Exception while trying to connect", ex);
        }
        return returnString;
    }

    public void disconnect() {
        this.getRemoteNoteEntryDao(userPreferences.getUserId(), userPreferences.getUserSecret()).disconnect();
    }

    /**
     * 1. fetches all local notebook<br>
     * 2. associates progress weight to each notebook<br>
     * 3. calls remote DAO to backup notebook and notifies caller of the progress<br>
     * 
     * @param progressConsumer
     *            - callback
     * @throws ControllerServiceException
     */
    public void backup(Consumer<Long> progressConsumer) throws ControllerServiceException {
        try {
            List<String> notebooks = ioHelper.getAllNoteBooks();
            long weightageOfEachNotebook = 100L / notebooks.size();
            long progress = 0;
            for (String notebook : notebooks) {
                List<NoteEntry> notes = this.getLocalNoteEntryDao(notebook).getAll(notebook);
                this.getRemoteNoteEntryDao(userPreferences.getUserId(), userPreferences.getUserSecret()).backup(notebook, notes);
                progress += weightageOfEachNotebook;
                progressConsumer.accept(progress);
            }
        } catch (Exception ex) {
            throw new ControllerServiceException("Failed to backup", ex);
        }
    }

    /**
     * As this overwrites all local noteEntries with the cloud noteEntries, we export the local notebooks first.
     * 1. fetches all remote notes<br>
     * 2. associates progress weight to each notebook<br>
     * 3. gets all local noteEntries for each notebook<br> 
     * 4. Upserts all noteEntries for each notebook into local store<br> 
     * 
     * @param progressConsumer - callback
     * @throws ControllerServiceException
     */
    public void restore(Consumer<Long> progressConsumer) throws ControllerServiceException {
        try {
            Map<String, List<NoteEntry>> notebookMap = this.getRemoteNoteEntryDao(userPreferences.getUserId(), userPreferences.getUserSecret()).restore();
            long weightageOfEachNotebook = 100L / notebookMap.size();
            
            Set<String> notebookNames = notebookMap.keySet();
            long progress = 0;
            for (String notebook : notebookNames) {
                List<NoteEntry> remoteNotes = notebookMap.get(notebook);
                LocalNoteEntryDao localDao = this.getLocalNoteEntryDao(notebook);
                List<NoteEntry> localNotes = localDao.getAll(notebook);
                HashSet<String> lookupSetOfLocalNoteEntries = localNotes.stream().map((noteEntry)->noteEntry.getId()).collect(Collectors.toCollection(HashSet::new));
                for (NoteEntry remoteNote : remoteNotes) {
                    if(lookupSetOfLocalNoteEntries.contains(remoteNote.getId())) {
                        localDao.editNoteEntry(notebook, remoteNote);
                    } else {
                        localDao.addNoteEntry(notebook, remoteNote);
                    }
                }
                progress += weightageOfEachNotebook;
                progressConsumer.accept(progress);
            }
        } catch (Exception ex) {
            throw new ControllerServiceException("Failed to restore", ex);
        }
    }

    public void clearCachedNotebook(String notebbok) {
        DaoConfig.localDaoMap.remove(userPreferences.getBasePath()+"-"+notebbok);
    }

}
