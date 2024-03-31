#!/bin/bash
cd "`dirname "$0"`"

#Remove TLSv1.2 when JNotes web is also migrated to Java 17.
./jre17/bin/java -jar -Dhttps.protocols="TLSv1.2" JNotes.jar