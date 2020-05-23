# JNotes

## Description:
  JNotes is a desktop application which allows quick-access/lookup to your key-value-info type of data.  
  Example:  
  Its perfect for day-to-day loopup notes at work  
  Key: "shared drive location to success"  Value: "\\Some\impossible\to\remember\path" Info: "release the files when done!" 
    
  Or you have a notebook for learning Dutch and you can store something like word-meaning-setences  
  Key: "snel"  Value: "fast"  Info: "JNotes is snel -> JNotes is fast"  
    
  As a developer I even have a notebook for my favourite GIT commands and a notebook for my favourite DB queries 
  Key: "squash"  Value: "git rebase -i HEAD~X"  Info: "use fixup instead of squash". 

## Purpose:
  There are several application to store Notes but they are more like "title-body" or "content-multiple tags". JNotes offers one of a kind: "key-value-content".  
  Usually I used to use OneNote and have a table to store this kind of data but I use OneNote for other daily activity as well like jotting down my analysis of a Prod Issue, during this time I do not wish to move away from that page just to find some frequently used password, this is why I built JNotes, its my lookup table!   
  
## Why JNotes:
  - It's keyboard friendly. TIP: Use shortcuts as much as possible, I built it for Keyboard poeple like me. 
  - Lookup table without fast search is no fun, so JNotes is quick. Try it.  
  - It's a Java based application, so if you have Java 11 or higher you can simply run the .jar without any installation.  
  - You can store your notes in cloud for free and forever. You can choose to encrypt your notes while storing in cloud, ensuring your data privacy.      
  - Its open source - https://github.com/joychakravarty/JNotes/edit/master/README.md - so fork it and customize it.  
 
 ## Run JNotes
  - Install Java 11 or above (verify using java -version)  
  - Download the uber jar JNotes-YourOS.jar from the milestone folder based on your OS.  
  - Run "java -jar JNotes-YourOS.jar" or simply double click the Jar
  
  ###### Detailed instructions for Windows users:  
    - Download Java from https://download.java.net/java/GA/jdk14.0.1/664493ef4a6946b186ff29eb326336a2/7/GPL/openjdk-14.0.1_windows-x64_bin.zip  
    - Move the downloaded zip from your Downloads folder to C:\Users\yourUserName\ and Unzip it there.  
    - Download https://github.com/joychakravarty/JNotes/blob/master/milestone/JNotes-windows.jar and also place it in C:\Users\yourUserName\  
    - On your desktop create a txt file called startJNotes.txt, edit and write:  
        C:\Users\yourUserName\jdk-14.0.1\bin\java -jar C:\Users\yourUserName\JNotes-windows.jar   
    - Rename startJNotes.txt to startJNotes.bat and double click the .bat file.  
 
 ## Build and run (for Developers)
    mvn clean install  
    java -jar JNotes.jar   
    OR  
    mvn clean javafx:run (For this, its good to have javafx sdk installed)
     
 ## Shortcuts: 
    Ctrl+N - New note  
    Ctrl+E - Edit note  
    Ctrl+S - Save note changes. (also for in-line edits)  
    Ctrl+D - Delete selected notes  
    Ctrl+X - Export Notebook to CSV  
    Ctrl+I - Import CSV/Properties to current Notebook  
    Ctrl+F - Takes you to search field  
    Ctrl+C - Copies the selected cell value  
    Ctrl+Y - Sync settings (with Cloud)  
    ESC    - Closes diaglogs/popups, clears search  
    Ctrl+Q - Quit JNotes  
  
 ## Tech Details:
  - Java 11.0.6  
  - JavaFX (openjfx) + FXML 13 (GUI controls)  
  - Lucene 8.5.0 (Data Access - data is written on your disk)  
  - Cassandra (Astra by Datastax - 6.8.0)  
  - JUnit 5.5.1  
  - Spring 5.2.2 [core removed - only used crypto module]
  - SceneBuilder 8.5.0 (Design UI)  
  - Maven 3.5.0 (Build)  
  - Eclipse Oxygen 4.7.3a (IDE)  
  - macOS Mojave 10.14.6 (Dev OS - also tested on Windows 10)  
  
 ## Features in progress
  - Notebook switching (done)  
  - Display info at the bottom (done)
  - Import/Export notebooks from/to csv  (done)
  - Option to store notes in cloud (done)  
  - Delete multiple notes (Done)
  - Edit and save in-line (Done)  
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
