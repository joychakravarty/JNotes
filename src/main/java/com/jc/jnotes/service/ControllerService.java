package com.jc.jnotes.service;

import static com.jc.jnotes.AppConfig.APP_CONFIG;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.jc.jnotes.UserPreferences;
import com.jc.jnotes.dao.local.LocalNoteEntryDao;
import com.jc.jnotes.dao.remote.RemoteNoteEntryDao;
import com.jc.jnotes.helper.IOHelper;
import com.jc.jnotes.model.NoteEntry;

/**
 * 
 * ViewControllers interact with DAO layer via this class.
 * 
 * @author Joy C
 *
 */
public class ControllerService {

    private final UserPreferences userPreferences;

    private final BiConsumer<String, String> localDaoInvalidator;
    private final BiConsumer<String, String> remoteDaoInvalidator;

    private final IOHelper ioHelper;

    public ControllerService(UserPreferences userPreferences, BiConsumer<String, String> localDaoInvalidator,
            BiConsumer<String, String> remoteDaoInvalidator, IOHelper ioHelper) {
        this.userPreferences = userPreferences;
        this.localDaoInvalidator = localDaoInvalidator;
        this.remoteDaoInvalidator = remoteDaoInvalidator;
        this.ioHelper = ioHelper;
    }

    public LocalNoteEntryDao getLocalNoteEntryDao(String notebook) {
        return APP_CONFIG.getLocalNoteEntryDao(userPreferences.getBasePath(), notebook);
    }

    public RemoteNoteEntryDao getRemoteNoteEntryDao(String userId, String userSecret) {
        return APP_CONFIG.getRemoteNoteEntryDao(userId, userSecret);
    }

    public void invalidateLocalDao(String notebook) {
        localDaoInvalidator.accept(userPreferences.getBasePath(), notebook);
    }

    public void invalidateRemoteDao(String userId, String userSecret) {
        remoteDaoInvalidator.accept(userId, userSecret);
    }

    public void addNoteEntry(NoteEntry noteEntry) throws ControllerServiceException {
        try {
            this.getLocalNoteEntryDao(userPreferences.getCurrentNotebook()).addNoteEntry(noteEntry);
            if (userPreferences.isConnected()) {
                this.getRemoteNoteEntryDao(userPreferences.getUserId(), userPreferences.getUserSecret()).addNoteEntry(noteEntry);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ControllerServiceException("Exception while creating new Note", ex);
        }
    }

    public void editNoteEntry(NoteEntry noteEntry) throws ControllerServiceException {
        try {
            this.getLocalNoteEntryDao(userPreferences.getCurrentNotebook()).editNoteEntry(noteEntry);
            if (userPreferences.isConnected()) {
                this.getRemoteNoteEntryDao(userPreferences.getUserId(), userPreferences.getUserSecret()).editNoteEntry(noteEntry);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ControllerServiceException("Exception while updating Note", ex);
        }
    }

    public List<NoteEntry> searchNotes(String searchTxt, boolean searchInfoAlso) {
        List<NoteEntry> notes;
        try {
            notes = this.getLocalNoteEntryDao(userPreferences.getCurrentNotebook()).searchNotes(searchTxt, searchInfoAlso);
        } catch (Exception ex) {
            ex.printStackTrace();
            notes = Collections.emptyList();
        }
        return notes;
    }

    public List<NoteEntry> getAll() {
        List<NoteEntry> allNoteEntries;
        try {
            allNoteEntries = this.getLocalNoteEntryDao(userPreferences.getCurrentNotebook()).getAll(userPreferences.getCurrentNotebook());
        } catch (Exception ex) {
            allNoteEntries = Collections.emptyList();
            System.err.println("Exception in loadAllNoteEntries. " + ex.getMessage());
        }
        return allNoteEntries;
    }

    public void deleteNoteEntries(List<NoteEntry> noteEntriesToBeDeleted) throws ControllerServiceException {
        try {
            this.getLocalNoteEntryDao(userPreferences.getCurrentNotebook()).deleteNoteEntries(noteEntriesToBeDeleted);
            if (userPreferences.isConnected()) {
                this.getRemoteNoteEntryDao(userPreferences.getUserId(), userPreferences.getUserSecret())
                        .deleteNoteEntries(noteEntriesToBeDeleted);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ControllerServiceException("Exception while trying to connect", ex);
        }
    }

    // Do not use UserPreference inside this method as it will be updated after the connection is made/verified.
    public String connect(boolean isNewUser, String userId, String userSecret) throws ControllerServiceException {
        System.out.println("Connecting to Cloud Datastore..");
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
                } else if (outcome == 3) {
                    returnString = "Failed to connect";
                } else {
                    System.out.println("Connected successfully!");
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ControllerServiceException("Exception while trying to connect", ex);
        }
        if(returnString!=null) {
            System.err.println(returnString);
        }
        return returnString;
    }

    public void disconnect() {
        String userId = userPreferences.getUserId();
        String userSecret = userPreferences.getUserSecret();
        this.invalidateRemoteDao(userId, userSecret);
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
            List<String> notebooks = ioHelper.getAllNotebooks();
            long weightageOfEachNotebook = 100L / notebooks.size();
            long progress = 0;
            for (String notebook : notebooks) {
                RemoteNoteEntryDao remoteDao = this.getRemoteNoteEntryDao(userPreferences.getUserId(), userPreferences.getUserSecret());
                List<NoteEntry> notes = this.getLocalNoteEntryDao(notebook).getAll(notebook);
                remoteDao.deleteNotebook(notebook);
                remoteDao.backup(notes);
                progress += weightageOfEachNotebook;
                progressConsumer.accept(progress);
            }
        } catch (Exception ex) {
            throw new ControllerServiceException("Failed to backup", ex);
        }
    }

    /**
     * As this overwrites all local noteEntries with the cloud noteEntries, we export the local notebooks first. 1. fetches
     * all remote notes<br>
     * 2. associates progress weight to each notebook<br>
     * 3. gets all local noteEntries for each notebook<br>
     * 4. Upserts all noteEntries for each notebook into local store<br>
     * 
     * @param progressConsumer
     *            - callback
     * @throws ControllerServiceException
     */
    public void restore(Consumer<Long> progressConsumer) throws ControllerServiceException {
        try {
            Map<String, List<NoteEntry>> notebookMap = this
                    .getRemoteNoteEntryDao(userPreferences.getUserId(), userPreferences.getUserSecret()).restore();
            long weightageOfEachNotebook = 100L / notebookMap.size();

            Set<String> notebookNames = notebookMap.keySet();
            long progress = 0;
            for (String notebook : notebookNames) {
                List<NoteEntry> remoteNotes = notebookMap.get(notebook);
                LocalNoteEntryDao localDao = this.getLocalNoteEntryDao(notebook);
                List<NoteEntry> localNotes = localDao.getAll(notebook);
                HashSet<String> lookupSetOfLocalNoteEntries = localNotes.stream().map((noteEntry) -> noteEntry.getId())
                        .collect(Collectors.toCollection(HashSet::new));
                for (NoteEntry remoteNote : remoteNotes) {
                    if (lookupSetOfLocalNoteEntries.contains(remoteNote.getId())) {
                        localDao.editNoteEntry(remoteNote);
                    } else {
                        localDao.addNoteEntry(remoteNote);
                    }
                }
                progress += weightageOfEachNotebook;
                progressConsumer.accept(progress);
            }
        } catch (Exception ex) {
            throw new ControllerServiceException("Failed to restore", ex);
        }
    }

    public void deleteNotebook(String notebookToBeDeleted) throws ControllerServiceException {
        try {
            ioHelper.deleteNotebook(notebookToBeDeleted);
            this.invalidateLocalDao(notebookToBeDeleted);
            if (userPreferences.isConnected()) {
                this.getRemoteNoteEntryDao(userPreferences.getUserId(), userPreferences.getUserSecret())
                        .deleteNotebook(notebookToBeDeleted);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ControllerServiceException("Failed to delete notebook", ex);
        }
    }

    public void renameNotebook(String notebookToBeRenamed, String newNotebookName) throws ControllerServiceException {
        try {
            ioHelper.moveNotebook(notebookToBeRenamed, newNotebookName);
            this.invalidateLocalDao(notebookToBeRenamed);
            if (userPreferences.isConnected()) {
                this.getRemoteNoteEntryDao(userPreferences.getUserId(), userPreferences.getUserSecret()).renameNotebook(notebookToBeRenamed,
                        newNotebookName);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ControllerServiceException("Failed to rename notebook", ex);
        }
    }

    public void addNotebook(String newNotebookName) throws ControllerServiceException {
        try {
            ioHelper.addNotebook(newNotebookName);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ControllerServiceException("Failed to add notebook", ex);
        }

    }

    public void moveNotes(List<NoteEntry> noteEntriesToBeMoved, String selectedNotebook, String destinationNotebook)
            throws ControllerServiceException {
        try {
            LocalNoteEntryDao localDestinationDao = this.getLocalNoteEntryDao(destinationNotebook);
            for (NoteEntry noteEntry : noteEntriesToBeMoved) {
                localDestinationDao.addNoteEntry(noteEntry);
            }
            LocalNoteEntryDao localSourceDao = this.getLocalNoteEntryDao(selectedNotebook);
            localSourceDao.deleteNoteEntries(noteEntriesToBeMoved);
            if (userPreferences.isConnected()) {
                RemoteNoteEntryDao remoteDao = this.getRemoteNoteEntryDao(userPreferences.getUserId(), userPreferences.getUserSecret());

                List<NoteEntry> destinationNotes = this.getLocalNoteEntryDao(destinationNotebook).getAll(destinationNotebook);
                remoteDao.backup(destinationNotes);

                List<NoteEntry> sourceNotes = this.getLocalNoteEntryDao(selectedNotebook).getAll(selectedNotebook);
                remoteDao.backup(sourceNotes);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ControllerServiceException("Failed to move notes", ex);
        }

    }

}
