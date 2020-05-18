package com.jc.jnotes.dao.remote.cassandra;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.BiFunction;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.jc.jnotes.AppConfig;
import com.jc.jnotes.dao.remote.RemoteNoteEntryDao;
import com.jc.jnotes.util.EncryptionUtil;

/**
 * 
 * @author Joy C
 *
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { AppConfig.class })
public class CassandraNoteEntryDaoTest {
    
    @Autowired
    private CassandraSessionManager sessionManager;

    @Autowired
    @Qualifier("remoteNoteEntryDaoFactory")
    private BiFunction<String, String, RemoteNoteEntryDao> remoteNoteEntryDaoFactory;

    public RemoteNoteEntryDao getRemoteNoteEntryDao(String userId, String userSecret) {
        return remoteNoteEntryDaoFactory.apply(userId, userSecret);
    }
    
    private static final String TEST_USER_ID = "jnotes_testuser";
    private static final String TEST_USER_SECRET = "jnotes_testsecret";
   
    /**
     * First run this has privileged user:
     * drop table if exists jnotes_testuser;
     * drop table if exists jnotes_testuser_secret_validation;
     */
    @Disabled("Enable only after dropping the test tables")
    @Test
    public void testSetupUser_NewUser() {
        boolean returnStatus = getRemoteNoteEntryDao(TEST_USER_ID, TEST_USER_SECRET).setupUser(TEST_USER_ID);
        
        assertTrue(returnStatus, "Setup new user should have been successful");
        
        
        ResultSet results = sessionManager.getClientSession()
                .execute(SimpleStatement.builder("SELECT * from "+ TEST_USER_ID).build());

        assertNotNull(results, TEST_USER_ID + " table should have been created");
        Row row = results.one();
        assertNull(row, "There shouldnt be any data in this newly created user table");
        
        results = sessionManager.getClientSession()
                .execute(SimpleStatement.builder("SELECT * from "+ TEST_USER_ID+"_secret_validation").build());
        assertNotNull(results, TEST_USER_ID+"_secret_validation" + " table should have been created");
        row = results.one();
        assertNotNull(row, "There should be 1 record in the table with encrypted validationText");
        
        int status = getRemoteNoteEntryDao(TEST_USER_ID, TEST_USER_SECRET).validateUserSecret();
        assertEquals(0, status, "Secret should have matched");
    }
    
    @Test
    public void testSetupUser_ExistingUser() {
        boolean returnStatus = getRemoteNoteEntryDao(TEST_USER_ID, TEST_USER_SECRET).setupUser(TEST_USER_ID);
        assertFalse(returnStatus, "Setup new user should have been successful");
    }
    

//
//    @Test
//    public void testSelectAllFromAdminCreatedTable() {
//
//        assertThrows(UnauthorizedException.class, () -> {
//            sessionManager.getClientSession().execute(SimpleStatement.builder("SELECT * from usersecret_validation").build());
//        });
//
//    }
    
  

}
