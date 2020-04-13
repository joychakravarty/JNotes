package com.jc.jnotes.model;

import java.util.Comparator;

import org.apache.commons.lang3.StringUtils;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;


/**
 * 
 * @author Joy C
 *
 */
public class NoteEntry implements Comparable<NoteEntry> {
    
    private static Comparator<String> nullSafeStringComparator = Comparator
            .nullsFirst(String::compareToIgnoreCase); 

    private static Comparator<NoteEntry> noteEntryComparator = Comparator
            .comparing(NoteEntry::getKey, nullSafeStringComparator);

    private final String id;
    private StringProperty key;
    private StringProperty value;
    private StringProperty info;
    //private final ObjectProperty<LocalDateTime> lastModifiedTime;
    
    public NoteEntry(String id, String key, String value, String info){
        if(id==null){
            throw new IllegalArgumentException("NoteEntry id cannot be null");
        }
        this.id=id;
        this.key = new SimpleStringProperty(key==null?StringUtils.EMPTY:key);
        this.value = new SimpleStringProperty(value==null?StringUtils.EMPTY:value);
        this.info = new SimpleStringProperty(info==null?StringUtils.EMPTY:info);
    }
    
    public String getId(){
        return id;
    }

    public String getKey() {
        return key.get();
    }
    
    public void setKey(String key) {
        this.key.set(key);
    }

    public StringProperty keyProperty() {
        return key;
    }

    public String getValue() {
        return value.get();
    }

    public void setValue(String value) {
        this.value.set(value);
    }

    public StringProperty valueProperty() {
        return value;
    }
    
    public String getInfo() {
        return info.get();
    }

    public void setInfo(String info) {
        this.info.set(info);
    }
    
    public StringProperty infoProperty() {
        return info;
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
        return noteEntryComparator.compare(this, other);
    }
    
}