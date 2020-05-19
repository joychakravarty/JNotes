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
package com.jc.jnotes.dao;

import static com.jc.jnotes.JNotesConstants.APP_NAME;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;

import com.jc.jnotes.dao.local.LocalNoteEntryDao;
import com.jc.jnotes.dao.local.lucene.LuceneNoteEntryDao;
import com.jc.jnotes.dao.remote.RemoteNoteEntryDao;
import com.jc.jnotes.dao.remote.cassandra.CassandraNoteEntryDao;
import com.jc.jnotes.dao.remote.cassandra.CassandraSessionManager;

/**
 * 
 * @author Joy C
 * 
 *
 */
@Configuration
@PropertySource("classpath:cassandra.properties")
public class DaoConfig {

    @Bean
    public CassandraSessionManager getCassandraSessionManager() {
        return new CassandraSessionManager();
    }
    
    //Flyweight
    //Local Dao's per basePath-notebook
    private final static Map<String, LocalNoteEntryDao> LOCAL_DAO_CACHE = new HashMap<>();
    //Remote Dao's per user-secret
    private final static Map<String, RemoteNoteEntryDao> REMOTE_DAO_CACHE = new HashMap<>();
    
    private String generateDaoCacheKey(String ... keys) {
        return String.join("-", keys);
    }

    @Bean(name="localNoteEntryDaoFactory")
    public BiFunction<String, String, LocalNoteEntryDao> getLocalNoteEntryDaoFactory() {
        return (basePath, notebook) -> {
            try {
                return getLocalNoteEntryDao(basePath, notebook);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }
    
    @Bean(name="localDaoInvalidator")
    public BiConsumer<String, String> getLocalDaoInvalidator() {
        return (basePath, notebook) -> {
            String cacheKey = generateDaoCacheKey(basePath, notebook);
            LOCAL_DAO_CACHE.remove(cacheKey);
        };
    }
    
    @Bean(name="remoteDaoInvalidator")
    public BiConsumer<String, String> getRemoteDaoInvalidator() {
        return (userId, userSecret) -> {
            String cacheKey = generateDaoCacheKey(userId, userSecret);
            REMOTE_DAO_CACHE.remove(cacheKey);
        };
    }

    @Bean(name="remoteNoteEntryDaoFactory")
    public BiFunction<String, String, RemoteNoteEntryDao> getRemoteNoteEntryDaoFactory() {
        return (userId, userSecret) -> {
            return getRemoteNoteEntryDao(userId, userSecret);
        };
    }

    @Bean
    @Scope(value = "prototype")
    public LocalNoteEntryDao getLocalNoteEntryDao(String basePath, String notebook) throws IOException {
        String cacheKey = generateDaoCacheKey(basePath, notebook);
        if (LOCAL_DAO_CACHE.get(cacheKey) != null) {
            return LOCAL_DAO_CACHE.get(cacheKey);
        } else {
            LocalNoteEntryDao noteEntryDao = new LuceneNoteEntryDao(basePath, APP_NAME, notebook);
            LOCAL_DAO_CACHE.put(cacheKey, noteEntryDao);
            return noteEntryDao;
        }
    }

    @Bean
    @Scope(value = "prototype")
    public RemoteNoteEntryDao getRemoteNoteEntryDao(String userId, String userSecret) {
        String cacheKey = generateDaoCacheKey(userId, userSecret);
        if (REMOTE_DAO_CACHE.get(cacheKey) != null) {
            return REMOTE_DAO_CACHE.get(cacheKey);
        } else {
            RemoteNoteEntryDao noteEntryDao = new CassandraNoteEntryDao(userId, userSecret);
            REMOTE_DAO_CACHE.put(cacheKey, noteEntryDao);
            return noteEntryDao;
        }
    }

}