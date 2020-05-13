package com.jc.jnotes.dao;

import static com.jc.jnotes.JNotesConstants.APP_NAME;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jc.jnotes.UserPreferences;
import com.jc.jnotes.dao.local.LocalNoteEntryDao;
import com.jc.jnotes.dao.local.LuceneNoteEntryDao;
import com.jc.jnotes.dao.remote.CassandraInstallerDao;
import com.jc.jnotes.dao.remote.CassandraNoteEntryDao;
import com.jc.jnotes.dao.remote.CassandraSessionManager;
import com.jc.jnotes.dao.remote.RemoteInstallerDao;
import com.jc.jnotes.dao.remote.RemoteNoteEntryDao;

/**
 * 
 * @author Joy C
 * 
 *
 */
@Component
public class DaoFactory {

    @Autowired
    private UserPreferences userPreferences;
    
    @Autowired
    private CassandraSessionManager cassandraSessionManager;

    // FlyWeight
    private final Map<String, LocalNoteEntryDao> localDaoMap = new HashMap<>();
    private RemoteInstallerDao remoteInstallerDao = null;
    private RemoteNoteEntryDao remoteNoteEntryDao = null;

    public LocalNoteEntryDao getLocalNoteEntryDao() throws IOException {
        String mapKey = userPreferences.getBasePath() + "-" + userPreferences.getCurrentNoteBook();
        if (localDaoMap.get(mapKey) != null) {
            return localDaoMap.get(mapKey);
        } else {
            LocalNoteEntryDao noteEntryDao = new LuceneNoteEntryDao(userPreferences.getBasePath(), APP_NAME,
                    userPreferences.getCurrentNoteBook());
            localDaoMap.put(mapKey, noteEntryDao);
            return noteEntryDao;
        }
    }
    
    public RemoteInstallerDao getRemoteInstallerDao() {
        if(remoteInstallerDao == null) {
            remoteInstallerDao = new CassandraInstallerDao(cassandraSessionManager, userPreferences.getUserId(), userPreferences.getUserSecret());
        }
        return remoteInstallerDao;
    }
    
    public RemoteNoteEntryDao getRemoteNoteEntryDao() {
        if(remoteNoteEntryDao == null) {
            remoteNoteEntryDao = new CassandraNoteEntryDao(cassandraSessionManager, userPreferences.getUserId(), userPreferences.getUserSecret());
        }
        return remoteNoteEntryDao;
    }

}