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
  New note - Ctrl+N  
  Edit note - Ctrl+E
  Edit note in-line - Ctrl+S (after editing a note)  
  Delete note - Ctrl+D  
  Export NoteBook to CSV - Ctrl+X  
  Import CSV/Properties to NoteBook - Ctrl+I  
  
  Ctrl+F -> takes you to search field  
  Ctrl+C -> copies the selected cell value  
  ESC -> Closes diaglogs/popups, clears search  
  
  Quit JNotes - Ctrl+Q  
  
 ## Tech Details:
  - Java 11.0.6  
  - JavaFX (openjfx) + FXML 13 (GUI controls)  
  - Lucene 8.5.0 (Data Access - data is written on your disk)  
  - SceneBuilder 8.5.0 (Design UI)  
  - Maven 3.5.0 (Build)  
  - Eclipse Oxygen 4.7.3a (IDE)  
  - macOS Mojave 10.14.6 (Dev OS - also tested on Windows 10)  
  
 ## Features in progress
  - NoteBook switching (done)  
  - Display info at the bottom (done)
  - Import/Export noteBooks from/to csv  (done)
  - Choice to select base folder for Jnotes (Pending)  
  - Hide password fields from table (Pending)  
  - Add flashcard feature (Pending)
  - Delete multiple notes (Done)
  - Edit and save in-line (Done)  
  
 ## Developer: Joy Chakravarty  
    joy.chakravarty84@gmail.com | https://www.linkedin.com/in/joychak/ 
