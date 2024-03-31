::Remove TLSv1.2 when JNotes web is also migrated to Java 17.
start ./jre17/bin/javaw -jar -Dhttps.protocols="TLSv1.2" "%~dp0JNotes.jar" %*