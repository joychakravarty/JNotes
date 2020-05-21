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
package com.jc.jnotes.model;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * 
 * Model class used by both JavaFX UI and also by DAO classes.
 * 
 * @author Joy C
 *
 */
public class NoteEntry implements Comparable<NoteEntry> {

    public static final String ID_COL_NAME = "id";
    public static final String KEY_COL_NAME = "key";
    public static final String VALUE_COL_NAME = "value";
    public static final String INFO_COL_NAME = "info";
    public static final String PASSWORD_FLAG_COL_NAME = "passwordFlag";
    public static final String LAST_MODIFIED_TIME_COL_NAME = "lastModifiedTime";

    private static Comparator<String> nullSafeStringComparator = Comparator.nullsFirst(String::compareToIgnoreCase);

    private static Comparator<NoteEntry> keyComparator = Comparator.comparing(NoteEntry::getKey, nullSafeStringComparator);

    private final String id;
    private StringProperty key;
    private StringProperty value;
    private StringProperty info;
    private StringProperty passwordFlag;
    private ObjectProperty<LocalDateTime> lastModifiedTime;

    // Only for Remote Dao
    private String notebook;

    public String getNotebook() {
        return notebook;
    }

    public void setNotebook(String notebook) {
        this.notebook = notebook;
    }

    public static String generateID() {
        return UUID.randomUUID().toString();
    }

    public NoteEntry(String id, String key, String value, String info, String passwordFlag) {
        this(id, key, value, info, passwordFlag, LocalDateTime.now());
    }

    public NoteEntry(String id, String key, String value, String info, String passwordFlag, LocalDateTime lastModifiedTime) {
        if (id == null) {
            throw new IllegalArgumentException("NoteEntry id cannot be null");
        }
        this.id = id;
        this.key = new SimpleStringProperty(key == null ? StringUtils.EMPTY : key);
        this.value = new SimpleStringProperty(value == null ? StringUtils.EMPTY : value);
        this.info = new SimpleStringProperty(info == null ? StringUtils.EMPTY : info);
        this.passwordFlag = new SimpleStringProperty(passwordFlag == null ? "N" : passwordFlag);
        this.lastModifiedTime = new SimpleObjectProperty<LocalDateTime>(lastModifiedTime);
    }

    public String getId() {
        return id;
    }

    public String getKey() {
        return key.get();
    }

    public void setKey(String key) {
        this.key.set(key);
        this.lastModifiedTime.set(LocalDateTime.now());
    }

    public StringProperty keyProperty() {
        return key;
    }

    public String getValue() {
        return value.get();
    }
    
    public String getDisplayValue() {
        if("Y".equals(passwordFlag.get())) {
            return "*".repeat(value.get().length());
        }
        return value.get();
    }

    public void setValue(String value) {
        this.value.set(value);
        this.lastModifiedTime.set(LocalDateTime.now());
    }

    public StringProperty valueProperty() {
        return value;
    }

    public String getInfo() {
        return info.get();
    }

    public void setInfo(String info) {
        this.info.set(info);
        this.lastModifiedTime.set(LocalDateTime.now());
    }

    public StringProperty infoProperty() {
        return info;
    }

    public String getPasswordFlag() {
        return passwordFlag.get();
    }

    public void setPasswordFlag(String passwordFlag) {
        this.passwordFlag.set(passwordFlag);
        this.lastModifiedTime.set(LocalDateTime.now());
    }

    public StringProperty passwordFlagProperty() {
        return passwordFlag;
    }

    public LocalDateTime getLastModifiedTime() {
        return lastModifiedTime.get();
    }

    public ObjectProperty<LocalDateTime> lastModifiedTimeProperty() {
        return lastModifiedTime;
    }

    public void setLastModifiedTime(LocalDateTime lastModifiedTime) {
        this.lastModifiedTime.set(lastModifiedTime);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        NoteEntry other = (NoteEntry) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return String.format("ID: [%s] Key: [%s] Value: [%s] Info[%s] PasswordFlag[%s]", id, key, value, info, passwordFlag);
    }

    @Override
    public int compareTo(NoteEntry other) {
        return keyComparator.compare(this, other);
    }

}