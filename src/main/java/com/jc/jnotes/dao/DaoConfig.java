package com.jc.jnotes.dao;

import static com.jc.jnotes.JNotesConstants.APP_NAME;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;

import com.jc.jnotes.UserPreferences;
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
    private final Map<String, LocalNoteEntryDao> localDaoMap = new HashMap<>();
    private final Map<String, RemoteNoteEntryDao> remoteDaoMap = new HashMap<>();

    @Bean
    public Function<UserPreferences, LocalNoteEntryDao> localNoteEntryDaoFactory() {
        return userPreferences -> {
            try {
                return getLocalNoteEntryDao(userPreferences);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    @Bean
    public BiFunction<String, String, RemoteNoteEntryDao> remoteNoteEntryDaoFactory() {
        return (userId, userSecret) -> {
            return getRemoteNoteEntryDao(userId, userSecret);
        };
    }

    @Bean
    @Scope(value = "prototype")
    public LocalNoteEntryDao getLocalNoteEntryDao(UserPreferences userPreferences) throws IOException {
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