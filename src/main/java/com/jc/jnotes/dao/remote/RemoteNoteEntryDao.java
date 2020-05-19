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
package com.jc.jnotes.dao.remote;

import java.util.List;
import java.util.Map;

import com.jc.jnotes.dao.NoteEntryDao;
import com.jc.jnotes.model.NoteEntry;

/**
 * Remote version of NoteEntryDao should also provide functionality of backup and restore(getAll)
 * along with CRUD.
 * 
 * @author Joy C
 *
 */
public interface RemoteNoteEntryDao extends NoteEntryDao {
    
    boolean setupUser(String userId);

    boolean backup(String notebook, List<NoteEntry> notes);

    void disconnect();
    
    void renameNotebook(String notebookToBeRenamed, String newNotebookName);
    
    void deleteNotebook(String notebookToBeDeleted);

    /**
     * 
     * @return 0 - success
     *  <br/>1 - userId does not exist 
     *  <br/>2 - userSecret is not correct
     */
    int validateUserSecret();

    Map<String, List<NoteEntry>> restore();

}