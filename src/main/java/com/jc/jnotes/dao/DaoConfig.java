package com.jc.jnotes.dao;

import static com.jc.jnotes.JNotesConstants.APP_NAME;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
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

    // FlyWeight
    public final static Map<String, LocalNoteEntryDao> localDaoMap = new HashMap<>();
    public final static Map<String, RemoteNoteEntryDao> remoteDaoMap = new HashMap<>();

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

    @Bean(name="remoteNoteEntryDaoFactory")
    public BiFunction<String, String, RemoteNoteEntryDao> getRemoteNoteEntryDaoFactory() {
        return (userId, userSecret) -> {
            return getRemoteNoteEntryDao(userId, userSecret);
        };
    }

    @Bean
    @Scope(value = "prototype")
    public LocalNoteEntryDao getLocalNoteEntryDao(String basePath, String notebook) throws IOException {
        String mapKey = basePath + "-" + notebook;
        if (localDaoMap.get(mapKey) != null) {
            return localDaoMap.get(mapKey);
        } else {
            LocalNoteEntryDao noteEntryDao = new LuceneNoteEntryDao(basePath, APP_NAME, notebook);
            localDaoMap.put(mapKey, noteEntryDao);
            return noteEntryDao;
        }
    }

    @Bean
    @Scope(value = "prototype")
    public RemoteNoteEntryDao getRemoteNoteEntryDao(String userId, String userSecret) {
        String mapKey = userId + "-" + userSecret;
        if (remoteDaoMap.get(mapKey) != null) {
            return remoteDaoMap.get(mapKey);
        } else {
            RemoteNoteEntryDao noteEntryDao = new CassandraNoteEntryDao(userId, userSecret);
            remoteDaoMap.put(mapKey, noteEntryDao);
            return noteEntryDao;
        }
    }

}