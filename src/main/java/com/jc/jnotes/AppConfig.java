package com.jc.jnotes;

import java.net.URL;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import com.jc.jnotes.dao.DaoFactory;
import com.jc.jnotes.dao.remote.CassandraSessionManager;
import com.jc.jnotes.helper.AlertHelper;
import com.jc.jnotes.helper.EncryptionHelper;
import com.jc.jnotes.helper.IOHelper;
import com.jc.jnotes.service.ControllerService;

@Configuration
@PropertySource("classpath:application.properties")
public class AppConfig {
    
    @Value( "${cassandra.security_bundle}" )
    private String securityBundle;
    
    @Bean
    public UserPreferences getUserPreferences() {
        return new UserPreferences();
    }
    
    @Bean
    public AlertHelper getAlertHelper() {
        return new AlertHelper();
    }
    
    @Bean
    public EncryptionHelper getEncryptionHelper() {
        return new EncryptionHelper();
    }
    
    @Bean
    public IOHelper getIOHelper() {
        return new IOHelper();
    }
    
    @Bean
    public URL getSecureConnectBundleURL() {
        return JNotesApplication.getResource(securityBundle);
    }
    
    @Bean
    public CassandraSessionManager getCassandraSessionManager() {
        return new CassandraSessionManager();
    }
    
    @Bean
    public DaoFactory getDaoFactory() {
        return new DaoFactory();
    }
    
    @Bean
    public ControllerService getControllerService() {
        return new ControllerService();
    }
    
}
