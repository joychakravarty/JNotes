package com.jc.jnotes.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import org.apache.lucene.index.IndexNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.jc.jnotes.UserPreferences;
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
                List<NoteEntry> notes = this.getRemoteNoteEntryDao(userId, userSecret).getAll("DUMMY");
                if (notes == null) {
                    returnString = "UserId does not exist";
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

    public void backup(Consumer<Long> progressConsumer) throws ControllerServiceException {
        try {
            // Find All Notebooks
            List<String> notebooks = ioHelper.getAllNoteBooks();
            long weightageOfEachNotebook = 100L / notebooks.size();
            for (String notebook : notebooks) {
                List<NoteEntry> notes = this.getLocalNoteEntryDao(notebook).getAll(notebook);
                this.getRemoteNoteEntryDao(userPreferences.getUserId(), userPreferences.getUserSecret()).backup(notebook, notes);
                progressConsumer.accept(weightageOfEachNotebook);
            }
        } catch (Exception ex) {
            throw new ControllerServiceException("Failed to backup", ex);
        }
    }

}
