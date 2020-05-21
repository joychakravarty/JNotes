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
 *  along with JNotes.  If not, see <https://www.gnu.org/licenses/>.
 * 
 * 
 */
package com.jc.jnotes;

import static com.jc.jnotes.JNotesConstants.APP_NAME;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import com.jc.jnotes.dao.local.LocalNoteEntryDao;
import com.jc.jnotes.dao.local.lucene.LuceneNoteEntryDao;
import com.jc.jnotes.dao.remote.RemoteNoteEntryDao;
import com.jc.jnotes.dao.remote.cassandra.CassandraNoteEntryDao;
import com.jc.jnotes.dao.remote.cassandra.CassandraSessionManager;
import com.jc.jnotes.helper.AlertHelper;
import com.jc.jnotes.helper.IOHelper;
import com.jc.jnotes.service.ControllerService;

/**
 * Custom Application Configuration file.
 * 
 * @author Joy C
 *
 */
public final class AppConfig {

    public static final AppConfig APP_CONFIG = new AppConfig();

    // Flyweight - Cached Prototype Beans
    // Local Dao's per basePath-notebook
    private final static Map<String, LocalNoteEntryDao> LOCAL_DAO_CACHE = new HashMap<>();
    // Remote Dao's per user-secret
    private final static Map<String, RemoteNoteEntryDao> REMOTE_DAO_CACHE = new HashMap<>();

    // Singleton beans
    private final UserPreferences userPreferences;
    private final AlertHelper alertHelper;
    private final IOHelper ioHelper;
    private final ControllerService controllerService;
    private final CassandraSessionManager cassandraSessionManager;
    private final BiConsumer<String, String> localDaoInvalidator;
    private final BiConsumer<String, String> remoteDaoInvalidator;

    private AppConfig() {
        try {
            userPreferences = new UserPreferences();
            alertHelper = new AlertHelper();
            ioHelper = new IOHelper(userPreferences);
            localDaoInvalidator = (basePath, notebook) -> {
                String cacheKey = generateDaoCacheKey(basePath, notebook);
                LOCAL_DAO_CACHE.remove(cacheKey);
            };
            remoteDaoInvalidator = (userId, userSecret) -> {
                String cacheKey = generateDaoCacheKey(userId, userSecret);
                REMOTE_DAO_CACHE.remove(cacheKey);
            };
            controllerService = new ControllerService(userPreferences, localDaoInvalidator, remoteDaoInvalidator, ioHelper);
            cassandraSessionManager = new CassandraSessionManager(JNotesApplication.getResourceAsStream("/cassandra/connection.properties"));
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException("Failed to load up AppConfig for JNotes : " + ex.getMessage());
        }
    }

    private String generateDaoCacheKey(String... keys) {
        return String.join("-", keys);
    }

    public UserPreferences getUserPreferences() {
        return userPreferences;
    }

    public AlertHelper getAlertHelper() {
        return alertHelper;
    }

    public IOHelper getIOHelper() {
        return ioHelper;
    }

    public ControllerService getControllerService() {
        return controllerService;
    }

    // Cached Prototype bean
    public LocalNoteEntryDao getLocalNoteEntryDao(String basePath, String notebook) {
        String cacheKey = generateDaoCacheKey(basePath, notebook);
        if (LOCAL_DAO_CACHE.get(cacheKey) != null) {
            return LOCAL_DAO_CACHE.get(cacheKey);
        } else {
            LocalNoteEntryDao noteEntryDao;
            try {
                noteEntryDao = new LuceneNoteEntryDao(basePath, APP_NAME, notebook);
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
            LOCAL_DAO_CACHE.put(cacheKey, noteEntryDao);
            return noteEntryDao;
        }
    }

    // Cached Prototype bean
    public RemoteNoteEntryDao getRemoteNoteEntryDao(String userId, String userSecret) {
        String cacheKey = generateDaoCacheKey(userId, userSecret);
        if (REMOTE_DAO_CACHE.get(cacheKey) != null) {
            return REMOTE_DAO_CACHE.get(cacheKey);
        } else {
            RemoteNoteEntryDao noteEntryDao = new CassandraNoteEntryDao(cassandraSessionManager, userId, userSecret);
            REMOTE_DAO_CACHE.put(cacheKey, noteEntryDao);
            return noteEntryDao;
        }
    }

    public BiConsumer<String, String> getLocalDaoInvalidator() {
        return localDaoInvalidator;
    }

    public BiConsumer<String, String> getRemoteDaoInvalidator() {
        return remoteDaoInvalidator;
    }

    public CassandraSessionManager getCassandraSessionManager() {
        return cassandraSessionManager;
    }

    /**
     * Do all cleanup activity here
     */
    public void close() {
        cassandraSessionManager.cleanup();
    }

}
