#**************************************** MAC ****************************************#

#Find which versions are available on your mac
d3m0n@JMac:/usr/libexec$ ./java_home -V

#Ensure you have JDK 17 or higher installed

#We wish to ship lightweight JRE along with JNotes, so get the modules which JNotes depends on
jdeps -s JNotes.jar

#Now create a compact JRE (lightweight) which only has the necessary modules.
d3m0n@JMac:/Library/Java/JavaVirtualMachines/jdk-17.0.2.jdk/Contents/Home/bin$ ./jlink --module-path "../jmods" --add-modules java.base,java.compiler,java.desktop,java.logging,java.management,java.naming,java.prefs,java.scripting,java.security.sasl,java.sql,java.xml,jdk.jfr,jdk.unsupported --output ~/jre17 --strip-debug --compress 2 --no-header-files --no-man-page

#**************************************** WINDOWS ****************************************#