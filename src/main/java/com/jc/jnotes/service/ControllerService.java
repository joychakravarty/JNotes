package com.jc.jnotes.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import org.apache.lucene.index.IndexNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jc.jnotes.UserPreferences;
import com.jc.jnotes.dao.local.LocalNoteEntryDao;
import com.jc.jnotes.dao.remote.RemoteInstallerDao;
import com.jc.jnotes.dao.remote.RemoteNoteEntryDao;
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
    private UserPreferences userPrefernces;
    
    @Autowired
    private RemoteInstallerDao remoteInstallerDao;

    @Autowired
    private Function<UserPreferences, LocalNoteEntryDao> localNoteEntryDaoFactory;
     
    public LocalNoteEntryDao getLocalNoteEntryDao() {
        return localNoteEntryDaoFactory.apply(userPrefernces);
    }
    
    @Autowired
    private Function<UserPreferences, RemoteNoteEntryDao> remoteNoteEntryDaoFactory;
     
    public RemoteNoteEntryDao getRemoteNoteEntryDao() {
        return remoteNoteEntryDaoFactory.apply(userPrefernces);
    }

    public void addNoteEntry(NoteEntry noteEntry) throws ControllerServiceException {
        try {
            this.getLocalNoteEntryDao().addNoteEntry(userPrefernces.getCurrentNoteBook(), noteEntry);
            if (userPrefernces.isConnected()) {
                this.getRemoteNoteEntryDao().addNoteEntry(userPrefernces.getCurrentNoteBook(), noteEntry);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ControllerServiceException("Exception while creating new Note", ex);
        }
    }

    public void editNoteEntry(NoteEntry noteEntry) throws ControllerServiceException {
        try {
            this.getLocalNoteEntryDao().editNoteEntry(userPrefernces.getCurrentNoteBook(), noteEntry);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ControllerServiceException("Exception while updating Note", ex);
        }
    }

    public List<NoteEntry> searchNotes(String searchTxt, boolean searchInfoAlso) {
        List<NoteEntry> notes;
        try {
            notes = this.getLocalNoteEntryDao().searchNotes(searchTxt, searchInfoAlso);
        } catch (Exception ex) {
            ex.printStackTrace();
            notes = Collections.emptyList();
        }
        return notes;
    }

    public List<NoteEntry> getAll() throws ControllerServiceException {
        List<NoteEntry> allNoteEntries;
        try {
            allNoteEntries = this.getLocalNoteEntryDao().getAll(userPrefernces.getCurrentNoteBook());
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
        this.getLocalNoteEntryDao().deleteNoteEntries(userPrefernces.getCurrentNoteBook(), noteEntriesToBeDeleted);
    }

    public String connect(String userId, String userSecret) throws ControllerServiceException {
        String returnString = null;
        try {
            boolean status = remoteInstallerDao.installUserOnline(userId);
            if (!status) {
                returnString = "UserId already exists";
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ControllerServiceException("Exception while trying to connect", ex);
        }
        return returnString;
    }

    public void disconnect() {
        this.getRemoteNoteEntryDao().closeConnection();
    }

}
