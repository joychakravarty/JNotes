package com.jc.jnotes;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.jc.jnotes.dao.DaoConfig;
import com.jc.jnotes.helper.AlertHelper;
import com.jc.jnotes.helper.IOHelper;
import com.jc.jnotes.service.ControllerService;

@Configuration
@Import({DaoConfig.class})
public class AppConfig {
    
    @Bean
    public UserPreferences getUserPreferences() {
        return new UserPreferences();
    }
    
    @Bean
    public AlertHelper getAlertHelper() {
        return new AlertHelper();
    }
    
    @Bean
    public IOHelper getIOHelper() {
        return new IOHelper();
    }
    
    @Bean
    public ControllerService getControllerService() {
        return new ControllerService();
    }
    
}
