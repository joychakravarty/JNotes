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
 * @author Joy C
 *
 */
public class NoteEntry implements Comparable<NoteEntry> {
    
    public static final String ID_COL_NAME = "id";
    public static final String KEY_COL_NAME = "key";
    public static final String VALUE_COL_NAME = "value";
    public static final String INFO_COL_NAME = "info";
    public static final String LAST_MODIFIED_TIME_COL_NAME = "lastModifiedTime";
    
    private static Comparator<String> nullSafeStringComparator = Comparator
            .nullsFirst(String::compareToIgnoreCase); 

    private static Comparator<NoteEntry> keyComparator = Comparator
            .comparing(NoteEntry::getKey, nullSafeStringComparator);

    private final String id;
    private StringProperty key;
    private StringProperty value;
    private StringProperty info;
    private ObjectProperty<LocalDateTime> lastModifiedTime;
    
    public static String generateID() {
        return UUID.randomUUID().toString();
    }
    
    public NoteEntry(String id, String key, String value, String info){
        this(id, key, value, info, LocalDateTime.now());
    }
    
    public NoteEntry(String id, String key, String value, String info, LocalDateTime lastModifiedTime){
        if(id==null){
            throw new IllegalArgumentException("NoteEntry id cannot be null");
        }
        this.id=id;
        this.key = new SimpleStringProperty(key==null?StringUtils.EMPTY:key);
        this.value = new SimpleStringProperty(value==null?StringUtils.EMPTY:value);
        this.info = new SimpleStringProperty(info==null?StringUtils.EMPTY:info);
        this.lastModifiedTime = new SimpleObjectProperty<LocalDateTime>(lastModifiedTime);
    }
    
    public String getId(){
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

    public LocalDateTime getLastModifiedTime() {
        return lastModifiedTime.get();
    }
    
    public ObjectProperty<LocalDateTime>  lastModifiedTimeProperty(){
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
    public String toString(){
        return String.format("ID: [%s] Key: [%s] Value: [%s] Info[%s]", id, key, value, info);
    }

    @Override
    public int compareTo(NoteEntry other) {
        return keyComparator.compare(this, other);
    }
    
}