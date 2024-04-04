#!/bin/bash
set -eu # exit on any error or any unset variable

# build the project
javac -Xlint:all,-serial \
      --release 21 \
      -d bin \
      src/SensorPanel.java

# create the jar file
jar cf bin/SensorPanel.jar $(find bin -type f -name "*.class")

# remove the class files
rm -rf bin/src

# inform the user of successful compilation
echo "Compilation successful"
