A# JNotes

## Run JNotes
  - Download JNotes  
     For Windows: [JNotes-windows.zip](https://github.com/joychakravarty/JNotes/blob/master/milestone/JNotes-windows.zip)
     For Mac: [JNotes-mac.zip](https://github.com/joychakravarty/JNotes/blob/master/milestone/JNotes-mac.zip)
  - Unzip the .zip file.
  - Double-click the "startJNotes" file.  

## Description:
  **JNotes is a free desktop application which is built for lookup (quick & frequent access) notes.**  
  Now you can view your synced notes here: [jnotes-web](https://jnotes-api.azurewebsites.net/notes)  
  Its perfect for day-to-day lookup notes at work:  
  Title: "shared drive location to success"  Value: "\\Some\impossible\to\remember\path" Content: "release the files when done!" 
    
  Or you can have a notebook for learning Dutch where you store something like word-meaning-setences:  
  Title: "snel"  Value: "fast"  Content: "JNotes is snel -> JNotes is fast"  
    
  As a developer I even have a notebook for my favourite GIT commands and a notebook for my favourite DB queries:  
  Title: "squash"  Value: "git rebase -i HEAD~X"  Content: "use fixup instead of squash". 

## Purpose:
  There are several applications to store Notes, but they are more like "title with body" or "content with multiple tags". JNotes offers one of a kind: "title with value and with content".  
  Usually I used to use OneNote and have a table with 3 columns to store this kind of data but I had the following issues:
  1. I have tons of pages in multiple sections of OneNote, and to access my frequent notes I had to switch from my working page to another page and it was always difficult to return to where I was.           
  2. When sharing my screen my passwords were visible.  
  3. It was difficult to share my notes with others or other devices, or simply export to xls.  
  *But most importantly I wished to build a customizable and dedicated notes application for lookup data.*  
  
## Why JNotes:
  - It's **keyboard friendly**. TIP: Use shortcuts as much as possible, I built it for Keyboard poeple like me. 
  - Lookup table without fast search is no fun, so **JNotes is fast**. Try it.  
  - It's a Java based application (and shipped with lightweight JRE), so you can **run it without any installation**.  
  - You can **store your notes in cloud for free and forever**. You can choose to encrypt your notes while storing in cloud, ensuring your data privacy.      
  - It's **open source** - https://github.com/joychakravarty/JNotes/edit/master/README.md - so fork it and customize it.  

![JNotes Windows](https://github.com/joychakravarty/JNotes/blob/master/WindowsExample.PNG?raw=true)

## Build and run (for Developers)
    ensure JAVA_HOME points to Java verion >=11
    mvn clean install  
    java -jar JNotes.jar   
    OR  
    mvn clean javafx:run (For this, its good to have javafx sdk installed)
     
## Shortcuts: 
    Ctrl+N - New note  
    Ctrl+E - Edit note  
    Ctrl+S - Save note changes. (also for in-line edits)  
    Ctrl+D - Delete selected notes  
    Ctrl+X - Export Notebook to file  
    Ctrl+I - Import file to current Notebook  
    Ctrl+F - Takes you to search field  
    Ctrl+C - Copies the selected cell value  
    Ctrl+Y - Sync settings (with Cloud)  
    ESC    - Closes diaglogs/popups, clears search  
    Ctrl+Q - Quit JNotes  
  
## Tech Details:
  - Java 17  
  - JavaFX (openjfx) + FXML (GUI controls)  
  - Lucene (Data Access - data is written on your disk)  
  - CosmosDB(Cassandra)  
  - JUnit 
  - SpringWeb
  - SceneBuilder (Design UI)  
  - Maven (Build)
  - macOS 12.1 (Dev OS - also tested on Windows 10)  
  
## Features in progress
  - Notebook switching (done)  
  - Display info at the bottom (done)
  - Import/Export notebooks from/to file  (done)
  - Option to store notes in cloud (done)  
  - Delete multiple notes (Done)
  - Edit and save in-line (Done)  
  - Ship with JRE (Done)  
  - Feature to move notes to another notebook (Done)  
  - Choice to select base folder for Jnotes (Pending)  
  - Hide password fields from table (Done)  
  - Add flashcard feature (Pending)  
  - Feel free to email me to request more features.
  
## Developer: Joy Chakravarty  
    joy.chakravarty84@gmail.com | https://www.linkedin.com/in/joychak/ 
 
## License:
    Copyright (C) 2020  Joy Chakravarty [joy.chakravarty84@gmail.com]  

    JNotes is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.  

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.  

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>  
