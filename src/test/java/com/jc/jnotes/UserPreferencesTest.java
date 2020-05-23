package com.jc.jnotes;

import static com.jc.jnotes.JNotesConstants.APP_NAME;
import static com.jc.jnotes.JNotesConstants.ONLINE_SYNC_CONF_FILE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

public class UserPreferencesTest {
    
    UserPreferences preferences = new UserPreferences();
    
    @Test
    public void testGetUserDetails_NewUser() throws IOException {
        preferences.clearUserIdAndUserSecret();
        Path path = Paths.get(preferences.getBasePath(), APP_NAME, ONLINE_SYNC_CONF_FILE);
        Files.delete(path);
        
        String userId = preferences.getUserId();
        String userSecret = preferences.getUserSecret();
        assertNull(userId);
        assertNull(userSecret);
        
    }
    
    @Test
    public void testGetUserDetails_OldUser() throws IOException {
        preferences.clearUserIdAndUserSecret();
        Path path = Paths.get(preferences.getBasePath(), APP_NAME, ONLINE_SYNC_CONF_FILE);
        Files.delete(path);
        
        String userId = preferences.getUserId();
        String userSecret = preferences.getUserSecret();
        assertNull(userId);
        assertNull(userSecret);
        String testUserId = "testnotes99";
        String testUserSecret = "xxx";
        preferences.setUserIdAndSecretForOnlineSync(testUserId, "xxx");
        assertTrue(path.toFile().exists());
        userId = preferences.getUserId();
        userSecret = preferences.getUserSecret();
        assertEquals(testUserId, userId);
        assertEquals(testUserSecret, userSecret);
        
    }
}
