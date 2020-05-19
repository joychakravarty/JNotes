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

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.jc.jnotes.dao.DaoConfig;
import com.jc.jnotes.helper.AlertHelper;
import com.jc.jnotes.helper.IOHelper;
import com.jc.jnotes.service.ControllerService;

@Configuration
@Import({ DaoConfig.class })
/**
 * The main Spring Application Configuration file.
 * 
 * @author Joy C
 *
 */
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
