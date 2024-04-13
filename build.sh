#!/bin/bash
set -eu # exit on any error or any unset variable

# build the project
javac -Xlint:all,-serial \
      --release 21 \
      -d bin \
      $(find src -type f -name "*.java")

# create the jar file
jar cfe bin/SensorPanel.jar src.SensorPanel -C bin src

# remove the class files
rm -rf bin/src

# inform the user of successful compilation
echo "Compilation successful"
