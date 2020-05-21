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
package com.jc.jnotes.dao.remote.cassandra;

import static com.jc.jnotes.AppConfig.APP_CONFIG;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.jc.jnotes.AppConfig;

/**
 * 
 * @author Joy C
 *
 */
public class CassandraNoteEntryDaoTest {
    
    private CassandraSessionManager sessionManager = AppConfig.APP_CONFIG.getCassandraSessionManager();
    
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
        boolean returnStatus = APP_CONFIG.getRemoteNoteEntryDao(TEST_USER_ID, TEST_USER_SECRET).setupUser(TEST_USER_ID);
        
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
        
        int status = APP_CONFIG.getRemoteNoteEntryDao(TEST_USER_ID, TEST_USER_SECRET).validateUserSecret();
        assertEquals(0, status, "Secret should have matched");
    }
    
    @Test
    public void testSetupUser_ExistingUser() {
        boolean returnStatus = APP_CONFIG.getRemoteNoteEntryDao(TEST_USER_ID, TEST_USER_SECRET).setupUser(TEST_USER_ID);
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
