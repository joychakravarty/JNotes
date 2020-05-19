# JNotes

## Description:
  This is a desktop application which allows quick-access/lookup to your key-value-info type of data.  
  Example:  
  Key: "Prd hostname"  Value: "yxz123"  Info: ""    
  Key: "Acceptance Webapp url"  Value: "https://accServer:8445/mywebapp"  Info: "notEncryptedNotImportantPassword". 
  
  (Or you may want to have a notebook for learning Dutch language like word-meaning-setences). 
  Key: "snel"  Value: "fast"  Info: "JNotes is fast \n JNotes is snel". 

## Purpose:
  There are several application to store Notes but they are more like "title-body" or "content-multiple tags". JNotes offers one of a kind: "key-value-content".  
  Usually I use OneNote and have a table to store this kind of data but I use OneNote for other daily activity as well like jotting down my analysis of a Prod Issue, during this time I do not wish to move away from that page to find my "Acceptance Webapp url", this is why I built JNotes, its my lookup table!   
  
## Why JNotes:
  - It's keyboard friendly. TIP: Use shortcuts as much as possible, I built it for Keyboard poeple like me. 
  - Lookup table without fast search is no fun, so JNotes is quick. Try it.  
  - It's a Java based application, so if you have Java 11 or higher you can simply run the .jar without any installation.  
  - You can store your notes in cloud for free and forever. You can choose to encrypt your notes while storing in cloud, making it secure ensuring privacy.    
  - Its open source - https://github.com/joychakravarty/JNotes/edit/master/README.md - so fork it and customize it.  
 
 ## Run JNotes
  - Install Java 11 or above (verify using java -version)  
  - Download the uber jar JNotes-YourOS.jar from the milestone folder based on your OS.  
  - Run java -jar JNotes-YourOS.jar  
   
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
  - Cassandra (Astra by Datastax)  
  - JUnit 5.2  
  - Spring 5.2.2
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
  - Hide password fields from table (Pending)  
  - Add flashcard feature (Pending)  
  - Feel free to email me to request more features.
  
 ## Developer: Joy Chakravarty  
    joy.chakravarty84@gmail.com | https://www.linkedin.com/in/joychak/ 
