package com.jc.jnotes;

import static com.jc.jnotes.JNotesConstants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

public class UserPreferencesTest {
    
    UserPreferences userPreferences = new UserPreferences(null);
    
    @Test
    public void testGetUserDetails_NewUser() throws IOException {
        userPreferences.clearUserIdAndUserSecret();
        Path path = Paths.get(userPreferences.getBasePath(), LOCAL_STORE_NAME, ONLINE_SYNC_CONF_FILE);
        Files.delete(path);
        
        String userId = userPreferences.getUserId();
        String userSecret = userPreferences.getUserSecret();
        assertNull(userId);
        assertNull(userSecret);
        
    }
    
    @Test
    public void testGetUserDetails_OldUser() throws IOException {
        userPreferences.clearUserIdAndUserSecret();
        Path path = Paths.get(userPreferences.getBasePath(), LOCAL_STORE_NAME, ONLINE_SYNC_CONF_FILE);
        Files.delete(path);
        
        String userId = userPreferences.getUserId();
        String userSecret = userPreferences.getUserSecret();
        assertNull(userId);
        assertNull(userSecret);
        String testUserId = "testnotes99";
        String testUserSecret = "xxx";
        userPreferences.setUserIdAndSecretForOnlineSync(testUserId, "xxx");
        assertTrue(path.toFile().exists());
        userId = userPreferences.getUserId();
        userSecret = userPreferences.getUserSecret();
        assertEquals(testUserId, userId);
        assertEquals(testUserSecret, userSecret);
        
    }
}
