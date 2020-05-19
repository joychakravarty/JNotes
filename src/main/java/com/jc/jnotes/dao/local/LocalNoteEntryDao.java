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
package com.jc.jnotes.dao.local;

import java.io.IOException;
import java.util.List;

import com.jc.jnotes.dao.NoteEntryDao;
import com.jc.jnotes.model.NoteEntry;

/**
 * Local version of NoteEntryDao should also provide functionality of Search along with CRUD.
 * 
 * @author Joy C
 *
 */
public interface LocalNoteEntryDao  extends NoteEntryDao {

    List<NoteEntry> searchNotes(String searchParam, boolean searchInfo) throws IOException;

}